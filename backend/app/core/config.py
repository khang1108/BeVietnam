from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    PROJECT_NAME: str = "Tourism App API"
    VERSION: str = "0.1.0"
    API_PREFIX: str = "/api/v1"

    # CORS
    ALLOWED_ORIGINS: List[str] = ["*"]

    DATABASE_URL: str = "postgresql+asyncpg://postgres:1@localhost:5432/bevietnam_db"

    JWT_SECRET_KEY: str = "replace-me-with-secure-secret"
    JWT_ALGORITHM: str = "HS256"
    JWT_ACCESS_TOKEN_EXPIRE_MINUTES: int = 60

    MINIO_ENDPOINT: str = "http://minio:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin"
    MINIO_BUCKET: str = "bevietnam"
    MINIO_SECURE: bool = False

    # AI Core
    AI_CORE_BASE_URL: str = "http://ai-core:8001"
    # First Quest Maker run loads bge-m3 from Hugging Face + embeds — often >60s in Docker.
    AI_CORE_TIMEOUT: int = 300  # seconds
    AI_CORE_USE_MOCK: bool = True  # True = dùng mock thay vì gọi thật

    class Config:
        env_file = ".env"
        case_sensitive = True
        extra = "ignore"


settings = Settings()
