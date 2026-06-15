# BeVietnam Frontend

## 1. TỔNG QUAN KIẾN TRÚC (Architecture Overview)
Dự án sử dụng **Feature-based Clean Architecture** kết hợp với **MVVM & Unidirectional Data Flow (UDF)**. 
*   **Ngôn ngữ:** Kotlin
*   **UI Toolkit:** Jetpack Compose (100% Compose, không dùng XML layout).
*   **Dependency Injection:** Dagger Hilt.

---

## 2. CẤU TRÚC THƯ MỤC CHI TIẾT (Folder Structure)

Toàn bộ logic nằm trong `src/main/java/com/bevietnam/`. Dự án chia làm 2 tầng chính: **Core** (Lõi hệ thống) và **UI** (Giao diện).

### 2.1. Tầng Giao Diện: `ui/`
Nơi hiển thị và tương tác với người dùng. **Tuyệt đối không chứa logic xử lý dữ liệu tại đây.**

*   **`ui/theme/`**: Bộ luật thiết kế.
    *   *Nhiệm vụ:* Định nghĩa `Color`, `Type`, `Shape`,...
*   **`ui/components/`**: Các mảnh ghép UI dùng chung.
    *   *Nhiệm vụ:* Chứa các Composable được dùng từ 2 nơi trở lên (vd: `FeedCard.kt`, `LoadingIndicator.kt`).
    *   *Qui định:* Component ở đây phải là "Stateless" (không chứa ViewModel). Dữ liệu được truyền vào qua tham số (parameters).
*   **`ui/navigation/`**: Bản đồ ứng dụng.
    *   *Nhiệm vụ:* Quản lý điều hướng bằng `NavHost`. Định nghĩa các route trong `Screen.kt`.
*   **`ui/screens/`**: Nơi chứa các màn hình cụ thể, chia theo tính năng (Feature-based).
    *   Ví dụ các thư mục: `feed/`, `discovery/`, `storyline/`, `profile/`.
    *   *Cấu trúc 1 Feature:* Phải luôn có ít nhất 2 file. Ví dụ trong `storyline/` sẽ có:
        1.  `StorylineScreen.kt`: Nhận `State` để vẽ UI, gửi `Event` (click) cho ViewModel.
        2.  `StorylineViewModel.kt`: Quản lý logic, giữ trạng thái màn hình (`StateFlow`).

### 2.2. Tầng Cốt Lõi: `core/`
Trái tim của ứng dụng. Tầng này độc lập hoàn toàn, không biết Jetpack Compose hay UI là gì.

*   **`core/model/`**: Các khuôn đúc dữ liệu.
    *   *Nhiệm vụ:* Định nghĩa Data Class (`Place`, `Event`, `Storyline`,...).
    *   *Lưu ý:* Các trường văn hóa (`cultural_context`) phải được xử lý cẩn thận tại đây.
*   **`core/domain/`**: Nơi chứa Luật Nghiệp Vụ.
    *   `repository/`: Các Interface hợp đồng (vd: `IFeedRepository`, `IMetadataRepository`). Khai báo tính năng hệ thống có thể làm, không quan tâm lấy data từ đâu.
    *   `usecase/`: Các hành động đơn lẻ (vd: `GetFeedUseCase`, `CaptureMetadataUseCase`). 
    *   *Qui định:* ViewModel **bắt buộc** phải gọi UseCase. Tuyệt đối không để ViewModel gọi thẳng Repository.
*   **`core/data/`**: Nơi cung cấp dữ liệu.
    *   `remote/`: Gọi API Backend (Retrofit). Chứa DTO (Data Transfer Object) và Mapper.
    *   `mock/`: **RẤT QUAN TRỌNG**. Nơi chứa dữ liệu giả (`MockData.kt`, `MockFeedRepository.kt`). Dùng để làm UI khi API chưa sẵn sàng.
    *   `repository/`: Các class thực thi Interface từ `domain/`.
*   **`core/di/`**: Cấu hình Hilt. Nơi nối Repository với UseCase, kết nối Mock hay Remote.
*   **`core/util/`**: Hỗ trợ dùng chung (`Resource.kt` xử lý trạng thái Loading/Success/Error).

---

## 3. LUỒNG DỮ LIỆU ĐỊNH HƯỚNG (Data Flow)
Mọi tính năng phải tuân thủ luồng dữ liệu 1 chiều (Unidirectional Data Flow) sau:

1.  **UI (Screen)** gửi một hành động (ví dụ: `onClick`) tới **ViewModel**.
2.  **ViewModel** gọi một **UseCase** ở tầng `domain/`.
3.  **UseCase** yêu cầu dữ liệu từ **Repository** (thông qua Interface).
4.  **Repository** lấy dữ liệu từ `remote/` (API) hoặc `mock/` (Mock Data), sau đó dùng Mapper chuyển thành `core/model/`.
5.  **Repository** trả dữ liệu về cho **UseCase**, đẩy lên **ViewModel**.
6.  **ViewModel** cập nhật `StateFlow`.
7.  **UI (Screen)** tự động vẽ lại (recompose) dựa trên StateFlow mới nhất.

---

## 4. QUY TRÌNH THÊM TÍNH NĂNG MỚI
*(Ví dụ: Khi bạn được giao task làm màn hình `Storyline`)*

1.  **Tạo Data:** Vào `core/model/` tạo file `Storyline.kt`.
2.  **Làm Hợp đồng:** Vào `core/domain/repository/` tạo interface `IStorylineRepository.kt`.
3.  **Tạo Mock Data:** Vào `core/data/mock/` viết `MockStorylineRepository.kt` implements cái interface trên. Trả về list dữ liệu giả.
4.  **Bind DI:** Vào `core/di/RepositoryModule.kt` gắn `IStorylineRepository` với `MockStorylineRepository` để hệ thống tự nhận diện.
5.  **Tạo UseCase:** Vào `core/domain/usecase/` viết `GetStorylinesUseCase.kt`.
6.  **Xây UI:** Vào `ui/screens/` tạo folder `storyline/`. Viết `StorylineViewModel.kt` (gọi UseCase) và `StorylineScreen.kt`.
7.  **Mở đường:** Đăng ký route mới vào `ui/navigation/NavGraph.kt`.
