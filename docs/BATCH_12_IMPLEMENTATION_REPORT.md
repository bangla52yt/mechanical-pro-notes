# BATCH 12 IMPLEMENTATION REPORT: GAP-12 ENGINEERING CALCULATION & FUNDAMENTALS COMPLETION

**Document Identifier:** DOC-MPN-040  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Batch:** Batch 12 (`GAP-12` — `CALC-02` Indirect Boiler Efficiency + `FUND-11` through `FUND-15`)  
**Date:** 2026-11-17  
**Status:** IMPLEMENTED IN SOURCE, DATABASE ASSET & ENGINEERING DOCS — NATIVE COMPILE & JVM JUNIT RUNTIME **UNVERIFIED** (JDK 17 / ANDROID SDK 35 ABSENT IN CONTAINER)

---

## 1. Executive Summary
Batch 12 completes **`GAP-12`** across engineering research documentation, Kotlin domain/UI code, the pre-packaged Room SQLite asset, and focused JVM JUnit tests without altering the Room schema (`user_version = 1`), preserving all `23` `Screen` routes, `8` `*ViewModel.kt` files, offline-first air-gap (`DEC-016`), and prior Batch 01–11 functionality:

1. **Part A — `CALC-02` (Indirect Boiler Efficiency / Heat Loss Method):**
   - Verified governing heat-loss formulations against **ASME PTC 4-2013** (`SRC-007`), **BS 845-1:1987**, **IS 8753:1977**, and **ISO 50002:2014** (`SRC-017`).
   - Transitioned `CALC-02` from `RESEARCH_PENDING` to `CalculationStatus.IMPLEMENTED` for **Natural Gas** (`fuel_type = 1.0`, Siegert $K = 0.38$, gravimetric $A_0 = 17.0\text{ kg/kg}$ corresponding to $9.8\text{ Nm}^3/\text{Nm}^3 \times 1.293 / 0.74$) and **lab-calibrated liquid/solid fuels** (`fuel_type = 2.0`).
   - Retained strict governance guards and an explicit **`UNVERIFIED MIXED-JHUT BOUNDARY`** notice: uncalibrated mixed garment jhut/solid fuels are rejected if volumetric GCV mode (`gcv_unit_mode = 2.0`) is attempted or if lab-measured `siegert_k`, `theoretical_air`, `GCV` (`kJ/kg`), and `H2_percent` are omitted; omitted optional losses (`M_fuel`, `L_unburnt`, `L_radiation`) are never silently fabricated and are explicitly disclosed in output notes.
2. **Part B — `FUND-11` through `FUND-15` (Five Remaining Fundamentals Modules):**
   - Researched and authored full engineering baselines (`DOC-MPN-009 §§ 2.11–2.15`, `DOC-MPN-010 SRC-018..022`) for:
     - `FUND-11` — Engineering Mathematics & Applied Mechanics
     - `FUND-12` — Engineering Mechanics (Statics & Dynamics)
     - `FUND-13` — Industrial Engineering & Plant Operations
     - `FUND-14` — Power Plant & Steam Utility Engineering
     - `FUND-15` — Instrumentation & Control Fundamentals
   - Upgraded all 5 topics in `FundamentalsCatalog.ALL_TOPICS` from `CONTENT_GAP` to **`VERIFIED_BASELINE` (`15 / 15` complete)** with cross-verified `EQ-*` and `CALC-*` links, and updated `FundamentalsScreen.kt` TopAppBar subtitle & safety notice cleanly while preserving `48.dp` bookmark toggles and `@Preview`s.

---

## 2. Research Sources & Governing Engineering Formulations

### 2.1 Primary Sources Added to `DOC-MPN-010`
- **`SRC-017`**: BS 845-1:1987, IS 8753:1977, ISO 50002:2014 (Indirect heat-loss boiler evaluation, Siegert factor & combustion air humidity moisture loss).
- **`SRC-018`**: Kreyszig (*Advanced Engineering Mathematics*, 10th Ed.) & ASME PTC 19.1-2018 (`FUND-11` interpolation, unit consistency, measurement uncertainty).
- **`SRC-019`**: Beer, Johnston, Mazurek & Cornwell (*Vector Mechanics for Engineers*, 12th Ed.) & ASME B30.9-2021 (`FUND-12` static/rotational dynamics, sling angle loading, belt wrap friction).
- **`SRC-020`**: ISO 50001:2018 & Maynard's Industrial Engineering Handbook (5th Ed.) (`FUND-13` EnPIs, SEC/ER scheduling, TPM, ABC/VED spares).
- **`SRC-021`**: P.K. Nag (*Power Plant Engineering*, 4th Ed., McGraw Hill) (`FUND-14` Rankine/steam utility circuit, economizer acid dew-point, deaeration).
- **`SRC-022`**: IEC 60751:2022, ANSI/ISA-5.1-2024, NFPA 85, & B.G. Lipták (*Instrument Engineers' Handbook*, 4th Ed.) (`FUND-15` 3-wire Pt100 RTD, 4–20 mA live-zero, PID & BMS interlocks).

### 2.2 `CALC-02` Verified GCV-Basis Equations & Worked Benchmark
All heat losses are normalized to **Fuel Gross Calorific Value ($\text{GCV}_{\text{mass}}$, $\text{kJ/kg}$)** per BS 845-1 / IS 8753 (with volumetric pipeline gas converted via $\text{GCV}_{\text{mass}} = \text{GCV}_{\text{vol}} / \rho_{\text{gas}}$):

- **$\Delta T = T_{\text{flue}} - T_{\text{ambient}}$ ($^\circ\text{C}$ or $\text{K}$)**
- **$L_1$ (Dry Flue Gas Loss, %):** $\frac{K \cdot \Delta T}{\text{CO}_2\%}$ ($K = 0.38$ verified Natural Gas default; explicit lab $K$ required when `fuel_type = 2.0`).
- **$L_2$ ($\text{H}_2$ Combustion Moisture Loss, %):** $\frac{9 \cdot (\text{H}_2\% / 100) \cdot (2442.0 + 2.09 \cdot \Delta T)}{\text{GCV}_{\text{mass}}} \times 100$
- **$L_3$ (Fuel Moisture Loss, %):** $\frac{(M_{\text{fuel}}\% / 100) \cdot (2442.0 + 2.09 \cdot \Delta T)}{\text{GCV}_{\text{mass}}} \times 100$ (`0.0%` + omitted notice if unsupplied)
- **$L_4$ (Air Humidity Moisture Loss, %):** $\frac{A_0 \cdot 0.015 \cdot 2.09 \cdot \Delta T}{\text{GCV}_{\text{mass}}} \times 100$ ($A_0 = 17.0\text{ kg/kg}$ Natural Gas default; explicit lab $A_0$ required when `fuel_type = 2.0`)
- **$L_{\text{unburnt}}$ + $L_{\text{rad}}$ (%):** Direct optional inputs (`0.0%` + omitted notice if unsupplied — never silently estimated)
- **$\eta_{\text{indirect}}$ (% GCV Basis):** $100 - (L_1 + L_2 + L_3 + L_4 + L_{\text{unburnt}} + L_{\text{rad}})$ bounded cleanly in $[30.0, 98.0]\%$

**Worked Natural Gas Benchmark (`T_flue=185°C`, `T_ambient=30°C`, `CO2=9.5%`, `GCV=39,800 kJ/Nm3`, `rho=0.74 kg/Nm3`, `H2=23.0%`):**
- $\text{GCV}_{\text{mass}} = 39800 / 0.74 = 53783.78\text{ kJ/kg}$, $\Delta T = 155^\circ\text{C}$
- $L_1 = 6.20\%$, $L_2 = 10.65\%$, $L_3 = 0.00\%$, $L_4 = 0.15\%$
- **With `L_radiation = 1.5%`:** $\eta_{\text{indirect}} = \mathbf{81.50\%}$ (GCV basis)
- **With `L_radiation` omitted:** $\eta_{\text{indirect}} = \mathbf{83.00\%}$ (partial-loss disclosure shown)

---

## 3. Modified Deliverables

### 3.1 Engineering Documentation Updated / Authored
- `docs/GENERAL_MECHANICAL_KNOWLEDGE_MAP.md` (`DOC-MPN-009`): Authored §§ `2.11`–`2.15` (`FUND-11`–`FUND-15`) and updated coverage summary to `15 / 15` disciplines.
- `docs/TECHNICAL_RESEARCH_SOURCE_REGISTER.md` (`DOC-MPN-010`): Added `SRC-017` through `SRC-022`.
- `docs/PHASE_06_ENGINEERING_SPECIFICATIONS.md` (`DOC-MPN-014`): Updated `CALC-02` specification, 13 variables, GCV-basis formulas, worked benchmark, and Mixed-Jhut boundary.
- `docs/PHASE_06_KNOWN_LIMITATIONS.md`: Updated § `1.4` distinguishing verified Natural Gas `CALC-02` from uncalibrated mixed-jhut restriction.
- `docs/BATCH_12_IMPLEMENTATION_REPORT.md` (`DOC-MPN-040`): This implementation report.
- `docs/CURRENT_WORK_CHECKPOINT.md`, `docs/SESSION_HANDOVER.md`, `docs/PHASE_PROGRESS_TRACKER.md`: Synchronized Batch 12 completion status (`GAP-12` source/DB implemented; native runtime unverified; `118` JUnit `@Test`s).

### 3.2 Kotlin Source, Seed Script & Pre-Packaged SQLite Database
| Artifact | Summary of Batch 12 Changes |
|---|---|
| `BoilerThermalCalculations.kt` | Updated `executeCalc02` to `CalculationStatus.IMPLEMENTED`, fixed Natural Gas gravimetric $A_0 = 17.0\text{ kg/kg}$, preserved volumetric-GCV & solid-fuel lab guards, added `UNVERIFIED MIXED-JHUT BOUNDARY` advisory for `fuel_type = 2.0`. |
| `CalculationSpecifications.kt` | Transitioned `CALC-02` metadata to `CalculationStatus.IMPLEMENTED` and exposed all `13` `FormulaVariable` parameters (`id = 6..10` required, `80..87` optional/governance). |
| `EngineeringCatalogs.kt` | Authored `FUND-11`–`FUND-15` full theoretical foundations, washing-plant applications, governing formulas, failure mechanisms, citations, and verified `EQ-*`/`CALC-*` links at `VERIFIED_BASELINE`. |
| `FundamentalsScreen.kt` | Updated TopAppBar subtitle cleanly for `0` gaps (`15 Verified Baselines`) and aligned the `SafetyWarningCard` notice to `"FUND-01 to FUND-15 reflect verified General Mechanical Knowledge Map baselines..."`. |
| `seed_phase06_calculations.py` | Synchronized `CALC-02` row (`IMPLEMENTED`) and all `13` `CALC-02` variables (`87` total). |
| `mechanical_pro_notes.db` | Surgically updated ONLY `CALC-02` row in `calculation` and `CALC-02` rows (`id = 6..10`, `80..87`) in `formula_variable`. Schema (`user_version = 1`), size (`753,664` bytes), `64` equipment, `64` FTS rows, `CALC-01/03..20`, and all user tables untouched. |

### 3.3 SQLite Database Integrity & Checksum Delta
- **Database Size:** Unchanged at `753,664` bytes (`PRAGMA user_version = 1`, `PRAGMA integrity_check = ok`, `PRAGMA foreign_key_check = []`).
- **Pre-Batch-12 SHA-256 (`d464bcb1...`):** `d464bcb1de3459916db31687a5414a55fce10876bb8324aa1056c669f7e16ee0`
- **Post-Batch-12 SHA-256:** `cd18a039d869dfabbe7cfdf43152ccea3b8eb89588a28eb5e895482d44b197ee`
- **Post-Batch-12 `calculation` + `formula_variable` SHA-256:** `82d5e5422b9ca8968e46ebe4802d32cd5203ed7eabe6fdae2c235beeaaf4a481` *(Note: Earlier Stage 02/03/04 static Python test scripts hardcode the pre-Batch-12 DB SHA-256 and pre-Batch-12 `CALC-02 = RESEARCH_PENDING` / `CONTENT_GAP = 5` constants, which are intentionally superseded by Batch 12.)*

### 3.4 JUnit Test Suite Updates (`118` `@Test` Methods Across `12` Files)
- `EngineeringCalculationsTest.kt`: Updated `CALC-02` status/variable-count test (`IMPLEMENTED`, `13` vars) and benchmark/mixed-jhut boundary test (`81.50%` Natural Gas + `UNVERIFIED MIXED-JHUT BOUNDARY`).
- `Stage02CalculationValidationTest.kt`: Updated volumetric GCV test to `IMPLEMENTED` (`83.00%` partial heat balance) and added strict rejection tests for solid fuel missing `siegert_k` and missing `theoretical_air`.
- `Stage04UiStateAndAccessibilityTest.kt`: Updated `boilerCalculationCatalogCoversCalc01Through14WithVerifiedCalc02AndSteamRoute` verifying all `CALC-01..14` are `IMPLEMENTED` and `CalculationViewModel` runs/validates `CALC-02`.
- `UnifiedSearchAndCatalogTest.kt`: Updated `FundamentalsCatalog` assertion to `15 VERIFIED_BASELINE / 0 CONTENT_GAP` and added English & Bengali catalog search assertions for `FUND-11..15`.

---

## 4. Verification Summary & Remaining Open Items
- **Minimal Sanity Checks Executed (Container Python):**
  - Kotlin delimiter/brace balance across all `76` `.kt` files (`64` main + `12` test): **0 errors**; exact JUnit `@Test` count = **`118`**.
  - `FUND-11..15` cross-referenced IDs (`17` `EQ-*` + `15` `CALC-*`) checked against SQLite DB: **0 missing IDs**.
  - SQLite `PRAGMA integrity_check = ok`, `PRAGMA foreign_key_check = []`, `PRAGMA user_version = 1`, `20` calculations, `87` formula variables, `64` equipment & FTS rows, `0` user rows.
  - `CALC-02` Natural Gas worked benchmark numerically verified in Python (`L1=6.20%`, `L2=10.65%`, `L4=0.15%`, `eta=81.50%` with `L_rad=1.5%`, `83.00%` without `L_rad`).
- **Blocked / Unverified Outside Container (Preserved Explicitly):**
  - Native `./gradlew testDebugUnitTest assembleDebug`, KSP Room schema generation, Compose runtime previews, and Emulator/device UI tests remain **BLOCKED / UNVERIFIED** in container (`JDK 17` and `Android SDK 35` absent).
  - Unresolved project items intentionally untouched in Batch 12: **`GAP-13`** (`62/64` non-full equipment depth + `5/64` schematics), **`REF-06`** ISO 20816-3 RPM discrepancy (`15,000` vs `30,000 RPM`), Bangladesh Boilers Act 2022 / Rules 2025 independent legal verification, and NIST REFPROP / CoolProp comparison against `IapwsIf97SteamEngine`.
