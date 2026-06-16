"""
Schemas package — re-exports all schemas for backward compatibility.
Import directly from sub-modules for new code:
    from app.schemas.place import PlaceSchema, PlacesResponse
    from app.schemas.feed import FeedItem, FeedResponse
    ...
"""
from services.backend.app.schemas.base import HealthResponse
from services.backend.app.schemas.place import PlaceSchema, PlacesResponse
from services.backend.app.schemas.feed import FeedItem, FeedResponse
from services.backend.app.schemas.storyline import (
    QuestTask,
    QuestChainResponse,
    StorylineTask,
    StorylineNextTaskResponse,
    VerifyTaskCaptureBody,
    VerifyTaskCaptureResponse,
)
from services.backend.app.schemas.capture import CaptureCreateRequest, CaptureResponse
from services.backend.app.schemas.question_pool import (
    QuestionPoolItem,
    QuestionPoolResponse,
    RuntimeContext,
    RuntimeContextRequest,
    SelectedQuestion,
    SelectQuestionResponse,
)
from services.backend.app.schemas.weather import (
    WeatherBatchRequest,
    WeatherBatchResponse,
    WeatherBatchResult,
    WeatherCoord,
    WeatherResponse,
)
from services.backend.app.schemas.user import (
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
    "QuestionPoolItem", "QuestionPoolResponse",
    "RuntimeContext", "RuntimeContextRequest",
    "SelectedQuestion", "SelectQuestionResponse",
    "WeatherResponse", "WeatherCoord", "WeatherBatchRequest", "WeatherBatchResult",
    "WeatherBatchResponse",
    "UserRegisterRequest", "UserLoginRequest", "UserResponse", "TokenResponse",
    "LogEntry", "LogsResponse",
]
