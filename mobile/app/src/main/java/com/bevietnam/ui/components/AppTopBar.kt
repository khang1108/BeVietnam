package com.bevietnam.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bevietnam.R

/**
 * Thanh tiêu đề trên cùng toàn cục (App Bar) của ứng dụng BeVietnam.
 *
 * Cung cấp tiêu đề thương hiệu chính giữa, nút bấm chức năng tạo bài viết mới (nếu được kích hoạt),
 * và nút nhấn truy cập hồ sơ cá nhân qua ảnh đại diện (avatar) của người dùng ở góc phải.
 * Đã được tùy chỉnh lại chiều cao để gọn gàng hơn.
 *
 * @param avatarUrl Đường dẫn ảnh hoặc tài nguyên ảnh đại diện của người dùng ([Any]).
 * @param onAvatarClick Sự kiện click vào ảnh đại diện để truy cập hồ sơ cá nhân.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param onCreatePostClick Sự kiện click vào nút đăng bài viết mới. Mặc định là rỗng.
 * @param showCreatePost Xác định có hiển thị nút thêm bài viết mới hay không. Mặc định là `false`.
 */
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 4.dp,
            spotColor = Color.Black.copy(alpha = 0.15f)
        ),
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(56.dp) // Chiều cao gọn gàng chuẩn Material 2/3 (thu nhỏ so với 64dp mặc định)
                .padding(horizontal = 8.dp)
        ) {
            // Tiêu đề App và Logo (Chính xác ở giữa)
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.15f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )

            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.Center)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.smarttravellogo),
                    contentDescription = stringResource(R.string.logo_desc),
                    modifier = Modifier
                        .size(44.dp)
                        .padding(end = 6.dp)
                        .scale(scale)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = androidx.compose.ui.graphics.Color(0xFFC59B4B) // Vàng sẫm (vàng đồng)
                )
            }

        }
    }
}
