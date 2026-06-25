"""규칙 기반 행동 분류.

YOLO pose 가 추출한 프레임별 keypoints 를 입력으로 받아
'자리이탈' / '손흔들기' / '미분류' 중 하나로 분류한다.

설계 원칙:
- 사람이 없는 프레임, keypoint 가 비어 있는 프레임에서도 절대 예외를 던지지 않는다.
- confidence 가 낮은 keypoint 는 신뢰하지 않는다 (KP_CONF_MIN).
- 좌표는 프레임 크기 / 어깨 너비로 정규화해 영상 해상도·피사체 크기에 둔감하게 만든다.
"""

from __future__ import annotations

import math

# --- COCO keypoint 인덱스 (YOLO pose) ---
LEFT_SHOULDER, RIGHT_SHOULDER = 5, 6
LEFT_WRIST, RIGHT_WRIST = 9, 10

# confidence 가 이 값 미만인 keypoint 는 신뢰하지 않는다.
KP_CONF_MIN = 0.5

# --- 자리이탈 임계값 ---
DEPARTURE_MOVE_RATIO = 0.22   # (초반 중심 → 후반 중심) 이동량 / 프레임 대각선
EDGE_MARGIN_RATIO = 0.08      # 화면 가장자리로 이 비율 이내까지 들어오면 이탈로 본다.
DEPARTURE_MIN_SCORE = 0.6

# --- 손흔들기 임계값 ---
WAVE_MIN_DIRECTION_CHANGES = 3    # 손목 좌우 방향 전환 횟수
WAVE_MIN_AMPLITUDE_RATIO = 0.15   # 손목 좌우 진폭 / 어깨 너비
WAVE_MIN_SCORE = 0.6

# --- 미분류 기본 응답 ---
_UNCLASSIFIED = {
    "detectedBehavior": "미분류",
    "confidence": 0.0,
    "draftText": "특이 행동이 감지되지 않았습니다.",
    "behaviorReason": "자리이탈/손흔들기 기준에 명확히 해당하지 않습니다.",
}


def _valid(kp: dict | None) -> bool:
    return (
        kp is not None
        and kp.get("confidence") is not None
        and kp["confidence"] >= KP_CONF_MIN
    )


def _primary_person(frame: dict) -> dict | None:
    """프레임 내 사람 중 box_confidence 가 가장 높은 사람을 주 대상으로 선택."""
    persons = frame.get("persons") or []
    if not persons:
        return None
    return max(persons, key=lambda p: (p.get("box_confidence") or 0.0))


def _kp(person: dict, idx: int) -> dict | None:
    kpts = person.get("keypoints") or []
    if idx < len(kpts) and _valid(kpts[idx]):
        return kpts[idx]
    return None


def _person_center(person: dict) -> tuple[float, float] | None:
    """신뢰 가능한 keypoint 들의 평균 좌표. 신뢰 keypoint 가 없으면 None."""
    kpts = person.get("keypoints") or []
    pts = [(k["x"], k["y"]) for k in kpts if _valid(k)]
    if not pts:
        return None
    n = len(pts)
    return sum(p[0] for p in pts) / n, sum(p[1] for p in pts) / n


def _shoulder_width(person: dict) -> float | None:
    ls, rs = _kp(person, LEFT_SHOULDER), _kp(person, RIGHT_SHOULDER)
    if ls and rs:
        return math.hypot(ls["x"] - rs["x"], ls["y"] - rs["y"])
    return None


def _avg(points: list[tuple[float, float]]) -> tuple[float, float]:
    n = len(points)
    return sum(p[0] for p in points) / n, sum(p[1] for p in points) / n


def _extract(frame: dict) -> dict:
    """프레임에서 분류에 필요한 값만 뽑아낸다 (사람 없으면 present=False)."""
    person = _primary_person(frame)
    if person is None:
        return {"present": False, "center": None, "lw": None, "rw": None,
                "ls": None, "rs": None, "sw": None}
    return {
        "present": True,
        "center": _person_center(person),
        "lw": _kp(person, LEFT_WRIST),
        "rw": _kp(person, RIGHT_WRIST),
        "ls": _kp(person, LEFT_SHOULDER),
        "rs": _kp(person, RIGHT_SHOULDER),
        "sw": _shoulder_width(person),
    }


def _direction_changes(xs: list[float]) -> int:
    """시계열의 진행 방향(증가/감소)이 바뀐 횟수. 작은 노이즈는 무시."""
    if len(xs) < 2:
        return 0
    span = max(xs) - min(xs)
    thresh = span * 0.1 if span else 0.0
    diffs = [b - a for a, b in zip(xs, xs[1:])]
    significant = [d for d in diffs if abs(d) > thresh]
    changes = 0
    for a, b in zip(significant, significant[1:]):
        if (a > 0) != (b > 0):
            changes += 1
    return changes


def _departure_score(samples, diag, width, height) -> tuple[float, str]:
    """자리이탈 점수와 근거. 여러 신호 중 가장 강한 것을 채택."""
    candidates: list[tuple[float, str]] = []
    centers = [s["center"] for s in samples if s["center"]]
    n = len(samples)

    # (a) 초반엔 감지되던 사람이 후반에 사라짐
    if n >= 3:
        first_present = any(s["present"] for s in samples[: max(1, n // 2)])
        tail = samples[-max(2, n // 3):]
        tail_ratio = sum(1 for s in tail if s["present"]) / len(tail)
        if first_present and tail_ratio <= 0.2:
            candidates.append((0.85, "초반에 감지되던 사람이 영상 후반에 사라졌습니다."))

    # (b) 초반 대비 후반 중심점 이동량
    if diag and len(centers) >= 2:
        k = max(1, len(centers) // 3)
        ex, ey = _avg(centers[:k])
        lx, ly = _avg(centers[-k:])
        move = math.hypot(lx - ex, ly - ey) / diag
        if move >= DEPARTURE_MOVE_RATIO:
            score = min(0.6 + (move - DEPARTURE_MOVE_RATIO) * 2.0, 0.95)
            candidates.append(
                (score, f"초반 대비 사람 중심점이 크게 이동했습니다 (이동 비율 {move:.2f}).")
            )

    # (c) 후반 중심점이 화면 가장자리로 벗어남
    if width and height and centers:
        cx, cy = centers[-1]
        mx, my = width * EDGE_MARGIN_RATIO, height * EDGE_MARGIN_RATIO
        if cx <= mx or cx >= width - mx or cy >= height - my:
            candidates.append((0.7, "사람 중심점이 화면 가장자리로 벗어났습니다."))

    return max(candidates, default=(0.0, ""))


def _wave_score(samples) -> tuple[float, str]:
    """손흔들기 점수와 근거. 좌/우 손목 중 더 강한 신호를 채택."""
    best: tuple[float, str] = (0.0, "")
    for side, wkey in (("왼손", "lw"), ("오른손", "rw")):
        xs: list[float] = []          # 어깨 중심 기준 손목의 상대 x
        amp_refs: list[float] = []     # 정규화용 어깨 너비
        raised = total = 0
        for s in samples:
            w, ls, rs = s[wkey], s["ls"], s["rs"]
            if not w or not (ls or rs):
                continue
            if ls and rs:
                shoulder_cx = (ls["x"] + rs["x"]) / 2
                shoulder_y = (ls["y"] + rs["y"]) / 2
            else:
                sh = ls or rs
                shoulder_cx, shoulder_y = sh["x"], sh["y"]
            xs.append(w["x"] - shoulder_cx)
            total += 1
            if w["y"] < shoulder_y:   # 이미지 좌표는 아래로 갈수록 y 증가 → 손목이 어깨보다 위
                raised += 1
            if s["sw"]:
                amp_refs.append(s["sw"])

        if len(xs) < 4:
            continue
        changes = _direction_changes(xs)
        amplitude = max(xs) - min(xs)
        sw = sum(amp_refs) / len(amp_refs) if amp_refs else None
        amp_ratio = amplitude / sw if sw else 0.0
        raised_ratio = raised / total if total else 0.0

        if changes >= WAVE_MIN_DIRECTION_CHANGES and amp_ratio >= WAVE_MIN_AMPLITUDE_RATIO:
            score = min(0.5 + 0.1 * changes + 0.3 * raised_ratio, 0.95)
            if score > best[0]:
                best = (
                    score,
                    f"{side} 손목이 좌우로 {changes}회 반복 이동했습니다 "
                    f"(진폭/어깨너비 {amp_ratio:.2f}).",
                )
    return best


def classify_behavior(frames, frame_width=None, frame_height=None) -> dict:
    """프레임별 keypoints 로부터 행동을 분류해 최상위 응답 필드 dict 를 만든다.

    반환 키: detectedBehavior, confidence, draftText, behaviorReason
    """
    result = dict(_UNCLASSIFIED)

    if not frames:
        result["behaviorReason"] = "분석된 프레임이 없습니다."
        return result

    samples = [_extract(fr) for fr in frames]
    if not any(s["present"] for s in samples):
        result["behaviorReason"] = "사람이 감지된 프레임이 없습니다."
        return result

    diag = (
        math.hypot(frame_width, frame_height)
        if frame_width and frame_height
        else None
    )
    dep_score, dep_reason = _departure_score(samples, diag, frame_width, frame_height)
    wave_score, wave_reason = _wave_score(samples)

    candidates = []
    if dep_score >= DEPARTURE_MIN_SCORE:
        candidates.append(
            ("자리이탈", dep_score, "학생이 자리에서 이탈한 것으로 감지되었습니다.", dep_reason)
        )
    if wave_score >= WAVE_MIN_SCORE:
        candidates.append(
            ("손흔들기", wave_score, "학생이 손을 흔드는 행동이 감지되었습니다.", wave_reason)
        )

    if not candidates:
        return result

    behavior, score, draft, reason = max(candidates, key=lambda c: c[1])
    return {
        "detectedBehavior": behavior,
        "confidence": round(score, 2),
        "draftText": draft,
        "behaviorReason": reason,
    }
