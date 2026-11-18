package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.steam.IapwsIf97SteamEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * JVM Unit Tests for IAPWS-IF97 Saturated Steam Engine (0.50 to 20.00 bar a).
 */
class SteamEngineTest {

    @Test
    fun gridAnchorsMatchExpectedSaturationPropertiesAndMonotonicity() {
        val samples = listOf(0.50, 0.75, 1.01325, 1.50, 2.00, 2.50, 3.00, 4.00, 5.00, 8.00, 10.00, 15.00, 20.00)
        var prevT = 0.0
        var prevHf = 0.0
        var prevVg = Double.MAX_VALUE
        var prevVf = 0.0

        for (p in samples) {
            val props = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(p)
            assertTrue("Tsat must increase strictly at P=$p", props.saturationTempCelsius > prevT)
            assertTrue("hf must increase strictly at P=$p", props.liquidEnthalpyHfKjKg > prevHf)
            assertTrue("vg must decrease strictly at P=$p", props.steamSpecificVolumeVgM3Kg < prevVg)
            assertTrue("vf must increase monotonically at P=$p", props.liquidSpecificVolumeVfM3Kg >= prevVf)
            assertEquals(
                "hg == hf + hfg at P=$p",
                props.liquidEnthalpyHfKjKg + props.evaporationEnthalpyHfgKjKg,
                props.steamEnthalpyHgKjKg,
                0.03
            )
            prevT = props.saturationTempCelsius
            prevHf = props.liquidEnthalpyHfKjKg
            prevVg = props.steamSpecificVolumeVgM3Kg
            prevVf = props.liquidSpecificVolumeVfM3Kg
        }
    }

    @Test
    fun gaugePressureConversionAt7BarMatchesBoilerOperatingRange() {
        val props = IapwsIf97SteamEngine.getPropertiesByGaugePressure(7.0)
        assertEquals(8.0133, props.pressureAbsoluteBar, 0.001)
        assertEquals(170.50, props.saturationTempCelsius, 0.05)
        assertEquals(2768.5, props.steamEnthalpyHgKjKg, 1.5)
        assertEquals(0.2400, props.steamSpecificVolumeVgM3Kg, 0.0005)
    }

    @Test
    fun linearPressureMidpointInterpolationStaysWithinTolerance() {
        val lowMid = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(0.75)
        assertEquals(91.76, lowMid.saturationTempCelsius, 0.05)
        assertEquals(2662.4, lowMid.steamEnthalpyHgKjKg, 1.0)
        assertEquals(2.2171, lowMid.steamSpecificVolumeVgM3Kg, 0.002)

        val midInterval = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(1.75)
        assertEquals(116.03, midInterval.saturationTempCelsius, 0.05)
        assertEquals(
            midInterval.liquidEnthalpyHfKjKg + midInterval.evaporationEnthalpyHfgKjKg,
            midInterval.steamEnthalpyHgKjKg,
            0.03
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfRangePressureBelowMinThrowsException() {
        IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(0.49)
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfRangePressureAboveMaxThrowsException() {
        IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(20.01)
    }

    @Test
    fun iapwsIf97Region4TsatAndLogPressureVgMatchOfficialBenchmarks() {
        // 4.25 bar a: midpoint of 4.00–4.50 bar interval where linear Tsat previously erred by -0.0511 C
        val p425 = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(4.25)
        assertEquals(145.81, p425.saturationTempCelsius, 0.01)
        assertEquals(0.4375, p425.steamSpecificVolumeVgM3Kg, 0.0005)
        assertEquals(613.89, p425.liquidEnthalpyHfKjKg, 0.25)
        assertEquals(2740.85, p425.steamEnthalpyHgKjKg, 0.35)

        // Additional midpoint benchmarks across low, medium, and upper grid intervals
        val p0525 = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(0.525)
        assertEquals(82.54, p0525.saturationTempCelsius, 0.01)
        assertEquals(3.0958, p0525.steamSpecificVolumeVgM3Kg, 0.001)

        val p1050 = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(10.50)
        assertEquals(182.02, p1050.saturationTempCelsius, 0.01)
        assertEquals(0.1855, p1050.steamSpecificVolumeVgM3Kg, 0.0005)

        val p1950 = IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(19.50)
        assertEquals(211.11, p1950.saturationTempCelsius, 0.01)
        assertEquals(0.1021, p1950.steamSpecificVolumeVgM3Kg, 0.0005)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nanAbsolutePressureThrowsException() {
        IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(Double.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun infiniteGaugePressureThrowsException() {
        IapwsIf97SteamEngine.getPropertiesByGaugePressure(Double.POSITIVE_INFINITY)
    }
}
