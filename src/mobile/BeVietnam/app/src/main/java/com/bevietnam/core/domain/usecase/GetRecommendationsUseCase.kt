package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.RecommendationItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm thực thi nghiệp vụ lấy danh sách gợi ý địa điểm được xếp hạng (Recommendations).
 *
 * Lớp này hoạt động như một thành phần trung gian duy nhất chứa logic nghiệp vụ cốt lõi,
 * cô lập phần hiển thị ở UI với các chi tiết lấy dữ liệu ở tầng Data.
 * Hiện tại sử dụng mock data, sau này sẽ consume `GET /feed` response.
 *
 * @property repository Hợp đồng Repository của bảng tin ([IFeedRepository]) dùng để lấy dữ liệu gợi ý.
 */
class GetRecommendationsUseCase @Inject constructor(
    private val repository: IFeedRepository
) {

    /**
     * Kích hoạt nghiệp vụ lấy danh sách gợi ý địa điểm đã được xếp hạng.
     *
     * Phương thức sử dụng toán tử `invoke` để đối tượng UseCase có thể được gọi
     * trực tiếp như một hàm thông thường từ ViewModel.
     *
     * @return Một [Flow] phát ra danh sách các gợi ý địa điểm ([RecommendationItem]).
     */
    operator fun invoke(): Flow<List<RecommendationItem>> = repository.getRecommendations()
}
