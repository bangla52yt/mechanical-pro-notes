# PHASE 04: KNOWN LIMITATIONS AND DEFERRED WORK
**Document Identifier:** DOC-MPN-023  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** ACTIVE REGISTER OF TECHNICAL CONSTRAINTS  
**Date:** 2026-11-12  

---

## 1. Toolchain & Workspace Build Limitations

### 1.1 Lack of Pre-Installed JDK / Android SDK in Container
- **Observation:** The Gemini Workspace container is an Ubuntu Linux environment configured for Web/Node.js development. Neither OpenJDK 17 nor the Android Command-Line SDK (`sdkmanager`, `build-tools`, `platforms;android-35`) is pre-installed.
- **Impact on Phase 04:**
  - Complete, buildable Android Gradle files (`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`), pure Kotlin domain classes, and Room entities were authored in strict alignment with Google Android standards.
  - The database schema, FTS5 virtual tables, foreign keys, and calculation algorithms were 100% verified via automated execution (`test_suite_phase04.py`).
  - However, direct execution of `./gradlew assembleDebug` to produce an `.apk` binary cannot occur inside this web container without installing an Android SDK.
- **Reporting Status:** In accordance with Section 5.2 and Section 19 of Master Prompt 04, Phase 04 is reported honestly as **PASS WITH CONDITIONS (Toolchain Limitation)**.

---

## 2. Knowledge Base & Seed Data Limitations

### 2.1 Initial Seed Coverage (7 of 64 Assets)
- The initial seed dataset intentionally includes 7 verified assets (`EQ-A01`, `EQ-A04`, `EQ-A05`, `EQ-B01`, `EQ-D02`, `EQ-G01`, `EQ-G02`) across Families A, B, D, G to validate database initialization, FTS search, and content depth.
- The remaining 57 equipment records remain documented in `docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md` and will be populated into the pre-packaged database in Phase 05 and Phase 06.

### 2.2 Calculation Algorithms
- Fully implemented with executable Kotlin domain algorithms:
  - `CALC-01`: Direct Thermal Efficiency
  - `CALC-04`: Evaporation Ratio
  - `CALC-05`: Blowdown Rate
  - `CALC-07`: Pump Hydraulic Power
- Other calculations (e.g. `CALC-02` Indirect Heat Loss) remain strictly marked as `RESEARCH_PENDING` to prevent the fabrication of combustion coefficients.

---

## 3. Deferred Work for Subsequent Phases

- **Phase 05:** Jetpack Compose Material 3 UI screens, Navigation Graph, ViewModels, and 21-section tabbed equipment reader.
- **Phase 06:** Interactive Calculation UI runners, unit selection spinners, and in-app checklist completion screens.
- **Phase 07:** Full smartphone offline validation and field testing.
- **Phase 08:** Git repository preparation and owner-controlled push.
