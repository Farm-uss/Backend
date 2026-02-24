# AI DB Contract (ERD aligned)

## Tables in scope

- `image_capture`
- `vision_inference`

## `/predict` response -> `vision_inference` mapping

- `model_name` -> `model_name`
- `task_type` -> `task_type`
- `label` -> `label`
- `confidence` -> `confidence`
- `bbox_json` -> `bbox_json`
- `inferred_at` -> `inferred_at`
- `is_abnormal` -> `is_abnormal`
- `abnormal_reason` -> `abnormal_reason`

## Additional response fields (service-level)

- `disease`
- `is_unknown`
- `model_version`
- `top3`
- `leaf_count`
- `fruit_count`
- `size_cm`
- `summary`

## Intentional adjustments from ERD (for practicality)

- `capture_id` is accepted in `/predict` request but not enforced inside AI service logic yet.
  - Reason: capture lifecycle is currently handled by Spring BE side.
- `bbox_json` is returned as empty list in placeholder inference.
  - Reason: current baseline is classification placeholder, not detection.
- Timestamp emitted in UTC from AI service.
  - Reason: stable cross-service time handling.

## Next integration step

- Spring BE should call AI `/predict`, then persist
  - `image_capture` row first
  - `vision_inference` row with returned fields + `capture_id`
