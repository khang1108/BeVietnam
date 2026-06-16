package com.bevietnam.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dp.Hairline
    ) {
        Screen.bottomNavItems.forEach { item ->
            val selected = currentRouteClass == item.route::class
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item.route) },
                icon = {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    if (item.title != null) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}
