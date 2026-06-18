"""
Schemas package — re-exports all schemas for backward compatibility.
Import directly from sub-modules for new code:
    from app.schemas.place import PlaceSchema, PlacesResponse
    from app.schemas.feed import FeedItem, FeedResponse
    ...
"""
from app.schemas.base import HealthResponse
from app.schemas.place import PlaceSchema, PlaceImportRequest, PlaceCreateRequest, PlacesResponse
from app.schemas.feed import FeedItem, FeedResponse
from app.schemas.storyline import (
    QuestTask,
    QuestChainResponse,
    StorylineTask,
    StorylineNextTaskResponse,
    VerifyTaskCaptureBody,
    VerifyTaskCaptureResponse,
)
from app.schemas.capture import CaptureCreateRequest, CaptureResponse
from app.schemas.user import (
    UserRegisterRequest,
    UserLoginRequest,
    UserResponse,
    TokenResponse,
    LogEntry,
    LogsResponse,
)

__all__ = [
    "HealthResponse",
    "PlaceSchema", "PlaceImportRequest", "PlaceCreateRequest", "PlacesResponse",
    "FeedItem", "FeedResponse",
    "QuestTask", "QuestChainResponse",
    "StorylineTask", "StorylineNextTaskResponse",
    "VerifyTaskCaptureBody", "VerifyTaskCaptureResponse",
    "CaptureCreateRequest", "CaptureResponse",
    "UserRegisterRequest", "UserLoginRequest", "UserResponse", "TokenResponse",
    "LogEntry", "LogsResponse",
]
