"""
BeVietnam AI Core — FastAPI Application.

This is the AI service entry point. It provides:
  - /health          — service health check
  - /generate-task   — cultural task generation (Quest Maker pipeline)
  - /quest-chain     — full quest chain for UI rendering
  - /explain-recommendation, /verify-capture, /generate-vlog — stubs

The AI Core is a separate service from the Backend.
Backend owns product data; AI Core owns retrieval, reasoning, and generation.
"""

import asyncio
import logging

from fastapi import FastAPI

from ai.api.routes import router
from ai.common.qdrant_store import qdrant_store

# ── Logging setup ─────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s │ %(name)-30s │ %(levelname)-5s │ %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

# ── FastAPI app ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="BeVietnam AI Core",
    version="0.2.0",
    description="AI agent service for cultural task generation and knowledge retrieval.",
)

app.include_router(router)


@app.get("/health")
def health() -> dict[str, str]:
    """Health check endpoint — used by backend and monitoring."""
    return {"status": "ok", "service": "ai-core", "version": "0.2.0"}


_EMBED_PRELOAD_TIMEOUT_S = 300.0


async def _preload_embedding_model() -> None:
    """Load bge-m3 in background (does NOT block accepting HTTP — see startup_event)."""
    try:
        await asyncio.wait_for(
            asyncio.to_thread(lambda: qdrant_store.embedding_model),
            timeout=_EMBED_PRELOAD_TIMEOUT_S,
        )
        logger.info("Embedding model ready (background preload).")
    except asyncio.TimeoutError:
        logger.warning(
            "Embedding preload timed out (%.0fs); will finish on first Culture Scout query.",
            _EMBED_PRELOAD_TIMEOUT_S,
        )
    except Exception:
        logger.exception(
            "Embedding preload failed; will retry on first Culture Scout query."
        )


@app.on_event("startup")
async def startup_event() -> None:
    """
    Listen on :8000 immediately.

    IMPORTANT: Do not ``await`` the embedding load here — Uvicorn/Starlette only
    starts accepting TCP connections after lifespan startup completes. Awaiting a
    long HF download blocked the port → backend ``ConnectError: All connection attempts failed``.
    Warm-up runs in the background; first /generate-task may wait until loaded
    (backend AI_CORE_TIMEOUT should be high enough — e.g. 300s in docker-compose).
    """
    logger.info("🚀 BeVietnam AI Core v0.2.0 — accepting HTTP; embedding preload starts in background")
    asyncio.create_task(_preload_embedding_model())
    logger.info("   Gemini client initializes on first Quest Maker LLM call")
