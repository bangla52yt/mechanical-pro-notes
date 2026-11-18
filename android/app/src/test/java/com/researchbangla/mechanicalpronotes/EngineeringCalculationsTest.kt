package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.calculation.BoilerThermalCalculations
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.calculation.WashPlantMaintenanceCalculations
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * JVM Unit Tests for all 20 Engineering Calculations (CALC-01 to CALC-20)
 * and safety/governance restrictions.
 */
class EngineeringCalculationsTest {

    @Test
    fun specificationsCoverAll20ModulesAndCalc02IsImplementedWithFullParameters() {
        val specs = CalculationSpecifications.ALL_CALCULATIONS
        assertEquals(20, specs.size)
        val calc02 = specs.first { it.id == "CALC-02" }
        assertEquals(CalculationStatus.IMPLEMENTED, calc02.status)
        assertEquals(13, calc02.variables.size)
    }

    @Test
    fun calc01DirectBoilerEfficiencyMatchesReference() {
        val res = BoilerThermalCalculations.executeCalc01(
            mapOf("m_s" to 4000.0, "h_s" to 2778.0, "h_w" to 335.0, "m_f" to 320.0, "NCV" to 35800.0)
        )
        assertEquals(85.30, res.primaryOutputValue, 0.1)
        assertEquals(CalculationStatus.IMPLEMENTED, res.status)
    }

    @Test
    fun calc02NaturalGasIndirectEfficiencyMatchesBenchmarkAndRetainsMixedJhutBoundary() {
        val gasRes = BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 185.0,
                "T_ambient" to 30.0,
                "CO2" to 9.5,
                "GCV" to 39800.0,
                "H2_percent" to 23.0,
                "fuel_type" to 1.0,
                "gcv_unit_mode" to 2.0,
                "fuel_density" to 0.74,
                "L_radiation" to 1.5
            )
        )
        assertEquals(CalculationStatus.IMPLEMENTED, gasRes.status)
        assertEquals(6.20, gasRes.secondaryOutputs["Dry Flue Gas Loss L1 (%)"] ?: 0.0, 0.05)
        assertEquals(10.65, gasRes.secondaryOutputs["H2 Combustion Loss L2 (%)"] ?: 0.0, 0.1)
        assertEquals(0.15, gasRes.secondaryOutputs["Air Moisture Loss L4 (%)"] ?: 0.0, 0.05)
        assertEquals(1.50, gasRes.secondaryOutputs["Radiation & Convection L_rad (%)"] ?: 0.0, 0.01)
        assertEquals(81.50, gasRes.primaryOutputValue, 0.15)
        assertTrue(gasRes.notesAndRecommendations.contains("BS 845-1"))

        val jhutPartialRes = BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 210.0,
                "T_ambient" to 30.0,
                "CO2" to 12.0,
                "GCV" to 16500.0,
                "H2_percent" to 5.5,
                "fuel_type" to 2.0,
                "gcv_unit_mode" to 1.0,
                "siegert_k" to 0.56,
                "theoretical_air" to 5.5
            )
        )
        assertEquals(CalculationStatus.IMPLEMENTED, jhutPartialRes.status)
        assertTrue(jhutPartialRes.notesAndRecommendations.contains("UNVERIFIED MIXED-JHUT BOUNDARY"))
        assertTrue(jhutPartialRes.notesAndRecommendations.contains("partial heat balance only"))
    }

    @Test
    fun calc03ToCalc08BoilerThermalMatchReference() {
        val r03 = BoilerThermalCalculations.executeCalc03(
            mapOf("m_s" to 5000.0, "h_s" to 2778.0, "h_w" to 377.0, "eta" to 86.0, "NCV" to 35800.0)
        )
        assertEquals(389.92, r03.primaryOutputValue, 0.5)

        val r04 = BoilerThermalCalculations.executeCalc04(mapOf("m_s" to 25000.0, "m_f" to 2000.0))
        assertEquals(12.50, r04.primaryOutputValue, 0.01)

        val r05 = BoilerThermalCalculations.executeCalc05(mapOf("TDS_feed" to 150.0, "TDS_boiler" to 3500.0))
        assertEquals(4.48, r05.primaryOutputValue, 0.02)

        val r06 = BoilerThermalCalculations.executeCalc06(mapOf("m_bd" to 250.0, "P_boiler" to 8.0))
        assertTrue(r06.primaryOutputValue >= 0.0)
        assertTrue((r06.secondaryOutputs["Flash Steam Generated (kg/h)"] ?: 0.0) in 25.0..32.0)

        val r07 = BoilerThermalCalculations.executeCalc07(mapOf("Q" to 10.0, "H" to 120.0, "density" to 960.0))
        assertEquals(3.14, r07.primaryOutputValue, 0.05)

        val r08 = BoilerThermalCalculations.executeCalc08(mapOf("m_feed" to 6000.0, "T_makeup" to 30.0))
        assertEquals(736.0, r08.primaryOutputValue, 15.0)
    }

    @Test
    fun calc09ToCalc14BoilerUtilitiesAndDisclaimers() {
        val r09 = BoilerThermalCalculations.executeCalc09(mapOf("P_set" to 10.0, "Area" to 800.0, "Kd" to 0.62))
        assertEquals(2870.0, r09.primaryOutputValue, 10.0)
        assertTrue(r09.notesAndRecommendations.contains("OCIB"))

        val r10 = BoilerThermalCalculations.executeCalc10(
            mapOf("m_gas" to 4500.0, "T_gas_in" to 220.0, "T_gas_out" to 140.0, "m_water" to 4000.0)
        )
        assertEquals(105.0, r10.primaryOutputValue, 0.1)

        val r11 = BoilerThermalCalculations.executeCalc11(mapOf("m_fuel" to 320.0, "EA_percent" to 15.0))
        assertEquals(60.11, r11.primaryOutputValue, 0.2)

        val r12 = BoilerThermalCalculations.executeCalc12(mapOf("m_flue" to 5200.0, "T_flue" to 160.0, "delta_P" to 150.0))
        assertEquals(106.29, r12.primaryOutputValue, 0.5)

        val r13 = BoilerThermalCalculations.executeCalc13(mapOf("H" to 30.0, "T_ambient" to 30.0, "T_stack" to 180.0))
        assertEquals(11.56, r13.primaryOutputValue, 0.2)

        val r14 = BoilerThermalCalculations.executeCalc14(mapOf("m_s" to 3000.0, "P_steam" to 7.0, "velocity" to 30.0))
        assertEquals(92.2, r14.primaryOutputValue, 1.5)
        assertEquals(100.0, r14.secondaryOutputs["Recommended Nominal Pipe Size (DN)"] ?: 0.0, 0.1)
    }

    @Test
    fun calc15ToCalc19WashPlantToolsMatchReference() {
        val r15 = WashPlantMaintenanceCalculations.executeCalc15(
            mapOf("d_pulley" to 160.0, "D_pulley" to 480.0, "C_center" to 750.0, "N_driver" to 1450.0)
        )
        assertEquals(2539.44, r15.primaryOutputValue, 0.5)

        val r16Safe = WashPlantMaintenanceCalculations.executeCalc16(
            mapOf("r_basket" to 60.0, "N_rpm" to 850.0, "max_rated_rpm" to 950.0)
        )
        assertEquals(484.76, r16Safe.primaryOutputValue, 0.5)
        assertFalse(r16Safe.notesAndRecommendations.startsWith("DANGER:"))

        val r16Danger = WashPlantMaintenanceCalculations.executeCalc16(
            mapOf("r_basket" to 60.0, "N_rpm" to 1050.0, "max_rated_rpm" to 950.0)
        )
        assertTrue(r16Danger.notesAndRecommendations.contains("DANGER:"))

        val r17 = WashPlantMaintenanceCalculations.executeCalc17(
            mapOf("m_water" to 800.0, "m_fabric" to 120.0, "T_initial" to 25.0, "T_target" to 70.0, "P_steam" to 3.5)
        )
        assertEquals(81.2, r17.primaryOutputValue, 1.5)

        val r18 = WashPlantMaintenanceCalculations.executeCalc18(
            mapOf("m_wet" to 180.0, "MC_in" to 50.0, "MC_out" to 5.0, "cycle_time" to 35.0)
        )
        assertEquals(146.17, r18.primaryOutputValue, 0.5)

        val r19 = WashPlantMaintenanceCalculations.executeCalc19(
            mapOf("hardness" to 220.0, "V_resin" to 500.0, "capacity_eq" to 1.3, "salt_dosage" to 140.0)
        )
        assertEquals(147.73, r19.primaryOutputValue, 0.5)
    }

    @Test
    fun calc20CompressedAirPressureDropUnitScalingAndHeaderGradientChecked() {
        val r20 = WashPlantMaintenanceCalculations.executeCalc20(
            mapOf("Q_cfm" to 150.0, "d_pipe" to 50.0, "L_pipe" to 60.0, "P_air" to 7.0)
        )
        // Expected ~0.019 bar after mm -> cm conversion (NOT 1.91e-7 bar!)
        assertEquals(0.019, r20.primaryOutputValue, 0.005)
        assertTrue(r20.notesAndRecommendations.contains("PRELIMINARY SIZING"))

        val highDrop = WashPlantMaintenanceCalculations.executeCalc20(
            mapOf("Q_cfm" to 250.0, "d_pipe" to 25.0, "L_pipe" to 100.0, "P_air" to 7.0)
        )
        assertTrue(highDrop.notesAndRecommendations.contains("0.1 bar per 100 m"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blowdownRejectsEqualFeedAndBoilerTds() {
        BoilerThermalCalculations.executeCalc05(mapOf("TDS_feed" to 1000.0, "TDS_boiler" to 1000.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun deaeratorRejectsMakeupTempExceedingDeaeratorTemp() {
        BoilerThermalCalculations.executeCalc08(
            mapOf("m_feed" to 5000.0, "T_makeup" to 115.0, "P_deaerator" to 0.2, "P_steam" to 7.0)
        )
    }
}
