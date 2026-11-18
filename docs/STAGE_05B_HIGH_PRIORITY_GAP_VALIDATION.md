# STAGE 05B: HIGH-PRIORITY GAP VALIDATION & REMEDIATION PLAN
**Document Identifier:** DOC-MPN-028  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Stage:** Stage 05B — Read-Only Validation & Remediation Planning (`GAP-01` through `GAP-06`)  
**Baseline Documents:** `docs/UI_COMPLETENESS_AUDIT_REPORT.md` (`DOC-MPN-026`), `docs/MISSING_OPTIONS_AND_FEATURES_REPORT.md` (`DOC-MPN-027`), `docs/STAGE_04_UI_UX_PREVIEW_PREPARATION_REPORT.md` (`DOC-MPN-024`)  
**Execution Mode:** STRICTLY READ-ONLY AUDIT (Zero Kotlin, XML, Gradle, Test, Gallery, or SQLite mutations performed)  
**Validation Date:** 2026-11-15  

---

## 1. Validation Scope, Classification Standard & Non-Breakage Invariants

Stage 05B independently verifies **`GAP-01` through `GAP-06`** directly against the live Android Kotlin source tree (`74` `.kt` files), Room schema/DAOs, domain calculation registry (`20` calculations), unit tests (`android/app/src/test`), and Python regression suite assertions (`test_suite_stage01..04.py`, `test_suite_phase04..07.py`).

### Severity Reconciliation Note
In `DOC-MPN-027`, **`GAP-01` through `GAP-04`** are categorized as `HIGH` priority and **`GAP-05` and `GAP-06`** as `MEDIUM` priority. Stage 05B validates all six gaps (`GAP-01`–`GAP-06`), resolves two secondary citation discrepancies in `DOC-MPN-027` (`UserDataDao.kt` does not exist — history methods reside on `CalculationDao` in `Daos.kt`; `ChecklistProgressEntity` resides in `DynamicUserEntities.kt`), and documents **two newly verified latent code bugs** uncovered during source tracing (`BOILER_SECTION` bookmark routing collision in `AppNavigation.kt:431`, and silent checklist remark overwrite in `ChecklistViewModel.kt:114–118` / `UserDataRepositoryImpl.kt:118–133`).

### Architectural & Regression Invariants Future Batches Must Respect
1. **`8` ViewModel Count Invariant (`[S3-06]`, `[S4-08]`):** `test_suite_stage03.py:348` and `test_suite_stage04.py:305–307` assert `len(vm_files) == 8` (`HomeViewModel`, `EquipmentViewModel`, `BoilerViewModel`, `CalculationViewModel`, `SearchViewModel`, `NotesViewModel`, `ChecklistViewModel`, `SettingsViewModel`). **No remediation batch may introduce a 9th `*ViewModel.kt` file.**
2. **`23` Navigation `Screen` Route Invariant (`[S4-01]`):** `test_suite_stage04.py:171–173` asserts `len(re.findall(r'object\s+(\w+)\s*:\s*Screen\(', screen_src)) == 23`. Hosting Calculation History as a **`ModalBottomSheet`** (or toggleable panel/tab) inside `CalculationListScreen` / `CalculationRunnerScreen` satisfies `GAP-02` cleanly while keeping `23` routes intact and maintaining bottom-bar visibility behavior cleanly.
3. **Repository / DAO Test Fake Coupling:** `Stage04UiStateAndAccessibilityTest.kt:69` (`FakeUserDataRepository : UserDataRepository`) and `BackupJsonCodecAndCalcRepoTest.kt:22` (`FakeCalculationDao : CalculationDao`) explicitly implement all interface members. Any new non-default method added to `CalculationDao` or `UserDataRepository` will break JUnit compilation unless the fake classes are updated in lockstep (or Kotlin interface defaults are used safely on `UserDataRepository`).
4. **`...Content(...)` Default Parameter Compatibility (`[S4-07]`):** Every new callback added to a stateless `...Content(...)` composable (`HomeScreenContent`, `CalculationListContent`, `CalculationRunnerContent`, `ChecklistScreenContent`, `BoilerDashboardContent`) MUST have a default empty lambda (`= {}`) so all `40` existing `@Preview` functions continue to compile unchanged.
5. **Protected Engineering Core:** Zero changes to `mechanical_pro_notes.db`, Room database `version` / entities / migrations, `BoilerThermalCalculations.kt`, `WashPlantMaintenanceCalculations.kt`, `IapwsIf97SteamEngine.kt`, `BackupJsonCodec` v2 SHA-256 contract, or locked `Color.kt` / `Theme.kt` tokens.

---

## 2. Independent Source Validation (`GAP-01` through `GAP-06`)

### `GAP-01` — 3 Orphaned Calculation Categories Excluded from Filter Chips
- **Verified status:** **CONFIRMED — Existing feature partially inaccessible / hardcoded filter mismatch (UI logic defect).** All `20` calculation rows exist in SQLite and `CalculationSpecifications.ALL_CALCULATIONS`, and render under `"All"`, but `CALC-07`, `CALC-09`, and `CALC-17` are excluded by a stale hardcoded filter array.
- **Source evidence:**
  - `CalculationListScreen.kt:68–77`: Hardcodes exactly `8` chip strings (`"All"`, `"Boiler Thermal"`, `"Boiler Water"`, `"Combustion & Draught"`, `"Steam Distribution"`, `"Wash Plant Machinery"`, `"Water Treatment"`, `"Pneumatics & Air"`).
  - `CalculationListScreen.kt:79–83`: Applies exact case-insensitive equality (`it.category.equals(selectedCategory, ignoreCase = true)`).
  - `mechanical_pro_notes.db` (`calculation` table) & `CalculationSpecifications.kt`: Verified `20` seeded calculations use **`10`** distinct categories — the `7` specific ones listed above plus `"Utility & Pumping"` (`CALC-07`), `"Boiler Safety"` (`CALC-09`), and `"Wash Plant Utility"` (`CALC-17`).
- **Root cause:** `CalculationListContent` maintains a static duplicate `categories` list in UI code instead of deriving distinct categories from `state.calculations` loaded from `CalculationRepository`.
- **Risk:** **Very Low.** Zero impact on `CalculationViewModel`, `CalculationRepository`, Room schema, formulas, or `40` `@Preview` signatures (`CalculationListPreviewData` uses `"Boiler Thermal"`, so previews continue rendering cleanly). Only minor check: ensure `"All"` chip comes first and preserve stable chip ordering (`state.calculations.map { it.category.trim() }.filter { it.isNotEmpty() }.distinct()`). Also ensure `selectedCategory` resets safely to `null` if a list reload ever drops an active category.
- **Recommended minimal fix:**
  - Replace hardcoded `val categories = listOf(...)` in `CalculationListScreen.kt:68–77` with `remember(state.calculations) { listOf("All") + state.calculations.map { it.category.trim() }.filter { it.isNotEmpty() }.distinct() }` (and optionally move pure category extraction/filtering into a tiny reusable helper function in `CalculationListScreen.kt` or `CalculationViewModel.kt` so unit tests can assert zero orphaned categories). Add `defaultMinSize(minHeight = 48.dp)` to those `FilterChip`s (`CalculationListScreen.kt:269–273`) while touching that block for `[S4-05]` consistency.
- **Regression tests:**
  - Add JUnit test in `Stage04UiStateAndAccessibilityTest.kt` (or a new calculation filter test) feeding all `CalculationSpecifications.ALL_CALCULATIONS` (`20` calculations) into the category extractor and asserting all `10` distinct categories are returned (`"Utility & Pumping"`, `"Boiler Safety"`, `"Wash Plant Utility"` included) and **every** calculation matches at least one specific non-`"All"` chip.
- **Implementation priority:** **1 (`HIGH` — Immediate / Batch 01).** Zero dependencies, 1-file surgical UI change, immediate unblocking of `CALC-07`, `CALC-09`, and `CALC-17` filtering.

---

### `GAP-02` — Missing Calculation History Viewer Screen (`calculation_history` Unreachable)
- **Verified status:** **CONFIRMED — Genuine MISSING UI viewer surface (backend persistence, export/import, and summary counter exist; individual delete DAO query does not exist yet).**
  - *Doc correction vs `DOC-MPN-027`:* `DOC-MPN-027` referenced `UserDataDao.kt` / `deleteCalculationHistory`; actual codebase places `getCalculationHistory()`, `insertHistory()`, and `deleteAllHistory()` on **`CalculationDao`** in `Daos.kt:149–156`, exposed via `UserDataRepository.getCalculationHistory()` / `saveCalculationHistory()` (`RepositoryInterfaces.kt:60–61`). Individual history-row deletion (`deleteHistoryById`) is not yet implemented on `CalculationDao` or `UserDataRepository`.
- **Source evidence:**
  - Save action: `CalculationRunnerScreen.kt:405–419` (`"Save to Personal History"`) -> `CalculationViewModel.kt:172–189` (`saveToHistory()`).
  - Persistence & Model: `DynamicUserEntities.kt:54–75` (`CalculationHistoryEntity`), `UserDataModels.kt:51–59` (`CalculationHistoryItem`), `Daos.kt:149–156` (`CalculationDao`), `UserDataRepositoryImpl.kt:78–104`.
  - Backup & Count: `SettingsScreen.kt:333–336` displays `state.totalHistoryCount`; `UserDataRepositoryImpl.kt:142, 195–207` and `BackupJsonCodec.kt` serialize/restore `calculation_history` with SHA-256 checksum protection.
  - Missing UI: `CalculationViewModel.kt` does not collect `userDataRepository.getCalculationHistory()`; `Screen.kt`, `AppNavigation.kt`, `CalculationListScreen.kt`, and `CalculationRunnerScreen.kt` never read or display `CalculationHistoryItem` records.
- **Root cause:** Phase 06 prioritized saving, counting, and backing up `calculation_history`, leaving the read/inspection UI surface deferred.
- **Risk:** **Low–Medium.**
  1. Adding a new `Screen.CalculationHistory` sealed `object` to `Screen.kt` would change `Screen` object count from `23` to `24` and **break `test_suite_stage04.py` `[S4-01]`** (`len(nav_objects) == 23`).
  2. Adding a new `HistoryViewModel.kt` would **break `[S3-06]` & `[S4-08]`** (`len(vm_files) == 8`).
  3. If per-row delete (`deleteHistoryById(id: Long)`) is added to `CalculationDao` and `UserDataRepository`, **`FakeCalculationDao` (`BackupJsonCodecAndCalcRepoTest.kt:22`) and `FakeUserDataRepository` (`Stage04UiStateAndAccessibilityTest.kt:69`) MUST also override that method** or JUnit compilation will fail. *(Whereas viewing/re-opening history via `getCalculationHistory()` needs zero DAO/Repo interface changes at all!)*
- **Recommended minimal fix:**
  - Observe `userDataRepository.getCalculationHistory()` inside existing **`CalculationViewModel`** (`CalculationListUiState.history: List<CalculationHistoryItem> = emptyList()`) — zero new ViewModels, uses already-injected `userDataRepository` (`AppContainer.kt`).
  - Add a TopBar `History` (`Icons.Default.History`) `IconButton` on `CalculationListScreen.kt` (`>= 48.dp`) that opens a `ModalBottomSheet` displaying saved runs sorted newest-first (timestamp formatted cleanly via `SimpleDateFormat` / `DateFormat`, `calculationId`, `calculationTitle`, formatted `inputValues`, `outputValues`, optional `userRemark`), with an empty state and tap-to-open `onNavigateToRunner(item.calculationId)`.
  - Keeping it as a `ModalBottomSheet` inside `CalculationListScreen.kt` preserves `23` `Screen` objects (`[S4-01]`) and `8` ViewModels (`[S4-08]`) with **zero changes to `CalculationDao`, `UserDataRepository`, `MechanicalProDatabase`, or `AppNavigation.kt`**. *(Optional follow-up in same batch or Batch 03 if owner wants row deletion: add `deleteHistoryItem(id: Long)` cleanly to `CalculationDao` + `UserDataRepository` + `FakeCalculationDao` + `FakeUserDataRepository`.)*
- **Regression tests:**
  - Extend `Stage04UiStateAndAccessibilityTest.kt` to verify `CalculationViewModel.listState.value.history` collects saved `CalculationHistoryItem` entries from `FakeUserDataRepository` (and preserves `saveToHistory()` flow).
  - Verify `test_suite_stage04.py` still passes `10/10` (`23` routes, `8` ViewModels, `@Preview` signatures compiling).
- **Implementation priority:** **1 (`HIGH` — Batch 01 alongside `GAP-01` & `GAP-04`).** Self-contained in `CalculationViewModel.kt` + `CalculationListScreen.kt`; unblocks complete read-access to the last orphaned Room table (`calculation_history`).

---

### `GAP-03` — `4` of `5` Bookmark Types Cannot Be Created from Any Screen (+ Confirmed `BOILER_SECTION` Routing Bug)
- **Verified status:** **CONFIRMED — Missing UI triggers for existing backend + CONFIRMED Navigation Routing Bug.** All 5 `BookmarkEntityType` enums are already supported in Room (`UserBookmarkEntity`), `UserDataRepositoryImpl.toggleBookmark(...)`, `BookmarksScreen.kt:154–160`, and `AppNavigation.kt:429–446`, **but** UI toggle controls only exist for `EQUIPMENT`, AND `AppNavigation.kt:431–433` misroutes `BOILER_SECTION` bookmarks!
- **Source evidence:**
  - Enum contract: `UserDataModels.kt:34–40` defines `EQUIPMENT`, `BOILER_SECTION`, `CALCULATION`, `CHECKLIST`, `TECHNICAL_STANDARD`.
  - Working type (`1/5`): `EquipmentDetailScreen.kt:105–118` & `EquipmentViewModel.kt:138, 162–170` (`BookmarkEntityType.EQUIPMENT`). *(Secondary UX note: `EquipmentViewModel.kt:138` reads `isBookmarked` once via `suspend` rather than observing `getAllBookmarks()` Flow, so unfavoriting from `BookmarksScreen` then pressing Back leaves the detail star stale until reload.)*
  - Missing creators (`4/5`):
    - `CALCULATION`: `CalculationRunnerScreen.kt:74–103` (`TopAppBar` has no `actions` slot or bookmark button) & `CalculationViewModel.kt` (no `isBookmarked` state or `toggleBookmark`).
    - `CHECKLIST`: `ChecklistScreen.kt:103–113` (`TopAppBar` has only Refresh/Reset) & `ChecklistViewModel.kt` (does not observe/toggle bookmarks even though `userDataRepository` is already in its constructor at `line 36`).
    - `TECHNICAL_STANDARD`: `ReferenceLibraryScreen.kt:135–176` renders `REF-01..12` cards with zero bookmark toggle (screen has no ViewModel dependency; pure catalog UI).
    - `BOILER_SECTION`: `BoilerDashboardScreen.kt:64–83` (`TopAppBar` has no `actions` slot; screen only injects `BoilerViewModel(boilerRepository)` without `userDataRepository`).
  - **Confirmed Routing Bug (`AppNavigation.kt:431–433`):**
    ```kotlin
    BookmarkEntityType.EQUIPMENT, BookmarkEntityType.BOILER_SECTION -> {
        navController.navigate(Screen.EquipmentDetail.createRoute(entityId))
    }
    ```
    Routing `BOILER_SECTION` to `Screen.EquipmentDetail.createRoute(entityId)` fails with `"Equipment '...' not found in local database"` (`EquipmentViewModel.kt:132`) for any non-`EQ-*` boiler section key (e.g., `"BOILER_TAB:GAS_BOILERS"` or `"ACT_2022_RULES_2025"`).
- **Root cause:** Bookmark UI was wired on `EquipmentDetailScreen` during Phase 05, while the other 4 destinations remained view-only; `BOILER_SECTION` was provisionally grouped with `EQUIPMENT` assuming boiler assets (`EQ-G01`/`EQ-G02`).
- **Risk:** **Medium.** Requires coordinated edits across `4` screens + `2` ViewModels + `AppNavigation.kt`. Adding `userDataRepository` to `BoilerViewModel` constructor would require updating `AppNavigation.kt`'s `boilerViewModel` instantiation (`AppNavigation.kt:79`, not directly in `Stage04` tests, wait — check `AppNavigation` instantiation vs tests). Passing existing `notesViewModel` bookmark state/toggle lambdas from `AppNavigation.kt` to `BoilerDashboardScreen` and `ReferenceLibraryScreen` avoids changing `BoilerViewModel`'s constructor altogether!
- **Recommended minimal fix (Zero DB / Zero ViewModel-Count Impact):**
  1. **`CALCULATION` (`CalculationViewModel` + `CalculationRunnerScreen`):** Track `bookmarkedCalcIds` (via `userDataRepository.getAllBookmarks()` flow) or `isBookmarked: Boolean` in `CalculationRunnerUiState` + `toggleBookmark()`, and add a `Bookmark` / `BookmarkBorder` `IconButton` in `CalculationRunnerScreen.kt` `TopAppBar` (`actions`). Reactive Flow keeps bookmark icon in sync after editing `BookmarksScreen`!
  2. **`CHECKLIST` (`ChecklistViewModel` + `ChecklistScreen`):** Collect `userDataRepository.getAllBookmarks()` filtered to `BookmarkEntityType.CHECKLIST` inside existing `ChecklistViewModel.kt` (`bookmarkedChecklistIds: Set<String>`) + `toggleCurrentChecklistBookmark()`, and add a `Bookmark` `IconButton` to `ChecklistScreen.kt` `TopAppBar` (`actions` beside Reset).
  3. **`TECHNICAL_STANDARD` (`ReferenceLibraryScreen` + `AppNavigation`):** Add optional `bookmarkedRefIds: Set<String> = emptySet()` and `onToggleBookmark: ((String, String) -> Unit)? = null` to `ReferenceLibraryScreen`, passing state & handler in `AppNavigation.kt:457–461` via `notesViewModel` (add small helper `toggleBookmark(entityType, entityId, title)` on `NotesViewModel.kt`, which already owns `userDataRepository` and collects `bookmarks`). Render compact `48.dp` `IconButton` on each `ref` card header.
  4. **`BOILER_SECTION` (`BoilerDashboardScreen` + `AppNavigation.kt` Routing Fix):** Pass `bookmarkedBoilerSections: Set<String>` + `onToggleSectionBookmark: (BoilerTab) -> Unit` from `AppNavigation.kt:292–303` into `BoilerDashboardScreen` (`TopAppBar` `actions`, bookmarking current active tab ID e.g. `"BOILER:${uiState.selectedTab.name}"`), **AND fix `AppNavigation.kt:431–433`** so `BookmarkEntityType.BOILER_SECTION` selects the matching `BoilerTab` via `boilerViewModel.selectTab(...)` (falling back safely to `EquipmentDetail` if an existing user backup somehow stored an `"EQ-"` `entityId` under `BOILER_SECTION`!).
  5. *(Optional hardening)* In `EquipmentViewModel.kt:138`, derive `isBookmarked` reactively from `userDataRepository.getAllBookmarks()` or re-check on resume so `EQUIPMENT` bookmarks also stay synced with `BookmarksScreen`.
- **Regression tests:**
  - Extend `Stage04UiStateAndAccessibilityTest.kt` to verify reactive bookmark toggling in `CalculationViewModel` (`BookmarkEntityType.CALCULATION`) and `ChecklistViewModel` (`BookmarkEntityType.CHECKLIST`).
  - Add unit test covering `BOILER_SECTION` tab-key parsing / fallback routing logic so both new `"BOILER:*"` tab keys and legacy `"EQ-*"` IDs never hit a dead end.
- **Implementation priority:** **2 (`HIGH` — Batch 02).** Coordinates across 5 UI files & `AppNavigation.kt`, so isolating it cleanly into Batch 02 prevents merge/regression noise in Batch 01.

---

### `GAP-04` — Bottom Navigation Bar Label Truncation Bug (`"Dedicated"`, `"Engineering"`)
- **Verified status:** **CONFIRMED — Pure Presentation / Labeling Defect.** Existing bottom navigation bar renders misleading first-word splits instead of concise module names.
- **Source evidence:**
  - `Screen.kt:14, 16, 17, 27, 51`: `bottomBarScreens = listOf(Home, WashPlant, BoilerModule, Calculations)` with full `title`s `"Home Dashboard"`, `"Wash Plant Equipment"`, `"Dedicated Boiler Module"`, `"Engineering Calculations"`.
  - `AppNavigation.kt:243`: `label = { Text(screen.title.split(" ").first()) }`, which outputs:
    - `Home` -> `"Home"`
    - `WashPlant` -> `"Wash"` (acceptable, though `"Equipment"` or `"Plant"` is clearer)
    - `BoilerModule` -> **`"Dedicated"`** (obscures `"Boiler"`)
    - `Calculations` -> **`"Engineering"`** (obscures `"Calcs"`)
- **Root cause:** Naïve `.split(" ").first()` string heuristic applied to multi-word full `Screen.title` strings.
- **Risk:** **Negligible.** Zero route changes (`route` strings unchanged), preserves `23` `Screen` objects (`[S4-01]`), zero ViewModel/DB/formula changes. Only caution on narrow `320.dp–360.dp` screens: ensure `"Equipment"` or `"Plant"` + `"Boiler"` + `"Calcs"` + `"Home"` fit on 1 line (`maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis`). (`"Plant"` / `"Boiler"` / `"Calcs"` or `"Equip"` / `"Boiler"` / `"Calcs"` stay completely unclipped even with `1.18f` High Legibility scaling!)
- **Recommended minimal fix:**
  - Add explicit `val shortTitle: String` (defaulting cleanly or specified explicitly for the 4 bottom bar screens in `Screen.kt`: `Home -> "Home"`, `WashPlant -> "Plant"` or `"Equipment"`, `BoilerModule -> "Boiler"`, `Calculations -> "Calcs"`), and update `AppNavigation.kt:243` to `Text(text = screen.shortTitle, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)`. Also keep `contentDescription = screen.title` on the icon (`line 242`) for full TalkBack announcements.
- **Regression tests:**
  - Assert in JUnit (`Stage04UiStateAndAccessibilityTest.kt` or navigation test) that `Screen.bottomBarScreens.map { it.shortTitle }` yields concise expected labels with none equal to `"Dedicated"` or `"Engineering"`.
  - Verify `test_suite_stage04.py` `[S4-01]` (`23` `Screen` objects) remains passing.
- **Implementation priority:** **1 (`HIGH` — Immediate / Batch 01).** Trivial 2-line change, zero regression risk, highest visual clarity improvement on every primary tab.

---

### `GAP-05` — Unused `HomeUiState.bookmarks` & Missing Dashboard Shortcuts
- **Verified status:** **CONFIRMED — Partially Implemented Feature.** `HomeViewModel` already queries, combines, and emits `bookmarks: List<UserBookmark>`, while `HomeScreen.kt` completely ignores `uiState.bookmarks` and omits planned quick links to `Checklists`, `Steam Table`, `Notes`, and `Bookmarks`. *(Classified `MEDIUM` in `DOC-MPN-027 §3`.)*
- **Source evidence:**
  - `HomeViewModel.kt:25, 50, 67`: `val bookmarks: List<UserBookmark> = emptyList()` populated reactively via `userDataRepository.getAllBookmarks()`.
  - `HomeScreen.kt:66–411`: Never reads `uiState.bookmarks`. Quick-action grid (`HomeScreen.kt:249–322`) renders only `4` `OutlinedButton`s (`Equipment (64)`, `Calculations (20)`, `Unit Converter (13)`, `Fundamentals (15)`).
  - `AppNavigation.kt:266–278`: Passes only callbacks for Drawer, Search, Equipment, Boiler, Calculations, EquipmentDetail, UnitConverter, and Fundamentals.
- **Root cause:** `HomeScreen` received Unit Converter and Fundamentals quick buttons in Stage 03 while `Checklists`, `Steam Table`, `Notes`, `Bookmarks`, and the `uiState.bookmarks` preview list were deferred.
- **Risk:** **Low.** Adding optional default parameters (`= {}`) to `HomeScreen` and `HomeScreenContent` preserves source compatibility with all 3 existing `@Preview` functions (`HomeScreen.kt:484–530`). Clicking a `Pinned Bookmark` item on `HomeScreen` can reuse a single `onNavigateToBookmark: (UserBookmark) -> Unit` handler in `AppNavigation.kt` (shared with the fixed `BookmarksScreen` dispatcher from `GAP-03`, preventing duplicated routing logic — especially ensuring `BOILER_SECTION` routes properly!).
- **Recommended minimal fix:**
  - Expand `HomeScreenContent`'s Quick Action Grid (`HomeScreen.kt:249–322`) with one additional 2-button row (`Checklists`, `Steam Table`) plus compact links/row or TopBar/section access for `Personal Notes` & `Bookmarks` (using `defaultMinSize(minHeight = 48.dp)`).
  - Render a compact `"Saved Bookmarks (${uiState.bookmarks.size})"` section on `HomeScreenContent` when `uiState.bookmarks.isNotEmpty()` (showing up to top `3–5` bookmarks with a `"View All"` action to `Screen.Bookmarks.route`, plus tap-to-open navigation), leaving Featured Equipment cleanly intact below.
  - Extract a single helper `navigateToBookmark(bookmarkId, entityType, ...)` in `AppNavigation.kt` shared by `BookmarksScreen` and `HomeScreen` so `GAP-03` and `GAP-05` stay DRY.
- **Regression tests:**
  - Add a `@Preview` / check with populated `bookmarks` in `HomePreviewData` (or a JUnit check verifying `HomeViewModel` exposes `bookmarks` in `HomeUiState`).
  - Confirm `[S4-02]` (`OperationalDarkGreen`, etc.), `[S4-05]` (`48.dp`), and `[S4-07]` (`fun HomeScreenContent(`) pass untouched.
- **Implementation priority:** **3 (`MEDIUM` — Batch 03, cleanly ordered AFTER or alongside `GAP-03` so bookmark click-routing shares the fixed `AppNavigation` dispatcher).**

---

### `GAP-06` — Checklist Schema Fields (`remarks`, `completion_timestamp`) Unexposed + Confirmed Silent Remark-Overwrite Bug
- **Verified status:** **CONFIRMED — Partially Implemented Feature WITH Confirmed Data-Loss Bug.**
  - *Doc correction vs `DOC-MPN-027`:* `ChecklistProgressEntity` is defined in `DynamicUserEntities.kt:77–95` (not `ChecklistProgressEntity.kt`). Both DB persistence and JSON v2 backup already store `completionTimestamp: Long?` and `remarks: String?`, **but** ViewModel/UI discard them AND toggling a checkbox silently erases any previously saved remark!
- **Source evidence:**
  - DB Entity & Backup: `DynamicUserEntities.kt:83–95` (`completionTimestamp: Long?`, `remarks: String?`); `UserDataModels.kt:42–49` (`ChecklistProgress`); `BackupJsonCodec.kt:313–320` already serializes/restores both fields.
  - Repository Upsert Behavior: `UserDataRepositoryImpl.kt:118–133` replaces row with `@Insert(onConflict = REPLACE)`:
    ```kotlin
    ChecklistProgressEntity(
        checklistId = checklistId,
        itemId = itemId,
        isCompleted = isCompleted,
        completionTimestamp = if (isCompleted) System.currentTimeMillis() else null,
        remarks = remarks
    )
    ```
  - **Confirmed Bug (`ChecklistViewModel.kt:107–120`):** `toggleItem(itemId)` calls `userDataRepository.setChecklistItemCompleted(checklistId, itemId, newCompleted)` without passing `remarks` (`defaults to null`), which **silently overwrites and erases any existing `remarks` stored in `checklist_progress`** whenever an item is checked/unchecked!
  - Discarded State (`ChecklistViewModel.kt:28–33, 49–60`): `ChecklistUiState` only stores `completedByChecklist: Map<String, Set<Int>>` and `completedItemIds: Set<Int>`, dropping `completionTimestamp` and `remarks`.
  - UI Surface (`ChecklistScreen.kt:233–305`): Item cards show checkbox + category + `MANDATORY` badge + `item.text` only; no timestamp caption or remark view/editor exists.
- **Root cause:** Initial checklist UI only wired boolean completion state (`isCompleted`), omitting metadata preservation and remark editing from `ChecklistViewModel` and `ChecklistScreen`.
- **Risk:** **Low–Medium.**
  - Removing or renaming `completedItemIds` or `completedByChecklist` on `ChecklistUiState` would break `Stage04UiStateAndAccessibilityTest.kt:224, 228` (`vm.uiState.value.completedItemIds`). Keep existing fields intact and **add** `progressByChecklist: Map<String, Map<Int, ChecklistProgress>>` alongside them!
  - Existing `UserDataRepository.setChecklistItemCompleted(checklistId, itemId, isCompleted, remarks: String? = null)` signature already accepts `remarks` (`RepositoryInterfaces.kt:66`), so **no interface/DAO change is required** (`FakeUserDataRepository` compiles with zero breakage!).
- **Recommended minimal fix:**
  1. In `ChecklistViewModel.kt`:
     - Store `progressByChecklist: Map<String, Map<Int, ChecklistProgress>> = emptyMap()` in `ChecklistUiState` (keeping `completedByChecklist` & `completedItemIds` untouched for backwards compatibility).
     - In `toggleItem(itemId)`: look up existing `remarks = currentState.progressByChecklist[checklistId]?.get(itemId)?.remarks` and pass `remarks = existingRemarks` to `setChecklistItemCompleted(...)` so toggling never wipes remarks!
     - Add `updateItemRemark(itemId: Int, remarks: String)` passing current `isCompleted` state + trimmed `remarks.ifBlank { null }` to `setChecklistItemCompleted(...)`.
  2. In `ChecklistScreen.kt`:
     - Show formatted `Completed: dd MMM HH:mm` caption when `progress?.completionTimestamp != null`, and render saved `remarks` inline when non-blank.
     - Add a `>= 48.dp` `"Add/Edit inspection remark"` `IconButton` on each checklist item card (positioned outside `.toggleable(...)` so tapping Remark doesn't accidentally toggle the checkbox!) opening a lightweight `AlertDialog` with `.imePadding()`-friendly `OutlinedTextField`.
  *(Note: Secondary enhancements mentioned in `DOC-MPN-027` such as custom user-created checklists or shift filters are outside minimal `GAP-06` scope and require schema/entity evaluation, so they should stay excluded from minimal remediation.)*
- **Regression tests:**
  - Extend `Stage04UiStateAndAccessibilityTest.kt`:
    1. Verify saving a remark via `updateItemRemark(...)` persists `remarks` in `ChecklistUiState.progressByChecklist`.
    2. **Crucial bug regression test:** Verify calling `toggleItem(itemId)` to uncheck or re-check an item preserves existing `remarks` instead of overwriting with `null`.
- **Implementation priority:** **3 (`MEDIUM` — Batch 03).** Independent from `GAP-01..05`; zero DAO/schema changes needed.

---

## 3. Cross-Cutting Safety Impact Matrix

| Technical Subsystem | Touched by Recommended Fixes? | Detailed Impact Assessment |
| :--- | :---: | :--- |
| **Engineering Formulas & Constants (`CALC-01`–`20`, IAPWS-IF97)** | **NO** | All 20 formulas, validation bounds, `CalculationResultFormatter`, and `56`-anchor linear-P steam table remain untouched. |
| **Room Schema, Migrations & Pre-Packaged DB (`v1`, `753,664 B`)** | **NO** | All 8 Room tables (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`, plus static catalog tables) already exist. Zero entity changes and zero migrations needed (`[S4-09]` & `DB SHA-256` remain identical). |
| **SAF Backup JSON v2 + SHA-256 Contract (`BackupJsonCodec`)** | **NO** | `calculation_history`, `user_bookmark`, and `checklist_progress` (`remarks`, `completion_timestamp`) are already serialized and restored in v2 backups. Fix for `GAP-02`, `GAP-03`, and `GAP-06` surfaces existing persisted fields. |
| **Navigation Graph & Route Count (`Screen.kt`, `23` routes)** | **MINIMAL / SAFE** | `Screen.shortTitle` (`GAP-04`) and fixing `BOILER_SECTION` handler (`GAP-03`) keep exact `23` `Screen` objects (`[S4-01]` passes). Hosting Calculation History in a `ModalBottomSheet` avoids adding route #24. |
| **ViewModel Count (`8` ViewModels)** | **NO** | Uses existing `CalculationViewModel`, `ChecklistViewModel`, `HomeViewModel`, and `NotesViewModel` (`[S3-06]`, `[S4-08]` remain `len == 8`). |
| **Locked Theme Tokens (`Color.kt`, `Theme.kt`)** | **NO** | Uses existing Material 3 typography, luminance-aware colors, and `48.dp` ergonomics. Zero token modifications. |

---

## 4. Recommended Isolated Remediation Batches

To minimize merge risk and allow step-by-step verification against both Python structural suites and JUnit tests:

| Batch | Gaps Covered | Exact Scope & Files | Why Isolated Together | Verification Target |
| :--- | :--- | :--- | :--- | :--- |
| **Batch 01 (Recommended First)** | **`GAP-04`**, **`GAP-01`**, **`GAP-02`** | `Screen.kt`, `AppNavigation.kt` (line `243` short label), `CalculationViewModel.kt`, `CalculationListScreen.kt` | Fixes bottom-bar confusion + unlocks all 3 orphaned calculations (`CALC-07/09/17`) + unlocks the completely unreachable `calculation_history` table using existing Room/Repo methods with zero interface breaking changes. | `test_suite_stage01..04.py` + new JUnit tests in `Stage04UiStateAndAccessibilityTest.kt` (10 categories + history StateFlow). |
| **Batch 02** | **`GAP-03`** | `AppNavigation.kt` (`BOILER_SECTION` fix + wiring), `NotesViewModel.kt`, `CalculationViewModel.kt`, `CalculationRunnerScreen.kt`, `ChecklistViewModel.kt`, `ChecklistScreen.kt`, `ReferenceLibraryScreen.kt`, `BoilerDashboardScreen.kt`, *(optional sync fix in `EquipmentViewModel.kt`)* | Unlocks all 4 missing `BookmarkEntityType` creators AND fixes the `BOILER_SECTION` navigation bug atomically across creator + dispatcher sides. | `Stage04UiStateAndAccessibilityTest.kt` bookmark toggle tests for Calculation & Checklist + tab routing test + `test_suite_stage04.py`. |
| **Batch 03** | **`GAP-06`**, **`GAP-05`** | `ChecklistViewModel.kt`, `ChecklistScreen.kt`, `HomeScreen.kt`, `AppNavigation.kt` (Home bookmark/shortcut callbacks) | Fixes the latent checklist remark-overwrite bug + exposes remarks/timestamps (`GAP-06`), and completes `HomeScreen` bookmark/quick-action wiring (`GAP-05`) reusing Batch 02's shared bookmark dispatcher. | `Stage04UiStateAndAccessibilityTest.kt` remark persistence & non-overwrite toggle regression test + `test_suite_stage04.py`. |

### Recommended Batch 01 Scope (Ready for Next Turn When Authorized)
1. **Bottom Navigation `shortTitle` (`GAP-04`):** Define clear concise labels (`"Home"`, `"Plant"`, `"Boiler"`, `"Calcs"`) on `Screen.kt` and update `AppNavigation.kt:243` with `maxLines = 1, softWrap = false`.
2. **Dynamic Calculation Category Chips (`GAP-01`):** Derive distinct categories dynamically from `state.calculations` in `CalculationListScreen.kt:68–77` so `"Utility & Pumping"` (`CALC-07`), `"Boiler Safety"` (`CALC-09`), and `"Wash Plant Utility"` (`CALC-17`) become filterable with zero orphaned items.
3. **Calculation History Viewer Sheet (`GAP-02`):** Observe `userDataRepository.getCalculationHistory()` in `CalculationViewModel.kt` and add a TopBar `History` `IconButton` + `ModalBottomSheet` in `CalculationListScreen.kt` displaying saved runs (date/time, inputs, outputs, notes, tap-to-reopen in `CalculationRunner`), plus regression JUnit tests in `Stage04UiStateAndAccessibilityTest.kt`.

---

## 5. Stage 05B Completion Honest Evidence Declaration
- **Container Limitations Preserved:** No native Android Studio / JDK 17 / Android SDK 35 compiler executed (`DOC-MPN-025`); JUnit tests listed above are source-verified targets pending native `./gradlew testDebugUnitTest` execution on an owner PC/CI environment.
- **Zero Mutated Code Assets:** Read-only validation only; all `74` `.kt` files, `mechanical_pro_notes.db` (`753,664` bytes), Gradle files, `visual-preview-gallery/index.html`, and Python test suites remain unmodified from pre-Stage-05B baseline.
