package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPlaceRepository @Inject constructor() : IPlaceRepository {

    // Dữ liệu giả (Mock Data) chuyên dụng để test UI mà không gọi API
    private val mockPlaces = listOf(
        Place(
            id = "place-hue-001",
            name = "Hoàng Thành Huế",
            category = "Lịch sử",
            imageUrl = com.bevietnam.R.drawable.hoang_thanh_hue.toString(),
            latitude = 16.4694,
            longitude = 107.5768,
            description = "Hoàng thành Huế (thuộc Quần thể di tích Cố đô Huế) là vòng tường thành thứ hai của Kinh thành Huế, có chức năng bảo vệ các cung điện quan trọng nhất của triều đình nhà Nguyễn và bảo vệ Tử Cấm thành. Hoàng thành và Tử Cấm thành thường được gọi chung là Đại Nội.",
            referenceUrl = "https://vi.wikipedia.org/wiki/Ho%C3%A0ng_th%C3%A0nh_Hu%E1%BA%BF"
        ),
        Place(
            id = "place-hoian-001",
            name = "Phố cổ Hội An",
            category = "Văn hóa",
            imageUrl = com.bevietnam.R.drawable.phoco_hoian.toString(),
            latitude = 15.8794,
            longitude = 108.3282,
            description = "Phố cổ Hội An là một đô thị cổ nằm ở hạ lưu sông Thu Bồn, thuộc vùng đồng bằng ven biển tỉnh Quảng Nam, Việt Nam. Nơi đây từng là một thương cảng quốc tế sầm uất, mang vẻ đẹp giao thoa kiến trúc đặc sắc giữa Việt Nam, Nhật Bản và Trung Hoa.",
            referenceUrl = "https://vi.wikipedia.org/wiki/Ph%E1%BB%91_c%E1%BB%95_H%E1%BB%99i_An"
        )
    )

    override fun getPlaces(): Flow<List<Place>> = flow {
        delay(1000)
        emit(mockPlaces)
    }

    override fun getPlaceDetail(id: String): Flow<Place?> = flow {
        delay(500)
        val place = mockPlaces.find { it.id == id }
        emit(place)
    }
}
