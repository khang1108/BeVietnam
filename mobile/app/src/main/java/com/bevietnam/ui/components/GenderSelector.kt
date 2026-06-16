package com.bevietnam.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bevietnam.R
import com.bevietnam.core.model.Gender

/**
 * Bộ chọn giới tính tùy biến (Gender Selector) của ứng dụng BeVietnam.
 *
 * Sử dụng các thẻ Surface phân bổ đều theo hàng ngang để tạo vùng chọn lớn,
 * tích hợp RadioButton tĩnh bên trong và tối ưu hóa khả năng tiếp cận (Accessibility) bằng cách gom nhóm
 * sự kiện nhấn cho toàn bộ thẻ thay vì chia nhỏ các điểm chạm.
 *
 * @param selected Giới tính đang được lựa chọn hiện tại ([Gender]), có thể là null nếu chưa chọn.
 * @param onSelect Callback kích hoạt khi người dùng nhấn chọn một giới tính cụ thể.
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 */
@Composable
fun GenderSelector(
    selected: Gender?,
    onSelect: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (gender in Gender.entries) {
            val isSelected = gender == selected
            val genderText = if (gender == Gender.MALE) stringResource(R.string.gender_male)
                             else stringResource(R.string.gender_female)

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp) // Kích thước chiều cao chuẩn 48dp cho Touch Target
                    .clickable(
                        onClickLabel = "Chọn giới tính $genderText",
                        onClick = { onSelect(gender) }
                    ),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null, // Vô hiệu hóa click riêng của RadioButton để Surface bọc ngoài xử lý trọn vẹn (Accessibility)
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = genderText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
