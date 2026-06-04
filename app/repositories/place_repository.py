from sqlalchemy.orm import Session
from app.models.place import Place
from typing import Optional

class PlaceRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self, category: Optional[str] = None, limit: int = 10, offset: int = 0):
        query = self.db.query(Place)
        if category:
            query = query.filter(Place.category == category)
        return query.offset(offset).limit(limit).all()

    def count(self, category: Optional[str] = None) -> int:
        query = self.db.query(Place)
        if category:
            query = query.filter(Place.category == category)
        return query.count()

    def get_by_name(self, name: str) -> Place | None:
        return self.db.query(Place).filter(Place.name == name).first()

    def create(self, **kwargs) -> Place:
        place = Place(**kwargs)
        self.db.add(place)
        self.db.commit()
        self.db.refresh(place)
        return place