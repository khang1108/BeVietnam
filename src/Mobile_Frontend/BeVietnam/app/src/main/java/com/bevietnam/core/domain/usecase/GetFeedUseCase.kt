package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.FeedItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedUseCase @Inject constructor(
    private val repository: IFeedRepository
) {
    operator fun invoke(): Flow<List<FeedItem>> = repository.getFeedItems()
}
