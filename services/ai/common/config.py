"""
AI Core Configuration.

Loads settings from environment variables (via pydantic-settings).
All external service connections (Qdrant, Gemini, embedding model)
are configured here so agent code never hardcodes URLs or keys.

Supports both:
  - Qdrant Cloud (QDRANT_CLUSTER_ENDPOINT + QDRANT_API_KEY)
  - Embedded local Qdrant (QDRANT_PATH, no Docker/server)
  - Local Qdrant server (QDRANT_HOST + QDRANT_PORT via Docker or binary)

Usage:
    from services.ai.common.config import settings
    print(settings.gemini_api_key)
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """
    Central configuration for the AI Core service.

    Environment variables override defaults.
    For local dev, values come from the root .env file.
    In Docker, they come from docker-compose environment section.
    """

    # ── Qdrant (Vector Database) ──────────────────────────────────────────────
    # Option 1: Qdrant Cloud (preferred for demo — already provisioned)
    qdrant_cluster_endpoint: str = ""
    qdrant_api_key: str = ""
    qdrant_cluster_id: str = ""

    # Option 2: Local Qdrant (via Docker)
    # If set, qdrant-client stores vectors directly on disk and no server is needed.
    qdrant_path: str = ""

    # Option 3: Local Qdrant server (Docker or standalone binary)
    qdrant_host: str = "vector-db"
    qdrant_port: int = 6333

    # Collection name (shared between cloud and local)
    qdrant_collection: str = "cultural_knowledge"

    # ── Embedding (HuggingFace Inference API — no local model needed) ────────
    embedding_model_name: str = "BAAI/bge-m3"
    embedding_dimension: int = 1024  # bge-m3 dense embedding dimension
    hf_token: str = ""               # HuggingFace token — get free at hf.co/settings/tokens

    # ── LLM (Gemini) ─────────────────────────────────────────────────────────
    gemini_api_key: str = ""
    gemini_model: str = "gemini-2.0-flash"
    llm_provider: str = "gemini"  # "gemini" | "vllm" | "mock"

    # ── Capture verification (reference-image grounding) ──────────────────────
    # SerpAPI Google Images key — fetch a reference photo for a task when none
    # is cached yet. Empty → fall back to text-only vision judging.
    serpapi_api_key: str = ""
    # Where downloaded reference images are cached (one per task, reused).
    reference_cache_dir: str = "data/reference_images"

    # ── LLM (self-hosted vLLM — OpenAI-compatible, replaces quota-dead Gemini) ─
    # Served by vllm_hosting/ on a Thundercompute L40, exposed via cloudflared.
    vllm_base_url: str = "https://api.iamphuckhang.dev/v1"
    vllm_model: str = "qwen2.5-14b-instruct"  # the --served-model-name
    vllm_api_key: str = ""  # empty = open endpoint

    # ── General ───────────────────────────────────────────────────────────────
    log_level: str = "INFO"

    @property
    def use_qdrant_cloud(self) -> bool:
        """True if Qdrant Cloud credentials are configured."""
        return bool(self.qdrant_cluster_endpoint and self.qdrant_api_key)

    class Config:
        env_file = ".env"
        case_sensitive = False
        extra = "ignore"


settings = Settings()
