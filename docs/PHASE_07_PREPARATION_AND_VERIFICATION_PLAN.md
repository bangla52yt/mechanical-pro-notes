# PHASE 07 PREPARATION & VERIFICATION PLAN
## Offline Application Validation, Physical Device Verification & Field Reliability Protocol

**Document Identifier:** DOC-MPN-018  
**Project:** Mechanical Pro Notes (Personal Offline-First Mechanical Reference Android App)  
**Phase:** Phase 07 — Offline Validation & Field Testing  
**Status:** SCOPE PREPARED & ON STANDBY — AWAITING EXPLICIT OWNER EXECUTION AUTHORIZATION  
**Date:** 12 November 2026  
**Governing Standard:** IEEE 1012 (System and Software Verification and Validation), ISO/IEC 25010, ASME PTC 4, IAPWS-IF97  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Executive Context & Scope Mandate

The Project Owner has formally accepted Phase 06 as:
**PASS WITH CONDITIONS — CONDITIONAL OWNER ACCEPTANCE** subject to nine specific governing conditions:
1. Preserve all existing Phase 06 source code, database changes, tests, and documentation.
2. Do not repeat the entire Phase 06 audit or unnecessarily recreate completed implementations.
3. Preserve all 20 calculation modules and existing Phase 05 calculation implementations.
4. Carry forward unresolved engineering verification items into Phase 07.
5. The IAPWS-IF97 engine must **not** be described as fully accuracy-verified until numerical benchmark values, independent reference comparisons, and documented error measurements are available.
6. `CALC-02`, `CALC-09`, `CALC-14`, `CALC-16`, `CALC-18`, and `CALC-20` must retain appropriate technical verification and safety restrictions.
7. Do not claim successful native Android compilation or physical-device verification until executed on actual hardware with the Android SDK toolchain.
8. Preserve `DEC-014` (Personal Data Isolation), `DEC-016` (Zero Internet Permission), Free-First policy (`DEC-006`), personal-use scope (`DEC-003`), and all existing project boundaries.
9. No unrelated code changes, Git commits, pushes, deployment, paid services, or external integrations.

**Zero Premature Implementation Directive:** This document establishes the scope, testing matrix, and validation protocols for Phase 07. **No implementation or source code modifications will occur until the Project Owner explicitly authorizes Phase 07 execution.**

---

## 2. Phase 07 Core Objectives & Verification Pillars

Phase 07 will rigorously evaluate the entire **Mechanical Pro Notes** application across eight core pillars:

```
┌────────────────────────────────────────────────────────────────────────┐
│                      PHASE 07 VERIFICATION PILLARS                     │
├──────────────────────────────────┬─────────────────────────────────────┤
│ 1. Offline Application Validation│ 5. Steam Engine Benchmark Validation│
│ 2. Native Compilation Strategy   │ 6. Database & Personal Data (DEC-014│
│ 3. Physical-Device Test Protocol │ 7. Navigation & UI Runtime Testing  │
│ 4. Engineering Calculation Audit │ 8. Plant Floor Field Reliability    │
└──────────────────────────────────┴─────────────────────────────────────┘
```

---

## 3. Pillar 1: Offline Application Validation (100% Air-Gapped)

- **Air-Gapped Operation:** The application must function with 100% feature parity with Airplane Mode enabled and Wi-Fi / Mobile Data disabled.
- **Hardware-Level Confinement:** Continued enforcement of **DEC-016**:
  - Verify that `android.permission.INTERNET` remains completely absent from `AndroidManifest.xml`.
  - Verify zero runtime socket creation, zero DNS resolution attempts, and zero HTTP client instantiations.
- **Local Asset Resolution:** Verify that all 64 equipment entries, 21-section blueprint chapters, 20 calculation definitions, formulas, and SVG/vector diagrams resolve exclusively from local APK assets and the embedded Room SQLite database (`mechanical_pro_notes.db`).
- **Zero Cloud Crash Reporting:** Verify zero third-party telemetry, analytics SDKs (Firebase Analytics, Crashlytics, Mixpanel), or ad trackers.

---

## 4. Pillar 2: Native Android Compilation Strategy

### 4.1 Truthful Environment Demarcation
- The current Gemini Workspace Linux container is an ephemeral web/Node.js environment lacking OpenJDK 17+ and the Android Command-Line SDK (`aapt2`, `d8`, `kotlinc`, platform-tools).
- **Prohibition:** No claim of successful native APK compilation (`.apk` binary generation) will be made inside this container.

### 4.2 External Build Toolchain Validation Strategy (When Authorized)
When the project owner prepares to compile the debug/release APK on their own development machine or CI environment, the build plan requires:
1. **Toolchain Baseline:**
   - OpenJDK 17 or OpenJDK 21 LTS.
   - Android Gradle Plugin (AGP) `8.7.2`.
   - Kotlin Gradle Plugin `2.0.21` with Compose Compiler plugin.
   - Android SDK Platform `android-35` (Target SDK 35, Min SDK 26).
   - Android SDK Build-Tools `35.0.0`.
2. **Build Commands to Verify:**
   - `./gradlew check` (Runs static analysis, ktlint, and Android lint).
   - `./gradlew testDebugUnitTest` (Runs local JVM unit tests).
   - `./gradlew assembleDebug` (Generates unaligned debug APK for side-loading).
3. **Workspace Pre-Validation:** Maintain 100% syntax compliance, valid `libs.versions.toml` dependencies, and zero broken package imports across all 40+ Kotlin source files.

---

## 5. Pillar 3: Physical-Device Testing Protocol

- **Target Device Profile:** Project Owner's personal Android smartphone.
- **Form Factor & Density Coverage:**
  - Modern tall aspect ratios (19.5:9, 20:9, 21:9).
  - High density displays (xhdpi, xxhdpi, xxxhdpi).
  - Android versions: Android 8.0 (API 26) through Android 15 (API 35).
- **Usability & Ergonomics on the Plant Floor:**
  - **Single-Handed Accessibility:** Bottom navigation bar, floating action buttons, and critical calculation buttons reachable with one thumb while standing on a boiler platform or machine catwalk.
  - **Touch Target Sizing:** Minimum $48 \times 48\text{ dp}$ touch targets for all buttons, chips, and input fields to support operation with industrial work gloves.
  - **High-Contrast Dark Theme:** Industrial Navy (`#0A192F` background, `#172A45` surface) and Safety Amber (`#FFB74D`) optimized for visibility under low boiler room lighting and direct factory fluorescent lighting.

---

## 6. Pillar 4: Engineering Calculation Verification & Safety Restrictions

In strict accordance with Owner Condition 6, specific technical verification and safety restrictions must be enforced across all 20 calculators, with dedicated focus on six specific modules:

### 6.1 Targeted Verification & Safety Restrictions
1. **`CALC-02` (Indirect Thermal Efficiency / Heat Loss Method):**
   - *Restriction:* Fuel ultimate analysis (C, H, O, N, S, Moisture, Ash) requires clear demarcation between standard pipeline natural gas and variable garment jhut waste.
   - *Verification:* Compare individual losses ($L_1$ to $L_6$) against ASME PTC 4 / BS 845 worked industrial energy audit reports. Display amber advisory regarding fuel moisture variance.
2. **`CALC-09` (Safety Valve Relieving Capacity Verification):**
   - *Safety Restriction:* **Mandatory Regulatory Inviolability.** The calculation is an engineering check only and cannot authorize valve setting changes or legal stamp certification.
   - *Verification:* Enforce permanent warning banner citing Bangladesh Boilers Act 2022 and OCIB 2025 Rules.
3. **`CALC-14` (Steam Header & Main Pipe Sizing):**
   - *Restriction:* Velocity limits must be constrained to safe industrial limits ($15 - 35\text{ m/s}$ for saturated steam, $25 - 45\text{ m/s}$ for superheated steam).
   - *Verification:* Check acoustic noise and erosion thresholds for undersized pipes; verify standard nominal bore (DN) mapping against ASME B36.10M.
4. **`CALC-16` (Hydro Extractor Centrifugal G-Force & Critical Speed Safety):**
   - *Safety Restriction:* High kinetic energy hazard. If user input speed exceeds manufacturer rated nameplate RPM ($N > N_{rated}$), UI must trigger an immediate high-priority red alert: **"CRITICAL OVER-SPEED DANGER: BASKET BURST HAZARD"**.
   - *Verification:* Assert rim velocity ($V_{rim}$) and dynamic hoop stress equations.
5. **`CALC-18` (Tumble Dryer Moisture Evaporation Rate & Airflow):**
   - *Restriction:* Must account for lint screen backpressure and sensible heat losses through uninsulated exhaust ductwork.
   - *Verification:* Validate psychrometric moisture carrying capacity ($\Delta \omega$) under factory humidity conditions.
6. **`CALC-20` (Compressed Air Line Pressure Drop):**
   - *Restriction:* Darcy-Weisbach flow must incorporate friction factor changes with Reynolds number.
   - *Verification:* Validate that total pressure drop in distribution mains remains $< 0.1\text{ bar}$ per $100\text{ m}$ of pipe.

---

## 7. Pillar 5: Steam Engine Numerical Benchmark Validation

In strict accordance with **Owner Condition 5**:
> *"The IAPWS-IF97 engine must not be described as fully accuracy-verified until numerical benchmark values, independent reference comparisons, and documented error measurements are available."*

### 7.1 Benchmark Validation Protocol
Phase 07 will establish a formal numerical error matrix comparing `IapwsIf97SteamEngine.kt` outputs against the official **NIST REFPROP / IAPWS-IF97 Standard Reference Database** across 20 distinct pressure points from $0.50\text{ bar(a)}$ to $20.00\text{ bar(a)}$:
1. **Parameters Evaluated:**
   - Saturation Temperature: $T_{sat}(P)\ [^\circ\text{C}]$
   - Saturated Liquid Enthalpy: $h_f(P)\ [\text{kJ/kg}]$
   - Latent Heat of Evaporation: $h_{fg}(P)\ [\text{kJ/kg}]$
   - Saturated Vapor Enthalpy: $h_g(P)\ [\text{kJ/kg}]$
   - Saturated Vapor Specific Volume: $v_g(P)\ [\text{m}^3/\text{kg}]$
   - Saturated Liquid Specific Volume: $v_f(P)\ [\text{m}^3/\text{kg}]$
   - Saturated Vapor Density: $\rho_g(P)\ [\text{kg/m}^3]$
2. **Acceptance Criteria:**
   - Temperature Deviation: $|T_{calc} - T_{ref}| \le 0.05^\circ\text{C}$.
   - Enthalpy Deviation: $\frac{|h_{calc} - h_{ref}|}{h_{ref}} \times 100 \le 0.10\%$.
   - Specific Volume Deviation: $\frac{|v_{calc} - v_{ref}|}{v_{ref}} \times 100 \le 0.20\%$.
   - Thermodynamic Identity Exactness: $|h_g - (h_f + h_{fg})| \equiv 0.00\text{ kJ/kg}$.

---

## 8. Pillar 6: Database Integrity & Personal Data Preservation (DEC-014)

- **Static vs Dynamic Isolation:**
  - Verify that pre-packaged catalog tables (`equipment`, `component`, `technical_parameter`, `calculation_definitions`, `formula_variables`) are opened read-only or updated strictly via managed Room migrations.
  - Verify that personal tables (`user_notes`, `calculation_history`, `checklists`, `bookmarks`) have zero foreign-key cascade dependencies on static tables.
- **Zero Data Loss on Deletion:**
  - Confirm that deleting or resetting calculation caches does not delete user notes or inspection records.
- **Local Backup / Export Protocol:**
  - Verify JSON backup export and import via Android Storage Access Framework (SAF) in `SettingsScreen.kt`.

---

## 9. Pillar 7: Navigation & UI Runtime Testing

- **Navigation Topology:**
  - Verify all 17 drawer destinations in `Screen.kt` and `AppNavigation.kt`.
  - Verify bottom navigation destinations: Home, Equipment, Calculations, Notes.
  - Verify deep-linking between `CalculationListScreen` $\leftrightarrow$ `CalculationRunnerScreen` and `SteamTableScreen`.
  - Verify 21-section machinery reader scrolling performance and content depth badge fidelity.
- **Reactive StateFlow Discipline:**
  - Verify zero memory leaks across screen rotation or process recreation.
  - Verify that calculation results update reactively without freezing the Compose UI thread.

---

## 10. Pillar 8: Plant Floor Field Reliability & Safety Review

- **Field Environment Simulation:**
  - Harsh factory ambient noise and high-temperature environments.
  - Intermittent shift handovers: Verify that calculation inputs and user notes persist across app force-close.
  - Rapid reference retrieval: Search response latency must remain under $50\text{ ms}$ for FTS5 queries.
- **Safety Alerts & Regulatory Citations:**
  - Verify that references to **The Boilers Act, 2022 (Act No. 06 of 2022)** and **Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)** appear correctly in boiler checklists, annual renewal notices, and inspection schedules.

---

## 11. Phase 07 Deliverables & Documentation Plan

When authorized, Phase 07 will produce:
1. `docs/PHASE_07_VERIFICATION_REPORT.md` (Detailed verification results across all 8 pillars).
2. `docs/PHASE_07_STEAM_BENCHMARK_ERROR_MATRIX.md` (Independent reference comparisons and documented error measurements).
3. `docs/PHASE_07_FIELD_SAFETY_AUDIT.md` (Safety restrictions and regulatory compliance audit).
4. `docs/PHASE_07_KNOWN_LIMITATIONS.md` (Updated environmental and physical device findings).
5. Comprehensive automated test harness `test_suite_phase07.py`.

---

## 12. Execution Pre-conditions & Halt Directive

- **PRE-CONDITION CHECK:** Phase 06 conditionally accepted by the Project Owner.
- **HALT DIRECTIVE:** In strict compliance with the Project Owner's instructions, **NO PHASE 07 IMPLEMENTATION CODE HAS BEEN CREATED OR ALTERED**.
- **AWAITING OWNER COMMAND:** The assistant is standing by at the execution boundary awaiting explicit owner instruction before Phase 07 testing commences.
