# STAGE 01: CRITICAL DEFECT REPAIR & VERIFICATION REPORT
**Document Identifier:** DOC-MPN-021  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Authoritative Audit Reference:** `docs/COMPLETE_PROJECT_STATUS_AUDIT.md` (`DOC-MPN-020`)  
**Classification Standard:** Strict Evidence Classification (`COMPLETE` / `PARTIAL` / `BLOCKED` / `UNVERIFIED`)  
**Timestamp:** 2026-11-14  

---

## 1. Defect Remediation Summary (`DEF-AUD-01` through `DEF-AUD-08`)

| Defect ID | Severity (Audit) | Remediation Scope | Code/Static Status | Runtime / Native Toolchain Status |
| :--- | :--- | :--- | :--- | :--- |
| **DEF-AUD-01** | High | Unicode NFC normalization + `[\p{L}\p{M}\p{N}]+` FTS4 query tokenization | **COMPLETE (Code & Python SQLite)** | **UNVERIFIED (Android Room FTS4 / JVM `SearchRepositoryTest` awaits JDK + Device)** |
| **DEF-AUD-02** | High | Truthful equipment-to-schematic & equipment-to-calculator mappings (`EQ-H03`, `FAM-H`, `FAM-G`) | **COMPLETE (Code & Static)** | **UNVERIFIED (JVM `EquipmentMappingTest` & Compose inspection await JDK + Device)** |
| **DEF-AUD-03** | Critical | Official Gradle 8.9 POSIX `gradlew` (`6,122` B, `chmod 755`, `sh -n` pass, wrapper JAR SHA-256 verified) | **COMPLETE (Launcher Script & Checksum)** | **BLOCKED (`java`/`javac`/`kotlinc`/Android SDK 35 absent in container)** |
| **DEF-AUD-04** | Medium-High | `CalculationResultFormatter` (`Locale.US`, small-value precision, trailing-zero stripping, `CALC-09` flags, `DN` labels) | **COMPLETE (Code & Static)** | **UNVERIFIED (JVM `CalculationFormatterAndSteamRangeTest` & UI visual check await JDK + Device)** |
| **DEF-AUD-05** | Medium | Steam table gauge-mode hint (`-0.51 to 18.98 bar(g)`) & error clarity without altering interpolation/grid | **COMPLETE (Code & Static)** | **UNVERIFIED (JVM `CalculationFormatterAndSteamRangeTest` & UI visual check await JDK + Device)** |
| **DEF-AUD-06** | Medium | Zoom `-`/reset/`+` controls (`48.dp`), parent-scroll-safe `awaitEachGesture`, dynamic pan clamp, drafting surface, XML `android:name` tags | **COMPLETE (Code, XML & Static)** | **UNVERIFIED (Compose pinch/pan gesture & screen-reader runtime await Android Device)** |
| **DEF-AUD-07** | Medium | Categorized `BackupImportResult` (`7` failure reasons), `BackupJsonCodec` (`kotlinx-serialization-json`), error UI styling, Room-safe transaction handling | **COMPLETE (Code & Static)** | **UNVERIFIED (JVM `BackupJsonCodecAndCalcRepoTest` & SAF DocumentFile flow await JDK + Device)** |
| **DEF-AUD-08** | Low-Medium | **A:** Theme-aware `SemanticBadgeColors`; **B:** Material `Icons.Default.Warning`; **C:** Stable `LazyColumn` keys; **D:** Room-first `CalculationRepositoryImpl` | **COMPLETE (Code & Static)** | **UNVERIFIED (Dark/Light badge rendering, Room-first test, & scroll stability await JDK + Device)** |

---

## 2. Root Causes Identified & Resolved

1. **`DEF-AUD-01` (Bengali FTS Search):** `SearchRepositoryImpl.buildSafeFtsQuery` matched only `[A-Za-z0-9\u0980-\u09FF]+` or risked splitting Unicode combining marks (`\p{M}`) and never normalized NFD-decomposed keyboard input to NFC before building FTS4 prefix tokens (`token*`).
2. **`DEF-AUD-02` (Equipment Mappings):** `EquipmentDetailScreen.kt` mapped `EQ-H03` (`Industrial Reverse Osmosis Plant`) to `diag_steam_condensate_loop` and `FAM-H` (non-`EQ-H01` assets including `EQ-H03`) to `CALC-08` (`Deaerator Steam Balance`). `CALC-08` belonged in `FAM-G` (Boilers), while `EQ-H03` has no dedicated schematic or calculator in `CALC-01`–`CALC-20`.
3. **`DEF-AUD-03` (`android/gradlew` Broken Launcher):** `android/gradlew` was a short stub rather than the full official Gradle 8.9 POSIX wrapper script, preventing reproducible CLI builds (`./gradlew testDebugUnitTest assembleDebug`) on Linux/macOS developer machines.
4. **`DEF-AUD-04` (Precision & Status Display):** `CalculationRunnerScreen.kt` formatted all numerical outputs with `String.format("%.2f", value)` using default locale—rounding `< 0.005` pressure drops (`0.0024` -> `0.00`, `0.0191` -> `0.02`), showing raw `1.0`/`0.0`/`-1.0` flags for `CALC-09` boiler MCR suitability, and formatting nominal pipe diameter as `100.00 DN`.
5. **`DEF-AUD-05` (Steam Gauge Hint):** `SteamTableScreen.kt` displayed absolute pressure range (`0.50 to 20.00 bar`) even while gauge mode was active (`-0.51 to 18.98 bar(g)`).
6. **`DEF-AUD-06` (Schematic UX & XML Metadata):** `ZoomableSchematicCard` lacked a zoom-out (`-`) button and `semantics { contentDescription }` accessibility labels, used un-clamped `detectTransformGestures` (interfering with parent vertical scrolling at `1x` zoom and allowing off-screen panning), lacked a drafting surface contrast boundary in Dark Mode, and `res/drawable/diag_*.xml` lacked `android:name` structural element identifiers.
7. **`DEF-AUD-07` (Opaque Backup Restore Failures):** `UserDataRepository.importUserPersonalDataJson` returned `Boolean` via `org.json.JSONObject`, collapsing syntax errors, missing required metadata, unsupported app/version, SHA-256 checksum mismatches, and SQLite transaction errors into a single generic failure boolean—and `org.json` stubs are unmocked under plain JVM JUnit tests.
8. **`DEF-AUD-08` (UI Polish & Repository Source of Truth):** Hardcoded light-only hex badge colors (`0xFFE8F5E9`, `0xFFFFF3E0`, `0xFFFFEBEE`) reduced Dark Mode contrast; `SafetyWarningCard` used emoji `"⚠️ "` instead of vector icon; `SearchScreen`, `BoilerDashboardScreen`, and `ReferenceLibraryScreen` omitted stable `LazyColumn` `key` lambdas; `CalculationRepositoryImpl.getCalculationById` skipped Room `calculationDao` and queried `CalculationSpecifications` directly, unlike `getAllCalculations()`.

---

## 3. Files Modified & Created

### Modified Application Files
- `android/gradlew` — Replaced stub with full official Gradle 8.9 POSIX wrapper launcher (`6,122` bytes, `chmod 755`).
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/SearchRepositoryImpl.kt` — Added `Normalizer.Form.NFC`, updated `buildSafeFtsQuery` (`internal`) to `Regex("[\\p{L}\\p{M}\\p{N}]+")`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/equipment/EquipmentDetailScreen.kt` — Removed `EQ-H03` from `diag_steam_condensate_loop`, mapped `EQ-H03 -> emptyList()`, moved `CALC-08` to `FAM-G`, mapped remaining `FAM-H` (`EQ-H02`/`EQ-H04`) to `CALC-07`, exposed `internal` helpers, and rendered explicit empty-state info cards when unmapped.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationRunnerScreen.kt` — Wired `CalculationResultFormatter` and theme-aware `semanticWarningColors`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/steam/IapwsIf97SteamEngine.kt` — Added `formatValidRangeHint(isGaugeMode)` and explicit gauge boundary message (`[-0.51, 18.98 bar g]`).
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/SteamTableScreen.kt` — Wired dynamic `formatValidRangeHint(isGaugeMode)`, fallback error banner, and `semanticPositiveColors` badge.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/theme/Color.kt` — Added `SchematicDraftingSurface`, `SemanticBadgeColors`, `semanticPositiveColors`, `semanticWarningColors`, `semanticDangerColors`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/components/CommonComponents.kt` — Replaced `"⚠️ "` with `Icons.Default.Warning`, upgraded `ZoomableSchematicCard` with `-`/`%`/`+` (`>=48.dp`) accessible controls, `awaitEachGesture` conditional touch interception, dynamic `onSizeChanged` pan clamping, and `SchematicDraftingSurface`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/model/UserDataModels.kt` — Added `BackupImportFailureReason` (`7` reasons) & sealed `BackupImportResult`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/repository/RepositoryInterfaces.kt` — Updated `importUserPersonalDataJson` signature to return `BackupImportResult`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/UserDataRepositoryImpl.kt` — Delegated JSON parsing/signing to `BackupJsonCodec` before entering `database.withTransaction`, catching `SQLiteException`/`IllegalStateException` and unexpected exceptions safely.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/settings/SettingsViewModel.kt` — Added `isImportError` flag and mapped `BackupImportResult` outcomes cleanly.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/settings/SettingsScreen.kt` — Tinted `importMessage` via `MaterialTheme.colorScheme.error` on `state.isImportError`, flagged SAF file I/O errors with `isError = true`, and updated `ComplianceRow` to semantic theme badges.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/CalculationRepositoryImpl.kt` — Updated `getCalculationById` to query `calculationDao` + `getVariablesForCalculation` first with fallback to `CalculationSpecifications`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/calculations/CalculationListScreen.kt` — Updated `CalculationStatusBadge` to `semanticPositiveColors`/`semanticWarningColors`/`semanticDangerColors`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/checklists/ChecklistScreen.kt` — Updated `MANDATORY` badge to `semanticDangerColors`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/search/SearchScreen.kt` — Added `key = { it.id }` to search result `items(...)`.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/boiler/BoilerDashboardScreen.kt` — Added stable `key` lambdas (`all-`, `gas-`, `jhut-`) to all three equipment `items(...)` blocks.
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/presentation/library/ReferenceLibraryScreen.kt` — Added `key = { it.id }` to `items(filteredList)`.
- `android/app/src/main/res/drawable/diag_belly_washer.xml`, `diag_hydro_extractor.xml`, `diag_gas_boiler_3pass.xml`, `diag_jhut_boiler_draft.xml`, `diag_steam_condensate_loop.xml` — Added unique `android:name` attributes (`73` total named `<vector>`/`<path>` elements across all 5 schematics).

### New Application, Test & Verification Files
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/calculation/CalculationResultFormatter.kt`
- `android/app/src/main/java/com/researchbangla/mechanicalpronotes/data/repository/BackupJsonCodec.kt`
- `android/app/src/test/java/com/researchbangla/mechanicalpronotes/SearchRepositoryTest.kt`
- `android/app/src/test/java/com/researchbangla/mechanicalpronotes/EquipmentMappingTest.kt`
- `android/app/src/test/java/com/researchbangla/mechanicalpronotes/CalculationFormatterAndSteamRangeTest.kt`
- `android/app/src/test/java/com/researchbangla/mechanicalpronotes/BackupJsonCodecAndCalcRepoTest.kt`
- `test_suite_phase07.py` — Updated check `P7-P6-03` to inspect `BackupJsonCodec.kt` alongside `UserDataRepositoryImpl.kt`.
- `test_suite_stage01.py` — Added Stage 1 automated verification harness (`10` checks covering `DEF-AUD-01`–`08`, JUnit file presence, and Kotlin comment/string-aware brace balance).

---

## 4. Regression Tests Added

1. **`SearchRepositoryTest.kt` (`DEF-AUD-01`):** Verifies Bengali diacritic preservation (`বয়লার*`, `বেলি* ওয়াশার*`, `হাইড্রো* এক্সট্র্যাক্টর*`, `রিভার্স* অসমোসিস*`), NFD-to-NFC normalization, quoted ID queries (`"EQ-A01"`), blank/punctuation rejection, and `searchLike` fallback on FTS exception.
2. **`EquipmentMappingTest.kt` (`DEF-AUD-02`):** Verifies `EQ-H03` (`Industrial RO Plant`) has `null` schematic and empty calculator list, `EQ-H01` maps to `CALC-19`, `EQ-H02` maps to `CALC-07`, and `FAM-G` boilers include `CALC-08` alongside proper boiler schematics.
3. **`CalculationFormatterAndSteamRangeTest.kt` (`DEF-AUD-04`, `DEF-AUD-05`):** Verifies `0.0191 bar`, `0.0024 bar`, `0.000045`, trailing-zero removal (`85 %`), `Locale.US` grouping (`2,870.5 kg/h`), `CALC-09` status mapping (`1.0`/`0.0`/`-1.0`), `DN 100` formatting, `NaN` handling, `formatValidRangeHint(isGaugeMode)`, and `-0.51..18.98 bar(g)` bounds.
4. **`BackupJsonCodecAndCalcRepoTest.kt` (`DEF-AUD-07`, `DEF-AUD-08-D`):** Verifies signed backup v2 round-trip fidelity, exact failure category mapping (`INVALID_JSON`, `MISSING_REQUIRED_FIELDS`, `UNSUPPORTED_APP`, `UNSUPPORTED_VERSION`, `CHECKSUM_MISMATCH`), zero user-data leakage in failure strings, and Room-first resolution in `CalculationRepositoryImpl`.

---

## 5. Actual Verification Execution Results

All checks executed on **2026-11-14** inside the workspace container:

| Suite / Check | Command | Actual Outcome |
| :--- | :--- | :--- |
| **Phase 04 Suite** | `python3 test_suite_phase04.py` | **10/10 PASS (100.0%)** |
| **Phase 05 Suite** | `python3 test_suite_phase05.py` | **10/10 PASS (100.0%)** |
| **Phase 06 Suite** | `python3 test_suite_phase06.py` | **10/10 PASS (100.0%)** |
| **Phase 07 Suite** | `python3 test_suite_phase07.py` | **27 PASS, 0 FAIL, 1 BLOCKED, 2 PENDING, 2 UNVERIFIED** (`0` open code defects) |
| **Stage 01 Suite** | `python3 test_suite_stage01.py` | **10/10 PASS** (Bengali SQLite FTS4 hits verified, `73` XML `android:name` tags valid, `sh -n android/gradlew` exit `0`, wrapper JAR SHA-256 verified, `62` Kotlin files brace-balanced) |
| **DB Preservation** | `sha256sum .../mechanical_pro_notes.db` | Unchanged SHA-256: `057c02af13cd0b19d16961beb93fead574c84261391290afb5e940c0acb35c4e` |

---

## 6. Remaining Unverified & Blocked Items (Honest Reporting)

### Blocked by Container Environment
- **Android Build & JVM Unit Test Execution:** `java`, `javac`, `kotlinc`, `gradle`, `sdkmanager`, and `adb` are **not installed** in the Linux container (`BLOCKED`). Therefore:
  - `./gradlew testDebugUnitTest` (executing all `6` JUnit test classes including the `4` new Stage 1 regression test classes) has **NOT executed**.
  - `./gradlew assembleDebug` (compiling APK, Compose UI, KSP Room generation, XML vector drawables, and Kolin type checking) has **NOT executed**.

### Unverified Until Executed on Developer PC / Physical Android Phone
1. Native Kotlin/Compose compilation and KSP Room codegen across all `62` Kotlin files.
2. Live execution of all `6` JVM unit test suites (`EngineeringCalculationsTest`, `SteamEngineTest`, `SearchRepositoryTest`, `EquipmentMappingTest`, `CalculationFormatterAndSteamRangeTest`, `BackupJsonCodecAndCalcRepoTest`).
3. On-device Android Room FTS4 Bengali search (`SQLite` device tokenizer behavior vs desktop `sqlite3`).
4. Physical touch interaction (`awaitEachGesture` pinch/pan/scroll coexistence, `48.dp` buttons), Light/Dark theme badge contrast, SAF `CreateDocument`/`OpenDocument` round-trip, and `ChecklistScreen` / `SettingsScreen` visual layout on Android phone (`API 26–35`).
5. Independent NIST REFPROP / CoolProp solver verification of `IapwsIf97SteamEngine` (`P7-P5-03`, Owner Condition 5).

---

## 7. Scope Preservation & Prohibitions Confirmation

- **Engineering Formulas, Constants, Steam Anchors & Interpolation:** Unchanged (`BoilerThermalCalculations.kt`, `WashPlantMaintenanceCalculations.kt`, `CalculationSpecifications.kt`, and `IapwsIf97SteamEngine.kt` `GRID` + linear-pressure interpolation remain byte-identical in mathematical behavior).
- **Pre-Populated SQLite Database:** `android/app/src/main/assets/database/mechanical_pro_notes.db` was **NOT modified** (`544,768` bytes, SHA-256 verified identical).
- **Git & Deployment:** No `.git` repository was initialized, modified, committed, pushed, or deployed; no Stage 2 feature work (Fundamentals/Unit Converter/content expansion) was started.
