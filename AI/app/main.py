from fastapi import FastAPI

from app.config import get_settings
from app.routers.predict import router as predict_router

settings = get_settings()

app = FastAPI(title=settings.app_name)
app.include_router(predict_router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": settings.app_name, "env": settings.app_env}
