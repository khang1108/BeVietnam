package com.bevietnam.core.data.repository

import com.bevietnam.core.data.remote.api.BeVietnamApi
import com.bevietnam.core.data.remote.mapper.toPlace
import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val api: BeVietnamApi
) : IPlaceRepository {

    override fun getPlaces(): Flow<List<Place>> = flow {
        val response = api.getPlaces()
        val places = response.items.map { it.toPlace() }
        emit(places)
    }

    override fun getPlaceDetail(id: String): Flow<Place?> = flow {
        val response = api.getPlaces()
        val place = response.items.find { it.id.toString() == id }?.toPlace()
        emit(place)
    }
}
