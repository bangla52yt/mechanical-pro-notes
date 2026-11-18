# PHASE 03: OFFLINE SEARCH AND CONTENT RENDERING ENGINE
**Document Identifier:** DOC-MPN-016  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED SEARCH & CONTENT ENGINE SPECIFICATION - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Search Subsystem Engineer  
**Date:** 2026-11-12  

---

## 1. Executive Search Architecture: Sub-50ms Offline Intelligence

A plant maintenance engineer frequently needs instant answers while troubleshooting a running machine (e.g., searching for *"tripping on high temperature"*, *"bearing vibration"*, *"EQ-A01"*, or *"ব্লোডাউন ক্যালকুলেশন"*).

To achieve sub-50ms search latency on an Android phone without an internet connection or external server, Mechanical Pro Notes implements a native **SQLite FTS5 (Full-Text Search 5)** virtual table engine coupled with Kotlin Coroutines.

```
[ User Search Input (English or Bengali) ]
                   |
                   v (Debounce 200ms via StateFlow)
+-------------------------------------------------------------------------------+
|                       SearchViewModel / SearchRepository                      |
|  - Sanitizes user input (removes unsafe SQL characters)                       |
|  - Appends prefix wildcards (`term*`) for real-time type-ahead suggestions    |
+-------------------------------------------------------------------------------+
                   |
                   v (Dispatched to Dispatchers.IO)
+-------------------------------------------------------------------------------+
|                      SQLite FTS5 Virtual Table Query                          |
|                                                                               |
|  SELECT equipment_id, name, bengali_name, category_name,                     |
|         snippet(equipment_fts, 5, '<b>', '</b>', '...', 12) AS snippet,       |
|         bm25(equipment_fts) AS rank                                           |
|  FROM equipment_fts                                                           |
|  WHERE equipment_fts MATCH :sanitizedQuery                                    |
|  ORDER BY rank ASC                                                            |
|  LIMIT 30;                                                                    |
+-------------------------------------------------------------------------------+
                   |
                   v
+-------------------------------------------------------------------------------+
|                      Categorized Search Results UI                            |
|  - Exact Equipment Matches (e.g., EQ-A01 Belly Washer)                        |
|  - Failure Mode & Symptom Matches (e.g., Plummer block overheating)           |
|  - Formula & Calculation Matches (e.g., CALC-05 Blowdown Rate)               |
|  - Statutory & Code Matches (e.g., Act 06 of 2022 Section 18)                 |
+-------------------------------------------------------------------------------+
```

---

## 2. Multi-Attribute Search Index Design

The FTS5 virtual table indexes seven distinct technical attributes for each machine:

1. **Equipment Identifiers:** Exact standard IDs (`EQ-A01`, `EQ-G01`, `EQ-D02`).
2. **Official English Names:** Technical engineering names (`Horizontal Front-Loading Belly Washer`).
3. **Bengali Names & Phonetics:** Bengali titles (*হরাইজন্টাল বেলি ওয়াশার*, *বয়লার*).
4. **Plant Colloquial Aliases:** Shop-floor terms (`Washing machine`, `Rotary drum`, `Belly`).
5. **Component Codes & Names:** Major wear components (`Plummer block`, `Mechanical face seal`, `Trunnion shaft`).
6. **Failure Symptoms & Root Causes:** Observable maintenance symptoms (`Excessive vibration`, `Water hammer`, `Steam leak`).
7. **Statutory Law Keywords:** Legal sections and compliance terms (`Boilers Act 2022`, `Hydraulic test`, `S.R.O. 279-Law/2025`).

### Performance Optimization:
- **Tokenizer:** `tokenize = 'unicode61 remove_diacritics 2'` ensures both accented characters and standard Unicode glyphs are indexed properly.
- **BM25 Ranking Algorithm:** Utilizes SQLite's internal Okapi BM25 ranking function to push exact equipment title matches to the top of the search list, followed by symptom matches.

---

## 3. High-Performance Content Rendering Engine

### 3.1 Lightweight Compose Markdown Parser (Zero-WebView)
Traditional Android applications often render technical documentation using embedded WebViews.
- **Why WebView is REJECTED:** WebViews consume 50–100 MB of RAM per instance, introduce 300ms–800ms rendering lag, and suffer from poor theme integration with dark mode.
- **Recommended Strategy:** A pure Jetpack Compose native Markdown parser that compiles markdown strings directly into **`AnnotatedString`** and **Native Compose UI Nodes**.
  - Headers (`#`, `##`, `###`) mapped directly to Material 3 Typography tokens (`headlineMedium`, `titleLarge`).
  - Bulleted lists and tables rendered as responsive Compose `Column` and `Row` layouts.
  - Zero memory bloat; instant rendering of 20+ page equipment teardowns.

---

## 4. Engineering Formula & Math Layout Engine

Many mechanical equations require fractions, square roots, subscripts, superscripts, and Greek letters (e.g., $\Delta P$, $\eta_{thermal}$, $\dot{m}_s$, $\mu$).

### Formula Architecture:
1. **Primary Formula Card:** Rendered using high-clarity native Compose typography with specialized Unicode mathematical glyphs:
   $$\eta = \frac{\dot{m}_s \times (h_s - h_w)}{\dot{m}_f \times \text{NCV}} \times 100\%$$
2. **Variable Legend Table:** Every variable is cleanly mapped to its SI unit and description:
   - $\dot{m}_s$: Steam generation rate ($\text{kg/h}$)
   - $h_s$: Enthalpy of saturated steam ($\text{kJ/kg}$)
   - $h_w$: Enthalpy of feed water ($\text{kJ/kg}$)
   - $\dot{m}_f$: Fuel consumption rate ($\text{kg/h}$ or $\text{Nm}^3\text{/h}$)
   - $\text{NCV}$: Net calorific value of fuel ($\text{kJ/kg}$ or $\text{kJ/Nm}^3$)
3. **Interactive Simulation Mode:** Tapping "Open in Calculator" transfers the formula's variables directly into the interactive calculation runner.

---

## 5. Offline Diagrams and Technical Media Subsystem

Textile washing machinery and boilers require clear visual references (P&IDs, piping schematics, bearing assembly cross-sections).

### Technical Asset Strategy:
1. **Schematic Diagrams (Vector Drawables / SVG):**
   - Piping flowsheets, electrical control logic, and valve schematics are stored as Android Vector Drawables (`res/drawable/`).
   - Resolution-independent, razor-sharp on any phone screen density, and near-zero file size (typically $15\text{–}40\text{ KB}$ per diagram).
2. **Photographic & Complex CAD Cross-Sections (WebP Format):**
   - High-resolution mechanical cutaways are stored locally as compressed **WebP** images with 85% lossless compression.
   - Typical image footprint is only $120\text{–}250\text{ KB}$ per machine, ensuring the entire 64-machine visual library stays under $25\text{ MB}$.
3. **Interactive Pinch-to-Zoom & Pan Canvas:**
   - Diagrams are wrapped in a custom Compose gesture modifier:
     ```kotlin
     Modifier.pointerInput(Unit) {
         detectTransformGestures { _, pan, zoom, _ ->
             scale = (scale * zoom).coerceIn(1f, 5f)
             offset += pan
         }
     }
     ```
   - Allows maintenance staff to zoom into intricate bearing seals or boiler burner nozzle assemblies with smooth 60 FPS fluidity.
4. **Zero External Network Dependencies:** No images are fetched from external CDNs, guaranteeing 100% availability in basement boiler rooms with zero cell reception.
