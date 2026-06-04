from fastapi import APIRouter
from datetime import datetime, timezone
from src.bevietnam.backend.app.schemas import FeedResponse, FeedItem

router = APIRouter()

_MOCK_FEED = [
    FeedItem(
        id="f-001",
        place_id="place-001",
        name="Văn Miếu - Quốc Tử Giám",
        category="temple",
        thumbnail_url="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Van_Mieu.jpg/800px-Van_Mieu.jpg",
        score=0.95,
        explanation="Đây là di tích lịch sử nổi bật nhất Hà Nội, phù hợp với hành trình khám phá văn hoá của bạn.",
        created_at=datetime.now(timezone.utc),
    ),
    FeedItem(
        id="f-002",
        place_id="place-002",
        name="Hồ Hoàn Kiếm",
        category="park",
        thumbnail_url="https://upload.wikimedia.org/wikipedia/commons/5/5c/Hoan_Kiem_lake.jpg",
        score=0.88,
        explanation="Hồ Hoàn Kiếm nằm trung tâm thành phố, lý tưởng để tham quan buổi sáng sớm hoặc chiều tà.",
        created_at=datetime.now(timezone.utc),
    ),
    FeedItem(
        id="f-003",
        place_id="place-003",
        name="Bảo tàng Lịch sử Quốc gia",
        category="museum",
        thumbnail_url=None,
        score=0.80,
        explanation="Bảo tàng giúp bạn hiểu sâu hơn về lịch sử Việt Nam từ thời tiền sử đến hiện đại.",
        created_at=datetime.now(timezone.utc),
    ),
]


@router.get("/feed", response_model=FeedResponse, tags=["Feed"])
async def get_feed():
    """
    GET /feed — Danh sách địa điểm gợi ý theo điểm số và giải thích.
    Android/web hiển thị recommendation cards kèm lý do gợi ý.
    """
    return FeedResponse(items=_MOCK_FEED)
