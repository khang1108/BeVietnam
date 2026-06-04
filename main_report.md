## Problem Analysis & Decomposition — Agent-Based Smart Tourism System for Vietnam
## 1. Problem Analysis 

### 1.1 Du lịch ở Việt Nam 

Ngành du lịch Việt Nam đang trải qua giai đoạn tăng trưởng mạnh mẽ chưa từng có. Theo VnEconomy, riêng trong năm 2025, lượng khách quốc tế đến Việt Nam đã tăng **20,4%** so với cùng kỳ năm trước và vượt **17,8%** so với mức trước đại dịch COVID-19 năm 2019, thiết lập kỷ lục mới cho toàn ngành. Song song với sự tăng trưởng này, thị trường du lịch nội địa cũng bùng nổ khi người Việt ngày càng có xu hướng khám phá chính đất nước mình, đặc biệt là ẩm thực và văn hoá vùng miền.

Tuy nhiên, sự tăng trưởng về lượng du khách không đi kèm với sự cải thiện tương xứng về chất lượng trải nghiệm du lịch. Du khách — cả trong nước lẫn quốc tế — đang đối mặt với một nghịch lý: thông tin về du lịch Việt Nam tràn ngập trên internet, nhưng lại không có một nguồn nào đủ sâu, đủ tin cậy, và đủ cập nhật để thực sự giúp họ hiểu và trải nghiệm Việt Nam một cách trọn vẹn. 

### 1.2 Đặt vấn đề 
<!-- Cần nói là người dùng hướng tới là ai, họ đang gặp vấn đề gì khi du lịch. -->
<!--TODO: Tìm hiểu thêm một vài ứng dụng/web về du lịch ở Việt Nam -->

### 1.3 Paint Points
<!--TODO: Thêm ví dụ cụ thể, khảo sát các tin tức -->

Qua quá trình khảo sát và phân tích thực tế, bốn vấn đề cốt lõi đã được nhận diện. Mỗi vấn đề không tồn tại đơn lẻ mà có mối liên hệ nhân quả với nhau, tạo thành một chuỗi thách thức mà bất kỳ giải pháp nào cũng cần phải giải quyết đồng thời.

**Vấn đề P1 — Thông tin phân tán và thiếu chiều sâu văn hoá.** Thông tin du lịch hiện tại bị phân mảnh trên nhiều nền tảng(*Google Maps, TripAdvisor, blog cá nhân, mạng xã hội*). Mỗi nguồn cung cấp một rất nhiều thông tin khác nhau nhưng không nguồn nào đủ chiều sâu về văn hóa, các thông tin chỉ đơn thuần các bài tóm tắt ngắn ngủi. Google Maps cho ta được tọa độ và giờ mở cửa nhưng thiếu câu chuyện ẩn sâu bên trong. Blog cá nhân cho ta mô tả sinh động nhưng thiếu tính hệ thống. TripAdvisor có đánh giá nhưng thiên về góc nhìn du khách nước ngoài. Du khách chỉ nhìn thấy tên, địa chỉ, và vài tấm ảnh — không ai giải thích được tại sao quán phở nhỏ trong con hẻm kia lại đặc biệt, món ăn đó có nguồn gốc từ vùng miền nào, hay người địa phương thực sự thưởng thức nó như thế nào.

**Vấn đề P2 — Du khách quốc tế gặp rào cản văn hoá, không chỉ ngôn ngữ.** Khi một du khách người Pháp đứng trước quán "Bún bò Huế", Google Translate chỉ dịch ra "Hue beef noodle soup" — thất bại hoàn toàn trong việc truyền tải niềm tự hào ẩm thực miền Trung, rằng vị cay đặc trưng đến từ sả và ớt xay, rằng người Huế ăn bún bò vào buổi sáng. Rào cản ngôn ngữ chỉ là bề mặt; rào cản văn hoá — sự thiếu hiểu biết về ý nghĩa sâu xa đằng sau mỗi món ăn, mỗi địa danh — mới là vấn đề thực sự khiến trải nghiệm du lịch trở nên hời hợt.

**Vấn đề P3 — Ẩm thực hẻm phố và sự kiện địa phương** Những quán ăn ngon nhất Sài Gòn thường nằm sâu trong hẻm, không có website hay Google Business Listing. Các sự kiện đường phố, đêm nhạc acoustic tại quán cà phê, chợ đêm cuối tuần — những trải nghiệm du khách khao khát nhất — chỉ được quảng bá qua hội nhóm kín hoặc truyền miệng. Chúng gần như không tồn tại trên các nền tảng du lịch quốc tế. Điều này khiến cho du khách nước ngoài khó tiếp cận tới các đặc sắc văn hóa của địa phương.

**Vấn đề P4 — Dữ liệu du lịch cũ, không được cập nhật tự động.** Một nhà hàng đóng cửa, một sự kiện bị hủy, giờ mở cửa thay đổi theo mùa — thông tin trên các ứng dụng du lịch hiện tại thường không phản ánh đúng thực tế. Việc dựa vào đánh giá cộng đồng (crowdsource) không đủ nhanh, và không có hệ thống tự động nào để xác minh độ tươi mới của dữ liệu.

### 1.4 Đặt câu hỏi phân tích
Từ bốn vấn đề cốt lõi trên, quá trình phân tích bài toán tiếp tục bằng việc đặt ra các câu hỏi trọng tâm nhằm xác định phạm vi giải pháp và ràng buộc kỹ thuật trong dự án.

- *"Làm thế nào để tập hợp thông tin từ nhiều nguồn không đồng nhất (Google Maps API, blog ẩm thực HTML, Facebook Events API, tin tức địa phương) vào một hệ thống duy nhất, khi mỗi nguồn có cấu trúc dữ liệu, giao thức truy cập, và giới hạn tốc độ hoàn toàn khác nhau?"* Câu hỏi này xuất phát từ P1 và P3, và đây chính là câu hỏi quan trọng nhất, vì toàn bộ hệ thống nói chung và các Agent nói riêng để hoạt động mượt mà, chính xác và đa dạng thì nó cần có một nguồn dữ liệu dồi dào. Bước này định hình yêu cầu cho hệ thống con thu thập dữ liệu — cần thiết kế các adapter chuyên biệt cho từng nguồn nhưng xuất ra cùng một định dạng chung.

- *"Bằng cách nào để bổ sung chiều sâu văn hoá cho mỗi địa điểm và món ăn một cách tự động, khi kiến thức văn hoá không nằm sẵn trong dữ liệu thu thập?"* Câu hỏi này xuất phát từ P1 và P2. Dữ liệu từ Google Maps hay Foody chỉ có tên, địa chỉ, đánh giá — không có "câu chuyện" đằng sau. Và nếu như ta có dữ liệu và tài liệu thì làm sao để ta có thể biến chúng thành các thông tin mà các Agents có thể làm việc và thao tác với chúng.

- *"Dịch ở ngữ cảnh văn hóa, khác gì so với dịch ngôn ngữ và làm thế nào để tự động hoá quá trình này?"* Đây là câu hỏi cốt lõi cho P2. "Bánh mì" không nên được dịch thành "bread" mà cần giải thích là "Vietnamese baguette sandwich — a fusion of French colonial bread with local herbs, pâté, and pickled vegetables." Câu hỏi này xác nhận rằng hệ thống cần một thành phần AI chuyên biệt cho dịch thuật văn hoá, không thể dùng dịch máy thông thường.

- *"Làm thế nào để đảm bảo dữ liệu luôn cập nhật khi nguồn gốc dữ liệu thay đổi liên tục?"* Xuất phát từ P4, câu hỏi này dẫn đến yêu cầu cơ chế giám sát định kỳ — đặc biệt với sự kiện (thay đổi giờ, hủy bỏ) cần được phát hiện nhanh hơn dữ liệu tĩnh (quán ăn, địa điểm). Ta cần phải cài đặt thời gian như nào? Bao lâu cào một lần, và cào như vậy có ổn và có các thách thức gì không? 

- *"Làm sao kết hợp dữ liệu đã xử lý thành trải nghiệm cá nhân hoá cho từng du khách, dựa trên vị trí, sở thích, ngôn ngữ, và thời điểm?"* Câu hỏi này vượt ra khỏi bốn vấn đề ban đầu để hướng đến giá trị cốt lõi của sản phẩm: không chỉ giải quyết vấn đề dữ liệu mà còn mang lại trải nghiệm vượt trội cho người dùng cuối.

- *"Với ràng buộc ngân sách gần bằng không, toàn bộ hệ thống có khả thi khi chạy hoàn toàn trên free tier của các dịch vụ cloud không?"* Đây là ràng buộc thực tế quan trọng, buộc mọi quyết định thiết kế phải tối ưu cho chi phí vận hành bằng không.

### 1.5 Xác định Input / Output của bài toán

Từ quá trình đặt câu hỏi, Input và Output của hệ thống tổng thể được xác định rõ ràng như sau.

**Input của hệ thống:**

| Loại Input | Mô tả | Ví dụ cụ thể |
|------------|--------|---------------|
| Nguồn dữ liệu ngoài | Dữ liệu thô từ nhiều nguồn khác nhau | Google Maps Places API trả về JSON (tên, tọa độ, rating, reviews); Foody.vn trả về HTML (tên quán, mô tả, ảnh); Facebook Events API trả về sự kiện công khai |
| Cấu hình hệ thống | Phạm vi địa lý, danh mục, từ khóa tìm kiếm | Bounding box TP.HCM: lat ∈ [10.65, 10.90], lng ∈ [106.55, 106.85]; Danh mục: food, entertainment, culture, event |
| Người dùng | Thông tin về người dùng cuối | Vị trí GPS (lat=10.78, lng=106.70), locale="en", lịch sử tương tác, thời điểm truy cập |
| Đóng góp cộng đồng | Nội dung do người dùng submit | Tên quán mới, mô tả, toạ độ, ảnh, danh mục |

**Output của hệ thống:**

| Loại Output | Mô tả | Ví dụ cụ thể |
|-------------|--------|---------------|
| Feed cá nhân hoá | Danh sách nội dung đã xếp hạng theo ngữ cảnh người dùng | JSON: [{name: "Phở Hòa", cultural_context_en: "A legendary pho restaurant since 1968...", score: 0.92}, ...] |
| Chi tiết địa điểm | Thông tin đầy đủ bao gồm câu chuyện văn hoá song ngữ | Place object với description_vi, description_en, cultural_context_vi, cultural_context_en, toạ độ, ảnh, món đặc trưng |
| Trạng thái sự kiện | Sự kiện được cập nhật liên tục | Event với status: "upcoming"/"cancelled"/"completed", cập nhật mỗi 6 giờ |
| Kết quả kiểm duyệt | Phản hồi cho nội dung cộng đồng | Status: APPROVED / REJECTED (kèm lý do) / NEEDS_REVIEW |
| Dailylife | Nhật ký cho chuyến hoạt động | Hằng ngày sẽ viết lại nhật ký hành trình cho người dùng |

<!-- Agent Contracts -->



### 1.6 Giải pháp đề xuất — Multi-Agent System (MAS)

**VietVibe**, một hệ thống du lịch thông minh dựa trên kiến trúc **Multi-Agent System (MAS)**.

Lý do chọn kiến trúc MAS thay vì một ứng dụng truyền thống xuất phát từ bản chất đa dạng của bài toán. Mỗi vấn đề (P1–P4) đòi hỏi một loại xử lý hoàn toàn khác nhau: P1 và P3 cần khả năng thu thập dữ liệu tự động từ nhiều nguồn không đồng nhất (web scraping, REST API, HTML parsing), P2 cần trí tuệ nhân tạo để dịch thuật văn hoá (LLM), P4 cần cơ chế giám sát và cập nhật theo lịch trình. Một hệ thống truyền thống sẽ trộn lẫn tất cả các logic này trong cùng một codebase, khiến cho việc phát triển, kiểm thử, và bảo trì trở nên cực kỳ khó khăn. Với kiến trúc MAS, mỗi agent là một đơn vị tự chủ, có trách nhiệm riêng, giao tiếp qua interface chuẩn hoá — cho phép phát triển song song, thay thế hoặc nâng cấp agent mà không ảnh hưởng đến toàn hệ thống.

Giải pháp VietVibe cụ thể hoá thành bảy mục tiêu đo lường được:

| ID | Mục tiêu | Chỉ số thành công |
|----|----------|-------------------|
| G1 | Tự động thu thập POI và sự kiện từ nhiều nguồn | Tối thiểu 200 POI + 50 sự kiện cho TP.HCM trong MVP |
| G2 | Bổ sung kiến thức văn hoá bằng AI cho mỗi địa điểm/món ăn | ≥ 80% items có cultural context sau khi chạy pipeline |
| G3 | Dịch thuật văn hoá Vi ↔ En (vượt qua dịch từ) | Mỗi item có cả `description_vi` và `description_en` |
| G4 | Feed cá nhân hoá trên ứng dụng di động | Feed ranking dựa trên ≥ 4 yếu tố |
| G5 | Cập nhật sự kiện liên tục (tối thiểu mỗi 6 giờ) | Event Monitor Agent chạy tự động theo lịch |
| G6 | Cho phép cộng đồng đóng góp và duy trì dữ liệu | Luồng community submission với AI moderation |
| G7 | Vận hành với chi phí gần bằng không | Toàn bộ stack chạy trên free tier |

---

## 2. Decomposition 
### 2.1 Các hệ thống con

Giải pháp MAS đã xác định ở phần Problem Analysis bây giờ cần được phân rã thành các phần nhỏ hơn để triển khai. Bước đầu tiên trong quá trình phân rã là phân tích luồng dữ liệu end-to-end: dữ liệu du lịch đi từ nguồn bên ngoài (Google Maps, blog, Facebook), qua các giai đoạn xử lý trung gian, và cuối cùng đến tay người dùng dưới dạng feed trên ứng dụng di động. Khi vẽ ra luồng này, ta nhận thấy bốn giai đoạn có trách nhiệm hoàn toàn khác nhau, dẫn đến quyết định phân rã thành bốn hệ thống con.

```
VietVibe System
├── S1. Data Acquisition      — Thu thập dữ liệu thô từ nguồn ngoài
├── S2. Data Intelligence     — Xử lý, làm giàu, dịch thuật dữ liệu
├── S3. Content Delivery      — Phân phối nội dung đến người dùng
└── S4. User Platform         — Nền tảng tương tác người dùng
```

**S1 — Data Acquisition** (Thu thập dữ liệu) giải quyết trực tiếp P1 và P3. Input là cấu hình nguồn dữ liệu (danh sách sources, vùng địa lý, danh mục), Output là các bản ghi dữ liệu thô (raw data items) dạng JSON. Lý do cách ly hệ thống con này là bởi mỗi nguồn dữ liệu có protocol, rate limit, và cấu trúc hoàn toàn khác nhau — Google Maps dùng REST API có giới hạn $200/tháng free credit, Foody.vn cần web scraping HTML, Facebook Events dùng Graph API chỉ cho public events. Nếu gộp logic thu thập với logic xử lý, hệ thống sẽ phức tạp không cần thiết và một thay đổi nhỏ ở một nguồn có thể phá vỡ toàn bộ pipeline.

**S2 — Data Processing** (Xử lý dữ liệu) giải quyết P1, P2, và P4. Input là dữ liệu thô từ S1, Output là các dữ liệu sạch, đã được bổ sung ngữ cảnh văn hoá và dịch thuật vi-en. Đây là "bộ não" của hệ thống — nơi AI thực sự phát huy tác dụng. Việc tách S2 khỏi S1 tuân theo nguyên tắc Separation of Concerns: S1 chỉ lo "lấy" dữ liệu, S2 chỉ lo "hiểu" và "làm giàu" dữ liệu. Nếu bước enrichment thất bại (do rate limit LLM), dữ liệu thô vẫn an toàn và có thể retry sau.

**S3 — Content Delivery** (Phân phối nội dung) giải quyết nhu cầu cá nhân hoá — một yêu cầu vượt ra ngoài P1–P4 nhưng tạo nên giá trị cốt lõi của sản phẩm. Input là toàn bộ dữ liệu đã xử lý + ngữ cảnh người dùng (GPS, ngôn ngữ, lịch sử), Output là feed đã xếp hạng. Hệ thống con này cũng bao gồm kiểm duyệt nội dung cộng đồng.

**S4 — User Platform** (Nền tảng người dùng) là giao diện cuối cùng mà du khách tương tác, bao gồm ứng dụng di động và form đóng góp cộng đồng.

| Hệ thống con | Vấn đề giải quyết | Input | Output |
|---|---|---|---|
| S1. Data Acquisition | P1 (phân tán), P3 (ẩn) | Source configs, keywords, geo-bounds | Raw data items (JSON) |
| S2. Data Intelligence | P1 (thiếu sâu), P2 (rào cản VH), P4 (lỗi thời) | Raw data items | Clean, enriched, translated records |
| S3. Content Delivery | Cá nhân hoá + kiểm duyệt | All records + user context | Ranked feed, moderation results |
| S4. User Platform | P3 (community contribution) | User actions | UI responses, submissions |

### 2.2 Hệ thống Agent và Module

Mỗi hệ thống con ở tầng 1 tiếp tục được phân rã thành các agent hoặc module cụ thể. Quyết định phân rã ở tầng này dựa trên ba tiêu chí: sự khác biệt về tần suất hoạt động, sự khác biệt về cơ chế xử lý, và sự khác biệt về nguồn dữ liệu.

```
S1. Data Acquisition
├── Agent 1: Crawler Agent           — Scrape quán ăn, địa điểm từ Maps, blog, tin tức
└── Agent 2: Event Monitor Agent     — Theo dõi sự kiện từ Facebook, Ticketbox

S2. Data Intelligence
├── Agent 3: Cleaner Agent           — Validate, khử trùng lặp, chuẩn hoá
├── Agent 4: Cultural Enricher Agent — Bổ sung ngữ cảnh văn hoá bằng LLM
└── Agent 5: Translator Agent        — Dịch thuật văn hoá Vi → En

S3. Content Delivery
├── Agent 6: Feed Curator Agent      — Xếp hạng và cá nhân hoá feed
└── Agent 7: Community Moderator     — Kiểm duyệt nội dung cộng đồng

S4. User Platform
├── Module: Auth & Identity          — Xác thực, quản lý session
├── Module: Mobile App (Expo)        — Giao diện người dùng
└── Module: Community Submission     — Form đóng góp

Cross-cutting:
└── Orchestrator                     — Lên lịch, điều phối, xử lý lỗi
```

**Tại sao S1 chia thành hai agent?** Crawler Agent thu thập dữ liệu tĩnh (quán ăn, địa điểm văn hoá) — loại dữ liệu thay đổi chậm, chỉ cần crawl mỗi ngày một lần (2:00 AM). Trong khi đó, Event Monitor Agent theo dõi sự kiện — loại dữ liệu thay đổi nhanh (sự kiện có thể bị hủy, đổi giờ bất kỳ lúc nào), cần chạy mỗi 6 giờ. Gộp chung hai cơ chế scheduling khác nhau vào một agent sẽ vi phạm nguyên tắc Single Responsibility.

**Tại sao S2 chia thành ba agent thay vì một?** Cleaner Agent xử lý thuần logic (validate, normalize, dedup) — không cần gọi LLM, chạy nhanh, chi phí bằng không. Cultural Enricher Agent gọi Gemini Flash API để tạo văn hoá — bị giới hạn 15 request/phút trên free tier. Translator Agent cũng gọi Gemini Flash nhưng với prompt và mục đích khác. Tách riêng ba bước cho phép: (a) nếu enrichment fail, dữ liệu sạch vẫn được lưu; (b) LLM quota được quản lý rõ ràng giữa enrichment và translation; (c) mỗi bước có thể test độc lập.

**Tại sao cần Orchestrator?** Với 7 agent có mối quan hệ phụ thuộc phức tạp (Crawler phải chạy trước Cleaner, Cleaner trước Enricher, Enricher trước Translator), cần một thành phần trung tâm để quản lý thứ tự thực thi, retry khi thất bại, và ghi log. Orchestrator không xử lý dữ liệu trực tiếp — nó chỉ phối hợp.

### 2.3 Chi tiết từng Agent
<!-- Cách mình triển khai Agent ở đây, hạ tầng, về API, chi phí, dữ liệu -->

**Agent 1 — Crawler Agent** nhận cấu hình nguồn (danh sách sources, vùng địa lý HCMC, danh mục) và trả về dữ liệu thô lưu vào bảng `raw_crawl_data`. Mỗi nguồn được xử lý bởi một adapter riêng: `google_maps_adapter` dùng REST API (Nearby Search), `foody_adapter` scrape HTML bằng BeautifulSoup + httpx, `news_adapter` scrape RSS + HTML. Tất cả adapter đều chuẩn hoá output thành schema chung: `{source, source_id, raw_data, entity_type, crawled_at, status}`. Agent chạy mỗi ngày lúc 2:00 AM, retry tối đa 3 lần với exponential backoff, skip source nếu fail liên tục.

**Agent 2 — Event Monitor Agent** theo dõi sự kiện từ Facebook Events, Ticketbox, và trang venue. Điểm khác biệt chính so với Crawler Agent là cơ chế change detection: agent so sánh dữ liệu mới với bản ghi hiện tại trong database, chỉ cập nhật khi có thay đổi (diff-based update). Sự kiện đã qua `end_time` tự động chuyển status thành "completed". Sự kiện không còn xuất hiện trên nguồn chuyển thành "cancelled" (soft delete).

**Agent 3 — Cleaner Agent** nhận dữ liệu thô từ `raw_crawl_data` và xuất dữ liệu sạch vào ba bảng: `places`, `events`, `food_items`. Xử lý nội bộ gồm ba bước tuần tự: (1) Validate — tọa độ nằm trong bounding box HCMC (10.65° ≤ lat ≤ 10.90°, 106.55° ≤ lng ≤ 106.85°), tên không rỗng, danh mục hợp lệ; (2) Normalize — chuẩn hoá địa chỉ ("Q.1" → "Quận 1"), số điện thoại (+84...), tên (trim, capitalize); (3) Deduplicate — fuzzy matching kết hợp Levenshtein distance + geo-proximity < 50m.

**Agent 4 — Cultural Enricher Agent** nhận bản ghi sạch từ Agent 3 và bổ sung trường `cultural_context_vi` bằng Gemini 2.0 Flash. Agent chọn prompt template theo danh mục (food, entertainment, historical), gọi LLM, kiểm tra chất lượng (độ dài ≥ 50 ký tự, phát hiện hallucination markers), rồi lưu kết quả. Chỉ xử lý items chưa có cultural context (idempotent).

**Agent 5 — Translator Agent** nhận nội dung tiếng Việt + cultural context và tạo ra `description_en`, `cultural_context_en`. Không dùng dịch máy mà dùng prompt yêu cầu "cultural translation" — giải thích khái niệm Việt Nam cho du khách nước ngoài.

**Agent 6 — Feed Curator Agent** nhận ngữ cảnh người dùng (GPS, locale, lịch sử) và trả về feed đã xếp hạng. Công thức scoring có 6 yếu tố: recency (mới), proximity (gần), popularity (phổ biến), diversity (đa dạng), cultural_depth (chiều sâu VH), event_urgency (sự kiện sắp diễn ra). Trọng số thay đổi theo locale: du khách quốc tế nhận `cultural_depth` weight cao hơn (0.25 vs 0.10).

**Agent 7 — Community Moderator Agent** kiểm duyệt qua 3 giai đoạn: spam filter (rule-based, tức thì), duplicate check, và LLM quality assessment. Thiết kế 3 giai đoạn nhằm tiết kiệm API quota — phần lớn spam bị chặn ở giai đoạn 1 (miễn phí) trước khi cần gọi LLM.

### 2.4 Backend 
<!-- Trình bày về các công nghệ sử dụng, sẽ deploy lên đâu, database dùng ở đâu, database cần lưu những gì -->
<!-- Deploy: Lên Azure, Database: Firebase Google -->
#### 2.4.1 Công nghệ sử dụng
<!-- Cần trình bày sẽ dùng công nghệ nào cho API, làm sao để giao tiếp backend-->

#### 2.4.2 Deploy 
<!-- Làm sao để triển khai ra thực tế, sử dụng nền tảng nào? -->

### 2.5 Frontend
<!-- Sẽ triển khai ở nền tảng nào Website hay là Mobile. Lợi ích của việc triển khai Mobile so với Website -->
<!-- Công nghệ sẽ sử dụng và làm thế để có thể giao tiếp với backend -->