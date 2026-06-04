"""
Culture Scout Fallback — Hardcoded Cultural Facts.

When Qdrant is unavailable or returns weak results, Culture Scout
falls back to these curated facts about Huế and Hội An.

This ensures the demo NEVER shows a blank screen or
generates culturally ungrounded tasks.
"""

# ── Curated fallback facts ────────────────────────────────────────────────────
# These are a small subset of the full Qdrant knowledge base,
# handpicked for high quality and demo relevance.

_FALLBACK_FACTS: list[dict] = [
    {
        "text": (
            "Kinh thành Huế được xây dựng từ năm 1805 dưới triều vua Gia Long, "
            "là kinh đô của triều Nguyễn — triều đại phong kiến cuối cùng của Việt Nam. "
            "UNESCO công nhận là Di sản Văn hóa Thế giới năm 1993."
        ),
        "place_name": "Kinh thành Huế",
        "category": "history",
        "source": "fallback",
        "score": 0.80,
    },
    {
        "text": (
            "Phố cổ Hội An được UNESCO công nhận là Di sản Văn hóa Thế giới năm 1999. "
            "Đây là thương cảng quốc tế sầm uất từ thế kỷ 15-19, "
            "nơi giao thoa văn hóa Việt - Hoa - Nhật - Pháp."
        ),
        "place_name": "Phố cổ Hội An",
        "category": "history",
        "source": "fallback",
        "score": 0.80,
    },
    {
        "text": (
            "Chùa Cầu (Lai Viễn Kiều) là biểu tượng của Hội An, "
            "được thương nhân Nhật Bản xây dựng vào thế kỷ 17. "
            "Bên trong có miếu thờ thần trấn yểm quái vật gây động đất."
        ),
        "place_name": "Chùa Cầu Hội An",
        "category": "architecture",
        "source": "fallback",
        "score": 0.75,
    },
    {
        "text": (
            "Bún bò Huế là món đặc trưng nhất của Huế với nước lèo đậm đà "
            "từ xương bò hầm, sả, ớt, mắm ruốc. "
            "Một bát chuẩn phải có chả cua, giò heo, huyết, rau sống."
        ),
        "place_name": "Huế",
        "category": "food",
        "source": "fallback",
        "score": 0.75,
    },
    {
        "text": (
            "Đèn lồng Hội An là biểu tượng văn hóa nổi tiếng. "
            "Vào đêm 14 âm lịch, toàn bộ đèn điện được tắt, "
            "chỉ còn ánh đèn lồng và nến tạo nên không gian huyền ảo."
        ),
        "place_name": "Phố cổ Hội An",
        "category": "tradition",
        "source": "fallback",
        "score": 0.75,
    },
    {
        "text": (
            "Cao lầu là món ăn đặc trưng chỉ có ở Hội An. "
            "Sợi mì được làm từ gạo ngâm trong nước tro lấy từ củi đảo Cù Lao Chàm, "
            "chỉ nước giếng Bá Lễ mới làm được sợi đúng vị."
        ),
        "place_name": "Hội An",
        "category": "food",
        "source": "fallback",
        "score": 0.70,
    },
]


def get_fallback_facts(place_name: str = "", limit: int = 5) -> list[dict]:
    """
    Return curated fallback facts, optionally filtered by place name.

    Args:
        place_name: If provided, prefer facts about this place.
        limit:      Maximum number of facts to return.

    Returns:
        List of cultural fact dicts.
    """
    if place_name:
        # Prefer facts matching the requested place
        matching = [f for f in _FALLBACK_FACTS if place_name.lower() in f["place_name"].lower()]
        other = [f for f in _FALLBACK_FACTS if place_name.lower() not in f["place_name"].lower()]
        return (matching + other)[:limit]

    return _FALLBACK_FACTS[:limit]
