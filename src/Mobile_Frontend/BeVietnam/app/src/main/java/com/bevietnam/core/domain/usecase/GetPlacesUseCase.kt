package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlacesUseCase @Inject constructor(
    private val repository: IPlaceRepository
) {
    operator fun invoke(): Flow<List<Place>> {
        return repository.getPlaces()
    }
}
