from fastapi import APIRouter, Query, Depends
from typing import Optional
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.place_service import PlaceService
from app.schemas.schemas import PlacesDBResponse

router = APIRouter()

@router.get("/places", response_model=PlacesDBResponse, tags=["Places"])
def get_places(
    category: Optional[str] = Query(None),
    limit: int = Query(10, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: Session = Depends(get_db),
):
    total, places = PlaceService(db).get_places(category=category, limit=limit, offset=offset)
    return PlacesDBResponse(total=total, items=places)