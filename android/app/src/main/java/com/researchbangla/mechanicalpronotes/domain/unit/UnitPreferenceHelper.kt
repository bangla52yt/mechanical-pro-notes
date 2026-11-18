package com.researchbangla.mechanicalpronotes.domain.unit

import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationResultFormatter
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import java.util.Locale

sealed interface DisplayUnitConversion {
    data class Converted(
        val value: Double,
        val formattedValue: String,
        val unitSymbol: String,
        val note: String? = null
    ) : DisplayUnitConversion

    data class Retained(
        val reason: String
    ) : DisplayUnitConversion
}

/**
 * Maps verified [UnitSystem] preferences cleanly onto [UnitConverterEngine] without altering
 * calculation formulas, database values, or inventing unverified conversions.
 */
object UnitPreferenceHelper {

    fun preferredCategoryUnits(category: UnitCategory, unitSystem: UnitSystem): Pair<String, String> {
        return when (unitSystem) {
            UnitSystem.METRIC -> when (category) {
                UnitCategory.LENGTH -> "mm" to "in"
                UnitCategory.AREA -> "m2" to "ft2"
                UnitCategory.VOLUME -> "m3" to "ft3"
                UnitCategory.MASS -> "kg" to "lb"
                UnitCategory.FORCE -> "N" to "kgf"
                UnitCategory.PRESSURE -> "bar_g" to "psi_g"
                UnitCategory.TEMPERATURE -> "C" to "F"
                UnitCategory.POWER -> "kW" to "hp_mech"
                UnitCategory.ENERGY -> "kJ" to "BTU"
                UnitCategory.FLOW_RATE -> "m3_h" to "GPM_US"
                UnitCategory.DENSITY -> "kg_m3" to "lb_ft3"
                UnitCategory.SPEED -> "m_s" to "ft_s"
                UnitCategory.VISCOSITY -> "cP" to "Pa_s"
            }
            UnitSystem.SI -> when (category) {
                UnitCategory.LENGTH -> "m" to "mm"
                UnitCategory.AREA -> "m2" to "cm2"
                UnitCategory.VOLUME -> "m3" to "L"
                UnitCategory.MASS -> "kg" to "g"
                UnitCategory.FORCE -> "N" to "kN"
                UnitCategory.PRESSURE -> "Pa_a" to "bar_a"
                UnitCategory.TEMPERATURE -> "K" to "C"
                UnitCategory.POWER -> "W" to "kW"
                UnitCategory.ENERGY -> "J" to "kJ"
                UnitCategory.FLOW_RATE -> "m3_s" to "m3_h"
                UnitCategory.DENSITY -> "kg_m3" to "g_cm3"
                UnitCategory.SPEED -> "m_s" to "km_h"
                UnitCategory.VISCOSITY -> "Pa_s" to "cP"
            }
            UnitSystem.IMPERIAL -> when (category) {
                UnitCategory.LENGTH -> "in" to "mm"
                UnitCategory.AREA -> "ft2" to "m2"
                UnitCategory.VOLUME -> "gal_us" to "L"
                UnitCategory.MASS -> "lb" to "kg"
                UnitCategory.FORCE -> "lbf" to "N"
                UnitCategory.PRESSURE -> "psi_g" to "bar_g"
                UnitCategory.TEMPERATURE -> "F" to "C"
                UnitCategory.POWER -> "hp_mech" to "kW"
                UnitCategory.ENERGY -> "BTU" to "kJ"
                UnitCategory.FLOW_RATE -> "GPM_US" to "m3_h"
                UnitCategory.DENSITY -> "lb_ft3" to "kg_m3"
                UnitCategory.SPEED -> "ft_s" to "m_s"
                UnitCategory.VISCOSITY -> "cP" to "Pa_s"
            }
        }
    }

    /**
     * Converts a canonical plant-engineering display value only when [canonicalUnit] maps strictly
     * and unambiguously to an existing [UnitConverterEngine] unit without subgroup violation.
     *
     * Returns `null` when the value is already in the preferred unit for [targetSystem].
     * Returns [DisplayUnitConversion.Retained] when conversion is unsupported, composite,
     * dimensionless, or physically ambiguous (e.g. `mm WC` differential vs gauge, `kg/h`, `kJ/kg`, `%`).
     */
    fun convertForDisplay(
        value: Double,
        canonicalUnit: String,
        targetSystem: UnitSystem
    ): DisplayUnitConversion? {
        val trimmedUnit = canonicalUnit.trim()
        if (trimmedUnit.isEmpty()) return null
        if (!value.isFinite()) {
            return DisplayUnitConversion.Retained("Non-finite value retained in canonical $trimmedUnit.")
        }

        val normalized = trimmedUnit.lowercase(Locale.US)
            .replace("°", "")
            .replace("\\s+".toRegex(), " ")

        val (fromCode, targetCode) = when (normalized) {
            "c", "celsius" -> "C" to when (targetSystem) {
                UnitSystem.SI -> "K"
                UnitSystem.IMPERIAL -> "F"
                UnitSystem.METRIC -> "C"
            }
            "bar g", "bar(g)" -> "bar_g" to when (targetSystem) {
                UnitSystem.SI -> "kPa_a"
                UnitSystem.IMPERIAL -> "psi_g"
                UnitSystem.METRIC -> "bar_g"
            }
            "bar a", "bar(a)" -> "bar_a" to when (targetSystem) {
                UnitSystem.SI -> "kPa_a"
                UnitSystem.IMPERIAL -> "psi_a"
                UnitSystem.METRIC -> "bar_a"
            }
            "mm" -> "mm" to when (targetSystem) {
                UnitSystem.SI -> "m"
                UnitSystem.IMPERIAL -> "in"
                UnitSystem.METRIC -> "mm"
            }
            "cm" -> "cm" to when (targetSystem) {
                UnitSystem.SI -> "m"
                UnitSystem.IMPERIAL -> "in"
                UnitSystem.METRIC -> "cm"
            }
            "m" -> "m" to when (targetSystem) {
                UnitSystem.SI -> "m"
                UnitSystem.IMPERIAL -> "ft"
                UnitSystem.METRIC -> "m"
            }
            "mm2" -> "mm2" to when (targetSystem) {
                UnitSystem.SI -> "m2"
                UnitSystem.IMPERIAL -> "in2"
                UnitSystem.METRIC -> "mm2"
            }
            "l" -> "L" to when (targetSystem) {
                UnitSystem.SI -> "m3"
                UnitSystem.IMPERIAL -> "gal_us"
                UnitSystem.METRIC -> "L"
            }
            "m3", "m3 before regeneration" -> "m3" to when (targetSystem) {
                UnitSystem.SI -> "m3"
                UnitSystem.IMPERIAL -> "ft3"
                UnitSystem.METRIC -> "m3"
            }
            "kg" -> "kg" to when (targetSystem) {
                UnitSystem.SI -> "kg"
                UnitSystem.IMPERIAL -> "lb"
                UnitSystem.METRIC -> "kg"
            }
            "kw" -> "kW" to when (targetSystem) {
                UnitSystem.SI -> "W"
                UnitSystem.IMPERIAL -> "hp_mech"
                UnitSystem.METRIC -> "kW"
            }
            "kj" -> "kJ" to when (targetSystem) {
                UnitSystem.SI -> "MJ"
                UnitSystem.IMPERIAL -> "BTU"
                UnitSystem.METRIC -> "kJ"
            }
            "m3/h" -> "m3_h" to when (targetSystem) {
                UnitSystem.SI -> "m3_s"
                UnitSystem.IMPERIAL -> "GPM_US"
                UnitSystem.METRIC -> "m3_h"
            }
            "cfm" -> "CFM" to when (targetSystem) {
                UnitSystem.SI -> "m3_s"
                UnitSystem.METRIC -> "m3_h"
                UnitSystem.IMPERIAL -> "CFM"
            }
            "kg/m3" -> "kg_m3" to when (targetSystem) {
                UnitSystem.SI -> "kg_m3"
                UnitSystem.IMPERIAL -> "lb_ft3"
                UnitSystem.METRIC -> "kg_m3"
            }
            "m/s" -> "m_s" to when (targetSystem) {
                UnitSystem.SI -> "m_s"
                UnitSystem.IMPERIAL -> "ft_s"
                UnitSystem.METRIC -> "m_s"
            }
            "rpm" -> "RPM" to when (targetSystem) {
                UnitSystem.SI -> "rad_s"
                UnitSystem.METRIC, UnitSystem.IMPERIAL -> "RPM"
            }
            else -> {
                if (targetSystem == UnitSystem.METRIC) {
                    return null
                }
                return DisplayUnitConversion.Retained(
                    "Retained in canonical $trimmedUnit (${targetSystem.displayName} direct engine conversion unavailable)."
                )
            }
        }

        if (fromCode == targetCode) {
            return null
        }

        return when (val result = UnitConverterEngine.convert(value, fromCode, targetCode)) {
            is UnitConversionResult.Success -> {
                val formatted = CalculationResultFormatter.formatNumber(
                    value = result.resultValue,
                    unitHint = "${result.toUnit.name} ${result.toUnit.symbol}"
                )
                DisplayUnitConversion.Converted(
                    value = result.resultValue,
                    formattedValue = formatted,
                    unitSymbol = result.toUnit.symbol,
                    note = result.advisoryNote
                )
            }
            is UnitConversionResult.Error -> DisplayUnitConversion.Retained(result.reason)
        }
    }
}
