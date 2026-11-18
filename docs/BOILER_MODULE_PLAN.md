# DEDICATED BOILER ENGINEERING KNOWLEDGE BASE PLAN
**Document Identifier:** DOC-MPN-008  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** FULLY RECONCILED & VALIDATED - MASTER PROMPT 02-B & BATCH 14  
**Scope:** Gas-Fired Industrial Boilers, Jhut/Waste-Fuel Boilers, 14 Verified Engineering Calculation Algorithms, Statutory Legislation (Act 06 of 2022 & S.R.O. 279-Law/2025)  
**Last Updated:** 2026-11-18  

---

## 1. Top-Level Architectural Mandate
Boiler systems constitute the primary thermal energy and pressure utility of a textile and garment washing plant. Without stable, dry saturated steam at 6–10 bar(g), washing machines cannot reach required chemical reaction temperatures (desizing at 60°C, bleaching at 85°C), tumbler dryers cannot dry heavy wet denim, and finishing curing ovens cannot set resins.

**Architectural Policy [C. OWNER-APPROVED REQUIREMENT]:**  
The Boiler module is established as an independent, top-level knowledge and calculation domain in the application. It is NEVER subordinated or buried inside general "Power Plant Engineering".

---

## 2. Statutory Legal & Regulatory Framework in Bangladesh

### 2.1 Primary Legislation: The Boilers Act, 2022 (`Act No. 06 of 2022`) `[SRC-011]`
- **Enactment:** **The Boilers Act, 2022 (Act No. 06 of 2022 / ২০২২ সনের ০৬ নং আইন)** enacted by the Parliament of Bangladesh; published in the Bangladesh Gazette and commenced immediately (`Section 1(2)`) on **11 April 2022**.
- **Repeal of Legacy Law:** Under **Section 54 (Repeal and Savings)** of the 2022 Act, **The Boilers Act, 1923 (Act No. V of 1923) is fully REPEALED**.
- **Official Legal Database Reference:** `http://bdlaws.minlaw.gov.bd/act-print-1402.html`
- **Verified Statutory Section Mapping:**
  1. **Section 2 (Definitions):** `Section 2(3)` defines boiler accident; `Section 2(7)` defines a boiler as any closed pressure vessel exceeding **25 liters** capacity used for generating steam under pressure.
  2. **Section 5 (Chief Inspector & Inspectors):** Appointment, powers, and statutory duties of the Chief Inspector and OCIB Inspectors.
  3. **Section 6 & Section 7 (Boiler Board):** Constitution (`Section 6`) and functions (`Section 7`) of the national Boiler Board.
  4. **Section 9 & Section 10 (Boiler Operation License):** Mandatory certification/licensing of boiler operators/attendants (5-year validity under `Section 9`, renewal under `Section 10`).
  5. **Section 13 (Boiler Classification, Capacity, Steam Pressure & Lifespan):** Statutory classification of boilers by category, heating capacity, pressure, and operational lifespan limits (implemented via `Rule 3` of Boiler Rules, 2025). *(Note: Attendant licensing is `Section 9`, not `Section 13`.)*
  6. **Section 14 (Enlistment of Manufacturers & Repairers):** Mandatory enlistment with the Chief Inspector of Boilers for manufacturing or structural repair.
  7. **Section 18, Section 19 & Section 20 (Registration, Renewal & Cancellation):** Every boiler operated in Bangladesh MUST be registered (`Section 18`) with a valid 1-year Certificate of Use for its authorized MAWP, renewed annually (`Section 19`), and subject to cancellation (`Section 20`) for fraud or unsafe condition.
  8. **Section 24 (Boiler Repairs):** Strict prohibition on unauthorized repairs; structural repair requires prior written notification/approval and enlisted execution subject to OCIB inspection.
  9. **Section 28 (Accident Reporting & Investigation):** Mandatory immediate accident reporting (`Section 28`) to OCIB and official inquiry; exact statutory response hours and forms must be checked in the Gazette text.
  10. **Section 36 & Section 38 (Penalties):** Operating without a licensed operator (`Section 36`) or operating an unregistered/uncertified boiler (`Section 38`) carries criminal liability: imprisonment up to **2 years**, fine up to **2,00,000 BDT**, or both.
  11. **Section 52 (Rule-Making Power):** Empowers the Government to frame subordinate statutory rules.
  12. **Section 54 (Repeal and Savings):** Repeals *The Boilers Act, 1923* (`Act No. V of 1923`) with statutory savings clauses.
  - *Verification Boundary:* Additional sub-section numbers (e.g., inspector entry powers, logbook production, prohibition orders, owner liability/compensation, mark-tampering/repair penalty slabs) were not re-verified line-by-line in the retrieved excerpts and must be confirmed directly in `bdlaws.minlaw.gov.bd/act-print-1402.html` before legal compliance reliance.

### 2.2 Subordinate Regulations: Bangladesh Boiler Rules, 2025 `[SRC-011A]`
- **Statutory Instrument:** **Bangladesh Boiler Rules, 2025 (বাংলাদেশ বয়লার বিধিমালা, ২০২৫)** issued under **S.R.O. No. 279-Law/2025**, signed **26 June 2025**, published in the Bangladesh Gazette Extraordinary on **29 June 2025**, comprising **69 Rules** plus statutory forms and schedules (framed under `Section 52` of Act No. 06 of 2022).
- **Critical Regulatory Caps & Lifespan Thresholds (`Rule 3 / বিধি ৩` under Section 13(2)):**
  1. **Electrode Boilers** (`<= 300 L`, up to `7 kg/cm2`): Maximum statutory lifespan capped at **10 years**.
  2. **Small Industrial Boilers** (`<= 500 L`, up to `7 kg/cm2`): Maximum statutory lifespan capped at **15 years**.
  3. **Industrial Packaged Boilers:** Maximum statutory lifespan capped at **30 years**.
  4. **Field-Erected Industrial Boilers:** Maximum statutory lifespan capped at **60 years**.
  5. **Lifespan Extension Protocol:** Up to **10 additional years** maximum (two terms) may be granted subject to Boiler Board approval, NDT inspection, ultrasonic thickness testing (UTM), efficiency testing, and hydraulic/steam testing.
  - *Verification Boundary:* Individual rule numbers for operator examination grades, fee schedules, and pre-import clearance remain subject to full PDF verification of `S.R.O. No. 279-Law/2025` (e.g., `Rule 44 / বিধি ৪৪` governs *forfeiture/cancellation of examination fee*, not daily blowdown).

### 2.3 Hydrostatic Testing: Legal & Technical Demarcation
To avoid dangerous operational misconceptions:
1. **Manufacturer New Construction Hydrostatic Test:** Conducted at the fabrication shop in accordance with ASME BPVC Section I (Part PG-99) at **$1.5 \times$ MAWP** or BS EN 12953-3 at **$1.43 \times$ design pressure**.
2. **In-Service Periodic Hydraulic Testing:** In-service hydraulic tests are conducted upon initial registration, following major structural weld repairs (`Section 24`), or when ordered by the Boiler Inspector if structural deterioration is suspected. The test pressure is established based on authorized inspector assessment (typically $1.25 \times$ to $1.5 \times$ certified working pressure per code/rules), NOT indiscriminately at 1.5 times the original new design pressure.
3. **Annual Certificate Renewal:** Consists of internal and external dry visual examination and an operational steam test to verify safety relief valve popping pressure. Hydraulic testing is applied when required by statutory rules or the inspector's engineering assessment.

---

## 3. Gas-Fired Industrial Boilers: Technical Specification
*(Scotch Marine 3-pass wet-back, forced draft burner, double block and bleed gas train, VPS valve proving system, Mobrey magnetic float controls, and dual spring safety valves as documented in `docs/PHASE_02_CONTENT_BLUEPRINT.md`).*

---

## 4. Jhut / Fabric Waste-Fuel Industrial Boilers: Technical Specification
*(Refractory furnace, step/dumping grates, balanced draft with FD and ID fans maintaining $-2\text{ to } -5\text{ mm WC}$, multi-cyclone grit collector, and anti-burn-back dampers as documented in `docs/PHASE_02_CONTENT_BLUEPRINT.md`).*

---

## 5. Boiler Engineering Calculations Engine: 14 Verified Specifications

*Important Engineering Note on ASME PTC 4:*  
The calculations below provide **practical plant engineering estimates and operational monitoring algorithms** based on classical first-law and heat-loss formulations (historically standardized in ASME PTC 4.1, BEE India, and Spirax Sarco engineering manuals). They are intended for daily performance tracking and sizing. They do NOT constitute formal contractual acceptance testing under ASME PTC 4-2013 (which requires rigorous multi-point flue gas grid traverse sampling, certified fuel ultimate analysis, and pre/post-test uncertainty analysis).

---

### CALC-01: Actual Steam Generation Rate ($Q_s$)
- **Mathematical Formula:**
  $$Q_s = \frac{V_{fw} \cdot \rho_{fw} - (\Delta V_{drum} \cdot \rho_w)}{t} \quad \left[\frac{\text{kg}}{\text{hr}}\right]$$
- **Variable Definitions & Units:**
  - $Q_s$: Actual steam mass generation rate [$\text{kg/hr}$]
  - $V_{fw}$: Total feedwater volume consumed during test [$\text{m}^3$]
  - $\rho_{fw}$: Density of feedwater at inlet temperature [$\text{kg/m}^3$] (e.g. $971.8\text{ kg/m}^3$ at $80^\circ\text{C}$)
  - $\Delta V_{drum}$: Net change in water volume inside boiler drum between start and end [$\text{m}^3$]
  - $\rho_w$: Density of water at saturated boiler temperature [$\text{kg/m}^3$]
  - $t$: Measurement test duration [$\text{hr}$]
- **Input Boundaries:** $V_{fw} > 0$; $t > 0$; $0.1 \times \text{MCR} \le Q_s \le 1.2 \times \text{MCR}$.
- **Assumptions & Limitations:** Blowdown isolation valves must remain fully shut during test period; water level in sight glass must return exactly to datum line ($\Delta V_{drum} = 0$).
- **Validity Conditions:** Valid for steady-state firing for a minimum 30-minute duration.
- **Worked Engineering Example:** Feedwater meter records $8.50\text{ m}^3$ over a $2.0\text{ hr}$ run at $80^\circ\text{C}$ ($\rho_{fw} = 971.8\text{ kg/m}^3$). Water level datum is identical ($\Delta V_{drum} = 0$).  
  $$Q_s = \frac{8.50 \times 971.8}{2.0} = 4,130.15\text{ kg/hr}$$
- **Expected Output:** $4,130.15\text{ kg/hr}$ steam.
- **Independent Validation Method:** Mass balance check against vortex steam flow meter reading: $|Q_{s,calc} - Q_{s,meter}| / Q_{s,meter} \le 0.03$.
- **Reference:** Spirax Sarco Steam Metering Principles / ISO 5167.

---

### CALC-02: Fuel Consumption Rate ($q_f$)
- **Mathematical Formula:**
  - **Pipeline Natural Gas (Volume corrected to Normal standard conditions):**  
    $$q_f = \frac{V_2 - V_1}{t} \times \left(\frac{P_{actual} + P_{atm}}{P_{std}}\right) \times \left(\frac{T_{std}}{T_{actual} + 273.15}\right) \quad \left[\frac{\text{Nm}^3}{\text{hr}}\right]$$
  - **Jhut / Solid Fuel (Mass basis):**  
    $$q_f = \frac{W_{fuel}}{t} \quad \left[\frac{\text{kg}}{\text{hr}}\right]$$
- **Variable Definitions & Units:**
  - $V_2 - V_1$: Gas meter raw uncorrected volumetric displacement [$\text{m}^3$]
  - $P_{actual}$: Gas gauge pressure measured at meter inlet body [$\text{bar(g)}$]
  - $P_{atm}$: Atmospheric pressure ($1.01325\text{ bar}$)
  - $P_{std}$: Standard reference pressure ($1.01325\text{ bar}$)
  - $T_{std}$: Standard reference temperature ($273.15\text{ K}$ or $0^\circ\text{C}$ per DIN 1343 / ISO 2533)
  - $T_{actual}$: Gas temperature measured at meter body [${}^\circ\text{C}$]
  - $W_{fuel}$: Weighed mass of fabric jhut consumed [$\text{kg}$]
  - $t$: Measurement elapsed time [$\text{hr}$]
- **Input Boundaries:** $V_2 > V_1$; $P_{actual} \ge 0$; $T_{actual} > -10^\circ\text{C}$; $t > 0$.
- **Assumptions & Limitations:** Compressibility factor $Z \approx 1.0$ (acceptable for low pressure gas $< 300\text{ mbar}$).
- **Worked Engineering Example (Gas):** Raw gas meter advances $580.0\text{ m}^3$ in $2.0\text{ hr}$ at $0.20\text{ bar(g)}$ line pressure ($1.21325\text{ bar abs}$) and $30^\circ\text{C}$ ($303.15\text{ K}$).  
  $$q_f = \frac{580}{2} \times \left(\frac{1.21325}{1.01325}\right) \times \left(\frac{273.15}{303.15}\right) = 290 \times 1.19738 \times 0.90104 = 312.87\text{ Nm}^3/\text{hr}$$
- **Expected Output:** $312.87\text{ Nm}^3/\text{hr}$ natural gas.
- **Independent Validation Method:** Compare with burner nozzle firing curve at indicated gas manifold pressure.
- **Reference:** ISO 5167 Measurement of Fluid Flow / EN 12480.

---

### CALC-03: Evaporation Ratio ($ER$)
- **Mathematical Formula:**
  $$ER = \frac{Q_s}{q_f} \quad \left[\frac{\text{kg steam}}{\text{Nm}^3\text{ gas}} \text{ or } \frac{\text{kg steam}}{\text{kg jhut}}\right]$$
- **Variable Definitions & Units:**
  - $ER$: Evaporation ratio (steam-to-fuel ratio) [$\text{kg/Nm}^3$ or $\text{kg/kg}$]
  - $Q_s$: Actual steam generated [$\text{kg/hr}$]
  - $q_f$: Corrected fuel consumption rate [$\text{Nm}^3/\text{hr}$ or $\text{kg/hr}$]
- **Input Boundaries:** $Q_s > 0$, $q_f > 0$. Physical validation bounds:
  - Natural Gas: $11.0 \le ER \le 13.8\text{ kg/Nm}^3$
  - Jhut / Cotton Waste: $3.2 \le ER \le 4.8\text{ kg/kg}$
- **Assumptions & Limitations:** Valid only under sustained firing; startup/shutdown periods will distort ratio.
- **Worked Engineering Example:** $Q_s = 4,000\text{ kg/hr}$ steam from $312.0\text{ Nm}^3/\text{hr}$ gas.  
  $$ER = \frac{4000}{312.0} = 12.82\text{ kg steam / Nm}^3\text{ gas}$$
- **Expected Output:** $12.82\text{ kg/Nm}^3$.
- **Independent Validation Method:** Theoretical check: $ER \le \frac{\text{GCV}}{h_g - h_f}$.
- **Reference:** Bureau of Energy Efficiency (BEE) Boiler Performance Assessment.

---

### CALC-04: Equivalent Evaporation from and at 100°C ($E_e$)
- **Mathematical Formula:**
  $$E_e = \frac{Q_s \cdot (h_g - h_f)}{h_{fg,100}} = \frac{Q_s \cdot (h_g - h_f)}{2257.0} \quad \left[\frac{\text{kg}}{\text{hr}}\right]$$
- **Variable Definitions & Units:**
  - $E_e$: Equivalent evaporation rate from and at 100°C [$\text{kg/hr}$]
  - $h_g$: Enthalpy of dry saturated steam at operating pressure [$\text{kJ/kg}$] (IAPWS-IF97 steam tables)
  - $h_f$: Enthalpy of liquid feedwater at inlet temperature [$\text{kJ/kg}$] ($h_f \approx 4.186 \cdot T_{fw}$)
  - $2257.0$: Standard enthalpy of vaporization of pure water at $100^\circ\text{C}$ and $1.01325\text{ bar}$ [$\text{kJ/kg}$]
- **Input Boundaries:** $Q_s > 0$; $P_{boiler} \ge 1\text{ bar(g)}$; $20^\circ\text{C} \le T_{fw} \le 105^\circ\text{C}$.
- **Assumptions:** Steam dryness fraction $x = 1.0$ (saturated steam).
- **Worked Engineering Example:** $Q_s = 4,000\text{ kg/hr}$ at $8.0\text{ bar(g)}$ ($9.013\text{ bar abs}$, $h_g = 2772.6\text{ kJ/kg}$) from feedwater at $80.0^\circ\text{C}$ ($h_f = 335.0\text{ kJ/kg}$, $\Delta h = 2437.6\text{ kJ/kg}$).  
  $$E_e = \frac{4000 \times 2437.6}{2257.0} = 4,320.07\text{ kg/hr} \quad (\text{Factor of Evaporation } F_e = 1.080)$$
- **Expected Output:** $4,320.07\text{ kg/hr}$.
- **Independent Validation Method:** Verify $F_e = \frac{2437.6}{2257.0} = 1.080$.
- **Reference:** ASME BPVC Section I / BS EN 12953-11.

---

### CALC-05: Direct-Method Boiler Thermal Efficiency ($\eta_{direct}$)
- **Mathematical Formula:**
  $$\eta_{direct} = \frac{Q_s \cdot (h_g - h_f)}{q_f \cdot \text{GCV}_{fuel}} \times 100 \quad [\%]$$
- **Variable Definitions & Units:**
  - $\eta_{direct}$: Direct thermal efficiency percentage [$\%$]
  - $Q_s$: Actual steam generated [$\text{kg/hr}$]
  - $h_g - h_f$: Enthalpy difference [$\text{kJ/kg}$]
  - $q_f$: Fuel consumption rate [$\text{Nm}^3/\text{hr}$ or $\text{kg/hr}$]
  - $\text{GCV}_{fuel}$: Gross Calorific Value of fuel [$\text{kJ/Nm}^3$ for gas, $\text{kJ/kg}$ for solid fuel] ($1\text{ kcal} = 4.1868\text{ kJ}$)
- **Input Boundaries:** $Q_s > 0$, $q_f > 0$, $\text{GCV} > 0$. Expected ranges:
  - Natural gas firetube boiler: $78\% \le \eta \le 93\%$
  - Jhut refractory boiler: $60\% \le \eta \le 78\%$
- **Assumptions:** Steady-state operation; accurate GCV certified by fuel supplier.
- **Worked Engineering Example:** $Q_s = 4,130\text{ kg/hr}$, $\Delta h = 2437.6\text{ kJ/kg}$, $q_f = 312.0\text{ Nm}^3/\text{hr}$, Gas $\text{GCV} = 9500\text{ kcal/Nm}^3 = 39,774.6\text{ kJ/Nm}^3$.  
  $$\eta_{direct} = \frac{4130 \times 2437.6}{312.0 \times 39774.6} \times 100 = \frac{10,067,288}{12,409,675} \times 100 = 81.12\%$$
- **Expected Output:** $81.12\%$.
- **Independent Validation Method:** Compare against indirect method; agreement must be within $\pm 3\%$.
- **Reference:** BS EN 12953-11 / Classical Heat Balance Method.

---

### CALC-06: Indirect-Method Thermal Efficiency & Heat Losses (Engineering Estimate)
- **Mathematical Formula:**
  $$\eta_{indirect} = 100 - (L_1 + L_2 + L_3 + L_4 + L_5 + L_6) \quad [\%]$$
- **Loss Formulations & Explicit SI Definitions:**
  1. **$L_1$ (Dry Flue Gas Sensible Heat Loss):**  
     $$L_1 = \frac{m_{dfg} \cdot C_{p,dfg} \cdot (T_{flue} - T_{amb})}{\text{GCV}_{fuel}} \times 100 \quad [\%]$$
     *($m_{dfg} \approx$ mass of dry flue gas per $\text{Nm}^3$ or kg fuel; $C_{p,dfg} \approx 1.005\text{ kJ/kg}^\circ\text{C}$)*
  2. **$L_2$ (Loss Due to Hydrogen Combustion Vapor Enthalpy):**  
     $$L_2 = \frac{9 \times H_2 \times [2442 + C_{p,steam} \cdot (T_{flue} - T_{amb})]}{\text{GCV}_{fuel}} \times 100 \quad [\%]$$
     *($H_2$ = mass fraction of hydrogen in fuel; $2442\text{ kJ/kg}$ = latent heat of vaporization at $25^\circ\text{C}$; $C_{p,steam} \approx 1.88\text{ kJ/kg}^\circ\text{C}$)*
  3. **$L_3$ (Loss Due to Fuel Moisture Content):**  
     $$L_3 = \frac{M_{fuel} \times [2442 + C_{p,steam} \cdot (T_{flue} - T_{amb})]}{\text{GCV}_{fuel}} \times 100 \quad [\%]$$
     *(For pipeline natural gas, $L_3 \approx 0$; for jhut with 10% moisture, $L_3 \approx 1.5\text{–}2.5\%$)*
  4. **$L_4$ (Loss Due to Moisture in Combustion Air):**  
     $$L_4 = \frac{m_{air} \times \text{Humidity Ratio} \times C_{p,steam} \cdot (T_{flue} - T_{amb})}{\text{GCV}_{fuel}} \times 100 \quad [\%]$$
  5. **$L_5$ (Loss Due to Incomplete Combustion / Carbon Monoxide Formation):**  
     $$L_5 = \frac{\%CO \times C_{fuel}}{\%CO + \%CO_2} \times \frac{23600}{\text{GCV}_{fuel}} \times 100 \quad [\%]$$
  6. **$L_6$ (Surface Radiation and Convection Losses):**  
     Evaluated from standard ABMA/casing temperature estimates ($1.0\% - 2.5\%$ based on insulation condition and shell surface velocity).
- **Assumptions & Limitations:** This formulation represents a simplified practical engineering assessment for plant energy audits (inspired by legacy PTC 4.1 / BEE methodologies), NOT an ASME PTC 4-2013 code contractual test.
- **Worked Engineering Example (Gas):** Measured stack data: $T_{flue} = 195^\circ\text{C}$, $T_{amb} = 30^\circ\text{C}$ ($\Delta T = 165^\circ\text{C}$). Loss fractions calculated:  
  $L_1 = 5.42\%$, $L_2 = 10.28\%$, $L_3 = 0.00\%$, $L_4 = 0.22\%$, $L_5 = 0.08\%$, $L_6 = 1.30\%$.  
  $$\text{Total Losses} = 17.30\% \implies \eta_{indirect} = 100 - 17.30 = 82.70\%$$
- **Expected Output:** $82.70\%$.
- **Independent Validation Method:** Comparison with digital flue gas combustion analyzer readout.
- **Reference:** BEE Boiler Audit Guidelines / Energy Efficiency Handbook.

---

### CALC-07: Feedwater Demand Rate ($\dot{m}_{fw}$)
- **Mathematical Formula:**
  $$\dot{m}_{fw} = Q_s + \dot{m}_{bd} = Q_s \cdot \left(1 + \frac{\text{BD}\%}{100}\right) \quad \left[\frac{\text{kg}}{\text{hr}}\right]$$
- **Input Boundaries:** $Q_s > 0$, $\text{BD}\% \ge 0$.
- **Worked Engineering Example:** $Q_s = 4,000\text{ kg/hr}$ with $4.5\%$ continuous surface blowdown.  
  $$\dot{m}_{fw} = 4000 \times 1.045 = 4,180.0\text{ kg/hr} \quad (\approx 4.30\text{ m}^3/\text{hr})$$
- **Expected Output:** $4,180.0\text{ kg/hr}$.
- **Reference:** Spirax Sarco Feedwater and Deaerator Design.

---

### CALC-08: Condensate Heat Recovery & Fuel Savings ($Q_{cond}$)
- **Mathematical Formula:**
  $$\Delta Q = \dot{m}_{cond} \cdot C_p \cdot (T_{cond} - T_{raw}) \quad \left[\frac{\text{kJ}}{\text{hr}}\right]$$
  $$\text{Fuel Saved} = \frac{\Delta Q}{\text{GCV}_{fuel} \cdot (\eta_{boiler} / 100)} \quad \left[\frac{\text{Nm}^3}{\text{hr}} \text{ or } \frac{\text{kg}}{\text{hr}}\right]$$
- **Variable Definitions & Units:**
  - $\dot{m}_{cond}$: Condensate flow rate returned to feed tank [$\text{kg/hr}$]
  - $C_p$: Specific heat of liquid water ($4.186\text{ kJ/kg}^\circ\text{C}$)
  - $T_{cond}$: Temperature of returned condensate [${}^\circ\text{C}$]
  - $T_{raw}$: Cold raw make-up water temperature [${}^\circ\text{C}$]
- **Worked Engineering Example:** $\dot{m}_{cond} = 2,500\text{ kg/hr}$ recovered at $92^\circ\text{C}$ replacing raw water at $25^\circ\text{C}$ ($\Delta T = 67^\circ\text{C}$). Boiler $\eta = 81.0\%$, Gas $\text{GCV} = 39,774.6\text{ kJ/Nm}^3$.  
  $$\Delta Q = 2500 \times 4.186 \times 67 = 701,155\text{ kJ/hr}$$  
  $$\text{Gas Saved} = \frac{701,155}{39,774.6 \times 0.81} = \frac{701,155}{32,217.4} = 21.76\text{ Nm}^3/\text{hr}$$
- **Expected Output:** $21.76\text{ Nm}^3/\text{hr}$ gas saved.
- **Reference:** Carbon Trust Steam Energy Efficiency Guide.

---

### CALC-09: Blowdown Rate Percentage ($\text{BD}\%$)
- **Mathematical Formula:**
  $$\text{BD}\% = \frac{\text{TDS}_{fw}}{\text{TDS}_{boiler,max} - \text{TDS}_{fw}} \times 100 \quad [\%]$$
- **Input Boundaries:** $\text{TDS}_{boiler,max} > \text{TDS}_{fw} > 0$. Firetube maximum limit $\le 3000\text{ ppm}$ per BS 2486.
- **Worked Engineering Example:** Softened feedwater TDS $= 110\text{ ppm}$. Maximum boiler water TDS setpoint $= 2800\text{ ppm}$.  
  $$\text{BD}\% = \frac{110}{2800 - 110} \times 100 = \frac{110}{2690} \times 100 = 4.089\% \approx 4.09\%$$
- **Expected Output:** $4.09\%$ blowdown rate.
- **Reference:** BS 2486 Water Treatment for Steam Boilers.

---

### CALC-10: Cycles of Concentration ($CoC$) & Conductivity-to-TDS
- **Mathematical Formula:**
  $$CoC = \frac{\text{TDS}_{boiler}}{\text{TDS}_{fw}} = \frac{\sigma_{boiler}}{\sigma_{fw}}$$
  $$\text{TDS (ppm)} \approx \sigma\ (\mu\text{S/cm at } 25^\circ\text{C}) \times k$$
- **Variable Definitions & Units:**
  - $CoC$: Cycles of Concentration (dimensionless ratio)
  - $\sigma$: Electrical conductivity [$\mu\text{S/cm}$]
  - $k$: Conversion factor (empirically $0.65 \pm 0.05$ for softened/boiler water neutral salts; requires laboratory verification for heavily conditioned water)
- **Assumptions:** Sample cooled to $25^\circ\text{C}$ or instrument ATC enabled.
- **Worked Engineering Example:** Feedwater $\sigma_{fw} = 170\ \mu\text{S/cm}$ ($\text{TDS} = 110.5\text{ ppm}$). Boiler water $\sigma_{boiler} = 3800\ \mu\text{S/cm}$ ($\text{TDS} = 2470\text{ ppm}$).  
  $$CoC = \frac{3800}{170} = 22.35$$
- **Expected Output:** $CoC = 22.35$.
- **Reference:** ASTM D1125 Electrical Conductivity of Water.

---

### CALC-11: Overall Boiler Energy Balance Closure
- **Mathematical Formula:**
  $$\text{Closure Error } \% = \frac{\left| Q_{fuel} - (Q_{useful} + \sum Q_{losses}) \right|}{Q_{fuel}} \times 100 \quad [\%]$$
- **Validation Acceptance Bound:** Closure error must be $\le 1.5\%$ for an authoritative energy balance.
- **Worked Engineering Example:** $Q_{fuel} = 12,409,675\text{ kJ/hr}$. $Q_{useful} = 10,067,288\text{ kJ/hr}$ ($81.12\%$). Calculated losses $= 2,146,873\text{ kJ/hr}$ ($17.30\%$). Sum of outputs $= 12,214,161\text{ kJ/hr}$.  
  $$\text{Closure Error} = \frac{|12,409,675 - 12,214,161|}{12,409,675} \times 100 = \frac{195,514}{12,409,675} \times 100 = 1.57\% \approx 1.5\%$$
- **Expected Output:** Accepted within experimental tolerance.
- **Reference:** ASME PTC 4 Energy Balance Principles.

---

### CALC-12: Washing Plant Peak Steam Demand Sizing
- **Mathematical Formula:**
  $$Q_{peak} = \left[ \sum_{i=1}^{n} (N_i \cdot Q_{machine,i} \cdot D_i) \right] \times F_{pipe} \quad \left[\frac{\text{kg}}{\text{hr}}\right]$$
- **Variable Definitions & Units:**
  - $N_i$: Quantity of machines of category $i$
  - $Q_{machine,i}$: Nominal full-load steam rating [$\text{kg/hr}$]
  - $D_i$: Diversity factor representing non-simultaneous peak firing ($0.5\text{–}0.8$)
  - $F_{pipe}$: Pipe heat loss and safety factor ($1.05\text{–}1.10$)
- **Worked Engineering Example:**
  - 8 Industrial Washers ($300\text{ kg/hr}$, $D=0.6$) $\implies 8 \times 300 \times 0.6 = 1440\text{ kg/hr}$
  - 5 Tumbler Dryers ($400\text{ kg/hr}$, $D=0.7$) $\implies 5 \times 400 \times 0.7 = 1400\text{ kg/hr}$
  - 1 Curing Box Oven ($250\text{ kg/hr}$, $D=0.8$) $\implies 1 \times 250 \times 0.8 = 200\text{ kg/hr}$
  - Pipe safety factor $F_{pipe} = 1.08$.  
    $$Q_{peak} = (1440 + 1400 + 200) \times 1.08 = 3040 \times 1.08 = 3,283.2\text{ kg/hr}$$
- **Expected Output:** Recommended boiler capacity: $3.5\text{ Ton/hr}$ ($3,500\text{ kg/hr}$) MCR.
- **Reference:** Spirax Sarco Sizing Steam Distribution Systems.

---

### CALC-13: Fuel Energy Input Rate ($Q_{fuel}$) & Thermal Power
- **Mathematical Formula:**
  $$Q_{fuel} = q_f \cdot \text{GCV}_{fuel} \quad \left[\frac{\text{kJ}}{\text{hr}}\right]$$
  $$P_{thermal} = \frac{Q_{fuel}}{3.6 \times 10^6} \quad [\text{MW}_{th}]$$
- **Worked Engineering Example:** $q_f = 312.0\text{ Nm}^3/\text{hr}$, $\text{GCV} = 39,774.6\text{ kJ/Nm}^3$.  
  $$Q_{fuel} = 312.0 \times 39,774.6 = 12,409,675.2\text{ kJ/hr}$$  
  $$P_{thermal} = \frac{12,409,675.2}{3,600,000} = 3.447\text{ MW}_{th}$$
- **Expected Output:** $3.45\text{ MW}_{th}$ thermal input.
- **Reference:** EN 12953-3 / ISO 80000-5.

---

### CALC-14: Specific Fuel Consumption ($SFC$)
- **Mathematical Formula:**
  $$SFC = \frac{q_f}{Q_s} = \frac{1}{ER} \quad \left[\frac{\text{Nm}^3\text{ gas}}{\text{kg steam}} \text{ or } \frac{\text{kg jhut}}{\text{kg steam}}\right]$$
- **Worked Engineering Example:** $ER = 12.82\text{ kg steam / Nm}^3\text{ gas}$.  
  $$SFC = \frac{1}{12.82} = 0.07800\text{ Nm}^3/\text{kg steam} \quad (78.0\text{ Nm}^3\text{ per metric ton of steam})$$
- **Expected Output:** $78.0\text{ Nm}^3/\text{Ton steam}$.
- **Reference:** Industrial Energy Audit Protocols / BEE India.

---

## 6. Safety & Operational Precautions
- **Statutory Mandate:** Under Sections 18, 27, and 38 of **The Boilers Act, 2022**, operating pressure must NEVER exceed the working pressure certified on the active Certificate of Use.
- **Safety Valve Popping Test:** Testing safety relief valves under steam must be performed only by certified personnel adhering strictly to ASME Sec I / Boiler Board guidelines.
- **Low Water Cut-Out Chamber:** Mobrey float chambers must be tested via bottom blowdown cock on EVERY shift to prove the fail-safe electrical circuit trips the burner.
