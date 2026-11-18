package com.researchbangla.mechanicalpronotes.domain.calculation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Formats engineering calculation values for plant-floor UI display without
 * altering underlying calculation formulas or numerical precision.
 */
object CalculationResultFormatter {

    private val usSymbols = DecimalFormatSymbols(Locale.US)

    fun isStatusFlagKey(label: String): Boolean {
        val lower = label.lowercase(Locale.US)
        return lower.contains("1=yes") || lower.contains("adequate for mcr")
    }

    fun formatPrimaryValue(value: Double, unit: String, outputName: String = ""): String {
        val formatted = formatNumber(value, unitHint = "$outputName $unit")
        return if (unit.isBlank()) formatted else "$formatted $unit"
    }

    fun formatSecondaryEntry(label: String, value: Double): Pair<String, String> {
        if (isStatusFlagKey(label)) {
            val cleanLabel = label.replace(Regex("\\s*\\(1=Yes[^)]*\\)"), "").trim()
            val statusText = when {
                !value.isFinite() -> "N/A"
                abs(value - 1.0) < 0.01 -> "YES — Adequate for MCR"
                abs(value) < 0.01 -> "NO — Below Boiler MCR"
                abs(value + 1.0) < 0.01 -> "N/A (MCR not provided)"
                else -> formatNumber(value, label)
            }
            return cleanLabel to statusText
        }

        if (label.contains("(DN)", ignoreCase = true) && value.isFinite()) {
            return label to "DN ${value.roundToInt()}"
        }

        return label to formatNumber(value, unitHint = label)
    }

    fun formatNumber(value: Double, unitHint: String = ""): String {
        if (!value.isFinite()) return "N/A"
        if (abs(value) < 1e-12) return "0"

        val absVal = abs(value)
        val lowerHint = unitHint.lowercase(Locale.US)
        val isHighPrecisionProperty =
            lowerHint.contains("m3/kg") ||
                lowerHint.contains("specific volume") ||
                lowerHint.contains("kg/m3") ||
                lowerHint.contains("density") ||
                lowerHint.contains("bar") ||
                lowerHint.contains("pressure drop") ||
                lowerHint.contains("drop per")

        val pattern = when {
            absVal < 0.0001 -> "0.######"
            absVal < 0.005 -> "0.#####"
            absVal < 0.1 -> "0.####"
            absVal < 10.0 && isHighPrecisionProperty -> "#,##0.####"
            absVal < 10.0 -> "#,##0.###"
            else -> "#,##0.##"
        }

        val formatter = DecimalFormat(pattern, usSymbols)
        val result = formatter.format(value)
        return if (result == "-0") "0" else result
    }
}
