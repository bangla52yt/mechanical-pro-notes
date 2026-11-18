# UI COMPLETENESS AUDIT REPORT: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-026  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Scope:** Complete Source-Level UI Inspection Across All 17 Core Modules & 23 Navigation Destinations  
**Evidence Standard:** Strict 6-Tier Honesty Classification (`IMPLEMENTED` / `PARTIAL` / `GENERIC OVERVIEW` / `CONTENT GAP` / `MISSING` / `UNVERIFIED RUNTIME`)  
**Companion Preview Artifact:** `visual-preview-gallery/index.html`  
**Audit Date (Pre-Batch-01 Baseline):** 2026-11-15  
**Checkpoint Synchronization Date:** 2026-11-16  

---

> **Checkpoint Synchronization Update (2026-11-16 — Post-Batch 01–03 & P0 Recovery):**  
> This report was originally authored on `2026-11-15` prior to Batches `01–03`. Source and test authoring for **Batch 01** (`GAP-04`, `GAP-01`, `GAP-02` — `DOC-MPN-029`), **Batch 02** (`GAP-03` — `DOC-MPN-030`), and **Batch 03** (`GAP-06` core remarks & timestamps — `DOC-MPN-031`), plus P0 restoration of executable `android/gradlew` (`Stage 01` `10/10`), have since occurred. All Android Compose UI screens remain **`UNVERIFIED RUNTIME`** (`Stage 05 BLOCKED`: no JDK 17, Android SDK 35, or Emulator/AVD in container), and `GAP-05`, `GAP-03` remainder (`EquipmentViewModel.kt:138`, `FundamentalsScreen.kt`), secondary `GAP-06`, and `GAP-07..13` remain open.

---

## 1. Executive Summary & Classification Legend

This audit inspects every Kotlin navigation route, Jetpack Compose screen, ViewModel state holder, static domain catalog, and pre-populated SQLite asset table directly from source code—without assuming completeness from prior markdown documentation.

### Classification Legend Used in This Report
| Classification | Meaning |
| :--- | :--- |
| **`IMPLEMENTED`** | Dedicated Compose screen exists in source with all intended primary controls, empty/error states, accessibility labels, and wired backend/catalog logic. |
| **`PARTIAL`** | Working dedicated screen exists in source, but specific planned options, filters, schema fields, or interactive shortcuts are missing or buggy. |
| **`GENERIC OVERVIEW`** | Route exists in `AppNavigation.kt` (`MOD-05`–`MOD-12`), rendering a shared 5-card `SecondaryModuleOverviewScreen` summary rather than module-specific interactive tools. |
| **`CONTENT GAP`** | UI renders properly, but underlying technical content is flagged `RESEARCH_PENDING`, `CONTENT_GAP`, or `INVENTORY_ONLY` in code/DB. |
| **`MISSING`** | Capability planned or partially backed by database schema/ViewModel state that has no UI surface anywhere in the app. |
| **`UNVERIFIED RUNTIME`** | Native Android APK compilation, Jetpack Compose layout rendering, Emulator interaction, and physical device testing remain blocked/pending outside the Linux web container (per `DOC-MPN-025`). |

### High-Level Audit Totals (Source & DB Verified)
- **Sealed `Screen` Routes (`Screen.kt`):** **`23` destinations** (`18` `drawerScreens` + `2` `PERSONAL LOGS` drawer routes + `3` parameterized/detail routes `EquipmentDetail`, `CalculationRunner`, `SteamTable`; `UnitConverter` is counted both as utility and inside `drawerScreens`).
- **Bottom Navigation Bar (`AppNavigation.kt:229–256`):** **`4` destinations** (`Home`, `WashPlant`, `BoilerModule`, `Calculations`), shown only on those 4 primary routes.
- **Equipment Database (`mechanical_pro_notes.db`):** **`64` assets** across **`16` system families (`FAM-A`–`FAM-P`)** -> **`2` `FULL_CONTENT_BLUEPRINT` (`EQ-A01`, `EQ-B01`)**, **`9` `PARTIAL_CONTENT_BLUEPRINT`**, **`7` `RESEARCH_PENDING`**, **`46` `INVENTORY_ONLY`** (enriched baseline 9 sections / 603 rows total).
- **Engineering Calculations (`CalculationSpecifications.kt` + DB):** **`20` modules (`CALC-01`–`CALC-20`)** -> **`19` `IMPLEMENTED`**, **`1` `RESEARCH_PENDING` (`CALC-02`)**.
- **Engineering Fundamentals (`EngineeringCatalogs.kt`):** **`15` topics (`FUND-01`–`FUND-15`)** -> **`10` `VERIFIED_BASELINE`**, **`5` `CONTENT_GAP` (`FUND-11`–`FUND-15`)**.
- **Runtime / Emulator Verification:** **`UNVERIFIED RUNTIME`** — Stage 05 verified that JDK 17, Android SDK 35, and an AVD/Emulator are absent in the current Linux container (`DOC-MPN-025`). Source code has `40` `@Preview` functions and `8/8` passing Python static/DB suites, but has not executed on a real Android UI surface yet.

---

## 2. Navigation Graph & Global Shell Audit (`Screen.kt`, `AppNavigation.kt`)

| Component | Source Evidence | Audit Status | Observations |
| :--- | :--- | :--- | :--- |
| **Modal Navigation Drawer** | `AppNavigation.kt:103–227` | **`IMPLEMENTED`** | Renders `18` `PLANT MODULES` (`Home`..`Settings` including `UnitConverter`) + horizontal dividers after `Etp` and `Calculations` + `2` `PERSONAL LOGS` (`Notes`, `Bookmarks`). Width capped `260–304.dp` with `DrawerDefaults.windowInsets`. |
| **Bottom Navigation Bar** | `AppNavigation.kt`, `Screen.kt` | **`IMPLEMENTED (Batch 01)`** | **2026-11-16 Sync:** Updated in Batch 01 (`GAP-04`, `DOC-MPN-029`) to explicit `shortTitle` values (`Home`, `Plant`, `Boiler`, `Calcs`), resolving `"Dedicated"` / `"Engineering"` truncation while preserving full `title` for accessibility/TalkBack. *(2026-11-15 pre-Batch baseline read `screen.title.split(" ").first()`. Runtime rendering still `UNVERIFIED RUNTIME`.)* |
| **Deep / ID Focus Routing** | `AppNavigation.kt:347, 391–402, 430–446` | **`IMPLEMENTED`** | Search results & Bookmarks route directly to exact `EquipmentDetail/{id}`, `CalculationRunner/{id}`, `Checklists` (`selectChecklist`), `Library` (`pendingReferenceFocusId`), and `Fundamentals` (`pendingFundamentalsFocusId`). |
| **Material 3 Theme & Contrast** | `Color.kt`, `Theme.kt` | **`IMPLEMENTED`** | Locked Dark (`#121418`/`#1A1D24`/`#63B3ED`/`#F6AD55`/`#68D391`) & Light (`#F4F6F9`/`#FFFFFF`) palettes + `1.18f` High Legibility font multiplier + luminance-aware semantic badges. (`UNVERIFIED RUNTIME` on OLED device.) |

---

## 3. Module-by-Module Completeness Audit (All 17 Core Modules + 6 Companion Screens)

### A. Core Modules (`MOD-01` through `MOD-17`)

| Module ID & Route | Source File | Source Status | Runtime Status | Implemented Widgets & Actions | Confirmed Gaps / Missing UI Elements |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **MOD-01: `home`**<br>Home Dashboard | `HomeScreen.kt`<br>`HomeViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Drawer button, TopBar Search icon, Search card, Top-Priority Boiler Hub card, 4 quick buttons (`Equipment 64`, `Calculations 20`, `Unit Converter 13`, `Fundamentals 15`), DB depth tiles (`2/9/7/46`), Featured `5` assets list, loading & error notice states. | 1. `HomeUiState.bookmarks` is loaded (`HomeViewModel:50, 67`) but **never rendered** in `HomeScreen.kt`.<br>2. Missing quick tiles for `Checklists`, `Notes`, `Bookmarks`, and `Steam Table` (`PHASE_03 §2`). |
| **MOD-02: `fundamentals`**<br>Mechanical Fundamentals | `FundamentalsScreen.kt`<br>`EngineeringCatalogs.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Honesty notice card (`10` Verified / `5` Gap), discipline group chips (`All` + 5 disciplines), `"Verified Baseline Only"` toggle, expandable topic cards (`FUND-01..15`), clickable chips jumping to linked Equipment/Calcs/Library. | 1. `FUND-11..15` carry `CONTENT_GAP` pending chapter authoring (`Master Plan §4.1`).<br>2. No bookmark toggle button on fundamental topics. |
| **MOD-03: `wash_plant`**<br>Wash Plant Equipment | `EquipmentListScreen.kt`<br>`EquipmentFilter.kt` | **`IMPLEMENTED`** | `UNVERIFIED RUNTIME` | Live search bar, `All (64)` + `FAM-A..P` horizontal chips with family counts, multi-select `ContentDepth` chips (`Full 2`, `Partial 9`, `Inventory 46`, `Pending 7`), Reset filter button, loading/empty states, detail cards. | Underlying asset depth: `46/64` (`71.9%`) remain `INVENTORY_ONLY` baseline depth (`EQ-A01`, `EQ-B01` are full). |
| **MOD-04: `boiler_module`**<br>Dedicated Boiler Module | `BoilerDashboardScreen.kt`<br>`BoilerViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | `ScrollableTabRow` with 4 tabs (`Overview`, `Gas Boilers`, `Jhut Boilers`, `Act 2022 & Rules 2025`), safety card, `CALC-01/05/09` formula launchers, `ZoomableSchematicCard` for Gas (`diag_gas_boiler_3pass`) & Jhut (`diag_jhut_boiler_draft`), FAM-G asset lists, plus Batch 02 TopBar `BOILER_SECTION` bookmark toggle (`DOC-MPN-030`). | 1. ~~Bottom bar `"Dedicated"`~~ -> Closed in Batch 01 (`"Boiler"`).<br>2. Overview lists only `3` of `14` boiler calcs and omits a direct `Steam Table` shortcut button (`GAP-10`).<br>3. Act 2022 / Rules 2025 tab lacks Grade-1/2/3 slab table and personal OCIB renewal expiry tracker (`GAP-10`; statutory claims also require independent legal verification). |
| **MOD-05: `pumps`**<br>Pumps & Fluid Systems | `AppNavigation.kt:678–691` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | `SecondaryModuleOverviewScreen`: LOTO safety warning, Engineering scope + API 610/ISO 5199 note, `CALC-07` formula card, 4 shop-floor bullets, `Browse ... Assets` CTA filtering `WashPlant` to `FAM-D`. | Shares generic template; lacks NPSHa margin chart or pump affinity curve helper screen. |
| **MOD-06: `transmission`**<br>Motors & Transmission | `AppNavigation.kt:692–705` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | LOTO entanglement card, ISO 4184/ISO 281 summary, `CALC-15` formula card, 4 bullets, `Browse ... Assets` -> `FAM-E`. | Shares generic template; no bearing relubrication interval or shaft alignment dial calculator. |
| **MOD-07: `steam`**<br>Steam & Condensate | `AppNavigation.kt:706–720` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | Double-block-and-bleed warning, Spirax Sarco summary, `CALC-14` formula card, **`Open IAPWS-IF97 Saturated Steam Properties` button**, 4 bullets, CTA -> `FAM-F`. | Shares generic template; no interactive steam trap troubleshooting matrix or PRV station checklist. |
| **MOD-08: `water`**<br>Water & Chemical Systems | `AppNavigation.kt:721–734` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | FRP/chemical PPE warning, BS 2486/ABMA targets, `CALC-19` formula card, 4 bullets, CTA -> `FAM-H`. | Shares generic template; no interactive boiler water chemistry target table (hardness/pH/TDS/sulfite). |
| **MOD-09: `pneumatics`**<br>Compressed Air & Pneumatics | `AppNavigation.kt:735–748` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | Receiver bleed warning, ISO 8573-1 summary, `CALC-20` formula card, 4 bullets, CTA -> `FAM-J`. | Shares generic template; no FRL / compressor differential maintenance log. |
| **MOD-10: `cooling_heating`**<br>Cooling & Heating Systems | `AppNavigation.kt:749–762` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | Hot-side first PHE warning, TEMA/CTI summary, `CALC-10` Economizer formula card, 4 bullets, CTA -> `FAM-K`. | Linked calculator `CALC-10` is Boiler Economizer rather than Chiller/Cooling Tower TR or approach calculator. |
| **MOD-11: `etp`**<br>ETP & Wastewater Systems | `AppNavigation.kt:763–776` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | Statutory confined-space H2S warning, DoE ECR 2023 limits summary, `CALC-07` pump power card, 4 bullets, CTA -> `FAM-L`. | Linked calculator is generic pump power (`CALC-07`); zero ETP chemical dosing, retention time, or BOD/COD calculator exists. |
| **MOD-12: `maintenance`**<br>Maintenance Management | `AppNavigation.kt:777–791` | **`GENERIC OVERVIEW`** | `UNVERIFIED RUNTIME` | LOTO 6-step summary, ISO 20816-3/ISO 55000 scope, `CALC-16` hydro check, 4 bullets, CTA -> `Checklists`. | Shares generic template; lacks interactive LOTO lock log, ISO 20816-3 zone lookup widget, or oil viscosity cross-reference table. |
| **MOD-13: `checklists`**<br>Inspection & Checklists | `ChecklistScreen.kt`<br>`ChecklistViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | 4 pre-seeded checklists (`25` total items), tab selector, Room persistence, progress fraction bar, Reset confirmation dialog, plus Batch 02 TopBar `CHECKLIST` bookmark toggle (`DOC-MPN-030`) and Batch 03 `GAP-06` core fix (`UserDataRepositoryImpl.kt`, `ChecklistViewModel.kt`, `ChecklistScreen.kt` preserving existing remarks/timestamps, formatting `completionTimestamp`, and exposing `>= 48.dp` remark edit button + Save/Clear/Cancel dialog — `DOC-MPN-031`). | Secondary `GAP-06` items remain open: no direct equipment ID link, no operator/shift filter, and no custom checklist/item creation. |
| **MOD-14: `calculations`**<br>Engineering Calculations | `CalculationListScreen.kt`<br>`CalculationViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | TopBar Unit Converter, Steam Table & **Calculation History Viewer (`ModalBottomSheet`)**, dynamic category filter chips (`Batch 01` — `GAP-01`/`GAP-02`, including `Utility & Pumping`, `Boiler Safety`, `Wash Plant Utility`), bottom bar `"Calcs"`, 20 calc cards. | No formula/variable search bar (`GAP-10`). (`CALC-02` remains `RESEARCH_PENDING`.) |
| **MOD-15: `library`**<br>Technical Reference Library | `ReferenceLibraryScreen.kt`<br>`EngineeringCatalogs.kt` | **`IMPLEMENTED`** | `UNVERIFIED RUNTIME` | All `12` verified references (`REF-01..12`), `5` category chips, authority headers, mandatory requirements boxes, deep-link highlight scroll, plus Batch 02 `TECHNICAL_STANDARD` bookmark toggle (`DOC-MPN-030`). | Unresolved engineering discrepancy: `REF-06` (`EngineeringCatalogs.kt:170`) states `120 RPM and 15,000 RPM` vs `FUND-05` (`line 335`) `120–30,000 RPM`. Statutory Act 2022/Rules 2025 text requires independent legal verification. |
| **MOD-16: `search`**<br>Search & Bookmarks | `SearchScreen.kt`<br>`SearchViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Multi-language (English + Bengali NFC) offline search across Equipment (`FTS4`/`LIKE`) + static Checklists, References & Fundamentals; Scope, Family, and Depth filter chips; click-through routing. | 1. Title says `"Search & Bookmarks"` yet offers **no button/tab to Bookmarks**.<br>2. **Does NOT index `Calculations (CALC-01..20)`, `User Notes`, or `Calculation History`**. |
| **MOD-17: `settings`**<br>Settings & Local Data | `SettingsScreen.kt`<br>`SettingsViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | App identity card, `3` Privacy/Offline compliance cards (`DEC-016`, `DEC-014`, `DEC-002`), Theme selector (`SYSTEM`/`LIGHT`/`DARK`), `1.18x` High Legibility switch, personal item counts, SAF JSON v2 SHA-256 export/import + overwrite switch. | 1. Phase 03 planned **SI/Metric/Imperial default unit selector** is missing.<br>2. `"Calcs"` history count is shown (`line 333`), yet no screen can view or delete those saved runs.<br>3. No `"Clear All Personal Data"` reset button. |

### B. Detail, Utility & Personal Companion Destinations (`6` Routes)

| Destination Route | Source File | Source Status | Runtime Status | Summary of Capabilities & Missing Pieces |
| :--- | :--- | :--- | :--- | :--- |
| **`equipment_detail/{equipmentId}`** | `EquipmentDetailScreen.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | 5 tabs (`Overview`, `Specs`, `Operation`, `Safety/FMEA`, `Maintenance`), TopBar Bookmark toggle, `Add Personal Field Note` prefill button, linked calculation launcher, `RESEARCH_PENDING`/`INVENTORY_ONLY` transparency banners, and `ZoomableSchematicCard` on `5` diagrams. Gap: `59/64` assets have no schematic vector; `46/64` are baseline `INVENTORY_ONLY`. |
| **`calculation_runner/{calcId}`** | `CalculationRunnerScreen.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Dynamic `formula_variable` inputs, validation, formulas/assumptions/limitations cards, `Steam Table` shortcut, `Save to Personal History`, plus Batch 02 TopBar `CALCULATION` bookmark toggle (`DOC-MPN-030`). Gaps: `CALC-02` locked (`RESEARCH_PENDING`), **no per-input unit switcher**, and History viewer lives on `CalculationListScreen` rather than inline here. |
| **`steam_table`** | `SteamTableScreen.kt` | **`IMPLEMENTED`** | `UNVERIFIED RUNTIME` | Offline IAPWS-IF97 saturation lookup (`0.5–20.0 bar(a)` / `-0.51..18.98 bar(g)`), `bar(g)`/`bar(a)` switch, preset chips, linear-P interpolation across `56` grid anchors. Pending: external NIST REFPROP verification on target hardware. |
| **`unit_converter`** | `UnitConverterScreen.kt`<br>`UnitConverterStateHolder.kt` | **`IMPLEMENTED`** | `UNVERIFIED RUNTIME` | Complete `13`-category converter with Swap button, physical vacuum (`< 0 bar(a)`) and `< 0 K` guards, Speed (linear vs rotational) & Viscosity (dynamic vs kinematic) dimension isolation, compact adaptive selector layout. |
| **`notes?equipmentId={id}`** | `NotesScreen.kt`<br>`NotesViewModel.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Add/Edit/Delete `UserNoteEntity` cards persisted in Room with clickable Equipment ID chip jumping to `EquipmentDetail`. Gaps: `tags_csv` DB column is never exposed in dialog; no search/tag filter; raw Equipment ID text field defaults hardcoded to `"EQ-A01"` without searchable dropdown. |
| **`bookmarks`** | `BookmarksScreen.kt` | **`PARTIAL`** | `UNVERIFIED RUNTIME` | Renders & routes all `5` `BookmarkEntityType` enums (`EQUIPMENT`, `BOILER_SECTION`, `CALCULATION`, `CHECKLIST`, `TECHNICAL_STANDARD`) with delete support. **2026-11-16 Sync (Batch 02):** `EquipmentDetail`, `BoilerDashboard`, `CalculationRunner`, `Checklist`, and `ReferenceLibrary` now create/toggle bookmarks (`DOC-MPN-030`); remaining `GAP-03` gaps are `EquipmentViewModel.kt:138` one-shot `isBookmarked` lookup and missing bookmark toggle on `FundamentalsScreen.kt`. |

---

## 4. Database Schema vs. UI Reachability Matrix

| Room Table (`AppDatabase.kt`) | Seeded / Stored Rows | UI Read Path | UI Create/Edit/Delete Path | Audit Verdict |
| :--- | :--- | :--- | :--- | :--- |
| `system_family` | `16` | `WashPlant`, `Search` | Read-only static | **`IMPLEMENTED`** |
| `equipment` / `equipment_fts` | `64` | `Home`, `WashPlant`, `Boiler`, `Detail`, `Search` | Read-only static | **`IMPLEMENTED`** (`46` `INVENTORY_ONLY` depth) |
| `knowledge_section` / `component` / `technical_parameter` / `failure_mode` / `maintenance_procedure` | `603` / `138` / `132` / `117` / `64` | `EquipmentDetailScreen` (5 tabs) | Read-only static | **`IMPLEMENTED`** |
| `calculation` / `formula_variable` | `20` / `79` | `Calculations`, `CalculationRunner`, `Boiler`, `Secondary` | Read-only static (`CALC-02` locked) | **`IMPLEMENTED (Batch 01)`** (Dynamic category filter fixed; runtime unverified) |
| `user_note` | `0` (user private) | `NotesScreen`, `Settings` count, JSON Export | `NotesScreen` CRUD (`tags_csv` hidden) | **`PARTIAL`** |
| `user_bookmark` | `0` (user private) | `BookmarksScreen`, `Settings` count, (`HomeViewModel` unused on `HomeScreen`) | All `5` enum types now creatable (`Batch 02` — `DOC-MPN-030`); `EquipmentViewModel:138` reactive gap & `FundamentalsScreen` remain | **`PARTIAL`** |
| `checklist_progress` | `0` (user private) | `ChecklistScreen` (`isCompleted`, formatted timestamp, `remarks` — `Batch 03`), `Settings` count | Checkbox toggle, reset & `remarks` Save/Clear dialog (`Batch 03` — `DOC-MPN-031`; runtime unverified) | **`PARTIAL`** *(Core data-loss/UI fixed; secondary workflow items open)* |
| `calculation_history` | `0` (user private) | `CalculationListScreen` History `ModalBottomSheet` (`Batch 01`), `Settings` count, JSON backup | Insert (`CalculationRunnerScreen`) + View/Delete (`CalculationListScreen` — `Batch 01`; runtime unverified) | **`IMPLEMENTED (Batch 01)`** |

---

## 5. Pre-Installation Preview Verification Evidence (`visual-preview-gallery/index.html`)

- **Artifact Created:** `/visual-preview-gallery/index.html` (`135,884` bytes, pure HTML/CSS/JS with **`0` external HTTP requests**).
- **Port 3000 Serving Probe:** Tested live against `http://localhost:3000/visual-preview-gallery/` and `http://localhost:3000/visual-preview-gallery/index.html` -> both returned **HTTP `200 OK`** directly from the running Vite dev server (`IS_GALLERY: True`). *(Note: for offline static sharing or `vite build` bundles, copy/open `visual-preview-gallery/index.html` directly because Vite root builds exclude non-`public/` subfolders unless explicitly served.)*
- **JS Syntax & DOM Smoke Check:** Passed `node --check` (`exit 0`) and Node `vm` render sweep across **all `24` gallery routes** (`Home`, `Fundamentals`, `WashPlant`, `EquipmentDetail`, `BoilerModule`, `Pumps..Maintenance`, `Checklists`, `Calculations`, `CalculationRunner`, `SteamTable`, `UnitConverter`, `Library`, `Search`, `Settings`, `Notes`, `Bookmarks`, and `Audit Matrix`), confirming live interactive unit conversions, guardrails, and steam interpolation inside the preview gallery without touching a single Android Kotlin/DB/Gradle file.
