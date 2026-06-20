"""Async data access for users."""

import uuid

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.models.models import UserModel


class UserRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_email(self, email: str) -> UserModel | None:
        normalized = email.strip().lower()
        result = await self.db.execute(
            select(UserModel).where(UserModel.email == normalized)
        )
        return result.scalar_one_or_none()

    async def get_by_id(self, user_id: str) -> UserModel | None:
        result = await self.db.execute(select(UserModel).where(UserModel.id == user_id))
        return result.scalar_one_or_none()

    async def create(self, name: str, email: str, hashed_password: str) -> UserModel:
        user = UserModel(
            id=str(uuid.uuid4()),
            name=name.strip(),
            email=email.strip().lower(),
            hashed_password=hashed_password,
        )
        self.db.add(user)
        await self.db.commit()
        await self.db.refresh(user)
        return user
