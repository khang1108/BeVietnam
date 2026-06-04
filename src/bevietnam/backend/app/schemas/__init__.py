"""
Schemas package — re-exports all schemas for backward compatibility.
Import directly from sub-modules for new code:
    from app.schemas.place import PlaceSchema, PlacesResponse
    from app.schemas.feed import FeedItem, FeedResponse
    ...
"""
from src.bevietnam.backend.app.schemas.base import HealthResponse
from src.bevietnam.backend.app.schemas.place import PlaceSchema, PlacesResponse
from src.bevietnam.backend.app.schemas.feed import FeedItem, FeedResponse
from src.bevietnam.backend.app.schemas.storyline import (
    QuestTask,
    QuestChainResponse,
    StorylineTask,
    StorylineNextTaskResponse,
    VerifyTaskCaptureBody,
    VerifyTaskCaptureResponse,
)
from src.bevietnam.backend.app.schemas.capture import CaptureCreateRequest, CaptureResponse
from src.bevietnam.backend.app.schemas.user import (
    UserRegisterRequest,
    UserLoginRequest,
    UserResponse,
    TokenResponse,
    LogEntry,
    LogsResponse,
)

__all__ = [
    "HealthResponse",
    "PlaceSchema", "PlacesResponse",
    "FeedItem", "FeedResponse",
    "QuestTask", "QuestChainResponse",
    "StorylineTask", "StorylineNextTaskResponse",
    "VerifyTaskCaptureBody", "VerifyTaskCaptureResponse",
    "CaptureCreateRequest", "CaptureResponse",
    "UserRegisterRequest", "UserLoginRequest", "UserResponse", "TokenResponse",
    "LogEntry", "LogsResponse",
]
