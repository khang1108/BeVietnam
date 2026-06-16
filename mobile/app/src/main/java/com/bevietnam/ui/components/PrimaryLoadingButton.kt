package com.bevietnam.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Nút nhấn chính có tích hợp trạng thái tải dữ liệu (Primary Loading Button) của ứng dụng BeVietnam.
 *
 * Cung cấp nút bấm mang phong cách thương hiệu nổi bật, tự động hiển thị vòng tròn tải dữ liệu (CircularProgressIndicator)
 * và vô hiệu hóa tương tác tạm thời khi tác vụ đang xử lý (Loading), chống spam nút từ phía người dùng.
 *
 * @param text Chuỗi văn bản hiển thị trên nút nhấn khi ở trạng thái bình thường.
 * @param isLoading Trạng thái xác định nút có đang thực thi tác vụ tải dữ liệu hay không.
 * @param onClick Sự kiện click kích hoạt khi người dùng nhấn nút.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param enabled Trạng thái kích hoạt tương tác của nút. Mặc định là `true`.
 */
@Composable
fun PrimaryLoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp), // Định hình chiều cao tối thiểu tiêu chuẩn 52dp (đáp ứng Touch Target)
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = !isLoading && enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
