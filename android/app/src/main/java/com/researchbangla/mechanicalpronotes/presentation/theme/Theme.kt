package com.researchbangla.mechanicalpronotes.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import com.researchbangla.mechanicalpronotes.domain.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = SteelBlueDarkPrimary,
    onPrimary = Color(0xFF003355),
    primaryContainer = SteelBlueDarkContainer,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SafetyAmberDarkWarning,
    onSecondary = Color(0xFF4A2800),
    background = CharcoalDarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = CharcoalDarkSurface,
    onSurface = Color(0xFFF7FAFC),
    surfaceVariant = CharcoalDarkSurfaceVar,
    onSurfaceVariant = Color(0xFFCBD5E0),
    outline = CharcoalOutline,
    error = SafetyRedDarkHazard,
    onError = Color(0xFF680003)
)

private val LightColorScheme = lightColorScheme(
    primary = SteelBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SteelBlueContainer,
    onPrimaryContainer = Color(0xFF001D36),
    secondary = SafetyAmberWarning,
    onSecondary = Color.White,
    background = IndustrialLightBg,
    onBackground = Color(0xFF1A202C),
    surface = IndustrialLightSurface,
    onSurface = Color(0xFF2D3748),
    surfaceVariant = IndustrialLightSurfaceVar,
    onSurfaceVariant = Color(0xFF4A5568),
    outline = IndustrialLightOutline,
    error = SafetyRedHazard,
    onError = Color.White
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

fun AppThemeMode.toThemeMode(): ThemeMode = when (this) {
    AppThemeMode.SYSTEM -> ThemeMode.SYSTEM
    AppThemeMode.LIGHT -> ThemeMode.LIGHT
    AppThemeMode.DARK -> ThemeMode.DARK
}

private fun TextStyle.scaled(factor: Float): TextStyle = copy(
    fontSize = if (fontSize.isSpecified) fontSize * factor else fontSize,
    lineHeight = if (lineHeight.isSpecified) lineHeight * factor else lineHeight
)

internal fun scaledTypography(base: Typography, factor: Float): Typography = if (factor == 1f) {
    base
} else {
    base.copy(
        displayLarge = base.displayLarge.scaled(factor),
        displayMedium = base.displayMedium.scaled(factor),
        displaySmall = base.displaySmall.scaled(factor),
        headlineLarge = base.headlineLarge.scaled(factor),
        headlineMedium = base.headlineMedium.scaled(factor),
        headlineSmall = base.headlineSmall.scaled(factor),
        titleLarge = base.titleLarge.scaled(factor),
        titleMedium = base.titleMedium.scaled(factor),
        titleSmall = base.titleSmall.scaled(factor),
        bodyLarge = base.bodyLarge.scaled(factor),
        bodyMedium = base.bodyMedium.scaled(factor),
        bodySmall = base.bodySmall.scaled(factor),
        labelLarge = base.labelLarge.scaled(factor),
        labelMedium = base.labelMedium.scaled(factor),
        labelSmall = base.labelSmall.scaled(factor)
    )
}

@Composable
fun MechanicalProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    highLegibility: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = scaledTypography(MechanicalTypography, if (highLegibility) 1.18f else 1f)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
