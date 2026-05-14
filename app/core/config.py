from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    PROJECT_NAME: str = "Tourism App API"
    VERSION: str = "0.1.0"
    API_PREFIX: str = "/api/v1"

    # CORS
    ALLOWED_ORIGINS: List[str] = ["*"]

    # AI Core
    AI_CORE_BASE_URL: str = "http://ai-core:8001"
    AI_CORE_TIMEOUT: int = 10  # seconds
    AI_CORE_USE_MOCK: bool = True  # True = dùng mock thay vì gọi thật

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
