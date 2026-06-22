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

    def generate_json_multimodal(
        self,
        system_prompt: str,
        user_prompt: str,
        images: list[tuple[bytes, str]],
        temperature: float = 0.1,
    ) -> dict[str, Any]:
        """
        Call Gemini with one or more images plus text, return parsed JSON.

        Args:
            system_prompt: System instruction (role, rubric, output schema).
            user_prompt:   Text prompt describing the task and what to compare.
            images:        List of (raw_bytes, mime_type) — sent in order.
            temperature:   Low by default for a stable, rubric-driven score.

        Returns:
            Parsed JSON dict, or {} on any failure so the caller can fall back.
        """
        try:
            parts: list[Any] = [
                types.Part.from_bytes(data=data, mime_type=mime)
                for data, mime in images
            ]
            parts.append(user_prompt)

            response = self.client.models.generate_content(
                model=settings.gemini_model,
                contents=parts,
                config=types.GenerateContentConfig(
                    system_instruction=system_prompt,
                    response_mime_type="application/json",
                    temperature=temperature,
                ),
            )
            result = json.loads(response.text.strip())
            logger.info("Gemini vision returned valid JSON (%d keys)", len(result))
            return result
        except json.JSONDecodeError as exc:
            logger.warning("Gemini vision returned invalid JSON: %s", exc)
            return {}
        except Exception as exc:
            logger.warning("Gemini vision call failed: %s", exc)
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

    def generate_json_multimodal(
        self,
        system_prompt: str,
        user_prompt: str,
        images: list[tuple[bytes, str]],
        max_tokens: int = 400,
        temperature: float = 0.1,
    ) -> dict[str, Any]:
        """
        Vision call to a VL model served by vLLM (OpenAI-compatible chat).

        Each image is inlined as a base64 data URL via an `image_url` content
        part, in order, followed by the text prompt. Returns parsed JSON, or {}
        on any failure so the caller can fall back. We do not send
        `response_format` here — not all VL servings accept it — and instead
        parse defensively.
        """
        import base64
        import httpx

        url = settings.vllm_base_url.rstrip("/") + "/chat/completions"
        headers = {"Content-Type": "application/json"}
        if settings.vllm_api_key:
            headers["Authorization"] = f"Bearer {settings.vllm_api_key}"

        content: list[dict[str, Any]] = []
        for data, mime in images:
            b64 = base64.b64encode(data).decode("ascii")
            content.append(
                {"type": "image_url", "image_url": {"url": f"data:{mime};base64,{b64}"}}
            )
        content.append({"type": "text", "text": user_prompt})

        payload = {
            "model": settings.vllm_model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": content},
            ],
            "temperature": temperature,
            "max_tokens": max_tokens,
        }
        try:
            response = httpx.post(url, json=payload, headers=headers, timeout=120.0)
            response.raise_for_status()
            raw_text = response.json()["choices"][0]["message"]["content"].strip()
            return _extract_json(raw_text)
        except Exception as exc:
            logger.warning("vLLM vision call failed: %s", exc)
            return {}


def _extract_json(text: str) -> dict[str, Any]:
    """Parse a JSON object from model text, tolerating code fences / prose."""
    text = text.strip()
    if text.startswith("```"):
        text = text.strip("`")
        text = text[text.find("{"):] if "{" in text else text
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        start, end = text.find("{"), text.rfind("}")
        if start != -1 and end > start:
            try:
                return json.loads(text[start : end + 1])
            except json.JSONDecodeError:
                return {}
    return {}


# ── Singleton instances ───────────────────────────────────────────────────────
# Import these in agent modules: from services.ai.common.llm import llm_gateway
llm_gateway = LLMGateway()
vllm_gateway = VLLMGateway()
