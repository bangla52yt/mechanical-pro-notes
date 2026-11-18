package com.researchbangla.mechanicalpronotes.domain.unit

/**
 * Robust Engineering Unit Conversion & Physical Boundary Validation Subsystem.
 *
 * Guiding Principles:
 * 1. Internal calculations ALWAYS use standard SI units (Pa, K, m3/s, W, J, kg).
 * 2. Absolute temperature (K, °C) is strictly distinguished from temperature difference (ΔT).
 * 3. Gauge pressure (bar(g), psi(g)) is strictly distinguished from absolute pressure (bar(a), psi(a)).
 * 4. Division-by-zero, non-finite (NaN / Infinity), and unphysical inputs are rejected.
 */
object UnitConverters {

    const val STANDARD_ATMOSPHERIC_PRESSURE_BAR = 1.01325
    const val ZERO_CELSIUS_IN_KELVIN = 273.15

    // --- PRESSURE CONVERSIONS ---
    fun barToPascal(bar: Double): Double = bar * 100_000.0
    fun pascalToBar(pa: Double): Double = pa / 100_000.0
    fun barToPsi(bar: Double): Double = bar * 14.50377
    fun psiToBar(psi: Double): Double = psi / 14.50377
    fun kgcm2ToBar(kgcm2: Double): Double = kgcm2 * 0.980665
    fun barToKgcm2(bar: Double): Double = bar / 0.980665

    /**
     * Converts gauge pressure in bar(g) to absolute pressure in bar(a).
     * P_abs = P_gauge + P_atm (default 1.01325 bar)
     */
    fun gaugeToAbsolutePressureBar(gaugeBar: Double, atmBar: Double = STANDARD_ATMOSPHERIC_PRESSURE_BAR): Double {
        require(gaugeBar >= -atmBar) { "Gauge pressure cannot be below absolute vacuum (-$atmBar bar)." }
        return gaugeBar + atmBar
    }

    /**
     * Converts absolute pressure in bar(a) to gauge pressure in bar(g).
     */
    fun absoluteToGaugePressureBar(absBar: Double, atmBar: Double = STANDARD_ATMOSPHERIC_PRESSURE_BAR): Double {
        require(absBar >= 0.0) { "Absolute pressure cannot be negative ($absBar bar)." }
        return absBar - atmBar
    }

    // --- TEMPERATURE CONVERSIONS ---
    /**
     * Absolute temperature conversion: Celsius to Kelvin.
     */
    fun celsiusToKelvin(celsius: Double): Double {
        require(celsius >= -ZERO_CELSIUS_IN_KELVIN) { "Temperature cannot be below absolute zero (-273.15 °C)." }
        return celsius + ZERO_CELSIUS_IN_KELVIN
    }

    fun kelvinToCelsius(kelvin: Double): Double {
        require(kelvin >= 0.0) { "Absolute temperature cannot be negative ($kelvin K)." }
        return kelvin - ZERO_CELSIUS_IN_KELVIN
    }

    fun celsiusToFahrenheit(celsius: Double): Double {
        require(celsius >= -ZERO_CELSIUS_IN_KELVIN) { "Temperature cannot be below absolute zero." }
        return (celsius * 9.0 / 5.0) + 32.0
    }

    fun fahrenheitToCelsius(fahrenheit: Double): Double {
        val c = (fahrenheit - 32.0) * 5.0 / 9.0
        require(c >= -ZERO_CELSIUS_IN_KELVIN) { "Temperature cannot be below absolute zero." }
        return c
    }

    /**
     * Temperature DIFFERENCE conversion: Δ°C to ΔK (identical step size).
     * Distinct from absolute temperature!
     */
    fun deltaCelsiusToDeltaKelvin(deltaC: Double): Double = deltaC
    fun deltaCelsiusToDeltaFahrenheit(deltaC: Double): Double = deltaC * 9.0 / 5.0

    // --- FLOW RATE CONVERSIONS ---
    fun m3hToLitersPerMinute(m3h: Double): Double = (m3h * 1000.0) / 60.0
    fun litersPerMinuteToM3h(lpm: Double): Double = (lpm * 60.0) / 1000.0
    fun m3hToUsGpm(m3h: Double): Double = m3h * 4.402868
    fun usGpmToM3h(gpm: Double): Double = gpm / 4.402868
    fun m3hToM3s(m3h: Double): Double = m3h / 3600.0

    // --- POWER & ENERGY CONVERSIONS ---
    fun kWToBhp(kw: Double): Double = kw * 1.341022
    fun bhpToKw(bhp: Double): Double = bhp / 1.341022
    fun kWToBoilerHorsepower(kw: Double): Double = kw / 9.8095
    fun boilerHorsepowerTokW(bhpBoiler: Double): Double = bhpBoiler * 9.8095
    fun kcalToKj(kcal: Double): Double = kcal * 4.1868
    fun kjToKcal(kj: Double): Double = kj / 4.1868

    // --- MASS & STEAM EVAPORATION ---
    fun kgHToTonsPerHour(kgH: Double): Double = kgH / 1000.0
    fun tonsPerHourToKgH(tph: Double): Double = tph * 1000.0

    // --- ROTATIONAL SPEED & FREQUENCY ---
    fun rpmToHertz(rpm: Double): Double = rpm / 60.0
    fun hertzToRpm(hz: Double): Double = hz * 60.0
    fun rpmToRadiansPerSecond(rpm: Double): Double = rpm * (2.0 * Math.PI) / 60.0

    // --- VALIDATION HELPER ---
    fun validatePositiveFinite(value: Double?, fieldName: String): Double {
        requireNotNull(value) { "$fieldName is required." }
        require(value.isFinite()) { "$fieldName must be a finite numerical value." }
        require(value > 0.0) { "$fieldName must be strictly positive ($value <= 0)." }
        return value
    }

    fun validateNonNegativeFinite(value: Double?, fieldName: String): Double {
        requireNotNull(value) { "$fieldName is required." }
        require(value.isFinite()) { "$fieldName must be a finite numerical value." }
        require(value >= 0.0) { "$fieldName cannot be negative ($value < 0)." }
        return value
    }
}
