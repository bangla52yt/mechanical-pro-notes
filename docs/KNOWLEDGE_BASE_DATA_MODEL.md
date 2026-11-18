# KNOWLEDGE BASE DATA MODEL: MECHANICAL PRO NOTES
**Document Identifier:** DOC-MPN-011  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** VALIDATED SCHEMA DESIGN - MASTER PROMPT 02-A  
**Scope:** Conceptual & Relational Data Architecture for Future Offline Android SQLite / Room Persistence  
**Last Updated:** 2026-11-11  

---

## 1. Architectural Design Principles
This data model is designed to support the offline-first personal reference application on an Android device. It delivers:
1. **Zero-Latency Offline Retrieval:** Normalized relational structure with indexed search vectors ensuring search responses under 100 milliseconds across thousands of technical records.
2. **Hierarchical Knowledge Organization:** Seamless drill-down from `SystemFamily` -> `EquipmentCategory` -> `Equipment` -> `EquipmentSubtype` -> `Component` -> `21-Section Knowledge`.
3. **Explicit Content Depth Tracking:** Every machine record tracks its documentation maturity (`FullBlueprint`, `PartialBlueprint`, `InventoryOnly`, `ResearchPending`).
4. **Dual Theory-Practice Linkage:** Bi-directional relational mapping linking academic engineering subjects (`Subject`) directly to plant equipment (`Equipment`) and calculations (`Calculation`).
5. **Interactive Calculation Engine Support:** Structured storage of mathematical variables, equations, validation boundaries, and unit conversion matrices.

*Note: This document represents a structural design deliverable. No Kotlin Room entity classes or SQL DDL scripts are implemented in this phase.*

---

## 2. Entity Relationship Overview
```
+------------------+       1:N       +---------------------+       1:N       +------------------+
|   SystemFamily   | --------------> |  EquipmentCategory  | --------------> |    Equipment     |
+------------------+                 +---------------------+                 +------------------+
                                                                                       |
                                +------------------------------------------------------+
                                |              |                  |                    |
                               v 1:N          v 1:N              v 1:N                v 1:N
                      +----------------+ +---------------+ +-----------------+ +-------------------+
                      | EquipmentSubtype | | KnowledgeSec  | | MaintenanceProc | |    FailureMode    |
                      +----------------+ +---------------+ +-----------------+ +-------------------+
                               |                 |                 |                     |
                               v 1:N             v                 v                     v
                       +---------------+   (21 Sections)     (Checklists)          (Troubleshooting)
                       |   Component   |
                       +---------------+

+------------------+       M:N       +---------------------+
|     Subject      | <=============> |  Equipment/Module   | (Theory to Practice Cross-Link)
+------------------+                 +---------------------+
        |
        v 1:N
+------------------+       1:N       +---------------------+
|   Calculation    | --------------> |   FormulaVariable   |
+------------------+                 +---------------------+
```

---

## 3. Entity Definitions & Attribute Specifications

### 3.1 Domain Group A: Equipment & System Taxonomy

#### Entity: `SystemFamily`
- `id` (PK, TEXT): Unique slug (e.g. `FAM-A-PROD-WASH`, `FAM-G-BOILERS`, `FAM-F-STEAM`).
- `code` (TEXT): Single-letter identifier (A through P).
- `name` (TEXT): Display name (e.g. "Production and Washing Machinery").
- `description` (TEXT): Engineering overview of the system family.
- `display_order` (INTEGER): UI ordering index.

#### Entity: `EquipmentCategory`
- `id` (PK, TEXT): Category slug (e.g. `CAT-BELLY-WASHER`, `CAT-FEED-PUMPS`, `CAT-GAS-BOILER`).
- `family_id` (FK -> `SystemFamily.id`, TEXT): Parent system family.
- `name` (TEXT): Name (e.g. "Industrial Belly Washers").
- `applicability` (TEXT): `[Common]`, `[Plant-Dependent]`, `[Optional/Specialized]`, `[Unknown]`.
- `icon_res` (TEXT): Vector icon identifier for Android drawable.

#### Entity: `Equipment`
- `id` (PK, TEXT): Matrix ID (e.g. `EQ-A01`, `EQ-D02`, `EQ-G01`).
- `category_id` (FK -> `EquipmentCategory.id`, TEXT): Parent category.
- `name` (TEXT): Machine title (e.g. "Horizontal Front-Loading Industrial Belly Washer").
- `content_depth_status` (TEXT): `FullBlueprint`, `PartialBlueprint`, `InventoryOnly`, `ResearchPending`.
- `alternative_names` (TEXT): Synonyms used in Bangladeshi/Indian garment washing mills.
- `main_function` (TEXT): Concise engineering purpose.
- `operating_principle` (TEXT): Summary of physical operating principle.
- `steam_dependent` (BOOLEAN): Flag indicating direct or indirect steam utility reliance.
- `safety_critical` (TEXT): `Low`, `Medium`, `High`, `Critical`.
- `verification_status` (TEXT): Evidence classification tag (`A`, `B`, `C`, `D`, `E`, `F`).
- `owner_confirmation_required` (TEXT): Specific parameters needing owner's plant input.

#### Entity: `EquipmentSubtype`
- `id` (PK, TEXT): Subtype slug (e.g. `SUB-EQ-A01-OPEN-POCKET`, `SUB-EQ-A01-DIVIDED`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Parent equipment.
- `subtype_name` (TEXT): Subtype title.
- `distinguishing_features` (TEXT): Mechanical distinctions, capacity variations, or door arrangements.
- `plant_variations` (TEXT): Operational differences between plants.

#### Entity: `Component`
- `id` (PK, TEXT): Component slug (e.g. `COMP-WASH-TRUNNION-SHAFT`, `COMP-BOILER-MOBREY`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Host equipment.
- `is_auxiliary` (BOOLEAN): True if auxiliary/peripheral equipment; False if main machine body.
- `name` (TEXT): Component name (e.g. "Main Cantilevered Trunnion Shaft").
- `material_spec` (TEXT): Metallurgy (e.g. "EN8 / AISI 1040 Carbon Steel or AISI 316L Sleeve").
- `function_description` (TEXT): Functional mechanical role and stresses endured.
- `common_failure_mode` (TEXT): Primary degradation mode (fatigue, wear, cavitation, pitting).

#### Entity: `TechnicalParameter`
- `id` (PK, TEXT): Parameter slug.
- `equipment_id` (FK -> `Equipment.id`, TEXT): Associated equipment.
- `parameter_name` (TEXT): Parameter title (e.g. "Drum Diameter", "Motor Power", "G-Factor").
- `nominal_value` (TEXT): Baseline nominal value or range.
- `unit` (TEXT): SI / Engineering unit (`mm`, `kW`, `RPM`, `bar`, `kg/hr`).
- `is_unverified` (BOOLEAN): True if empirical or plant-specific; False if verified by standard.
- `verification_notes` (TEXT): Source reference or owner confirmation requirement.

---

### 3.2 Domain Group B: 21-Section Knowledge Architecture

#### Entity: `KnowledgeSection`
- `id` (PK, TEXT): Section ID (e.g. `SEC-EQA01-01` to `SEC-EQA01-21`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Target equipment.
- `section_number` (INTEGER): Index from 1 to 21 matching the Standard Equipment Knowledge Template.
- `section_title` (TEXT): Title (e.g. "Preventive Maintenance", "Troubleshooting", "Calculations").
- `content_markdown` (TEXT): Comprehensive engineering text formatted in markdown.
- `is_applicable` (BOOLEAN): True if applicable; False if explicitly non-applicable with recorded reason.
- `non_applicable_reason` (TEXT, Nullable): Engineering justification if section is marked non-applicable.

#### Entity: `Diagram`
- `id` (PK, TEXT): Diagram identifier (e.g. `DIAG-BOILER-SCOTCH-WETBACK`, `DIAG-HYDRO-SUSPENSION`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Related equipment.
- `section_id` (FK -> `KnowledgeSection.id`, TEXT, Nullable): Related knowledge section.
- `title` (TEXT): Diagram title.
- `diagram_type` (TEXT): `ExplodedView`, `Schematic`, `P&ID`, `CrossSection`, `Assembly`.
- `asset_uri` (TEXT): Android asset path to vector SVG or high-res WebP image.
- `caption` (TEXT): Detailed caption highlighting numbered part callouts.

---

### 3.3 Domain Group C: Maintenance, Checklists & Troubleshooting

#### Entity: `MaintenanceProcedure`
- `id` (PK, TEXT): Procedure identifier (e.g. `PROC-WASHER-SEAL-REPLACE`, `PROC-BOILER-BLOWDOWN`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Associated equipment.
- `procedure_type` (TEXT): `DailyShift`, `WeeklyPM`, `MonthlyPM`, `AnnualOverhaul`, `ConditionMonitoring`.
- `title` (TEXT): Descriptive procedure name.
- `loto_required` (BOOLEAN): Lock-Out Tag-Out safety isolation requirement flag.
- `ppe_required` (TEXT): Required PPE (Safety boots, goggles, heat-resistant gloves, ear protection).
- `step_by_step_instructions` (TEXT): Sequenced actionable technical steps.

#### Entity: `InspectionChecklist`
- `id` (PK, TEXT): Checklist ID (e.g. `CHK-BOILER-DAILY-SHIFT`, `CHK-COMPRESSOR-WEEKLY`).
- `equipment_id` (FK -> `Equipment.id`, TEXT): Associated equipment.
- `frequency` (TEXT): `PerShift`, `Daily`, `Weekly`, `Monthly`, `Quarterly`, `Annual`.
- `title` (TEXT): Checklist title.

#### Entity: `ChecklistItem`
- `id` (PK, TEXT): Item ID.
- `checklist_id` (FK -> `InspectionChecklist.id`, TEXT): Parent checklist.
- `item_order` (INTEGER): Sequence order.
- `checkpoint_description` (TEXT): Inspection target (e.g. "Test Mobrey water level chamber blowdown").
- `acceptable_condition` (TEXT): Nominal standard (e.g. "Burner cut-off alarms sound within 3 seconds").
- `action_if_defective` (TEXT): Mandatory remediation if standard is not met.

#### Entity: `FailureMode`
- `id` (PK, TEXT): Failure record identifier.
- `equipment_id` (FK -> `Equipment.id`, TEXT): Associated equipment.
- `symptom` (TEXT): Audible, visible, or operational symptom (e.g. "Excessive vibration during wash reverse").
- `probable_cause` (TEXT): Mechanical explanation (e.g. "Worn trunnion bearings or loose plummer block bolts").
- `root_cause_analysis` (TEXT): Deep metallurgical or operational cause.
- `corrective_action` (TEXT): Immediate maintenance fix.
- `preventive_measure` (TEXT): Long-term protocol to prevent recurrence.
- `severity` (TEXT): `Minor`, `Major`, `CriticalShutdown`.

---

### 3.4 Domain Group D: Calculations Engine & Formula Architecture

#### Entity: `Calculation`
- `id` (PK, TEXT): Calculation ID (`CALC-01` to `CALC-14`).
- `title` (TEXT): Name of engineering calculation.
- `domain_discipline` (TEXT): Thermodynamics, Fluid Mechanics, Machine Design, Heat Transfer.
- `formula_latex` (TEXT): LaTeX formatted mathematical representation.
- `summary_description` (TEXT): Practical purpose of the calculation in plant operations.
- `assumptions` (TEXT): Physical assumptions (e.g. steady flow, negligible heat loss).
- `validity_conditions` (TEXT): Operating boundaries where calculation is valid.
- `worked_example` (TEXT): Complete numerical walk-through with input parameters and expected output.
- `independent_verification_method` (TEXT): Cross-check formula or physical method.
- `reference_source_id` (FK -> `ReferenceSource.id`, TEXT): Citation linking to authoritative code.

#### Entity: `FormulaVariable`
- `id` (PK, TEXT): Variable ID.
- `calculation_id` (FK -> `Calculation.id`, TEXT): Parent calculation.
- `symbol` (TEXT): Mathematical symbol (e.g. $Q_s$, $h_g$, $P$, $\eta$).
- `name` (TEXT): Plain name (e.g. "Steam Generation Rate", "Enthalpy of Saturated Steam").
- `unit` (TEXT): Standard engineering unit (e.g. `kg/hr`, `kJ/kg`, `bar(g)`, `m3/min`, `%`).
- `is_input` (BOOLEAN): True if entered by user; False if calculated result.
- `default_value` (REAL, Nullable): Realistic baseline value for washing plant machinery.
- `min_value` (REAL, Nullable): Minimum plausible physical boundary.
- `max_value` (REAL, Nullable): Maximum plausible physical boundary.
- `validation_message` (TEXT, Nullable): Warning message if user enters value outside physical boundaries.

---

### 3.5 Domain Group E: References & Cross-Discipline Mapping

#### Entity: `ReferenceSource`
- `id` (PK, TEXT): Source identifier matching `SRC-xxx` (e.g. `SRC-001`, `SRC-011`).
- `title` (TEXT): Title of standard, act, or publication.
- `author_publisher` (TEXT): ASME, ISO, CEN, Parliament of Bangladesh, Spirax Sarco, SKF.
- `publication_id` (TEXT): Code, Act number, or ISBN.
- `current_status` (TEXT): `Current`, `Withdrawn`, `Superseded`, `Statutory`.
- `official_url` (TEXT): Official repository or publishing body link.

#### Entity: `CrossReference`
- `id` (PK, TEXT): Compound relationship ID.
- `source_entity_type` (TEXT): `Subject`, `Equipment`, `Calculation`, `Component`.
- `source_entity_id` (TEXT): Identifier of source.
- `target_entity_type` (TEXT): `Subject`, `Equipment`, `Calculation`, `Component`.
- `target_entity_id` (TEXT): Identifier of target.
- `relationship_note` (TEXT): Engineering explanation of link.

---

## 4. SQLite / Room Query & Indexing Strategy
To guarantee <100ms response time on mobile devices:
1. **Full-Text Search (FTS5):** A virtual SQLite table `fts_knowledge_search` indexing `Equipment.name`, `Equipment.alternative_names`, `Component.name`, `FailureMode.symptom`, `FailureMode.probable_cause`, and `KnowledgeSection.content_markdown`.
2. **Compound Indexing:**
   - Index on `(category_id, applicability)` in `Equipment`.
   - Index on `(equipment_id, content_depth_status)` in `Equipment`.
   - Index on `(equipment_id, section_number)` in `KnowledgeSection`.
   - Index on `(equipment_id, severity)` in `FailureMode`.
   - Index on `(calculation_id, is_input)` in `FormulaVariable`.
3. **Read-Only In-Memory Pre-Population:** Core reference database shipped as pre-packaged SQLite asset (`/assets/databases/mechanical_pro.db`), eliminating runtime startup parsing.
