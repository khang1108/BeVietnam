from pydantic_settings import BaseSettings
from typing import List

class Settings(BaseSettings):
    PROJECT_NAME: str = "Tourism App API"
    VERSION: str = "0.1.0"
    API_PREFIX: str = "/api/v1"

    ALLOWED_ORIGINS: List[str] = ["*"]

    AI_CORE_BASE_URL: str = "http://ai-core:8001"
    AI_CORE_TIMEOUT: int = 10
    AI_CORE_USE_MOCK: bool = True

    DATABASE_URL: str = "postgresql://postgres:311006@localhost:5432/bevietnam"

    SECRET_KEY: str = "change-me-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440

    MINIO_ENDPOINT: str = "http://localhost:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin123"
    
    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()