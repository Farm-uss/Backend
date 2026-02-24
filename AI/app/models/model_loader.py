from functools import lru_cache

import torch

from app.config import get_settings


@lru_cache
def load_model() -> torch.nn.Module | None:
    settings = get_settings()
    model_path = settings.model_path

    # TODO: Load trained model from model_path.
    # Keep returning None until model artifact is ready.
    _ = model_path
    return None
