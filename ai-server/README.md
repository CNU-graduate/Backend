# AI Server — YOLO Pose 행동인식 서버

졸업프로젝트 Backend의 AI 추론 서버입니다.
FastAPI 위에서 [Ultralytics YOLO](https://docs.ultralytics.com/) pose 모델을 사용해
업로드된 영상에서 사람의 키포인트(자세)를 추출하고,
그 결과를 바탕으로 **규칙 기반 행동 분류**(자리이탈 / 손흔들기 / 미분류)를 수행합니다.

## 기능

- `GET /health` — 헬스 체크 (모델 로딩 여부 포함)
- `POST /analyze` — 영상 파일 업로드 → YOLO pose 추론 → 행동 분류 + 결과 JSON 반환
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
  "detectedBehavior": "자리이탈",
  "confidence": 0.82,
  "draftText": "학생이 자리에서 이탈한 것으로 감지되었습니다.",
  "behaviorReason": "초반 대비 사람 중심점이 크게 이동했습니다 (이동 비율 0.45).",
  "video": {
    "fps": 30.0,
    "total_frames": 300,
    "frame_width": 640,
    "frame_height": 480,
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

#### 최상위 행동 분류 필드

| 필드 | 설명 |
|------|------|
| `detectedBehavior` | 감지된 행동: `"자리이탈"`, `"손흔들기"`, `"미분류"` 중 하나 |
| `confidence` | 분류 신뢰도 (0.0 ~ 0.95). 미분류는 `0.0` |
| `draftText` | 사람이 읽을 수 있는 요약 문장 (초안) |
| `behaviorReason` | 그렇게 판단한 근거 |

> 기존 프레임별 `keypoints` JSON(`frames`)은 그대로 유지되며, 위 4개 필드가 응답 최상위에 추가됩니다.

## 행동 분류 기준

규칙 기반 분류 로직은 [behavior.py](behavior.py) 에 분리되어 있습니다.
프레임마다 `box_confidence` 가 가장 높은 사람을 **주 대상**으로 삼고,
`confidence` 가 `0.5`(`KP_CONF_MIN`) 미만인 keypoint 는 신뢰하지 않습니다.

### 1. 자리이탈

다음 신호 중 가장 강한 것을 채택합니다 (점수 `0.6` 이상이면 자리이탈).

- **중심점 이동**: 프레임별 사람 중심점(신뢰 keypoint 평균)을 구해, 영상 초반 1/3 평균과
  후반 1/3 평균의 이동량이 프레임 대각선의 `22%`(`DEPARTURE_MOVE_RATIO`) 이상이면 자리이탈.
- **사라짐**: 초반에 감지되던 사람이 후반 구간에서 거의 사라지면(존재 비율 ≤ 20%) 자리이탈.
- **화면 가장자리 이탈**: 후반 중심점이 화면 좌·우·하단 가장자리(`EDGE_MARGIN_RATIO 8%`) 안으로 벗어나면 자리이탈.

### 2. 손흔들기

COCO `left_wrist`(9) / `right_wrist`(10) 좌표를 사용합니다 (점수 `0.6` 이상이면 손흔들기).

- 어깨 중심 기준 손목의 **좌우 상대 위치 시계열**에서 방향 전환이
  `3회`(`WAVE_MIN_DIRECTION_CHANGES`) 이상 반복되고,
- 좌우 진폭이 **어깨 너비 대비 `15%`**(`WAVE_MIN_AMPLITUDE_RATIO`) 이상이면 손흔들기.
- 손목이 어깨보다 위로 올라간 비율이 높을수록 신뢰도가 올라갑니다.
- confidence 가 낮은(`< 0.5`) 손목 keypoint 가 있는 프레임은 계산에서 제외됩니다.

### 3. 미분류

위 두 조건에 명확히 해당하지 않거나, 사람이 감지된 프레임이 없으면
`detectedBehavior` 는 `"미분류"`, `confidence` 는 `0.0` 으로 반환합니다.
(사람이 없거나 keypoint 가 비어 있어도 서버는 예외를 던지지 않습니다.)

> 임계값(`DEPARTURE_MOVE_RATIO`, `WAVE_MIN_DIRECTION_CHANGES` 등)은
> [behavior.py](behavior.py) 상단 상수로 모여 있어 영상 특성에 맞게 조정할 수 있습니다.

## API 문서

서버 실행 후 Swagger UI 에서 직접 테스트할 수 있습니다.

- Swagger UI: <http://localhost:8000/docs>
- ReDoc: <http://localhost:8000/redoc>

## 프로젝트 구조

```
ai-server/
├── main.py              # FastAPI 앱 (엔드포인트 + 추론 로직)
├── behavior.py          # 규칙 기반 행동 분류 로직
├── realtime_webcam.py   # 로컬 웹캠 실시간 행동 분류 데모
├── requirements.txt     # 의존성
├── .gitignore
└── README.md
```

## 실시간 웹캠 테스트

로컬 노트북 웹캠으로 행동 분류를 실시간 확인할 수 있는 데모입니다.
`/analyze` API 와 동일한 [behavior.py](behavior.py) 분류 로직을 그대로 재사용합니다.

```bash
cd ai-server
source .venv/bin/activate          # 가상환경 (없으면 위 설치 단계 참고)

python realtime_webcam.py
```

- 웹캠 화면 위에 **스켈레톤**과 함께 `detectedBehavior`, `confidence`, `behaviorReason` 가 표시됩니다.
- **`q` 키**를 누르면 종료됩니다.
- 영상은 **어떤 형태로도 저장되지 않으며**, 실시간 화면 표시만 수행합니다.
- 한글 폰트가 없는 환경에서는 행동 라벨이 영문(`Seat departure` / `Hand waving` / `Unclassified`)으로 표시됩니다.

환경 변수:

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `CAM_INDEX` | `0` | 사용할 웹캠 인덱스 |
| `WINDOW_SIZE` | `45` | 행동 분류에 사용할 최근 프레임 수 |
| `CLASSIFY_EVERY` | `5` | N 프레임마다 분류를 재계산 |
| `YOLO_MODEL_PATH` | `yolo11n-pose.pt` | 사용할 YOLO pose 모델 |

> GUI 창(`cv2.imshow`)이 필요하므로 디스플레이가 있는 로컬 환경에서 실행하세요.
> 서버(SSH/헤드리스) 환경에서는 창을 띄울 수 없습니다.

## 참고 / TODO

현재는 keypoint 추출 + 규칙 기반 행동 분류(자리이탈/손흔들기/미분류) 단계입니다. 이후 후보:

- 규칙 기반 → 키포인트 시퀀스 학습 기반 **행동 분류 모델**(낙상, 폭력 등)로 확장
- 비동기 처리 / 작업 큐(영상이 길 경우)
- Spring Boot Backend 와의 연동 규격(요청·응답 스키마) 확정
