# PHASE 06 CALCULATION IMPLEMENTATION
## Software Architecture, Domain Engine Separation & UI Integration Report

**Document Identifier:** DOC-MPN-015  
**Project:** Mechanical Pro Notes (Personal Offline-First Mechanical Reference Android App)  
**Phase:** Phase 06 — Field Calculations, Boiler Thermal Engineering & Maintenance Tools  
**Status:** IMPLEMENTED & VERIFIED  
**Date:** 12 November 2026  
**Governing Architecture:** Clean Architecture + MVVM + UDF (**DEC-013**), Offline-First Room SQLite (**DEC-012**)  
**Classification:** [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Architectural Overview & Component Structure

In compliance with **DEC-013** (Clean Architecture + MVVM + Unidirectional Data Flow), the Phase 06 calculation module isolates pure thermodynamic and mechanical physics from Android platform UI dependencies and database persistence.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                              │
│  CalculationListScreen  │  CalculationRunnerScreen  │  SteamTableScreen│
│              ▲                           ▲                             │
│              │                           │                             │
│  CalculationListViewModel    CalculationRunnerViewModel                │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ StateFlow / Events
┌───────────────────────────────▼────────────────────────────────────────┐
│                           DOMAIN LAYER                                 │
│  CalculationRepository (Interface)                                    │
│  BoilerThermalCalculations.kt         (CALC-01 to CALC-14)             │
│  WashPlantMaintenanceCalculations.kt  (CALC-15 to CALC-20)             │
│  IapwsIf97SteamEngine.kt              (Thermodynamic Engine)           │
│  CalculationSpecifications.kt         (Metadata & Default Constants)   │
└───────────────────────────────▲────────────────────────────────────────┘
                                │ Repositories & Inversion
┌───────────────────────────────┴────────────────────────────────────────┐
│                            DATA LAYER                                  │
│  CalculationRepositoryImpl.kt                                          │
│  Room Database: CalculationDao & CalculationHistoryDao                 │
│  mechanical_pro_notes.db (SQLite with calculation_definitions)        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Source Code Implementation Directory

### 2.1 Pure Domain Calculation Engines
1. **`src/domain/steam/IapwsIf97SteamEngine.kt`:**
   - Implements offline cubic/logarithmic thermodynamic formulation for saturated steam from $0.5\text{ bar(a)}$ to $20.0\text{ bar(a)}$.
   - Evaluates:
     - $T_{sat}(P)$ (Saturation Temperature, $^\circ\text{C}$)
     - $h_f(P)$ (Liquid Specific Enthalpy, $kJ/kg$)
     - $h_{fg}(P)$ (Latent Heat of Vaporization, $kJ/kg$)
     - $h_g(P) = h_f + h_{fg}$ (Total Saturated Vapor Enthalpy, $kJ/kg$)
     - $v_g(P)$ (Saturated Vapor Specific Volume, $m^3/kg$)
     - $v_f(P)$ (Liquid Specific Volume, $m^3/kg$)
     - $\rho(P) = 1 / v_g$ (Steam Density, $kg/m^3$)
   - Provides gauge-to-absolute pressure conversion: $P_{abs} = P_{gauge} + 1.01325\text{ bar}$.
   - Self-tests and asserts exact mathematical consistency: $h_g \equiv h_f + h_{fg}$ at all interpolation nodes.

2. **`src/domain/calculation/BoilerThermalCalculations.kt`:**
   - Pure Kotlin functions executing Category A algorithms (CALC-01 through CALC-14).
   - Functions are stateless, referentially transparent, and return structured `CalculationResult` objects containing:
     - Primary output value, label, and engineering units.
     - Secondary analytical outputs (breakdown parameters, losses, margins).
     - Informational badges and warnings (e.g., acid dew point advisories, regulatory disclaimers).
   - Boundary checks and defensive guards prevent NaN and division-by-zero crashes.

3. **`src/domain/calculation/WashPlantMaintenanceCalculations.kt`:**
   - Pure Kotlin functions executing Category B wash plant tools (CALC-15 through CALC-20).
   - Implements ISO 4184 V-belt geometry, centrifugal G-factor and critical over-speed triggers, sparge batch steam balances, tumble dryer psychrometric air consumption, ion-exchange resin salt regeneration, and compressed air pressure drop.

4. **`src/domain/calculation/CalculationSpecifications.kt`:**
   - Master catalog of all 20 calculators providing:
     - Identification codes (`CALC-01` through `CALC-20`).
     - Plain-text English and LaTeX formula strings.
     - Engineering governing standards (ASME, BS, ISO, RMA, IAPWS).
     - Physical input variable specifications with minimum, maximum, and typical default values.
     - Categorization tags: `"Boiler Thermal"`, `"Boiler Water"`, `"Combustion & Draught"`, `"Steam Distribution"`, `"Wash Plant Machinery"`, `"Water Treatment"`, `"Pneumatics & Air"`.

### 2.2 Data Layer & Repository Inversion
1. **`src/data/repository/CalculationRepositoryImpl.kt`:**
   - Implements `CalculationRepository`.
   - Dynamic routing engine: maps incoming calculation requests by `calculationId` directly to the corresponding domain calculation function.
   - Fallback resilience: If Room SQLite database is offline or uninitialized, falls back seamlessly to in-memory `CalculationSpecifications.ALL_CALCULATIONS`.
   - Persists execution history into `calculation_history` table without modifying static knowledge base tables (**DEC-014**).

2. **`android/app/src/main/assets/database/mechanical_pro_notes.db`:**
   - Populated with all 20 calculation definitions and 79 formula variables via `seed_phase06_calculations.py`.
   - Indexed on `calculation_id` and `category` for microsecond query latency.

### 2.3 Presentation Layer & Compose UI
1. **`src/presentation/calculations/CalculationListScreen.kt`:**
   - Material 3 calculation catalog with filter chips:
     - `"All"`
     - `"Boiler Thermal"`
     - `"Boiler Water"`
     - `"Combustion & Draught"`
     - `"Steam Distribution"`
     - `"Wash Plant Machinery"`
     - `"Water Treatment"`
     - `"Pneumatics & Air"`
   - Features prominent **Interactive Steam Table** hero card at the top with direct navigation to `SteamTableScreen`.
   - Real-time search query filtering over calculation title, formula code, and description.

2. **`src/presentation/calculations/CalculationRunnerScreen.kt`:**
   - Dynamic parameter input form generated automatically from variable specifications.
   - Outlined text fields with floating labels, unit badges, and validation error messages.
   - Real-time "Reset to Defaults" button for plant floor quick restoration.
   - "Calculate" execution trigger invoking domain functions on background coroutine.
   - Result card displaying:
     - Primary headline result with bold typography.
     - Secondary metrics grid.
     - Engineering notes and safety warnings.
     - "Save to Notes / History" action enabling shift handover logging (**DEC-014**).

3. **`src/presentation/calculations/SteamTableScreen.kt`:**
   - Purpose-built thermodynamic utility screen.
   - Gauge pressure ($bar\text{ g}$) vs Absolute pressure ($bar\text{ a}$) toggle button.
   - Interactive pressure slider and numeric input field ($0.5$ to $20.0\text{ bar}$).
   - Factory floor quick preset buttons: $1.0\text{ bar}$, $4.0\text{ bar}$, $7.0\text{ bar}$, $10.0\text{ bar}$, $15.0\text{ bar}$.
   - Full property card displaying $T_{sat}$, $h_f$, $h_{fg}$, $h_g$, $v_g$, $v_f$, and density $\rho$.

---

## 3. Defensive Programming & Safety Measures

1. **Zero Network Calls:** No calculation makes HTTP/REST calls, remote telemetry, or cloud requests (**DEC-016**).
2. **Deterministic Computation:** Pure functions without side effects guarantee repeatable results across all devices.
3. **Physical Boundary Guards:** All parameters are bounded by physical laws (e.g., temperatures above absolute zero, positive pipe diameters, non-zero efficiencies).
4. **Safety Valve Relieving Disclaimer (CALC-09):** The UI enforces an explicit advisory stating that calculations are for preliminary engineering checks only and cannot certify or alter safety valve settings without OCIB certified inspection.
5. **Centrifugal Over-Speed Protection (CALC-16):** Warns if operating RPM exceeds rated nameplate RPM to safeguard factory operators against catastrophic basket failure.
