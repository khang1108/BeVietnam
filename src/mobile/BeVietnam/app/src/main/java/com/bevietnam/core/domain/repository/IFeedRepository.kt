package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.FeedItem
import kotlinx.coroutines.flow.Flow

interface IFeedRepository {
    fun getFeedItems(): Flow<List<FeedItem>>
}
