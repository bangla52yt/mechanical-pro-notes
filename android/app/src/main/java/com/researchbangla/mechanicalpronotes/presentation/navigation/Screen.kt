package com.researchbangla.mechanicalpronotes.presentation.navigation

/**
 * 17 Top-Level Module Navigation Routes & Detail Destinations.
 *
 * Pattern:
 * - 4 Primary destinations on Bottom Navigation Bar (Home, Wash Plant, Boiler, Calculations)
 * - 17 Full module destinations in Modal Navigation Drawer
 * - Parameterized detail routes for equipment reader and calculation runner
 */
sealed class Screen(
    val route: String,
    val title: String,
    val bengaliTitle: String,
    val shortTitle: String = title.split(" ").first()
) {

    // 17 Primary & Secondary Modules
    object Home : Screen("home", "Home Dashboard", "হোম ড্যাশবোর্ড", "Home")
    object Fundamentals : Screen("fundamentals", "Mechanical Fundamentals", "মেকানিক্যাল বেসিকস")
    object WashPlant : Screen("wash_plant", "Wash Plant Equipment", "ওয়াশ প্ল্যান্ট ইকুইপমেন্ট", "Plant")
    object BoilerModule : Screen("boiler_module", "Dedicated Boiler Module", "ইন্ডাস্ট্রিয়াল বয়লার", "Boiler")
    object Pumps : Screen("pumps", "Pumps & Fluid Systems", "পাম্প ও ফ্লুইড সিস্টেম")
    object Transmission : Screen("transmission", "Motors & Transmission", "মোটর ও ট্রান্সমিশন")
    object Steam : Screen("steam", "Steam & Condensate Systems", "স্টিম ও কনডেনসেট")
    object Water : Screen("water", "Water & Chemical Systems", "ওয়াটার ও কেমিক্যাল ট্রিটমেন্ট")
    object Pneumatics : Screen("pneumatics", "Compressed Air & Pneumatics", "কম্প্রেসড এয়ার ও নিউমেটিক্স")
    object CoolingHeating : Screen("cooling_heating", "Cooling & Heating Systems", "কুলিং ও হিটিং সিস্টেম")
    object Etp : Screen("etp", "ETP & Wastewater Systems", "ইটিপি ও বর্জ্যপানি ট্রিটমেন্ট")
    object Maintenance : Screen("maintenance", "Maintenance Management", "মেইনটেন্যান্স ম্যানেজমেন্ট")
    object Checklists : Screen("checklists", "Inspection & Checklists", "ইন্সপেকশন চেকলিস্ট")
    object Calculations : Screen("calculations", "Engineering Calculations", "ইঞ্জিনিয়ারিং ক্যালকুলেশনস", "Calcs")
    object Library : Screen("library", "Technical Reference Library", "টেকনিক্যাল রেফারেন্স লাইব্রেরি")
    object Search : Screen("search", "Offline Search", "অফলাইন সার্চ", "Search")
    object Settings : Screen("settings", "Settings & Local Data", "সেটিংস ও ডাটা ম্যানেজমেন্ট")

    // Detail & Interactive Routes
    object EquipmentDetail : Screen("equipment_detail/{equipmentId}", "Equipment Detail", "ইকুইপমেন্ট বিবরণ") {
        fun createRoute(equipmentId: String): String = "equipment_detail/$equipmentId"
    }

    object CalculationRunner : Screen("calculation_runner/{calcId}", "Calculation Runner", "ক্যালকুলেটর") {
        fun createRoute(calcId: String): String = "calculation_runner/$calcId"
    }

    object Notes : Screen("notes?equipmentId={equipmentId}", "Personal Notes", "ব্যক্তিগত নোটস") {
        fun createRoute(equipmentId: String? = null): String =
            if (equipmentId != null) "notes?equipmentId=$equipmentId" else "notes"
    }

    object Bookmarks : Screen("bookmarks", "Saved Bookmarks", "সংরক্ষিত বুকমার্ক")
    object SteamTable : Screen("steam_table", "IAPWS-IF97 Steam Table", "স্টিম টেবিল")
    object UnitConverter : Screen("unit_converter", "Engineering Unit Converter", "ইউনিট কনভার্টার")

    companion object {
        val bottomBarScreens = listOf(Home, WashPlant, BoilerModule, Calculations)

        val drawerScreens = listOf(
            Home,
            Fundamentals,
            WashPlant,
            BoilerModule,
            Pumps,
            Transmission,
            Steam,
            Water,
            Pneumatics,
            CoolingHeating,
            Etp,
            Maintenance,
            Checklists,
            Calculations,
            UnitConverter,
            Library,
            Search,
            Settings
        )

        val dedicatedFamilyDestinations: List<FamilyModuleDestination> = listOf(
            FamilyModuleDestination(
                familyId = "FAM-C",
                route = "family_c",
                drawerTitle = "Finishing & Special Process",
                screenTitle = "Garment Finishing & Specialized Processing",
                bengaliTitle = "গার্মেন্টস ফিনিশিং ও স্পেশাল প্রসেস"
            ),
            FamilyModuleDestination(
                familyId = "FAM-I",
                route = "family_i",
                drawerTitle = "Chemical Dosing Systems",
                screenTitle = "Chemical Storage, Mixing & Dosing Systems",
                bengaliTitle = "কেমিক্যাল মিক্সিং ও ডোজিং সিস্টেম"
            ),
            FamilyModuleDestination(
                familyId = "FAM-M",
                route = "family_m",
                drawerTitle = "Material Handling & Hoists",
                screenTitle = "Material Handling & Overhead Lifting",
                bengaliTitle = "ম্যাটেরিয়াল হ্যান্ডলিং ও ওভারহেড হোয়েস্ট"
            ),
            FamilyModuleDestination(
                familyId = "FAM-N",
                route = "family_n",
                drawerTitle = "Workshop & Fabrication",
                screenTitle = "Maintenance Workshop & Fabrication Machines",
                bengaliTitle = "মেইনটেন্যান্স ওয়ার্কশপ ও মেশিন শপ"
            ),
            FamilyModuleDestination(
                familyId = "FAM-O",
                route = "family_o",
                drawerTitle = "Plant Safety & Fire Systems",
                screenTitle = "Plant Safety & Mechanical Protection Systems",
                bengaliTitle = "প্ল্যান্ট সেফটি ও ফায়ার প্রোটেকশন"
            ),
            FamilyModuleDestination(
                familyId = "FAM-P",
                route = "family_p",
                drawerTitle = "Ventilation & Exhaust",
                screenTitle = "Plant Ventilation, Exhaust & Lint Extraction",
                bengaliTitle = "ভেন্টিলেশন, এগজস্ট ও লিন্ট এক্সট্রাকশন"
            )
        )

        fun findFamilyDestinationById(familyId: String): FamilyModuleDestination? =
            dedicatedFamilyDestinations.firstOrNull { it.familyId.equals(familyId.trim(), ignoreCase = true) }
    }
}

data class FamilyModuleDestination(
    val familyId: String,
    val route: String,
    val drawerTitle: String,
    val screenTitle: String,
    val bengaliTitle: String
)
