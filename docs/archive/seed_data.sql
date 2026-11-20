-- ==============================================================================
-- DEPRECATION NOTICE (AUD-011):
-- THIS FILE IS AN OBSOLETE PHASE 04 PARTIAL SEED DRAFT (6 REPRESENTATIVE ASSETS).
-- DO NOT USE IT TO POPULATE THE PRODUCTION DATABASE.
-- Authoritative 64-asset SQLite asset (`mechanical_pro_notes.db`) is generated
-- by `/build_database.py`, `/seed_equipment_part1.py`, `/seed_equipment_part2.py`,
-- and `/seed_phase06_calculations.py`.
-- ==============================================================================
-- MECHANICAL PRO NOTES — VERIFIED INITIAL SEED DATASET (PHASE 04)
-- Classification: Strict 6-Tier Evidence Classification
-- Scope: Initial verified test dataset covering Washing, Extraction, Pumps, and
--        Dedicated Industrial Boilers across FULL, PARTIAL, and RESEARCH_PENDING tiers.
-- ==============================================================================

-- 1. SYSTEM FAMILIES
INSERT OR REPLACE INTO system_family (id, code, name, bengali_name, icon_name, sort_order, is_dedicated_boiler) VALUES
('FAM-A', 'WASHING_MACHINERY', 'Production & Washing Machinery', 'উৎপাদন ও ওয়াশিং যন্ত্রপাতি', 'ic_washing', 1, 0),
('FAM-B', 'EXTRACTION_SYSTEMS', 'Centrifugal Extraction Systems', 'সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর', 'ic_extractor', 2, 0),
('FAM-D', 'PUMPS_FLUID_SYSTEMS', 'Pumps & Fluid Handling Systems', 'পাম্প ও ফ্লুইড ট্রান্সপোর্ট সিস্টেম', 'ic_pump', 4, 0),
('FAM-G', 'BOILERS', 'Industrial Steam Boilers & Heat Recovery', 'ইন্ডাস্ট্রিয়াল স্টিম বয়লার ও হিট রিকভারি', 'ic_boiler', 7, 1);

-- 2. EQUIPMENT CATEGORIES
INSERT OR REPLACE INTO equipment_category (id, family_id, name, bengali_name, description) VALUES
('CAT-A01', 'FAM-A', 'Horizontal Washing Machines', 'হরাইজন্টাল ওয়াশিং মেশিন', 'Bulk garment tumbling, scouring, enzyme washing and chemical processing'),
('CAT-B01', 'FAM-B', 'Centrifugal Hydro Extractors', 'সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর', 'High-speed mechanical moisture de-watering'),
('CAT-D01', 'FAM-D', 'Boiler Feed Water Pumps', 'বয়লার ফিড ওয়াটার পাম্প', 'High-pressure multistage boiler feed water injection'),
('CAT-G01', 'FAM-G', 'Gas-Fired Steam Boilers', 'গ্যাস-ফায়ার্ড স্টিম বয়লার', 'High-efficiency 3-pass wet-back packaged natural gas boilers'),
('CAT-G02', 'FAM-G', 'Biomass & Jhut Steam Boilers', 'ঝুট ও বায়োমাস স্টিম বয়লার', 'Solid fabric waste and biomass-fired industrial steam boilers');

-- 3. EQUIPMENT ASSETS (6 verified representative assets across 3 content-depth tiers)
INSERT OR REPLACE INTO equipment (id, category_id, family_id, name, bengali_name, aliases_csv, content_depth_status, criticality_tier, capacity_range, operating_temperature, primary_drive_type, lubrication_regime, statutory_requirement, is_plant_dependent, created_at, updated_at) VALUES
('EQ-A01', 'CAT-A01', 'FAM-A', 'Horizontal Front-Loading Belly Washer', 'হরাইজন্টাল ফ্রন্ট-লোডিং বেলি ওয়াশার', 'Belly Washer|||Garment Washing Machine|||Rotary Drum Washer', 'FULL_CONTENT_BLUEPRINT', 'PRIMARY_PRODUCTION', '150 kg – 350 kg dry weight', '30°C – 95°C', 'Inverter-Duty VFD Helical Gear Drive (7.5 kW – 15 kW)', 'Lithium EP2 Grease (Plummer blocks weekly)', 'LOTO interlock on hinged door', 0, 1794500000000, 1794500000000),
('EQ-A04', 'CAT-A01', 'FAM-A', 'Atmospheric Sample Dyeing & Washing Machine', 'অ্যাটমোসফিয়ারিক স্যাম্পল ওয়াশিং মেশিন', 'Sample Washer|||Lab Wash Machine', 'PARTIAL_CONTENT_BLUEPRINT', 'PRIMARY_PRODUCTION', '5 kg – 25 kg dry batch', '20°C – 98°C', 'Direct V-Belt Induction Motor (1.5 kW – 3 kW)', 'ISO VG 46 Light Machine Oil', 'Standard emergency stop', 0, 1794500000000, 1794500000000),
('EQ-A05', 'CAT-A01', 'FAM-A', 'Ultrasonic Enzyme Washing Reactor', 'আল্ট্রাসনিক এনজাইম ওয়াশিং রিঅ্যাক্টর', 'Sonochemical Washer|||Cavitation Washer', 'RESEARCH_PENDING', 'PRIMARY_PRODUCTION', 'Awaiting plant verification', '40°C – 60°C', 'Piezoelectric Transducer Array (40 kHz)', 'Dry solid-state transducer', 'Plant-specific electrical insulation', 1, 1794500000000, 1794500000000),
('EQ-B01', 'CAT-B01', 'FAM-B', 'Vertical Basket Centrifugal Hydro Extractor', 'ভার্টিক্যাল বাস্কেট সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর', 'Hydro Extractor|||Centrifuge|||Water Spinner', 'FULL_CONTENT_BLUEPRINT', 'PRIMARY_PRODUCTION', '50 kg – 150 kg wet weight', 'Ambient (25°C – 50°C)', 'High-Torque Induction Motor with Centrifugal Clutch (5.5 kW – 11 kW)', 'High-Speed Synthetic Bearing Grease (NLGI 2 / 3 monthly)', 'Dynamic interlock: lid cannot open while basket rotates', 0, 1794500000000, 1794500000000),
('EQ-D02', 'CAT-D01', 'FAM-D', 'Multi-Stage Centrifugal Boiler Feed Water Pump', 'মাল্টি-স্টেজ সেন্ট্রিফিউগাল বয়লার ফিড ওয়াটার পাম্প', 'Boiler Feed Pump|||Multistage Pump|||BFP', 'PARTIAL_CONTENT_BLUEPRINT', 'CRITICAL_UTILITY', '5 m³/h – 25 m³/h at 12–25 bar', '85°C – 105°C deaerated water', '2-Pole 3-Phase TEFC Induction Motor (11 kW – 37 kW at 2900 RPM)', 'ISO VG 68 Turbine Oil / Grease', 'Dual standby requirement for statutory boiler compliance', 0, 1794500000000, 1794500000000),
('EQ-G01', 'CAT-G01', 'FAM-G', 'Natural Gas-Fired Three-Pass Wet-Back Packaged Steam Boiler', 'ন্যাচারাল গ্যাস-ফায়ার্ড থ্রি-পাস ওয়েট-ব্যাক প্যাকেজড স্টিম বয়লার', 'Gas Boiler|||Steam Generator|||Firetube Boiler', 'PARTIAL_CONTENT_BLUEPRINT', 'CRITICAL_UTILITY', '2.0 TPH – 10.0 TPH at 10.54 bar(g)', '184°C (Saturated Steam at 10.54 bar)', 'FD Burner Blower Motor (7.5 kW – 22 kW)', 'ISO VG 220 Gear Oil for modulating motor', 'The Boilers Act 2022 (Act 06 of 2022) & Rules 2025 registration', 0, 1794500000000, 1794500000000),
('EQ-G02', 'CAT-G02', 'FAM-G', 'Biomass/Jhut/Solid-Fuel Hand/Stoker-Fired Steam Boiler', 'বায়োমাস/ঝুট সলিড-ফুয়েল স্টিম বয়লার', 'Jhut Boiler|||Wood Boiler|||Solid Waste Boiler', 'PARTIAL_CONTENT_BLUEPRINT', 'CRITICAL_UTILITY', '1.5 TPH – 6.0 TPH at 8.0–10.0 bar(g)', '175°C – 184°C', 'Dual Fan System: FD Fan (5.5 kW) + ID Fan (15 kW)', 'High-Temp Bearing Grease on Fan Shafts', 'Mandatory Multicyclone spark arrestor & annual inspection', 0, 1794500000000, 1794500000000);

-- 4. EQUIPMENT SUBTYPES
INSERT OR REPLACE INTO equipment_subtype (id, equipment_id, subtype_name, bengali_name, distinguishing_features, typical_capacity) VALUES
('SUB-A01-1', 'EQ-A01', 'Standard Fixed-Speed Belly Washer', 'ফিক্সড স্পিড বেলি ওয়াশার', 'Dual forward-reverse contactors with single-speed 1440 RPM motor and pulley ratio', '150 kg – 250 kg'),
('SUB-A01-2', 'EQ-A01', 'VFD Inverter-Modulated Low-Liquor Belly Washer', 'ভিএফডি ইনভার্টার বেলি ওয়াশার', 'Integrated VFD with 1:5 liquor ratio and automated chemical dosing manifold', '250 kg – 350 kg'),
('SUB-B01-1', 'EQ-B01', 'Three-Leg Suspended Centrifugal Hydro', 'থ্রি-লেগ সাসপেন্ডেড হাইড্রো', 'Three pendulum suspension links with heavy coil springs for dynamic unbalance absorption', '50 kg – 100 kg'),
('SUB-G01-1', 'EQ-G01', 'Three-Pass Wet-Back Gas Packaged Boiler', 'থ্রি-পাস ওয়েট-ব্যাক গ্যাস বয়লার', 'Fully submerged turnaround combustion chamber without refractory rear wall', '2.0 TPH – 10.0 TPH');

-- 5. COMPONENTS
INSERT OR REPLACE INTO component (id, equipment_id, name, bengali_name, material_specification, primary_function, wear_tier, maintenance_method) VALUES
('CMP-A01-01', 'EQ-A01', 'Inner Rotating Basket', 'অভ্যন্তরীণ ঘূর্ণনশীল ড্রাম', 'AISI 316L Stainless Steel', 'Holds garments and wash liquor during tumbling and stone wash', 'STRUCTURAL_PERMANENT', 'Visual check for weld cracks and snagging burrs monthly'),
('CMP-A01-02', 'EQ-A01', 'Main Trunnion Shaft', 'মেইন ট্রানিয়ন শ্যাফট', 'Forged Alloy Steel (EN19 / 42CrMo4)', 'Transmits rotational torque and supports cantilevered drum mass', 'STRUCTURAL_PERMANENT', 'Ultrasonic non-destructive testing every 3 years'),
('CMP-A01-03', 'EQ-A01', 'Plummer Block Spherical Roller Bearing', 'প্লামার ব্লক স্পফেরিক্যাল রোলার বিয়ারিং', 'High-Carbon Chromium Steel (SKF 222xx series)', 'Carries heavy radial and reversing axial shock loads', 'WEAR_PART', 'Re-grease with Lithium EP2 weekly; vibration check monthly'),
('CMP-A01-04', 'EQ-A01', 'PTFE Mechanical Face Seal', 'পিটিএফই মেকানিক্যাল ফেস সিল', 'Silicon Carbide / Carbon with PTFE Gland', 'Prevents chemical wash liquor leakage into bearing housing', 'CONSUMABLE', 'Inspect quench drain daily; replace on leakage'),
('CMP-G01-01', 'EQ-G01', 'Corrugated Furnace Flue Tube', 'করুগেটেড ফার্নেস ফ্লু টিউব', 'BS EN 10028-2 / ASME SA-516 Gr 70 Carbon Steel', 'Primary combustion chamber with corrugated profile for thermal expansion relief', 'STRUCTURAL_PERMANENT', 'Visual & ultrasonic thickness measurement during annual statutory inspection'),
('CMP-G01-02', 'EQ-G01', 'Dual Spring-Loaded Safety Valve', 'ডুয়েল স্প্রিং-লোডেড সেফটি ভালভ', 'Cast Steel Body (WCB) with Stainless Steel Trim', 'Prevents over-pressurization above MAWP per ASME Sec I PG-67', 'WEAR_PART', 'Manual hand-easing weekly; live steam popping test annually');

-- 6. KNOWLEDGE SECTIONS (Representative 21-Section Teardown for EQ-A01)
INSERT OR REPLACE INTO knowledge_section (id, equipment_id, section_number, section_title, content_markdown, verification_tier) VALUES
(1, 'EQ-A01', 1, 'Introduction & Purpose', 'The Horizontal Front-Loading Belly Washer is the workhorse of textile washing plants, performing bulk desizing, enzyme washing, stone washing, bleaching, and chemical softening on denim jeans, knitwear, and woven apparel.', 'TIER_2_OEM'),
(2, 'EQ-A01', 3, 'Working Principle', 'Garments tumble inside a perforated cylindrical SS 316L basket fitted with internal lifter baffles. As the drum rotates at a critical Froude number of 0.6–0.8 (typically 26–32 RPM), garments are lifted to the 11 o clock position and drop by gravity into the wash bath.', 'TIER_3_HANDBOOK'),
(3, 'EQ-A01', 13, 'Lubrication Schedule', 'Drive-end and non-drive-end plummer block bearings (222xx series) require NLGI Grade 2 Lithium Complex EP grease (Mobilith SHC 220 or Shell Gadus S2 V220). Apply 40g per bearing housing every 160 operating hours (weekly). Clean purge ports.', 'TIER_2_OEM'),
(4, 'EQ-A01', 15, 'Failure Modes & Root Causes', 'Symptom: High 1X vibration and rumbling noise from rear drive housing. Probable root cause: Bearing inner raceway flaking or trunnion shaft deflection due to heavy stone loading. Diagnostic: Measure overall vibration velocity per ISO 20816-3:2026; if >4.5 mm/s RMS (Zone C/D), inspect bearing.', 'TIER_1_STATUTORY');

-- 7. TECHNICAL PARAMETERS
INSERT OR REPLACE INTO technical_parameter (id, equipment_id, parameter_group, parameter_name, standard_value, unit, tolerance) VALUES
(1, 'EQ-A01', 'MECHANICAL', 'Drum Diameter', '1400', 'mm', '±5 mm'),
(2, 'EQ-A01', 'MECHANICAL', 'Drum Length', '1600', 'mm', '±5 mm'),
(3, 'EQ-A01', 'ELECTRICAL', 'Main Motor Power', '11.0', 'kW', 'Nominal'),
(4, 'EQ-A01', 'OPERATIONAL', 'Standard Tumbling Speed', '28', 'RPM', '26–32 RPM'),
(5, 'EQ-G01', 'THERMAL', 'Rated Steam Output', '5000', 'kg/h', '±2%'),
(6, 'EQ-G01', 'PRESSURE', 'Design Pressure (MAWP)', '10.54', 'bar(g)', 'Statutory Maximum');

-- 8. FAILURE MODES
INSERT OR REPLACE INTO failure_mode (id, equipment_id, symptom, root_cause, diagnostic_step, corrective_action, vibration_signature, severity) VALUES
('FAIL-A01-01', 'EQ-A01', 'Rumbling noise and high radial vibration from trunnion bearing housing', 'Plummer block spherical roller bearing raceway spalling or moisture contamination', 'Vibration accelerometer measurement; grease sample analysis for metal particles', 'Lockout equipment, extract spherical roller bearing, inspect journal, pack with fresh EP2 grease', 'Dominant 1X running speed with BPFO bearing fault sidebands', 'HIGH'),
('FAIL-G01-01', 'EQ-G01', 'Frequent burner lockout on flame failure during high fire', 'Gas pressure drop below low-pressure switch setting or dirty UV flame sensor', 'Check gas train differential pressure manometer; clean UV photocell lens', 'Clean UV scanner lens; adjust gas regulator setpoint; verify pilot gas flow', 'None (Combustion electrical safety)', 'CRITICAL_SHUTDOWN');

-- 9. CALCULATIONS (Verified Implementations & Stubs)
INSERT OR REPLACE INTO calculation (id, code, title, bengali_title, category, formula_latex, engineering_reference, assumptions, limitations, status) VALUES
('CALC-01', 'BOILER_DIRECT_EFFICIENCY', 'Boiler Thermal Efficiency (Direct Method)', 'বয়লার থার্মাল এফিসিয়েন্সি (ডিরেক্ট মেথড)', 'BOILER', '\eta = \frac{\dot{m}_s (h_s - h_w)}{\dot{m}_f \times \text{NCV}} \times 100\%', 'ASME PTC 4 Fired Steam Generators', 'Steady state firing, constant steam pressure, accurate fuel flow measurement', 'Does not account for individual stack and radiation heat losses', 'IMPLEMENTED'),
('CALC-04', 'EVAPORATION_RATIO', 'Steam-to-Fuel Evaporation Ratio', 'স্টিম-টু-ফুয়েল ইভাপোরেশন রেশিও', 'BOILER', 'ER = \frac{\dot{m}_s}{\dot{m}_f}', 'Standard Plant Energy Auditing Practice', 'Consistent fuel quality and steady steam generation', 'Varies with feed water temperature and firing modulation', 'IMPLEMENTED'),
('CALC-05', 'BLOWDOWN_RATE', 'Boiler Blowdown Rate Calculation', 'বয়লার ব্লোডাউন রেট ক্যালকুলেশন', 'BOILER', 'BD\% = \frac{\text{TDS}_{feed}}{\text{TDS}_{boiler} - \text{TDS}_{feed}} \times 100\%', 'Spirax Sarco Boiler Water Quality Guidelines', 'Completely mixed boiler drum water; accurate TDS electrical conductivity readings', 'Assumes negligible carryover in saturated steam (<0.1%)', 'IMPLEMENTED'),
('CALC-07', 'PUMP_HYDRAULIC_POWER', 'Boiler Feed Pump Hydraulic Power', 'বয়লার ফিড পাম্প হাইড্রোলিক পাওয়ার', 'PUMP', 'P_{hyd} = \frac{\rho g Q H}{3600 \times 1000}', 'Hydraulic Institute (HI) Pump Standards', 'Incompressible liquid, steady flow, water density at operating temperature', 'Requires motor mechanical efficiency to estimate electrical power draw', 'IMPLEMENTED'),
('CALC-02', 'BOILER_INDIRECT_EFFICIENCY', 'Boiler Efficiency (Heat Loss / Indirect Method)', 'বয়লার ইনডিরেক্ট এফিসিয়েন্সি (হিট লস মেথড)', 'BOILER', '\eta = 100 - (L_1 + L_2 + L_3 + L_4 + L_5 + L_6)', 'BS EN 12953 / ASME PTC 4', 'Requires complete flue gas analysis (O2, CO2, CO, Stack Temp)', 'Pending plant flue-gas analyzer model confirmation', 'RESEARCH_PENDING');

-- 10. FORMULA VARIABLES FOR CALC-01 & CALC-05
INSERT OR REPLACE INTO formula_variable (id, calculation_id, symbol, name, unit, default_value, min_bound, max_bound, is_required) VALUES
(1, 'CALC-01', 'm_s', 'Steam Generation Rate', 'kg/h', 5000.0, 100.0, 50000.0, 1),
(2, 'CALC-01', 'h_s', 'Enthalpy of Saturated Steam', 'kJ/kg', 2780.0, 2000.0, 3200.0, 1),
(3, 'CALC-01', 'h_w', 'Enthalpy of Feed Water', 'kJ/kg', 356.0, 0.0, 600.0, 1),
(4, 'CALC-01', 'm_f', 'Fuel Consumption Rate', 'Nm³/h', 380.0, 10.0, 5000.0, 1),
(5, 'CALC-01', 'NCV', 'Net Calorific Value of Fuel', 'kJ/Nm³', 35800.0, 10000.0, 50000.0, 1),
(6, 'CALC-05', 'TDS_feed', 'Feed Water TDS', 'ppm', 120.0, 5.0, 500.0, 1),
(7, 'CALC-05', 'TDS_boiler', 'Boiler Drum Water TDS', 'ppm', 3000.0, 500.0, 5000.0, 1),
(8, 'CALC-05', 'm_s', 'Steam Generation Rate', 'kg/h', 5000.0, 100.0, 50000.0, 0);

-- 11. FTS VIRTUAL TABLE SYNCHRONIZATION
INSERT OR REPLACE INTO equipment_fts (rowid, id, name, bengali_name, aliases_csv, content_depth_status, primary_drive_type) VALUES
(1, 'EQ-A01', 'Horizontal Front-Loading Belly Washer', 'হরাইজন্টাল ফ্রন্ট-লোডিং বেলি ওয়াশার', 'Belly Washer|||Garment Washing Machine|||Rotary Drum Washer', 'FULL_CONTENT_BLUEPRINT', 'Inverter-Duty VFD Helical Gear Drive (7.5 kW – 15 kW)'),
(2, 'EQ-A04', 'Atmospheric Sample Dyeing & Washing Machine', 'অ্যাটমোসফিয়ারিক স্যাম্পল ওয়াশিং মেশিন', 'Sample Washer|||Lab Wash Machine', 'PARTIAL_CONTENT_BLUEPRINT', 'Direct V-Belt Induction Motor (1.5 kW – 3 kW)'),
(3, 'EQ-A05', 'Ultrasonic Enzyme Washing Reactor', 'আল্ট্রাসনিক এনজাইম ওয়াশিং রিঅ্যাক্টর', 'Sonochemical Washer|||Cavitation Washer', 'RESEARCH_PENDING', 'Piezoelectric Transducer Array (40 kHz)'),
(4, 'EQ-B01', 'Vertical Basket Centrifugal Hydro Extractor', 'ভার্টিক্যাল বাস্কেট সেন্ট্রিফিউগাল হাইড্রো এক্সট্র্যাক্টর', 'Hydro Extractor|||Centrifuge|||Water Spinner', 'FULL_CONTENT_BLUEPRINT', 'High-Torque Induction Motor with Centrifugal Clutch (5.5 kW – 11 kW)'),
(5, 'EQ-D02', 'Multi-Stage Centrifugal Boiler Feed Water Pump', 'মাল্টি-স্টেজ সেন্ট্রিফিউগাল বয়লার ফিড ওয়াটার পাম্প', 'Boiler Feed Pump|||Multistage Pump|||BFP', 'PARTIAL_CONTENT_BLUEPRINT', '2-Pole 3-Phase TEFC Induction Motor (11 kW – 37 kW at 2900 RPM)'),
(6, 'EQ-G01', 'Natural Gas-Fired Three-Pass Wet-Back Packaged Steam Boiler', 'ন্যাচারাল গ্যাস-ফায়ার্ড থ্রি-পাস ওয়েট-ব্যাক প্যাকেজড স্টিম বয়লার', 'Gas Boiler|||Steam Generator|||Firetube Boiler', 'PARTIAL_CONTENT_BLUEPRINT', 'FD Burner Blower Motor (7.5 kW – 22 kW)'),
(7, 'EQ-G02', 'Biomass/Jhut/Solid-Fuel Hand/Stoker-Fired Steam Boiler', 'বায়োমাস/ঝুট সলিড-ফুয়েল স্টিম বয়লার', 'Jhut Boiler|||Wood Boiler|||Solid Waste Boiler', 'PARTIAL_CONTENT_BLUEPRINT', 'Dual Fan System: FD Fan (5.5 kW) + ID Fan (15 kW)');
