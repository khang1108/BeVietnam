package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ đăng nhập tài khoản người dùng (Login).
 *
 * Lớp này che giấu chi tiết triển khai dữ liệu của tầng Data, đóng vai trò
 * làm cầu nối nghiệp vụ duy nhất cho tầng UI.
 *
 * @property authRepository Contract Repository quản lý đăng ký/xác thực ([IAuthRepository]).
 */
class LoginUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    /**
     * Kích hoạt nghiệp vụ đăng nhập tài khoản dưới dạng luồng dữ liệu quan sát được bọc trong [Result].
     *
     * @param email Địa chỉ email tài khoản người dùng đăng nhập.
     * @param password Mật khẩu tài khoản người dùng đăng nhập.
     * @return Một [Flow] phát ra kết quả [Result] chứa đối tượng [User], hoặc lỗi ném ra nếu xác thực thất bại.
     */
    operator fun invoke(email: String, password: String): Flow<Result<User>> {
        return authRepository.login(email, password)
    }
}
