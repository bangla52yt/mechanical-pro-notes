# STAGE 03 REPORT: DATABASE INTEGRITY, EQUIPMENT KNOWLEDGE & CORE MODULES
**Document Identifier:** DOC-MPN-023  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Scope:** Stage 3 Areas A–E (Room Migrations & Integrity, 62-Asset Equipment Knowledge Enrichment, Multi-Select Filters, 13-Category Unit Converter, Authoritative Catalogs, Search & Fundamentals)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Date:** 2026-11-14  

---

## 1. Executive Summary
Stage 3 execution across **Areas A through E** is complete at the source-code, pre-packaged SQLite database asset (`mechanical_pro_notes.db`), JUnit test authoring, and Python/SQLite verification levels, preserving all prior Phase 01–07 deliverables, Stage 1 defect repairs (`DEF-AUD-01`–`08`), Stage 2 calculation validations (`CALC-02, 06, 09, 11, 19, 20`), offline-only policy (`DEC-016`), and personal user-data isolation (`DEC-014`).

| Evidence Tier | Stage 3 Coverage Summary |
| :--- | :--- |
| **[A] Verified Code / SQLite / Static** | Explicit Room v1 non-destructive migration contract (`DatabaseMigrations.kt`); all `16` entities matched `1:1` against SQLite; `62/62` non-FULL equipment records enriched across all `9` required content areas without promoting depth badges (`2/9/7/46` preserved); `EquipmentFilter` & multi-select UI wired; `13`-category `UnitConverterEngine` + `UnitConverterScreen`; single-source `EngineeringCatalogs.kt` (`4` checklists, `12` references, `15` fundamentals with honest `5` `CONTENT_GAP` tags); isolated `searchKnowledgeBase`; `4` new JUnit test files (`20` `@Test` methods); `73` `.kt` files delimiter-balanced; `test_suite_stage03.py` **10/10 PASS**, `test_suite_stage02.py` **8/8 PASS**, `test_suite_stage01.py` **10/10 PASS**, `test_suite_phase07.py` **27 PASS / 0 FAIL / 1 BLOCKED / 2 PENDING / 2 UNVERIFIED**, `test_suite_phase04/05/06.py` **10/10 PASS each**. |
| **[B] Blocked in Container** | Native Kotlin compilation and Android APK assembly (`./gradlew testDebugUnitTest assembleDebug`) remain **BLOCKED** inside the Gemini web container because OpenJDK 17 (`javac`/`kotlinc`) and Android SDK 35 are absent. |
| **[C] Unverified / Pending External** | JVM runtime execution of all JUnit suites (`DatabaseIntegrityAndMigrationTest`, `EquipmentFilterTest`, `UnitConverterTest`, `UnifiedSearchAndCatalogTest`, `Stage02CalculationValidationTest`, Stage 1 & Phase 06–07 suites), external NIST REFPROP steam cross-check, and physical smartphone UI/rotation testing remain **PENDING / UNVERIFIED** on the Owner's PC/phone. |

---

## 2. Workstream A — Room Database Migration & Integrity
- **Modified:** `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/local/database/migration/DatabaseMigrations.kt`
- **Test Added:** `android/app/src/test/java/com/researchbangla/mechanicalpronotes/DatabaseIntegrityAndMigrationTest.kt` (`4` `@Test` methods)
- **Changes & Verification:**
  1. Audited `MechanicalProDatabase.kt` (`version = 1`, `16` entities) against `mechanical_pro_notes.db` (`PRAGMA user_version = 1`): all `15` relational tables and `equipment_fts` FTS4 virtual table match 1:1.
  2. Replaced the commented-out empty `MIGRATION_1_2` placeholder stub with an explicit non-destructive v1 contract (`EARLIEST_SUPPORTED_VERSION = 1`, `CURRENT_DATABASE_VERSION = 1`, `USER_DATA_TABLES`, `STATIC_KNOWLEDGE_TABLES`, `ALL_MIGRATIONS = emptyArray()`).
  3. Added runtime integrity helpers `verifyUserTablesIsolatedAndPresent(db)` (verifying all `4` user tables exist with `0` foreign keys pointing to static knowledge) and `verifyDatabaseIntegrity(db)` (`PRAGMA integrity_check == 'ok'`).
  4. Verified via live SQLite v1→v2 upgrade simulation in `test_suite_stage03.py` `[S3-01]` that synthetic rows in `user_note`, `user_bookmark`, `calculation_history`, and `checklist_progress` remain untouched (`(1, 1, 1, 1)`) with `PRAGMA foreign_key_check == []` and `integrity == 'ok'`.
  5. Note: `MechanicalProDatabase` retains `.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)` without `fallbackToDestructiveMigration()`, forbidding destructive upgrades.

---

## 3. Workstream B — Equipment Knowledge Content Enrichment (`mechanical_pro_notes.db`)
- **Updated Asset:** `android/app/src/main/assets/database/mechanical_pro_notes.db`
  - **File Size:** `753,664` bytes
  - **File SHA-256:** `d464bcb1de3459916db31687a5414a55fce10876bb8324aa1056c669f7e16ee0`
- **Protection Invariants Verified:**
  - `content_depth_status` counts strictly preserved: **`2` `FULL_CONTENT_BLUEPRINT`**, **`9` `PARTIAL_CONTENT_BLUEPRINT`**, **`7` `RESEARCH_PENDING`**, **`46` `INVENTORY_ONLY`** (`64` total assets; `equipment_fts` = `64` rows).
  - `EQ-A01` and `EQ-B01` retain all `21` original full blueprint sections untouched.
  - `calculation` (`20` rows) and `formula_variable` (`79` rows) strictly unchanged (canonical JSON SHA-256: `5dfc658879fe0adcb0904ee7361c2b229fd85e3ef1cedf90568bfae1443d19bd`).
  - Zero fabricated OEM tolerances, unverified intervals, or cross-machine boilerplate contamination (verified `'hardness slip'` exists strictly on `EQ-H01` Sodium Cation Exchange Water Softener only; unverified plant-specific parameters explicitly marked for field/nameplate confirmation).
- **Enrichment Totals Across All 64 Assets:**
  - `knowledge_section`: `603` rows (`>= 8` sections across every enriched record covering required sections `{1, 3, 4, 6, 9, 12, 15, 18, 20}`)
  - `component`: `138` machine-specific rows
  - `technical_parameter`: `132` machine-specific rows
  - `failure_mode`: `117` machine-specific rows
  - `maintenance_procedure`: `64` machine-specific rows
  - 100% Unicode NFC clean across all bilingual English/Bengali rows; `PRAGMA integrity_check == 'ok'`; `PRAGMA foreign_key_check == []`.

---

## 4. Workstream C — Equipment List & Search Multi-Select Filters
- **Created:** `domain/equipment/EquipmentFilter.kt` (`EquipmentFilterCriteria`, `apply`, `countByDepth`, `tokenizeQuery`)
- **Updated:** `presentation/equipment/EquipmentViewModel.kt`, `EquipmentListScreen.kt`, `presentation/search/SearchViewModel.kt`, `SearchScreen.kt`
- **Test Added:** `EquipmentFilterTest.kt` (`6` `@Test` methods)
- **Capabilities Delivered:**
  1. Added `"All (64)"` tab alongside the `16` system families (`FAM-A`–`FAM-P`) in `EquipmentListScreen.kt` (via nullable `selectFamily(familyId: String?)`).
  2. Implemented multi-select OR-union `ContentDepth` `FilterChip` selectors (`Full`, `Partial`, `Inventory Only`, `Research Pending`) with live scoped counters (`countByDepth`), `"All"` reset chip, and inline search bar (matching English, NFC/NFD Bengali, equipment ID, aliases, category, and drive type).
  3. Added an informative empty state with `"Reset All Filters"` button on both `EquipmentListScreen` and `SearchScreen`, preserving stable `LazyColumn` keys (`key = { it.id }`).

---

## 5. Workstream D — 13-Category Engineering Unit Converter
- **Created:**
  - `domain/unit/UnitConverterEngine.kt` (`UnitCategory`, `UnitSubgroup`, `EngineeringUnit`, `UnitConversionResult`, `UnitConverterEngine`)
  - `presentation/units/UnitConverterStateHolder.kt` (preserves strict `8` `*ViewModel.kt` architecture invariant checked by Phase 07)
  - `presentation/units/UnitConverterScreen.kt`
- **Wired In:** `Screen.kt` (`Screen.UnitConverter`), `AppNavigation.kt` (`composable(Screen.UnitConverter.route)` + drawer icon), `CalculationListScreen.kt` (top-bar & banner shortcut), `HomeScreen.kt` (`Unit Converter (13)` quick-action button)
- **Test Added:** `UnitConverterTest.kt` (`7` `@Test` methods)
- **Engineering & Boundary Guarantees:**
  - All `13` physical categories: `Length`, `Area`, `Volume`, `Mass`, `Force`, `Pressure`, `Temperature`, `Power`, `Energy`, `Flow Rate`, `Density`, `Speed`, `Viscosity`.
  - Strict SI internal conversion; gauge vs absolute pressure disambiguation (`P_atm = 1.01325 bar(a)`) rejecting below-vacuum pressures (`< 0 bar(a)` / `< -1.01325 bar(g)`); offset-aware temperature conversion rejecting `< 0 K`; subgroup isolation preventing direct linear↔rotational speed and dynamic↔kinematic viscosity conversion without radius/density; rejection of `NaN`, `±Infinity`, blanks, and cross-category conversions.
  - Engineering formatting via `formatEngineeringValue` (no destructive `%.2f` truncation); `UnitConverters.kt` and all `CALC-01..20` formula calculators left cleanly separated and untouched.

---

## 6. Workstream E — Single-Source Catalogs, Unified Offline Search & Native Fundamentals
- **Created:**
  - `domain/catalog/EngineeringCatalogs.kt` (`PlantChecklistCatalog`, `TechnicalReferenceCatalog`, `FundamentalsCatalog`, `EngineeringCatalogSearch`)
  - `presentation/fundamentals/FundamentalsScreen.kt`
- **Updated:**
  - `presentation/checklists/ChecklistViewModel.kt` & `presentation/library/ReferenceLibraryScreen.kt` refactored to consume `PlantChecklistCatalog` and `TechnicalReferenceCatalog` as single sources of truth (zero duplicated catalog arrays).
  - `domain/repository/RepositoryInterfaces.kt` (`UnifiedSearchResult`, `searchKnowledgeBase`) & `data/repository/SearchRepositoryImpl.kt` (searches equipment via FTS4/LIKE + static catalogs via `EngineeringCatalogSearch` while keeping `user_note`, `user_bookmark`, `calculation_history`, `checklist_progress` isolated).
  - `presentation/search/SearchViewModel.kt` & `SearchScreen.kt` (added `SearchScope` filter chips: `All`, `Equipment`, `Checklists`, `References`, `Fundamentals` + click routing).
  - `presentation/home/HomeScreen.kt` (`Fundamentals (15)` quick-action button) & `presentation/navigation/AppNavigation.kt` (replaced `SecondaryModuleOverviewScreen` stub redirect for `Screen.Fundamentals` with native `FundamentalsScreen`).
- **Test Added:** `UnifiedSearchAndCatalogTest.kt` (`3` `@Test` methods)
- **Content-Gap Honesty:** `FundamentalsCatalog` delivers `FUND-01` through `FUND-10` as `VERIFIED_BASELINE` (grounded in `DOC-MPN-009`) and explicitly tags `FUND-11` through `FUND-15` (planned in `DOC-MPN-001 §4.1` without standalone General Knowledge chapters yet) as `CONTENT_GAP` so users are never misled by inflated completeness claims.

---

## 7. Complete Stage 3 Verification Ledger

| Suite / Artifact | Result in Container | Notes / Honest Boundary |
| :--- | :--- | :--- |
| `test_suite_stage03.py` | **10/10 PASS** | `[S3-01]`–`[S3-10]`: live SQLite v1→v2 upgrade preservation test, 16-table Room parity, 62/62 asset enrichment + NFC + calc SHA-256, Area C/D/E checks, 73 `.kt` brace/paren balance |
| `test_suite_stage02.py` | **8/8 PASS** | `[S2-01]`–`[S2-08]`: `CALC-02, 06, 09, 11, 19, 20` preserved cleanly alongside updated DB calc hash |
| `test_suite_stage01.py` | **10/10 PASS** | `DEF-AUD-01`–`08` preserved cleanly (`0` regressions) |
| `test_suite_phase07.py` | **27 PASS, 0 FAIL, 1 BLOCKED, 2 PENDING, 2 UNVERIFIED** | Zero open code defects (`DEF-P7-01`–`08` remain closed) |
| `test_suite_phase06.py` | **10/10 PASS** | 20 calculators + IAPWS-IF97 parity preserved |
| `test_suite_phase05.py` | **10/10 PASS** | Material 3 UI, 17-screen nav, FTS, JSON backup parity preserved |
| `test_suite_phase04.py` | **10/10 PASS** | Schema, isolation, FK rules, atomic rollback preserved |
| `./gradlew testDebugUnitTest assembleDebug` | **BLOCKED (No JDK / Android SDK)** | Requires execution on Owner's PC with JDK 17 + Android SDK 35 |
| Physical Device & NIST REFPROP Checks | **PENDING / UNVERIFIED** | Requires Owner's Android phone & external solver run |

---

## 8. Items Remaining Outside Container (Stage 4 / Owner Hardware Gate)
1. **Local JDK 17 / Android SDK 35 Build & JVM Tests:** Run `cd android && ./gradlew testDebugUnitTest assembleDebug` to execute Kotlin compilation and all `11` JUnit test suites on the JVM (original Phase 04–07 suites + Stage 1 + Stage 2 + 4 Stage 3 suites) and build `app-debug.apk`.
2. **External Steam Property Cross-Check:** Compare `IapwsIf97SteamEngine` against an independent NIST REFPROP / CoolProp solver (`Condition 5`, `P7-P5-03`).
3. **Physical Smartphone QA:** Install `app-debug.apk` on Android hardware and test Unit Converter precision, multi-select equipment filters, unified search across checklists/references/fundamentals, rotation/process-death state persistence, and field checklists (`P7-P3-04`, `P7-P7-04`).
