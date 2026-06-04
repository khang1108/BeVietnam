from sqlalchemy.orm import Session
from app.models.capture import Capture
from datetime import datetime, timezone

class CaptureRepository:
    def __init__(self, db: Session):
        self.db = db

    def create(self, user_id: int, place_id: int, task_id: str = None,
               timestamp: datetime = None, latitude: float = None,
               longitude: float = None, media_url: str = None,
               note: str = None) -> Capture:
        capture = Capture(
            user_id=user_id,
            place_id=place_id,
            task_id=task_id,
            timestamp=timestamp or datetime.now(timezone.utc),
            latitude=latitude,
            longitude=longitude,
            media_url=media_url,
            note=note,
        )
        self.db.add(capture)
        self.db.commit()
        self.db.refresh(capture)
        return capture