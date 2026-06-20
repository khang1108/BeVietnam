"""
AI Core Configuration.

Loads settings from environment variables (via pydantic-settings).
All external service connections (Qdrant, vLLM, embedding model)
are configured here so agent code never hardcodes URLs or keys.

Supports both:
  - Qdrant Cloud (QDRANT_CLUSTER_ENDPOINT + QDRANT_API_KEY)
  - Embedded local Qdrant (QDRANT_PATH, no Docker/server)
  - Local Qdrant server (QDRANT_HOST + QDRANT_PORT via Docker or binary)

Usage:
    from services.ai.common.config import settings
    print(settings.vllm_base_url)
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

    # ── LLM (self-hosted vLLM — OpenAI-compatible, only provider) ─────────────
    # Served by vllm_hosting/ on L40 GPU(s), exposed via cloudflared.
    llm_provider: str = "vllm"  # "vllm" | "mock"
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
