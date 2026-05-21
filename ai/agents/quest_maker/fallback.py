"""
Quest Maker Fallback — Curated Huế Quest Chain.

When Gemini fails or Safety Keeper rejects the output,
the workflow returns a task from this pre-built quest chain.

This guarantees the demo NEVER shows a blank screen.
The fallback chain covers a realistic 5-task Huế exploration
that feels like a real geocaching experience.
"""

# ── Fallback Quest Chain: Khám phá Kinh thành Huế ────────────────────────────
# A 5-task chain exploring Huế Imperial City and surroundings.

FALLBACK_QUEST_CHAIN: list[dict] = [
    {
        "quest_id": "quest-hue-imperial",
        "task_id": "hue-task-1",
        "step_index": 1,
        "title": "Khám phá Ngọ Môn — Cổng vào Hoàng thành",
        "description": (
            "Tìm và chụp ảnh Ngọ Môn — cổng chính phía nam của Hoàng thành Huế. "
            "Hãy chú ý đến Lầu Ngũ Phụng với 5 mái trên đỉnh cổng."
        ),
        "cultural_explanation": (
            "Ngọ Môn là nơi diễn ra các nghi lễ quan trọng nhất của triều Nguyễn. "
            "Lầu Ngũ Phụng có 5 mái tượng trưng cho Kim - Mộc - Thủy - Hỏa - Thổ."
        ),
        "completion_requirement": "Chụp một ảnh Ngọ Môn với metadata vị trí.",
        "unlock_condition": {
            "type": "capture_required",
            "requires_photo": True,
            "requires_location": True,
        },
        "next_task_hint": "Đằng sau cổng này là nơi vua thiết triều — hãy bước vào bên trong...",
        "difficulty": "easy",
        "reason_codes": ["culture", "history", "architecture", "photo_task"],
    },
    {
        "quest_id": "quest-hue-imperial",
        "task_id": "hue-task-2",
        "step_index": 2,
        "title": "Điện Thái Hòa — Nơi vua thiết triều",
        "description": (
            "Tìm Điện Thái Hòa bên trong Hoàng thành. "
            "Chụp ảnh chi tiết trang trí sơn son thếp vàng hoặc hình rồng phượng."
        ),
        "cultural_explanation": (
            "Điện Thái Hòa là trung tâm quyền lực của triều Nguyễn. "
            "Nội thất được trang trí bằng nghệ thuật sơn son thếp vàng tinh xảo."
        ),
        "completion_requirement": "Chụp một ảnh chi tiết trang trí bên trong hoặc ngoài Điện Thái Hòa.",
        "unlock_condition": {
            "type": "capture_required",
            "requires_photo": True,
            "requires_location": True,
        },
        "next_task_hint": "Vua không chỉ làm việc — vua còn nghỉ ngơi trong một khu vườn bí mật...",
        "difficulty": "easy",
        "reason_codes": ["culture", "history", "architecture", "detail_capture"],
    },
    {
        "quest_id": "quest-hue-imperial",
        "task_id": "hue-task-3",
        "step_index": 3,
        "title": "Tìm góc yên bình trong Tử Cấm Thành",
        "description": (
            "Khám phá Tử Cấm Thành — khu vực riêng tư của vua. "
            "Tìm và chụp ảnh một góc vườn, hồ nước, hoặc chi tiết kiến trúc yên tĩnh."
        ),
        "cultural_explanation": (
            "Tử Cấm Thành là nơi chỉ vua và gia đình được vào. "
            "Vườn Cơ Hạ bên trong là nơi vua giải trí với hồ sen và nhà thủy tạ."
        ),
        "completion_requirement": "Chụp một ảnh góc yên bình trong khu Tử Cấm Thành.",
        "unlock_condition": {
            "type": "capture_required",
            "requires_photo": True,
            "requires_location": False,
        },
        "next_task_hint": "Bên ngoài thành, có một dòng sông mang hương thơm của núi rừng...",
        "difficulty": "medium",
        "reason_codes": ["culture", "architecture", "observation", "nature"],
    },
    {
        "quest_id": "quest-hue-imperial",
        "task_id": "hue-task-4",
        "step_index": 4,
        "title": "Thưởng thức ẩm thực cung đình Huế",
        "description": (
            "Tìm một quán bán bánh bèo, bánh nậm, hoặc bánh lọc — "
            "ba loại bánh đặc trưng của ẩm thực cung đình Huế. Chụp ảnh món ăn."
        ),
        "cultural_explanation": (
            "Bánh bèo, bánh nậm, bánh lọc thể hiện triết lý ẩm thực Huế: "
            "phần ăn nhỏ, trình bày đẹp, hương vị tinh tế. "
            "Người Huế thường thưởng thức theo bộ ba."
        ),
        "completion_requirement": "Chụp ảnh một trong ba loại bánh Huế và upload.",
        "unlock_condition": {
            "type": "capture_required",
            "requires_photo": True,
            "requires_location": False,
        },
        "next_task_hint": "Hành trình kết thúc bên dòng sông nơi âm nhạc cung đình vang lên...",
        "difficulty": "easy",
        "reason_codes": ["food", "culture", "local_life", "photo_task"],
    },
    {
        "quest_id": "quest-hue-imperial",
        "task_id": "hue-task-5",
        "step_index": 5,
        "title": "Sông Hương lúc hoàng hôn",
        "description": (
            "Đến bờ sông Hương trước hoàng hôn. "
            "Chụp ảnh dòng sông với cầu Tràng Tiền hoặc Chùa Thiên Mụ ở phía xa."
        ),
        "cultural_explanation": (
            "Sông Hương là linh hồn của Huế, được đặt tên theo hương thơm "
            "của các loài hoa rừng thượng nguồn. Ca Huế trên sông Hương là trải nghiệm "
            "văn hóa độc đáo chỉ có ở đây."
        ),
        "completion_requirement": "Chụp ảnh sông Hương với cảnh hoàng hôn hoặc cầu Tràng Tiền.",
        "unlock_condition": {
            "type": "capture_required",
            "requires_photo": True,
            "requires_location": True,
        },
        "next_task_hint": "Bạn đã hoàn thành hành trình khám phá Kinh thành Huế! 🎉",
        "difficulty": "easy",
        "reason_codes": ["nature", "culture", "scenic", "quest_finale"],
    },
]


def get_fallback_task(step_index: int = 1) -> dict:
    """
    Return a specific fallback task by step index.

    Args:
        step_index: Which task in the chain to return (1-5).

    Returns:
        Task dict from the fallback chain.
        If step_index is out of range, returns the first task.
    """
    idx = step_index - 1
    if 0 <= idx < len(FALLBACK_QUEST_CHAIN):
        return FALLBACK_QUEST_CHAIN[idx]
    return FALLBACK_QUEST_CHAIN[0]


def get_fallback_chain() -> list[dict]:
    """Return the entire fallback quest chain."""
    return FALLBACK_QUEST_CHAIN
