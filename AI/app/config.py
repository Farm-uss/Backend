from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "FarmUS AI Model Service"
    app_env: str = "local"
    app_port: int = 8001

    model_path: str = "./model_store/model.pt"
    model_version: str = "v0.1.0"
    confidence_threshold: float = 0.75

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


@lru_cache
def get_settings() -> Settings:
    return Settings()
