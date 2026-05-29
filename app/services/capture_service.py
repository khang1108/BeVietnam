from sqlalchemy.orm import Session
from app.repositories.capture_repository import CaptureRepository
from app.core.logger import logger
from datetime import datetime
from typing import Optional

class CaptureService:
    def __init__(self, db: Session):
        self.repo = CaptureRepository(db)

    def create_capture(self, user_id: int, place_id: int,
                       task_id: Optional[str] = None,
                       timestamp: Optional[datetime] = None,
                       latitude: Optional[float] = None,
                       longitude: Optional[float] = None,
                       media_url: Optional[str] = None,
                       note: Optional[str] = None):
        capture = self.repo.create(
            user_id=user_id,
            place_id=place_id,
            task_id=task_id,
            timestamp=timestamp,
            latitude=latitude,
            longitude=longitude,
            media_url=media_url,
            note=note,
        )
        logger.info(f"Capture created: id={capture.id} user_id={user_id} place_id={place_id}")
        return capture