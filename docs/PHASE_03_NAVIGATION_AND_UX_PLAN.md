# PHASE 03: NAVIGATION AND UX ARCHITECTURE PLAN
**Document Identifier:** DOC-MPN-015  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED NAVIGATION & UX PLAN - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Mobile UX Engineer  
**Date:** 2026-11-12  

---

## 1. Executive UX Philosophy: Industrial Shop-Floor Ergonomics

Mechanical Pro Notes is designed for an engineer or maintenance specialist operating inside an active industrial plant (textile washing, dyeing, and boiler house).

### Industrial Usability Principles:
1. **High-Contrast, Distraction-Free Aesthetics:** Built with **Material Design 3 (M3)** using a muted industrial palette (Charcoal, Slate Grey, Technical Steel Blue, and Safety Alert Amber). Zero frivolous animations, decorative gradients, or social media styling.
2. **Glove/One-Handed Ergonomics:** Minimum touch target size of $48\times48\text{ dp}$ for all primary buttons, tabs, and checklist selectors, allowing effortless one-handed thumb navigation while standing on a shop floor.
3. **Deep OLED Dark Mode:** True dark theme (`#121418` surface) reduces eye strain in dimly lit boiler rooms and extends smartphone battery life during 12-hour factory shifts.
4. **Instant Visual Hierarchy:** Transparent badges for content depth:
   - `[FULL BLUEPRINT]` (Emerald Green container)
   - `[PARTIAL BLUEPRINT]` (Sky Blue container)
   - `[INVENTORY ONLY]` (Slate Neutral container)
   - `[RESEARCH PENDING]` (Amber Warning container)
   Users immediately know whether a screen contains an exhaustive 21-section teardown or an initial technical inventory record.

---

## 2. Master 17-Section Navigation Hierarchy

To maintain complete logical integrity across all mechanical domains without cluttering the screen, navigation is split between a **Primary Bottom Navigation Bar** (for the 4 most frequent daily workflows) and a **Comprehensive Modal Navigation Drawer** (accessing all 17 specialized modules).

```
+-------------------------------------------------------------------------------+
|                               TOP APP BAR                                     |
|  [ = ] Hamburger Menu       Mechanical Pro Notes           [ Search ] [ Bookmarks ] |
+-------------------------------------------------------------------------------+
|                                                                               |
|                               ACTIVE SCREEN CONTENT                           |
|                                                                               |
+-------------------------------------------------------------------------------+
|                           BOTTOM NAVIGATION BAR (M3)                          |
|  [ Dashboard ]       [ Wash Plant ]       [ Boiler House ]    [ Calculations ]|
+-------------------------------------------------------------------------------+
```

### The 17 Logical Modules:

| # | Module Name | Primary Route | Role & Content Scope |
|---|---|---|---|
| **01** | **Home Dashboard** | `route_home` | Quick launch shortcuts, recently viewed machines, plant KPI summaries, safety alerts. |
| **02** | **Mechanical Fundamentals** | `route_fundamentals` | Thermodynamics, fluid mechanics, materials, lubrication science, heat transfer basics. |
| **03** | **Wash Plant Equipment** | `route_wash_plant` | Complete explorer of Washing Machinery, Extractors, Dryers, Finishing (Families A, B, C, L, M). |
| **04** | **Dedicated Boiler Module** | `route_boiler_module` | Top-level module: Gas-fired & Jhut boilers, burner systems, The Boilers Act 2022, Boiler Rules 2025. |
| **05** | **Pumps & Fluid Systems** | `route_pumps` | Multistage centrifugal feed pumps, chemical dosing pumps, vacuum pumps, piping hydraulics (Family D). |
| **06** | **Motors & Transmission** | `route_transmission` | Induction motors, VFDs, gearboxes, chains, timing belts, couplings, alignment tolerances (Family E). |
| **07** | **Steam & Condensate** | `route_steam_systems` | PRVs, steam traps (thermodynamic, inverted bucket), condensate recovery, flash vessels (Family G). |
| **08** | **Water & Chemical Systems** | `route_water_treatment` | Softeners, RO plants, chemical dosing tanks, caustic recovery units (Family I). |
| **09** | **Compressed Air & Pneumatics**| `route_pneumatics` | Rotary screw compressors, refrigerated dryers, pneumatic cylinders, FRL units, air distribution (Family F). |
| **10** | **Cooling & Heating Systems** | `route_cooling_heating`| Cooling towers, shell-and-tube heat exchangers, economizers, thermal oil heaters (Family H). |
| **11** | **ETP & Wastewater Systems** | `route_etp` | Dissolved Air Flotation (DAF), bio-reactors, filter presses, sludge dewatering (Family J). |
| **12** | **Maintenance Management** | `route_maintenance` | Preventive (PM), Predictive (PdM), Root Cause Analysis (RCA), LOTO, lubrication schedules. |
| **13** | **Inspection & Checklists** | `route_checklists` | Daily, weekly, monthly interactive plant checklists with local check-off state persistence. |
| **14** | **Engineering Calculations** | `route_calculations` | Suite of 14 verified boiler algorithms + pump head, compressor CFM, and steam pipe sizing. |
| **15** | **Technical Reference Library**| `route_library` | Bangladesh Boiler Law (Act 06 of 2022 & Rules 2025), ISO 20816-3:2026, ASME Sec I, BS EN 12953 summaries. |
| **16** | **Search & Bookmarks** | `route_search` | Sub-50ms full-text FTS5 offline search with multi-tag filtering and saved bookmarks. |
| **17** | **Settings & Data Management** | `route_settings` | Theme switching (Dark/Light/System), Unit selection (SI/Metric/Imperial), Local JSON Backup & Restore. |

---

## 3. Screen Wireframes and Interaction Models

### 3.1 Screen Flow 01: Wash Plant Equipment Explorer (`route_wash_plant`)
1. **Level 1: System Family Grid / List:**
   - Displays system families (e.g. *Family A: Production Washing Machinery*, *Family B: Centrifugal Extractors*).
   - Shows count of equipment ($n=5$ in Family A) and badge for highest content depth available.
2. **Level 2: Equipment Sub-List:**
   - Cards display Equipment ID (`EQ-A01`), Name, Bengali title (*হরাইজন্টাল বেলি ওয়াশার*), and Capacity range.
   - Filter pills allow filtering by `[FULL BLUEPRINT]`, `[PARTIAL]`, or `[INVENTORY]`.
3. **Level 3: Deep Equipment Detail Screen (Tabbed 21-Section Reader):**
   - Header with Equipment ID, Name, Criticality Badge, Bookmark toggle, and Personal Note button.
   - Horizontal scrollable M3 Primary Tab Row dividing the 21 sections into 5 ergonomic categories:
     - **Tab 1: Overview & Specs** (Sections 01 to 06: Intro, Apps, Principles, Components, Specs).
     - **Tab 2: Operation & Transmission** (Sections 07 to 11: Drive, Controls, Utilities, Startup/Shutdown).
     - **Tab 3: Maintenance & Lubrication** (Sections 12 to 14: Daily/Weekly PM, Lubrication Schedule, LOTO).
     - **Tab 4: Troubleshooting & Failures** (Sections 15 to 17: Failure Modes, Root Causes, Diagnostics, Vibration).
     - **Tab 5: Safety & Regulatory** (Sections 18 to 21: Spare Parts, Stat Safety, Codes, Worked Calculations).

---

### 3.2 Screen Flow 02: Dedicated Boiler House Module (`route_boiler_module`)
1. **Boiler Control Hub Screen:**
   - Top Segmented Button: `[Gas-Fired Boilers]` | `[Jhut / Solid-Fuel Boilers]` | `[Statutory & Safety]`.
2. **Gas-Fired Boiler View (`EQ-G01`):**
   - 3-Pass Wet-Back construction layout diagram.
   - Modulating gas train components (Gas filter, Slam-shut valve, Dual solenoid, Air-fuel ratio controller).
   - Direct button to launch **Boiler Efficiency Calculator (CALC-01)**.
3. **Jhut / Solid-Fuel Boiler View (`EQ-G02`):**
   - Manual vs Stoker grate configurations.
   - Fly-ash handling, Multicyclone dust collectors, and draft fan operations (FD & ID fans).
4. **Statutory & Regulatory Tab:**
   - Complete breakdown of **The Boilers Act, 2022 (Act No. 06 of 2022)**.
   - **Bangladesh Boiler Rules, 2025 (S.R.O. 279-Law/2025)** operational guidelines.
   - Annual inspection preparation checklist & Hydrostatic test safety rules.

---

### 3.3 Screen Flow 03: Engineering Calculation Runner (`route_calculations`)
1. **Calculation Selection Screen:**
   - Categorized by Boiler, Pumps, Steam, Compressed Air.
   - Search bar filtering by formula name or physical variable (e.g. "TDS", "Blowdown", "Efficiency").
2. **Interactive Calculation Screen:**
   - Top Card: Mathematical formula rendered in high-clarity typography with SI unit notations.
   - Form Fields: Numerical input boxes with default baseline values pre-filled (e.g., Feedwater TDS = 150 ppm, Boiler Drum TDS = 3000 ppm).
   - Instant boundary checking: Real-time red highlight if user enters an impossible physical value (e.g., Feedwater TDS > Boiler Drum TDS).
   - "Calculate" Action Button (triggers pure Kotlin domain use case).
   - Output Display Card:
     - Calculated primary output (e.g., $BD\% = 5.26\%$).
     - Secondary outputs (Blowdown flow rate in kg/h).
     - Practical engineering recommendation & energy-saving tip.
     - "Save to History" or "Add Personal Observation Note".

---

## 4. Typography & Color Specifications (Material 3)

```kotlin
// Material 3 Dark Industrial Palette (Tailored for Factory Lighting)
val CharcoalBackground = Color(0xFF121418)
val CharcoalSurface    = Color(0xFF1A1D24)
val CharcoalSurfaceVariant = Color(0xFF242832)

val IndustrialSteelBlue = Color(0xFF5B9BD5)   // Primary interactive elements
val SafetyAmberAlert    = Color(0xFFE5A93C)   // Warning, Research Pending, LOTO alerts
val SafetyRedHazard     = Color(0xFFD9534F)   // Trip conditions, statutory violations
val IndustrialGreenOk   = Color(0xFF5CB85C)   // Normal operating zone, Full blueprint

val MonospaceFormulaFont = FontFamily.Monospace // Used for all engineering formulas and IDs
```
