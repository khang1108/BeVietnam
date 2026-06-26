"""Auth endpoints: register, login, and current user."""

from fastapi import APIRouter, Depends, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.api.dependencies import get_current_user, get_db
from services.backend.app.models.models import UserModel
from services.backend.app.schemas.user import (
    TokenResponse,
    UserLoginRequest,
    UserRegisterRequest,
    UserResponse,
)
from services.backend.app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["Auth"])


def _to_user_response(user: UserModel) -> UserResponse:
    return UserResponse(
        id=user.id,
        name=user.name,
        email=user.email,
        created_at=user.created_at,
    )


@router.post(
    "/register",
    response_model=TokenResponse,
    status_code=status.HTTP_201_CREATED,
)
async def register(body: UserRegisterRequest, db: AsyncSession = Depends(get_db)):
    token, user = await AuthService(db).register(body.name, body.email, body.password)
    return TokenResponse(access_token=token, user=_to_user_response(user))


@router.post("/login", response_model=TokenResponse)
async def login(body: UserLoginRequest, db: AsyncSession = Depends(get_db)):
    token, user = await AuthService(db).login(body.email, body.password)
    return TokenResponse(access_token=token, user=_to_user_response(user))


@router.post("/token", response_model=TokenResponse)
async def token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: AsyncSession = Depends(get_db),
):
    token_value, user = await AuthService(db).login(
        form_data.username,
        form_data.password,
    )
    return TokenResponse(access_token=token_value, user=_to_user_response(user))


@router.get("/me", response_model=UserResponse)
async def me(current_user: UserModel = Depends(get_current_user)):
    return _to_user_response(current_user)
