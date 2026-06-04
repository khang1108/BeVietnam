import uuid
from sqlalchemy import Column, String, Float, DateTime, Boolean, Text, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime, timezone

# IMPORT Base từ cấu hình database thay vì tự định nghĩa lại
from app.core.database import Base

def utcnow() -> datetime:
    return datetime.now(timezone.utc)

# Hàm hỗ trợ sinh UUID tự động thành string
def generate_uuid() -> str:
    return str(uuid.uuid4())

class PlaceModel(Base):
    __tablename__ = "places"

    # Thêm default=generate_uuid và index=True
    id = Column(String, primary_key=True, default=generate_uuid, index=True)
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

    id = Column(String, primary_key=True, default=generate_uuid, index=True)
    name = Column(String(100), nullable=False)
    email = Column(String(200), unique=True, nullable=False, index=True)
    hashed_password = Column(String(200), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=utcnow)

    captures = relationship("CaptureModel", back_populates="user")

class CaptureModel(Base):
    __tablename__ = "captures"

    id = Column(String, primary_key=True, default=generate_uuid, index=True)
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