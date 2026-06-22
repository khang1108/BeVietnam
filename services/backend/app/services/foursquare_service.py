"""Foursquare Places proxy — live nearby POIs for the explore map.

Keeps the API key server-side. Classifies each result into a coarse bucket
by its Foursquare category name (robust to category-id changes) so the
frontend can colour/filter markers without knowing Foursquare's taxonomy.
"""

import logging

import httpx

from services.backend.app.core.config import settings
from services.backend.app.core.rate_limit import foursquare_limiter
from services.backend.app.schemas.nearby import NearbyPlace, NearbyResponse

logger = logging.getLogger(__name__)

# Coarse bucket -> keywords matched against the Foursquare category name.
_BUCKET_KEYWORDS: list[tuple[str, tuple[str, ...]]] = [
    ("food", ("café", "cafe", "coffee", "restaurant", "food", "noodle", "bar", "bakery", "tea", "dessert", "eatery", "breakfast", "brunch", "bistro", "pub", "snack", "diner")),
    ("lodging", ("hotel", "hostel", "homestay", "guest", "lodg", "resort", "motel", "bed and breakfast", "inn")),
    ("culture", ("museum", "art", "gallery", "temple", "pagoda", "shrine", "theater", "theatre")),
    ("history", ("historic", "monument", "memorial", "palace", "citadel", "tomb", "heritage", "landmark")),
    ("nature", ("park", "beach", "mountain", "garden", "lake", "river", "forest", "nature", "trail", "scenic")),
]


def _classify(category_name: str) -> str:
    name = category_name.lower()
    for bucket, keywords in _BUCKET_KEYWORDS:
        if any(kw in name for kw in keywords):
            return bucket
    return "place"


class FoursquareService:
    """Thin async adapter over Foursquare Places search."""

    async def search_nearby(
        self,
        latitude: float,
        longitude: float,
        radius: int = 2000,
        limit: int = 30,
    ) -> NearbyResponse:
        if not settings.FOURSQUARE_API_KEY:
            return NearbyResponse(total=0, items=[])

        if not await foursquare_limiter.allow():
            return NearbyResponse(total=0, items=[])

        fields = "fsq_place_id,name,latitude,longitude,categories,location,distance"
        if settings.FOURSQUARE_PREMIUM:
            fields += ",rating,popularity"
        params = {
            "ll": f"{latitude},{longitude}",
            "radius": str(max(1, min(radius, 100_000))),
            "limit": str(max(1, min(limit, 50))),
            "sort": "DISTANCE",
            "fields": fields,
        }
        headers = {
            "Authorization": f"Bearer {settings.FOURSQUARE_API_KEY}",
            "X-Places-Api-Version": settings.FOURSQUARE_API_VERSION,
            "Accept": "application/json",
        }
        try:
            async with httpx.AsyncClient(timeout=settings.FOURSQUARE_TIMEOUT) as client:
                response = await client.get(
                    f"{settings.FOURSQUARE_BASE_URL.rstrip('/')}/places/search",
                    params=params,
                    headers=headers,
                )
                response.raise_for_status()
                payload = response.json()
        except (httpx.RequestError, httpx.HTTPStatusError, ValueError) as exc:
            logger.warning("Foursquare nearby search failed: %s", exc)
            return NearbyResponse(total=0, items=[])

        items: list[NearbyPlace] = []
        for raw in payload.get("results", []):
            lat = raw.get("latitude")
            lng = raw.get("longitude")
            if lat is None or lng is None:
                continue
            categories = raw.get("categories") or []
            label = categories[0].get("name", "") if categories else ""
            items.append(
                NearbyPlace(
                    id=str(raw.get("fsq_place_id") or ""),
                    name=str(raw.get("name") or ""),
                    latitude=float(lat),
                    longitude=float(lng),
                    category=_classify(label),
                    category_label=label or "Place",
                    address=(raw.get("location") or {}).get("formatted_address"),
                    distance_meters=raw.get("distance"),
                    rating=raw.get("rating"),
                    popularity=raw.get("popularity"),
                )
            )

        return NearbyResponse(total=len(items), items=items)


foursquare_service = FoursquareService()
