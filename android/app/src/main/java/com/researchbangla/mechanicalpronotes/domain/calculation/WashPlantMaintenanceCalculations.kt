package com.researchbangla.mechanicalpronotes.domain.calculation

import com.researchbangla.mechanicalpronotes.domain.model.CalculationExecutionResult
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.steam.IapwsIf97SteamEngine
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConverters

/**
 * Category B: Wash Plant Maintenance & Reliability Tools (CALC-15 to CALC-20).
 * Pure Kotlin deterministic engineering implementations.
 */
object WashPlantMaintenanceCalculations {

    /**
     * CALC-15: V-Belt Drive Sizing & Tensioning
     * ISO 4184 / RMA Mechanical Power Transmission Standards
     */
    fun executeCalc15(inputs: Map<String, Double>): CalculationExecutionResult {
        val dMm = UnitConverters.validatePositiveFinite(inputs["d_pulley"], "Driver Pulley Pitch Diameter (d, mm)")
        val dDrivenMm = UnitConverters.validatePositiveFinite(inputs["D_pulley"], "Driven Pulley Pitch Diameter (D, mm)")
        val cMm = UnitConverters.validatePositiveFinite(inputs["C_center"], "Center Distance (C, mm)")
        val nDriverRpm = UnitConverters.validatePositiveFinite(inputs["N_driver"], "Motor Driver Speed (N, RPM)")

        require(cMm > (dMm + dDrivenMm) / 2.0) {
            "Center distance ($cMm mm) must exceed minimum clearance between pulleys."
        }

        // Speed ratio and driven speed
        val speedRatio = dDrivenMm / dMm
        val nDrivenRpm = nDriverRpm / speedRatio

        // Pitch length: L = 2C + (π/2)*(D+d) + (D-d)^2 / (4C)
        val term1 = 2.0 * cMm
        val term2 = (Math.PI / 2.0) * (dDrivenMm + dMm)
        val term3 = Math.pow(dDrivenMm - dMm, 2.0) / (4.0 * cMm)
        val pitchLengthMm = term1 + term2 + term3

        // Recommended strand deflection under finger pressure (1 mm per 100 mm center distance)
        val deflectionMm = cMm / 100.0

        return CalculationExecutionResult(
            calculationId = "CALC-15",
            primaryOutputName = "Belt Pitch Length",
            primaryOutputValue = round2(pitchLengthMm),
            primaryOutputUnit = "mm",
            secondaryOutputs = mapOf(
                "Speed Ratio (i)" to round2(speedRatio),
                "Driven Drum/Shaft Speed (RPM)" to round2(nDrivenRpm),
                "Standard Deflection Check (mm)" to round2(deflectionMm)
            ),
            notesAndRecommendations = "Check strand tension after 24 hours of run-in. Deflection force should match belt section (SPZ, SPA, SPB, SPC) manufacturer guidelines.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-16: Hydro Extractor Centrifugal G-Force & Critical RPM
     * Centrifugal Acceleration: G = (r * ω^2) / g
     */
    fun executeCalc16(inputs: Map<String, Double>): CalculationExecutionResult {
        val radiusCm = UnitConverters.validatePositiveFinite(inputs["r_basket"], "Basket Drum Radius (r, cm)")
        val speedRpm = UnitConverters.validatePositiveFinite(inputs["N_rpm"], "Operating Rotational Speed (N, RPM)")
        val maxRatedRpm = UnitConverters.validatePositiveFinite(
            inputs["max_rated_rpm"] ?: (speedRpm * 1.10),
            "Manufacturer Rated Maximum RPM"
        )

        // a = r * omega^2 -> RCF = 0.00001118 * r_cm * N^2
        val omegaRadS = (2.0 * Math.PI * speedRpm) / 60.0
        val rMeters = radiusCm / 100.0
        val accelerationMs2 = rMeters * Math.pow(omegaRadS, 2.0)
        val gForce = accelerationMs2 / 9.80665

        // Rim peripheral velocity: v = pi * D * N / 60
        val rimVelocityMs = (Math.PI * (2.0 * rMeters) * speedRpm) / 60.0

        val exceedsRated = speedRpm > maxRatedRpm

        return CalculationExecutionResult(
            calculationId = "CALC-16",
            primaryOutputName = "Relative Centrifugal Force (RCF)",
            primaryOutputValue = round2(gForce),
            primaryOutputUnit = "G",
            secondaryOutputs = mapOf(
                "Centrifugal Acceleration (m/s2)" to round2(accelerationMs2),
                "Basket Rim Speed (m/s)" to round2(rimVelocityMs),
                "Rated RPM Margin (%)" to round2(((maxRatedRpm - speedRpm) / maxRatedRpm) * 100.0)
            ),
            notesAndRecommendations = if (exceedsRated) {
                "DANGER: Operating RPM exceeds rated safe limit! Severe mechanical risk of drum explosion or basket structural failure."
            } else {
                "SAFETY RESTRICTION: Theoretical calculation does NOT authorize exceeding manufacturer's nameplate RPM. Inspect suspension links and lid interlocks regularly."
            },
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-17: Wash Batch Steam Consumption
     * Thermal Liquor Ratio Heating: Q = (m_w * C_pw + m_g * C_pg) * ΔT * LossFactor
     */
    fun executeCalc17(inputs: Map<String, Double>): CalculationExecutionResult {
        val waterKg = UnitConverters.validatePositiveFinite(inputs["m_water"], "Bath Water Mass (kg)")
        val fabricKg = UnitConverters.validatePositiveFinite(inputs["m_fabric"], "Garment Dry Load (kg)")
        val tInitial = UnitConverters.validatePositiveFinite(inputs["T_initial"], "Initial Water Temp (°C)")
        val tTarget = UnitConverters.validatePositiveFinite(inputs["T_target"], "Target Washing Temp (°C)")
        val pSteamBar = inputs["P_steam"] ?: 3.5

        require(tTarget > tInitial) { "Target temperature ($tTarget°C) must exceed initial temp ($tInitial°C)." }

        val deltaT = tTarget - tInitial
        val cpWater = 4.186 // kJ/kg.°C
        val cpCotton = 1.34 // kJ/kg.°C for dry cotton

        val qWaterKj = waterKg * cpWater * deltaT
        val qCottonKj = fabricKg * cpCotton * deltaT
        val qTotalKj = (qWaterKj + qCottonKj) * 1.10 // 10% thermal loss through drum shell and ventilation

        val steamProps = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pSteamBar)
        val hfg = steamProps.evaporationEnthalpyHfgKjKg

        val steamRequiredKg = qTotalKj / hfg
        val specificSteamConsumption = steamRequiredKg / fabricKg // kg steam / kg garment

        return CalculationExecutionResult(
            calculationId = "CALC-17",
            primaryOutputName = "Batch Steam Consumption",
            primaryOutputValue = round2(steamRequiredKg),
            primaryOutputUnit = "kg steam/batch",
            secondaryOutputs = mapOf(
                "Total Heating Energy (kJ)" to round2(qTotalKj),
                "Specific Steam Consumption" to round2(specificSteamConsumption),
                "Liquor Ratio (L:G)" to round2(waterKg / fabricKg)
            ),
            notesAndRecommendations = "Liquor ratio significantly dictates steam consumption. Reducing liquor ratio from 1:8 to 1:5 saves ~35% heating steam.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-18: Tumble Dryer Evaporation Rate & Airflow
     */
    fun executeCalc18(inputs: Map<String, Double>): CalculationExecutionResult {
        val mWetKg = UnitConverters.validatePositiveFinite(inputs["m_wet"], "Initial Wet Garment Load (kg)")
        val mcInPercent = UnitConverters.validatePositiveFinite(inputs["MC_in"], "Initial Moisture Content (% wet basis)")
        val mcOutPercent = inputs["MC_out"] ?: 5.0 // Default 5% final moisture
        val cycleTimeMinutes = UnitConverters.validatePositiveFinite(inputs["cycle_time"], "Drying Cycle Time (min)")

        require(mcOutPercent in 0.0..50.0) { "Target outlet moisture ($mcOutPercent%) must be between 0% and 50%." }
        require(mcInPercent > mcOutPercent) { "Initial moisture must exceed target moisture." }
        require(mcInPercent < 90.0) { "Moisture content must be <90%." }

        // Bone dry fabric mass: m_dry = m_wet * (1 - MC_in)
        val boneDryKg = mWetKg * (1.0 - mcInPercent / 100.0)
        // Final goods mass: m_final = m_dry / (1 - MC_out)
        val mFinalKg = boneDryKg / (1.0 - mcOutPercent / 100.0)
        val waterEvaporatedKg = mWetKg - mFinalKg

        val evaporationRateKgH = waterEvaporatedKg / (cycleTimeMinutes / 60.0)

        // Heat duty required: ~2450 kJ/kg water latent heat * 1.35 efficiency factor (~70% thermal efficiency)
        val heatDutyKw = (evaporationRateKgH * 2450.0 * 1.35) / 3600.0

        // Airflow estimation: typical 1.0 kg air removes ~0.02 kg moisture
        val airMassKgH = waterEvaporatedKg * 50.0 / (cycleTimeMinutes / 60.0)
        val airCfm = (airMassKgH / 1.18 / 60.0) * 35.3147 // Convert m3/min to CFM

        return CalculationExecutionResult(
            calculationId = "CALC-18",
            primaryOutputName = "Water Evaporation Rate",
            primaryOutputValue = round2(evaporationRateKgH),
            primaryOutputUnit = "kg water/h",
            secondaryOutputs = mapOf(
                "Total Moisture Removed (kg)" to round2(waterEvaporatedKg),
                "Dryer Thermal Duty (kW)" to round2(heatDutyKw),
                "Estimated Exhaust Airflow (CFM)" to round2(airCfm)
            ),
            notesAndRecommendations = "Ensure lint screen is cleared every shift to prevent airflow restriction and excessive cycle time.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-19: Water Softener Resin Capacity & Salt Regeneration Sizing
     * Separates salt required per regeneration from daily salt consumption; computes daily
     * consumption only when explicit regeneration frequency (regen_per_day) or daily water
     * demand (daily_water_m3) is supplied.
     */
    fun executeCalc19(inputs: Map<String, Double>): CalculationExecutionResult {
        val hardnessPpm = UnitConverters.validatePositiveFinite(inputs["hardness"], "Raw Water Hardness (ppm as CaCO3)")
        val resinVolumeLiters = UnitConverters.validatePositiveFinite(inputs["V_resin"], "Cation Resin Volume (L)")
        val resinCapacityEqL = UnitConverters.validatePositiveFinite(inputs["capacity_eq"] ?: 1.3, "Resin Operating Capacity (eq/L)")
        val saltDosageGL = UnitConverters.validatePositiveFinite(inputs["salt_dosage"] ?: 140.0, "Salt Dosage per Liter Resin (g/L)")

        require(resinCapacityEqL in 0.4..3.0) {
            "Resin operating capacity ($resinCapacityEqL eq/L) must be within physical SAC resin range [0.4, 3.0] eq/L."
        }
        require(saltDosageGL in 40.0..400.0) {
            "Brine salt dosage ($saltDosageGL g/L resin) must be within [40, 400] g/L."
        }

        // 1 eq = 50 g CaCO3 equivalent. Total softening capacity:
        val totalCapacityGramsCaCo3 = resinVolumeLiters * resinCapacityEqL * 50.0
        // Soft water yield: m3 = grams CaCO3 / (ppm = g/m3)
        val softWaterRunM3 = totalCapacityGramsCaCo3 / hardnessPpm

        // Salt required per single regeneration cycle (kg):
        val saltRequiredKg = (resinVolumeLiters * saltDosageGL) / 1000.0

        val regenPerDay: Double? = when {
            inputs.containsKey("regen_per_day") -> {
                UnitConverters.validatePositiveFinite(inputs["regen_per_day"], "Regenerations per Day (cycles/day)")
            }
            inputs.containsKey("daily_water_m3") -> {
                val dailyWaterM3 = UnitConverters.validatePositiveFinite(inputs["daily_water_m3"], "Daily Soft Water Demand (m3/day)")
                dailyWaterM3 / softWaterRunM3
            }
            else -> null
        }

        val secondary = linkedMapOf(
            "Total Exchange Capacity (g CaCO3)" to round2(totalCapacityGramsCaCo3),
            "Salt Required per Regeneration (kg)" to round2(saltRequiredKg)
        )
        val scheduleNote = if (regenPerDay != null) {
            val dailySaltKg = saltRequiredKg * regenPerDay
            secondary["Regenerations per Day (cycles/day)"] = round2(regenPerDay)
            secondary["Daily Salt Consumption (kg/day)"] = round2(dailySaltKg)
            "Daily salt consumption (${round2(dailySaltKg)} kg/day) computed from ${round2(regenPerDay)} cycles/day."
        } else {
            "Daily salt consumption omitted because regeneration frequency (regen_per_day) or daily water volume (daily_water_m3) was not supplied."
        }

        return CalculationExecutionResult(
            calculationId = "CALC-19",
            primaryOutputName = "Soft Water Run Volume",
            primaryOutputValue = round2(softWaterRunM3),
            primaryOutputUnit = "m3 before regeneration",
            secondaryOutputs = secondary,
            notesAndRecommendations = "Assumes SAC resin operating capacity=${round2(resinCapacityEqL)} eq/L (50 g CaCO3/eq), salt dosage=${round2(saltDosageGL)} g/L resin, and negligible hardness leakage. $scheduleNote Maintain boiler feedwater hardness < 5 ppm as CaCO3.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-20: Compressed Air CFM & Distribution Pipe Pressure Drop
     * Retains empirical 0.00075 preliminary sizing correlation (closest to smooth drawn aluminum/copper
     * Darcy-Weisbach at 20-25°C) while enforcing positive gauge supply pressure validation and
     * documenting its ~26-33% lower drop prediction relative to rough steel / Atlas Copco 450.
     */
    fun executeCalc20(inputs: Map<String, Double>): CalculationExecutionResult {
        val airCfm = UnitConverters.validatePositiveFinite(inputs["Q_cfm"], "Compressed Air Demand (CFM, FAD)")
        val pipeDiameterMm = UnitConverters.validatePositiveFinite(inputs["d_pipe"], "Pipe Internal Diameter (mm)")
        val lengthMeters = UnitConverters.validatePositiveFinite(inputs["L_pipe"], "Total Pipe Length (m)")
        val pressureBarG = UnitConverters.validatePositiveFinite(inputs["P_air"] ?: 7.0, "Supply Pressure (bar g)")

        val pAbsBar = UnitConverters.gaugeToAbsolutePressureBar(pressureBarG)

        // Air velocity in pipe assuming isothermal FAD->compressed density scaling: v = Q_actual / A
        val compressionRatio = pAbsBar / UnitConverters.STANDARD_ATMOSPHERIC_PRESSURE_BAR
        val qM3MinFad = airCfm / 35.3147
        val qActualM3S = (qM3MinFad / compressionRatio) / 60.0
        val areaM2 = Math.PI * Math.pow(pipeDiameterMm / 1000.0, 2.0) / 4.0
        val velocityMs = qActualM3S / areaM2

        // Empirical pressure drop (preliminary smooth-bore correlation calibrated for d in cm):
        // ΔP (bar) = (0.00075 * L_m * Q_cfm^1.85) / (d_cm^5 * P_abs_bar)
        val pipeDiameterCm = pipeDiameterMm / 10.0
        val pressureDropBar = (0.00075 * lengthMeters * Math.pow(airCfm, 1.85)) /
                              (Math.pow(pipeDiameterCm, 5.0) * pAbsBar)

        // Air receiver capacity rule of thumb: ~0.15 m3 per 100 CFM
        val recommendedReceiverM3 = (airCfm / 100.0) * 0.15

        val dropPer100m = pressureDropBar * (100.0 / lengthMeters)
        val dropWarning = if (dropPer100m > 0.10) {
            " WARNING: Pressure drop gradient (${round3(dropPer100m)} bar per 100 m) exceeds 0.1 bar per 100 m main header limit; increase pipe diameter."
        } else {
            ""
        }
        val velocityWarning = if (velocityMs > 10.0) {
            " WARNING: Air velocity (${round2(velocityMs)} m/s) exceeds 10 m/s recommended header ceiling; consider upsizing pipe diameter."
        } else {
            ""
        }

        return CalculationExecutionResult(
            calculationId = "CALC-20",
            primaryOutputName = "Estimated Line Pressure Drop",
            primaryOutputValue = round3(pressureDropBar),
            primaryOutputUnit = "bar",
            secondaryOutputs = mapOf(
                "Compressed Air Velocity (m/s)" to round2(velocityMs),
                "Drop per 100 m (bar/100m)" to round3(dropPer100m),
                "Recommended Air Receiver (m3)" to round2(recommendedReceiverM3),
                "Operating Pressure (bar g)" to pressureBarG
            ),
            notesAndRecommendations = "PRELIMINARY SIZING: Uses empirical 0.00075 correlation (straight smooth bore, ~20°C isothermal dry air, excluding elbows/FRL fittings). Commercial galvanized steel (eps=0.045 mm Darcy-Weisbach) and Atlas Copco 450 yield ~1.35x-1.50x higher drop; apply fittings equivalent length in final header design. Keep velocity 6-10 m/s.$dropWarning$velocityWarning",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    private fun round2(v: Double) = Math.round(v * 100.0) / 100.0
    private fun round3(v: Double) = Math.round(v * 1000.0) / 1000.0
}
