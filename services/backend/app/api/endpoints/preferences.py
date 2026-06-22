"""Per-user preference endpoints — drive personalized feed ranking."""
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.api.dependencies import get_current_user, get_db
from services.backend.app.models.models import UserModel
from services.backend.app.repositories.preference_repository import PreferenceRepository
from services.backend.app.schemas.preference import PreferenceSchema

router = APIRouter()


@router.get("/me/preferences", response_model=PreferenceSchema, tags=["Preferences"])
async def get_my_preferences(
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Return the authenticated user's personalization settings (empty defaults if unset)."""
    pref = await PreferenceRepository(db).get(current_user.id)
    if pref is None:
        return PreferenceSchema()
    return PreferenceSchema(
        interests=list(pref.interests or []),
        home_latitude=pref.home_latitude,
        home_longitude=pref.home_longitude,
    )


@router.put("/me/preferences", response_model=PreferenceSchema, tags=["Preferences"])
async def update_my_preferences(
    body: PreferenceSchema,
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Create or update the authenticated user's personalization settings."""
    interests = [tag.strip().lower() for tag in body.interests if tag.strip()]
    pref = await PreferenceRepository(db).upsert(
        user_id=current_user.id,
        interests=interests,
        home_latitude=body.home_latitude,
        home_longitude=body.home_longitude,
    )
    return PreferenceSchema(
        interests=list(pref.interests or []),
        home_latitude=pref.home_latitude,
        home_longitude=pref.home_longitude,
    )
