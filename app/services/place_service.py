from sqlalchemy.orm import Session
from app.repositories.place_repository import PlaceRepository
from app.core.logger import logger
from typing import Optional

class PlaceService:
    def __init__(self, db: Session):
        self.repo = PlaceRepository(db)

    def get_places(self, category: Optional[str] = None, limit: int = 10, offset: int = 0):
        places = self.repo.get_all(category=category, limit=limit, offset=offset)
        total = self.repo.count(category=category)
        logger.info(f"GET /places — total: {total}, returned: {len(places)}")
        return total, places

    def seed(self):
        seed_data = [
            {
                "name": "Đại Nội Huế",
                "category": "temple",
                "description": "Kinh thành Huế, trung tâm quyền lực của triều Nguyễn từ 1802 đến 1945.",
                "latitude": 16.4698,
                "longitude": 107.5796,
                "image_url": None,
                "reference_url": "https://hueworldheritage.org.vn",
            },
            {
                "name": "Phố cổ Hội An",
                "category": "district",
                "description": "Đô thị cổ Hội An, di sản văn hóa thế giới được UNESCO công nhận năm 1999.",
                "latitude": 15.8801,
                "longitude": 108.3380,
                "image_url": None,
                "reference_url": "https://hoianancienttown.vn",
            },
        ]
        for data in seed_data:
            if not self.repo.get_by_name(data["name"]):
                self.repo.create(**data)
                logger.info(f"Seeded place: {data['name']}")
            else:
                logger.info(f"Place already exists, skip: {data['name']}")