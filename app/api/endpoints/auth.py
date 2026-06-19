from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.auth_service import AuthService
from app.schemas.schemas import RegisterRequest, LoginRequest, TokenResponse, UserResponse
from app.api.dependencies import get_current_user
from app.models.user import User

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/register", response_model=UserResponse, status_code=201)
def register(body: RegisterRequest, db: Session = Depends(get_db)):
    return AuthService(db).register(email=body.email, password=body.password)

@router.post("/login", response_model=TokenResponse)
def login(body: LoginRequest, db: Session = Depends(get_db)):
    auth_service = AuthService(db)
    token = auth_service.login(email=body.email, password=body.password)
    user = auth_service.get_user_by_email(body.email)
    return TokenResponse(access_token=token, token_type="bearer", user=user)

@router.get("/me", response_model=UserResponse)
def me(current_user: User = Depends(get_current_user)):
    return current_user