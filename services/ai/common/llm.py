"""
LLM Gateway — Gemini Integration.

Provides a clean interface for the AI agents to call Gemini.
All LLM calls go through this gateway so we can:
  - swap models easily
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

from google import genai
from google.genai import types

from services.ai.common.config import settings

logger = logging.getLogger(__name__)


class LLMGateway:
    """
    Gateway to Gemini LLM for structured JSON generation.

    Uses google-genai SDK with response_mime_type="application/json"
    to enforce structured output from the model.
    """

    def __init__(self) -> None:
        self._client: genai.Client | None = None

    @property
    def client(self) -> genai.Client:
        """Lazy-initialize the Gemini client (created once, reused)."""
        if self._client is None:
            if not settings.gemini_api_key:
                raise ValueError(
                    "GEMINI_API_KEY is not set. "
                    "Add it to your .env file or environment variables."
                )
            self._client = genai.Client(api_key=settings.gemini_api_key)
            logger.info("Gemini client initialized (model: %s)", settings.gemini_model)
        return self._client

    def generate_json(
        self,
        system_prompt: str,
        user_prompt: str,
    ) -> dict[str, Any]:
        """
        Call Gemini and return a parsed JSON dict.

        Args:
            system_prompt: System instruction (role, rules, output format).
            user_prompt:   User-facing prompt with context and request.

        Returns:
            Parsed Python dict from the model's JSON response.

        Raises:
            Logs a warning and returns an empty dict if generation fails,
            so the caller can use fallback behavior.
        """
        try:
            response = self.client.models.generate_content(
                model=settings.gemini_model,
                contents=user_prompt,
                config=types.GenerateContentConfig(
                    system_instruction=system_prompt,
                    response_mime_type="application/json",
                    temperature=0.7,
                ),
            )

            # Parse the JSON response text
            raw_text = response.text.strip()
            result = json.loads(raw_text)
            logger.info("Gemini returned valid JSON (%d keys)", len(result))
            return result

        except json.JSONDecodeError as exc:
            logger.warning("Gemini returned invalid JSON: %s", exc)
            return {}
        except Exception as exc:
            logger.warning("Gemini call failed: %s", exc)
            return {}


class VLLMGateway:
    """
    Gateway to the self-hosted vLLM endpoint (OpenAI-compatible).

    Same `generate_json(system_prompt, user_prompt)` contract as LLMGateway so
    callers can swap providers without changing code. Uses httpx directly (the
    `openai` SDK is not a dependency) and asks for a JSON object via
    `response_format`. Returns {} on any failure so callers fall back cleanly.
    """

    def generate_json(
        self,
        system_prompt: str,
        user_prompt: str,
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
            "max_tokens": 600,
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


# ── Singleton instances ───────────────────────────────────────────────────────
# Import these in agent modules: from services.ai.common.llm import llm_gateway
llm_gateway = LLMGateway()
vllm_gateway = VLLMGateway()
