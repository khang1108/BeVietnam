package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ lấy thông tin chi tiết của một người dùng dựa trên ID tài khoản.
 *
 * Lớp này che giấu chi tiết triển khai dữ liệu của tầng Data, đóng vai trò
 * làm cầu nối nghiệp vụ duy nhất cho tầng UI.
 *
 * @property userRepository Contract Repository quản lý tài khoản người dùng ([IUserRepository]).
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    /**
     * Kích hoạt nghiệp vụ lấy hồ sơ người dùng dưới dạng luồng dữ liệu quan sát được bọc trong [Result].
     *
     * @param id Định danh duy nhất của người dùng cần lấy thông tin.
     * @return Một [Flow] phát ra kết quả [Result] chứa đối tượng [User], hoặc lỗi ném ra nếu thất bại.
     */
    operator fun invoke(id: Int): Flow<Result<User>> {
        return userRepository.getUser(id)
    }
}
