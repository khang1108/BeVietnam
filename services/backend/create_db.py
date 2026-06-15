"""
Script tạo database bevietnam_db nếu chưa tồn tại
"""
import os
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

load_dotenv()

# Kết nối đến PostgreSQL mặc định (postgres database)
# Dùng psycopg2 (sync) cho script này
default_engine = create_engine(
    "postgresql://postgres:1@localhost:5432/postgres",
    echo=False
)

try:
    with default_engine.connect() as conn:
        # Tự động commit để tránh transaction issues
        conn.connection.autocommit = True
        
        # Kiểm tra database đã tồn tại chưa
        result = conn.execute(
            text("SELECT 1 FROM pg_database WHERE datname = 'bevietnam_db'")
        )
        
        if result.fetchone():
            print("✅ Database 'bevietnam_db' đã tồn tại")
        else:
            conn.execute(text("CREATE DATABASE bevietnam_db"))
            print("✅ Tạo database 'bevietnam_db' thành công")
            
except Exception as e:
    print(f"❌ Lỗi: {e}")
    print("\n💡 Hướng dẫn:")
    print("1. Kiểm tra PostgreSQL đã chạy chưa")
    print("2. Kiểm tra password trong .env có đúng không")
    print("3. Nếu PostgreSQL chưa cài, cài từ: https://www.postgresql.org/download/")
finally:
    default_engine.dispose()
