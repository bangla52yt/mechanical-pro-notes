# PHASE 06 TEST RESULTS
## Automated Verification Log for Field Calculations, Thermodynamic Engine & Room Database

**Document Identifier:** DOC-MPN-017  
**Project:** Mechanical Pro Notes (Personal Offline-First Mechanical Reference Android App)  
**Phase:** Phase 06 — Field Calculations, Boiler Thermal Engineering & Maintenance Tools  
**Status:** ALL TESTS PASSED (10/10, 100%)  
**Execution Timestamp:** 12 November 2026, 01:43:40 PST  
**Test Harness:** `test_suite_phase06.py` + `test_suite_phase05.py`  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Test Execution Summary

| Test ID | Test Category | Target Component | Assertions | Result |
|---|---|---|---|---|
| **P6-T01** | Database Registration | Room SQLite `calculation_definitions` | Verify all 20 calculation modules registered (`CALC-01` to `CALC-20`) | **PASS** |
| **P6-T02** | Variable Bounds & Units | Room SQLite `formula_variables` | Verify 79 formula variables across all 20 calculators with min/max bounds | **PASS** |
| **P6-T03** | Thermodynamic Precision | `IapwsIf97SteamEngine.kt` | Validated at 0.5, 1.0, 5.0, 10.0, 15.0, 20.0 bar; gauge-to-absolute conversion | **PASS** |
| **P6-T04** | Boiler Thermal Physics | `BoilerThermalCalculations.kt` | Verify CALC-01 through CALC-07 (Efficiency, Firing, Blowdown, Pump) | **PASS** |
| **P6-T05** | Boiler Utility Physics | `BoilerThermalCalculations.kt` | Verify CALC-08 through CALC-14 (Deaerator, Safety Valve, Economizer, Fans, Stack, Pipe) | **PASS** |
| **P6-T06** | Wash Plant Tools | `WashPlantMaintenanceCalculations.kt`| Verify CALC-15 through CALC-20 (V-Belt, Hydro Extractor, Batch Steam, Dryer, Softener, Air) | **PASS** |
| **P6-T07** | Mathematical Robustness | Defensive Exception Handling | Zero-division prevention, negative input rejection, bounds enforcement | **PASS** |
| **P6-T08** | Offline Security Audit | `AndroidManifest.xml` (**DEC-016**) | Verify complete absence of `android.permission.INTERNET` | **PASS** |
| **P6-T09** | User Data Isolation | SQLite `calculation_history` (**DEC-014**) | Verify personal calculation logs isolated without cascade interference | **PASS** |
| **P6-T10** | Regression Suite | Phase 05 UI & Navigation Suite | Execute all 10 Phase 05 tests to confirm zero backward regressions | **PASS** |

**Overall Score:** 10 / 10 Tests Passed (100.0%)

---

## 2. Detailed Test Case Audit Logs

### P6-T01: 20 Calculation Registrations in Room SQLite Database
- **Methodology:** Queries `calculation_definitions` table in `android/app/src/main/assets/database/mechanical_pro_notes.db`.
- **Observed Output:**
  ```
  Registered IDs: CALC-01, CALC-02, CALC-03, CALC-04, CALC-05, CALC-06, CALC-07, CALC-08, CALC-09, CALC-10, CALC-11, CALC-12, CALC-13, CALC-14, CALC-15, CALC-16, CALC-17, CALC-18, CALC-19, CALC-20
  ```
- **Conclusion:** 100% of required calculation modules exist and match data model constraints.

### P6-T02: Formula Variables, Units & Physical Input Bounds
- **Methodology:** Traverses all formula variables linked to calculations via foreign keys.
- **Observed Metrics:**
  - Total Verified Variables: 79 variables.
  - Bounds Integrity: 79 / 79 variables have `min_value < max_value` and valid default values within `[min_value, max_value]`.
  - Units Standard: Verified SI and plant-floor units ($kg/h, Nm^3/h, bar, ^\circ C, kW, mm, CFM$).

### P6-T03: IAPWS-IF97 Thermodynamic Precision
- **Methodology:** Evaluates saturation temperature, specific liquid enthalpy, latent heat, and vapor volume across $0.5$ to $20.0\text{ bar(a)}$.
- **Benchmark Assertions:**
  - At $1.01325\text{ bar(a)}$: $T_{sat} = 100.00^\circ\text{C}$, $h_f = 419.10\text{ kJ/kg}$, $h_{fg} = 2256.70\text{ kJ/kg}$, $h_g = 2675.80\text{ kJ/kg}$.
  - At $8.01325\text{ bar(a)}$ ($7\text{ bar g}$): $T_{sat} = 170.50^\circ\text{C}$, $h_g = 2768.47\text{ kJ/kg}$.
  - Gauge-to-Absolute conversion check: $7.0\text{ bar(g)} + 1.01325 \implies 8.01325\text{ bar(a)} \implies T_{sat} = 170.50^\circ\text{C}$.
  - Identity check: $|h_g - (h_f + h_{fg})| < 10^{-6}\text{ kJ/kg}$ across all points.

### P6-T04: Boiler Thermal Calculations (CALC-01 to CALC-07)
- **CALC-01 Direct Thermal Efficiency:** $m_s=4000\text{ kg/h}, h_s=2763.5, h_w=355.0, m_f=315\text{ Nm}^3\text{/h}, \text{NCV}=35800 \implies \eta = 85.30\%$.
- **CALC-03 Firing Rate:** $m_s=5000\text{ kg/h}, P=8\text{ bar(g)}, T_w=85^\circ\text{C}, \text{NCV}=35800, \eta=86\% \implies \dot{m}_f = 389.92\text{ Nm}^3\text{/h}$.
- **CALC-04 Evaporation Ratio:** $m_s=30000\text{ kg}, m_f=2400\text{ Nm}^3 \implies \text{ER} = 12.50\text{ kg/Nm}^3$.
- **CALC-05 Continuous Blowdown:** $\text{TDS}_{feed}=150\text{ ppm}, \text{TDS}_{boiler}=3500\text{ ppm} \implies \%B = 4.48\%$.
- **CALC-06 Flash Steam Recovery:** $\dot{m}_B=200\text{ kg/h}, P_{boiler}=8\text{ bar(g)}, P_{flash}=0.5\text{ bar(g)} \implies \dot{m}_{flash} = 28.54\text{ kg/h}$.
- **CALC-07 Feedwater Pump Power:** $Q=5.5\text{ m}^3\text{/h}, H=130\text{ m}, \eta_{pump}=0.65 \implies P_{brake} = 3.14\text{ kW}$.

### P6-T05: Boiler Utility Calculations (CALC-08 to CALC-14)
- **CALC-08 Deaerator Balance:** $\dot{m}_{make}=2500, T_{make}=28, \dot{m}_{cond}=2500, T_{cond}=80, P_{da}=0.2\text{ bar(g)} \implies \dot{m}_{steam} = 735.72\text{ kg/h}$.
- **CALC-09 Safety Valve Capacity:** $P_{set}=10\text{ bar(g)}, d=32\text{ mm}, K_d=0.85 \implies W = 2869.49\text{ kg/h}$ ($> \text{MCR } 2500\text{ kg/h} \implies \text{PASS}$).
- **CALC-10 Economizer Recovery:** Flue gas $230 \to 150^\circ\text{C}, \dot{m}_w=4000\text{ kg/h} \implies \dot{Q} = 105.00\text{ kW}, \Delta T_w = 22.55^\circ\text{C}$.
- **CALC-11 FD Fan Airflow:** Gas $320\text{ Nm}^3\text{/h}, \text{EA}=15\% \implies Q_{fan} = 60.11\text{ m}^3\text{/min}$.
- **CALC-12 ID Fan Capacity:** Fuel $500\text{ kg/h}, T_{flue}=160^\circ\text{C} \implies Q_{id} = 106.29\text{ m}^3\text{/min}$.
- **CALC-13 Stack Draught:** $H=30\text{ m}, T_{gas}=160^\circ\text{C}, T_{amb}=30^\circ\text{C} \implies \Delta P = 11.56\text{ mm WC}$.
- **CALC-14 Steam Pipe Sizing:** $\dot{m}_s=3000\text{ kg/h}, P=7\text{ bar(g)}, V=25\text{ m/s} \implies d_{inner} = 92.21\text{ mm}$ (Recommended DN100).

### P6-T06: Wash Plant Maintenance Tools (CALC-15 to CALC-20)
- **CALC-15 V-Belt Pitch Length:** $d=140\text{ mm}, D=420\text{ mm}, C=800\text{ mm} \implies L_p = 2539.44\text{ mm}$ (Belt B98/B100, Ratio 3.0:1).
- **CALC-16 Hydro Extractor G-Force:** $D=1.20\text{ m}, N=850\text{ RPM} \implies G = 484.76\text{ G}$, Rim Speed $= 53.41\text{ m/s}$.
- **CALC-17 Wash Batch Steam:** Dry load $150\text{ kg}, L:R=1:6, 28 \to 65^\circ\text{C} \implies m_{steam} = 81.18\text{ kg steam/batch}$.
- **CALC-18 Tumble Dryer Evaporation:** Wet $250\text{ kg} \to 156\text{ kg}, 35\text{ min} \implies \dot{m}_{evap} = 146.17\text{ kg water/h}$.
- **CALC-19 Water Softener Yield:** $V_{resin}=500\text{ L}, C=1.9\text{ eq/L}, \text{Hardness}=180\text{ ppm} \implies V_{soft} = 147.73\text{ m}^3, M_{salt} = 70.0\text{ kg}$.
- **CALC-20 Compressed Air Drop:** $Q=120\text{ CFM}, L=120\text{ m}, d=40\text{ mm}, P=7\text{ bar(g)} \implies \Delta P \approx 0.00\text{ bar}$ (optimal oversized header).

### P6-T07: Mathematical Robustness & Sanity Guards
- Verified exception triggers for zero-division, invalid pressure inputs, inverted temperature inputs ($T_{initial} > T_{target}$), and negative values.

### P6-T08: DEC-016 Zero Network Permission Verification
- Audited `AndroidManifest.xml`. Confirmed zero `<uses-permission android:name="android.permission.INTERNET">` declarations.

### P6-T09: DEC-014 Personal User Data Isolation
- Executed isolated insert and select against `calculation_history` table in a transaction roll-back sandbox. Static data was 100% unaffected.

### P6-T10: Regression Verification with Phase 05 Test Suite
- Executed `test_suite_phase05.py`. All 10 Phase 05 tests passed cleanly with 100% success rate.
