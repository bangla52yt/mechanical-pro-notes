#!/usr/bin/env python3
"""
Seed all 20 Phase 06 Engineering Calculations and Variables into mechanical_pro_notes.db
"""

import sqlite3
import os

DB_PATH = "android/app/src/main/assets/database/mechanical_pro_notes.db"

CALCULATIONS = [
    # CALC-01
    ("CALC-01", "BOILER_DIRECT_EFFICIENCY", "Boiler Thermal Efficiency (Direct Method)", "বয়লার থার্মাল এফিসিয়েন্সি (ডাইরেক্ট মেথড)", "Boiler Thermal",
     r"\eta = \frac{m_s \cdot (h_s - h_w)}{m_f \cdot NCV} \times 100", "ASME PTC 4.1 / BS EN 12953 / IS 8753",
     "Steady-state boiler operation; verified steam and feedwater enthalpy from IAPWS-IF97.",
     "Requires accurate fuel mass/volume flow metering and laboratory-tested fuel NCV.", "IMPLEMENTED"),
    # CALC-02
    ("CALC-02", "BOILER_INDIRECT_EFFICIENCY", "Boiler Efficiency (Heat Loss / Indirect Method)", "বয়লার ইনডাইরেক্ট এফিসিয়েন্সি (হিট লস পদ্ধতি)", "Boiler Thermal",
     r"\eta = 100 - (L_{dry} + L_{H2} + L_{moist} + L_{air} + L_{unburnt} + L_{rad})", "ASME PTC 4-2013 / BS 845-1 / IS 8753",
     "Calibrated dry-flue gas analyzer (%CO2 = CO2_max*(20.95-%O2)/20.95); GCV in mass units (kJ/kg) or volumetric gas (kJ/Nm3 converted via fuel_density); Gas Siegert K=0.38 and gravimetric A0=17.0 kg/kg at 0.015 kg/kg air humidity.",
     "Natural gas (GCV in kJ/Nm3 + fuel_density or kJ/kg) uses verified BS 845-1 / IS 8753 heat-loss equations. Solid/mixed garment jhut (fuel_type=2) lacks fixed composition and remains UNVERIFIED unless lab-measured GCV (kJ/kg), H2%, siegert_k, theoretical_air, M_fuel, and L_unburnt are explicitly supplied.", "IMPLEMENTED"),
    # CALC-03
    ("CALC-03", "FUEL_FIRING_RATE", "Boiler Fuel Firing Rate Requirement", "বয়লার ফুয়েল ফায়ারিং রেট হিসাব", "Boiler Thermal",
     r"m_f = \frac{m_s \cdot (h_s - h_w)}{(\eta / 100) \cdot NCV}", "Plant Steam Demand Energy Balance Standard",
     "Thermal efficiency reflects target burner and boiler operating condition.",
     "Transient peak loads will demand momentary burner firing above nominal calculated rate.", "IMPLEMENTED"),
    # CALC-04
    ("CALC-04", "EVAPORATION_RATIO", "Steam-to-Fuel Evaporation Ratio", "স্টিম-টু-ফুয়েল ইভাপোরেশন রেশিও", "Boiler Thermal",
     r"ER = \frac{m_s}{m_f}", "Industrial Energy Audit & Plant Baseline Standard",
     "Simultaneous measurement of steam generation and fuel consumption over representative shift.",
     "Varies with feedwater temperature and steam operating pressure.", "IMPLEMENTED"),
    # CALC-05
    ("CALC-05", "BLOWDOWN_RATE", "Boiler Continuous Blowdown Rate", "বয়লার ব্লো-ডাউন রেট ক্যালকুলেশন", "Boiler Water",
     r"BD\% = \frac{TDS_{feed}}{TDS_{boiler} - TDS_{feed}} \times 100", "ABMA Water Quality Guidelines & BS 2486",
     "Complete mixing of boiler water; TDS salt mass balance in steady state.",
     "Does not replace periodic bottom manual blowdown for heavy sludge removal.", "IMPLEMENTED"),
    # CALC-06
    ("CALC-06", "BLOWDOWN_HEAT_RECOVERY", "Blowdown Flash Steam & Heat Recovery", "ব্লো-ডাউন হিট রিকভারি ও ফ্ল্যাশ স্টিম সাশ্রয়", "Boiler Thermal",
     r"Q_{rec} = m_{bd} \cdot (h_{f,boiler} - h_{f,drain}) \cdot \eta_{hex}", "Spirax Sarco Steam Engineering / Energy Efficiency Guide",
     "Continuous blowdown passes through dedicated flash vessel and plate heat exchanger.",
     "Intermittent bottom blowdown cannot be safely routed through standard recovery exchangers.", "IMPLEMENTED"),
    # CALC-07
    ("CALC-07", "PUMP_HYDRAULIC_POWER", "Boiler Feed Pump Hydraulic Power & Motor Rating", "বয়লার ফিড পাম্প হাইড্রোলিক পাওয়ার ও মোটর রেটিং", "Utility & Pumping",
     r"P_{hyd} = \frac{\rho \cdot g \cdot Q \cdot H}{3.6 \times 10^6}", "ISO 5199 / Hydraulic Institute Standards",
     "Multistage centrifugal feed pump handling hot water at 80°C–95°C.",
     "Check NPSH available to avoid cavitation during high-temperature pumping.", "IMPLEMENTED"),
    # CALC-08
    ("CALC-08", "DEAERATOR_STEAM_BALANCE", "Deaerator Steam Balance & Feedwater Heating", "ডিয়ারেশন স্টিম ব্যালেন্স ও ফিডওয়াটার হিটিং", "Boiler Thermal",
     r"m_{steam} = m_{feed} \cdot \frac{h_{feed} - h_{makeup}}{h_{steam} - h_{makeup}}", "ASME Deaerator Design Standard & Water Treatment Manual",
     "Deaerator operates at 0.2 bar g (105°C) to achieve <0.005 mg/L dissolved O2.",
     "Assumes uniform condensation without significant vent steam waste.", "IMPLEMENTED"),
    # CALC-09
    ("CALC-09", "SAFETY_VALVE_CAPACITY", "Safety Valve Relieving Capacity Verification", "সেফটি ভালভ রিলিভিং ক্যাপাসিটি ভেরিফিকেশন", "Boiler Safety",
     r"W = 0.51 \cdot K_d \cdot A \cdot (1.03 \cdot P_{set,abs})", "ASME BPVC Section I PG-67 to PG-73 / Boiler Rules 2025",
     "Full-lift spring loaded safety valve with 3% overpressure allowance.",
     "PRELIMINARY ESTIMATE ONLY: Does not replace statutory OCIB witness certification.", "IMPLEMENTED"),
    # CALC-10
    ("CALC-10", "ECONOMIZER_HEAT_RECOVERY", "Economizer Flue Gas Heat Recovery", "ইকোনোমাইজার ফ্লু গ্যাস হিট রিকভারি", "Boiler Thermal",
     r"Q_{eco} = m_{gas} \cdot C_{p,gas} \cdot (T_{in} - T_{out})", "Industrial Heat Exchanger & Boiler Efficiency Standards",
     "Counter-flow finned-tube economizer heat transfer without condensation.",
     "Flue gas outlet temp must not drop below acid dew point (~120°C for gas, ~140°C for sulfur).", "IMPLEMENTED"),
    # CALC-11
    ("CALC-11", "FD_FAN_AIRFLOW", "Forced Draft (FD) Fan & Combustion Air Sizing", "এফডি ফ্যান ও কম্বাশন এয়ার ফ্লো নির্ধারণ", "Combustion & Draught",
     r"V_{air} = V_{fuel} \cdot A_0 \cdot (1 + \frac{EA\%}{100})", "Combustion Engineering / Fan Application Manual",
     "Complete fuel combustion with 15% standard excess air for gas packaged burners.",
     "Requires air density adjustment for high ambient temperature or elevation.", "IMPLEMENTED"),
    # CALC-12
    ("CALC-12", "ID_FAN_DRAUGHT", "Induced Draft (ID) Fan Sizing & Static Head", "আইডি ফ্যান ড্রাফট ও মোটর রেটিং নির্ধারণ", "Combustion & Draught",
     r"P_{ID} = \frac{Q_{flue} \cdot \Delta P_{total}}{3600 \times 1000 \times \eta_{fan}}", "Industrial Boiler Draught System Engineering Standard",
     "ID fan maintains -2 to -5 mm WC negative pressure inside boiler furnace.",
     "Essential for solid-fuel (jhut/coal) boilers; not required for positive-pressure gas boilers.", "IMPLEMENTED"),
    # CALC-13
    ("CALC-13", "CHIMNEY_NATURAL_DRAUGHT", "Chimney Natural Stack Draught", "চিমনি ন্যাচারাল স্ট্যাক ড্রাফট হিসাব", "Combustion & Draught",
     r"\Delta P = 353 \cdot H \cdot \left( \frac{1}{T_{amb}} - \frac{1}{T_{flue}} \right)", "Barometric Stack Draught Thermodynamic Formulation",
     "Steady thermal column inside smooth vertical steel stack.",
     "Neglects stack wall friction and cold wind downdraught effects.", "IMPLEMENTED"),
    # CALC-14
    ("CALC-14", "STEAM_PIPE_SIZING", "Steam Header & Distribution Pipe Sizing", "স্টিম পাইপ ডায়ামিটার ও ভেলোসিটি হিসাব", "Steam Distribution",
     r"d = \sqrt{ \frac{4 \cdot m_s \cdot v_g}{\pi \cdot v \cdot 3600} } \times 1000", "Spirax Sarco Steam Sizing / ASME B31.1 Power Piping",
     "Saturated dry steam flow at standard velocity 25–35 m/s.",
     "PRELIMINARY SIZING: Verify line length and valve friction for total allowable pressure drop.", "IMPLEMENTED"),
    # CALC-15
    ("CALC-15", "V_BELT_DRIVE_SIZING", "V-Belt Sizing, Speed Ratio & Tensioning", "ভি-বেল্ট ড্রাইভ সাইজিং, স্পিড রেশিও ও টেনশনিং", "Wash Plant Machinery",
     r"L = 2C + \frac{\pi}{2}(D+d) + \frac{(D-d)^2}{4C}", "ISO 4184 / RMA Mechanical Drive Standards",
     "Standard wedge or classical V-belts on industrial washing machines and dryers.",
     "Manufacturer specific tension forces must be verified for exact belt profile.", "IMPLEMENTED"),
    # CALC-16
    ("CALC-16", "HYDRO_EXTRACTOR_G_FORCE", "Hydro Extractor Centrifugal G-Force & Rim Velocity", "হাইড্রো এক্সট্রাক্টর সেন্ট্রিফিউগাল জি-ফোর্স হিসাব", "Wash Plant Machinery",
     r"G = \frac{r \cdot \omega^2}{g} \approx 0.00001118 \cdot r_{cm} \cdot N^2", "Centrifugal Dewatering Dynamics & Machinery Safety Standards",
     "Rigid cylindrical basket rotating about true vertical spindle axis.",
     "CRITICAL SAFETY: Never exceed manufacturer nameplate rated maximum RPM.", "IMPLEMENTED"),
    # CALC-17
    ("CALC-17", "WASH_BATCH_STEAM_CONSUMPTION", "Washing Batch Steam Consumption & Thermal Balance", "ওয়াশ ব্যাচ স্টিম কনজাম্পশন ও থার্মাল ব্যালেন্স", "Wash Plant Utility",
     r"Q = (m_w C_{pw} + m_g C_{pg}) \cdot \Delta T \times 1.10", "Textile Wet Processing Thermal Energy Standards",
     "Direct steam sparging or jacketed steam coil in industrial garment washing machine.",
     "Assumes uniform liquor mixing and standard 10% thermal shell dissipation.", "IMPLEMENTED"),
    # CALC-18
    ("CALC-18", "DRYER_EVAPORATION_AIRFLOW", "Tumble Dryer Moisture Evaporation Rate & Airflow", "টাম্বল ড্রায়ার ময়েশ্চার ইভাপোরেশন রেট ও এয়ারফ্লো", "Wash Plant Machinery",
     r"m_{evap} = m_{wet} - \frac{m_{wet} \cdot (1 - MC_{in})}{1 - MC_{out}}", "Industrial Garment Drying Thermal Balance Guidelines",
     "Commercial textile drying drum at 65°C–80°C exhaust temperature.",
     "Fabric type, thickness, and weave density influence moisture release rate.", "IMPLEMENTED"),
    # CALC-19
    ("CALC-19", "WATER_SOFTENER_RESIN_CAPACITY", "Water Softener Resin Capacity & Salt Sizing", "ওয়াটার সফটনার রেজিন ক্যাপাসিটি ও লবণ হিসাব", "Water Treatment",
     r"V_{run} = \frac{V_{resin} \times Capacity_{resin} \times 50}{Hardness_{raw}}", "Industrial Ion Exchange Softening Standard (WQA / ASME)",
     "Strong acid cation resin operating with standard NaCl brine counter-current regeneration.",
     "Raw water iron, chlorine, or turbidity will reduce resin exchange capacity over time.", "IMPLEMENTED"),
    # CALC-20
    ("CALC-20", "COMPRESSED_AIR_PRESSURE_DROP", "Compressed Air Velocity & Line Pressure Drop", "কম্প্রেসড এয়ার ভেলোসিটি ও লাইন প্রেশার ড্রপ", "Pneumatics & Air",
     r"\Delta P = \frac{c \cdot L \cdot Q^{1.85}}{d^5 \cdot P_{abs}}", "Compressed Air and Gas Institute (CAGI) / ISO 8573",
     "Clean, dry compressed air distribution header in factory wash plant.",
     "Does not include individual pneumatic valve and filter-regulator-lubricator (FRL) losses.", "IMPLEMENTED")
]

VARIABLES = [
    # CALC-01
    ("CALC-01", "m_s", "Steam Flow Rate", "kg/h", 4000.0, 100.0, 50000.0, 1),
    ("CALC-01", "h_s", "Steam Enthalpy", "kJ/kg", 2778.0, 1000.0, 3500.0, 1),
    ("CALC-01", "h_w", "Feedwater Enthalpy", "kJ/kg", 335.0, 0.0, 1000.0, 1),
    ("CALC-01", "m_f", "Fuel Consumption", "unit/h", 320.0, 10.0, 10000.0, 1),
    ("CALC-01", "NCV", "Fuel Net Calorific Value", "kJ/unit", 35800.0, 5000.0, 100000.0, 1),

    # CALC-02 (5 required + 8 optional/governance)
    ("CALC-02", "T_flue", "Flue Gas Temperature", "°C", 185.0, 80.0, 450.0, 1),
    ("CALC-02", "T_ambient", "Ambient Air Temperature", "°C", 30.0, 5.0, 55.0, 1),
    ("CALC-02", "CO2", "Flue Gas Dry CO2", "%", 9.5, 1.0, 20.0, 1),
    ("CALC-02", "GCV", "Gross Calorific Value", "kJ/kg (or kJ/Nm3 if mode=2)", 39800.0, 8000.0, 120000.0, 1),
    ("CALC-02", "H2_percent", "Fuel Hydrogen Content (H2)", "%", 23.0, 0.0, 30.0, 1),
    ("CALC-02", "fuel_type", "Fuel Type (1=Gas, 2=Solid/Liquid)", "mode", 1.0, 1.0, 2.0, 0),
    ("CALC-02", "gcv_unit_mode", "GCV Basis (1=kJ/kg, 2=kJ/Nm3)", "mode", 2.0, 1.0, 2.0, 0),
    ("CALC-02", "fuel_density", "Gas Fuel Density (req if kJ/Nm3)", "kg/Nm3", 0.74, 0.55, 1.50, 0),
    ("CALC-02", "siegert_k", "Siegert K (blank=0.38 Gas; req Solid)", "factor", None, 0.25, 0.90, 0),
    ("CALC-02", "theoretical_air", "Stoichiometric Air A0 (req Solid)", "kg/kg", None, 2.0, 20.0, 0),
    ("CALC-02", "M_fuel", "Fuel Moisture Content", "%", None, 0.0, 70.0, 0),
    ("CALC-02", "L_unburnt", "Unburnt CO / Ash Carbon Loss", "%", None, 0.0, 25.0, 0),
    ("CALC-02", "L_radiation", "Surface Radiation & Convection Loss", "%", None, 0.0, 15.0, 0),

    # CALC-03
    ("CALC-03", "m_s", "Target Steam Load", "kg/h", 5000.0, 100.0, 50000.0, 1),
    ("CALC-03", "h_s", "Steam Enthalpy", "kJ/kg", 2778.0, 1500.0, 3500.0, 1),
    ("CALC-03", "h_w", "Feedwater Enthalpy", "kJ/kg", 377.0, 0.0, 1000.0, 1),
    ("CALC-03", "eta", "Boiler Thermal Efficiency", "%", 86.0, 20.0, 98.0, 1),
    ("CALC-03", "NCV", "Fuel Net Calorific Value", "kJ/unit", 35800.0, 5000.0, 100000.0, 1),

    # CALC-04
    ("CALC-04", "m_s", "Steam Generated", "kg", 25000.0, 100.0, 500000.0, 1),
    ("CALC-04", "m_f", "Fuel Consumed", "unit", 2000.0, 10.0, 50000.0, 1),

    # CALC-05
    ("CALC-05", "TDS_feed", "Feedwater TDS", "ppm", 150.0, 1.0, 2000.0, 1),
    ("CALC-05", "TDS_boiler", "Boiler Drum Max TDS Limit", "ppm", 3500.0, 500.0, 8000.0, 1),
    ("CALC-05", "m_s", "Steam Generation (Optional)", "kg/h", 4000.0, 0.0, 50000.0, 0),

    # CALC-06
    ("CALC-06", "m_bd", "Continuous Blowdown Rate", "kg/h", 250.0, 10.0, 5000.0, 1),
    ("CALC-06", "P_boiler", "Boiler Operating Pressure", "bar g", 8.0, 1.0, 20.0, 1),
    ("CALC-06", "P_flash", "Flash Tank Pressure", "bar g", 0.5, 0.0, 3.0, 1),
    ("CALC-06", "T_makeup", "Make-up Water Temperature", "°C", 25.0, 10.0, 60.0, 1),

    # CALC-07
    ("CALC-07", "Q", "Pump Flow Rate", "m3/h", 10.0, 0.5, 200.0, 1),
    ("CALC-07", "H", "Total Dynamic Head", "m", 120.0, 10.0, 600.0, 1),
    ("CALC-07", "density", "Water Density", "kg/m3", 960.0, 800.0, 1100.0, 1),
    ("CALC-07", "eta_pump", "Pump Efficiency", "ratio", 0.65, 0.1, 0.95, 1),

    # CALC-08
    ("CALC-08", "m_feed", "Feedwater Flow Rate", "kg/h", 6000.0, 100.0, 50000.0, 1),
    ("CALC-08", "T_makeup", "Inlet Soft Make-up Temp", "°C", 30.0, 5.0, 80.0, 1),
    ("CALC-08", "P_deaerator", "Deaerator Pressure", "bar g", 0.2, 0.0, 1.0, 1),
    ("CALC-08", "P_steam", "Supply Steam Pressure", "bar g", 7.0, 1.0, 20.0, 1),

    # CALC-09
    ("CALC-09", "P_set", "Safety Valve Set Pressure", "bar g", 10.0, 1.0, 30.0, 1),
    ("CALC-09", "Area", "Orifice Discharge Area", "mm2", 800.0, 50.0, 10000.0, 1),
    ("CALC-09", "Kd", "Derated Discharge Coefficient", "ratio", 0.62, 0.4, 0.9, 1),
    ("CALC-09", "MCR", "Boiler Max Evaporation (Optional)", "kg/h", 5000.0, 0.0, 50000.0, 0),

    # CALC-10
    ("CALC-10", "m_gas", "Flue Gas Mass Flow", "kg/h", 4500.0, 200.0, 50000.0, 1),
    ("CALC-10", "T_gas_in", "Flue Gas Inlet Temperature", "°C", 220.0, 150.0, 450.0, 1),
    ("CALC-10", "T_gas_out", "Flue Gas Outlet Temperature", "°C", 140.0, 110.0, 300.0, 1),
    ("CALC-10", "m_water", "Feedwater Flow Rate", "kg/h", 4000.0, 100.0, 50000.0, 1),
    ("CALC-10", "T_water_in", "Feedwater Inlet Temperature", "°C", 85.0, 20.0, 105.0, 1),

    # CALC-11
    ("CALC-11", "m_fuel", "Fuel Firing Rate", "Nm3/h or kg/h", 320.0, 10.0, 5000.0, 1),
    ("CALC-11", "EA_percent", "Excess Air Percentage", "%", 15.0, 5.0, 80.0, 1),
    ("CALC-11", "delta_P", "Total Static Head Resistance", "mm WC", 120.0, 20.0, 500.0, 1),
    ("CALC-11", "eta_fan", "Fan Static Efficiency", "ratio", 0.70, 0.4, 0.9, 1),

    # CALC-12
    ("CALC-12", "m_flue", "Flue Gas Mass Flow Rate", "kg/h", 5200.0, 200.0, 50000.0, 1),
    ("CALC-12", "T_flue", "Operating Flue Gas Temp", "°C", 160.0, 100.0, 350.0, 1),
    ("CALC-12", "delta_P", "Total System Draught Loss", "mm WC", 180.0, 20.0, 600.0, 1),
    ("CALC-12", "eta_fan", "Fan Static Efficiency", "ratio", 0.65, 0.4, 0.85, 1),

    # CALC-13
    ("CALC-13", "H", "Chimney Stack Height", "m", 30.0, 5.0, 120.0, 1),
    ("CALC-13", "T_ambient", "Ambient Air Temperature", "°C", 30.0, 0.0, 50.0, 1),
    ("CALC-13", "T_stack", "Average Stack Flue Gas Temp", "°C", 180.0, 80.0, 400.0, 1),

    # CALC-14
    ("CALC-14", "m_s", "Steam Mass Flow Rate", "kg/h", 3000.0, 50.0, 30000.0, 1),
    ("CALC-14", "P_steam", "Steam Line Pressure", "bar g", 7.0, 0.5, 20.0, 1),
    ("CALC-14", "velocity", "Target Steam Velocity", "m/s", 30.0, 15.0, 50.0, 1),

    # CALC-15
    ("CALC-15", "d_pulley", "Motor Driver Pulley Diameter", "mm", 160.0, 50.0, 800.0, 1),
    ("CALC-15", "D_pulley", "Driven Machine Pulley Diameter", "mm", 480.0, 80.0, 1500.0, 1),
    ("CALC-15", "C_center", "Center-to-Center Distance", "mm", 750.0, 200.0, 3000.0, 1),
    ("CALC-15", "N_driver", "Motor Driver Speed", "RPM", 1450.0, 300.0, 3600.0, 1),

    # CALC-16
    ("CALC-16", "r_basket", "Basket Drum Radius", "cm", 60.0, 15.0, 150.0, 1),
    ("CALC-16", "N_rpm", "Operating Drum Speed", "RPM", 850.0, 100.0, 2000.0, 1),
    ("CALC-16", "max_rated_rpm", "Nameplate Max Rated Speed", "RPM", 950.0, 200.0, 2500.0, 0),

    # CALC-17
    ("CALC-17", "m_water", "Wash Bath Water Volume", "kg", 800.0, 50.0, 5000.0, 1),
    ("CALC-17", "m_fabric", "Dry Garment Load", "kg", 120.0, 10.0, 1000.0, 1),
    ("CALC-17", "T_initial", "Initial Bath Water Temp", "°C", 25.0, 10.0, 60.0, 1),
    ("CALC-17", "T_target", "Target Process Temp", "°C", 70.0, 30.0, 95.0, 1),
    ("CALC-17", "P_steam", "Inlet Steam Pressure", "bar g", 3.5, 1.0, 10.0, 1),

    # CALC-18
    ("CALC-18", "m_wet", "Initial Wet Garment Load", "kg", 180.0, 20.0, 1000.0, 1),
    ("CALC-18", "MC_in", "Initial Moisture (% wet basis)", "%", 50.0, 15.0, 85.0, 1),
    ("CALC-18", "MC_out", "Target Final Moisture", "%", 5.0, 0.0, 20.0, 1),
    ("CALC-18", "cycle_time", "Drying Cycle Duration", "min", 35.0, 10.0, 120.0, 1),

    # CALC-19
    ("CALC-19", "hardness", "Raw Water Hardness (ppm as CaCO3)", "ppm", 220.0, 10.0, 1500.0, 1),
    ("CALC-19", "V_resin", "Cation Resin Volume", "L", 500.0, 25.0, 10000.0, 1),
    ("CALC-19", "capacity_eq", "Resin Operating Capacity", "eq/L", 1.3, 0.8, 2.0, 1),
    ("CALC-19", "salt_dosage", "Salt Dosage per Liter Resin", "g/L", 140.0, 80.0, 250.0, 1),

    # CALC-20
    ("CALC-20", "Q_cfm", "Compressed Air Demand", "CFM", 150.0, 10.0, 2000.0, 1),
    ("CALC-20", "d_pipe", "Pipe Internal Diameter", "mm", 50.0, 15.0, 200.0, 1),
    ("CALC-20", "L_pipe", "Total Pipe Run Length", "m", 60.0, 5.0, 500.0, 1),
    ("CALC-20", "P_air", "Supply Pressure", "bar g", 7.0, 2.0, 15.0, 1)
]

def seed():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    for calc in CALCULATIONS:
        cur.execute("""
            INSERT OR REPLACE INTO calculation (id, code, title, bengali_title, category, formula_latex, engineering_reference, assumptions, limitations, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, calc)

    cur.execute("DELETE FROM formula_variable")
    for var in VARIABLES:
        cur.execute("""
            INSERT INTO formula_variable (calculation_id, symbol, name, unit, default_value, min_bound, max_bound, is_required)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, var)

    conn.commit()
    count_calc = cur.execute("SELECT COUNT(*) FROM calculation").fetchone()[0]
    count_var = cur.execute("SELECT COUNT(*) FROM formula_variable").fetchone()[0]
    conn.close()

    print(f"Successfully seeded database: {count_calc} calculations, {count_var} formula variables.")

if __name__ == "__main__":
    seed()
