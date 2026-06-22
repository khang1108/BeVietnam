package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase chịu trách nhiệm lấy thông tin chi tiết của một địa danh (Place).
 *
 * Tách biệt logic nghiệp vụ khỏi UI (ViewModel) theo kiến trúc Clean Architecture.
 */
class GetPlaceDetailUseCase @Inject constructor(
    private val placeRepository: IPlaceRepository
) {
    operator fun invoke(id: String): Flow<Place?> {
        return placeRepository.getPlaceDetail(id)
    }
}
