# Project Specification: BeVietnam - Smart Tourism System

## 1. Thông tin chung (General Information)
* [cite_start]**Tên dự án:** BeVietnam - A Smart Tourism System for Vietnam[cite: 1].
* **Nhóm tác giả:** Khang P. Nguyen, Nghia T. Hoang, Cong N. Thien, Thai. V. Nguyen và Phi. [cite_start]H. Nguyen[cite: 2, 3].
* [cite_start]**Đơn vị:** Đại học Khoa học Tự nhiên - ĐHQG TP.HCM[cite: 4].
* [cite_start]**Ngày thực hiện:** 17 tháng 4 năm 2026[cite: 5].

## 2. Tầm nhìn và Vấn đề cốt lõi (Vision & Core Problems)
[cite_start]Hệ thống giải quyết khoảng cách giữa sự tăng trưởng du lịch và trải nghiệm thông tin số[cite: 58].
* [cite_start]**Vấn đề cốt lõi:** Du lịch tăng tốc nhưng người dùng khó tiếp cận thông tin đúng, đủ, kịp thời và có chiều sâu văn hóa[cite: 59].
* **Các điểm yếu (Paint Points):**
    * [cite_start]Thông tin phân tán trên nhiều nguồn (Maps, TripAdvisor, blog) và thiếu "câu chuyện" văn hóa[cite: 89, 90, 91].
    * [cite_start]Rào cản văn hóa: Dịch máy thông thường không truyền tải được bản sắc và niềm tự hào ẩm thực[cite: 93, 94].
    * [cite_start]Các địa điểm "hẻm phố" và sự kiện địa phương ít hiện diện trên các nền tảng quốc tế[cite: 101, 102, 104].
    * [cite_start]Dữ liệu cũ, thiếu cơ chế tự động xác minh độ tươi mới[cite: 106, 109].

## 3. Kiến trúc Hệ thống (System Architecture)
[cite_start]Hệ thống sử dụng kiến trúc **Multi-Agent System (MAS)** để phân rã bài toán thành các tác tử tự chủ[cite: 175, 176].

### Tầng 1: Nguồn dữ liệu (External Sources)
* [cite_start]Google Maps API, Facebook Events API, Food Blogs (Foody, Diadiemanuong), Gemini Flash LLM[cite: 286, 287, 288, 289, 290].

### Tầng 2: Lưu trữ (Data Store)
* [cite_start]**Postgres (Supabase):** Lưu trữ địa điểm, sự kiện, món ăn, người dùng[cite: 306].
* [cite_start]**Object Storage:** Lưu trữ hình ảnh[cite: 307].
* [cite_start]**Auth (Supabase):** Quản lý phiên đăng nhập qua JWT[cite: 308].

### Tầng 3: Tác tử xử lý (Agent Layer)
[cite_start]Gồm 7 Agents được điều phối bởi một **Orchestrator**[cite: 356]:
1.  [cite_start]**Crawler Agent:** Thu thập dữ liệu thô từ Google Maps, blog ẩm thực, báo địa phương[cite: 193, 217].
2.  [cite_start]**Event Monitor Agent:** Theo dõi sự kiện từ Facebook, Ticketbox; cập nhật mỗi 6 giờ[cite: 193, 223].
3.  [cite_start]**Cleaner Agent:** Validate, loại bỏ trùng lặp (Fuzzy dedup) và chuẩn hóa dữ liệu thô[cite: 195, 229].
4.  [cite_start]**Cultural Enricher Agent:** Sử dụng Gemini 2.0 Flash để bổ sung bối cảnh văn hóa, lịch sử[cite: 196, 234, 244].
5.  [cite_start]**Translator Agent:** Dịch thuật văn hóa ($Vi \rightarrow En$) đảm bảo giữ nguyên giá trị nội dung[cite: 197, 250, 256].
6.  [cite_start]**Feed Curator Agent:** Xếp hạng và cá nhân hóa feed cho từng người dùng dựa trên vị trí, sở thích[cite: 205, 265].
7.  [cite_start]**Community Moderator Agent:** Xét duyệt nội dung đóng góp từ cộng đồng bằng AI[cite: 206, 272].

### Tầng 4: API Layer (Backend)
* [cite_start]Sử dụng **Python + FastAPI** để triển khai REST API[cite: 309, 475].
* **Các Endpoints chính:**
    * [cite_start]`GET /feed`: Lấy luồng tin cá nhân hóa[cite: 310].
    * [cite_start]`GET /places`, `GET /place/{id}`: Tra cứu địa điểm[cite: 310].
    * [cite_start]`GET /events`, `GET /event/{id}`: Thông tin sự kiện thực tế[cite: 310].
    * [cite_start]`POST /community`: Tiếp nhận đóng góp từ người dùng[cite: 310].
    * [cite_start]`POST /auth`: Xác thực và định danh[cite: 310].

### Tầng 5: Client Layer (Website Requirement)
[cite_start]Mặc dù tài liệu gốc đề cập Mobile (Expo), Website cần thực hiện các chức năng tương đương[cite: 372]:
* [cite_start]**UI Modules:** Feed cá nhân hóa, Bản đồ hiển thị POI (Google Maps SDK), Chi tiết địa điểm song ngữ, Lịch sự kiện, Form đóng góp cộng đồng[cite: 313, 317, 319, 320].

## 4. Công nghệ chủ chốt (Core Tech Stack)
* [cite_start]**Backend:** FastAPI (Python), APScheduler (điều phối job)[cite: 475, 478].
* [cite_start]**AI/LLM:** Gemini 2.0 Flash (Free tier)[cite: 244, 477].
* [cite_start]**Infrastructure:** Vận hành trên Cloud Free Tier (Render, Supabase)[cite: 479].
* [cite_start]**Database:** PostgreSQL (Supabase)[cite: 476].

## 5. Ràng buộc và Mục tiêu (Constraints & Goals)
* [cite_start]**Chi phí:** Vận hành 100% trên Cloud Free Tier[cite: 177].
* [cite_start]**Độ tươi dữ liệu:** Cập nhật sự kiện mỗi 6 giờ[cite: 177].
* [cite_start]**Chất lượng nội dung:** Trên 80% mục dữ liệu có bối cảnh văn hóa từ AI[cite: 177].
* [cite_start]**Đa ngôn ngữ:** 100% song ngữ Việt - Anh cho mọi mục thông tin[cite: 177].