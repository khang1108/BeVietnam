package com.bevietnam.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.bevietnam.R

/**
 * Component tạo hình nền họa tiết Trống Đồng Đông Sơn in chìm.
 * Lớp phủ này nằm dưới cùng và làm nền cho các nội dung giao diện khác.
 * 
 * @param modifier Modifier dùng để căn chỉnh bên ngoài
 * @param alpha Độ trong suốt của họa tiết (mặc định 0.05f để rất mờ ảo, không cản trở chữ)
 * @param content Khối giao diện chính nằm đè lên trên lớp họa tiết
 */
@Composable
fun CulturalBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.05f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Lớp Background Họa Tiết
        Image(
            painter = painterResource(id = R.drawable.dong_son_pattern),
            contentDescription = null,
            contentScale = ContentScale.Crop, // Phóng to phủ kín hoặc cắt bớt
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        )
        
        // Lớp Nội dung chính nằm đè lên trên
        content()
    }
}
