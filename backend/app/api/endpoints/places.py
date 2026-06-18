import json
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, Query, Depends, HTTPException
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.dependencies import get_db
from app.models.models import PlaceModel
from app.schemas.place import PlaceSchema, PlacesResponse, PlaceCreateRequest, PlaceImportRequest

router = APIRouter()


@router.get("/places", response_model=PlacesResponse, tags=["Places"])
async def get_places(
    category: Optional[str] = Query(None, description="Filter by category"),
    limit: int = Query(10, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: AsyncSession = Depends(get_db),
):
    """
    GET /places — Trả về danh sách địa điểm du lịch.
    """
    query = select(PlaceModel)
    count_query = select(func.count()).select_from(PlaceModel)

    if category:
        query = query.where(PlaceModel.category == category)
        count_query = count_query.where(PlaceModel.category == category)

    query = query.offset(offset).limit(limit)
    result = await db.execute(query)
    places = result.scalars().all()
    total = await db.scalar(count_query)

    return PlacesResponse(
        total=total if total is not None else 0,
        items=[PlaceSchema.model_validate(place) for place in places],
    )


@router.post("/places/import", tags=["Places"])
async def import_places(
    payload: PlaceImportRequest,
    db: AsyncSession = Depends(get_db),
):
    """
    POST /places/import — Load place JSON data from local file or request body.
    """
    place_items = payload.items
    if payload.file_path:
        file_path = Path(payload.file_path)
        if not file_path.is_absolute():
            file_path = Path(__file__).resolve().parents[3] / payload.file_path
        if not file_path.exists():
            raise HTTPException(status_code=404, detail=f"File not found: {file_path}")
        try:
            raw = json.loads(file_path.read_text(encoding="utf-8"))
        except Exception as exc:
            raise HTTPException(status_code=400, detail=f"Unable to parse JSON: {exc}") from exc

        if isinstance(raw, dict):
            raw = [raw]
        place_items = [PlaceCreateRequest.model_validate(item) for item in raw]

    if not place_items:
        raise HTTPException(status_code=400, detail="No place data provided for import")

    records = []
    for item in place_items:
        data = item.model_dump()
        place_id = data.pop("place_id", None)
        if place_id:
            data["id"] = place_id
        records.append(PlaceModel(**data))

    db.add_all(records)
    await db.commit()
    return {"inserted": len(records)}
