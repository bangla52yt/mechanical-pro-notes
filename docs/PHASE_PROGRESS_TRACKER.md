# PHASE PROGRESS TRACKER: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-003  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** `MPN-GITHUB-READY-01` PREPARED (`DOC-MPN-045`) + BATCH 15A `PARTIAL/BLOCKED` (`DOC-MPN-044`) — SAFE `.gitignore`, ZERO SECRETS, `android/gradlew` `0755`, DB `856,064` B; NO `.git` / NO REMOTE REPO CREATED; NATIVE BUILD/TESTS/LINT/KSP/EMULATOR **BLOCKED** (NO JDK 17 / SDK 35)  
**Last Updated:** 2026-11-18 *(Section 2 preserves 2026-11-13 historical baseline records with reconciliation notes)*  

---

## 1. Overall Project Lifecycle & Status Matrix

| Phase | Phase Name | Status | Owner Approval | Current Verified State & Next Action |
|---|---|---|---|---|
| **Phase 01** | Documentation Foundation & Scope Alignment | **COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | Core docs, plans, and project governance files complete. |
| **Phase 02** | Plant Knowledge Base & Content Blueprint | **COMPLETE (UPGRADED IN BATCH 13–14)** | [C. OWNER-APPROVED REQUIREMENT] | 64-asset blueprint & depth reconciled (`56/64` FULL, `1/64` PARTIAL [`EQ-G02`], `7/64` RESEARCH_PENDING, `0/64` INVENTORY_ONLY); `mechanical_pro_notes.db` = `856,064` bytes (`1,190` sections, `18` schematics). Batch 14 reconciled `REF-06` (`120–30,000 r/min`) & verified Boilers Act `06` of `2022` / headline Rules `2025`; minor Gazette PDF rules await full-text check. |
| **Phase 03** | Android Architecture & Workspace Baseline | **COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | Architecture & baseline approved (`DEC-011–017`). |
| **Phase 04** | Core Domain & Room Database Implementation | **PARTIALLY COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | Entities, DAOs, repositories & DB asset authored (`856,064` bytes, `user_version = 1`); native Room/KSP compilation (`android/app/schemas/` absent) and migration execution remain unverified. |
| **Phase 05** | Material 3 UI & Compose Reference Screens | **PARTIALLY COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | Screens, themes, navigation, `@Preview`s, `18` `diag_*.xml` schematics, and Batch 01–14 UI/bookmark/search/module/notes/calc/unit/boiler/settings/checklist/fundamentals/equipment/steam/statutory fixes authored; `Stage 05` blocked, native Compose runtime & APK validation unverified. |
| **Phase 06** | Field Calculations & Maintenance Tools | **PARTIALLY COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | All 20 calculations (`CALC-01`–`CALC-20`, with `CALC-02` Gas-verified & uncalibrated Mixed-Jhut explicitly boundary-guarded, `87` formula vars) + Region 4 Eq. 31 / log-P `v_g` IAPWS-IF97 steam engine authored; external NIST REFPROP benchmark & native JVM JUnit execution remain unverified. |
| **Phase 07** | Offline Validation & Field Testing | **PARTIALLY COMPLETE** | [C. OWNER-APPROVED REQUIREMENT] | Batch 14 static/DB/steam/delimiter sanity checks passed (`0` errors; prior `Stage 01..04` hardcoded pre-Batch-12/13/14 constants intentionally superseded); native APK compilation & physical-device validation remain outstanding. |
| **Phase 08** | GitHub Repository Preparation & Owner Push | **PARTIALLY COMPLETE** | `DEC-002`, `DEC-004` | Root `README.md`, hardened root + `android/.gitignore`, `DOC-MPN-045` mobile upload checklist, `gradle-wrapper.jar` (8.9), and filesystem-executable `android/gradlew` (`0755`; Git `100755` unverified as `.git` absent) ready; GitHub repo not yet created and APK unbuilt. |

---

## 2. Granular Phase Details

### Phase 01: Documentation Foundation & Scope Alignment
- **Status:** **COMPLETED**
- **Deliverables:** `MASTER_PROJECT_PLAN.md`, `PROJECT_DECISION_LOG.md`, `PHASE_PROGRESS_TRACKER.md`, `CURRENT_WORK_CHECKPOINT.md`, `PROJECT_SAFETY_RULES.md`, `SESSION_HANDOVER.md`.

---

### Phase 02: Plant Knowledge Base & Content Blueprint (Master Prompt 02-B Final Reconciliation)
- **Status:** **COMPLETED & ACCEPTED (Phase 02 Acceptance Gate Passed)**
- **Completed Corrections & Audits:**
  1. **Equipment Inventory Reconciled (64 Records):** Full mathematical reconciliation of `docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md` resolving previous count ambiguities. All 64 assets assigned exact unique IDs across 16 Families (A to P):
     - `[FULL CONTENT BLUEPRINT]`: 2 records (`EQ-A01`, `EQ-B01`).
     - `[PARTIAL CONTENT BLUEPRINT]`: 9 records (`EQ-A04`, `EQ-B02`, `EQ-B03`, `EQ-D02`, `EQ-F02`, `EQ-G01`, `EQ-G02`, `EQ-J01`, `EQ-L01`).
     - `[RESEARCH PENDING]`: 7 records (`EQ-A05`, `EQ-B04`, `EQ-B05`, `EQ-C04`, `EQ-C05`, `EQ-H03`, `EQ-K02`).
     - `[INVENTORY ONLY]`: 46 records (`EQ-A02`, `EQ-A03`, `EQ-C01`–`EQ-C03`, `EQ-D01`, `EQ-D03`–`EQ-D06`, `EQ-E01`–`EQ-E06`, `EQ-F01`, `EQ-F03`–`EQ-F06`, `EQ-G03`, `EQ-G04`, `EQ-H01`, `EQ-H02`, `EQ-H04`, `EQ-I01`–`EQ-I03`, `EQ-J02`–`EQ-J04`, `EQ-K01`, `EQ-L02`–`EQ-L05`, `EQ-M01`–`EQ-M03`, `EQ-N01`–`EQ-N03`, `EQ-O01`, `EQ-O02`, `EQ-P01`).
     - **Reconciliation Total:** $2 + 9 + 7 + 46 = 64$ verified unique records. Zero missing, zero duplicates.
  2. **Bangladesh Boiler Law Statutory Alignment:** Referenced against `bdlaws.minlaw.gov.bd/act-print-1402.html` for **The Boilers Act, 2022 (Act No. 06 of 2022 / ২০২২ সনের ০৬ নং আইন)** and **Bangladesh Boiler Rules, 2025 (বয়লার বিধিমালা, ২০২৫; S.R.O. No. 279-Law/2025)**. *Synchronization Note (2026-11-16): Statutory citations and rules require independent legal verification before formal compliance reliance.*
  3. **Hydrostatic Test Demarcation:** Distinctly separated manufacturer initial shop hydro test (ASME Sec I PG-99 at $1.5 \times$ MAWP / BS EN 12953-3 at $1.43 \times$ design pressure) from in-service periodic statutory hydrostatic test ordered by Chief Inspector of Boilers during registration, major weld repair (Sec 24), or suspected defect (typically $1.25\text{–}1.5 \times$ authorized working pressure, not uncalibrated $1.5 \times$ original design pressure). Annual renewal consists of visual inspection and live steam test for safety valve popping.
  4. **Machine Vibration Standard Demarcation:** **ISO 20816-3:2026** scope noted for industrial machines $>15\text{ kW}$, 120–30,000 RPM, with exclusions (rotary screw compressors, integrated rotodynamic pumps per ISO 10816-7, reciprocating per ISO 10816-6/20816-8, submersibles) and Zones A–D guidance. *Synchronization Note (2026-11-16): `EngineeringCatalogs.kt:170` (`REF-06`) cites `120 RPM and 15,000 RPM` whereas `EngineeringCatalogs.kt:335` (`FUND-05`) and this section cite `120–30,000 RPM`; this conflict remains unresolved pending standard verification.*
  5. **14 Boiler Calculation Algorithms Verified:** All 14 engineering formulas formatted in LaTeX, verified in SI units with boundary conditions and physical assumptions. Formally noted as practical plant engineering estimates / energy auditing tools, demarcated from ASME PTC 4 (superseding PTC 4.1) contract acceptance test procedures.
  6. **Source Register Fully Audited:** 16 primary sources categorized across 6 tiers, with current editions, withdrawal dates, and official URLs.
  7. **Deliverables Verified on Disk:**
     - `docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md` (64 records)
     - `docs/BOILER_MODULE_PLAN.md` (Act 06 of 2022 + S.R.O. 279-Law/2025 + 14 validated calculations)
     - `docs/GENERAL_MECHANICAL_KNOWLEDGE_MAP.md` (10 disciplines + ISO 20816-3:2026 scope)
     - `docs/KNOWLEDGE_BASE_DATA_MODEL.md` (Relational schema supporting content depth)
     - `docs/TECHNICAL_RESEARCH_SOURCE_REGISTER.md` (16 validated engineering sources)
     - `docs/PHASE_02_CONTENT_BLUEPRINT.md` (Reconciled 64 records + 21-section templates for `EQ-A01` & `EQ-B01`)
- **Phase 02 Acceptance Gate:** **PASSED**. Phase 02 is complete and verified. Ready for Phase 03 upon owner instruction.

---

### Phase 03: Android Architecture & Workspace Baseline
- **Status:** **COMPLETED** (Approved by Owner via Master Prompt 04)
- **Completed Deliverables on Disk:**
  1. `docs/PHASE_03_ANDROID_ARCHITECTURE.md` (Clean Architecture, MVVM, UDF, AppContainer DI, Package Structure).
  2. `docs/PHASE_03_DATABASE_ARCHITECTURE.md` (Room SQLite schema, FTS5 Virtual Tables, Seeding via Assets, Indexing).
  3. `docs/PHASE_03_NAVIGATION_AND_UX_PLAN.md` (17-module navigation hierarchy, Material 3 industrial dark theme, 21-section reader).
  4. `docs/PHASE_03_OFFLINE_SEARCH_AND_CONTENT_ENGINE.md` (Sub-50ms FTS5 search, Compose markdown parser, formula rendering, local WebP/SVG diagrams).
  5. `docs/PHASE_03_BACKUP_AND_DATA_PROTECTION.md` (Dynamic vs static data separation, SAF JSON backup, Zero Internet Permission security guarantee).
  6. `docs/PHASE_03_TECH_STACK_DECISION.md` (Evaluation of Kotlin + Compose + Room vs Flutter/React Native/XML, Min SDK 26, Target SDK 35).

---

### Phase 04: Core Domain & Room Database Implementation
- **Status:** **COMPLETED (PASS WITH CONDITIONS — Toolchain Environment Limitation)**
- **Completed Deliverables on Disk:**
  1. **Native Android Gradle Baseline:** `android/gradle/libs.versions.toml`, `android/settings.gradle.kts`, `android/build.gradle.kts`, `android/app/build.gradle.kts`, `android/app/src/main/AndroidManifest.xml` (DEC-016: Zero INTERNET permission enforced), `android/app/proguard-rules.pro`.
  2. **Pure Kotlin Domain Layer:** `ContentDepth.kt`, `EquipmentModels.kt`, `CalculationModels.kt`, `UserDataModels.kt`, `UnitConverters.kt`, `RepositoryInterfaces.kt`.
  3. **Room SQLite Database & Repositories:** `StaticEntities.kt`, `DynamicUserEntities.kt` (with NO_ACTION delete protection), `EquipmentFtsEntity.kt`, `RoomConverters.kt`, `Daos.kt`, `MechanicalProDatabase.kt`, `DatabaseMigrations.kt`, `EntityMappers.kt`, `EquipmentRepositoryImpl.kt`, `BoilerRepositoryImpl.kt`, `SearchRepositoryImpl.kt`, `CalculationRepositoryImpl.kt`, `UserDataRepositoryImpl.kt`, `AppContainer.kt`, `MechanicalProApplication.kt`.
  4. **Verified Seed Asset:** `seed_data.sql` and compiled pre-indexed binary SQLite asset `android/app/src/main/assets/database/mechanical_pro_notes.db` (140 KB).
  5. **Automated Verification:** `test_suite_phase04.py` — 10/10 automated tests passed (100% pass rate).
  6. **Phase 04 Documentation:** `docs/PHASE_04_IMPLEMENTATION_REPORT.md`, `docs/PHASE_04_DATABASE_IMPLEMENTATION.md`, `docs/PHASE_04_DOMAIN_MODEL_IMPLEMENTATION.md`, `docs/PHASE_04_TEST_RESULTS.md`, `docs/PHASE_04_KNOWN_LIMITATIONS.md`.
- **Known Limitations & Toolchain Assessment:**
  - Full `./gradlew assembleDebug` APK build could not be compiled directly in this Linux Web container due to absence of pre-installed OpenJDK 17 and Android SDK command-line tools. All code and database schemas are verified.
- **Next Step:** Phase 04 accepted as PASS WITH CONDITIONS. Phase 05 completed.

---

### Phase 05: Material 3 UI & Compose Reference Screens
- **Status:** **COMPLETED (PASS WITH CONDITIONS — Local Container Android SDK Limitation)**
- **Completed Deliverables on Disk:**
  1. **Material 3 Theme Foundation:** `presentation/theme/Color.kt`, `presentation/theme/Type.kt`, `presentation/theme/Theme.kt` (Industrial Navy, Safety Amber, Safety Red, Content-depth badges).
  2. **Reusable Material 3 UI Components:** `presentation/components/CommonComponents.kt` (`ContentDepthBadge`, `SafetyWarningCard`, `MathFormulaCard`, `EquipmentCardItem`, `EmptyStateCard`).
  3. **17-Module Dual-Tier Navigation:** `presentation/navigation/Screen.kt`, `presentation/navigation/AppNavigation.kt` (ModalNavigationDrawer with 17 destinations, BottomNavigationBar with 4 primary destinations).
  4. **Compose Reference Screens (12 Screens):**
     - Home Dashboard: `HomeScreen.kt`, `HomeViewModel.kt`.
     - Equipment Browsing: `EquipmentListScreen.kt`, `EquipmentViewModel.kt`.
     - 21-Section Machinery Reader: `EquipmentDetailScreen.kt` (5-tab scrollable reader).
     - Dedicated Boiler Hub: `BoilerDashboardScreen.kt`, `BoilerViewModel.kt` (Gas & Jhut boilers, Act 06 of 2022, Rules 2025).
     - Engineering Calculations: `CalculationListScreen.kt`, `CalculationRunnerScreen.kt`, `CalculationViewModel.kt`.
     - Offline Search: `SearchScreen.kt`, `SearchViewModel.kt` (Debounced FTS5 English/Bengali search).
     - Personal Field Notes: `NotesScreen.kt`, `NotesViewModel.kt`.
     - Saved Bookmarks: `BookmarksScreen.kt`.
     - Maintenance Checklists: `ChecklistScreen.kt`, `ChecklistViewModel.kt`.
     - Technical Reference Library: `ReferenceLibraryScreen.kt`.
     - Settings & Local Data: `SettingsScreen.kt`, `SettingsViewModel.kt`.
     - Root Activity: `MainActivity.kt`.
  5. **Automated Verification:** `test_suite_phase05.py` — 10/10 automated tests passed (100% pass rate).
  6. **Phase 05 Documentation Deliverables:** `PHASE_05_UI_IMPLEMENTATION.md`, `PHASE_05_NAVIGATION_IMPLEMENTATION.md`, `PHASE_05_SCREEN_CATALOG.md`, `PHASE_05_VIEWMODEL_INTEGRATION.md`, `PHASE_05_UI_TEST_RESULTS.md`, `PHASE_05_KNOWN_LIMITATIONS.md`.
- **Owner Acceptance:** Formally accepted by Project Owner on 2026-11-12 as PASS WITH CONDITIONS.
- **Phase 06 Implementation & Verification:**
  - **Category A (14 Boiler Thermal & Utility Calculators):** `CALC-01` to `CALC-14` implemented in `BoilerThermalCalculations.kt`.
  - **Category B (6 Wash Plant Maintenance Tools):** `CALC-15` to `CALC-20` implemented in `WashPlantMaintenanceCalculations.kt`.
  - **Thermodynamic Engine:** `IapwsIf97SteamEngine.kt` implemented (0.5 to 20 bar, cubic/log interpolation, $h_g \equiv h_f + h_{fg}$).
  - **Database Integration:** Seeded Room SQLite with all 20 calculation modules and 79 formula variables.
  - **Material 3 UI Screens:** Enhanced `CalculationListScreen.kt` (with category chips and hero steam card), `CalculationRunnerScreen.kt` (dynamic forms and note history logging), and new `SteamTableScreen.kt` (gauge/absolute toggles and presets).
  - **Verification:** `test_suite_phase06.py` (10/10 tests passed, 100%) and Phase 05 regression suite (10/10 tests passed, 100%).
  - **Deliverables:** `PHASE_06_ENGINEERING_SPECIFICATIONS.md`, `PHASE_06_CALCULATION_IMPLEMENTATION.md`, `PHASE_06_STEAM_TABLE_ENGINE.md`, `PHASE_06_TEST_RESULTS.md`, `PHASE_06_KNOWN_LIMITATIONS.md`.
- **Owner Acceptance:** Formally accepted by Project Owner on 2026-11-12 as **PASS WITH CONDITIONS — CONDITIONAL OWNER ACCEPTANCE** (DEC-020) subject to 9 conditions:
  1. Preserving all existing Phase 06 code, database changes, tests, and documentation.
  2. Zero repetition of Phase 06 audit or unnecessary recreation of completed code.
  3. Preserving all 20 calculation modules and Phase 05 calculations.
  4. Carrying forward unresolved engineering verification items into Phase 07.
  5. The IAPWS-IF97 engine must NOT be claimed as fully accuracy-verified until numerical benchmark values, independent reference comparisons, and documented error measurements are available.
  6. Maintaining technical verification and safety restrictions on CALC-02, CALC-09, CALC-14, CALC-16, CALC-18, and CALC-20.
  7. No claim of native Android compilation or physical-device verification.
  8. Preserving DEC-014, DEC-016, Free-First policy, personal-use scope, and project boundaries.
  9. Zero unrelated code changes, Git commits, pushes, deployment, paid services, or external integrations.

---

### Phase 07: Offline Validation, Defect Remediation & Re-Verification
- **Status:** **REMEDIATED & RE-VERIFIED (PASS WITH CONDITIONS)** (`DEF-P7-01`–`08` Closed + Direct-Fix Completion Applied)
- **Execution Deliverable:** `docs/PHASE_07_EXECUTION_REPORT.md` (DOC-MPN-019), `test_suite_phase07.py`, and `phase07_test_summary.json` summarizing 32 checks across 8 pillars (**27 PASS, 0 FAIL, 1 BLOCKED, 2 PENDING, 2 UNVERIFIED**):
  1. *Offline Application Validation (`DEC-016`):* **3 PASS.** Complete air-gap verified across `AndroidManifest.xml` (zero `INTERNET`, `allowBackup="false"`) and 54 main `.kt` files; 64-asset SQLite DB (**544,768 bytes**) + 5 offline Vector Drawable schematics (`diag_*.xml`) verified.
  2. *Native Android Build Readiness:* **2 PASS, 1 BLOCKED.** `DEF-P7-01` (Gradle 8.9 `gradle-wrapper.jar` + executable `gradlew`/`gradlew.bat`, KSP `2.0.21-1.0.27`, Material Icons Extended, `proguard-rules.pro` keep rules, `res/`, JVM JUnit tests with `ALL_CALCULATIONS` fix) & `DEF-P7-02` (presentation/ViewModel/domain signature parity) closed. Container lacks `javac`/`kotlinc`/`sdkmanager` (`BLOCKED`).
  3. *Physical-Device Ergonomics & UI Spec:* **3 PASS, 1 PENDING.** Industrial palettes, DataStore `AppThemeMode`, `1.18x` High Legibility scaling, bottom `NavigationBar`, `48.dp` target sizing on `EquipmentCardItem`, `ZoomableSchematicCard`, `AlertDialog` confirmations, 16-family search chips, and all 9 secondary domain views (`DomainModuleSpec`) verified; physical smartphone testing `PENDING`.
  4. *Engineering Calculations & Safety (20 Modules):* **8 PASS, 1 UNVERIFIED.** `DEF-P7-03` (`CALC-02` retained as `RESEARCH_PENDING`) & `DEF-P7-04` (`CALC-20` `mm->cm` $10^5$ scaling = `0.0191 bar` + `velocityMs > 10 m/s` + `0.1 bar/100m` header gradient alert + `CALC-08,10,11,12,14,16,18` bounds/safety guards) closed; 20/20 formula parity verified; `EngineeringCalculationsTest.kt` JVM run `UNVERIFIED`.
  5. *IAPWS-IF97 Steam Engine Audit:* **2 PASS, 1 UNVERIFIED.** `DEF-P7-05` (**56** strictly monotonic `GRID` rows) & `DEF-P7-06` (approved **linear-pressure interpolation**: **21/21 PASS** vs DOC-MPN-016, max $|\Delta T_{sat}| = 0.040^\circ\text{C}$, max $\epsilon(h_g) = 0.0362\%$, max $\epsilon(v_g) = 0.0808\%$) closed; `SteamEngineTest.kt` authored with midpoint tests; independent external NIST REFPROP solver remains `UNVERIFIED` (Owner Condition 5).
  6. *Database Integrity & `DEC-014`:* **3 PASS.** All **64/64** equipment assets across **16/16** families seeded with full baseline records (`78` components, `130` params, `304` sections, `64` FMEA, `64` PM; `2` FULL / `9` PARTIAL / `7` RESEARCH_PENDING / `46` INVENTORY_ONLY preserved); savepoint cascade confirmed `user_note`/`calculation_history` survival; `MechanicalProDatabase.kt` wires `.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)`; `DEF-P7-07` closed (`database.withTransaction` atomic `backup_version = 2` SHA-256 JSON + deduplication, Android SAF `CreateDocument`/`OpenDocument`, Room/SQLite `user_note` DDL parity).
  7. *Navigation & UI Runtime Topology:* **3 PASS, 1 PENDING.** 17 drawer routes + `Notes`/`Bookmarks` shortcuts + all 9 secondary domain modules + calculation/steam links, 5-tab machinery reader with `SimpleMarkdownText` & zoomable schematics, and 8 `StateFlow` ViewModels verified; rotation and Compose frame timing `PENDING` smartphone execution.
  8. *Plant Floor Reliability & Statutory Citations:* **3 PASS.** Python FTS4 latency over 64 enriched assets benchmarked at `0.268 ms` avg / `1.292 ms` max (`< 50 ms` target) with sanitized `buildSafeFtsQuery()`; **The Boilers Act, 2022** (`> 25 L`, annual visual + live safety-valve popping renewal) and **Bangladesh Boiler Rules, 2025** (`Grade-1/2/3`, 30y/60y caps) citations verified across `ChecklistViewModel`, `BoilerDashboardScreen`, `ReferenceLibraryScreen` (`REF-01`–`REF-12`), & `EquipmentDetailScreen`; `DEF-P7-08` closed via Room `ChecklistProgressEntity`/`ChecklistDao` + `DataStore`.
- **Regression Suites:** `test_suite_phase04.py` (**10/10 PASS**), `test_suite_phase05.py` (**10/10 PASS**), and `test_suite_phase06.py` (**10/10 PASS**) all passed cleanly.
- **Carried-Forward Non-Container Verification:** Native APK compilation (`BLOCKED`), Kotlin JVM JUnit execution (`UNVERIFIED`), external NIST REFPROP benchmark (`UNVERIFIED` per Condition 5), and physical smartphone validation (`PENDING`).

---

### Phase 08: GitHub Repository Preparation & Owner Push
- **Status:** **WORKSPACE PREPARED (READY FOR OWNER BUILD & PUSH)**
- **Authorized Target Repository:** `researchbangla/mechanical-pro-notes` exclusively (`DEC-002`, `DEC-004`).
- **Completed Deliverables:** Root `.gitignore`, `android/.gitignore`, `android/gradle/wrapper/gradle-wrapper.jar` (Gradle 8.9, SHA-256 verified), executable `android/gradlew` (permission restored to `-rwxr-xr-x` during P0 recovery), and root `README.md` (`./gradlew testDebugUnitTest assembleDebug` / `adb install` / owner commit & push workflow). APK not yet built; zero `git commit` or `git push` executed.

---

## 3. Post-Phase-07 Verification Stages, Gap Remediation Batches & P0 Recovery (Synchronized 2026-11-16)

| Workstream / Stage | Scope | Reported Static / Audit Status | Runtime / Native Status |
|---|---|---|---|
| **Stage 01** (`test_suite_stage01.py`) | Build/manifest/DB packaging & wrapper checks | Pre-recovery: `9/10` (`android/gradlew` lacked `+x`); **Post-P0 Recovery: `10/10 PASS`** | Native `./gradlew` execution blocked (no JDK 17) |
| **Stage 02** (`test_suite_stage02.py`) | Domain, content depth & engineering catalogs | **`8/8 PASS`** (static Python check) | JVM JUnit execution unconfirmed |
| **Stage 03** (`test_suite_stage03.py`) | Database (`753,664` bytes, `603` knowledge sections), FTS & backup codec | **`10/10 PASS`** (static Python check) | Room/KSP compile & migrations unverified (`schemas/` absent) |
| **Stage 04** (`test_suite_stage04.py`) | UI state, `@Preview`s & accessibility checks | **`10/10 PASS`** (static Python check; re-verified after Batch 09) | Compose rendering & touch QA unverified |
| **Stage 05** (`DOC-MPN-025`) | Android Preview / Emulator readiness audit | Audit complete | **BLOCKED** (missing JDK 17, Android SDK 35, Android Studio, Emulator/AVD, `/dev/kvm`) |
| **Stage 05B** (`DOC-MPN-028`) | High-priority gap validation & batch planning | **COMPLETE** (planning doc authored) | N/A |
| **Batch 01** (`DOC-MPN-029`) | `GAP-01` (dynamic calc categories), `GAP-02` (Calc History sheet), `GAP-04` (bottom bar `shortTitle`s) | Source & JUnit tests authored | Runtime unverified |
| **Batch 02** (`DOC-MPN-030`) | `GAP-03` (bookmark toggles for Runner, Checklist, Library, Boiler; `BOILER_SECTION` routing) | Source & JUnit tests authored *(remaining `GAP-03` items closed in Batch 04)* | Runtime unverified |
| **Batch 03** (`DOC-MPN-031`) | `GAP-06` core data integrity (preserve checklist `remarks` & timestamps, remark edit dialog, `3` `@Test`s / `7` scenarios across 4 files) | Source & JUnit tests authored; secondary `GAP-06` remains open | Runtime unverified |
| **Batch 04** (`DOC-MPN-032`) | `GAP-05` (`HomeScreen` saved bookmarks & shortcuts), `GAP-03` (`EquipmentViewModel` reactive bookmarks + `FundamentalsScreen` bookmark toggle & `FUND-*/REF-*` routing) across 6 files | Source & `4` `@Test`s authored; **`Stage 04` `10/10` static PASS** | Runtime unverified |
| **Batch 05** (`DOC-MPN-033`) | `GAP-07` (Calculation/Personal Note/Calc History offline search, `SearchScope` UI, `openNoteById`, `focusHistoryRecord`, Search vs Bookmarks separation) across 12 files | Source & `7` `@Test`s authored; **`Stage 01..04` & `Phase 05` static PASS** | Runtime unverified |
| **Batch 06** (`DOC-MPN-034`) | `GAP-08` (`CoolingHeating`/`Etp` calc corrections, catalog-backed `MOD-05..12`, dedicated `FAM-C/I/M/N/O/P` destinations, zero duplicate routes) across 4 files | Source & `4` `@Test`s authored; **`Stage 01..04` & `Phase 05` static PASS** | Runtime unverified |
| **Batch 07** (`DOC-MPN-035`) | `GAP-09` (Searchable equipment picker, unassociated & legacy-ID safety, `tags_csv` tagging, debounced Notes search, filters/sorts, `createdAt` preservation, detail dialog, search refresh) across 12 files | Source & `6` `@Test`s authored; **`Stage 01..04`, `Phase 05..06` static PASS** | Runtime unverified |
| **Batch 08** (`DOC-MPN-036`) | `GAP-10` (Calculation discovery search/reset, `UnitSystem` DataStore switching & `UnitPreferenceHelper`, Boiler `CALC-01..14` shortcuts & `CALC-02` `RESEARCH_PENDING`, Steam Table single-route consistency) across 18 files | Source & `4` `@Test`s authored; **`Stage 01..04`, `Phase 05..06` static PASS** | Runtime unverified |
| **Batch 09** (`DOC-MPN-037`) | `GAP-11` (Complete Settings layout + live counters, transactional 4-table personal data wipe preserving catalogs & preferences, two-step confirmation dialogs, duplicate/error safety, post-delete search refresh) across 7 files | Source & `3` `@Test`s authored; **`Stage 01..04`, `Phase 05..06` static PASS** | Runtime unverified |
| **Batch 10** (`DOC-MPN-038`) | `GAP-06` (Equipment association & Equipment Detail entry, optional operator & 4-label shift execution metadata, custom checklist CRUD + packaged template protection, status summary & duplicate-write guard, Settings wipe/count & `backup_version = 2` disclosure) across 13 files | Source & `5` `@Test`s authored; **`Stage 01..04`, `Phase 05..06` static PASS** | Runtime unverified |
| **Batch 11** (`DOC-MPN-039`) | `GAP-05` (Home `TopAppBar` Bookmarks shortcut, `HomePinnedBookmarkCard` independent `BookmarkRemove` button + safe removal in `HomeViewModel`/`NotesViewModel`, Featured Equipment live pin toggle via `EquipmentCardItem`, `HomeActivitySummary` & `HomeRecentActivityCard`, hardened deep-link resolution for `REF-*`/`FUND-*`/`EQ-*`/`CALC-*`/`CHK-*`) across 7 files | Source & `3` `@Test`s authored; **`Stage 04` `10/10` static PASS** | Runtime unverified |
| **Batch 12** (`DOC-MPN-040`) | `GAP-12` (`CALC-02` Indirect Boiler Efficiency ASME PTC 4-2013 / BS 845-1 / IS 8753 Natural Gas `IMPLEMENTED` + lab-calibrated/uncalibrated Mixed-Jhut boundary guards + `87` DB vars; `FUND-11..15` `VERIFIED_BASELINE` completing `15/15` Fundamentals + `DOC-MPN-009`/`010`/`014` & `FundamentalsScreen` updates) | Source, DB asset, docs & `+2` net `@Test`s authored; minimal sanity checks passed (`0` errors) | Runtime unverified |
| **Batch 13** (`DOC-MPN-041`) | `GAP-13` (`64/64` deep equipment upgrade [`56` FULL / `1` PARTIAL `EQ-G02` / `7` RESEARCH_PENDING / `0` INVENTORY_ONLY], `1,190` unique `knowledge_section` rows, `209` components, `206` params, `130` FMEA, `128` PM procedures; `18` AAPT2-safe `diag_*.xml` schematics across `16` families; machine-aware condition monitoring & adaptive Section 13 cards; `DOC-MPN-010` `SRC-023..028`, `DOC-MPN-007` & `+3` `@Test`s in `EquipmentMappingTest.kt`) | Source, schematics, `856,064`-byte DB, docs & `+3` `@Test`s authored; minimal sanity checks passed (`0` errors) | Runtime unverified |
| **Batch 14** (`DOC-MPN-042`) | Standards, Boiler Law & Steam Engine (`REF-06` `ISO 20816-3:2026` `120–30,000 r/min` + exclusions across `EngineeringCatalogs.kt`/`EquipmentDetailScreen.kt`/DB; Boilers Act `06` of `2022` & `S.R.O. 279-Law/2025` verified + `Rule 44`/`Sec 11`/`24h`/`13 April`/`২০১৫` fixes; `IapwsIf97SteamEngine` Eq. 31 + log-P `v_g` + unsupported region docs + `SteamTableScreen.kt`; `+4` `@Test`s & `DOC-MPN-008/010/016/042`) | Source, `856,064`-byte DB (`fe2fb5f9c47cabc2abb56e2873b3531b1b1512d2f4e09df6cadaef1791b90720`), docs & `+4` `@Test`s authored; minimal sanity checks passed (`0` errors) | Runtime unverified |
| **Batch 14 Closure** (`DOC-MPN-043`) | Independent engineering validation (`ISO 20816-3:2026`, Boilers Act 2022 / Rules 2025 web verification, `501`-pt IAPWS-95 `P_sat` `<= 0.0179%` & Clausius-Clapeyron `h_fg` `<= 0.0454%`) & 31-item Categories A–H gap register | Static/math/web audit complete | Native build & REFPROP blocked |
| **Batch 15A** (`DOC-MPN-044`) | `chmod +x android/gradlew` (`0755`), attempted `./gradlew --version` / `testDebugUnitTest` / `assembleDebug` / `lintDebug`, Room `16/16` entity-SQLite parity, Manifest `0`-permission check & `gradle.properties` assessment | `android/gradlew` `+x` fixed (`PARTIAL`), `125` `@Test`s (`0` `@Ignore`) & DB/Manifest verified statically | **BLOCKED** in container (JDK 17, SDK 35, KSP export, Emulator & `.git` absent) |
| **GitHub Prep** (`MPN-GITHUB-READY-01`, `DOC-MPN-045`) | Safe `.gitignore` hardening (root + `android/.gitignore`, Room `schemas/` preserved), secret/privacy scan (`0` secrets, `0` user DB rows), Gradle wrapper `0755` check & mobile-friendly `GITHUB_REPOSITORY_SETUP_AND_UPLOAD_CHECKLIST.md` | **PREPARED** (`.git` & remote intentionally not created) | Owner UI repo creation & host Batch 15A build pending |

### Summary of Open Items & Verification Gaps
- **JUnit Test Execution:** **`125` `@Test` methods** (`0` `@Ignore`) are authored across `12` files in `android/app/src/test/`; native JVM execution (`./gradlew testDebugUnitTest`) remains **BLOCKED** in container (`EXIT=1`, no `JAVA_HOME`/`java`).
- **Open Functional Gaps (`DOC-MPN-043` / `DOC-MPN-044`, 31-Item Register):** `GAP-01`–`GAP-13` core UI & Batch 14 fixes are implemented in source/DB, while P0–P3 work remains across native APK/KSP/Lint/Emulator verification, external NIST REFPROP/CoolProp comparison, `backup_version = 3` custom-checklist portability (`REM-G01`), `7` `RESEARCH_PENDING` + `EQ-G02`/`CALC-02` mixed-jhut data (`REM-B01/B02/C01`), full Gazette PDF `67` minor rules (`REM-D02`), and host `android/gradle.properties` / tooling hygiene.
- **External Verification Still Needed:** Host JDK 17 + Android SDK 35 native `./gradlew testDebugUnitTest lintDebug assembleDebug` + Emulator QA, independent **NIST REFPROP / CoolProp** solver cross-check on `h_f`, `h_fg`, `v_f`, `v_g`, and full **Bangladesh Gazette PDF** text verification for all 69 minor rules in `S.R.O. No. 279-Law/2025`.

