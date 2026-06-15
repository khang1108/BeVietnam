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
    # First Quest Maker run loads bge-m3 from Hugging Face + embeds — often >60s in Docker.
    AI_CORE_TIMEOUT: int = 300  # seconds
    AI_CORE_USE_MOCK: bool = True  # True = dùng mock thay vì gọi thật

    # Context-aware question selection
    QUESTION_POOL_PATH: str = "data/question_pool.json"

    # Goong context enrichment. Leave GOONG_API_KEY empty for local fallback mode.
    GOONG_API_KEY: str = ""
    GOONG_BASE_URL: str = "https://rsapi.goong.io"
    GOONG_TIMEOUT: int = 10

    # OpenWeather context enrichment. Leave key empty for local fallback mode.
    OPENWEATHER_API_KEY: str = ""
    OPENWEATHER_BASE_URL: str = "https://api.openweathermap.org/data/2.5"
    OPENWEATHER_TIMEOUT: int = 10

    class Config:
        env_file = ".env"
        case_sensitive = True
        extra = "ignore"


settings = Settings()
