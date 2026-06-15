package com.bevietnam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Lớp cấu trúc chứa các màu sắc ngữ nghĩa và đặc thù phi tiêu chuẩn của BeVietnam.
 */
data class CulturalColors(
    val easyColor: Color,
    val mediumColor: Color,
    val hardColor: Color,
    val completedGray: Color,
    val completionBlue: Color,
    val amberColor: Color,
    val goldColor: Color,
    val shimmerLight: Color,
    val shimmerDark: Color,
    val permissionGreenBg: Color,
    val permissionGreenText: Color,
    val permissionOrangeBg: Color,
    val permissionOrangeText: Color
)

/**
 * Local dùng để cung cấp và truy cập bảng màu đặc thù ở bất kỳ Composable con nào.
 */
val LocalCulturalColors = staticCompositionLocalOf {
    CulturalColors(
        easyColor = Color.Unspecified,
        mediumColor = Color.Unspecified,
        hardColor = Color.Unspecified,
        completedGray = Color.Unspecified,
        completionBlue = Color.Unspecified,
        amberColor = Color.Unspecified,
        goldColor = Color.Unspecified,
        shimmerLight = Color.Unspecified,
        shimmerDark = Color.Unspecified,
        permissionGreenBg = Color.Unspecified,
        permissionGreenText = Color.Unspecified,
        permissionOrangeBg = Color.Unspecified,
        permissionOrangeText = Color.Unspecified
    )
}

private val LightCulturalColors = CulturalColors(
    easyColor = EasyGreen,
    mediumColor = MediumOrange,
    hardColor = HardRed,
    completedGray = CompletedGray,
    completionBlue = CompletionBlue,
    amberColor = CulturalAmber,
    goldColor = CulturalGold,
    shimmerLight = Color(0xFFE0E0E0),
    shimmerDark = Color(0xFFC8C8C8),
    permissionGreenBg = PermissionGreenBg,
    permissionGreenText = PermissionGreenText,
    permissionOrangeBg = PermissionOrangeBg,
    permissionOrangeText = PermissionOrangeText
)

private val DarkCulturalColors = CulturalColors(
    easyColor = EasyGreen.copy(alpha = 0.8f),
    mediumColor = MediumOrange.copy(alpha = 0.8f),
    hardColor = HardRed.copy(alpha = 0.8f),
    completedGray = CompletedGray.copy(alpha = 0.6f),
    completionBlue = CompletionBlue.copy(alpha = 0.8f),
    amberColor = CulturalAmber.copy(alpha = 0.8f),
    goldColor = CulturalGold.copy(alpha = 0.8f),
    shimmerLight = Color(0xFF3A3A3A),
    shimmerDark = Color(0xFF222222),
    permissionGreenBg = PermissionGreenBg.copy(alpha = 0.2f),
    permissionGreenText = PermissionGreenText.copy(alpha = 0.9f),
    permissionOrangeBg = PermissionOrangeBg.copy(alpha = 0.2f),
    permissionOrangeText = PermissionOrangeText.copy(alpha = 0.9f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRedLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryRed,
    onPrimaryContainer = Color.White,
    secondary = PrimaryRedLight,
    onSecondary = Color.Black,
    background = Color(0xFF1E1B18), // Xám tối pha tông ấm đặc trưng Việt Nam
    onBackground = Color(0xFFF8EFE5),
    surface = Color(0xFF2A2624),
    onSurface = Color(0xFFF8EFE5),
    onSurfaceVariant = Color(0xFFDDC0BA)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PrimaryRedLight,
    onPrimaryContainer = Color.White,
    secondary = PrimaryRedLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3DFD5),
    onSecondaryContainer = Color(0xFF64635A),
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = Background, // Màu vàng cát giấy dó ấm áp
    onBackground = TextPrimary,
    surface = CardBackground, // Màu trắng cho Card nổi bật
    onSurface = TextPrimary,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = md_theme_light_outline,
    outlineVariant = Color(0xFFE0D5C5), // Màu viền nhẹ
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    scrim = md_theme_light_scrim
)

@Composable
fun BeVietnamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val culturalColors = if (darkTheme) DarkCulturalColors else LightCulturalColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalCulturalColors provides culturalColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
