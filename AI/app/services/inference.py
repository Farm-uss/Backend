from datetime import datetime, timezone

from app.config import get_settings
from app.schemas import PredictResponse, TopKItem
from app.services.preprocess import decode_image

# Placeholder labels until a trained model is wired in.
CLASS_NAMES = ["healthy", "powdery_mildew", "leaf_blight", "rust"]


def run_inference(image_bytes: bytes, task_type: str = "DISEASE_CLASSIFICATION") -> PredictResponse:
    settings = get_settings()

    # Decode now to fail fast on invalid uploads.
    _ = decode_image(image_bytes)

    # TODO: Replace with real torch model inference.
    confidence = 0.62
    predicted_label = "leaf_blight"
    is_unknown = confidence < settings.confidence_threshold
    disease = "unknown" if is_unknown else predicted_label
    is_abnormal = disease != "healthy"
    abnormal_reason = "low_confidence" if is_unknown else None

    return PredictResponse(
        disease=disease,
        confidence=confidence,
        is_unknown=is_unknown,
        is_abnormal=is_abnormal,
        abnormal_reason=abnormal_reason,
        model_version=settings.model_version,
        model_name="farmus-placeholder-model",
        task_type=task_type,
        label=predicted_label,
        bbox_json=[],
        inferred_at=datetime.now(timezone.utc),
        top3=[
            TopKItem(label="leaf_blight", p=0.62),
            TopKItem(label="healthy", p=0.27),
            TopKItem(label="powdery_mildew", p=0.11),
        ],
        leaf_count=None,
        fruit_count=None,
        size_cm=None,
        summary=f"Predicted {predicted_label} (placeholder inference).",
    )
