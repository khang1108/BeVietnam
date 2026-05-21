"""
AI Core Client — Backend → AI Core HTTP Communication.

The backend calls AI Core via HTTP to get AI-generated content.
This client handles:
  - Mock mode (AI_CORE_USE_MOCK=true): returns pre-baked responses
  - Real mode: calls AI Core service via httpx
  - Fallback: returns safe defaults when AI Core is unreachable
"""

import httpx
import logging
from app.core.config import settings
from typing import Any, Dict, Optional

logger = logging.getLogger(__name__)

# ── Mock responses (used when AI_CORE_USE_MOCK=True) ──────────────────────────
_MOCK_RESPONSES: Dict[str, Any] = {
    "generate_task": {
        "status": "ok",
        "data": {
            "quest_id": "quest-hue-imperial",
            "task_id": "mock-task-001",
            "step_index": 1,
            "title": "Khám phá Ngọ Môn — Cổng vào Hoàng thành",
            "description": "Tìm và chụp ảnh Ngọ Môn — cổng chính phía nam của Hoàng thành Huế.",
            "cultural_explanation": "Ngọ Môn là nơi diễn ra các nghi lễ quan trọng nhất của triều Nguyễn.",
            "completion_requirement": "Chụp một ảnh Ngọ Môn với metadata vị trí.",
            "unlock_condition": {"type": "capture_required", "requires_photo": True, "requires_location": True},
            "next_task_hint": "Đằng sau cổng này là nơi vua thiết triều...",
            "difficulty": "easy",
            "reason_codes": ["culture", "history", "architecture"],
        },
        "metadata": {"ai_generated": False, "fallback": True},
    },
    "quest_chain": {
        "status": "ok",
        "quest_id": "quest-hue-imperial",
        "place_name": "Kinh thành Huế",
        "total_tasks": 5,
        "tasks": [],  # Will be populated from AI Core
    },
    "explain_recommendation": {
        "explanation": "Đây là mock explanation từ AI Core.",
    },
    "verify_capture": {
        "status": "ok",
        "data": {
            "status": "approved",
            "reason": "Mock AI Core — ảnh được chấp nhận",
            "confidence": 1.0,
        },
        "metadata": {"ai_generated": False, "fallback": True},
    },
}

# ── Fallback response when AI Core is unreachable ─────────────────────────────
_FALLBACK: Dict[str, Any] = {
    "error": "AI Core unavailable",
    "fallback": True,
}


class AICoreClient:
    """
    HTTP client for communicating with the AI Core service.

    Endpoints:
      POST /generate-task          → Generate next cultural task
      GET  /quest-chain            → Get full quest chain for UI
      POST /explain-recommendation → Explain recommendation
      POST /verify-capture         → Capture Judge (ảnh chứng minh task)
    """

    def __init__(self) -> None:
        self.base_url = settings.AI_CORE_BASE_URL
        self.timeout = settings.AI_CORE_TIMEOUT
        self.use_mock = settings.AI_CORE_USE_MOCK

    async def _post(self, endpoint: str, payload: Dict) -> Dict:
        """Send POST request to AI Core, fallback if error."""
        if self.use_mock:
            key = endpoint.lstrip("/").replace("-", "_")
            logger.info("AI Core mock mode: returning mock for '%s'", key)
            return _MOCK_RESPONSES.get(key, _FALLBACK)
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}{endpoint}", json=payload
                )
                response.raise_for_status()
                return response.json()
        except (httpx.RequestError, httpx.HTTPStatusError) as exc:
            logger.warning(
                "AI Core POST %s failed: %s: %r",
                endpoint,
                type(exc).__name__,
                exc,
            )
            return _FALLBACK

    async def _get(self, endpoint: str, params: Dict | None = None) -> Dict:
        """Send GET request to AI Core, fallback if error."""
        if self.use_mock:
            key = endpoint.lstrip("/").replace("-", "_")
            logger.info("AI Core mock mode: returning mock for '%s'", key)
            return _MOCK_RESPONSES.get(key, _FALLBACK)
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}{endpoint}", params=params
                )
                response.raise_for_status()
                return response.json()
        except (httpx.RequestError, httpx.HTTPStatusError) as exc:
            logger.warning(
                "AI Core GET %s failed: %s: %r",
                endpoint,
                type(exc).__name__,
                exc,
            )
            return _FALLBACK

    # ── Public methods ────────────────────────────────────────────────────────

    async def generate_task(
        self,
        user_id: str,
        context: Optional[Dict] = None,
    ) -> Dict:
        """Call AI Core to generate the next cultural task."""
        payload = {"user_id": user_id, **(context or {})}
        return await self._post("/generate-task", payload)

    async def get_quest_chain(
        self,
        quest_id: str = "quest-hue-imperial",
    ) -> Dict:
        """Call AI Core to get the full quest chain for UI rendering."""
        return await self._get("/quest-chain", {"quest_id": quest_id})

    async def explain_recommendation(
        self,
        place_id: str,
        user_id: str,
    ) -> Dict:
        """Call AI Core for recommendation explanation."""
        return await self._post(
            "/explain-recommendation",
            {"place_id": place_id, "user_id": user_id},
        )

    async def verify_capture(
        self,
        user_id: str,
        task: Dict,
        capture: Dict,
    ) -> Dict:
        """Capture Judge — xác minh ảnh / minh chứng hoàn thành task."""
        payload = {"user_id": user_id, "task": task, "capture": capture}
        return await self._post("/verify-capture", payload)


# Singleton instance
ai_core_client = AICoreClient()
