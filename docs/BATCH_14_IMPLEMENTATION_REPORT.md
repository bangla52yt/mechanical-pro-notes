# BATCH 14 IMPLEMENTATION REPORT: ENGINEERING STANDARDS, BOILER LEGAL REFERENCES & STEAM PROPERTY ENGINE
**Document Identifier:** DOC-MPN-042  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** IMPLEMENTED IN SOURCE, SQLITE ASSET, UNIT TESTS & DOCS (`125` authored `@Test`s across `12` files) — NATIVE ANDROID BUILD, JVM JUNIT & EMULATOR VERIFICATION REMAIN **BLOCKED** IN CONTAINER (JDK 17 / ANDROID SDK 35 ABSENT)  
**Date:** 2026-11-18  

---

## 1. Executive Summary
Batch 14 resolves three cross-cutting engineering, statutory-reference, and thermodynamic engine items carried forward from the Batch 13 checkpoint, strictly preserving the offline-first architecture (`DEC-016`), Room schema `user_version = 1` (`16` entities, empty `ALL_MIGRATIONS`), `BackupJsonCodec` (`backup_version = 2`), all 20 calculation formulas, `15/15` Fundamentals, and `64`-asset (`56/1/7/0`) equipment coverage:

1. **Objective A (`REF-06` — ISO 20816-3 Vibration Standard):** Resolved the `15,000 RPM` (`REF-06`) vs `30,000 RPM` (`FUND-05`) conflict to **`120 r/min` to `30,000 r/min` (*in situ*)** for industrial machines **`> 15 kW`** under **`ISO 20816-3:2026`** (replacing withdrawn `ISO 20816-3:2022` and superseded `ISO 10816-3`), separated rigid vs flexible foundations, Group 1 vs Group 2, Criterion I (Zones A–D) & Criterion II, and tightened equipment/DB condition-monitoring guidance to enforce standard exclusions (`<= 15 kW`, `< 120 r/min`, rotodynamic/submerged pumps under `ISO 10816-7`, reciprocating machines under `ISO 10816-6`/`20816-8`, rotary positive-displacement equipment, and static vessels/valves).
2. **Objective B (Bangladesh Boilers Act, 2022 & Boiler Rules, 2025):** Verified against **The Boilers Act, 2022 (`Act No. 06 of 2022`)** (`bdlaws.minlaw.gov.bd/act-print-1402.html`) and **Bangladesh Boiler Rules, 2025 (`S.R.O. No. 279-Law/2025`)**, removing fabricated/erroneous citations (`Rule 44`, `Sec 11 cert`, `Section 13` as operator licensing, `Section 24`/`55` as repeal, unverified `24h` accident window, wrong `13 April 2022` commencement date, and Bengali `২০১৫` typo), adding verified provisions (`11 April 2022` commencement, `Section 2(7)` `> 25 L`, `Section 9` operator license, `Section 13` classification/lifespan, `Sections 18–19` inspection/certificate, `Section 24` repair permit, `Sections 36 & 38` penalties, `Section 54` Repeal & Savings, `29 June 2025` / `69 Rules`, `Rule 3` `10/15/30/60 yr` + up to `+10 yr` extension caps), and preserving explicit Gazette verification boundaries where minor rule numbers were not text-retrieved.
3. **Objective C (`IapwsIf97SteamEngine.kt` Assessment & Fixes):** Verified all `56` `GRID` nodes (`<= 0.0048 °C` vs IAPWS-IF97 Region 4) and fixed two real mathematical defects inside `[0.50, 20.00 bar a]`: replaced linear `T_sat` interpolation (`-0.0511 °C` midpoint bias at `4.25 bar a`) with the exact **IAPWS-IF97 Region 4 Equation 31 (`tsatIf97Celsius`)**, and fixed `v_g` to use **logarithmic-pressure interpolation** (`logFraction = ln(P/p0)/ln(p1/p0)`, fixing `+0.145%` linear-fraction error at `4.25 bar a`). Added IEEE-754 `isFinite()` / `atmosphericBar > 0.0` / `.coerceIn(...)` safeguards and documented explicit out-of-scope regions (`< 0.50 bar a`, Regions `1, 2, 3, 5`, backward equations).

---

## 2. Modified Source, Seed, Database, Test & Documentation Inventory

### 2.1 Kotlin Application Source (`7` files modified)
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/catalog/EngineeringCatalogs.kt`
  - `CHK-BLR-DAILY` `standardReference`: updated from erroneous `"Bangladesh Boiler Rules 2025, Rule 44"` to `"The Boilers Act, 2022 (Sec 18–19) / OCIB Operational Practice"`.
  - `REF-01`: corrected commencement date (`11 April 2022`), repeal (`Section 54`), added `Section 2(7)`, `Section 9`, `Section 13`, `Sections 18–19`, `Section 24`, `Sections 36 & 38`.
  - `REF-02`: fixed Bengali `২০১৫` -> `২০২৫`, added `29 June 2025; 69 Rules`, `Rule 3` lifespan caps (`10/15/30/60 yr` + up to `+10 yr`), replaced erroneous `"Section 13 mandates..."` with `Section 9` + `Boiler Rules, 2025`, and retained full Gazette PDF caveat.
  - `REF-06`: updated `120 RPM and 15,000 RPM` -> `120 r/min and 30,000 r/min`, added `ISO 20816-3:2026` lineage, Group 1/Group 2, Rigid/Flexible, Criterion I/Criterion II, Rigid Group 2 baseline guidance, and explicit exclusions.
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/equipment/EquipmentDetailScreen.kt`
  - Refined `buildConditionMonitoringGuidance(equipment)` across static vessels/valves (`EQ-F01..F06, G03, G04, H01, H02, I02, J03, J04, L03, M02, O02`), boilers (`EQ-G01, G02`), rotodynamic/submerged pumps (`EQ-D01, D02, D05, H03, H04, L01` -> `ISO 10816-7`; dry drivers `> 15 kW` -> `ISO 20816-3:2026`), diesel fire pump (`EQ-O01` -> `ISO 10816-6` + `ISO 10816-7` + `NFPA 20/25`), transmission (`EQ-E04..E06`), PD pumps/compressors (`EQ-J01, L02, D03, D04, D06, L04`), slow-speed hoists/conveyors `< 120 r/min` (`EQ-L05, M01, M03`), process/safety (`EQ-C02..C05, N03`), small machines separating `<= 15 kW` OEM baseline (`EQ-A01..A05, B01, B02, E01`), and `else` `> 15 kW` / `120–30,000 r/min`.
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerDashboardScreen.kt`
  - Updated `STATUTORY_RULES` with verified Boilers Act 2022 & Rules 2025 sections (`11 April 2022`, `Sec 54`, `Sec 9`, `Sec 13`, `Sec 18/19`, `Sec 24`, `Sec 28` without unverified `"24h"`, `Sec 36/38`, `29 June 2025`, `Rule 3`, Gazette disclaimer).
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/checklists/ChecklistViewModel.kt` & `SearchScreen.kt`
  - Removed erroneous `Rule 44` citations.
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/steam/IapwsIf97SteamEngine.kt`
  - Added exact Eq. 31 `tsatIf97Celsius`, fixed log-P `v_g` interpolation (`logFraction`), added `isFinite()` / `atmosphericBar > 0.0` / `.coerceIn(...)` bounds guards, and documented out-of-scope regions in KDoc.
- `/android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/SteamTableScreen.kt`
  - Updated `MathFormulaCard` methodology text to document Region 4 `[0.50, 20.00 bar a]`, Eq. 31 `T_sat(P)`, log-P `v_g`, and explicitly unsupported regions.

### 2.2 Seed Scripts & Rebuilt SQLite Asset
- `/build_database.py` (`EQ-E01, EQ-A01, EQ-B01`), `/seed_equipment_part1.py` (`EQ-D01, D02, E01, G01` removing false `Sec 11 cert` -> `Sec 18/19`), `/seed_equipment_part2.py` (`EQ-H03, H04, L01, M03, O01` removing false `ISO 20816-3:2026` applicability).
- Rebuilt `/android/app/src/main/assets/database/mechanical_pro_notes.db`:
  - **Size:** `856,064` bytes (`user_version = 1`)
  - **SHA-256:** `fe2fb5f9c47cabc2abb56e2873b3531b1b1512d2f4e09df6cadaef1791b90720`
  - **Invariants:** `16` families, `64` equipment (`56` FULL / `1` PARTIAL [`EQ-G02`] / `7` RESEARCH_PENDING / `0` INVENTORY_ONLY), `64` `equipment_fts`, `14` subtypes, `209` components, `206` params, `1,190` knowledge sections, `130` failure modes, `128` PM procedures, `20` calculations, `87` formula variables, `0` synthetic personal rows.

### 2.3 Unit Tests (`+4` `@Test`s -> `125` total across `12` files)
- `SteamEngineTest.kt`: `+3` `@Test`s covering Eq. 31 `T_sat`, log-P `v_g` at `0.525, 4.25, 10.50, 19.50 bar a`, and `NaN` / `+Infinity` pressure guards.
- `EquipmentMappingTest.kt`: expanded `conditionMonitoringGuidanceDistinguishesStaticPdBeltsAndRotatingMachinery` across `EQ-I02, F01, J01, D01, O01, M03, E04, A01, P01`.
- `UnifiedSearchAndCatalogTest.kt`: `+1` `@Test` (`batch14StandardsAndStatutoryCitationsAreAccurate`) asserting `REF-06` `30,000 r/min` / exclusions and absence of `15,000 RPM` / `Rule 44` / `Sec 11`.

### 2.4 Documentation Updated (`8` files)
- `DOC-MPN-010` (`TECHNICAL_RESEARCH_SOURCE_REGISTER.md`): `SRC-009`, `SRC-011`, `SRC-011A`, Sec 3 updates.
- `DOC-MPN-008` (`BOILER_MODULE_PLAN.md`): Act 06 of 2022 & Rules 2025 verification boundaries.
- `DOC-MPN-016` (`PHASE_06_STEAM_TABLE_ENGINE.md`): Eq. 31 equations, 56-node grid, log-P `v_g`, 15-point check, unsupported regions, external REFPROP note.
- `PHASE_06_KNOWN_LIMITATIONS.md`: updated `[0.50, 20.00 bar a]` (`81.32–212.38 °C`) & unsupported region statement.
- `DOC-MPN-042` (`BATCH_14_IMPLEMENTATION_REPORT.md` — this report), `CURRENT_WORK_CHECKPOINT.md` (`DOC-MPN-004`), `SESSION_HANDOVER.md` (`DOC-MPN-006`), `PHASE_PROGRESS_TRACKER.md` (`DOC-MPN-003`).

---

## 3. Verification Ledger & Honest Container Boundaries

| Check Category | Status in Container | Details |
|---|---|---|
| Kotlin Syntax & Balance | **PASS (Python Delimiter Check)** | `10/10` edited `.kt` files (`0` brace/paren/bracket/string balance errors); `@Test` count verified at **`125`** across **`12`** test files. |
| SQLite Schema, Integrity & Cleanliness | **PASS (Python `sqlite3`)** | `856,064` bytes, SHA-256 `fe2fb5f9c47cabc2abb56e2873b3531b1b1512d2f4e09df6cadaef1791b90720`, `user_version = 1`, `PRAGMA integrity_check = ok`, `PRAGMA foreign_key_check = []`, `56/1/7/0` depths, `0` personal rows, `0` false `Rule 44`/`Sec 11`/`15,000 RPM` occurrences. |
| Steam Engine Mathematical Check | **PASS (Python Reference Reproduction)** | All `56` grid nodes match IAPWS-IF97 Eq. 31 within `<= 0.004757 °C`; `15`-point continuous Eq. 31 evaluation (`0.50`–`20.00 bar a`) matches Eq. 31 to `0.000000 °C` with log-P `v_g` interpolation verified. |
| Native Android Build & JVM JUnit (`./gradlew testDebugUnitTest assembleDebug`) | **UNVERIFIED / BLOCKED** | Container lacks JDK 17 (`java`/`javac`/`kotlinc`), Android SDK 35, and Emulator/AVD (`/dev/kvm`). |
| External Verification Still Needed | **OPEN** | Independent **NIST REFPROP / CoolProp** solver cross-check against `IapwsIf97SteamEngine` (`P7-P5-03`); full **Bangladesh Gazette PDF** text check for all 69 Rules in `S.R.O. 279-Law/2025`; plant batch lab elemental/GCV tests for mixed-jhut (`CALC-02`/`EQ-G02`); plant/OEM verification for `7` `RESEARCH_PENDING` assets. |
