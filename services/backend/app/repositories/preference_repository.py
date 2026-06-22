"""Async data access for per-user preferences."""

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.models.models import UserPreferenceModel


class PreferenceRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get(self, user_id: str) -> UserPreferenceModel | None:
        result = await self.db.execute(
            select(UserPreferenceModel).where(UserPreferenceModel.user_id == user_id)
        )
        return result.scalar_one_or_none()

    async def upsert(
        self,
        user_id: str,
        interests: list[str],
        home_latitude: float | None,
        home_longitude: float | None,
    ) -> UserPreferenceModel:
        pref = await self.get(user_id)
        if pref is None:
            pref = UserPreferenceModel(user_id=user_id)
            self.db.add(pref)
        pref.interests = interests
        pref.home_latitude = home_latitude
        pref.home_longitude = home_longitude
        await self.db.commit()
        await self.db.refresh(pref)
        return pref
