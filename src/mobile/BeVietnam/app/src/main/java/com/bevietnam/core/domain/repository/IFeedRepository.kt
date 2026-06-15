package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.RecommendationItem
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các hợp đồng nghiệp vụ liên quan đến Bảng tin gợi ý (Recommendation Feed).
 *
 * Đóng vai trò là cầu nối trừu tượng giúp tầng Domain truy xuất danh sách gợi ý địa điểm
 * được xếp hạng dựa trên thuật toán recommendation của ứng dụng.
 */
interface IFeedRepository {

    /**
     * Lấy danh sách các gợi ý địa điểm đã được xếp hạng cho người dùng hiện tại.
     *
     * Hiện tại trả về mock data. Sau này sẽ consume `GET /feed` response từ backend.
     *
     * @return Một [Flow] phát ra danh sách chứa các gợi ý địa điểm ([RecommendationItem]).
     */
    fun getRecommendations(): Flow<List<RecommendationItem>>
}
