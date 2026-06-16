package com.bevietnam.core.data.repository

import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.mapper.toRecommendationItem
import com.bevietnam.core.domain.repository.IFeedRepository
import com.bevietnam.core.model.RecommendationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val api: BeVietnamApi
) : IFeedRepository {
    override fun getRecommendations(): Flow<List<RecommendationItem>> = flow {
        val response = api.getFeed()
        val items = response.items.map { it.toRecommendationItem() }
        emit(items)
    }
}
