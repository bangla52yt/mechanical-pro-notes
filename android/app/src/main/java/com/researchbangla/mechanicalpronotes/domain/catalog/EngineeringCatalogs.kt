package com.researchbangla.mechanicalpronotes.domain.catalog

import java.text.Normalizer

/**
 * Single Authoritative Runtime Catalogs for:
 * 1. Inspection & Maintenance Checklists ([PlantChecklistCatalog])
 * 2. Technical Reference Library ([TechnicalReferenceCatalog])
 * 3. General Mechanical Engineering Fundamentals ([FundamentalsCatalog])
 * 4. Unified Offline Knowledge Search Index ([EngineeringKnowledgeSearch])
 *
 * Strictly static engineering knowledge only — personal user records
 * (`user_note`, `user_bookmark`, `calculation_history`, `checklist_progress`)
 * remain completely isolated inside Room UserDataDao per DEC-014.
 */

// ---------------------------------------------------------------------------
// 1. PLANT CHECKLIST CATALOG (Single Source of Truth)
// ---------------------------------------------------------------------------

data class CatalogChecklistItem(
    val id: Int,
    val text: String,
    val category: String,
    val isCrucial: Boolean = false
)

data class CatalogChecklist(
    val id: String,
    val title: String,
    val description: String,
    val standardReference: String,
    val items: List<CatalogChecklistItem>,
    val defaultEquipmentId: String = "UNASSOCIATED",
    val relatedFamilyId: String? = null
)

object PlantChecklistCatalog {
    const val ID_BOILER_DAILY = "CHK-BLR-DAILY"
    const val ID_BOILER_WEEKLY = "CHK-BLR-WEEKLY"
    const val ID_WASH_PM = "CHK-WASH-PREV"
    const val ID_HYDRO_SAFETY = "CHK-HYDRO-SAFE"

    val ALL_CHECKLISTS: List<CatalogChecklist> = listOf(
        CatalogChecklist(
            id = ID_BOILER_DAILY,
            title = "Daily Boiler Pre-Start & Blowdown",
            description = "Standard operational check before lighting up burner and post-shift blowdown.",
            standardReference = "The Boilers Act, 2022 (Sec 18–19) / OCIB Operational Practice",
            defaultEquipmentId = "EQ-G01",
            relatedFamilyId = "FAM-G",
            items = listOf(
                CatalogChecklistItem(1, "Check feedwater tank level and deaerator temperature (min 85°C)", "Pre-Start", true),
                CatalogChecklistItem(2, "Inspect water gauge glass and verify dual gauge cock functionality", "Pre-Start", true),
                CatalogChecklistItem(3, "Verify gas line train pressure and check for smell/gas detector status", "Fuel Line", true),
                CatalogChecklistItem(4, "Check water softener hardness (< 5 ppm as CaCO3) and TDS", "Water Quality", false),
                CatalogChecklistItem(5, "Perform surface continuous blowdown sampling test", "Blowdown", false),
                CatalogChecklistItem(6, "Execute bottom intermittent blowdown under reduced load", "Blowdown", true),
                CatalogChecklistItem(7, "Inspect chimney flue gas temperature and draft fan dampers", "Combustion", false),
                CatalogChecklistItem(8, "Verify Mobrey water level controller high/low alarms and trip", "Safety", true)
            )
        ),
        CatalogChecklist(
            id = ID_BOILER_WEEKLY,
            title = "Weekly Boiler Safety & Interlock Test",
            description = "Weekly physical testing of safety valves and automated cutout controls.",
            standardReference = "Boilers Act 2022 / ASME Section I",
            defaultEquipmentId = "EQ-G01",
            relatedFamilyId = "FAM-G",
            items = listOf(
                CatalogChecklistItem(11, "Manually ease spring-loaded safety valve levers at >75% working pressure", "Safety", true),
                CatalogChecklistItem(12, "Conduct evaporation test to verify low water fuel cutoff trip", "Safety", true),
                CatalogChecklistItem(13, "Blow down gauge glass water and steam cocks to clear sediment", "Instruments", false),
                CatalogChecklistItem(14, "Inspect chemical dosing pump stroke and chemical tank level", "Water Treatment", false),
                CatalogChecklistItem(15, "Check FD/ID fan V-belt tension and bearing temperature", "Drives", false),
                CatalogChecklistItem(16, "Inspect burner flame scanner UV cell lens for soot accumulation", "Burner", false)
            )
        ),
        CatalogChecklist(
            id = ID_WASH_PM,
            title = "Washing Machine Mechanical PM",
            description = "Weekly preventive maintenance check on front-load industrial washers.",
            standardReference = "Wash Plant Maintenance Standard Operating Procedure",
            defaultEquipmentId = "EQ-A01",
            relatedFamilyId = "FAM-A",
            items = listOf(
                CatalogChecklistItem(21, "Check door pneumatic safety interlock (must prevent drum rotation when open)", "Safety", true),
                CatalogChecklistItem(22, "Inspect main drive V-belts for cracking, glazing, or slack", "Drives", false),
                CatalogChecklistItem(23, "Check main shaft spherical roller bearing temperature (< 65°C)", "Bearings", true),
                CatalogChecklistItem(24, "Inspect steam injection pneumatic valve and check valve for seat leaks", "Steam", false),
                CatalogChecklistItem(25, "Verify water drain dump valve seals and lint trap clearance", "Drain", false),
                CatalogChecklistItem(26, "Check inverter drive cooling fans and electrical terminal tightness", "Electrical", false)
            )
        ),
        CatalogChecklist(
            id = ID_HYDRO_SAFETY,
            title = "Hydro Extractor Safety & Brake Audit",
            description = "Centrifugal hydro extractor critical safety and vibration inspection.",
            standardReference = "Bangladesh Labour Rules 2015, Schedule IV",
            defaultEquipmentId = "EQ-B01",
            relatedFamilyId = "FAM-B",
            items = listOf(
                CatalogChecklistItem(31, "Test lid electromechanical interlock (drum must halt before lid unlatches)", "Safety", true),
                CatalogChecklistItem(32, "Verify pneumatic brake lining wear and emergency stop response time (< 20s)", "Braking", true),
                CatalogChecklistItem(33, "Inspect three-point pendulum suspension springs and damper links", "Suspension", true),
                CatalogChecklistItem(34, "Check dynamic balance and monitor vibration level during ramp-up", "Vibration", false),
                CatalogChecklistItem(35, "Replenish spindle bearing grease with NLGI 2 lithium complex grease", "Lubrication", false)
            )
        )
    )

    fun isPackagedChecklistId(checklistId: String): Boolean {
        val trimmed = checklistId.trim()
        return ALL_CHECKLISTS.any { it.id.equals(trimmed, ignoreCase = true) }
    }
}

// ---------------------------------------------------------------------------
// 2. TECHNICAL REFERENCE LIBRARY CATALOG (Single Source of Truth)
// ---------------------------------------------------------------------------

data class EngineeringReferenceItem(
    val id: String,
    val title: String,
    val bengaliTitle: String,
    val category: String,
    val authority: String,
    val summary: String,
    val mandatoryRequirements: String
)

object TechnicalReferenceCatalog {
    val ALL_REFERENCES: List<EngineeringReferenceItem> = listOf(
        EngineeringReferenceItem(
            id = "REF-01",
            title = "The Boilers Act, 2022 (Act No. 06 of 2022)",
            bengaliTitle = "বয়লার আইন, ২০২২ (২০২২ সনের ০৬ নং আইন)",
            category = "Bangladesh Statutory",
            authority = "Parliament of Bangladesh / Office of Chief Inspector of Boilers (OCIB)",
            summary = "Primary statutory legislation regulating boiler registration, inspection, operation, repair permits, and accident reporting in Bangladesh (commenced 11 April 2022). Repealed the former Boilers Act, 1923 under Section 54.",
            mandatoryRequirements = "Mandatory registration for closed steam vessels exceeding 25 liters capacity (Section 2(7)). Attendant certification/licensing under Section 9 & Rules 2025; classification & statutory lifespan framework under Section 13. Uncertified or unlicensed operation carries criminal penal liability (Sections 36 & 38). Annual renewal under Section 19 (Certificate under Section 18) requires OCIB internal/external visual examination and live safety-valve steam popping test; hydraulic testing (1.25x–1.5x MAWP) applies upon initial registration, after Section 24 structural weld repair permit, or when ordered by the Inspector."
        ),
        EngineeringReferenceItem(
            id = "REF-02",
            title = "Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)",
            bengaliTitle = "বাংলাদেশ বয়লার বিধিমালা, ২০২৫",
            category = "Bangladesh Statutory",
            authority = "Ministry of Industries / OCIB (Published 29 June 2025; 69 Rules)",
            summary = "Subordinate statutory rules under Act No. 06 of 2022 defining boiler categories and lifespan caps (Rule 3: 10 yr electrode, 15 yr small industrial, 30 yr packaged industrial, 60 yr field-erected; up to +10 yr max Board-approved extension with NDT/hydraulic/steam tests), inspection rules, and forms.",
            mandatoryRequirements = "Dual spring-loaded safety valves required. Continuous supervision by OCIB-certified Grade-1, Grade-2, or Grade-3 boiler attendants required per capacity slab under Section 9 of the Act and the Boiler Rules, 2025. Verify specific Rule/Schedule forms in the official Bangladesh Gazette."
        ),
        EngineeringReferenceItem(
            id = "REF-03",
            title = "Bangladesh Labour Act, 2006 (Amended 2013) & Rules 2015",
            bengaliTitle = "বাংলাদেশ শ্রম আইন, ২০০৬ ও শ্রম বিধিমালা, ২০১৫",
            category = "Bangladesh Statutory",
            authority = "Ministry of Labour and Employment / DIFE",
            summary = "Occupational health and machinery safety law. Regulates pressure vessel inspection, fencing of moving machinery, hydro extractor interlocks, and personal protective equipment.",
            mandatoryRequirements = "Moving pulleys and v-belts must be fully encased in steel guards. Hydro extractors must feature lid interlock preventing opening while spinning."
        ),
        EngineeringReferenceItem(
            id = "REF-04",
            title = "ASME BPVC Section I — Rules for Construction of Power Boilers",
            bengaliTitle = "এএসএমই বয়লার অ্যান্ড প্রেসার ভেসেল কোড - সেকশন ১",
            category = "International Standard",
            authority = "American Society of Mechanical Engineers",
            summary = "Global benchmark standard for boiler shell thickness design, material specifications (SA-516 Grade 70), stay bolts, welding qualification, and safety valve relieving capacity.",
            mandatoryRequirements = "Minimum shell plate thickness equations, joint efficiency factors, and mandatory ASME 'S' stamp fabrication compliance."
        ),
        EngineeringReferenceItem(
            id = "REF-05",
            title = "Spirax Sarco — The Steam and Condensate Loop",
            bengaliTitle = "স্পাইরাক্স সারকো — স্টিম অ্যান্ড কনডেনসেট লুপ গাইড",
            category = "Thermal Engineering",
            authority = "Spirax-Sarco Engineering plc",
            summary = "Definitive industrial engineering manual on steam distribution sizing, condensate return pipework, thermodynamic and float steam trap selection, and water hammer prevention.",
            mandatoryRequirements = "Steam lines must pitch 1:100 toward drain pockets with separator traps every 30-50 meters to prevent catastrophic water hammer."
        ),
        EngineeringReferenceItem(
            id = "REF-06",
            title = "ISO 20816-3:2026 (replacing ISO 20816-3:2022 & ISO 10816-3): Mechanical Vibration Severity Standard",
            bengaliTitle = "আইএসও ২০৮১৬-৩:২০২৬ (পূর্বে আইএসও ১০৮১৬-৩): মেকানিক্যাল ভাইব্রেশন স্ট্যান্ডার্ড",
            category = "Maintenance & Reliability",
            authority = "International Organization for Standardization (ISO/TC 108/SC 2)",
            summary = "Vibration evaluation guidelines for coupled industrial machines with nominal power above 15 kW and operating speeds between 120 r/min and 30,000 r/min measured in situ.",
            mandatoryRequirements = "Classifies machines into Group 1 (>300 kW or shaft height >=315 mm) and Group 2 (15–300 kW or shaft height 160–315 mm) on Rigid vs Flexible supports using Criterion I (Zones A–D broad-band RMS velocity/displacement) and Criterion II (change from baseline). Standard Rigid Group 2 field baseline guide: Zone A <=1.4, Zone B <=2.8, Zone C <=4.5, Zone D >4.5 mm/s RMS (verify Group 1/Flexible curves in published ISO text). Explicitly EXCLUDES machines <=15 kW, <120 r/min, reciprocating engines/compressors (ISO 10816-6 / ISO 20816-8), rotodynamic pumps covered by ISO 10816-7, submerged motor-pumps, and rotary positive-displacement compressors/blowers."
        ),
        EngineeringReferenceItem(
            id = "REF-07",
            title = "ASME BPVC Section VIII Div 1 — Pressure Vessels & Hydro Tanks",
            bengaliTitle = "এএসএমই প্রেসার ভেসেল কোড - সেকশন ৮",
            category = "International Standard",
            authority = "American Society of Mechanical Engineers",
            summary = "Design, radiography, hydrostatic testing, and PRV sizing standard for air receivers, deaerator storage tanks, flash vessels, and high-pressure dye/wash pressure vessels.",
            mandatoryRequirements = "Air receivers and blowdown flash vessels require certified relief valves set at or below vessel MAWP plus automated bottom condensate/sludge drain."
        ),
        EngineeringReferenceItem(
            id = "REF-08",
            title = "BS 2486 — Boiler Water & Feedwater Treatment Recommendations",
            bengaliTitle = "বিএস ২৪৮৬: বয়লার ওয়াটার ট্রিটমেন্ট স্ট্যান্ডার্ড",
            category = "Thermal Engineering",
            authority = "British Standards Institution",
            summary = "Water chemistry targets for packaged shell firetube boilers (<20 bar) protecting heat-transfer tubes against carbonate scaling, caustic embrittlement, and oxygen pitting.",
            mandatoryRequirements = "Target feedwater hardness <5 ppm CaCO3, feedwater O2 <0.02 mg/L (after chemical sulfite/hydrazine scavenging), drum pH 10.5–11.8, drum TDS <=3,500 ppm, and daily silica/phosphate checks."
        ),
        EngineeringReferenceItem(
            id = "REF-09",
            title = "ISO 10472 (Parts 1–6) — Industrial Laundry Machinery Safety",
            bengaliTitle = "আইএসও ১০৪৭২: ইন্ডাস্ট্রিয়াল লন্ড্রি মেশিনারি সেফটি",
            category = "Maintenance & Reliability",
            authority = "International Organization for Standardization",
            summary = "Safety design and interlock requirements for belly washers, washer-extractors, hydro extractors, and steam-heated tumble dryers.",
            mandatoryRequirements = "Mandatory dual-channel door/lid interlock preventing hatch opening above zero-speed, dynamic unbalance trip switch, and emergency-stop mushroom circuit isolating steam & main power."
        ),
        EngineeringReferenceItem(
            id = "REF-10",
            title = "NFPA 20 — Stationary Fire Pumps & Factory Fire Hydrants",
            bengaliTitle = "এনএফপিএ ২০: ফায়ার পাম্প সিস্টেম স্ট্যান্ডার্ড",
            category = "International Standard",
            authority = "National Fire Protection Association",
            summary = "Selection, weekly churn test, suction head, and jockey pump pressure staging rules governing garment washing plant electric & diesel fire pump rooms.",
            mandatoryRequirements = "Weekly no-flow run test (minimum 10 min electric / 30 min diesel) with automated start pressure drop verification and fuel tank >=75% ready reserve."
        ),
        EngineeringReferenceItem(
            id = "REF-11",
            title = "ISO 8573-1 — Compressed Air Purity & Filtration Classes",
            bengaliTitle = "আইএসও ৮৫৭৩-১: কমপ্রেসড এয়ার ফিল্ট্রেশন",
            category = "Maintenance & Reliability",
            authority = "International Organization for Standardization",
            summary = "Defines solid particle, water dew-point, and oil aerosol thresholds for rotary screw compressors serving pneumatic washer valves and solenoid manifolds.",
            mandatoryRequirements = "Maintain Class 2.4.2 or cleaner for plant instrument air using 1-micron coalescing filter and refrigerated dryer (+3°C pressure dew point) to avoid sticky solenoids."
        ),
        EngineeringReferenceItem(
            id = "REF-12",
            title = "Bangladesh DoE ECR 2023 — Garment Washing ETP Discharge Limits",
            bengaliTitle = "পরিবেশ সংরক্ষণ বিধিমালা ২০২৩ (ইটিপি নির্গমন মান)",
            category = "Bangladesh Statutory",
            authority = "Department of Environment (DoE), Bangladesh",
            summary = "Statutory effluent quality compliance standards for textile dyeing and washing ETP discharge into inland surface water.",
            mandatoryRequirements = "Mandatory continuous flow-meter logging with compliance within pH 6.0–9.0, BOD5 <=30 mg/L, COD <=200 mg/L, TSS <=100 mg/L, Oil & Grease <=10 mg/L, and temperature <=40°C."
        )
    )
}

// ---------------------------------------------------------------------------
// 3. MECHANICAL ENGINEERING FUNDAMENTALS CATALOG (DOC-MPN-009 + §4.1 Audit)
// ---------------------------------------------------------------------------

enum class FundamentalTopicStatus(val label: String) {
    VERIFIED_BASELINE("Verified Discipline Baseline"),
    CONTENT_GAP("Content Pending Authoring")
}

data class FundamentalTopic(
    val id: String,
    val title: String,
    val bengaliTitle: String,
    val disciplineGroup: String,
    val status: FundamentalTopicStatus,
    val theoreticalFoundations: String,
    val washingPlantApplications: String,
    val governingFormulas: List<String>,
    val failureMechanisms: String,
    val linkedEquipmentIds: List<String>,
    val linkedCalcIds: List<String>,
    val sourceCitation: String
)

object FundamentalsCatalog {
    val ALL_TOPICS: List<FundamentalTopic> = listOf(
        FundamentalTopic(
            id = "FUND-01",
            title = "Applied Thermodynamics",
            bengaliTitle = "ফলিত তাপগতিবিদ্যা (Applied Thermodynamics)",
            disciplineGroup = "Thermal & Energy",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "First & Second Laws of Thermodynamics; Rankine steam cycle; pure water/steam saturation properties (IAPWS-IF97), sensible & latent enthalpy (h_f, h_fg, h_g), and moist-air psychrometrics.",
            washingPlantApplications = "Steam boilers generating 6–10 bar(g) saturated steam, tumbler dryer radiator heating, direct steam injection into wash baths (40°C–90°C), flash vessels, and refrigerated air dryers.",
            governingFormulas = listOf(
                "Q = m_dot * (h2 - h1)  [Steady heat transfer enthalpy balance]",
                "eta_direct = [Q_s * (h_g - h_f) / (q_fuel * GCV)] * 100",
                "Q_dry = m_air * Cp_air * ΔT + m_evap * h_fg"
            ),
            failureMechanisms = "Thermal shock on cold headers, water hammer from trapped condensate, high flue stack loss reducing efficiency, and wet carryover steam reducing drying rate.",
            linkedEquipmentIds = listOf("EQ-B03", "EQ-F04", "EQ-G01", "EQ-G02", "EQ-J02"),
            linkedCalcIds = listOf("CALC-01", "CALC-06", "CALC-17", "CALC-18"),
            sourceCitation = "DOC-MPN-009 §2.1 | IAPWS-IF97"
        ),
        FundamentalTopic(
            id = "FUND-02",
            title = "Fluid Mechanics & Hydraulic Machinery",
            bengaliTitle = "ফ্লুইড মেকানিক্স ও হাইড্রোলিক যন্ত্রপাতি",
            disciplineGroup = "Fluid & Pumping",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Fluid statics, Pascal's Law, continuity (A·v = const), Bernoulli equation, Darcy-Weisbach pipe friction head loss, Reynolds number (Re), centrifugal pump affinity laws, and Net Positive Suction Head (NPSHa vs NPSHr).",
            washingPlantApplications = "Multistage boiler feed pumps, raw/softened centrifugal transfer pumps, AODD chemical/sludge pumps, process steam/air pipe sizing, and tilting washer hydraulic rams.",
            governingFormulas = listOf(
                "h_f = f * (L / D) * (v^2 / (2 * g))  [Darcy-Weisbach]",
                "P_hyd = (rho * g * Q * H) / 3.6e6  [kW]",
                "Q1/Q2 = N1/N2 | H1/H2 = (N1/N2)^2 | P1/P2 = (N1/N2)^3  [Affinity Laws]",
                "NPSHa >= NPSHr + 0.5 to 1.0 m  [Cavitation Safety Margin]"
            ),
            failureMechanisms = "Vapor cavitation pitting on hot feed pump impellers, valve water hammer blowing flange gaskets, excessive header pressure drops starving machines.",
            linkedEquipmentIds = listOf("EQ-A04", "EQ-D01", "EQ-D02", "EQ-D03", "EQ-F01"),
            linkedCalcIds = listOf("CALC-07", "CALC-14", "CALC-20"),
            sourceCitation = "DOC-MPN-009 §2.2 | API 610 / ISO 5199"
        ),
        FundamentalTopic(
            id = "FUND-03",
            title = "Heat Transfer (Conduction, Convection, Radiation)",
            bengaliTitle = "তাপ সঞ্চালন ও হিট এক্সচেঞ্জার",
            disciplineGroup = "Thermal & Energy",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Fourier conduction law, Newton convective cooling, Stefan-Boltzmann radiation, Overall Heat Transfer Coefficient (U), Log Mean Temperature Difference (LMTD), and effectiveness-NTU.",
            washingPlantApplications = "Drain heat recovery Plate Heat Exchangers (PHE), gas boiler smoke-tube convective passes, jhut refractory furnace radiation, rockwool pipe cladding, and tumbler finned coils.",
            governingFormulas = listOf(
                "Q = U * A * ΔT_lm  [Heat Exchanger Duty]",
                "ΔT_lm = (ΔT1 - ΔT2) / ln(ΔT1 / ΔT2)",
                "q_pipe = [2 * pi * k * L * (T_in - T_out)] / ln(r2 / r1)"
            ),
            failureMechanisms = "Calcium scale & wash lint fouling plates/tubes cutting U-value, water-logged pipe insulation causing Corrosion Under Insulation (CUI) and massive heat waste.",
            linkedEquipmentIds = listOf("EQ-B03", "EQ-F05", "EQ-F06", "EQ-G01"),
            linkedCalcIds = listOf("CALC-10", "CALC-11"),
            sourceCitation = "DOC-MPN-009 §2.3 | TEMA / BS 5970"
        ),
        FundamentalTopic(
            id = "FUND-04",
            title = "Strength of Materials & Pressure Vessels",
            bengaliTitle = "শক্তি ও পীড়ন বিদ্যা (Strength of Materials)",
            disciplineGroup = "Mechanics & Design",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Tensile/compressive/shear stress, Hooke's Law, Young's modulus, thin/thick cylinder hoop stress (sigma_h = p·d / 2t) and longitudinal stress, combined shaft bending + torsion (Tresca / von Mises), and buckling.",
            washingPlantApplications = "Boiler pressure shells & tubes, cantilevered washing-machine trunnion shafts under alternating wet loads, centrifugal hydro basket hoop stress, overhead hoist beams (`EQ-M01`), steam headers.",
            governingFormulas = listOf(
                "t = (P * R) / (S * E - 0.6 * P) + C  [ASME Sec I Shell Thickness]",
                "tau_max = sqrt((sigma_b / 2)^2 + tau_t^2)  [Combined Shaft Shear]",
                "sigma_hoop = rho * (omega * r)^2  [Rotating Hydro Basket Ring]"
            ),
            failureMechanisms = "Alternating fatigue fracture on washer trunnion shafts, catastrophic hydro basket burst from stress-corrosion cracking, boiler shell/furnace bulge under overheating.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-B01", "EQ-G01", "EQ-M01"),
            linkedCalcIds = listOf("CALC-16"),
            sourceCitation = "DOC-MPN-009 §2.4 | ASME BPVC Sec I / Sec VIII"
        ),
        FundamentalTopic(
            id = "FUND-05",
            title = "Theory of Machines & ISO 20816-3:2026 Vibration",
            bengaliTitle = "মেশিন তত্ত্ব ও মেকানিক্যাল ভাইব্রেশন (ISO 20816-3)",
            disciplineGroup = "Mechanics & Design",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Kinematic linkages, gear train ratios, dynamic unbalance (F = m·r·omega²), critical whirling shaft speed, spring-damper isolation, and ISO 20816-3:2026 broad-band RMS velocity evaluation (>15 kW, 120–30,000 RPM).",
            washingPlantApplications = "Three-point pendulum suspension on hydro extractors, drum shock isolators, high-speed lint/ID blowers, compressor vibration pads, and gearbox backlash trending.",
            governingFormulas = listOf(
                "F_unbalance = m * r * omega^2",
                "Rigid Foundation (ISO 20816-3): Zone A <=1.4 | Zone B <=2.8 | Zone C <=4.5 | Zone D >4.5 mm/s",
                "Flexible Foundation: Zone A <=2.3 | Zone B <=4.5 | Zone C <=7.1 | Zone D >7.1 mm/s"
            ),
            failureMechanisms = "Hydro extractor resonant shaking from unequal load or broken damper links, lint buildup causing 1x unbalance on exhaust blowers, gear tooth shock pitting.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-B01", "EQ-E02", "EQ-P01"),
            linkedCalcIds = listOf("CALC-16"),
            sourceCitation = "DOC-MPN-009 §2.5 | ISO 20816-3:2026"
        ),
        FundamentalTopic(
            id = "FUND-06",
            title = "Machine Design & Transmission Elements",
            bengaliTitle = "মেশিন ডিজাইন, বিয়ারিং ও ট্রান্সমিশন এলিমেন্ট",
            disciplineGroup = "Mechanics & Design",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Fatigue safety factors (Goodman/Soderberg), ISO 281 L10h bearing fatigue life, bolted joint preload torque (T = K·d·F_i), V-belt & timing-belt tension geometry, couplings, and mechanical face seal lubrication.",
            washingPlantApplications = "222xx/223xx spherical roller plummer blocks, SPB/SPC matched V-belt sets (`CALC-15`), flexible jaw/pin couplings, SiC cartridge seals on pumps, high-tensile steam flange bolting.",
            governingFormulas = listOf(
                "L10h = (10^6 / (60 * N)) * (C / P)^p  [p=3 ball, p=10/3 roller]",
                "T = K * d * F_i  [Bolt Tightening Torque, K ~ 0.15–0.20]",
                "L_belt = 2*C + (pi/2)*(D + d) + (D - d)^2 / (4*C)"
            ),
            failureMechanisms = "Grease washout spalling plummer-block bearings, mismatched V-belts burning in worn pulley grooves, elastomer coupling dust from shaft misalignment, mechanical seal dry run.",
            linkedEquipmentIds = listOf("EQ-E01", "EQ-E03", "EQ-E04", "EQ-E05", "EQ-E06"),
            linkedCalcIds = listOf("CALC-15"),
            sourceCitation = "DOC-MPN-009 §2.6 | ISO 281 / ISO 4184"
        ),
        FundamentalTopic(
            id = "FUND-07",
            title = "Material Science, Metallurgy & Corrosion",
            bengaliTitle = "মেটেরিয়াল সায়েন্স, মেটালার্জি ও ক্ষয়রোধ",
            disciplineGroup = "Materials & Fabrication",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Iron-Carbon phase behavior, heat treatments, austenitic stainless steels (AISI 304 vs 316L, PREN = %Cr + 3.3·%Mo + 16·%N), pitting/crevice/chloride SCC, caustic embrittlement, and elastomer limits (PTFE, EPDM, FKM/Viton, NBR).",
            washingPlantApplications = "AISI 316L perforated inner wash drums resisting chloride bleach/acids, SA-516 Gr 70 boiler pressure plates, PTFE steam valve seats, EPDM water vs Viton diesel/chemical seals, EN8/EN19 shafts.",
            governingFormulas = listOf(
                "PREN = %Cr + 3.3 * %Mo + 16 * %N  [>23 for 316L chloride service]",
                "Corrosion Rate CR = (K * ΔW) / (A * t * rho)  [mm/yr]"
            ),
            failureMechanisms = "Pitting/stress-corrosion cracking on AISI 304 drums exposed to hypochlorite bleach, boiler tube caustic cracking from pH excursions, wrong NBR rubber swelling in steam/chemicals.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-D06", "EQ-F01", "EQ-G01", "EQ-I01"),
            linkedCalcIds = emptyList(),
            sourceCitation = "DOC-MPN-009 §2.7 | ASTM / AISI"
        ),
        FundamentalTopic(
            id = "FUND-08",
            title = "Manufacturing Technology & Workshop Practice",
            bengaliTitle = "ম্যানুফ্যাকচারিং টেকনোলজি ও ওয়ার্কশপ প্র্যাকটিস",
            disciplineGroup = "Materials & Fabrication",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Turning/boring cutting speed (N = 1000·V_c / pi·D), ISO limits & fits (e.g. H7/k5 bearing fits), SMAW & SS316L TIG (GTAW) welding heat input & HAZ control, NPT vs BSPT pipe threads, dial shaft alignment.",
            washingPlantApplications = "Lathe turning replacement SS pump sleeves, keyway boring pulleys, sanitary TIG repair of drum baffles, structural SMAW repair on hoist rails, dial-indicator rim & face alignment.",
            governingFormulas = listOf(
                "N_spindle = (1000 * V_c) / (pi * D)  [RPM]",
                "Heat Input HI = eta * (V * I * 60) / (v * 1000)  [kJ/mm]"
            ),
            failureMechanisms = "Stress-concentration fatigue fracture from sharp un-filleted lathe shoulder radii, 'sugared' oxidized SS316L TIG welds without back-purge, bearing inner-ring crack from oversize shaft fit, stray weld ground current pitting bearings.",
            linkedEquipmentIds = listOf("EQ-N01", "EQ-N02", "EQ-N03"),
            linkedCalcIds = emptyList(),
            sourceCitation = "DOC-MPN-009 §2.8 | ISO 286-2 / AWS D1.1"
        ),
        FundamentalTopic(
            id = "FUND-09",
            title = "Maintenance & Reliability Engineering",
            bengaliTitle = "মেইনটেন্যান্স ও রিলায়েবিলিটি ইঞ্জিনিয়ারিং",
            disciplineGroup = "Reliability & Safety",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Bathtub failure curve, FMEA, 5-Whys/Fishbone RCA, MTBF/MTTR/Availability, OEE, Condition-Based Maintenance (CBM), ISO 20816-3 vibration, IR thermography, lubrication tribology, and 6-step LOTO.",
            washingPlantApplications = "Steam trap ultrasonic/IR surveys, MCC electrical hot-spot thermography (>15°C delta alert), daily boiler/softener checklists, standardized EP2 vs ISO VG 220 lubrication routes.",
            governingFormulas = listOf(
                "Availability A = [MTBF / (MTBF + MTTR)] * 100",
                "OEE = Availability * Performance * Quality",
                "BPFO = (n * f_r / 2) * (1 - (d/D)*cos(alpha))  [Outer Race Bearing Fault]"
            ),
            failureMechanisms = "Run-to-failure destroying expensive trunnion shafts secondarily, grease gun over-pumping blowing seal lips, failed open steam traps wasting massive gas fuel undetected.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-B01", "EQ-E03", "EQ-F02", "EQ-G01"),
            linkedCalcIds = listOf("CALC-04", "CALC-16"),
            sourceCitation = "DOC-MPN-009 §2.9 | ISO 20816-3 / Mobley"
        ),
        FundamentalTopic(
            id = "FUND-10",
            title = "Industrial Refrigeration & Psychrometrics",
            bengaliTitle = "ইন্ডাস্ট্রিয়াল রেফ্রিজারেশন ও সাইক্রোমেট্রি",
            disciplineGroup = "Thermal & Energy",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Vapor-compression refrigeration cycle (evaporator, compressor, condenser, expansion valve), P-h enthalpy cycle & COP, dry/wet bulb temperature, relative humidity, and compressed-air pressure dew point (PDP).",
            washingPlantApplications = "Refrigerated compressed-air dryers dropping compressed air to +3°C PDP, low-temperature enzymatic wash water chillers (10°C–15°C), and induced-draft cooling towers.",
            governingFormulas = listOf(
                "COP = q_e / w_c = (h1 - h4) / (h2 - h1)",
                "q_condenser = q_e + w_c"
            ),
            failureMechanisms = "Choked air-cooled condenser fins causing high-head trips in garment plants, wet air flooding pneumatic washer valves on failed dryer auto-drains, low-flow evaporator freeze-ups.",
            linkedEquipmentIds = listOf("EQ-J02", "EQ-K01", "EQ-K02"),
            linkedCalcIds = emptyList(),
            sourceCitation = "DOC-MPN-009 §2.10 | ASHRAE / ISO 8573-1"
        ),
        FundamentalTopic(
            id = "FUND-11",
            title = "Engineering Mathematics & Applied Mechanics",
            bengaliTitle = "ইঞ্জিনিয়ারিং গণিত ও ফলিত বলবিদ্যা",
            disciplineGroup = "Mechanics & Design",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Dimensional consistency & Buckingham Pi theorem; gauge vs absolute pressure and Kelvin temperature scaling; volumetric vs gravimetric fuel calorific basis conversion; vectors, linear systems, pipe continuity (Q = A·v), piecewise linear interpolation, and ASME PTC 19.1 uncertainty propagation.",
            washingPlantApplications = "Linear enthalpy/saturation interpolation in IAPWS-IF97 steam tables (SteamTableScreen), unit-basis conversion in indirect boiler efficiency (CALC-02: kJ/Nm3 to kJ/kg via gas density) and FD combustion air sizing (CALC-11: Normal 0°C vs ambient FAD), steam/air header velocity checks (CALC-14, CALC-20), and chemical dosing ratio scaling.",
            governingFormulas = listOf(
                "y = y1 + ((x - x1) / (x2 - x1)) * (y2 - y1)  [Piecewise Linear Interpolation]",
                "P_abs = P_gauge + 1.01325 bar | T_K = T_C + 273.15",
                "GCV_mass (kJ/kg) = GCV_vol (kJ/Nm3) / rho_gas (kg/Nm3)",
                "V_FAD = V_Nm3 * (T_amb_K / 273.15)  [Normal-to-Ambient Air Correction]",
                "v = (4 * Q_m3s) / (pi * d_m^2)  [Pipe Flow Velocity Continuity]"
            ),
            failureMechanisms = "Silent gauge-vs-absolute pressure mix-ups (~10% error around 9 bar), substituting volumetric gas GCV (kJ/Nm3) into mass-basis equations (~1.35x L2/L4 inflation), mixing Normal 0°C air vs 30°C ambient FAD (~11% fan undersizing), and curve extrapolation past calibrated table bounds.",
            linkedEquipmentIds = listOf("EQ-D02", "EQ-F02", "EQ-G01"),
            linkedCalcIds = listOf("CALC-02", "CALC-11", "CALC-14"),
            sourceCitation = "DOC-MPN-009 §2.11 | Kreyszig (10th Ed.) / ASME PTC 19.1 / IAPWS-IF97"
        ),
        FundamentalTopic(
            id = "FUND-12",
            title = "Engineering Mechanics (Statics & Dynamics)",
            bengaliTitle = "ইঞ্জিনিয়ারিং মেকানিক্স (স্থিতি ও গতিবিদ্যা)",
            disciplineGroup = "Mechanics & Design",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Newton's Laws of Motion; planar & 3D static equilibrium (Sum(F)=0, Sum(M)=0); Free Body Diagrams; dry Coulomb friction (Ff <= mu·N); capstan/belt wrap friction (T1/T2 <= exp(mu·theta)); centroid & mass moment of inertia (I = Sum(m·r^2)); rotational dynamics (tau = I·alpha, KE = 0.5·I·omega^2) and centripetal acceleration (a_c = omega^2·r).",
            washingPlantApplications = "Two-leg and multi-leg chain-hoist sling angle load multiplication lifting washer drums and motors (EQ-M01, ASME B30.9), centrifugal hydro extractor basket G-force & braking kinetic energy (EQ-B01, CALC-16), V-belt arc-of-contact friction and tight/slack side tensioning (EQ-E04, CALC-15), and concrete anchor-bolt shear design on reversing belly washers (EQ-A01).",
            governingFormulas = listOf(
                "Sum(F) = 0, Sum(M) = 0 | T_sling = W / (2 * cos(theta_vertical))",
                "tau = I * alpha | KE_rot = 0.5 * I * omega^2  [omega = 2*pi*N / 60]",
                "G = (r * omega^2) / g = 0.00001118 * r_cm * N^2  [Centrifugal G-Force]",
                "T1 / T2 <= exp(mu * theta_rad)  [Belt / Capstan Friction Limit]"
            ),
            failureMechanisms = "Overloading rigging slings beyond 90 deg included angle (at 120 deg each leg carries 100% of load weight), sheared washer foundation anchor bolts from reversing drum shock inertia, V-belt slip and glazing from slack tension or tiny pulley wrap angle, and hydro spindle overload from unbalanced wet garments.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-B01", "EQ-E04", "EQ-M01"),
            linkedCalcIds = listOf("CALC-15", "CALC-16"),
            sourceCitation = "DOC-MPN-009 §2.12 | Beer & Johnston (12th Ed.) / ASME B30.9"
        ),
        FundamentalTopic(
            id = "FUND-13",
            title = "Industrial Engineering & Plant Operations",
            bengaliTitle = "ইন্ডাস্ট্রিয়াল ইঞ্জিনিয়ারিং ও প্ল্যান্ট অপারেশনস",
            disciplineGroup = "Reliability & Safety",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Utility and batch capacity balancing; bottleneck & Theory of Constraints scheduling; SMED changeover reduction; TPM 8 pillars; structured maintenance work-order flow; ABC/VED spare criticality classification; ISO 50001:2018 / ISO 50002 energy baseline and Energy Performance Indicators (EnPIs); shift handover and PDCA continuous improvement.",
            washingPlantApplications = "Staggering cold wash-bath fill and direct steam injection peaks across washing bays (EQ-A01) to prevent boiler header collapse (EQ-G01) and dryer starvation (EQ-B03), tracking per-shift Evaporation Ratio (CALC-04) and Specific Steam Consumption (CALC-17), scheduling ETP equalization buffering (EQ-L01), and stocking VED-critical seals/belts/gauges.",
            governingFormulas = listOf(
                "SEC_steam = m_steam_total / m_garment_processed  [kg steam / kg dry garment]",
                "ER = m_steam / m_fuel  [Gas: 11.5–13.5 kg/Nm3 | Cotton Jhut: 3.5–4.5 kg/kg]",
                "Utilization = (Operating_Hours / Scheduled_Hours) * 100",
                "PM_Compliance (%) = (PM_Completed_OnTime / PM_Scheduled) * 100"
            ),
            failureMechanisms = "Simultaneous cold wash startups pulling header pressure down and halting finishing dryers, reactive firefighting exhausting critical spares and extending MTTR, unbuffered dye/bleach batch dumps tripping ETP biological reactors, and unlogged shift handovers masking chronic steam-trap waste.",
            linkedEquipmentIds = listOf("EQ-A01", "EQ-B03", "EQ-G01", "EQ-L01"),
            linkedCalcIds = listOf("CALC-04", "CALC-17", "CALC-18"),
            sourceCitation = "DOC-MPN-009 §2.13 | ISO 50001:2018 / ISO 50002 / Maynard's Handbook"
        ),
        FundamentalTopic(
            id = "FUND-14",
            title = "Power Plant & Steam Utility Engineering",
            bengaliTitle = "পাওয়ার প্ল্যান্ট ও স্টিম ইউটিলিটি ইঞ্জিনিয়ারিং",
            disciplineGroup = "Thermal & Energy",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Rankine & steam utility cycles; fire-tube vs water-tube boiler design; forced-draft vs balanced-draft furnace circuits; indirect heat-loss accounting (ASME PTC 4 / BS 845-1); flue economizer and continuous blowdown flash recovery; water softening & thermal deaeration (Henry's Law O2 stripping at 102–105°C); chimney stack buoyancy draught.",
            washingPlantApplications = "End-to-end plant utility loop: Water Softener (EQ-H01) -> Deaerator (CALC-08) -> Multistage Feed Pump (EQ-D02, CALC-07) -> Economizer (CALC-10, >=120°C gas / >=140°C sulfur floor) -> Gas/Jhut Boiler (EQ-G01, EQ-G02) -> Header PRV/Steam Traps (EQ-F02, EQ-F04), balanced negative draft (-2 to -5 mm WC via ID fan CALC-12), and blowdown heat recovery (CALC-05, CALC-06).",
            governingFormulas = listOf(
                "eta_indirect = 100 - (L1_dry + L2_H2 + L3_moist + L4_air + L5_unburnt + L6_rad)",
                "dP_draft = 353 * H * (1 / T_amb_K - 1 / T_stack_K)  [mm WC Natural Draught]",
                "BD% = [TDS_feed / (TDS_boiler - TDS_feed)] * 100  [ABMA Blowdown Balance]",
                "m_steam_da = m_feed * (h_da - h_makeup) / (h_steam - h_makeup)"
            ),
            failureMechanisms = "Oxygen pitting in smoke tubes when deaerator drops below 102–105°C, cold-end sulfuric acid dew-point corrosion in economizers below 120–140°C, positive furnace blowback on Jhut ID fan trip, and furnace tube overheating from scale/hardness breakthrough.",
            linkedEquipmentIds = listOf("EQ-D02", "EQ-F02", "EQ-F04", "EQ-G01", "EQ-G02", "EQ-H01"),
            linkedCalcIds = listOf("CALC-02", "CALC-05", "CALC-06", "CALC-08", "CALC-10", "CALC-12", "CALC-13"),
            sourceCitation = "DOC-MPN-009 §2.14 | P.K. Nag (4th Ed.) / ASME PTC 4-2013 / BS 845-1"
        ),
        FundamentalTopic(
            id = "FUND-15",
            title = "Instrumentation & Control Fundamentals",
            bengaliTitle = "ইনস্ট্রুমেন্টেশন ও কন্ট্রোল ফান্ডামেন্টালস",
            disciplineGroup = "Reliability & Safety",
            status = FundamentalTopicStatus.VERIFIED_BASELINE,
            theoreticalFoundations = "Primary process sensors: Bourdon/diaphragm pressure gauges, 3-wire Pt100 RTDs (IEC 60751, alpha=0.00385), Type-J/K thermocouples, DP/vortex/mag flow meters, Mobrey float & conductivity LWCOs; 4–20 mA DC live-zero current loops (<3.6 mA wire-break fault detection); On-Off hysteresis vs PID control; NFPA 85 Burner Management Systems (pre-purge, <=4 s FFRT); hardwired safety interlocks & proof calibration.",
            washingPlantApplications = "Boiler dual Mobrey LWCO safety chambers (EQ-G04) wired independently of PLC feedwater control, LEL combustible gas detectors & double-block-and-bleed gas trains (EQ-O02, EQ-G01), 3-wire Pt100 loops controlling wash baths (EQ-A03) & curing ovens (EQ-C01), pneumatic modulating angle-seat steam valves (EQ-F01), and VFD header-pressure PID control.",
            governingFormulas = listOf(
                "I_mA = 4 + 16 * (PV - LRV) / (URV - LRV)  [4–20 mA Live-Zero Linear Scaling]",
                "R_Pt100(T) ≈ 100 * (1 + 0.00385 * T_C)  [100.00 Ω @ 0°C, 138.51 Ω @ 100°C]",
                "u(t) = Kp*e(t) + Ki*integral(e dt) + Kd*(de/dt)  [Ideal PID Loop]",
                "Span Error% = [(Measured - Reference) / (URV - LRV)] * 100"
            ),
            failureMechanisms = "Sludge/scale seizing Mobrey floats in false 'normal water' position causing boiler dry-firing, unauthorized safety-interlock jumper wires defeating LWCO/flame trips, corroded Pt100 terminal resistance (~+2.6°C error per extra 1 Ω) scorching garments in ovens, and VFD EMI noise hunting unshielded 4–20 mA valve signals.",
            linkedEquipmentIds = listOf("EQ-A03", "EQ-C01", "EQ-F01", "EQ-G01", "EQ-G04", "EQ-O02"),
            linkedCalcIds = listOf("CALC-09", "CALC-11"),
            sourceCitation = "DOC-MPN-009 §2.15 | IEC 60751 / ISA-5.1 / NFPA 85 / Liptak"
        )
    )
}

// ---------------------------------------------------------------------------
// 4. UNIFIED OFFLINE SEARCH INDEX HELPER (Static Reference Data Only)
// ---------------------------------------------------------------------------

enum class SearchResultType(val label: String) {
    CHECKLIST("Checklist"),
    REFERENCE("Reference"),
    FUNDAMENTAL("Fundamental")
}

data class CatalogSearchHit(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String,
    val badgeLabel: String
)

object EngineeringCatalogSearch {

    /**
     * Searches static offline catalogs (Checklists, Technical References, Fundamentals)
     * cleanly without touching or exposing isolated user personal tables.
     */
    fun searchStaticCatalogs(rawQuery: String, limit: Int = 30): List<CatalogSearchHit> {
        val tokens = tokenize(rawQuery)
        if (tokens.isEmpty()) return emptyList()

        val hits = mutableListOf<CatalogSearchHit>()

        PlantChecklistCatalog.ALL_CHECKLISTS.forEach { chk ->
            val blob = normalizeAll(
                chk.id,
                chk.title,
                chk.description,
                chk.standardReference,
                chk.items.joinToString(" ") { "${it.category} ${it.text}" }
            )
            if (tokens.all { blob.contains(it) }) {
                hits.add(
                    CatalogSearchHit(
                        id = chk.id,
                        type = SearchResultType.CHECKLIST,
                        title = "${chk.id}: ${chk.title}",
                        subtitle = "${chk.items.size} checks • ${chk.standardReference}",
                        badgeLabel = "CHECKLIST"
                    )
                )
            }
        }

        TechnicalReferenceCatalog.ALL_REFERENCES.forEach { ref ->
            val blob = normalizeAll(
                ref.id,
                ref.title,
                ref.bengaliTitle,
                ref.category,
                ref.authority,
                ref.summary,
                ref.mandatoryRequirements
            )
            if (tokens.all { blob.contains(it) }) {
                hits.add(
                    CatalogSearchHit(
                        id = ref.id,
                        type = SearchResultType.REFERENCE,
                        title = "${ref.id}: ${ref.title}",
                        subtitle = "${ref.bengaliTitle} • ${ref.category}",
                        badgeLabel = "REFERENCE"
                    )
                )
            }
        }

        FundamentalsCatalog.ALL_TOPICS.forEach { topic ->
            val blob = normalizeAll(
                topic.id,
                topic.title,
                topic.bengaliTitle,
                topic.disciplineGroup,
                topic.theoreticalFoundations,
                topic.washingPlantApplications,
                topic.failureMechanisms,
                topic.governingFormulas.joinToString(" "),
                topic.linkedEquipmentIds.joinToString(" "),
                topic.linkedCalcIds.joinToString(" "),
                topic.sourceCitation
            )
            if (tokens.all { blob.contains(it) }) {
                hits.add(
                    CatalogSearchHit(
                        id = topic.id,
                        type = SearchResultType.FUNDAMENTAL,
                        title = "${topic.id}: ${topic.title}",
                        subtitle = "${topic.bengaliTitle} • ${topic.status.label}",
                        badgeLabel = if (topic.status == FundamentalTopicStatus.VERIFIED_BASELINE) "FUNDAMENTALS" else "CONTENT GAP"
                    )
                )
            }
        }

        return hits.take(limit)
    }

    private fun tokenize(raw: String): List<String> {
        val nfc = Normalizer.normalize(raw.trim(), Normalizer.Form.NFC).lowercase()
        if (nfc.isBlank()) return emptyList()
        return nfc.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    private fun normalizeAll(vararg parts: String): String =
        Normalizer.normalize(parts.joinToString(" "), Normalizer.Form.NFC).lowercase()
}
