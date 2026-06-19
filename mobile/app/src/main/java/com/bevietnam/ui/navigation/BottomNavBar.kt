package com.bevietnam.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.reflect.KClass

/**
 * Thanh điều hướng dưới cùng (Bottom Navigation Bar) của ứng dụng BeVietnam.
 *
 * Tự động kết xuất các mục điều hướng (Items) được định nghĩa sẵn trong lớp [Screen],
 * hỗ trợ tự động đánh dấu (highlight) tab đang hoạt động dựa trên đối tượng KClass của Route hiện tại.
 *
 * @param modifier [Modifier] dùng để căn chỉnh, định hình kích thước layout bên ngoài truyền vào.
 * @param currentRouteClass Đối tượng [KClass] đại diện cho Route của màn hình đang hiển thị để xác định tab được chọn. Mặc định là [ProfileRoute].
 * @param onItemSelected Callback kích hoạt khi người dùng nhấn chọn một tab điều hướng mới, trả về Route object tương ứng.
 */
@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    currentRouteClass: KClass<*>? = ProfileRoute::class,
    onItemSelected: (Any) -> Unit = {}
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            .shadow(
                elevation = 24.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                clip = true
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) // Sử dụng màu nền của Theme (hỗ trợ Dark Mode)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(56.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Screen.bottomNavItems.forEach { item ->
            val selected = currentRouteClass == item.route::class
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null, // Loại bỏ hiệu ứng ripple để giống phong cách Threads/Instagram
                        onClick = { onItemSelected(item.route) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
