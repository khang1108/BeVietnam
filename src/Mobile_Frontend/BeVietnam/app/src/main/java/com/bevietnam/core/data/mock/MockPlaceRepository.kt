package com.bevietnam.core.data.mock

import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPlaceRepository @Inject constructor() : IPlaceRepository {
    
    fun getMockPlaces(): List<Place> = listOf(
        Place(
            id = "1",
            name = "Hội An Phố Cổ",
            category = "Lãng mạn",
            description = "Phố cổ Hội An là một trong những điểm đến đẹp nhất Việt Nam với những ngôi nhà cổ kính, đèn lồng rực rỡ và không khí lãng mạn.",
            location = "Hội An, Quảng Nam",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Hoi_An_Covered_Bridge.jpg/1280px-Hoi_An_Covered_Bridge.jpg",
            rating = 4.9f,
            reviewCount = 2341
        ),
        Place(
            id = "2",
            name = "Chợ Bến Thành",
            category = "Sôi động",
            description = "Chợ Bến Thành là biểu tượng của Sài Gòn, nơi mua sắm sầm uất với đủ loại hàng hóa từ đặc sản địa phương đến thời trang.",
            location = "Quận 1, TP.HCM",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2d/Ben_Thanh_market_2009.jpg/1280px-Ben_Thanh_market_2009.jpg",
            rating = 4.7f,
            reviewCount = 1892
        ),
        Place(
            id = "3",
            name = "Tràng An, Ninh Bình",
            category = "Thiên nhiên",
            description = "Quần thể danh thắng Tràng An là Di sản Thế giới UNESCO với những dãy núi đá vôi hùng vĩ và hệ thống hang động kỳ bí.",
            location = "Ninh Bình",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Trang_An_Landscape_Complex.jpg/1280px-Trang_An_Landscape_Complex.jpg",
            rating = 4.8f,
            reviewCount = 3102
        ),
        Place(
            id = "4",
            name = "Vịnh Hạ Long",
            category = "Thiên nhiên",
            description = "Vịnh Hạ Long là kỳ quan thiên nhiên thế giới với hàng nghìn đảo đá vôi nhô lên từ mặt biển xanh thẳm.",
            location = "Quảng Ninh",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/Halong_bay.jpg/1280px-Halong_bay.jpg",
            rating = 4.9f,
            reviewCount = 5210
        ),
        Place(
            id = "5",
            name = "Phố đi bộ Hồ Gươm",
            category = "Văn hóa",
            description = "Khu phố đi bộ quanh Hồ Hoàn Kiếm là trái tim của Hà Nội, nơi diễn ra nhiều sự kiện văn hóa và là điểm hẹn quen thuộc của người dân.",
            location = "Hoàn Kiếm, Hà Nội",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Hoan_Kiem_Lake_Hanoi.jpg/1280px-Hoan_Kiem_Lake_Hanoi.jpg",
            rating = 4.6f,
            reviewCount = 4087
        ),
        Place(
            id = "6",
            name = "Bãi biển Mỹ Khê",
            category = "Nghỉ dưỡng",
            description = "Bãi biển Mỹ Khê được tạp chí Forbes bình chọn là một trong những bãi biển quyến rũ nhất hành tinh với cát trắng mịn và nước biển trong xanh.",
            location = "Đà Nẵng",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Da_Nang_beach.jpg/1280px-Da_Nang_beach.jpg",
            rating = 4.8f,
            reviewCount = 2756
        )
    )

    override fun getPlaces(): Flow<List<Place>> = flowOf(getMockPlaces())

    override fun getPlaceDetail(id: String): Flow<Place?> = flowOf(
        getMockPlaces().find { it.id == id }
    )
}
