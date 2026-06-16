"""Curated local fallback facts for Culture Scout retrieval."""

from __future__ import annotations

from typing import Any

# ── Curated fallback facts ────────────────────────────────────────────────────
# These are a small subset of the full Qdrant knowledge base,
# handpicked for high quality and demo relevance.

_DEFAULT_SCORE = 0.72

_FALLBACK_FACTS: list[dict[str, Any]] = [
    {
        "chunk_id": "fallback-hue-imperial-001",
        "place_id": "hue-imperial-city",
        "text": (
            "Kinh thành Huế được xây dựng từ năm 1805 dưới triều vua Gia Long, "
            "là kinh đô của triều Nguyễn — triều đại phong kiến cuối cùng của Việt Nam. "
            "UNESCO công nhận là Di sản Văn hóa Thế giới năm 1993."
        ),
        "place_name": "Kinh thành Huế",
        "category": "history",
        "source": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "source_type": "official",
        "source_title": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "source_url": "https://hueworldheritage.org.vn/",
        "publisher": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "page_or_section": "Kinh thành Huế",
        "review_status": "approved",
        "language": "vi",
        "tags": ["history", "heritage", "nguyen-dynasty"],
        "score": 0.80,
    },
    {
        "chunk_id": "fallback-hue-imperial-002",
        "place_id": "hue-imperial-city",
        "text": (
            "Hoàng thành Huế gồm Ngọ Môn, Điện Thái Hòa, Tử Cấm Thành và hệ thống "
            "cung điện, miếu thờ phản ánh trật tự lễ nghi của triều Nguyễn."
        ),
        "place_name": "Kinh thành Huế",
        "category": "architecture",
        "source": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "source_type": "official",
        "source_title": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "source_url": "https://hueworldheritage.org.vn/",
        "publisher": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "page_or_section": "Hoàng thành Huế",
        "review_status": "approved",
        "language": "vi",
        "tags": ["architecture", "heritage", "nguyen-dynasty"],
        "score": 0.78,
    },
    {
        "chunk_id": "fallback-hue-thien-mu-001",
        "place_id": "thien-mu-pagoda",
        "text": (
            "Chùa Thiên Mụ nằm trên đồi Hà Khê bên bờ sông Hương, được xây dựng "
            "năm 1601 dưới thời chúa Nguyễn Hoàng."
        ),
        "place_name": "Chùa Thiên Mụ",
        "category": "religion",
        "source": "Cục Du lịch Quốc gia Việt Nam",
        "source_type": "official",
        "source_title": "Cục Du lịch Quốc gia Việt Nam",
        "source_url": "https://vietnamtourism.gov.vn/",
        "publisher": "Cục Du lịch Quốc gia Việt Nam",
        "page_or_section": "Chùa Thiên Mụ",
        "review_status": "approved",
        "language": "vi",
        "tags": ["religion", "history", "perfume-river"],
        "score": 0.76,
    },
    {
        "chunk_id": "fallback-hue-river-001",
        "place_id": "perfume-river",
        "text": (
            "Sông Hương chảy qua trung tâm thành phố Huế và gắn liền với đời sống "
            "văn hóa, nghệ thuật, tâm linh của người Huế."
        ),
        "place_name": "Sông Hương",
        "category": "tradition",
        "source": "Cục Du lịch Quốc gia Việt Nam",
        "source_type": "official",
        "source_title": "Cục Du lịch Quốc gia Việt Nam",
        "source_url": "https://vietnamtourism.gov.vn/",
        "publisher": "Cục Du lịch Quốc gia Việt Nam",
        "page_or_section": "Sông Hương",
        "review_status": "approved",
        "language": "vi",
        "tags": ["tradition", "nature", "art"],
        "score": 0.75,
    },
    {
        "chunk_id": "fallback-hue-cuisine-001",
        "place_id": "hue-cuisine",
        "text": (
            "Bún bò Huế là món ăn đặc trưng của Huế với nước dùng từ xương bò, "
            "sả, ớt và mắm ruốc, thường ăn kèm rau sống."
        ),
        "place_name": "Huế",
        "category": "food",
        "source": "Cục Du lịch Quốc gia Việt Nam",
        "source_type": "official",
        "source_title": "Cục Du lịch Quốc gia Việt Nam",
        "source_url": "https://vietnamtourism.gov.vn/",
        "publisher": "Cục Du lịch Quốc gia Việt Nam",
        "page_or_section": "Ẩm thực Huế",
        "review_status": "approved",
        "language": "vi",
        "tags": ["food", "cuisine", "hue"],
        "score": 0.75,
    },
    {
        "chunk_id": "fallback-hoi-an-001",
        "place_id": "hoi-an-ancient-town",
        "text": (
            "Phố cổ Hội An được UNESCO công nhận là Di sản Văn hóa Thế giới năm 1999, "
            "từng là thương cảng quốc tế sầm uất từ thế kỷ 15 đến 19."
        ),
        "place_name": "Phố cổ Hội An",
        "category": "history",
        "source": "UNESCO World Heritage Centre",
        "source_type": "unesco",
        "source_title": "Hoi An Ancient Town",
        "source_url": "https://whc.unesco.org/",
        "publisher": "UNESCO World Heritage Centre",
        "page_or_section": "Hoi An Ancient Town",
        "review_status": "approved",
        "language": "vi",
        "tags": ["history", "heritage", "trade"],
        "score": 0.72,
    },
    {
        "chunk_id": "fallback-hoi-an-002",
        "place_id": "hoi-an-ancient-town",
        "text": (
            "Cao lầu là món ăn đặc trưng chỉ có ở Hội An. "
            "Sợi mì được làm từ gạo ngâm trong nước tro lấy từ củi đảo Cù Lao Chàm, "
            "chỉ nước giếng Bá Lễ mới làm được sợi đúng vị."
        ),
        "place_name": "Hội An",
        "category": "food",
        "source": "Cục Du lịch Quốc gia Việt Nam",
        "source_type": "official",
        "source_title": "Cục Du lịch Quốc gia Việt Nam",
        "source_url": "https://vietnamtourism.gov.vn/",
        "publisher": "Cục Du lịch Quốc gia Việt Nam",
        "page_or_section": "Ẩm thực Hội An",
        "review_status": "approved",
        "language": "vi",
        "tags": ["food", "hoi-an"],
        "score": 0.70,
    },
]


def get_fallback_facts(
    place_name: str = "",
    limit: int = 5,
    place_id: str = "",
    category: str = "",
    language: str = "",
) -> list[dict[str, Any]]:
    """
    Return curated fallback facts, optionally filtered by place name.

    Args:
        place_name: If provided, prefer facts about this place.
        limit: Maximum number of facts to return.
        place_id: Optional stable POI ID filter.
        category: Optional fact category filter.
        language: Optional language filter.

    Returns:
        List of cultural fact dicts.
    """
    filters = {
        "place_id": place_id.strip().lower(),
        "category": category.strip().lower(),
        "language": language.strip().lower(),
    }
    place_query = place_name.strip().lower()

    def matches(fact: dict[str, Any]) -> bool:
        if filters["place_id"] and fact.get("place_id") != filters["place_id"]:
            return False
        if filters["category"] and fact.get("category") != filters["category"]:
            return False
        if filters["language"] and fact.get("language") != filters["language"]:
            return False
        return True

    candidates = [fact for fact in _FALLBACK_FACTS if matches(fact)]

    if place_query:
        matching = [
            fact for fact in candidates
            if place_query in fact.get("place_name", "").lower()
        ]
        other = [
            fact for fact in candidates
            if place_query not in fact.get("place_name", "").lower()
        ]
        candidates = matching + other

    normalized = []
    for fact in candidates[:limit]:
        normalized.append({**fact, "score": fact.get("score", _DEFAULT_SCORE)})
    return normalized
