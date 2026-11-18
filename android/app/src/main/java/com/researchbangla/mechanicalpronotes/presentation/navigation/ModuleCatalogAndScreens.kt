package com.researchbangla.mechanicalpronotes.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.catalog.CatalogChecklist
import com.researchbangla.mechanicalpronotes.domain.catalog.EngineeringReferenceItem
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalTopic
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalTopicStatus
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalsCatalog
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.catalog.TechnicalReferenceCatalog
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.SystemFamily
import com.researchbangla.mechanicalpronotes.presentation.components.ContentDepthBadge
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.EquipmentCardItem
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentListUiState
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticWarningColors

data class SecondaryModuleConfig(
    val screenRoute: String,
    val mappedFamilyId: String?,
    val modulePurpose: String,
    val safetyTitle: String?,
    val safetyMessage: String?,
    val isStatutorySafety: Boolean = false,
    val linkedCalcIds: List<String> = emptyList(),
    val plannedCalculatorMessage: String? = null,
    val linkedReferenceIds: List<String> = emptyList(),
    val linkedFundamentalIds: List<String> = emptyList(),
    val showChecklistsCatalog: Boolean = false,
    val showSteamTableShortcut: Boolean = false,
    val showNotesShortcut: Boolean = false,
    val showUnitConverterShortcut: Boolean = false,
    val showAllCalculationsShortcut: Boolean = false
)

data class ResolvedSecondaryModule(
    val config: SecondaryModuleConfig,
    val family: SystemFamily?,
    val equipment: List<Equipment>,
    val calculations: List<CalculationDefinition>,
    val references: List<EngineeringReferenceItem>,
    val fundamentals: List<FundamentalTopic>,
    val checklists: List<CatalogChecklist>
)

data class ResolvedFamilyDestination(
    val destination: FamilyModuleDestination,
    val family: SystemFamily?,
    val displayTitle: String,
    val displayBengaliTitle: String,
    val equipment: List<Equipment>,
    val references: List<EngineeringReferenceItem>,
    val fundamentals: List<FundamentalTopic>
)

object SecondaryModuleResolver {

    fun getModuleConfig(screen: Screen): SecondaryModuleConfig = when (screen) {
        Screen.Pumps -> SecondaryModuleConfig(
            screenRoute = Screen.Pumps.route,
            mappedFamilyId = "FAM-D",
            modulePurpose = "Centrifugal water, multistage boiler feedwater, AODD diaphragm, chemical metering, submersible effluent, and fuel transfer pumping systems.",
            safetyTitle = "Pumps & Fluid Systems — Field Safety & LOTO",
            safetyMessage = "Isolate suction and discharge valves, depressurize hot boiler feed casings before opening mechanical seals, and lock out motor starters.",
            linkedCalcIds = listOf("CALC-07"),
            linkedReferenceIds = listOf("REF-10"),
            linkedFundamentalIds = listOf("FUND-02")
        )
        Screen.Transmission -> SecondaryModuleConfig(
            screenRoute = Screen.Transmission.route,
            mappedFamilyId = "FAM-E",
            modulePurpose = "Induction motors, helical/worm gearboxes, spherical roller bearings, V-belt drives, couplings, and synchronous timing transmissions.",
            safetyTitle = "Motors & Transmission — Guarding & LOTO",
            safetyMessage = "Lock out main MCC disconnect before removing belt/coupling guards or servicing rotating shafts (Bangladesh Labour Rules 2015).",
            linkedCalcIds = listOf("CALC-15"),
            linkedReferenceIds = listOf("REF-03", "REF-06"),
            linkedFundamentalIds = listOf("FUND-05", "FUND-06")
        )
        Screen.Steam -> SecondaryModuleConfig(
            screenRoute = Screen.Steam.route,
            mappedFamilyId = "FAM-F",
            modulePurpose = "Steam angle-seat valves, steam traps, pressure reducing stations (PRVs), flash vessels, drain heat recovery PHEs, and thermal pipe cladding.",
            safetyTitle = "Steam & Condensate — Pressurized Thermal Isolation",
            safetyMessage = "Apply double-block-and-bleed isolation before opening traps, PRV diaphragms, or strainers, and warm headers slowly via bypass to prevent water hammer.",
            isStatutorySafety = true,
            linkedCalcIds = listOf("CALC-14", "CALC-06"),
            linkedReferenceIds = listOf("REF-05"),
            linkedFundamentalIds = listOf("FUND-01", "FUND-03"),
            showSteamTableShortcut = true
        )
        Screen.Water -> SecondaryModuleConfig(
            screenRoute = Screen.Water.route,
            mappedFamilyId = "FAM-H",
            modulePurpose = "Sodium cation-exchange water softeners, sand/carbon filtration, industrial RO plants, hydro-pneumatic boosters, and boiler feedwater treatment.",
            safetyTitle = "Water & Chemical Treatment — Vessel Isolation",
            safetyMessage = "Depressurize softener/filter vessels before opening valve manifolds and wear chemical splash PPE when handling brine or water-treatment chemicals.",
            linkedCalcIds = listOf("CALC-19", "CALC-05", "CALC-08"),
            linkedReferenceIds = listOf("REF-08")
        )
        Screen.Pneumatics -> SecondaryModuleConfig(
            screenRoute = Screen.Pneumatics.route,
            mappedFamilyId = "FAM-J",
            modulePurpose = "Oil-injected rotary screw compressors, refrigerated compressed air dryers, ASME air receivers, and coalescing filtration trains.",
            safetyTitle = "Compressed Air & Pneumatics — Stored Energy Bleed",
            safetyMessage = "Bleed air receivers and manifold accumulators to 0.0 bar(g) and lock out compressor starters before changing separator elements or actuators.",
            linkedCalcIds = listOf("CALC-20"),
            linkedReferenceIds = listOf("REF-07", "REF-11"),
            linkedFundamentalIds = listOf("FUND-10")
        )
        Screen.CoolingHeating -> SecondaryModuleConfig(
            screenRoute = Screen.CoolingHeating.route,
            mappedFamilyId = "FAM-K",
            modulePurpose = "Induced-draft cooling towers and industrial water chillers supporting wash-plant process temperature control and auxiliary heat rejection.",
            safetyTitle = "Cooling & Heating Systems — Electrical & Refrigerant Safety",
            safetyMessage = "Lock out cooling tower fan motors before entering fan stacks and use certified refrigerant recovery procedures when servicing chiller circuits.",
            linkedCalcIds = emptyList(),
            plannedCalculatorMessage = "Dedicated cooling tower (range, approach, evaporation loss) and water chiller (TR / COP) calculators are planned and not yet implemented in the CALC-01..20 catalog.",
            linkedReferenceIds = emptyList(),
            linkedFundamentalIds = listOf("FUND-03", "FUND-10"),
            showUnitConverterShortcut = true
        )
        Screen.Etp -> SecondaryModuleConfig(
            screenRoute = Screen.Etp.route,
            mappedFamilyId = "FAM-L",
            modulePurpose = "Effluent Treatment Plant (ETP) machinery including DAF units, Roots aeration blowers, filter presses, progressive cavity sludge pumps, and fine screens.",
            safetyTitle = "ETP & Wastewater — Confined Space & ECR Compliance",
            safetyMessage = "Confined-space hazard: equalize/sludge sumps can accumulate H2S and low O2. Perform gas testing, forced ventilation, and permit isolation before entry.",
            isStatutorySafety = true,
            linkedCalcIds = emptyList(),
            plannedCalculatorMessage = "Dedicated ETP calculators (BOD/COD organic loading, hydraulic retention time, chemical coagulant dosing, and sludge dewatering) are planned and not yet implemented in CALC-01..20.",
            linkedReferenceIds = listOf("REF-12")
        )
        else -> SecondaryModuleConfig(
            screenRoute = Screen.Maintenance.route,
            mappedFamilyId = null,
            modulePurpose = "Preventive and predictive maintenance execution, LOTO checklists, machinery vibration/safety standards, workshop fabrication links, and personal field logs.",
            safetyTitle = "Maintenance Management — 6-Step LOTO Discipline",
            safetyMessage = "Isolate all energy sources (electrical, steam, compressed air, hydraulic, gravity) and verify zero-energy state before inspecting rotating or pressurized equipment.",
            linkedCalcIds = emptyList(),
            plannedCalculatorMessage = "Dedicated reliability calculators (MTBF/Availability and ISO 281 L10h bearing life) are planned; open Plant Calculators for all 20 verified thermal and machinery tools.",
            linkedReferenceIds = listOf("REF-03", "REF-06", "REF-09"),
            linkedFundamentalIds = listOf("FUND-08", "FUND-09"),
            showChecklistsCatalog = true,
            showNotesShortcut = true,
            showAllCalculationsShortcut = true
        )
    }

    fun resolveModule(
        screen: Screen,
        allEquipment: List<Equipment>,
        systemFamilies: List<SystemFamily>,
        allCalculations: List<CalculationDefinition> = CalculationSpecifications.ALL_CALCULATIONS
    ): ResolvedSecondaryModule {
        val config = getModuleConfig(screen)
        val family = config.mappedFamilyId?.let { targetId ->
            systemFamilies.firstOrNull { it.id.equals(targetId, ignoreCase = true) }
        }
        val equipment = if (config.mappedFamilyId != null) {
            allEquipment.filter { it.familyId.equals(config.mappedFamilyId, ignoreCase = true) }
        } else {
            emptyList()
        }
        val calcLookupSource = allCalculations.ifEmpty { CalculationSpecifications.ALL_CALCULATIONS }
        val calculations = config.linkedCalcIds.mapNotNull { calcId ->
            calcLookupSource.firstOrNull { it.id.equals(calcId, ignoreCase = true) }
                ?: CalculationSpecifications.getCalculationById(calcId)
        }
        val references = config.linkedReferenceIds.mapNotNull { refId ->
            TechnicalReferenceCatalog.ALL_REFERENCES.firstOrNull { it.id.equals(refId, ignoreCase = true) }
        }
        val fundamentals = config.linkedFundamentalIds.mapNotNull { fundId ->
            FundamentalsCatalog.ALL_TOPICS.firstOrNull { it.id.equals(fundId, ignoreCase = true) }
        }
        val checklists = if (config.showChecklistsCatalog) {
            PlantChecklistCatalog.ALL_CHECKLISTS
        } else {
            emptyList()
        }

        return ResolvedSecondaryModule(
            config = config,
            family = family,
            equipment = equipment,
            calculations = calculations,
            references = references,
            fundamentals = fundamentals,
            checklists = checklists
        )
    }

    private val familyReferenceIds: Map<String, List<String>> = mapOf(
        "FAM-C" to listOf("REF-03"),
        "FAM-I" to emptyList(),
        "FAM-M" to listOf("REF-03"),
        "FAM-N" to listOf("REF-03"),
        "FAM-O" to listOf("REF-03", "REF-10"),
        "FAM-P" to listOf("REF-03")
    )

    fun resolveFamilyDestination(
        destination: FamilyModuleDestination,
        allEquipment: List<Equipment>,
        systemFamilies: List<SystemFamily>
    ): ResolvedFamilyDestination {
        val family = systemFamilies.firstOrNull { it.id.equals(destination.familyId, ignoreCase = true) }
        val equipment = allEquipment.filter { it.familyId.equals(destination.familyId, ignoreCase = true) }
        val familyPrefix = "EQ-${destination.familyId.removePrefix("FAM-")}"
        val equipmentIds = equipment.map { it.id.uppercase() }.toSet()

        val fundamentals = FundamentalsCatalog.ALL_TOPICS.filter { topic ->
            topic.linkedEquipmentIds.any { eqId ->
                val upper = eqId.uppercase()
                upper in equipmentIds || upper.startsWith(familyPrefix, ignoreCase = true)
            }
        }
        val refIds = familyReferenceIds[destination.familyId.uppercase()].orEmpty()
        val references = refIds.mapNotNull { refId ->
            TechnicalReferenceCatalog.ALL_REFERENCES.firstOrNull { it.id.equals(refId, ignoreCase = true) }
        }

        return ResolvedFamilyDestination(
            destination = destination,
            family = family,
            displayTitle = family?.name ?: destination.screenTitle,
            displayBengaliTitle = family?.bengaliName ?: destination.bengaliTitle,
            equipment = equipment,
            references = references,
            fundamentals = fundamentals
        )
    }
}

@Composable
fun SecondaryModuleScreen(
    screen: Screen,
    equipmentListState: EquipmentListUiState,
    calculations: List<CalculationDefinition>,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onBrowseFamilyInWashPlant: (String?) -> Unit,
    onOpenCalculator: (String) -> Unit,
    onOpenSteamTable: () -> Unit,
    onOpenUnitConverter: () -> Unit,
    onNavigateToReference: (String) -> Unit,
    onNavigateToFundamental: (String) -> Unit,
    onNavigateToChecklist: (String) -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToCalculations: () -> Unit,
    onNavigateToFamilyDestination: (String) -> Unit
) {
    val resolved = remember(
        screen,
        equipmentListState.allEquipment,
        equipmentListState.systemFamilies,
        calculations
    ) {
        SecondaryModuleResolver.resolveModule(
            screen = screen,
            allEquipment = equipmentListState.allEquipment,
            systemFamilies = equipmentListState.systemFamilies,
            allCalculations = calculations
        )
    }

    SecondaryModuleContent(
        screen = screen,
        resolved = resolved,
        isLoading = equipmentListState.isLoading && equipmentListState.allEquipment.isEmpty(),
        onOpenDrawer = onOpenDrawer,
        onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
        onBrowseFamilyInWashPlant = onBrowseFamilyInWashPlant,
        onOpenCalculator = onOpenCalculator,
        onOpenSteamTable = onOpenSteamTable,
        onOpenUnitConverter = onOpenUnitConverter,
        onNavigateToReference = onNavigateToReference,
        onNavigateToFundamental = onNavigateToFundamental,
        onNavigateToChecklist = onNavigateToChecklist,
        onNavigateToNotes = onNavigateToNotes,
        onNavigateToCalculations = onNavigateToCalculations,
        onNavigateToFamilyDestination = onNavigateToFamilyDestination
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryModuleContent(
    screen: Screen,
    resolved: ResolvedSecondaryModule,
    isLoading: Boolean = false,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit = {},
    onBrowseFamilyInWashPlant: (String?) -> Unit = {},
    onOpenCalculator: (String) -> Unit = {},
    onOpenSteamTable: () -> Unit = {},
    onOpenUnitConverter: () -> Unit = {},
    onNavigateToReference: (String) -> Unit = {},
    onNavigateToFundamental: (String) -> Unit = {},
    onNavigateToChecklist: (String) -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToCalculations: () -> Unit = {},
    onNavigateToFamilyDestination: (String) -> Unit = {}
) {
    val config = resolved.config

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = screen.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = screen.bengaliTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Module Purpose Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (resolved.family != null) {
                            "${resolved.family.id}: ${resolved.family.name}"
                        } else {
                            screen.title
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = config.modulePurpose,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 2. Module Safety & Statutory Callout
            if (config.safetyTitle != null && config.safetyMessage != null) {
                SafetyWarningCard(
                    title = config.safetyTitle,
                    message = config.safetyMessage,
                    isStatutory = config.isStatutorySafety
                )
            }

            // 3. Verified Calculators OR Explicit Planned/Unavailable Status
            Text(
                text = "Module Calculators",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )

            if (resolved.calculations.isNotEmpty()) {
                resolved.calculations.forEach { calc ->
                    ModuleCalculationLinkCard(
                        calc = calc,
                        onClick = { onOpenCalculator(calc.id) }
                    )
                }
            }

            if (config.plannedCalculatorMessage != null) {
                PlannedCapabilityCard(
                    title = "No Dedicated ${screen.title} Calculator Available Yet",
                    message = config.plannedCalculatorMessage
                )
            }

            if (config.showSteamTableShortcut) {
                OutlinedButton(
                    onClick = onOpenSteamTable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open IAPWS-IF97 Saturated Steam Properties")
                }
            }

            if (config.showUnitConverterShortcut) {
                OutlinedButton(
                    onClick = onOpenUnitConverter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Engineering Unit Converter (Temp / Power / Energy)")
                }
            }

            if (config.showAllCalculationsShortcut) {
                OutlinedButton(
                    onClick = onNavigateToCalculations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse All 20 Plant Calculators")
                }
            }

            // 4. Live Associated Equipment Family Records (when mapped to a SystemFamily)
            if (config.mappedFamilyId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Equipment in ${config.mappedFamilyId} (${resolved.equipment.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() }
                    )
                    TextButton(
                        onClick = { onBrowseFamilyInWashPlant(config.mappedFamilyId) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Filter Directory")
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    resolved.equipment.isEmpty() -> {
                        EmptyStateCard(
                            title = "No Equipment Records Found",
                            message = "No equipment records are currently loaded for family ${config.mappedFamilyId} in the offline catalog."
                        )
                    }
                    else -> {
                        resolved.equipment.forEach { eq ->
                            EquipmentCardItem(
                                equipment = eq,
                                onClick = { onNavigateToEquipmentDetail(eq.id) }
                            )
                        }
                    }
                }
            }

            // 5. Maintenance Checklists & Field Workflows (MOD-12 Maintenance)
            if (resolved.checklists.isNotEmpty()) {
                Text(
                    text = "Inspection & Maintenance Checklists (${resolved.checklists.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                resolved.checklists.forEach { chk ->
                    ModuleCatalogLinkCard(
                        badge = chk.id,
                        title = chk.title,
                        subtitle = "${chk.items.size} items • ${chk.standardReference}",
                        actionLabel = "Open checklist ${chk.title}",
                        onClick = { onNavigateToChecklist(chk.id) }
                    )
                }
            }

            if (config.showNotesShortcut) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToNotes,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Field Notes", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToFamilyDestination("FAM-N") },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Workshop (FAM-N)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // 6. Verified Technical References & Fundamentals
            if (resolved.references.isNotEmpty() || resolved.fundamentals.isNotEmpty()) {
                Text(
                    text = "Standards & Engineering Fundamentals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                resolved.references.forEach { ref ->
                    ModuleCatalogLinkCard(
                        badge = ref.id,
                        title = ref.title,
                        subtitle = "${ref.category} • ${ref.authority}",
                        actionLabel = "Open technical reference ${ref.title}",
                        onClick = { onNavigateToReference(ref.id) }
                    )
                }
                resolved.fundamentals.forEach { topic ->
                    ModuleCatalogLinkCard(
                        badge = if (topic.status == FundamentalTopicStatus.VERIFIED_BASELINE) topic.id else "${topic.id} • PLANNED",
                        title = topic.title,
                        subtitle = "${topic.disciplineGroup} • ${topic.status.label}",
                        actionLabel = "Open engineering fundamental ${topic.title}",
                        onClick = { onNavigateToFundamental(topic.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EquipmentFamilyDestinationScreen(
    destination: FamilyModuleDestination,
    equipmentListState: EquipmentListUiState,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onOpenInFullDirectory: (String) -> Unit,
    onNavigateToReference: (String) -> Unit,
    onNavigateToFundamental: (String) -> Unit
) {
    val resolved = remember(
        destination,
        equipmentListState.allEquipment,
        equipmentListState.systemFamilies
    ) {
        SecondaryModuleResolver.resolveFamilyDestination(
            destination = destination,
            allEquipment = equipmentListState.allEquipment,
            systemFamilies = equipmentListState.systemFamilies
        )
    }

    EquipmentFamilyDestinationContent(
        resolved = resolved,
        isLoading = equipmentListState.isLoading && equipmentListState.allEquipment.isEmpty(),
        onOpenDrawer = onOpenDrawer,
        onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
        onOpenInFullDirectory = onOpenInFullDirectory,
        onNavigateToReference = onNavigateToReference,
        onNavigateToFundamental = onNavigateToFundamental
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFamilyDestinationContent(
    resolved: ResolvedFamilyDestination,
    isLoading: Boolean = false,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit = {},
    onOpenInFullDirectory: (String) -> Unit = {},
    onNavigateToReference: (String) -> Unit = {},
    onNavigateToFundamental: (String) -> Unit = {}
) {
    val familyId = resolved.destination.familyId
    val fullCount = resolved.equipment.count { it.contentDepth == ContentDepth.FULL_BLUEPRINT }
    val partialCount = resolved.equipment.count { it.contentDepth == ContentDepth.PARTIAL_BLUEPRINT }
    val inventoryCount = resolved.equipment.count { it.contentDepth == ContentDepth.INVENTORY_ONLY }
    val researchCount = resolved.equipment.count { it.contentDepth == ContentDepth.RESEARCH_PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${resolved.destination.drawerTitle} ($familyId)",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = resolved.displayBengaliTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = resolved.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = resolved.displayBengaliTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Catalog Classification: ${resolved.equipment.size} assets " +
                            "(Full: $fullCount • Partial: $partialCount • Inventory Only: $inventoryCount • Research Pending: $researchCount)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (resolved.equipment.isNotEmpty() && fullCount == 0) {
                PlannedCapabilityCard(
                    title = "Baseline Catalog Depth Notice",
                    message = "Records in $familyId currently carry INVENTORY_ONLY / RESEARCH_PENDING baseline sections rather than full 21-section engineering manuals."
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equipment Records (${resolved.equipment.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                TextButton(
                    onClick = { onOpenInFullDirectory(familyId) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Open in All Equipment")
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                resolved.equipment.isEmpty() -> {
                    EmptyStateCard(
                        title = "No Equipment Records in $familyId",
                        message = "No plant equipment entries were found for $familyId (${resolved.displayTitle}) in the current offline catalog."
                    )
                }
                else -> {
                    resolved.equipment.forEach { equipment ->
                        EquipmentCardItem(
                            equipment = equipment,
                            onClick = { onNavigateToEquipmentDetail(equipment.id) }
                        )
                    }
                }
            }

            if (resolved.references.isNotEmpty() || resolved.fundamentals.isNotEmpty()) {
                Text(
                    text = "Related Standards & Fundamentals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                resolved.references.forEach { ref ->
                    ModuleCatalogLinkCard(
                        badge = ref.id,
                        title = ref.title,
                        subtitle = "${ref.category} • ${ref.authority}",
                        actionLabel = "Open technical reference ${ref.title}",
                        onClick = { onNavigateToReference(ref.id) }
                    )
                }
                resolved.fundamentals.forEach { topic ->
                    ModuleCatalogLinkCard(
                        badge = if (topic.status == FundamentalTopicStatus.VERIFIED_BASELINE) topic.id else "${topic.id} • PLANNED",
                        title = topic.title,
                        subtitle = "${topic.disciplineGroup} • ${topic.status.label}",
                        actionLabel = "Open engineering fundamental ${topic.title}",
                        onClick = { onNavigateToFundamental(topic.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCalculationLinkCard(
    calc: CalculationDefinition,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val statusColors = if (calc.status == CalculationStatus.IMPLEMENTED) {
        semanticPositiveColors(isDark)
    } else {
        semanticWarningColors(isDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${calc.title} calculator",
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${calc.id} • ${calc.category}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColors.container
                ) {
                    Text(
                        text = if (calc.status == CalculationStatus.IMPLEMENTED) "IMPLEMENTED" else calc.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColors.content,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = calc.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = calc.bengaliTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ref: ${calc.engineeringReference}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun PlannedCapabilityCard(
    title: String,
    message: String
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val warnColors = semanticWarningColors(isDark)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = warnColors.container.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, warnColors.content.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = warnColors.content,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = warnColors.content
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ModuleCatalogLinkCard(
    badge: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = actionLabel,
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private object ModuleScreensPreviewData {
    val sampleCoolingEq = listOf(
        Equipment(
            id = "EQ-K01",
            categoryId = "CAT-K1",
            familyId = "FAM-K",
            name = "Induced Draft Counter-Flow Cooling Tower",
            bengaliName = "ইন্ডিউসড ড্রাফট কাউন্টার-ফ্লো কুলিং টাওয়ার",
            contentDepth = ContentDepth.INVENTORY_ONLY,
            criticalityTier = CriticalityTier.UTILITY_SUPPORT
        ),
        Equipment(
            id = "EQ-K02",
            categoryId = "CAT-K1",
            familyId = "FAM-K",
            name = "Industrial Water Chiller (Air-Cooled / Water-Cooled)",
            bengaliName = "ইন্ডাস্ট্রিয়াল ওয়াটার চিলার",
            contentDepth = ContentDepth.RESEARCH_PENDING,
            criticalityTier = CriticalityTier.UTILITY_SUPPORT
        )
    )
    val sampleCoolingFamily = SystemFamily(
        id = "FAM-K",
        code = "COOLING_REFRIGERATION",
        name = "Cooling & Industrial Refrigeration Systems",
        bengaliName = "কুলিং টাওয়ার ও ইন্ডাস্ট্রিয়াল চিলার",
        iconKey = "cooling",
        sortOrder = 11
    )
}

@Preview(name = "SecondaryModule - CoolingHeating Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun SecondaryModuleCoolingLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SecondaryModuleContent(
            screen = Screen.CoolingHeating,
            resolved = SecondaryModuleResolver.resolveModule(
                screen = Screen.CoolingHeating,
                allEquipment = ModuleScreensPreviewData.sampleCoolingEq,
                systemFamilies = listOf(ModuleScreensPreviewData.sampleCoolingFamily)
            ),
            onOpenDrawer = {}
        )
    }
}

@Preview(name = "FamilyDestination - FAM-O Empty Dark", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun FamilyDestinationEmptyDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        val dest = Screen.findFamilyDestinationById("FAM-O")!!
        EquipmentFamilyDestinationContent(
            resolved = SecondaryModuleResolver.resolveFamilyDestination(
                destination = dest,
                allEquipment = emptyList(),
                systemFamilies = emptyList()
            ),
            onOpenDrawer = {}
        )
    }
}
