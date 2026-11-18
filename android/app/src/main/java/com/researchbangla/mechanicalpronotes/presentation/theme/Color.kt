package com.researchbangla.mechanicalpronotes.presentation.theme

import androidx.compose.ui.graphics.Color

// Industrial Material 3 Palette
// Designed for high-contrast visibility on industrial shop floors and boiler houses

// Dark Surface Palette (True OLED-friendly dark mode)
val CharcoalDarkBackground = Color(0xFF121418)
val CharcoalDarkSurface    = Color(0xFF1A1D24)
val CharcoalDarkSurfaceVar = Color(0xFF242832)
val CharcoalOutline        = Color(0xFF3E4452)

// Light Surface Palette
val IndustrialLightBg      = Color(0xFFF4F6F9)
val IndustrialLightSurface = Color(0xFFFFFFFF)
val IndustrialLightSurfaceVar = Color(0xFFE5E9F0)
val IndustrialLightOutline = Color(0xFFB0B7C3)

// Brand & Engineering Accent Colors
val SteelBluePrimary       = Color(0xFF2B6CB0)
val SteelBlueDarkPrimary   = Color(0xFF63B3ED)
val SteelBlueContainer     = Color(0xFFEBF8FF)
val SteelBlueDarkContainer = Color(0xFF2A4365)

val SafetyAmberWarning     = Color(0xFFDD6B20)
val SafetyAmberDarkWarning = Color(0xFFF6AD55)
val SafetyAmberContainer   = Color(0xFFFEEBC8)

val SafetyRedHazard        = Color(0xFFE53E3E)
val SafetyRedDarkHazard    = Color(0xFFFC8181)

val OperationalGreen       = Color(0xFF38A169)
val OperationalDarkGreen   = Color(0xFF68D391)
val OperationalGreenContainer = Color(0xFFC6F6D5)

// Content Depth Specific Colors
val FullBlueprintGreen     = Color(0xFF2E7D32)
val PartialBlueprintBlue   = Color(0xFF0288D1)
val InventoryOnlyGrey      = Color(0xFF757575)
val ResearchPendingOrange  = Color(0xFFED6C02)

// Neutral drafting canvas for vector schematics (keeps dark engineering linework crisp in Dark Mode)
val SchematicDraftingSurface = Color(0xFFF4F6F9)

data class SemanticBadgeColors(
    val container: Color,
    val content: Color
)

fun semanticPositiveColors(isDark: Boolean): SemanticBadgeColors = if (isDark) {
    SemanticBadgeColors(
        container = OperationalDarkGreen.copy(alpha = 0.18f),
        content = OperationalDarkGreen
    )
} else {
    SemanticBadgeColors(
        container = OperationalGreen.copy(alpha = 0.14f),
        content = FullBlueprintGreen
    )
}

fun semanticWarningColors(isDark: Boolean): SemanticBadgeColors = if (isDark) {
    SemanticBadgeColors(
        container = SafetyAmberDarkWarning.copy(alpha = 0.18f),
        content = SafetyAmberDarkWarning
    )
} else {
    SemanticBadgeColors(
        container = SafetyAmberWarning.copy(alpha = 0.14f),
        content = ResearchPendingOrange
    )
}

fun semanticDangerColors(isDark: Boolean): SemanticBadgeColors = if (isDark) {
    SemanticBadgeColors(
        container = SafetyRedDarkHazard.copy(alpha = 0.18f),
        content = SafetyRedDarkHazard
    )
} else {
    SemanticBadgeColors(
        container = SafetyRedHazard.copy(alpha = 0.14f),
        content = Color(0xFFC62828)
    )
}
