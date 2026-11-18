#!/usr/bin/env python3
"""
Rebuild android/app/src/main/assets/database/mechanical_pro_notes.db
from scratch with:
  1. Exact Room-compatible DDL, indices, Foreign Keys, FTS4, and user_version = 1.
  2. All 16 System Families (FAM-A..FAM-P) and Categories (CAT-A01..CAT-P01).
  3. All 64 Reconciled Equipment Records upgraded for GAP-13:
     (56 FULL_CONTENT_BLUEPRINT, 1 PARTIAL_CONTENT_BLUEPRINT [EQ-G02], 7 RESEARCH_PENDING, 0 INVENTORY_ONLY).
  4. Equipment-specific subtypes, components, technical parameters, failure modes,
     PM procedures, and profile-tailored knowledge sections across all 64 machines.
  5. All 20 Phase 06 / Batch 12 Engineering Calculations & 87 Formula Variables (CALC-02 = IMPLEMENTED).
"""

import os
import sqlite3
from seed_phase06_calculations import CALCULATIONS, VARIABLES
from seed_equipment_part1 import PART1_SPECS
from seed_equipment_part2 import PART2_SPECS

DB_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "android/app/src/main/assets/database/mechanical_pro_notes.db",
)
NOW_TS = 1794500000000

SCHEMA_SQL = """
PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS equipment_fts;
DROP TABLE IF EXISTS checklist_progress;
DROP TABLE IF EXISTS calculation_history;
DROP TABLE IF EXISTS user_bookmark;
DROP TABLE IF EXISTS user_note;
DROP TABLE IF EXISTS formula_variable;
DROP TABLE IF EXISTS calculation;
DROP TABLE IF EXISTS maintenance_procedure;
DROP TABLE IF EXISTS failure_mode;
DROP TABLE IF EXISTS knowledge_section;
DROP TABLE IF EXISTS technical_parameter;
DROP TABLE IF EXISTS component;
DROP TABLE IF EXISTS equipment_subtype;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS equipment_category;
DROP TABLE IF EXISTS system_family;

PRAGMA foreign_keys = ON;
PRAGMA user_version = 1;

CREATE TABLE system_family (
    id TEXT PRIMARY KEY NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    bengali_name TEXT NOT NULL,
    icon_name TEXT NOT NULL,
    sort_order INTEGER NOT NULL,
    is_dedicated_boiler INTEGER NOT NULL
);

CREATE TABLE equipment_category (
    id TEXT PRIMARY KEY NOT NULL,
    family_id TEXT NOT NULL,
    name TEXT NOT NULL,
    bengali_name TEXT NOT NULL,
    description TEXT,
    FOREIGN KEY(family_id) REFERENCES system_family(id) ON UPDATE NO ACTION ON DELETE RESTRICT
);
CREATE INDEX index_equipment_category_family_id ON equipment_category (family_id);

CREATE TABLE equipment (
    id TEXT PRIMARY KEY NOT NULL,
    category_id TEXT NOT NULL,
    family_id TEXT NOT NULL,
    name TEXT NOT NULL,
    bengali_name TEXT NOT NULL,
    aliases_csv TEXT,
    content_depth_status TEXT NOT NULL,
    criticality_tier TEXT NOT NULL,
    capacity_range TEXT,
    operating_temperature TEXT,
    primary_drive_type TEXT,
    lubrication_regime TEXT,
    statutory_requirement TEXT,
    is_plant_dependent INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(category_id) REFERENCES equipment_category(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
    FOREIGN KEY(family_id) REFERENCES system_family(id) ON UPDATE NO ACTION ON DELETE RESTRICT
);
CREATE INDEX index_equipment_category_id ON equipment (category_id);
CREATE INDEX index_equipment_family_id ON equipment (family_id);
CREATE INDEX index_equipment_content_depth_status ON equipment (content_depth_status);

CREATE TABLE equipment_subtype (
    id TEXT PRIMARY KEY NOT NULL,
    equipment_id TEXT NOT NULL,
    subtype_name TEXT NOT NULL,
    bengali_name TEXT NOT NULL,
    distinguishing_features TEXT NOT NULL,
    typical_capacity TEXT NOT NULL,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_equipment_subtype_equipment_id ON equipment_subtype (equipment_id);

CREATE TABLE component (
    id TEXT PRIMARY KEY NOT NULL,
    equipment_id TEXT NOT NULL,
    name TEXT NOT NULL,
    bengali_name TEXT NOT NULL,
    material_specification TEXT NOT NULL,
    primary_function TEXT NOT NULL,
    wear_tier TEXT NOT NULL,
    maintenance_method TEXT NOT NULL,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_component_equipment_id ON component (equipment_id);
CREATE INDEX index_component_wear_tier ON component (wear_tier);

CREATE TABLE technical_parameter (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    equipment_id TEXT NOT NULL,
    parameter_group TEXT NOT NULL,
    parameter_name TEXT NOT NULL,
    standard_value TEXT NOT NULL,
    unit TEXT NOT NULL,
    tolerance TEXT,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_technical_parameter_equipment_id ON technical_parameter (equipment_id);

CREATE TABLE knowledge_section (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    equipment_id TEXT NOT NULL,
    section_number INTEGER NOT NULL,
    section_title TEXT NOT NULL,
    content_markdown TEXT NOT NULL,
    verification_tier TEXT NOT NULL,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE UNIQUE INDEX index_knowledge_section_equipment_id_section_number ON knowledge_section (equipment_id, section_number);

CREATE TABLE failure_mode (
    id TEXT PRIMARY KEY NOT NULL,
    equipment_id TEXT NOT NULL,
    symptom TEXT NOT NULL,
    root_cause TEXT NOT NULL,
    diagnostic_step TEXT NOT NULL,
    corrective_action TEXT NOT NULL,
    vibration_signature TEXT,
    severity TEXT NOT NULL,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_failure_mode_equipment_id ON failure_mode (equipment_id);

CREATE TABLE maintenance_procedure (
    id TEXT PRIMARY KEY NOT NULL,
    equipment_id TEXT NOT NULL,
    title TEXT NOT NULL,
    frequency TEXT NOT NULL,
    required_tools_csv TEXT NOT NULL,
    safety_lockout_procedure TEXT NOT NULL,
    action_steps_json TEXT NOT NULL,
    FOREIGN KEY(equipment_id) REFERENCES equipment(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_maintenance_procedure_equipment_id ON maintenance_procedure (equipment_id);

CREATE TABLE calculation (
    id TEXT PRIMARY KEY NOT NULL,
    code TEXT NOT NULL,
    title TEXT NOT NULL,
    bengali_title TEXT NOT NULL,
    category TEXT NOT NULL,
    formula_latex TEXT NOT NULL,
    engineering_reference TEXT NOT NULL,
    assumptions TEXT NOT NULL,
    limitations TEXT NOT NULL,
    status TEXT NOT NULL
);
CREATE UNIQUE INDEX index_calculation_code ON calculation (code);

CREATE TABLE formula_variable (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    calculation_id TEXT NOT NULL,
    symbol TEXT NOT NULL,
    name TEXT NOT NULL,
    unit TEXT NOT NULL,
    default_value REAL,
    min_bound REAL,
    max_bound REAL,
    is_required INTEGER NOT NULL,
    FOREIGN KEY(calculation_id) REFERENCES calculation(id) ON UPDATE NO ACTION ON DELETE CASCADE
);
CREATE INDEX index_formula_variable_calculation_id ON formula_variable (calculation_id);

CREATE TABLE user_note (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    equipment_id TEXT NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    tags_csv TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
CREATE INDEX index_user_note_equipment_id ON user_note (equipment_id);
CREATE INDEX index_user_note_created_at ON user_note (created_at);

CREATE TABLE user_bookmark (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    entity_title TEXT NOT NULL,
    created_at INTEGER NOT NULL
);
CREATE UNIQUE INDEX index_user_bookmark_entity_type_entity_id ON user_bookmark (entity_type, entity_id);

CREATE TABLE calculation_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    calculation_id TEXT NOT NULL,
    calculation_title TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    inputs_json TEXT NOT NULL,
    outputs_json TEXT NOT NULL,
    user_remark TEXT
);
CREATE INDEX index_calculation_history_calculation_id ON calculation_history (calculation_id);
CREATE INDEX index_calculation_history_timestamp ON calculation_history (timestamp);

CREATE TABLE checklist_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    checklist_id TEXT NOT NULL,
    item_id INTEGER NOT NULL,
    is_completed INTEGER NOT NULL,
    completion_timestamp INTEGER,
    remarks TEXT
);
CREATE UNIQUE INDEX index_checklist_progress_checklist_id_item_id ON checklist_progress (checklist_id, item_id);

CREATE VIRTUAL TABLE equipment_fts USING FTS4 (
    id,
    name,
    bengali_name,
    aliases_csv,
    content_depth_status,
    primary_drive_type
);
"""

SYSTEM_FAMILIES = [
    ("FAM-A", "WASHING_MACHINERY", "Production & Washing Machinery", "উৎপাদন ও ওয়াশিং যন্ত্রপাতি", "ic_washing", 1, 0),
    ("FAM-B", "EXTRACTION_DRYING", "Extraction & Drying Systems", "এক্সট্রাকশন ও ড্রায়িং সিস্টেম", "ic_extractor", 2, 0),
    ("FAM-C", "FINISHING_SPECIALIZED", "Garment Finishing & Specialized Processing", "গার্মেন্টস ফিনিশিং ও স্পেশাল প্রসেস", "ic_finishing", 3, 0),
    ("FAM-D", "PUMPS_FLUID_SYSTEMS", "Pumps & Fluid Handling Equipment", "পাম্প ও ফ্লুইড হ্যান্ডলিং যন্ত্রপাতি", "ic_pump", 4, 0),
    ("FAM-E", "POWER_TRANSMISSION", "Mechanical Power Transmission & Tribology", "পাওয়ার ট্রান্সমিশন ও বিয়ারিং সিস্টেম", "ic_transmission", 5, 0),
    ("FAM-F", "STEAM_DISTRIBUTION", "Steam & Thermal Distribution Systems", "স্টিম ও থার্মাল ডিস্ট্রিবিউশন সিস্টেম", "ic_steam", 6, 0),
    ("FAM-G", "BOILERS", "Industrial Boiler Systems (Gas & Jhut-Fired)", "ইন্ডাস্ট্রিয়াল বয়লার সিস্টেম (গ্যাস ও ঝুট)", "ic_boiler", 7, 1),
    ("FAM-H", "WATER_TREATMENT", "Water Treatment & Process Distribution", "ওয়াটার ট্রিটমেন্ট ও প্রসেস লাইন", "ic_water", 8, 0),
    ("FAM-I", "CHEMICAL_DOSING", "Chemical Storage, Mixing & Dosing Systems", "কেমিক্যাল মিক্সিং ও ডোজিং সিস্টেম", "ic_chemical", 9, 0),
    ("FAM-J", "COMPRESSED_AIR", "Compressed Air Generation & Distribution", "কম্প্রেসড এয়ার জেনারেশন ও লাইন", "ic_air", 10, 0),
    ("FAM-K", "COOLING_REFRIGERATION", "Cooling & Industrial Refrigeration Systems", "কুলিং টাওয়ার ও ইন্ডাস্ট্রিয়াল চিলার", "ic_cooling", 11, 0),
    ("FAM-L", "EFFLUENT_TREATMENT", "Effluent Treatment Plant (ETP) Machinery", "ইটিপি (ETP) বর্জ্য শোধনাগার যন্ত্রপাতি", "ic_etp", 12, 0),
    ("FAM-M", "MATERIAL_HANDLING", "Material Handling & Overhead Lifting", "ম্যাটেরিয়াল হ্যান্ডলিং ও ওভারহেড হোয়েস্ট", "ic_lifting", 13, 0),
    ("FAM-N", "MAINTENANCE_WORKSHOP", "Maintenance Workshop & Fabrication Machines", "মেইনটেন্যান্স ওয়ার্কশপ ও মেশিন শপ", "ic_workshop", 14, 0),
    ("FAM-O", "PLANT_SAFETY", "Plant Safety & Mechanical Protection Systems", "প্ল্যান্ট সেফটি ও ফায়ার প্রোটেকশন", "ic_safety", 15, 0),
    ("FAM-P", "VENTILATION_EXHAUST", "Plant Ventilation, Exhaust & Dust/Lint Extraction", "ভেন্টিলেশন, এগজস্ট ও লিন্ট এক্সট্রাকশন", "ic_ventilation", 16, 0),
]

CATEGORIES = [
    ("CAT-A01", "FAM-A", "Industrial Washing Machines", "ইন্ডাস্ট্রিয়াল ওয়াশিং মেশিন", "Bulk, divided-pocket, tilting, sample, and sanitary garment washers"),
    ("CAT-B01", "FAM-B", "Centrifugal Hydro Extractors", "সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর", "High-G mechanical dewatering extractors"),
    ("CAT-B02", "FAM-B", "Industrial Garment Dryers", "ইন্ডাস্ট্রিয়াল গার্মেন্টস ড্রায়ার", "Steam, gas, and RF/tunnel garment dryers"),
    ("CAT-C01", "FAM-C", "Denim Curing, Whisker & Spray Units", "কিউরিং ওভেন, হুইস্কার ও স্প্রে বুথ", "Resin curing ovens, 3D whisker mannequins, PP spray booths, ozone & laser units"),
    ("CAT-D01", "FAM-D", "Process Water & Boiler Feed Pumps", "প্রসেস ওয়াটার ও বয়লার ফিড পাম্প", "Centrifugal water and high-pressure multistage boiler feed pumps"),
    ("CAT-D02", "FAM-D", "Chemical, Sump & Fuel Pumps", "কেমিক্যাল, সাম্প ও ফুয়েল পাম্প", "AODD, dosing metering, submersible effluent, and diesel fuel pumps"),
    ("CAT-E01", "FAM-E", "Motors, Gearboxes & Bearings", "মোটর, গিয়ারবক্স ও বিয়ারিং", "Induction motors, reduction gearboxes, and spherical roller plummer blocks"),
    ("CAT-E02", "FAM-E", "Belts, Couplings & Timing Drives", "বেল্ট, কাপলিং ও টাইমিং ড্রাইভ", "V-belt drives, flexible jaw couplings, and synchronous timing belt drives"),
    ("CAT-F01", "FAM-F", "Steam Valves, Traps & PRVs", "স্টিম ভালভ, ট্র্যাপ ও পিআরভি", "Pneumatic angle-seat valves, thermodynamic/float traps, and pilot PRVs"),
    ("CAT-F02", "FAM-F", "Condensate, Heat Recovery & Cladding", "কনডেনসেট, হিট এক্সচেঞ্জার ও ইনসুলেশন", "Flash vessels, plate heat exchangers, and pipe cladding jackets"),
    ("CAT-G01", "FAM-G", "Gas-Fired Industrial Boilers", "গ্যাস-ফায়ার্ড স্টিম বয়লার", "Packaged 3-pass wet-back natural gas fire-tube boilers"),
    ("CAT-G02", "FAM-G", "Solid Fuel / Jhut Steam Boilers", "ঝুট ও বায়োমাস স্টিম বয়লার", "Jhut and solid biomass balanced-draft industrial steam boilers"),
    ("CAT-G03", "FAM-G", "Boiler Safety Mountings & Level Controls", "বয়লার সেফটি ভালভ ও লেভেল কন্ট্রোল", "Full-lift spring safety valves and Mobrey magnetic water level controllers"),
    ("CAT-H01", "FAM-H", "Water Softening, Filtration & Booster Systems", "ওয়াটার সফটনার, ফিল্টার ও বুস্টার", "Sodium cation softeners, sand/carbon filters, RO membranes, and hydro-pneumatic boosters"),
    ("CAT-I01", "FAM-I", "Chemical Prep, Caustic & Safety Containment", "কেমিক্যাল মিক্সিং, কস্টিক ও সেফটি শাওয়ার", "SS316L prep tanks, safety showers, and exothermic caustic dissolving units"),
    ("CAT-J01", "FAM-J", "Compressed Air Generation & Treatment", "কম্প্রেসার, ড্রায়ার, রিসিভার ও ফিল্টার", "Rotary screw compressors, refrigerated dryers, receiver vessels, coalescing filters"),
    ("CAT-K01", "FAM-K", "Cooling Towers & Process Chillers", "কুলিং টাওয়ার ও প্রসেস চিলার", "Induced draft counter-flow cooling towers and industrial water chillers"),
    ("CAT-L01", "FAM-L", "ETP Clarification, Aeration & Dewatering", "ইটিপি ড্যাফ, ব্লোয়ার ও ফিল্টার প্রেস", "DAF flotation, tri-lobe Roots blowers, filter presses, screw pumps, and drum screens"),
    ("CAT-M01", "FAM-M", "Hoists, Trolleys & Wet Conveyors", "চেইন হোয়েস্ট, ট্রলি ও কনভেয়র", "Electric chain hoists, PE garment trolleys, and cleated incline conveyors"),
    ("CAT-N01", "FAM-N", "Mechanical Workshop Fabrication Tools", "লেদ, পিলার ড্রিল ও ওয়েল্ডিং মেশিন", "Gap-bed engine lathes, pillar drills, and TIG/MMA inverter welders"),
    ("CAT-O01", "FAM-O", "Fire & Gas Safety Protection Systems", "ফায়ার পাম্প ও গ্যাস লিক ডিটেকশন", "Diesel emergency fire pumps and combustible gas detection/shutoff systems"),
    ("CAT-P01", "FAM-P", "Exhaust & Lint Extraction Systems", "লিন্ট ও হিউমিডিটি এগজস্ট ব্লোয়ার", "Heavy-duty centrifugal roof exhaust and lint extraction fans"),
]

# 64 Unique Reconciled Equipment Records (EQ-A01..EQ-P01)
EQUIPMENT = [
    # FAMILY A (5)
    ("EQ-A01", "CAT-A01", "FAM-A", "Horizontal Front-Loading Belly Washer (Open Pocket)", "হরাইজন্টাল ফ্রন্ট-লোডিং বেলি ওয়াশার", "Belly Washer|||Open Pocket Washer|||Rotary Drum Washer", "FULL_CONTENT_BLUEPRINT", "PRIMARY_PRODUCTION", "150 kg – 350 kg dry weight (owner to verify drum nameplate)", "30°C – 95°C", "Inverter-Duty VFD Helical Gear / V-Belt Drive (7.5–15 kW)", "Lithium EP2 Grease (Plummer blocks weekly)", "Door pneumatic/electrical LOTO interlock (ISO 10472-2)", 0),
    ("EQ-A02", "CAT-A01", "FAM-A", "Side-Loading Divided-Pocket Belly Washer", "সাইড-লোডিং ডিভাইডেড-পকেট বেলি ওয়াশার", "Divided Pocket Washer|||3-Pocket Washer|||2-Pocket Washer", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner plant nameplate verification", "30°C – 95°C", "Three-Phase Induction Motor + V-Belt / Gearbox", "Lithium EP2 Grease on trunnion housings", "Compartment latch & door interlock (ISO 10472-2)", 0),
    ("EQ-A03", "CAT-A01", "FAM-A", "Sample / Recipe Washing Machine", "স্যাম্পল / রেসিপি ওয়াশিং মেশিন", "Sample Washer|||Lab Washing Machine", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner plant nameplate verification", "20°C – 98°C", "Direct V-Belt / Single-Phase or 3-Phase Drive", "ISO VG 46 Light Machine Oil / Sealed Bearings", "Emergency stop & lid interlock (ISO 6330)", 0),
    ("EQ-A04", "CAT-A01", "FAM-A", "Pneumatic/Hydraulic Tilting Industrial Washer", "টিল্টিং ইন্ডাস্ট্রিয়াল ওয়াশিং মেশিন", "Tilting Washer|||Front Tilting Washer", "PARTIAL_CONTENT_BLUEPRINT", "PRIMARY_PRODUCTION", "200 kg – 400 kg dry batch (owner to verify)", "30°C – 95°C", "VFD Drive Motor + Hydraulic Tilting Pack (70 bar)", "EP2 Bearing Grease & ISO VG 46 Hydraulic Oil", "Two-hand tilt safety guard & braided steam hose check (OSHA 1910.212)", 1),
    ("EQ-A05", "CAT-A01", "FAM-A", "Barrier Sanitary Washing Machine", "ব্যারিয়ার স্যানিটারি ওয়াশিং মেশিন", "Sanitary Washer|||Pass-Through Cleanroom Washer", "RESEARCH_PENDING", "SECONDARY_AUXILIARY", "Awaiting owner confirmation & nameplate", "30°C – 90°C", "VFD Drum Drive with Pneumatic Suspension Airbags", "Sealed for life / High-temp synthetic grease", "Clean/dirty side door interlock (EN 14065)", 1),

    # FAMILY B (5)
    ("EQ-B01", "CAT-B01", "FAM-B", "Vertical Basket Centrifugal Hydro Extractor (Suspended 3-Point)", "ভার্টিক্যাল বাস্কেট সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর", "Hydro Extractor|||Centrifuge|||Water Spinner", "FULL_CONTENT_BLUEPRINT", "SAFETY_CRITICAL", "50 kg – 150 kg wet batch (owner to verify basket dia)", "Ambient (25°C – 50°C)", "High-Torque Induction Motor + Centrifugal Clutch (5.5–11 kW)", "High-Speed Synthetic Bearing Grease (NLGI 2/3 monthly)", "CRITICAL: Zero-speed lid interlock & brake test (ISO 10472-3)", 0),
    ("EQ-B02", "CAT-B01", "FAM-B", "Tilting Centrifugal Hydro Extractor", "টিল্টিং সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর", "Tilting Hydro|||Automated Discharge Hydro", "PARTIAL_CONTENT_BLUEPRINT", "SAFETY_CRITICAL", "100 kg – 250 kg wet batch (owner to verify)", "Ambient (25°C – 50°C)", "VFD Motor + Hydraulic Pivot Tilting System", "NLGI 2 Synthetic Bearing Grease + ISO VG 46 Hydraulic", "Lid speed interlock + hydraulic pivot pin NDT check (ISO 10472-3)", 1),
    ("EQ-B03", "CAT-B02", "FAM-B", "Industrial Steam-Heated Tumbler Dryer", "ইন্ডাস্ট্রিয়াল স্টিম-হিটেড টাম্বল ড্রায়ার", "Steam Dryer|||Garment Tumbler Dryer", "PARTIAL_CONTENT_BLUEPRINT", "PRIMARY_PRODUCTION", "100 kg – 250 kg batch (owner to verify)", "65°C – 95°C drum air", "Reversing Geared Drum Drive + Exhaust Blower Motor", "High-temp lithium grease on trunnion rollers", "Lint fire prevention cleaning & coil trap check (ISO 10472-4 / NFPA 86)", 0),
    ("EQ-B04", "CAT-B02", "FAM-B", "Industrial Gas-Fired Tumbler Dryer", "ইন্ডাস্ট্রিয়াল গ্যাস-ফায়ার্ড টাম্বল ড্রায়ার", "Gas Dryer|||Direct Gas Tumbler", "RESEARCH_PENDING", "SAFETY_CRITICAL", "Awaiting owner confirmation & gas burner rating", "70°C – 110°C", "Drum Gearmotor + Gas Premix Burner Blower", "High-temp EP2 grease on drum support bearings", "Gas flame safeguard & purge interlock (NFPA 54 / NFPA 86)", 1),
    ("EQ-B05", "CAT-B02", "FAM-B", "Radio Frequency (RF) / Conveyor Tunnel Dryer", "আরএফ / কনভেয়র টানেল ড্রায়ার", "RF Dryer|||Conveyor Tunnel Dryer", "RESEARCH_PENDING", "SECONDARY_AUXILIARY", "Awaiting owner confirmation if installed", "60°C – 110°C", "Conveyor Geared Drive + RF Generator / Multi-Zone Steam", "Food-grade/high-temp conveyor chain oil", "RF shielding & conveyor pinch guards (ISO 10472-4)", 1),

    # FAMILY C (5)
    ("EQ-C01", "CAT-C01", "FAM-C", "Curing / Baking Box Oven", "কিউরিং / বেকিং বক্স ওভেন", "Curing Oven|||Denim Baking Oven|||Resin Oven", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner plant nameplate verification", "140°C – 160°C", "Hot Air Recirculation Centrifugal Fan Motor", "High-temperature bearing grease (>180°C rated)", "Over-temperature cutoff & exhaust interlock (NFPA 86)", 0),
    ("EQ-C02", "CAT-C01", "FAM-C", "3D Whisker / Scraping Machine", "থ্রি-ডি হুইস্কার / স্ক্র্যাপিং মেশিন", "Whisker Machine|||Inflatable Mannequin|||Denim Dummy", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "0.5 – 1.5 bar regulated air", "Ambient", "Pneumatic Cylinders & Air Pressure Regulators", "FRL Pneumatic Mist Lubricator (ISO VG 32)", "Air pressure relief regulator guard (OSHA 1910.212)", 0),
    ("EQ-C03", "CAT-C01", "FAM-C", "Chemical Spraying Booth (Water Curtain)", "কেমিক্যাল স্প্রে বুথ (ওয়াটার কার্টেন)", "PP Spray Booth|||Potassium Permanganate Booth", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner fan CFM & pump verification", "Ambient", "Water Curtain Pump + Exhaust Fan + Spray Guns", "Standard motor bearing grease", "Chemical mist containment & PPE/LEV airflow verification (OSHA 1910.107)", 0),
    ("EQ-C04", "CAT-C01", "FAM-C", "Industrial Ozone Fading & Bleaching Machine", "ইন্ডাস্ট্রিয়াল ওজোন ফেডিং ও ব্লিচিং মেশিন", "Ozone Machine|||O3 Garment Tumbler", "RESEARCH_PENDING", "SAFETY_CRITICAL", "Awaiting owner confirmation & O3 capacity", "Ambient – 40°C", "Rotary Drum + O3 Corona Generator + Catalyst Destructor", "Ozone-resistant fluorinated grease (PFPE)", "CRITICAL: Ambient O3 sensor interlock < 0.1 ppm (OSHA 1910.1000)", 1),
    ("EQ-C05", "CAT-C01", "FAM-C", "Laser Denim Marking & Fading Machine", "লেজার ডেনিম মার্কিং ও ফেডিং মেশিন", "Laser Machine|||Denim Laser Engraver", "RESEARCH_PENDING", "SAFETY_CRITICAL", "Awaiting owner confirmation & laser wattage", "Water-cooled 20°C", "Galvo Scanning Mirrors + Chiller + HEPA Fume Extractor", "Cleanroom optical rail wipe; zero oil mist", "Class 4 enclosed interlock & laser safety eyewear (ANSI Z136.1)", 1),

    # FAMILY D (6)
    ("EQ-D01", "CAT-D01", "FAM-D", "Single-Stage End-Suction Centrifugal Water Pump", "সিঙ্গেল-স্টেজ সেন্ট্রিফিউগাল ওয়াটার পাম্প", "Transfer Pump|||Raw Water Pump|||End Suction Pump", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner plant nameplate verification", "20°C – 60°C", "Direct Coupled 3-Phase Induction Motor (415V, 50Hz)", "Lithium EP2 Grease / Sealed Bearings", "Coupling guard & dry-run protection (ISO 2858 / ISO 5199)", 0),
    ("EQ-D02", "CAT-D01", "FAM-D", "High-Pressure Multistage Centrifugal Boiler Feed Pump", "হাই-প্রেশার মাল্টিস্টেজ বয়লার ফিড পাম্প", "Boiler Feed Pump|||BFP|||Multistage Pump", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "5 – 25 m³/h at 12–25 bar (owner to verify)", "85°C – 105°C feedwater", "2-Pole TEFC Induction Motor (2900 RPM, 11–37 kW)", "ISO VG 68 Oil / High-Temp Synthetic Bearing Grease", "CRITICAL UTILITY: Dual Duty+Standby per Boilers Act 2022 / API 610", 0),
    ("EQ-D03", "CAT-D02", "FAM-D", "Air-Operated Double Diaphragm (AODD) Pump", "এয়ার-অপারেটেড ডাবল ডায়াফ্রাম (AODD) পাম্প", "AODD Pump|||Diaphragm Chemical Pump|||Sludge Air Pump", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Compressed air 3 – 7 bar", "Ambient – 70°C", "Oil-Free Compressed Air Spool Valve", "Non-lubricated air spool or FRL clean dry air", "Chemical hose whip-check & bonding ground (ISO 14847)", 0),
    ("EQ-D04", "CAT-D02", "FAM-D", "Positive Displacement Chemical Metering Pump", "কেমিক্যাল মিটারিং ডোজিং পাম্প", "Dosing Pump|||Metering Pump", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner L/h stroke verification", "Ambient – 50°C", "Fractional kW Motor / Solenoid Electromagnetic Drive", "Gearbox synthetic PAO oil per OEM", "Pressure relief & calibration column (API 675)", 0),
    ("EQ-D05", "CAT-D02", "FAM-D", "Submersible Effluent Sump Pump", "সাবমার্সিবল ইফ্লুয়েন্ট সাম্প পাম্প", "Sump Pump|||Drain Trench Pump", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner sump pit flow/head verification", "30°C – 75°C wash effluent", "Submerged Hermetic Motor with SiC Double Seal", "Internal dielectric oil seal chamber", "ELCB/RCCB earth-leakage trip & lifting chain (ISO 9906)", 0),
    ("EQ-D06", "CAT-D02", "FAM-D", "Positive Displacement Fuel Oil / Diesel Transfer Pump", "ডিজেল / ফুয়েল অয়েল ট্রান্সফার পাম্প", "Diesel Pump|||Fuel Gear Pump", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner LPM verification", "Ambient", "4-Pole Induction Motor + Internal Relief Gear Pump", "Pumped diesel lubricates gears; motor EP2 grease", "Fire shutoff fusible link & earthing (API 676)", 0),

    # FAMILY E (6)
    ("EQ-E01", "CAT-E01", "FAM-E", "Three-Phase Squirrel Cage Induction Motor", "থ্রি-ফেজ ইনডাকশন মোটর", "Electric Motor|||3-Phase Motor|||TEFC Motor", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "0.75 kW – 75 kW across plant (owner to verify)", "Max winding 130°C–155°C", "Electrical 415V 50Hz 3-Phase Stator/Rotor", "Polyurea / Lithium Complex EP2 Grease (per motor size)", "IEC 60034-1 earthing; ISO 20816-3:2026 (>15 kW) / OEM baseline (<=15 kW)", 0),
    ("EQ-E02", "CAT-E01", "FAM-E", "Helical / Worm Speed Reduction Gearbox", "হেলিকাল / ওয়ার্ম স্পিড রিডাকশন গিয়ারবক্স", "Gearbox|||Reducer|||Washer Gear Reducer", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner ratio & torque verification", "50°C – 85°C oil sump", "Direct Coupled or Belt-Driven Input Shaft", "ISO VG 220 / VG 320 EP Gear Oil (4000-hr drain)", "Breather plug cleanliness & AGMA 6013 thermal rating", 0),
    ("EQ-E03", "CAT-E01", "FAM-E", "Heavy-Duty Spherical Roller Bearings & Plummer Blocks", "স্পফেরিক্যাল রোলার বিয়ারিং ও প্লামার ব্লক", "Plummer Block|||222xx Bearing|||Trunnion Bearing", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "22216 – 22232 series (verify shaft dia)", "Max 80°C outer ring", "Anti-Friction Rolling Element Support", "NLGI Grade 2 Lithium Complex EP Grease", "Labyrinth/Taconite moisture seal inspection (ISO 281)", 0),
    ("EQ-E04", "CAT-E02", "FAM-E", "Industrial V-Belt & Pulley Transmission", "ইন্ডাস্ট্রিয়াল ভি-বেল্ট ও পুলি ড্রাইভ", "V-Belt Drive|||SPB Belt|||B-Section Pulley", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "A/B/C & SPA/SPB/SPC profiles", "Max 60°C belt temp", "Friction Wedge Transmission", "NONE — Keep strictly oil/grease free", "Full enclosed mesh guard & belt tension check (ISO 4184)", 0),
    ("EQ-E05", "CAT-E02", "FAM-E", "Flexible Jaw & Pin-Bush Shaft Couplings", "ফ্লেক্সিবল জ ও পিন-বুশ শ্যাফট কাপলিং", "Jaw Coupling|||Pin Bush Coupling|||Rotex Coupling", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner shaft bore sizes", "Max 80°C elastomer", "Elastomer Compression Torsion Coupling", "NONE — Elastomer dry run", "Locked coupling sheet-metal guard (ISO 10441 / AGMA 9000)", 0),
    ("EQ-E06", "CAT-E02", "FAM-E", "Synchronous Timing Belt & Toothed Sprocket Drive", "সিনক্রোনাস টাইমিং বেল্ট ও স্প্রোকেট ড্রাইভ", "Timing Belt|||HTD Belt Drive", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "HTD 8M / 14M profiles (plant-dependent)", "Max 70°C", "Positive Tooth Engagement Belt", "NONE — Keep clean and dry", "Guard enclosure & laser pulley alignment (ISO 5296)", 1),

    # FAMILY F (6)
    ("EQ-F01", "CAT-F01", "FAM-F", "Pneumatic Angle-Seat Piston Valve", "নিউম্যাটিক অ্যাঙ্গেল-সিট পিস্টন ভালভ", "Angle Seat Valve|||Steam Y-Valve|||Pneumatic Steam Valve", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "DN25 / DN40 / DN50 (owner to verify)", "Max 180°C steam", "Compressed Air Spring-Return Actuator (4–6 bar)", "PTFE self-lubricating stem packing", "Steam passing check & actuator exhaust silencer (ASME B16.34)", 0),
    ("EQ-F02", "CAT-F01", "FAM-F", "Industrial Steam Traps (Thermodynamic & Ball Float)", "ইন্ডাস্ট্রিয়াল স্টিম ট্র্যাপ (থার্মোডাইনামিক ও বল ফ্লোট)", "Steam Trap|||TD Trap|||Ball Float Trap", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "DN15 – DN50, ΔP up to 12 bar", "120°C – 190°C", "Thermodynamic Disc or Mechanical Float Lever", "Internal water condensate seal only", "Ultrasonic blow-through & water hammer prevention (ISO 6552 / ISO 6704)", 0),
    ("EQ-F03", "CAT-F01", "FAM-F", "Pilot-Operated Steam Pressure Reducing Valve (PRV)", "স্টিম প্রেশার রিডিউসিং ভালভ (PRV)", "PRV Valve|||Steam Pressure Regulator", "INVENTORY_ONLY", "CRITICAL_UTILITY", "8–10 bar(g) down to 3–4 bar(g)", "140°C – 185°C", "Steam Pilot Diaphragm Self-Actuated", "None (Y-strainer 100-mesh upstream)", "Downstream safety relief valve mandatory (ASME B16.34)", 0),
    ("EQ-F04", "CAT-F02", "FAM-F", "Condensate Flash Vessel & Return Pump System", "কনডেনসেট ফ্ল্যাশ ভেসেল ও রিটার্ন পাম্প", "Condensate Recovery|||Flash Tank", "INVENTORY_ONLY", "CRITICAL_UTILITY", "Awaiting owner condensate flow rate", "95°C – 140°C", "Steam-Powered Motive Pump or Electric Low-NPSH Pump", "Sealed high-temp pump bearings", "Flash vessel safety relief valve & NPSH check (ASME Sec VIII)", 0),
    ("EQ-F05", "CAT-F02", "FAM-F", "Plate Heat Exchanger for Drain Heat Recovery", "প্লেট হিট এক্সচেঞ্জার (ড্রেন হিট রিকভারি)", "PHE|||Heat Recovery Exchanger", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner confirmation if PHE installed", "25°C – 75°C", "Stationary Gasketed SS316L Plates", "Food-grade tie-bar thread anti-seize", "Periodic CIP backflush against wash lint fouling (EN 13445)", 1),
    ("EQ-F06", "CAT-F02", "FAM-F", "Thermal Pipe Cladding & Removable Valve Insulation Jackets", "স্টিম পাইপ ক্ল্যাডিং ও ভালভ ইনসুলেশন", "Rockwool Cladding|||Steam Insulation Jacket", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "50 mm – 75 mm Rockwool (owner to verify)", "Surface < 50°C", "Passive Thermal Barrier", "None", "Personnel burn protection & CUI weather-proofing (BS 5970)", 0),

    # FAMILY G (4) — DEDICATED BOILER SYSTEMS
    ("EQ-G01", "CAT-G01", "FAM-G", "3-Pass Wet-Back Gas-Fired Industrial Steam Boiler", "থ্রি-পাস ওয়েট-ব্যাক গ্যাস-ফায়ার্ড স্টিম বয়লার", "Gas Boiler|||Firetube Boiler|||Steam Boiler", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "2.0 – 10.0 TPH at 10.54 bar(g) (owner to verify)", "184°C saturated steam", "Modulating Natural Gas Burner + FD Fan (7.5–22 kW)", "ISO VG 220 actuator oil; high-temp fan grease", "STATUTORY: The Boilers Act, 2022 (Act 06 of 2022) & Rules 2025 (S.R.O. 279-Law/2025)", 0),
    ("EQ-G02", "CAT-G02", "FAM-G", "Jhut / Textile Waste-Fuel Steam Boiler", "ঝুট / বায়োমাস সলিড-ফুয়েল স্টিম বয়লার", "Jhut Boiler|||Biomass Boiler|||Solid Fuel Boiler", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "1.5 – 6.0 TPH at 8–10 bar(g) (owner to verify)", "175°C – 184°C steam", "Balanced Draft: FD Fan + Water-Cooled ID Fan", "High-temp lithium complex EP2 on FD/ID fan blocks", "STATUTORY: Boilers Act 2022, Multicyclone & spark arrestor inspection", 0),
    ("EQ-G03", "CAT-G03", "FAM-G", "Dual Spring-Loaded Full-Lift Safety Relief Valves", "ডুয়েল স্প্রিং-লোডেড সেফটি রিলিফ ভালভ", "Boiler Safety Valve|||Pop Valve", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Set at MAWP <= 10.54 bar(g) (verify cert)", "185°C steam", "Direct Calibrated Helical Compression Spring", "NONE — Dry SS trim seats", "STATUTORY: Weekly hand-easing & annual OCIB live popping (ASME Sec I PG-67)", 0),
    ("EQ-G04", "CAT-G03", "FAM-G", "Mobrey Magnetic Water Level Controller", "মোব্রে ম্যাগনেটিক ওয়াটার লেভেল কন্ট্রোলার", "Mobrey Switch|||Low Water Cutoff|||LWCO", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Dual Pump Control + 1st/2nd Low Water Trip", "185°C boiler water", "Magnetic Float Follower Actuating Micro-Switches", "None (external magnetic linkage)", "CRITICAL: Daily blowdown chamber test & evaporation LWCO test (BS 759)", 0),

    # FAMILY H (4)
    ("EQ-H01", "CAT-H01", "FAM-H", "Industrial Sodium Cation Exchange Water Softener", "সোডিয়াম ক্যাটায়ন ওয়াটার সফটনার প্ল্যান্ট", "Water Softener|||Resin Vessel", "INVENTORY_ONLY", "CRITICAL_UTILITY", "Awaiting owner m³/h & resin liters verification", "Ambient – 40°C", "Multiport / Pneumatic Valves + Brine Ejector", "Valve O-ring silicone grease", "Boiler feed hardness < 5 ppm as CaCO3 (AWWA B100)", 0),
    ("EQ-H02", "CAT-H01", "FAM-H", "Multi-Grade Sand & Activated Carbon Filter Plant", "মাল্টি-গ্রেড স্যান্ড ও কার্বন ফিল্টার", "Sand Filter|||Carbon Filter|||MGF ACF", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner vessel diameter verification", "Ambient", "Backwash Pump + Butterfly Valve Array", "Valve stem grease", "ΔP < 0.8 bar backwash trigger; chlorine < 0.1 ppm before softener", 0),
    ("EQ-H03", "CAT-H01", "FAM-H", "Industrial Reverse Osmosis (RO) Plant", "ইন্ডাস্ট্রিয়াল রিভার্স অসমোসিস (RO) প্ল্যান্ট", "RO Plant|||Membrane Water Plant", "RESEARCH_PENDING", "SECONDARY_AUXILIARY", "Awaiting owner confirmation if RO installed", "20°C – 35°C", "High-Pressure Multistage Stainless Booster Pump", "High-speed motor bearings", "CIP membrane cleaning & SDI < 3 monitoring (ASTM D4194)", 1),
    ("EQ-H04", "CAT-H01", "FAM-H", "Hydro-Pneumatic Pressure Booster System", "হাইড্রো-নিউম্যাটিক প্রেশার বুস্টার সিস্টেম", "Booster Pump Set|||Water Header Booster", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "3.5 – 4.5 bar header (owner to verify)", "Ambient", "VFD Lead-Lag Multistage Pumps + Nitrogen Bladder Tank", "Sealed pump bearings", "Bladder pre-charge verification every 3 months (DIN 1988-5)", 0),

    # FAMILY I (3)
    ("EQ-I01", "CAT-I01", "FAM-I", "Stainless Steel Chemical Preparation & Mixing Tank", "স্টেইনলেস স্টিল কেমিক্যাল মিক্সিং ট্যাংক", "Chem Mixing Tank|||SS316L Agitator Tank", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner tank liter capacity", "Ambient – 60°C", "Top-Mounted Geared Propeller Agitator", "Food-grade gearbox oil (prevent wash bath spot)", "Splash lid interlock & SS316L weld passivation (ASME B73.1)", 0),
    ("EQ-I02", "CAT-I01", "FAM-I", "Chemical Spill Containment & Emergency Safety Shower", "কেমিক্যাল স্পিল কনটেইনমেন্ট ও সেফটি শাওয়ার", "Safety Shower|||Eye Wash Station", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Potable water 2–3 bar continuous", "16°C – 38°C tepid", "Pull-Rod / Foot-Treadle Stay-Open Ball Valve", "None", "MANDATORY: Weekly flush test & unobstructed 10s access (ANSI/ISEA Z358.1)", 0),
    ("EQ-I03", "CAT-I01", "FAM-I", "Bulk Caustic Soda Flake Dissolving & Transfer Unit", "কস্টিক সোডা ফ্লেক ডিসলভিং ইউনিট", "Caustic Tank|||NaOH Dissolver", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Awaiting owner batch size", "Exothermic > 80°C", "Cooling Jacketed SS Tank + Chemical Transfer Pump", "Alkali-resistant Viton/PTFE pump seals", "CRITICAL: PPE face shield, cooling water active before NaOH addition (OSHA 1910.1200)", 0),

    # FAMILY J (4)
    ("EQ-J01", "CAT-J01", "FAM-J", "Rotary Screw Air Compressor (Oil-Injected)", "রোটারি স্ক্রু এয়ার কম্প্রেসার", "Screw Compressor|||Air Compressor", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "Awaiting owner kW & CFM nameplate verification", "Oil sump 75°C – 95°C", "VFD or Fixed-Speed Motor + Helical Airend (7–8 bar)", "Dedicated Synthetic Screw Compressor Oil (ISO VG 46, 4000h)", "High-temp trip < 105°C; separator ΔP check (ISO 1217; excl. ISO 20816-3)", 0),
    ("EQ-J02", "CAT-J01", "FAM-J", "Refrigerated Compressed Air Dryer", "রেফ্রিজারেটেড কম্প্রেসড এয়ার ড্রায়ার", "Air Dryer|||Refrigerated Air Dryer", "INVENTORY_ONLY", "CRITICAL_UTILITY", "+3°C pressure dew point", "Max inlet air 45°C", "Hermetic Refrigerant Compressor + Condenser Fan", "Refrigerant POE oil sealed", "Timed/float condensate auto-drain test daily (ISO 8573-1 / ISO 7183)", 0),
    ("EQ-J03", "CAT-J01", "FAM-J", "Vertical Compressed Air Receiver Tank", "কম্প্রেসড এয়ার রিসিভার ট্যাংক", "Air Receiver|||Air Storage Tank", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Awaiting owner liter volume & MAWP", "Ambient – 50°C", "Static Pressure Vessel with Auto-Drain & PRV", "None", "CRITICAL: Daily condensate drain & periodic NDT thickness inspection (ASME Sec VIII)", 0),
    ("EQ-J04", "CAT-J01", "FAM-J", "High-Efficiency Coalescing & Activated Carbon Line Filters", "কোয়ালেসিং ও অ্যাক্টিভেটেড কার্বন এয়ার ফিল্টার", "Line Filter|||Oil Removal Air Filter", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "0.01 micron / < 0.01 mg/m³ oil", "Ambient", "Differential Pressure Gauge + Replaceable Element", "None", "Replace element when ΔP exceeds 0.4–0.5 bar (ISO 8573-2 / ISO 12500)", 0),

    # FAMILY K (2)
    ("EQ-K01", "CAT-K01", "FAM-K", "Induced Draft Counter-Flow Cooling Tower", "ইনডিউসড ড্রাফট কাউন্টার-ফ্লো কুলিং টাওয়ার", "Cooling Tower|||FRP Cooling Tower", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner TR & plant confirmation", "37°C in / 32°C out", "Axial Overhead Fan Gearbox / Direct Fan Motor", "Waterproof marine EP2 grease & gear oil", "Biocide Legionella dosing & basin blowdown TDS control (CTI STD-201)", 1),
    ("EQ-K02", "CAT-K01", "FAM-K", "Industrial Water Chiller (Air-Cooled / Water-Cooled)", "ইন্ডাস্ট্রিয়াল ওয়াটার চিলার", "Process Chiller|||Enzyme Chiller", "RESEARCH_PENDING", "SECONDARY_AUXILIARY", "Awaiting owner confirmation & TR rating", "10°C – 15°C chilled water", "Screw/Scroll Compressors + Plate/Shell Evaporator", "Synthetic POE Refrigeration Oil", "Low-flow freeze protection & refrigerant leak check (ASHRAE 15 / AHRI 550)", 1),

    # FAMILY L (5)
    ("EQ-L01", "CAT-L01", "FAM-L", "Dissolved Air Flotation (DAF) Unit", "ডিজলভড এয়ার ফ্লোটেশন (DAF) ইউনিট", "DAF Unit|||ETP Flotation Tank", "PARTIAL_CONTENT_BLUEPRINT", "CRITICAL_UTILITY", "Awaiting owner m³/h ETP capacity", "30°C – 45°C effluent", "Recycle Saturation Pump + Chain Surface Skimmer Drive", "Waterproof grease on skimmer sprocket bearings", "Recycle microbubble pressure 4.5–5.5 bar & sludge blanket control (WEF MOP 8)", 0),
    ("EQ-L02", "CAT-L01", "FAM-L", "Tri-Lobe Roots Aeration Blower", "ট্রাই-লোব রুটস অ্যারেশন ব্লোয়ার", "Roots Blower|||ETP Air Blower", "INVENTORY_ONLY", "CRITICAL_UTILITY", "Awaiting owner m³/min & mbar", "Discharge 60°C – 95°C", "V-Belt Coupled TEFC Motor + Helical Timing Gears", "ISO VG 220 Synthetic Gear Oil + NLGI 2/3 High-Temp Grease", "Clean suction filter ΔP & gear oil sump level daily (ISO 1217)", 0),
    ("EQ-L03", "CAT-L01", "FAM-L", "Recessed Chamber Plate-and-Frame Filter Press", "প্লেট-অ্যান্ড-ফ্রেম স্লাজ ফিল্টার প্রেস", "Filter Press|||ETP Sludge Dewatering", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Cake dryness 30–40% solids", "Ambient", "Hydraulic Cylinder Power Pack (160–220 bar)", "ISO VG 46 / VG 68 Hydraulic Oil", "Hydraulic interlock & plate cloth integrity wash (DIN 19569)", 0),
    ("EQ-L04", "CAT-L01", "FAM-L", "Progressive Cavity Sludge Pump (Screw Pump)", "প্রোগ্রেসিভ ক্যাভিটি স্লাজ স্ক্রু পাম্প", "Screw Pump|||Mono Pump|||Sludge Feed Pump", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Up to 6–10 bar feed pressure", "Ambient – 45°C", "Helical Gearmotor + Universal Pin Joint Rotor/Stator", "Joint boot grease per OEM", "CRITICAL: Dry-run sensor mandatory — elastomer stator burns < 30s (ISO 14847)", 0),
    ("EQ-L05", "CAT-L01", "FAM-L", "Rotary Drum Fine Screen / Mechanical Bar Screen", "রোটারি ড্রাম ফাইন স্ক্রিন / বার স্ক্রিন", "Drum Screen|||Lint Separator", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "0.5 – 1.5 mm wedge wire slot", "30°C – 65°C effluent", "Geared Chain/Friction Drive + High-Pressure Spray Header", "Water-resistant EP2 grease on pillow blocks", "Anti-jam torque overload & pumice stone trap clearing (WEF MOP 8)", 0),

    # FAMILY M (3)
    ("EQ-M01", "CAT-M01", "FAM-M", "Electric Chain Hoist & Monorail Trolley", "ইলেকট্রিক চেইন হোয়েস্ট ও মনোরেল ট্রলি", "Chain Hoist|||Hydro Basket Hoist|||Overhead Crane", "INVENTORY_ONLY", "SAFETY_CRITICAL", "1000 – 2000 kg SWL (verify beam tag)", "Ambient", "Dual-Brake Hoist Motor + Motorized Trolley", "Gearbox grease + dry/light oil on calibrated load chain", "CRITICAL SAFETY: Upper limit switch, hook safety latch, annual SWL load test (ASME B30.16)", 0),
    ("EQ-M02", "CAT-M01", "FAM-M", "Polyethylene Wet Garment Trolleys", "পলিথিলিন ওয়েট গার্মেন্টস ট্রলি", "Garment Trolley|||Wet Lot Cart", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "300 – 600 kg wet batch", "Ambient – 50°C", "Manual Swivel & Rigid Polyurethane Casters", "Grease caster swivel races; clear lint from axles monthly", "Castor wheel brake & crack check (EN 840)", 0),
    ("EQ-M03", "CAT-M01", "FAM-M", "Cleated Belt Conveyor for Wet Garment Cake & Stone", "ক্লিটেড বেল্ট কনভেয়র", "Wet Conveyor|||Garment Belt Conveyor", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner confirmation if installed", "Ambient – 50°C", "Shaft-Mounted Gearmotor + PVC Cleated Belt", "Sealed conveyor roller bearings", "Emergency pull-cord stop & nip-point guards (CEMA 402)", 1),

    # FAMILY N (3)
    ("EQ-N01", "CAT-N01", "FAM-N", "Heavy-Duty Engine Lathe (Gap Bed)", "হেভি-ডিউটি ইঞ্জিন লেদ মেশিন", "Engine Lathe|||Workshop Lathe", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Awaiting owner swing/center length", "Oil bath headstock", "Headstock Geared Spindle + Leadscrew Feedbox", "ISO VG 68 Headstock & Slideway Oil (ISO VG 68 / VG 220)", "Chuck shield guard, never leave chuck key in chuck (ISO 23125)", 0),
    ("EQ-N02", "CAT-N01", "FAM-N", "Vertical Pillar Drill Machine", "ভার্টিক্যাল পিলার ড্রিল মেশিন", "Drill Press|||Pillar Drill", "INVENTORY_ONLY", "SECONDARY_AUXILIARY", "Max 25–32 mm drill chuck (verify)", "Ambient", "Stepped V-Belt Cone Pulley Drive", "Spindle quill light oil; rack grease", "Workpiece MUST be bolted/clamped in machine vice (ISO 16093)", 0),
    ("EQ-N03", "CAT-N01", "FAM-N", "Inverter TIG / MMA Welding Machine", "ইনভার্টার টিগ (TIG) / আর্ক ওয়েল্ডিং মেশিন", "TIG Welder|||SS316L Welder|||Welding Set", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "200A – 400A DC Inverter", "Cooling fan", "IGBT High-Frequency Inverter + Argon Purge", "None (blow out dust with dry air monthly)", "CRITICAL: Insulated mat on wet wash floor + gas flashback/purging PPE (AWS D1.1 / ASME IX)", 0),

    # FAMILY O (2)
    ("EQ-O01", "CAT-O01", "FAM-O", "Diesel Engine Driven Emergency Fire Pump", "ডিজেল ইঞ্জিন চালিত ইমার্জেন্সি ফায়ার পাম্প", "Fire Pump|||Emergency Hydrant Pump", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Awaiting owner GPM/head & weekly test log", "Engine 80°C – 95°C", "Compression-Ignition Diesel Engine + Horizontal Split Pump", "15W-40 Diesel Engine Oil + Dual 24V Trickle Batteries", "STATUTORY SAFETY: Mandatory weekly 30-min no-flow/flow auto-crank test (NFPA 20)", 0),
    ("EQ-O02", "CAT-O01", "FAM-O", "Combustible Gas Leak Detection & Shutoff System", "কমবাস্টিবল গ্যাস লিক ডিটেকশন ও শাটঅফ সিস্টেম", "Gas Detector|||LEL Sensor|||Slam-Shut Valve", "INVENTORY_ONLY", "SAFETY_CRITICAL", "Alarm 10% LEL, Trip Slam-Shut 20% LEL", "Ambient boiler room", "24V DC Catalytic/IR Detector + Solenoid Slam-Shut Valve", "None (keep sensor sintered head silicone-free)", "CRITICAL SAFETY: Bump-test & 6-monthly LEL calibration certificate (EN 60079-29-1)", 0),

    # FAMILY P (1)
    ("EQ-P01", "CAT-P01", "FAM-P", "Heavy-Duty Centrifugal Lint & Moisture Exhaust Blower", "সেন্ট্রিফিউগাল লিন্ট ও ময়েশ্চার এগজস্ট ব্লোয়ার", "Exhaust Blower|||Lint Blower|||Dryer Exhaust Fan", "INVENTORY_ONLY", "PRIMARY_PRODUCTION", "Awaiting owner CFM & static pressure", "40°C – 90°C exhaust", "V-Belt / Direct Centrifugal Backward-Curved Impeller", "High-temp lithium complex EP2 on fan pedestal bearings", "Weekly lint scraping off impeller blades to prevent dynamic unbalance (AMCA 210 / ISO 20816-3:2026)", 0),
]

ALL_SPECS = {**PART1_SPECS, **PART2_SPECS}
assert len(ALL_SPECS) == 64, f"Expected 64 equipment specs, got {len(ALL_SPECS)}"

# Synchronize content_depth_status in EQUIPMENT list from GAP-13 reconciled specs
EQUIPMENT = [
    row[:6] + (ALL_SPECS[row[0]]["depth"],) + row[7:]
    for row in EQUIPMENT
]

SUBTYPES = []
COMPONENTS = []
TECHNICAL_PARAMETERS = []
FAILURE_MODES = []
MAINTENANCE_PROCEDURES = []

SECTION_TITLES_21 = [
    (1, "Introduction & Operational Purpose"),
    (2, "Machine Classification & Plant Applicability"),
    (3, "Working Principle & Engineering Mechanics"),
    (4, "Major Assemblies & Structural Architecture"),
    (5, "Rotating Elements, Drives & Power Transmission"),
    (6, "Bearings, Seals & Wear Components"),
    (7, "Steam, Water, Air & Electrical Utility Interfaces"),
    (8, "Control System, Interlocks & Instrumentation"),
    (9, "Critical Technical Parameters & Nameplate Checklist"),
    (10, "Pre-Start Safety & Shift Handover Inspection"),
    (11, "Standard Operating Envelope & Process Loads"),
    (12, "Preventive Maintenance Schedule (Daily to Annual)"),
    (13, "Lubrication Chart, Grease Grades & Re-Lubrication Intervals"),
    (14, "Vibration Monitoring & Condition Diagnostics (ISO 20816-3:2026)"),
    (15, "Common Failure Modes, Root Causes & Troubleshooting"),
    (16, "Disassembly, Component Replacement & Reassembly Guide"),
    (17, "Critical Spare Parts Holding & Wear Tier Classification"),
    (18, "Lockout/Tagout (LOTO) & Hazardous Energy Safety Rules"),
    (19, "Energy, Steam & Water Conservation Best Practices"),
    (20, "Applicable International & Bangladesh Statutory Standards"),
    (21, "Owner Plant-Specific Verification Checklist"),
]
SECTION_TITLE_MAP = dict(SECTION_TITLES_21)


def get_profile_section_title(num: int, profile: str) -> str:
    if num == 13 and profile in ("BELT_COUPLING", "STATIC_PASSIVE", "STATIC_SAFETY", "STATIC_THERMAL"):
        return "Dry-Service Care, Cleanliness & Corrosion Protection"
    if num == 14:
        if profile in ("STATIC_PASSIVE", "STATIC_SAFETY", "STATIC_THERMAL"):
            return "Structural Integrity, Inspection & Testing Standard"
        if profile in ("PD_COMPRESSOR", "PD_FLUID"):
            return "Thermal, Differential Pressure & Positive-Displacement Monitoring"
        if profile == "BELT_COUPLING":
            return "Belt Tension, Sheave & Shaft Alignment Standards"
        if profile == "LIFTING_MECH":
            return "Brake Holding, Load-Chain Wear & Structural NDT Standard"
        if profile in ("PNEUMATIC_PROCESS", "VALVE_THERMAL", "FLUID_CHEMICAL", "FLUID_HYDRAULIC"):
            return "Process Control, Leak Integrity & Condition Diagnostics"
        if profile == "THERMAL_COOLING":
            return "Thermal Performance, Superheat & Auxiliary Drive Monitoring"
    return SECTION_TITLE_MAP[num]


def build_full_21_sections(eq_id: str, name: str, domain_focus: str):
    rows = []
    specifics = {
        "EQ-A01": {
            1: "The Horizontal Front-Loading Belly Washer (Open Pocket) is the core garment wet-processing machine in textile washing plants for desizing, enzyme wash, bleach, acid wash, and pumice stone washing.",
            2: "System Family A (Production & Washing Machinery). Present in virtually 100% of denim and woven washing facilities. Verify exact dry weight rating (typically 150–350 kg) on plant nameplate.",
            3: "Garments tumble inside an AISI 316L perforated drum fitted with lifters. At wash speed (26–32 RPM, Froude 0.6–0.8), clothes climb to 11 o'clock and drop through chemical liquor. Periodic 30s forward / 5s pause / 30s reverse prevents fabric roping.",
            4: "Consists of outer stainless tub, perforated AISI 316L inner drum, cantilevered cast/forged spider hub, rear trunnion shaft assembly, pneumatically sealed loading door, and heavy structural steel A-frame chassis.",
            5: "Typically driven by an inverter-duty 4-pole TEFC motor (7.5–15 kW) via multiple SPB V-belts or a helical speed reducer coupling into the rear trunnion shaft.",
            6: "Main spherical roller bearings (SKF 222xx series in plummer blocks), Viton/PTFE shaft lip seals, SiC/Carbon water seal, and EPDM front door gasket exposed to chlorine and enzymes.",
            7: "Utilities: Softened cold/hot water headers (2–4 bar), direct steam sparging line (3–5 bar via angle-seat piston valve), compressed air (6 bar) for valves/locks, and 415V 3-phase 50Hz power.",
            8: "Safety door interlock prevents motor energization while unlatched. Water level pressure transducer/float switch and PT100 RTD temperature probe control bath fill and steam heating.",
            9: "Verify on owner's plant nameplate: Drum Diameter (mm), Drum Depth (mm), Dry Capacity (kg), Main Motor (kW), Inverter Make/Model, and Plummer Block Bearing Number.",
            10: "Before shift: Check door gasket seal integrity, pneumatic valve air pressure (6 bar), steam trap downstream discharge, drain dump valve closure, and quench drain tell-tale behind rear hub.",
            11: "Never exceed dry garment batch rating or load oversized stone charges off-center. Avoid thermal shock (sudden 90°C direct steam opening into cold bath without gradual ramp).",
            12: "Daily: Check quench seal drain for leakage & clean lint trap. Weekly: Grease bearings (EP2), check V-belt deflection, inspect pneumatic steam valve stem. Monthly: Vibration & amperage log. Annually: Door hinge/spider hub NDT.",
            13: "Apply 35–45g NLGI Grade 2 Lithium Complex EP grease (e.g., Shell Gadus S2 V220 / Mobilith SHC 220) per plummer block every 160 operating hours (weekly) via clean zerk nipples. Purge moisture cleanly.",
            14: "Measure triaxial velocity on Drive-End (DE) and Non-Drive-End (NDE) plummer blocks during distribution spin. Note ISO 20816-3:2026 formally covers coupled machines >15 kW (120–30,000 r/min); typical 7.5–15 kW washers use OEM baseline trending (<2.8 mm/s field guide, >4.5 mm/s inspect, >7.1 mm/s shut down).",
            15: "1) Bearing rumble -> moisture ingress through failed water face seal (always replace mechanical seal together with bearing). 2) Steam hammer -> failed check valve or cold trap. 3) Inverter OC trip -> over-loaded stone batch or binding trunnion.",
            16: "LOTO electrical + lock steam & air isolation valves! Remove belt guard, unbolt driven pulley/taper-lock bush, dismantle plummer cap, extract failed 222xx bearing and shaft sleeve, install NEW mechanical seal, torque cap bolts evenly.",
            17: "Critical Spares: Mechanical seal kit (CONSUMABLE), Door EPDM gasket (CONSUMABLE), SPB V-belt matched set (CONSUMABLE), Angle-seat valve seal kit (CONSUMABLE), SKF 222xx Spherical Roller Bearing + Adapter Sleeve (WEAR_PART).",
            18: "NEVER open drain dump valve under boiling >85°C liquor. Mandatory LOTO padlock on main isolator AND closed steam manual valve before entering drum to retrieve snagged stone/tool.",
            19: "Lag outer tub & steam valve bodies, repair passing pneumatic steam valves immediately (a 2mm seat leak wastes >15 kg/h steam), and recover hot wash drain heat via Plate Heat Exchanger.",
            20: "ISO 10472-2 (Safety requirements for industrial laundry/washing machinery), DIN EN 60456, ISO 20816-3:2026 (>15 kW drivers / OEM baseline <=15 kW), Bangladesh Labour Rules 2015 machine guarding.",
            21: "Owner To Confirm In Plant: [ ] Drum dia & length [ ] Motor kW & VFD code [ ] Bearing SKU (DE/NDE) [ ] Steam header inlet pressure.",
        },
        "EQ-B01": {
            1: "The Vertical Basket Centrifugal Hydro Extractor removes free water mechanically from washed garments prior to thermal drying, cutting tumbler steam consumption by >60%.",
            2: "System Family B (Extraction & Drying). Critical safety-class rotating machine present in 100% of industrial washing plants. High stored kinetic energy.",
            3: "A perforated AISI 316L vertical drum spins at 750–950 RPM generating 300–500G centrifugal acceleration, forcing water outward through perforations into the outer shell drain.",
            4: "Outer heavy steel casing suspended from 3 cast-iron columns via forged pendulum links and heavy coil springs/dampers allowing dynamic self-centering under modest load unbalance.",
            5: "High-torque 3-phase TEFC motor (5.5–11 kW) drives the vertical spindle through a mechanical centrifugal shoe clutch (soft start) or VFD inverter ramp + multi-belt V-drive.",
            6: "Upper & lower spindle angular-contact/spherical roller bearings, oil-seal labyrinth shield, centrifugal clutch friction shoes, and pneumatic/mechanical brake lining.",
            7: "415V 3-phase power for spindle motor and brake control; 6 bar clean compressed air for pneumatic lid interlock actuator and pneumatic brake cylinder (if air-braked).",
            8: "CRITICAL DUAL INTERLOCK: 1) Lid-closed limit switch prevents startup; 2) Zero-speed electromagnetic/centrifugal lid lock physically prevents lid opening until drum rotation = 0 RPM.",
            9: "Verify on owner nameplate: Basket Diameter (e.g., 1000/1200 mm), Wet Batch Rating (kg), MAX RATED RPM (NEVER exceed — use CALC-16!), and Brake Type.",
            10: "Pre-Start Safety: Test lid lock manually (cannot lift while latched), listen for brake release click, check suspension link cotter pins, verify vibration trip wand clearance.",
            11: "Distribute heavy denim jeans evenly in a doughnut ring around basket wall before startup. NEVER load dry stones or mismatched single heavy blankets; never run bypassed lid switch.",
            12: "Daily: Verify zero-speed lid lock & brake stop time (<25s). Weekly: Inspect suspension springs, V-belts, and brake pad wear. Monthly: Check basket weld hoops & spindle temperature (<65°C). Annually: Magnetic particle / dye penetrant test on basket shell.",
            13: "Repack upper & lower vertical spindle bearings monthly (or every 250 hrs) with high-speed synthetic NLGI 2/3 lithium complex grease. Wipe excess grease off clutch drum — ZERO oil on clutch/brake!",
            14: "Monitor outer casing peak vibration during critical speed passage (~250 RPM) and full extraction RPM. Sudden bump or persistent >7.1 mm/s RMS indicates unbalanced cake — HIT E-STOP IMMEDIATELY.",
            15: "1) Violent casing shake -> uneven wet garment loading or broken suspension spring. 2) Slow ramp-up / burning smell -> worn centrifugal clutch shoes. 3) Brake takes >35s -> worn brake lining or low air pressure.",
            16: "MANDATORY LOTO! Block basket with wooden wedge before working on bottom spindle. Pull driven pulley, drop spindle assembly downward/upward per OEM, inspect shaft bearing seats for fretting.",
            17: "Critical Spares: Brake pads (CONSUMABLE), Clutch friction shoes (CONSUMABLE), Matched V-belts (CONSUMABLE), Lid micro-switch & zero-speed relay (WEAR_PART), Spindle bearing set (WEAR_PART).",
            18: "DANGER: Bypassing the lid interlock can amputate limbs in <0.5s. If basket develops a rim weld crack, DECOMMISSION IMMEDIATELY — high-RPM basket rupture is fatal. Check CALC-16.",
            19: "Extending hydro extraction cycle by 1–2 minutes lowers residual moisture from 60% down to 48%, saving substantial steam inside downstream tumble dryers (calculate via CALC-18).",
            20: "ISO 10472-3 (Safety requirements for centrifugal extractors), OEM vibration baseline (5.5–11 kW falls below ISO 20816-3 15 kW floor; flexible-mount unbalance trip), Bangladesh Labour Rules 2015 Schedule IV safety guards.",
            21: "Owner To Confirm In Plant: [ ] Basket inner diameter [ ] Maximum rated RPM on plate [ ] Clutch vs VFD type [ ] Brake actuation (Pneumatic vs Manual lever).",
        },
    }
    base = specifics[eq_id]
    for num, title in SECTION_TITLES_21:
        text = base[num]
        tier = "TIER_1_STATUTORY" if num in (8, 10, 14, 18, 20) else ("TIER_2_OEM" if num in (5, 6, 9, 12, 13, 16) else "TIER_3_HANDBOOK")
        rows.append((eq_id, num, title, text, tier))
    return rows


KNOWLEDGE_SECTIONS = []
KNOWLEDGE_SECTIONS.extend(build_full_21_sections("EQ-A01", "Horizontal Front-Loading Belly Washer", "Garment Washing"))
KNOWLEDGE_SECTIONS.extend(build_full_21_sections("EQ-B01", "Vertical Basket Centrifugal Hydro Extractor", "Centrifugal Extraction"))

VALID_DEPTHS = {"FULL_CONTENT_BLUEPRINT", "PARTIAL_CONTENT_BLUEPRINT", "RESEARCH_PENDING", "INVENTORY_ONLY"}
VALID_CRITICALITIES = {"CRITICAL_UTILITY", "PRIMARY_PRODUCTION", "SECONDARY_AUXILIARY", "SAFETY_CRITICAL"}
VALID_WEAR_TIERS = {"CONSUMABLE", "WEAR_PART", "STRUCTURAL_PERMANENT"}
VALID_SEVERITIES = {"LOW", "MEDIUM", "HIGH", "CRITICAL_SHUTDOWN"}
VALID_FREQS = {"DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "BI_ANNUAL", "ANNUAL", "STATUTORY_RENEWAL"}


def populate_enriched_equipment_tables():
    for eq_row in EQUIPMENT:
        eq_id, _, _, _, _, _, depth, crit, _, _, _, _, _, _ = eq_row
        assert depth in VALID_DEPTHS, f"Invalid depth {depth} for {eq_id}"
        assert crit in VALID_CRITICALITIES, f"Invalid criticality {crit} for {eq_id}"
        spec = ALL_SPECS[eq_id]
        profile = spec["profile"]

        for sub in spec.get("subtypes", []):
            sub_id, sub_name, sub_bn, feat, cap = sub
            SUBTYPES.append((sub_id, eq_id, sub_name, sub_bn, feat, cap))

        for cmp_row in spec.get("components", []):
            cmp_id, cname, cbn, mat, func, wtier, maint = cmp_row
            assert wtier in VALID_WEAR_TIERS, f"Invalid wear_tier {wtier} on {cmp_id}"
            COMPONENTS.append((cmp_id, eq_id, cname, cbn, mat, func, wtier, maint))

        for prm in spec.get("params", []):
            pgroup, pname, pval, punit, ptol = prm
            TECHNICAL_PARAMETERS.append((eq_id, pgroup, pname, pval, punit, ptol))

        for fm in spec.get("failures", []):
            fid, symp, root, diag, corr, vib, sev = fm
            assert sev in VALID_SEVERITIES, f"Invalid severity {sev} on {fid}"
            FAILURE_MODES.append((fid, eq_id, symp, root, diag, corr, vib, sev))

        for proc in spec.get("procedures", []):
            pid, ptitle, pfreq, ptools, ploto, psteps = proc
            assert pfreq in VALID_FREQS, f"Invalid frequency {pfreq} on {pid}"
            MAINTENANCE_PROCEDURES.append((pid, eq_id, ptitle, pfreq, ptools, ploto, psteps))

        if eq_id not in ("EQ-A01", "EQ-B01"):
            sec_dict = spec.get("sections", {})
            assert len(sec_dict) >= 14, f"{eq_id} expected rich equipment-specific sections, got {len(sec_dict)}"
            for sec_num in sorted(sec_dict.keys()):
                assert 1 <= sec_num <= 21, f"Out of range section {sec_num} on {eq_id}"
                sec_title = get_profile_section_title(sec_num, profile)
                sec_md = sec_dict[sec_num]
                tier = "TIER_1_STATUTORY" if sec_num in (8, 10, 14, 18, 20) else ("TIER_2_OEM" if sec_num in (5, 6, 9, 12, 13, 16) else "TIER_3_HANDBOOK")
                KNOWLEDGE_SECTIONS.append((eq_id, sec_num, sec_title, sec_md, tier))


populate_enriched_equipment_tables()


def build():
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.executescript(SCHEMA_SQL)

    cur.executemany(
        "INSERT INTO system_family (id, code, name, bengali_name, icon_name, sort_order, is_dedicated_boiler) VALUES (?, ?, ?, ?, ?, ?, ?)",
        SYSTEM_FAMILIES,
    )
    cur.executemany(
        "INSERT INTO equipment_category (id, family_id, name, bengali_name, description) VALUES (?, ?, ?, ?, ?)",
        CATEGORIES,
    )

    eq_rows = [row + (NOW_TS, NOW_TS) for row in EQUIPMENT]
    cur.executemany(
        """INSERT INTO equipment (
            id, category_id, family_id, name, bengali_name, aliases_csv,
            content_depth_status, criticality_tier, capacity_range, operating_temperature,
            primary_drive_type, lubrication_regime, statutory_requirement, is_plant_dependent,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        eq_rows,
    )

    cur.executemany(
        "INSERT INTO equipment_subtype (id, equipment_id, subtype_name, bengali_name, distinguishing_features, typical_capacity) VALUES (?, ?, ?, ?, ?, ?)",
        SUBTYPES,
    )
    cur.executemany(
        "INSERT INTO component (id, equipment_id, name, bengali_name, material_specification, primary_function, wear_tier, maintenance_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        COMPONENTS,
    )
    cur.executemany(
        "INSERT INTO technical_parameter (equipment_id, parameter_group, parameter_name, standard_value, unit, tolerance) VALUES (?, ?, ?, ?, ?, ?)",
        TECHNICAL_PARAMETERS,
    )
    cur.executemany(
        "INSERT INTO knowledge_section (equipment_id, section_number, section_title, content_markdown, verification_tier) VALUES (?, ?, ?, ?, ?)",
        KNOWLEDGE_SECTIONS,
    )
    cur.executemany(
        "INSERT INTO failure_mode (id, equipment_id, symptom, root_cause, diagnostic_step, corrective_action, vibration_signature, severity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        FAILURE_MODES,
    )
    cur.executemany(
        "INSERT INTO maintenance_procedure (id, equipment_id, title, frequency, required_tools_csv, safety_lockout_procedure, action_steps_json) VALUES (?, ?, ?, ?, ?, ?, ?)",
        MAINTENANCE_PROCEDURES,
    )

    cur.executemany(
        "INSERT INTO calculation (id, code, title, bengali_title, category, formula_latex, engineering_reference, assumptions, limitations, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        CALCULATIONS,
    )
    cur.executemany(
        "INSERT INTO formula_variable (calculation_id, symbol, name, unit, default_value, min_bound, max_bound, is_required) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        VARIABLES,
    )

    # Populate FTS4 index for all 64 equipment records
    fts_rows = [
        (idx + 1, row[0], row[3], row[4], row[5], row[6], row[10])
        for idx, row in enumerate(EQUIPMENT)
    ]
    cur.executemany(
        "INSERT INTO equipment_fts (rowid, id, name, bengali_name, aliases_csv, content_depth_status, primary_drive_type) VALUES (?, ?, ?, ?, ?, ?, ?)",
        fts_rows,
    )

    conn.commit()

    # Self-checks
    fk_err = cur.execute("PRAGMA foreign_key_check;").fetchall()
    integ = cur.execute("PRAGMA integrity_check;").fetchone()[0]
    fam_cnt = cur.execute("SELECT COUNT(*) FROM system_family").fetchone()[0]
    eq_cnt = cur.execute("SELECT COUNT(*) FROM equipment").fetchone()[0]
    fts_cnt = cur.execute("SELECT COUNT(*) FROM equipment_fts").fetchone()[0]
    calc_cnt = cur.execute("SELECT COUNT(*) FROM calculation").fetchone()[0]
    var_cnt = cur.execute("SELECT COUNT(*) FROM formula_variable").fetchone()[0]
    depths = dict(cur.execute("SELECT content_depth_status, COUNT(*) FROM equipment GROUP BY content_depth_status").fetchall())
    uv = cur.execute("PRAGMA user_version;").fetchone()[0]
    sub_cnt = cur.execute("SELECT COUNT(*) FROM equipment_subtype").fetchone()[0]
    cmp_cnt = cur.execute("SELECT COUNT(*) FROM component").fetchone()[0]
    prm_cnt = cur.execute("SELECT COUNT(*) FROM technical_parameter").fetchone()[0]
    sec_cnt = cur.execute("SELECT COUNT(*) FROM knowledge_section").fetchone()[0]
    fm_cnt = cur.execute("SELECT COUNT(*) FROM failure_mode").fetchone()[0]
    proc_cnt = cur.execute("SELECT COUNT(*) FROM maintenance_procedure").fetchone()[0]
    conn.close()

    assert not fk_err, f"Foreign key errors: {fk_err}"
    assert integ == "ok", f"Integrity check failed: {integ}"
    assert fam_cnt == 16 and eq_cnt == 64 and fts_cnt == 64 and calc_cnt == 20 and var_cnt == 87, (
        f"Unexpected core counts: fam={fam_cnt}, eq={eq_cnt}, fts={fts_cnt}, calc={calc_cnt}, var={var_cnt}"
    )
    assert depths == {
        "FULL_CONTENT_BLUEPRINT": 56,
        "PARTIAL_CONTENT_BLUEPRINT": 1,
        "RESEARCH_PENDING": 7,
    }, f"Unexpected content depth counts: {depths}"

    print(f"Rebuilt {DB_PATH} cleanly (user_version={uv}):")
    print(f"  Families: {fam_cnt}, Equipment: {eq_cnt}, FTS4 rows: {fts_cnt}")
    print(f"  Depths: {depths}")
    print(f"  Child counts -> Subtypes: {sub_cnt}, Components: {cmp_cnt}, Params: {prm_cnt}, Sections: {sec_cnt}, Failures: {fm_cnt}, Procedures: {proc_cnt}")
    print(f"  Calculations: {calc_cnt}, Variables: {var_cnt}, Integrity: {integ}")


if __name__ == "__main__":
    build()
