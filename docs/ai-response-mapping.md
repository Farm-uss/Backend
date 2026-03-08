# Farm-Us AI 응답 매핑 가이드

이 문서는 외부 AI 모델의 출력값을 Farm-Us 백엔드 응답 계약에 맞추기 위한 기준입니다.

## 1) 표준 입력 스키마 (AI -> BE)

백엔드는 AI 응답을 아래 기준 형태(`AiPredictResponse`)로 해석합니다.

```json
{
  "disease": "string",
  "confidence": 0.0,
  "is_unknown": false,
  "is_abnormal": false,
  "abnormal_reason": "string|null",
  "model_version": "string",
  "model_name": "string",
  "task_type": "DISEASE_CLASSIFICATION|...",
  "label": "string",
  "bbox_json": [],
  "inferred_at": "2026-03-01T10:00:00Z",
  "top3": [{"label": "a7", "p": 0.91}],
  "leaf_count": 3,
  "fruit_count": 1,
  "size_cm": 12.34,
  "summary": "string|null"
}
```

## 2) 외부 모델 별칭 키 허용 규칙

`VisionInferenceService` 정규화 로직에서 다음 별칭 키를 허용합니다.

- `disease`: `predictions`, `label`
- `confidence`: `score`, `probability`, `top3[0].p`
- `bbox_json`: `bbox`
- `top3`: `top_k`
- `leaf_count`: `leafCount`
- `fruit_count`: `fruitCount`
- `size_cm`: `sizeCm`, `leaf_area`

키가 누락된 경우 기본값을 사용합니다.

- `disease`: `"unknown"`
- `confidence`: `0.0`
- `model_name`: `"external-model"`
- `model_version`: `"unknown"`
- `task_type`: API 요청값 사용
- `inferred_at`: 서버 UTC 현재 시각

## 3) Farm-Us API 응답 계약 (BE -> Client)

`/api/v1/farms/{farmId}/crops/{cropsId}/vision-inference`는 `VisionInferenceCheckResponse`를 반환합니다.

```json
{
  "success": true,
  "code": "200",
  "data": {
    "diseaseStatus": 1,
    "diseaseId": "a7",
    "diseaseName": "잎마름병",
    "diseaseDescription": "잎 가장자리부터 갈변하며 진행되면 잎 전체가 마를 수 있습니다.",
    "confidence": 91,
    "causes": ["..."],
    "symptoms": ["..."],
    "solutions": ["..."]
  }
}
```

## 4) DB 저장 매핑

정규화된 AI 필드는 아래처럼 저장됩니다.

- `VisionInference.label <- ai.label`
- `VisionInference.confidence <- ai.confidence`
- `VisionInference.bboxJson <- ai.bboxJson`
- `VisionInference.isAbnormal <- ai.isAbnormal`
- `VisionInference.abnormalReason <- ai.abnormalReason`
- `GrowthMeasurement.leafCount <- ai.leafCount`
- `GrowthMeasurement.fruitCount <- ai.fruitCount`
- `GrowthMeasurement.leafArea <- ai.sizeCm` (현재 size 용도로 사용)
- `GrowthMeasurement.aiVerdict <- ai.disease`
- `GrowthMeasurement.aiLabel <- ai.label`

## 5) 참고 사항

- 이 매핑 기준을 유지하면 Farm-Us 기존 응답 키는 변하지 않습니다.
- 외부 모델은 별칭 키를 사용해도 백엔드 계약을 깨지 않습니다.
- 모델 출력에 bbox/risk 정보가 없으면 해당 필드는 null 또는 빈 값으로 저장됩니다.
