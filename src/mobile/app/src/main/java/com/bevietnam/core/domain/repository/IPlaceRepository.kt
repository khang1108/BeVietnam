package com.bevietnam.core.domain.repository

import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow

interface IPlaceRepository {
    fun getPlaces(): Flow<List<Place>>
    fun getPlaceDetail(id: String): Flow<Place?>
}
