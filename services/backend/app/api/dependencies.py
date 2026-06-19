from typing import AsyncGenerator

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError
from sqlalchemy.ext.asyncio import AsyncSession

from ..core.database import AsyncSessionLocal
from ..core.security import decode_token
from ..models.models import UserModel
from ..repositories.user_repository import UserRepository

bearer_scheme = HTTPBearer(auto_error=True)


async def get_db() -> AsyncGenerator:
    """Dependency provider cho FastAPI routes"""
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
    db: AsyncSession = Depends(get_db),
) -> UserModel:
    """Resolve the bearer JWT to the current user, or raise 401."""
    creds_exc = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid or expired token",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        user_id = decode_token(credentials.credentials).get("sub")
    except JWTError:
        raise creds_exc
    if not user_id:
        raise creds_exc
    user = await UserRepository(db).get_by_id(user_id)
    if user is None:
        raise creds_exc
    return user