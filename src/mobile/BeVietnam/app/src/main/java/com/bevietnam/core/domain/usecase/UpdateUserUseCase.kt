package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ cập nhật thông tin hồ sơ người dùng.
 *
 * Lớp này che giấu chi tiết triển khai dữ liệu của tầng Data, đóng vai trò
 * làm cầu nối nghiệp vụ duy nhất cho tầng UI.
 *
 * @property userRepository Contract Repository quản lý tài khoản người dùng ([IUserRepository]).
 */
class UpdateUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    /**
     * Kích hoạt nghiệp vụ cập nhật hồ sơ người dùng dưới dạng luồng dữ liệu quan sát được bọc trong [Result].
     *
     * @param user Đối tượng [User] chứa thông tin mới cần cập nhật.
     * @return Một [Flow] phát ra kết quả [Result] chứa Unit biểu thị cập nhật thành công, hoặc lỗi nếu thất bại.
     */
    operator fun invoke(user: User): Flow<Result<Unit>> {
        return userRepository.updateUser(user)
    }
}
