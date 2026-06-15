package com.bevietnam.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Thanh tìm kiếm tùy biến (Search Bar) của ứng dụng BeVietnam.
 *
 * Cung cấp ô nhập liệu bo tròn góc tích hợp kính lúp trang nhã, phục vụ việc tìm kiếm nhanh danh lam thắng cảnh,
 * ẩm thực, và bài viết lịch sử văn hóa trên các màn hình chính.
 *
 * @param query Văn bản/từ khóa tìm kiếm hiện tại trong ô.
 * @param onQueryChanged Callback kích hoạt khi người dùng thay đổi hoặc gõ từ khóa tìm kiếm mới.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param placeholder Văn bản gợi ý mờ hiển thị khi ô tìm kiếm còn trống. Mặc định là `"Tìm kiếm..."`.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tìm kiếm..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Biểu tượng tìm kiếm",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
