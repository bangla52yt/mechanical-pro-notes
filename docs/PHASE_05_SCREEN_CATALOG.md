# PHASE 05 SCREEN CATALOG & ARCHITECTURE SPECIFICATION

**Project:** Mechanical Pro Notes  
**Application Scope:** Personal Offline-First Mechanical Reference Android App  
**Phase:** 05 — Material 3 UI & Compose Reference Screens  
**Date:** 12 November 2026  

---

## 1. Catalog of Implemented Screens

The presentation layer comprises 12 distinct screen composables built with Material 3:

### 1.1 Home Dashboard (`HomeScreen.kt`)
- **Key Elements:** Plant operational status overview, quick statistics (48 Assets, 4 Boiler Units, 5 Calculations, 4 Checklists), daily shortcut tiles, and quick search shortcut.
- **ViewModel:** `HomeViewModel` (observes `EquipmentRepository` and `BoilerRepository`).

### 1.2 Equipment List Screen (`EquipmentListScreen.kt`)
- **Key Elements:** Horizontal system family filter chips (FAM-A to FAM-G), equipment count summary, scrollable `EquipmentCardItem` cards with content depth badges.
- **ViewModel:** `EquipmentViewModel` (observes `EquipmentRepository`).

### 1.3 21-Section Equipment Detail Reader (`EquipmentDetailScreen.kt`)
- **Key Elements:** Top app bar with equipment ID and bookmark toggle; sticky identity card; 5-tab scrollable reader covering all 21 technical sections:
  - Tab 0: Overview & Identity, Functional Principle, Core Specs, Bangladesh Standards.
  - Tab 1: Sub-assemblies, Components, Motor Drive & Transmission.
  - Tab 2: Operating Parameters, Tolerances & ISO 10816 Vibration.
  - Tab 3: Lubrication Register, Preventive Maintenance & Critical Spares.
  - Tab 4: FMEA Failure Modes, Safety & LOTO Interlocks, Personal Field Notes.
- **Fallback Rule:** Unpopulated sections display clean scheduled-expansion notices; no hallucinated data.

### 1.4 Dedicated Boiler Module Dashboard (`BoilerDashboardScreen.kt`)
- **Key Elements:** High-pressure steam boiler hub; Gas-fired vs Jhut/solid-fuel unit cards; statutory banner for Bangladesh Boilers Act, 2022 and Boiler Rules, 2025; quick calculation shortcuts (CALC-01, CALC-04, CALC-05).
- **ViewModel:** `BoilerViewModel`.

### 1.5 Engineering Calculations List (`CalculationListScreen.kt`)
- **Key Elements:** Mathematical formula cards displaying LaTeX representations, standard references, category chips, and clear status badges (`IMPLEMENTED` vs `RESEARCH_PENDING`).
- **ViewModel:** `CalculationViewModel`.

### 1.6 Calculation Runner Screen (`CalculationRunnerScreen.kt`)
- **Key Elements:** Dynamic form builder for numerical variables; input range validation (`[min, max]`); calculation execution button; result breakdown card displaying primary output, secondary parameters, engineering notes; and button to save to personal history.
- **Safety Disclaimer:** Prominent card stating calculations are for reference and must be verified with calibrated plant instruments.

### 1.7 Offline Search Screen (`SearchScreen.kt`)
- **Key Elements:** Debounced search text field; clear button; system family filter chips; instant SQLite FTS5 search across IDs, English names, and Bengali terms.
- **ViewModel:** `SearchViewModel`.

### 1.8 Personal Field Notes Screen (`NotesScreen.kt`)
- **Key Elements:** Listing of personal maintenance logs and equipment observations; floating action button; full note creation/editing dialog; timestamp display; direct link chips to equipment assets.
- **ViewModel:** `NotesViewModel`.

### 1.9 Saved Bookmarks Screen (`BookmarksScreen.kt`)
- **Key Elements:** Pinned equipment, boilers, and calculations for quick plant floor retrieval; direct one-tap jump to target screens; unpin action.
- **ViewModel:** `NotesViewModel`.

### 1.10 Maintenance Checklists Screen (`ChecklistScreen.kt`)
- **Key Elements:** Selector for industrial checklists (Daily Boiler Blowdown, Weekly Safety Valves, Washer PM, Hydro Extractor Brake Audit); visual progress bar; mandatory check indicators; toggleable checkboxes.
- **ViewModel:** `ChecklistViewModel`.

### 1.11 Technical Reference Library (`ReferenceLibraryScreen.kt`)
- **Key Elements:** Authoritative repository of statutory laws (Boilers Act, 2022; Boiler Rules, 2025), ASME codes, Spirax Sarco thermal guides, and ISO vibration severity standards.
- **Filtering:** Category filter chips for quick regulatory lookup.

### 1.12 Settings & Privacy Screen (`SettingsScreen.kt`)
- **Key Elements:** App version indicator; architecture audit confirmation (Zero Internet Permission DEC-016 verified absent, NO_ACTION isolation DEC-014, Free-First DEC-002); Plant Floor High Legibility font toggle; Personal Data counters; JSON export backup dialog.
- **ViewModel:** `SettingsViewModel`.
