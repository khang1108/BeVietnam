package com.bevietnam.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Calendar

/**
 * Ô chọn ngày tháng (Date Picker Field) được thiết kế đặc thù cho việc lựa chọn ngày sinh nhật.
 *
 * Sử dụng một ô nhập liệu chỉ đọc và phủ một lớp cảm ứng trong suốt lên trên để đảm bảo hứng trọn
 * sự kiện nhấn của người dùng, từ đó kích hoạt hộp thoại DatePickerDialog của hệ thống Android một cách an toàn và mượt mà.
 *
 * @param label Nhãn tiêu đề hiển thị phía trên ô chọn ngày.
 * @param displayValue Chuỗi ngày tháng định dạng hiển thị hiện tại (ví dụ: dd/MM/yyyy).
 * @param onDateSelected Callback kích hoạt khi người dùng lựa chọn thành công ngày tháng trên hộp thoại ([LocalDate]).
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 */
@Composable
fun DatePickerField(
    label: String,
    displayValue: String,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "dd/MM/yyyy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null, // Icon trang trí để null
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            // Lớp phủ cảm ứng trong suốt đảm bảo kích hoạt sự kiện nhấn 100% an toàn và chuẩn Accessibility
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        onClickLabel = "Mở hộp thoại chọn ngày sinh nhật",
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    onDateSelected(LocalDate.of(year, month + 1, day))
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    )
            )
        }
    }
}
