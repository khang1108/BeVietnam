from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.repositories.user_repository import UserRepository
from app.core.security import hash_password, verify_password, create_access_token
from app.core.logger import logger

class AuthService:
    def __init__(self, db: Session):
        self.repo = UserRepository(db)

    def register(self, email: str, password: str):
        if self.repo.get_by_email(email):
            logger.warning(f"Register failed: email already exists {email}")
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email already registered")
        user = self.repo.create(email=email, hashed_password=hash_password(password))
        logger.info(f"New user registered: {email}")
        return user

    def login(self, email: str, password: str) -> str:
        user = self.repo.get_by_email(email)
        if not user or not verify_password(password, user.hashed_password):
            logger.warning(f"Login failed for: {email}")
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")
        logger.info(f"User logged in: {email}")
        return create_access_token({"sub": str(user.id)})