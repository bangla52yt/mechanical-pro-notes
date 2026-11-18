# STAGE 02 ENGINEERING CALCULATION VALIDATION REPORT
**Document Identifier:** DOC-MPN-022  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Scope:** Stage 2 — Engineering Calculation Integrity & Validation (`CALC-02`, `CALC-06`, `CALC-09`, `CALC-11`, `CALC-19`, `CALC-20`)  
**Date:** 2026-11-14  
**Classification Standard:** Strict Evidence Classification (Code / Static / Python-Verified vs. Unexecuted JVM & Device Checks)  

---

## 1. Executive Summary

Stage 2 remediated the six engineering calculation integrity gaps carried forward from `COMPLETE_PROJECT_STATUS_AUDIT.md` (`DOC-MPN-020`) while preserving:
- All Stage 1 fixes (`DEF-AUD-01`–`08`),
- Unchanged Room SQLite seed asset `mechanical_pro_notes.db` (`544,768` bytes, SHA-256 `057c02af13cd0b19d16961beb93fead574c84261391290afb5e940c0acb35c4e`, `20` calculations, `79` formula variables),
- Approved IAPWS-IF97 steam table `GRID` and linear-pressure interpolation (`P7-06`), and
- Unrelated calculation modules (`CALC-01, 03, 04, 05, 07, 08, 10, 12, 13, 14, 15, 16, 17, 18`).

| Calc ID | Title | Stage 2 Disposition | Native JVM Status |
|---|---|---|---|
| **CALC-02** | Indirect Boiler Efficiency (Heat Loss) | **RESEARCH_PENDING** (unit-basis & omitted-loss guards added; execution remains locked) | JUnit authored; requires JDK |
| **CALC-06** | Blowdown Flash & Heat Recovery | **IMPLEMENTED_VERIFICATION_PENDING** (`P_boiler > P_flash` enforced; silent `.coerceIn`/`.coerceAtLeast` removed) | JUnit authored; requires JDK |
| **CALC-09** | Safety Valve Relieving Capacity | **IMPLEMENTED_VERIFICATION_PENDING** (`0.51` conservative constant verified vs `0.52515`; `Kd`/`P_set`/`MCR` bounds + throat diameter added) | JUnit authored; requires JDK |
| **CALC-11** | FD Fan & Combustion Air Sizing | **IMPLEMENTED_VERIFICATION_PENDING** (`Nm3/min` Normal vs `m3/min` ambient FAD separated; mass fuel requires `A_0`) | JUnit authored; requires JDK |
| **CALC-19** | Water Softener Resin & Salt Sizing | **IMPLEMENTED_VERIFICATION_PENDING** (fabricated 1-regen/day duplicate removed; conditional daily salt on cycle/flow input) | JUnit authored; requires JDK |
| **CALC-20** | Compressed Air Line Pressure Drop | **IMPLEMENTED_VERIFICATION_PENDING** (positive `P_air` enforced; `0.00075` smooth-bore vs Darcy-Weisbach/Atlas Copco `450` documented) | JUnit authored; requires JDK |

---

## 2. Calculation-by-Calculation Engineering Analysis

### 2.1 `CALC-02` — Indirect Boiler Efficiency (Heat Loss Method)
- **Previous Behavior:** `CalculationSpecifications.kt` and the `formula_variable` table exposed `5` inputs (`T_flue`, `T_ambient`, `CO2`, `GCV` with ambiguous `"kJ/unit"`, `H2_percent`), while `BoilerThermalCalculations.executeCalc02` silently defaulted `CO2 = 9.5`, `H2_percent = 23.0`, `M_fuel = 0.0`, `L_unburnt = 0.2`, `L_radiation = 1.0`, `kFactor = 0.38`, and `theoreticalAir = 10.0`. `L2`, `L3`, and `L4` combined mass-basis numerators (`kJ/kg fuel`) with unvalidated `GCV` (`kJ/unit`).
- **Identified Issue:**
  1. **Dimensional Mismatch:** Supplying natural gas volumetric `GCV = 39,800 kJ/Nm3` directly into `L2 = 9 * (H2%/100) * (2442 + 2.09*ΔT) / GCV * 100` mixes `kJ/kg fuel` with `kJ/Nm3 fuel`, inflating `L2` and `L4` by $1/\rho_{\text{gas}} \approx 1.35\times$ (`14.39%` vs `10.65%` at $\rho = 0.74\text{ kg/Nm}^3$).
  2. **5-of-8 Input Gap & Unsupplied Fuel Constants:** `CO2` and `H2_percent` had silent fallback defaults; `M_fuel`, `L_unburnt`, `L_radiation`, `siegert_k`, and `theoretical_air` were assumed without user confirmation.
- **Reference / Derivation (ASME PTC 4 / BS 845 / IS 8753):**
  - Gravimetric conversion for gaseous fuels: $\text{GCV}_{\text{mass}} = \text{GCV}_{\text{vol}} / \rho_{\text{gas}}$ $[\text{kJ/kg}]$, where pipeline natural gas $\rho_{\text{gas}} \approx 0.72\text{–}0.80\text{ kg/Nm}^3$.
  - $L_1 = K \cdot (T_{\text{flue}} - T_{\text{amb}}) / \%CO_2$ ($K = 0.38$ natural gas dry flue gas only; solid fuels require $K \approx 0.50\text{–}0.68$ and ultimate carbon/flue analysis).
  - $L_2 = \frac{9 \cdot (H_2/100) \cdot (2442 + 2.09\Delta T)}{\text{GCV}_{\text{mass}}} \times 100$, $L_3 = \frac{(M_{\text{fuel}}/100) \cdot (2442 + 2.09\Delta T)}{\text{GCV}_{\text{mass}}} \times 100$, $L_4 = \frac{A_0 \cdot 0.015 \cdot 2.09\Delta T}{\text{GCV}_{\text{mass}}} \times 100$.
- **Exact Implementation Changes:**
  - Enforced strict presence/finiteness validation for `T_flue`, `T_ambient`, `CO2` ($1\text{–}20\%$), `GCV`, and `H2_percent` ($0\text{–}30\%$) — removed silent `9.5` and `23.0` defaults.
  - Added optional unit governance controls (`gcv_unit_mode`: `1.0 = kJ/kg`, `2.0 = kJ/Nm3`; `fuel_type`: `1.0 = Gas`, `2.0 = Solid/Liquid`; `fuel_density` `0.55..1.50 kg/Nm3`; `siegert_k`; `theoretical_air`). Volumetric `gcv_unit_mode = 2.0` requires `fuel_density` and rejects incompatible `fuel_type = 2.0`. Solid fuel (`fuel_type = 2.0`) requires explicit `siegert_k` and `theoretical_air`.
  - Un-supplied losses (`M_fuel`, `L_unburnt`, `L_radiation`) are excluded (`0.0%`) and explicitly disclosed in `notesAndRecommendations` rather than silently fabricated.
  - Retained `CalculationStatus.RESEARCH_PENDING` across `CalculationSpecifications.kt`, `executeCalc02`, and Room DB (`CalculationViewModel` disables UI execution).
- **Assumptions & Limitations:** 5-input Room UI omits moisture, CO/unburnt carbon, radiation curve, fuel density, and solid-fuel Siegert factors; remains unsafe for non-gas/jhut audits without laboratory ultimate analysis and calibrated $\text{O}_2/\text{CO}_2/\text{CO}$ measurements.
- **Test Cases & Results:** `Stage02CalculationValidationTest` tests volumetric gas conversion (`39800 / 0.74 -> L2 = 10.65%`), missing density rejection, solid-fuel volumetric GCV rejection, missing `CO2`/`H2_percent` rejection, and `RESEARCH_PENDING` lock. Python audit (`S2-02`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `RESEARCH_PENDING`

---

### 2.2 `CALC-06` — Blowdown Flash Steam & Heat Recovery
- **Previous Behavior:** `BoilerThermalCalculations.executeCalc06` allowed `P_boiler <= P_flash` and silently clamped `flashFraction = ((hfBoiler - hfFlash) / hfgFlash).coerceIn(0.0, 0.40)` and `recoverableHeatKw.coerceAtLeast(0.0)`, while hiding `eta_hex = 0.85` and `+ 41.86` offset.
- **Identified Issue:** Invalid pressure inversions (`P_boiler <= P_flash`) returned `0` flash instead of an error; high blowdown heat recovery could be distorted by silent clamping; hot make-up water (`T_makeup + 10°C >= T_sat(P_flash)`) was clamped to `0.0 kW` instead of catching a 2nd-law HX approach violation.
- **Reference / Derivation (Spirax Sarco Flash & Heat Recovery Mass-Energy Balance):**
  $$x_{\text{flash}} = \frac{h_{f,\text{boiler}} - h_{f,\text{flash}}}{h_{fg,\text{flash}}}, \quad \dot{m}_{\text{flash}} = \dot{m}_{\text{bd}} \cdot x_{\text{flash}}, \quad \dot{m}_{\text{res}} = \dot{m}_{\text{bd}} - \dot{m}_{\text{flash}}$$
  $$Q_{\text{HX}} = \frac{\dot{m}_{\text{res}} \cdot \left(h_{f,\text{flash}} - c_p \cdot (T_{\text{makeup}} + \Delta T_{\text{app}})\right) \cdot \eta_{\text{hex}}}{3600}\text{ [kW]}, \quad \Delta T_{\text{app}} = 10^\circ\text{C} \Rightarrow 41.86\text{ kJ/kg}$$
- **Exact Implementation Changes:**
  - Enforced `require(pBoilerBar > pFlashBar)`, non-negative `P_flash` & `T_makeup`, `eta_hex in 0.10..1.0`, `hfBoiler > hfFlash`, and `T_makeup + 10.0 < T_sat(P_flash)`.
  - Removed `.coerceIn(0.0, 0.40)` and `.coerceAtLeast(0.0)` completely.
  - Added `"Flash Steam Latent Power (kW)"` to `secondaryOutputs` and documented `eta_hex` + `10°C` cold-end pinch in `notesAndRecommendations` and `CalculationSpecifications.kt` (updating `P_boiler` spec maxBound note to `18.98 bar g` steam-engine ceiling).
- **Assumptions & Limitations:** Continuous blowdown flash vessel + shell/plate heat exchanger (`eta_hex = 0.85` default, `10°C` cold-end pinch, saturated liquid $c_p \approx 4.186\text{ kJ/(kg}\cdot\text{°C)}$); valid up to `18.98 bar(g)` steam-table ceiling; not applicable to intermittent bottom sludge blowdown.
- **Test Cases & Results:** Normal case (`250 kg/h`, `8 bar g`, `0.5 bar g`, `25°C`) gives `28.54 kg/h` flash (`17.64 kW` latent) and `16.77 kW` HX sensible recovery; `P_boiler == P_flash`, `P_boiler < P_flash`, and `T_makeup = 92°C` at `0 bar g` all throw `IllegalArgumentException`. Python audit (`S2-03`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `IMPLEMENTED_VERIFICATION_PENDING`

---

### 2.3 `CALC-09` — Safety Valve Relieving Capacity Verification
- **Previous Behavior:** Used `W = 0.51 * Kd * Area_mm2 * ((P_set + 1.01325) * 1.03)` without bounds validation on `Kd`, `P_set`, or `MCR`, and only accepted `Area (mm2)` without relating it to throat diameter $d$ cited in `DOC-MPN-014`.
- **Identified Issue:** Required independent verification of `0.51` vs `0.52515` (`ASME Section I`), missing parameter validation (`Kd > 1.0`, `Kd <= 0`, negative `MCR`), and clarity on `Area` vs `d_throat`.
- **Reference / Derivation (ASME BPVC Section I PG-67 to PG-73 / Napier Equation):**
  - Imperial ASME Section I saturated steam Napier equation: $W_{\text{lb/h}} = 51.5 \cdot P_{\text{psia}} \cdot A_{\text{in}^2} \cdot K_d \cdot K_b \cdot K_{sh}$.
  - Exact unit conversion to $(\text{kg/h}, \text{bar(a)}, \text{mm}^2)$:
    $$c_{51.5} = 51.5 \times \frac{0.45359237\text{ kg/lb} \times 14.50377377\text{ psi/bar}}{25.4^2\text{ mm}^2/\text{in}^2} = 0.5251535\text{ }\frac{\text{kg}}{\text{h}\cdot\text{bar(a)}\cdot\text{mm}^2}$$
  - When ASME Sec I mandatory $0.90$ certification derating is folded into the constant instead of $K_d$, $0.90 \times 0.52515 = 0.47264$; meanwhile simplified rounded metric Napier ($50\text{ lb/(h}\cdot\text{in}^2\cdot\text{psia)}$) yields $0.50986 \approx 0.51$ (`-2.89%` conservative relative to raw `0.52515`).
  - **Decision:** Preserved `0.51` (`-2.89%` conservative margin) rather than substituting `0.52515`, because user-entered `Kd` (`[0.4, 0.9]`) may reflect either un-derated nozzle coefficient or NB-18 derated coefficient.
  - **Input Review (`Area` vs `d_throat`):** Retained `Area (mm2)` as primary Room/UI input because industrial API/ASME safety valve orifices (D, E, F, G, H, J…) and curtain-lift areas are cataloged directly in $\text{mm}^2$, while supporting optional `"d_throat"` ($\text{mm} \to \pi d^2 / 4$) and outputting `"Equivalent Throat Diameter (mm)"` (`31.92 mm` for `800 mm2`).
- **Exact Implementation Changes:** Validated `P_set in 0.5..35.0 bar g` (dry saturated range below ASME $K_n$ region), `Kd in 0.10..1.0`, non-negative `MCR`, optional `d_throat`, added `"Equivalent Throat Diameter (mm)"` secondary output, and documented `0.51` vs `0.52515` + OCIB statutory restriction.
- **Assumptions & Limitations:** Preliminary estimate assuming dry saturated steam ($K_{sh} = 1.0$), open atmospheric discharge ($K_b = 1.0$), $3\%$ overpressure, and no high-pressure Napier $K_n$ correction. Does not replace OCIB inspector witness certification or manufacturer nameplate stamped capacity.
- **Test Cases & Results:** Reference (`P_set=10`, `Area=800`, `Kd=0.62`) yields `2869.49 kg/h`, `d_eq = 31.92 mm`; matching `d_throat = 31.9154 mm` agrees within `0.5 kg/h`; `Kd = 1.25` throws `IllegalArgumentException`. Python audit (`S2-04`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `IMPLEMENTED_VERIFICATION_PENDING`

---

### 2.4 `CALC-11` — Forced Draft (FD) Fan & Combustion Air Sizing
- **Previous Behavior:** Spec variable labeled `m_fuel` as `"Nm3/h or kg/h"`, while engine hardcoded `theoreticalAirNm3 = 9.8` (Natural Gas volumetric ratio) and divided `actualAirNm3H / 60.0` directly into `"m3/min (FAD)"` while computing fan shaft power without temperature correction.
- **Identified Issue:**
  1. Applying `9.8` to cotton jhut (`kg/h`, $A_0 \approx 4.5\text{–}5.5\text{ Nm}^3/\text{kg}$) or biomass overstates airflow by $\sim 2\times$.
  2. Treating $0^\circ\text{C}$ Normal volume ($\text{Nm}^3/\text{min}$) as ambient FAD ($\text{m}^3/\text{min}$ at $30^\circ\text{C}$) underpredicts fan actual volumetric capacity and motor shaft power by $(303.15/273.15) - 1 = +10.98\%$.
- **Reference / Derivation:**
  $$V_{\text{air,N}} = \dot{m}_{\text{fuel}} \cdot A_0 \cdot \left(1 + \frac{\text{EA}\%}{100}\right)\text{ [Nm}^3\text{/h at }0^\circ\text{C}, 101.325\text{ kPa]}$$
  $$Q_{\text{FAD}} = V_{\text{air,N}} \cdot \frac{T_{\text{amb}} + 273.15}{273.15}, \quad P_{\text{shaft}} = \frac{(Q_{\text{FAD}}/3600) \cdot (\Delta P_{\text{mmWC}} \cdot 9.80665)}{1000 \cdot \eta_{\text{fan}}}\text{ [kW]}$$
- **Exact Implementation Changes:**
  - Updated `CalculationSpecifications.kt` to label default 4-input `m_fuel` clearly as `"Nm3/h"` (Natural Gas default `A_0 = 9.8`).
  - Added explicit guards: if `"fuel_unit_kg" >= 0.5`, user MUST provide explicit `"A_0"` ($2.0\text{–}20.0\text{ Nm}^3/\text{kg}$), else `IllegalArgumentException` prevents Natural Gas `9.8` misuse.
  - Changed primary output explicitly to `"Combustion Air Requirement (Normal 0°C)"` in **`Nm3/min`** (`60.11 Nm3/min` at `320 Nm3/h`, `15% EA`) and added ambient FAD airflow (`66.71 m3/min` at default `30°C` or user `"T_ambient"`) + temperature-corrected fan shaft/motor power in `secondaryOutputs`.
- **Assumptions & Limitations:** Default `A_0 = 9.8` valid for pipeline Natural Gas only; sea-level barometric pressure ($101.325\text{ kPa}$) and $30^\circ\text{C}$ default intake temp unless `T_ambient` is supplied.
- **Test Cases & Results:** Natural gas baseline gives `60.11 Nm3/min` & `66.71 m3/min` FAD (`30°C`); jhut case (`500 kg/h`, `A_0=5.2`, `35% EA`, `35°C`) gives `58.50 Nm3/min` & `65.99 m3/min` FAD; `fuel_unit_kg=1.0` without `A_0` throws `IllegalArgumentException`. Python audit (`S2-05`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `IMPLEMENTED_VERIFICATION_PENDING`

---

### 2.5 `CALC-19` — Water Softener Resin Capacity & Salt Sizing
- **Previous Behavior:** Output `"Daily Salt Consumption (kg/day)" to round2(saltRequiredKg)` unconditionally, silently assuming exactly `1.0` regeneration cycle per day even when daily water demand or regeneration frequency was unknown. Also lacked bounds validation on `capacity_eq` and `salt_dosage`.
- **Identified Issue:** Silent fabrication of a daily salt consumption number identical to per-regeneration salt (`70.0 kg/cycle` displayed as `70.0 kg/day`), misleading operators if the plant regenerates 2–4×/day or once every 2 days.
- **Reference / Derivation (WQA / Strong Acid Cation $\text{CaCO}_3$ Equivalent Balance):**
  $$V_{\text{run}} = \frac{V_{\text{resin}}\text{ [L]} \times C_{\text{eq}}\text{ [eq/L]} \times 50\text{ [g CaCO}_3\text{/eq]}}{\text{Hardness}\text{ [mg/L = g/m}^3\text{]}}\text{ [m}^3\text{/cycle]}, \quad M_{\text{salt,cycle}} = \frac{V_{\text{resin}} \cdot S_{\text{dose}}}{1000}\text{ [kg/cycle]}$$
  $$M_{\text{salt,day}} = M_{\text{salt,cycle}} \times N_{\text{regen/day}}, \quad \text{where } N_{\text{regen/day}} = \text{supplied } \text{regen\_per\_day} \text{ or } \frac{V_{\text{daily}}}{V_{\text{run}}}$$
- **Exact Implementation Changes:**
  - Enforced `capacity_eq in 0.4..3.0` and `salt_dosage in 40.0..400.0`.
  - Removed unconditional `"Daily Salt Consumption (kg/day)" = saltRequiredKg` duplication.
  - Emits `"Regenerations per Day (cycles/day)"` and `"Daily Salt Consumption (kg/day)"` ONLY when `"regen_per_day"` or `"daily_water_m3"` is explicitly provided; otherwise states omission transparently in `notesAndRecommendations`.
- **Assumptions & Limitations:** Assumes SAC resin at specified operating capacity (`50 g CaCO3/eq`), brine efficiency reflected by user `salt_dosage`, and negligible hardness slip prior to breakthrough; suspended iron/chlorine/turbidity derate field capacity.
- **Test Cases & Results:** Default inputs (`220 ppm`, `500 L`, `1.3 eq/L`, `140 g/L`) yield `147.73 m3/cycle`, `70.0 kg/cycle`, and NO `"Daily Salt Consumption (kg/day)"` key; with `regen_per_day = 2.5` yields `175.0 kg/day`; with `daily_water_m3 = 295.45` yields `140.0 kg/day`. Python audit (`S2-06`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `IMPLEMENTED_VERIFICATION_PENDING`

---

### 2.6 `CALC-20` — Compressed Air Velocity & Line Pressure Drop
- **Previous Behavior:** Used $\Delta P = \frac{0.00075 \cdot L_m \cdot Q_{\text{CFM}}^{1.85}}{d_{\text{cm}}^5 \cdot (P_{\text{air}} + 1.01325)}$ without validating `P_air` against zero or negative gauge/vacuum values (`-1.01325` caused division by zero), and without documenting the variance vs Darcy-Weisbach and Atlas Copco empirical correlations.
- **Identified Issue:** Required independent engineering comparison of empirical `0.00075` vs Darcy-Weisbach and manufacturer sizing equations (`450` Atlas Copco) plus explicit supply-pressure validation and assumption disclosure.
- **Reference / Independent Derivation & Comparison (`150 CFM FAD`, `L = 60 m`, `P = 7.0 bar(g)`):**
  - Convert Atlas Copco steel-line empirical formula $\Delta p = \frac{450 \cdot L \cdot q_{\text{L/s}}^{1.85}}{d_{\text{mm}}^5 \cdot P_{\text{abs}}}$ into `(L_m, Q_CFM, d_cm, P_abs)` units ($1\text{ CFM} = 0.47194744\text{ L/s}$, $d_{\text{mm}}^5 = 10^5 d_{\text{cm}}^5$):
    $$c_{\text{Atlas,eq}} = \frac{450 \times 0.47194744^{1.85}}{10^5} = 0.001122$$
  - Comparison against Darcy-Weisbach $\Delta P = f \frac{L}{D} \frac{\rho v^2}{2}$ (dry air at $25^\circ\text{C}$, $\rho_{\text{line}} = 9.373\text{ kg/m}^3$, Haaland friction factor $f$):

| Case (`L`, `P=7 bar g`) | Current `0.00075` | DW Smooth ($\varepsilon=0.0015\text{ mm}$) | DW Commercial Steel ($\varepsilon=0.045\text{ mm}$) | Atlas Copco `450` ($c_{\text{eq}}=0.001122$) |
|---|---|---|---|---|
| `150 CFM`, `d=50 mm`, `L=60 m` ($v=4.64\text{ m/s}$, $Re=117{,}394$) | **`0.01907 bar`** | `0.02099 bar` (`+10.1%`, $f=0.0174$) | `0.02573 bar` (`+34.9%`, $f=0.0213$) | `0.02852 bar` (`+49.6%`) |
| `150 CFM`, `d=25 mm`, `L=60 m` ($v=18.55\text{ m/s}$, $Re=234{,}788$) | **`0.61020 bar`** | `0.59809 bar` (`-2.0%`, $f=0.0155$) | `0.90998 bar` (`+49.1%`, $f=0.0235$) | `0.91270 bar` (`+49.6%`) |
| `250 CFM`, `d=25 mm`, `L=100 m` ($v=30.91\text{ m/s}$, $Re=391{,}313$) | **`2.61663 bar`** | `2.55537 bar` (`-2.3%`, $f=0.0143$) | `4.16250 bar` (`+59.1%`, $f=0.0233$) | `3.91378 bar` (`+49.6%`) |

  - **Engineering Finding:** `0.00075` closely represents **smooth-bore drawn copper/aluminum piping** (within $\sim 2\text{–}10\%$ of Colebrook-White/Haaland at $\varepsilon = 0.0015\text{ mm}$), whereas **commercial/galvanized carbon steel pipe** ($\varepsilon = 0.045\text{ mm}$) and Atlas Copco `450` predict **$1.35\times\text{–}1.50\times$ higher pressure drop** (i.e., `0.00075` is $\sim 26\text{–}33\%$ non-conservative for rough steel headers). Because pipe roughness $\varepsilon$, line air temperature, and valve/elbow equivalent lengths are not captured in the 4-variable DB form, Stage 2 **retains `0.00075` as a `PRELIMINARY SIZING` equation** and makes these bounds explicit.
- **Exact Implementation Changes:** Added strict positive finite validation on `P_air` + `UnitConverters.gaugeToAbsolutePressureBar(pressureBarG)`, documented smooth-bore/isothermal/straight-pipe assumptions and the `1.35x–1.50x` rough-steel / Atlas Copco `450` spread in `notesAndRecommendations` and `CalculationSpecifications.kt`.
- **Assumptions & Limitations:** Preliminary straight smooth-bore sizing at $\sim 20\text{–}25^\circ\text{C}$ isothermal compressed air, turbulent flow, excluding elbows, tees, isolation valves, and FRL units. Use Darcy-Weisbach with pipe $\varepsilon$ or apply fittings equivalent length + $1.5\times$ margin for aged galvanized steel headers.
- **Test Cases & Results:** Reference (`150 CFM`, `50 mm`, `60 m`, `7 bar g`) preserves `0.019 bar` (`0.01907` exact) with Darcy-Weisbach/Atlas Copco disclosure; `P_air = -1.01325` or `0.0` throws `IllegalArgumentException`. Python audit (`S2-07`) `PASS`; native JVM `UNVERIFIED` pending JDK.
- **Status:** `IMPLEMENTED_VERIFICATION_PENDING`

---

## 3. Verification Summary Ledger

| Verification Suite | Scope | Result | Environment Note |
|---|---|---|---|
| `test_suite_stage02.py` | All 6 Stage 2 modules, DB SHA-256 lock, `Stage02CalculationValidationTest.kt` parity | **8/8 PASS** | Static/Python formula & file verification |
| `test_suite_stage01.py` | Stage 1 regression (`DEF-AUD-01`–`08`, Kotlin brace balance) | **10/10 PASS** | Zero Stage 1 regressions |
| `test_suite_phase07.py` | 8-pillar Phase 07 suite (including 21-point steam table & 20-calc parity) | **27 PASS, 0 FAIL, 1 BLOCKED, 2 PENDING, 2 UNVERIFIED** | `javac`/`kotlinc`/`sdkmanager` absent in container |
| `test_suite_phase06.py` | Phase 06 DB/calc regression | **10/10 PASS** | Preserved |
| `./gradlew testDebugUnitTest assembleDebug` | Native Kotlin compilation & JUnit execution (`EngineeringCalculationsTest` + `Stage02CalculationValidationTest`) | **UNVERIFIED / BLOCKED IN CONTAINER** | Requires Owner PC with JDK 17 + Android SDK 35 |
