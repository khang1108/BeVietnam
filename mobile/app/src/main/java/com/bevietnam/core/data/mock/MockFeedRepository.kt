package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.RecommendationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockFeedRepository @Inject constructor() : IFeedRepository {

    private val mockRecommendations = listOf(
        RecommendationItem(
            id = "rec-001",
            placeId = "place-hue-001",
            name = "Hoàng Thành Huế",
            category = "Lịch sử",
            thumbnailUrl = com.bevietnam.R.drawable.hoang_thanh_hue.toString(),
            score = 0.98f,
            explanation = "Dựa trên sở thích khám phá di tích lịch sử triều Nguyễn của bạn.",
            createdAt = Instant.now().toString()
        ),
        RecommendationItem(
            id = "rec-002",
            placeId = "place-hoian-001",
            name = "Phố cổ Hội An",
            category = "Văn hóa",
            thumbnailUrl = com.bevietnam.R.drawable.phoco_hoian.toString(),
            score = 0.95f,
            explanation = "85% người dùng từng đến Huế cũng đánh giá cao kiến trúc Hội An.",
            createdAt = Instant.now().toString()
        )
    )

    override fun getRecommendations(): Flow<List<RecommendationItem>> = flow {
        delay(1200)
        emit(mockRecommendations)
    }
}
