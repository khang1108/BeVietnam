package com.bevietnam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/**
 * Thanh tiêu đề trên cùng toàn cục (App Bar) của ứng dụng BeVietnam.
 *
 * Cung cấp tiêu đề thương hiệu chính giữa, nút bấm chức năng tạo bài viết mới (nếu được kích hoạt),
 * và nút nhấn truy cập hồ sơ cá nhân qua ảnh đại diện (avatar) của người dùng ở góc phải.
 *
 * @param avatarUrl Đường dẫn ảnh hoặc tài nguyên ảnh đại diện của người dùng ([Any]).
 * @param onAvatarClick Sự kiện click vào ảnh đại diện để truy cập hồ sơ cá nhân.
 * @param onCreatePostClick Sự kiện click vào nút đăng bài viết mới. Mặc định là rỗng.
 * @param showCreatePost Xác định có hiển thị nút thêm bài viết mới hay không. Mặc định là `false`.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    avatarUrl: Any?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCreatePostClick: () -> Unit = {},
    showCreatePost: Boolean = false
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (showCreatePost) {
                IconButton(onClick = onCreatePostClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Đăng bài viết mới",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "bevietnam",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            // Tối ưu hóa Touch Target lên tối thiểu 48x48dp để đảm bảo khả năng tiếp cận (Accessibility)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClickLabel = "Truy cập hồ sơ cá nhân",
                        onClick = onAvatarClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Ảnh đại diện người dùng",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
