package com.bevietnam.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Ô nhập liệu tùy chỉnh (Custom Text Field) mang phong cách thiết kế đặc trưng của BeVietnam.
 *
 * Tích hợp nhãn mô tả (Label), biểu tượng dẫn đầu (Leading Icon) và gợi ý nhập liệu (Placeholder).
 * Hỗ trợ các cơ chế biến đổi trực quan như ẩn mật khẩu khi nhập liệu.
 *
 * @param label Nhãn tiêu đề hiển thị phía trên ô nhập liệu.
 * @param value Giá trị chuỗi văn bản hiện tại trong ô nhập liệu.
 * @param onValueChange Callback kích hoạt khi người dùng thay đổi nội dung văn bản nhập vào.
 * @param placeholder Văn bản gợi ý mờ hiển thị khi ô nhập liệu còn trống.
 * @param leadingIcon Biểu tượng vectơ hiển thị ở góc trái của ô nhập liệu ([ImageVector]).
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param visualTransformation Quy tắc biến đổi hiển thị văn bản nhập liệu (ví dụ: ẩn mật khẩu). Mặc định là không biến đổi.
 */
@Composable
fun BeVietnamTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null, // Icon mang tính chất trang trí minh họa, để null cho TalkBack bỏ qua
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}
