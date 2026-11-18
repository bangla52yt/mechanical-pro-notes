# BATCH 08 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-036  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 08 (`GAP-10` Calculation Discovery, Unit System Switching & Boiler Navigation Integration: Calculation List Search/Filter/Empty-Reset, `SI`/`Metric`/`Imperial` Preference Persistence via DataStore & Verified Display Conversions, Boiler Dashboard `CALC-01..14` Navigation & `CALC-02` `RESEARCH_PENDING` Disclosure, Single-Route Steam Table Consistency & Focused JUnit Tests)  
**Documentation Date:** 2026-11-16  
**Status:** BATCH 08 FEATURES & UNIT TESTS IMPLEMENTED IN SOURCE (`test_suite_stage01..04.py`, `test_suite_phase05.py`, `test_suite_phase06.py`: ALL PASS STATIC) — NATIVE KOTLIN COMPILE, JVM JUNIT EXECUTION, COMPOSE RENDERING & APK BUILD REMAIN **UNVERIFIED / BLOCKED** IN CONTAINER

---

## 1. Executive Summary
Batch 08 implemented **`GAP-10`** across **18 Kotlin source and test files** (`1` created, `17` modified), preserving the existing Room schema and migrations, pre-packaged SQLite database (`753,664` bytes), Backup JSON codec (`BackupJsonCodec.kt`), all `20` calculation formulas and outputs, `8` `*ViewModel.kt` count (`[S3-06]` / `[S4-08]`), `23` `Screen` objects (`[S4-01]`), zero internet permission (`DEC-016`), and existing user notes/bookmarks/history data:

1. **Objective A — Calculation Discovery & Navigation (`CalculationViewModel.kt`, `CalculationListScreen.kt`, `SearchRepositoryImpl.kt`):**
   - Added `CalculationCatalogFilter.filter(calculations, query, selectedCategory)` matching NFC-normalized Calculation ID, code, English title, Bengali title, category, engineering reference, formula, assumptions, limitations, status, and variable metadata (`symbol`, `name`, `unit`), ranked by ID/title/metadata relevance and deduplicated cleanly.
   - Wired `searchQuery`, `selectedCategory`, and `filteredCalculations` into `CalculationListUiState` alongside `onSearchQueryChange`, `selectCategory`, and `clearFilters()` (synchronizing category chips & search state cleanly in `CalculationListScreen.kt` with a `>= 48.dp` `"Reset Search & Filters"` button in the empty state).
   - Preserved `Screen.CalculationRunner.createRoute(calc.id)` as the single calculation execution route and deduplicated `SearchRepositoryImpl.searchCalculations` results without altering Batch 07 Notes search or global `SearchScreen`.
2. **Objective B — Unit System Switching & Safe Verified Conversions (`UserDataModels.kt`, `RepositoryInterfaces.kt`, `UserPreferencesRepository.kt`, `UnitPreferenceHelper.kt`, `SettingsViewModel.kt`, `SettingsScreen.kt`, `UnitConverterStateHolder.kt`, `UnitConverterScreen.kt`, `CalculationViewModel.kt`, `CalculationRunnerScreen.kt`, `SteamTableScreen.kt`, `AppNavigation.kt`):**
   - Added `enum class UnitSystem` (`SI`, `METRIC`, `IMPERIAL`, default `METRIC`) to `UserPreferences` persisted in DataStore (`Keys.UNIT_SYSTEM`, safe fallback parsing to `METRIC`, zero Room schema or backup format edits).
   - Added `Measurement Unit System` selector chips (`>= 48.dp`) on `SettingsScreen` wired to `SettingsViewModel.setUnitSystem(...)`.
   - Created `UnitPreferenceHelper` (`domain/unit/UnitPreferenceHelper.kt`) strictly reusing verified `UnitConverterEngine` unit definitions for both `UnitConverterStateHolder` presets and non-destructive secondary display equivalents (`bar(g)`, `bar(a)`, `°C`, `mm`, `cm`, `m`, `mm2`, `L`, `m3`, `kg`, `kW`, `kJ`, `m3/h`, `CFM`, `kg/m3`, `m/s`, `RPM`).
   - Ambiguous or unverified composite/dimensionless units (`mm WC`, `bar` ΔP, `kg/h`, `kJ/kg`, `m3/kg`, `Nm3/h`, `Nm3/min`, `%`, `ppm`, `ratio`, `G`) return `DisplayUnitConversion.Retained` with explicit disclosure so canonical formulas, database bounds, notes, bookmarks, and saved calculation history remain completely untouched.
3. **Objective C — Boiler Dashboard Navigation Integration (`BoilerViewModel.kt`, `BoilerDashboardScreen.kt`, `AppNavigation.kt`):**
   - Added `BoilerCalculationCatalog` mapping all `14` boiler calculations (`CALC-01..14`) for `BoilerTab.OVERVIEW`, contextual subsets for `GAS_BOILERS` (`10` calcs) and `JHUT_BOILERS` (`9` calcs), and preliminary safety valve check `CALC-09` for `STATUTORY_RULES`.
   - Rendered interactive `>= 48.dp` `BoilerCalculationShortcutCard`s navigating directly to `Screen.CalculationRunner.createRoute(calc.id)`, explicitly marking `CALC-02` as `RESEARCH_PENDING` (*"Execution disabled (inspect definition)"*) without inventing calculations or claiming statutory compliance.
   - Added accessible `>= 48.dp` IAPWS-IF97 Saturated Steam Table shortcuts in the Boiler TopAppBar, Overview hero card, Gas Boilers tab, and Jhut Boilers tab.
4. **Objective D & E — Steam Table Shortcut Consistency, UI Quality & Accessibility (`AppNavigation.kt`, `SteamTableScreen.kt`, `CalculationListScreen.kt`, `CalculationRunnerScreen.kt`, `BoilerDashboardScreen.kt`):**
   - Verified all Steam Table triggers (`HomeScreen`, `BoilerDashboardScreen`, `CalculationListScreen`, `CalculationRunnerScreen`, and `SecondaryModuleScreen` for `Screen.Steam`) target the single `Screen.SteamTable.route` (`"steam_table"`) without duplicate screens or routes.
   - Preserved `.imePadding()`, `>= 48.dp` touch targets, `heading()`/`Role.Button` semantics, stateless `...Content(...)` composables with defaulted parameters, and all `40` `@Preview` composables.

---

## 2. Exact Source, Test & Documentation Files Created or Modified (`18` Code/Test Files + Docs)

| File Path | Action | Rationale & Implemented Change |
| :--- | :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/model/UserDataModels.kt` | Modified | Added `UnitSystem` enum (`SI`, `METRIC`, `IMPERIAL`) and `unitSystem: UnitSystem = UnitSystem.METRIC` to `UserPreferences`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/repository/RepositoryInterfaces.kt` | Modified | Added defaulted `suspend fun setUnitSystem(unitSystem: UnitSystem) {}` to `UserPreferencesRepository`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/local/preferences/UserPreferencesRepository.kt` | Modified | Persisted `unit_system` preference in DataStore with safe `METRIC` fallback. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/unit/UnitPreferenceHelper.kt` | Created | Pure-Kotlin helper providing `preferredCategoryUnits` and `convertForDisplay` strictly via `UnitConverterEngine` with `DisplayUnitConversion.Converted` / `Retained` safety. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/SearchRepositoryImpl.kt` | Modified | Deduplicated calculation search results by normalized uppercase ID while preserving `[S3-08]` isolation. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/settings/SettingsViewModel.kt` | Modified | Exposed `unitSystem` in `SettingsUiState` and added `setUnitSystem(unitSystem)`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/settings/SettingsScreen.kt` | Modified | Added accessible `SI` / `Metric` / `Imperial` `FilterChip`s (`>= 48.dp`) to Display & Readability settings card. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/units/UnitConverterStateHolder.kt` | Modified | Added `preferredUnitSystem` and `applyUnitSystemPreference` updating category unit presets safely. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/units/UnitConverterScreen.kt` | Modified | Updated TopAppBar subtitle to indicate active `${state.preferredUnitSystem.displayName} Preset`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationViewModel.kt` | Modified | Added `CalculationCatalogFilter`, search/category filter state, optional `UserPreferencesRepository`, and `unitSystem` propagation to list & runner states. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationListScreen.kt` | Modified | Added `.imePadding()`, calculation discovery search bar with `48.dp` clear icon, active `UnitSystem` caption, and `48.dp` reset CTA in empty state. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationRunnerScreen.kt` | Modified | Added active `UnitSystem` header indicator and live verified input/output equivalent hints via `UnitPreferenceHelper` without altering canonical calculations or formatters. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/SteamTableScreen.kt` | Modified | Added optional `unitSystem` parameter showing verified SI/Imperial equivalents alongside canonical IAPWS-IF97 metrics while leaving `kJ/kg` & `m3/kg` untouched. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerViewModel.kt` | Modified | Added `BoilerCalculationCatalog` mapping `CALC-01..14` on Overview and contextual subsets across Boiler tabs. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerDashboardScreen.kt` | Modified | Added interactive `CALC-01..14` cards (`CALC-02` `RESEARCH_PENDING` marked clearly) and Steam Table shortcuts across tabs & TopAppBar. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Modified | Wired `userPreferencesRepository` into `CalculationViewModel`, `UnitConverterStateHolder`, `BoilerDashboardScreen`, and `SteamTableScreen`, plus `Screen.SteamTable.route` from Boiler module. |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Modified | Added `FakeUserPreferencesRepository` and `3` `@Test` methods (`calculationCatalogFilterAndViewModelDiscoverByIdCodeTitleBengaliCategoryVariableAndReset`, `unitSystemPreferencePersistsAcrossSettingsCalculationsConverterWithoutMutatingUserData`, `boilerCalculationCatalogCoversCalc01Through14AndPreservesResearchPendingAndSteamRoute`). |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/UnitConverterTest.kt` | Modified | Added `1` `@Test` method (`unitPreferenceHelperConvertsVerifiedUnitsAndRetainsAmbiguousOrUnsupportedUnits`). |

*Documentation created/updated in Batch 08:* `docs/BATCH_08_IMPLEMENTATION_REPORT.md` (`DOC-MPN-036`), plus synchronized checkpoint metadata in `docs/CURRENT_WORK_CHECKPOINT.md`, `docs/SESSION_HANDOVER.md`, and `docs/PHASE_PROGRESS_TRACKER.md`. Zero Room schema/migration changes, Backup JSON codec edits, pre-packaged SQLite DB changes (`753,664` bytes preserved), calculation formula edits, or Git commits/pushes were performed.

---

## 3. Authored Tests vs. Executed Verification (Strict Separation)

### Authored JVM JUnit Tests (NOT Executed on JVM)
- **Batch 08 Additions (`4` `@Test` methods total across 2 test files):**
  - *`Stage04UiStateAndAccessibilityTest.kt` (`3` new `@Test`s; now `37` in file):*
    1. `calculationCatalogFilterAndViewModelDiscoverByIdCodeTitleBengaliCategoryVariableAndReset`
    2. `unitSystemPreferencePersistsAcrossSettingsCalculationsConverterWithoutMutatingUserData`
    3. `boilerCalculationCatalogCoversCalc01Through14AndPreservesResearchPendingAndSteamRoute`
  - *`UnitConverterTest.kt` (`1` new `@Test`; now `8` in file):*
    1. `unitPreferenceHelperConvertsVerifiedUnitsAndRetainsAmbiguousOrUnsupportedUnits`
- **Total Authored JUnit Inventory:** **`105` `@Test` methods across `12` test files** in `android/app/src/test/`. **Native JVM JUnit execution (`./gradlew testDebugUnitTest`) was NOT executed** because JDK 17 and Android SDK 35 are absent in the container environment.

### Static / Python Verification Executed (Post-Batch 08)
- **`python3 test_suite_stage01.py` -> `10/10 PASSED` (`exit 0`)**
- **`python3 test_suite_stage02.py` -> `8/8 PASSED` (`exit 0`)**
- **`python3 test_suite_stage03.py` -> `10/10 PASSED` (`exit 0`)**: Confirmed `[S3-06]` (`8` ViewModels), `[S3-08]` (`user_note` isolation preserved), and balanced braces/parens across `76` `.kt` files.
- **`python3 test_suite_stage04.py` -> `10/10 PASSED` (`exit 0`)**: Confirmed `23` `Screen` objects (`[S4-01]`), `.imePadding()` & responsive layouts (`[S4-04]`), `>=48.dp` targets & accessibility labels (`[S4-05]`), `40` `@Preview` functions (`[S4-07]`), `37` Stage 04 `@Test`s & `8` ViewModels (`[S4-08]`), preserved DB byte size (`753,664` B) & calc SHA-256 (`[S4-09]`), and syntax balance (`[S4-10]`).
- **`python3 test_suite_phase05.py` -> `10/10 PASSED` (`exit 0`)**
- **`python3 test_suite_phase06.py` -> `10/10 PASSED` (`exit 0`)**
- **Scope Disclaimer:** Python static checks inspect source structure, annotations, invariants, and pre-packaged SQLite integrity only; they do **not** establish Kotlin compilation, Compose layout rendering, Room query compilation, APK packaging, or Android runtime behavior.

---

## 4. Open Items & Preserved Risks
- **Unresolved Feature / Content Gaps:** `GAP-11`, secondary `GAP-06`, `GAP-12` (`CALC-02` `RESEARCH_PENDING`, `FUND-11..15` `CONTENT_GAP`), and `GAP-13` (`62/64` non-full assets, `5/64` schematics).
- **Engineering / Legal Items Preserved:** `REF-06` ISO 20816-3 RPM conflict (`15,000` vs `30,000 RPM`), independent legal verification of Bangladesh Boilers Act, 2022 & Rules, 2025, and external NIST REFPROP comparison for `IapwsIf97SteamEngine`.
- **Native Verification Blocked Outside Container:** Open `android/` in Android Studio (JDK 17 + Android SDK 35), inspect `@Preview` composables, run `./gradlew testDebugUnitTest assembleDebug` (`105` authored JUnit tests), and verify on an API 34/35 Android Emulator and physical device before any owner Git push.
