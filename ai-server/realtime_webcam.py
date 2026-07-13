"""로컬 웹캠 실시간 행동 분류 데모.

- 노트북 웹캠을 OpenCV 로 열어 프레임을 읽는다.
- YOLO pose 로 프레임별 keypoint 를 추출한다.
- 최근 프레임들을 모아 behavior.py 의 classify_behavior 로 행동을 분류한다.
- 화면에 detectedBehavior / confidence / behaviorReason 를 오버레이한다.
- q 키를 누르면 종료한다.
- 영상은 어떤 형태로도 저장하지 않는다 (실시간 표시만).

실행:
    python realtime_webcam.py
환경 변수:
    YOLO_MODEL_PATH  사용할 pose 모델 (기본 yolo11n-pose.pt)
    CAM_INDEX        웹캠 인덱스 (기본 0)
    WINDOW_SIZE      행동 분류에 사용할 최근 프레임 수 (기본 45)
    CLASSIFY_EVERY   N 프레임마다 분류 재계산 (기본 5)
"""

from __future__ import annotations

import os
from collections import deque

import cv2
import numpy as np
from ultralytics import YOLO

from behavior import classify_behavior

MODEL_PATH = os.getenv("YOLO_MODEL_PATH", "yolo11n-pose.pt")
CAM_INDEX = int(os.getenv("CAM_INDEX", "0"))
WINDOW_SIZE = int(os.getenv("WINDOW_SIZE", "45"))
CLASSIFY_EVERY = int(os.getenv("CLASSIFY_EVERY", "5"))

# 화면 표시용 영문 라벨 (한글 폰트가 없을 때 폴백)
BEHAVIOR_EN = {
    "자리이탈": "Seat departure",
    "손흔들기": "Hand waving",
    "미분류": "Unclassified",
}

# 한글 렌더링용 폰트 후보 (macOS / Linux / Windows). 없으면 영문 폴백.
_FONT_CANDIDATES = [
    "/System/Library/Fonts/AppleSDGothicNeo.ttc",
    "/System/Library/Fonts/Supplemental/AppleGothic.ttf",
    "/usr/share/fonts/truetype/nanum/NanumGothic.ttf",
    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
    "C:/Windows/Fonts/malgun.ttf",
]


def result_to_persons(result) -> list[dict]:
    """YOLO Result 한 개를 behavior.py 가 기대하는 persons 구조로 변환.

    main.py 의 _analyze_video 와 동일한 방어 로직을 복제한다.
    (main.py 를 수정하지 않기 위해 의도적으로 중복.)
    """
    keypoints = result.keypoints
    boxes = result.boxes

    n_persons = len(boxes) if boxes is not None else 0
    xy = (
        keypoints.xy.cpu().numpy()
        if keypoints is not None and keypoints.xy is not None
        else None
    )
    conf = (
        keypoints.conf.cpu().numpy()
        if keypoints is not None and keypoints.conf is not None
        else None
    )
    box_conf = (
        boxes.conf.cpu().numpy()
        if boxes is not None and boxes.conf is not None
        else None
    )

    persons = []
    for i in range(n_persons):
        kpts = []
        if xy is not None and i < xy.shape[0] and xy.shape[1] > 0:
            kpts = [
                {
                    "x": float(xy[i, j, 0]),
                    "y": float(xy[i, j, 1]),
                    "confidence": float(conf[i, j])
                    if conf is not None and i < conf.shape[0]
                    else None,
                }
                for j in range(xy.shape[1])
            ]
        persons.append(
            {
                "box_confidence": float(box_conf[i])
                if box_conf is not None and i < box_conf.shape[0]
                else None,
                "keypoints": kpts,
            }
        )
    return persons


class _TextDrawer:
    """한글 가능하면 PIL 로, 아니면 OpenCV 로 텍스트를 그린다."""

    def __init__(self) -> None:
        self._pil_font = None
        try:
            from PIL import ImageFont  # noqa: PLC0415

            for path in _FONT_CANDIDATES:
                if os.path.exists(path):
                    self._pil_font = ImageFont.truetype(path, 26)
                    self._pil_font_small = ImageFont.truetype(path, 18)
                    break
        except Exception:  # PIL 미설치 등 → 영문 폴백
            self._pil_font = None

    @property
    def korean_ok(self) -> bool:
        return self._pil_font is not None

    def draw(self, frame, lines: list[tuple[str, tuple[int, int, int]]]):
        """lines: (text, (B,G,R)) 목록을 좌상단에 세로로 그린다."""
        if self._pil_font is not None:
            return self._draw_pil(frame, lines)
        return self._draw_cv(frame, lines)

    def _draw_pil(self, frame, lines):
        from PIL import Image, ImageDraw  # noqa: PLC0415

        img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        draw = ImageDraw.Draw(img)
        y = 12
        for idx, (text, bgr) in enumerate(lines):
            font = self._pil_font if idx == 0 else self._pil_font_small
            rgb = (bgr[2], bgr[1], bgr[0])
            # 가독성을 위한 검은 외곽선
            for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                draw.text((12 + dx, y + dy), text, font=font, fill=(0, 0, 0))
            draw.text((12, y), text, font=font, fill=rgb)
            y += 34 if idx == 0 else 26
        return cv2.cvtColor(np.asarray(img), cv2.COLOR_RGB2BGR)

    def _draw_cv(self, frame, lines):
        y = 30
        for idx, (text, bgr) in enumerate(lines):
            scale = 0.9 if idx == 0 else 0.6
            cv2.putText(frame, text, (12, y), cv2.FONT_HERSHEY_SIMPLEX,
                        scale, (0, 0, 0), 4, cv2.LINE_AA)
            cv2.putText(frame, text, (12, y), cv2.FONT_HERSHEY_SIMPLEX,
                        scale, bgr, 2, cv2.LINE_AA)
            y += 38 if idx == 0 else 28
        return frame


def main() -> None:
    print(f"YOLO pose 모델 로딩: {MODEL_PATH}")
    model = YOLO(MODEL_PATH)

    cap = cv2.VideoCapture(CAM_INDEX)
    if not cap.isOpened():
        raise SystemExit(f"웹캠을 열 수 없습니다 (CAM_INDEX={CAM_INDEX}).")

    drawer = _TextDrawer()
    if not drawer.korean_ok:
        print("한글 폰트를 찾지 못해 영문 라벨로 표시합니다.")

    window: deque[dict] = deque(maxlen=WINDOW_SIZE)
    behavior = {
        "detectedBehavior": "미분류",
        "confidence": 0.0,
        "behaviorReason": "분석 대기 중...",
    }
    frame_idx = 0
    print("실시간 분석 시작 — 종료하려면 q 를 누르세요.")

    try:
        while True:
            ok, frame = cap.read()
            if not ok:
                print("프레임을 읽지 못했습니다. 종료합니다.")
                break

            h, w = frame.shape[:2]
            result = model(frame, verbose=False)[0]

            # 사람이 없어도 빈 persons 로 안전하게 누적
            window.append({"persons": result_to_persons(result)})

            if frame_idx % CLASSIFY_EVERY == 0 and window:
                behavior = classify_behavior(list(window), w, h)

            # 스켈레톤이 그려진 프레임 위에 행동 정보 오버레이
            annotated = result.plot()

            label = behavior["detectedBehavior"]
            if not drawer.korean_ok:
                label = BEHAVIOR_EN.get(label, label)
            color = (0, 0, 255) if behavior["detectedBehavior"] != "미분류" else (0, 200, 0)

            reason = behavior.get("behaviorReason", "")
            if not drawer.korean_ok:
                reason = ""  # 근거는 한글 문장이라 영문 폴백 시 생략
            lines = [
                (f"{label}  ({behavior['confidence']:.2f})", color),
            ]
            if reason:
                lines.append((reason, (255, 255, 255)))
            lines.append(("press 'q' to quit", (200, 200, 200)))

            annotated = drawer.draw(annotated, lines)
            cv2.imshow("Realtime Behavior (YOLO Pose)", annotated)

            frame_idx += 1
            if cv2.waitKey(1) & 0xFF == ord("q"):
                break
    finally:
        cap.release()
        cv2.destroyAllWindows()
        print("종료되었습니다. (영상은 저장되지 않았습니다.)")


if __name__ == "__main__":
    main()
