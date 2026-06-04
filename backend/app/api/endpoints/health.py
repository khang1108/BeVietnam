from fastapi import APIRouter
from datetime import datetime, timezone
from app.schemas import HealthResponse
from app.core.config import settings

router = APIRouter()


@router.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """
    Health check endpoint.
    Android & web smoke tests gọi endpoint này để kiểm tra backend alive.
    """
    return HealthResponse(
        status="ok",
        version=settings.VERSION,
        timestamp=datetime.now(timezone.utc),
    )
