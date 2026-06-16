package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến việc quản lý thông tin Hồ sơ cá nhân (Profile).
 *
 * Cung cấp giải pháp trừu tượng giúp truy xuất thông tin chi tiết và cập nhật các thay đổi
 * về hồ sơ của người dùng trong hệ thống.
 */
interface IUserRepository {
    
    /**
     * Lấy thông tin tài khoản chi tiết của một người dùng cụ thể dựa trên định danh tài khoản.
     *
     * @param id Mã định danh duy nhất của người dùng cần truy vấn.
     * @return Một [Flow] phát ra đối tượng [Result] chứa dữ liệu của người dùng ([User]) nếu thành công,
     * hoặc chứa ngoại lệ lỗi nếu có sự cố xảy ra trong quá trình truy xuất.
     */
    fun getUser(id: String): Flow<Result<User>>

    /**
     * Cập nhật thông tin hồ sơ cá nhân của người dùng hiện tại lên hệ thống.
     *
     * @param user Đối tượng [User] chứa đầy đủ các thông tin mới nhất cần cập nhật.
     * @return Một [Flow] phát ra đối tượng [Result] chứa Unit biểu thị cập nhật thành công,
     * hoặc chứa ngoại lệ lỗi chi tiết nếu quá trình cập nhật gặp sự cố.
     */
    fun updateUser(user: User): Flow<Result<Unit>>
}
