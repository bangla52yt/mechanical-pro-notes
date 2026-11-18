# PHASE 05 NAVIGATION IMPLEMENTATION REPORT

**Project:** Mechanical Pro Notes  
**Application Scope:** Personal Offline-First Mechanical Reference Android App  
**Phase:** 05 — Material 3 UI & Compose Reference Screens  
**Date:** 12 November 2026  

---

## 1. Navigation Topology Overview

The application implements a dual-tier navigation system optimized for one-handed operation on an Android mobile device:

```
                                  [AppNavigation]
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
       [ModalNavigationDrawer]                        [NavigationBar (BottomBar)]
    (All 17 Plant Modules + Settings)               (4 Primary Daily Hotspots)
                 │                                               │
                 ├── 01. Home Dashboard ◄────────────────────────┼── Tab 1: Home
                 ├── 02. Mechanical Fundamentals                 ├── Tab 2: Wash Plant
                 ├── 03. Wash Plant Equipment ◄──────────────────┼── Tab 3: Boiler
                 ├── 04. Dedicated Boiler Module ◄───────────────┼── Tab 4: Calculations
                 ├── 05. Pumps & Fluid Systems                   └─────────────────────────
                 ├── 06. Motors & Transmission
                 ├── 07. Steam & Condensate
                 ├── 08. Water & Chemical Systems
                 ├── 09. Compressed Air & Pneumatics
                 ├── 10. Cooling & Heating Systems
                 ├── 11. ETP & Wastewater Systems
                 ├── 12. Maintenance Management
                 ├── 13. Inspection & Checklists
                 ├── 14. Engineering Calculations ◄───────────────
                 ├── 15. Technical Reference Library
                 ├── 16. Search & Bookmarks
                 └── 17. Settings & Local Data
```

---

## 2. Route Specifications (`Screen.kt`)

Every destination is defined as a strongly typed singleton object under the sealed `Screen` class:

| Route Path | Screen Object | Destination Description | UI Presentation Pattern |
|---|---|---|---|
| `home` | `Screen.Home` | Main operational dashboard | Top statistics, daily shortcuts, critical alerts |
| `wash_plant` | `Screen.WashPlant` | Equipment list & family filter | Horizontal family filter chips + scrollable cards |
| `boiler_module` | `Screen.BoilerModule` | Gas, solid fuel & regulatory hub | Dual-fuel overview, statutory OCIB mandate |
| `calculations` | `Screen.Calculations` | Verified formula catalog | Mathematical formula cards + status badges |
| `equipment_detail/{equipmentId}` | `Screen.EquipmentDetail` | 21-Section Machinery Reader | 5-Tab scrollable comprehensive reader |
| `calculation_runner/{calcId}` | `Screen.CalculationRunner` | Real-time calculation form | Input variables, range bounds, result breakdown |
| `search` | `Screen.Search` | FTS5 multi-language search | Debounced search bar, family filter chips |
| `notes?equipmentId={id}` | `Screen.Notes` | Personal field observations | Dialog editor, note cards, asset badges |
| `bookmarks` | `Screen.Bookmarks` | Saved reference pins | Direct one-tap jump to pinned assets |
| `checklists` | `Screen.Checklists` | Plant maintenance audit | Interactive checkboxes, progress meter |
| `library` | `Screen.Library` | Laws, ASME codes, guides | Expandable reference cards with citations |
| `settings` | `Screen.Settings` | Privacy audit & JSON backup | Zero-network verification, JSON export dialog |

---

## 3. Back Stack & State Preservation

- Implements `popUpTo(Screen.Home.route) { saveState = true }`, `launchSingleTop = true`, and `restoreState = true` on top-level navigation to ensure memory efficiency and back-press predictability.
- Detail routes (`EquipmentDetail`, `CalculationRunner`, `Notes`, `Search`) push onto the stack and pop cleanly back to the calling screen.
