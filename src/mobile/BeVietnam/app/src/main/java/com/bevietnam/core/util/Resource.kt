package com.bevietnam.core.util

/**
 * Lớp bọc trạng thái dữ liệu (Resource wrapper) đại diện cho các trạng thái của một tác vụ bất đồng bộ,
 * đặc biệt là tải dữ liệu từ API hoặc Local Database.
 *
 * @param T Kiểu dữ liệu của kết quả trả về khi tác vụ thành công.
 */
sealed class Resource<out T> {
    
    /**
     * Trạng thái thực thi tác vụ thành công và chứa dữ liệu kết quả.
     *
     * @property data Dữ liệu kết quả trả về của tác vụ.
     */
    data class Success<out T>(val data: T) : Resource<T>()
    
    /**
     * Trạng thái thực thi tác vụ gặp lỗi.
     *
     * @property message Thông điệp mô tả lỗi thân thiện với người dùng.
     * @property exception Ngoại lệ (Throwable) chi tiết nếu có để phục vụ debug.
     */
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    
    /**
     * Trạng thái tác vụ đang được thực thi (đang tải).
     */
    object Loading : Resource<Nothing>()
}
