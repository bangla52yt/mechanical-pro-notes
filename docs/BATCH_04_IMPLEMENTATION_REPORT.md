# BATCH 04 IMPLEMENTATION REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-032  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authorized Scope:** Batch 04 (`GAP-05` Home Bookmark Integration & Shortcuts + `GAP-03` Reactive `EquipmentViewModel` Bookmarks & `FundamentalsScreen` Bookmark Support)  
**Documentation Date:** 2026-11-16  
**Status:** BATCH 04 FEATURES & UNIT TESTS IMPLEMENTED IN SOURCE (`test_suite_stage04.py`: `10/10 PASS` STATIC) — NATIVE KOTLIN COMPILE, JVM JUNIT EXECUTION, COMPOSE RENDERING & APK BUILD REMAIN **UNVERIFIED / BLOCKED** IN CONTAINER

---

## 1. Executive Summary
Batch 04 implemented **`GAP-05`** and the remaining two **`GAP-03`** bookmark gaps across **6 Kotlin source/test files**, preserving existing Room database schemas, migrations, pre-packaged SQLite data (`753,664` bytes), calculation logic (`20` modules), backup JSON format, and Material 3 palette tokens:

1. **`GAP-05` — Home Screen Bookmark Integration & Quick Shortcuts (`HomeViewModel.kt`, `HomeScreen.kt`, `AppNavigation.kt`):**
   - Deduplicated collected bookmarks inside `HomeViewModel` (`.filter { it.entityId.isNotBlank() }.distinctBy { it.entityType to it.entityId.trim().uppercase() }`) and added defensive deduplication + composite keys in `HomeScreen`.
   - Added a **Saved Bookmarks** section to `HomeScreenContent` featuring a `View All` link, compact `LazyRow` cards (`HomePinnedBookmarkCard` with minimum `48.dp` touch target height, merged semantics, `Role.Button`, and entity-type badges: `EQUIP`, `BOILER`, `CALC`, `CHECKLIST`, `FUNDAMENTAL`, `REFERENCE`), and a clean empty-state card (`"No bookmarks saved yet"`).
   - Added 4 quick-navigation `OutlinedButton` shortcuts (`Checklists`, `Notes`, `Bookmarks (N)`, `Steam Table`) wired to existing routes (`Screen.Checklists`, `Screen.Notes.createRoute(null)`, `Screen.Bookmarks`, `Screen.SteamTable`).
   - Extracted shared `navigateToBookmarkTarget` dispatch in `AppNavigation.kt` used identically by `HomeScreen` and `BookmarksScreen`.
2. **`GAP-03` — Reactive Equipment Bookmark State (`EquipmentViewModel.kt`):**
   - Replaced the one-shot `userDataRepository.isBookmarked(equip.id)` lookup with a single `init` collector on `userDataRepository.getAllBookmarks()` mapped to normalized, deduplicated uppercase `EQUIPMENT` IDs (`bookmarkedEquipmentIds` `MutableStateFlow<Set<String>>`, `.distinctUntilChanged()`).
   - Combined `userDataRepository.getNotesForEquipment(equip.id)` with `bookmarkedEquipmentIds.map { ids -> equipKey in ids }.distinctUntilChanged()` inside `loadEquipmentDetail` (`detailJob?.cancel()` prevents duplicate collectors), plus an `inFlightBookmarkIds` double-tap guard in `toggleBookmark(...)`. Removing or adding an equipment bookmark from `BookmarksScreen` or another screen now reflects immediately without reopening Equipment Detail.
3. **`GAP-03` — Fundamentals Screen Bookmark Support (`FundamentalsScreen.kt`, `AppNavigation.kt`):**
   - Reused existing shared bookmark storage (`BookmarkEntityType.TECHNICAL_STANDARD` + `topic.id` `"FUND-01"`..`"FUND-15"`) via `notesViewModel` (preserving the `8` `*ViewModel.kt` invariant enforced by `[S4-08]`).
   - Added `bookmarkedTopicIds` and `onToggleBookmark` parameters with default values to `FundamentalsScreen` and `FundamentalsScreenContent` (preserving preview/call-site compatibility) and added `>= 48.dp` `IconButton` bookmark toggles with descriptive `contentDescription` labels on every `FundamentalTopicCard`.
   - Partitioned `TECHNICAL_STANDARD` UI bookmark sets in `AppNavigation.kt` (`!startsWith("FUND-")` for `ReferenceLibraryScreen` vs `startsWith("FUND-")` for `FundamentalsScreen`) and added `TechnicalStandardBookmarkTarget` + `resolveTechnicalStandardBookmarkTarget(entityId)` to route `FUND-*` / matching `FundamentalsCatalog` IDs to `Screen.Fundamentals` (with focus scroll/highlight and `showOnlyVerified` filter reset when focusing a `CONTENT_GAP` topic) and `REF-*` IDs to `Screen.Library`.

---

## 2. Exact Source & Test Files Modified (`6` Files)

| File Path | Rationale & Implemented Change |
| :--- | :--- |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/home/HomeViewModel.kt` | Deduplicate and filter blank-ID bookmarks collected from `UserDataRepository.getAllBookmarks()`. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/home/HomeScreen.kt` | Add Saved Bookmarks strip (empty state + `LazyRow` cards), Home shortcuts (`Checklists`, `Notes`, `Bookmarks`, `Steam Table`), updated `HomePreviewData`, and preserved preview call compatibility. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/equipment/EquipmentViewModel.kt` | Replace one-shot `isBookmarked` query with a single reactive `getAllBookmarks()` flow combined into `loadEquipmentDetail` + `inFlightBookmarkIds` toggle guard. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/fundamentals/FundamentalsScreen.kt` | Add `>= 48.dp` accessible bookmark toggles per `FundamentalTopicCard`, case-insensitive ID focus matching, `showOnlyVerified` filter keying on focus target, and preview update. |
| `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/navigation/AppNavigation.kt` | Wire Home shortcuts + shared `navigateToBookmarkTarget`, Fundamentals bookmark state/toggle via `notesViewModel`, partitioned `TECHNICAL_STANDARD` bookmark sets, and `resolveTechnicalStandardBookmarkTarget` (`FUND-*` vs `REF-*`). |
| `android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage04UiStateAndAccessibilityTest.kt` | Add `seedBookmarks(...)` fake helper and `4` Batch 04 JUnit `@Test` methods covering Home bookmarks/deduplication/empty state, `FUND-*`/`REF-*` routing, reactive `EquipmentViewModel` bookmarks, and reactive `Fundamentals` bookmarks alongside `ReferenceLibrary`. |

*Documentation created in Batch 04:* `docs/BATCH_04_IMPLEMENTATION_REPORT.md` (`DOC-MPN-032`). Zero database files, backup codecs, calculation formulas, schemas, historical audit docs, or Git state were touched.

---

## 3. Authored Tests vs. Executed Verification (Strict Separation)

### Authored JVM JUnit Tests (NOT Executed on JVM)
- **Batch 04 Additions (`4` `@Test` methods in `Stage04UiStateAndAccessibilityTest.kt`, now `21` `@Test` methods in file):**
  1. `homeViewModelDisplaysSavedBookmarksDeduplicatesAndHandlesEmptyState`
  2. `technicalStandardBookmarkResolverRoutesFundamentalsAndReferencesCleanly`
  3. `equipmentViewModelReactsToExternalBookmarkChangesWithoutReopeningDetail`
  4. `notesViewModelManagesFundamentalsBookmarksReactivelyAlongsideReferences`
- **Total Authored JUnit Inventory:** **`84` `@Test` methods across `12` test files** in `android/app/src/test/`. **Native JVM JUnit execution (`./gradlew testDebugUnitTest`) was NOT executed** because JDK 17 and Android SDK 35 are absent in the container environment.

### Static / Python Verification Executed
- **`python3 test_suite_stage04.py` -> `10/10 PASSED` (`exit 0`):** Verified `23` `Screen` routes, `40` `@Preview` annotations across `17` UI files, `21` Stage 04 `@Test` methods, strict `8` ViewModel count, preserved SQLite DB byte size (`753,664` bytes) and calculation SHA-256 checksums, and balanced Kotlin braces/parentheses across all `74` `.kt` files.
- **Scope Disclaimer:** Python static checks inspect source structure, annotations, invariants, and pre-packaged SQLite integrity only; they do **not** establish Kotlin compilation, Compose layout rendering, APK packaging, or Android runtime behavior.

---

## 4. Remaining Limitations & Carried-Forward Gaps
- **Unverified Outside Container (JDK 17 + Android SDK 35 Required):** Kotlin compilation, KSP Room schema generation, JVM unit test execution (`./gradlew testDebugUnitTest`), Compose `@Preview` inspection, `./gradlew assembleDebug`, Android Emulator run, NIST REFPROP comparison (`IapwsIf97SteamEngine`), and physical smartphone testing remain unverified.
- **Unmodified Open Scope Gaps:** `GAP-07` through `GAP-11`, secondary `GAP-06` enhancements (checklist-to-equipment deep link, operator/shift metadata, custom checklist authoring), `GAP-12` (`CALC-02` `RESEARCH_PENDING`, `FUND-11..15` `CONTENT_GAP`), `GAP-13` (`62/64` non-full equipment records, `5/64` schematics), `REF-06` RPM limit reconciliation (`15,000` vs `30,000 RPM`), and statutory legal verification remain outside Batch 04 scope.
