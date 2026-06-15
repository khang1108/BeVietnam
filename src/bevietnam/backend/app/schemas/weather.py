"""Weather schemas for server-side OpenWeather-backed context."""

from typing import Literal

from pydantic import BaseModel, Field

WeatherCondition = Literal["sunny", "cloudy", "rainy", "hot", "any"]


class WeatherResponse(BaseModel):
    condition: WeatherCondition
    temp: float | None = None
    source: str


class WeatherCoord(BaseModel):
    lat: float = Field(ge=-90, le=90)
    lng: float = Field(ge=-180, le=180)


class WeatherBatchRequest(BaseModel):
    coordinates: list[WeatherCoord] = Field(default_factory=list, max_length=100)


class WeatherBatchResult(WeatherCoord):
    condition: WeatherCondition
    temp: float | None = None


class WeatherBatchResponse(BaseModel):
    results: list[WeatherBatchResult]
