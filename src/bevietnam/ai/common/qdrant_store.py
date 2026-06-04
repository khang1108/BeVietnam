"""
Qdrant Vector Store — Cultural Knowledge Retrieval.

Wraps the Qdrant client and bge-m3 embedding model to provide
semantic search over cultural documents about Vietnamese places.

The embedding model (BAAI/bge-m3) runs locally — no external API needed.
It is loaded lazily on first use and reused for all subsequent calls.

Usage:
    from ai.common.qdrant_store import qdrant_store

    results = qdrant_store.search("Kinh thành Huế lịch sử", limit=5)
    # Returns list of dicts: [{"text": "...", "place_name": "...", "score": 0.85}, ...]
"""

import logging
from pathlib import Path
from typing import Any

from qdrant_client import QdrantClient
from qdrant_client.models import Distance, PointStruct, VectorParams
from sentence_transformers import SentenceTransformer

from src.bevietnam.ai.common.config import settings

logger = logging.getLogger(__name__)


class QdrantStore:
    """
    Vector store for cultural knowledge retrieval.

    Uses:
      - BAAI/bge-m3 for local embedding (strong Vietnamese support)
      - Qdrant for similarity search

    The embedding model and Qdrant client are lazy-loaded
    so the app starts fast and loads them on first query.
    """

    def __init__(self) -> None:
        self._embedding_model: SentenceTransformer | None = None
        self._qdrant_client: QdrantClient | None = None

    # ── Lazy Initialization ───────────────────────────────────────────────────

    @property
    def embedding_model(self) -> SentenceTransformer:
        """Load bge-m3 embedding model (once, then reuse)."""
        if self._embedding_model is None:
            logger.info(
                "Loading embedding model: %s (this may take a moment on first run)...",
                settings.embedding_model_name,
            )
            self._embedding_model = SentenceTransformer(settings.embedding_model_name)
            logger.info("Embedding model loaded successfully.")
        return self._embedding_model

    @property
    def client(self) -> QdrantClient:
        """Connect to Qdrant - cloud, embedded local, or server."""
        if self._qdrant_client is None:
            if settings.use_qdrant_cloud:
                # Qdrant Cloud REST API requires port 6333
                cloud_url = settings.qdrant_cluster_endpoint.rstrip("/")
                if ":6333" not in cloud_url:
                    cloud_url = f"{cloud_url}:6333"

                self._qdrant_client = QdrantClient(
                    url=cloud_url,
                    api_key=settings.qdrant_api_key,
                    timeout=60,
                )
                logger.info(
                    "Connected to Qdrant Cloud: %s",
                    cloud_url[:60],
                )
            elif settings.qdrant_path:
                qdrant_path = Path(settings.qdrant_path).expanduser()
                qdrant_path.parent.mkdir(parents=True, exist_ok=True)

                self._qdrant_client = QdrantClient(path=str(qdrant_path))
                logger.info(
                    "Connected to embedded local Qdrant at %s",
                    qdrant_path,
                )
            else:
                # Local Qdrant server (Docker or standalone binary)
                self._qdrant_client = QdrantClient(
                    host=settings.qdrant_host,
                    port=settings.qdrant_port,
                )
                logger.info(
                    "Connected to local Qdrant at %s:%s",
                    settings.qdrant_host,
                    settings.qdrant_port,
                )
        return self._qdrant_client

    # ── Public API ────────────────────────────────────────────────────────────

    def search(
        self,
        query: str,
        limit: int = 5,
        place_filter: str | None = None,
    ) -> list[dict[str, Any]]:
        """
        Search for cultural facts related to a query.

        Args:
            query:        Natural language search query (e.g. "lịch sử Huế").
            limit:        Maximum number of results to return.
            place_filter: Optional place name to filter results.

        Returns:
            List of dicts, each containing:
              - text: the cultural fact/passage
              - place_name: associated place
              - category: history/food/architecture/tradition
              - source: where the fact came from
              - score: similarity score (0.0 - 1.0)
        """
        try:
            # Embed the query
            query_vector = self.embedding_model.encode(query).tolist()

            # Build optional filter
            query_filter = None
            if place_filter:
                from qdrant_client.models import FieldCondition, Filter, MatchValue

                query_filter = Filter(
                    must=[
                        FieldCondition(
                            key="place_name",
                            match=MatchValue(value=place_filter),
                        )
                    ]
                )

            # Search Qdrant
            results = self.client.query_points(
                collection_name=settings.qdrant_collection,
                query=query_vector,
                limit=limit,
                query_filter=query_filter,
            )

            # Convert to simple dicts for agent consumption
            return [
                {
                    "text": point.payload.get("text", ""),
                    "place_name": point.payload.get("place_name", ""),
                    "category": point.payload.get("category", ""),
                    "source": point.payload.get("source", ""),
                    "score": round(point.score, 4),
                }
                for point in results.points
            ]

        except Exception as exc:
            logger.warning("Qdrant search failed: %s", exc)
            return []

    def ensure_collection(self) -> None:
        """
        Create the cultural_knowledge collection if it doesn't exist.
        Called by the seed script before uploading documents.
        """
        collections = [c.name for c in self.client.get_collections().collections]
        if settings.qdrant_collection not in collections:
            self.client.create_collection(
                collection_name=settings.qdrant_collection,
                vectors_config=VectorParams(
                    size=settings.embedding_dimension,
                    distance=Distance.COSINE,
                ),
            )
            logger.info("Created Qdrant collection: %s", settings.qdrant_collection)
        else:
            logger.info("Qdrant collection already exists: %s", settings.qdrant_collection)

    def upsert_documents(self, documents: list[dict[str, Any]]) -> int:
        """
        Embed and upload cultural documents to Qdrant.

        Args:
            documents: List of dicts with keys: text, place_name, category, source.

        Returns:
            Number of documents uploaded.
        """
        texts = [doc["text"] for doc in documents]
        embeddings = self.embedding_model.encode(texts, show_progress_bar=True)

        points = [
            PointStruct(
                id=idx,
                vector=embedding.tolist(),
                payload={
                    "text": doc["text"],
                    "place_name": doc.get("place_name", ""),
                    "category": doc.get("category", ""),
                    "source": doc.get("source", ""),
                },
            )
            for idx, (doc, embedding) in enumerate(zip(documents, embeddings))
        ]

        self.client.upsert(
            collection_name=settings.qdrant_collection,
            points=points,
        )
        logger.info("Uploaded %d documents to Qdrant", len(points))
        return len(points)


# ── Singleton instance ────────────────────────────────────────────────────────
# Import this in agent modules: from ai.common.qdrant_store import qdrant_store
qdrant_store = QdrantStore()
