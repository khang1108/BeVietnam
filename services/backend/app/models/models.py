"""
SQLAlchemy DB models.

TODO(Backend): Configure DB engine in core/database.py
    from sqlalchemy import create_engine
    from sqlalchemy.orm import DeclarativeBase, sessionmaker
    DATABASE_URL = settings.DATABASE_URL
"""
from sqlalchemy import Column, String, Float, DateTime, Boolean, Text, ForeignKey, JSON
from sqlalchemy.orm import DeclarativeBase, relationship
from datetime import datetime, timezone


class Base(DeclarativeBase):
    pass


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


class PlaceModel(Base):
    __tablename__ = "places"

    id = Column(String, primary_key=True)
    name = Column(String(200), nullable=False)
    category = Column(String(50), nullable=False)  # temple | museum | park | district
    description = Column(Text, nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    image_url = Column(String(500), nullable=True)
    reference_url = Column(String(500), nullable=True)
    created_at = Column(DateTime, default=utcnow)

    captures = relationship("CaptureModel", back_populates="place")


class UserModel(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True)
    name = Column(String(100), nullable=False)
    email = Column(String(200), unique=True, nullable=False, index=True)
    hashed_password = Column(String(200), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=utcnow)

    captures = relationship("CaptureModel", back_populates="user")
    preference = relationship(
        "UserPreferenceModel", back_populates="user", uselist=False, cascade="all, delete-orphan"
    )


class UserPreferenceModel(Base):
    """Per-user personalization signals used to rank the feed."""
    __tablename__ = "user_preferences"

    user_id = Column(String, ForeignKey("users.id"), primary_key=True)
    # List of category tags, e.g. ["history", "architecture", "cuisine"].
    interests = Column(JSON, nullable=False, default=list)
    home_latitude = Column(Float, nullable=True)
    home_longitude = Column(Float, nullable=True)
    updated_at = Column(DateTime, default=utcnow, onupdate=utcnow)

    user = relationship("UserModel", back_populates="preference")


class CaptureModel(Base):
    __tablename__ = "captures"

    id = Column(String, primary_key=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False, index=True)
    task_id = Column(String, nullable=True)
    place_id = Column(String, ForeignKey("places.id"), nullable=False, index=True)
    timestamp = Column(DateTime, nullable=False, default=utcnow)
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)
    media_url = Column(String(500), nullable=True)
    note = Column(Text, nullable=True)
    created_at = Column(DateTime, default=utcnow)

    user = relationship("UserModel", back_populates="captures")
    place = relationship("PlaceModel", back_populates="captures")
