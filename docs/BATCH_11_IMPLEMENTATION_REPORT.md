# BATCH 11 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-039  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 11 (`GAP-05` Home Screen Bookmark Completion & Synchronization across Batch 02/04 Integrations)  
**Documentation Date:** 2026-11-17  
**Status:** BATCH 11 (`GAP-05`) FEATURES & UNIT TESTS IMPLEMENTED IN SOURCE (`test_suite_stage04.py`: `10/10 PASS` STATIC) — NATIVE KOTLIN COMPILE, JVM JUNIT EXECUTION, COMPOSE RENDERING & APK BUILD REMAIN **UNVERIFIED / BLOCKED** IN CONTAINER

---

## 1. Executive Summary
Batch 11 completed the remaining explicitly documented **GAP-05 (Home Screen Bookmark Integration)** requirements building directly on Batch 02 (`DOC-MPN-030` `GAP-03` core bookmark stores & routes) and Batch 04 (`DOC-MPN-032` initial Home bookmarks strip), while preserving the offline-first policy (`DEC-016`), Room v1 schema (`16` entities) & migrations (`ALL_MIGRATIONS.isEmpty()`), pre-packaged SQLite database (`753,664` bytes), `BackupJsonCodec.kt` (`backup_version = 2`), all `20` calculation formulas, `8` `*ViewModel.kt` count (`[S3-06]` / `[S4-08]`), `23` `Screen` objects (`[S4-01]`), and `40` `@Preview` composables (`[S4-07]`):

1. **Reactive Home Bookmark State, Removal & Featured Toggle (`HomeViewModel.kt`, `NotesViewModel.kt`, `CommonComponents.kt`, `HomeScreen.kt`):**
   - Extended `HomeUiState` with backward-compatible defaulted fields `bookmarkedEquipmentIds: Set<String> = emptySet()` and `activitySummary: HomeActivitySummary = HomeActivitySummary()`, computed reactively by combining `equipmentRepository`, `boilerRepository`, and `UserDataRepository` flows (`getAllBookmarks()`, `getAllUserNotes()`, `getCalculationHistory()`, `getAllChecklistProgress()`, `getCustomChecklists()`).
   - Added concurrency-guarded `toggleEquipmentBookmark(equipment)` and safe `removeBookmark(bookmark)` on `HomeViewModel` (and upgraded `NotesViewModel.removeBookmark` identically) using `userDataRepository.isBookmarked(...)` lookup across stored/trimmed entity IDs so legacy untrimmed entries delete cleanly and stale duplicate unpin taps never accidentally re-insert removed bookmarks.
   - Added optional defaulted `isBookmarked: Boolean? = null` and `onToggleBookmark: (() -> Unit)? = null` parameters on `EquipmentCardItem` in `CommonComponents.kt` (`>= 48.dp` accessible bookmark toggle button beside `ContentDepthBadge`) and wired Featured Blueprint Equipment cards on `HomeScreen` to reflect and toggle live bookmark status.
   - Added a dedicated `>= 48.dp` `BookmarkRemove` `IconButton` on `HomePinnedBookmarkCard` while scoping `semantics(mergeDescendants = true)` to the non-button header row so TalkBack announces card navigation and bookmark removal independently.
2. **Home `TopAppBar` Bookmarks Shortcut & Deep-Link Synchronization (`HomeScreen.kt`, `AppNavigation.kt`, `BoilerViewModel.kt`):**
   - Added the `48.dp` Bookmarks shortcut icon button beside Search in `HomeScreen` `TopAppBar.actions` matching `PHASE_03 §2` (`DOC-MPN-015`) and `SearchScreen`.
   - Hardened `navigateToBookmarkTarget` in `AppNavigation.kt` to guard blank entity IDs, normalize `EQUIPMENT` (`EQ-*`) and `CALCULATION` (`CALC-*`) route IDs, clear stale equipment filters + use `launchSingleTop = true` for `CHECKLIST` deep links (supporting both packaged `CHK-*` and Batch 10 `CHK-CUSTOM-*` templates), and resolve `REF-*` targets case-insensitively against `TechnicalReferenceCatalog.ALL_REFERENCES` inside `resolveTechnicalStandardBookmarkTarget`.
   - Updated `resolveBoilerBookmarkTarget` (`BoilerViewModel.kt`) to uppercase legacy lowercase `EQ-*` boiler equipment bookmark IDs cleanly.
   - Fixed a pre-existing preview compile omission in `HomeScreenDarkPreview` (`onNavigateToEquipment = {}`).
3. **Home Recent Activity & Checklist Status Summary (`HomeViewModel.kt`, `HomeScreen.kt`, `AppNavigation.kt`):**
   - Addressed the remaining `DOC-MPN-027` (`GAP-05`) item (`"and recent activity/overdue checks"`) via `HomeRecentActivityCard`, showing live checklist completed/pending counts across packaged and Batch 10 custom checklists with 1-tap navigation to the active/pending checklist (`onNavigateToActiveChecklist`), latest saved field note (`onNavigateToNotes`), and latest saved calculation run (`onNavigateToCalculation`), or an honest zero-activity message when no checks, notes, or calculation history exist yet.

---

## 2. Exact Source, Test & Documentation Files Modified (`7` Kotlin Files + Docs)

| File Path | Action | Rationale & Implemented Change |
| :--- | :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/home/HomeViewModel.kt` | Modified | Added `HomeActivitySummary`, defaulted `bookmarkedEquipmentIds` & `activitySummary` on `HomeUiState`, combined offline activity & bookmark streams, and added concurrency-guarded `toggleEquipmentBookmark` and safe `removeBookmark`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/notes/NotesViewModel.kt` | Modified | Hardened `removeBookmark` with safe `isBookmarked` verification across stored/trimmed/case variants so `BookmarksScreen` deletion never re-inserts removed bookmarks. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/components/CommonComponents.kt` | Modified | Added defaulted `isBookmarked` & `onToggleBookmark` (`>= 48.dp` toggle `IconButton`) to `EquipmentCardItem`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerViewModel.kt` | Modified | Normalized `BoilerBookmarkTarget.Equipment` IDs with `.uppercase()` in `resolveBoilerBookmarkTarget`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/home/HomeScreen.kt` | Modified | Added `48.dp` `TopAppBar` Bookmarks button, `HomeRecentActivityCard`, `HomePinnedBookmarkCard` separate `48.dp` remove button, Featured Equipment live bookmark toggles, `HomePreviewData` sample activity/bookmark IDs, and fixed `HomeScreenDarkPreview` parameter omission. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Modified | Imported `TechnicalReferenceCatalog`, normalized `REF-*` in `resolveTechnicalStandardBookmarkTarget`, hardened `navigateToBookmarkTarget` (blank guard, `EQUIPMENT`/`CALCULATION` ID casing, `CHECKLIST` filter reset + `launchSingleTop`), and wired Home callbacks. |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Modified | Fixed `HomeUiState.featuredEquipment` property reference at line 1692 (`featuredBoilers` -> `featuredEquipment`) and added `3` Batch 11 GAP-05 `@Test` methods covering featured toggle/safe remove/cross-ViewModel sync, `HomeActivitySummary` & custom checklist reactivity, and `REF-*`/`FUND-*`/`BOILER` resolver normalization. |

*Documentation created/updated in Batch 11:* `docs/BATCH_11_IMPLEMENTATION_REPORT.md` (`DOC-MPN-039`), plus synchronized checkpoint metadata in `docs/CURRENT_WORK_CHECKPOINT.md` (`DOC-MPN-004`), `docs/SESSION_HANDOVER.md` (`DOC-MPN-006`), and `docs/PHASE_PROGRESS_TRACKER.md` (`DOC-MPN-003`). Zero Room schema/migration changes, Backup JSON codec edits, pre-packaged SQLite DB edits (`753,664` bytes preserved), calculation formula edits, or Git commits/pushes were performed.

---

## 3. Authored Tests vs. Executed Verification (Strict Separation)

### Authored JVM JUnit Tests (NOT Executed on JVM)
- **Batch 11 Additions (`3` `@Test` methods in `Stage04UiStateAndAccessibilityTest.kt`; now `48` in file):**
  1. `homeViewModelTogglesFeaturedEquipmentBookmarkRemovesBookmarksSafelyAndSyncsWithOtherViewModels` — verifies legacy padded ID normalization in `bookmarkedEquipmentIds`, bidirectional sync between `HomeViewModel`, `EquipmentViewModel`, and `NotesViewModel`, safe legacy removal, and idempotent stale `removeBookmark` calls never re-inserting deleted bookmarks.
  2. `homeViewModelComputesRecentActivityAndChecklistStatusFromOfflineData` — verifies `HomeActivitySummary` across packaged & custom checklists, notes, calculation history, and honest zero-activity state after `clearAllPersonalData()`.
  3. `bookmarkNavigationResolversNormalizeCasingForAllStandardBoilerAndReferenceTargets` — verifies case/whitespace normalization in `resolveTechnicalStandardBookmarkTarget` (`ref-01` -> `REF-01`, `fund-02` -> `FUND-02`) and `resolveBoilerBookmarkTarget` (`eq-g01` -> `EQ-G01`, `boiler:safety_law` -> `BoilerTab.SAFETY_LAW`).
- **Total Authored JUnit Inventory:** **`116` `@Test` methods across `12` test files** in `android/app/src/test/`. **Native JVM JUnit execution (`./gradlew testDebugUnitTest`) was NOT executed** because JDK 17 and Android SDK 35 are absent in the container environment.

### Minimal Static Sanity Check Executed (Post-Batch 11)
- **`python3 test_suite_stage04.py` -> `10/10 PASSED` (`exit 0`)**: Verified `23` `Screen` routes (`[S4-01]`), contrast & theme tokens (`[S4-02]`), `.imePadding()` & responsive layouts (`[S4-04]`), `>=48.dp` targets & accessibility semantics (`[S4-05]`), `40` `@Preview` functions (`[S4-07]`), `48` Stage 04 `@Test`s & `8` `*ViewModel.kt` count (`[S4-08]`), unchanged `753,664`-byte DB & calc SHA-256 (`[S4-09]`), and balanced syntax across `76` `.kt` files (`[S4-10]`).
- **Scope Disclaimer:** Static Python checks inspect source structure, delimiter balance, and pre-packaged SQLite integrity only; they do **not** verify Kotlin compilation, Compose rendering, Room/DataStore runtime behavior, APK packaging, or physical-device execution.

---

## 4. Open Items & Preserved Risks
- **Remaining Functional / Content Gaps:** `GAP-12` (`CALC-02` `RESEARCH_PENDING`, `FUND-11..15` `CONTENT_GAP`) and `GAP-13` (`62/64` non-full equipment depth, `5/64` schematics). (`GAP-01`–`GAP-11`, including full `GAP-05`, are now implemented in source; native runtime remains unverified.)
- **Preserved Engineering / Legal Items:** `REF-06` ISO 20816-3 RPM conflict (`15,000` vs `30,000 RPM`), independent legal verification of Bangladesh Boilers Act, 2022 & Rules, 2025, and external NIST REFPROP comparison for `IapwsIf97SteamEngine`.
- **Native Verification Pending Outside Container:** Open `android/` in Android Studio (JDK 17 + Android SDK 35), inspect `@Preview` composables, run `./gradlew testDebugUnitTest assembleDebug` (`116` authored JUnit tests), and verify on an API 34/35 Android Emulator and physical device before any owner Git push.
