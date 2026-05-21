"""
Storyline Endpoints — Quest chain and task generation.

Provides:
  - GET /storyline/quest          → Full quest chain for Duolingo-style path UI
  - POST /storyline/verify-capture → Capture Judge — ảnh minh chứng hoàn thành task
  - GET /storyline/next-task      → Generate next AI-powered cultural task
"""

from fastapi import APIRouter, Query
from app.schemas.schemas import (
    QuestChainResponse,
    QuestTask,
    StorylineNextTaskResponse,
    StorylineTask,
    VerifyTaskCaptureBody,
    VerifyTaskCaptureResponse,
)
from app.core.ai_core_client import ai_core_client

router = APIRouter()

# ── Mock fallback task (used when AI Core is fully unavailable) ───────────────
_MOCK_TASK = StorylineTask(
    task_id="task-001",
    title="Khám phá Văn Miếu - Quốc Tử Giám",
    description="Đến thăm trường đại học đầu tiên của Việt Nam, được xây dựng năm 1070 dưới triều Lý Thánh Tông.",
    cultural_explanation=(
        "Văn Miếu là nơi thờ Khổng Tử và các bậc hiền triết Nho giáo. "
        "Quốc Tử Giám là trường đại học quốc gia đầu tiên của Việt Nam, "
        "đào tạo nhân tài cho đất nước suốt gần 700 năm từ thế kỷ XI đến XIX."
    ),
    difficulty="easy",
    completion_requirement="Chụp ảnh tại Khuê Văn Các và upload lên ứng dụng.",
    place_id="place-001",
    score=0.92,
)


@router.get("/storyline/quest", response_model=QuestChainResponse, tags=["Storyline"])
async def get_quest_chain(
    user_id: str = Query("demo-user", description="ID của người dùng"),
    quest_id: str = Query("quest-hue-imperial", description="ID của quest chain"),
):
    """
    GET /storyline/quest — Full quest chain for the Duolingo-style path UI.

    Returns all tasks in the chain with their status (locked/active/completed).
    The website uses this to render the connected path nodes.
    """
    raw = await ai_core_client.get_quest_chain(quest_id=quest_id)

    if raw.get("fallback") or raw.get("error"):
        # Return a minimal chain if AI Core is unavailable
        return QuestChainResponse(
            quest_id=quest_id,
            place_name="Kinh thành Huế",
            total_tasks=1,
            current_step=1,
            tasks=[
                QuestTask(
                    task_id=_MOCK_TASK.task_id,
                    title=_MOCK_TASK.title,
                    description=_MOCK_TASK.description,
                    cultural_explanation=_MOCK_TASK.cultural_explanation,
                    completion_requirement=_MOCK_TASK.completion_requirement,
                    difficulty=_MOCK_TASK.difficulty,
                    status="active",
                )
            ],
        )

    # Build QuestTask objects from AI Core response
    tasks = []
    for i, t in enumerate(raw.get("tasks", [])):
        # First task is active, rest are locked (for demo)
        status = "active" if i == 0 else "locked"
        tasks.append(
            QuestTask(
                quest_id=t.get("quest_id", quest_id),
                task_id=t.get("task_id", f"task-{i+1}"),
                step_index=t.get("step_index", i + 1),
                title=t.get("title", ""),
                description=t.get("description", ""),
                cultural_explanation=t.get("cultural_explanation", ""),
                completion_requirement=t.get("completion_requirement", ""),
                unlock_condition=t.get("unlock_condition", {}),
                next_task_hint=t.get("next_task_hint", ""),
                difficulty=t.get("difficulty", "easy"),
                reason_codes=t.get("reason_codes", []),
                status=status,
            )
        )

    return QuestChainResponse(
        quest_id=raw.get("quest_id", quest_id),
        place_name=raw.get("place_name", "Huế"),
        total_tasks=raw.get("total_tasks", len(tasks)),
        current_step=1,
        tasks=tasks,
    )


@router.post(
    "/storyline/verify-capture",
    response_model=VerifyTaskCaptureResponse,
    tags=["Storyline"],
)
async def verify_task_capture(body: VerifyTaskCaptureBody):
    """
    Gửi ảnh (data URL trong demo) lên Capture Judge qua AI Core.

    Nếu được chấp nhận, client cập nhật chuỗi quest (unlock bước kế).
    """
    raw = await ai_core_client.verify_capture(
        user_id=body.user_id,
        task=body.task,
        capture=body.capture,
    )
    inner: dict = {}
    if isinstance(raw, dict):
        err = raw.get("error")
        fb = raw.get("fallback")
        if not (fb and err):
            data = raw.get("data")
            if isinstance(data, dict):
                inner = data

    status = str(inner.get("status", "error"))
    approved = status == "approved"
    reason = inner.get("reason", "")
    conf = inner.get("confidence", 0.0)

    return VerifyTaskCaptureResponse(
        approved=approved,
        status=status,
        reason=str(reason or ""),
        confidence=float(conf) if conf is not None else 0.0,
    )


@router.get("/storyline/next-task", response_model=StorylineNextTaskResponse, tags=["Storyline"])
async def get_next_task(user_id: str = Query(..., description="ID của người dùng")):
    """
    GET /storyline/next-task — Generate the next cultural task via AI Core.

    Calls the Quest Maker pipeline:
    Culture Scout (Qdrant) → Quest Maker (Gemini) → Safety Keeper → Publisher.

    Falls back to mock task if AI Core is unavailable.
    """
    # Call AI Core to generate a task
    raw = await ai_core_client.generate_task(user_id=user_id)
    fallback = raw.get("fallback", False)

    if fallback or ai_core_client.use_mock:
        return StorylineNextTaskResponse(
            task=_MOCK_TASK,
            ai_generated=False,
            fallback=True,
        )

    # Extract task data from Publisher-wrapped response
    task_data = raw.get("data", raw)

    task = StorylineTask(
        task_id=task_data.get("task_id", _MOCK_TASK.task_id),
        title=task_data.get("title", _MOCK_TASK.title),
        description=task_data.get("description", _MOCK_TASK.description),
        cultural_explanation=task_data.get("cultural_explanation", _MOCK_TASK.cultural_explanation),
        difficulty=task_data.get("difficulty", "medium"),
        completion_requirement=task_data.get("completion_requirement", _MOCK_TASK.completion_requirement),
        place_id=task_data.get("place_id"),
        score=task_data.get("score", 0.5),
    )

    ai_generated = raw.get("metadata", {}).get("ai_generated", True)
    return StorylineNextTaskResponse(
        task=task,
        ai_generated=ai_generated,
        fallback=not ai_generated,
    )
