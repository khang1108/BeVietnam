"""
LLM Gateway — self-hosted vLLM (OpenAI-compatible).

Single gateway for all AI agent text generation. Every LLM call goes through it so we can:
  - swap models from config (settings.vllm_model)
  - add logging/tracing later
  - handle errors + fallback in one place

Usage:
    from services.ai.common.llm import llm_gateway

    result = llm_gateway.generate_json(
        system_prompt="You are a cultural task designer...",
        user_prompt="Generate a task for a traveler near Huế...",
    )
    # result is a parsed Python dict from the JSON response
"""

import json
import logging
from typing import Any

from services.ai.common.config import settings

logger = logging.getLogger(__name__)


class VLLMGateway:
    """
    Gateway to the self-hosted vLLM endpoint (OpenAI-compatible).

    Structured-JSON generation. Uses httpx directly (the `openai` SDK is not a
    dependency) and asks for a JSON object via `response_format`. Returns {} on
    any failure so callers fall back cleanly.
    """

    def generate_json(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = 600,
    ) -> dict[str, Any]:
        import httpx

        url = settings.vllm_base_url.rstrip("/") + "/chat/completions"
        headers = {"Content-Type": "application/json"}
        if settings.vllm_api_key:
            headers["Authorization"] = f"Bearer {settings.vllm_api_key}"
        payload = {
            "model": settings.vllm_model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.3,
            "max_tokens": max_tokens,
            "response_format": {"type": "json_object"},
        }
        try:
            response = httpx.post(url, json=payload, headers=headers, timeout=90.0)
            response.raise_for_status()
            raw_text = response.json()["choices"][0]["message"]["content"].strip()
            result = json.loads(raw_text)
            logger.info("vLLM returned valid JSON (%d keys)", len(result))
            return result
        except json.JSONDecodeError as exc:
            logger.warning("vLLM returned invalid JSON: %s", exc)
            return {}
        except Exception as exc:
            logger.warning("vLLM call failed: %s", exc)
            return {}


# ── Singleton instance ────────────────────────────────────────────────────────
# Import in agent modules: from services.ai.common.llm import llm_gateway
# `vllm_gateway` is a back-compat alias for the same instance.
llm_gateway = VLLMGateway()
vllm_gateway = llm_gateway
