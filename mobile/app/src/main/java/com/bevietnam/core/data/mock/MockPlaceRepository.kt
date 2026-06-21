package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bản giả (mock) của [IPlaceRepository] dùng để chạy/demo UI khi backend chưa sẵn sàng.
 * Khi nối backend thật, đổi binding trong RepositoryModule sang PlaceRepository.
 */
@Singleton
class MockPlaceRepository @Inject constructor() : IPlaceRepository {

    private val places = listOf(
        Place(
            id = "1",
            name = "Văn Miếu - Quốc Tử Giám",
            category = "temple",
            description = "Trường đại học đầu tiên của Việt Nam, xây dựng năm 1070 thời Lý.",
            latitude = 21.0294,
            longitude = 105.8355,
            imageUrl = null,
            referenceUrl = null
        ),
        Place(
            id = "2",
            name = "Phố cổ Hội An",
            category = "district",
            description = "Đô thị cổ ven sông Thu Bồn, di sản văn hóa thế giới UNESCO.",
            latitude = 15.8801,
            longitude = 108.3380,
            imageUrl = null,
            referenceUrl = null
        ),
        Place(
            id = "3",
            name = "Hoàng thành Huế",
            category = "monument",
            description = "Kinh thành triều Nguyễn bên dòng sông Hương thơ mộng.",
            latitude = 16.4698,
            longitude = 107.5796,
            imageUrl = null,
            referenceUrl = null
        ),
        Place(
            id = "4",
            name = "Tràng An",
            category = "nature",
            description = "Quần thể hang động và núi đá vôi kỳ vĩ ở Ninh Bình.",
            latitude = 20.2506,
            longitude = 105.9133,
            imageUrl = null,
            referenceUrl = null
        )
    )

    override fun getPlaces(): Flow<List<Place>> = flow { emit(places) }

    override fun getPlaceDetail(id: String): Flow<Place?> = flow {
        emit(places.find { it.id == id })
    }
}
