# COMPLETE PROJECT STATUS AUDIT, REMAINING WORK INVENTORY & PRE-INSTALLATION PREVIEW PLAN

**Document Identifier:** DOC-MPN-020  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Target Repository:** `researchbangla/mechanical-pro-notes`  
**Audit Date (Historical Baseline Body):** 2026-11-14  
**Checkpoint Synchronization Date:** 2026-11-16  
**Audit Scope:** Full project-wide engineering, codebase, database, content depth, packaging, verification, and preview readiness assessment (Phases 01–08 preparation)  
**Classification Standard:** Strict 6-Tier Evidence Classification (`PASS`, `FAIL`, `BLOCKED`, `PENDING`, `UNVERIFIED`, `PARTIAL`)  
**Scope Notice:** **HISTORICAL BASELINE + 2026-11-16 CHECKPOINT SYNC.** Sections 1–13 below preserve the `2026-11-14` pre-Stage-01 snapshot (`54` Kotlin files, `2` JUnit files, `0` `@Preview`s, `544,768`-byte DB, non-executable `665`-byte `gradlew` shim). See the synchronization block immediately below for current post-Batch-03 / P0-recovery facts.

---

> ### Checkpoint Synchronization Update (2026-11-16 — Post-Stages 01–05B, Batches 01–03 & P0 Recovery)
>
> - **Superseded Baseline Metrics Since `2026-11-14`:**
>   - `android/gradlew`: Replaced with full Gradle 8.9 POSIX wrapper (`6,123` B) and restored to executable `-rwxr-xr-x` during P0 recovery; `python3 test_suite_stage01.py` now passes **`10/10`** (up from `9/10` pre-recovery). *(Native `./gradlew` still cannot run in container.)*
>   - **Database Asset (`mechanical_pro_notes.db`):** Expanded to **`753,664` bytes** (`603` `knowledge_section` rows; asset depth distribution remains `2/64` FULL [`EQ-A01`, `EQ-B01`], `9/64` PARTIAL, `7/64` RESEARCH_PENDING, `46/64` INVENTORY_ONLY).
>   - **UI Previews & Tests:** **`40`** Compose `@Preview` functions authored; **`80` `@Test` methods authored across `12` test files** in `android/app/src/test/` (native JVM `./gradlew testDebugUnitTest` execution remains **unconfirmed**).
>   - **Static Python Suites & Gap Batches:** `Stage 02` (`8/8`), `Stage 03` (`10/10`), and `Stage 04` (`10/10`) static checks reported passing; `Stage 05` Android preview/emulator capability audited and confirmed **BLOCKED** (`DOC-MPN-025`, missing JDK 17 / Android SDK 35 / `/dev/kvm`); `Stage 05B` (`DOC-MPN-028`) planned `Batch 01–03`. Source/test authoring completed for **Batch 01** (`GAP-01`, `GAP-02`, `GAP-04` — `DOC-MPN-029`), **Batch 02** (`GAP-03` core bookmark integration — `DOC-MPN-030`), and **Batch 03** (`GAP-06` core checklist remarks/timestamps preservation — `DOC-MPN-031`).
> - **Aligned Lifecycle Statuses:** Phase 01 `COMPLETE`; Phase 02 `BLUEPRINT COMPLETE — FULL-DEPTH CONTENT INCOMPLETE`; Phase 03 `COMPLETE`; Phases 04–08 `PARTIALLY COMPLETE` (native Room/KSP schema generation [`android/app/schemas/` absent], migration tests, JUnit JVM run, NIST REFPROP benchmark, APK build, Emulator/device QA, and owner push remain unverified/pending).
> - **Confirmed Remaining Open Items:** `GAP-05` (`HomeScreen` bookmark card/shortcuts), `GAP-03` remainder (`EquipmentViewModel.kt:138` one-shot `isBookmarked` & `FundamentalsScreen.kt` bookmark button), secondary `GAP-06` items, `GAP-07..11`, `GAP-12` (`CALC-02` `RESEARCH_PENDING`, `FUND-11..15`), `GAP-13` (asset depth & `5/64` schematics), unresolved **`REF-06` ISO 20816-3 RPM limit conflict** (`15,000 RPM` at `EngineeringCatalogs.kt:170` vs `30,000 RPM` at `EngineeringCatalogs.kt:335`), and independent legal verification notice for Bangladesh Boilers Act, 2022 & Rules, 2025.

---

## 1. Executive Project Status Verdict

**Overall Verdict: FEATURE-IMPLEMENTED AT SOURCE LEVEL, BUT NOT YET BUILD-VERIFIED, NOT FULLY FIELD-CONTENT DEEP, AND NOT READY FOR UNTESTED DAILY FIELD RELIANCE.**

Across Phases 01–07, a substantial native Android Clean Architecture + Jetpack Compose + Room SQLite application (`72` Android workspace files, `54` main Kotlin files, `2` JVM JUnit test files, `544,768`-byte pre-populated SQLite asset covering `64` equipment records across `16` families and `20` interactive engineering calculations + `56`-point IAPWS-IF97 steam engine) has been authored in `/android/`. However, **contradicting the earlier "0 Open Code Defects" claim in `docs/CURRENT_WORK_CHECKPOINT.md`**, this independent audit uncovered **real, reproducible code, search, cross-reference, calculation-UI, packaging, and content-depth defects** alongside zero native build/emulator/physical-device verification in this Linux web container.

### Transparent Weighted Completion Calculation

To avoid misleading single-number optimism, completion is calculated across five weighted project dimensions using strictly verified workspace evidence:

| Dimension | Weight | Assessed Completion | Weighted Contribution | Evidence Basis |
|---|---|---|---|---|
| **1. Architecture, Domain Models & Core Data Layer** | 20% | 88% | 17.6% | All 15 Room entities match SQLite DDL 15/15; atomic `v2` SHA-256 backup/restore, DataStore prefs, offline air-gap manifest verified; penalized for empty `MIGRATION_1_2` stub, swallowed import error reasons, and `SearchRepositoryImpl` Bengali FTS4 regex bug. |
| **2. Calculation Engines & Steam Table** | 20% | 76% | 15.2% | All 20 formulas + 56-row linear-P steam table implemented and verified via Python (`test_suite_phase06/07.py`); penalized for unverified NIST REFPROP benchmark (Owner Condition 5), `CALC-02` pending constants/units, `CALC-06/09/11/19/20` edge gaps, missing dedicated `UnitConverters` UI, and UI `%.2f` precision truncation. |
| **3. Material 3 UI, Navigation & Ergonomics** | 20% | 75% | 15.0% | All 17 drawer routes, 4 bottom-bar primary hubs, 12 dedicated screens + 9 `DomainModuleSpec` screens, 5 vector schematics implemented; penalized for `EQ-H03` Deaerator/RO mismatch, `32.dp` zoom buttons, unlabeled vectors, steam gauge hint bug, zero `@Preview` functions, and hardcoded light pastels. |
| **4. Knowledge Base & Technical Content Depth** | 25% | 42% | 10.5% | Database structurally holds `64/64` baseline equipment records (`304` sections, `64` FMEA, `64` PM), `REF-01`–`REF-12` statutory refs (Act 06 of 2022 / Rules 2025); penalized heavily because `46/64` (71.9%) are `INVENTORY_ONLY` (153/184 unique baseline templates), `7/64` are `RESEARCH_PENDING`, even `2/64` `FULL` assets (`EQ-A01`, `EQ-B01`) are concise 130–254-char summaries rather than deep overhaul manuals, and Checklists/Library/Fundamentals are Kotlin-hardcoded outside SQLite/FTS4. |
| **5. Build Tooling, Native Compilation & Hardware QA** | 15% | 22% | 3.3% | Official Gradle 8.9 `gradle-wrapper.jar` (`43,504` bytes), `.gitignore`, `README.md`, and 2 JUnit test files exist; penalized for non-executable `gradlew` (`644`), zero JDK/SDK/KSP compilation proof, zero JVM JUnit execution, zero `schemas/` export, and zero emulator/physical-phone testing. |
| **Weighted Total** | **100%** | — | **61.6%** | **~62% overall true project completion** (~79% code/scaffold written, ~42% deep domain content, 0% native Android binary/device verified). |

---

## 2. What Is Truly Complete (Empirical Evidence Only)

All items below were directly confirmed on disk during this audit (no unverified claims included):

1. **Zero-Internet & Privacy Architecture (`DEC-016`, `DEC-014`):**
   - `android/app/src/main/AndroidManifest.xml` defines `android:allowBackup="false"` and contains **zero** `INTERNET`, `ACCESS_NETWORK_STATE`, or telemetry permissions.
   - `grep` across all `54` main Kotlin files confirms zero HTTP clients, zero analytics SDKs, zero cloud dependencies, and isolated user tables (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`) separated from static asset records via `NO_ACTION` foreign-key/decoupled string IDs.
2. **Database Structural Parity (`15/15` Tables + `FTS4`):**
   - Read-only `PRAGMA table_info`, `index_list`, and `foreign_key_list` verification of `android/app/src/main/assets/database/mechanical_pro_notes.db` (`544,768` bytes) confirms **100% column, type, nullability, primary-key, foreign-key, and index alignment** with `StaticEntities.kt`, `DynamicUserEntities.kt`, and `EquipmentFtsEntity.kt`.
3. **Complete Navigation & Core Screen Scaffolding:**
   - `Screen.kt` + `AppNavigation.kt` implement all `17` `ModalNavigationDrawer` entries, `4` bottom `NavigationBar` primary tabs (`Home`, `WashPlant`, `BoilerModule`, `Calculations`), plus parameterized routes for `EquipmentDetail`, `CalculationRunner`, `Notes`, `Bookmarks`, and `SteamTable`.
   - Secondary drawer routes (`Pumps`, `Transmission`, `Steam`, `Water`, `Pneumatics`, `CoolingHeating`, `Etp`, `Maintenance`) render via `DomainModuleScreen` (`DomainModuleSpec`) linking directly to filtered `EquipmentList`, `Calculations`, and `Checklists` (with `Fundamentals` routing to `Library`).
4. **Local Preferences, Theme & Backup Cryptography Core:**
   - `UserPreferencesRepository.kt` persists `AppThemeMode` (`SYSTEM`, `LIGHT`, `DARK`) and `highLegibilityMode` (`1.18x` typography multiplier in `Theme.kt`) via Jetpack DataStore.
   - `UserDataRepositoryImpl.kt` implements `database.withTransaction` atomic JSON export/import (`backup_version = 2`), SHA-256 checksum validation, merge deduplication (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`), and Android SAF `CreateDocument` / `OpenDocument` launchers in `SettingsScreen.kt`.
5. **Statutory Legal Alignment (`BD` Boiler Law):**
   - `BoilerDashboardScreen.kt`, `ReferenceLibraryScreen.kt` (`REF-01`–`REF-12`), and `ChecklistViewModel.kt` incorporate **The Boilers Act, 2022 (Act No. 06 of 2022)** (`> 25 L` threshold, annual visual + live safety-valve popping renewal, Sec 24 weld repair approval) and **Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)** (`Grade-1/2/3`, 30y/60y caps), cleanly separated from ASME / BS EN shop hydro rules.
6. **IAPWS-IF97 Tabulated Baseline & Linear-P Interpolation (`IapwsIf97SteamEngine.kt`):**
   - `56` strictly monotonic `GRID` rows covering `[0.50, 20.00] bar(a)` using approved linear-pressure interpolation (`21/21` points passing `DOC-MPN-016` tolerances: max $|\Delta T_{sat}| = 0.040^\circ\text{C}$, max $\epsilon(h_g) = 0.0362\%$, max $\epsilon(v_g) = 0.0808\%$).
7. **20 Calculation Implementations (`BoilerThermalCalculations.kt`, `WashPlantMaintenanceCalculations.kt`):**
   - All `CALC-01` through `CALC-20` execute cleanly in Python parity checks and enforce bounds/safety flags on `CALC-08`, `CALC-10`, `CALC-11`, `CALC-12`, `CALC-14`, `CALC-16`, `CALC-18`, and `CALC-20`, while retaining `CALC-02` as `RESEARCH_PENDING` (Owner Condition 6).

---

## 3. What Is Partially Complete

1. **Android Build & Gradle Wrapper:** `gradle-wrapper.jar` (`Gradle 8.9`, `43,504` bytes), `libs.versions.toml`, `app/build.gradle.kts`, `proguard-rules.pro`, and `gradlew.bat` exist, **but** `android/gradlew` is mode `644` (`-rw-r--r--`, non-executable on Linux/macOS) and is a 14-line minimal shim (`665` bytes) rather than the full POSIX Gradle wrapper script; `android/app/schemas/` does not exist yet because KSP has never compiled; zero `.apk` has been built.
2. **Knowledge Base Content Depth (`64` Assets):** Every asset has non-empty baseline rows (`0/64` missing), **but** only `2/64` (`EQ-A01`, `EQ-B01`) reach 21 sections—and even those are brief `130–254`-character summaries with only `1` FMEA and `1` PM record each. `9/64` are partial (`5–6` sections), `7/64` are `RESEARCH_PENDING`, and `46/64` (`71.9%`) are `INVENTORY_ONLY` (`4` baseline sections sharing template text across families).
3. **Interactive Unit Conversion (`UnitConverters.kt`):** Complete Kotlin conversion functions exist for pressure (`bar`/`kPa`/`psi`), temperature (`°C`/`°F`/`K`), flow (`TPH`/`kg/h`/`LPM`), power (`kW`/`HP`), and energy (`kJ`/`kcal`/`BTU`), **but** no UI screen or quick converter drawer/sheet exposes them (only Steam Table has a `bar(g)`/`bar(a)` toggle).
4. **Offline Vector Schematics (`res/drawable/diag_*.xml`):** `5` schematics exist (`diag_washer_drum`, `diag_hydro_extractor`, `diag_gas_boiler_3pass`, `diag_jhut_boiler_draft`, `diag_steam_condensate_loop`) wrapped in `ZoomableSchematicCard`, **but** they contain pure unlabeled `<path>` geometry with no numbered callouts, plus UX gesture/touch defects (`32.dp` buttons, no `-` zoom out).
5. **Search Engine (`SearchRepositoryImpl.kt`):** Fast FTS4 + LIKE fallback works well for English/ASCII and equipment IDs (`EQ-A01`), **but** breaks on multi-character Bengali words with vowel signs (`া, ি, ী, ু, ূ, ে, ো`) and halant (`্`) due to missing `\p{M}` in regex tokenization.
6. **Hardcoded vs. Database-Driven Reference Data:** Equipment and Calculations live in SQLite, **but** Checklists (`4` checklists / `25` items in `ChecklistViewModel.kt`), Technical Reference Library (`REF-01`–`REF-12` in `ReferenceLibraryScreen.kt`), and Secondary Module descriptions (`DomainModuleSpec` in `AppNavigation.kt`) are hardcoded in Kotlin, making them invisible to offline `SearchScreen`.
7. **Database Migrations (`DatabaseMigrations.kt`):** `.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)` is wired in `MechanicalProDatabase.kt`, **but** `MIGRATION_1_2` is an empty commented stub and `android/app/schemas/` + `MigrationTestHelper` tests do not exist yet.

---

## 4. What Is Incomplete / Missing

| Missing / Unverified Item | Impact | Classification |
|---|---|---|
| Native Android compilation (`./gradlew assembleDebug`) | Cannot install on emulator or phone; Kotlin/KSP/Compose/ProGuard errors undetected | `BLOCKED` (no JDK/Android SDK in container) |
| JVM unit tests (`./gradlew testDebugUnitTest`) | `EngineeringCalculationsTest.kt` & `SteamEngineTest.kt` never executed on JVM | `BLOCKED` / `UNVERIFIED` |
| External NIST REFPROP / CoolProp benchmark | Required by Owner Condition 5 before claiming IAPWS-IF97 numerical accuracy | `UNVERIFIED` |
| Jetpack `@Preview` annotations (`0` in codebase) | Android Studio interactive Compose Layout Preview unusable without editing screens | `MISSING` |
| Room Migration tests (`androidx-room-testing` in `libs.versions.toml`, unused) | Risk of user data loss on future DB version bumps | `MISSING` |
| Dedicated Mechanical Fundamentals content (`15` planned topics) | `Screen.Fundamentals` redirects to `Library`; planned discipline pages missing | `MISSING` |
| Equipment Content-Depth Filter Pills (`[FULL] / [PARTIAL] / [INVENTORY]`) | `EquipmentListScreen.kt` cannot filter out `INVENTORY_ONLY` placeholders | `MISSING` |
| Interactive Unit Converter Tool UI | `UnitConverters.kt` utilities unreachable by user on shop floor | `MISSING` |
| Pre-installation Android Emulator walkthrough & UI screenshots | UI layout, dark-mode contrast, Bengali shaping, SAF flow unverified | `PENDING` |
| Physical smartphone field validation & APK verification | Real touch gloves, screen glare, process death, and file picker unverified | `PENDING` |

---

## 5. Technical, Mathematical, Schema & UI/UX Defects (Verified Concrete Evidence)

1. **`DEF-AUD-01` [HIGH] — Bengali FTS4 Search Token Splitting & False Positives (`SearchRepositoryImpl.kt:42`):**
   - `buildSafeFtsQuery()` uses `Regex("[\\p{L}\\p{N}]+")`, omitting Unicode Combining Marks (`\p{M}`). Bengali vowel signs (`া, ি, ী, ু, ূ, ে, ো`) and conjunct halant (`্`) split single Bengali words into broken consonant fragments:
     - Empirical SQLite verification on `mechanical_pro_notes.db`:
       - `"বয়লার"` $\rightarrow$ `"বয* ল* র*"` $\rightarrow$ **`0` FTS hits** (actual `"বয়লার*"` = **`3`**), silently degrading to `LIKE` scan.
       - `"হাইড্রো"` $\rightarrow$ `"হ* ইড* র*"` $\rightarrow$ **`0` FTS hits** (actual = **`3`**).
       - `"ওয়াশার"` $\rightarrow$ `"ওয* শ* র*"` $\rightarrow$ **`0` FTS hits** (actual = **`2`**).
       - `"বেলি"` $\rightarrow$ `"ব* ল*"` $\rightarrow$ **`4` FALSE-POSITIVE FTS hits** (actual Belly Washers = **`2`**); because `ftsResults.isNotEmpty()` is true, `SearchRepositoryImpl` skips `LIKE` fallback and displays wrong equipment!
   - **Fix:** Change token regex to `Regex("[\\p{L}\\p{M}\\p{N}]+")` (keeping NFC normalization), and add unit tests covering Bengali conjunct queries.

2. **`DEF-AUD-02` [HIGH] — Wrong Equipment ID Cross-Reference on `EQ-H03` (`EquipmentDetailScreen.kt:705, 736`):**
   - In `mechanical_pro_notes.db`, `EQ-H03` is **`Industrial Reverse Osmosis (RO) Plant`** (`RESEARCH_PENDING`), NOT a Deaerator (`EQ-G04` or `FAM-F`/`FAM-G` covers feed/deaerator context; no dedicated deaerator ID is `EQ-H03`).
   - `EquipmentDetailScreen.kt:705` displays `diag_steam_condensate_loop` on `EQ-H03`, and line `736` links `EQ-H03` to `"CALC-08: Deaerator Steam Balance"`.
   - **Fix:** Remove `EQ-H03` from `diag_steam_condensate_loop` and reassign `EQ-H03` to water/chemical calculations (`CALC-19` / RO recovery note) while mapping `CALC-08` to feedwater/boiler equipment (`FAM-G` / `EQ-G04`).

3. **`DEF-AUD-03` [HIGH] — Non-Executable Minimal `gradlew` (`android/gradlew`):**
   - `ls -la android/gradlew` shows `-rw-r--r--` (`644`), `665` bytes (`14` lines, missing POSIX `APP_HOME` resolution fallback when symlinked/invoked from arbitrary working directories and missing `+x` permission). Running `./gradlew assembleDebug` on Linux/macOS immediately fails with `Permission denied`.
   - **Fix:** Restore standard executable Gradle 8.9 POSIX `gradlew` (`chmod 755`).

4. **`DEF-AUD-04` [HIGH] — Calculator Result UI `%.2f` Truncation & Boolean Float Encoding (`CalculationRunnerScreen.kt:243, 264`):**
   - Both primary (`String.format("%.2f %s", ...)`) and secondary (`String.format("%.2f", value)`) outputs force 2 decimal places, wiping out `round3`/`round4` domain precision:
     - `CALC-20` exact value `0.0191 bar` renders as `"0.02 bar"`, and header pressure drops `< 0.005 bar` display `"0.00 bar"`.
     - `CALC-12` flue gas density (`round3`) and `CALC-14` `v_g` (`0.0996 m³/kg` -> `"0.10"`).
     - `CALC-09` encodes an orifice adequacy status flag (`1.0` / `0.0` / `-1.0`) in `secondaryOutputs` and renders `"1.00"` / `"-1.00"`.
   - **Fix:** Add precision metadata / adaptive formatting (`%.4f` for values $< 0.1$ or specific properties like `v_g`/$\Delta P$) and map `CALC-09` adequacy flag to explicit text rather than raw numeric float.

5. **`DEF-AUD-05` [MEDIUM] — `SteamTableScreen.kt:154` Static Absolute Range Hint in Gauge Mode:**
   - Supporting hint always renders `"Valid Range: 0.50 to 20.00 bar(a)"` even when the user selects `Gauge: bar(g)` (valid gauge range is `-0.51 to 18.99 bar(g)` for $P_{atm} = 1.013\text{ bar}$).

6. **`DEF-AUD-06` [MEDIUM] — `ZoomableSchematicCard` Ergonomics, Gesture Conflict & Unlabeled Vectors (`CommonComponents.kt:304–342`, `res/drawable/diag_*.xml`):**
   - Zoom `+` (`minHeight = 32.dp`) and `100%` (`minHeight = 32.dp`) buttons violate the project's `48.dp` minimum industrial glove touch-target requirement (`DEC-015`).
   - Missing `-` zoom-out button; `detectTransformGestures` at `scale == 1f` captures vertical pan gestures inside the `210.dp` box and interferes with parent page scrolling.
   - All 5 vector XMLs (`diag_*.xml`) contain raw unlabeled colored paths with no callout numbers or part tags on the canvas.

7. **`DEF-AUD-07` [MEDIUM] — Backup Restore Exception Swallowing & Library Mismatch (`UserDataRepositoryImpl.kt:345`):**
   - Single `catch (_: Exception) { false }` merges corrupt JSON, wrong `app_name`, SHA-256 tamper mismatch, and unsupported future `backup_version > 2` into a generic boolean failure, violating `docs/PHASE_03_BACKUP_AND_DATA_PROTECTION.md` (`DOC-MPN-017`) error diagnostics. Uses `org.json.JSONObject` while bundling `kotlinx-serialization-json`.

8. **`DEF-AUD-08` [MEDIUM] — Dark-Mode Hardcoded Light Pastels, Emoji Icon, Unkeyed `LazyColumn` & Stale In-Memory Repo Bypass:**
   - Hardcoded `Color(0xFFE8F5E9)` / `Color(0xFFFFF3E0)` surfaces in `SettingsScreen.kt:363`, `SteamTableScreen.kt:210`, `CalculationRunnerScreen.kt:117` produce glaring bright boxes on Industrial Dark Mode.
   - Unicode emoji `"⚠️"` in `CommonComponents.kt:91` instead of Material `Icons.Default.Warning`.
   - `LazyColumn.items` without stable `key` in `SearchScreen.kt:166`, `BoilerDashboardScreen.kt:133, 157, 181`, and `ReferenceLibraryScreen.kt:227`.
   - `CalculationRepositoryImpl.getCalculationById()` reads in-memory `CalculationSpecifications.ALL_CALCULATIONS` instead of SQLite while `getAllCalculations()` reads Room `CalculationDao`.

---

## 6. Module-by-Module Completion Matrix

| ID | Module / Feature | Expected Functionality | Current Status | Evidence | Remaining Work |
|---|---|---|---|---|---|
| **MOD-01** | Home Dashboard (`HomeScreen`) | Plant overview, boiler statutory card, quick equipment/calc entry | **Code Complete (Unrendered)** | `HomeScreen.kt`, `HomeViewModel.kt` | Verify layout on Emulator; update stats dynamically |
| **MOD-02** | Mechanical Fundamentals (`Fundamentals`) | 15 mechanical discipline guides (`MASTER_PROJECT_PLAN §4.1`) | **Placeholder Redirect** | `AppNavigation.kt` routes `Screen.Fundamentals` to `ReferenceLibraryScreen` | Author 15 discipline topics or formally ratify merge into Library |
| **MOD-03** | Wash Plant Equipment (`WashPlant` + `EquipmentDetail`) | 64 assets, search/family/depth filters, 21-section reader + schematics | **Code Complete / Content Thin** | `EquipmentListScreen.kt`, `EquipmentDetailScreen.kt`, `64` DB rows | Fix `EQ-H03` bug; add `[FULL/PARTIAL/INVENTORY]` filter pills; expand 46 `INVENTORY_ONLY` machines |
| **MOD-04** | Dedicated Boiler Module (`BoilerModule`) | Boiler hub, Gas/Jhut quick cards, Act 06 of 2022 & Rules 2025 | **Code Complete (Unrendered)** | `BoilerDashboardScreen.kt`, `BoilerViewModel.kt` | Add `LazyColumn` item `key`; visual inspection on Emulator |
| **MOD-05** | Pumps & Fluid Systems (`Pumps`) | Dedicated pumps & fluid diagnostics | **Secondary Hub Only** | `DomainModuleSpec` in `AppNavigation.kt` filtering `FAM-D` | Adequate for MVP; optional pump-curve guidance post-install |
| **MOD-06** | Motors & Transmission (`Transmission`) | Motors, VFD, belts, bearings, chain drives | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-E` / `CALC-15` | Adequate for MVP; add alignment tolerances post-install |
| **MOD-07** | Steam & Condensate (`Steam`) | Headers, PRV, traps, flash recovery | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-F` / `CALC-06/14` | Adequate for MVP; label `diag_steam_condensate_loop` |
| **MOD-08** | Water & Chemical Systems (`Water`) | Softeners, dosing, RO, TDS control | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-H` / `CALC-19` | Fix `EQ-H03` RO links in `EquipmentDetailScreen.kt` |
| **MOD-09** | Compressed Air & Pneumatics (`Pneumatics`) | Compressors, dryers, receivers, pneumatic actuators | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-J` / `CALC-20` | Verify `CALC-20` UI decimal formatting |
| **MOD-10** | Cooling & Heating (`CoolingHeating`) | Cooling towers, chillers, heat exchangers | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-K` / `FAM-N` | Expand `INVENTORY_ONLY` assets post-install |
| **MOD-11** | ETP & Wastewater (`Etp`) | ETP pumps, blowers, filter press, sludge | **Secondary Hub Only** | `DomainModuleSpec` filtering `FAM-I` | Expand `INVENTORY_ONLY` assets post-install |
| **MOD-12** | Maintenance Management (`Maintenance`) | PM philosophy, lubrication, ISO 20816-3 vibration | **Secondary Hub Only** | `DomainModuleSpec` linking Checklists/Library | Add ISO 20816-3 quick reference table UI post-install |
| **MOD-13** | Inspection & Checklists (`Checklists`) | Persisted boiler, hydro, washer, safety checklists | **Code Complete (Hardcoded List)** | `ChecklistScreen.kt`, `ChecklistViewModel.kt` (`4` checklists/`25` items), Room `ChecklistProgressEntity` | Optionally move definitions to DB for FTS searchability |
| **MOD-14** | Engineering Calculations (`Calculations`) | 20 interactive SI calculators + history persistence | **Code Complete (UI/Math Gaps)** | `BoilerThermalCalculations.kt`, `WashPlantMaintenanceCalculations.kt`, `CalculationRunnerScreen.kt` | Fix `%.2f` truncation, `CALC-06/09/11/19` clarity, complete `CALC-02` constants |
| **MOD-15** | Technical Reference Library (`Library`) | Statutory law, hydro rules, ISO 20816-3, steam safety | **Code Complete (Hardcoded)** | `ReferenceLibraryScreen.kt` (`REF-01`–`REF-12`) | Fix dark-mode card contrasts + `LazyColumn` key; index in search |
| **MOD-16** | Offline Search (`Search`) | Sub-50ms bilingual English/Bengali FTS4 search | **Buggy on Bengali** | `SearchScreen.kt`, `SearchRepositoryImpl.kt` | Fix `Regex("[\\p{L}\\p{M}\\p{N}]+")`; add `LazyColumn` key |
| **MOD-17** | Settings & Local Data (`Settings`) | Theme, font scaling, SAF JSON backup/restore | **Code Complete (Diagnostic Gap)** | `SettingsScreen.kt`, `UserDataRepositoryImpl.kt` | Distinguish backup error codes; fix dark-mode badge colors |
| **MOD-18** | IAPWS-IF97 Steam Table (`SteamTable`) | 0.5–20 bar(a) lookup, `bar(g)`/`bar(a)` toggle | **PASS vs DOC-MPN-016 / UNVERIFIED vs REFPROP** | `IapwsIf97SteamEngine.kt`, `SteamTableScreen.kt` | Fix gauge range hint (`line 154`); run NIST REFPROP verification |
| **MOD-19** | Unit Converter Utilities (`UnitConverters`) | Multi-unit industrial conversions | **Backend Only (No UI)** | `domain/unit/UnitConverters.kt` | Add quick Unit Converter bottom sheet or drawer screen |
| **MOD-20** | Build, Tests & GitHub Repo Prep | Reproducible APK, JVM tests, Room migration tests | **Unverified / Broken `gradlew`** | `build.gradle.kts`, `gradlew`, `2` JUnit files | `chmod +x` + full `gradlew`, run `./gradlew testDebugUnitTest assembleDebug` |

---

## 7. Engineering Calculation Audit (`CALC-01` to `CALC-20`)

### Documentation Numbering Reconciliation Note
`docs/BOILER_MODULE_PLAN.md` (`DOC-MPN-008`) numbered its 14 draft boiler calculations differently from the canonical `CALC-01`–`CALC-20` registry in `docs/PHASE_06_ENGINEERING_SPECIFICATIONS.md` (`DOC-MPN-014`) and `CalculationSpecifications.kt` (for example, `DOC-MPN-008` `CALC-05` was Direct Efficiency, whereas canonical implemented code `CALC-01` is Direct Efficiency). **All UI, database, and JUnit tests follow canonical `DOC-MPN-014` numbering.**

| Calc ID | Name | Status in Code | Audit Verdict & Specific Findings |
|---|---|---|---|
| **CALC-01** | Direct Boiler Thermal Efficiency | `IMPLEMENTED` | **PASS** — Formula $\eta = \dot{m}_s(h_g - h_{fw}) / (\dot{m}_f \cdot GCV) \times 100$ sound; depends on UI lookup of steam enthalpy. |
| **CALC-02** | Indirect Boiler Heat-Loss Efficiency | `RESEARCH_PENDING` (Blocked in UI) | **DEFECTS / INCOMPLETE** — Owner Condition 6 respected (`enabled = false`). However, `CalculationSpecifications.kt` exposes only `5` of `8` engine inputs (`M_fuel`, `L_unburnt`, `L_radiation` hidden); uses natural-gas Siegert `kFactor = 0.38` & `theoreticalAir = 10.0`; `GCV` allows gas/solid units (`MJ/Nm3`) while `L2`/`L4` assume mass basis (`kg`). |
| **CALC-03** | Fuel Firing Rate Estimation | `IMPLEMENTED` | **PASS** — Sound inverse efficiency energy balance. |
| **CALC-04** | Boiler Evaporation Ratio | `IMPLEMENTED` | **PASS** — Clean actual & From-and-At $100^\circ\text{C}$ equivalent ratio. |
| **CALC-05** | Boiler Blowdown Rate (%) | `IMPLEMENTED` | **PASS** — Standard $TDS_f / (TDS_b - TDS_f)$ mass balance with high-blowdown alert (`> 10%`). |
| **CALC-06** | Flash Steam & Heat Recovery | `IMPLEMENTED` | **GAP** — Uses `coerceIn` when `P_boiler <= P_flash` instead of explicit validation error (silently yields `0` flash); `eta_hex = 0.85` hardcoded & hidden; undocumented `+ 41.86` condensate subcooling/approach offset. |
| **CALC-07** | Centrifugal Pump Hydraulic & Shaft Power | `IMPLEMENTED` | **PASS** — Correctly divides by $3.6 \times 10^6 \eta_p \eta_m$. |
| **CALC-08** | Deaerator Steam Consumption | `IMPLEMENTED` | **PASS** — Bounds & sub-cooled feed alerts verified; UI link bug on `EQ-H03` (`DEF-AUD-02`). |
| **CALC-09** | Steam Safety Valve Discharge Capacity (Napier) | `IMPLEMENTED` | **WARNING / PRECISION GAP** — Uses `0.51 * P_abs * Area_mm2 * Kd * Kb`; factor `0.51` is ~`2.89%` more conservative than exact ASME Sec I SI constant `0.52515` (`51.5 * 0.45359237 * 14.50377 / 25.4^2`). Expects `Area (mm2)` instead of throat diameter `d` cited in `DOC-MPN-014`. Secondary `"Adequate (1=Yes)"` renders as `"1.00"`/`"0.00"`/`"-1.00"` in UI. Mandatory statutory disclaimer present. |
| **CALC-10** | Economizer Feedwater Heat Gain | `IMPLEMENTED` | **PASS** — Acid dew-point warning `< 130°C` verified. |
| **CALC-11** | Combustion Air & ID/FD Fan Flow | `IMPLEMENTED` | **UNIT AMBIGUITY** — Variable label says `"Nm3/h or kg/h"`, yet code hardcodes `9.8 Nm3 air / Nm3 natural gas` (overestimates cotton jhut ~2x) and divides `Nm3/h / 60` directly as `"m3/min (FAD)"` without ambient temperature/pressure correction. |
| **CALC-12** | Flue Gas Stack Velocity & Heat Loss | `IMPLEMENTED` | **UI TRUNCATION** — Density `round3` truncated to `%.2f` by `CalculationRunnerScreen.kt`. High-velocity / acid dew-point alerts intact. |
| **CALC-13** | Chimney Natural Draught (Theoretical) | `IMPLEMENTED` | **PASS** — Sound ideal-gas column balance $353 \cdot H \cdot (1/T_a - 1/T_g)$. |
| **CALC-14** | Steam Header Pipe Sizing (DN & Velocity) | `IMPLEMENTED` | **UI TRUNCATION** — Engine `v_g` (`round4`) truncated to `%.2f` in secondary output. Velocity `> 40 m/s` erosion alert intact. |
| **CALC-15** | V-Belt Length, Center Distance & Ratio | `IMPLEMENTED` | **PASS** — Standard pitch-length & arc-of-contact geometry. |
| **CALC-16** | Hydro Extractor G-Force & Speed | `IMPLEMENTED` | **PASS (Verification Restricted)** — Standard $\omega^2 r / g$, includes `> 550 G` mechanical balance alert. |
| **CALC-17** | Washer Batch Steam Heating Demand | `IMPLEMENTED` | **PASS** — Batch water + garment thermal mass divided by batch heating time. |
| **CALC-18** | Tumble Dryer Moisture Evaporation | `IMPLEMENTED` | **PASS (Verification Restricted)** — Latent heat + sensible + `> 60%` exhaust humidity alert. |
| **CALC-19** | Water Softener Resin & Brine Regeneration | `IMPLEMENTED` | **REDUNDANT OUTPUT** — `"Salt Required per Regeneration (kg)"` and `"Daily Salt Consumption (kg/day)"` emit identical values (implicitly assumes 1 regeneration/day without exposing cycle frequency). |
| **CALC-20** | Compressed Air Header $\Delta P$ & Receiver Sizing | `IMPLEMENTED` | **UI TRUNCATION & EMPIRICAL VARIANCE** — Engine uses `0.00075 * L_m * Q_cfm^1.85 / (d_cm^5 * P_abs)` (`0.0191 bar` at `150 CFM, 60m, 50mm, 7 bar(g)`), ~17% below Darcy-Weisbach (`0.0231 bar`, `f=0.02`) and ~33% below Atlas Copco `450` metric formula (`0.0285 bar`); UI `%.2f` rounds `0.0191` to `0.02` and collapses smaller drops `< 0.005` to `0.00 bar`. Receiver sizing logic sound. |

---

## 8. Steam Engine Verification Status (`IapwsIf97SteamEngine.kt`)

- **Tabulated Grid:** `56` strictly monotonic pressures from `0.50 bar(a)` to `20.00 bar(a)` storing $T_{sat}$, $h_f$, $h_{fg}$, $h_g$ ($h_g \equiv h_f + h_{fg}$ invariant), and $v_g$.
- **Interpolation Method:** Restored to approved **linear-pressure interpolation** (`P7-06`), passing **21/21 points** against `DOC-MPN-016` tolerances (max $|\Delta T_{sat}| = 0.040^\circ\text{C}$, max $\epsilon(h_g) = 0.0362\%$, max $\epsilon(v_g) = 0.0808\%$).
- **External Independent Benchmark (Owner Condition 5):** **UNVERIFIED.** Neither NIST Chemistry WebBook / REFPROP nor CoolProp `IF97` has been executed against the `56` grid anchors or intermediate midpoints in a verified JVM/external test report. Until executed, the engine must be labeled **"Engineering Reference Interpolation (Awaiting NIST REFPROP Verification)"**, never "fully IAPWS-IF97 certified."
- **UI Defect:** `SteamTableScreen.kt:154` always shows `bar(a)` range even when `bar(g)` is selected (`DEF-AUD-05`).

---

## 9. Database & Content Depth Reality Check (`mechanical_pro_notes.db`)

- **File:** `android/app/src/main/assets/database/mechanical_pro_notes.db` (`544,768` bytes). Also present is `seed_data.sql` (`98` lines), which is a **legacy Phase 04 partial snippet** (`4` families, `7` equipment rows, `4` sections, `5` calcs) unused by runtime Room asset copy—a trap for future contributors unless documented or removed/updated.
- **Verified SQLite Table Counts (16 Tables):**
  - `system_family`: **16** | `equipment_category`: **22** | `equipment`: **64** | `equipment_fts`: **64**
  - `equipment_subtype`: **12** | `component`: **78** | `technical_parameter`: **130** | `knowledge_section`: **304**
  - `failure_mode`: **64** | `maintenance_procedure`: **64** | `calculation`: **20** | `formula_variable`: **79**
  - User dynamic tables (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`): **0** each (clean initial state).
- **Content Depth Nuance (Critical Honest Finding):**
  - `FULL_CONTENT_BLUEPRINT` (**2 assets**: `EQ-A01`, `EQ-B01`): Each has all **21 sections** (`[1..21]`), `2` subtypes, `3–4` components, `3–4` params, `1` FMEA, `1` PM. However, each section is concise summary prose (**130–254 characters/section**, ~3.7–4.0 KB total per machine), NOT full troubleshooting/tolerance shop manuals, and only `1` FMEA + `1` PM record exists per machine.
  - `PARTIAL_CONTENT_BLUEPRINT` (**9 assets**): `5–6` sections (`998–1,657` chars), `1` subtype, `2–3` components, `1–3` params, `1` FMEA, `1` PM.
  - `RESEARCH_PENDING` (**7 assets**): `4` baseline sections (`1,083–1,120` chars), `0` subtypes, `1` component, `2` params, `1` FMEA, `1` PM.
  - `INVENTORY_ONLY` (**46 assets, 71.9% of library**): `4` baseline sections (`1,000–1,020` chars total; **153 unique** bodies across 184 rows, with `31` shared family templates), `0` subtypes, `1` component, `2` params, `1` FMEA (`46` unique symptoms), `1` PM.
- **Schema & Migration State:** `15/15` Room entity definitions match SQLite DDL cleanly, **except** `DatabaseMigrations.kt` `MIGRATION_1_2` is an empty stub, `android/app/schemas/` has not been generated yet, and `androidx-room-testing` is unexercised.

---

## 10. UI/UX, Accessibility & Ergonomic Findings

1. **Glove Touch Targets (`CommonComponents.kt:304–316`):** `ZoomableSchematicCard` `+` and `100%` buttons use `minHeight = 32.dp`, below the `48.dp` minimum industrial floor standard (`EquipmentCardItem` uses `48.dp`). No `-` zoom-out button exists.
2. **Scroll vs. Pinch-Zoom Conflict (`CommonComponents.kt:328–342`):** `detectTransformGestures` inside the `210.dp` schematic card intercepts single-finger vertical scroll gestures even at `scale == 1f`.
3. **Unlabeled Schematics (`res/drawable/diag_*.xml`):** The 5 schematics are pure geometry paths relying on external legend text rather than on-diagram numbered callouts (`1, 2, 3...`).
4. **Dark Theme Glare Regressions:** Hardcoded light pastel hex backgrounds `Color(0xFFE8F5E9)`, `Color(0xFFFFEBEE)`, `Color(0xFFFFF3E0)` in `SettingsScreen.kt:363`, `SteamTableScreen.kt:210`, and `CalculationRunnerScreen.kt:117` break Industrial Dark Mode eye comfort.
5. **Calculator Output Precision Loss (`CalculationRunnerScreen.kt:243, 264`):** Hardcoded `%.2f` hides real engineering numbers (`CALC-20`, `CALC-12`, `CALC-14`) and prints booleans as `1.00`/`-1.00` (`CALC-09`).
6. **Compose Stability & Previews:** `LazyColumn.items` lacks `key = { ... }` in `SearchScreen.kt:166`, `BoilerDashboardScreen.kt:133, 157, 181`, and `ReferenceLibraryScreen.kt:227`. Zero `@Preview` functions exist across all 54 Kotlin files.
7. **Emoji Instead of Vector Icon (`CommonComponents.kt:91`):** Uses `"⚠️"` Unicode glyph rather than Material `Icons.Default.Warning`, causing inconsistent cross-OEM rendering.

---

## 11. Pre-Installation Preview Options Evaluation

Before installing onto a physical Android phone, three preview mechanisms were evaluated against project rules ("do not convert to a web app; do not build a second UI"):

| Option | Mechanism | Feasibility in This Web Container | Feasibility on Owner's PC | Fidelity & Limitations | Verdict |
|---|---|---|---|---|---|
| **Option 1** | Android Studio Compose `@Preview` | **Not Possible** (no Android Studio/JDK/SDK) | **High** (after adding `@Preview` functions) | Fast layout & Light/Dark/Font inspection per screen, **but** no live Room SQLite copy, no NavHost drawer, no SAF file picker, no DataStore persistence. | **SUPPLEMENTARY** (Add `@Preview` functions in P1/P2) |
| **Option 2** | **Full Android Emulator (AVD) via Android Studio / CLI (`sdkmanager` + `emulator`)** | **Not Possible** (no `/dev/kvm`, `4.0 GiB` RAM, no Android SDK/JDK) | **Full & Fast** (Windows/macOS/Linux PC with `16+ GB` RAM & virtualization) | **Runs the exact compiled `.apk`** against the real `544 KB` Room asset, FTS4, DataStore, SAF `CreateDocument`/`OpenDocument` JSON export/import, rotation, and dark mode before touching any personal phone. Cannot simulate outdoor sunlight glare, wet shop-floor gloves, or phone-specific OEM battery savers. | **PRIMARY RECOMMENDED PREVIEW** |
| **Option 3** | Web / Vite / React Preview UI | Technically possible in container | Possible | **REJECTED.** Re-implementing Kotlin Compose + Room screens in `src/App.tsx` creates a non-native duplicate UI that cannot test APK compilation, Room migrations, Compose gestures, or SAF permissions, and violates project constraints. | **REJECTED** |

---

## 12. Step-by-Step Pre-Installation Android Emulator Preview Plan (Owner PC)

Because this Linux container lacks JDK 17, Android SDK, `/dev/kvm`, and sufficient RAM (`4.0 GiB`), the actual interactive preview must execute on the **Owner's local Windows/macOS/Linux PC** using **Option 2 (Official Android Emulator / AVD)**:

### Step 1 — Prerequisites on Owner PC
1. Install **Android Studio Ladybug (2024.2+)** (includes bundled **JDK 17/21**, Android SDK Platform 35, Platform-Tools `adb`, and Emulator) — or install Android Command-Line Tools + Temurin JDK 17.
2. Verify hardware virtualization is enabled (`VT-x`/`AMD-V` on Windows/Linux, native on macOS) and install **Android SDK Platform 35**, **Build-Tools 35.0.0**, and **Google APIs Intel x86_64** (or **ARM 64 v8a** on Apple Silicon) System Image for **API 34 or 35**.

### Step 2 — Fix Non-Executable Wrapper & Compile + Unit Test
From the repository root (`mechanical-pro-notes/`):
```bash
# Linux / macOS:
cd android
chmod +x gradlew
./gradlew --version
./gradlew testDebugUnitTest assembleDebug

# Windows PowerShell:
cd android
.\gradlew.bat testDebugUnitTest assembleDebug
```
- **Expected artifacts:** `app/build/outputs/apk/debug/app-debug.apk` and test report `app/build/reports/tests/testDebugUnitTest/index.html`.
- *(Recommended pre-build fix before compiling)*: Apply `P0` fixes (`DEF-AUD-01` Bengali FTS4 regex, `DEF-AUD-02` `EQ-H03` links, `DEF-AUD-03` full `gradlew`, `DEF-AUD-04` calculation formatting) first so the very first APK tested already reflects accurate behavior.

### Step 3 — Create & Boot Virtual Device (AVD)
1. In Android Studio **Device Manager $\rightarrow$ Create Virtual Device $\rightarrow$ Pixel 7 (1080×2400, 420 dpi) $\rightarrow$ API 35 (Android 15)**, RAM $\ge 2048\text{ MB}$, **Hardware - GLES 2.0/3.0**.
2. Or via CLI:
   ```bash
   avdmanager create avd -n MPN_Pixel7_API35 -k "system-images;android-35;google_apis;x86_64" -d "pixel_7"
   emulator -avd MPN_Pixel7_API35 -no-snapshot-load &
   adb wait-for-device
   ```

### Step 4 — Install APK & Launch in Airplane Mode (Zero-Network Check)
```bash
# Disable virtual Wi-Fi & mobile data to verify true offline operation:
adb shell svc wifi disable
adb shell svc data disable

adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.researchbangla.mechanicalpronotes/.presentation.MainActivity
```

### Step 5 — Interactive Walkthrough & Visual Verification Checklist
Verify each screen on the Emulator UI while capturing live Logcat & PNG screenshots:
1. **Start Logcat monitor:** `adb logcat -c && adb logcat *:E AndroidRuntime:E SQLiteLog:E > emulator_errors.log`
2. **Home & Drawer Navigation:** Open `ModalNavigationDrawer`; test all `17` modules + `Personal Notes` + `Bookmarks`. Verify `0` crashes (`AndroidRuntime:E` empty).
3. **Equipment Library (`64` Assets) & Schematics:** Open `EQ-A01` (Full), `EQ-G01` (Partial), `EQ-H03` (verify RO vs Deaerator fix), `EQ-P01` (Inventory). Test 5 tabs (`Overview`, `Specs`, `Operation`, `FMEA`, `PM`) and pinch/button zoom on `ZoomableSchematicCard`.
4. **Bengali & English FTS Search:** Search `"EQ-A01"`, `"Boiler"`, `"বয়লার"`, `"বেলি"`, `"হাইড্রো"`. Verify zero false positives and sub-second results.
5. **Boiler Hub, Calculators & Steam Table:** Run `CALC-01`, `CALC-09` (check safety disclaimer), `CALC-20` (check `0.0191 bar`), verify `CALC-02` displays pending notice, toggle `bar(g)` / `bar(a)` on `SteamTableScreen`.
6. **Settings, Theme, Font Scaling & SAF Backup/Restore:** Toggle `Dark` mode + `High Legibility (1.18x)`, inspect contrast; create a note, bookmark, and checklist tick; export `.json` via system Downloads picker, clear/verify merge or overwrite restore.
7. **Capture Screenshots for Inspection:**
   ```bash
   adb exec-out screencap -p > preview_home.png
   ```
8. **Stop / Restart Emulator:**
   ```bash
   adb shell am force-stop com.researchbangla.mechanicalpronotes
   adb emu kill
   ```

### What Emulator Preview Can vs. Cannot Prove
- **Proves:** Exact Jetpack Compose layout, Kotlin/KSP/R8 build success, real SQLite asset copy & FTS4 queries, DataStore theme/font persistence, rotation state, and SAF document picker flows.
- **Cannot Prove (Must Wait for Physical Phone):** Real-world boiler-house sunlight/glare readability, gloved finger ergonomics on physical glass, device-specific OEM memory killing, and physical USB/SD document provider behavior.

---

## 13. Prioritized Remaining Work Backlog (`P0`–`P3`)

### Mandatory Completion Before Physical Phone Daily Use (`P0` & Required `P1`)

| Task ID | Priority | Category | Affected Files / Modules | Problem Description | Exact Required Change | Dependencies | Verification Method | Required Before First Phone Install? |
|---|---|---|---|---|---|---|---|---|
| **REM-01** | **P0** | Build / Packaging | `android/gradlew` | `gradlew` is mode `644` (non-executable) and minimal 14-line stub; fails `./gradlew` on Linux/macOS. | Replace with standard Gradle 8.9 POSIX `gradlew` script and set `chmod 755`. | None | `test -x android/gradlew` and `./gradlew --version` on JDK 17 host. | **YES (Required)** |
| **REM-02** | **P0** | Search / DB | `SearchRepositoryImpl.kt:42` | `Regex("[\\p{L}\\p{N}]+")` drops `\p{M}` combining vowel/halant marks, breaking Bengali queries (`"বয়লার"` -> `0` FTS hits, `"বেলি"` -> `4` false positives). | Update regex to `Regex("[\\p{L}\\p{M}\\p{N}]+")` and add NFC normalization + Bengali FTS tests. | None | SQLite + JVM test verifying exact hits for `"বয়লার"`, `"বেলি"`, `"হাইড্রো"`, `"ওয়াশার"`. | **YES (Required)** |
| **REM-03** | **P0** | Data Integrity / UI | `EquipmentDetailScreen.kt:705, 736` | `EQ-H03` (`Industrial RO Plant`) wrongly renders `diag_steam_condensate_loop` and links to `CALC-08: Deaerator Steam Balance`. | Remove `EQ-H03` from `diag_steam_condensate_loop` and map `EQ-H03` to `CALC-19` / RO; map `CALC-08` to `FAM-G`/`EQ-G04`. | None | Visual check in `EquipmentDetailScreen` for `EQ-H03` & `EQ-G04` + unit test on mapping helper. | **YES (Required)** |
| **REM-04** | **P0** | Calculations / UI | `CalculationRunnerScreen.kt:243, 264`, `BoilerThermalCalculations.kt` | Hardcoded `%.2f` truncates `CALC-20` (`0.0191`->`0.02`), `CALC-12`, `CALC-14`; `CALC-09` renders boolean adequacy as `1.00`/`-1.00`. | Format `< 0.1` / `ΔP` / `v_g` to `%.4f` (`%.3f` for density); render `CALC-09` adequacy as descriptive pass/fail label. | None | Run `CALC-09`, `CALC-12`, `CALC-14`, `CALC-20` on JVM test + Emulator UI check. | **YES (Required)** |
| **REM-05** | **P0** | Build & JVM QA | `android/` (`build.gradle.kts`, `EngineeringCalculationsTest.kt`, `SteamEngineTest.kt`) | Native Android APK and Kotlin JVM JUnit suites have never been compiled or executed. | Run `./gradlew testDebugUnitTest assembleDebug` on Owner PC / CI JDK 17 + SDK 35; resolve any Kotlin/KSP/Compose compiler errors. | `REM-01` | Green `testDebugUnitTest` HTML report + valid `app-debug.apk` + generated `app/schemas/1.json`. | **YES (Required)** |
| **REM-06** | **P0** | Engineering QA (Cond. 5) | `IapwsIf97SteamEngine.kt`, `SteamTableScreen.kt:154` | External NIST REFPROP / CoolProp benchmark unverified (Condition 5); gauge hint always displays `bar(a)` range (`line 154`). | Run independent REFPROP/CoolProp comparison script across all `56` grid + `21` midpoint pressures and record max errors; fix `line 154` dynamic `bar(g)` range text (`-0.51 to 18.99 bar(g)`). | None | Documented benchmark report attached to `docs/`; Emulator gauge-mode UI check. | **YES (Required before engineering reliance)** |
| **REM-07** | **P1** | UI Ergonomics | `CommonComponents.kt:91, 304–342`, `SettingsScreen.kt:363`, `SteamTableScreen.kt:210`, `CalculationRunnerScreen.kt:117` | `ZoomableSchematicCard` buttons `32.dp` (`< 48.dp`), missing `-` zoom-out, scroll gesture conflict at `1x`; hardcoded light pastel cards in Dark Mode; `⚠️` emoji. | Set `48.dp` min targets + `-` button; disable `detectTransformGestures` pan hijack at `scale == 1f` or add toggle; replace `Color(0xFFE8F5E9/FFF3E0/FFEBEE)` with M3 tonal theme tokens; use `Icons.Default.Warning`. | `REM-05` | Emulator inspection in Dark Mode + `1.18x` High Legibility mode + touch scroll test. | **YES (Recommended before phone install)** |
| **REM-08** | **P1** | Backup / Diagnostics | `UserDataRepositoryImpl.kt:345`, `SettingsViewModel.kt`, `CalculationRepositoryImpl.kt` | Backup import returns generic `false` on corrupt JSON vs wrong app vs newer `backup_version > 2` vs SHA-256 failure; `getCalculationById` bypasses Room. | Return sealed `BackupImportResult` (`Success`, `UnsupportedVersion`, `ChecksumMismatch`, `InvalidFormat`) and surface specific messages in `SettingsScreen`; keep DB/in-memory sync consistent. | `REM-05` | JUnit + Emulator SAF round-trip with valid, tampered, and `backup_version = 99` JSON files. | **YES (Recommended before relying on backup)** |
| **REM-09** | **P1** | Calculations Accuracy | `CalculationSpecifications.kt`, `BoilerThermalCalculations.kt`, `WashPlantMaintenanceCalculations.kt` | `CALC-02` hidden inputs/unit mismatch; `CALC-06` missing `P_boiler > P_flash` error & hidden `0.85`; `CALC-09` Area vs throat `d`; `CALC-11` Jhut vs Gas air factor & FAD temp; `CALC-19` duplicate salt outputs. | Expose explicit error on `CALC-06` invalid pressure; clarify `CALC-09` orifice area vs diameter & `0.51` conservative margin; separate `CALC-11` fuel air-ratio selector or strict Natural Gas label; add regens/day to `CALC-19`; finish `CALC-02` inputs/constants before unlocking. | `REM-05` | `EngineeringCalculationsTest.kt` boundary & unit-consistency assertions. | **Post-install OK (except `CALC-02` stays locked)** |
| **REM-10** | **P1** | Schematics & Stability | `res/drawable/diag_*.xml`, `SearchScreen.kt:166`, `BoilerDashboardScreen.kt`, `ReferenceLibraryScreen.kt` | Vectors lack callout numbers (`1..N`); `LazyColumn.items` missing stable `key`; `seed_data.sql` obsolete snippet risks contributor confusion. | Add numbered callouts/labels matching legends on 5 `diag_*.xml`; add `key = { it.id }` to all `LazyColumn` lists; mark or archive `seed_data.sql` as legacy Phase 04 reference. | `REM-05` | Emulator inspection of all 5 schematics + Compose recomposition/scroll check. | **Post-install acceptable (UI polish)** |

### Important Content & Planned Feature Completion (`P2`) — Acceptable for Iterative Releases

| Task ID | Priority | Category | Affected Files / Modules | Problem Description | Exact Required Change | Dependencies | Verification Method | Required Before First Phone Install? |
|---|---|---|---|---|---|---|---|---|
| **REM-11** | **P2** | Knowledge Base Depth | `build_database.py` (authoring source) + `mechanical_pro_notes.db` + `EquipmentDetailScreen.kt` | `46/64` assets are `INVENTORY_ONLY` (`4` template sections), `7` `RESEARCH_PENDING`, `9` `PARTIAL`; even `2` `FULL` assets are brief `130–254`-char section summaries with 1 FMEA/PM each. | Iteratively expand priority plant equipment (`EQ-A04`, `EQ-B02`, `EQ-G01`, `EQ-G02`, `EQ-G03`, `EQ-G04`, `EQ-F01`–`06`, `EQ-H01`–`03`, `EQ-J01`) toward rich 21-section guides with multiple FMEA/PM records; version DB cleanly (`v1 -> v2`). | `REM-12` (Real migration) | SQLite row/length/unique-content audit + FTS4 rebuild verification. | **NO (Post-install content waves)** |
| **REM-12** | **P2** | Database Migrations | `DatabaseMigrations.kt`, `android/app/schemas/`, new `MigrationTest.kt` | `MIGRATION_1_2` is an empty stub; `androidx-room-testing` unused; `schemas/` unexported. | Generate `1.json` via KSP build; implement real `Migration` + `MigrationTestHelper` validating `user_note`/`calculation_history`/`checklist_progress` survival. | `REM-05` | Instrumented/JVM `MigrationTest` passing `v1 -> v2`. | **NO (Required BEFORE any DB schema bump `> 1`)** |
| **REM-13** | **P2** | Missing Planned Features | `EquipmentListScreen.kt`, `UnitConverters.kt`, `AppNavigation.kt`, `ChecklistViewModel.kt`, `ReferenceLibraryScreen.kt` | Missing `[FULL/PARTIAL/INVENTORY]` filter pills (`DOC-MPN-015`), missing UI for `UnitConverters.kt`, `Screen.Fundamentals` redirects to Library, Checklists/Library invisible to global FTS4 search. | Add depth filter chips on `EquipmentListScreen`; add Unit Converter sheet/screen; index Checklists/Reference cards/Fundamentals in search or SQLite FTS4. | `REM-05` | Emulator UI walkthrough across filter chips, Unit Converter, and cross-module search. | **NO (Post-install feature iteration)** |

### Optional Enhancements (`P3`) — Nice-to-Have Post-Phone Iteration

| Task ID | Priority | Category | Affected Files / Modules | Problem Description | Exact Required Change | Dependencies | Verification Method | Required Before First Phone Install? |
|---|---|---|---|---|---|---|---|---|
| **REM-14** | **P3** | Developer DX | Compose screen files (`presentation/**`) | `0` `@Preview` functions exist in codebase. | Add `@Preview(showBackground = true)` + Dark Mode previews for `HomeScreen`, `EquipmentDetailScreen`, `CalculationRunnerScreen`, `SteamTableScreen`. | `REM-05` | Render in Android Studio Split/Design Preview. | **NO (Optional DX)** |
| **REM-15** | **P3** | UX / Reporting | `CalculationRunnerScreen.kt`, `NotesScreen.kt` | Exported notes/calcs cannot be shared cleanly as human-readable text reports offline. | Add offline Android ShareSheet text/Markdown summary export for calculation results & field logs (no network required). | `REM-05` | Test intent chooser on emulator/phone. | **NO (Optional enhancement)** |
| **REM-16** | **P3** | CI Automation | `.github/workflows/android-ci.yml` | No automated GitHub Actions check on Owner's `researchbangla/mechanical-pro-notes` repo. | Optionally add free GitHub Actions Ubuntu workflow running `./gradlew testDebugUnitTest assembleDebug` on push. | Owner repo push | GitHub Actions green check on Owner push. | **NO (Optional enhancement)** |

---

## 14. Physical Device Field Testing Checklist

Execute on the Owner's physical Android phone **after** Emulator preview (`Section 12`) and `P0` fixes pass:

1. **Air-Gap & Manifest Audit:** Verify App Info $\rightarrow$ Permissions shows zero network/location/camera/contacts permissions; run full 20-minute session with Airplane Mode ON.
2. **Boiler House Lighting & Glare Test:** Inspect Industrial Dark Mode and Light Mode under high-ambient boiler house fluorescent/daylight glare and dim pit lighting; verify `1.18x` High Legibility mode readability.
3. **Gloved / Wet-Hand Touch Test:** Operate `NavigationBar`, `ModalNavigationDrawer`, `EquipmentCardItem`, `ZoomableSchematicCard` buttons, and `CalculationRunnerScreen` numeric keypad with thin work gloves.
4. **Configuration Change & Process Death Survival:** Rotate phone (`Portrait <-> Landscape`) mid-calculation and mid-note edit; background app for 5 minutes and resume to confirm `SavedState` / Room persistence.
5. **SAF Backup & Restore Round-Trip:** Create 2 field notes, 2 bookmarks, 1 saved calculation, and 3 checklist ticks $\rightarrow$ Export `.json` to local phone storage $\rightarrow$ Clear app storage $\rightarrow$ Import `.json` $\rightarrow$ Verify 100% lossless restore and tamper rejection on an edited checksum file.
6. **Statutory & Safety Guardrails Check:** Confirm `CALC-02` displays pending warning and blocks unverified execution; confirm `CALC-09` safety valve estimate and `REF-01`–`REF-12` display The Boilers Act, 2022 (`> 25 L`) and Bangladesh Boiler Rules, 2025 statutory notices clearly.

---

## 15. Recommended Immediate Next Action

**STOP & AWAIT OWNER SELECTION.** Do NOT install, push, or rewrite architecture speculatively. The highest-value immediate path is:

1. **Next Implementation Turn (Upon Owner Authorization):** Execute the **`P0` Pre-Preview Fix Batch (`REM-01` through `REM-04` + `REM-06` gauge range hint + `REM-07` dark-mode/touch fixes)** in a clean, surgical patch set so the codebase has zero known search, mapping, decimal-formatting, or `gradlew` defects before compilation.
2. **Owner PC Build & Emulator Preview (`REM-05` + Section 12):** Run `./gradlew testDebugUnitTest assembleDebug` and inspect the app interactively in the **Android Studio Pixel 7 (API 35) Emulator** using the Section 12 walkthrough before installing onto the physical smartphone.
3. **Parallel Content Roadmap (`REM-11`):** Plan iterative content expansion across the `46` `INVENTORY_ONLY` and `7` `RESEARCH_PENDING` plant machines after the core Android binary is verified running cleanly.
