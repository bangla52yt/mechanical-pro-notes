# BATCH 15A NATIVE ANDROID BUILD, JVM TESTS & VERIFICATION REPORT

**Document Identifier:** DOC-MPN-044  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Repository Target:** `researchbangla/mechanical-pro-notes` (`DEC-002`, `DEC-004`)  
**Baseline Audited:** Batch 14 Closure Audit (`DOC-MPN-043`)  
**Date:** 2026-11-18  
**Execution Mode:** Controlled Implementation + Native Verification (`Build Integrity > Test Integrity > Data Safety`)  

---

## 1. Executive Summary & Batch 15A Acceptance Matrix

In Batch 15A, the native workspace configuration, toolchain availability, Gradle Wrapper defect (`REM-A02`), static Room/Manifest invariants, and executable verification commands were directly tested on disk.

- **Overall Batch 15A Verdict:** **PARTIAL / BLOCKED ON EXTERNAL HOST TOOLCHAIN** (Zero source, schema, backup-format, or domain regressions introduced; minimal `chmod +x android/gradlew` wrapper permission fix applied cleanly and verified on disk, while native JDK/SDK/KSP/Lint/Emulator steps remain genuinely `BLOCKED` inside this container).

| Acceptance Criterion | Status | Evidence Summary |
|---|---|---|
| **1. Gradle Wrapper Execution (`REM-A02`)** | **PARTIAL** *(Filesystem Fixed; JDK Pre-Flight Blocked)* | Restored POSIX `+x` (`-rw-r--r--` `0644` -> `-rwxr-xr-x` `0755`) via `chmod +x android/gradlew` while preserving exact `gradlew` SHA-256 (`604267e7...`), `gradle-wrapper.jar` (`43,504` B, `33` ZIP entries `OK`), and Gradle `8.9-bin.zip` URL. `(cd android && ./gradlew --version)` now executes cleanly up to Java discovery (no longer `EXIT=126 Permission denied`), failing at `EXIT=1` (`ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH`). Git file mode (`100755` vs `100644`) is uncheckable here because `.git` is absent. |
| **2. JVM Unit Test Execution (`REM-C02`, `REM-H01`)** | **BLOCKED** | `(cd android && ./gradlew testDebugUnitTest)` exited `1` (`JAVA_HOME` unset / `java` not found). Static discovery across `android/app/src/test/` confirms **`12` test `.kt` files**, **`125` `@Test` methods**, and **`0` `@Ignore` methods**. |
| **3. Debug APK Compilation (`REM-A01`)** | **BLOCKED** | `(cd android && ./gradlew assembleDebug)` exited `1` (`JAVA_HOME` unset / missing JDK 17 & Android SDK 35). Static manifest inspection confirms `0` `<uses-permission>` elements (`INTERNET` absent) and `applicationId = "com.researchbangla.mechanicalpronotes"` (`debug` suffix `".debug"`). |
| **4. Android Lint Verification (`REM-H02`)** | **BLOCKED** | `(cd android && ./gradlew lintDebug)` exited `1` (`JAVA_HOME` unset / missing Android SDK 35). |
| **5. Room KSP Schema Verification (`REM-A03`)** | **PARTIAL** *(Static Parity Verified; KSP Export Blocked)* | Verified `MechanicalProDatabase.kt` (`version = 1`, `exportSchema = true`, `16` entities), `app/build.gradle.kts` (`ksp` `2.0.21-1.0.27`, `room { schemaDirectory("$projectDir/schemas") }`), `DatabaseMigrations.kt`, and `mechanical_pro_notes.db` (`PRAGMA user_version = 1`, `16/16` matching tables). `android/app/schemas/` remains absent until native KSP runs. |
| **6. API 34/35 Emulator Verification (`REM-F01`)** | **BLOCKED** | `adb`, `emulator`, `avdmanager`, `sdkmanager`, and `/dev/kvm` are absent in this container. Full Android Studio & CLI verification procedure provided in Section 8. |

---

## 2. Environment Capability Report (Phase A)

All system tools, environment variables, disk/memory resources, Gradle files, and dependency repository endpoints were probed directly in `/app/applet`:

| Capability / Probe | Result | Observed Output / State |
|---|---|---|
| **OS & Kernel** | Verified | `Linux localhost 4.19.0-gvisor #1 SMP` / `Debian GNU/Linux 12 (bookworm)` (`x86_64`) |
| **Resources** | Verified | `2` vCPU cores, `4.0 GiB` RAM (`3.5 GiB` available, `0 B` Swap), `504 GiB` filesystem mounted at `/` |
| **`java` / `javac` / `kotlinc`** | `NOT FOUND` | `which` returned missing for all three; `/usr/lib/jvm/*` absent; `JAVA_HOME` unset. |
| **Android SDK / Build-Tools** | `NOT FOUND` | `adb`, `emulator`, `sdkmanager`, `avdmanager` missing; `ANDROID_HOME` & `ANDROID_SDK_ROOT` unset; `android/local.properties` absent. |
| **System `gradle` & Cache** | `NOT FOUND` | `gradle` missing; `/root/.gradle` and `/root/.android` absent. |
| **Wrapper Distribution Config** | Present & Valid | `android/gradle/wrapper/gradle-wrapper.properties` configures `gradle-8.9-bin.zip`; `gradle-wrapper.jar` (`43,504` B, `sha256: 498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17`) passes ZIP integrity (`33` entries, `testzip() == None`). |
| **Gradle / AGP / KSP Stack** | Present in Config | `android/gradle/libs.versions.toml`: `agp = "8.7.2"`, `kotlin = "2.0.21"`, `ksp = "2.0.21-1.0.27"`, `composeBom = "2024.10.01"`, `room = "2.6.1"`, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, `JavaVersion.VERSION_17`. |
| **Network Dependency Reachability** | Reachable (`0`-byte HEAD) | `curl -sI` returned `307` on `services.gradle.org/distributions/gradle-8.9-bin.zip`, `200 OK` on `repo.maven.apache.org/maven2/`, and `200 OK` on `dl.google.com/dl/android/maven2/.../maven-metadata.xml`. |
| **Why Container Toolchain Install Was Avoided** | Safety Boundary Enforced | Downloading JDK 17 + Android SDK Platform 35 + Build-Tools 35 + Gradle 8.9 + Maven/Compose artifacts requires multi-GB SDK setup and daemon JVM/KSP memory (`2+ GiB`) in a `4.0 GiB` zero-swap gVisor sandbox without Android Emulator hardware acceleration (`/dev/kvm` absent). Per Phase A rules, no system SDKs were installed. |

---

## 3. Gradle Wrapper Fix & Git File-Mode Status (Phase B)

1. **Before Fix:** `ls -la android/gradlew` showed `-rw-r--r--` (`0o644`, `6,123` B). Running `(cd android && ./gradlew --version)` failed immediately at OS exec with `Permission denied` (`EXIT: 126`).
2. **Minimal Correction Applied:** Executed `chmod +x android/gradlew`.
3. **Post-Fix Verification:**
   - File mode: **`-rwxr-xr-x` (`0o755`)**.
   - Byte preservation verified:
     - `android/gradlew`: `6,123` B | SHA-256 `604267e763d4d26920c0a40e1010c922b3b4ef78d8894e80e511ef1b5b9d7240` (unmodified script bytes).
     - `android/gradle/wrapper/gradle-wrapper.jar`: `43,504` B | SHA-256 `498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17` (`33` ZIP entries verified).
     - `android/gradle/wrapper/gradle-wrapper.properties`: `250` B (`gradle-8.9-bin.zip` preserved).
   - Execution check `(cd android && ./gradlew --version)`: wrapper shell script executed without permission error and failed cleanly at `gradlew:111`:
     `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.` (`EXIT: 1`).
4. **Git Mode Status:** Running `git ls-files --stage android/gradlew` returned `fatal: not a git repository (or any of the parent directories): .git`. Because `.git` is not initialized in `/app/applet`, Git index mode (`100755` vs `100644`) could not be inspected or staged; when creating/updating the Git clone on the host PC, ensure `git update-index --chmod=+x android/gradlew` is checked before committing.

---

## 4. JVM Unit Test Attempt & Test Inventory (Phase C)

- **Command Attempted:** `(cd android && ./gradlew testDebugUnitTest)`
- **Outcome:** **`BLOCKED`** (`EXIT: 1` — `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH`).
- **Execution Counts:** Discovered via native runner: `0` (JVM did not launch) | Passed: `0` | Failed: `0` | Skipped: `0` | Ignored: `0` | Compile Errors: Not reached (`N/A`).
- **Authored Test Discovery on Disk (`12` Files / `125` `@Test` Methods / `0` `@Ignore`):**

| Test File (`android/app/src/test/java/com/researchbangla/mechanicalpronotes/`) | `@Test` Count | `@Ignore` Count | Uses `org.junit.Test` |
|---|---|---|---|
| `BackupJsonCodecAndCalcRepoTest.kt` | `3` | `0` | `True` |
| `CalculationFormatterAndSteamRangeTest.kt` | `4` | `0` | `True` |
| `DatabaseIntegrityAndMigrationTest.kt` | `4` | `0` | `True` |
| `EngineeringCalculationsTest.kt` | `9` | `0` | `True` |
| `EquipmentFilterTest.kt` | `6` | `0` | `True` |
| `EquipmentMappingTest.kt` | `6` | `0` | `True` |
| `SearchRepositoryTest.kt` | `3` | `0` | `True` |
| `Stage02CalculationValidationTest.kt` | `18` | `0` | `True` |
| `Stage04UiStateAndAccessibilityTest.kt` | `48` | `0` | `True` |
| `SteamEngineTest.kt` | `8` | `0` | `True` |
| `UnifiedSearchAndCatalogTest.kt` | `8` | `0` | `True` |
| `UnitConverterTest.kt` | `8` | `0` | `True` |
| **Total** | **`125`** | **`0`** | `12/12` |

---

## 5. Debug APK Build & Manifest Permission Audit (Phase D & E)

- **Debug Build Command Attempted:** `(cd android && ./gradlew assembleDebug)` -> **`BLOCKED`** (`EXIT: 1`, `JAVA_HOME` / `java` absent).
- **APK Artifact Verification:** **None generated** (`android/app/build/` does not exist; `versionCode = 1`, `versionName = "1.0.0"`, `applicationId = "com.researchbangla.mechanicalpronotes"`, debug suffix `".debug"`, variant `debug` remain configured in `android/app/build.gradle.kts` without compilation).
- **Static `AndroidManifest.xml` Security Audit (`android/app/src/main/AndroidManifest.xml`):**
  - `<uses-permission>` elements found: **`[]` (`0` permissions declared)**.
  - `android.permission.INTERNET` status: **ABSENT** (present only inside explanatory privacy comment on lines 4–9).
  - `android:allowBackup`: **`false`**.
  - Launcher Activity: `.presentation.MainActivity` (`android:exported="true"`, `windowSoftInputMode="adjustResize"`).

---

## 6. Android Lint Audit (Phase E)

- **Command Attempted:** `(cd android && ./gradlew lintDebug)` -> **`BLOCKED`** (`EXIT: 1`, `JAVA_HOME` / `java` / Android SDK 35 absent).
- **Lint Reports Generated:** None (`android/app/build/reports/lint-results-debug.*` absent). No lint rules were suppressed or modified.

---

## 7. Room KSP Schema Verification (Phase F)

Static cross-verification between Room Kotlin declarations and the packaged SQLite database `android/app/src/main/assets/database/mechanical_pro_notes.db`:

- **Build Tooling Configuration (`android/app/build.gradle.kts`):**
  - `com.google.devtools.ksp` (`2.0.21-1.0.27`) and `androidx.room` (`2.6.1`) plugins enabled.
  - `ksp(libs.androidx.room.compiler)` declared.
  - `room { schemaDirectory("$projectDir/schemas") }` configured (lines `24–26`).
- **Database Declaration (`MechanicalProDatabase.kt`):**
  - `version = 1`, `exportSchema = true`, `createFromAsset("database/mechanical_pro_notes.db")`, `addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)`.
  - `DatabaseMigrations.kt`: `CURRENT_DATABASE_VERSION = 1`, `ALL_MIGRATIONS = emptyArray()` (no destructive fallback on upgrade).
- **16/16 Entity-to-SQLite Table Parity Verified Directly:**
  - `StaticEntities.kt` (`11`): `system_family`, `equipment_category`, `equipment`, `equipment_subtype`, `component`, `technical_parameter`, `knowledge_section`, `failure_mode`, `maintenance_procedure`, `calculation`, `formula_variable`.
  - `DynamicUserEntities.kt` (`4`): `user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`.
  - `EquipmentFtsEntity.kt` (`1`): `equipment_fts` (FTS4 virtual table).
  - SQLite asset `PRAGMA user_version = 1`; all `16` user/static/FTS tables and `19` supporting indices match cleanly.
- **Native KSP Schema Export Status:** **`BLOCKED`** — `android/app/schemas/` does not exist because KSP (`kspDebugKotlin`) has never compiled in this container.
- **Note on `android/.gitignore`:** Line `22` of `android/.gitignore` currently reads `android/app/schemas/` (relative to `android/`). No edit was made in this verification pass, and `MechanicalProDatabase.kt` remains cleanly at `version = 1`.

---

## 8. `android/gradle.properties` Assessment & Host Reproducibility Guide (Phase G & H)

### 8.1 Assessment of `android/gradle.properties` (`REM-A06`)
- **Decision Taken:** **`android/gradle.properties` was NOT created in Batch 15A.**
- **Rationale:** Because Gradle execution is blocked by missing JDK 17 in this container, no live Gradle error or JVM heap exhaustion could be demonstrated locally. Per strict Phase H scope discipline (*do not create configuration without a demonstrated build requirement*), guessing flags before the first real host sync was intentionally avoided.
- **Technical Risk / Recommendation for First Host Build:** `android/app/build.gradle.kts` uses AGP `8.7.2` with numerous `androidx.*` artifacts (`core-ktx`, `compose`, `room`, `datastore`, `navigation`). In AGP 8.x, `android.useAndroidX` defaults to `false` when `gradle.properties` is absent, meaning the first `./gradlew` run on JDK 17 + SDK 35 is highly likely to fail Gradle configuration unless `android/gradle.properties` is added with:
  ```properties
  org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
  android.useAndroidX=true
  ```

### 8.2 Reproducible Host Workstation Requirements & Verification Steps (Phase G & H)
- **Required Host Stack:**
  - **JDK:** JDK 17 (`JAVA_HOME` pointing to JDK 17, matching `JavaVersion.VERSION_17` and `jvmTarget = "17"`).
  - **Android SDK:** `compileSdk = 35` (`platforms;android-35`), `build-tools;35.0.0`, `platform-tools` (`adb`), Android Studio Ladybug `2024.2+` (or Gradle 8.9 CLI).
  - **First-Sync Internet:** Required once on the build host to populate `~/.gradle` (`gradle-8.9-bin.zip`, `google()`, `mavenCentral()`, `gradlePluginPortal()`).
  - **Emulator / Device:** API `34` or `35` AVD (`Pixel 7` or `360–412 dp` profile) or physical Android smartphone (`minSdk >= 26`).
- **Step-by-Step Host Execution:**
  1. Ensure `android/gradlew` stays executable (`chmod +x android/gradlew`; in Git run `git update-index --chmod=+x android/gradlew`).
  2. Open `android/` in Android Studio (allowing `local.properties` `sdk.dir=...` auto-generation) or set `ANDROID_HOME` and `JAVA_HOME`. If AGP fails on `android.useAndroidX` or daemon heap, create `android/gradle.properties` with the two lines above.
  3. Run:
     ```bash
     cd android
     ./gradlew testDebugUnitTest lintDebug assembleDebug
     ```
  4. Verify generated outputs: `android/app/schemas/.../1.json`, `125/125` passing tests in `testDebugUnitTest/index.html`, zero unresolved errors in `lint-results-debug.html`, and `android/app/build/outputs/apk/debug/app-debug.apk`.
  5. Boot API 34/35 AVD, disable network (`adb shell svc wifi disable && adb shell svc data disable`), install APK (`adb install -r app/build/outputs/apk/debug/app-debug.apk`), verify startup, FTS4 search, all `18` `diag_*.xml` zoom renders, calculations & Steam Table, Boiler Dashboard & Checklists, Notes & SAF backup/restore, dark/light contrast, rotation, and inspect `adb logcat` (`0` fatal exceptions).

---

## 9. Updated Remaining-Work Register Delta (Post-Batch 15A)

All `31` tracked items from `DOC-MPN-043` remain accurately cataloged, with Batch 15A deltas highlighted below:

| ID | Category | Previous (`DOC-MPN-043`) | Batch 15A Updated Status | Priority | Note |
|---|---|---|---|---|---|
| `REM-A02` | A — Core App | `DEFECT_CONFIRMED` (`-rw-r--r--`, `EXIT=126`) | **`PARTIALLY_IMPLEMENTED`** | **P0** | Filesystem `-rwxr-xr-x` (`0755`) fixed & wrapper integrity verified (`./gradlew --version` passes exec check up to missing JDK `EXIT=1`); host Git `100755` verification & full `--version` output await JDK 17 + `.git` repo. |
| `REM-A01` | A — Core App | `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` | **P0** | `./gradlew assembleDebug` attempted -> `EXIT=1` (no JDK 17 / SDK 35). |
| `REM-A03` | A — Core App | `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` (`PARTIAL` static parity) | **P1** | 16/16 entities match SQLite v1 DDL; KSP export of `schemas/1.json` awaits JDK 17 build. |
| `REM-A06` | A — Core App | `PARTIALLY_IMPLEMENTED` | `PARTIALLY_IMPLEMENTED` | **P1** | Intentionally not created without live Gradle run; likely needed on first JDK 17 AGP `8.7.2` sync (`android.useAndroidX=true`). |
| `REM-C02` / `REM-H01` | C / H | `COMPLETE_NOT_INDEPENDENTLY_VERIFIED` / `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` | **P0** | `./gradlew testDebugUnitTest` attempted -> `EXIT=1` (`125` `@Test`s / `0` `@Ignore` verified on disk). |
| `REM-E01` | E — Steam | `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` | **P0** | External NIST REFPROP / CoolProp comparison outside container (`CoolProp`/`iapws` absent). |
| `REM-F01` | F — Usability | `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` | **P0** | API 34/35 Emulator / `@Preview` walkthrough awaits Android Studio host. |
| `REM-H02` | H — QA | `BLOCKED_ENVIRONMENT` | `BLOCKED_ENVIRONMENT` | **P1** | `./gradlew lintDebug` attempted -> `EXIT=1`. |

*(All remaining P1–P3 items `REM-A04`, `REM-A05`, `REM-B01..B05`, `REM-C01/03/04`, `REM-D01/02`, `REM-E02/03`, `REM-F02..F04`, `REM-G01..G03`, `REM-H03/H04` remain unchanged from `DOC-MPN-043`.)*

---

## 10. Files Modified & Git Safety Confirmation

- **Modified Executable Permission Only:** `android/gradlew` (`0644` -> `0755`, script content SHA-256 unchanged).
- **Created Documentation:** `docs/BATCH_15A_NATIVE_VERIFICATION_REPORT.md` (`DOC-MPN-044`).
- **Synchronized Continuity Docs:** `docs/CURRENT_WORK_CHECKPOINT.md` (`DOC-MPN-004`), `docs/SESSION_HANDOVER.md` (`DOC-MPN-006`), `docs/PHASE_PROGRESS_TRACKER.md` (`DOC-MPN-003`).
- **Zero Modifications To:** Kotlin source code, XML layouts/drawables, Gradle scripts/TOML, Room schema (`version = 1`), `BackupJsonCodec` (`backup_version = 2`), SQLite asset `mechanical_pro_notes.db`, or engineering calculations/formulas.
- **Git Status:** `.git` repository directory is not initialized in `/app/applet`; zero `git commit`, `git push`, deployment, or release actions occurred.
