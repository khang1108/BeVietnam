"""
AI Core Client — Sprint 0 Contract
Backend gọi AI Core qua HTTP. Không implement logic AI ở đây.
"""
import httpx
from app.core.config import settings
from typing import Any, Dict, Optional

# ── Mock responses (dùng khi AI_CORE_USE_MOCK=True) ──────────────────────────
_MOCK_RESPONSES: Dict[str, Any] = {
    "generate_task": {
        "task_id": "mock-task-001",
        "title": "Visit Văn Miếu",
        "description": "Explore the Temple of Literature, one of Hanoi's most iconic landmarks.",
        "score": 0.92,
    },
    "explain_recommendation": {
        "explanation": "Đây là mock explanation từ AI Core.",
    },
}

# ── Fallback response khi AI Core không trả lời ───────────────────────────────
_FALLBACK: Dict[str, Any] = {
    "error": "AI Core unavailable",
    "fallback": True,
}


class AICoreClient:
    """
    Contract với AI Core service.

    Endpoints đã biết:
      POST /generate-task      → tạo task gợi ý cho user
      POST /explain            → giải thích recommendation
    """

    def __init__(self):
        self.base_url = settings.AI_CORE_BASE_URL
        self.timeout = settings.AI_CORE_TIMEOUT
        self.use_mock = settings.AI_CORE_USE_MOCK

    async def _post(self, endpoint: str, payload: Dict) -> Dict:
        """Gửi POST request tới AI Core, fallback nếu lỗi."""
        if self.use_mock:
            key = endpoint.lstrip("/").replace("-", "_")
            return _MOCK_RESPONSES.get(key, _FALLBACK)
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}{endpoint}", json=payload
                )
                response.raise_for_status()
                return response.json()
        except (httpx.RequestError, httpx.HTTPStatusError):
            return _FALLBACK

    # ── Public methods ────────────────────────────────────────────────────────

    async def generate_task(self, user_id: str, context: Optional[Dict] = None) -> Dict:
        """Gọi AI Core để sinh task gợi ý tiếp theo cho user."""
        return await self._post("/generate-task", {"user_id": user_id, "context": context or {}})

    async def explain_recommendation(self, place_id: str, user_id: str) -> Dict:
        """Gọi AI Core để lấy giải thích tại sao gợi ý địa điểm này."""
        return await self._post("/explain", {"place_id": place_id, "user_id": user_id})


# Singleton instance
ai_core_client = AICoreClient()
