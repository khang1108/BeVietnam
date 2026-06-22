package com.bevietnam.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Nhãn lựa chọn danh mục bộ lọc (Category Chip) của ứng dụng BeVietnam.
 *
 * Cung cấp giao diện bộ lọc nhanh trực quan dưới dạng Chip, tự động thay đổi màu sắc, đường viền
 * dựa trên trạng thái được chọn hay không được chọn bởi người dùng.
 *
 * @param category Tên danh mục hiển thị trên Chip bộ lọc.
 * @param isSelected Trạng thái xác định nhãn này đang được chọn hay không.
 * @param onSelected Sự kiện click kích hoạt khi người dùng nhấn chọn nhãn danh mục.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        modifier = modifier.padding(horizontal = 4.dp)
    )
}
