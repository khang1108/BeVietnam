# Phân rã bài toán — Hệ thống Du lịch Thông minh dựa trên Multi-Agent (BeVietnam)

**Phạm vi MVP:** TP. Hồ Chí Minh

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Phân rã bài toán (Decomposition)](#2-phân-rã-bài-toán-decomposition)
3. [Nhận dạng mẫu (Pattern Recognition)](#3-nhận-dạng-mẫu-pattern-recognition)
4. [Trừu tượng hóa (Abstraction)](#4-trừu-tượng-hóa-abstraction)
5. [Thiết kế thuật toán (Algorithm Design)](#5-thiết-kế-thuật-toán-algorithm-design)
6. [Kiến trúc hệ thống (System Architecture)](#6-kiến-trúc-hệ-thống-system-architecture)
7. [Pipeline chi tiết từng phần](#7-pipeline-chi-tiết-từng-phần)
8. [Sơ lược giải pháp kỹ thuật](#8-sơ-lược-giải-pháp-kỹ-thuật)

---

## 1. Tổng quan dự án

### 1.1 Tóm tắt

**BeVietnam** là một hệ thống du lịch thông minh được xây dựng trên kiến trúc Multi-Agent System (MAS), tập trung vào việc giúp khách du lịch (trong nước và quốc tế) khám phá chiều sâu văn hóa Việt Nam — từ ẩm thực đường phố trong những con hẻm nhỏ, các sự kiện địa phương hàng tuần, show ca nhạc, cho đến câu chuyện văn hóa vùng miền ẩn sau mỗi địa điểm.

Khác với các ứng dụng du lịch truyền thống chỉ liệt kê địa điểm nổi tiếng, VietCulture sử dụng một hệ thống đa tác tử (multi-agent) để:
- **Tự động thu thập** dữ liệu từ nhiều nguồn (Google Maps, Facebook Events, blog ẩm thực, báo địa phương)
- **Xác minh và làm sạch** dữ liệu thô thành thông tin đáng tin cậy
- **Làm giàu văn hóa** — dùng AI để bổ sung ngữ cảnh lịch sử, ý nghĩa vùng miền, cách thưởng thức món ăn theo kiểu người địa phương
- **Dịch thuật văn hóa** — không chỉ dịch chữ mà giải thích khái niệm Việt Nam cho khách nước ngoài
- **Cập nhật liên tục** sự kiện, quán mới mở, thay đổi giờ hoạt động

Trải nghiệm chính của người dùng là một **feed cá nhân hóa** — cuộn qua các câu chuyện văn hóa, sự kiện sắp diễn ra, và địa điểm ẩm thực được AI curate theo vùng miền và sở thích, trên ứng dụng mobile.

### 1.2 Đặt vấn đề

Với sự phát triển mạnh mẽ của thị trường du lịch trong nước, lượng khách du lịch đến Việt Nam đã tăng lên đáng kể. Theo VnEconomy, chỉ trong năm 2025, số lượng khách du lịch quốc tế đến Việt Nam đã tăng 20,4% so với năm trước và cao hơn 17,8% so với năm 2019 (trước dịch Covid-19), đánh dấu cột mốc cao nhất từ trước đến nay.

Tuy nhiên, trải nghiệm du lịch hiện tại tồn tại **bốn vấn đề cốt lõi**:

**P1 — Thông tin du lịch quá nhiều, thiếu chiều sâu văn hóa.** Thông tin về địa điểm nằm rải rác trên Google Maps, TripAdvisor, các blog cá nhân, và mạng xã hội. Không có nguồn nào cung cấp đầy đủ ngữ cảnh văn hóa — tại sao một quán phở nhỏ trong hẻm lại đặc biệt, món ăn này có nguồn gốc vùng miền nào, hay người địa phương thưởng thức nó ra sao. Khách du lịch chỉ thấy tên, địa chỉ, và vài bức ảnh — thiếu "linh hồn" của địa điểm.

**P2 — Khách nước ngoài khó nắm bắt văn hóa địa phương.** Khi một khách Pháp đứng trước quán "Bún bò Huế", Google Translate chỉ cho ra "Hue beef noodle soup" — không giải thích được rằng đây là niềm tự hào ẩm thực của miền Trung, rằng vị cay đặc trưng đến từ sả và ớt bột, hay rằng người Huế ăn bún bò vào buổi sáng như một nghi thức hàng ngày. Rào cản ngôn ngữ chỉ là bề nổi — rào cản văn hóa mới là vấn đề thực sự.

**P3 — Ẩm thực hẻm nhỏ và sự kiện địa phương.** Những quán ăn ngon nhất Sài Gòn thường nằm sâu trong hẻm, không có website, không đăng ký Google Business. Các sự kiện đường phố, show ca nhạc acoustic tại quán cà phê, chợ đêm cuối tuần — những trải nghiệm mà khách du lịch khao khát nhất — lại chỉ được quảng bá qua Facebook Groups địa phương hoặc truyền miệng. Chúng gần như không tồn tại trên các nền tảng du lịch quốc tế.

**P4 — Dữ liệu du lịch lỗi thời, không có cơ chế cập nhật liên tục.** Một quán ăn đóng cửa, một sự kiện bị hủy, giờ mở cửa thay đổi theo mùa — thông tin trên các ứng dụng du lịch hiện tại thường không phản ánh thực tế. Việc dựa vào người dùng đánh giá (crowdsourced reviews) không đủ nhanh, và không có hệ thống tự động xác minh tính cập nhật của dữ liệu.

### 1.3 Mục tiêu

Xây dựng một hệ thống Multi-Agent System có khả năng:

| ID | Mục tiêu | Đo lường thành công |
|----|----------|---------------------|
| G1 | Tự động thu thập dữ liệu địa điểm và sự kiện từ nhiều nguồn | Tối thiểu 200 POI + 50 events cho TP.HCM trong MVP |
| G2 | Làm giàu mỗi địa điểm/món ăn với ngữ cảnh văn hóa bằng AI | ≥ 80% items có cultural context sau khi pipeline chạy |
| G3 | Dịch thuật văn hóa Vi ↔ En (không chỉ dịch chữ) | Mỗi item có cả description_vi và description_en |
| G4 | Cung cấp feed cá nhân hóa trên ứng dụng mobile | Feed ranking dựa trên ≥ 4 factors |
| G5 | Cập nhật sự kiện liên tục (tối thiểu mỗi 6 giờ) | Event Monitor Agent chạy tự động theo lịch |
| G6 | Cho phép cộng đồng đóng góp và duy trì dữ liệu | Community submission flow với AI moderation |
| G7 | Vận hành với chi phí gần bằng 0 | Toàn bộ stack chạy trên free tier |

---

## 2. Phân rã bài toán (Decomposition)
### 2.1 Decomposition Level 1 — Bốn hệ thống con

Bài toán tổng thể được chia thành bốn hệ thống con (sub-system), mỗi hệ thống đảm nhiệm một trách nhiệm rõ ràng:

```
BeVietnam System
├── S1. Data Acquisition      — Thu thập dữ liệu thô từ bên ngoài
├── S2. Data Intelligence     — Xử lý, làm giàu, dịch thuật dữ liệu
├── S3. Content Delivery      — Phân phối nội dung đến người dùng
└── S4. User Platform         — Nền tảng tương tác người dùng
```

| Sub-system | Bài toán giải quyết | Input | Output |
|------------|---------------------|-------|--------|
| **S1. Data Acquisition** | Dữ liệu nằm rải rác trên nhiều nguồn khác nhau (P1, P3) | Source configs, keywords, geo-bounds | Raw data items (JSON) |
| **S2. Data Intelligence** | Dữ liệu thô cần được làm sạch, loại trùng lặp, bổ sung văn hóa, và dịch (P1, P2, P4) | Raw data items | Clean, enriched, translated records |
| **S3. Content Delivery** | Người dùng cần thấy nội dung phù hợp, không phải danh sách thô | All records + user context | Ranked, personalized feed |
| **S4. User Platform** | Người dùng cần giao diện để tương tác và đóng góp (P3) | User actions | UI responses, submissions |

### 2.2 Decomposition Level 2 — Agents và Modules

Mỗi sub-system được chia tiếp thành các **agent** hoặc **module**:

```
S1. Data Acquisition
├── Agent 1: Crawler Agent           — Scrape dữ liệu từ Google Maps, food blogs, báo
└── Agent 2: Event Monitor Agent     — Theo dõi sự kiện từ Facebook Events, Ticketbox, venues

S2. Data Intelligence
├── Agent 3: Cleaner Agent           — Validate, deduplicate, normalize
├── Agent 4: Cultural Enricher Agent — Thêm ngữ cảnh văn hóa qua LLM
└── Agent 5: Translator Agent        — Dịch thuật văn hóa Vi → En

S3. Content Delivery
├── Agent 6: Feed Curator Agent      — Xếp hạng và cá nhân hóa feed
└── Agent 7: Community Moderator Agent — Xét duyệt nội dung cộng đồng

S4. User Platform
├── Module: Auth & Identity          — Đăng nhập, quản lý phiên
├── Module: Mobile App (Expo)        — Giao diện người dùng
└── Module: Community Submission     — Form đóng góp địa điểm/sự kiện

Điều phối tất cả:
└── Orchestrator                     — Lên lịch, điều phối, xử lý lỗi
```

### 2.3 Decomposition Level 3 — Chi tiết từng Agent

#### Agent 1: Crawler Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Thu thập dữ liệu thô từ nhiều nguồn bên ngoài |
| **Input** | Danh sách sources + query config (khu vực, category, keywords) |
| **Output** | Raw data items lưu vào bảng `raw_crawl_data` |
| **Trigger** | Orchestrator lên lịch mỗi ngày (2:00 AM) |
| **Sources** | Google Maps Places API, food blogs (foody.vn, diadiemanuong.com), báo địa phương |
| **Thuật toán chính** | Adaptive crawl — ưu tiên source có data freshness cao |
| **Xử lý lỗi** | Retry tối đa 3 lần với exponential backoff; skip source nếu vẫn fail |

#### Agent 2: Event Monitor Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Theo dõi và cập nhật sự kiện địa phương hàng tuần |
| **Input** | Source configs cho events (Facebook Events, Ticketbox, venue pages) |
| **Output** | Event records với status (upcoming / cancelled / completed) |
| **Trigger** | Mỗi 6 giờ (events thay đổi nhanh hơn places) |
| **Categories** | Ca nhạc, Art, Food festival, Workshop, Night market, Street performance |
| **Thuật toán chính** | Change detection — so sánh dữ liệu mới với bản cũ, chỉ update khi có thay đổi |

#### Agent 3: Cleaner Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Validate, loại trùng lặp, chuẩn hóa dữ liệu thô |
| **Input** | Raw data từ `raw_crawl_data` |
| **Output** | Clean records vào `places`, `events`, `food_items` |
| **Trigger** | Chạy ngay sau khi Crawler Agent hoàn thành |
| **Thuật toán chính** | Fuzzy dedup (Levenshtein distance trên tên + address, kết hợp geo-proximity < 50m) |
| **Validation rules** | Tọa độ nằm trong bounding box TP.HCM, phone format hợp lệ, URL reachable |

#### Agent 4: Cultural Enricher Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Dùng LLM để thêm ngữ cảnh văn hóa cho mỗi địa điểm / món ăn |
| **Input** | Clean record (place hoặc food_item) + category |
| **Output** | Trường `cultural_context_vi` — câu chuyện văn hóa, lịch sử, ý nghĩa |
| **Trigger** | Sau Cleaner Agent, chỉ xử lý items chưa có cultural context |
| **LLM** | Gemini 2.0 Flash (free tier: 15 RPM, 1M tokens/ngày) |
| **Thuật toán chính** | Category-based prompt selection → LLM call → quality validation → store hoặc retry |

#### Agent 5: Translator Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Dịch thuật văn hóa Vi → En (không chỉ dịch chữ) |
| **Input** | Vietnamese content + cultural context |
| **Output** | Trường `description_en`, `cultural_context_en` |
| **Trigger** | Sau Cultural Enricher Agent |
| **LLM** | Gemini Flash (chia sẻ quota với Enricher Agent) |
| **Điểm khác biệt** | "Bánh mì" → không phải "bread" mà là "Vietnamese baguette sandwich — a fusion of French colonial bread with local herbs, pâté, and pickled vegetables" |

#### Agent 6: Feed Curator Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Xếp hạng và tạo feed cá nhân hóa cho mỗi user |
| **Input** | User context (location, locale, preferences, history) + all content |
| **Output** | Danh sách feed items đã được ranked |
| **Trigger** | On-demand khi user mở app hoặc refresh feed |
| **Thuật toán chính** | Weighted scoring formula (6 factors) — chi tiết tại mục 5.3 |

#### Agent 7: Community Moderator Agent

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Xét duyệt nội dung do cộng đồng đóng góp |
| **Input** | Community submission (new place, edit suggestion, event report) |
| **Output** | Trạng thái: Approved / Rejected / Needs Review |
| **Trigger** | On-demand khi có submission mới |
| **Pipeline** | Spam filter → Duplicate check → Content quality (LLM) → Auto-approve hoặc queue |

#### Orchestrator (Điều phối)

| Thuộc tính | Chi tiết |
|------------|---------|
| **Trách nhiệm** | Lên lịch, điều phối thứ tự chạy, xử lý lỗi, logging |
| **Scheduling** | Cron-based: daily crawl (2 AM), event monitor (mỗi 6h) |
| **Dependency resolution** | DAG: Crawler → Cleaner → Enricher → Translator |
| **Error handling** | Retry tối đa 3 lần, exponential backoff, log vào `agent_logs` |
| **Observability** | Mỗi agent run ghi: agent_name, duration_ms, status, input/output summary |

### 2.4 Agent Responsibility Matrix

Ma trận cho thấy mỗi agent chịu trách nhiệm với những thao tác nào trên dữ liệu:

| Agent | Đọc nguồn ngoài | Ghi raw data | Đọc raw data | Ghi clean data | Đọc clean data | Gọi LLM | Ghi feed | Đọc user context |
|-------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Crawler Agent | ✓ | ✓ | | | | | | |
| Event Monitor Agent | ✓ | ✓ | | | | | | |
| Cleaner Agent | | | ✓ | ✓ | | | | |
| Cultural Enricher Agent | | | | ✓ | ✓ | ✓ | | |
| Translator Agent | | | | ✓ | ✓ | ✓ | | |
| Feed Curator Agent | | | | | ✓ | | ✓ | ✓ |
| Community Moderator Agent | | | | ✓ | ✓ | ✓ | | |
| Orchestrator | | | | | | | | |

Orchestrator không thao tác dữ liệu trực tiếp — nó chỉ điều phối thứ tự thực thi và giám sát trạng thái các agent khác.

---

## 3. Nhận dạng mẫu (Pattern Recognition)
### 3.1 Patterns trong dữ liệu

**Pattern D1 — Duplicate across sources.** Cùng một quán phở xuất hiện trên Google Maps (tên tiếng Anh), trên foody.vn (tên tiếng Việt), và trên Facebook (tên viết tắt). Ba bản ghi khác tên nhưng cùng một thực thể.
- **Nhận dạng**: Tên khác nhau + địa chỉ tương tự + tọa độ gần nhau (< 50m)
- **Giải pháp tái sử dụng**: Fuzzy matching + geo-proximity clustering → áp dụng cho tất cả entity types (places, events, food items)

**Pattern D2 — Missing data fields.** Dữ liệu từ food blogs thường có tên + mô tả nhưng thiếu tọa độ. Dữ liệu từ Google Maps có tọa độ nhưng thiếu giá. Mỗi nguồn thiếu những trường khác nhau theo pattern có thể dự đoán.
- **Nhận dạng**: Source X luôn thiếu field Y
- **Giải pháp tái sử dụng**: Source-specific field mapping + cross-source enrichment (merge dữ liệu từ nhiều nguồn cho cùng entity)

**Pattern D3 — Spam và nội dung chất lượng thấp.** Community submissions có xu hướng spam theo mẫu: nội dung quá ngắn (< 10 ký tự), chứa URL quảng cáo, hoặc duplicate location đã tồn tại.
- **Nhận dạng**: Tập hợp rules dựa trên chiều dài, URL patterns, duplicate check
- **Giải pháp tái sử dụng**: Rule-based pre-filter chạy trước LLM quality check → tiết kiệm API calls

### 3.2 Patterns trong hành vi người dùng

**Pattern U1 — Content preference by locale.** Khách Việt Nam ưu tiên: events > food > culture. Khách nước ngoài ưu tiên: culture > food > events. Pattern này ảnh hưởng thứ tự feed.
- **Áp dụng**: Feed Curator Agent dùng `user.locale` làm weight modifier trong ranking formula.

**Pattern U2 — Time-based interest.** Cuối tuần: người dùng tìm events và giải trí nhiều hơn. Buổi trưa: tìm quán ăn. Buổi tối: tìm quán cà phê, bar, show.
- **Áp dụng**: Feed Curator Agent thêm time-based boost cho category phù hợp với thời điểm trong ngày/tuần.

**Pattern U3 — Proximity decay.** Sự quan tâm của người dùng giảm nhanh theo khoảng cách — quán cách 500m hấp dẫn hơn quán cách 5km, ngay cả khi quán xa có rating cao hơn.
- **Áp dụng**: Inverse distance weighting trong Feed Curator Agent.

### 3.3 Patterns trong agent pipeline

**Pattern P1 — ETL (Extract → Transform → Load).** Pipeline chính Crawler → Cleaner → Enricher → Translator lặp lại mẫu ETL cổ điển. Nhận diện pattern này cho phép áp dụng các best practices đã biết: idempotency, checkpoint/restart, logging mỗi stage.
- **Tái sử dụng**: Tất cả 4 agent trong chuỗi đều implement cùng interface `process(batch) → results + errors`.

**Pattern P2 — Event-driven trigger.** Khi user submit nội dung mới → trigger Community Moderator Agent. Khi Crawler hoàn thành → trigger Cleaner. Đây là pattern event-driven lặp lại.
- **Tái sử dụng**: Thiết kế Orchestrator dựa trên callback/event system thay vì polling.

**Pattern P3 — Request-response with caching.** Mỗi lần user mở feed → Feed Curator Agent tính toán ranking. Nhưng dữ liệu nền (places, events) không đổi liên tục. Pattern: cache kết quả ranking, invalidate khi dữ liệu nền thay đổi.
- **Tái sử dụng**: Cache layer trước Feed Curator Agent, TTL = 30 phút cho feed, 6 giờ cho place data.

### 3.4 Tổng hợp: Pattern → Quyết định thiết kế

| Pattern ID | Pattern | Quyết định thiết kế |
|------------|---------|---------------------|
| D1 | Duplicate across sources | Fuzzy dedup algorithm trong Cleaner Agent |
| D2 | Missing fields by source | Source-specific adapters + cross-source merge |
| D3 | Spam patterns | Rule-based pre-filter trước LLM moderation |
| U1 | Locale-based preference | Locale weight trong Feed ranking formula |
| U2 | Time-based interest | Time-of-day/week boost trong Feed ranking |
| U3 | Proximity decay | Inverse distance weighting |
| P1 | ETL pipeline | Shared agent interface: `process(batch)` |
| P2 | Event-driven trigger | Orchestrator callback system |
| P3 | Request-response caching | Cache layer với TTL-based invalidation |

---

## 4. Trừu tượng hóa (Abstraction)

Phần này xác định các tầng trừu tượng, giúp ẩn đi chi tiết không cần thiết và làm hệ thống dễ hiểu, dễ mở rộng.

### 4.1 Agent Interface Abstraction

Mặc dù 7 agent có logic nội bộ rất khác nhau (crawl web, gọi LLM, tính ranking), từ bên ngoài chúng đều tuân theo cùng một interface:

```
BaseAgent
├── name: string                          — Tên định danh
├── process(input: AgentInput) → AgentOutput  — Xử lý chính
├── validate(input: AgentInput) → bool    — Kiểm tra input hợp lệ
└── on_error(error: Error) → RetryOrSkip  — Xử lý lỗi
```

```
AgentInput
├── batch: list[Record]    — Dữ liệu cần xử lý
├── config: dict           — Cấu hình riêng cho agent
└── run_id: string         — ID để tracking

AgentOutput
├── results: list[Record]  — Dữ liệu đã xử lý
├── errors: list[Error]    — Lỗi phát sinh
└── metrics: dict          — duration_ms, items_processed, items_failed
```

Nhờ interface chung, Orchestrator không cần biết chi tiết bên trong mỗi agent — nó chỉ gọi `agent.process(input)` và nhận `output`. Thay đổi logic nội bộ của một agent không ảnh hưởng đến Orchestrator hay các agent khác.

### 4.2 Data Model Abstraction

Ba entity types chính (Place, Event, FoodItem) có nhiều trường chung. Thay vì thiết kế 3 model hoàn toàn riêng biệt, trừu tượng hóa các trường chung thành một base:

```
BaseEntity (abstract)
├── id: UUID
├── name: string
├── description_vi: text
├── description_en: text
├── cultural_context_vi: text
├── cultural_context_en: text
├── category: string
├── source: string             — Nguồn dữ liệu gốc
├── source_id: string          — ID trên nguồn gốc
├── verified: boolean          — Đã xác minh?
├── created_at: timestamp
└── updated_at: timestamp

Place extends BaseEntity
├── lat: float
├── lng: float
├── address: string
├── district: string
├── avg_rating: float
└── sentiment_score: float

Event extends BaseEntity
├── venue_place_id: FK → Place
├── start_time: timestamp
├── end_time: timestamp
├── recurrence: string         — "weekly", "monthly", "one-time"
├── status: string             — "upcoming", "cancelled", "completed"
└── ticket_url: string

FoodItem extends BaseEntity
├── place_id: FK → Place
├── name_vi: string
├── name_en: string
├── cultural_story_vi: text
├── cultural_story_en: text
├── price_range: string
└── is_signature: boolean
```

Nhờ abstraction này, các agent xử lý dữ liệu (Cleaner, Enricher, Translator) có thể dùng chung logic cho các trường base — chỉ cần thêm logic riêng cho trường đặc thù của mỗi entity type.

### 4.3 Pipeline Abstraction

Ba kiểu pipeline xuất hiện lặp lại trong hệ thống, được trừu tượng hóa thành 3 templates:

**Template 1 — Sequential Pipeline** (Crawler → Cleaner → Enricher → Translator)
```
SequentialPipeline
├── stages: list[Agent]            — Danh sách agents chạy tuần tự
├── run() → PipelineResult         — Chạy stage 1 → output → stage 2 → ...
└── checkpoint_after_each: bool    — Lưu kết quả trung gian để resume nếu fail
```

**Template 2 — On-demand Pipeline** (Feed Curator, Community Moderator)
```
OnDemandPipeline
├── agent: Agent                   — Agent xử lý request
├── handle(request) → response     — Xử lý một request đơn lẻ
└── cache_ttl: int                 — Cache kết quả trong bao lâu (giây)
```

**Template 3 — Scheduled Pipeline** (Event Monitor)
```
ScheduledPipeline
├── agent: Agent                   — Agent chạy định kỳ
├── interval: string               — "6h", "24h", "weekly"
└── run_and_diff() → changes       — Chạy và chỉ trả về thay đổi so với lần trước
```

### 4.4 Tầng trừu tượng toàn hệ thống

Hệ thống được chia thành các tầng trừu tượng, mỗi tầng chỉ giao tiếp với tầng liền kề:

```
┌─────────────────────────────────────────────────┐
│  Tầng 5: Mobile App (Expo/React Native)         │  ← Người dùng tương tác
├─────────────────────────────────────────────────┤
│  Tầng 4: REST API (FastAPI)                     │  ← Giao tiếp client-server
├─────────────────────────────────────────────────┤
│  Tầng 3: Agent Layer (7 Agents + Orchestrator)  │  ← Logic nghiệp vụ AI
├─────────────────────────────────────────────────┤
│  Tầng 2: Data Store (Postgres + Object Storage) │  ← Lưu trữ
├─────────────────────────────────────────────────┤
│  Tầng 1: External Adapters (APIs + Scrapers)    │  ← Nguồn dữ liệu ngoài
└─────────────────────────────────────────────────┘
```

Ví dụ: Mobile App (Tầng 5) không bao giờ gọi trực tiếp Gemini API (Tầng 1). Nó gọi REST API (Tầng 4), API chuyển request đến Feed Curator Agent (Tầng 3), agent đọc dữ liệu từ Postgres (Tầng 2). Mỗi tầng che giấu chi tiết implementation của tầng dưới.

---

## 5. Thiết kế thuật toán (Algorithm Design)

### 5.1 Orchestrator Scheduling Algorithm

Orchestrator quản lý thứ tự chạy các agent dựa trên DAG (Directed Acyclic Graph) phụ thuộc:

```
DAG Dependencies:
  Crawler Agent      → Cleaner Agent
  Event Monitor Agent → Cleaner Agent
  Cleaner Agent      → Cultural Enricher Agent
  Cultural Enricher  → Translator Agent
  Translator Agent   → (pipeline hoàn thành)
  Feed Curator Agent → (on-demand, không nằm trong DAG)
  Community Moderator → (on-demand, không nằm trong DAG)
```

**Pseudocode — DAG Executor:**

```
function execute_pipeline(dag):
    ready_queue = agents có 0 dependencies chưa hoàn thành
    completed = {}

    while ready_queue không rỗng:
        agent = ready_queue.dequeue()
        result = run_with_retry(agent, max_retries=3)

        if result.status == SUCCESS:
            completed.add(agent)
            for each downstream in dag.dependents(agent):
                if all dependencies of downstream in completed:
                    ready_queue.enqueue(downstream)
        else:
            log_failure(agent, result.errors)
            // Downstream agents không được enqueue → pipeline dừng tại nhánh này

    return completed
```

**Pseudocode — Retry with Exponential Backoff:**

```
function run_with_retry(agent, max_retries):
    for attempt in 1..max_retries:
        try:
            output = agent.process(input)
            log(agent.name, "SUCCESS", output.metrics)
            return output
        catch error:
            wait_time = 2^attempt * 1000ms    // 2s, 4s, 8s
            log(agent.name, "RETRY", attempt, error)
            sleep(wait_time)

    log(agent.name, "FAILED after max retries")
    return FailureResult(errors)
```

### 5.2 Deduplication Algorithm (Cleaner Agent)

Khi dữ liệu đến từ nhiều nguồn, cùng một quán ăn có thể xuất hiện nhiều lần với tên khác nhau. Thuật toán loại trùng lặp kết hợp 3 tín hiệu:

**Pseudocode:**

```
function deduplicate(new_record, existing_records):
    candidates = []

    for each existing in existing_records:
        score = 0

        // Signal 1: Source ID match (cùng nguồn, cùng ID → chắc chắn trùng)
        if new_record.source == existing.source
           AND new_record.source_id == existing.source_id:
            return DUPLICATE(existing)     // exact match

        // Signal 2: Geo-proximity
        distance = haversine(new_record.lat, new_record.lng,
                             existing.lat, existing.lng)
        if distance < 50 meters:
            score += 0.5

        // Signal 3: Fuzzy name matching
        name_similarity = 1 - levenshtein_ratio(
            normalize(new_record.name),
            normalize(existing.name)
        )
        if name_similarity > 0.7:
            score += name_similarity * 0.5

        if score >= 0.7:
            candidates.append((existing, score))

    if candidates:
        best_match = max(candidates, key=score)
        if best_match.score >= 0.85:
            return DUPLICATE(best_match)   // high confidence
        else:
            return MAYBE_DUPLICATE(best_match)  // cần review

    return NEW_RECORD

function normalize(name):
    // Bỏ dấu tiếng Việt, lowercase, bỏ ký tự đặc biệt
    return remove_diacritics(name).lower().strip()
```

**Haversine distance** (tính khoảng cách giữa 2 tọa độ GPS):

```
function haversine(lat1, lng1, lat2, lng2):
    R = 6371000  // bán kính Trái Đất (mét)
    φ1, φ2 = radians(lat1), radians(lat2)
    Δφ = radians(lat2 - lat1)
    Δλ = radians(lng2 - lng1)

    a = sin(Δφ/2)² + cos(φ1) * cos(φ2) * sin(Δλ/2)²
    c = 2 * atan2(√a, √(1-a))

    return R * c   // khoảng cách tính bằng mét
```

### 5.3 Feed Ranking Algorithm (Feed Curator Agent)

Mỗi content item trong feed được tính điểm bằng công thức weighted scoring:

```
feed_score(item, user) =
      w₁ × recency(item)
    + w₂ × proximity(item, user)
    + w₃ × popularity(item)
    + w₄ × diversity_bonus(item, current_feed)
    + w₅ × cultural_depth(item)
    + w₆ × event_urgency(item)
```

**Chi tiết từng factor:**

```
function recency(item):
    // Exponential decay: mới hơn → điểm cao hơn
    hours_ago = (now - item.updated_at).total_hours()
    return e^(-0.01 × hours_ago)     // decay rate λ = 0.01

function proximity(item, user):
    // Inverse distance: gần hơn → điểm cao hơn
    d = haversine(item.lat, item.lng, user.lat, user.lng)
    return 1 / (1 + d / 1000)        // normalize bằng km

function popularity(item):
    // Log-scaled để tránh popular items áp đảo
    interactions = item.view_count + 2 × item.save_count + 3 × item.like_count
    return log(1 + interactions) / log(1 + MAX_INTERACTIONS)

function diversity_bonus(item, current_feed):
    // Bonus nếu category chưa xuất hiện nhiều trong feed hiện tại
    category_count = count(current_feed, item.category)
    return 1 / (1 + category_count)   // giảm dần khi cùng category xuất hiện nhiều

function cultural_depth(item):
    // Items có cultural context sâu → ưu tiên hơn
    if item.cultural_context is not empty:
        return len(item.cultural_context) / MAX_CONTEXT_LENGTH
    return 0

function event_urgency(item):
    // Chỉ áp dụng cho events: sắp diễn ra → boost
    if item.type != "event": return 0
    hours_until = (item.start_time - now).total_hours()
    if hours_until < 0: return 0      // đã qua
    if hours_until < 24: return 1.0   // trong 24h tới
    if hours_until < 72: return 0.5   // trong 3 ngày tới
    return 0.1
```

**Default weights** (điều chỉnh dựa trên `user.locale`):

| Weight | Khách Việt Nam | Khách nước ngoài |
|--------|:-:|:-:|
| w₁ (recency) | 0.20 | 0.15 |
| w₂ (proximity) | 0.25 | 0.20 |
| w₃ (popularity) | 0.15 | 0.10 |
| w₄ (diversity) | 0.10 | 0.10 |
| w₅ (cultural_depth) | 0.10 | 0.25 |
| w₆ (event_urgency) | 0.20 | 0.20 |

Khách nước ngoài được tăng weight `cultural_depth` (0.25 vs 0.10) vì đây là giá trị cốt lõi mà VietCulture cung cấp cho họ.

### 5.4 Cultural Enrichment Algorithm (Cultural Enricher Agent)

```
function enrich_batch(items):
    prompt_templates = load_templates_by_category()
    results = []

    for item in items:
        if item.cultural_context_vi is not empty:
            skip    // đã enrich rồi

        template = prompt_templates[item.category]
        prompt = template.format(
            name=item.name,
            district=item.district,
            category=item.category,
            description=item.description_vi
        )

        response = call_llm(prompt, model="gemini-flash")

        // Quality validation
        if len(response) < 50:
            retry with more specific prompt
        if contains_hallucination_markers(response):
            discard and log warning

        item.cultural_context_vi = response
        results.append(item)

    return results

function contains_hallucination_markers(text):
    // Phát hiện dấu hiệu LLM bịa đặt
    markers = ["theo truyền thuyết kể rằng", "không có thông tin chính xác",
               "tôi không chắc chắn", "có thể là"]
    return any(marker in text.lower() for marker in markers)
```

### 5.5 Community Moderation Algorithm (Community Moderator Agent)

```
function moderate(submission):
    // Stage 1: Rule-based spam filter (nhanh, không tốn LLM)
    spam_score = 0
    if len(submission.description) < 10: spam_score += 0.4
    if contains_url_patterns(submission.description): spam_score += 0.3
    if submission.user.account_age < 1 day: spam_score += 0.2
    if user_submission_count_today(submission.user) > 5: spam_score += 0.3

    if spam_score >= 0.7:
        return REJECTED(reason="spam detected")

    // Stage 2: Duplicate check
    dedup_result = deduplicate(submission, existing_records)
    if dedup_result == DUPLICATE:
        return REJECTED(reason="duplicate of " + dedup_result.match.id)

    // Stage 3: LLM content quality check
    quality_prompt = f"""
    Đánh giá bài đóng góp sau cho hệ thống du lịch:
    Tên: {submission.name}
    Mô tả: {submission.description}
    Loại: {submission.entity_type}

    Cho điểm 1-10 về: (a) tính chính xác, (b) hữu ích cho du khách,
    (c) có nội dung không phù hợp không?
    Trả lời JSON: {{"accuracy": N, "usefulness": N, "inappropriate": bool}}
    """

    quality = call_llm(quality_prompt, model="gemini-flash")

    if quality.inappropriate:
        return REJECTED(reason="inappropriate content")
    if quality.accuracy >= 7 AND quality.usefulness >= 6:
        return APPROVED
    else:
        return NEEDS_REVIEW(quality_scores=quality)
```

---

## 6. Kiến trúc hệ thống (System Architecture)

### 6.1 Sơ đồ kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SOURCES (Tầng 1)                        │
│  ┌──────────┐  ┌──────────────┐  ┌───────────┐  ┌───────────────┐   │
│  │ Google   │  │ Facebook     │  │ Food      │  │ Gemini Flash  │   │
│  │ Maps API │  │ Events API   │  │ Blogs     │  │ LLM API       │   │
│  └────┬─────┘  └───────┬──────┘  └──────┬────┘  └────────┬──────┘   │
└───────┼────────────────┼────────────────┼────────────────┼──────────┘
        │                │                │                │
┌───────▼────────────────▼────────────────▼────────────────▼──────────┐
│                    AGENT LAYER (Tầng 3)                             │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                     ORCHESTRATOR                            │    │
│  │  ┌─────────┐  ┌─────────────┐  ┌──────────┐  ┌──────────┐   │    │
│  │  │Scheduler│  │DAG Executor │  │Retry Mgr │  │Logger    │   │    │
│  │  └─────────┘  └─────────────┘  └──────────┘  └──────────┘   │    │
│  └─────────────────────┬───────────────────────────────────────┘    │
│                        │ điều phối                                  │
│  ┌──────────┐ ┌────────┴──┐ ┌───────────┐ ┌────────────┐            │
│  │ Crawler  │→│  Cleaner  │→│ Cultural  │→│ Translator │            │
│  │ Agent    │ │  Agent    │ │ Enricher  │ │ Agent      │            │
│  └──────────┘ └───────────┘ └───────────┘ └────────────┘            │
│  ┌──────────────┐  ┌────────────┐  ┌─────────────────┐              │
│  │Event Monitor │  │Feed Curator│  │Community        │              │
│  │Agent         │  │Agent       │  │Moderator Agent  │              │
│  └──────────────┘  └────────────┘  └─────────────────┘              │ 
└────────────────────────────┬────────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────────┐
│                    DATA STORE (Tầng 2)                              │
│  ┌──────────────────┐  ┌─────────────────┐  ┌───────────────────┐   │
│  │ Postgres (Supa.) │  │ Object Storage  │  │ Auth (Supabase)   │   │
│  │ places, events,  │  │ images          │  │ JWT tokens        │   │
│  │ food_items, users│  │                 │  │                   │   │
│  └────────┬─────────┘  └─────────────────┘  └───────────────────┘   │
└───────────┼─────────────────────────────────────────────────────────┘
            │
┌───────────▼─────────────────────────────────────────────────────────┐
│                    API LAYER (Tầng 4) — FastAPI                     │
│  GET /feed     GET /places    GET /events    POST /community        │
│  GET /place/:id   GET /event/:id   GET /search   POST /auth         │
└───────────┬─────────────────────────────────────────────────────────┘
            │ REST (JSON)
┌───────────▼─────────────────────────────────────────────────────────┐
│                    CLIENT (Tầng 5) — Expo / React Native            │
│  ┌──────────┐ ┌───────────┐ ┌────────┐ ┌───────────┐ ┌─────────┐    │
│  │Feed      │ │Explore    │ │Detail  │ │Events     │ │Community│    │
│  │Screen    │ │Map Screen │ │Screen  │ │Calendar   │ │Submit   │    │
│  └──────────┘ └───────────┘ └────────┘ └───────────┘ └─────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 Luồng dữ liệu chính (Data Flow)

```
[Google Maps] ──┐
[Food Blogs] ───┤──→ Crawler Agent ──→ raw_crawl_data (Postgres)
[Local News] ───┘                              │
                                               ▼
[Facebook Events] ──→ Event Monitor ──→ Cleaner Agent ──→ places / events / food_items
[Ticketbox]  ───────────────┘                  │
                                               ▼
                                    Cultural Enricher Agent
                                    (+ Gemini Flash LLM)
                                               │
                                               ▼
                                    Translator Agent
                                    (+ Gemini Flash LLM)
                                               │
                                               ▼
                                    Clean, enriched, translated data
                                    stored in Postgres
                                               │
                                               ▼
                   User opens app ──→ Feed Curator Agent ──→ Ranked feed ──→ Mobile App
```

### 6.3 Luồng tương tác người dùng

**Luồng 1 — Xem feed (chính):**
```
User mở app → GET /feed?lat=X&lng=Y&locale=vi
  → API gọi Feed Curator Agent
    → Agent đọc places + events + food_items từ Postgres
    → Tính feed_score cho mỗi item
    → Sort descending, lấy top 20
  → API trả JSON response
→ Mobile render feed cards
```

**Luồng 2 — Xem chi tiết địa điểm:**
```
User tap vào feed card → GET /place/:id
  → API đọc place + food_items + media từ Postgres
  → Trả về full detail (including cultural_context)
→ Mobile render detail screen
  → Hiển thị cultural story, ảnh, bản đồ, món ăn đặc trưng
```

**Luồng 3 — Đóng góp cộng đồng:**
```
User tap "Thêm địa điểm" → Điền form → POST /community
  → API tạo community_submission
  → Trigger Community Moderator Agent
    → Spam filter → Dedup check → LLM quality check
    → Nếu APPROVED: insert vào places/events
    → Nếu REJECTED: thông báo lý do
  → API trả kết quả
→ Mobile hiển thị trạng thái submission
```

### 6.4 Sơ đồ Agent Orchestration

```
                    ┌───────────────┐
                    │  Orchestrator │
                    │  (Scheduler)  │
                    └──────┬────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼──────┐ ┌───▼────┐ ┌────▼───────────┐
        │ Daily Job  │ │ 6h Job │ │ On-Demand Jobs │
        │ (2:00 AM)  │ │        │ │                │
        └─────┬──────┘ └───┬────┘ └────┬───────────┘
              │            │           │
    ┌─────────▼──────┐  ┌──▼──────────┐│  ┌──────────────────┐
    │ Crawler Agent  │  │Event Monitor││  │ Feed Curator     │ ← user request
    └─────────┬──────┘  └──┬──────────┘│  └──────────────────┘
              │            │           │  ┌──────────────────┐
              └─────┬──────┘           └──│Community Mod     │ ← user submission
                    ▼                     └──────────────────┘
           ┌────────────────┐
           │ Cleaner Agent  │
           └───────┬────────┘
                   ▼
           ┌────────────────────┐
           │Cultural Enricher   │
           └───────┬────────────┘
                   ▼
           ┌────────────────┐
           │Translator Agent│
           └───────┬────────┘
                   ▼
           Pipeline Complete
           (log to agent_logs)
```

---

## 7. Pipeline chi tiết từng phần

### 7.1 Data Acquisition Pipeline

**Mục tiêu:** Thu thập dữ liệu thô về địa điểm, quán ăn, sự kiện từ nhiều nguồn bên ngoài.

**Sources và chiến lược cho mỗi nguồn:**

| Source | Phương pháp | Dữ liệu thu được | Giới hạn |
|--------|-------------|-------------------|----------|
| Google Maps Places API | REST API (Nearby Search) | Tên, tọa độ, rating, reviews, giờ mở cửa, ảnh | $200/tháng free credit |
| Food blogs (foody.vn, diadiemanuong.com) | Web scraping (BeautifulSoup + httpx) | Tên quán, mô tả, giá, ảnh, review | Cần respect robots.txt, rate limit |
| Facebook Events | Graph API (public events) | Tên event, thời gian, địa điểm, mô tả | Chỉ public events, API hạn chế |
| Ticketbox | Web scraping | Show ca nhạc, workshop, thời gian, giá vé | Structured data, ít thay đổi |
| Báo địa phương | Web scraping (RSS + HTML) | Sự kiện, festival, tin du lịch | Unstructured, cần NLP extract |

**Luồng xử lý:**

```
1. Orchestrator gọi Crawler Agent với config:
   - sources: ["google_maps", "foody", "diadiemanuong", "ticketbox"]
   - geo_bounds: {sw: [10.65, 106.55], ne: [10.90, 106.85]}  // TP.HCM
   - categories: ["food", "entertainment", "culture", "event"]

2. Crawler Agent chạy từng source adapter:
   a. google_maps_adapter.crawl(bounds, categories) → raw items
   b. foody_adapter.crawl(bounds) → raw items
   c. ...

3. Mỗi raw item được normalize thành schema chung:
   {
     source: "google_maps",
     source_id: "ChIJ...",
     raw_data: { ... original response ... },
     entity_type: "place",
     crawled_at: "2026-04-06T02:15:00Z",
     status: "pending"
   }

4. Insert vào raw_crawl_data table.

5. Log: "Crawled 150 items from 4 sources in 45s"
```

### 7.2 Data Processing Pipeline

**Mục tiêu:** Biến dữ liệu thô thành dữ liệu sạch, có văn hóa, đa ngôn ngữ.

**Stage 1 — Cleaner Agent:**

```
Input: raw_crawl_data WHERE status = 'pending'

Bước 1: Validate
  - Tọa độ nằm trong bounding box TP.HCM? (10.65 ≤ lat ≤ 10.90, 106.55 ≤ lng ≤ 106.85)
  - Tên không rỗng?
  - Category thuộc taxonomy cho phép?
  → Invalid items: status = 'rejected', log lý do

Bước 2: Normalize
  - Address: chuẩn hóa "Q.1" → "Quận 1", "p.Bến Nghé" → "Phường Bến Nghé"
  - Category: map từ source-specific → unified taxonomy
  - Phone: chuẩn hóa format (+84...)
  - Name: trim, capitalize

Bước 3: Deduplicate
  - Chạy dedup algorithm (mục 5.2) so với existing records
  - DUPLICATE → merge fields (lấy data mới hơn), update existing record
  - MAYBE_DUPLICATE → flag for review
  - NEW_RECORD → insert vào places/events/food_items

Bước 4: Update status
  - raw_crawl_data.status = 'processed'

Output: Clean records trong places/events/food_items
```

**Stage 2 — Cultural Enricher Agent:**

```
Input: places/food_items WHERE cultural_context_vi IS NULL

Bước 1: Select prompt template theo category
  - "food" → food_culture_prompt.txt
  - "entertainment" → entertainment_prompt.txt
  - "historical" → history_prompt.txt

Bước 2: Gọi Gemini Flash API
  - Rate limit: max 15 requests/phút (free tier)
  - Batch processing: xử lý 15 items mỗi phút

Bước 3: Validate response
  - Length >= 50 chars? (quá ngắn = LLM trả lời chung chung)
  - Không chứa hallucination markers?
  - Pass → save cultural_context_vi
  - Fail → retry 1 lần với prompt cụ thể hơn, nếu vẫn fail → skip, log warning

Output: Records với cultural_context_vi đã được điền
```

**Stage 3 — Translator Agent:**

```
Input: records WHERE description_en IS NULL AND description_vi IS NOT NULL

Bước 1: Gọi Gemini Flash với translation prompt
  - Không dùng dịch máy thông thường
  - Prompt yêu cầu "cultural translation" — giải thích concept VN cho foreigner
  - Ví dụ: "Translate and explain for a foreign tourist who knows nothing
    about Vietnamese culture. Don't just translate words — explain meaning."

Bước 2: Save description_en, cultural_context_en

Output: Records đầy đủ Vi + En
```

### 7.3 Event Monitoring Pipeline

**Mục tiêu:** Theo dõi sự kiện địa phương, cập nhật thường xuyên hơn data tĩnh.

```
Chạy mỗi 6 giờ:

1. Crawl events từ sources (Facebook Events, Ticketbox, venue pages)

2. So sánh với events hiện có trong DB:
   a. Event mới (chưa có source_id trong DB) → Insert với status "upcoming"
   b. Event đã có nhưng thay đổi (thời gian, địa điểm, mô tả) → Update
   c. Event đã qua end_time → status = "completed"
   d. Event không còn xuất hiện trên source → status = "cancelled" (soft)

3. Cho events mới → chuyển qua Cleaner → Enricher → Translator pipeline

4. Notify Feed Curator: invalidate cache cho events-related feeds

5. Log: "Event monitor: 5 new, 3 updated, 12 completed, 1 cancelled"
```

### 7.4 Feed Generation Pipeline

**Mục tiêu:** Khi user mở app, trả về feed cá nhân hóa trong < 500ms.

```
1. User request: GET /feed?lat=10.78&lng=106.70&locale=en&page=1

2. Check cache:
   - Cache key = hash(lat_rounded, lng_rounded, locale, page)
   - Nếu cache hit và TTL < 30 phút → return cached feed

3. Nếu cache miss → Feed Curator Agent:
   a. Query candidates từ Postgres:
      - Places trong radius 5km từ user
      - Events có status "upcoming" và start_time trong 7 ngày tới
      - Food items tại các places đã query
      - Limit: 200 candidates

   b. Tính feed_score cho mỗi candidate (algorithm mục 5.3)

   c. Sort by feed_score descending

   d. Diversity pass:
      - Duyệt top-down, nếu 3 items liên tiếp cùng category → swap item thứ 3
        với item tiếp theo có category khác
      - Đảm bảo feed đa dạng, không toàn food hoặc toàn events

   e. Paginate: lấy items cho page requested (20 items/page)

4. Cache result (TTL = 30 phút)

5. Return JSON response với feed items
```

### 7.5 Community Pipeline

**Mục tiêu:** Cho phép cộng đồng đóng góp địa điểm mới và duy trì tính cập nhật.

```
1. User điền form trên app:
   - Tên địa điểm/sự kiện
   - Mô tả (Vi hoặc En)
   - Category
   - Tọa độ (từ pin trên map hoặc GPS hiện tại)
   - Ảnh (optional)

2. POST /community → tạo community_submission với status "pending"

3. Community Moderator Agent xử lý (algorithm mục 5.5):
   a. Spam filter (rule-based, instant)
   b. Duplicate check (so với existing data)
   c. LLM quality check (Gemini Flash)

4. Kết quả:
   - APPROVED → Insert vào places/events → Chạy qua Enricher + Translator
   - REJECTED → Thông báo user với lý do
   - NEEDS_REVIEW → Queue cho admin/team review (MVP: manual check)

5. User nhận notification về trạng thái submission
```

---

## 8. Sơ lược giải pháp kỹ thuật

### 8.1 Công nghệ dự kiến

| Tầng | Công nghệ | Vai trò |
|------|-----------|---------|
| Mobile | Expo (React Native) + Expo Router | Ứng dụng mobile cross-platform |
| Backend API | Python FastAPI | REST API, cùng ngôn ngữ với agent code |
| Agent Framework | Python (custom) | 7 agents + Orchestrator, tự viết để demo algorithm design |
| Database | Supabase (Postgres) | Lưu trữ chính, auth, storage |
| AI/LLM | Google Gemini 2.0 Flash | Cultural enrichment, translation, moderation |
| Map | react-native-maps + Google Maps SDK | Bản đồ tương tác trên mobile |
| Web Scraping | BeautifulSoup + httpx | Thu thập dữ liệu từ food blogs, event pages |
| Scheduling | APScheduler | Cron jobs cho agent orchestration |
| Hosting | Render Free Tier | Backend deployment |

### 8.2 Chi phí

Toàn bộ hệ thống được thiết kế để chạy trên free tier, phù hợp với ngân sách đồ án:

| Dịch vụ | Free Tier | Cách tối ưu |
|---------|-----------|-------------|
| Supabase | 500MB DB, 1GB storage, 50K auth users | Chỉ scope TP.HCM; purge raw_crawl_data đã processed; nén ảnh |
| Gemini Flash | 15 RPM, 1M tokens/ngày | Cache kết quả enrichment; chỉ enrich items chưa có context; batch processing |
| Render | 750h/tháng, sleep sau 15 phút | Dùng cron-job.org để keep-alive; accept cold start delay |
| Google Maps SDK | Free cho mobile apps | Không giới hạn cho MVP |

**Ước tính sử dụng:**
- 200 POI × enrichment prompt (~200 tokens/item) = 40K tokens (<<< 1M/ngày limit)
- 200 POI × translation prompt (~300 tokens/item) = 60K tokens
- Event monitoring mỗi 6h × ~50 events = 200 LLM calls/ngày (<<< 15 RPM = 21.600/ngày)
- DB storage: 200 places × ~2KB + 100 events × ~1KB + metadata ≈ < 1MB (<<< 500MB limit)

### 8.3 Rủi ro và giảm thiểu

| # | Rủi ro | Mức độ | Giảm thiểu |
|---|--------|--------|------------|
| R1 | **LLM hallucination** — Gemini bịa đặt thông tin văn hóa sai | Cao | Hallucination detection trong prompt output; chỉ enrich facts có thể verify; disclaimer trên UI |
| R2 | **Free tier rate limits** — vượt quota Gemini hoặc Supabase | Trung bình | Rate limiter trong agent code; cache aggressively; batch processing vào off-peak |
| R3 | **Web scraping bị chặn** — Food blogs block scraper | Trung bình | Respect robots.txt; rate limit requests; fallback sang source khác; community contributions bù đắp |
| R4 | **Dữ liệu lỗi thời** — quán đóng cửa nhưng vẫn hiển thị | Trung bình | Community report flow; periodic re-crawl; "Xác nhận" button trên UI |
| R5 | **Timeline 2 tháng** — scope quá lớn cho 4-6 người | Cao | MVP strict: chỉ TP.HCM, chỉ feed + detail + community; cut map explore nếu cần; song song hóa frontend và backend work |
