"""Map endpoints backed by server-side Goong MapTiles calls."""

import logging

import httpx
from fastapi import APIRouter, HTTPException, Query, Response, Request

from services.backend.app.schemas.maps import MapConfigResponse
from services.backend.app.services.map_service import map_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/maps", tags=["Maps"])


@router.get("/config", response_model=MapConfigResponse)
async def get_map_config():
    """Return backend-hosted map config for mobile clients."""
    return MapConfigResponse(
        enabled=map_service.enabled,
        style_url=map_service.backend_style_url() if map_service.enabled else None,
    )


@router.get("/style")
async def get_map_style():
    """Return Goong MapLibre style JSON with resources rewritten via backend."""
    try:
        return await map_service.get_style()
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except (httpx.RequestError, httpx.HTTPStatusError, ValueError) as exc:
        logger.warning("Goong map style failed: %s", exc)
        raise HTTPException(status_code=502, detail="Map style is unavailable") from exc


@router.get("/proxy{suffix:path}")
async def proxy_map_asset(
    suffix: str,
    url: str = Query(..., description="Goong map asset URL")
):
    """Proxy allowed Goong style assets/tiles while keeping provider key server-side."""
    # Append suffix (e.g. .json, @2x.png) to the target Goong URL if requested by MapLibre
    if suffix:
        url += suffix

    try:
        content, content_type = await map_service.proxy_url(url)
        return Response(
            content=content,
            media_type=content_type,
            headers={"Cache-Control": "public, max-age=3600"},
        )
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except httpx.HTTPStatusError as exc:
        status_code = exc.response.status_code
        if status_code != 404:
            logger.warning("Goong map proxy upstream returned %d: %s", status_code, exc)
        raise HTTPException(status_code=status_code, detail="Map asset is unavailable") from exc
    except httpx.RequestError as exc:
        logger.warning("Goong map proxy connection failed: %s", exc)
        raise HTTPException(status_code=502, detail="Map asset is unavailable") from exc
