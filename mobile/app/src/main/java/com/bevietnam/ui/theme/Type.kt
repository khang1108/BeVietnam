package com.bevietnam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.bevietnam.R

val provider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontPlusJakartaSans = GoogleFont("Plus Jakarta Sans")
val fontBeVietnamPro = GoogleFont("Be Vietnam Pro")

val fontFamilyPlusJakartaSans = FontFamily(
    Font(googleFont = fontPlusJakartaSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontPlusJakartaSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontPlusJakartaSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontPlusJakartaSans, fontProvider = provider, weight = FontWeight.Bold)
)

val fontFamilyBeVietnamPro = FontFamily(
    Font(googleFont = fontBeVietnamPro, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontBeVietnamPro, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontBeVietnamPro, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontBeVietnamPro, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = fontFamilyPlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamilyPlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamilyBeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamilyBeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamilyBeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.02.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamilyBeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
