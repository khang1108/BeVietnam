package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ đăng ký tài khoản người dùng mới (Register).
 *
 * Lớp này che giấu chi tiết triển khai dữ liệu của tầng Data, đóng vai trò
 * làm cầu nối nghiệp vụ duy nhất cho tầng UI.
 *
 * @property authRepository Contract Repository quản lý đăng ký/xác thực ([IAuthRepository]).
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    /**
     * Kích hoạt nghiệp vụ đăng ký tài khoản mới dưới dạng luồng dữ liệu quan sát được bọc trong [Result].
     *
     * @param name Họ và tên đầy đủ của người dùng đăng ký.
     * @param gender Giới tính của người dùng ([Gender]).
     * @param dateOfBirth Chuỗi ngày sinh nhật định dạng dd/MM/yyyy.
     * @param email Địa chỉ email đăng ký.
     * @param password Mật khẩu tài khoản đăng ký.
     * @return Một [Flow] phát ra kết quả [Result] chứa đối tượng [User] vừa tạo mới, hoặc lỗi nếu đăng ký thất bại.
     */
    operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Flow<Result<User>> = authRepository.register(name, email, password)
}
