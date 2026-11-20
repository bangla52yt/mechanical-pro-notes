package com.researchbangla.mechanicalpronotes.domain.calculation

import com.researchbangla.mechanicalpronotes.domain.model.CalculationExecutionResult
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.steam.IapwsIf97SteamEngine
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConverters

/**
 * Category A: Boiler Thermal & Utility Calculations (CALC-01 to CALC-14).
 * Pure Kotlin deterministic engineering implementations.
 */
object BoilerThermalCalculations {

    /**
     * CALC-01: Direct Thermal Efficiency (%)
     * Formula: η = [ m_s * (h_s - h_w) ] / [ m_f * NCV ] * 100
     */
    fun executeCalc01(inputs: Map<String, Double>): CalculationExecutionResult {
        val ms = UnitConverters.validatePositiveFinite(inputs["m_s"], "Steam Generation Rate (m_s, kg/h)")
        val hs = UnitConverters.validatePositiveFinite(inputs["h_s"], "Steam Enthalpy (h_s, kJ/kg)")
        val hw = UnitConverters.validateNonNegativeFinite(inputs["h_w"], "Feedwater Enthalpy (h_w, kJ/kg)")
        val mf = UnitConverters.validatePositiveFinite(inputs["m_f"], "Fuel Consumption (m_f)")
        val ncv = UnitConverters.validatePositiveFinite(inputs["NCV"], "Fuel Net Calorific Value (NCV, kJ/kg or kJ/Nm3)")

        require(hs > hw) { "Steam enthalpy ($hs kJ/kg) must exceed feedwater enthalpy ($hw kJ/kg)." }

        val usefulHeatKjH = ms * (hs - hw)
        val fuelHeatInputKjH = mf * ncv
        val efficiencyPercent = (usefulHeatKjH / fuelHeatInputKjH) * 100.0

        require(efficiencyPercent in 10.0..100.0) {
            "Calculated efficiency ($efficiencyPercent%) is physically implausible. Verify fuel NCV and flow rates."
        }

        return CalculationExecutionResult(
            calculationId = "CALC-01",
            primaryOutputName = "Thermal Efficiency (Direct Method)",
            primaryOutputValue = round2(efficiencyPercent),
            primaryOutputUnit = "%",
            secondaryOutputs = mapOf(
                "Useful Heat Output (kW)" to round2(usefulHeatKjH / 3600.0),
                "Fuel Heat Input (kW)" to round2(fuelHeatInputKjH / 3600.0)
            ),
            notesAndRecommendations = "Direct efficiency computed via input-output energy balance. Standard packaged gas boiler operates at 82%–88% NCV.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-02: Indirect Boiler Efficiency / Heat Loss Method (ASME PTC 4-2013 / BS 845-1 / IS 8753)
     * Verified for steady-state natural gas (Siegert K=0.38, gravimetric A_0=17.0 kg/kg, volumetric
     * GCV converted via fuel_density) and lab-calibrated liquid/solid fuels supplying mass GCV (kJ/kg),
     * explicit siegert_k, and theoretical_air. Uncalibrated mixed garment jhut retains an explicit
     * UNVERIFIED boundary notice and strict rejection if solid-fuel parameters are missing.
     */
    fun executeCalc02(inputs: Map<String, Double>): CalculationExecutionResult {
        val tFlue = UnitConverters.validatePositiveFinite(inputs["T_flue"], "Flue Gas Temperature (T_flue, °C)")
        val tAmbient = UnitConverters.validatePositiveFinite(inputs["T_ambient"], "Ambient Temperature (T_ambient, °C)")
        val co2Percent = UnitConverters.validatePositiveFinite(inputs["CO2"], "Flue Gas CO2 Concentration (%)")
        val gcvInput = UnitConverters.validatePositiveFinite(inputs["GCV"], "Gross Calorific Value (GCV)")
        val h2Percent = UnitConverters.validateNonNegativeFinite(inputs["H2_percent"], "Hydrogen Percentage in Fuel (%)")

        require(tFlue > tAmbient) { "Flue gas temperature ($tFlue°C) must exceed ambient ($tAmbient°C)." }
        require(co2Percent in 1.0..20.0) { "CO2 percentage ($co2Percent%) must be in valid range [1, 20]." }
        require(h2Percent <= 30.0) { "Hydrogen percentage ($h2Percent%) exceeds physical fuel range [0, 30]." }

        // Fuel-type & GCV unit-basis governance parameters
        // fuel_type: 1.0 = Natural Gas (default), 2.0 = Solid / Liquid Fuel (kg basis ONLY)
        val fuelType = inputs["fuel_type"] ?: 1.0
        require(fuelType == 1.0 || fuelType == 2.0) { "fuel_type must be 1.0 (Natural Gas) or 2.0 (Solid/Liquid mass fuel)." }

        // gcv_unit_mode: 1.0 = Mass basis (kJ/kg), 2.0 = Volumetric gas basis (kJ/Nm3 requiring fuel_density)
        val gcvUnitMode = inputs["gcv_unit_mode"]
        if (gcvUnitMode != null) {
            require(gcvUnitMode == 1.0 || gcvUnitMode == 2.0) {
                "gcv_unit_mode must be 1.0 (kJ/kg mass basis) or 2.0 (kJ/Nm3 gas basis)."
            }
        }
        if (fuelType == 2.0 && gcvUnitMode == 2.0) {
            throw IllegalArgumentException(
                "Incompatible fuel and GCV unit: solid/liquid fuels (fuel_type=2.0) must use mass-basis GCV (kJ/kg), not kJ/Nm3."
            )
        }

        val fuelDensityKgNm3 = inputs["fuel_density"]?.let {
            UnitConverters.validatePositiveFinite(it, "Fuel Gas Density (kg/Nm3)").also { rho ->
                require(rho in 0.55..1.50) { "Gaseous fuel density ($rho kg/Nm3) must be in physical range [0.55, 1.50]." }
            }
        }

        val gcvMassKjKg: Double
        val gcvBasisNote: String
        when {
            gcvUnitMode == 2.0 -> {
                val rho = fuelDensityKgNm3 ?: throw IllegalArgumentException(
                    "Volumetric gas GCV (kJ/Nm3) requires fuel_density (kg/Nm3) to convert to kJ/kg mass basis before L2/L3/L4 evaluation."
                )
                gcvMassKjKg = gcvInput / rho
                gcvBasisNote = "Converted volumetric gas GCV ($gcvInput kJ/Nm3 / $rho kg/Nm3 = ${round2(gcvMassKjKg)} kJ/kg)."
            }
            gcvUnitMode == 1.0 -> {
                gcvMassKjKg = gcvInput
                gcvBasisNote = "Mass-basis GCV ($gcvInput kJ/kg) verified."
            }
            fuelDensityKgNm3 != null -> {
                gcvMassKjKg = gcvInput / fuelDensityKgNm3
                gcvBasisNote = "Converted GCV using supplied fuel_density=$fuelDensityKgNm3 kg/Nm3 to ${round2(gcvMassKjKg)} kJ/kg."
            }
            else -> {
                gcvMassKjKg = gcvInput
                gcvBasisNote = "Assumes mass-basis GCV ($gcvInput kJ/kg); if using volumetric gas kJ/Nm3, set gcv_unit_mode=2.0 and fuel_density (~0.74 kg/Nm3)."
            }
        }

        val kFactor: Double = if (fuelType == 2.0) {
            val suppliedK = UnitConverters.validatePositiveFinite(inputs["siegert_k"], "Siegert K-Factor (for solid/liquid fuel)")
            require(suppliedK in 0.25..0.90) { "Siegert K-factor ($suppliedK) must be in physical range [0.25, 0.90]." }
            suppliedK
        } else {
            inputs["siegert_k"]?.let {
                UnitConverters.validatePositiveFinite(it, "Siegert K-Factor").also { k ->
                    require(k in 0.25..0.90) { "Siegert K-factor ($k) must be in physical range [0.25, 0.90]." }
                }
            } ?: 0.38
        }

        // Gravimetric stoichiometric air: ~17.0 kg air / kg pipeline natural gas (9.8 Nm3/Nm3 * 1.293 / 0.74)
        val theoreticalAirKgKg: Double = if (fuelType == 2.0) {
            UnitConverters.validatePositiveFinite(inputs["theoretical_air"], "Stoichiometric Air (kg air/kg fuel)").also { a ->
                require(a in 2.0..20.0) { "Stoichiometric air ratio ($a kg/kg) must be in range [2.0, 20.0]." }
            }
        } else {
            inputs["theoretical_air"]?.let {
                UnitConverters.validatePositiveFinite(it, "Stoichiometric Air (kg air/kg fuel)").also { a ->
                    require(a in 2.0..20.0) { "Stoichiometric air ratio ($a kg/kg) must be in range [2.0, 20.0]." }
                }
            } ?: 17.0
        }

        // Do not silently assume un-supplied fuel moisture, unburnt carbon, or radiation losses
        val omittedTerms = mutableListOf<String>()
        val moistureFuelPercent = inputs["M_fuel"]?.let {
            UnitConverters.validateNonNegativeFinite(it, "Fuel Moisture M_fuel (%)").also { m ->
                require(m <= 70.0) { "Fuel moisture ($m%) cannot exceed 70%." }
            }
        } ?: run {
            omittedTerms.add("M_fuel")
            0.0
        }
        val unburntLossPercent = inputs["L_unburnt"]?.let {
            UnitConverters.validateNonNegativeFinite(it, "Unburnt CO/Carbon Loss L_unburnt (%)").also { u ->
                require(u <= 25.0) { "Unburnt loss ($u%) cannot exceed 25%." }
            }
        } ?: run {
            omittedTerms.add("L_unburnt")
            0.0
        }
        val radiationLossPercent = inputs["L_radiation"]?.let {
            UnitConverters.validateNonNegativeFinite(it, "Radiation & Convection Loss L_radiation (%)").also { r ->
                require(r <= 15.0) { "Radiation loss ($r%) cannot exceed 15%." }
            }
        } ?: run {
            omittedTerms.add("L_radiation")
            0.0
        }

        val deltaT = tFlue - tAmbient

        // L1: Dry Flue Gas Loss (%) - BS 845-1 / IS 8753 Siegert equation
        val l1DryFlue = (kFactor * deltaT) / co2Percent

        // L2: Heat loss due to moisture formed by combustion of H2 (%)
        val l2Hydrogen = (9.0 * (h2Percent / 100.0) * (2442.0 + 2.09 * deltaT) / gcvMassKjKg) * 100.0

        // L3: Heat loss due to moisture in fuel (%)
        val l3MoistureFuel = if (moistureFuelPercent > 0.0) {
            ((moistureFuelPercent / 100.0) * (2442.0 + 2.09 * deltaT) / gcvMassKjKg) * 100.0
        } else 0.0

        // L4: Heat loss due to moisture in combustion air (%) at 0.015 kg H2O/kg dry air reference humidity
        val humidity = 0.015
        val l4MoistureAir = ((theoreticalAirKgKg * humidity * 2.09 * deltaT) / gcvMassKjKg) * 100.0

        val totalLosses = l1DryFlue + l2Hydrogen + l3MoistureFuel + l4MoistureAir + unburntLossPercent + radiationLossPercent
        val indirectEfficiency = 100.0 - totalLosses

        require(indirectEfficiency in 30.0..98.0) {
            "Computed indirect efficiency ($indirectEfficiency%) out of physical bounds [30, 98]."
        }

        val omittedNote = if (omittedTerms.isNotEmpty()) {
            " Un-supplied losses (${omittedTerms.joinToString(", ")}) excluded (0.0%) rather than assumed."
        } else ""

        val fuelScopeNote = if (fuelType == 2.0) {
            val missingSolidLab = listOf("M_fuel", "L_unburnt", "L_radiation").filter { it in omittedTerms }
            val solidGapNote = if (missingSolidLab.isNotEmpty()) {
                " Mixed-jhut/solid-fuel losses (${missingSolidLab.joinToString(", ")}) not entered; efficiency reflects partial heat balance only."
            } else {
                ""
            }
            "UNVERIFIED MIXED-JHUT BOUNDARY (ASME PTC 4 / BS 845-1, K=$kFactor, A0=$theoreticalAirKgKg kg/kg): Solid/mixed-jhut results rely strictly on lab-verified GCV (kJ/kg), H2%, siegert_k, theoretical_air, moisture, and ash LOI.$solidGapNote"
        } else {
            "BS 845-1 / IS 8753 Natural Gas Heat-Loss Balance (GCV basis, K=$kFactor, A0=$theoreticalAirKgKg kg/kg)."
        }

        return CalculationExecutionResult(
            calculationId = "CALC-02",
            primaryOutputName = "Indirect Boiler Efficiency (GCV Basis)",
            primaryOutputValue = round2(indirectEfficiency),
            primaryOutputUnit = "%",
            secondaryOutputs = mapOf(
                "Dry Flue Gas Loss L1 (%)" to round2(l1DryFlue),
                "H2 Combustion Loss L2 (%)" to round2(l2Hydrogen),
                "Fuel Moisture Loss L3 (%)" to round2(l3MoistureFuel),
                "Air Moisture Loss L4 (%)" to round2(l4MoistureAir),
                "Unburnt Loss L_unburnt (%)" to round2(unburntLossPercent),
                "Radiation & Convection L_rad (%)" to round2(radiationLossPercent),
                "Total Losses (%)" to round2(totalLosses)
            ),
            notesAndRecommendations = "$fuelScopeNote $gcvBasisNote$omittedNote Note: Natural gas GCV efficiency is ~9–10% lower than NCV efficiency due to latent heat of H2 combustion moisture.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-03: Boiler Fuel Firing Rate Requirement
     * Formula: m_f = [ m_s * (h_s - h_w) ] / [ (η / 100) * NCV ]
     */
    fun executeCalc03(inputs: Map<String, Double>): CalculationExecutionResult {
        val ms = UnitConverters.validatePositiveFinite(inputs["m_s"], "Target Steam Production (kg/h)")
        val hs = UnitConverters.validatePositiveFinite(inputs["h_s"], "Steam Enthalpy (kJ/kg)")
        val hw = UnitConverters.validateNonNegativeFinite(inputs["h_w"], "Feedwater Enthalpy (kJ/kg)")
        val efficiency = UnitConverters.validatePositiveFinite(inputs["eta"], "Boiler Efficiency (%)")
        val ncv = UnitConverters.validatePositiveFinite(inputs["NCV"], "Fuel NCV (kJ/kg or kJ/Nm3)")

        require(efficiency in 20.0..98.0) { "Efficiency must be between 20% and 98%." }
        require(hs > hw) { "Steam enthalpy must exceed feedwater enthalpy." }

        val usefulHeatKjH = ms * (hs - hw)
        val fuelRate = usefulHeatKjH / ((efficiency / 100.0) * ncv)
        val thermalPowerKw = (fuelRate * ncv) / 3600.0

        return CalculationExecutionResult(
            calculationId = "CALC-03",
            primaryOutputName = "Fuel Firing Rate",
            primaryOutputValue = round2(fuelRate),
            primaryOutputUnit = "unit/h (Nm3/h or kg/h)",
            secondaryOutputs = mapOf(
                "Useful Steam Power (kW)" to round2(usefulHeatKjH / 3600.0),
                "Total Thermal Firing Rate (kW)" to round2(thermalPowerKw)
            ),
            notesAndRecommendations = "Required burner input to satisfy plant steam demand at target thermal efficiency.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-04: Steam Evaporation Ratio
     * Formula: ER = m_s / m_f
     */
    fun executeCalc04(inputs: Map<String, Double>): CalculationExecutionResult {
        val ms = UnitConverters.validatePositiveFinite(inputs["m_s"], "Steam Generation Rate (kg/h)")
        val mf = UnitConverters.validatePositiveFinite(inputs["m_f"], "Fuel Consumption (Nm3/h or kg/h)")

        val er = ms / mf

        return CalculationExecutionResult(
            calculationId = "CALC-04",
            primaryOutputName = "Steam-to-Fuel Evaporation Ratio",
            primaryOutputValue = round2(er),
            primaryOutputUnit = "kg steam / unit fuel",
            secondaryOutputs = mapOf(
                "Hourly Steam (kg/h)" to ms,
                "Hourly Fuel (unit/h)" to mf
            ),
            notesAndRecommendations = "Natural gas benchmark: 11.5–13.5 kg/Nm3; Garment cotton jhut benchmark: 3.8–4.8 kg/kg.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-05: Continuous & Bottom Blowdown Rate (%)
     * Formula: BD% = [ TDS_feed / (TDS_boiler - TDS_feed) ] * 100
     */
    fun executeCalc05(inputs: Map<String, Double>): CalculationExecutionResult {
        val tdsFeed = UnitConverters.validatePositiveFinite(inputs["TDS_feed"], "Feedwater TDS (ppm)")
        val tdsBoiler = UnitConverters.validatePositiveFinite(inputs["TDS_boiler"], "Boiler Drum Max TDS (ppm)")

        require(tdsBoiler > tdsFeed) { "Boiler drum TDS limit ($tdsBoiler ppm) must exceed feedwater TDS ($tdsFeed ppm)." }

        val bdPercent = (tdsFeed / (tdsBoiler - tdsFeed)) * 100.0
        val ms = UnitConverters.validateNonNegativeFinite(inputs["m_s"] ?: 0.0, "Steam Generation Rate (m_s, kg/h)")
        val bdFlowKgH = if (ms > 0.0) ms * (bdPercent / 100.0) else 0.0

        return CalculationExecutionResult(
            calculationId = "CALC-05",
            primaryOutputName = "Blowdown Rate",
            primaryOutputValue = round2(bdPercent),
            primaryOutputUnit = "%",
            secondaryOutputs = if (bdFlowKgH > 0.0) mapOf("Blowdown Mass Flow (kg/h)" to round2(bdFlowKgH)) else emptyMap(),
            notesAndRecommendations = "Adhering to blowdown limits prevents priming, foaming, and carryover into industrial wash wheels.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-06: Blowdown Heat Recovery Potential
     * Enforces P_boiler > P_flash and thermodynamic HX temperature approach without silent clamping.
     */
    fun executeCalc06(inputs: Map<String, Double>): CalculationExecutionResult {
        val bdFlowKgH = UnitConverters.validatePositiveFinite(inputs["m_bd"], "Blowdown Water Flow (kg/h)")
        val pBoilerBar = UnitConverters.validatePositiveFinite(inputs["P_boiler"], "Boiler Pressure (bar g)")
        val pFlashBar = UnitConverters.validateNonNegativeFinite(inputs["P_flash"] ?: 0.5, "Flash Tank Pressure (bar g)")
        val tMakeup = UnitConverters.validateNonNegativeFinite(inputs["T_makeup"] ?: 25.0, "Make-up Water Temperature (°C)")
        val hexEfficiency = inputs["eta_hex"] ?: 0.85

        require(hexEfficiency.isFinite() && hexEfficiency in 0.10..1.0) {
            "Heat exchanger effectiveness eta_hex ($hexEfficiency) must be between 0.10 and 1.00."
        }
        require(pBoilerBar > pFlashBar) {
            "Boiler operating pressure ($pBoilerBar bar g) must strictly exceed flash tank pressure ($pFlashBar bar g)."
        }

        val boilerProps = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pBoilerBar)
        val flashProps = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pFlashBar)

        val hfBoiler = boilerProps.liquidEnthalpyHfKjKg
        val hfFlash = flashProps.liquidEnthalpyHfKjKg
        val hfgFlash = flashProps.evaporationEnthalpyHfgKjKg

        require(hfBoiler > hfFlash && hfgFlash > 0.0) {
            "Boiler saturated liquid enthalpy ($hfBoiler kJ/kg) must exceed flash enthalpy ($hfFlash kJ/kg)."
        }

        // Flash steam fraction (exact thermodynamic enthalpy ratio; no silent clamping)
        val flashFraction = (hfBoiler - hfFlash) / hfgFlash
        require(flashFraction > 0.0 && flashFraction < 1.0) {
            "Computed flash steam fraction ($flashFraction) is outside physical range (0, 1)."
        }
        val flashSteamKgH = bdFlowKgH * flashFraction
        val residualWaterKgH = bdFlowKgH - flashSteamKgH
        val flashSteamPowerKw = (flashSteamKgH * hfgFlash) / 3600.0

        // Sensible heat recovery in blowdown heat exchanger with 10°C minimum approach pinch (ΔT_approach = 10°C -> 41.86 kJ/kg)
        val approachDeltaC = 10.0
        val drainOutletTempC = tMakeup + approachDeltaC
        require(drainOutletTempC < flashProps.saturationTempCelsius) {
            "Make-up water temperature ($tMakeup°C) plus ${approachDeltaC}°C HX approach (${drainOutletTempC}°C) must be below flash saturation temperature (${flashProps.saturationTempCelsius}°C)."
        }
        val hDrainKjKg = drainOutletTempC * 4.186
        val recoverableHeatKw = (residualWaterKgH * (hfFlash - hDrainKjKg) * hexEfficiency) / 3600.0
        require(recoverableHeatKw > 0.0) {
            "Computed residual heat recovery ($recoverableHeatKw kW) must be strictly positive."
        }

        return CalculationExecutionResult(
            calculationId = "CALC-06",
            primaryOutputName = "Recoverable Heat Power",
            primaryOutputValue = round2(recoverableHeatKw),
            primaryOutputUnit = "kW",
            secondaryOutputs = mapOf(
                "Flash Steam Generated (kg/h)" to round2(flashSteamKgH),
                "Flash Fraction (%)" to round2(flashFraction * 100.0),
                "Residual Blowdown Water (kg/h)" to round2(residualWaterKgH),
                "Flash Steam Latent Power (kW)" to round2(flashSteamPowerKw)
            ),
            notesAndRecommendations = "Primary kW reports residual liquid HX recovery assuming eta_hex=${round2(hexEfficiency)} and 10°C cold-end drain approach above T_makeup (${round2(drainOutletTempC)}°C drain); flash steam adds ${round2(flashSteamPowerKw)} kW to deaerator.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-07: Feedwater Pump Hydraulic Power & Motor Rating (kW)
     * Formula: P_hyd = (ρ * g * Q * H) / (3.6e6)
     */
    fun executeCalc07(inputs: Map<String, Double>): CalculationExecutionResult {
        val qM3h = UnitConverters.validatePositiveFinite(inputs["Q"], "Flow Rate Q (m3/h)")
        val hMeters = UnitConverters.validatePositiveFinite(inputs["H"], "Total Dynamic Head H (m)")
        val density = UnitConverters.validatePositiveFinite(inputs["density"] ?: 960.0, "Water Density (kg/m3)")
        val pumpEfficiency = UnitConverters.validatePositiveFinite(inputs["eta_pump"] ?: 0.65, "Pump Efficiency")

        require(density in 800.0..1100.0) {
            "Water density ($density kg/m3) must be within physical feedwater range [800.0, 1100.0] kg/m3."
        }
        require(pumpEfficiency in 0.1..0.98) { "Pump efficiency must be between 10% and 98%." }

        val g = 9.81
        val hydraulicPowerKw = (density * g * qM3h * hMeters) / 3600000.0
        val shaftPowerKw = hydraulicPowerKw / pumpEfficiency
        val motorRatingKw = shaftPowerKw * 1.20 // 20% standard motor reserve

        return CalculationExecutionResult(
            calculationId = "CALC-07",
            primaryOutputName = "Pump Hydraulic Power",
            primaryOutputValue = round2(hydraulicPowerKw),
            primaryOutputUnit = "kW",
            secondaryOutputs = mapOf(
                "Shaft Brake Power (kW)" to round2(shaftPowerKw),
                "Recommended Motor Rating (kW)" to round2(motorRatingKw)
            ),
            notesAndRecommendations = "Pump head must exceed boiler safety valve lift pressure + economizer ΔP + static elevation.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-08: Deaerator Steam Balance
     */
    fun executeCalc08(inputs: Map<String, Double>): CalculationExecutionResult {
        val mFeed = UnitConverters.validatePositiveFinite(inputs["m_feed"], "Required Feedwater Output (kg/h)")
        val tMakeup = UnitConverters.validatePositiveFinite(inputs["T_makeup"], "Make-up Water Temp (°C)")
        val pDeaeratorBar = inputs["P_deaerator"] ?: 0.2 // Standard deaerator pressure 0.2 bar g (105°C)
        val pSteamBar = inputs["P_steam"] ?: 7.0 // Supply steam line pressure

        val deaeratorProps = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pDeaeratorBar)
        val steamProps = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pSteamBar)

        val hFeedTarget = deaeratorProps.liquidEnthalpyHfKjKg
        val hMakeup = tMakeup * 4.186
        val hSteamSupply = steamProps.steamEnthalpyHgKjKg

        require(hFeedTarget > hMakeup) {
            "Make-up water temperature ($tMakeup°C) must be below deaerator saturation temperature (${deaeratorProps.saturationTempCelsius}°C)."
        }
        require(hSteamSupply > hFeedTarget) {
            "Supply steam pressure ($pSteamBar bar g) must exceed deaerator pressure ($pDeaeratorBar bar g)."
        }

        // Energy balance: m_steam * h_steam + (m_feed - m_steam) * h_makeup = m_feed * h_feed
        // m_steam * (h_steam - h_makeup) = m_feed * (h_feed - h_makeup)
        val mSteam = mFeed * (hFeedTarget - hMakeup) / (hSteamSupply - hMakeup)

        return CalculationExecutionResult(
            calculationId = "CALC-08",
            primaryOutputName = "Deaerator Steam Consumption",
            primaryOutputValue = round2(mSteam),
            primaryOutputUnit = "kg/h",
            secondaryOutputs = mapOf(
                "Deaerator Target Temperature (°C)" to round2(deaeratorProps.saturationTempCelsius),
                "Net Soft Make-up Inflow (kg/h)" to round2(mFeed - mSteam),
                "Deaerator Steam Fraction (%)" to round2((mSteam / mFeed) * 100.0)
            ),
            notesAndRecommendations = "Maintains dissolved oxygen below 0.005 mg/L to prevent pitting corrosion in boiler tubes.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-09: Safety Valve Relieving Capacity Verification (Preliminary Estimation)
     * Uses conservative metric Napier coefficient 0.51 kg/(h * bar(a) * mm2) (~2.89% conservative
     * relative to unadjusted 51.5 Imperial -> 0.52515 SI factor for saturated steam up to 35 bar(g)).
     */
    fun executeCalc09(inputs: Map<String, Double>): CalculationExecutionResult {
        val pSetBarG = UnitConverters.validatePositiveFinite(inputs["P_set"], "Safety Valve Set Pressure (bar g)")
        require(pSetBarG in 0.5..35.0) {
            "Safety valve set pressure ($pSetBarG bar g) is outside industrial dry saturated scope [0.5, 35.0 bar g] without ASME Kn high-pressure correction."
        }

        val orificeAreaMm2 = when {
            inputs.containsKey("Area") -> UnitConverters.validatePositiveFinite(inputs["Area"], "Orifice Discharge Area (mm2)")
            inputs.containsKey("d_throat") -> {
                val dMm = UnitConverters.validatePositiveFinite(inputs["d_throat"], "Orifice Throat Diameter (mm)")
                Math.PI * dMm * dMm / 4.0
            }
            else -> UnitConverters.validatePositiveFinite(null, "Orifice Discharge Area (mm2)")
        }

        val kd = inputs["Kd"] ?: 0.62 // Derated discharge coefficient
        require(kd.isFinite() && kd in 0.10..1.0) {
            "Derated discharge coefficient Kd ($kd) must be between 0.10 and 1.00."
        }
        val boilerMcrKgH = UnitConverters.validateNonNegativeFinite(inputs["MCR"] ?: 0.0, "Boiler Max Evaporation MCR (kg/h)")

        val pAbs = UnitConverters.gaugeToAbsolutePressureBar(pSetBarG)
        val pRelievingAbs = pAbs * 1.03 // 3% overpressure allowance per ASME Section I PG-67/PG-73
        val equivalentThroatMm = Math.sqrt((4.0 * orificeAreaMm2) / Math.PI)

        // Conservative simplified metric Napier equation: W (kg/h) = 0.51 * Kd * A_mm2 * P_relieving(bar a)
        // (~2.89% below exact 51.5 Imperial conversion 0.52515 kg/(h*bar(a)*mm2), assuming dry saturated steam, Kb=1.0, Ksh=1.0)
        val relievingCapacityKgH = 0.51 * kd * orificeAreaMm2 * pRelievingAbs

        val isAdequate = if (boilerMcrKgH > 0.0) relievingCapacityKgH >= boilerMcrKgH else null

        return CalculationExecutionResult(
            calculationId = "CALC-09",
            primaryOutputName = "Calculated Relieving Capacity",
            primaryOutputValue = round2(relievingCapacityKgH),
            primaryOutputUnit = "kg/h",
            secondaryOutputs = mapOf(
                "Relieving Absolute Pressure (bar a)" to round2(pRelievingAbs),
                "Equivalent Throat Diameter (mm)" to round2(equivalentThroatMm),
                "Adequate for MCR (1=Yes, 0=No)" to (if (isAdequate == true) 1.0 else if (isAdequate == false) 0.0 else -1.0)
            ),
            notesAndRecommendations = "PRELIMINARY ESTIMATE ONLY (saturated steam, Kb=1.0, Ksh=1.0, conservative 0.51 Napier constant = -2.9% vs 0.52515): Does not constitute statutory certification. Confirm nameplate certified Kd and throat/curtain area with OCIB certified inspectors before commissioning.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-10: Economizer Flue Gas Heat Recovery
     */
    fun executeCalc10(inputs: Map<String, Double>): CalculationExecutionResult {
        val mGasKgH = UnitConverters.validatePositiveFinite(inputs["m_gas"], "Flue Gas Mass Flow (kg/h)")
        val tGasIn = UnitConverters.validatePositiveFinite(inputs["T_gas_in"], "Flue Gas Inlet Temp (°C)")
        val tGasOut = UnitConverters.validatePositiveFinite(inputs["T_gas_out"], "Flue Gas Outlet Temp (°C)")
        val mWaterKgH = UnitConverters.validatePositiveFinite(inputs["m_water"], "Feedwater Flow Rate (kg/h)")
        val tWaterIn = UnitConverters.validateNonNegativeFinite(inputs["T_water_in"] ?: 85.0, "Feedwater Inlet Temp (°C)")

        require(tWaterIn in 0.0..150.0) {
            "Feedwater inlet temperature ($tWaterIn°C) must be within liquid water economizer range [0.0, 150.0] °C."
        }
        require(tWaterIn < tGasIn) {
            "Feedwater inlet temperature ($tWaterIn°C) must be strictly below flue gas inlet temperature ($tGasIn°C)."
        }
        require(tGasIn > tGasOut) { "Flue gas inlet temp ($tGasIn°C) must exceed outlet ($tGasOut°C)." }
        require(tGasOut >= 120.0) { "Outlet temp ($tGasOut°C) should stay above 120°C to avoid acid dew point condensation." }

        val cpGas = 1.05 // kJ/kg.K
        val cpWater = 4.19 // kJ/kg.K

        val qRecoveredKjH = mGasKgH * cpGas * (tGasIn - tGasOut)
        val qRecoveredKw = qRecoveredKjH / 3600.0
        val deltaTWater = qRecoveredKjH / (mWaterKgH * cpWater)
        val tWaterOut = tWaterIn + deltaTWater
        val efficiencyGainPercent = deltaTWater / 6.0 // Rule of thumb: ~6°C water rise = ~1% boiler efficiency gain
        val steamingWarning = if (tWaterOut >= 105.0) {
            " WARNING: Feedwater outlet (${round2(tWaterOut)}°C) approaches saturation; verify economizer steaming margin against drum pressure."
        } else {
            ""
        }

        return CalculationExecutionResult(
            calculationId = "CALC-10",
            primaryOutputName = "Economizer Heat Recovery",
            primaryOutputValue = round2(qRecoveredKw),
            primaryOutputUnit = "kW",
            secondaryOutputs = mapOf(
                "Feedwater Temp Rise (°C)" to round2(deltaTWater),
                "Feedwater Outlet Temp (°C)" to round2(tWaterOut),
                "Estimated Boiler Efficiency Gain (%)" to round2(efficiencyGainPercent)
            ),
            notesAndRecommendations = "Maintain stack temperature above acid dew point (minimum 120°C for gas, 140°C for sulfur fuels) to avoid cold-end corrosion.$steamingWarning",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-11: Forced Draft (FD) Fan & Combustion Air Sizing
     * Distinguishes Natural Gas Nm3/h default stoichiometry (A_0 = 9.8 Nm3/Nm3) from
     * custom user-supplied A_0 (required for kg/h solid/liquid fuels), and separates
     * Normal reference flow (Nm3/min at 0°C, 1 atm) from ambient FAD flow (m3/min at T_ambient).
     */
    fun executeCalc11(inputs: Map<String, Double>): CalculationExecutionResult {
        val fuelRate = UnitConverters.validatePositiveFinite(inputs["m_fuel"], "Fuel Firing Rate")
        val excessAirPercent = UnitConverters.validateNonNegativeFinite(inputs["EA_percent"] ?: 15.0, "Excess Air (%)")
        val deltaPStaticMmWc = UnitConverters.validatePositiveFinite(inputs["delta_P"] ?: 120.0, "Total Static Head Resistance (mm WC)")
        val fanEfficiency = inputs["eta_fan"] ?: 0.70

        require(excessAirPercent <= 200.0) { "Excess air percentage ($excessAirPercent%) exceeds physical ceiling [0, 200]." }
        require(fanEfficiency.isFinite() && fanEfficiency in 0.2..0.98) { "FD fan efficiency ($fanEfficiency) must be between 0.20 and 0.98." }

        val fuelIsMassKg = (inputs["fuel_unit_kg"] ?: 0.0) >= 0.5
        val customStoichA0 = inputs["A_0"]
        if (fuelIsMassKg && customStoichA0 == null) {
            throw IllegalArgumentException(
                "Mass-basis fuel firing (kg/h) requires explicit stoichiometric air factor A_0 (Nm3 air/kg fuel). Default 9.8 applies only to Natural Gas (Nm3/h)."
            )
        }
        val theoreticalAirRatio = if (customStoichA0 != null) {
            UnitConverters.validatePositiveFinite(customStoichA0, "Stoichiometric Air Ratio A_0").also { a0 ->
                require(a0 in 2.0..20.0) { "Stoichiometric air ratio A_0 ($a0) must be within physical bounds [2.0, 20.0]." }
            }
        } else {
            9.8 // Natural Gas Nm3 air per Nm3 gas ONLY
        }

        val tAmbientC = inputs["T_ambient"] ?: 30.0
        require(tAmbientC.isFinite() && tAmbientC in -10.0..60.0) {
            "Ambient air temperature ($tAmbientC°C) must be within [-10, 60] °C."
        }

        val normalAirNm3H = fuelRate * theoreticalAirRatio * (1.0 + excessAirPercent / 100.0)
        val normalAirNm3Min = normalAirNm3H / 60.0

        // Convert Normal (0°C, 1.01325 bar) volumetric air to actual Free Air Delivery (FAD) at ambient temp
        val fadTempFactor = UnitConverters.celsiusToKelvin(tAmbientC) / UnitConverters.ZERO_CELSIUS_IN_KELVIN
        val actualAirFadM3H = normalAirNm3H * fadTempFactor
        val actualAirFadM3Min = actualAirFadM3H / 60.0

        // Fan shaft power scales with ACTUAL volumetric flow rate at operating intake temperature
        val deltaPPascals = deltaPStaticMmWc * 9.80665
        val fanPowerKw = ((actualAirFadM3H / 3600.0) * deltaPPascals) / (1000.0 * fanEfficiency)

        val stoichSourceNote = if (customStoichA0 != null) {
            "User-supplied stoichiometric air factor A_0=${round2(theoreticalAirRatio)} Nm3/unit."
        } else {
            "Assumes Natural Gas firing rate in Nm3/h (A_0=9.8 Nm3 air/Nm3 gas); supply A_0 explicitly before applying to kg/h jhut, coal, or HFO."
        }

        return CalculationExecutionResult(
            calculationId = "CALC-11",
            primaryOutputName = "Combustion Air Requirement (Normal 0°C)",
            primaryOutputValue = round2(normalAirNm3Min),
            primaryOutputUnit = "Nm3/min",
            secondaryOutputs = mapOf(
                "Hourly Combustion Air (Nm3/h)" to round2(normalAirNm3H),
                "FAD Airflow at ${round2(tAmbientC)}°C (m3/min)" to round2(actualAirFadM3Min),
                "FD Fan Shaft Power at FAD (kW)" to round2(fanPowerKw),
                "Recommended Motor Rating (kW)" to round2(fanPowerKw * 1.25)
            ),
            notesAndRecommendations = "$stoichSourceNote Primary flow is Normal (0°C, 101.325 kPa); actual FAD flow and fan shaft power are corrected to ${round2(tAmbientC)}°C intake air.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-12: Induced Draft (ID) Fan Sizing & Static Draught
     */
    fun executeCalc12(inputs: Map<String, Double>): CalculationExecutionResult {
        val flueGasFlowKgH = UnitConverters.validatePositiveFinite(inputs["m_flue"], "Flue Gas Mass Flow (kg/h)")
        val tFlue = UnitConverters.validatePositiveFinite(inputs["T_flue"], "Flue Gas Temperature (°C)")
        val deltaPTotalMmWc = UnitConverters.validatePositiveFinite(inputs["delta_P"], "Total System Draught Resistance (mm WC)")
        val fanEfficiency = inputs["eta_fan"] ?: 0.65

        require(fanEfficiency in 0.2..0.98) { "ID fan efficiency ($fanEfficiency) must be between 0.20 and 0.98." }

        // Flue gas density at operating temperature
        val rho0 = 1.293 // kg/Nm3 at 0°C
        val rhoFlue = rho0 * (273.15 / (tFlue + 273.15))
        val flueGasM3Min = (flueGasFlowKgH / rhoFlue) / 60.0

        val deltaPPascals = deltaPTotalMmWc * 9.80665
        val fanPowerKw = ((flueGasFlowKgH / 3600.0 / rhoFlue) * deltaPPascals) / (1000.0 * fanEfficiency)

        return CalculationExecutionResult(
            calculationId = "CALC-12",
            primaryOutputName = "ID Fan Volumetric Capacity",
            primaryOutputValue = round2(flueGasM3Min),
            primaryOutputUnit = "m3/min (Hot Gas)",
            secondaryOutputs = mapOf(
                "Flue Gas Operating Density (kg/m3)" to round3(rhoFlue),
                "ID Fan Shaft Power (kW)" to round2(fanPowerKw),
                "Recommended Motor Power (kW)" to round2(fanPowerKw * 1.25)
            ),
            notesAndRecommendations = "Sized to maintain slight negative draft (-2 to -5 mm WC) in the boiler furnace.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-13: Chimney Natural Stack Draught
     * Formula: ΔP = 353 * H * [ (1 / T_ambient) - (1 / T_stack) ]
     */
    fun executeCalc13(inputs: Map<String, Double>): CalculationExecutionResult {
        val heightMeters = UnitConverters.validatePositiveFinite(inputs["H"], "Chimney Height (m)")
        val tAmbientC = UnitConverters.validatePositiveFinite(inputs["T_ambient"], "Ambient Temperature (°C)")
        val tStackC = UnitConverters.validatePositiveFinite(inputs["T_stack"], "Average Stack Gas Temperature (°C)")

        require(tStackC > tAmbientC) { "Stack temperature ($tStackC°C) must exceed ambient ($tAmbientC°C)." }

        val tAmbientK = tAmbientC + 273.15
        val tStackK = tStackC + 273.15

        // Theoretical static natural draft in mm of water column
        val draftMmWc = 353.0 * heightMeters * ((1.0 / tAmbientK) - (1.0 / tStackK))
        val draftPascals = draftMmWc * 9.80665

        return CalculationExecutionResult(
            calculationId = "CALC-13",
            primaryOutputName = "Theoretical Natural Stack Draught",
            primaryOutputValue = round2(draftMmWc),
            primaryOutputUnit = "mm WC",
            secondaryOutputs = mapOf(
                "Natural Draught (Pascals)" to round2(draftPascals),
                "Chimney Height (m)" to heightMeters
            ),
            notesAndRecommendations = "Static draft created by buoyancy differential between hot chimney column and cooler ambient atmosphere.",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    /**
     * CALC-14: Steam Header & Main Pipe Sizing
     * Formula: d = sqrt[ (4 * m_s * v_g) / (π * v * 3600) ] * 1000
     */
    fun executeCalc14(inputs: Map<String, Double>): CalculationExecutionResult {
        val msKgH = UnitConverters.validatePositiveFinite(inputs["m_s"], "Steam Mass Flow (kg/h)")
        val pBarG = UnitConverters.validatePositiveFinite(inputs["P_steam"], "Steam Pressure (bar g)")
        val targetVelocityMs = inputs["velocity"] ?: 30.0 // Standard steam velocity 25–35 m/s

        require(targetVelocityMs in 10.0..60.0) { "Target velocity must be between 10 m/s and 60 m/s." }

        val props = IapwsIf97SteamEngine.getPropertiesByGaugePressure(pBarG)
        val vg = props.steamSpecificVolumeVgM3Kg

        // d = sqrt( (4 * Q) / (pi * v) )
        val qM3s = (msKgH * vg) / 3600.0
        val internalDiameterM = Math.sqrt((4.0 * qM3s) / (Math.PI * targetVelocityMs))
        val internalDiameterMm = internalDiameterM * 1000.0

        // Find nearest standard nominal pipe size (DN)
        val standardDns = listOf(25, 32, 40, 50, 65, 80, 100, 125, 150, 200, 250, 300)
        val recommendedDn = standardDns.firstOrNull { it >= internalDiameterMm } ?: 300

        val velocityAlert = when {
            targetVelocityMs > 35.0 -> " WARNING: Velocity >35 m/s increases piping erosion and noise at elbows/valves."
            targetVelocityMs < 15.0 -> " NOTE: Low velocity (<15 m/s) increases condensate settling; ensure drip legs and steam traps are spaced every 30–50 m."
            else -> ""
        }

        return CalculationExecutionResult(
            calculationId = "CALC-14",
            primaryOutputName = "Required Internal Diameter",
            primaryOutputValue = round2(internalDiameterMm),
            primaryOutputUnit = "mm",
            secondaryOutputs = mapOf(
                "Recommended Nominal Pipe Size (DN)" to recommendedDn.toDouble(),
                "Steam Specific Volume (m3/kg)" to round4(vg),
                "Volumetric Steam Flow (m3/h)" to round2(msKgH * vg)
            ),
            notesAndRecommendations = "PRELIMINARY SIZING: Velocity 25–35 m/s prevents noise, erosion, and excessive pressure drop. Confirm with complete isometric piping pressure drop analysis.$velocityAlert",
            status = CalculationStatus.IMPLEMENTED
        )
    }

    private fun round2(v: Double) = Math.round(v * 100.0) / 100.0
    private fun round3(v: Double) = Math.round(v * 1000.0) / 1000.0
    private fun round4(v: Double) = Math.round(v * 10000.0) / 10000.0
}
