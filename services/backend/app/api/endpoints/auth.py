"""Auth endpoints: register, login (JWT), me."""
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from ...models.models import UserModel
from ...schemas.user import (
    TokenResponse,
    UserLoginRequest,
    UserRegisterRequest,
    UserResponse,
)
from ...services.auth_service import AuthService
from ..dependencies import get_current_user, get_db

router = APIRouter(prefix="/auth", tags=["Auth"])


def _to_user_response(user: UserModel) -> UserResponse:
    return UserResponse(
        id=user.id, name=user.name, email=user.email, created_at=user.created_at
    )


@router.post("/register", response_model=UserResponse, status_code=201)
async def register(body: UserRegisterRequest, db: AsyncSession = Depends(get_db)):
    user = await AuthService(db).register(body.name, body.email, body.password)
    return _to_user_response(user)


@router.post("/login", response_model=TokenResponse)
async def login(body: UserLoginRequest, db: AsyncSession = Depends(get_db)):
    token, user = await AuthService(db).login(body.email, body.password)
    return TokenResponse(access_token=token, user=_to_user_response(user))


@router.get("/me", response_model=UserResponse)
async def me(current_user: UserModel = Depends(get_current_user)):
    return _to_user_response(current_user)
