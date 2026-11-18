# PHASE 06 PREPARATION & EXECUTION SCOPE

**Document Identifier:** DOC-MPN-013  
**Project:** Mechanical Pro Notes (Personal Offline-First Reference App)  
**Phase:** Phase 06 — Field Calculations & Maintenance Tools  
**Status:** SCOPE PREPARED — AWAITING EXPLICIT OWNER EXECUTION AUTHORIZATION  
**Date:** 12 November 2026  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]  

---

## 1. Executive Context & Scope Mandate

The Project Owner has formally accepted Phase 05 as **PASS WITH CONDITIONS**. In accordance with the Owner's instructions:
1. **Zero Premature Implementation:** This document establishes the technical, mathematical, and architectural execution scope for Phase 06. **No implementation code will be written until the owner provides separate explicit authorization.**
2. **Carried-Forward Limitations:** The absence of native Android build tools (OpenJDK/Android SDK) in the Gemini Linux Web workspace container is preserved and recognized. Verification will utilize static analysis, domain boundary testing, and Python/SQLite integration harnesses without making false claims of native APK compilation.
3. **Core Principles Preserved:** Strict offline operation (**DEC-016** zero network permissions), free-first policy (**DEC-002** zero cost), personal data isolation (**DEC-014** zero cascade risk), and personal-use focus without public release or monetization mechanisms.

---

## 2. Phase 06 Objective

Phase 06 expands Mechanical Pro Notes from an equipment and regulatory reference catalog into a practical, on-the-floor **mathematical diagnostic and energy-auditing engine**. It implements the full suite of validated industrial boiler and wash plant maintenance calculations, offline thermodynamic steam property lookups, and mechanical diagnostic tools.

---

## 3. Comprehensive Calculation Suite Specification

### 3.1 Boiler Thermal & Utility Calculations (14 Algorithms)

| Calculation ID | Formula Title | Engineering Scope & Governing Standard | Input Parameters | Primary Output & Units | Status |
|---|---|---|---|---|---|
| **CALC-01** | Direct Boiler Thermal Efficiency | Input-Output Method (ASME PTC 4 / BS EN 12953) | Steam flow ($m_s$), steam enthalpy ($h_s$), feedwater enthalpy ($h_w$), fuel flow ($m_f$), fuel NCV | Thermal Efficiency ($\eta, \%$) | Core Implemented; verify UI binding |
| **CALC-02** | Indirect Thermal Efficiency (Heat Loss) | ASME PTC 4 / ISO 50002 Energy Audit Standards | Flue gas temp ($T_g$), ambient temp ($T_a$), $\%CO_2$ or $\%O_2$, fuel analysis, radiation factor | Efficiency ($\eta, \%$) & individual heat loss breakdown | Scope for Phase 06 |
| **CALC-03** | Required Fuel Firing Rate | Plant Steam Demand Energy Balance | Required steam load ($m_s$), operating pressure, feedwater temp, fuel LCV, boiler efficiency | Fuel consumption rate ($m^3/h$ gas or $kg/h$ jhut) | Scope for Phase 06 |
| **CALC-04** | Steam Evaporation Ratio | Steam-to-Fuel Ratio (Energy Audit Baseline) | Steam generated ($m_s$), fuel consumed ($m_f$) | Evaporation Ratio ($kg\text{ steam}/m^3\text{ gas}$ or $kg/kg$) | Core Implemented; verify UI binding |
| **CALC-05** | Surface Continuous Blowdown Rate | Water Chemistry Salt Mass Balance | Feedwater TDS ($ppm$), Boiler Drum Max Allowable TDS ($ppm$) | Blowdown Rate ($B, \%$ of feedwater) | Core Implemented; verify UI binding |
| **CALC-06** | Blowdown Heat Recovery Savings | Flash Steam & Heat Exchanger Energy Balance | Blowdown flow ($kg/h$), boiler pressure ($bar$), make-up water temp, fuel cost | Heat recovered ($kW$) & monetary fuel savings/year | Scope for Phase 06 |
| **CALC-07** | Feedwater Pump Hydraulic Power & Motor | ISO 5199 / Hydraulic Institute Standards | Feedwater flow ($Q$), total dynamic head ($H$), water density, pump & motor efficiency | Motor Power Rating ($kW / HP$) | Core Implemented; verify UI binding |
| **CALC-08** | Deaerator Steam Consumption Balance | Thermal Mass & Energy Balance | Feedwater make-up temp, condensate return flow & temp, deaerator operating pressure | Deaerator heating steam requirement ($kg/h$) | Scope for Phase 06 |
| **CALC-09** | Safety Valve Relieving Capacity Verification | ASME Section I PG-67 to PG-73 & OCIB 2025 Rules | Maximum steaming capacity, valve bore diameter, set pressure, overpressure allowance | Minimum required relieving capacity ($kg/h$) & pass/fail audit | Scope for Phase 06 |
| **CALC-10** | Economizer Flue Gas Heat Recovery | Convective Heat Exchanger Thermodynamics | Flue gas flow, inlet/outlet gas temp, feedwater flow rate | Water temperature rise ($\Delta T$) & boiler efficiency gain ($\%$) | Scope for Phase 06 |
| **CALC-11** | Combustion Air Requirement & FD Fan Sizing | Stoichiometric Combustion + Excess Air ($EA\%$) | Fuel composition/type, fuel firing rate, excess air percentage, static pressure drop | Forced Draft Fan flow rate ($m^3/min$) & motor power ($kW$) | Scope for Phase 06 |
| **CALC-12** | Induced Draft (ID) Fan Sizing & Static Head | Flue Gas Flow & System Draught Balance | Flue gas mass flow, flue gas temp, density, total boiler/economizer/duct resistance | ID Fan Capacity ($m^3/min$) & static pressure ($mm\text{ WC}$) | Scope for Phase 06 |
| **CALC-13** | Natural Stack / Chimney Draught | Density Differential (Barometric Equation) | Ambient temperature ($T_a$), chimney average flue gas temp ($T_g$), stack height ($H$) | Theoretical Natural Draught ($mm\text{ WC} / Pa$) | Scope for Phase 06 |
| **CALC-14** | Steam Header & Main Pipe Sizing | Steam Velocity & Specific Volume Thermodynamics | Steam mass flow ($kg/h$), line pressure ($bar\text{ g}$), target velocity ($25\text{–}35\text{ m/s}$) | Internal Pipe Diameter ($mm$), recommended nominal bore | Scope for Phase 06 |

---

### 3.2 Textile Wash Plant Maintenance & Utility Tools (6 Algorithms)

| Calculation ID | Formula Title | Engineering Scope | Primary Output |
|---|---|---|---|
| **CALC-15** | Industrial Washer V-Belt Sizing & Tension | ISO 4184 / Rubber Manufacturers Association | Pitch length ($L_p$), center distance, strand deflection force ($N$) |
| **CALC-16** | Hydro Extractor G-Force & Critical RPM | Centrifugal Acceleration ($G = r \omega^2 / g$) | Centrifugal G-factor, rim speed ($m/s$), dynamic load |
| **CALC-17** | Washing Batch Steam Consumption | Thermal Liquor Ratio Heating ($Q = m C_p \Delta T$) | Total batch steam requirement ($kg$) & heating cycle duration |
| **CALC-18** | Tumble Dryer Evaporation & Airflow | Moisture Mass Balance & Psychrometric Air Volume | Evaporative moisture rate ($kg/h$), burner duty, exhaust CFM |
| **CALC-19** | Water Softener Ion-Exchange Capacity | Resin Capacity ($eq/L$) & Raw Water Hardness | Soft water cycle yield ($m^3$), regeneration salt ($NaCl, kg$) |
| **CALC-20** | Compressed Air CFM & Line Pressure Drop | Darcy-Weisbach Compressible Fluid Flow | Air receiver capacity ($m^3$), distribution line pressure drop ($bar$) |

---

## 4. Thermodynamic Steam Table & Engineering Reference Engines

Phase 06 will integrate an offline, high-precision thermodynamic saturated steam properties engine covering the typical wash plant boiler envelope ($0.5\text{ bar g}$ to $20\text{ bar g}$):
1. **Saturation Temperature ($T_{sat}, ^\circ C$):** Accurate polynomial approximation based on IAPWS-IF97.
2. **Specific Volume of Saturated Steam ($v_g, m^3/kg$):** For exact steam velocity and pipe sizing.
3. **Specific Enthalpy of Liquid ($h_f, kJ/kg$):** Sensible heat of water at saturation.
4. **Latent Heat of Vaporization ($h_{fg}, kJ/kg$):** Heat available for industrial garment heating/washing processes.
5. **Specific Enthalpy of Saturated Vapor ($h_g, kJ/kg$):** Total energy of dry saturated steam.

---

## 5. Architectural Implementation Plan (For Authorized Execution)

When authorized, implementation will follow the approved Clean Architecture + MVVM + UDF pattern (**DEC-013**):
1. **Domain Layer:**
   - Enhance `CalculationModels.kt` with formula variable definitions, SI unit conversion helpers, and strict physical validity assertions (`validatePositiveFinite`, `validateRange`).
   - Implement `SteamTableEngine.kt` containing deterministic IAPWS-IF97 formulation without external API calls.
2. **Data Layer:**
   - Expand `CalculationRepositoryImpl.kt` to execute the full portfolio of 20 algorithms.
   - Seed pre-packaged calculation definitions and variable bounds into Room SQLite database.
3. **Presentation Layer:**
   - Enhance `CalculationRunnerScreen.kt` with live unit toggles (Metric / Imperial / Plant floor units like $bar\text{ vs }kg/cm^2$, $kg/h\text{ vs }lbs/h$).
   - Create dedicated `SteamTableScreen.kt` for one-tap pressure-to-enthalpy lookup on the factory floor.
   - Provide direct integration with `NotesScreen.kt` allowing calculated energy metrics to be saved directly to equipment logs.
4. **Verification & Quality Assurance:**
   - Create automated Python/SQLite verification suite `test_suite_phase06.py` asserting mathematical precision, physical boundary enforcement, and personal data isolation.

---

## 6. Execution Pre-conditions & Halt Directive

- **PRE-CONDITION CHECK:** Phase 05 conditionally accepted.
- **HALT DIRECTIVE:** In strict adherence to the project owner's instruction, **NO PHASE 06 IMPLEMENTATION CODE HAS BEEN CREATED OR ALTERED**.
- **AWAITING OWNER COMMAND:** The assistant is standing by at the execution boundary awaiting explicit owner instruction to initiate Phase 06 coding.
