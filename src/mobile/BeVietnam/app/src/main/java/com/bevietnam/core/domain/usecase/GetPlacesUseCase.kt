package com.bevietnam.core.domain.usecase

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase xử lý nghiệp vụ lấy danh sách toàn bộ các địa danh du lịch văn hóa.
 *
 * Lớp này che giấu chi tiết triển khai dữ liệu của tầng Data, đóng vai trò
 * làm cầu nối nghiệp vụ duy nhất cho tầng UI.
 *
 * @property repository Contract Repository quản lý địa danh ([IPlaceRepository]).
 */
class GetPlacesUseCase @Inject constructor(
    private val repository: IPlaceRepository
) {
    /**
     * Kích hoạt nghiệp vụ lấy danh sách địa danh văn hóa dưới dạng luồng dữ liệu quan sát được.
     *
     * @return Một [Flow] phát ra danh sách các [Place].
     */
    operator fun invoke(): Flow<List<Place>> {
        return repository.getPlaces()
    }
}
