import os
from pathlib import Path
from dotenv import load_dotenv
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession

# Single declarative Base lives in app/models/models.py. Re-export it here so that
# `from ...core.database import Base` keeps working AND every model shares ONE metadata.
# Relative import resolves under both `app.*` and `services.backend.app.*` run paths.
from ..models.models import Base  # noqa: F401  (re-exported)

# 1. Tự động tính toán đường dẫn tuyệt đối đến file .env
# File này đang ở: backend/app/core/database.py -> Lùi 3 cấp sẽ ra thư mục backend
BASE_DIR = Path(__file__).resolve().parent.parent.parent
env_path = BASE_DIR / ".env"

# 2. Ép load_dotenv đọc chính xác file tại đường dẫn đó
load_dotenv(dotenv_path=env_path)

DATABASE_URL = os.getenv("DATABASE_URL")

# Nếu vẫn không thấy, nó sẽ in ra đúng cái đường dẫn nó vừa tìm để ta dễ bắt lỗi
if not DATABASE_URL:
    raise ValueError(f"DATABASE_URL is not set in environment variables. Đã thử tìm tại: {env_path}")

# Khởi tạo Async Engine
engine = create_async_engine(
    DATABASE_URL,
    echo=False, 
    future=True
)

# Khởi tạo Session factory
AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)