"""Shared base schemas."""
from pydantic import BaseModel
from datetime import datetime


class HealthResponse(BaseModel):
    status: str = "ok"
    version: str
    timestamp: datetime
