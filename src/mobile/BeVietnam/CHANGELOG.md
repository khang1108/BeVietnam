# Cập nhật dự án BeVietnam

## 1. Sửa lỗi hệ thống và Package

- SessionManager.kt: Di chuyển về đúng package `com.bevietnam.core.domain.session`, cập nhật lại logic quản lý người dùng.
- AuthViewModel.kt và ProfileViewModel.kt: Cập nhật import để trỏ đúng về vị trí mới của SessionManager.
- RepositoryModule.kt: Cấu hình Dagger Hilt để bind IFeedRepository với MockFeedRepository.

## 2. Chuẩn hóa Tầng Cốt Lõi (Core Layer)

Hoàn thiện các Model, Interface và UseCase để đảm bảo ViewModel không gọi trực tiếp Repository.

- FeedItem.kt: Hoàn thiện model cho bảng tin.
- IFeedRepository.kt: Tạo mới interface repository.
- MockFeedRepository.kt: Thực thi dữ liệu giả cho repository.
- GetFeedUseCase.kt và GetTasksUseCase.kt: Tạo mới các UseCase tương ứng.
- ExploreViewModel và StorylineViewModel: Refactor để chuyển sang sử dụng UseCase thay vì gọi Repository trực tiếp.

## 3. Tối ưu Tầng Giao Diện (UI Layer)

Tách các thành phần giao diện nhỏ vào thư mục `com.bevietnam.ui.components` để tái sử dụng (Stateless Components).

- SearchBar.kt: Tách ra thành component dùng chung cho Explore và các màn hình tìm kiếm.
- PlaceCard.kt: Nâng cấp thiết kế và đưa vào thư mục dùng chung.
- FeedCard.kt: Tạo mới để hiển thị các bài đăng trên bảng tin.
- ErrorView.kt: Cập nhật thêm tính năng Thử lại (Retry).

## 4. Hoàn thiện các Màn hình và Previews

Cấu trúc lại logic theo luồng dữ liệu một chiều (UDF), thêm Preview cho các trạng thái Loading, Success, Empty.

- ExploreScreen.kt: Xóa component lặp lại, dùng component chung, thêm Preview.
- FeedScreen.kt: Chuyển từ trang trống thành màn hình bảng tin hoàn chỉnh với dữ liệu mẫu và Preview.
- ProfileScreen.kt: Sửa lỗi tính toán paddingValues, làm sạch mã nguồn, thêm Preview.
- AuthScreen.kt: Tách logic UI khỏi logic ViewModel để hỗ trợ Preview cho màn hình Đăng nhập/Đăng ký.
- StorylineScreen.kt: Cập nhật Preview cho các nhiệm vụ khám phá văn hóa.

## Kết quả

- Không còn lỗi biên dịch liên quan đến import và package.
- Kiến trúc đúng theo README (ViewModel -> UseCase -> Repository).
- Sẵn sàng chạy và có đầy đủ Preview trong Android Studio.
