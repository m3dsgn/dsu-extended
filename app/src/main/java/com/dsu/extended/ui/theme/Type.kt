package com.dsu.extended.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dsu.extended.R

enum class AppFontPreset {
    SYSTEM_DEFAULT,
    GOOGLE_SANS_FLEX,
    ROBOTO_FLEX,
    MANROPE;

    val value: String
        get() = name.lowercase()

    companion object {
        fun fromPreference(value: String): AppFontPreset {
            return entries.firstOrNull { it.value == value } ?: SYSTEM_DEFAULT
        }
    }
}

val AppFontFamily = FontFamily.Default

private val ManropeFontFamily = FontFamily(
    Font(resId = R.font.manrope_wght, weight = FontWeight.Normal),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Medium),
    Font(resId = R.font.manrope_wght, weight = FontWeight.SemiBold),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Bold),
)

val MiuixFontFamily = ManropeFontFamily

private fun optionalDeviceFont(
    familyName: String,
    weight: FontWeight,
    style: FontStyle = FontStyle.Normal,
): Font {
    return Font(
        familyName = DeviceFontFamilyName(familyName),
        weight = weight,
        style = style,
    )
}

private val GoogleSansFlexFontFamily = FontFamily(
    optionalDeviceFont("google-sans-flex", FontWeight.Normal),
    optionalDeviceFont("google-sans-flex", FontWeight.Medium),
    optionalDeviceFont("google-sans-flex", FontWeight.SemiBold),
    optionalDeviceFont("google-sans-flex", FontWeight.Bold),
    optionalDeviceFont("google-sans", FontWeight.Normal),
    optionalDeviceFont("google-sans", FontWeight.Medium),
    optionalDeviceFont("google-sans", FontWeight.SemiBold),
    optionalDeviceFont("google-sans", FontWeight.Bold),
    optionalDeviceFont("google-sans-text", FontWeight.Normal),
    optionalDeviceFont("google-sans-text", FontWeight.Medium),
    optionalDeviceFont("roboto-flex", FontWeight.Normal),
    optionalDeviceFont("roboto-flex", FontWeight.Medium),
    optionalDeviceFont("roboto-flex", FontWeight.SemiBold),
    optionalDeviceFont("roboto-flex", FontWeight.Bold),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Normal),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Medium),
    Font(resId = R.font.manrope_wght, weight = FontWeight.SemiBold),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Bold),
)

private val RobotoFlexFontFamily = FontFamily(
    optionalDeviceFont("roboto-flex", FontWeight.Normal),
    optionalDeviceFont("roboto-flex", FontWeight.Medium),
    optionalDeviceFont("roboto-flex", FontWeight.SemiBold),
    optionalDeviceFont("roboto-flex", FontWeight.Bold),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Normal),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Medium),
    Font(resId = R.font.manrope_wght, weight = FontWeight.SemiBold),
    Font(resId = R.font.manrope_wght, weight = FontWeight.Bold),
)

fun resolveAppFontFamily(preset: AppFontPreset): FontFamily {
    return when (preset) {
        AppFontPreset.SYSTEM_DEFAULT -> FontFamily.Default
        AppFontPreset.GOOGLE_SANS_FLEX -> GoogleSansFlexFontFamily
        AppFontPreset.ROBOTO_FLEX -> RobotoFlexFontFamily
        AppFontPreset.MANROPE -> ManropeFontFamily
    }
}

fun createExpressiveTypography(fontFamily: FontFamily): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

fun createMiuixTypography(fontFamily: FontFamily): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.1.sp,
    ),
)

val Typography = createExpressiveTypography(AppFontFamily)
val MiuixTypography = createMiuixTypography(MiuixFontFamily)

@Immutable
data class DsuTextStyleSet(
    val cardTitle: TextStyle,
    val progressText: TextStyle,
    val statusText: TextStyle,
    val logText: TextStyle,
    val errorText: TextStyle,
    val suggestionText: TextStyle,
    val deviceInfoText: TextStyle,
    val buttonText: TextStyle,
)

fun createDsuTextStyles(fontFamily: FontFamily): DsuTextStyleSet {
    return DsuTextStyleSet(
        cardTitle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        progressText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
        ),
        statusText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        logText = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
        errorText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        suggestionText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.25.sp,
        ),
        deviceInfoText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        buttonText = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
    )
}

val LocalDsuTextStyles = staticCompositionLocalOf { createDsuTextStyles(AppFontFamily) }

object DSUTextStyles {
    val cardTitle: TextStyle
        @Composable get() = LocalDsuTextStyles.current.cardTitle

    val progressText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.progressText

    val statusText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.statusText

    val logText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.logText

    val errorText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.errorText

    val suggestionText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.suggestionText

    val deviceInfoText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.deviceInfoText

    val buttonText: TextStyle
        @Composable get() = LocalDsuTextStyles.current.buttonText
}
