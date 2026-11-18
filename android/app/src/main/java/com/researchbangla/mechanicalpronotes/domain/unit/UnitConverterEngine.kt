package com.researchbangla.mechanicalpronotes.domain.unit

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

/**
 * Pure-Kotlin 13-Category Mechanical Engineering Unit Converter Engine.
 *
 * Completely distinct from formula calculators (CALC-01..20) while preserving
 * existing [UnitConverters] boundary rules:
 * - Strict SI internal base conversion.
 * - Offset-aware Temperature conversions (°C, °F, K, °R) with absolute-zero rejection (< 0 K).
 * - Gauge vs Absolute pressure disambiguation with physical vacuum limit (>= -1.01325 bar(g)).
 * - Dynamic vs Kinematic viscosity subgroup separation (rejects cross-subgroup conversion
 *   because fluid density rho is not implied).
 * - Rejects NaN, +/-Infinity, cross-category units, blank/unknown codes, and unphysical values.
 * - Engineering precision formatter avoiding destructive `%.2f` truncation.
 */
enum class UnitCategory(
    val id: String,
    val displayName: String,
    val note: String? = null
) {
    LENGTH("LENGTH", "Length", "Standard linear & pipe/shaft dimensions"),
    AREA("AREA", "Area", "Cross-sectional flow, shell & orifice areas"),
    VOLUME("VOLUME", "Volume", "Tank, vessel, drum & receiver capacities"),
    MASS("MASS", "Mass", "Fuel, resin, steam mass & batch weights"),
    FORCE("FORCE", "Force", "Mechanical load, actuator thrust & torque arms"),
    PRESSURE(
        "PRESSURE",
        "Pressure",
        "Gauge (g) uses P_atm = 1.01325 bar(a); physical absolute vacuum >= 0 bar(a)"
    ),
    TEMPERATURE(
        "TEMPERATURE",
        "Temperature",
        "Absolute scale points (°C, °F, K, °R) >= 0 K. Use ΔT separately for intervals."
    ),
    POWER("POWER", "Power", "Mechanical, shaft, electric & thermal duty"),
    ENERGY("ENERGY", "Energy", "Heat content, enthalpy load & mechanical work"),
    FLOW_RATE(
        "FLOW_RATE",
        "Flow Rate",
        "Strictly volumetric flow (mass flow m³/kg requires fluid density)"
    ),
    DENSITY("DENSITY", "Density", "Fluid, steam & bulk material density"),
    SPEED("SPEED", "Speed", "Linear velocity (piping/belts) & rotational RPM/rad·s⁻¹"),
    VISCOSITY(
        "VISCOSITY",
        "Viscosity",
        "Dynamic (Pa·s, cP) and Kinematic (m²/s, cSt) are separate physical dimensions without fluid density"
    )
}

enum class UnitSubgroup {
    STANDARD,
    PRESSURE_ABSOLUTE,
    PRESSURE_GAUGE,
    TEMPERATURE_ABSOLUTE,
    SPEED_LINEAR,
    SPEED_ROTATIONAL,
    VISCOSITY_DYNAMIC,
    VISCOSITY_KINEMATIC
}

data class EngineeringUnit(
    val code: String,
    val symbol: String,
    val name: String,
    val category: UnitCategory,
    val toBaseFactor: Double = 1.0,
    val subgroup: UnitSubgroup = UnitSubgroup.STANDARD,
    val minPhysicalValue: Double? = null
)

sealed class UnitConversionResult {
    data class Success(
        val inputValue: Double,
        val fromUnit: EngineeringUnit,
        val toUnit: EngineeringUnit,
        val resultValue: Double,
        val formattedResult: String,
        val advisoryNote: String? = null
    ) : UnitConversionResult()

    data class Error(
        val reason: String
    ) : UnitConversionResult()
}

object UnitConverterEngine {

    private const val P_ATM_PA = 101_325.0 // 1.01325 bar(a)
    private const val ZERO_C_K = 273.15
    private const val G_STD = 9.80665

    val unitsByCategory: Map<UnitCategory, List<EngineeringUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            EngineeringUnit("m", "m", "Meter", UnitCategory.LENGTH, 1.0),
            EngineeringUnit("mm", "mm", "Millimeter", UnitCategory.LENGTH, 1e-3),
            EngineeringUnit("cm", "cm", "Centimeter", UnitCategory.LENGTH, 1e-2),
            EngineeringUnit("km", "km", "Kilometer", UnitCategory.LENGTH, 1_000.0),
            EngineeringUnit("in", "in", "Inch", UnitCategory.LENGTH, 0.0254),
            EngineeringUnit("ft", "ft", "Foot", UnitCategory.LENGTH, 0.3048),
            EngineeringUnit("yd", "yd", "Yard", UnitCategory.LENGTH, 0.9144),
            EngineeringUnit("um", "µm", "Micrometer (Micron)", UnitCategory.LENGTH, 1e-6)
        ),
        UnitCategory.AREA to listOf(
            EngineeringUnit("m2", "m²", "Square Meter", UnitCategory.AREA, 1.0, minPhysicalValue = 0.0),
            EngineeringUnit("cm2", "cm²", "Square Centimeter", UnitCategory.AREA, 1e-4, minPhysicalValue = 0.0),
            EngineeringUnit("mm2", "mm²", "Square Millimeter", UnitCategory.AREA, 1e-6, minPhysicalValue = 0.0),
            EngineeringUnit("ft2", "ft²", "Square Foot", UnitCategory.AREA, 0.09290304, minPhysicalValue = 0.0),
            EngineeringUnit("in2", "in²", "Square Inch", UnitCategory.AREA, 0.00064516, minPhysicalValue = 0.0)
        ),
        UnitCategory.VOLUME to listOf(
            EngineeringUnit("m3", "m³", "Cubic Meter", UnitCategory.VOLUME, 1.0, minPhysicalValue = 0.0),
            EngineeringUnit("L", "L", "Liter", UnitCategory.VOLUME, 1e-3, minPhysicalValue = 0.0),
            EngineeringUnit("mL", "mL", "Milliliter / cm³", UnitCategory.VOLUME, 1e-6, minPhysicalValue = 0.0),
            EngineeringUnit("gal_us", "gal(US)", "US Gallon", UnitCategory.VOLUME, 0.003785411784, minPhysicalValue = 0.0),
            EngineeringUnit("gal_uk", "gal(UK)", "Imperial Gallon", UnitCategory.VOLUME, 0.00454609, minPhysicalValue = 0.0),
            EngineeringUnit("ft3", "ft³", "Cubic Foot", UnitCategory.VOLUME, 0.028316846592, minPhysicalValue = 0.0)
        ),
        UnitCategory.MASS to listOf(
            EngineeringUnit("kg", "kg", "Kilogram", UnitCategory.MASS, 1.0, minPhysicalValue = 0.0),
            EngineeringUnit("g", "g", "Gram", UnitCategory.MASS, 1e-3, minPhysicalValue = 0.0),
            EngineeringUnit("mg", "mg", "Milligram", UnitCategory.MASS, 1e-6, minPhysicalValue = 0.0),
            EngineeringUnit("ton_metric", "t", "Metric Tonne", UnitCategory.MASS, 1_000.0, minPhysicalValue = 0.0),
            EngineeringUnit("lb", "lb", "Pound-mass", UnitCategory.MASS, 0.45359237, minPhysicalValue = 0.0)
        ),
        UnitCategory.FORCE to listOf(
            EngineeringUnit("N", "N", "Newton", UnitCategory.FORCE, 1.0),
            EngineeringUnit("kN", "kN", "Kilonewton", UnitCategory.FORCE, 1_000.0),
            EngineeringUnit("kgf", "kgf", "Kilogram-force", UnitCategory.FORCE, G_STD),
            EngineeringUnit("lbf", "lbf", "Pound-force", UnitCategory.FORCE, 4.4482216152605)
        ),
        UnitCategory.PRESSURE to listOf(
            EngineeringUnit("Pa_a", "Pa(a)", "Pascal (Absolute)", UnitCategory.PRESSURE, 1.0, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0),
            EngineeringUnit("kPa_a", "kPa(a)", "Kilopascal (Absolute)", UnitCategory.PRESSURE, 1_000.0, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0),
            EngineeringUnit("MPa_a", "MPa(a)", "Megapascal (Absolute)", UnitCategory.PRESSURE, 1_000_000.0, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0),
            EngineeringUnit("bar_a", "bar(a)", "Bar (Absolute)", UnitCategory.PRESSURE, 100_000.0, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0),
            EngineeringUnit("bar_g", "bar(g)", "Bar (Gauge)", UnitCategory.PRESSURE, 100_000.0, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 100_000.0),
            EngineeringUnit("kPa_g", "kPa(g)", "Kilopascal (Gauge)", UnitCategory.PRESSURE, 1_000.0, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 1_000.0),
            EngineeringUnit("mbar", "mbar(g)", "Millibar (Gauge)", UnitCategory.PRESSURE, 100.0, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 100.0),
            EngineeringUnit("psi_a", "psi(a)", "PSI (Absolute)", UnitCategory.PRESSURE, 6_894.757293168361, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0),
            EngineeringUnit("psi_g", "psi(g)", "PSI (Gauge)", UnitCategory.PRESSURE, 6_894.757293168361, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 6_894.757293168361),
            EngineeringUnit("kgcm2_g", "kg/cm²(g)", "kgf/cm² (Gauge)", UnitCategory.PRESSURE, 98_066.5, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 98_066.5),
            EngineeringUnit("mmH2O", "mmH₂O", "mm Water Column (ΔP/Draft)", UnitCategory.PRESSURE, 9.80665, UnitSubgroup.PRESSURE_GAUGE, -P_ATM_PA / 9.80665),
            EngineeringUnit("atm", "atm", "Standard Atmosphere (abs)", UnitCategory.PRESSURE, P_ATM_PA, UnitSubgroup.PRESSURE_ABSOLUTE, 0.0)
        ),
        UnitCategory.TEMPERATURE to listOf(
            EngineeringUnit("C", "°C", "Celsius", UnitCategory.TEMPERATURE, 1.0, UnitSubgroup.TEMPERATURE_ABSOLUTE, -ZERO_C_K),
            EngineeringUnit("F", "°F", "Fahrenheit", UnitCategory.TEMPERATURE, 1.0, UnitSubgroup.TEMPERATURE_ABSOLUTE, -459.67),
            EngineeringUnit("K", "K", "Kelvin", UnitCategory.TEMPERATURE, 1.0, UnitSubgroup.TEMPERATURE_ABSOLUTE, 0.0),
            EngineeringUnit("R", "°R", "Rankine", UnitCategory.TEMPERATURE, 1.0, UnitSubgroup.TEMPERATURE_ABSOLUTE, 0.0)
        ),
        UnitCategory.POWER to listOf(
            EngineeringUnit("W", "W", "Watt", UnitCategory.POWER, 1.0),
            EngineeringUnit("kW", "kW", "Kilowatt", UnitCategory.POWER, 1_000.0),
            EngineeringUnit("MW", "MW", "Megawatt", UnitCategory.POWER, 1_000_000.0),
            EngineeringUnit("hp_mech", "hp", "Mechanical Horsepower (550 ft·lbf/s)", UnitCategory.POWER, 745.6998715822702),
            EngineeringUnit("hp_metric", "PS", "Metric Horsepower (CV/PS)", UnitCategory.POWER, 735.49875),
            EngineeringUnit("kcal_h", "kcal/h", "Kilocalorie per hour", UnitCategory.POWER, 4186.8 / 3600.0),
            EngineeringUnit("BTU_h", "BTU/h", "BTU per hour", UnitCategory.POWER, 1055.05585262 / 3600.0),
            EngineeringUnit("TR", "TR", "Ton of Refrigeration (12,000 BTU/h)", UnitCategory.POWER, 3516.85284207)
        ),
        UnitCategory.ENERGY to listOf(
            EngineeringUnit("J", "J", "Joule", UnitCategory.ENERGY, 1.0),
            EngineeringUnit("kJ", "kJ", "Kilojoule", UnitCategory.ENERGY, 1_000.0),
            EngineeringUnit("MJ", "MJ", "Megajoule", UnitCategory.ENERGY, 1_000_000.0),
            EngineeringUnit("kWh", "kWh", "Kilowatt-hour", UnitCategory.ENERGY, 3_600_000.0),
            EngineeringUnit("kcal", "kcal", "Kilocalorie (IT)", UnitCategory.ENERGY, 4_186.8),
            EngineeringUnit("BTU", "BTU", "British Thermal Unit (IT)", UnitCategory.ENERGY, 1_055.05585262)
        ),
        UnitCategory.FLOW_RATE to listOf(
            EngineeringUnit("m3_s", "m³/s", "Cubic meter per second", UnitCategory.FLOW_RATE, 1.0, minPhysicalValue = 0.0),
            EngineeringUnit("m3_h", "m³/h", "Cubic meter per hour", UnitCategory.FLOW_RATE, 1.0 / 3600.0, minPhysicalValue = 0.0),
            EngineeringUnit("L_s", "L/s", "Liter per second", UnitCategory.FLOW_RATE, 1e-3, minPhysicalValue = 0.0),
            EngineeringUnit("L_min", "L/min", "Liter per minute", UnitCategory.FLOW_RATE, 1e-3 / 60.0, minPhysicalValue = 0.0),
            EngineeringUnit("GPM_US", "GPM(US)", "US Gallon per minute", UnitCategory.FLOW_RATE, 0.003785411784 / 60.0, minPhysicalValue = 0.0),
            EngineeringUnit("CFM", "CFM", "Cubic feet per minute", UnitCategory.FLOW_RATE, 0.028316846592 / 60.0, minPhysicalValue = 0.0)
        ),
        UnitCategory.DENSITY to listOf(
            EngineeringUnit("kg_m3", "kg/m³", "Kilogram per cubic meter", UnitCategory.DENSITY, 1.0, minPhysicalValue = 0.0),
            EngineeringUnit("g_cm3", "g/cm³", "Gram per cubic centimeter (kg/L)", UnitCategory.DENSITY, 1_000.0, minPhysicalValue = 0.0),
            EngineeringUnit("lb_ft3", "lb/ft³", "Pound per cubic foot", UnitCategory.DENSITY, 16.01846337396014, minPhysicalValue = 0.0)
        ),
        UnitCategory.SPEED to listOf(
            EngineeringUnit("m_s", "m/s", "Meter per second", UnitCategory.SPEED, 1.0, UnitSubgroup.SPEED_LINEAR),
            EngineeringUnit("m_min", "m/min", "Meter per minute", UnitCategory.SPEED, 1.0 / 60.0, UnitSubgroup.SPEED_LINEAR),
            EngineeringUnit("km_h", "km/h", "Kilometer per hour", UnitCategory.SPEED, 1_000.0 / 3600.0, UnitSubgroup.SPEED_LINEAR),
            EngineeringUnit("ft_s", "ft/s", "Foot per second", UnitCategory.SPEED, 0.3048, UnitSubgroup.SPEED_LINEAR),
            EngineeringUnit("ft_min", "ft/min", "Foot per minute (FPM)", UnitCategory.SPEED, 0.3048 / 60.0, UnitSubgroup.SPEED_LINEAR),
            EngineeringUnit("RPM", "RPM", "Revolutions per minute", UnitCategory.SPEED, 1.0, UnitSubgroup.SPEED_ROTATIONAL),
            EngineeringUnit("Hz", "Hz", "Revolutions per second (rps)", UnitCategory.SPEED, 60.0, UnitSubgroup.SPEED_ROTATIONAL),
            EngineeringUnit("rad_s", "rad/s", "Radians per second", UnitCategory.SPEED, 60.0 / (2.0 * Math.PI), UnitSubgroup.SPEED_ROTATIONAL)
        ),
        UnitCategory.VISCOSITY to listOf(
            EngineeringUnit("Pa_s", "Pa·s", "Pascal-second (Dynamic)", UnitCategory.VISCOSITY, 1.0, UnitSubgroup.VISCOSITY_DYNAMIC, 0.0),
            EngineeringUnit("cP", "cP", "Centipoise (mPa·s, Dynamic)", UnitCategory.VISCOSITY, 1e-3, UnitSubgroup.VISCOSITY_DYNAMIC, 0.0),
            EngineeringUnit("P", "P", "Poise (Dynamic)", UnitCategory.VISCOSITY, 0.1, UnitSubgroup.VISCOSITY_DYNAMIC, 0.0),
            EngineeringUnit("m2_s", "m²/s", "Square meter per second (Kinematic)", UnitCategory.VISCOSITY, 1.0, UnitSubgroup.VISCOSITY_KINEMATIC, 0.0),
            EngineeringUnit("cSt", "cSt", "Centistokes (mm²/s, Kinematic)", UnitCategory.VISCOSITY, 1e-6, UnitSubgroup.VISCOSITY_KINEMATIC, 0.0),
            EngineeringUnit("St", "St", "Stokes (Kinematic)", UnitCategory.VISCOSITY, 1e-4, UnitSubgroup.VISCOSITY_KINEMATIC, 0.0)
        )
    )

    private val allUnitsByCode: Map<String, EngineeringUnit> =
        unitsByCategory.values.flatten().associateBy { it.code }

    fun getUnits(category: UnitCategory): List<EngineeringUnit> =
        unitsByCategory[category] ?: emptyList()

    fun findUnit(code: String): EngineeringUnit? =
        allUnitsByCode[code.trim()]

    /**
     * Converts [value] from [fromCode] to [toCode], returning either [UnitConversionResult.Success]
     * or [UnitConversionResult.Error].
     */
    fun convert(value: Double, fromCode: String, toCode: String): UnitConversionResult {
        if (!value.isFinite()) {
            return UnitConversionResult.Error("Input value must be a finite real number (NaN/Infinity rejected).")
        }
        val fromUnit = findUnit(fromCode)
            ?: return UnitConversionResult.Error("Unsupported source unit '$fromCode'.")
        val toUnit = findUnit(toCode)
            ?: return UnitConversionResult.Error("Unsupported target unit '$toCode'.")

        if (fromUnit.category != toUnit.category) {
            return UnitConversionResult.Error(
                "Cross-category conversion rejected: ${fromUnit.symbol} (${fromUnit.category.displayName}) cannot convert directly to ${toUnit.symbol} (${toUnit.category.displayName})."
            )
        }

        // Subgroup isolation for Speed (linear vs rotational) and Viscosity (dynamic vs kinematic)
        if (fromUnit.category == UnitCategory.SPEED && fromUnit.subgroup != toUnit.subgroup) {
            return UnitConversionResult.Error(
                "Linear velocity (${fromUnit.symbol}) and rotational speed (${toUnit.symbol}) cannot convert without pulley/drum pitch radius r (v = omega * r)."
            )
        }
        if (fromUnit.category == UnitCategory.VISCOSITY && fromUnit.subgroup != toUnit.subgroup) {
            return UnitConversionResult.Error(
                "Dynamic viscosity (${fromUnit.symbol}) and kinematic viscosity (${toUnit.symbol}) require fluid density rho (nu = mu / rho) and cannot convert directly."
            )
        }

        // Strict non-negative / physical floor checks
        fromUnit.minPhysicalValue?.let { minVal ->
            if (value < minVal - 1e-9) {
                return UnitConversionResult.Error(
                    if (fromUnit.category == UnitCategory.TEMPERATURE) {
                        "Unphysical temperature: $value ${fromUnit.symbol} falls below absolute zero (0 K / -273.15 °C)."
                    } else if (fromUnit.category == UnitCategory.PRESSURE) {
                        "Unphysical pressure: $value ${fromUnit.symbol} falls below absolute vacuum (0 bar(a) / -1.01325 bar(g))."
                    } else {
                        "Negative physical magnitude rejected: $value ${fromUnit.symbol} cannot be less than ${formatEngineeringValue(minVal)}."
                    }
                )
            }
        }

        val converted = when (fromUnit.category) {
            UnitCategory.TEMPERATURE -> convertTemperature(value, fromUnit.code, toUnit.code)
            UnitCategory.PRESSURE -> convertPressure(value, fromUnit, toUnit)
            else -> (value * fromUnit.toBaseFactor) / toUnit.toBaseFactor
        }

        if (!converted.isFinite()) {
            return UnitConversionResult.Error("Numerical overflow during conversion.")
        }

        val advisory = when {
            fromUnit.category == UnitCategory.PRESSURE && fromUnit.subgroup != toUnit.subgroup ->
                "Gauge/Absolute reference uses standard atmospheric P_atm = 1.01325 bar(a)."
            fromUnit.category == UnitCategory.TEMPERATURE ->
                "Absolute scale conversion. For temperature differences (ΔT), Δ°C = ΔK and Δ°F = 1.8·Δ°C."
            else -> null
        }

        return UnitConversionResult.Success(
            inputValue = value,
            fromUnit = fromUnit,
            toUnit = toUnit,
            resultValue = converted,
            formattedResult = formatEngineeringValue(converted),
            advisoryNote = advisory
        )
    }

    /**
     * Parses raw text input cleanly and converts to [toCode]. Rejects blanks or non-numeric strings.
     */
    fun convertFromText(rawInput: String, fromCode: String, toCode: String): UnitConversionResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return UnitConversionResult.Error("Enter a numeric value to convert.")
        }
        val parsed = trimmed.toDoubleOrNull()
            ?: return UnitConversionResult.Error("Invalid numeric input '$trimmed'.")
        return convert(parsed, fromCode, toCode)
    }

    private fun convertTemperature(value: Double, fromCode: String, toCode: String): Double {
        if (fromCode == toCode) return value
        val kelvin = when (fromCode) {
            "K" -> value
            "C" -> value + ZERO_C_K
            "F" -> (value - 32.0) * 5.0 / 9.0 + ZERO_C_K
            "R" -> value * 5.0 / 9.0
            else -> error("Unknown temperature unit: $fromCode")
        }
        return when (toCode) {
            "K" -> kelvin
            "C" -> kelvin - ZERO_C_K
            "F" -> (kelvin - ZERO_C_K) * 9.0 / 5.0 + 32.0
            "R" -> kelvin * 9.0 / 5.0
            else -> error("Unknown temperature unit: $toCode")
        }
    }

    private fun convertPressure(
        value: Double,
        fromUnit: EngineeringUnit,
        toUnit: EngineeringUnit
    ): Double {
        if (fromUnit.code == toUnit.code) return value
        val absPascals = when (fromUnit.subgroup) {
            UnitSubgroup.PRESSURE_ABSOLUTE -> value * fromUnit.toBaseFactor
            UnitSubgroup.PRESSURE_GAUGE -> value * fromUnit.toBaseFactor + P_ATM_PA
            else -> error("Invalid pressure subgroup")
        }
        return when (toUnit.subgroup) {
            UnitSubgroup.PRESSURE_ABSOLUTE -> absPascals / toUnit.toBaseFactor
            UnitSubgroup.PRESSURE_GAUGE -> (absPascals - P_ATM_PA) / toUnit.toBaseFactor
            else -> error("Invalid pressure subgroup")
        }
    }

    /**
     * Formats engineering scalars with high significant-figure fidelity (never destructive %.2f):
     * - Clean zero normalization (-0.0 -> 0)
     * - Scientific notation for extreme magnitudes (< 1e-4 or >= 1e7)
     * - Up to 8 significant digits with trailing fraction zeroes stripped cleanly
     */
    fun formatEngineeringValue(value: Double, sigDigits: Int = 8): String {
        if (!value.isFinite()) return value.toString()
        if (abs(value) < 1e-12) return "0"

        val absVal = abs(value)
        if (absVal < 1e-4 || absVal >= 1e7) {
            return String.format(java.util.Locale.US, "%.6e", value)
                .replace(Regex("(\\.\\d*?[1-9])0+e"), "$1e")
                .replace(".0e", "e")
        }

        val magnitude = floor(log10(absVal)).toInt()
        val decimals = (sigDigits - 1 - magnitude).coerceIn(0, 10)
        val scale = 10.0.pow(decimals)
        val rounded = round(value * scale) / scale
        val fixed = String.format(java.util.Locale.US, "%.${decimals}f", rounded)
        return if (decimals > 0) {
            fixed.trimEnd('0').trimEnd('.')
        } else {
            fixed
        }
    }
}
