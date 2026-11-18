# MISSING OPTIONS & FEATURES REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-027  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Companion Audit:** `docs/UI_COMPLETENESS_AUDIT_REPORT.md` (`DOC-MPN-026`)  
**Authoring Basis:** Direct Static Inspection of Kotlin Source Files, Room Schema & `mechanical_pro_notes.db`  
**Audit Date (Pre-Batch-01 Baseline):** 2026-11-15  
**Checkpoint Synchronization Date:** 2026-11-16  

---

> **Checkpoint Synchronization Update (2026-11-16 — Post-Batch 01–03 & P0 Recovery):**  
> - **Implemented at Source/Test Level (Runtime Unverified):** `GAP-01`, `GAP-02`, `GAP-04` (`Batch 01` — `DOC-MPN-029`), `GAP-03` core bookmark integration (`Batch 02` — `DOC-MPN-030`), and `GAP-06` core checklist remarks/timestamp preservation & editing (`Batch 03` — `DOC-MPN-031`).  
> - **P0 Build Tooling Recovery:** `android/gradlew` executable permission restored (`-rwxr-xr-x`); `test_suite_stage01.py` now passes `10/10` (static).  
> - **Remaining Open Items:** `GAP-05`, `GAP-03` remainder (`EquipmentViewModel.kt:138` one-shot `isBookmarked`, `FundamentalsScreen.kt` bookmark button), secondary `GAP-06` items, `GAP-07..13`, `REF-06` vs `FUND-05` RPM limit conflict (`15,000` vs `30,000 RPM`), independent legal verification of Bangladesh Boilers Act 2022 / Rules 2025, and native Android compilation / JVM JUnit execution / Stage 05 Emulator QA (`BLOCKED` in container).

---

## 1. Purpose

This report catalogs every **missing option, hidden UI bug, unreachable database field, missing navigation action, and incomplete content gap** uncovered during the source-verified Phase A inspection—prioritized so future Android implementation stages can close them cleanly before phone deployment.

---

## 2. Critical & High-Priority UI / Functional Gaps

### GAP-01 — 3 Orphaned Calculation Categories Excluded from Filter Chips (`HIGH` -> `SOURCE-IMPLEMENTED IN BATCH 01; RUNTIME UNVERIFIED`)
- **Baseline Evidence (`2026-11-15`):** `CalculationListScreen.kt:68–77` hardcoded 8 filter chips, omitting `"Utility & Pumping"` (`CALC-07`), `"Boiler Safety"` (`CALC-09`), and `"Wash Plant Utility"` (`CALC-17`).
- **2026-11-16 Batch 01 Status (`DOC-MPN-029`):** Derived dynamic categories via `extractCalculationCategories()` / `filterCalculationsByCategory()` in `CalculationViewModel.kt` / `CalculationListScreen.kt`; unit/static checks authored; Compose runtime verification remains pending (`Stage 05 BLOCKED`).

### GAP-02 — Missing Calculation History Viewer Screen (`HIGH` -> `SOURCE-IMPLEMENTED IN BATCH 01; RUNTIME UNVERIFIED`)
- **Baseline Evidence (`2026-11-15`):** `saveResultToHistory()`, `getAllCalculationHistory()`, and `deleteCalculationHistory()` existed without any UI viewer.
- **2026-11-16 Batch 01 Status (`DOC-MPN-029`):** Implemented reactive history observation in `CalculationViewModel.kt` and a Calculation History `ModalBottomSheet` in `CalculationListScreen.kt` with empty state and delete confirmation; runtime device/emulator QA remains unverified.

### GAP-03 — `4` of `5` Bookmark Types Cannot Be Created from Any Screen (`HIGH` -> `PARTIALLY RESOLVED IN BATCH 02; REMAINDER OPEN`)
- **Baseline Evidence (`2026-11-15`):** `BookmarksScreen.kt` and `AppNavigation.kt` supported `EQUIPMENT`, `BOILER_SECTION`, `CALCULATION`, `CHECKLIST`, and `TECHNICAL_STANDARD`, while only `EquipmentDetailScreen.kt` exposed a bookmark toggle.
- **2026-11-16 Batch 02 Status (`DOC-MPN-030`):** Added reactive bookmark toggles in `CalculationRunnerScreen.kt`, `ChecklistScreen.kt`, `ReferenceLibraryScreen.kt`, and `BoilerDashboardScreen.kt` plus canonical/legacy `BOILER_SECTION` routing in `AppNavigation.kt`.
- **Remaining Open Gap:** `EquipmentViewModel.kt` (line `138`) still uses a one-shot `isBookmarked` lookup rather than reactive observation, and `FundamentalsScreen.kt` has no bookmark button. Native runtime remains unverified.

### GAP-04 — Bottom Navigation Bar Label Truncation Bug (`HIGH` -> `SOURCE-IMPLEMENTED IN BATCH 01; RUNTIME UNVERIFIED`)
- **Baseline Evidence (`2026-11-15`):** `AppNavigation.kt:243` used `screen.title.split(" ").first()`, rendering `"Dedicated"` and `"Engineering"`.
- **2026-11-16 Batch 01 Status (`DOC-MPN-029`):** Added explicit `shortTitle` properties on `Screen` (`"Home"`, `"Plant"`, `"Boiler"`, `"Calcs"`) while preserving full `title` for TalkBack/accessibility; runtime layout verification remains pending (`Stage 05 BLOCKED`).

---

## 3. Medium-Priority Feature, Filter & Workflow Gaps

### GAP-05 — Unused `HomeUiState.bookmarks` & Missing Dashboard Shortcuts (`MEDIUM`)
- **Evidence:** `HomeViewModel.kt:50, 67` collects `userDataRepository.getAllBookmarks()` into `HomeUiState.bookmarks`, yet `HomeScreen.kt` never reads `uiState.bookmarks`.
- **What Is Missing:** Planned `PHASE_03 §2` Home dashboard shortcuts for `Checklists`, `Notes`, `Saved Bookmarks`, `Steam Table`, and recent activity/overdue checks are absent from the quick-action grid.

### GAP-06 — Checklist Schema Fields (`remarks`, `completion_timestamp`) Unexposed (`MEDIUM` -> `CORE FIX IN BATCH 03; SECONDARY WORKFLOW OPEN`)
- **Baseline Evidence (`2026-11-15`):** `ChecklistProgressEntity.kt` stored `remarks: String?` and `completionTimestamp: Long?`, unexposed in UI.
- **2026-11-16 Batch 03 Status (`DOC-MPN-031`):** Implemented core data-preservation & UI fixes in `UserDataRepositoryImpl.kt`, `ChecklistViewModel.kt`, `ChecklistScreen.kt`, and `Stage04UiStateAndAccessibilityTest.kt` (`3` `@Test`s / `7` scenarios): toggling/clearing items preserves existing remarks and timestamps, completion timestamps render in UI, and each item has a separate `>= 48.dp` remark button opening a Save/Clear/Cancel dialog. Static `Stage 04` passed `10/10`; JVM JUnit & Android runtime remain unverified.
- **Remaining Open Secondary Items:** No **equipment link** (e.g., jumping from Daily Boiler Pre-Start to `EQ-G01`), no operator/shift filter, and no custom checklist creation. *(Bookmark toggle closed in Batch 02.)*

### GAP-07 — Search Screen Scope & Title Mismatch (`"Search & Bookmarks"`) (`MEDIUM`)
- **Evidence:** `Screen.kt:29` names the module `"Search & Bookmarks"`, yet `SearchScreen.kt` & `SearchViewModel.kt` search only Equipment + static Checklists/References/Fundamentals.
- **What Is Missing:**
  - No button/tab inside `SearchScreen` to open `BookmarksScreen`.
  - No indexing/results for **Calculations (`CALC-01..20`)**, **Personal Field Notes (`user_note`)**, or **Calculation History**.

### GAP-08 — Secondary Modules (`MOD-05..12`) Use Generic Static Overview (`MEDIUM`)
- **Evidence:** `AppNavigation.kt:471–792` routes `Pumps`, `Transmission`, `Steam`, `Water`, `Pneumatics`, `CoolingHeating`, `Etp`, and `Maintenance` to the same 5-card `SecondaryModuleOverviewScreen`.
- **Specific Module Gaps:**
  - `CoolingHeating` (`FAM-K`) links `CALC-10` (Boiler Economizer) rather than a Chiller TR / Cooling Tower approach calculator.
  - `Etp` (`FAM-L`) links `CALC-07` (generic pump power) with no ETP chemical dosing, hydraulic retention time, or BOD/COD calculator.
  - `Maintenance` lacks interactive LOTO lock-out checklist stepper, ISO 20816-3 zone lookup chart, or bearing/lubrication grease equivalence guide (`NLGI EP2` / `ISO VG 46/68/220`).
  - Plant families `FAM-C` (Finishing), `FAM-I` (Chemicals), `FAM-M` (Hoists), `FAM-N` (Workshop), `FAM-O` (Fire/Safety), and `FAM-P` (Ventilation) have no dedicated drawer modules—reachable only via `WashPlant` tabs.

### GAP-09 — Personal Notes Missing `tags_csv`, Search & Equipment Picker (`MEDIUM`)
- **Evidence:** `UserNoteEntity` includes `tags_csv`, whereas `NotesScreen.kt:191–221` editor dialog exposes only `Equipment ID` (raw text box defaulting hardcoded `"EQ-A01"`), `Title`, and `Body`.
- **What Is Missing:** Tags chip editor (`tags_csv`), keyword search / filter by Equipment or Tag, and an Equipment ID dropdown selector.

### GAP-10 — Calculation Runner Missing Unit Switcher, Search & Boiler Dashboard Links (`MEDIUM`)
- **What Is Missing:**
  - `CalculationListScreen.kt` has no keyword/variable search box (`PHASE_03 §3.3`).
  - `CalculationRunnerScreen.kt` accepts fixed scalar units (e.g., generic `unit/h` / `kJ/unit` in `CALC-01/03`) without per-input unit conversion chips (user must leave to `UnitConverterScreen`).
  - `BoilerDashboardScreen.kt` Overview lists only `3` (`CALC-01/05/09`) of the `14` boiler calculations and has no `Steam Table` shortcut button; `Act 2022 & Rules 2025` tab lacks Grade-1/2/3 attendant capacity slab table and OCIB annual certificate renewal reminder tracker.

### GAP-11 — Settings Missing Default Unit Preference & Personal Data Wipe (`MEDIUM`)
- **What Is Missing in `SettingsScreen.kt`:**
  - `PHASE_03 §2` specified `"Unit selection (SI/Metric/Imperial)"` preference in Settings; currently only Theme (`SYSTEM`/`LIGHT`/`DARK`), `1.18x` High Legibility font switch, and JSON Export/Import exist.
  - No `"Clear All Personal Data"` button (with double confirmation) for resetting local user tables.

---

## 4. Low-Priority / Content & Engineering Debt Items

### GAP-12 — Calculation & Fundamentals Engineering Gaps (`CALC-02`, `FUND-11..15`) (`LOW / CONTENT`)
- **`CALC-02` (Indirect Boiler Efficiency):** Intentionally locked (`RESEARCH_PENDING`) in `CalculationRunnerScreen.kt` pending verified mixed Jhut fuel ash/moisture/gas balance calibration (`Stage 2 DOC-MPN-022`).
- **`FUND-11..15`:** `5` of `15` Fundamentals chapters remain `CONTENT_GAP` awaiting full authoring per `Master Project Plan §4.1`.

### GAP-13 — Asset Content Depth (`46/64` Baseline Inventory) & Offline Schematics (`5/64`) (`LOW / CONTENT`)
- **Database Content Distribution:** `2` `FULL_CONTENT_BLUEPRINT` (`EQ-A01`, `EQ-B01`), `9` `PARTIAL_CONTENT_BLUEPRINT`, `7` `RESEARCH_PENDING`, **`46` (`71.9%`) `INVENTORY_ONLY`** (baseline 9-section guidance requiring plant nameplate validation).
- **Offline Vector Schematics:** Only `5` SVG/Vector Drawables exist (`diag_gas_boiler_3pass`, `diag_jhut_boiler_draft`, `diag_belly_washer_drum`, `diag_hydro_extractor_suspension`, `diag_steam_header_trap`), leaving `11` families without dedicated vector diagrams (`EquipmentDetailScreen` shows honest empty-schematic message).

---

## 5. Summary Action Matrix (Synchronized 2026-11-16)

| Priority / Track | Gap IDs | Current Synchronized Status & Next Target |
| :--- | :--- | :--- |
| **1 — Navigation & Discovery (Batch 01)** | `GAP-01`, `GAP-02`, `GAP-04` | **SOURCE-IMPLEMENTED (`DOC-MPN-029`); RUNTIME UNVERIFIED.** Verify in native JVM & Compose runtime on JDK 17 / Android SDK 35 host. |
| **2 — Bookmark Wiring (Batch 02)** | `GAP-03` | **PARTIALLY RESOLVED (`DOC-MPN-030`); RUNTIME UNVERIFIED.** Remaining: convert `EquipmentViewModel.kt:138` one-shot `isBookmarked` lookup to reactive flow; decide/implement `FundamentalsScreen.kt` bookmark support. |
| **3 — Checklist Data Integrity (Batch 03)** | `GAP-06` | **CORE FIX IMPLEMENTED (`DOC-MPN-031`); RUNTIME UNVERIFIED.** Remaining secondary workflow items: equipment link, operator/shift filter, custom checklists. |
| **4 — Open Functional & Search Gaps** | `GAP-05`, `GAP-07..11` | **OPEN.** Implement `HomeScreen` bookmark strip/shortcuts (`GAP-05`), `SearchScreen` indexing/Bookmarks link (`GAP-07`), secondary module specialization (`GAP-08`), Notes `tags_csv`/picker/search (`GAP-09`), calc search/unit switcher/Boiler 14-calc & OCIB tracker (`GAP-10`), and Settings default unit & data wipe (`GAP-11`). |
| **5 — Content, Engineering & Legal Debt** | `GAP-12`, `GAP-13`, `REF-06` RPM Conflict | **OPEN / UNVERIFIED.** Complete `CALC-02`, `FUND-11..15`, equipment depth (`46/64` `INVENTORY_ONLY`) & schematics (`5/64`); resolve `REF-06` (`15,000 RPM` vs `30,000 RPM`); obtain independent legal verification for Bangladesh Boilers Act, 2022 & Rules, 2025. |
| **6 — Native Build & Emulator Pass** | `STAGE 05 BLOCKED` | P0 `chmod +x android/gradlew` completed (`Stage 01` `10/10` static). Compile APK (`./gradlew testDebugUnitTest assembleDebug`) on JDK 17 + Android SDK 35 host (`80` authored `@Test`s across `12` files), verify Compose UI on Emulator, and run NIST REFPROP benchmark before phone install. |

