package com.bevietnam.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Chỉ báo tải dữ liệu hình tròn (Loading Indicator) của ứng dụng BeVietnam.
 *
 * Sử dụng CircularProgressIndicator chuẩn của Material Design 3, tự động căn giữa trong container.
 * Lớp này được thiết kế linh hoạt, nhận [Modifier] từ bên ngoài truyền vào để quyết định kích thước hiển thị.
 *
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào (ví dụ: Modifier.fillMaxSize() để chiếm trọn màn hình).
 */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
