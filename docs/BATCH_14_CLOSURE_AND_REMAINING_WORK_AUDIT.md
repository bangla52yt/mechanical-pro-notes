# BATCH 14 CLOSURE, NATIVE VERIFICATION & COMPLETE REMAINING WORK AUDIT

**Document Identifier:** DOC-MPN-043  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Milestone Evaluated:** Batch 14 — Engineering Standards (`REF-06`), Bangladesh Boiler Statutory References & `IapwsIf97SteamEngine`  
**Audit Date:** 2026-11-18  
**Audit Mode:** Controlled Verification + Full Project Gap Discovery (`Project Integrity > Engineering Accuracy > Verification > Feature Expansion`)  
**Workspace Constraints Preserved:** Zero `INTERNET` permission (`DEC-016`), Room `version = 1`, `BackupJsonCodec` `backup_version = 2`, User-Data Isolation (`DEC-014`), Free-First Policy (`DEC-002`), Zero `git commit`/`push`/`deploy`.

---

## 1. Executive Summary

- **Batch 14 Engineering Accuracy Verdict:** **VERIFIED IN DOMAIN CONTENT & MATH, SUBJECT TO EXTERNAL DEPENDENCIES.** Independent primary web checks and numerical thermodynamic verification confirm that Batch 14's engineering corrections are genuine and internally consistent:
  1. **`REF-06` / `ISO 20816-3:2026`:** Verified directly against official standards notices (`sis.se`, `kssn.net`) that `ISO 20816-3:2022` has been officially withdrawn and replaced by `ISO 20816-3:2026`, with `120–30,000 r/min` (*in situ*, machines `> 15 kW`), Rigid vs. Flexible Group 1/2 zones, and pump/reciprocating/rotary-PD exclusions implemented consistently across `EngineeringCatalogs.kt`, `EquipmentDetailScreen.kt`, and `mechanical_pro_notes.db`.
  2. **Bangladesh Boiler Statutory Law:** Verified directly against `bdlaws.minlaw.gov.bd`, `dpp.gov.bd`, and `vdb-loi.com` for **The Boilers Act, 2022 (`Act No. 06 of 2022`, `11 April 2022`)** Sections `9, 11, 13, 18, 24, 28, 36, 38, 42, 44, 54` and **Bangladesh Boiler Rules, 2025 (`S.R.O. No. 279-Law/2025`, published `29 June 2025`, `69 Rules`)** including `Rule 3` lifespan limits (`10/15/30/60 yr + 10 yr`) and proof that `Rule 44` governs examination fee cancellation (confirming Batch 14's purge of prior fabricated `"Rule 44 daily blowdown"` and `"Sec 11 cert"` claims).
  3. **`IapwsIf97SteamEngine.kt`:** Exact IAPWS-IF97 Region 4 Eq. 31 $T_{sat}(P)$, log-pressure $v_g$, and $h_g \equiv h_f + h_{fg}$ across all `56` strictly monotonic grid nodes (`[0.50, 20.00 bar a]`) match independent Wagner–Pruss IAPWS-95 $P_{sat}(T)$ to within **`0.017872%`** and Clausius–Clapeyron $h_{fg}$ thermodynamic consistency to within **`0.0454%`** across a 501-point sweep (`0.0460%` max across grid anchors).
- **Native Build & Execution Verdict:** **BLOCKED IN CONTAINER + REPRODUCIBLE WRAPPER PERMISSION DEFECT.**
  - `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `java`, `javac`, `kotlinc`, `gradle`, `adb`, `sdkmanager`, `avdmanager`, `emulator`, and `android/local.properties` are all absent in this container (`4.19.0-gvisor`).
  - Furthermore, **`android/gradlew` (`6,123` bytes) currently lacks the POSIX execute bit (`-rw-r--r--`)**, failing `./gradlew --version` with `Permission denied` (`EXIT_CODE=126`). This directly contradicts prior claims in `CURRENT_WORK_CHECKPOINT.md` and `STAGE_05_EMULATOR_PREVIEW_READINESS_REPORT.md` stating `-rwxr-xr-x`.
  - None of the `125` authored JVM JUnit `@Test` methods, KSP/Room compilation (`android/app/schemas/` absent), Android Lint, Release ProGuard APK build, or Android Emulator UI tests have ever been executed.
- **Full-Project Readiness:** Source architecture (`64` main `.kt` files, `12` test `.kt` files, `42` `@Preview` composables, `18` offline `diag_*.xml` vector schematics, `856,064`-byte SQLite asset with `64` equipment / `20` calculations / `87` formula variables) is cohesive, offline-safe, and free of syntax/`TODO`/`FIXME` traps, **but release readiness requires unblocking P0 native verification on a JDK 17 + Android SDK 35 host** plus resolving documented content, backup-portability, and test-harness drift items below.

---

## 2. Batch 14 Scope Baseline (`DOC-MPN-042` & Changed Artifacts)

Batch 14 targeted three engineering remediation areas documented in `docs/BATCH_14_IMPLEMENTATION_REPORT.md` (`DOC-MPN-042`):
1. **Objective A (`REF-06`):** Resolve `15,000` vs `30,000 RPM` conflict in `ISO 20816-3` across `EngineeringCatalogs.kt`, `EquipmentDetailScreen.kt`, `EquipmentMappingTest.kt`, `mechanical_pro_notes.db`, and docs.
2. **Objective B (Bangladesh Boiler Statutory References):** Verify and align `The Boilers Act, 2022 (Act No. 06 of 2022)` and `Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)` across `EngineeringCatalogs.kt`, `BoilerDashboardScreen.kt`, `ChecklistViewModel.kt`, `SearchScreen.kt`, `seed_equipment_part1.py`, `seed_equipment_part2.py`, `build_database.py`, `mechanical_pro_notes.db`, and docs, while purging false rules/dates (`Rule 44`, `Sec 11 cert`, wrong `Sec 13/24/55`, unverified `24h` window, `13 April 2022`, `২০১৫` typo).
3. **Objective C (`IapwsIf97SteamEngine`):** Implement exact Region 4 Eq. 31 $T_{sat}(P)$, log-pressure `v_g`, finite/atmospheric bounds checks, and unsupported-region documentation in `IapwsIf97SteamEngine.kt`, `SteamTableScreen.kt`, `SteamEngineTest.kt`, and `docs/PHASE_06_STEAM_TABLE_ENGINE.md`.

---

## 3. Native Android Build & Test Attempt Log

Per controlled verification protocol, all native build and verification tools were probed directly in `/app/applet` and `/app/applet/android`:

| Check / Command | Outcome | Exit Code | Observed Output / Finding |
|---|---|---|---|
| `command -v java javac kotlinc gradle adb emulator sdkmanager avdmanager` | `BLOCKED_ENVIRONMENT` | N/A | All `8` binaries returned `NOT FOUND`. `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT` are `UNSET`. `android/local.properties` and `android/gradle.properties` are absent. |
| `ls -la android/gradlew android/gradle/wrapper/gradle-wrapper.jar` | `DEFECT_CONFIRMED` | `0` | `android/gradlew` = `-rw-r--r--` (`6,123` B, lacks `+x`); `gradle-wrapper.jar` = `43,504` B; `gradle-wrapper.properties` = `250` B (`gradle-8.9-bin.zip`). |
| `(cd android && ./gradlew --version)` | `DEFECT_CONFIRMED` | `126` | `bash: ./gradlew: Permission denied` (fails even before checking `JAVA_HOME`). |
| `./gradlew testDebugUnitTest` (`125` `@Test`s) | `BLOCKED_ENVIRONMENT` | N/A | Cannot run without JDK 17 + `+x` on `android/gradlew`. |
| `./gradlew assembleDebug` / `lintDebug` | `BLOCKED_ENVIRONMENT` | N/A | Cannot run without JDK 17 + Android SDK 35 (`platforms;android-35`, `build-tools;35.0.0`) + Gradle dependency cache. |
| `ls android/app/schemas` / `android/app/src/androidTest` | `PLANNED_NOT_IMPLEMENTED` | N/A | Neither directory exists; KSP has never generated Room schema JSONs and zero instrumentation tests exist. |
| Python scientific packages (`iapws`, `CoolProp`, `scipy`, `numpy`) | `BLOCKED_ENVIRONMENT` | N/A | All `4` third-party solvers `NOT_INSTALLED`; Python `3.11` standard `sqlite3` and `math` are available. |

### Exact Android Studio & CLI Steps Required Outside Container (Host PC)
1. **Prerequisites:** Install **JDK 17** (`JAVA_HOME` configured) and **Android Studio Ladybug (2024.2+)** or **Android SDK Platform 35 + Build-Tools 35.0.0 + Platform-Tools (`adb`)**, plus an **API 34/35 Emulator AVD** (first Gradle sync requires internet access on the build host to populate `~/.gradle` from `google()`, `mavenCentral()`, and `gradlePluginPortal()`).
2. **Restore Wrapper Permission & Verify Local SDK Path:**
   ```bash
   cd android
   chmod +x gradlew
   ./gradlew --version
   ```
   *(On Android Studio open, allow Studio to write gitignored `android/local.properties` with `sdk.dir=...`.)*
3. **Run JVM Tests, Compilation & Lint:**
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```
4. **Verify Artifacts Generated:**
   - `android/app/schemas/com.researchbangla.mechanicalpronotes.data.local.database.MechanicalProDatabase/1.json` (KSP export)
   - `android/app/build/reports/tests/testDebugUnitTest/index.html` (`125` `@Test`s)
   - `android/app/build/reports/lint-results-debug.html`
   - `android/app/build/outputs/apk/debug/app-debug.apk`
5. **Emulator / Physical Device Checks (Impossible Headlessly):**
   - `adb shell svc wifi disable && adb shell svc data disable` -> `adb install -r app/build/outputs/apk/debug/app-debug.apk`
   - Live Compose rendering (`Home`, `WashPlant`, `Boiler`, `Calculations`, `SteamTable`, `EquipmentDetail` all `18` `diag_*.xml` zooms, `Settings`, `Checklists`, `Notes`, `Bookmarks`, `Search` FTS4), rotation, IME keyboard overlap, Dark/Light & `1.18x` contrast, TalkBack focus order, `48.dp` gloved touch ergonomics, and Android SAF JSON backup/restore via Downloads document provider.

---

## 4. Independent Batch 14 Engineering & Standards Validation

### 4.1 Steam Property Engine (`IapwsIf97SteamEngine.kt`, `SteamTableScreen.kt`, `SteamEngineTest.kt`)

| Verification Check | Method & Sample Scope | Measured Result | Verdict |
|---|---|---|---|
| **GRID Shape & Monotonicity** | Parsed `GRID` array directly from `IapwsIf97SteamEngine.kt:71-128` | `56` rows × `7` cols; `P`, `T_sat`, `h_f`, `v_f` strictly increasing; `h_fg`, `v_g` strictly decreasing | **PASS** |
| **IF97 Region 4 Eq. 31 ($T_{sat}$)** | Executed exact IAPWS-IF97 Eq. 31 (`n1..n10`) across all `56` nodes & `501`-pt sweep `[0.50..20.00 bar a]` | Max `|GRID[i][1] - Eq.31| = 0.004757 °C` (2-decimal table rounding); continuous engine deviation `0.000000 °C` | **PASS** |
| **IAPWS-95 Vapor-Pressure Consistency** | Independent Wagner–Pruss (1993) $P_{sat}(T_{sat})$ check across `56` anchors & `501` points | Max relative pressure deviation `= 0.017872%` (at `11.8100 bar a`; grid nodes `0.017870%`) | **PASS** |
| **Clausius–Clapeyron Consistency** | Independent numerical differentiation $h_{fg,CC} = T_K (v_g - v_f) \frac{dP_{sat}}{dT}$ via IAPWS-95 across all `56` nodes & `501` points | Max deviation across `56` nodes `= 0.0460%` (`0` outliers `> 0.15%`); max across `501` intermediate points `= 0.0454%` (at `18.0110 bar a`) | **PASS** |
| **Enthalpy Identity ($h_g = h_f + h_{fg}$)** | Checked `GRID[i][4]` and runtime formula `val hg = hf + hfg` (`line 178`) | `GRID` rounded column differs by `<= 0.050 kJ/kg`; runtime enforces exact `hg = hf + hfg` | **PASS** |
| **Safety Guards & Range Hints** | Code inspection of `lines 147–224` & `SteamTableScreen.kt` | `isFinite()`, `atmosphericBar > 0.0`, bounds `0.50..20.00 bar(a)` / `-0.51325..18.98675 bar(g)` enforced; unsupported regions documented (`lines 20-26`) | **PASS** *(Low-severity edge note below)* |

- **Low-Severity Defect Noted (`E-03`):** In `IapwsIf97SteamEngine.kt:212`, if a caller passes a custom `atmosphericBar` != `1.01325`, the bounds check at lines 209–210 uses `minGauge`/`maxGauge`, **but** the exception message hardcodes `[-0.51, 18.98 bar g]` (`SteamTableScreen.kt` only uses default `1.01325 bar`, so default tests in `CalculationFormatterAndSteamRangeTest.kt:56` are unaffected). Also `GRID[i][1]` and `GRID[i][4]` are unreferenced dead columns at runtime.
- **External Boundary Preserved:** Physical comparison against external **NIST REFPROP / CoolProp** remains **UNVERIFIED** in container (`CoolProp`/`iapws` `NOT_INSTALLED`; Owner Condition 5).

### 4.2 Bangladesh Boiler Law Verification (`Act No. 06 of 2022` & `S.R.O. No. 279-Law/2025`)

Verified via web search against official Bangladesh Ministry of Law (`bdlaws.minlaw.gov.bd`), Department of Printing & Publications (`dpp.gov.bd`), and regulatory legal summaries (`vdb-loi.com`):

| Citation in App (`EngineeringCatalogs`, `BoilerDashboard`, `Checklist`, `DB`) | Independent Web Evidence | Verdict |
|---|---|---|
| **The Boilers Act, 2022 (`Act No. 06 of 2022`)** enacted `11 April 2022` | Verified on `bdlaws.minlaw.gov.bd`; repeals `The Boilers Act, 1923 (Act No. V of 1923)` under **Sec 54** | **VERIFIED** |
| **Sec 9 & Sec 36** (Boiler operator license; penalty up to 2 yrs / 2 lakh BDT) | Verified against `bdlaws.minlaw.gov.bd` (`সকল` boiler operation requires valid license; violation punished under Sec 36) | **VERIFIED** |
| **Sec 11** (`Cancellation of boiler operation license` upon show cause) | Confirms Batch 14's removal of erroneous Batch 13 `"Sec 11 cert"` claim | **VERIFIED** |
| **Sec 13** (`3 boiler categories` defined with capacity, steam pressure, lifespan fixed by Rules) | Verified against `bdlaws.minlaw.gov.bd` (`Electrode`, `Small Industrial`, `Industrial`) | **VERIFIED** |
| **Sec 18–19 & Sec 38** (Mandatory registration & certificate for boiler use) | Verified against `bdlaws.minlaw.gov.bd` | **VERIFIED** |
| **Sec 24 & Sec 42** (Boiler repair authorization/conditions; up to 1 yr / 1 lakh BDT) | Verified against `bdlaws.minlaw.gov.bd` | **VERIFIED** |
| **Sec 28 & Sec 44** (Immediate accident reporting *তৎক্ষণাৎ*, without unverified `24h` claim) | Verified (`BoilerDashboardScreen.kt:388` omits bogus `24h` window) | **VERIFIED** |
| **Bangladesh Boiler Rules, 2025 (`S.R.O. No. 279-Law/2025`)** signed `26 June 2025`, Gazette `29 June 2025`, `69 Rules` | Verified via `dpp.gov.bd` / `vdb-loi.com` summary | **VERIFIED** |
| **Rule 3 Lifespans** (Electrode `<=300L, <=7 kg/cm², 10 yr`; Small Industrial `<=500L, <=7 kg/cm², 15 yr`; Packaged `30 yr`; Field-erected `60 yr`; `+10 yr` extension subject to Board approval & NDT) | Verified verbatim against published `S.R.O. No. 279-Law/2025` Rule 3 summary | **VERIFIED** |
| **Rule 44 Purge** (Removal of old `"Rule 44 daily blowdown"`) | Verified `Rule 44` actually governs examination fee forfeiture/cancellation (*পরীক্ষার ফি বাতিল*) | **VERIFIED** |
| **Global Text Audit** (`Rule 44`, `Sec 11`, `24h`, `১৩ এপ্রিল`, `২০১৫`) | `0` stale occurrences across all `64` `.kt` files (`EngineeringCatalogs.kt:155` `২০১৫` is valid `Bangladesh Labour Rules, 2015`) & `0` stale hits across all `16` DB tables | **CLEAN** |

- **External Boundary:** Full verbatim Bengali Gazette PDF (`57958_89222.pdf`) for the remaining ~67 minor procedural rules (attestation fee schedules, attendant capacity slabs, exact periodic hydro renewal procedures) was not retrieved inline and remains flagged transparently.

### 4.3 `ISO 20816-3:2026` (`REF-06`, `FUND-05`, `EquipmentDetailScreen`, DB)

Verified directly against official standards publication records (`sis.se`, `kssn.net`):
- **`ISO 20816-3:2026` Status:** Genuine current edition replacing withdrawn `ISO 20816-3:2022` (which had replaced `ISO 10816-3` / `ISO 7919-3`).
- **Scope Accuracy:** Machines with nominal power **`> 15 kW`** and nominal operating speeds **`120–30,000 r/min`** measured *in situ*; Group 1 (`> 300 kW` or electrical shaft height $\ge 315\text{ mm}$) & Group 2 (`15–300 kW` or `160–315 mm`); Rigid vs. Flexible supports; Criterion I (Zones A–D) & Criterion II (baseline magnitude change); explicit exclusions (`<= 15 kW`, `< 120 r/min`, `ISO 10816-7` rotodynamic pumps, `ISO 10816-6`/`20816-8` reciprocating, rotary positive-displacement screw/lobe machines, static pressure vessels). **All checks PASS.**

### 4.4 Database Integrity, Parity & Content Audit (`mechanical_pro_notes.db`)

Directly verified via read-only `sqlite3` queries on `android/app/src/main/assets/database/mechanical_pro_notes.db`:
- **File Metadata:** `856,064` bytes | `PRAGMA user_version = 1` | SHA-256: `fe2fb5f9c47cabc2abb56e2873b3531b1b1512d2f4e09df6cadaef1791b90720`
- **Structural Health:** `PRAGMA integrity_check = [('ok',)]` | `PRAGMA foreign_key_check = []` | User personal tables (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`) = `0` rows each.
- **Asset & Knowledge Coverage (`64/64` across `16/16` families `FAM-A..P`):**
  - `56` `FULL_CONTENT_BLUEPRINT` (`EQ-A01`, `EQ-B01` with `21` sections; `54` others with `15–19` sections alongside normalized `equipment_subtype`/`technical_parameter`/`component`/`failure_mode`/`maintenance_procedure` tables)
  - `1` `PARTIAL_CONTENT_BLUEPRINT`: `EQ-G02` (Jhut / Textile Waste-Fuel Steam Boiler)
  - `7` `RESEARCH_PENDING`: `EQ-A05` (Barrier Washer), `EQ-B04` (Gas Tumbler Dryer), `EQ-B05` (RF/Tunnel Dryer), `EQ-C04` (Ozone Fading), `EQ-C05` (Laser Denim Marking), `EQ-H03` (Industrial RO Plant), `EQ-K02` (Industrial Water Chiller)
  - `0` `INVENTORY_ONLY`
- **Static Content Totals:** `1,190` `knowledge_section` | `209` `component` (`CONSUMABLE`, `STRUCTURAL_PERMANENT`, `WEAR_PART`) | `206` `technical_parameter` | `130` `failure_mode` | `128` `maintenance_procedure` | `14` `equipment_subtype` | `22` `equipment_category` | `16` `system_family` | `64` `equipment_fts`.
- **Calculations Parity:** `20` `calculation` rows (`20/20` `IMPLEMENTED`) and `87` `formula_variable` rows match `CalculationSpecifications.kt` (`20` calcs, `87` `FormulaVariable` entries) 100%.
- **FTS4 English & Bengali Queries:** Verified active returns (`boiler`=`4`, `pump`=`15`, `compressor`=`2`, `বয়লার*`=`3`, `হাইড্রো*`=`3`, `পাম্প*`=`9`).

---

## 5. Full Project Gap Discovery Summary (Categories A–H)

Audit coverage across all 8 project domains revealed **31 tracked items** (`REM-A01`–`REM-H04`):
- `COMPLETE_VERIFIED`: **3** (`REM-B04`, `REM-D01`, `REM-G02` — verified against primary external standards/laws and codebase/DB architecture; UI rendering covered separately under `REM-F01`)
- `COMPLETE_NOT_INDEPENDENTLY_VERIFIED`: **3** (`REM-B03`, `REM-C02`, `REM-C04`)
- `PARTIALLY_IMPLEMENTED`: **4** (`REM-A06`, `REM-C01`, `REM-F04`, `REM-G01`)
- `PLANNED_NOT_IMPLEMENTED`: **7** (`REM-A04`, `REM-A05`, `REM-B05`, `REM-C03`, `REM-E02`, `REM-F02`, `REM-H04`)
- `BLOCKED_EXTERNAL_DEPENDENCY`: **3** (`REM-B01`, `REM-B02`, `REM-D02`)
- `BLOCKED_ENVIRONMENT`: **6** (`REM-A01`, `REM-A03`, `REM-E01`, `REM-F01`, `REM-H01`, `REM-H02`)
- `DEFECT_CONFIRMED`: **5** (`REM-A02` `gradlew` missing `+x`, `REM-E03` custom-atm Steam error hint, `REM-F03` stale `visual-preview-gallery/index.html`, `REM-G03` legacy `seed_data.sql` trap, `REM-H03` drifted legacy `test_suite_*.py` scripts)

---

## 6. Complete Itemized Remaining-Work Register (Categories A–H)

| ID | Category | Title / Description | Status | Evidence | Impact / Risk | Recommended Fix / Action | Priority |
|---|---|---|---|---|---|---|---|
| **REM-A01** | **A — Core App** | Native Android Debug Compile & Build Verification (`assembleDebug`) | `BLOCKED_ENVIRONMENT` | Container lacks JDK 17 (`java`/`javac`/`kotlinc` `NOT FOUND`), Android SDK 35 (`sdkmanager`/`adb` absent), `android/local.properties`. | Kotlin/KSP/Compose/Resource compiler issues remain undetected; no installable `.apk` exists. | Run `./gradlew assembleDebug` on JDK 17 + Android SDK 35 workstation / CI. | **P0** |
| **REM-A02** | **A — Core App** | `android/gradlew` Lacks POSIX Execute Permission (`-rw-r--r--`) & False Checkpoint Claim | `DEFECT_CONFIRMED` | `ls -la android/gradlew` -> `-rw-r--r--` (`6,123` B); `(cd android && ./gradlew --version)` -> `Permission denied` (`EXIT=126`); contradicted `DOC-MPN-004:12`. | Blocks `./gradlew` immediately on any Linux/macOS host or CI runner until `chmod +x` is run. | Run `chmod +x android/gradlew`; updated docs (`DOC-MPN-004`/`006`) now state actual disk state. | **P0** |
| **REM-A03** | **A — Core App** | Room KSP Schema Export (`android/app/schemas/`) & Instrumented Migration Tests | `BLOCKED_ENVIRONMENT` | `MechanicalProDatabase.kt` sets `exportSchema = true`, `app/build.gradle.kts:25` configures `$projectDir/schemas`, `androidx-room-testing` is bundled, but `android/app/schemas/` does not exist (`os.path.exists == False`). | Future Room `v2+` schema upgrades risk destructive data loss or undetected SQL/Entity mismatch. | Compile debug build to generate `schemas/1.json`, commit schema JSON, and add `MigrationTestHelper` tests before any future `v2` migration. | **P1** |
| **REM-A04** | **A — Core App** | Android Instrumented UI / Compose / SAF Test Suite (`src/androidTest/`) | `PLANNED_NOT_IMPLEMENTED` | `app/build.gradle.kts:103-104` declares `androidx.junit` & `espresso-core`, `testInstrumentationRunner` is configured, yet `android/app/src/androidTest/` does not exist. | Compose navigation across `23` routes, SAF picker contracts, Room SQLite asset copy, and DataStore round-trips lack automated UI tests. | Author targeted instrumentation/Robolectric Compose & Room tests once Android SDK/Emulator host is active. | **P2** |
| **REM-A05** | **A — Core App** | Release Signing Config & ProGuard / R8 Minified Release Verification (`assembleRelease`) | `PLANNED_NOT_IMPLEMENTED` | `app/build.gradle.kts:30-37` enables `isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-rules.pro` (`15` lines), but defines no `signingConfig` for `release`. | Release R8 shrinking could strip kotlinx-serialization / Room / reflection classes unless tested in release APK. | Configure local non-committed release keystore properties (if release APK desired) and run `./gradlew assembleRelease`. | **P3** |
| **REM-A06** | **A — Core App** | Absent `android/gradle.properties` JVM Heap / AndroidX Config | `PARTIALLY_IMPLEMENTED` | `os.path.exists("android/gradle.properties") == False` (`DOC-MPN-025:89-91`). | Fresh Gradle sync relies on default daemon heap (`512m–1g`), which can OOM under KSP + Compose compilation on local PCs. | Add standard `android/gradle.properties` (`org.gradle.jvmargs=-Xmx2048m`, `android.useAndroidX=true`) before/during first Studio build. | **P1** |
| **REM-B01** | **B — Knowledge** | `7` Plant/OEM-Dependent Equipment Records Marked `RESEARCH_PENDING` | `BLOCKED_EXTERNAL_DEPENDENCY` | DB query: `EQ-A05, EQ-B04, EQ-B05, EQ-C04, EQ-C05, EQ-H03, EQ-K02` carry `content_depth_status = 'RESEARCH_PENDING'` (`is_plant_dependent = 1`). | Machine-specific rated capacities, burner/RF/ozone/laser/RO membrane/chiller OEM clearances cannot be finalized without plant equipment plates. | Inspect actual plant machines/OEM manuals, update `seed_equipment_part1/2.py`, rebuild DB cleanly, and promote assets. | **P2** |
| **REM-B02** | **B — Knowledge** | `EQ-G02` (Jhut Steam Boiler) `PARTIAL_CONTENT_BLUEPRINT` Lab Dependency | `BLOCKED_EXTERNAL_DEPENDENCY` | DB query: `EQ-G02` is the sole `PARTIAL_CONTENT_BLUEPRINT` asset; Batch 13/14 reports document dependency on plant mixed-jhut elemental/GCV/ash lab analysis. | Grate fouling, ash fusion, and slagging thresholds vary widely by plant cotton/polyester blend. | Obtain local mixed-jhut proximate/ultimate lab analysis and update `EQ-G02` records. | **P2** |
| **REM-B03** | **B — Knowledge** | Visual Device Verification of All `18` Vector Schematics (`diag_*.xml`) | `COMPLETE_NOT_INDEPENDENTLY_VERIFIED` | All `18` XMLs in `res/drawable/` pass Python XML/`android:name` & `EquipmentMappingTest` mapping checks (`64/64` mapped in `EquipmentDetailScreen.kt`), but zero AAPT2/Compose renders run. | Badge alignment `[1]`–`[6]`, stroke legibility, and `ZoomableSchematicCard` pinch/pan smoothness unconfirmed on real screens. | Inspect all `18` `diag_*.xml` renders in Android Studio Preview / Emulator walkthrough. | **P1** |
| **REM-B04** | **B — Knowledge** | `ISO 20816-3:2026` Alignment Across `REF-06`, `FUND-05`, `EquipmentDetailScreen` & DB | `COMPLETE_VERIFIED` | Verified against `sis.se` & `kssn.net` official release notices (`120–30,000 r/min`, `>15 kW`, exclusions). DB/`kt` grep confirms `0` stale `15,000` values. | None for engineering accuracy; UI rendering check covered by `REM-F01`. | No further content edit needed; close engineering check upon Emulator visual check. | **P3** |
| **REM-B05** | **B — Knowledge** | Secondary Module Depth Upgrades (`MOD-10` Cooling, `MOD-11` ETP, `MOD-12` Maintenance) | `PLANNED_NOT_IMPLEMENTED` | `ModuleCatalogAndScreens.kt:147,160,170` & `DOC-MPN-027` note missing chiller/ETP domain workflows and interactive LOTO / ISO 20816-3 zone lookup / bearing grease equivalence tables in `Maintenance`. | Secondary hubs currently provide real equipment links + planned notices, but lack dedicated diagnostic tools. | Scope optional post-release diagnostic cards (`L10h`/grease chart/LOTO stepper) if requested after v1 APK QA. | **P3** |
| **REM-C01** | **C — Calculations** | `CALC-02` Mixed-Jhut Elemental/Moisture/Ash-LOI Calibration | `PARTIALLY_IMPLEMENTED` | `BoilerThermalCalculations.kt:56-224` & `CalculationSpecifications.kt` implement Natural Gas (`IMPLEMENTED`, `87` total DB vars) while restricting uncalibrated `fuel_type=2.0` unless user supplies lab `siegert_k`, `theoretical_air`, mass `GCV`. | Plant engineers firing uncharacterized garment cutting jhut cannot rely on default constants and must obtain lab fuel inputs. | Collect plant fuel test data and optionally add calibrated Bangladesh cotton-jhut preset in a future data update. | **P2** |
| **REM-C02** | **C — Calculations** | JVM Execution of `43` Calculation, Formatter & Unit Converter `@Test`s | `COMPLETE_NOT_INDEPENDENTLY_VERIFIED` | `EngineeringCalculationsTest.kt` (`9`), `Stage02CalculationValidationTest.kt` (`18`), `CalculationFormatterAndSteamRangeTest.kt` (`4`), `UnitConverterTest.kt` (`8`), `BackupJsonCodecAndCalcRepoTest.kt` (`3` — partial calc repo) authored in `src/test/`, never executed by `kotlinc`/JUnit. | Kotlin runtime regressions (`require()`, rounding, locale formatting, unit helper mappings) unconfirmed on JVM. | Run `./gradlew testDebugUnitTest` on JDK 17 host and confirm 100% green report. | **P0** |
| **REM-C03** | **C — Calculations** | Planned Cooling/Chiller (`TR`/`COP`), ETP (`BOD`/`COD`/`HRT`), & Reliability (`MTBF`/`L10h`) Calculators | `PLANNED_NOT_IMPLEMENTED` | Explicitly disclosed in `ModuleCatalogAndScreens.kt:147, 160, 170` as not yet implemented in `CALC-01..20`. | Engineers opening Cooling/ETP/Maintenance hubs see equipment & references but have no calculators for those disciplines. | Design, research, and implement verified specs (`CALC-21+`) in a future release after v1 verification. | **P3** |
| **REM-C04** | **C — Calculations** | Field Validation of `CALC-20` Empirical Air $\Delta P$ Formula vs Plant Loops | `COMPLETE_NOT_INDEPENDENTLY_VERIFIED` | `WashPlantMaintenanceCalculations.kt` (`Stage 02 DOC-MPN-022`) notes empirical `0.00075` factor can diverge ~17–33% vs Darcy-Weisbach / Atlas Copco `450`. | Preliminary header ΔP could under-predict rough/corroded pipe losses; disclosure exists in formula limitations. | Validate predictions against physical plant air receiver pressure gauges during Phase 07/field QA. | **P3** |
| **REM-D01** | **D — Boiler Law** | `The Boilers Act, 2022 (Act 06 of 2022)` & `Rule 3` + `Rule 44` Purge Accuracy | `COMPLETE_VERIFIED` | Official web checks (`bdlaws.minlaw.gov.bd`, `dpp.gov.bd`, `vdb-loi.com`) verified `Act 06 of 2022` Secs `9/11/13/18-19/24/28/36/38/42/44/54`, `S.R.O. 279-Law/2025` `29 June 2025`, `Rule 3` (`10/15/30/60+10 yr`), `Rule 44` = exam fee cancellation. `0` stale strings in DB/`.kt`. | Legal citations in v1 app match authoritative public statutory records. | Preserve current statutory text; do not modify without official Gazette proof. | **P3** |
| **REM-D02** | **D — Boiler Law** | Full Gazette PDF Audit of Unretrieved Rules (`67` Minor Rules) & Attendant Slab / OCIB Tracker | `BLOCKED_EXTERNAL_DEPENDENCY` | Official Gazette PDF `57958_89222.pdf` was not downloaded/parsed in full (`DOC-MPN-042`, `DOC-MPN-004:48`); `DOC-MPN-027` (`GAP-10`) also noted missing Grade-1/2/3 slab table & OCIB renewal date tracker UI in `BoilerDashboardScreen`. | Exact fee tables, form numbers, and minor renewal sub-clauses outside verified Rules 3 & 44 remain unquoted; certificate expiry tracker is manual checklists only. | Review full `S.R.O. No. 279-Law/2025` Bengali Gazette PDF offline and add Grade-1/2/3 capacity table & optional local renewal reminder if verified. | **P2** |
| **REM-E01** | **E — Steam Table** | External NIST REFPROP / CoolProp Binary Benchmark (`P7-P5-03`, Owner Condition 5) | `BLOCKED_ENVIRONMENT` | `iapws`, `CoolProp`, `scipy`, `numpy` `NOT_INSTALLED`. Container verified Eq. 31 + Wagner–Pruss IAPWS-95 `P_sat` (`<= 0.0179%`) & Clausius–Clapeyron `h_fg` (`<= 0.0454%`) across `501` points, not `h_f`/`h_g`/`v_g`/`v_f` directly against NIST/CoolProp binary tables. | Owner Condition 5 prohibits claiming formal external REFPROP certification until independent table cross-check completes. | Run Python `iapws` / `CoolProp` / NIST WebBook comparison script across all `56` anchors & `21+` midpoints on workstation and archive max residuals. | **P0** |
| **REM-E02** | **E — Steam Table** | Out-of-Scope Steam Regions (`< 0.50 bar a`, Subcooled R1, Superheated R2, `> 20 bar a` R3, R5, Backward `T(p,h/s)`) | `PLANNED_NOT_IMPLEMENTED` | Explicitly documented in `IapwsIf97SteamEngine.kt:20-26`, `SteamTableScreen.kt`, and `PHASE_06_KNOWN_LIMITATIONS.md`. | Cannot evaluate condenser vacuum `< 0.50 bar(a)` or superheated turbine/high-pressure lines `> 20 bar(a)`. | Keep honest boundary in v1; extend only if high-pressure/superheat plant requirements arise later. | **P3** |
| **REM-E03** | **E — Steam Table** | `IapwsIf97SteamEngine.getPropertiesByGaugePressure` Hardcoded `[-0.51, 18.98 bar g]` Text & Dead `GRID` Cols | `DEFECT_CONFIRMED` | `IapwsIf97SteamEngine.kt:209-213`: exception string hardcodes `-0.51, 18.98 bar g` even when custom `atmosphericBar` shifts `minGauge..maxGauge`; `GRID[i][1]` and `GRID[i][4]` unused at runtime. | No user-facing bug at default `1.01325 bar`, but misleading error text if custom altitude `atmosphericBar` is called programmatically. | Format `minGauge`..`maxGauge` cleanly in exception (keeping `-0.51`/`18.98` wording or matching test expectation) in a future tidy pass. | **P3** |
| **REM-F01** | **F — Field Usability** | `Stage 05` Android Emulator / AVD Compose Rendering, `42` `@Preview`s & Accessibility Walkthrough | `BLOCKED_ENVIRONMENT` | Container has `0` JDK/Android SDK/AVD/`/dev/kvm` (`DOC-MPN-025`). All `42` `@Preview` functions and Batch 01–14 UI changes (`imePadding`, Dark/Light contrast, `1.18x` scaling, `48.dp` touch targets) remain unrendered. | Hidden layout clipping, slow recomposition, IME overlap, TalkBack navigation bugs, or Compose crashes on `360dp` screens cannot be caught statically. | Open `android/` in Android Studio, inspect `@Preview`s, boot API 34/35 AVD, and run full interactive screen walkthrough + Logcat check. | **P0** |
| **REM-F02** | **F — Field Usability** | Physical Android Smartphone Shop-Floor Validation (`P7-P3-04`, `P7-P7-04`) | `PLANNED_NOT_IMPLEMENTED` | Zero physical APK install or hardware QA performed (`DOC-MPN-019`). | Sunlight/boiler-house glare legibility, gloved touch accuracy, rotation/process-kill survival, and hardware SAF provider behavior untested. | Install verified debug APK on owner smartphone after Emulator pass and test on shop floor. | **P1** |
| **REM-F03** | **F — Field Usability** | Stale `visual-preview-gallery/index.html` Web Mockup Artifact | `DEFECT_CONFIRMED` | Python scan hit `['Rule 44', '15,000', '24h', '২০১৫']` in `visual-preview-gallery/index.html` (frozen `2026-11-15` pre-Batch-01..14). | Does NOT ship inside Android APK, **but** displays outdated/disproven Batch 01–14 UI and statutory strings if opened by reviewers. | Either synchronize `visual-preview-gallery/index.html` with Batch 01–14 or add a clear top banner marking it a deprecated pre-Batch-01 mockup. | **P2** |
| **REM-F04** | **F — Field Usability** | UI Chrome Bilingual Localization Parity & PDF/CSV Field Report Export | `PARTIALLY_IMPLEMENTED` | Equipment, references, fundamentals & Search support bilingual English/Bengali, **but** UI navigation, settings, checklists & calculator forms are mostly English (`Notes`/`Checklists`/`Calculations`); export is raw `.json` backup only (no PDF/CSV printable shift report). | Operators wanting full Bengali UI labels or printable shift inspection PDFs cannot generate them directly in-app. | Optional post-v1 enhancement (`strings-bn.xml` + Android `PdfDocument` offline shift/calc report export). | **P3** |
| **REM-G01** | **G — Data & Backup** | `BackupJsonCodec` (`backup_version = 2`) Excludes Custom Checklists & Operator/Shift Execution Metadata | `PARTIALLY_IMPLEMENTED` | `BackupJsonCodec.kt`, `UserDataRepositoryImpl.kt:267-280`, `SettingsScreen.kt:486`, `UserPreferencesRepository.kt`: custom checklist JSON & shift/operator execution logs are stored in DataStore, cleared by `clearAllPersonalData()`, **but excluded from SAF v2 JSON export/import**. | User-created checklists and shift/operator logs are lost when migrating phones via JSON backup/restore (honestly disclosed in UI). | Design `backup_version = 3` JSON (preserving `v1`/`v2` backward compatibility) or move custom checklists into Room `v2` in a future update after v1 native verification. | **P1** |
| **REM-G02** | **G — Data & Backup** | Kotlin-Hosted Catalogs (`Checklists`, `REF-01..12`, `FUND-01..15`) Outside SQLite FTS4 | `COMPLETE_VERIFIED` | `EngineeringCatalogs.kt` stores `PlantChecklistCatalog`, `TechnicalReferenceCatalog`, `FundamentalsCatalog`; `SearchRepositoryImpl` searches them in-memory alongside SQLite FTS4 (`UnifiedSearchAndCatalogTest` verified logic). | Works offline and keeps Room schema at `v1`, but FTS4 ranking/prefixing does not cover static Kotlin catalogs uniformly unless migrated later. | Retain `EngineeringCatalogSearch` for v1 stability; evaluate unified FTS indexing only if Room `v2` is introduced. | **P3** |
| **REM-G03** | **G — Data & Backup** | Legacy Partial `seed_data.sql` (`4` Families, `7` Equipment) Present in Root | `DEFECT_CONFIRMED` | Confirmed on disk alongside full `build_database.py` / `seed_equipment_part1/2.py` (`DOC-MPN-020 §9`). | Contributors or automated tools reading `seed_data.sql` could mistake it for the active 64-asset database schema/seed. | Archive or delete `seed_data.sql` (or replace with clear deprecation header / full dump) during repository cleanup. | **P3** |
| **REM-H01** | **H — QA & Release** | Full Execution of All `125` Authored JVM JUnit `@Test`s Across `12` Test Files | `BLOCKED_ENVIRONMENT` | Counted directly across `12` files (`Stage04UiStateAndAccessibilityTest`: 48, `Stage02CalculationValidationTest`: 18, `EngineeringCalculationsTest`: 9, `SteamEngineTest`: 8, `UnifiedSearchAndCatalogTest`: 8, `UnitConverterTest`: 8, `EquipmentFilterTest`: 6, `EquipmentMappingTest`: 6, `CalculationFormatterAndSteamRangeTest`: 4, `DatabaseIntegrityAndMigrationTest`: 4, `BackupJsonCodecAndCalcRepoTest`: 3, `SearchRepositoryTest`: 3). | Zero Kotlin unit tests have ever compiled or executed (`./gradlew testDebugUnitTest`). | Unblock `REM-A01`/`A02` and execute full JVM JUnit suite to green status. | **P0** |
| **REM-H02** | **H — QA & Release** | Android Lint Verification (`lintDebug`) & Clean Build Log Capture | `BLOCKED_ENVIRONMENT` | Cannot run `./gradlew lintDebug` in container without JDK 17 / SDK 35. | Manifest, Compose API compatibility (`minSdk = 26`, `targetSdk = 35`), and resource/translation warnings remain uninspected. | Execute `./gradlew lintDebug` on local workstation and resolve any warnings/errors before APK release. | **P1** |
| **REM-H03** | **H — QA & Release** | Drifted Pre-Batch-12/13/14 Root Python Suites (`test_suite_phase05/06/07.py`, `test_suite_stage01..04.py`) | `DEFECT_CONFIRMED` | Read-only run showed ONLY `test_suite_phase04.py` (`10/10`) passes now; `phase05/06/07` & `stage01..04` fail on intentionally superseded assertions (`87` vs `79` vars, `56/1/7` vs `2/9/7/46` depths, `CALC-02 = IMPLEMENTED`, Eq. 31 text in `IapwsIf97SteamEngine`, `"S.R.O. No. 279-Law/2025"`, new DB SHA-256, `android/gradlew` missing `+x`). Note running `test_suite_phase07.py` also refreshes `phase07_test_summary.json` with those stale failures. | Running root Python scripts produces noisy false failures that obscure real regressions during static checks. | Update or archive legacy Stage/Phase static Python checkers into one current `Batch 14+` integrity suite (or keep historical scripts clearly marked frozen). | **P2** |
| **REM-H04** | **H — QA & Release** | Owner-Controlled Git Commit & Push (`researchbangla/mechanical-pro-notes`) | `PLANNED_NOT_IMPLEMENTED` | `.git` absent / zero commits executed per `DEC-002` / `DEC-004`. | Local workspace changes (`DOC-MPN-043`, Batch 01–14) are not yet committed to owner remote repo. | Owner commits and pushes ONLY after P0 native compilation, `125` unit tests, NIST check, and Emulator QA pass. | **P1** |

---

## 7. Reconciliation Against Existing Docs (`CURRENT_WORK_CHECKPOINT.md`, `SESSION_HANDOVER.md`, `BATCH_14_IMPLEMENTATION_REPORT.md`)

1. **False Claim Disproven (`android/gradlew`):** Both `docs/CURRENT_WORK_CHECKPOINT.md` (line 12, 40) and `docs/PHASE_PROGRESS_TRACKER.md` (line 21, 147) / `docs/STAGE_05_EMULATOR_PREVIEW_READINESS_REPORT.md` (line 23, 80) state `android/gradlew` has `-rwxr-xr-x`. Actual filesystem stat on `2026-11-18` is **`-rw-r--r--` (`6,123` bytes)** and `(cd android && ./gradlew --version)` exits **`126` (`Permission denied`)**. Synced in Batch 14 closure updates without touching build config.
2. **`@Preview` Count Reconciled:** Prior docs cited `40` `@Preview` composables (from Stage 04). Actual count across `64` main `.kt` files on disk is **`42` `@Preview` composables** (`+2` added in `ModuleCatalogAndScreens.kt` during Batch 06).
3. **`FULL_CONTENT_BLUEPRINT` Section Counts Clarified:** While `EQ-A01` and `EQ-B01` store `21` rows in `knowledge_section`, the `54` assets promoted in Batch 13 hold `15–19` `knowledge_section` rows (always covering sections `1, 3–10, 12–16, 18` plus applicable `17, 19–21`, with Subtypes & Technical Parameters stored cleanly in normalized SQLite tables `equipment_subtype` and `technical_parameter`).
4. **Honest Revision of `"0 core gaps remain"`:** `CURRENT_WORK_CHECKPOINT.md:47` stated `"0 core gaps remain unimplemented"`. While `GAP-01`–`GAP-13` core deliverables were authored through Batch 14, this full audit confirms **real remaining gaps**: missing `android/app/schemas/` & Migration tests (`REM-A03`), `android/src/androidTest/` (`REM-A04`), `backup_version = 2` omission of custom checklists/shift metadata (`REM-G01`), unimplemented Cooling/ETP/Reliability calculators (`REM-C03`), `7` `RESEARCH_PENDING` + `1` `PARTIAL` (`EQ-G02`) + `CALC-02` jhut calibration (`REM-B01/B02/C01`), `67` unretrieved Boiler Rules 2025 minor rules (`REM-D02`), stale `visual-preview-gallery/index.html` (`REM-F03`), drifted Python regression scripts (`REM-H03`), plus the **unexecuted native build (`REM-A01/A02/H01`) & external REFPROP check (`REM-E01`)**.

---

## 8. Prioritized Execution Roadmap (`P0` -> `P3`)

### P0 — Blocking Build, Safety & Verification Must-Fix Before First Phone Install
1. **`REM-A02`:** Restore POSIX execute bit on `android/gradlew` (`chmod +x android/gradlew`).
2. **`REM-A01` + `REM-H01` + `REM-C02`:** Run `./gradlew testDebugUnitTest assembleDebug` on JDK 17 + Android SDK 35; confirm all **`125` JUnit `@Test`s** pass and capture build/KSP output.
3. **`REM-E01`:** Execute independent NIST Chemistry WebBook / REFPROP / CoolProp Python cross-check across all `56` `IapwsIf97SteamEngine` grid points and intermediate pressures (clearing Owner Condition 5).
4. **`REM-F01`:** Boot API 34/35 Android Emulator (`Pixel 7` / `360–412dp`), inspect `@Preview`s and `18` schematics, test Dark/Light & `1.18x` modes, IME padding, FTS4 English/Bengali search, and SAF backup/restore with Airplane mode ON.

### P1 — High-Priority Pre-Release Stabilization
5. **`REM-A06` + `REM-A03` + `REM-H02`:** Add `android/gradle.properties`, archive KSP-generated `android/app/schemas/1.json`, author `MigrationTestHelper` contract test, and clear `./gradlew lintDebug`.
6. **`REM-B03` + `REM-F02`:** Inspect all `18` rendered `diag_*.xml` schematics on Emulator and complete physical smartphone field ergonomics QA (`48.dp` glove touch, outdoor glare, process death).
7. **`REM-G01`:** Upgrade backup portability (`backup_version = 3` in `BackupJsonCodec` preserving v1/v2 backward compatibility) so custom checklists and shift/operator logs are safely backed up & restored.
8. **`REM-H04`:** Owner executes clean `git commit` & `git push` to `researchbangla/mechanical-pro-notes`.

### P2 — Important Domain Completion & Tooling Hygiene
9. **`REM-B01` + `REM-B02` + `REM-C01`:** Collect actual plant OEM specs for the `7` `RESEARCH_PENDING` assets (`EQ-A05, B04, B05, C04, C05, H03, K02`) and lab fuel analysis for `EQ-G02`/`CALC-02` mixed jhut.
10. **`REM-D02`:** Audit full verbatim PDF of `Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)` (`57958_89222.pdf`) and add Grade-1/2/3 slab table / local renewal tracker if verified.
11. **`REM-A04` + `REM-H03` + `REM-F03`:** Author Compose/Room `androidTest` coverage; modernize/consolidate drifted Python static test suites; sync or retire `visual-preview-gallery/index.html`.

### P3 — Post-Release Enhancements & Cleanup
12. **`REM-C03` + `REM-B05`:** Implement verified Cooling/Chiller (`TR`/`COP`), ETP (`BOD`/`COD`/`HRT`), and Maintenance Reliability (`MTBF`/`L10h`) calculators + diagnostic tables.
13. **`REM-F04` + `REM-A05` + `REM-E02` + `REM-E03` + `REM-G02` + `REM-G03`:** Release keystore/ProGuard smoke verification (`assembleRelease`), optional Bengali UI localization & offline PDF shift-report export, custom `atmosphericBar` message polish, and `seed_data.sql` cleanup.

---

## 9. Recommended Next Batch Scope (Proposed **Batch 15**)

**Do NOT begin Batch 15 inside this air-gapped container without host tooling if native compilation is the goal;** instead, split Batch 15 cleanly into two gated tracks:

1. **Batch 15A — Host Native Build, Verification & REFPROP Closure (Requires Owner PC: JDK 17 + SDK 35 + Emulator + CoolProp/REFPROP):**
   - `chmod +x android/gradlew`, add `android/gradle.properties`, run `./gradlew testDebugUnitTest lintDebug assembleDebug`.
   - Fix any Kotlin/Compose/KSP/Lint issues discovered by the real compiler (if any).
   - Commit generated `android/app/schemas/1.json`, capture `Stage 05` Emulator screenshots + Logcat (`0` fatal errors), and run external NIST REFPROP / CoolProp benchmark report closing `P7-P5-03`.
2. **Batch 15B — Backup Portability (`v3`), Verification Suite Hygiene & Plant-Data Readiness (Optional Code Follow-up After 15A):**
   - Implement `backup_version = 3` in `BackupJsonCodec.kt` + `UserDataRepositoryImpl.kt` + `SettingsScreen.kt` (extending backup/restore to custom checklists + execution metadata while preserving v1/v2 compatibility and Room `version = 1`), modernize the stale Python static check suite, and prepare plant inspection sheets for the `7` `RESEARCH_PENDING` assets & `EQ-G02`/`CALC-02` lab jhut inputs.
