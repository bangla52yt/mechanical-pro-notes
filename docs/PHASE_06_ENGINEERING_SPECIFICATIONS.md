# PHASE 06 ENGINEERING SPECIFICATIONS
## Comprehensive Field Calculations, Boiler Thermal Engineering & Wash Plant Maintenance Diagnostic Suite

**Document Identifier:** DOC-MPN-014  
**Project:** Mechanical Pro Notes (Personal Offline-First Mechanical Reference Android App)  
**Phase:** Phase 06 — Field Calculations, Boiler Thermal Engineering & Maintenance Tools  
**Status:** IMPLEMENTED & VERIFIED  
**Date:** 12 November 2026  
**Governing Standard:** ASME PTC 4, BS 845, IS 8753, ISO 5199, ISO 4184, IAPWS-IF97, OCIB 2025 Rules  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Executive Summary & Design Scope

Phase 06 elevates **Mechanical Pro Notes** from a static equipment and regulatory reference catalog into an active, on-the-floor mathematical diagnostic and thermodynamic engineering engine. All calculations are implemented natively in Kotlin domain models, fully offline, deterministic, zero-network dependent (**DEC-016**), and safeguarded with physical sanity assertions and zero-division defenses.

The calculation suite consists of:
1. **Category A: Boiler Thermal & Utility Calculations (14 Calculators: CALC-01 to CALC-14):** Covering input-output efficiency, indirect heat loss balance (ASME PTC 4 / BS 845 / IS 8753), fuel firing rate, evaporation ratio, blowdown salt balance, flash steam recovery, feedwater hydraulics, deaeration, preliminary safety valve relieving verification, economizer recovery, FD/ID draft fan capacities, chimney stack draught, and steam pipe velocity sizing.
2. **Category B: Wash Plant Maintenance & Utility Tools (6 Calculators: CALC-15 to CALC-20):** Covering industrial washer V-belt pitch length and tension deflection (ISO 4184), hydro extractor centrifugal G-force and critical RPM speed limits, washing batch steam energy requirement, tumble dryer moisture evaporation and exhaust airflow, water softener ion-exchange cycle capacity and salt regeneration, and compressed air line pressure drop.
3. **Category C: Offline Thermodynamic Steam Engine:** High-precision IAPWS-IF97 formulation covering 0.5 to 20 bar saturated steam properties ($T_{sat}, h_f, h_{fg}, h_g, v_f, v_g, \rho$).

---

## 2. Category A: Boiler Thermal & Utility Calculations (CALC-01 to CALC-14)

### CALC-01: Direct Boiler Thermal Efficiency (Input-Output Method)
- **Governing Standard:** ASME PTC 4 (Fired Steam Generators), BS EN 12953, IS 8753.
- **Mathematical Formula:**
  $$\eta_{direct} = \frac{m_s \cdot (h_s - h_w)}{m_f \cdot \text{NCV}} \times 100$$
- **Engineering Derivation:** Ratio of thermal energy absorbed by generated steam to chemical energy delivered by fuel over a steady-state measurement interval.
- **Variables & Physical Bounds:**
  - $m_s$ (Steam Flow Rate): $100.0 - 50000.0\text{ kg/h}$ (Default: $4000.0\text{ kg/h}$)
  - $h_s$ (Enthalpy of Steam): $2000.0 - 3200.0\text{ kJ/kg}$ (Default: $2763.5\text{ kJ/kg}$ at 7 bar(g))
  - $h_w$ (Enthalpy of Feedwater): $40.0 - 650.0\text{ kJ/kg}$ (Default: $355.0\text{ kJ/kg}$ at $85^\circ\text{C}$)
  - $m_f$ (Fuel Consumption Rate): $10.0 - 10000.0\text{ kg/h or Nm}^3\text{/h}$ (Default: $320.0\text{ Nm}^3\text{/h}$)
  - $\text{NCV}$ (Net Calorific Value / LHV): $1000.0 - 60000.0\text{ kJ/kg or kJ/Nm}^3$ (Default: $35800.0\text{ kJ/Nm}^3$ for Pipeline Natural Gas)
- **Primary Output:** Thermal Efficiency ($\eta, \%$).
- **Secondary Outputs:** Heat Output ($kW$), Heat Input ($kW$), Specific Heat Absorption ($kJ/kg$).
- **Worked Benchmark:** $m_s = 4000, h_s = 2763.5, h_w = 355.0, m_f = 320, \text{NCV} = 35800 \implies \eta = 83.94\%$.

---

### CALC-02: Indirect Thermal Efficiency / Heat Loss Method (ASME PTC 4 / BS 845-1 / IS 8753)
- **Implementation Status (Batch 12 / `GAP-12` Updated 2026-11-17):** **IMPLEMENTED FOR GAS-FIRED & LAB-CALIBRATED FUELS** *(Uncalibrated solid/mixed-garment `jhut` remains explicitly restricted/unverified unless lab ultimate/proximate constants are entered).*
- **Governing Standard:** **ASME PTC 4-2013** (Section 5 Energy Balance Method), **BS 845-1:1987** (Concise Procedure), **IS 8753:1977** / **ISO 50002:2014** (Energy Audits).
- **Mathematical Formula (GCV / HHV Mass Basis):**
  $$\eta_{\text{indirect}} = 100 - (L_1 + L_2 + L_3 + L_4 + L_5 + L_6)$$
  - **GCV Unit Basis Conversion:** When firing gaseous fuel (`fuel_type = 1.0`) with volumetric gross calorific value (`gcv_unit_mode = 2.0`, $\text{kJ/Nm}^3$), fuel gas density $\rho_{\text{gas}}$ ($0.55\text{–}1.50\text{ kg/Nm}^3$, e.g., $0.74\text{ kg/Nm}^3$) converts volumetric GCV to gravimetric basis:
    $$\text{GCV}_{\text{mass}} = \frac{\text{GCV}_{\text{vol}}}{\rho_{\text{gas}}}\text{ [kJ/kg]}$$
    *(Solid/liquid fuels `fuel_type = 2.0` must use mass-basis $\text{kJ/kg}$; volumetric `kJ/Nm3` mode is rejected).*
  - **$L_1$ (Dry Flue Gas Sensible Heat Loss — Siegert Formula, BS 845-1 / IS 8753):**
    $$L_1 = \frac{K_{\text{Siegert}} \cdot (T_{\text{flue}} - T_{\text{ambient}})}{\%CO_2}$$
    where $K_{\text{Siegert}} = 0.38$ for pipeline natural gas (`fuel_type = 1.0`), and must be explicitly supplied (`siegert_k` $\in [0.25, 0.90]$) for solid/liquid fuels (`fuel_type = 2.0`).
  - **$L_2$ (Latent & Sensible Loss from $H_2$ Combustion Forming $\text{H}_2\text{O}$):**
    $$L_2 = \frac{9 \cdot (H_2\% / 100) \cdot \left[2442 + 2.09 \cdot (T_{\text{flue}} - T_{\text{ambient}})\right]}{\text{GCV}_{\text{mass}}} \times 100$$
  - **$L_3$ (Heat Loss due to Moisture Present in Fuel $M_{\text{fuel}}\%$):**
    $$L_3 = \frac{(M_{\text{fuel}}\% / 100) \cdot \left[2442 + 2.09 \cdot (T_{\text{flue}} - T_{\text{ambient}})\right]}{\text{GCV}_{\text{mass}}} \times 100$$
  - **$L_4$ (Heat Loss due to Moisture in Combustion Air at reference $w_{\text{air}} = 0.015\text{ kg/kg}$):**
    $$L_4 = \frac{A_0 \cdot 0.015 \cdot 2.09 \cdot (T_{\text{flue}} - T_{\text{ambient}})}{\text{GCV}_{\text{mass}}} \times 100$$
    where $A_0 = 17.0\text{ kg air/kg fuel}$ for pipeline natural gas (`fuel_type = 1.0`) and must be explicitly supplied (`theoretical_air` $\in [2.0, 20.0]\text{ kg/kg}$) for solid/liquid fuels (`fuel_type = 2.0`).
  - **$L_5$ (Incomplete Combustion / CO & Unburnt Ash Carbon Loss):** User-measured `L_unburnt` ($\%$) from calibrated CO analyzer or ash Loss-on-Ignition ($L_{\text{CO}} = \frac{\%CO}{\%CO + \%CO_2} \cdot \frac{5744 \cdot C\%}{\text{GCV}_{\text{mass}}}$). Omitted terms default to `0.0%` only with explicit disclosure in `notesAndRecommendations`.
  - **$L_6$ (Surface Radiation & Convection Loss):** User-supplied `L_radiation` ($\%$, ABMA chart typically $1.0\%\text{–}2.0\%$ at MCR for insulated packaged firetube boilers).
- **Exposed Variables & Physical Bounds (`13` total: `5` Required + `8` Optional/Governance):**
  - `T_flue` (Required): $80.0 - 450.0^\circ\text{C}$ (Default: $185.0^\circ\text{C}$)
  - `T_ambient` (Required): $5.0 - 55.0^\circ\text{C}$ (Default: $30.0^\circ\text{C}$)
  - `CO2` (Required, dry flue gas volume): $1.0 - 20.0\%$ (Default: $9.5\%$)
  - `GCV` (Required): $8000.0 - 120000.0\text{ kJ/unit}$ (Default: $39800.0\text{ kJ/Nm}^3$)
  - `H2_percent` (Required): $0.0 - 30.0\%$ (Default: $23.0\%$)
  - `fuel_type` (Optional): `1.0` = Natural Gas, `2.0` = Solid/Liquid (Default: `1.0`)
  - `gcv_unit_mode` (Optional): `1.0` = `kJ/kg`, `2.0` = `kJ/Nm3` (Default: `2.0`)
  - `fuel_density` (Optional / Required when `gcv_unit_mode = 2.0`): $0.55 - 1.50\text{ kg/Nm}^3$ (Default: $0.74\text{ kg/Nm}^3$)
  - `siegert_k` (Optional for Gas [`0.38`]; **Required** for Solid/Liquid): $0.25 - 0.90$
  - `theoretical_air` (Optional for Gas [`17.0 kg/kg`]; **Required** for Solid/Liquid): $2.0 - 20.0\text{ kg/kg}$
  - `M_fuel` (Optional): $0.0 - 70.0\%$ (Fuel moisture; $0\%$ if omitted for pipeline gas, disclosed explicitly)
  - `L_unburnt` (Optional): $0.0 - 25.0\%$ (Unburnt CO/ash LOI loss; disclosed if omitted)
  - `L_radiation` (Optional): $0.0 - 15.0\%$ (Surface radiation/convection loss; disclosed if omitted)
- **Worked Natural Gas Benchmark:** $T_{\text{flue}} = 185^\circ\text{C}$, $T_{\text{amb}} = 30^\circ\text{C}$, $CO_2 = 9.5\%$, $\text{GCV} = 39,800\text{ kJ/Nm}^3$, $\rho = 0.74\text{ kg/Nm}^3$ ($\text{GCV}_{\text{mass}} = 53,783.78\text{ kJ/kg}$), $H_2 = 23\%$, $L_{\text{rad}} = 1.5\% \implies L_1 = 6.20\%$, $L_2 = 10.65\%$, $L_4 = 0.15\%$, $L_6 = 1.50\% \implies \eta_{\text{indirect}} = 81.50\%$ (GCV basis; $\approx 89\text{–}90\%$ NCV basis).
- **Explicit Mixed-Jhut Safety Boundary:** Mixed garment waste (*jhut*) lacks a fixed elemental composition ($C, H_2, M_{\text{fuel}}$, ash LOI, GCV). Running `CALC-02` with `fuel_type = 2.0` without lab-verified `siegert_k`, `theoretical_air`, and mass-basis `GCV` is rejected, and solid-fuel results carry an explicit `UNVERIFIED MIXED-JHUT BOUNDARY` advisory if field moisture or grate unburnt carbon losses are omitted.
- **Primary Output:** Indirect Boiler Efficiency ($\eta_{\text{indirect}}, \%$ on GCV basis).
- **Secondary Outputs:** $L_1, L_2, L_3, L_4, L_{\text{unburnt}}, L_{\text{rad}},$ and Total Losses ($\%$).

---

### CALC-03: Required Fuel Firing Rate
- **Governing Standard:** Industrial Boiler Thermal Energy Balance.
- **Mathematical Formula:**
  $$\dot{m}_f = \frac{\dot{m}_s \cdot (h_s - h_w)}{\text{LHV} \cdot (\eta / 100)}$$
- **Variables & Physical Bounds:**
  - $\dot{m}_s$ (Steam Demand): $100.0 - 50000.0\text{ kg/h}$ (Default: $5000.0\text{ kg/h}$)
  - $P$ (Operating Pressure): $1.0 - 32.0\text{ bar(g)}$ (Default: $8.0\text{ bar(g)}$)
  - $T_w$ (Feedwater Temperature): $20.0 - 140.0^\circ\text{C}$ (Default: $85.0^\circ\text{C}$)
  - $\text{LHV}$ (Fuel Lower Heating Value): $5000.0 - 55000.0\text{ kJ/kg or kJ/Nm}^3$ (Default: $35800.0\text{ kJ/Nm}^3$)
  - $\eta$ (Expected Boiler Efficiency): $60.0 - 95.0\%$ (Default: $86.0\%$)
- **Primary Output:** Fuel Firing Rate ($\text{Nm}^3\text{/h}$ or $\text{kg/h}$).
- **Secondary Output:** Thermal Fuel Power Input ($kW$).

---

### CALC-04: Steam Evaporation Ratio (Steam-to-Fuel Ratio)
- **Governing Standard:** ISO 50002 Energy Audit Key Performance Indicator (EnPI).
- **Mathematical Formula:**
  $$\text{ER} = \frac{\text{Total Steam Generated (kg)}}{\text{Total Fuel Consumed (kg or Nm}^3\text{)}}$$
- **Variables & Physical Bounds:**
  - Total Steam Generated: $100.0 - 500000.0\text{ kg}$ (Default: $30000.0\text{ kg}$)
  - Total Fuel Consumed: $10.0 - 50000.0\text{ Nm}^3\text{ or kg}$ (Default: $2400.0\text{ Nm}^3$)
- **Primary Output:** Evaporation Ratio ($\text{kg steam / Nm}^3\text{ gas}$ or $\text{kg steam / kg fuel}$).
- **Benchmark Interpretation:**
  - Natural Gas: Typical $11.5 - 13.5\text{ kg/Nm}^3$.
  - Heavy Fuel Oil (Furnace Oil): $13.5 - 14.5\text{ kg/kg}$.
  - Diesel: $13.8 - 14.8\text{ kg/kg}$.
  - Jhut / Textile Fabric Waste: $3.5 - 4.5\text{ kg/kg}$.
  - Rice Husk: $3.0 - 4.0\text{ kg/kg}$.

---

### CALC-05: Surface Continuous Blowdown Rate
- **Governing Standard:** BS 2486 (Recommendations for treatment of water for steam boilers) / ABMA Boiler Water Limits.
- **Mathematical Formula:**
  $$\%B = \frac{\text{TDS}_{feed}}{\text{TDS}_{boiler} - \text{TDS}_{feed}} \times 100$$
  $$\dot{m}_B = \dot{m}_s \times \frac{\%B}{100 - \%B} = \dot{m}_s \times \frac{\text{TDS}_{feed}}{\text{TDS}_{boiler} - 2 \cdot \text{TDS}_{feed}} \approx \dot{m}_s \times \frac{\text{TDS}_{feed}}{\text{TDS}_{boiler}}$$
- **Variables & Physical Bounds:**
  - $\text{TDS}_{feed}$ (Feedwater Total Dissolved Solids): $5.0 - 800.0\text{ ppm}$ (Default: $150.0\text{ ppm}$)
  - $\text{TDS}_{boiler}$ (Maximum Drum Limit per ABMA/BS 2486): $1000.0 - 6000.0\text{ ppm}$ (Default: $3500.0\text{ ppm}$)
  - $\dot{m}_s$ (Steam Generation Rate): $100.0 - 50000.0\text{ kg/h}$ (Default: $4000.0\text{ kg/h}$)
- **Primary Output:** Blowdown Rate ($\%B, \%$ of feedwater).
- **Secondary Output:** Blowdown Mass Flow ($\dot{m}_B, \text{kg/h}$).

---

### CALC-06: Blowdown Flash Steam & Heat Recovery Savings
- **Governing Standard:** Industrial Steam Systems Energy Efficiency Guidelines.
- **Mathematical Formula:**
  $$\%X_{flash} = \frac{h_{f,high} - h_{f,flash}}{h_{fg,flash}} \times 100$$
  $$\dot{m}_{flash} = \dot{m}_B \times \frac{\%X_{flash}}{100}$$
  $$\dot{Q}_{recovered} = \dot{m}_{flash} \cdot h_{fg,flash} + (\dot{m}_B - \dot{m}_{flash}) \cdot C_p \cdot (T_{residual} - T_{drain\_target})$$
- **Variables & Physical Bounds:**
  - $\dot{m}_B$ (Blowdown Flow): $10.0 - 5000.0\text{ kg/h}$ (Default: $200.0\text{ kg/h}$)
  - $P_{boiler}$ (Boiler Drum Pressure): $2.0 - 30.0\text{ bar(g)}$ (Default: $8.0\text{ bar(g)}$)
  - $P_{flash}$ (Flash Vessel Pressure): $0.2 - 3.0\text{ bar(g)}$ (Default: $0.5\text{ bar(g)}$)
  - Heat Exchanger Effectiveness ($\epsilon$): $0.50 - 0.95$ (Default: $0.80$)
- **Primary Output:** Flash Steam Generated ($\text{kg/h}$).
- **Secondary Outputs:** Flash Steam Percentage ($\%$), Sensible Heat Recoverable ($kW$), Equivalent Annual Natural Gas Savings ($Nm^3/year$).

---

### CALC-07: Feedwater Pump Hydraulic Power & Motor Rating
- **Governing Standard:** ISO 5199 / Hydraulic Institute Standards (Centrifugal Multistage Pumps).
- **Mathematical Formula:**
  $$P_{hyd} = \frac{\dot{m}_w \cdot g \cdot H}{3.6 \times 10^6} = \frac{Q \cdot \rho \cdot g \cdot H}{3.6 \times 10^6}\text{ [kW]}$$
  $$P_{brake} = \frac{P_{hyd}}{\eta_{pump}}$$
  $$P_{motor\_rated} = \frac{P_{brake}}{\eta_{motor}} \times \text{Safety Factor (1.15 to 1.25)}$$
- **Variables & Physical Bounds:**
  - $Q$ (Feedwater Flow Rate): $1.0 - 100.0\text{ m}^3\text{/h}$ (Default: $5.5\text{ m}^3\text{/h}$)
  - $H$ (Total Dynamic Head): $20.0 - 500.0\text{ m}$ (Default: $130.0\text{ m}$ for 10 bar boiler)
  - $\eta_{pump}$ (Pump Hydraulic Efficiency): $40.0 - 85.0\%$ (Default: $65.0\%$)
  - $\eta_{motor}$ (Motor Electrical Efficiency): $75.0 - 96.0\%$ (Default: $90.0\%$)
- **Primary Output:** Pump Shaft Brake Power ($kW$).
- **Secondary Outputs:** Hydraulic Power ($kW$), Recommended Standard Motor Frame Size ($kW / HP$).

---

### CALC-08: Deaerator Steam Consumption Balance
- **Governing Standard:** Heat Exchange Institute (HEI) Standards for Spray & Tray Deaerators.
- **Mathematical Formula:**
  $$\dot{m}_{steam} = \frac{\dot{m}_{make} \cdot (h_{da} - h_{make}) + \dot{m}_{cond} \cdot (h_{da} - h_{cond})}{h_{steam} - h_{da}}$$
- **Variables & Physical Bounds:**
  - $\dot{m}_{make}$ (Make-up Water Flow): $0.0 - 30000.0\text{ kg/h}$ (Default: $2500.0\text{ kg/h}$)
  - $T_{make}$ (Make-up Water Temperature): $10.0 - 50.0^\circ\text{C}$ (Default: $28.0^\circ\text{C}$)
  - $\dot{m}_{cond}$ (Condensate Return Flow): $0.0 - 30000.0\text{ kg/h}$ (Default: $2500.0\text{ kg/h}$)
  - $T_{cond}$ (Condensate Return Temperature): $40.0 - 95.0^\circ\text{C}$ (Default: $80.0^\circ\text{C}$)
  - $P_{da}$ (Deaerator Operating Pressure): $0.1 - 0.8\text{ bar(g)}$ (Default: $0.2\text{ bar(g)}$, corresponding to $105^\circ\text{C}$)
  - $P_{steam}$ (Heating Steam Supply Pressure): $1.0 - 10.0\text{ bar(g)}$ (Default: $3.0\text{ bar(g)}$)
- **Primary Output:** Required Deaerating Steam Flow ($\text{kg/h}$).
- **Secondary Outputs:** Blended Inlet Water Temperature ($^\circ\text{C}$), Steam Mass Fraction ($\%$ of Feedwater).

---

### CALC-09: Safety Valve Relieving Capacity Verification (Preliminary Audit Reference)
- **Governing Standard:** ASME Boiler and Pressure Vessel Code Section I (PG-67 to PG-73), BS EN ISO 4126-1, Bangladesh Boilers Act 2022 / OCIB Rules 2025.
- **Regulatory Warning:** Preliminary plant-floor reference only. This calculation cannot authorize valve stamp, set pressure alterations, or certify relieving capacity. Formal certification requires an authorized Chief Inspector of Boilers (OCIB) certified inspector and stamped valve nameplate.
- **Mathematical Formula:**
  $$W = 51.45 \cdot A \cdot P \cdot K_d \cdot K_b \cdot K_{sh}$$
  Where:
  - $P = (\text{Set Pressure} \times 1.03) + 1.01325\text{ bar (absolute)}$ (for $3\%$ overpressure per ASME Section I)
  - $A = \frac{\pi}{4} d^2\text{ (Orifice area, mm}^2\text{)}$
  - $K_d$ (Discharge Coefficient): Certified derated coefficient (typically $0.90 \times 0.975 = 0.878$ or valve manufacturer tag).
  - $K_b$ (Backpressure correction): $1.0$ for open atmospheric discharge.
  - $K_{sh}$ (Superheat factor): $1.0$ for dry saturated steam.
- **Variables & Physical Bounds:**
  - $P_{set}$ (Set Pressure): $1.0 - 32.0\text{ bar(g)}$ (Default: $10.0\text{ bar(g)}$)
  - $d_{orifice}$ (Valve Throat Diameter): $10.0 - 150.0\text{ mm}$ (Default: $32.0\text{ mm}$)
  - $K_d$ (Derated Coefficient of Discharge): $0.50 - 0.95$ (Default: $0.85$)
  - $\dot{m}_{mcr}$ (Boiler Maximum Continuous Steaming Rating): $500.0 - 30000.0\text{ kg/h}$ (Default: $2500.0\text{ kg/h}$)
- **Primary Output:** Total Certified Relieving Capacity ($W, \text{kg/h}$).
- **Secondary Outputs:** Capacity Margin ($\%$ excess or deficiency vs MCR), Regulatory Compliance Flag (PASS / FAIL against requirement that valve capacity $\ge \text{MCR}$).

---

### CALC-10: Economizer Flue Gas Heat Recovery
- **Governing Standard:** Flue Gas Heat Exchanger Thermodynamics.
- **Mathematical Formula:**
  $$\dot{Q}_{rec} = \dot{m}_{fg} \cdot C_{p,fg} \cdot (T_{fg,in} - T_{fg,out})$$
  $$\Delta T_{water} = \frac{\dot{Q}_{rec} \cdot \eta_{hx}}{\dot{m}_w \cdot C_{p,w}}$$
  $$\Delta \eta_{boiler} \approx \frac{\Delta T_{water}}{6^\circ\text{C}}\text{ (Rule of thumb: each }6^\circ\text{C rise in feedwater yields }\sim 1\%\text{ fuel efficiency gain)}$$
- **Variables & Physical Bounds:**
  - $\dot{m}_w$ (Feedwater Flow): $500.0 - 30000.0\text{ kg/h}$ (Default: $4000.0\text{ kg/h}$)
  - $T_{fg,in}$ (Inlet Flue Gas Temp): $150.0 - 350.0^\circ\text{C}$ (Default: $230.0^\circ\text{C}$)
  - $T_{fg,out}$ (Target Outlet Flue Gas Temp): $110.0 - 200.0^\circ\text{C}$ (Default: $150.0^\circ\text{C}$, guarded above acid dew point)
  - $\dot{m}_{fuel}$ (Fuel Consumption): $50.0 - 3000.0\text{ Nm}^3\text{/h}$ (Default: $320.0\text{ Nm}^3\text{/h}$)
  - Specific Flue Gas Volume: $10.0 - 16.0\text{ Nm}^3\text{ gas / Nm}^3\text{ fuel}$ (Default: $11.5\text{ Nm}^3/\text{Nm}^3$)
- **Primary Output:** Thermal Energy Recovered ($\dot{Q}_{rec}, kW$).
- **Secondary Outputs:** Feedwater Temperature Rise ($\Delta T_w, ^\circ\text{C}$), Estimated Boiler Efficiency Improvement ($\%$), Fuel Cost Savings ($\% / \text{BDT per year}$).

---

### CALC-11: Combustion Air Requirement & FD Fan Sizing
- **Governing Standard:** Industrial Burner & Combustion Stoichiometry.
- **Mathematical Formula:**
  $$V_{air,theor} \approx 9.5\text{ to }10.5\text{ Nm}^3\text{ air / Nm}^3\text{ natural gas}$$
  $$V_{air,actual} = V_{air,theor} \times \left(1 + \frac{\text{EA}\%}{100}\right) \times \dot{m}_{fuel}$$
  $$Q_{fan,flow} = V_{air,actual} \times \frac{273.15 + T_{amb}}{273.15} \times \text{Safety Factor (1.15 to 1.20)}$$
  $$P_{fan} = \frac{Q_{fan} \cdot \Delta P_{static}}{60000 \cdot \eta_{fan}}$$
- **Variables & Physical Bounds:**
  - $\dot{m}_{fuel}$ (Gas Firing Rate): $20.0 - 3000.0\text{ Nm}^3\text{/h}$ (Default: $320.0\text{ Nm}^3\text{/h}$)
  - $\text{EA}\%$ (Excess Air): $5.0 - 50.0\%$ (Default: $15.0\%$)
  - $\Delta P_{static}$ (Total Air Resistance / Windbox Pressure): $50.0 - 450.0\text{ mm WC}$ (Default: $150.0\text{ mm WC}$)
  - $\eta_{fan}$ (Fan Static Efficiency): $50.0 - 85.0\%$ (Default: $70.0\%$)
  - $T_{amb}$ (Ambient Temperature): $10.0 - 50.0^\circ\text{C}$ (Default: $32.0^\circ\text{C}$)
- **Primary Output:** Required Fan Volumetric Airflow ($Q_{fan}, \text{m}^3\text{/min}$ and $\text{CFM}$).
- **Secondary Outputs:** FD Fan Static Head ($mm\text{ WC} / Pa$), Required Motor Shaft Power ($kW / HP$).

---

### CALC-12: Induced Draft (ID) Fan Sizing & Static Head
- **Governing Standard:** Flue Gas Extraction & Draft System Engineering.
- **Mathematical Formula:**
  $$V_{flue,wet} = V_{air,actual} + V_{combustion\_products}$$
  $$Q_{id,actual} = V_{flue,wet} \times \frac{273.15 + T_{flue}}{273.15} \times \text{Safety Factor (1.20)}$$
  $$P_{id\_motor} = \frac{Q_{id} \cdot \Delta P_{system}}{60000 \cdot \eta_{id\_fan}}$$
- **Variables & Physical Bounds:**
  - $\dot{m}_{fuel}$ (Fuel Firing Rate): $50.0 - 5000.0\text{ kg/h or Nm}^3\text{/h}$ (Default: $500.0\text{ kg/h}$ for biomass/jhut)
  - $T_{flue}$ (Flue Gas Temperature at Fan Inlet): $120.0 - 280.0^\circ\text{C}$ (Default: $160.0^\circ\text{C}$)
  - $\Delta P_{system}$ (Draft Drop across Furnace, Boiler Passes, Economizer, Cyclone, Bagfilter, Ductwork): $100.0 - 600.0\text{ mm WC}$ (Default: $220.0\text{ mm WC}$)
  - $\eta_{id\_fan}$ (Fan Efficiency): $50.0 - 80.0\%$ (Default: $65.0\%$)
- **Primary Output:** ID Fan Volumetric Capacity ($m^3/min$ and $m^3/h$).
- **Secondary Outputs:** Operating Flue Gas Density ($\rho_{flue}, kg/m^3$), ID Fan Motor Power ($kW / HP$).

---

### CALC-13: Natural Chimney Stack Draught
- **Governing Standard:** Barometric Equation for Buoyancy-Driven Thermal Draught.
- **Mathematical Formula:**
  $$\Delta P_{natural} = 353 \cdot H \cdot \left(\frac{1}{T_{amb} + 273.15} - \frac{1}{T_{stack,avg} + 273.15}\right)\text{ [mm WC]}$$
  $$\Delta P_{Pa} = \Delta P_{natural} \times 9.80665\text{ [Pa]}$$
- **Variables & Physical Bounds:**
  - $H$ (Stack / Chimney Height): $5.0 - 80.0\text{ m}$ (Default: $30.0\text{ m}$)
  - $T_{stack,avg}$ (Average Flue Gas Temperature inside Chimney): $80.0 - 350.0^\circ\text{C}$ (Default: $160.0^\circ\text{C}$)
  - $T_{amb}$ (Ambient Temperature): $5.0 - 50.0^\circ\text{C}$ (Default: $30.0^\circ\text{C}$)
  - Barometric Pressure: $95.0 - 105.0\text{ kPa}$ (Default: $101.325\text{ kPa}$)
- **Primary Output:** Theoretical Natural Draught ($\Delta P_{nat}, mm\text{ WC}$).
- **Secondary Outputs:** Draught in Pascals ($Pa$), Flue Gas Velocity ($\text{m/s}$).

---

### CALC-14: Steam Header & Main Distribution Pipe Sizing
- **Governing Standard:** Spirax Sarco & ASME B31.1 Power Piping Sizing Principles.
- **Mathematical Formula:**
  $$d_{inner} = \sqrt{\frac{4 \cdot \dot{m}_s \cdot v_g}{3600 \cdot \pi \cdot V}} \times 1000\text{ [mm]}$$
  Where $v_g$ is specific volume of saturated steam from the IAPWS-IF97 engine at line pressure $P$.
- **Variables & Physical Bounds:**
  - $\dot{m}_s$ (Steam Mass Flow): $50.0 - 50000.0\text{ kg/h}$ (Default: $3000.0\text{ kg/h}$)
  - $P$ (Operating Pressure): $0.5 - 25.0\text{ bar(g)}$ (Default: $7.0\text{ bar(g)}$)
  - $V$ (Target Steam Velocity): $15.0 - 45.0\text{ m/s}$ (Default: $25.0\text{ m/s}$ for saturated steam, $35.0\text{ m/s}$ for superheated)
- **Primary Output:** Minimum Internal Pipe Diameter ($d_{inner}, mm$).
- **Secondary Outputs:** Recommended Standard Nominal Bore (e.g., DN50, DN80, DN100, DN150), Actual Velocity at Selected Pipe Size ($m/s$).

---

## 3. Category B: Wash Plant Maintenance & Utility Tools (CALC-15 to CALC-20)

### CALC-15: Industrial Washer V-Belt Sizing & Deflection Force
- **Governing Standard:** ISO 4184, RMA (Rubber Manufacturers Association) Classical & Narrow V-Belt Standards.
- **Mathematical Formula:**
  $$L_p = 2C + \frac{\pi}{2}(D + d) + \frac{(D - d)^2}{4C}$$
  $$F_{deflection} \approx \frac{T_e + 2T_0}{16}\text{ or empirical RMA strand tension chart (typically 35 to 65 N for B-section)}$$
  $$\text{Deflection Distance } \delta = \frac{\text{Span Length } t}{64}$$
  Where $t = \sqrt{C^2 - \left(\frac{D - d}{2}\right)^2}$.
- **Variables & Physical Bounds:**
  - $d$ (Motor Pulley Pitch Diameter): $60.0 - 400.0\text{ mm}$ (Default: $140.0\text{ mm}$)
  - $D$ (Machine Drum Pulley Pitch Diameter): $150.0 - 1500.0\text{ mm}$ (Default: $420.0\text{ mm}$)
  - $C$ (Center-to-Center Distance): $300.0 - 2500.0\text{ mm}$ (Default: $800.0\text{ mm}$)
  - Belt Section: Classical A, B, C or Narrow SPA, SPB (Default: Section B)
- **Primary Output:** Pitch Length ($L_p, mm$ and Belt Code).
- **Secondary Outputs:** Speed Reduction Ratio ($D/d : 1$), Target Strand Deflection Distance ($\delta, mm$), Recommended Deflection Force ($N$).

---

### CALC-16: Hydro Extractor Centrifugal G-Force & Critical Speed Safety Audit
- **Governing Standard:** Centrifugal Machinery Dynamics & ISO 11111 Textile Machinery Safety.
- **Safety Critical Warning:** High rotational energy. Operating beyond rated nameplate RPM can cause catastrophic drum burst or violent basket rupture.
- **Mathematical Formula:**
  $$G\text{-factor} = \frac{r \cdot \omega^2}{g} = \frac{D \cdot \pi^2 \cdot N^2}{1800 \cdot 9.80665} \approx 5.59 \times 10^{-4} \cdot D \cdot \left(\frac{N}{100}\right)^2$$
  $$V_{rim} = \frac{\pi \cdot D \cdot N}{60}\text{ [m/s]}$$
- **Variables & Physical Bounds:**
  - $D$ (Basket / Drum Diameter): $0.40 - 2.00\text{ m}$ (Default: $1.20\text{ m}$)
  - $N$ (Operating Rotational Speed): $200.0 - 1800.0\text{ RPM}$ (Default: $850.0\text{ RPM}$)
  - $N_{rated}$ (Manufacturer Rated Max RPM): $400.0 - 2000.0\text{ RPM}$ (Default: $900.0\text{ RPM}$)
  - $M_{load}$ (Wet Garment Wet Weight): $50.0 - 500.0\text{ kg}$ (Default: $200.0\text{ kg}$)
- **Primary Output:** Centrifugal Force ($G\text{-Factor}, \times g$).
- **Secondary Outputs:** Outer Rim Peripheral Velocity ($V_{rim}, m/s$), Hoop Stress Factor, Safety Over-speed Alert (TRIGGERED if $N > N_{rated}$).

---

### CALC-17: Washing Batch Steam Consumption & Heating Energy
- **Governing Standard:** Textile Wet Processing Thermodynamics.
- **Mathematical Formula:**
  $$Q_{sensible} = (M_{liquor} \cdot C_{p,water} + M_{fabric} \cdot C_{p,cotton} + M_{metal} \cdot C_{p,steel}) \cdot (T_{target} - T_{initial})$$
  $$Q_{losses} = Q_{sensible} \times f_{loss}\text{ (where }f_{loss}\text{ is 0.10 to 0.15 for uninsulated rotary machines)}$$
  $$m_{steam} = \frac{Q_{total}}{h_{fg} \cdot \eta_{direct\_sparge}}$$
- **Variables & Physical Bounds:**
  - $M_{fabric}$ (Dry Garment Weight): $20.0 - 500.0\text{ kg}$ (Default: $150.0\text{ kg}$)
  - Liquor Ratio ($L:R$): $1:3\text{ to }1:15$ (Default: $1:6$, meaning $900\text{ kg}$ water)
  - $T_{initial}$ (Incoming Water Temperature): $15.0 - 45.0^\circ\text{C}$ (Default: $28.0^\circ\text{C}$)
  - $T_{target}$ (Process Wash Temperature): $40.0 - 95.0^\circ\text{C}$ (Default: $65.0^\circ\text{C}$)
  - $P_{steam}$ (Steam Line Pressure): $2.0 - 6.0\text{ bar(g)}$ (Default: $3.5\text{ bar(g)}$)
- **Primary Output:** Batch Steam Requirement ($m_{steam}, kg\text{ steam / batch}$).
- **Secondary Outputs:** Heating Thermal Energy ($MJ / kWh$), Estimated Heating Ramp Duration ($minutes$ at standard sparge nozzle capacity).

---

### CALC-18: Tumble Dryer Moisture Evaporation Rate & Exhaust CFM Airflow
- **Governing Standard:** Industrial Drying Psychrometrics & Energy Consumption.
- **Mathematical Formula:**
  $$\dot{m}_{evap} = \frac{M_{wet} - M_{dry}}{t_{cycle} / 60}\text{ [kg water / h]}$$
  $$\dot{Q}_{thermal} = \dot{m}_{evap} \cdot q_{spec}\text{ (where }q_{spec}\text{ is typically }4500\text{ to }6000\text{ kJ/kg water evaporated)}$$
  $$\text{Exhaust Air Flow } Q_{cfm} = \frac{\dot{m}_{evap}}{\Delta \omega \cdot \rho_{air} \cdot 60} \times 35.3147\text{ [CFM]}$$
- **Variables & Physical Bounds:**
  - $M_{wet}$ (Hydro-extracted Batch Weight): $50.0 - 500.0\text{ kg}$ (Default: $250.0\text{ kg}$ with $60\%$ retention)
  - $M_{dry}$ (Target Bone-dry Weight): $30.0 - 300.0\text{ kg}$ (Default: $156.0\text{ kg}$)
  - $t_{cycle}$ (Drying Cycle Duration): $15.0 - 90.0\text{ minutes}$ (Default: $35.0\text{ minutes}$)
  - Specific Heat Consumption: $4000.0 - 7500.0\text{ kJ/kg water}$ (Default: $5200.0\text{ kJ/kg}$)
- **Primary Output:** Moisture Evaporation Rate ($\dot{m}_{evap}, kg/h$).
- **Secondary Outputs:** Dryer Heating Load ($kW$), Burner Gas / Steam Consumption, Recommended Exhaust Blower CFM ($m^3/min$ and $CFM$).

---

### CALC-19: Water Softener Ion-Exchange Capacity & Salt Regeneration Yield
- **Governing Standard:** Water Treatment Ion-Exchange Design (Strong Acid Cation Resin).
- **Mathematical Formula:**
  $$\text{Total Exchange Capacity (eq)} = V_{resin} \cdot C_{resin}$$
  $$\text{Cycle Yield } V_{soft} = \frac{V_{resin} \cdot C_{resin}}{\text{Raw Water Total Hardness (eq/m}^3\text{)}} = \frac{V_{resin} \cdot C_{resin} \cdot 50000}{\text{Hardness (ppm as CaCO}_3\text{)}}\text{ [m}^3\text{]}$$
  $$M_{salt} = V_{resin} \times S_{dose}\text{ (typically }120\text{ to }200\text{ g NaCl / L resin)}$$
- **Variables & Physical Bounds:**
  - $V_{resin}$ (Cation Resin Volume): $50.0 - 5000.0\text{ Liters}$ (Default: $500.0\text{ L}$)
  - $C_{resin}$ (Exchange Capacity): $1.2 - 2.2\text{ eq/L}$ (Default: $1.9\text{ eq/L}$ for crosslinked polystyrene)
  - $\text{Hardness}$ (Raw Water Total Hardness): $50.0 - 800.0\text{ ppm as CaCO}_3$ (Default: $180.0\text{ ppm}$)
  - Salt Dosage Rate: $100.0 - 250.0\text{ g NaCl / L resin}$ (Default: $140.0\text{ g/L}$)
- **Primary Output:** Soft Water Yield per Regeneration Cycle ($V_{soft}, m^3$ and $Liters$).
- **Secondary Outputs:** Salt Required per Regeneration ($kg\text{ NaCl}$), Softening Run Time at rated flow ($hours$).

---

### CALC-20: Compressed Air Line Sizing & Pressure Drop
- **Governing Standard:** Darcy-Weisbach & Weymouth Compressible Flow for Pneumatic Systems.
- **Mathematical Formula:**
  $$\Delta P = \frac{L \cdot Q^{1.85} \cdot \rho}{C \cdot d^{5} \cdot P_{in}}\text{ or Darcy-Weisbach: }\Delta P = f \cdot \frac{L}{d} \cdot \frac{\rho \cdot v^2}{2}$$
- **Variables & Physical Bounds:**
  - $Q_{fpm}$ (Free Air Delivery, FAD): $10.0 - 5000.0\text{ CFM}$ or $m^3/min$ (Default: $120.0\text{ CFM}$)
  - $L$ (Pipe Equivalent Length including fittings): $10.0 - 1000.0\text{ m}$ (Default: $120.0\text{ m}$)
  - $d_{pipe}$ (Pipe Internal Diameter): $15.0 - 150.0\text{ mm}$ (Default: $40.0\text{ mm}$)
  - $P_{supply}$ (Compressor Discharge Pressure): $4.0 - 14.0\text{ bar(g)}$ (Default: $7.0\text{ bar(g)}$)
- **Primary Output:** Distribution Pressure Drop ($\Delta P, bar$ and $psi$).
- **Secondary Outputs:** Compressed Air Velocity ($m/s$, recommended $< 8\text{ m/s}$ in mains), Terminal Pressure at Machines ($bar(g)$).

---

## 4. Verification & Validation Summary

All 20 algorithms are validated against analytical engineering benchmarks and tested in `test_suite_phase06.py`:
- 100% of physical inputs are bound by lower and upper physical limits.
- Zero-division exceptions are prevented using `max(epsilon, denominator)` guards and explicit boundary exceptions.
- Zero network reliance is verified via static manifest audit (**DEC-016**).
- User notes and calculation logs are isolated from static catalog tables (**DEC-014**).
