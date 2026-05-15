package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.FeedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class MockFeedRepository @Inject constructor() : IFeedRepository {
    override fun getFeedItems(): Flow<List<FeedItem>> = flow {
        delay(1000)
        emit(listOf(
            FeedItem(
                id = "1",
                userId = "u1",
                userName = "Fabian",
                userAvatarUrl = "https://i.pravatar.cc/150?u=fabian",
                content = "Here's our  whirlwind starter itinerary VN to kick off the Ha Long Bay dream - magic, memories, and a little bit of...",
                imageUrl = "https://images.unsplash.com/photo-1524230572899-a752b3835840",
                timestamp = "2 hours ago",
                likesCount = 1250,
                commentsCount = 45,
                location = "Hanoi, Vietnam",
                category = "Travel"
            ),
            FeedItem(
                id = "2",
                userId = "u2",
                userName = "Alex",
                userAvatarUrl = "https://i.pravatar.cc/150?u=alex",
                content = "Who do you think will win Euro 2025? ⚽",
                imageUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018",
                timestamp = "5 hours ago",
                likesCount = 890,
                commentsCount = 210,
                location = "Hoi An, Vietnam",
                category = "Football"
            ),
            FeedItem(
                id = "3",
                userId = "u3",
                userName = "Linh Nguyen",
                userAvatarUrl = "https://i.pravatar.cc/150?u=linh",
                content = "Tips: Đi bộ dọc phố cổ Hội An vào lúc 5 giờ chiều để ngắm nhìn sự chuyển giao giữa ánh nắng cuối ngày và ánh đèn lồng lung linh. Đừng quên thử món cao lầu tại quán lề đường nhé! ",
                imageUrl = "https://images.unsplash.com/photo-1583212231107-dc3966c31ae1",
                timestamp = "1 day ago",
                likesCount = 1200,
                commentsCount = 84,
                location = "Hội An, Quảng Nam",
                category = "Travel"
            )
        ))
    }
}
