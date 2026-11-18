# MASTER PROJECT PLAN: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-001  
**Project:** Mechanical Pro Notes (Personal Mechanical Knowledge & Plant Maintenance Reference App)  
**Target User:** Project Owner (Personal Android Device Exclusive Use)  
**Future Repository Target:** `researchbangla/mechanical-pro-notes`  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** ACTIVE BASELINE — CHECKPOINT SYNCHRONIZED (P0 RECOVERY)  
**Original Baseline Date:** 2026-11-11  
**Last Updated:** 2026-11-16  

---

## 1. Information Classification Taxonomy
Every statement, requirement, and technical proposal in this master plan is governed by the following strict classification:
- **[A. VERIFIED CURRENT FACT]:** Empirically verified in the active Gemini Workspace environment.
- **[B. HISTORICAL REFERENCE]:** Preserved solely as historical background; NOT an active task or dependency.
- **[C. OWNER-APPROVED REQUIREMENT]:** Explicitly mandated and approved by the Project Owner.
- **[D. PROPOSED DECISION]:** Technical or architectural recommendation awaiting formal owner review and approval.
- **[E. UNVERIFIED INFORMATION]:** Assumed or reported previously but lacking formal verification.
- **[F. UNKNOWN OR MISSING INFORMATION]:** Knowledge gaps requiring owner clarification or field data.

---

## 2. Authoritative Project Purpose & Scope

### 2.1 Core Mission [C. OWNER-APPROVED REQUIREMENT]
Mechanical Pro Notes is a **Personal Mechanical Knowledge & Plant Maintenance Reference App** built initially and exclusively for the project owner's personal use on their own Android phone.

The application serves as a comprehensive, technically reliable, and mathematically rigorous engineering and maintenance reference covering:
1. Practical foundational mechanical engineering principles.
2. Complete industrial equipment, utilities, and transmission systems of a textile/garment washing plant.
3. A dedicated, top-level industrial boiler engineering suite (Gas-fired & Jhut/waste-fuel boilers).

### 2.2 Explicit Out-of-Scope Boundaries [C. OWNER-APPROVED REQUIREMENT]
Under explicit instruction from the Project Owner, the following concerns are strictly **OUT OF SCOPE** and will receive zero project effort or code allocation:
- Play Store deployment, packaging, or public listing.
- Public product marketing, branding, or launch.
- Monetization, commercial licensing, in-app purchases, or advertisements.
- Multi-user authentication, cloud accounts, or user access tiers.
- Anti-piracy, DRM, or duplicate copy protection mechanisms.
- Public-facing cloud business infrastructure.

---

## 3. Workflow & Git Management Protocol [C. OWNER-APPROVED REQUIREMENT]

### 3.1 Workspace-First Development Model
1. The project is created, developed, and maintained directly within the **Gemini Workspace** first.
2. All project documentation, plans, checklists, and source files are preserved within this workspace.
3. No external Grok recovery, Grok file comparison, or old Git forensic recovery will be performed. This is a fresh project baseline.
4. Remote GitHub repository creation and initial repository saving will occur later, after the Workspace project foundation is completely ready.

### 3.2 Owner-Controlled GitHub Push Protocol
1. The project owner personally performs all GitHub pushes.
2. The AI assistant prepares the project files, verifies workspace integrity, and provides exact, unambiguous recommended commit messages.
3. The AI assistant will never claim that a Git push occurred unless it has been independently and cryptographically verified.
4. No GitHub credentials or tokens will be requested in prompts or chats.

---

## 4. Required Knowledge Structure

### 4.1 Part A: General Mechanical Engineering Knowledge (Plant Maintenance Foundation) [C. OWNER-APPROVED REQUIREMENT]
The application includes foundational mechanical engineering knowledge structured to support practical plant maintenance, troubleshooting, and field calculations:
- Engineering Mathematics and Applied Mechanics.
- Engineering Mechanics (Statics, Dynamics, Forces, Equilibrium).
- Strength of Materials (Stress-strain, shear forces, bending moments, torsion, shaft deflection, pressure vessels).
- Theory of Machines & Mechanisms (Linkages, gear trains, cams, balancing, mechanical vibration).
- Machine Design (Shafts, bearings, couplings, keys, bolted/welded joints, springs, belts, brakes).
- Thermodynamics (Laws of thermodynamics, thermodynamic cycles, steam tables, Mollier charts).
- Fluid Mechanics & Hydraulic Machinery (Fluid statics, Bernoulli, pipe head loss, pumps, turbines).
- Heat Transfer (Conduction, convection, radiation, industrial heat exchangers).
- Manufacturing Technology & Workshop Practice (Machining, lathe, welding, pipe fitting, fabrication).
- Material Science & Metallurgy (Ferrous/non-ferrous alloys, heat treatment, corrosion prevention).
- Industrial Engineering & Plant Operations (Preventive maintenance systems, OEE, downtime reduction).
- Power Plant Engineering (Thermal cycle fundamentals, power generation auxiliaries).
- Refrigeration & Air Conditioning (Industrial cooling, chillers, psychrometry).
- Instrumentation & Control Fundamentals (Pressure gauges, RTD/thermocouples, flow meters, PLC basics).
- Mechanical Maintenance & Reliability Engineering (Root cause failure analysis, lubrication protocols, vibration analysis).

### 4.2 Part B: Complete Textile/Garment Washing Plant Mechanical Systems [C. OWNER-APPROVED REQUIREMENT]
Comprehensive coverage of machinery, piping, utilities, and auxiliaries found in textile/garment washing plants (documented in detail in `docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md`):
1. Washing machines & industrial washers (Belly washers, front/side loading, open-pocket, barrier washers, sample washers).
2. Hydro extractors & extraction systems (Centrifugal basket extractors, tilting hydro systems).
3. Industrial dryers & drying systems (Steam-heated tumblers, gas-fired dryers, lint extraction).
4. Garment finishing & processing equipment (Curing ovens, 3D whisker machines, spray booths, ozone machines).
5. Pumps & pumping systems (Centrifugal, multistage boiler feed pumps, chemical dosing, AODD, ETP sludge pumps).
6. Motors, drives, gearboxes & transmission systems (Induction motors, VFDs, helical/worm gearboxes).
7. Bearings, couplings, shafts, belts, chains & mechanical seals.
8. Valves, pipelines, fittings & fluid distribution systems (Pneumatic angle seat valves, PRVs, steam traps, safety relief valves).
9. Dedicated Boilers & steam generation systems (Gas-fired and Jhut-fired).
10. Steam distribution, traps, condensate recovery & flash tanks.
11. Water supply, storage, filtration, softening & RO plants.
12. Chemical storage, preparation, dosing & transfer systems.
13. Compressed air generation, drying, storage & distribution (Rotary screw compressors, refrigerated dryers, receiver tanks).
14. Pneumatic & hydraulic systems (Cylinders, solenoid valves, FRL units).
15. Cooling, heating & heat-exchange equipment (Plate heat exchangers, shell-and-tube, drain heat recovery).
16. Chillers, cooling towers & associated auxiliaries.
17. Effluent treatment plant (ETP) mechanical equipment (DAF units, aerators, clarifiers).
18. Wastewater, sludge handling & dewatering equipment (Filter presses, progressive cavity pumps).
19. Conveyors, lifting equipment & material handling systems (Electric chain hoists, garment trolleys).
20. Workshop machines & fabrication equipment (Lathes, pillar drills, welding sets, pipe threaders).
21. Maintenance, inspection, lubrication & condition monitoring equipment (Thermal cameras, vibration pens, alignment kits).
22. Mechanical safety & emergency-related equipment (Fire hydrant pumps, gas leak detectors, emergency stop systems).
23. Plant auxiliary systems (Ventilation blowers, lint ducts, exhaust hoods).

### 4.3 Part C: Dedicated Top-Level Boiler Module [C. OWNER-APPROVED REQUIREMENT]
Detailed separately in `docs/BOILER_MODULE_PLAN.md`:
- **Gas-Fired Industrial Boilers:** Gas train, burners, flame supervision, Scotch marine construction, feedwater conditioning, automatic blowdown, combustion control.
- **Jhut / Fabric Waste-Fuel Boilers:** Jhut fuel characteristics, handling/feeding, refractory furnace, step/dumping grates, ash handling, balanced draft (FD + ID fans), back-fire prevention.
- **Boiler Engineering & Management:** ASME PTC 4.1 efficiency calculations (Direct & Indirect), steam evaporation ratio, blowdown TDS control, feedwater calculations, Mobrey level interlocks, safety valve calibration, statutory inspection checklists.

---

## 5. Standard Equipment Knowledge Template (21 Sections) [C. OWNER-APPROVED REQUIREMENT]
Every significant piece of machinery adheres to a 21-section engineering template:
1. Introduction & Purpose
2. Applications in Textile/Garment Washing Plants
3. Working Principle
4. Construction & Major Components
5. Component Functions & Interactions
6. Technical Specifications & Selection Criteria
7. Engineering Drawings, Diagrams & Schematics
8. Installation & Alignment Protocols
9. Commissioning Procedures
10. Operating Principles & Shift Procedures
11. Preventive Maintenance (Daily, Weekly, Monthly, Annual)
12. Predictive & Condition-Based Maintenance (Vibration, Thermal, Oil)
13. Lubrication Requirements & Schedule
14. Inspection Checklists
15. Troubleshooting & Symptom Matrix
16. Failure Analysis & Root Cause Determination
17. Spare Parts & Replacement Protocols
18. Safety Precautions & Interlocks
19. Relevant Engineering Calculations (Power, Flow, Sizing)
20. Practical Field Tips & Maintenance Log Format
21. Applicable Standards, Codes & Manufacturer References

---

## 6. Architecture & Technical Proposals

### 6.1 Status of Technical Architecture [D. PROPOSED DECISION]
The following architectural choices are proposed engineering recommendations for the Android application and are **awaiting formal Owner confirmation**:
- **UI Platform [D. PROPOSED DECISION]:** Native Android Kotlin + Jetpack Compose with Material Design 3.
- **Architecture Pattern [D. PROPOSED DECISION]:** Clean Architecture + MVVM with Unidirectional Data Flow (UDF).
- **Local Storage [D. PROPOSED DECISION]:** Android Room SQLite Database for offline content and Jetpack DataStore for user preferences.
- **Math Rendering [D. PROPOSED DECISION]:** Lightweight vector-based or native Compose formula rendering.
- **Performance Targets [D. PROPOSED DECISION]:** App launch under 1.5s, 60+ FPS UI rendering, search latency <100ms.

*Note: These decisions remain classified as [D. PROPOSED DECISION] until the Project Owner explicitly reviews and confirms the technology stack.*

### 6.2 Financial & Security Policy [C. OWNER-APPROVED REQUIREMENT]
- **Free-First Policy:** 100% free and open-source toolchain. Zero expenses, zero subscriptions, zero paid APIs.
- **No Credentials in Code:** No secret tokens, passwords, or credentials stored in code or chat.

---

## 7. Phased Roadmap (Aligned to Workspace-First Protocol)

> **Checkpoint Synchronization Note (2026-11-16):** The roadmap statuses below reflect empirical workspace reality as of P0 Recovery (`DOC-MPN-004`, `DOC-MPN-003`, `DOC-MPN-031`), superseding the initial `2026-11-11` kickoff labels (`IN PROGRESS` / `NOT STARTED`). Verification across Phases `04–07` rests on static Python checks (`Stage 01` `10/10` post-`chmod +x android/gradlew`, `Stage 02` `8/8`, `Stage 03` `10/10`, `Stage 04` `10/10`) and `80` authored JUnit `@Test`s (`12` test files); native Android compilation, JVM unit execution, Room KSP schema generation, NIST REFPROP benchmarking, Compose rendering, and physical-device QA remain blocked/unverified in this container.

| Phase | Title | Focus & Deliverables | Status |
|---|---|---|---|
| **Phase 01** | Documentation Foundation & Scope Alignment | Core project docs, decision log, safety rules, personal scope alignment | **COMPLETE** |
| **Phase 02** | Plant Knowledge Base & Content Blueprint | Blueprint & 64-asset reconciliation (`2/64` FULL [`EQ-A01`, `EQ-B01`], `9/64` PARTIAL, `7/64` RESEARCH_PENDING, `46/64` INVENTORY_ONLY); `mechanical_pro_notes.db` (`753,664` bytes, `603` sections) | **BLUEPRINT COMPLETE — FULL-DEPTH CONTENT INCOMPLETE** |
| **Phase 03** | Android Architecture & Workspace Baseline | Clean Architecture, MVVM, Room/FTS, SAF backup, offline architecture docs | **COMPLETE** |
| **Phase 04** | Core Domain & Room Database Implementation | Domain models, Room entities/DAOs/repositories, pre-populated SQLite asset (`android/app/schemas/` and native KSP/migration run unverified) | **PARTIALLY COMPLETE** |
| **Phase 05** | UI/UX & Compose Reference Screens | Material 3 UI, navigation, `40` `@Preview`s, Batch 01–03 UI fixes (`Stage 05` blocked; `GAP-05` & `GAP-07..11` open; native runtime unverified) | **PARTIALLY COMPLETE** |
| **Phase 06** | Field Calculations & Maintenance Tools | 20 calculators (`CALC-02` `RESEARCH_PENDING`) + 56-row IAPWS-IF97 engine (external NIST REFPROP benchmark & native JUnit run unverified) | **PARTIALLY COMPLETE** |
| **Phase 07** | Offline Validation & Field Testing | Static validation & Batch 01–03 remediation suites passed; native APK build & physical-device field QA outstanding | **PARTIALLY COMPLETE** |
| **Phase 08** | GitHub Repository Preparation & Owner Push | `README.md`, `.gitignore`, `gradle-wrapper.jar`, executable `android/gradlew` prepared; APK unbuilt & owner push pending | **PARTIALLY COMPLETE** |

### 7.1 Active Unresolved Items & Open Decisions (Reference Only)
- **Open Implementation Gaps:** `GAP-05` (`HomeScreen` bookmark card & shortcuts), `GAP-03` remainder (`EquipmentViewModel.kt:138` one-shot `isBookmarked` & `FundamentalsScreen.kt` bookmark button), secondary `GAP-06` enhancements, `GAP-07..11`, `GAP-12` (`CALC-02` & `FUND-11..15`), and `GAP-13` (equipment content depth & `5/64` vector schematics). See `DOC-MPN-027` and `DOC-MPN-031`.
- **Unresolved Technical Conflict (`REF-06` vs `FUND-05`):** `EngineeringCatalogs.kt:170` (`REF-06`) lists ISO 20816-3 speed bounds as `120 RPM and 15,000 RPM`, whereas `EngineeringCatalogs.kt:335` (`FUND-05`) and `DOC-MPN-003` cite `120–30,000 RPM`. Preserved unresolved pending external standard check.
- **Statutory Disclaimer:** Citations to **The Boilers Act, 2022 (Act No. 06 of 2022)** and **Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)** are documentation/code references requiring independent legal verification prior to statutory reliance.
- **Environment Blockers:** Native `./gradlew testDebugUnitTest assembleDebug`, Compose preview rendering, and Emulator/AVD testing require an external host with **JDK 17**, **Android SDK Platform 35**, and hardware virtualization (`/dev/kvm`).
