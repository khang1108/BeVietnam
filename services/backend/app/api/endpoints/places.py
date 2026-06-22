from fastapi import APIRouter, Query
from typing import Optional
from services.backend.app.schemas import PlacesResponse, PlaceSchema
from services.backend.app.schemas.nearby import NearbyResponse
from services.backend.app.services.foursquare_service import foursquare_service

router = APIRouter()


@router.get("/places/nearby", response_model=NearbyResponse, tags=["Places"])
async def get_nearby_places(
    lat: float = Query(..., description="Latitude of the map center"),
    lng: float = Query(..., description="Longitude of the map center"),
    radius: int = Query(2000, ge=50, le=50000, description="Search radius in meters"),
    limit: int = Query(30, ge=1, le=50),
):
    """
    GET /places/nearby — Live POIs (cafe, homestay, restaurant, ...) quanh điểm hiện tại.

    Proxy Foursquare Places (key giữ ở server). Frontend gọi lại mỗi khi map di chuyển
    (moveend) để cập nhật marker theo viewport. Trả về rỗng nếu chưa cấu hình API key.
    """
    return await foursquare_service.search_nearby(
        latitude=lat, longitude=lng, radius=radius, limit=limit
    )

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
