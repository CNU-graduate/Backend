# AI Server — YOLO Pose 행동인식 서버

졸업프로젝트 Backend의 AI 추론 서버입니다.
FastAPI 위에서 [Ultralytics YOLO](https://docs.ultralytics.com/) pose 모델을 사용해
업로드된 영상에서 사람의 키포인트(자세)를 추출합니다.

## 기능

- `GET /health` — 헬스 체크 (모델 로딩 여부 포함)
- `POST /analyze` — 영상 파일 업로드 → YOLO pose 추론 → 결과 JSON 반환
- 업로드된 원본 영상은 처리 완료(성공/실패 무관) 후 **항상 삭제**됩니다.

## 요구 사항

- Python 3.10+
- 의존성은 [requirements.txt](requirements.txt) 참고

## 설치 및 실행

```bash
cd ai-server

# 가상환경 (권장)
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 서버 실행 (기본 0.0.0.0:8000)
python main.py
# 또는
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

최초 실행 시 YOLO pose 가중치(`yolo11n-pose.pt`)가 자동으로 다운로드됩니다.

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `YOLO_MODEL_PATH` | `yolo11n-pose.pt` | 사용할 YOLO pose 모델 경로/이름 |
| `FRAME_STRIDE` | `5` | N 프레임마다 1번씩 추론 (1 = 모든 프레임) |
| `HOST` | `0.0.0.0` | 바인딩 호스트 |
| `PORT` | `8000` | 바인딩 포트 |
| `RELOAD` | (빈 값) | 값을 주면 코드 변경 시 자동 리로드 |

## API

### `GET /health`

```bash
curl http://localhost:8000/health
```

```json
{ "status": "ok", "model_loaded": true }
```

### `POST /analyze`

`multipart/form-data` 의 `file` 필드로 영상을 업로드합니다.
허용 확장자: `.mp4`, `.avi`, `.mov`, `.mkv`, `.webm`

```bash
curl -X POST http://localhost:8000/analyze \
  -F "file=@sample.mp4"
```

응답 예시:

```json
{
  "filename": "sample.mp4",
  "video": {
    "fps": 30.0,
    "total_frames": 300,
    "frame_stride": 5,
    "analyzed_frames": 60
  },
  "frames": [
    {
      "frame_index": 0,
      "timestamp_sec": 0.0,
      "person_count": 1,
      "persons": [
        {
          "box_confidence": 0.91,
          "keypoints": [
            { "x": 320.5, "y": 180.2, "confidence": 0.98 }
          ]
        }
      ]
    }
  ]
}
```

> `keypoints` 는 YOLO pose 의 17개 COCO 키포인트(코, 눈, 어깨, 팔꿈치 …) 순서를 따릅니다.

## API 문서

서버 실행 후 Swagger UI 에서 직접 테스트할 수 있습니다.

- Swagger UI: <http://localhost:8000/docs>
- ReDoc: <http://localhost:8000/redoc>

## 프로젝트 구조

```
ai-server/
├── main.py            # FastAPI 앱 (엔드포인트 + 추론 로직)
├── requirements.txt   # 의존성
├── .gitignore
└── README.md
```

## 참고 / TODO

현재는 키포인트 추출까지만 수행하는 뼈대입니다. 이후 단계 후보:

- 추출한 키포인트 시퀀스로 실제 **행동 분류**(예: 낙상, 폭력 등) 모델 연동
- 비동기 처리 / 작업 큐(영상이 길 경우)
- Spring Boot Backend 와의 연동 규격(요청·응답 스키마) 확정
