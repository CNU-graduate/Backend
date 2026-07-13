"""FastAPI 기반 YOLO Pose 행동인식 서버.

- GET  /health   : 헬스 체크
- POST /analyze  : 영상 업로드 → YOLO pose 추론 → 결과 JSON 반환

원본 업로드 영상은 처리 완료(또는 실패) 후 항상 삭제된다.
"""

from __future__ import annotations

import logging
import os
import shutil
import tempfile
import uuid
from contextlib import asynccontextmanager
from pathlib import Path

import cv2
from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.responses import JSONResponse
from ultralytics import YOLO

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("ai-server")

# 환경 변수로 모델 경로 / 추론 주기 조절 가능
MODEL_PATH = os.getenv("YOLO_MODEL_PATH", "yolo11n-pose.pt")
# N 프레임마다 1번씩 추론 (1 = 모든 프레임). 영상 길이에 따라 부하 조절.
FRAME_STRIDE = int(os.getenv("FRAME_STRIDE", "5"))
# 허용 영상 확장자
ALLOWED_EXTENSIONS = {".mp4", ".avi", ".mov", ".mkv", ".webm"}

# 모델은 무거우므로 앱 시작 시 한 번만 로드한다.
ml_models: dict[str, YOLO] = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("YOLO pose 모델 로딩: %s", MODEL_PATH)
    ml_models["pose"] = YOLO(MODEL_PATH)
    logger.info("모델 로딩 완료")
    yield
    ml_models.clear()


app = FastAPI(
    title="YOLO Pose 행동인식 서버",
    description="영상을 업로드하면 YOLO pose 로 사람 키포인트를 추출한다.",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health")
async def health() -> dict:
    """헬스 체크. 모델 로딩 여부를 함께 반환한다."""
    return {"status": "ok", "model_loaded": "pose" in ml_models}


def _save_upload_to_tempfile(file: UploadFile) -> Path:
    """업로드 파일을 임시 디렉터리에 저장하고 경로를 반환한다."""
    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"지원하지 않는 파일 형식입니다: '{suffix}'. 허용: {sorted(ALLOWED_EXTENSIONS)}",
        )

    tmp_dir = Path(tempfile.gettempdir()) / "ai-server-uploads"
    tmp_dir.mkdir(parents=True, exist_ok=True)
    dest = tmp_dir / f"{uuid.uuid4().hex}{suffix}"

    with dest.open("wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    return dest


def _analyze_video(video_path: Path) -> dict:
    """영상을 프레임 단위로 읽어 YOLO pose 추론 결과를 집계한다."""
    model = ml_models["pose"]

    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        raise HTTPException(status_code=400, detail="영상 파일을 열 수 없습니다.")

    fps = cap.get(cv2.CAP_PROP_FPS) or 0.0
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT) or 0)

    frames_result: list[dict] = []
    frame_idx = -1
    try:
        while True:
            ok, frame = cap.read()
            if not ok:
                break
            frame_idx += 1
            if frame_idx % FRAME_STRIDE != 0:
                continue

            # verbose=False 로 콘솔 로그 억제
            results = model(frame, verbose=False)
            result = results[0]

            persons = []
            keypoints = result.keypoints
            boxes = result.boxes

            # 검출된 사람 수는 boxes 기준으로 센다.
            # (무검출 프레임에서 ultralytics 가 keypoints.xy 를 (1, 0, 2) 로
            #  반환해 사람 수가 1처럼 보이는 경우가 있어 boxes 와 어긋난다.)
            n_persons = len(boxes) if boxes is not None else 0

            xy = (
                keypoints.xy.cpu().numpy()  # (사람수, 키포인트수, 2)
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

            frames_result.append(
                {
                    "frame_index": frame_idx,
                    "timestamp_sec": round(frame_idx / fps, 3) if fps else None,
                    "person_count": len(persons),
                    "persons": persons,
                }
            )
    finally:
        cap.release()

    return {
        "video": {
            "fps": round(fps, 3),
            "total_frames": total_frames,
            "frame_stride": FRAME_STRIDE,
            "analyzed_frames": len(frames_result),
        },
        "frames": frames_result,
    }


@app.post("/analyze")
async def analyze(file: UploadFile = File(...)) -> JSONResponse:
    """영상 파일을 업로드 받아 YOLO pose 추론 결과를 반환한다.

    처리가 끝나면(성공/실패 무관) 업로드된 원본 영상은 삭제된다.
    """
    if "pose" not in ml_models:
        raise HTTPException(status_code=503, detail="모델이 아직 준비되지 않았습니다.")

    video_path = _save_upload_to_tempfile(file)
    try:
        logger.info("분석 시작: %s (원본명=%s)", video_path.name, file.filename)
        result = _analyze_video(video_path)
        return JSONResponse(content={"filename": file.filename, **result})
    finally:
        # 원본 영상은 처리 후 항상 삭제
        try:
            video_path.unlink(missing_ok=True)
            logger.info("원본 영상 삭제 완료: %s", video_path.name)
        except OSError as exc:
            logger.warning("원본 영상 삭제 실패: %s (%s)", video_path.name, exc)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=os.getenv("HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", "8000")),
        reload=bool(os.getenv("RELOAD", "")),
    )
