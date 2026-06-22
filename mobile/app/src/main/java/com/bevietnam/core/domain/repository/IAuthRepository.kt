package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Gender
import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến xác thực và quản lý tài khoản người dùng.
 *
 * Đóng vai trò là cầu nối trừu tượng (abstraction layer) giúp tách biệt logic nghiệp vụ
 * của tầng Domain với các chi tiết triển khai công nghệ ở tầng Data.
 */
interface IAuthRepository {
    
    /**
     * Thực hiện yêu cầu đăng nhập hệ thống bằng email và mật khẩu.
     *
     * @param email Địa chỉ email của tài khoản người dùng đăng nhập.
     * @param password Mật khẩu của tài khoản người dùng đăng nhập.
     * @return Một [Flow] phát ra đối tượng [Result] chứa thông tin của người dùng ([User]) đăng nhập thành công,
     * hoặc chứa ngoại lệ lỗi nếu đăng nhập thất bại.
     */
    fun login(email: String, password: String): Flow<Result<User>>

    /**
     * Thực hiện yêu cầu đăng ký tài khoản người dùng mới trên hệ thống.
     *
     * @param name Họ và tên đầy đủ của người dùng đăng ký.
     * @param gender Giới tính lựa chọn của người dùng ([Gender]).
     * @param dateOfBirth Chuỗi đại diện cho ngày sinh nhật của người dùng (ví dụ định dạng: dd/MM/yyyy).
     * @param email Địa chỉ email đăng ký tài khoản mới.
     * @param password Mật khẩu đăng ký tài khoản mới.
     * @return Một [Flow] phát ra đối tượng [Result] chứa thông tin tài khoản người dùng ([User]) vừa được tạo,
     * hoặc chứa ngoại lệ lỗi nếu đăng ký thất bại.
     */
    fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Result<User>>
}
