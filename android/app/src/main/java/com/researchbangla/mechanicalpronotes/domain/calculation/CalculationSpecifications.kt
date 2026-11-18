package com.researchbangla.mechanicalpronotes.domain.calculation

import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.model.FormulaVariable

/**
 * Authoritative Technical Catalog of all 20 Phase 06 Engineering Calculations.
 */
object CalculationSpecifications {

    val ALL_CALCULATIONS: List<CalculationDefinition> = listOf(
        // CALC-01
        CalculationDefinition(
            id = "CALC-01",
            code = "BOILER_DIRECT_EFFICIENCY",
            title = "Boiler Thermal Efficiency (Direct Method)",
            bengaliTitle = "বয়লার থার্মাল এফিসিয়েন্সি (ডাইরেক্ট মেথড)",
            category = "Boiler Thermal",
            formulaLatex = "\\eta = \\frac{m_s \\cdot (h_s - h_w)}{m_f \\cdot NCV} \\times 100",
            engineeringReference = "ASME PTC 4.1 / BS EN 12953 / IS 8753",
            assumptions = "Steady-state boiler operation; verified steam and feedwater enthalpy from IAPWS-IF97.",
            limitations = "Requires accurate fuel mass/volume flow metering and laboratory-tested fuel NCV.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(1, "CALC-01", "m_s", "Steam Flow Rate", "kg/h", 4000.0, 100.0, 50000.0),
                FormulaVariable(2, "CALC-01", "h_s", "Steam Enthalpy", "kJ/kg", 2778.0, 1000.0, 3500.0),
                FormulaVariable(3, "CALC-01", "h_w", "Feedwater Enthalpy", "kJ/kg", 335.0, 0.0, 1000.0),
                FormulaVariable(4, "CALC-01", "m_f", "Fuel Consumption", "unit/h", 320.0, 10.0, 10000.0),
                FormulaVariable(5, "CALC-01", "NCV", "Fuel Net Calorific Value", "kJ/unit", 35800.0, 5000.0, 100000.0)
            )
        ),
        // CALC-02
        CalculationDefinition(
            id = "CALC-02",
            code = "BOILER_INDIRECT_EFFICIENCY",
            title = "Boiler Efficiency (Heat Loss / Indirect Method)",
            bengaliTitle = "বয়লার ইনডাইরেক্ট এফিসিয়েন্সি (হিট লস পদ্ধতি)",
            category = "Boiler Thermal",
            formulaLatex = "\\eta = 100 - (L_{dry} + L_{H2} + L_{moist} + L_{air} + L_{unburnt} + L_{rad})",
            engineeringReference = "ASME PTC 4-2013 / BS 845-1 / IS 8753",
            assumptions = "Calibrated dry-flue gas analyzer (%CO2 = CO2_max*(20.95-%O2)/20.95); GCV in mass units (kJ/kg) or volumetric gas (kJ/Nm3 converted via fuel_density); Gas Siegert K=0.38 and gravimetric A0=17.0 kg/kg at 0.015 kg/kg air humidity.",
            limitations = "Natural gas (GCV in kJ/Nm3 + fuel_density or kJ/kg) uses verified BS 845-1 / IS 8753 heat-loss equations. Solid/mixed garment jhut (fuel_type=2) lacks fixed composition and remains UNVERIFIED unless lab-measured GCV (kJ/kg), H2%, siegert_k, theoretical_air, M_fuel, and L_unburnt are explicitly supplied.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(6, "CALC-02", "T_flue", "Flue Gas Temperature", "°C", 185.0, 80.0, 450.0, true),
                FormulaVariable(7, "CALC-02", "T_ambient", "Ambient Air Temperature", "°C", 30.0, 5.0, 55.0, true),
                FormulaVariable(8, "CALC-02", "CO2", "Flue Gas Dry CO2", "%", 9.5, 1.0, 20.0, true),
                FormulaVariable(9, "CALC-02", "GCV", "Gross Calorific Value", "kJ/kg (or kJ/Nm3 if mode=2)", 39800.0, 8000.0, 120000.0, true),
                FormulaVariable(10, "CALC-02", "H2_percent", "Fuel Hydrogen Content (H2)", "%", 23.0, 0.0, 30.0, true),
                FormulaVariable(80, "CALC-02", "fuel_type", "Fuel Type (1=Gas, 2=Solid/Liquid)", "mode", 1.0, 1.0, 2.0, false),
                FormulaVariable(81, "CALC-02", "gcv_unit_mode", "GCV Basis (1=kJ/kg, 2=kJ/Nm3)", "mode", 2.0, 1.0, 2.0, false),
                FormulaVariable(82, "CALC-02", "fuel_density", "Gas Fuel Density (req if kJ/Nm3)", "kg/Nm3", 0.74, 0.55, 1.50, false),
                FormulaVariable(83, "CALC-02", "siegert_k", "Siegert K (blank=0.38 Gas; req Solid)", "factor", null, 0.25, 0.90, false),
                FormulaVariable(84, "CALC-02", "theoretical_air", "Stoichiometric Air A0 (req Solid)", "kg/kg", null, 2.0, 20.0, false),
                FormulaVariable(85, "CALC-02", "M_fuel", "Fuel Moisture Content", "%", null, 0.0, 70.0, false),
                FormulaVariable(86, "CALC-02", "L_unburnt", "Unburnt CO / Ash Carbon Loss", "%", null, 0.0, 25.0, false),
                FormulaVariable(87, "CALC-02", "L_radiation", "Surface Radiation & Convection Loss", "%", null, 0.0, 15.0, false)
            )
        ),
        // CALC-03
        CalculationDefinition(
            id = "CALC-03",
            code = "FUEL_FIRING_RATE",
            title = "Boiler Fuel Firing Rate Requirement",
            bengaliTitle = "বয়লার ফুয়েল ফায়ারিং রেট হিসাব",
            category = "Boiler Thermal",
            formulaLatex = "m_f = \\frac{m_s \\cdot (h_s - h_w)}{(\\eta / 100) \\cdot NCV}",
            engineeringReference = "Plant Steam Demand Energy Balance Standard",
            assumptions = "Thermal efficiency reflects target burner and boiler operating condition.",
            limitations = "Transient peak loads will demand momentary burner firing above nominal calculated rate.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(21, "CALC-03", "m_s", "Target Steam Load", "kg/h", 5000.0, 100.0, 50000.0),
                FormulaVariable(22, "CALC-03", "h_s", "Steam Enthalpy", "kJ/kg", 2778.0, 1500.0, 3500.0),
                FormulaVariable(23, "CALC-03", "h_w", "Feedwater Enthalpy", "kJ/kg", 377.0, 0.0, 1000.0),
                FormulaVariable(24, "CALC-03", "eta", "Boiler Thermal Efficiency", "%", 86.0, 20.0, 98.0),
                FormulaVariable(25, "CALC-03", "NCV", "Fuel Net Calorific Value", "kJ/unit", 35800.0, 5000.0, 100000.0)
            )
        ),
        // CALC-04
        CalculationDefinition(
            id = "CALC-04",
            code = "EVAPORATION_RATIO",
            title = "Steam-to-Fuel Evaporation Ratio",
            bengaliTitle = "স্টিম-টু-ফুয়েল ইভাপোরেশন রেশিও",
            category = "Boiler Thermal",
            formulaLatex = "ER = \\frac{m_s}{m_f}",
            engineeringReference = "Industrial Energy Audit & Plant Baseline Standard",
            assumptions = "Simultaneous measurement of steam generation and fuel consumption over representative shift.",
            limitations = "Varies with feedwater temperature and steam operating pressure.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(31, "CALC-04", "m_s", "Steam Generated", "kg", 25000.0, 100.0, 500000.0),
                FormulaVariable(32, "CALC-04", "m_f", "Fuel Consumed", "unit", 2000.0, 10.0, 50000.0)
            )
        ),
        // CALC-05
        CalculationDefinition(
            id = "CALC-05",
            code = "BLOWDOWN_RATE",
            title = "Boiler Continuous Blowdown Rate",
            bengaliTitle = "বয়লার ব্লো-ডাউন রেট ক্যালকুলেশন",
            category = "Boiler Water",
            formulaLatex = "BD\\% = \\frac{TDS_{feed}}{TDS_{boiler} - TDS_{feed}} \\times 100",
            engineeringReference = "ABMA Water Quality Guidelines & BS 2486",
            assumptions = "Complete mixing of boiler water; TDS salt mass balance in steady state.",
            limitations = "Does not replace periodic bottom manual blowdown for heavy sludge removal.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(41, "CALC-05", "TDS_feed", "Feedwater TDS", "ppm", 150.0, 1.0, 2000.0),
                FormulaVariable(42, "CALC-05", "TDS_boiler", "Boiler Drum Max TDS Limit", "ppm", 3500.0, 500.0, 8000.0),
                FormulaVariable(43, "CALC-05", "m_s", "Steam Generation (Optional)", "kg/h", 4000.0, 0.0, 50000.0, false)
            )
        ),
        // CALC-06
        CalculationDefinition(
            id = "CALC-06",
            code = "BLOWDOWN_HEAT_RECOVERY",
            title = "Blowdown Flash Steam & Heat Recovery",
            bengaliTitle = "ব্লো-ডাউন হিট রিকভারি ও ফ্ল্যাশ স্টিম সাশ্রয়",
            category = "Boiler Thermal",
            formulaLatex = "Q_{rec} = m_{res} \\cdot (h_{f,flash} - h_{drain}) \\cdot \\eta_{hex}",
            engineeringReference = "Spirax Sarco Steam Engineering / Energy Efficiency Guide",
            assumptions = "Continuous blowdown with P_boiler > P_flash (up to 18.98 bar g steam table ceiling), default eta_hex=0.85, and 10°C cold-end HX approach above T_makeup.",
            limitations = "Rejects P_boiler <= P_flash or T_makeup + 10°C >= T_sat(P_flash). Intermittent bottom blowdown cannot be routed through standard recovery exchangers.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(51, "CALC-06", "m_bd", "Continuous Blowdown Rate", "kg/h", 250.0, 10.0, 5000.0),
                FormulaVariable(52, "CALC-06", "P_boiler", "Boiler Operating Pressure", "bar g", 8.0, 1.0, 18.98),
                FormulaVariable(53, "CALC-06", "P_flash", "Flash Tank Pressure", "bar g", 0.5, 0.0, 3.0),
                FormulaVariable(54, "CALC-06", "T_makeup", "Make-up Water Temperature", "°C", 25.0, 10.0, 60.0)
            )
        ),
        // CALC-07
        CalculationDefinition(
            id = "CALC-07",
            code = "PUMP_HYDRAULIC_POWER",
            title = "Boiler Feed Pump Hydraulic Power & Motor Rating",
            bengaliTitle = "বয়লার ফিড পাম্প হাইড্রোলিক পাওয়ার ও মোটর রেটিং",
            category = "Utility & Pumping",
            formulaLatex = "P_{hyd} = \\frac{\\rho \\cdot g \\cdot Q \\cdot H}{3.6 \\times 10^6}",
            engineeringReference = "ISO 5199 / Hydraulic Institute Standards",
            assumptions = "Multistage centrifugal feed pump handling hot water at 80°C–95°C.",
            limitations = "Check NPSH available to avoid cavitation during high-temperature pumping.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(61, "CALC-07", "Q", "Pump Flow Rate", "m3/h", 10.0, 0.5, 200.0),
                FormulaVariable(62, "CALC-07", "H", "Total Dynamic Head", "m", 120.0, 10.0, 600.0),
                FormulaVariable(63, "CALC-07", "density", "Water Density", "kg/m3", 960.0, 800.0, 1100.0),
                FormulaVariable(64, "CALC-07", "eta_pump", "Pump Efficiency", "ratio", 0.65, 0.1, 0.95)
            )
        ),
        // CALC-08
        CalculationDefinition(
            id = "CALC-08",
            code = "DEAERATOR_STEAM_BALANCE",
            title = "Deaerator Steam Balance & Feedwater Heating",
            bengaliTitle = "ডিয়ারেশন স্টিম ব্যালেন্স ও ফিডওয়াটার হিটিং",
            category = "Boiler Thermal",
            formulaLatex = "m_{steam} = m_{feed} \\cdot \\frac{h_{feed} - h_{makeup}}{h_{steam} - h_{makeup}}",
            engineeringReference = "ASME Deaerator Design Standard & Water Treatment Manual",
            assumptions = "Deaerator operates at 0.2 bar g (105°C) to achieve <0.005 mg/L dissolved O2.",
            limitations = "Assumes uniform condensation without significant vent steam waste.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(71, "CALC-08", "m_feed", "Feedwater Flow Rate", "kg/h", 6000.0, 100.0, 50000.0),
                FormulaVariable(72, "CALC-08", "T_makeup", "Inlet Soft Make-up Temp", "°C", 30.0, 5.0, 80.0),
                FormulaVariable(73, "CALC-08", "P_deaerator", "Deaerator Pressure", "bar g", 0.2, 0.0, 1.0),
                FormulaVariable(74, "CALC-08", "P_steam", "Supply Steam Pressure", "bar g", 7.0, 1.0, 20.0)
            )
        ),
        // CALC-09
        CalculationDefinition(
            id = "CALC-09",
            code = "SAFETY_VALVE_CAPACITY",
            title = "Safety Valve Relieving Capacity Verification",
            bengaliTitle = "সেফটি ভালভ রিলিভিং ক্যাপাসিটি ভেরিফিকেশন",
            category = "Boiler Safety",
            formulaLatex = "W = 0.51 \\cdot K_d \\cdot A \\cdot (1.03 \\cdot P_{set,abs})",
            engineeringReference = "ASME BPVC Section I PG-67 to PG-73 / Boiler Rules 2025",
            assumptions = "Full-lift spring loaded safety valve, dry saturated steam (K_sh=1.0), open discharge (K_b=1.0), 3% overpressure, P_set in [0.5, 35.0] bar g, conservative 0.51 Napier constant (-2.89% vs 0.52515).",
            limitations = "PRELIMINARY ESTIMATE ONLY: Does not replace statutory OCIB witness certification or manufacturer stamped Kd and orifice/curtain flow area.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(81, "CALC-09", "P_set", "Safety Valve Set Pressure", "bar g", 10.0, 1.0, 30.0),
                FormulaVariable(82, "CALC-09", "Area", "Orifice Discharge Area", "mm2", 800.0, 50.0, 10000.0),
                FormulaVariable(83, "CALC-09", "Kd", "Derated Discharge Coefficient", "ratio", 0.62, 0.4, 0.9),
                FormulaVariable(84, "CALC-09", "MCR", "Boiler Max Evaporation (Optional)", "kg/h", 5000.0, 0.0, 50000.0, false)
            )
        ),
        // CALC-10
        CalculationDefinition(
            id = "CALC-10",
            code = "ECONOMIZER_HEAT_RECOVERY",
            title = "Economizer Flue Gas Heat Recovery",
            bengaliTitle = "ইকোনোমাইজার ফ্লু গ্যাস হিট রিকভারি",
            category = "Boiler Thermal",
            formulaLatex = "Q_{eco} = m_{gas} \\cdot C_{p,gas} \\cdot (T_{in} - T_{out})",
            engineeringReference = "Industrial Heat Exchanger & Boiler Efficiency Standards",
            assumptions = "Counter-flow finned-tube economizer heat transfer without condensation.",
            limitations = "Flue gas outlet temp must not drop below acid dew point (~120°C for gas, ~140°C for sulfur).",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(91, "CALC-10", "m_gas", "Flue Gas Mass Flow", "kg/h", 4500.0, 200.0, 50000.0),
                FormulaVariable(92, "CALC-10", "T_gas_in", "Flue Gas Inlet Temperature", "°C", 220.0, 150.0, 450.0),
                FormulaVariable(93, "CALC-10", "T_gas_out", "Flue Gas Outlet Temperature", "°C", 140.0, 110.0, 300.0),
                FormulaVariable(94, "CALC-10", "m_water", "Feedwater Flow Rate", "kg/h", 4000.0, 100.0, 50000.0),
                FormulaVariable(95, "CALC-10", "T_water_in", "Feedwater Inlet Temperature", "°C", 85.0, 20.0, 105.0)
            )
        ),
        // CALC-11
        CalculationDefinition(
            id = "CALC-11",
            code = "FD_FAN_AIRFLOW",
            title = "Forced Draft (FD) Fan & Combustion Air Sizing",
            bengaliTitle = "এফডি ফ্যান ও কম্বাশন এয়ার ফ্লো নির্ধারণ",
            category = "Combustion & Draught",
            formulaLatex = "V_{air,N} = V_{fuel} \\cdot A_0 \\cdot (1 + \\frac{EA\\%}{100}),\\quad Q_{FAD} = V_{air,N} \\cdot \\frac{T_{amb}+273.15}{273.15}",
            engineeringReference = "Combustion Engineering / Fan Application Manual",
            assumptions = "Default stoichiometric air A_0=9.8 Nm3 air/Nm3 gas applies ONLY to pipeline Natural Gas (Nm3/h); primary output is Normal (0°C, 1 atm) and fan shaft power uses 30°C FAD volume.",
            limitations = "Do NOT enter kg/h solid/liquid fuel (jhut, coal, HFO) under default A_0=9.8; supply explicit fuel-specific stoichiometric ratio A_0 (Nm3/kg).",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(101, "CALC-11", "m_fuel", "Natural Gas Firing Rate (Default A0=9.8)", "Nm3/h", 320.0, 10.0, 5000.0),
                FormulaVariable(102, "CALC-11", "EA_percent", "Excess Air Percentage", "%", 15.0, 5.0, 80.0),
                FormulaVariable(103, "CALC-11", "delta_P", "Total Static Head Resistance", "mm WC", 120.0, 20.0, 500.0),
                FormulaVariable(104, "CALC-11", "eta_fan", "Fan Static Efficiency", "ratio", 0.70, 0.4, 0.9)
            )
        ),
        // CALC-12
        CalculationDefinition(
            id = "CALC-12",
            code = "ID_FAN_DRAUGHT",
            title = "Induced Draft (ID) Fan Sizing & Static Head",
            bengaliTitle = "আইডি ফ্যান ড্রাফট ও মোটর রেটিং নির্ধারণ",
            category = "Combustion & Draught",
            formulaLatex = "P_{ID} = \\frac{Q_{flue} \\cdot \\Delta P_{total}}{3600 \\times 1000 \\times \\eta_{fan}}",
            engineeringReference = "Industrial Boiler Draught System Engineering Standard",
            assumptions = "ID fan maintains -2 to -5 mm WC negative pressure inside boiler furnace.",
            limitations = "Essential for solid-fuel (jhut/coal) boilers; not required for positive-pressure gas boilers.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(111, "CALC-12", "m_flue", "Flue Gas Mass Flow Rate", "kg/h", 5200.0, 200.0, 50000.0),
                FormulaVariable(112, "CALC-12", "T_flue", "Operating Flue Gas Temp", "°C", 160.0, 100.0, 350.0),
                FormulaVariable(113, "CALC-12", "delta_P", "Total System Draught Loss", "mm WC", 180.0, 20.0, 600.0),
                FormulaVariable(114, "CALC-12", "eta_fan", "Fan Static Efficiency", "ratio", 0.65, 0.4, 0.85)
            )
        ),
        // CALC-13
        CalculationDefinition(
            id = "CALC-13",
            code = "CHIMNEY_NATURAL_DRAUGHT",
            title = "Chimney Natural Stack Draught",
            bengaliTitle = "চিমনি ন্যাচারাল স্ট্যাক ড্রাফট হিসাব",
            category = "Combustion & Draught",
            formulaLatex = "\\Delta P = 353 \\cdot H \\cdot \\left( \\frac{1}{T_{amb}} - \\frac{1}{T_{flue}} \\right)",
            engineeringReference = "Barometric Stack Draught Thermodynamic Formulation",
            assumptions = "Steady thermal column inside smooth vertical steel stack.",
            limitations = "Neglects stack wall friction and cold wind downdraught effects.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(121, "CALC-13", "H", "Chimney Stack Height", "m", 30.0, 5.0, 120.0),
                FormulaVariable(122, "CALC-13", "T_ambient", "Ambient Air Temperature", "°C", 30.0, 0.0, 50.0),
                FormulaVariable(123, "CALC-13", "T_stack", "Average Stack Flue Gas Temp", "°C", 180.0, 80.0, 400.0)
            )
        ),
        // CALC-14
        CalculationDefinition(
            id = "CALC-14",
            code = "STEAM_PIPE_SIZING",
            title = "Steam Header & Distribution Pipe Sizing",
            bengaliTitle = "স্টিম পাইপ ডায়ামিটার ও ভেলোসিটি হিসাব",
            category = "Steam Distribution",
            formulaLatex = "d = \\sqrt{ \\frac{4 \\cdot m_s \\cdot v_g}{\\pi \\cdot v \\cdot 3600} } \\times 1000",
            engineeringReference = "Spirax Sarco Steam Sizing / ASME B31.1 Power Piping",
            assumptions = "Saturated dry steam flow at standard velocity 25–35 m/s.",
            limitations = "PRELIMINARY SIZING: Verify line length and valve friction for total allowable pressure drop.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(131, "CALC-14", "m_s", "Steam Mass Flow Rate", "kg/h", 3000.0, 50.0, 30000.0),
                FormulaVariable(132, "CALC-14", "P_steam", "Steam Line Pressure", "bar g", 7.0, 0.5, 20.0),
                FormulaVariable(133, "CALC-14", "velocity", "Target Steam Velocity", "m/s", 30.0, 15.0, 50.0)
            )
        ),
        // CALC-15
        CalculationDefinition(
            id = "CALC-15",
            code = "V_BELT_DRIVE_SIZING",
            title = "V-Belt Sizing, Speed Ratio & Tensioning",
            bengaliTitle = "ভি-বেল্ট ড্রাইভ সাইজিং, স্পিড রেশিও ও টেনশনিং",
            category = "Wash Plant Machinery",
            formulaLatex = "L = 2C + \\frac{\\pi}{2}(D+d) + \\frac{(D-d)^2}{4C}",
            engineeringReference = "ISO 4184 / RMA Mechanical Drive Standards",
            assumptions = "Standard wedge or classical V-belts on industrial washing machines and dryers.",
            limitations = "Manufacturer specific tension forces must be verified for exact belt profile.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(141, "CALC-15", "d_pulley", "Motor Driver Pulley Diameter", "mm", 160.0, 50.0, 800.0),
                FormulaVariable(142, "CALC-15", "D_pulley", "Driven Machine Pulley Diameter", "mm", 480.0, 80.0, 1500.0),
                FormulaVariable(143, "CALC-15", "C_center", "Center-to-Center Distance", "mm", 750.0, 200.0, 3000.0),
                FormulaVariable(144, "CALC-15", "N_driver", "Motor Driver Speed", "RPM", 1450.0, 300.0, 3600.0)
            )
        ),
        // CALC-16
        CalculationDefinition(
            id = "CALC-16",
            code = "HYDRO_EXTRACTOR_G_FORCE",
            title = "Hydro Extractor Centrifugal G-Force & Rim Velocity",
            bengaliTitle = "হাইড্রো এক্সট্রাক্টর সেন্ট্রিফিউগাল জি-ফোর্স হিসাব",
            category = "Wash Plant Machinery",
            formulaLatex = "G = \\frac{r \\cdot \\omega^2}{g} \\approx 0.00001118 \\cdot r_{cm} \\cdot N^2",
            engineeringReference = "Centrifugal Dewatering Dynamics & Machinery Safety Standards",
            assumptions = "Rigid cylindrical basket rotating about true vertical spindle axis.",
            limitations = "CRITICAL SAFETY: Never exceed manufacturer nameplate rated maximum RPM.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(151, "CALC-16", "r_basket", "Basket Drum Radius", "cm", 60.0, 15.0, 150.0),
                FormulaVariable(152, "CALC-16", "N_rpm", "Operating Drum Speed", "RPM", 850.0, 100.0, 2000.0),
                FormulaVariable(153, "CALC-16", "max_rated_rpm", "Nameplate Max Rated Speed", "RPM", 950.0, 200.0, 2500.0, false)
            )
        ),
        // CALC-17
        CalculationDefinition(
            id = "CALC-17",
            code = "WASH_BATCH_STEAM_CONSUMPTION",
            title = "Washing Batch Steam Consumption & Thermal Balance",
            bengaliTitle = "ওয়াশ ব্যাচ স্টিম কনজাম্পশন ও থার্মাল ব্যালেন্স",
            category = "Wash Plant Utility",
            formulaLatex = "Q = (m_w C_{pw} + m_g C_{pg}) \\cdot \\Delta T \\times 1.10",
            engineeringReference = "Textile Wet Processing Thermal Energy Standards",
            assumptions = "Direct steam sparging or jacketed steam coil in industrial garment washing machine.",
            limitations = "Assumes uniform liquor mixing and standard 10% thermal shell dissipation.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(161, "CALC-17", "m_water", "Wash Bath Water Volume", "kg", 800.0, 50.0, 5000.0),
                FormulaVariable(162, "CALC-17", "m_fabric", "Dry Garment Load", "kg", 120.0, 10.0, 1000.0),
                FormulaVariable(163, "CALC-17", "T_initial", "Initial Bath Water Temp", "°C", 25.0, 10.0, 60.0),
                FormulaVariable(164, "CALC-17", "T_target", "Target Process Temp", "°C", 70.0, 30.0, 95.0),
                FormulaVariable(165, "CALC-17", "P_steam", "Inlet Steam Pressure", "bar g", 3.5, 1.0, 10.0)
            )
        ),
        // CALC-18
        CalculationDefinition(
            id = "CALC-18",
            code = "DRYER_EVAPORATION_AIRFLOW",
            title = "Tumble Dryer Moisture Evaporation Rate & Airflow",
            bengaliTitle = "টাম্বল ড্রায়ার ময়েশ্চার ইভাপোরেশন রেট ও এয়ারফ্লো",
            category = "Wash Plant Machinery",
            formulaLatex = "m_{evap} = m_{wet} - \\frac{m_{wet} \\cdot (1 - MC_{in})}{1 - MC_{out}}",
            engineeringReference = "Industrial Garment Drying Thermal Balance Guidelines",
            assumptions = "Commercial textile drying drum at 65°C–80°C exhaust temperature.",
            limitations = "Fabric type, thickness, and weave density influence moisture release rate.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(171, "CALC-18", "m_wet", "Initial Wet Garment Load", "kg", 180.0, 20.0, 1000.0),
                FormulaVariable(172, "CALC-18", "MC_in", "Initial Moisture (% wet basis)", "%", 50.0, 15.0, 85.0),
                FormulaVariable(173, "CALC-18", "MC_out", "Target Final Moisture", "%", 5.0, 0.0, 20.0),
                FormulaVariable(174, "CALC-18", "cycle_time", "Drying Cycle Duration", "min", 35.0, 10.0, 120.0)
            )
        ),
        // CALC-19
        CalculationDefinition(
            id = "CALC-19",
            code = "WATER_SOFTENER_RESIN_CAPACITY",
            title = "Water Softener Resin Capacity & Salt Sizing",
            bengaliTitle = "ওয়াটার সফটনার রেজিন ক্যাপাসিটি ও লবণ হিসাব",
            category = "Water Treatment",
            formulaLatex = "V_{run} = \\frac{V_{resin} \\times Capacity_{resin} \\times 50}{Hardness_{raw}}",
            engineeringReference = "Industrial Ion Exchange Softening Standard (WQA / ASME)",
            assumptions = "Strong acid cation resin (50 g CaCO3/eq); reports per-regeneration salt (kg/cycle). Daily salt requires explicit regen_per_day or daily_water_m3.",
            limitations = "Raw water iron, chlorine, or turbidity will reduce resin exchange capacity; no assumed 1-regen/day schedule when frequency is omitted.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(181, "CALC-19", "hardness", "Raw Water Hardness (ppm as CaCO3)", "ppm", 220.0, 10.0, 1500.0),
                FormulaVariable(182, "CALC-19", "V_resin", "Cation Resin Volume", "L", 500.0, 25.0, 10000.0),
                FormulaVariable(183, "CALC-19", "capacity_eq", "Resin Operating Capacity", "eq/L", 1.3, 0.8, 2.0),
                FormulaVariable(184, "CALC-19", "salt_dosage", "Salt Dosage per Liter Resin", "g/L", 140.0, 80.0, 250.0)
            )
        ),
        // CALC-20
        CalculationDefinition(
            id = "CALC-20",
            code = "COMPRESSED_AIR_PRESSURE_DROP",
            title = "Compressed Air Velocity & Line Pressure Drop",
            bengaliTitle = "কম্প্রেসড এয়ার ভেলোসিটি ও লাইন প্রেশার ড্রপ",
            category = "Pneumatics & Air",
            formulaLatex = "\\Delta P = \\frac{0.00075 \\cdot L \\cdot Q_{CFM}^{1.85}}{d_{cm}^5 \\cdot P_{abs}}",
            engineeringReference = "CAGI / Smooth-Bore Darcy-Weisbach Comparison (Preliminary)",
            assumptions = "PRELIMINARY SIZING: Empirical c=0.00075 matches smooth drawn aluminum/copper pipe at ~20°C isothermal dry air, straight equivalent length only.",
            limitations = "Underpredicts rough commercial galvanized steel (eps=0.045 mm Darcy-Weisbach) and Atlas Copco 450 by ~26-33% (~1.35x-1.50x higher drop in steel); omits elbows/valves/FRLs.",
            status = CalculationStatus.IMPLEMENTED,
            variables = listOf(
                FormulaVariable(191, "CALC-20", "Q_cfm", "Compressed Air Demand", "CFM", 150.0, 10.0, 2000.0),
                FormulaVariable(192, "CALC-20", "d_pipe", "Pipe Internal Diameter", "mm", 50.0, 15.0, 200.0),
                FormulaVariable(193, "CALC-20", "L_pipe", "Total Pipe Run Length", "m", 60.0, 5.0, 500.0),
                FormulaVariable(194, "CALC-20", "P_air", "Supply Pressure", "bar g", 7.0, 2.0, 15.0)
            )
        )
    )

    fun getCalculationById(id: String): CalculationDefinition? {
        return ALL_CALCULATIONS.find { it.id.equals(id, ignoreCase = true) }
    }
}
