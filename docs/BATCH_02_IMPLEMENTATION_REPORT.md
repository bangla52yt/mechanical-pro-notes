# BATCH 02 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-030  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 02 Only (`GAP-03` — Complete Bookmark Integration across `CALCULATION`, `CHECKLIST`, `TECHNICAL_STANDARD`, and `BOILER_SECTION` + `BOILER_SECTION` navigation routing fix)  
**Execution Date:** 2026-11-15  
**Status:** IMPLEMENTED IN SOURCE & VERIFIED BY STATIC / PYTHON REGRESSION SUITES (`0 FAIL`) — NATIVE KOTLIN COMPILE & JVM JUNIT EXECUTION REMAIN **BLOCKED** IN CONTAINER (JDK 17 / ANDROID SDK 35 ABSENT)

---

## 1. Executive Summary
Batch 02 completed **`GAP-03`** across the `9` authorized Kotlin presentation/navigation files and `1` authorized JUnit test file without altering any Room schemas, DAOs, repository interfaces, migrations, `BackupJsonCodec` v2 contracts, calculation formulas, `EquipmentViewModel.kt`, locked theme tokens (`Color.kt`/`Theme.kt`), ViewModel count (`8`), Screen route count (`23`), or `visual-preview-gallery/index.html`:

1. **`CALCULATION` Bookmarks (`CalculationViewModel.kt`, `CalculationRunnerScreen.kt`):** Added reactive collection of `userDataRepository.getAllBookmarks()` filtered to `BookmarkEntityType.CALCULATION`, exposed `isBookmarked` on `CalculationRunnerUiState` (kept synced across runner loads and external bookmark removals), added duplicate-guarded `toggleBookmark()`, and added a contextual `>= 48.dp` bookmark `IconButton` in `CalculationRunnerScreen`'s `TopAppBar`.
2. **`CHECKLIST` Bookmarks (`ChecklistViewModel.kt`, `ChecklistScreen.kt`):** Added reactive `bookmarkedChecklistIds` and `isSelectedChecklistBookmarked` on `ChecklistUiState` (updating cleanly on `selectChecklist(...)` and bookmark stream changes), added duplicate-guarded `toggleChecklistBookmark(...)`, and placed a `>= 48.dp` bookmark `IconButton` beside the existing Reset action in `ChecklistScreen`'s `TopAppBar`.
3. **`TECHNICAL_STANDARD` Bookmarks (`NotesViewModel.kt`, `ReferenceLibraryScreen.kt`, `AppNavigation.kt`):** Added duplicate-guarded generic `toggleBookmark(entityType, entityId, title)` on `NotesViewModel` reusing its existing reactive `bookmarks` flow, added defaulted `bookmarkedRefIds` and `onToggleBookmark` parameters on `ReferenceLibraryScreen` with a `>= 48.dp` bookmark button on every `REF-01..12` card header, and wired them through `AppNavigation`.
4. **`BOILER_SECTION` Bookmarks & Routing Fix (`BoilerViewModel.kt`, `BoilerDashboardScreen.kt`, `AppNavigation.kt`):** Preserved `BoilerViewModel`'s constructor, added pure deterministic helpers (`BoilerBookmarkTarget`, `boilerSectionBookmarkId(tab)`, `resolveBoilerBookmarkTarget(entityId)`), added a `>= 48.dp` `TopAppBar` bookmark button for the active `BoilerTab` wired via `NotesViewModel`, and fixed `AppNavigation.kt`'s `BOILER_SECTION` dispatcher so canonical `"BOILER:<TAB>"` and bare `BoilerTab` IDs route to `BoilerModule` with `selectTab(...)` + `launchSingleTop = true`, explicit legacy `"EQ-..."` IDs route to `EquipmentDetail`, and unknown/malformed IDs fall back safely to `BoilerTab.OVERVIEW` without misrouting to `EquipmentDetail`.

---

## 2. Exact Modified & Created Files

### Authorized Source Files Modified (`9`)
| File Path | Summary of Change |
| :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerViewModel.kt` | Added `BoilerBookmarkTarget`, `boilerSectionBookmarkId(tab)`, and deterministic `resolveBoilerBookmarkTarget(entityId)` supporting canonical `"BOILER:<TAB>"`, case-insensitive/bare `BoilerTab` names, explicit legacy `"EQ-*"`, and safe `OVERVIEW` fallback. Constructor unchanged. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerDashboardScreen.kt` | Added defaulted `bookmarkedSectionIds` & `onToggleSectionBookmark` parameters to `BoilerDashboardScreen` and `BoilerDashboardContent` (preserving `@Preview`s) plus `>= 48.dp` `TopAppBar` bookmark `IconButton`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/notes/NotesViewModel.kt` | Added in-flight duplicate-tap guard (`inFlightBookmarkKeys`) and generic `toggleBookmark(entityType, entityId, title)` reusing `userDataRepository.toggleBookmark(...)` and reactive `bookmarks` Flow. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/library/ReferenceLibraryScreen.kt` | Added defaulted `bookmarkedRefIds` & `onToggleBookmark` parameters (preserving `@Preview`s) and rendered a `>= 48.dp` bookmark `IconButton` in each `REF-01..12` card header. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationViewModel.kt` | Exposed `isBookmarked` on `CalculationRunnerUiState`, launched `observeCalculationBookmarks()` on `getAllBookmarks()`, synced `loadCalculationRunner(...)`, and added duplicate-guarded `toggleBookmark()`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationRunnerScreen.kt` | Added defaulted `onToggleBookmark: () -> Unit = {}` parameter (preserving `@Preview`s) and contextual `>= 48.dp` `TopAppBar` bookmark `IconButton`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/checklists/ChecklistViewModel.kt` | Exposed `bookmarkedChecklistIds` & `isSelectedChecklistBookmarked` on `ChecklistUiState`, observed `getAllBookmarks()`, updated bookmark sync in `selectChecklist(...)`, and added duplicate-guarded `toggleChecklistBookmark(...)`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/checklists/ChecklistScreen.kt` | Added defaulted `onToggleBookmark: (String) -> Unit = {}` parameter (preserving `@Preview`s) and `>= 48.dp` bookmark `IconButton` beside the Reset button in `TopAppBar`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Derived `bookmarkedBoilerSectionIds` and `bookmarkedReferenceIds` from `notesViewModel.uiState`, wired `BoilerDashboardScreen` & `ReferenceLibraryScreen`, and fixed `BookmarkEntityType.BOILER_SECTION` dispatch in `BookmarksScreen` preserving `launchSingleTop = true`. |

### Authorized Test File Modified (`1`)
| File Path | Summary of Change |
| :--- | :--- |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Upgraded `FakeUserDataRepository` to reactive `MutableStateFlow<List<UserBookmark>>` persistence and added `5` Batch 02 `@Test` methods (`14` total in file) covering all `9` required regression scenarios (`Calculation` toggle/sync, `Checklist` toggle/switch sync, `NotesViewModel` `TECHNICAL_STANDARD` & `BOILER_SECTION` toggles + duplicate prevention, and `resolveBoilerBookmarkTarget` canonical, case-insensitive, bare tab, legacy `EQ-G01`, and malformed fallback cases). |

### Documentation Files Created / Updated (`3`)
- Created `docs/BATCH_02_IMPLEMENTATION_REPORT.md` (`DOC-MPN-030`)
- Updated `docs/CURRENT_WORK_CHECKPOINT.md` (`DOC-MPN-004`)
- Updated `docs/SESSION_HANDOVER.md` (`DOC-MPN-006`)

---

## 3. Verification Results (Strict Separation)

### Executed and Passed (`0` Failures)
| Check / Suite | Result | Verified Scope |
| :--- | :--- | :--- |
| `python3 test_suite_stage04.py` | **10/10 PASSED** | `23` `Screen` objects (`[S4-01]`), locked `Color.kt`/`Theme.kt` (`[S4-02]`), CalculationRunner integrity (`[S4-03]`), `.imePadding()`/compact layout (`[S4-04]`), `>=48.dp` ergonomics (`[S4-05]`), honesty banners (`[S4-06]`), `40` `@Preview`s across `17` UI files (`[S4-07]`), **`14` `@Test` methods + `8` ViewModels + offline manifest** (`[S4-08]`), DB size `753,664 B` & calc SHA-256 (`[S4-09]`), `74` `.kt` files with `0` unbalanced delimiters (`[S4-10]`). |
| `python3 test_suite_stage03.py` | **10/10 PASSED** | Migrations & user-table isolation (`[S3-01]`), Room schema parity (`[S3-02]`), `62`-asset enrichment (`[S3-03]`), filters (`[S3-04]`), UnitConverter (`[S3-05]`), `8` ViewModels (`[S3-06]`), catalogs/Fundamentals (`[S3-07]`–`[S3-08]`), JUnit coverage (`[S3-09]`), `74` `.kt` syntax balance (`[S3-10]`). |
| `python3 test_suite_stage02.py` | **8/8 PASSED** | Calculation integrity across `CALC-02, 06, 09, 11, 19, 20` and immutable DB calc SHA-256. |
| `python3 test_suite_stage01.py` | **10/10 PASSED** | All `DEF-AUD-01..08` protections, Gradle 8.9 wrapper check, formatter, steam interpolation, backup codec, and `74` `.kt` delimiter balance. |
| `python3 test_suite_phase05.py` | **10/10 PASSED** | Presentation structure, offline manifest, `17`-module navigation, Material 3 tokens, DB/FTS5/checklists/JSON backup. |
| `python3 test_suite_phase06.py` | **10/10 PASSED** | All `20` calculations & IAPWS-IF97 saturated steam benchmark checks. |
| Targeted Static Checks (Python) | **PASS** | Verified `Stage04 @Tests=14`, `kt_files=74`, `vm_files=8`, `@Previews=40`; old combined `EQUIPMENT, BOILER_SECTION` branch removed; `resolveBoilerBookmarkTarget` + `launchSingleTop = true` present; SHA-256 hashes unchanged for `EquipmentViewModel.kt`, `BackupJsonCodec.kt`, `Color.kt`, `Theme.kt`, and `visual-preview-gallery/index.html`. |

### Executed and Failed
- **None in Python/static checks (`0` failures).**

### Blocked
- `./android/gradlew --version`: Exited non-zero with `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.` JDK 17 and Android SDK 35 are absent in the container.

### Unverified
- Native Kotlin compilation (`kotlinc`), JVM JUnit execution (`./gradlew testDebugUnitTest` including the `5` newly authored Batch 02 `@Test` methods), APK assembly (`assembleDebug`), Compose `@Preview` rendering, and live Android Emulator / physical device interactive UI testing.

---

## 4. Deviations, Protected Invariants & Remaining Limitations
- **Deviations from Batch 02 Scope:** **None.** Excluded `EquipmentViewModel.kt`, database schema/DAOs/repositories/migrations, `BackupJsonCodec`, `Color.kt`, `Theme.kt`, and `visual-preview-gallery/` cleanly.
- **Remaining Limitations:** Reactive icon toggles, `TopAppBar` layout fit across compact `320dp–360dp` widths, `BookmarksScreen` → `BoilerModule` tab selection navigation, and JUnit execution await native JDK 17 + Android Studio / SDK 35 verification outside this container.
- **Hold Status:** Stopped cleanly after Batch 02. **Batch 03 (`GAP-06`, `GAP-05`)** remains held pending separate owner authorization.
