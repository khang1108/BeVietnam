"""Question pool loading, context resolution, and selection logic."""

from __future__ import annotations

import json
import logging
import math
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import httpx

from services.backend.app.core.config import settings
from services.backend.app.core.rate_limit import goong_limiter
from services.backend.app.schemas.question_pool import (
    QuestionPoolItem,
    QuestionPoolResponse,
    RuntimeContext,
    RuntimeContextRequest,
    SelectQuestionResponse,
    SelectedQuestion,
)
from services.backend.app.services.weather_service import weather_service

logger = logging.getLogger(__name__)


_FALLBACK_POOL = [
    QuestionPoolItem(
        question_id="fallback-hue-ngo-mon-detail",
        title="Quan sát biểu tượng ở Ngọ Môn",
        question_text="Tìm một chi tiết kiến trúc tại Ngọ Môn thể hiện quyền lực hoàng gia và chụp ảnh lại.",
        cultural_explanation=(
            "Ngọ Môn là cổng chính phía nam của Hoàng thành Huế, gắn với các nghi lễ quan trọng của triều Nguyễn."
        ),
        source_text="Fallback cultural task for Hue Imperial City.",
        source="fallback",
        place_name="Kinh thành Huế",
        latitude=16.4692,
        longitude=107.5775,
        radius_meters=1800,
        categories=["history", "architecture", "culture"],
        difficulty="easy",
        required_media="photo",
        indoor_outdoor="outdoor",
        weather_tags=["sunny", "cloudy", "any"],
        time_tags=["morning", "afternoon", "any"],
        estimated_duration_minutes=20,
    ),
    QuestionPoolItem(
        question_id="fallback-hue-rainy-court-music",
        title="Tìm câu chuyện nhã nhạc cung đình",
        question_text="Khi trời mưa, hãy tìm một bảng thông tin hoặc khu trưng bày nhắc đến nhã nhạc cung đình Huế.",
        cultural_explanation=(
            "Nhã nhạc cung đình Huế là di sản văn hóa phi vật thể, phản ánh đời sống nghi lễ của triều Nguyễn."
        ),
        source_text="Fallback indoor rainy-day cultural task for Hue.",
        source="fallback",
        place_name="Kinh thành Huế",
        latitude=16.4692,
        longitude=107.5775,
        radius_meters=2200,
        categories=["history", "art", "tradition"],
        difficulty="easy",
        required_media="photo",
        indoor_outdoor="indoor",
        weather_tags=["rainy", "any"],
        time_tags=["morning", "afternoon", "any"],
        estimated_duration_minutes=15,
    ),
]


def _repo_root() -> Path:
    return Path(__file__).resolve().parents[4]


def _resolve_pool_path() -> Path:
    configured = Path(settings.QUESTION_POOL_PATH).expanduser()
    if configured.is_absolute():
        return configured
    return _repo_root() / configured


def _normalize_tag(value: str | None, fallback: str = "any") -> str:
    if not value:
        return fallback
    return value.strip().lower().replace(" ", "_") or fallback


def _infer_time_of_day(now: datetime | None = None) -> str:
    hour = (now or datetime.now()).hour
    if 5 <= hour < 11:
        return "morning"
    if 11 <= hour < 17:
        return "afternoon"
    if 17 <= hour < 21:
        return "evening"
    return "night"


def _distance_meters(
    lat1: float,
    lon1: float,
    lat2: float | None,
    lon2: float | None,
) -> float | None:
    if lat2 is None or lon2 is None:
        return None
    radius = 6_371_000
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)
    a = (
        math.sin(delta_phi / 2) ** 2
        + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2
    )
    return radius * (2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)))


class GoongContextResolver:
    """Small Goong REST adapter. Safe no-op until GOONG_API_KEY is configured."""

    async def reverse_geocode(self, latitude: float, longitude: float) -> dict[str, str]:
        if not settings.GOONG_API_KEY:
            return {}

        if not await goong_limiter.allow():
            return {}

        try:
            async with httpx.AsyncClient(timeout=settings.GOONG_TIMEOUT) as client:
                response = await client.get(
                    f"{settings.GOONG_BASE_URL.rstrip('/')}/Geocode",
                    params={
                        "latlng": f"{latitude},{longitude}",
                        "api_key": settings.GOONG_API_KEY,
                    },
                )
                response.raise_for_status()
                payload = response.json()
        except (httpx.RequestError, httpx.HTTPStatusError, ValueError) as exc:
            logger.warning("Goong reverse geocode failed: %s", exc)
            return {}

        results = payload.get("results")
        if not isinstance(results, list) or not results:
            return {}

        first = results[0] if isinstance(results[0], dict) else {}
        return {
            "formatted_address": str(first.get("formatted_address") or ""),
            "place_name": str(first.get("name") or first.get("formatted_address") or ""),
        }


class QuestionPoolService:
    """Selects context-appropriate questions from a durable pool."""

    def __init__(self) -> None:
        self._goong = GoongContextResolver()

    def load_pool(self) -> tuple[list[QuestionPoolItem], bool, Path]:
        path = _resolve_pool_path()
        if not path.exists():
            return list(_FALLBACK_POOL), True, path

        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            logger.warning("Question pool load failed from %s: %s", path, exc)
            return list(_FALLBACK_POOL), True, path

        raw_items = payload.get("questions", payload) if isinstance(payload, dict) else payload
        if not isinstance(raw_items, list):
            return list(_FALLBACK_POOL), True, path

        items: list[QuestionPoolItem] = []
        for raw in raw_items:
            if not isinstance(raw, dict):
                continue
            try:
                items.append(QuestionPoolItem(**raw))
            except Exception as exc:
                logger.warning("Skipping invalid question pool item: %s", exc)

        return (items or list(_FALLBACK_POOL)), not bool(items), path

    def list_questions(self) -> QuestionPoolResponse:
        items, fallback, path = self.load_pool()
        return QuestionPoolResponse(
            total=len(items),
            items=items,
            source_path=str(path),
            fallback=fallback,
        )

    async def resolve_context(self, request: RuntimeContextRequest) -> RuntimeContext:
        goong_context = await self._goong.reverse_geocode(
            latitude=request.latitude,
            longitude=request.longitude,
        )
        weather = request.weather
        if weather is None:
            weather_result = await weather_service.get_condition(
                lat=request.latitude,
                lng=request.longitude,
            )
            weather = weather_result.condition

        source = "goong" if goong_context else "local"
        return RuntimeContext(
            latitude=request.latitude,
            longitude=request.longitude,
            weather=_normalize_tag(weather),
            time_of_day=_normalize_tag(request.time_of_day, _infer_time_of_day()),
            formatted_address=goong_context.get("formatted_address", ""),
            place_name=goong_context.get("place_name", ""),
            source=source,
            resolved_at=datetime.now(timezone.utc),
        )

    async def select(self, request: RuntimeContextRequest) -> SelectQuestionResponse:
        pool, fallback, _ = self.load_pool()
        context = await self.resolve_context(request)
        completed = set(request.completed_question_ids)
        selected: list[SelectedQuestion] = []

        for question in pool:
            if question.question_id in completed:
                continue
            score, reasons, distance = self._score_question(question, context, request)
            selected.append(
                SelectedQuestion(
                    question=question,
                    score=round(score, 2),
                    reasons=reasons,
                    distance_meters=round(distance, 1) if distance is not None else None,
                )
            )

        selected.sort(key=lambda item: item.score, reverse=True)
        return SelectQuestionResponse(
            context=context,
            selected=selected[: request.limit],
            total_pool_size=len(pool),
            fallback=fallback,
        )

    def _score_question(
        self,
        question: QuestionPoolItem,
        context: RuntimeContext,
        request: RuntimeContextRequest,
    ) -> tuple[float, list[str], float | None]:
        score = 0.0
        reasons: list[str] = []

        distance = _distance_meters(
            context.latitude,
            context.longitude,
            question.latitude,
            question.longitude,
        )
        if distance is not None:
            if distance <= question.radius_meters:
                score += 40
                reasons.append("near_user")
            else:
                score += max(0, 25 - (distance - question.radius_meters) / 200)
        elif context.place_name and question.place_name:
            haystack = f"{context.place_name} {context.formatted_address}".lower()
            if question.place_name.lower() in haystack:
                score += 25
                reasons.append("place_name_match")

        weather_tags = {_normalize_tag(tag) for tag in question.weather_tags}
        if context.weather in weather_tags:
            score += 20
            reasons.append("weather_match")
        elif "any" in weather_tags:
            score += 8
            reasons.append("weather_flexible")
        elif context.weather == "rainy" and question.indoor_outdoor == "outdoor":
            score -= 25
            reasons.append("rainy_outdoor_penalty")

        if context.weather == "rainy" and question.indoor_outdoor == "indoor":
            score += 12
            reasons.append("rainy_indoor_bonus")

        time_tags = {_normalize_tag(tag) for tag in question.time_tags}
        if context.time_of_day in time_tags:
            score += 12
            reasons.append("time_match")
        elif "any" in time_tags:
            score += 4
            reasons.append("time_flexible")

        interest_tags = {_normalize_tag(tag) for tag in request.interests}
        question_tags = {_normalize_tag(tag) for tag in question.categories}
        overlap = interest_tags & question_tags
        if overlap:
            score += 10 + 5 * len(overlap)
            reasons.append("interest_match")

        if question.difficulty == "easy":
            score += 3
        if not reasons:
            reasons.append("general_best_available")

        return score, reasons, distance


question_pool_service = QuestionPoolService()
