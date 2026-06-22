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
// Serif display face to match the web's --font-display (Playfair supports Vietnamese; Cinzel does not).
val fontPlayfairDisplay = GoogleFont("Playfair Display")

val fontFamilyPlayfair = FontFamily(
    Font(googleFont = fontPlayfairDisplay, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontPlayfairDisplay, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontPlayfairDisplay, fontProvider = provider, weight = FontWeight.Bold)
)

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
    // Display + headline = Playfair Display serif (web serif headers).
    displayLarge = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),

    headlineLarge = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = fontFamilyPlayfair, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),

    // Titles = Be Vietnam Pro (sans body face).
    titleLarge = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = fontFamilyBeVietnamPro, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)
