package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến Địa danh du lịch văn hóa.
 *
 * Cung cấp giải pháp trừu tượng để truy vấn danh sách địa danh hấp dẫn cũng như thông tin
 * chi tiết của từng địa danh cụ thể trong hệ thống.
 */
interface IPlaceRepository {
    
    /**
     * Lấy danh sách toàn bộ các địa danh du lịch văn hóa có trong hệ thống ứng dụng.
     *
     * @return Một [Flow] phát ra danh sách các địa danh du lịch [Place].
     */
    fun getPlaces(): Flow<List<Place>>

    /**
     * Lấy thông tin chi tiết đầy đủ của một địa danh cụ thể dựa trên mã định danh duy nhất.
     *
     * @param id Mã định danh duy nhất của địa danh du lịch văn hóa cần truy vấn.
     * @return Một [Flow] phát ra đối tượng địa danh [Place] nếu tìm thấy, hoặc `null` nếu không tồn tại.
     */
    fun getPlaceDetail(id: String): Flow<Place?>
}
