# Tourism App — Backend API

> Sprint 1 · Last updated: 13/05/2026

## Yêu cầu

- Python 3.11+ (khuyến nghị), hoặc Python 3.13
- pip / py (Windows)

## Cài đặt & Chạy local

```bash
# 1. Vào thư mục backend
cd backend

# 2. Copy file cấu hình
cp .env.example .env

# 3. Cài thư viện
py -m pip install fastapi uvicorn[standard] httpx python-dotenv pydantic pydantic-settings

# 4. Khởi động server
py -m uvicorn app.main:app --reload
```

Server chạy tại: **http://localhost:8000**

---

## Xem tài liệu API

Mở trình duyệt vào:

```
http://localhost:8000/docs
```

Giao diện Swagger cho phép test tất cả endpoint ngay trên trình duyệt.

---

## Smoke Test — Kiểm tra nhanh từng endpoint

### Health (bắt buộc test trước)
```
GET http://localhost:8000/api/v1/health
```
✅ Kết quả mong đợi:
```json
{ "status": "ok", "version": "0.1.0" }
```

---

### Places — Danh sách địa điểm
```
GET http://localhost:8000/api/v1/places
GET http://localhost:8000/api/v1/places?category=temple
GET http://localhost:8000/api/v1/places?limit=2&offset=0
```
✅ Kết quả: danh sách địa điểm có `id, name, category, description, latitude, longitude`

---

### Feed — Gợi ý địa điểm
```
GET http://localhost:8000/api/v1/feed
```
✅ Kết quả: mảng items có `score` (0-1) và `explanation` (lý do gợi ý)

---

### Storyline — Task tiếp theo
```
GET http://localhost:8000/api/v1/storyline/next-task?user_id=user-001
```
✅ Kết quả: task có `title, description, cultural_explanation, difficulty, completion_requirement`

---

### Captures — Gửi metadata ảnh chụp
```
POST http://localhost:8000/api/v1/captures
Content-Type: application/json

{
  "user_id": "user-001",
  "task_id": "task-001",
  "place_id": "place-001",
  "latitude": 21.0275,
  "longitude": 105.8357,
  "media_url": "https://placeholder.url/photo.jpg",
  "note": "Ảnh chụp tại Khuê Văn Các"
}
```
✅ Kết quả: object capture với `id` được tạo tự động

---

## Cấu trúc thư mục

```
backend/
├── app/
│   ├── main.py                   ← entry point
│   ├── core/
│   │   ├── config.py             ← settings (.env)
│   │   └── ai_core_client.py    ← contract với AI Core (mock mode)
│   ├── api/
│   │   ├── router.py
│   │   └── endpoints/
│   │       ├── health.py
│   │       ├── places.py
│   │       ├── feed.py
│   │       ├── storyline.py
│   │       ├── captures.py
│   │       └── logs.py
│   └── schemas/schemas.py
├── .env.example
└── requirements.txt
```

## Lưu ý cho team

- **AI Core**: đang dùng mock (`AI_CORE_USE_MOCK=true` trong `.env`). Đổi thành `false` khi AI Core team sẵn sàng.
- **Database**: captures lưu in-memory. Backend Engineer 1 sẽ kết nối DB thật ở Sprint 2.
- **CORS**: đang mở `*` cho dev. Sẽ giới hạn domain trước khi deploy production.
