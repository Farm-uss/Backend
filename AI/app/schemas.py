from datetime import datetime

from pydantic import BaseModel, Field


class TopKItem(BaseModel):
    label: str
    p: float = Field(ge=0.0, le=1.0)


class PredictResponse(BaseModel):
    disease: str
    confidence: float = Field(ge=0.0, le=1.0)
    is_unknown: bool
    is_abnormal: bool
    abnormal_reason: str | None = None
    model_version: str
    model_name: str
    task_type: str
    label: str
    bbox_json: list[dict] | None = None
    inferred_at: datetime
    top3: list[TopKItem]
    leaf_count: int | None = None
    fruit_count: int | None = None
    size_cm: float | None = None
    summary: str | None = None
