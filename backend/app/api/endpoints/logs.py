from fastapi import APIRouter
from datetime import datetime, timezone
from app.schemas.schemas import LogsResponse, LogEntry

router = APIRouter()

_MOCK_LOGS = [
    LogEntry(level="INFO", message="Backend started successfully.", timestamp=datetime.now(timezone.utc)),
    LogEntry(level="INFO", message="AI Core mock mode enabled.", timestamp=datetime.now(timezone.utc)),
]


@router.get("/logs", response_model=LogsResponse, tags=["Logs"])
async def get_logs():
    """
    GET /logs — Trả về logs hệ thống (dùng cho debug).
    """
    return LogsResponse(logs=_MOCK_LOGS)
