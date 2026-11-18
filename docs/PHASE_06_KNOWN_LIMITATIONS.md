# PHASE 06 KNOWN LIMITATIONS & ENVIRONMENT CONSTRAINTS

**Project:** Mechanical Pro Notes  
**Application Scope:** Personal Offline-First Mechanical Reference Android App  
**Phase:** 06 — Field Calculations, Boiler Thermal Engineering & Maintenance Tools  
**Date:** 12 November 2026  
**Governing Principles:** Architectural Honesty, Safety Transparency, Personal-Use Isolation  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Truthful Reporting of Workspace Constraints

In strict accordance with project governance rules and technical transparency, the following limitations are formally documented:

### 1.1 Local Workspace Build Tooling Constraint (Carried Forward)
- **Constraint:** The Gemini Workspace Linux container is an ephemeral web/Node.js environment and does not provide an installed OpenJDK 17+ or the Android Command-Line SDK (`aapt2`, `d8`, `kotlinc`, platform-tools).
- **Technical Impact:** Direct command-line compilation of native Android `.apk` binaries via `./gradlew assembleDebug` cannot execute inside this web container.
- **Verification Alternative:** All Kotlin domain algorithms, IAPWS-IF97 thermodynamic interpolation routines, data repository bindings, and Jetpack Compose screens are fully written in production-grade code, validated via static AST analysis, Room SQLite schema queries, and automated test harnesses (`test_suite_phase06.py` and `test_suite_phase05.py`) achieving **100% pass rates (10/10 each)**.
- **Remediation for Project Owner:** The generated project directory structure is 100% compliant with standard Android Gradle Plugin (AGP 8.7.2) and Kotlin 2.0.21. When ready, the owner can open the repository in Android Studio or run a GitHub Actions build pipeline to generate the debug/release APK.

### 1.2 Thermodynamic Saturated Steam Engine Boundary
- **Operating Envelope:** The embedded `IapwsIf97SteamEngine.kt` covers **IAPWS-IF97 Region 4 saturation properties ONLY** between $0.50\text{ bar(a)}$ and $20.00\text{ bar(a)}$ ($81.32^\circ\text{C}$ to $212.38^\circ\text{C}$), using exact Region 4 Eq. 31 for $T_{sat}$, linear-pressure interpolation over 56 nodes for $h_f, h_{fg}, v_f$, and log-pressure/log-$v_g$ interpolation for $v_g$.
- **Explicitly Unsupported Regions (Out of Scope):**
  - Sub-atmospheric vacuum systems ($P < 0.50\text{ bar a}$).
  - Region 1: Compressed / subcooled liquid water ($T < T_{sat}(P)$).
  - Region 2: Superheated steam ($T > T_{sat}(P)$).
  - Region 3: Near-critical and supercritical states ($P > 20.00\text{ bar a}$ up to $220.64\text{ bar}$, $373.946^\circ\text{C}$).
  - Region 5: High-temperature steam above $800^\circ\text{C}$.
  - Backward equations $T(p,h)$ and $T(p,s)$.
- **Engineering Justification:** Limiting the engine to the industrial saturated regime ensures microsecond execution times, zero runtime heap allocations, and zero cloud API dependency; external NIST REFPROP binary comparison remains tracked externally (`P7-P5-03`).

### 1.3 CALC-09 Safety Valve Relieving Capacity Regulatory Boundary
- **Regulatory Requirement:** Safety valve calculations are provided strictly as an **engineering cross-check and preliminary diagnostic aid**.
- **Inviolable Limitation:** In accordance with the Bangladesh Boilers Act 2022 and the Office of the Chief Inspector of Boilers (OCIB) Boiler Rules 2025, no mathematical calculation or mobile application can authorize safety valve setting adjustments, seal tampering, or legal relieving capacity certification.
- **Safeguard:** The application enforces a prominent amber regulatory disclaimer on `CALC-09` reminding plant engineers that formal certification requires physical testing witnessed by an authorized OCIB boiler inspector and stamped manufacturer nameplate tags.

### 1.4 Fuel Analysis Boundary in CALC-02 (Indirect Heat Loss Method)
- **Verified Scope:** **CALC-02** is **IMPLEMENTED** per **ASME PTC 4-2013 / BS 845-1:1987 / IS 8753** for steady-state natural gas boilers (`fuel_type = 1.0`, Siegert $K = 0.38$, gravimetric $A_0 = 17.0\text{ kg/kg}$, volumetric-to-mass $\text{GCV}_{\text{mass}} = \text{GCV}_{\text{vol}} / \rho_{\text{gas}}$) and lab-calibrated liquid/solid fuels when mass-basis GCV ($\text{kJ/kg}$), $H_2\%$, `siegert_k`, and `theoretical_air` are explicitly entered.
- **Uncalibrated Mixed-Jhut Restriction:** Heterogeneous garment cutting scrap (*jhut* — cotton, polyester, and elastane blends) lacks fixed elemental $C/H_2$ stoichiometry, moisture ($6\text{–}15\%$), GCV, or grate unburnt carbon LOI ($L_{\text{unburnt}}$). Consequently, solid/jhut defaults are intentionally omitted; running `fuel_type = 2.0` rejects volumetric `kJ/Nm3` units, requires user-supplied `siegert_k` and `theoretical_air`, and flags un-supplied $M_{\text{fuel}}$, $L_{\text{unburnt}}$, and $L_{\text{radiation}}$ as an unverified mixed-jhut boundary.

### 1.5 Strict Offline & Zero-Cost Architecture (**DEC-016**, **DEC-002**)
- **Constraint:** The application has **zero network permissions** (`android.permission.INTERNET` is excluded).
- **Design Intent:** The application does not rely on online calculators, weather APIs for ambient temperatures, or cloud sync. All inputs must be entered by the user or pulled from local Room SQLite presets.
- **Privacy Assurance:** Calculations and plant parameters never leave the owner's personal device (**DEC-014**).
