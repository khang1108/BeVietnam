"""Storyline schemas — API contract for quest chain and task generation."""
from pydantic import BaseModel, Field
from typing import Optional, List


class QuestTask(BaseModel):
    """A single task in a quest chain (Duolingo-style node)."""
    quest_id: str = ""
    task_id: str
    step_index: int = 1
    title: str
    description: str
    cultural_explanation: str = Field(description="Giải thích văn hoá/lịch sử")
    completion_requirement: str = Field(description="Điều kiện hoàn thành task")
    unlock_condition: dict = Field(default_factory=dict)
    next_task_hint: str = ""
    difficulty: str = Field(default="easy", description="easy | medium | hard")
    reason_codes: list[str] = []
    place_id: Optional[str] = None
    status: str = Field(default="locked", description="locked | active | completed")


class QuestChainResponse(BaseModel):
    """Full quest chain for the Duolingo-style path UI."""
    quest_id: str
    place_name: str
    total_tasks: int
    current_step: int = 1
    tasks: List[QuestTask]


class StorylineTask(BaseModel):
    """Storyline task schema."""
    task_id: str
    title: str
    description: str
    cultural_explanation: str = Field(description="Giải thích văn hoá/lịch sử")
    difficulty: str = Field(description="easy | medium | hard")
    completion_requirement: str = Field(description="Điều kiện hoàn thành task")
    place_id: Optional[str] = None
    score: float = Field(ge=0.0, le=1.0)


class StorylineNextTaskResponse(BaseModel):
    task: StorylineTask
    ai_generated: bool
    fallback: bool = False


class VerifyTaskCaptureBody(BaseModel):
    """Web/Android: gửi task hiện tại + capture."""
    user_id: str
    task: dict = Field(description="Quest task như đang hiển thị")
    capture: dict = Field(
        default_factory=dict,
        description="media_url, note, place_id,...",
    )


class VerifyTaskCaptureResponse(BaseModel):
    approved: bool
    status: str
    reason: str = ""
    confidence: float = 0.0
