package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.calculation.BoilerThermalCalculations
import com.researchbangla.mechanicalpronotes.domain.calculation.WashPlantMaintenanceCalculations
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * Stage 2 Engineering Calculation Validation & Boundary Regression Tests
 * Covering CALC-02, CALC-06, CALC-09, CALC-11, CALC-19, and CALC-20.
 */
class Stage02CalculationValidationTest {

    // -------------------------------------------------------------------------
    // CALC-02: GCV Unit Consistency, Omitted Loss Disclosure & Solid-Fuel Guards
    // -------------------------------------------------------------------------

    @Test
    fun calc02ConvertsVolumetricGasGcvWhenDensityProvidedAndDisclosesOmittedLosses() {
        val gasVolumetric = BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 185.0,
                "T_ambient" to 30.0,
                "CO2" to 9.5,
                "GCV" to 39800.0,
                "H2_percent" to 23.0,
                "gcv_unit_mode" to 2.0,
                "fuel_density" to 0.74
            )
        )
        assertEquals(CalculationStatus.IMPLEMENTED, gasVolumetric.status)
        // Converted mass GCV = 39800 / 0.74 = 53783.78 kJ/kg -> L2 = 10.65%, L1 = 6.20%, L4 = 0.15%, eta = 83.00%
        assertEquals(10.65, gasVolumetric.secondaryOutputs["H2 Combustion Loss L2 (%)"] ?: 0.0, 0.1)
        assertEquals(0.0, gasVolumetric.secondaryOutputs["Radiation & Convection L_rad (%)"] ?: -1.0, 0.01)
        assertEquals(83.00, gasVolumetric.primaryOutputValue, 0.15)
        assertTrue(gasVolumetric.notesAndRecommendations.contains("Un-supplied losses (M_fuel, L_unburnt, L_radiation) excluded"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc02RejectsVolumetricGasGcvWithoutDensity() {
        BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 185.0,
                "T_ambient" to 30.0,
                "CO2" to 9.5,
                "GCV" to 39800.0,
                "H2_percent" to 23.0,
                "gcv_unit_mode" to 2.0
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc02RejectsSolidFuelWithVolumetricGcvMode() {
        BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 210.0,
                "T_ambient" to 30.0,
                "CO2" to 12.0,
                "GCV" to 17500.0,
                "H2_percent" to 5.5,
                "fuel_type" to 2.0,
                "gcv_unit_mode" to 2.0,
                "fuel_density" to 0.80,
                "siegert_k" to 0.56,
                "theoretical_air" to 5.5
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc02RejectsMissingRequiredCo2OrH2() {
        BoilerThermalCalculations.executeCalc02(
            mapOf("T_flue" to 185.0, "T_ambient" to 30.0, "GCV" to 39800.0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc02RejectsSolidFuelWithoutLabSiegertK() {
        BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 210.0,
                "T_ambient" to 30.0,
                "CO2" to 12.0,
                "GCV" to 16500.0,
                "H2_percent" to 5.5,
                "fuel_type" to 2.0,
                "gcv_unit_mode" to 1.0,
                "theoretical_air" to 5.5
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc02RejectsSolidFuelWithoutLabTheoreticalAir() {
        BoilerThermalCalculations.executeCalc02(
            mapOf(
                "T_flue" to 210.0,
                "T_ambient" to 30.0,
                "CO2" to 12.0,
                "GCV" to 16500.0,
                "H2_percent" to 5.5,
                "fuel_type" to 2.0,
                "gcv_unit_mode" to 1.0,
                "siegert_k" to 0.56
            )
        )
    }

    // -------------------------------------------------------------------------
    // CALC-06: P_boiler > P_flash & No Silent Clamping
    // -------------------------------------------------------------------------

    @Test
    fun calc06NormalBlowdownRecoveryMatchesThermodynamicsAndExposesLatentPower() {
        val res = BoilerThermalCalculations.executeCalc06(
            mapOf("m_bd" to 250.0, "P_boiler" to 8.0, "P_flash" to 0.5, "T_makeup" to 25.0, "eta_hex" to 0.85)
        )
        assertEquals(28.54, res.secondaryOutputs["Flash Steam Generated (kg/h)"] ?: 0.0, 0.2)
        assertEquals(16.77, res.primaryOutputValue, 0.2)
        assertTrue((res.secondaryOutputs["Flash Steam Latent Power (kW)"] ?: 0.0) in 17.0..18.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc06RejectsEqualBoilerAndFlashPressuresInsteadOfSilentZeroClamp() {
        BoilerThermalCalculations.executeCalc06(
            mapOf("m_bd" to 250.0, "P_boiler" to 3.0, "P_flash" to 3.0, "T_makeup" to 25.0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc06RejectsFlashPressureExceedingBoilerPressure() {
        BoilerThermalCalculations.executeCalc06(
            mapOf("m_bd" to 250.0, "P_boiler" to 2.0, "P_flash" to 3.0, "T_makeup" to 25.0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc06RejectsNegativeFlashPressureOrHotMakeupApproachViolation() {
        BoilerThermalCalculations.executeCalc06(
            mapOf("m_bd" to 250.0, "P_boiler" to 8.0, "P_flash" to 0.0, "T_makeup" to 92.0)
        )
    }

    // -------------------------------------------------------------------------
    // CALC-09: Napier Coefficient 0.51, Area vs Throat Diameter & Bounds
    // -------------------------------------------------------------------------

    @Test
    fun calc09ComputesConservativeNapierCapacityAndEquivalentThroatDiameter() {
        val byArea = BoilerThermalCalculations.executeCalc09(
            mapOf("P_set" to 10.0, "Area" to 800.0, "Kd" to 0.62, "MCR" to 2500.0)
        )
        assertEquals(2869.49, byArea.primaryOutputValue, 0.2)
        assertEquals(31.92, byArea.secondaryOutputs["Equivalent Throat Diameter (mm)"] ?: 0.0, 0.05)
        assertEquals(1.0, byArea.secondaryOutputs["Adequate for MCR (1=Yes, 0=No)"] ?: -1.0, 0.001)

        val byThroat = BoilerThermalCalculations.executeCalc09(
            mapOf("P_set" to 10.0, "d_throat" to 31.9154, "Kd" to 0.62)
        )
        assertEquals(byArea.primaryOutputValue, byThroat.primaryOutputValue, 0.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc09RejectsOutOfBoundsDischargeCoefficient() {
        BoilerThermalCalculations.executeCalc09(
            mapOf("P_set" to 10.0, "Area" to 800.0, "Kd" to 1.25)
        )
    }

    // -------------------------------------------------------------------------
    // CALC-11: Natural Gas Default vs Custom A_0 & Normal vs Ambient FAD
    // -------------------------------------------------------------------------

    @Test
    fun calc11SeparatesNormalAirflowFromAmbientFadAndRejectsMassFuelWithoutA0() {
        val ng = BoilerThermalCalculations.executeCalc11(
            mapOf("m_fuel" to 320.0, "EA_percent" to 15.0)
        )
        assertEquals("Nm3/min", ng.primaryOutputUnit)
        assertEquals(60.11, ng.primaryOutputValue, 0.05)
        assertEquals(66.71, ng.secondaryOutputs["FAD Airflow at 30.0°C (m3/min)"] ?: 0.0, 0.1)

        val jhut = BoilerThermalCalculations.executeCalc11(
            mapOf("m_fuel" to 500.0, "EA_percent" to 35.0, "fuel_unit_kg" to 1.0, "A_0" to 5.2, "T_ambient" to 35.0)
        )
        assertEquals(58.50, jhut.primaryOutputValue, 0.05)
        assertEquals(65.99, jhut.secondaryOutputs["FAD Airflow at 35.0°C (m3/min)"] ?: 0.0, 0.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc11RejectsMassBasisFuelWithoutExplicitStoichA0() {
        BoilerThermalCalculations.executeCalc11(
            mapOf("m_fuel" to 500.0, "EA_percent" to 35.0, "fuel_unit_kg" to 1.0)
        )
    }

    // -------------------------------------------------------------------------
    // CALC-19: Separate Per-Regen vs Daily Salt Consumption
    // -------------------------------------------------------------------------

    @Test
    fun calc19OmitsDailySaltWithoutFrequencyAndCalculatesWhenSupplied() {
        val noFreq = WashPlantMaintenanceCalculations.executeCalc19(
            mapOf("hardness" to 220.0, "V_resin" to 500.0, "capacity_eq" to 1.3, "salt_dosage" to 140.0)
        )
        assertEquals(147.73, noFreq.primaryOutputValue, 0.05)
        assertEquals(70.0, noFreq.secondaryOutputs["Salt Required per Regeneration (kg)"] ?: 0.0, 0.01)
        assertFalse(noFreq.secondaryOutputs.containsKey("Daily Salt Consumption (kg/day)"))

        val withCycles = WashPlantMaintenanceCalculations.executeCalc19(
            mapOf("hardness" to 220.0, "V_resin" to 500.0, "capacity_eq" to 1.3, "salt_dosage" to 140.0, "regen_per_day" to 2.5)
        )
        assertEquals(175.0, withCycles.secondaryOutputs["Daily Salt Consumption (kg/day)"] ?: 0.0, 0.01)

        val withDailyWater = WashPlantMaintenanceCalculations.executeCalc19(
            mapOf("hardness" to 220.0, "V_resin" to 500.0, "capacity_eq" to 1.3, "salt_dosage" to 140.0, "daily_water_m3" to 295.45)
        )
        assertEquals(140.0, withDailyWater.secondaryOutputs["Daily Salt Consumption (kg/day)"] ?: 0.0, 0.2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc19RejectsOutOfBoundsResinCapacity() {
        WashPlantMaintenanceCalculations.executeCalc19(
            mapOf("hardness" to 220.0, "V_resin" to 500.0, "capacity_eq" to 5.0)
        )
    }

    // -------------------------------------------------------------------------
    // CALC-20: Compressed Air Pressure Validation & Limitations Disclosure
    // -------------------------------------------------------------------------

    @Test
    fun calc20PreservesSmoothEmpiricalDropWithDarcyAtlasDisclosure() {
        val res = WashPlantMaintenanceCalculations.executeCalc20(
            mapOf("Q_cfm" to 150.0, "d_pipe" to 50.0, "L_pipe" to 60.0, "P_air" to 7.0)
        )
        assertEquals(0.019, res.primaryOutputValue, 0.001)
        assertTrue(res.notesAndRecommendations.contains("Atlas Copco 450"))
        assertTrue(res.notesAndRecommendations.contains("Darcy-Weisbach"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun calc20RejectsZeroOrNegativeGaugeSupplyPressure() {
        WashPlantMaintenanceCalculations.executeCalc20(
            mapOf("Q_cfm" to 150.0, "d_pipe" to 50.0, "L_pipe" to 60.0, "P_air" to -1.01325)
        )
    }
}
