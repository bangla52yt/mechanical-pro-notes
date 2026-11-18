# Mechanical Pro Notes

**Personal Offline-First Mechanical Engineering & Garment Washing Plant Maintenance Reference App (Android)**

Target repository: `researchbangla/mechanical-pro-notes` (`DEC-002`)

---

## Key Architecture & Safety Policies

- **Zero Network Policy (`DEC-016`):** `android.permission.INTERNET` and `ACCESS_NETWORK_STATE` are completely absent from `AndroidManifest.xml`, and `android:allowBackup="false"` is enforced. All knowledge lookup, full-text search, steam tables, and engineering calculations execute 100% locally.
- **Personal Data Separation (`DEC-014`):** Personal field notes (`user_note` with `ON DELETE NO ACTION` foreign key protection), bookmarks (`user_bookmark`), calculation history (`calculation_history`), and checklist state (`checklist_progress`) are isolated from static equipment seed updates and support **SHA-256 checksummed JSON export/import** via Android Storage Access Framework (SAF).
- **Content-Depth Transparency (`DEC-009`):** All **64 equipment records** across **16 system families (`FAM-A`..`FAM-P`)** display honest status badges (`FULL_CONTENT_BLUEPRINT`, `PARTIAL_CONTENT_BLUEPRINT`, `RESEARCH_PENDING`, `INVENTORY_ONLY`).
- **Dedicated Boiler Module (`DEC-008`):** Industrial gas and jhut/biomass boilers (`FAM-G`) are separated into a top-level dashboard citing **The Boilers Act, 2022 (Act No. 06 of 2022)** and **Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)**.
- **Safety Governance & Calculation Boundaries:**
  - `CALC-01`..`CALC-20` implemented across boiler thermal efficiency/utilities and wash plant maintenance tools (`CALC-02` retains `RESEARCH_PENDING` governance status until plant gas-analyzer calibration and jhut elemental fuel limits are established).
  - Offline saturated steam table (`0.50`–`20.00 bar a`) using approved **linear-pressure interpolation** across a **56-node** IAPWS-IF97 saturation grid (`21/21` points reconciled to DOC-MPN-016 anchors; independent external NIST REFPROP verification remains recommended prior to statutory certification).

---

## Tech Stack

- **Language & UI:** Kotlin `2.0.21`, Jetpack Compose (BOM `2024.10.01`), Material 3 (`1.3.0`), Material Icons Extended
- **Persistence:** Android Room `2.6.1` (KSP) + SQLite `FTS4` bilingual English/Bengali search (`mechanical_pro_notes.db`) + Jetpack DataStore Preferences
- **Build Target:** Android Gradle Plugin `8.7.2`, Gradle `8.9`, JDK `17`, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`

---

## Building the APK on Android Studio / Local Workstation

The Linux workspace container used to author this repository does not bundle JDK 17 or Android SDK, while the official **Gradle 8.9 wrapper binary (`android/gradle/wrapper/gradle-wrapper.jar`)** and `gradlew` scripts are already included. To compile and install on your personal Android phone:

1. **Install Prerequisites:** OpenJDK 17 and Android Studio (Ladybug+ with Android SDK Platform 35 & Build-Tools 35).
2. **Run JVM Unit Tests:**
   ```bash
   cd android
   ./gradlew testDebugUnitTest
   ```
3. **Build Debug APK & Install via USB/Wireless ADB:**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   *(You can also open the `android/` directory directly in Android Studio to sync KSP, inspect Vector Drawable schematics, and run directly onto your USB/wireless Android phone.)*
4. **Rebuilding Pre-Populated SQLite Seed DB (optional):**
   ```bash
   python3 build_database.py
   ```
5. **Running Python Structural & Formula Verification Suites:**
   ```bash
   python3 test_suite_phase04.py
   python3 test_suite_phase05.py
   python3 test_suite_phase06.py
   python3 test_suite_phase07.py
   ```

---

## Owner Git Push Checklist (Phase 08)

Per `DEC-002` and `DEC-004`, push only to `researchbangla/mechanical-pro-notes`:

```bash
git status
git add .
git commit -m "Complete Mechanical Pro Notes v1.0.0 offline Android codebase and Phase 07/08 readiness"
git remote add origin https://github.com/researchbangla/mechanical-pro-notes.git
git push -u origin main
```
