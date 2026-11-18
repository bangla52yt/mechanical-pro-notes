# BATCH 05 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-033  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 05 (`GAP-07` Search & Discovery Integration: Calculation Search, Personal User Note Search, Calculation History Search, Search vs. Bookmarks Navigation Separation, Accessible Scoped Search UI & Focused Unit Tests)  
**Documentation Date:** 2026-11-16  
**Status:** BATCH 05 FEATURES & UNIT TESTS IMPLEMENTED IN SOURCE (`test_suite_stage01..04.py` & `test_suite_phase05.py`: ALL PASS STATIC) — NATIVE KOTLIN COMPILE, JVM JUNIT EXECUTION, COMPOSE RENDERING & APK BUILD REMAIN **UNVERIFIED / BLOCKED** IN CONTAINER

---

## 1. Executive Summary
Batch 05 implemented **`GAP-07`** across **12 Kotlin source and test files**, preserving the offline-first Room architecture, Room database schema/migrations, pre-packaged SQLite data (`753,664` bytes), calculation formulas (`20` modules), backup JSON format, zero internet permission (`DEC-016`), and strict static isolation invariants (`[S3-08]` / `[S4-08]`):

1. **Objective 1 — Search Calculations (`RepositoryInterfaces.kt`, `SearchRepositoryImpl.kt`, `AppNavigation.kt`):**
   - Extended `UnifiedSearchResult` with `calculations: List<CalculationSearchHit>` (defaulted to `emptyList()` to preserve source compatibility).
   - Added `searchCalculations(...)` in `SearchRepositoryImpl` reading `CalculationDao` entities merged with `CalculationSpecifications.ALL_CALCULATIONS`, matching `id`, `code`, `title`, `bengaliTitle`, `category`, `engineeringReference`, `assumptions`, `limitations`, `formulaLatex`, `status`, and variable metadata (`symbol`, `name`, `unit`). Ranked exact ID/code matches first, then title/category matches, then deep metadata/variable matches.
   - Tapping a calculation result routes cleanly to `Screen.CalculationRunner.createRoute(calcId)`.
2. **Objective 2 — Search User Notes (`RepositoryInterfaces.kt`, `SearchRepositoryImpl.kt`, `NotesViewModel.kt`, `AppNavigation.kt`):**
   - Extended `UnifiedSearchResult` with `userNotes: List<UserNoteSearchHit>`.
   - Added `searchPersonalNotes(...)` reading `UserNoteDao.getAllNotes()` directly from `UserNoteEntity` without invoking `UserNote.init` `require(...)` constraints—safely handling empty titles (`"Field Note ($safeEq)"` / `"Untitled Field Note"`) and empty note bodies (`"No note observation text"`) without crashing—matching `title`, `body`, `equipmentId`, and `tagsCsv`, ordered by newest `updatedAt` while remaining strictly isolated from static equipment/catalog results (`EngineeringCatalogSearch`).
   - Added `NotesViewModel.openNoteById(noteId, fallbackEquipmentId)` (handling both loaded and loading-in-progress emissions via `pendingNoteIdToOpen`) so tapping a searched note opens `Screen.Notes` directly into that exact note in the editor.
3. **Objective 3 — Search Calculation History (`RepositoryInterfaces.kt`, `SearchRepositoryImpl.kt`, `CalculationViewModel.kt`, `CalculationListScreen.kt`, `AppNavigation.kt`):**
   - Extended `UnifiedSearchResult` with `calculationHistory: List<CalculationHistorySearchHit>`.
   - Added `searchCalculationHistory(...)` parsing `outputsJson` and `inputsJson` via `BackupJsonCodec` and formatting outputs/inputs with `CalculationResultFormatter` + timestamp (`dd MMM yyyy, HH:mm`, `Locale.US`), matching `calculationId`, `calculationTitle`, formatted timestamp, formatted result summaries, raw output tokens, inputs summary, and `userRemark`. Multiple runs of the same calculation remain cleanly distinguishable via timestamp, result summary, inputs summary, and remark cards.
   - Added `CalculationViewModel.focusHistoryRecord(historyId)` / `clearPendingHistoryFocus()` and wired `CalculationListScreen.kt` with a `LaunchedEffect`, `rememberLazyListState()` auto-scroll inside `CalculationHistorySheetContent`, and primary highlight styling on the exact focused `CalculationHistoryEntryCard`.
4. **Objective 4 — Separate Search and Bookmarks Navigation (`Screen.kt`, `SearchScreen.kt`, `AppNavigation.kt`):**
   - Renamed `Screen.Search` from `"Search & Bookmarks"` to `"Offline Search"` (`shortTitle = "Search"`, keeping route `"search"` and all `23` `Screen` objects intact without duplicate bottom-bar or drawer routes).
   - Added a `>= 48.dp` Bookmarks action `IconButton` (`Icons.Default.Bookmark`, `contentDescription = "Open Saved Bookmarks"`) in `SearchScreen`'s `TopAppBar` wired to `Screen.Bookmarks`.
5. **Objective 5 — Scoped & Accessible Search UI (`SearchViewModel.kt`, `SearchScreen.kt`):**
   - Expanded `SearchScope` (`ALL`, `EQUIPMENT`, `CALCULATIONS`, `NOTES`, `HISTORY`, `CHECKLISTS`, `REFERENCES`, `FUNDAMENTALS`).
   - Normalized debounce flow in `SearchViewModel` (`.map { it.trim() }.debounce(250).distinctUntilChanged()`) to avoid duplicate searches on trailing spaces or repeated queries, and cleared all hits + `hasSearched = false` for queries `< 2` characters.
   - Added distinct section headings (`heading()` semantics) and `>= 48.dp` `Role.Button` cards (`CalculationSearchHitCard`, `UserNoteSearchHitCard`, `CalculationHistorySearchHitCard`) with `remember(...)` filtered list derivations, unique composite `LazyColumn` keys, updated empty-query/no-results copy, and refreshed `SearchPreviewData.sampleResults`.
6. **Objective 6 — Focused JUnit Tests (`UnifiedSearchAndCatalogTest.kt`, `Stage04UiStateAndAccessibilityTest.kt`):**
   - Added `3` repository-level search tests in `UnifiedSearchAndCatalogTest.kt` and `4` ViewModel/UI-state/navigation separation tests in `Stage04UiStateAndAccessibilityTest.kt` (`7` new `@Test` methods total).

---

## 2. Exact Source, Test & Documentation Files Created or Modified (`12` Code/Test Files + Docs)

| File Path | Rationale & Implemented Change |
| :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/repository/RepositoryInterfaces.kt` | Add `CalculationSearchHit`, `UserNoteSearchHit`, `CalculationHistorySearchHit`, and extend `UnifiedSearchResult` cleanly with defaults. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/SearchRepositoryImpl.kt` | Add defaultable `calculationDao` & `userNoteDao` dependencies; implement ranked `searchCalculations`, safe `searchPersonalNotes`, and formatted `searchCalculationHistory` while preserving `[S3-08]` static isolation checks. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/di/AppContainer.kt` | Inject `database.calculationDao()` and `database.userNoteDao()` into `SearchRepositoryImpl`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/Screen.kt` | Rename `Screen.Search` title from `"Search & Bookmarks"` to `"Offline Search"` (`shortTitle = "Search"`) while preserving route `"search"` and all `23` `Screen` objects. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/notes/NotesViewModel.kt` | Add `pendingNoteIdToOpen` handling and `openNoteById(...)` to navigate cleanly to an exact searched personal note in the editor. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationViewModel.kt` | Add `pendingHistoryFocusId` state plus `focusHistoryRecord(...)` and `clearPendingHistoryFocus()`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationListScreen.kt` | Auto-open `CalculationHistorySheetContent`, auto-scroll via `rememberLazyListState()`, and visually highlight focused calculation history entries. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/search/SearchViewModel.kt` | Expand `SearchScope`, track calculation/note/history hits and filtered accessors, debounce trimmed distinct queries, and clear state when `< 2` chars. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/search/SearchScreen.kt` | Add `TopAppBar` Bookmarks button (`48.dp`), `remember` filtered lists, `CALCULATIONS`/`NOTES`/`HISTORY` filter chips & cards with `heading()` / `Role.Button` / `48.dp` targets, and empty/no-results states. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Wire `SearchScreen` callbacks to `CalculationRunner`, `NotesViewModel.openNoteById` + `Screen.Notes`, `CalculationViewModel.focusHistoryRecord` + `Screen.Calculations`, and `Screen.Bookmarks`. |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/UnifiedSearchAndCatalogTest.kt` | Add `FakeCalculationDao`, `FakeUserNoteDao`, and `3` `@Test` methods verifying calculation search/ranking, empty-title/body note safety & static isolation, and history multi-run search/formatting. |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Add `4` `@Test` methods covering `SearchScope` filtering, `SearchViewModel` debounce/dedup/short-query/no-results, `NotesViewModel.openNoteById` / `CalculationViewModel.focusHistoryRecord`, and Search vs. Bookmarks route/title separation. |

*Documentation created/updated in Batch 05:* `docs/BATCH_05_IMPLEMENTATION_REPORT.md` (`DOC-MPN-033`), plus synchronized checkpoint metadata in `docs/CURRENT_WORK_CHECKPOINT.md`, `docs/SESSION_HANDOVER.md`, and `docs/PHASE_PROGRESS_TRACKER.md`. Zero Room schema/migration changes, pre-packaged SQLite database modifications (`753,664` bytes preserved), calculation formula edits, backup codec changes, equipment/boiler/steam edits, Git commits/pushes, or deployments were performed.

---

## 3. Authored Tests vs. Executed Verification (Strict Separation)

### Authored JVM JUnit Tests (NOT Executed on JVM)
- **Batch 05 Additions (`7` `@Test` methods):**
  - *`UnifiedSearchAndCatalogTest.kt` (`3` new `@Test`s; now `6` in file):*
    1. `searchRepositoryMatchesCalculationsAcrossIdsCategoriesVariablesAndBengaliTitlesWithRanking`
    2. `searchRepositoryMatchesUserNotesSafelyIncludingEmptyTitleAndBodyWithoutPollutingStaticKnowledge`
    3. `searchRepositoryMatchesCalculationHistoryByTitleOutputsTimestampAndRemarkToDistinguishRuns`
  - *`Stage04UiStateAndAccessibilityTest.kt` (`4` new `@Test`s; now `25` in file):*
    1. `searchUiStateFiltersCalculationsNotesHistoryAndCatalogScopesCorrectly`
    2. `searchViewModelDeduplicatesDebouncedQueriesClearsShortQueriesAndHandlesNoResults`
    3. `notesViewModelOpenNoteByIdAndCalculationViewModelFocusHistoryRecordResolveExactTargets`
    4. `searchAndBookmarksScreensAreCleanlySeparatedWithoutDuplicateBottomBarRoutes`
- **Total Authored JUnit Inventory:** **`91` `@Test` methods across `12` test files** in `android/app/src/test/`. **Native JVM JUnit execution (`./gradlew testDebugUnitTest`) was NOT executed** because JDK 17 and Android SDK 35 are absent in the container environment.

### Static / Python Verification Executed
- **`python3 test_suite_stage01.py` -> `10/10 PASSED` (`exit 0`)**
- **`python3 test_suite_stage02.py` -> `8/8 PASSED` (`exit 0`)**
- **`python3 test_suite_stage03.py` -> `10/10 PASSED` (`exit 0`)**: Confirmed zero static-search violations (`[S3-08]`), preserved `8` ViewModels (`[S3-06]`), `23` `Screen` objects, and DB integrity.
- **`python3 test_suite_stage04.py` -> `10/10 PASSED` (`exit 0`)**: Confirmed `23` `Screen` routes, `40` `@Preview` annotations across `17` UI files, `25` Stage 04 `@Test` methods, strict `8` ViewModel count, preserved SQLite DB byte size (`753,664` B) & calculation SHA-256, and balanced Kotlin braces/parens across all `74` `.kt` files.
- **`python3 test_suite_phase05.py` -> `10/10 PASSED` (`exit 0`)**
- **Scope Disclaimer:** Python static checks inspect source structure, annotations, invariants, and pre-packaged SQLite integrity only; they do **not** establish Kotlin compilation, Compose layout rendering, Room query compilation, APK packaging, or Android runtime behavior.

---

## 4. Remaining Limitations & Carried-Forward Gaps
- **Unverified Outside Container (JDK 17 + Android SDK 35 Required):** Kotlin compilation, KSP Room schema generation, JVM unit test execution (`./gradlew testDebugUnitTest`), Compose `@Preview` inspection, `./gradlew assembleDebug`, Android Emulator run, NIST REFPROP comparison (`IapwsIf97SteamEngine`), and physical smartphone testing remain unverified.
- **Unmodified Open Scope Gaps:** `GAP-08` through `GAP-11`, secondary `GAP-06` enhancements (checklist-to-equipment deep link, operator/shift metadata, custom checklist authoring), `GAP-12` (`CALC-02` `RESEARCH_PENDING`, `FUND-11..15` `CONTENT_GAP`), `GAP-13` (`62/64` non-full equipment records, `5/64` schematics), `REF-06` RPM limit reconciliation (`15,000` vs `30,000 RPM`), and statutory legal verification remain outside Batch 05 scope.
