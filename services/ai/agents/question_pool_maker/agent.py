"""Question Pool Maker agent.

Turns grounded cultural facts into reusable questions/tasks. Runtime selection
belongs to the backend; this agent only generates structured candidates.
"""

from __future__ import annotations

import logging
import uuid
from typing import Any

from src.bevietnam.ai.agents.question_pool_maker.prompts import (
    SYSTEM_PROMPT,
    build_user_prompt,
)
from src.bevietnam.ai.agents.safety_keeper import SafetyKeeper
from src.bevietnam.ai.common.llm import llm_gateway

logger = logging.getLogger(__name__)

_REQUIRED_FIELDS = [
    "title",
    "question_text",
    "cultural_explanation",
    "source_text",
    "source",
    "place_name",
    "categories",
    "difficulty",
    "required_media",
    "indoor_outdoor",
    "weather_tags",
    "time_tags",
    "estimated_duration_minutes",
]


class QuestionPoolMaker:
    """Generate reusable question-pool items from cultural facts."""

    def __init__(self) -> None:
        self.used_fallback = False

    def generate(
        self,
        facts: list[dict[str, Any]],
        place_name: str = "",
        language: str = "vi",
        max_questions: int = 20,
    ) -> list[dict[str, Any]]:
        if not facts:
            return []

        prompt = build_user_prompt(
            facts=facts,
            place_name=place_name,
            language=language,
            max_questions=max_questions,
        )
        raw = llm_gateway.generate_json(
            system_prompt=SYSTEM_PROMPT,
            user_prompt=prompt,
        )
        questions = raw.get("questions") if isinstance(raw, dict) else None
        if not isinstance(questions, list):
            logger.info("Question Pool Maker using deterministic fallback.")
            self.used_fallback = True
            questions = self._fallback_questions(facts, place_name, max_questions)

        keeper = SafetyKeeper()
        normalized: list[dict[str, Any]] = []
        for raw_question in questions[:max_questions]:
            if not isinstance(raw_question, dict):
                continue
            item = self._normalize_question(raw_question, place_name, language)
            validation_item = {**item, "description": item["question_text"]}
            valid, errors = keeper.validate_full(validation_item, _REQUIRED_FIELDS)
            if valid:
                normalized.append(item)
            else:
                logger.warning("Question rejected: %s", errors)

        if normalized:
            return normalized
        return [
            self._normalize_question(item, place_name, language)
            for item in self._fallback_questions(facts, place_name, max_questions)
        ]

    def _normalize_question(
        self,
        raw: dict[str, Any],
        default_place: str,
        language: str,
    ) -> dict[str, Any]:
        source_text = str(raw.get("source_text") or raw.get("cultural_explanation") or "")
        place_name = str(raw.get("place_name") or default_place or "Việt Nam")
        question_id = raw.get("question_id") or f"q-{uuid.uuid4().hex[:12]}"
        return {
            "question_id": str(question_id),
            "title": str(raw.get("title") or "Khám phá văn hóa"),
            "question_text": str(raw.get("question_text") or raw.get("description") or ""),
            "cultural_explanation": str(raw.get("cultural_explanation") or source_text),
            "source_text": source_text,
            "source": str(raw.get("source") or ""),
            "place_name": place_name,
            "latitude": raw.get("latitude"),
            "longitude": raw.get("longitude"),
            "radius_meters": int(raw.get("radius_meters") or 1200),
            "categories": self._normalize_list(raw.get("categories") or raw.get("reason_codes") or ["culture"]),
            "difficulty": str(raw.get("difficulty") or "easy"),
            "required_media": str(raw.get("required_media") or "photo"),
            "indoor_outdoor": str(raw.get("indoor_outdoor") or "any"),
            "weather_tags": self._normalize_list(raw.get("weather_tags") or ["any"]),
            "time_tags": self._normalize_list(raw.get("time_tags") or ["any"]),
            "estimated_duration_minutes": int(raw.get("estimated_duration_minutes") or 15),
            "language": language,
            "metadata": raw.get("metadata") if isinstance(raw.get("metadata"), dict) else {},
        }

    def _fallback_questions(
        self,
        facts: list[dict[str, Any]],
        default_place: str,
        max_questions: int,
    ) -> list[dict[str, Any]]:
        questions: list[dict[str, Any]] = []
        for fact in facts[:max_questions]:
            text = str(fact.get("text") or "")
            place_name = str(fact.get("place_name") or default_place or "Việt Nam")
            category = str(fact.get("category") or "culture")
            questions.append(
                {
                    "title": f"Quan sát {place_name}",
                    "question_text": (
                        "Tìm một chi tiết tại địa điểm này liên quan đến sự kiện, kiến trúc, "
                        "hoặc phong tục trong tư liệu và ghi lại bằng ảnh hoặc ghi chú."
                    ),
                    "cultural_explanation": text,
                    "source_text": text,
                    "source": str(fact.get("source") or ""),
                    "place_name": place_name,
                    "categories": [category],
                    "difficulty": "easy",
                    "required_media": "photo",
                    "indoor_outdoor": "any",
                    "weather_tags": ["any"],
                    "time_tags": ["any"],
                    "estimated_duration_minutes": 15,
                }
            )
        return questions

    def _normalize_list(self, value: Any) -> list[str]:
        if isinstance(value, list):
            result = [str(item).strip().lower() for item in value if str(item).strip()]
            return result or ["any"]
        if isinstance(value, str) and value.strip():
            return [value.strip().lower()]
        return ["any"]
