package com.bevietnam.core.data.mock

import com.bevietnam.core.model.Place

object MockData {
    val places = listOf(
        Place(
            id = "1",
            name = "Hồ Hoàn Kiếm",
            category = "Di tích lịch sử",
            description = "Biểu tượng văn hóa của thủ đô Hà Nội với truyền thuyết trả gươm thần.",
            location = "Hoàn Kiếm, Hà Nội",
            imageUrl = "https://example.com/hanoi.jpg",
            rating = 4.8f,
            reviewCount = 1250
        ),
        Place(
            id = "2",
            name = "Phố Cổ Hội An",
            category = "Di sản văn hóa",
            description = "Thương cảng cổ sầm uất với những ngôi nhà vàng đặc trưng và đèn lồng lung linh.",
            location = "Hội An, Quảng Nam",
            imageUrl = "https://example.com/hoian.jpg",
            rating = 4.9f,
            reviewCount = 3400
        ),
        Place(
            id = "3",
            name = "Vịnh Hạ Long",
            category = "Kỳ quan thiên nhiên",
            description = "Di sản thiên nhiên thế giới với hàng ngàn đảo đá vôi kỳ vĩ trên làn nước xanh ngắt.",
            location = "Hạ Long, Quảng Ninh",
            imageUrl = "https://example.com/halong.jpg",
            rating = 4.7f,
            reviewCount = 5600
        )
    )
}
