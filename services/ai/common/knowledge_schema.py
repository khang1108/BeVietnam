"""
Knowledge chunk schema for BeVietnam cultural retrieval.

The AI service uses these models as the single contract for curated cultural
knowledge before it is embedded in Qdrant or used by agents. Cultural claims
must remain traceable to an official source, UNESCO source, or approved book.
"""

from __future__ import annotations

import re
from datetime import date
from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field, HttpUrl, field_validator, model_validator


_SLUG_PATTERN = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
_SUPPORTED_LANGUAGES = {"vi", "en"}


class SourceType(StrEnum):
    """Allowed source categories for cultural claims."""

    OFFICIAL = "official"
    UNESCO = "unesco"
    BOOK = "book"


class ReviewStatus(StrEnum):
    """Human review status for a knowledge chunk."""

    APPROVED = "approved"
    NEEDS_REVIEW = "needs_review"
    REJECTED = "rejected"


class SourceRef(BaseModel):
    """Traceable source reference shown in AI responses."""

    source_type: SourceType
    title: str = Field(min_length=1, max_length=240)
    publisher: str = Field(min_length=1, max_length=160)
    url: HttpUrl | None = None
    page_or_section: str = Field(default="", max_length=160)


class KnowledgeChunk(BaseModel):
    """
    A retrieval-ready cultural knowledge unit.

    The text should be short enough for retrieval and generation prompts, while
    metadata keeps every cultural claim auditable for the report and pilot.
    """

    chunk_id: str = Field(min_length=3, max_length=120)
    place_id: str = Field(min_length=1, max_length=120)
    place_name: str = Field(min_length=1, max_length=160)
    category: str = Field(min_length=1, max_length=40)
    language: str = Field(default="vi", min_length=2, max_length=5)
    text: str = Field(min_length=40, max_length=900)
    source_type: SourceType
    source_title: str = Field(min_length=1, max_length=240)
    source_url: HttpUrl | None = None
    publisher: str = Field(min_length=1, max_length=160)
    page_or_section: str = Field(default="", max_length=160)
    reviewed_at: date
    review_status: ReviewStatus = ReviewStatus.APPROVED
    related_poi_ids: list[str] = Field(default_factory=list)
    tags: list[str] = Field(default_factory=list)
    notes: str = Field(default="", max_length=500)

    @field_validator("chunk_id", "place_id")
    @classmethod
    def validate_slug(cls, value: str) -> str:
        """Ensure IDs are stable URL-safe slugs."""
        normalized = value.strip().lower()
        if not _SLUG_PATTERN.fullmatch(normalized):
            raise ValueError(
                "must be a lowercase slug using letters, numbers, and hyphens"
            )
        return normalized

    @field_validator("category", "language")
    @classmethod
    def normalize_lower_token(cls, value: str) -> str:
        """Normalize compact classifier fields."""
        return value.strip().lower()

    @field_validator("language")
    @classmethod
    def validate_language(cls, value: str) -> str:
        """Restrict pilot knowledge to Vietnamese or English."""
        if value not in _SUPPORTED_LANGUAGES:
            raise ValueError("language must be 'vi' or 'en'")
        return value

    @field_validator("place_name", "source_title", "publisher", "page_or_section")
    @classmethod
    def strip_text_field(cls, value: str) -> str:
        """Trim user-curated text fields."""
        return value.strip()

    @field_validator("related_poi_ids", "tags")
    @classmethod
    def normalize_slug_list(cls, values: list[str]) -> list[str]:
        """Normalize and deduplicate slug-like list fields."""
        normalized: list[str] = []
        for raw_value in values:
            value = str(raw_value).strip().lower()
            if value and value not in normalized:
                normalized.append(value)
        return normalized

    @model_validator(mode="after")
    def validate_source_traceability(self) -> "KnowledgeChunk":
        """Require source URLs for approved official and UNESCO chunks."""
        if (
            self.review_status == ReviewStatus.APPROVED
            and self.source_type in {SourceType.OFFICIAL, SourceType.UNESCO}
            and self.source_url is None
        ):
            raise ValueError(
                "approved official/UNESCO chunks require source_url"
            )
        return self

    def source_ref(self) -> SourceRef:
        """Return the compact source reference used in AI responses."""
        return SourceRef(
            source_type=self.source_type,
            title=self.source_title,
            publisher=self.publisher,
            url=self.source_url,
            page_or_section=self.page_or_section,
        )

    def qdrant_payload(self) -> dict[str, Any]:
        """Return a JSON-safe payload for Qdrant."""
        payload = self.model_dump(mode="json")
        payload["source"] = self.source_title
        return payload


class KnowledgeChunkCollection(BaseModel):
    """Versioned collection wrapper for curated knowledge JSON files."""

    version: int = Field(default=1, ge=1)
    dataset_id: str = Field(min_length=3, max_length=120)
    dataset_name: str = Field(min_length=1, max_length=160)
    description: str = Field(default="", max_length=500)
    chunks: list[KnowledgeChunk] = Field(min_length=1)

    @field_validator("dataset_id")
    @classmethod
    def validate_dataset_id(cls, value: str) -> str:
        """Ensure dataset IDs are stable slugs."""
        normalized = value.strip().lower()
        if not _SLUG_PATTERN.fullmatch(normalized):
            raise ValueError(
                "dataset_id must be a lowercase slug using letters, numbers, "
                "and hyphens"
            )
        return normalized

    @model_validator(mode="after")
    def validate_unique_chunk_ids(self) -> "KnowledgeChunkCollection":
        """Prevent duplicate chunks in a curated dataset."""
        chunk_ids = [chunk.chunk_id for chunk in self.chunks]
        duplicates = sorted(
            chunk_id for chunk_id in set(chunk_ids) if chunk_ids.count(chunk_id) > 1
        )
        if duplicates:
            raise ValueError(f"duplicate chunk_id values: {duplicates}")
        return self


def parse_knowledge_payload(payload: Any) -> list[KnowledgeChunk]:
    """
    Parse either a collection wrapper or a raw list of knowledge chunks.

    This lets small scripts validate both the final curated dataset format and
    ad hoc arrays emitted during content preparation.
    """
    if isinstance(payload, dict) and "chunks" in payload:
        return KnowledgeChunkCollection.model_validate(payload).chunks
    if isinstance(payload, list):
        return [KnowledgeChunk.model_validate(item) for item in payload]
    raise ValueError("knowledge payload must be a collection object or chunk list")
