# PHASE 03: DATABASE ARCHITECTURE SPECIFICATION
**Document Identifier:** DOC-MPN-014  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED DATABASE ARCHITECTURAL SPECIFICATION - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Offline Database Engineer  
**Date:** 2026-11-12  

---

## 1. Executive Database Overview

Mechanical Pro Notes relies on an embedded **Android Room SQLite Database** (`androidx.room`) designed for 100% offline querying, sub-millisecond retrieval, robust referential integrity, and lightning-fast full-text search via **SQLite FTS5**.

### Key Architectural Principles:
1. **Pre-Packaged Asset Seeding:** The static baseline knowledge base (64 equipment records across 16 system families, industrial boiler specifications, 14 verified calculations, inspection checklists, and technical standards) is compiled into a pristine read-only SQLite database file (`mechanical_pro_notes.db`) located in `app/src/main/assets/database/`. Room initializes this on first app launch using `.createFromAsset("database/mechanical_pro_notes.db")`.
2. **Separation of Static vs Dynamic Data:**
   - **Static Reference Tables:** Pre-populated, immutable during normal app operation, updated only via formal app version migrations.
   - **Dynamic User Tables:** Storing user personal notes, custom checklist completion logs, bookmarks, and calculation history. These tables survive database upgrades and can be independently exported as JSON.
3. **Content Depth Transparency:** Every equipment entity stores an explicit enum `content_depth_status` (`FULL_CONTENT_BLUEPRINT`, `PARTIAL_CONTENT_BLUEPRINT`, `INVENTORY_ONLY`, `RESEARCH_PENDING`). The UI queries this field to render appropriate badges, preventing inventory-only records from masquerading as complete engineering guides.

---

## 2. Entity-Relationship Architecture (Room SQLite Tables)

```
+-------------------+       +-----------------------+       +-------------------+
|   system_family   | 1---N |  equipment_category   | 1---N |     equipment     |
|-------------------|       |-----------------------|       |-------------------|
| PK id (VARCHAR)   |       | PK id (VARCHAR)       |       | PK id (VARCHAR)   |
| name              |       | FK family_id          |       | FK category_id    |
| description       |       | name                  |       | name              |
| sort_order        |       | description           |       | bengali_name      |
+-------------------+       +-----------------------+       | model_numbers     |
                                                            | content_depth     |
                                                            | criticality       |
                                                            | updated_at        |
                                                            +-------------------+
                                                              | 1        | 1
                                                              |          |
                                       +----------------------+          +----------------------+
                                       | N                                                      | N
                            +-----------------------+                                +-----------------------+
                            |   equipment_subtype   |                                |   knowledge_section   |
                            |-----------------------|                                |-----------------------|
                            | PK id (VARCHAR)       |                                | PK id (INTEGER AUTO)  |
                            | FK equipment_id       |                                | FK equipment_id       |
                            | subtype_name          |                                | section_number (1-21) |
                            | drive_type            |                                | section_title         |
                            | typical_capacity      |                                | content_markdown      |
                            +-----------------------+                                +-----------------------+
                                       | 1                                                      |
                                       | N                                                      |
                            +-----------------------+                                +-----------------------+
                            |       component       |                                |   technical_parameter |
                            |-----------------------|                                |-----------------------|
                            | PK id (VARCHAR)       |                                | PK id (INTEGER AUTO)  |
                            | FK equipment_id       |                                | FK equipment_id       |
                            | component_name        |                                | parameter_name        |
                            | material_spec         |                                | standard_value        |
                            | function_description  |                                | unit                  |
                            | wear_tier             |                                | tolerance_range       |
                            +-----------------------+                                +-----------------------+

+-------------------------------------------------------------------------------------------------------+
|                                    DYNAMIC USER DATA ENTITIES                                         |
|  +-----------------------+   +-----------------------+   +-----------------------------------------+  |
|  |       user_note       |   |     user_bookmark     |   |          calculation_history            |  |
|  |-----------------------|   |-----------------------|   |-----------------------------------------|  |
|  | PK id (INTEGER AUTO)  |   | PK id (INTEGER AUTO)  |   | PK id (INTEGER AUTO)                    |  |
|  | FK equipment_id       |   | entity_type           |   | FK calculation_id                       |  |
|  | note_title            |   | entity_id             |   | execution_timestamp                     |  |
|  | note_body             |   | created_at            |   | input_values_json                       |  |
|  | tags_csv              |   +-----------------------+   | output_values_json                      |  |
|  | created_at            |                               | notes                                   |  |
|  | updated_at            |                               +-----------------------------------------+  |
|  +-----------------------+                                                                            |
+-------------------------------------------------------------------------------------------------------+
```

---

## 3. Detailed Table Schema Definitions

### 3.1 Static Knowledge Base Entities

#### 1. `system_family`
- `id` TEXT PRIMARY KEY (e.g., `'FAM-A'`, `'FAM-B'`)
- `code` TEXT NOT NULL UNIQUE (e.g., `'WASHING_MACHINERY'`, `'BOILERS'`)
- `name` TEXT NOT NULL
- `bengali_name` TEXT NOT NULL
- `icon_name` TEXT NOT NULL
- `sort_order` INTEGER NOT NULL

#### 2. `equipment_category`
- `id` TEXT PRIMARY KEY (e.g., `'CAT-A01'`)
- `family_id` TEXT NOT NULL REFERENCES `system_family(id)` ON DELETE RESTRICT
- `name` TEXT NOT NULL
- `bengali_name` TEXT NOT NULL
- `description` TEXT

#### 3. `equipment` (Master Table for 64 Assets)
- `id` TEXT PRIMARY KEY (e.g., `'EQ-A01'`, `'EQ-G01'`)
- `category_id` TEXT NOT NULL REFERENCES `equipment_category(id)` ON DELETE RESTRICT
- `family_id` TEXT NOT NULL REFERENCES `system_family(id)` ON DELETE RESTRICT
- `name` TEXT NOT NULL
- `bengali_name` TEXT NOT NULL
- `aliases_csv` TEXT
- `content_depth_status` TEXT NOT NULL (Enum: `FULL_CONTENT_BLUEPRINT`, `PARTIAL_CONTENT_BLUEPRINT`, `INVENTORY_ONLY`, `RESEARCH_PENDING`)
- `criticality_tier` TEXT NOT NULL (Enum: `CRITICAL_UTILITY`, `PRIMARY_PRODUCTION`, `SECONDARY_AUXILIARY`, `SAFETY_CRITICAL`)
- `standard_capacity_range` TEXT
- `operating_temperature_range` TEXT
- `primary_drive_type` TEXT
- `lubrication_regime` TEXT
- `statutory_requirement` TEXT
- `created_at` INTEGER NOT NULL
- `updated_at` INTEGER NOT NULL

#### 4. `equipment_subtype`
- `id` TEXT PRIMARY KEY (e.g., `'SUB-A01-1'`)
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `subtype_name` TEXT NOT NULL
- `bengali_name` TEXT NOT NULL
- `distinguishing_features` TEXT NOT NULL
- `typical_capacity` TEXT NOT NULL

#### 5. `component`
- `id` TEXT PRIMARY KEY (e.g., `'CMP-A01-01'`)
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `name` TEXT NOT NULL
- `bengali_name` TEXT NOT NULL
- `material_specification` TEXT NOT NULL (e.g., `'AISI 316L'`, `'Forged EN19'`)
- `primary_function` TEXT NOT NULL
- `wear_tier` TEXT NOT NULL (Enum: `CONSUMABLE`, `WEAR_PART`, `STRUCTURAL_PERMANENT`)
- `maintenance_method` TEXT NOT NULL

#### 6. `technical_parameter`
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `parameter_group` TEXT NOT NULL (e.g., `'ELECTRICAL'`, `'THERMAL'`, `'MECHANICAL'`)
- `parameter_name` TEXT NOT NULL
- `standard_value` TEXT NOT NULL
- `unit` TEXT NOT NULL
- `tolerance` TEXT

#### 7. `knowledge_section` (Supports 21 Standard Sections per Machine)
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `section_number` INTEGER NOT NULL (1 to 21)
- `section_title` TEXT NOT NULL
- `content_markdown` TEXT NOT NULL
- `verification_tier` TEXT NOT NULL (Enum: `TIER_1_STATUTORY`, `TIER_2_OEM`, `TIER_3_HANDBOOK`, `TIER_4_EMPIRICAL`)

#### 8. `maintenance_procedure`
- `id` TEXT PRIMARY KEY (e.g., `'MNT-A01-01'`)
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `procedure_title` TEXT NOT NULL
- `maintenance_frequency` TEXT NOT NULL (Enum: `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `BI_ANNUAL`, `ANNUAL`)
- `required_tools_csv` TEXT NOT NULL
- `safety_lockout_procedure` TEXT NOT NULL (LOTO steps)
- `action_steps_json` TEXT NOT NULL

#### 9. `inspection_checklist` & `checklist_item`
- `checklist_id` TEXT PRIMARY KEY
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `frequency` TEXT NOT NULL
- `item_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `item_text` TEXT NOT NULL
- `acceptance_criteria` TEXT NOT NULL
- `is_statutory_mandate` INTEGER NOT NULL DEFAULT 0

#### 10. `failure_mode`
- `id` TEXT PRIMARY KEY
- `equipment_id` TEXT NOT NULL REFERENCES `equipment(id)` ON DELETE CASCADE
- `symptom_description` TEXT NOT NULL
- `probable_root_cause` TEXT NOT NULL
- `immediate_diagnostic_step` TEXT NOT NULL
- `corrective_action` TEXT NOT NULL
- `vibration_signature` TEXT
- `severity_level` TEXT NOT NULL

#### 11. `calculation` & `formula_variable` (Supports 14 Boiler Calculations)
- `id` TEXT PRIMARY KEY (e.g., `'CALC-01'`, `'CALC-05'`)
- `code` TEXT NOT NULL UNIQUE (e.g., `'BOILER_DIRECT_EFFICIENCY'`, `'BLOWDOWN_RATE'`)
- `title` TEXT NOT NULL
- `bengali_title` TEXT NOT NULL
- `category` TEXT NOT NULL (Enum: `BOILER`, `PUMP`, `COMPRESSED_AIR`, `PIPING`)
- `formula_latex` TEXT NOT NULL
- `engineering_reference` TEXT NOT NULL
- `physical_assumptions` TEXT NOT NULL
- `limitations` TEXT NOT NULL
- `variable_id` INTEGER PRIMARY KEY AUTOINCREMENT
- `calculation_id` TEXT NOT NULL REFERENCES `calculation(id)` ON DELETE CASCADE
- `variable_symbol` TEXT NOT NULL
- `variable_name` TEXT NOT NULL
- `standard_unit` TEXT NOT NULL
- `default_value` REAL
- `min_valid_bound` REAL
- `max_valid_bound` REAL

#### 12. `reference_source`
- `id` TEXT PRIMARY KEY (e.g., `'SRC-01'`)
- `short_title` TEXT NOT NULL
- `full_citation` TEXT NOT NULL
- `tier` TEXT NOT NULL
- `official_url` TEXT
- `status` TEXT NOT NULL (Enum: `ACTIVE`, `WITHDRAWN`, `SUPERSEDED`)

---

## 4. SQLite FTS5 Full-Text Search Virtual Table Architecture

To enable instant (<30ms) offline search across thousands of pages of mechanical documentation, Room configures an SQLite **FTS5** virtual table:

```sql
CREATE VIRTUAL TABLE IF NOT EXISTS equipment_fts USING fts5 (
    equipment_id UNINDEXED,
    name,
    bengali_name,
    aliases,
    category_name,
    family_name,
    components_summary,
    failure_symptoms,
    technical_keywords,
    tokenize = 'unicode61 remove_diacritics 2'
);
```

### Auto-Sync Triggers:
SQLite triggers automatically synchronize `equipment_fts` whenever an equipment record or failure mode is inserted, updated, or migrated, guaranteeing that the search index is never stale.

---

## 5. Indexing Strategy for Zero-Latency Performance

To ensure smooth 60+ FPS navigation on mid-range and low-memory Android phones, the following compound and foreign key indexes are created:

1. `CREATE INDEX idx_equipment_family ON equipment(family_id);`
2. `CREATE INDEX idx_equipment_category ON equipment(category_id);`
3. `CREATE INDEX idx_equipment_depth ON equipment(content_depth_status);`
4. `CREATE INDEX idx_knowledge_eq_section ON knowledge_section(equipment_id, section_number);`
5. `CREATE INDEX idx_component_eq_wear ON component(equipment_id, wear_tier);`
6. `CREATE INDEX idx_param_eq_group ON technical_parameter(equipment_id, parameter_group);`
7. `CREATE INDEX idx_failure_eq_severity ON failure_mode(equipment_id, severity_level);`
8. `CREATE INDEX idx_user_note_eq ON user_note(equipment_id);`

---

## 6. Pre-Packaged Database Seeding vs Runtime Population

### Evaluation of Seeding Strategies:
| Strategy | Cold Launch Time | Memory Usage | Implementation Safety | Recommendation |
|---|---|---|---|---|
| **Runtime JSON / CSV Parsing** | Poor (3.5 to 8.0 seconds on first launch) | High spike (parsing multi-MB JSON into objects) | High risk of OOM crash on low-end phones | **REJECTED** |
| **Room `createFromAsset()`** | Instant (<150ms first launch) | Zero heap overhead (SQLite memory mapped) | 100% deterministic, pre-indexed, verified at build time | **RECOMMENDED PRIMARY CHOICE** |

### Implementation Workflow:
1. In the build pipeline, the authoritative markdown and JSON knowledge base is compiled into an SQLite file: `mechanical_pro_notes.db`.
2. The SQLite file is placed into `app/src/main/assets/database/mechanical_pro_notes.db`.
3. Room builds the database instance:
   ```kotlin
   Room.databaseBuilder(context, MechanicalProDatabase::class.java, "mechanical_pro_notes.db")
       .createFromAsset("database/mechanical_pro_notes.db")
       .fallbackToDestructiveMigrationOnDowngrade()
       .build()
   ```

---

## 7. Migration & Versioning Strategy

- **Version 1 (Initial Release):** Baseline schema with 64 equipment assets, 14 boiler calculations, and initial user tables.
- **Future Content Expansions (e.g. enriching `[RESEARCH PENDING]` records):**
  - Managed via strictly numbered `Migration(1, 2)`, `Migration(2, 3)`.
  - Migrations only alter static reference tables via `INSERT OR REPLACE` or `UPDATE` queries.
  - **Inviolable Rule:** User dynamic data (`user_note`, `user_bookmark`, `calculation_history`) MUST NEVER be dropped or wiped during schema migrations.
