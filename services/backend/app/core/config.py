from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    PROJECT_NAME: str = "Tourism App API"
    VERSION: str = "0.1.0"
    API_PREFIX: str = "/api/v1"
    PUBLIC_API_BASE_URL: str = "http://localhost:8000/api/v1"

    # CORS
    ALLOWED_ORIGINS: List[str] = ["*"]

    # AI Core
    AI_CORE_BASE_URL: str = "http://ai-core:8001"
    # First Quest Maker run loads bge-m3 from Hugging Face + embeds — often >60s in Docker.
    AI_CORE_TIMEOUT: int = 300  # seconds
    AI_CORE_USE_MOCK: bool = True  # True = dùng mock thay vì gọi thật

    # Context-aware question selection
    QUESTION_POOL_PATH: str = "data/question_pool.json"

    # Pre-built spotlight posts that feed the personalized recommendation feed.
    SPOTLIGHTS_PATH: str = "data/posts/hue_spotlights.json"

    # Goong context enrichment. Leave GOONG_API_KEY empty for local fallback mode.
    GOONG_API_KEY: str = ""
    GOONG_BASE_URL: str = "https://rsapi.goong.io"
    GOONG_TIMEOUT: int = 10

    # OpenWeather context enrichment. Leave key empty for local fallback mode.
    OPENWEATHER_API_KEY: str = ""
    OPENWEATHER_BASE_URL: str = "https://api.openweathermap.org/data/2.5"
    OPENWEATHER_TIMEOUT: int = 10

    # SerpAPI is used offline to enrich stored feed posts with MinIO image URLs.
    SERPAPI_API_KEY: str = ""
    FEED_IMAGE_PREFIX: str = "feed-posts"

    # Foursquare Places (new platform) — live nearby POIs (cafe / homestay / …).
    # Leave key empty to disable the /places/nearby endpoint (returns empty).
    FOURSQUARE_API_KEY: str = ""
    FOURSQUARE_BASE_URL: str = "https://places-api.foursquare.com"
    FOURSQUARE_API_VERSION: str = "2025-06-17"
    FOURSQUARE_TIMEOUT: int = 10
    # Premium fields (rating/popularity) need a production key with billing.
    # Requesting them on a non-premium key 429s the whole call, so gate it.
    FOURSQUARE_PREMIUM: bool = False

    # Outbound rate limiting for shared third-party free tiers.
    # GLOBAL (all users share one quota), per-process. Tune via env for demo.
    RATE_LIMIT_ENABLED: bool = True
    FOURSQUARE_RPM: int = 30      # requests/min
    FOURSQUARE_DAILY: int = 2000  # hard cap/day (0 = unlimited)
    OPENWEATHER_RPM: int = 40
    OPENWEATHER_DAILY: int = 5000
    GOONG_RPM: int = 20
    GOONG_DAILY: int = 1500

    # Auth / JWT — override SECRET_KEY in production via env.
    SECRET_KEY: str = "dev-only-change-me-in-production-32-bytes-minimum"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # MinIO / S3-compatible object storage for capture media.
    MINIO_ENDPOINT: str = "http://localhost:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin"
    MINIO_BUCKET: str = "bevietnam-captures"

    class Config:
        env_file = ".env"
        case_sensitive = True
        extra = "ignore"


settings = Settings()
