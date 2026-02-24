from fastapi import APIRouter, File, Form, UploadFile

from app.schemas import PredictResponse
from app.services.inference import run_inference

router = APIRouter(tags=["inference"])


@router.post("/predict", response_model=PredictResponse)
async def predict(
    image: UploadFile = File(...),
    task_type: str = Form(default="DISEASE_CLASSIFICATION"),
    capture_id: int | None = Form(default=None),
) -> PredictResponse:
    content = await image.read()
    _ = capture_id
    return run_inference(content, task_type=task_type)
