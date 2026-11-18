package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.unit.DisplayUnitConversion
import com.researchbangla.mechanicalpronotes.domain.unit.UnitCategory
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConversionResult
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConverterEngine
import com.researchbangla.mechanicalpronotes.domain.unit.UnitPreferenceHelper
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Stage 3 Area D — 13-Category Mechanical Engineering Unit Converter Tests.
 */
class UnitConverterTest {

    private fun assertConvertedValue(
        value: Double,
        from: String,
        to: String,
        expected: Double,
        tolerance: Double = 1e-4
    ) {
        val res = UnitConverterEngine.convert(value, from, to)
        assertTrue("Expected Success for $value $from -> $to, got $res", res is UnitConversionResult.Success)
        val success = res as UnitConversionResult.Success
        assertTrue(
            "Expected $expected ± $tolerance for $value $from -> $to, got ${success.resultValue}",
            abs(success.resultValue - expected) <= tolerance
        )
    }

    @Test
    fun all13RequiredCategoriesExistAndContainAtLeastThreeUnits() {
        assertEquals(13, UnitCategory.entries.size)
        UnitCategory.entries.forEach { category ->
            val units = UnitConverterEngine.getUnits(category)
            assertTrue("Category ${category.name} should define >= 3 units", units.size >= 3)
        }
    }

    @Test
    fun linearAndPowerAndFlowRoundTripsAreAccurate() {
        assertConvertedValue(25.4, "mm", "in", 1.0, 1e-6)
        assertConvertedValue(1.0, "m2", "ft2", 10.7639104, 1e-4)
        assertConvertedValue(1.0, "m3", "L", 1000.0, 1e-6)
        assertConvertedValue(1.0, "ton_metric", "lb", 2204.6226, 1e-3)
        assertConvertedValue(100.0, "N", "kgf", 10.197162, 1e-4)
        assertConvertedValue(10.0, "kW", "hp_mech", 13.41022, 1e-4)
        assertConvertedValue(1.0, "kWh", "kJ", 3600.0, 1e-6)
        assertConvertedValue(10.0, "m3_h", "GPM_US", 44.028675, 1e-4)
        assertConvertedValue(1.0, "g_cm3", "kg_m3", 1000.0, 1e-6)
    }

    @Test
    fun gaugeAndAbsolutePressureConversionsHandleAtmosphericOffsetAndVacuumFloor() {
        // 0 bar(g) == 1.01325 bar(a)
        assertConvertedValue(0.0, "bar_g", "bar_a", 1.01325, 1e-6)
        assertConvertedValue(10.0, "bar_g", "psi_g", 145.0377, 1e-2)
        assertConvertedValue(-1.01325, "bar_g", "bar_a", 0.0, 1e-6)

        val belowVacuumGauge = UnitConverterEngine.convert(-1.1, "bar_g", "bar_a")
        assertTrue(belowVacuumGauge is UnitConversionResult.Error)

        val negativeAbs = UnitConverterEngine.convert(-0.1, "bar_a", "bar_g")
        assertTrue(negativeAbs is UnitConversionResult.Error)
    }

    @Test
    fun temperatureOffsetConversionsAndAbsoluteZeroLimitsAreEnforced() {
        assertConvertedValue(0.0, "C", "F", 32.0, 1e-6)
        assertConvertedValue(0.0, "C", "K", 273.15, 1e-6)
        assertConvertedValue(-40.0, "C", "F", -40.0, 1e-6)
        assertConvertedValue(100.0, "C", "R", 671.67, 1e-3)

        assertTrue(UnitConverterEngine.convert(-273.16, "C", "K") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convert(-0.01, "K", "C") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convert(-460.0, "F", "C") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convert(-1.0, "R", "K") is UnitConversionResult.Error)
    }

    @Test
    fun speedAndViscositySubgroupMismatchesAreRejectedWithPhysicalExplanation() {
        // Within-subgroup works
        assertConvertedValue(1500.0, "RPM", "Hz", 25.0, 1e-6)
        assertConvertedValue(30.0, "m_s", "km_h", 108.0, 1e-6)
        assertConvertedValue(1.0, "Pa_s", "cP", 1000.0, 1e-6)
        assertConvertedValue(100.0, "cSt", "m2_s", 1e-4, 1e-8)

        // Cross-subgroup blocked cleanly
        val linearToRpm = UnitConverterEngine.convert(10.0, "m_s", "RPM")
        assertTrue(linearToRpm is UnitConversionResult.Error)

        val dynToKin = UnitConverterEngine.convert(10.0, "cP", "cSt")
        assertTrue(dynToKin is UnitConversionResult.Error)
    }

    @Test
    fun crossCategoryNanInfinityAndBlankInputsAreRejectedCleanly() {
        assertTrue(UnitConverterEngine.convert(10.0, "m", "kg") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convert(Double.NaN, "m", "cm") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convert(Double.POSITIVE_INFINITY, "m", "cm") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convertFromText("   ", "m", "cm") is UnitConversionResult.Error)
        assertTrue(UnitConverterEngine.convertFromText("abc", "m", "cm") is UnitConversionResult.Error)
    }

    @Test
    fun engineeringFormatterPreservesSmallMagnitudePrecisionWithoutTruncatingToZero() {
        assertEquals("0.0001", UnitConverterEngine.formatEngineeringValue(1e-4))
        assertEquals("1e-5", UnitConverterEngine.formatEngineeringValue(1e-5))
        assertEquals("0.00089", UnitConverterEngine.formatEngineeringValue(0.00089))
        assertEquals("0", UnitConverterEngine.formatEngineeringValue(-0.0))
    }

    @Test
    fun unitPreferenceHelperConvertsVerifiedUnitsAndRetainsAmbiguousOrUnsupportedUnits() {
        // Verified Metric -> SI & Imperial conversions
        val tempSi = UnitPreferenceHelper.convertForDisplay(100.0, "°C", UnitSystem.SI) as DisplayUnitConversion.Converted
        assertEquals(373.15, tempSi.value, 1e-4)
        assertEquals("K", tempSi.unitSymbol)

        val tempImp = UnitPreferenceHelper.convertForDisplay(100.0, "°C", UnitSystem.IMPERIAL) as DisplayUnitConversion.Converted
        assertEquals(212.0, tempImp.value, 1e-4)
        assertEquals("°F", tempImp.unitSymbol)

        val pressImp = UnitPreferenceHelper.convertForDisplay(7.0, "bar(g)", UnitSystem.IMPERIAL) as DisplayUnitConversion.Converted
        assertEquals(101.5264, pressImp.value, 1e-2)
        assertEquals("psi(g)", pressImp.unitSymbol)

        val rpmSi = UnitPreferenceHelper.convertForDisplay(1500.0, "RPM", UnitSystem.SI) as DisplayUnitConversion.Converted
        assertEquals(157.0796, rpmSi.value, 1e-3)
        assertEquals("rad/s", rpmSi.unitSymbol)

        // Canonical metric units return null when UnitSystem.METRIC is active
        assertNull(UnitPreferenceHelper.convertForDisplay(7.0, "bar(g)", UnitSystem.METRIC))

        // Composite, dimensionless, and differential units must never be silently fabricated
        listOf("%", "ppm", "kg/h", "kJ/kg", "m3/kg", "mm WC", "bar", "Nm3/min", "ratio").forEach { unit ->
            val retained = UnitPreferenceHelper.convertForDisplay(10.0, unit, UnitSystem.IMPERIAL)
            assertTrue("Expected Retained for $unit, got $retained", retained is DisplayUnitConversion.Retained)
        }

        // Physical bounds still enforced
        assertTrue(UnitPreferenceHelper.convertForDisplay(-300.0, "°C", UnitSystem.SI) is DisplayUnitConversion.Retained)
        assertTrue(UnitPreferenceHelper.convertForDisplay(Double.NaN, "kW", UnitSystem.IMPERIAL) is DisplayUnitConversion.Retained)
    }
}
