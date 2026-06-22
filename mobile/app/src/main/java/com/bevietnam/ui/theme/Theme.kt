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

// Dark theme — imperial lacquer (web signature): gold on warm brown-black.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC69A3F),            // imperial gold
    onPrimary = Color(0xFF1A120B),          // lacquer ink on gold
    primaryContainer = Color(0xFFA98034),   // gold-600
    onPrimaryContainer = Color(0xFFF7EFDC),
    secondary = Color(0xFF9E2B25),          // son red seal
    onSecondary = Color(0xFFF7EFDC),
    secondaryContainer = Color(0xFF511613),
    onSecondaryContainer = Color(0xFFE9BDB9),
    tertiary = Color(0xFF3F7A5E),           // jade
    onTertiary = Color(0xFFF7EFDC),
    background = Color(0xFF1A120B),          // lacquer
    onBackground = Color(0xFFEFE6D2),
    surface = Color(0xFF241910),
    onSurface = Color(0xFFEFE6D2),
    surfaceVariant = Color(0xFF2F2016),
    onSurfaceVariant = Color(0xFFC9BBA2),
    outline = Color(0xFF4A3826),
    outlineVariant = Color(0xFF3A2C1C),
    error = Color(0xFFC5605A),
    onError = Color(0xFF1A120B)
)

// Light theme — warm rice-paper (web --paper): gold primary, ink text.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC69A3F),            // imperial gold
    onPrimary = Color(0xFF1A120B),          // ink on gold (gold is too light for white)
    primaryContainer = Color(0xFFEFE1BF),
    onPrimaryContainer = Color(0xFF443316),
    secondary = Color(0xFF9E2B25),          // son red seal
    onSecondary = Color(0xFFFAF5EA),
    secondaryContainer = Color(0xFFE9BDB9),
    onSecondaryContainer = Color(0xFF380F0D),
    tertiary = Color(0xFF3F7A5E),           // jade
    onTertiary = Color(0xFFFAF5EA),
    error = Color(0xFF9E2B25),
    errorContainer = Color(0xFFF6E3E1),
    onError = Color(0xFFFAF5EA),
    onErrorContainer = Color(0xFF380F0D),
    background = Color(0xFFFAF5EA),          // cream paper
    onBackground = Color(0xFF1F1810),        // ink
    surface = Color(0xFFFDFAF2),
    onSurface = Color(0xFF1F1810),
    surfaceVariant = Color(0xFFE6D9BF),
    onSurfaceVariant = Color(0xFF5A4D3A),
    outline = Color(0xFFD6C4A3),
    outlineVariant = Color(0xFFE3D6BD),
    surfaceTint = Color(0xFFC69A3F),
    scrim = Color(0xFF1A120B)
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
            window.statusBarColor = colorScheme.background.toArgb()
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
