from fastapi import APIRouter, Query
from typing import Optional
from services.backend.app.schemas import PlacesResponse, PlaceSchema

router = APIRouter()

# ── Mock / seed data ──────────────────────────────────────────────────────────
_MOCK_PLACES = [
    PlaceSchema(
        id="place-001",
        name="Văn Miếu - Quốc Tử Giám",
        category="temple",
        description="Temple of Literature, founded in 1070. Vietnam's first national university.",
        latitude=21.0275,
        longitude=105.8357,
        image_url="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Van_Mieu.jpg/800px-Van_Mieu.jpg",
        reference_url="https://vanmieu.gov.vn",
    ),
    PlaceSchema(
        id="place-002",
        name="Hồ Hoàn Kiếm",
        category="park",
        description="Hoàn Kiếm Lake sits at the heart of Hanoi's historic quarter.",
        latitude=21.0285,
        longitude=105.8524,
        image_url="https://upload.wikimedia.org/wikipedia/commons/5/5c/Hoan_Kiem_lake.jpg",
        reference_url=None,
    ),
    PlaceSchema(
        id="place-003",
        name="Bảo tàng Lịch sử Quốc gia",
        category="museum",
        description="National Museum of Vietnamese History, showcasing artifacts from prehistoric times.",
        latitude=21.0245,
        longitude=105.8592,
        image_url=None,
        reference_url="https://baotanglichsu.vn",
    ),
    PlaceSchema(
        id="place-004",
        name="Chùa Một Cột",
        category="temple",
        description="One Pillar Pagoda — a historic Buddhist temple built in 1049.",
        latitude=21.0353,
        longitude=105.8342,
        image_url=None,
        reference_url=None,
    ),
    PlaceSchema(
        id="place-005",
        name="Phố cổ Hà Nội",
        category="district",
        description="Hanoi Old Quarter — 36 ancient guild streets, vibrant street food and culture.",
        latitude=21.0341,
        longitude=105.8500,
        image_url=None,
        reference_url=None,
    ),
]


# ── Endpoint ──────────────────────────────────────────────────────────────────
@router.get("/places", response_model=PlacesResponse, tags=["Places"])
async def get_places(
    category: Optional[str] = Query(None, description="Filter by category"),
    limit: int = Query(10, ge=1, le=100),
    offset: int = Query(0, ge=0),
):
    """
    GET /places — Trả về danh sách địa điểm du lịch.
    Sprint 1: dùng mock data. Sẽ kết nối DB thật sau khi schema sẵn sàng.

    - **category**: lọc theo loại (temple, museum, park, ...)
    - **limit / offset**: phân trang
    """
    filtered = _MOCK_PLACES
    if category:
        filtered = [p for p in filtered if p.category == category]

    paginated = filtered[offset : offset + limit]

    return PlacesResponse(total=len(filtered), items=paginated)
