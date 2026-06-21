package com.bevietnam.ui.content

/**
 * Nội dung văn hóa mẫu (UI-only) cho màn Khám phá: ẩm thực và câu chuyện.
 *
 * Đây là dữ liệu giả phục vụ demo. Khi có nguồn dữ liệu thật, thay [CultureContent] bằng
 * dữ liệu lấy từ repository/usecase. Dùng [colorHex] (Long) để tránh phụ thuộc Compose ở tầng dữ liệu.
 */
data class FoodPlace(
    val name: String,
    val address: String,
    val note: String
)

data class FoodItem(
    val id: String,
    val name: String,
    val region: String,
    val description: String,
    val colorHex: Long,
    val restaurants: List<FoodPlace>
)

data class CultureStory(
    val id: String,
    val title: String,
    val tag: String,
    val excerpt: String,
    val body: String,
    val colorHex: Long
)

object CultureContent {

    val foods = listOf(
        FoodItem(
            id = "pho",
            name = "Phở Hà Nội",
            region = "Hà Nội",
            description = "Phở là món ăn quốc hồn quốc túy của Việt Nam, gồm bánh phở, nước dùng ninh từ xương nhiều giờ, thịt bò/gà và rau thơm. Xuất hiện đầu thế kỷ 20 ở miền Bắc.",
            colorHex = 0xFFC9651F,
            restaurants = listOf(
                FoodPlace("Phở Bát Đàn", "49 Bát Đàn, Hoàn Kiếm, Hà Nội", "Phở bò gia truyền, tự phục vụ"),
                FoodPlace("Phở Thìn Lò Đúc", "13 Lò Đúc, Hai Bà Trưng, Hà Nội", "Phở bò tái lăn đặc trưng"),
                FoodPlace("Phở Gia Truyền", "Hàng Trống, Hoàn Kiếm, Hà Nội", "Nước dùng đậm đà, mở buổi sáng")
            )
        ),
        FoodItem(
            id = "banhmi",
            name = "Bánh mì",
            region = "Sài Gòn",
            description = "Bánh mì là sự giao thoa ẩm thực Pháp - Việt: vỏ giòn kẹp pa-tê, thịt nguội, rau dưa và nước sốt. Được nhiều tạp chí quốc tế bình chọn là món đường phố ngon nhất thế giới.",
            colorHex = 0xFFB79023,
            restaurants = listOf(
                FoodPlace("Bánh mì Huỳnh Hoa", "26 Lê Thị Riêng, Quận 1, TP.HCM", "Nhân đầy đặn, nổi tiếng nhất Sài Gòn"),
                FoodPlace("Bánh mì Bảy Hổ", "19 Huỳnh Khương Ninh, Quận 1", "Quán lâu đời hơn 80 năm"),
                FoodPlace("Bánh mì Hòa Mã", "53 Cao Thắng, Quận 3", "Bánh mì chảo kiểu xưa")
            )
        ),
        FoodItem(
            id = "caolau",
            name = "Cao lầu",
            region = "Hội An",
            description = "Cao lầu là món mì đặc sản phố cổ Hội An, sợi mì dai vàng làm từ nước giếng Bá Lễ và tro củi tràm, ăn kèm thịt xá xíu, rau sống và da heo chiên giòn.",
            colorHex = 0xFF6E7B3E,
            restaurants = listOf(
                FoodPlace("Cao lầu Thanh", "26 Thái Phiên, Hội An", "Quán cao lầu truyền thống"),
                FoodPlace("Cao lầu Bà Bé", "Trong chợ Hội An", "Giá bình dân, đông khách địa phương")
            )
        ),
        FoodItem(
            id = "bunbo",
            name = "Bún bò Huế",
            region = "Huế",
            description = "Bún bò Huế có nước dùng cay nồng từ sả và ruốc, sợi bún to, ăn kèm giò heo và chả cua. Là niềm tự hào ẩm thực của xứ Huế.",
            colorHex = 0xFFA6471F,
            restaurants = listOf(
                FoodPlace("Bún bò Bà Tuyết", "47 Nguyễn Công Trứ, Huế", "Nước dùng đậm đà chuẩn vị Huế"),
                FoodPlace("Bún bò Mệ Kéo", "20 Bạch Đằng, Huế", "Quán bình dân nổi tiếng")
            )
        )
    )

    val stories = listOf(
        CultureStory(
            id = "hoguom",
            title = "Sự tích Hồ Gươm",
            tag = "Truyền thuyết",
            excerpt = "Vua Lê Lợi trả gươm thần cho Rùa Vàng sau khi đánh tan giặc Minh...",
            body = "Vào thế kỷ 15, khi giặc Minh đô hộ nước ta, Lê Lợi dấy binh khởi nghĩa ở Lam Sơn. Đức Long Quân cho nghĩa quân mượn thanh gươm thần: lưỡi gươm do Lê Thận bắt được khi kéo lưới, còn chuôi gươm nạm ngọc Lê Lợi tìm thấy trên ngọn cây. Ghép lại vừa khít, trên lưỡi khắc hai chữ \"Thuận Thiên\".\n\nNhờ gươm thần, nghĩa quân Lam Sơn đánh đâu thắng đó, quét sạch giặc Minh ra khỏi bờ cõi. Một năm sau khi lên ngôi, vua Lê Lợi dạo thuyền trên hồ Tả Vọng thì Rùa Vàng nổi lên đòi lại gươm. Vua nâng gươm, Rùa Vàng há miệng đớp lấy rồi lặn xuống nước.\n\nTừ đó, hồ Tả Vọng được đổi tên thành Hồ Gươm (Hồ Hoàn Kiếm - nghĩa là \"trả gươm\"), trở thành biểu tượng linh thiêng giữa lòng Thủ đô Hà Nội.",
            colorHex = 0xFF6E7B3E
        ),
        CultureStory(
            id = "banhchung",
            title = "Sự tích Bánh chưng Bánh giầy",
            tag = "Truyền thuyết",
            excerpt = "Hoàng tử Lang Liêu dâng bánh chưng bánh giầy và được vua Hùng truyền ngôi...",
            body = "Vua Hùng thứ sáu muốn chọn người nối ngôi, bèn ra điều kiện: ai dâng được lễ vật ý nghĩa nhất sẽ được truyền ngôi. Các hoàng tử đua nhau tìm của ngon vật lạ, riêng Lang Liêu nhà nghèo được thần báo mộng dùng gạo nếp làm bánh.\n\nChàng làm bánh chưng vuông tượng trưng cho Đất, bánh giầy tròn tượng trưng cho Trời, nhân đậu thịt thể hiện công ơn cha mẹ và muôn loài. Vua Hùng nếm thấy ngon, cảm động trước ý nghĩa sâu sắc nên truyền ngôi cho Lang Liêu.\n\nTừ đó, bánh chưng bánh giầy trở thành món không thể thiếu trong ngày Tết cổ truyền, nhắc nhớ đạo lý uống nước nhớ nguồn.",
            colorHex = 0xFFA6471F
        )
    )

    fun food(id: String): FoodItem? = foods.find { it.id == id }
    fun story(id: String): CultureStory? = stories.find { it.id == id }
}
