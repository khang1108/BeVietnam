"""
Qdrant Vector Store — Cultural Knowledge Retrieval.

Embedding được thực hiện qua HuggingFace Inference API (BAAI/bge-m3)
thay vì chạy model local — không cần PyTorch, không tốn RAM.

Cấu hình cần thiết trong .env:
    HF_TOKEN=hf_xxx...           # token miễn phí tại hf.co/settings/tokens
    QDRANT_CLUSTER_ENDPOINT=...  # Qdrant Cloud URL
    QDRANT_API_KEY=...           # Qdrant Cloud API key
"""

import logging
import time
from pathlib import Path
from typing import Any
from urllib.parse import urlparse

import httpx
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance,
    FieldCondition,
    Filter,
    MatchValue,
    PointStruct,
    VectorParams,
)

from services.ai.common.config import settings
from services.ai.common.knowledge_schema import KnowledgeChunk

logger = logging.getLogger(__name__)

_HF_API_URL = "https://router.huggingface.co/hf-inference/models/{model}/pipeline/feature-extraction"
_MAX_RETRIES = 3
_RETRY_DELAY = 20  # giây — chờ khi HF model đang warm-up


class HFEmbedder:
    """
    Gọi HuggingFace Inference API để tạo embedding.
    Không cần download model, không tốn RAM local.
    """

    def __init__(self) -> None:
        self._url = _HF_API_URL.format(model=settings.embedding_model_name)
        self._headers = {"Authorization": f"Bearer {settings.hf_token}"}

    def encode(self, texts: str | list[str]) -> list[list[float]]:
        """
        Encode một hoặc nhiều văn bản thành vector.
        Trả về list of vectors (mỗi vector là list[float]).
        """
        if isinstance(texts, str):
            texts = [texts]

        payload = {"inputs": texts, "options": {"wait_for_model": True}}

        for attempt in range(1, _MAX_RETRIES + 1):
            try:
                resp = httpx.post(
                    self._url,
                    headers=self._headers,
                    json=payload,
                    timeout=60.0,
                )

                if resp.status_code == 503:
                    # Model đang warm-up trên HF server
                    logger.info(
                        "HF model warming up, waiting %ds (attempt %d/%d)...",
                        _RETRY_DELAY, attempt, _MAX_RETRIES,
                    )
                    time.sleep(_RETRY_DELAY)
                    continue

                resp.raise_for_status()
                result = resp.json()

                # HF trả về [[vec], [vec]] cho batch hoặc [vec] cho single
                if isinstance(result[0], float):
                    return [result]
                return result

            except httpx.HTTPStatusError as exc:
                logger.error(
                    "HF API error %s: %s",
                    exc.response.status_code,
                    exc.response.text[:200],
                )
                raise
            except Exception as exc:
                logger.error("HF API call failed: %s", exc)
                raise

        raise RuntimeError(f"HF model không available sau {_MAX_RETRIES} lần thử")

    def encode_single(self, text: str) -> list[float]:
        """Encode một văn bản, trả về vector duy nhất."""
        return self.encode(text)[0]


class QdrantStore:
    """
    Vector store cho cultural knowledge retrieval.

    Dùng HuggingFace Inference API (BAAI/bge-m3) để embed — không cần model local.
    Kết nối Qdrant Cloud hoặc local server.
    """

    def __init__(self) -> None:
        self._embedder: HFEmbedder | None = None
        self._qdrant_client: QdrantClient | None = None

    @property
    def embedder(self) -> HFEmbedder:
        if self._embedder is None:
            if not settings.hf_token:
                raise ValueError(
                    "HF_TOKEN chưa được set. "
                    "Lấy token miễn phí tại https://huggingface.co/settings/tokens "
                    "rồi thêm vào .env: HF_TOKEN=hf_xxx..."
                )
            self._embedder = HFEmbedder()
            logger.info(
                "HF Embedder ready (model: %s via API)",
                settings.embedding_model_name,
            )
        return self._embedder

    @property
    def client(self) -> QdrantClient:
        if self._qdrant_client is None:
            if settings.use_qdrant_cloud:
                cloud_url = settings.qdrant_cluster_endpoint.rstrip("/")
                if not urlparse(cloud_url).scheme:
                    cloud_url = f"https://{cloud_url}"
                # Qdrant Cloud Python quickstart passes the cluster URL directly.
                # Source: https://qdrant.tech/documentation/cloud-quickstart/#3-connect-to-qdrant-cloud
                self._qdrant_client = QdrantClient(
                    url=cloud_url,
                    api_key=settings.qdrant_api_key,
                    timeout=60,
                )
                logger.info("Connected to Qdrant Cloud: %s", cloud_url[:60])
            elif settings.qdrant_path:
                path = Path(settings.qdrant_path).expanduser()
                path.parent.mkdir(parents=True, exist_ok=True)
                self._qdrant_client = QdrantClient(path=str(path))
                logger.info("Connected to embedded Qdrant at %s", path)
            else:
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
        place_id_filter: str | None = None,
        category_filter: str | None = None,
        language_filter: str | None = None,
    ) -> list[dict[str, Any]]:
        """Tìm kiếm cultural facts liên quan đến query."""
        try:
            query_vector = self.embedder.encode_single(query)
            query_filter = self._build_query_filter(
                place_name=place_filter,
                place_id=place_id_filter,
                category=category_filter,
                language=language_filter,
            )

            results = self.client.query_points(
                collection_name=settings.qdrant_collection,
                query=query_vector,
                limit=limit,
                query_filter=query_filter,
            )

            return [
                {
                    "text": p.payload.get("text", ""),
                    "chunk_id": p.payload.get("chunk_id", ""),
                    "place_id": p.payload.get("place_id", ""),
                    "place_name": p.payload.get("place_name", ""),
                    "category": p.payload.get("category", ""),
                    "source": p.payload.get("source", ""),
                    "source_type": p.payload.get("source_type", ""),
                    "source_title": p.payload.get("source_title", ""),
                    "source_url": p.payload.get("source_url", ""),
                    "publisher": p.payload.get("publisher", ""),
                    "page_or_section": p.payload.get("page_or_section", ""),
                    "review_status": p.payload.get("review_status", ""),
                    "language": p.payload.get("language", ""),
                    "tags": p.payload.get("tags", []),
                    "score": round(p.score, 4),
                }
                for p in results.points
            ]

        except Exception as exc:
            logger.warning("Qdrant search failed: %s", exc)
            return []

    def _build_query_filter(
        self,
        place_name: str | None = None,
        place_id: str | None = None,
        category: str | None = None,
        language: str | None = None,
    ) -> Filter | None:
        """Build an optional Qdrant payload filter."""
        conditions: list[FieldCondition] = []
        filter_values = {
            "place_name": place_name,
            "place_id": place_id,
            "category": category,
            "language": language,
        }

        for key, raw_value in filter_values.items():
            value = raw_value.strip() if isinstance(raw_value, str) else raw_value
            if value:
                conditions.append(
                    FieldCondition(key=key, match=MatchValue(value=value))
                )

        if not conditions:
            return None
        return Filter(must=conditions)

    def ensure_collection(self) -> None:
        """Tạo collection nếu chưa có."""
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
            logger.info("Qdrant collection exists: %s", settings.qdrant_collection)

    def upsert_documents(self, documents: list[dict[str, Any]]) -> int:
        """Embed và upload documents vào Qdrant."""
        texts = [doc["text"] for doc in documents]
        logger.info("Embedding %d documents via HF API...", len(texts))

        # Batch từng 32 docs để tránh timeout
        all_embeddings: list[list[float]] = []
        batch_size = 32
        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            vecs = self.embedder.encode(batch)
            all_embeddings.extend(vecs)
            logger.info(
                "  Embedded %d/%d",
                min(i + batch_size, len(texts)),
                len(texts),
            )

        points = [
            PointStruct(
                id=idx,
                vector=vec,
                payload=self._payload_from_document(doc),
            )
            for idx, (doc, vec) in enumerate(zip(documents, all_embeddings))
        ]

        self.client.upsert(collection_name=settings.qdrant_collection, points=points)
        logger.info("Uploaded %d documents to Qdrant", len(points))
        return len(points)

    def _payload_from_document(self, document: dict[str, Any]) -> dict[str, Any]:
        """
        Build a Qdrant payload from a knowledge chunk or legacy fact.

        New curated files should use KnowledgeChunk. The legacy seed file still
        works so current demo data can be seeded before the full curation pass.
        """
        if "chunk_id" in document:
            return KnowledgeChunk.model_validate(document).qdrant_payload()

        return {
            "text": document["text"],
            "place_name": document.get("place_name", ""),
            "category": document.get("category", ""),
            "source": document.get("source", ""),
            "review_status": "needs_review",
        }


qdrant_store = QdrantStore()
