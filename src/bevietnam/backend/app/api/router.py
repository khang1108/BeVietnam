"""
API v1 router — registers all feature routers under /api/v1 prefix.

To add a new feature:
    1. Create app/api/v1/endpoints/<feature>.py
    2. Import and include router here
"""
from fastapi import APIRouter
from src.bevietnam.backend.app.api.endpoints import storyline
from src.bevietnam.backend.app.api.endpoints import (
    captures,
    feed,
    health,
    logs,
    places,
    question_pool,
    weather,
)

api_router = APIRouter()

api_router.include_router(health.router)
api_router.include_router(places.router)
api_router.include_router(feed.router)
api_router.include_router(storyline.router)
api_router.include_router(captures.router)
api_router.include_router(logs.router)
api_router.include_router(question_pool.router)
api_router.include_router(weather.router)
