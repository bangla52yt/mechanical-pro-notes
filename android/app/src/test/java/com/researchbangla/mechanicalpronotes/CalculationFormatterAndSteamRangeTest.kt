package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationResultFormatter
import com.researchbangla.mechanicalpronotes.domain.steam.IapwsIf97SteamEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression tests for DEF-AUD-04 (CalculationResultFormatter) & DEF-AUD-05 (Steam gauge range hints).
 */
class CalculationFormatterAndSteamRangeTest {

    @Test
    fun formatterPreservesSmallPrecisionAndStripsTrailingZerosWithUsLocale() {
        assertEquals("0.0191 bar", CalculationResultFormatter.formatPrimaryValue(0.0191, "bar", "Pressure Drop"))
        assertEquals("0.0024 bar", CalculationResultFormatter.formatPrimaryValue(0.0024, "bar", "Pressure Drop"))
        assertEquals("0.000045", CalculationResultFormatter.formatNumber(0.000045))
        assertEquals("85 %", CalculationResultFormatter.formatPrimaryValue(85.0, "%", "Thermal Efficiency"))
        assertEquals("2,870.5 kg/h", CalculationResultFormatter.formatPrimaryValue(2870.5, "kg/h", "Relieving Capacity"))
        assertEquals("N/A", CalculationResultFormatter.formatNumber(Double.NaN))
    }

    @Test
    fun formatterConvertsCalc09StatusFlagsAndDnPipeSizes() {
        val adequate = CalculationResultFormatter.formatSecondaryEntry("Capacity Adequate for MCR? (1=Yes, 0=No)", 1.0)
        assertEquals("Capacity Adequate for MCR?", adequate.first)
        assertEquals("YES — Adequate for MCR", adequate.second)

        val insufficient = CalculationResultFormatter.formatSecondaryEntry("Capacity Adequate for MCR? (1=Yes, 0=No)", 0.0)
        assertEquals("NO — Below Boiler MCR", insufficient.second)

        val unknownMcr = CalculationResultFormatter.formatSecondaryEntry("Capacity Adequate for MCR? (1=Yes, 0=No)", -1.0)
        assertEquals("N/A (MCR not provided)", unknownMcr.second)

        val dn = CalculationResultFormatter.formatSecondaryEntry("Recommended Nominal Pipe Size (DN)", 100.0)
        assertEquals("DN 100", dn.second)
    }

    @Test
    fun steamEngineGaugeRangeHintsAndBoundariesMatchPhysicalLimits() {
        assertTrue(IapwsIf97SteamEngine.formatValidRangeHint(isGaugeMode = true).contains("-0.51 to 18.98 bar(g)"))
        assertTrue(IapwsIf97SteamEngine.formatValidRangeHint(isGaugeMode = false).contains("0.50 to 20.00 bar(a)"))

        val minGauge = IapwsIf97SteamEngine.getPropertiesByGaugePressure(-0.51)
        assertTrue(minGauge.pressureAbsoluteBar >= 0.50)

        val maxGauge = IapwsIf97SteamEngine.getPropertiesByGaugePressure(18.98)
        assertTrue(maxGauge.pressureAbsoluteBar <= 20.00)
    }

    @Test(expected = IllegalArgumentException::class)
    fun steamEngineRejectsGaugePressureBelowMinWithGaugeLimit() {
        try {
            IapwsIf97SteamEngine.getPropertiesByGaugePressure(-0.52)
        } catch (ex: IllegalArgumentException) {
            assertTrue(ex.message?.contains("-0.51") == true)
            throw ex
        }
    }
}
