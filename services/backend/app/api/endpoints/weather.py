"""Weather endpoints backed by server-side OpenWeather calls."""

from fastapi import APIRouter, Query

from services.backend.app.schemas.weather import (
    WeatherBatchRequest,
    WeatherBatchResponse,
    WeatherBatchResult,
    WeatherResponse,
)
from services.backend.app.services.weather_service import weather_service

router = APIRouter(prefix="/weather", tags=["Weather"])


@router.get("", response_model=WeatherResponse)
async def get_weather(
    lat: float = Query(..., ge=-90, le=90),
    lng: float = Query(..., ge=-180, le=180),
):
    """Resolve current weather for one GPS coordinate."""
    result = await weather_service.get_condition(lat, lng)
    return WeatherResponse(
        condition=result.condition,
        temp=result.temp,
        source=result.source,
    )


@router.post("/batch", response_model=WeatherBatchResponse)
async def get_weather_batch(body: WeatherBatchRequest):
    """Resolve current weather for coordinates, preserving input order."""
    coords = [(coord.lat, coord.lng) for coord in body.coordinates]
    results = await weather_service.get_conditions_batch(coords)
    return WeatherBatchResponse(
        results=[
            WeatherBatchResult(
                lat=coord.lat,
                lng=coord.lng,
                condition=result.condition,
                temp=result.temp,
            )
            for coord, result in zip(body.coordinates, results)
        ]
    )
