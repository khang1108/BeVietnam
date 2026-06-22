"""Personalized recommendation feed — ranked per authenticated user."""
from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.api.dependencies import get_current_user, get_db
from services.backend.app.models.models import UserModel
from services.backend.app.repositories.preference_repository import PreferenceRepository
from services.backend.app.schemas.feed import FeedResponse
from services.backend.app.services.post_service import post_service
from services.backend.app.services.services import capture_service

router = APIRouter()


@router.get("/feed", response_model=FeedResponse, tags=["Feed"])
async def get_feed(
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    weather: Optional[str] = Query(None, description="Override weather context, e.g. rainy/sunny"),
    time_of_day: Optional[str] = Query(None, description="Override time context, e.g. morning"),
    limit: int = Query(20, ge=1, le=50),
):
    """
    GET /feed — Danh sách bài viết gợi ý, xếp hạng riêng cho từng user.

    Ranking dựa trên sở thích (interests), bối cảnh thời tiết/thời điểm,
    và loại bỏ những địa điểm user đã ghé (novelty). Yêu cầu đăng nhập.
    """
    pref = await PreferenceRepository(db).get(current_user.id)
    interests = list(pref.interests or []) if pref else []
    visited = capture_service.get_visited_place_ids(current_user.id)

    items = post_service.rank(
        interests=interests,
        visited_place_ids=visited,
        weather=weather,
        time_of_day=time_of_day,
        limit=limit,
    )
    return FeedResponse(items=items)
