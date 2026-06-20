"""Authentication use cases: register and login."""

from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.core.security import (
    create_access_token,
    hash_password,
    verify_password,
)
from services.backend.app.models.models import UserModel
from services.backend.app.repositories.user_repository import UserRepository


class AuthService:
    def __init__(self, db: AsyncSession):
        self.repo = UserRepository(db)

    async def register(self, name: str, email: str, password: str) -> UserModel:
        if await self.repo.get_by_email(email):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered",
            )
        return await self.repo.create(
            name=name,
            email=email,
            hashed_password=hash_password(password),
        )

    async def login(self, email: str, password: str) -> tuple[str, UserModel]:
        user = await self.repo.get_by_email(email)
        if not user or not verify_password(password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password",
                headers={"WWW-Authenticate": "Bearer"},
            )
        token = create_access_token({"sub": user.id})
        return token, user
