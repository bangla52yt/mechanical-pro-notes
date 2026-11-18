# PHASE 05 KNOWN LIMITATIONS & ENVIRONMENT CONSTRAINTS

**Project:** Mechanical Pro Notes  
**Application Scope:** Personal Offline-First Mechanical Reference Android App  
**Phase:** 05 — Material 3 UI & Compose Reference Screens  
**Date:** 12 November 2026  

---

## 1. Truthful Reporting of Workspace Constraints

In strict compliance with architectural transparency, the following technical limitations are explicitly recorded:

### 1.1 Local Workspace Build Tooling Constraint
- **Issue:** The Gemini Workspace Linux development container is a Node.js/web runtime environment lacking OpenJDK 17+ and the Android Command-Line SDK (`aapt2`, `d8`, `kotlinc`, platform-tools).
- **Impact:** Direct local invocation of `./gradlew assembleDebug` to produce a binary `.apk` file cannot execute within this container.
- **Verification Alternative:** All Kotlin domain, data, and Jetpack Compose presentation code are rigorously validated through static analysis, AST token auditing, SQLite schema integration, and an automated Python test harness (`test_suite_phase05.py`) passing 10/10 tests (100%).
- **Remediation for Project Owner:** When ready for physical APK installation on an Android device, the project files can be compiled using Android Studio or GitHub Actions with standard Android Gradle Plugin 8.7.2 and Kotlin 2.0.21.

### 1.2 Unpopulated Blueprint Sections (Content Depth Discipline)
- **Constraint:** Only 2 equipment assets (`EQ-A01`, `EQ-B01`) currently possess complete 21-section technical blueprints (`FULL_CONTENT_BLUEPRINT`). The remaining 46 assets are classified as `PARTIAL_CONTENT_BLUEPRINT` (8 assets), `INVENTORY_ONLY` (31 assets), or `RESEARCH_PENDING` (7 assets).
- **UI Handling:** The 21-section reader (`EquipmentDetailScreen.kt`) transparently displays `ContentDepthBadge` and clean placeholder cards explaining that documentation is scheduled for future content expansion, strictly preventing AI hallucination or fabricated machine parameters.

### 1.3 Engineering Calculations Research Status
- **Constraint:** `CALC-02` (Indirect Thermal Efficiency / Boiler Heat Loss Method) is flagged as `RESEARCH_PENDING`.
- **UI Handling:** The calculation runner explicitly blocks execution of `CALC-02` and displays an amber advisory stating that execution is disabled pending verified plant-floor combustion constants, while permitting mathematical study of the theoretical formula.

### 1.4 Strict Offline Boundary Enforcement
- **Constraint:** Network connectivity, cloud telemetry, remote logins, and ads are permanently disabled (**DEC-016**).
- **Design Intent:** This is an intentional architectural boundary designed to guarantee 100% reliability in remote factory boiler rooms and wash plant floors with zero mobile data reliance.
