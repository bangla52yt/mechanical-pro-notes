# BATCH 01 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-029  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 01 Only (`GAP-04`, `GAP-01`, `GAP-02` per `docs/STAGE_05B_HIGH_PRIORITY_GAP_VALIDATION.md` [`DOC-MPN-028`])  
**Execution Date:** 2026-11-15  
**Status:** IMPLEMENTED IN SOURCE & VERIFIED BY STATIC / PYTHON REGRESSION SUITES (`0 FAIL`) — NATIVE KOTLIN COMPILE & JVM JUNIT RUN REMAIN **BLOCKED** IN CONTAINER (JDK 17 / ANDROID SDK 35 ABSENT)  

---

## 1. Executive Summary
Batch 01 remediated the three authorized high-priority UI/workflow gaps without modifying any SQLite schemas, calculation formulas, backup JSON contracts, ViewModel counts (`8`), Screen route counts (`23`), Visual Preview Gallery, or locked Material 3 palette tokens:

1. **`GAP-04` (Bottom Navigation Bar Short Labels):** Added explicit concise `shortTitle` labels (`"Home"`, `"Plant"`, `"Boiler"`, `"Calcs"`) on `Screen.kt` and rendered single-line `screen.shortTitle` inside `AppNavigation.kt`, eliminating `"Dedicated"` and `"Engineering"` label truncation while preserving full `screen.title` content descriptions for TalkBack and all `23` routes.
2. **`GAP-01` (Dynamic Calculation Category Filter Chips):** Replaced the stale hardcoded 8-string filter array in `CalculationListScreen.kt` with dynamic extraction (`extractCalculationCategories`) and case-insensitive filtering (`filterCalculationsByCategory`), unlocking previously orphaned categories `"Utility & Pumping"` (`CALC-07`), `"Boiler Safety"` (`CALC-09`), and `"Wash Plant Utility"` (`CALC-17`).
3. **`GAP-02` (Calculation History Viewer):** Wired reactive observation of `userDataRepository.getCalculationHistory()` (`CalculationDao.getCalculationHistory()`) inside existing `CalculationViewModel` and implemented a `ModalBottomSheet` (`CalculationHistorySheetContent`) accessible from both the TopAppBar `History` icon button and inline `History (N)` section trigger in `CalculationListScreen.kt`. Displays saved timestamps, formatted outputs, input summaries, engineering notes, an `EmptyStateCard` when empty, and guarded `onNavigateToRunner(...)` reopening when the target calculation route exists.

---

## 2. Exact Modified Files

| File Path | Change Summary | Gap Addressed |
| :--- | :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/Screen.kt` | Added `val shortTitle: String = title.split(" ").first()` parameter on `sealed class Screen`; assigned `"Home"`, `"Plant"`, `"Boiler"`, and `"Calcs"` to `Home`, `WashPlant`, `BoilerModule`, and `Calculations`. Preserved all `23` `Screen` objects and exact routes. | `GAP-04` |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Updated `NavigationBarItem` label (`line 243`) to `Text(text = screen.shortTitle, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)`. | `GAP-04` |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationViewModel.kt` | Added `val history: List<CalculationHistoryItem> = emptyList()` to `CalculationListUiState` and launched concurrent `observeCalculationHistory()` collecting `userDataRepository.getCalculationHistory()`. | `GAP-02` |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationListScreen.kt` | Added `extractCalculationCategories`, `filterCalculationsByCategory`, `resolveHistoryRunnerCalculationId`, `formatHistoryTimestamp`, TopAppBar & header `History` triggers, `ModalBottomSheet` viewer (`CalculationHistorySheetContent`, `CalculationHistoryEntryCard`, `EmptyStateCard`), `48.dp` minimum chip targets, and preserved all 3 `@Preview` functions. | `GAP-01`, `GAP-02` |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Updated `FakeUserDataRepository` with mutable `historyFlow` and added `3` targeted JUnit tests (`bottomNavigationScreensUseConciseExplicitShortTitles`, `dynamicCalculationCategoriesCoverAll20CalculationsWithoutOrphans`, `calculationViewModelObservesHistoryAndResolvesValidRunnerRoutes`) — bringing `Stage04UiStateAndAccessibilityTest` from `6` to `9` `@Test` methods. | `GAP-04`, `GAP-01`, `GAP-02` |
| `docs/BATCH_01_IMPLEMENTATION_REPORT.md` | Authored this Batch 01 verification & implementation report (`DOC-MPN-029`). | Documentation |
| `docs/CURRENT_WORK_CHECKPOINT.md` | Recorded Batch 01 implementation & test results (`DOC-MPN-004`). | Documentation |
| `docs/SESSION_HANDOVER.md` | Recorded Batch 01 completion and preserved Batch 02/03 hold (`DOC-MPN-006`). | Documentation |

---

## 3. Verification Results (Honest Separation: Executed vs. Blocked)

### Executed Automated Python & Static Checks (`0 FAIL`)

| Suite / Command | Outcome | Scope & Verified Details |
| :--- | :--- | :--- |
| `python3 test_suite_stage04.py` | **10/10 PASSED** | `23` `Screen` objects intact (`[S4-01]`), locked `Color.kt`/`Theme.kt` (`[S4-02]`), CalculationRunner integrity (`[S4-03]`), IME/compact responsiveness (`[S4-04]`), `>=48.dp` ergonomics (`[S4-05]`), honesty banners (`[S4-06]`), `40` `@Preview`s across `17` UI files (`[S4-07]`), `9` `@Test` methods + `8` ViewModels + offline manifest (`[S4-08]`), DB size `753,664 B` & calc SHA-256 unchanged (`[S4-09]`), `74` `.kt` files with `0` unbalanced braces/parens (`[S4-10]`). |
| `python3 test_suite_stage03.py` | **10/10 PASSED** | DB migration contract, Room schema parity, `62`-asset enrichment (`2/9/7/46`), EquipmentFilter, 13-category UnitConverter, `8` ViewModels (`[S3-06]`), Fundamentals/Catalogs, `74` `.kt` delimiter balance. |
| `python3 test_suite_stage02.py` | **8/8 PASSED** | Calculation integrity across `CALC-02, 06, 09, 11, 19, 20` and immutable DB `calculation`/`formula_variable` SHA-256. |
| `python3 test_suite_stage01.py` | **10/10 PASSED** | All `DEF-AUD-01..08` protections, Gradle 8.9 wrapper check, `CalculationResultFormatter`, steam interpolation, backup codec, and `74` `.kt` delimiter balance. |
| `python3 test_suite_phase05.py` | **10/10 PASSED** | 17-module `Screen.kt` topology, Material 3 tokens, ViewModels, Equipment/Boiler/Calculation/Notes/Settings architecture. |
| `python3 test_suite_phase06.py` | **10/10 PASSED** | All 20 calculations & IAPWS-IF97 saturated steam benchmark checks. |
| `python3 test_suite_phase07.py` | **27 PASS / 0 FAIL** (`1` BLOCKED, `2` PENDING, `2` UNVERIFIED) | Cross-file parameter checks, `21/21` linear-P steam nodes, DB & FTS4 latency, `8` `StateFlow` ViewModels. |

### Blocked Toolchain & Runtime Checks (`UNVERIFIED` / `BLOCKED` in Container)

| Check | Status | Exact Reason & Verified Output |
| :--- | :--- | :--- |
| `./android/gradlew --version` / `./gradlew testDebugUnitTest assembleDebug` | **BLOCKED** | Executed `./android/gradlew --version` -> returned non-zero with `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.` JDK 17 and Android SDK 35 are absent in this Linux container (`DOC-MPN-025`). |
| Kotlin Compiler (`kotlinc`), Compose Previews rendering, & JVM JUnit execution (`Stage04UiStateAndAccessibilityTest.kt`) | **UNVERIFIED** | JUnit tests (including the 3 new Batch 01 tests) are authored and statically syntax-balanced (`[S4-10]`), **but have not been compiled or executed on a JVM** yet. |
| Android Emulator / Physical Smartphone UI verification | **PENDING** | Live rendering of the short bottom-bar labels, dynamic filter chips, and `ModalBottomSheet` history viewer awaits Android Studio / Emulator execution outside this container. |

---

## 4. Regression Protection & Scope Deviations
- **Preserved Counts & Immutable Assets:** `23` `Screen` objects, `8` `*ViewModel.kt` files, `40` `@Preview` composables across `17` UI files, `74` `.kt` files, `mechanical_pro_notes.db` (`753,664` bytes, `2/9/7/46` depth counts, calc SHA-256 `5dfc658879fe0adcb0904ee7361c2b229fd85e3ef1cedf90568bfae1443d19bd`), `BackupJsonCodec` v2 SHA-256 contract, `Color.kt`/`Theme.kt` tokens, and `visual-preview-gallery/index.html` were all preserved cleanly.
- **Deviations from Batch 01 Scope:** **None.** Only `GAP-04`, `GAP-01`, and `GAP-02` plus their focused unit tests and tracking docs were touched. Individual history row deletion was intentionally omitted (no `CalculationDao.deleteHistoryById` exists yet, preserving strict DAO/interface non-breakage per `DOC-MPN-028`).
- **Next Hold Point:** Execution stopped after Batch 01. **Batch 02 (`GAP-03`)** and **Batch 03 (`GAP-06`, `GAP-05`)** remain held pending separate owner authorization.
