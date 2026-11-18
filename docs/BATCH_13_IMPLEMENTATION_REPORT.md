# BATCH 13 IMPLEMENTATION REPORT: GAP-13 EQUIPMENT CONTENT DEPTH & ENGINEERING SCHEMATICS
**Document Identifier:** DOC-MPN-041  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** IMPLEMENTED IN SOURCE, SCHEMATICS, SQLITE ASSET, UNIT TESTS & DOCS (`121` authored `@Test`s) — NATIVE ANDROID BUILD, JVM JUNIT & EMULATOR VERIFICATION REMAIN **BLOCKED** IN CONTAINER (JDK 17 / ANDROID SDK 35 ABSENT)  
**Date:** 2026-11-17  

---

## 1. Executive Summary
Batch 13 (`GAP-13`) upgrades the offline plant library from `2/64` full records and `5` schematics to **`64/64` deep field-usable mechanical references** (`1,190` machine-tailored `knowledge_section` entries, `0` duplicated boilerplate, `0` `INVENTORY_ONLY` placeholders) and **`18` offline AAPT2-safe Android Vector Drawable schematics** mapped across all **16 system families (`FAM-A`–`FAM-P`) and all 64 assets (`EQ-A01`–`EQ-P01`)**.

Strict engineering honesty and project boundaries were maintained throughout:
- **Zero fabrication:** No OEM-specific bolt torques, bearing clearances, drum RPMs, or chemical recipes were invented. Unconfirmed OEM/plant-optional assets retain **`RESEARCH_PENDING` (`7`)**, and mixed-jhut waste fuel remains **`PARTIAL_CONTENT_BLUEPRINT` (`1`, `EQ-G02`)** pending plant batch elemental/GCV laboratory testing.
- **Zero schema or feature regression:** Preserved Room `user_version = 1`, `16` entities, empty `ALL_MIGRATIONS`, `BackupJsonCodec` (`backup_version = 2`), Batch 12 `CALC-02` (`IMPLEMENTED`, `87` `formula_variable` rows), `15/15` Fundamentals (`FUND-01`–`FUND-15`), FTS4 search (`equipment_fts`), zero network permissions (`DEC-016`), and Batch 01–12 functionality.

---

## 2. Final Reconciled Content Depth Distribution (`64` Assets)
| Content Depth Status | Count | Equipment IDs | Rationale |
|---|---:|---|---|
| **`FULL_CONTENT_BLUEPRINT`** | **56** | `EQ-A01..A04`, `EQ-B01..B03`, `EQ-C01..C03`, `EQ-D01..D06`, `EQ-E01..E06`, `EQ-F01..F06`, `EQ-G01`, `EQ-G03..G04`, `EQ-H01..H02`, `EQ-H04`, `EQ-I01..I03`, `EQ-J01..J04`, `EQ-K01`, `EQ-L01..L05`, `EQ-M01..M03`, `EQ-N01..N03`, `EQ-O01..O02`, `EQ-P01` | Upgraded from baseline/partial specs with full applicable working principles, component maps, failure modes, `>= 2` structured PM/repair procedures, and profile-appropriate knowledge sections. |
| **`PARTIAL_CONTENT_BLUEPRINT`** | **1** | `EQ-G02` | Mechanics, balanced-draft control, hopper back-fire safeguards, and Boilers Act 2022 compliance fully authored; kept `PARTIAL` because mixed-jhut elemental GCV/$H_2$/ash composition requires batch laboratory testing. |
| **`RESEARCH_PENDING`** | **7** | `EQ-A05`, `EQ-B04`, `EQ-B05`, `EQ-C04`, `EQ-C05`, `EQ-H03`, `EQ-K02` | Full safety hazards, operating principles, generic industrial engineering specs, and PM workflows added; kept `RESEARCH_PENDING` where owner plant presence or OEM-proprietary curves/catalyst/chiller/RF specs require plant verification. |
| **`INVENTORY_ONLY`** | **0** | *(None)* | Eliminated all 46 shallow placeholders. |
| **Total** | **64** | `EQ-A01`–`EQ-P01` | $56 + 1 + 7 + 0 = 64$ ($100\%$ reconciled). |

---

## 3. Offline Vector Drawable Schematics (`18` Drawables / `64` Assets)
All 18 schematics live in `android/app/src/main/res/drawable/diag_*.xml` and strictly use AAPT2-safe tags (`<vector>`, `<group>`, `<path>`, `<clip-path>` — **zero `<text>` elements**), paired with `[1]`–`[6]` numbered vector callout badges, directional flow arrows, matching legend entries, and explicit `"Conceptual engineering arrangement — verify OEM P&ID/GA before maintenance"` disclaimers in `EquipmentDetailScreen.kt`.

1. **5 Upgraded Existing Schematics:** `diag_belly_washer.xml` (`EQ-A01..A05`), `diag_hydro_extractor.xml` (`EQ-B01..B02`), `diag_tumbler_dryer.xml` (`EQ-B03..B05`), `diag_gas_boiler_3pass.xml` (`EQ-G01, G03, G04`), `diag_jhut_boiler_draft.xml` (`EQ-G02`).
2. **13 Newly Authored Schematics:**
   - `diag_finishing_oven_booth.xml` (`EQ-C01..C05`)
   - `diag_centrifugal_pump_npsh.xml` (`EQ-D01, D02, D05`)
   - `diag_pd_metering_pump.xml` (`EQ-D03, D04, D06`)
   - `diag_power_transmission.xml` (`EQ-E01..E06`)
   - `diag_steam_condensate_loop.xml` (`EQ-F01..F06`)
   - `diag_water_treatment_loop.xml` (`EQ-H01..H04`)
   - `diag_chemical_dosing_safety.xml` (`EQ-I01..I03`)
   - `diag_compressed_air_system.xml` (`EQ-J01..J04`)
   - `diag_cooling_chiller_loop.xml` (`EQ-K01..K02`)
   - `diag_etp_clarification.xml` (`EQ-L01..L05`)
   - `diag_lifting_conveyor.xml` (`EQ-M01..M03`)
   - `diag_workshop_machines.xml` (`EQ-N01..N03`)
   - `diag_safety_exhaust_utility.xml` (`EQ-O01..O02, P01`)

---

## 4. Database, Seeding & Presentation Updates

### 4.1 Seed Architecture & Rebuilt SQLite Asset
- **`/seed_equipment_part1.py` (`37` records, `FAM-A`–`FAM-G`) & `/seed_equipment_part2.py` (`27` records, `FAM-H`–`FAM-P`):** Authored unique, asset-appropriate engineering records using strict enum validation (`WEAR_PART` / `CONSUMABLE` / `STRUCTURAL_PERMANENT`, fixing `14` prior invalid `SAFETY_CRITICAL` tier labels in Part 1).
- **`/build_database.py`:** Merged all `64` assets, enforced strict enum assertions across all tables, generated adaptive section titles via `get_profile_section_title()` (sections 13 & 14 reflect dry-service vs lubricated and machine-type vibration/condition monitoring), and synchronized `CALC-02` + `87` `formula_variable` rows cleanly.
- **Rebuilt `/android/app/src/main/assets/database/mechanical_pro_notes.db`:**
  - **File Size:** **`856,064` bytes** (`user_version = 1`)
  - **SHA-256:** `c8fcd68599c1a22a18c0b8d71151524756eb9ae1f0d30dc61b7b4c0385946f40`
  - **Row Counts:** `system_family = 16`, `equipment = 64`, `equipment_fts = 64` (English & Bengali indexed), `equipment_subtype = 14`, `component = 209`, `technical_parameter = 206`, `knowledge_section = 1,190` (`1,190` distinct `content_markdown` rows — zero boilerplate), `failure_mode = 130`, `maintenance_procedure = 128`, `calculation = 20`, `formula_variable = 87`, and `0` synthetic user rows in `user_note` / `user_bookmark` / `calculation_history` / `checklist_progress`.
  - **SQLite Integrity Checks:** `PRAGMA integrity_check == ok`, `PRAGMA foreign_key_check == []`, FTS4 `"Boiler"` -> `4` matches, FTS4 `"বয়লার"` -> `3` matches.

### 4.2 Presentation, Condition Monitoring & Test Updates
- **`EquipmentDetailScreen.kt`:**
  - Expanded `getSchematicForEquipment(equipment)` to cover all `64` assets across all `18` schematics with conceptual arrangement subtitles and `[1]`–`[6]` legends.
  - Added machine-aware `buildConditionMonitoringGuidance(equipment)` separating static/passive/safety equipment (`EQ-F06, G03, G04, I02, J03, J04, M02, O02`), dry belts/couplings (`EQ-E04..E06`), positive-displacement compressors (`EQ-J01, L02` explicitly excluded from ISO 20816-3), PD/metering pumps (`EQ-D03, D04, D06, L04`), process/safety apparatus (`EQ-C02..C05, N03`), and rotating machines evaluated under ISO 20816-3:2026.
  - Made Section 13 heading adaptive (`"13. Dry-Service Care & Cleanliness Regime"` vs `"13. Lubrication Regime"`).
  - Reconciled `getRelatedCalculationsForEquipment`: `EQ-G01` links `CALC-01..05, 08..11, 13`; `EQ-G02` adds `CALC-12`; centrifugal pumps (`EQ-D01, D02, D05`) link `CALC-07` while PD/metering pumps (`EQ-D03, D04, D06`) link `emptyList()`; `EQ-H03` links `emptyList()`.
- **`EquipmentMappingTest.kt` (`+3` net `@Test`s, bringing authored suite total to `121` across `12` files):**
  - Updated RO schematic test (`R.drawable.diag_water_treatment_loop`, zero centrifugal pump calc mislinking) and Family G boiler calculation assertions (`CALC-02` on `EQ-G01`/`EQ-G02`, `CALC-12` on `EQ-G02`).
  - Added `positiveDisplacementPumpsDoNotMislinkCentrifugalPumpCalculator`, `all16FamiliesMapToDedicatedOfflineSchematicsWithConceptualDisclaimer`, and `conditionMonitoringGuidanceDistinguishesStaticPdBeltsAndRotatingMachinery`.
- **Documentation Updated:** `DOC-MPN-010` (`SRC-023`–`SRC-028`), `DOC-MPN-007` (`56/1/7/0` matrix + schematic IDs), `DOC-MPN-041` (this report), `CURRENT_WORK_CHECKPOINT.md`, `SESSION_HANDOVER.md`, and `PHASE_PROGRESS_TRACKER.md`.

---

## 5. Verification Ledger & Honest Container Boundaries
| Check Category | Status in Container | Details |
|---|---|---|
| SQLite Schema & Integrity | **PASS (Python `sqlite3`)** | `user_version = 1`, `PRAGMA integrity_check = ok`, `PRAGMA foreign_key_check = []`, `56/1/7/0` depth balance, `87` formula variables, `1,190` unique `knowledge_section` texts, FTS4 English & Bengali queries verified. |
| XML Schematic & Resource Parity | **PASS (Python `ElementTree`)** | All `18` `diag_*.xml` parse cleanly with allowed tags `{vector, group, path, clip-path}` only; `src (18) == test (18) == xml (18)` parity verified. |
| Kotlin Syntax Sanity | **PASS (Python Delimiter Check)** | `EquipmentDetailScreen.kt` and `EquipmentMappingTest.kt` show `0` parenthesis/brace/bracket balance errors. |
| Native Android Build & JVM JUnit (`./gradlew testDebugUnitTest assembleDebug`) | **UNVERIFIED / BLOCKED** | Container lacks JDK 17 (`java`/`javac`/`kotlinc`), Android SDK 35, AAPT2 runtime, and Emulator/AVD (`/dev/kvm`). Visual schematic rendering, KSP Room schema verification, and `121` JUnit tests remain unexecuted until opened in Android Studio / CI. |
| Preserved Unresolved External Items | **OPEN** | `CALC-02` uncalibrated mixed-jhut elemental testing boundary; `REF-06` (`15,000` vs `30,000 RPM`) ISO 20816-3 discrepancy; independent legal verification of Bangladesh Boilers Act 2022 / Rules 2025; external NIST REFPROP verification of `IapwsIf97SteamEngine`. |
