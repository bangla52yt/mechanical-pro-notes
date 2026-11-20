# Mechanical Pro Notes

**Offline-First Mechanical Engineering & Garment Washing Plant Maintenance Reference App (Android)**

Target repository: `researchbangla/mechanical-pro-notes` (`DEC-002`)

---

## Key Architecture & Safety Policies

- **Zero Network Policy (`DEC-016`):** `android.permission.INTERNET` and `ACCESS_NETWORK_STATE` are completely absent from `AndroidManifest.xml`, and `android:allowBackup="false"` is enforced. All knowledge lookup, full-text search, steam tables, and engineering calculations execute 100% locally.
- **Personal Data Separation (`DEC-014`):** Personal field notes (`user_note`), bookmarks (`user_bookmark`), calculation history (`calculation_history`), and checklist progress (`checklist_progress`) live in isolated Room tables with zero cascading links to static equipment rows, alongside custom checklists and execution metadata in DataStore Preferences.
- **Backup v3 Portability & Recovery:** Supports **SHA-256 checksummed JSON export/import (`backup_version = 3`)** covering all 6 personal collections (`user_notes`, `user_bookmarks`, `checklist_progress`, `calculation_history`, `custom_checklists`, `checklist_execution_metadata`) via Android Storage Access Framework (SAF), with pre-restore snapshot and compensating rollback across Room + DataStore on error and backward compatibility with v1/v2 backups.
- **Content-Depth Transparency (`DEC-009`):** All **64 equipment records** across **16 system families (`FAM-A`–`FAM-P`)** display explicit status badges (**`56` `FULL_CONTENT_BLUEPRINT`**, **`1` `PARTIAL_CONTENT_BLUEPRINT`** [`EQ-G02`], **`7` `RESEARCH_PENDING`**, **`0` `INVENTORY_ONLY`**), **`18` offline AAPT2-safe Vector Drawable schematics**, and **`15/15` verified Mechanical Fundamentals (`FUND-01`–`FUND-15`)**.
- **Dedicated Boiler Module & Statutory Citations (`DEC-008`):** Industrial gas and jhut/biomass boilers (`FAM-G`) are separated into a top-level dashboard citing **The Boilers Act, 2022 (Act No. 06 of 2022)**, **Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)**, and **ISO 20816-3:2026 (`120–30,000 r/min`, `> 15 kW`)** condition monitoring boundaries.
- **Safety Governance & Calculation Boundaries:**
  - All **`20` calculations (`CALC-01`–`CALC-20`, `87` formula variables)** are `IMPLEMENTED` with strict `NaN`/`Infinity` and physical bounds validation.
  - **`CALC-02` (Indirect Boiler Efficiency):** Verified for natural gas (BS 845-1 / IS 8753, enforcing mass `kJ/kg` vs volumetric `kJ/Nm3 + fuel_density` parity); solid/mixed garment **jhut** (`fuel_type = 2`) retains an explicit **`UNVERIFIED` calibration boundary** requiring lab-measured GCV (`kJ/kg`), H2%, `siegert_k`, `theoretical_air`, `M_fuel`, and `L_unburnt`.
  - **Saturated Steam Table (`IapwsIf97SteamEngine`):** IAPWS-IF97 Region 4 only (`0.50–20.00 bar(a)`, dynamic gauge offset `Patm` defaulting to `1.01325 bar`), using exact Region 4 Equation 31 for `T_sat(P)`, log-pressure interpolation for `v_g`, and 56-node linear-P interpolation for `h_f` / `h_fg`. Independent external **NIST REFPROP / CoolProp** solver cross-check remains recommended prior to statutory certification.

---

## Tech Stack & Workspace Layout

- **Authoritative Android App (`android/`):** Kotlin `2.0.21`, Jetpack Compose (BOM `2024.10.01`), Material 3 (`1.3.0`), Material Icons Extended, Room `2.6.1` (KSP) + SQLite `FTS4` bilingual English/Bengali search (`mechanical_pro_notes.db`, `user_version = 1`), Jetpack DataStore Preferences, `kotlinx.serialization`.
- **Build Target:** Android Gradle Plugin `8.7.2`, Gradle `8.9`, JDK `17`, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`.
- **Root Workspace Helper Files:**
  - `build_database.py`, `seed_equipment_part1.py`, `seed_equipment_part2.py`, `seed_phase06_calculations.py` — authoritative Python generators that built `android/app/src/main/assets/database/mechanical_pro_notes.db` (`856,064` bytes). *(Note: `docs/archive/seed_data.sql` is an obsolete Phase 04 partial draft relocated out of Android assets and kept only for historical context.)*
  - `test_suite_phase04.py`..`07.py` & `test_suite_stage01.py`..`04.py` — offline Python DB, engineering formula parity, and Kotlin static integrity suites.
  - `visual-preview-gallery/` — standalone historical HTML mockup viewer for quick browser inspection (non-authoritative; does not replace the Jetpack Compose app).
  - `index.html`, `package.json`, `vite.config.ts`, `src/` — container environment web host scaffolding unused by the Android app.

---

## Building & Testing on Android Studio / Local Workstation

The Linux container used for authoring does not bundle JDK 17 or Android SDK 35, while the official **Gradle 8.9 wrapper (`android/gradlew`, `gradle-wrapper.jar`)** and `android/gradle.properties` are included. Run native Android build and test verification on a workstation with **JDK 17** and **Android SDK Platform 35**:

1. **JVM Unit Tests (12+ test files in `android/app/src/test/`):**
   ```bash
   cd android
   ./gradlew testDebugUnitTest
   ```
2. **Android Lint & Debug APK:**
   ```bash
   ./gradlew lintDebug assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. **Android Instrumented Tests (`android/app/src/androidTest/`):**
   Connect an API 26–35 Android device or emulator (`androidx.test` Room asset opening, FTS4 bilingual search, Backup v3 round-trip, and `MainActivity` launch/recreation smoke tests):
   ```bash
   ./gradlew connectedDebugAndroidTest
   ```
4. **Release APK Signing (Personal / Owner Build):**
   `release` build type enables R8 minification and resource shrinking (`android/app/proguard-rules.pro`). Do **not** commit keystores or passwords into the repository. Configure your private signing keystore locally via `keystore.properties` (excluded by `.gitignore`) or Gradle `-P` / environment variables inside `android/app/build.gradle.kts`, then run:
   ```bash
   ./gradlew assembleRelease
   ```
5. **Running Offline Python Verification Suites (from repo root):**
   ```bash
   python3 test_suite_phase04.py
   python3 test_suite_phase05.py
   python3 test_suite_phase06.py
   python3 test_suite_phase07.py
   python3 test_suite_stage01.py
   python3 test_suite_stage02.py
   python3 test_suite_stage03.py
   python3 test_suite_stage04.py
   ```
   *(Pre-built `android/app/src/main/assets/database/mechanical_pro_notes.db` is committed directly; only re-run `python3 build_database.py` if intentionally modifying seed scripts.)*

