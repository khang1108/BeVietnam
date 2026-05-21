package com.bevietnam.core.data.repository

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// TODO(Backend): Inject BeVietnamApi and implement actual API calls
@Singleton
class PlaceRepository @Inject constructor(
    // private val api: BeVietnamApi
) : IPlaceRepository {

    override fun getPlaces(): Flow<List<Place>> {
        TODO("Not yet implemented: connect to GET /api/v1/places")
    }

    override fun getPlaceDetail(id: String): Flow<Place?> {
        TODO("Not yet implemented: connect to GET /api/v1/places/{id}")
    }
}
