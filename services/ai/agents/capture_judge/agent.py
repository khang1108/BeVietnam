"""
Capture Judge — vision-backed task completion verification.

The agent asks the vision backend whether the submitted image satisfies the
specific task. It never approves merely because an image exists. If the VLM is
unavailable, it returns `needs_review` so storyline clients can stay stable
without silently unlocking the next task.
"""

import logging
from typing import Any

from services.ai.common.config import settings
from services.ai.common.llm import llm_gateway
from services.ai.common.tracing import traceable

logger = logging.getLogger(__name__)

_SYSTEM_PROMPT = (
    "You are Capture Judge for a cultural tourism quest app. Decide whether the "
    "photo satisfies the task requirement. Use only visible evidence in the "
    "image and the task text. Do not reward generic selfies or unrelated photos. "
    "Return strict JSON with keys: match (boolean), confidence (number from 0 "
    "to 1), reason (short sentence in the requested language)."
)


class CaptureJudge:
    """Verify a quest capture using the vLLM vision backend with safe fallback."""

    def __init__(self, gateway: Any | None = None) -> None:
        self._gateway = gateway or llm_gateway

    @traceable(name="Capture Judge Verify", run_type="chain")
    def verify(self, context: dict[str, Any]) -> dict[str, Any]:
        task = context.get("task") or {}
        capture = context.get("capture") or {}
        language = str(context.get("language") or task.get("language") or "vi")
        image_url = self._extract_image_url(capture)

        if not image_url:
            return self._result(
                match=False,
                confidence=0.0,
                reason=self._missing_image_reason(language),
                fallback=True,
                ai_generated=False,
            )

        if settings.llm_provider == "mock":
            return self._needs_review(language, "mock_mode")

        verdict = self._vision_verdict(task, capture, image_url, language)
        if not verdict:
            return self._needs_review(language, "vision_unavailable")

        match = bool(verdict.get("match", False))
        confidence = self._clamp_confidence(verdict.get("confidence", 0.0))
        reason = str(verdict.get("reason") or "").strip()
        if not reason:
            reason = self._default_reason(language, match)

        return self._result(
            match=match,
            confidence=confidence,
            reason=reason,
            fallback=False,
            ai_generated=True,
        )

    def _vision_verdict(
        self,
        task: dict[str, Any],
        capture: dict[str, Any],
        image_url: str,
        language: str,
    ) -> dict[str, Any]:
        user_prompt = (
            f"Language: {language}\n"
            f"Task title: {task.get('title', '')}\n"
            f"Task description: {task.get('description', '')}\n"
            f"Completion requirement: {task.get('completion_requirement', '')}\n"
            f"Required object/scenery: {task.get('required_object', '') or task.get('target_object', '')}\n"
            f"Place: {task.get('place_name', '') or task.get('place_id', '')}\n"
            f"User note: {capture.get('note', '')}\n"
            "Judge if the image clearly matches the requirement."
        )
        return self._gateway.generate_vision_json(
            system_prompt=_SYSTEM_PROMPT,
            user_prompt=user_prompt,
            image_url=image_url,
        )

    def _extract_image_url(self, capture: dict[str, Any]) -> str:
        for key in ("media_url", "image_url", "url", "data_url"):
            value = capture.get(key)
            if isinstance(value, str) and value.strip():
                return value.strip()

        raw_base64 = capture.get("base64") or capture.get("image_base64")
        if isinstance(raw_base64, str) and raw_base64.strip():
            mime = str(capture.get("mime_type") or "image/jpeg").strip() or "image/jpeg"
            encoded = raw_base64.strip()
            if encoded.startswith("data:image/"):
                return encoded
            return f"data:{mime};base64,{encoded}"

        return ""

    def _needs_review(self, language: str, reason_code: str) -> dict[str, Any]:
        if language.lower().startswith("en"):
            reason = (
                "The image was received, but automatic vision verification is "
                "temporarily unavailable. Please keep it for manual review."
            )
        else:
            reason = (
                "Đã nhận ảnh, nhưng hệ thống kiểm tra hình ảnh tạm thời chưa khả dụng. "
                "Vui lòng giữ ảnh để kiểm tra thủ công."
            )
        logger.info("Capture Judge fallback: %s", reason_code)
        return {
            "match": False,
            "status": "needs_review",
            "confidence": 0.2,
            "reason": reason,
            "fallback": True,
            "ai_generated": False,
            "reason_code": reason_code,
        }

    def _result(
        self,
        match: bool,
        confidence: float,
        reason: str,
        fallback: bool,
        ai_generated: bool,
    ) -> dict[str, Any]:
        status = "approved" if match and confidence >= 0.65 else "rejected"
        return {
            "match": match,
            "status": status,
            "confidence": confidence,
            "reason": reason,
            "fallback": fallback,
            "ai_generated": ai_generated,
        }

    @staticmethod
    def _clamp_confidence(value: Any) -> float:
        try:
            confidence = float(value)
        except (TypeError, ValueError):
            return 0.0
        return round(max(0.0, min(1.0, confidence)), 2)

    @staticmethod
    def _missing_image_reason(language: str) -> str:
        if language.lower().startswith("en"):
            return "No image evidence was provided for this task."
        return "Chưa có ảnh minh chứng cho nhiệm vụ này."

    @staticmethod
    def _default_reason(language: str, match: bool) -> str:
        if language.lower().startswith("en"):
            return "The photo matches the task." if match else "The photo does not clearly match the task."
        return "Ảnh phù hợp với nhiệm vụ." if match else "Ảnh chưa khớp rõ với nhiệm vụ."
