package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.RecommendationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [IFeedRepository] dùng để chạy/demo UI khi backend chưa sẵn sàng.
 * Khi nối backend thật, đổi binding trong RepositoryModule sang FeedRepository.
 */
@Singleton
class MockFeedRepository @Inject constructor() : IFeedRepository {

    override fun getRecommendations(): Flow<List<RecommendationItem>> = flow {
        emit(
            listOf(
                RecommendationItem(
                    id = "1",
                    placeId = "1",
                    name = "Văn Miếu - Quốc Tử Giám",
                    category = "temple",
                    thumbnailUrl = null,
                    score = 0.95f,
                    explanation = "Phù hợp với sở thích văn hóa - lịch sử của bạn.",
                    createdAt = "2024-01-01"
                ),
                RecommendationItem(
                    id = "2",
                    placeId = "2",
                    name = "Phố cổ Hội An",
                    category = "district",
                    thumbnailUrl = null,
                    score = 0.90f,
                    explanation = "Địa điểm được yêu thích bởi du khách quốc tế.",
                    createdAt = "2024-01-02"
                ),
                RecommendationItem(
                    id = "3",
                    placeId = "3",
                    name = "Hoàng thành Huế",
                    category = "monument",
                    thumbnailUrl = null,
                    score = 0.88f,
                    explanation = "Khám phá kiến trúc cung đình triều Nguyễn.",
                    createdAt = "2024-01-03"
                )
            )
        )
    }
}
