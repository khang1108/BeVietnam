from fastapi import APIRouter, Query
from app.schemas.schemas import StorylineNextTaskResponse, StorylineTask
from app.core.ai_core_client import ai_core_client

router = APIRouter()

# Mock task đầy đủ các trường theo Sprint 1 requirements
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


@router.get("/storyline/next-task", response_model=StorylineNextTaskResponse, tags=["Storyline"])
async def get_next_task(user_id: str = Query(..., description="ID của người dùng")):
    """
    GET /storyline/next-task — Task văn hoá tiếp theo cho user.

    Trả về đầy đủ: title, description, cultural_explanation,
    difficulty, completion_requirement, và place reference.
    Android hiển thị task mà không cần hardcode local mock.
    """
    # Thử gọi AI Core; nếu mock mode hoặc lỗi thì dùng mock task
    raw = await ai_core_client.generate_task(user_id=user_id)
    fallback = raw.get("fallback", False)

    if fallback or ai_core_client.use_mock:
        return StorylineNextTaskResponse(
            task=_MOCK_TASK,
            ai_generated=False,
            fallback=True,
        )

    task = StorylineTask(
        task_id=raw.get("task_id", _MOCK_TASK.task_id),
        title=raw.get("title", _MOCK_TASK.title),
        description=raw.get("description", _MOCK_TASK.description),
        cultural_explanation=raw.get("cultural_explanation", _MOCK_TASK.cultural_explanation),
        difficulty=raw.get("difficulty", "medium"),
        completion_requirement=raw.get("completion_requirement", _MOCK_TASK.completion_requirement),
        place_id=raw.get("place_id"),
        score=raw.get("score", 0.5),
    )
    return StorylineNextTaskResponse(task=task, ai_generated=True, fallback=False)
