package com.researchbangla.mechanicalpronotes.presentation.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.R
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistProgressSummary
import com.researchbangla.mechanicalpronotes.presentation.checklists.PlantChecklist
import com.researchbangla.mechanicalpronotes.presentation.components.ContentDepthBadge
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.components.SimpleMarkdownText
import com.researchbangla.mechanicalpronotes.presentation.components.ZoomableSchematicCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@Composable
fun EquipmentDetailScreen(
    equipmentId: String,
    viewModel: EquipmentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNoteEditor: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    relevantChecklists: List<PlantChecklist> = emptyList(),
    checklistSummaries: Map<String, ChecklistProgressSummary> = emptyMap(),
    onOpenChecklistForEquipment: (String, String?) -> Unit = { _, _ -> },
    onCreateCustomChecklistForEquipment: (String) -> Unit = {}
) {
    LaunchedEffect(equipmentId) {
        viewModel.loadEquipmentDetail(equipmentId)
    }

    val state by viewModel.detailState.collectAsState()
    EquipmentDetailContent(
        equipmentId = equipmentId,
        state = state,
        onNavigateBack = onNavigateBack,
        onToggleBookmark = { eq -> viewModel.toggleBookmark(eq) },
        onSelectTab = { index -> viewModel.selectDetailTab(index) },
        onNavigateToNoteEditor = onNavigateToNoteEditor,
        onNavigateToCalculation = onNavigateToCalculation,
        relevantChecklists = relevantChecklists,
        checklistSummaries = checklistSummaries,
        onOpenChecklistForEquipment = onOpenChecklistForEquipment,
        onCreateCustomChecklistForEquipment = onCreateCustomChecklistForEquipment
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailContent(
    equipmentId: String,
    state: EquipmentDetailUiState,
    onNavigateBack: () -> Unit,
    onToggleBookmark: (Equipment) -> Unit = {},
    onSelectTab: (Int) -> Unit = {},
    onNavigateToNoteEditor: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    relevantChecklists: List<PlantChecklist> = emptyList(),
    checklistSummaries: Map<String, ChecklistProgressSummary> = emptyMap(),
    onOpenChecklistForEquipment: (String, String?) -> Unit = { _, _ -> },
    onCreateCustomChecklistForEquipment: (String) -> Unit = {}
) {
    val equipment = state.equipment

    val tabTitles = listOf(
        "Overview & Specs",
        "Components",
        "Operation",
        "Maintenance",
        "Troubles & Safety"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = equipment?.id ?: equipmentId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (equipment != null) {
                            Text(
                                text = equipment.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (equipment != null) {
                        IconButton(onClick = { onToggleBookmark(equipment) }) {
                            Icon(
                                imageVector = if (state.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (state.isBookmarked) {
                                    "Remove bookmark for ${equipment.name}"
                                } else {
                                    "Bookmark ${equipment.name}"
                                },
                                tint = if (state.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (equipment != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = equipment.id,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ContentDepthBadge(depth = equipment.contentDepth)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = equipment.bengaliName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Family: ${equipment.familyId} | Tier: ${equipment.criticalityTier.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                        if (equipment.aliases.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Also known as: ${equipment.aliases.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { onSelectTab(index) },
                            text = { Text(title) }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (state.selectedTab) {
                        0 -> OverviewSpecsTab(equipment, onNavigateToCalculation)
                        1 -> ComponentsTab(equipment)
                        2 -> OperationParametersTab(equipment)
                        3 -> MaintenanceLubricationTab(
                            equipment = equipment,
                            relevantChecklists = relevantChecklists,
                            checklistSummaries = checklistSummaries,
                            onOpenChecklistForEquipment = onOpenChecklistForEquipment,
                            onCreateCustomChecklistForEquipment = onCreateCustomChecklistForEquipment
                        )
                        4 -> TroubleshootingSafetyTab(
                            equipment = equipment,
                            userNotes = state.userNotes,
                            onAddNote = { onNavigateToNoteEditor(equipment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewSpecsTab(
    equipment: Equipment,
    onNavigateToCalculation: (String) -> Unit
) {
    val overviewSections = equipment.knowledgeSections.filter {
        it.sectionNumber in 1..5 || it.sectionNumber in listOf(19, 20)
    }
    val relatedCalcs = getRelatedCalculationsForEquipment(equipment)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (equipment.contentDepth == ContentDepth.RESEARCH_PENDING ||
            equipment.contentDepth == ContentDepth.INVENTORY_ONLY
        ) {
            SafetyWarningCard(
                title = "Classification Transparency: ${equipment.contentDepth.displayName}",
                message = "Only verified baseline identity, applicable regulatory guidance, and populated tables are displayed for ${equipment.id}. Unverified machine-specific OEM tolerances are not fabricated.",
                isStatutory = false
            )
        }

        DetailSectionCard(title = "Asset Identity & Baseline Specs") {
            SpecRow("Equipment ID", equipment.id)
            SpecRow("Category ID", equipment.categoryId)
            SpecRow("Content Status", equipment.contentDepth.displayName)
            equipment.capacityRange?.let { SpecRow("Capacity Range", it) }
            equipment.operatingTemperature?.let { SpecRow("Operating Temp", it) }
            equipment.primaryDriveType?.let { SpecRow("Drive Type", it) }
            equipment.lubricationRegime?.let { SpecRow("Lubrication", it) }
            equipment.statutoryRequirement?.let { SpecRow("Statutory Mandate", it) }
        }

        DetailSectionCard(title = "Linked Engineering Calculators") {
            if (relatedCalcs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    relatedCalcs.forEach { (calcId, label) ->
                        OutlinedButton(
                            onClick = { onNavigateToCalculation(calcId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$calcId — $label", modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                Text(
                    text = "No dedicated calculator is currently mapped to ${equipment.id} (${equipment.name}) in the verified 20-module registry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (equipment.subtypes.isNotEmpty()) {
            DetailSectionCard(title = "Machine Subtypes & Variants (${equipment.subtypes.size})") {
                equipment.subtypes.forEach { subtype ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "${subtype.name} (${subtype.bengaliName})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtype.distinguishingFeatures,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Typical Capacity: ${subtype.typicalCapacity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }

        overviewSections.forEach { section ->
            KnowledgeSectionCard(section)
        }

        if (overviewSections.none { it.sectionNumber == 20 }) {
            DetailSectionCard(title = "20. Applicable Standards & Bangladesh Regulations") {
                Text(
                    text = if (equipment.familyId == "FAM-G") {
                        "Governed under The Boilers Act, 2022 (Act No. 06 of 2022) & Bangladesh Boiler Rules, 2025. Annual statutory renewal requires OCIB internal/external visual examination and live safety-valve steam popping test; hydraulic testing (1.25x–1.5x MAWP) applies on initial registration, after Section 24 structural weld repair, or when ordered by the Inspector."
                    } else {
                        equipment.statutoryRequirement
                            ?: "Operates under Bangladesh Labour Act, 2006 (Amended 2013) & Labour Rules, 2015 plant mechanical safety requirements."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ComponentsTab(equipment: Equipment) {
    val componentSections = equipment.knowledgeSections.filter { it.sectionNumber in 6..8 }
    val schematicInfo = getSchematicForEquipment(equipment)

    if (equipment.components.isEmpty() && componentSections.isEmpty() && schematicInfo == null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            EmptyStateCard(
                title = "No Components Populated",
                message = "No component breakdown currently populated for ${equipment.id} (${equipment.contentDepth.displayName})."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (schematicInfo != null) {
                item {
                    ZoomableSchematicCard(
                        title = schematicInfo.title,
                        subtitle = schematicInfo.subtitle,
                        drawableRes = schematicInfo.drawableRes,
                        legendText = schematicInfo.legend
                    )
                }
            } else {
                item {
                    DetailSectionCard(title = "Offline Technical Schematic") {
                        Text(
                            text = "No offline vector schematic is currently available for ${equipment.id} (${equipment.name}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (equipment.components.isNotEmpty()) {
                item {
                    Text(
                        text = "Sub-assemblies & Mechanical Components (${equipment.components.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(equipment.components, key = { it.id }) { comp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = comp.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = comp.wearTier.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (comp.bengaliName.isNotBlank()) {
                                Text(
                                    text = comp.bengaliName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Material: ${comp.materialSpecification}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = comp.primaryFunction,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (comp.maintenanceMethod.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Maintenance: ${comp.maintenanceMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            items(componentSections, key = { "sec-${it.sectionNumber}" }) { section ->
                KnowledgeSectionCard(section)
            }
        }
    }
}

@Composable
private fun OperationParametersTab(equipment: Equipment) {
    val operationSections = equipment.knowledgeSections.filter { it.sectionNumber in 9..12 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DetailSectionCard(title = "Technical & Operating Parameters (${equipment.technicalParameters.size})") {
            if (equipment.technicalParameters.isNotEmpty()) {
                equipment.technicalParameters.forEach { param ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${param.group}: ${param.parameterName}",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${param.standardValue} ${param.unit}".trim(),
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                        }
                        param.tolerance?.takeIf { it.isNotBlank() }?.let { tol ->
                            Text(
                                text = "Tolerance / Limits: $tol",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else {
                Text(
                    text = "No numeric technical parameters populated for ${equipment.id} (${equipment.contentDepth.displayName}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        operationSections.forEach { section ->
            KnowledgeSectionCard(section)
        }

        val (monitorTitle, monitorBody) = buildConditionMonitoringGuidance(equipment)
        DetailSectionCard(title = monitorTitle) {
            Text(
                text = monitorBody,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MaintenanceLubricationTab(
    equipment: Equipment,
    relevantChecklists: List<PlantChecklist> = emptyList(),
    checklistSummaries: Map<String, ChecklistProgressSummary> = emptyMap(),
    onOpenChecklistForEquipment: (String, String?) -> Unit = { _, _ -> },
    onCreateCustomChecklistForEquipment: (String) -> Unit = {}
) {
    val maintenanceSections = equipment.knowledgeSections.filter { it.sectionNumber in listOf(13, 14, 15, 16, 18) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DetailSectionCard(title = "Inspection & Maintenance Checklists (${relevantChecklists.size})") {
            if (relevantChecklists.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    relevantChecklists.forEach { chk ->
                        val summary = checklistSummaries[chk.id]
                        val statusSuffix = if (summary != null) {
                            " • ${summary.status.label} (${summary.completedCount}/${summary.totalCount})"
                        } else {
                            " • ${chk.items.size} items"
                        }
                        OutlinedButton(
                            onClick = { onOpenChecklistForEquipment(equipment.id, chk.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chk.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = (if (chk.isCustom) "Custom Template" else "Packaged Template") + statusSuffix,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No packaged checklist is currently pre-mapped to ${equipment.id}. Create a custom checklist for this machine or open the checklist module.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenChecklistForEquipment(equipment.id, relevantChecklists.firstOrNull()?.id) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("View Checklists")
                }
                Button(
                    onClick = { onCreateCustomChecklistForEquipment(equipment.id) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom Checklist")
                }
            }
        }

        val lubeText = equipment.lubricationRegime
            ?: "NLGI Grade 2 Lithium Complex Grease (EP2) or OEM oil viscosity grade; verify bearing housing temperature before re-lubrication."
        val isDryOrNonLube = lubeText.startsWith("None", ignoreCase = true)
        DetailSectionCard(
            title = if (isDryOrNonLube) {
                "13. Dry-Service Care & Cleanliness Regime"
            } else {
                "13. Lubrication Regime"
            }
        ) {
            Text(
                text = lubeText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        DetailSectionCard(title = "14. Maintenance Procedures (${equipment.maintenanceProcedures.size})") {
            if (equipment.maintenanceProcedures.isNotEmpty()) {
                equipment.maintenanceProcedures.forEach { proc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = proc.title,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = proc.frequency.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "LOTO: ${proc.safetyLockoutProcedure}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (proc.requiredTools.isNotEmpty()) {
                                Text(
                                    text = "Tools: ${proc.requiredTools.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (proc.actionSteps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                proc.actionSteps.forEachIndexed { idx, step ->
                                    Text(
                                        text = "${idx + 1}. $step",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Detailed step-by-step PM procedure schedules pending research expansion for ${equipment.id}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        maintenanceSections.forEach { section ->
            KnowledgeSectionCard(section)
        }
    }
}

@Composable
private fun TroubleshootingSafetyTab(
    equipment: Equipment,
    userNotes: List<UserNote>,
    onAddNote: () -> Unit
) {
    val troubleSections = equipment.knowledgeSections.filter { it.sectionNumber in listOf(17, 21) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SafetyWarningCard(
            title = "LOTO & Safety Interlocks",
            message = equipment.statutoryRequirement
                ?: "Isolate main power, lock out steam/compressed-air isolating valves, bleed residual pressure, and verify zero energy before maintenance access.",
            isStatutory = equipment.familyId == "FAM-G"
        )

        DetailSectionCard(title = "17. Known Failure Modes & Rectifications (${equipment.failureModes.size})") {
            if (equipment.failureModes.isNotEmpty()) {
                equipment.failureModes.forEach { fmea ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Symptom: ${fmea.symptom}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fmea.severity.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Root Cause: ${fmea.rootCause}", style = MaterialTheme.typography.bodySmall)
                            if (fmea.diagnosticStep.isNotBlank()) {
                                Text("Diagnostic Check: ${fmea.diagnosticStep}", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rectification: ${fmea.correctiveAction}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            fmea.vibrationSignature?.takeIf { it.isNotBlank() }?.let { vib ->
                                Text(
                                    text = "Vibration Signature: $vib",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No failure modes recorded for ${equipment.id} in current baseline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        troubleSections.forEach { section ->
            KnowledgeSectionCard(section)
        }

        DetailSectionCard(title = "21. Personal Field Notes (${userNotes.size})") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal maintenance log & observations",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onAddNote) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Note")
                }
            }

            if (userNotes.isEmpty()) {
                Text(
                    text = "No personal notes recorded yet for this equipment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                userNotes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = note.title, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = note.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeSectionCard(section: KnowledgeSection) {
    DetailSectionCard(title = "${section.sectionNumber}. ${section.sectionTitle}") {
        SimpleMarkdownText(markdown = section.contentMarkdown)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Verification: ${section.verificationTier}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

internal data class EquipmentSchematicMeta(
    val title: String,
    val drawableRes: Int,
    val legend: String,
    val subtitle: String = "Conceptual Engineering Schematic (Verify OEM Construction Drawing)"
)

internal fun buildConditionMonitoringGuidance(equipment: Equipment): Pair<String, String> {
    return when (equipment.id) {
        "EQ-F01", "EQ-F02", "EQ-F03", "EQ-F04", "EQ-F05", "EQ-F06",
        "EQ-G03", "EQ-G04",
        "EQ-H01", "EQ-H02",
        "EQ-I02",
        "EQ-J03", "EQ-J04",
        "EQ-L03",
        "EQ-M02",
        "EQ-O02" ->
            "Inspection, Structural & Calibration Integrity" to
                "Static/vessel/valve/instrumentation asset (${equipment.id}): Excluded from ISO 20816-3 rotating machinery vibration limits. Verify pressure relief certification, NDT shell thickness, ΔP/backwash/trap integrity, weekly flush/function tests, or LEL gas calibration per governing standard."
        "EQ-G01", "EQ-G02" ->
            "Boiler Integrity, Flue Thermal & Auxiliary Fan Check" to
                "Stationary steam boiler (${equipment.id}): Pressure parts follow The Boilers Act, 2022 & OCIB visual/NDT/safety-valve rules (NOT ISO 20816-3). Trend flue stack temperature, O2/CO2, TDS/hardness, and monitor auxiliary FD/ID fan bearings separately (ISO 20816-3:2026 where >15 kW and 120–30,000 r/min; OEM baseline if <=15 kW)."
        "EQ-D01", "EQ-D02", "EQ-D05", "EQ-H03", "EQ-H04", "EQ-L01" ->
            "Centrifugal / Rotodynamic & Submerged Pump Monitoring" to
                "Rotodynamic / submerged pump (${equipment.id}): Integrated rotodynamic and submerged pumps are excluded from standalone ISO 20816-3 scope and evaluated under ISO 10816-7 / OEM hydraulic guidelines. Monitor NPSHa vs suction vacuum, mechanical seal weep/dielectric oil, motor amp balance, and apply ISO 20816-3:2026 only to coupled dry electric drivers >15 kW (120–30,000 r/min)."
        "EQ-O01" ->
            "Diesel Fire Pump Engine & Split-Pump Readiness (NFPA 20/25)" to
                "Reciprocating compression-ignition engine + fire pump (${equipment.id}): Excluded from standard ISO 20816-3 limits. Evaluate diesel engine vibration per ISO 10816-6 and fire pump per ISO 10816-7 / NFPA 20/25 alongside weekly 30-min churn runs, dual 24V batteries, oil pressure, coolant temperature, and coupling alignment (<0.05 mm)."
        "EQ-E04", "EQ-E05", "EQ-E06" ->
            "Belt Tension, Sheave & Shaft Alignment Standard" to
                "Mechanical transmission (${equipment.id}): Excluded directly from ISO 20816-3 limits. Check pulley laser/straightedge coplanarity, belt deflection under mid-span load, or elastomer coupling angular/parallel offset (<0.05 mm dial/laser guide); evaluate coupled motors >15 kW (120–30,000 r/min) under ISO 20816-3:2026."
        "EQ-J01", "EQ-L02" ->
            "Positive-Displacement Thermal, ΔP & Acoustic Check" to
                "Positive-displacement screw/lobe machine (${equipment.id}): Explicitly excluded from standard ISO 20816-3 rotordynamic limits. Trend oil sump discharge temp (<105°C screw), separator/suction filter ΔP, helical timing gear backlash, and gear oil cleanliness."
        "EQ-D03", "EQ-D04", "EQ-D06", "EQ-L04" ->
            "Flow Pulsation, Relief & Dry-Run Monitoring" to
                "Positive-displacement pump (${equipment.id}): Excluded from standard ISO 20816-3 rotordynamic limits. Monitor stroke calibration, check-valve seating, suction strainer vacuum/clogging, and mandatory dry-run protection (progressive cavity stators fail rapidly without fluid)."
        "EQ-L05", "EQ-M01", "EQ-M03" ->
            "Slow-Speed Mechanical, Brake & Belt Tracking Check" to
                "Slow-speed / lifting / conveyor system (${equipment.id}): Operating speeds (<120 r/min drum/conveyor rollers) or hoist intermittent duty fall outside ISO 20816-3 (120–30,000 r/min, >15 kW). Verify drum out-of-round/chain tension, brake holding & SWL chain wear, pull-cord stops, and idler rotation."
        "EQ-C02", "EQ-C03", "EQ-C04", "EQ-C05", "EQ-N03" ->
            "Process Safety, Airflow & Interlock Verification" to
                "Process/safety/fabrication unit (${equipment.id}): Verify local exhaust velocity / water curtain flow, ambient O3 (<0.1 ppm) or Class 4 laser optical enclosure interlocks, or welder ground insulation before shift startup. Trend rotating exhaust fan/chiller drivers separately per OEM limits or ISO 20816-3:2026 (>15 kW)."
        "EQ-A01", "EQ-A02", "EQ-A03", "EQ-A04", "EQ-A05", "EQ-B01", "EQ-B02", "EQ-E01" ->
            "Alignment, Rotor Unbalance & Vibration Guidance" to
                "Industrial rotating/wash machinery (${equipment.id}): Machines >15 kW at 120–30,000 r/min follow ISO 20816-3:2026 (Group 1 vs Group 2, Rigid vs Flexible supports, Criterion I Zones A–D & Criterion II baseline change). Sub-15 kW washers, hydro extractors (<=11 kW), and small motors fall below ISO 20816-3's 15 kW nominal floor — apply OEM baseline velocity/temperature trending (<2.8 mm/s field guide) alongside unbalance trip and bearing temp checks."
        else ->
            "Alignment & Mechanical Vibration Standard" to
                "Coupled industrial machinery (${equipment.id}): Where nominal power exceeds 15 kW and operating speed is 120–30,000 r/min in situ, apply ISO 20816-3:2026 (Group 1 / Group 2, Rigid vs Flexible support, Zones A–D & baseline change) alongside housing temperature (<75°C); machines <=15 kW use OEM baseline trending."
    }
}

internal fun getSchematicForEquipment(equipment: Equipment): EquipmentSchematicMeta? {
    return when {
        equipment.id in listOf("EQ-A01", "EQ-A02", "EQ-A03", "EQ-A04", "EQ-A05") -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-A04" -> "Tilting Garment Washer Conceptual Cross-Section"
                "EQ-A05" -> "Barrier Sanitary Washer Conceptual Arrangement"
                else -> "Garment Washer Cross-Section Schematic"
            },
            subtitle = "Conceptual Engineering Schematic — Confirm OEM Drum & Tilt/Partition Layout",
            drawableRes = R.drawable.diag_belly_washer,
            legend = "[1] Inner SS316L Perforated Drum & Lifters | [2] Trunnion Shaft & Plummer Bearing (Grease Pt) | [3] Mechanical Water Seal & Quench Drain | [4] V-Belt / Gearmotor Drive | [5] Steam Sparger & Water/Chem Inlet | [6] Door Interlock & Bottom Drain Valve"
        )
        equipment.id in listOf("EQ-B01", "EQ-B02") -> EquipmentSchematicMeta(
            title = if (equipment.id == "EQ-B02") {
                "Tilting Centrifugal Hydro Extractor Conceptual Schematic"
            } else {
                "3-Point Pendulum Hydro Extractor Schematic"
            },
            subtitle = "Conceptual Cross-Section — Verify Basket Dia, Max RPM & Pivot/Pendulum OEM Specs",
            drawableRes = R.drawable.diag_hydro_extractor,
            legend = "[1] Safety Lid & Zero-Speed Interlock | [2] Perforated SS316L Basket & Hub | [3] Pendulum Columns & Springs | [4] Taper Spindle Bearing & Brake | [5] Motor & Centrifugal Clutch/V-Belt | [6] Gravity Water Drain"
        )
        equipment.id in listOf("EQ-B03", "EQ-B04", "EQ-B05") -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-B04" -> "Gas-Fired Tumbler Dryer Conceptual Airflow Schematic"
                "EQ-B05" -> "Tunnel / RF Conveyor Dryer Conceptual Layout"
                else -> "Steam-Heated Tumbler Dryer Conceptual Schematic"
            },
            subtitle = "Conceptual Airflow & Fire-Safety Diagram — Verify OEM Coil/Burner/RF Geometry",
            drawableRes = R.drawable.diag_tumbler_dryer,
            legend = "[1] Heated Air Plenum (Steam Coil / Gas Burner / Tunnel Zone) | [2] Reversing Perforated Drum | [3] Trunnion Rollers & Drive Motor | [4] Pull-Out Lint Screen (Clean Every Batch) | [5] Exhaust Blower & Roof Duct | [6] High-Limit & Door Interlock"
        )
        equipment.familyId == "FAM-C" -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-C01" -> "Curing / Baking Box Oven Conceptual Schematic"
                "EQ-C02" -> "3D Whisker Pneumatic Station Conceptual Layout"
                "EQ-C03" -> "Water-Curtain PP Spray Booth Conceptual Layout"
                "EQ-C04" -> "Ozone Fading & Catalyst Destructor Conceptual Layout"
                else -> "Enclosed Class 4 Laser Finishing Conceptual Layout"
            },
            subtitle = "Conceptual Process & Exhaust Schematic — Confirm OEM Enclosure & Lev/O3/Laser Specs",
            drawableRes = R.drawable.diag_finishing_oven_booth,
            legend = "[1] Process Chamber / Mannequin / Garment Zone | [2] Heater / Air / Spray / O3 / Laser Source | [3] Curtain Pump or Recirculation Fan | [4] Local Exhaust Ventilation (LEV) Stack | [5] Interlock / O3 / Over-Temp Sensor | [6] Collection Sump / Filter Tray"
        )
        equipment.id in listOf("EQ-D01", "EQ-D02", "EQ-D05") -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-D02" -> "Multistage Boiler Feed Pump & NPSH Arrangement"
                "EQ-D05" -> "Submersible Effluent Sump Pumping Conceptual Layout"
                else -> "End-Suction Centrifugal Pump & NPSH Arrangement"
            },
            subtitle = "Conceptual Pumping Arrangement — Verify OEM Curve, NPSHr & Piping Isometric",
            drawableRes = R.drawable.diag_centrifugal_pump_npsh,
            legend = "[1] Supply Tank / Deaerator / Sump Head | [2] Suction Line & Y-Strainer Check | [3] Impeller Volute / Multistage Casing | [4] Mechanical Seal & Flush | [5] Flexible Coupling & TEFC Motor | [6] Discharge Check & Isolation Valves"
        )
        equipment.id in listOf("EQ-D03", "EQ-D04", "EQ-D06") -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-D03" -> "AODD Chemical/Sludge Pump Conceptual Loop"
                "EQ-D04" -> "Positive Displacement Metering Pump Conceptual Loop"
                else -> "Diesel / Fuel Gear Transfer Pump Conceptual Loop"
            },
            subtitle = "Conceptual Positive-Displacement Schematic — Verify OEM Relief & Material Compatibility",
            drawableRes = R.drawable.diag_pd_metering_pump,
            legend = "[1] Supply Drum & Foot Strainer | [2] Compressed Air Spool / Motor Gear Drive | [3] Diaphragm / Plunger / Gear Pumping Head | [4] Inlet & Discharge Ball Check Valves | [5] Pressure Relief / Bypass Line | [6] Calibration Column / Pulsation Check"
        )
        equipment.familyId == "FAM-E" -> EquipmentSchematicMeta(
            title = "Mechanical Power Transmission & Alignment Schematic",
            subtitle = "Conceptual Drive Train Arrangement — Confirm Exact Gearbox Ratio, Belt Section & Bearing SKU",
            drawableRes = R.drawable.diag_power_transmission,
            legend = "[1] 3-Phase Induction Motor | [2] Flexible Coupling (Alignment Plane) | [3] Reduction Gearbox & Oil Sight Glass | [4] Guarded V-Belt / Timing Sheaves | [5] Plummer Block Bearing & Grease Point | [6] Driven Shaft & Triaxial Vibration Point"
        )
        equipment.familyId == "FAM-F" -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-F05" -> "Steam / Drain Heat Recovery Conceptual Arrangement"
                "EQ-F06" -> "Thermal Cladded Steam Header & Return Arrangement"
                else -> "Steam Distribution & Condensate Return Loop"
            },
            subtitle = "Conceptual System Arrangement — Verify Plant Header Isometric & Trap Sizing",
            drawableRes = R.drawable.diag_steam_condensate_loop,
            legend = "[1] High-Pressure Steam Header | [2] Moisture Separator & Drip Strainer | [3] Pilot PRV / Angle-Seat Control Valve | [4] Washer/Dryer Heat Exchanger Load | [5] Float / TD Steam Trap | [6] Flash Recovery Tank & Condensate Pump"
        )
        equipment.id in listOf("EQ-G01", "EQ-G03", "EQ-G04") -> EquipmentSchematicMeta(
            title = when (equipment.id) {
                "EQ-G03" -> "Boiler Drum & Dual Full-Lift Safety Valve Arrangement"
                "EQ-G04" -> "Boiler Drum, Mobrey LWCO & Gauge Column Arrangement"
                else -> "3-Pass Wet-Back Firetube Gas Boiler Schematic"
            },
            subtitle = "Conceptual Boiler Arrangement — Verify OCIB Stamped Drawing & Boiler Nameplate",
            drawableRes = R.drawable.diag_gas_boiler_3pass,
            legend = "[1] Modulating Gas Burner & UV Flame Scanner | [2] Pass 1 Corrugated Furnace | [3] Wet-Back Chamber & Passes 2–3 Tubes | [4] Dual Spring Safety Valves | [5] Mobrey LWCO & Gauge Glass | [6] Main Steam Stop, Stack & Blowdown"
        )
        equipment.id == "EQ-G02" -> EquipmentSchematicMeta(
            title = "Solid-Fuel (Jhut) Boiler & Balanced Draft System",
            subtitle = "Conceptual Draft Arrangement — Verify Furnace Grate Type, Cyclone & OCIB Drawing",
            drawableRes = R.drawable.diag_jhut_boiler_draft,
            legend = "[1] External Jhut Grate Furnace & Ash Pit | [2] Forced Draft (FD) Air Plenum | [3] Boiler Drum & Smoke Tubes | [4] Safety Valve & Steam Header | [5] Multi-Cyclone Spark/Ash Collector | [6] Water-Cooled ID Fan & Stack"
        )
        equipment.familyId == "FAM-H" -> EquipmentSchematicMeta(
            title = "Water Filtration, Softening, RO & Booster System",
            subtitle = "Conceptual Water Treatment Flow — Confirm Vessel Dimensions & Plant RO Presence",
            drawableRes = R.drawable.diag_water_treatment_loop,
            legend = "[1] Raw Tube-Well Water Tank | [2] Multi-Grade Sand & Carbon Filters | [3] Sodium Cation Softener & Brine Tank | [4] Optional RO Membrane Skid | [5] Soft Water Storage Tank | [6] Lead-Lag Hydro-Pneumatic Booster & Bladder"
        )
        equipment.familyId == "FAM-I" -> EquipmentSchematicMeta(
            title = "Chemical Prep Tank, Caustic Jacket & Emergency Shower",
            subtitle = "Conceptual Chemical Handling & Safety Layout — Verify Tank Capacity & Bund Volume",
            drawableRes = R.drawable.diag_chemical_dosing_safety,
            legend = "[1] Spill Containment Bund | [2] SS316L Chemical Mixing/Dissolving Tank | [3] Top Geared Agitator & Splash Lid | [4] Exothermic Cooling Water Jacket | [5] Chemical Transfer Valve | [6] Emergency Safety Shower & Eyewash (ANSI Z358.1)"
        )
        equipment.familyId == "FAM-J" -> EquipmentSchematicMeta(
            title = "Compressed Air Generation, Storage & Treatment Loop",
            subtitle = "Conceptual Compressed Air Arrangement — Verify Compressor kW, Receiver & Filter ΔP",
            drawableRes = R.drawable.diag_compressed_air_system,
            legend = "[1] Intake Filter & Rotary Screw Airend | [2] Oil Sump, Cooler & Thermostatic Valve | [3] Vertical Air Receiver, PRV & Auto-Drain | [4] +3°C Refrigerated Air Dryer | [5] Coalescing & Carbon Line Filters | [6] Plant Distribution Ring Header"
        )
        equipment.familyId == "FAM-K" -> EquipmentSchematicMeta(
            title = "Cooling Tower & Process Chiller Closed/Open Loop",
            subtitle = "Conceptual Cooling Arrangement — Confirm Cooling Tower / Chiller Presence on Site",
            drawableRes = R.drawable.diag_cooling_chiller_loop,
            legend = "[1] Induced-Draft Tower Fill Pack | [2] Overhead Axial Fan & Drift Eliminator | [3] Condenser Water Pump | [4] Chiller Compressor/Condenser Skid | [5] Chilled Water Evaporator Buffer | [6] Enzyme/Laser Cooling Load"
        )
        equipment.familyId == "FAM-L" -> EquipmentSchematicMeta(
            title = "ETP Screening, Aeration, DAF & Sludge Dewatering",
            subtitle = "Conceptual ETP Process Train — Verify Plant ETP Flowsheet & DoE ECR Limits",
            drawableRes = R.drawable.diag_etp_clarification,
            legend = "[1] Rotary Drum Fine Screen | [2] Tri-Lobe Roots Blower & Aeration Grid | [3] DAF Air Saturation Vessel (5 bar) | [4] DAF Clarifier & Surface Skimmer | [5] Progressive Cavity Screw Sludge Pump | [6] Hydraulic Recessed Filter Press"
        )
        equipment.familyId == "FAM-M" -> EquipmentSchematicMeta(
            title = "Overhead Chain Hoist, Trolley & Wet Incline Conveyor",
            subtitle = "Conceptual Material Handling Schematic — Verify Beam SWL Tag & Guarding",
            drawableRes = R.drawable.diag_lifting_conveyor,
            legend = "[1] Monorail I-Beam & Travel Trolley | [2] Dual-Brake Electric Hoist & Upper Limit | [3] Grade 80 Load Chain & Safety Hook | [4] PE Wet Garment Cart & Casters | [5] Cleated Incline Wet Conveyor | [6] Nip-Point Guards & Pull-Cord E-Stop"
        )
        equipment.familyId == "FAM-N" -> EquipmentSchematicMeta(
            title = "Maintenance Workshop Machines & Welding Safety Setup",
            subtitle = "Conceptual Workshop Arrangement — Verify Individual Machine Guards & PPE",
            drawableRes = R.drawable.diag_workshop_machines,
            legend = "[1] Lathe Geared Headstock & Chuck Shield | [2] Carriage, Toolpost & Tailstock | [3] Pillar Drill Clamped Machine Vice | [4] Stepped Pulley Guard & E-Stop | [5] Inverter TIG/MMA Welder & Argon Cylinder | [6] Insulated Floor Mat & Extraction"
        )
        else -> EquipmentSchematicMeta(
            title = "Emergency Fire Pump, LEL Gas Shutoff & Exhaust Fan",
            subtitle = "Conceptual Plant Safety & Exhaust Arrangement — Verify Statutory Test & Duct Layout",
            drawableRes = R.drawable.diag_safety_exhaust_utility,
            legend = "[1] Diesel Emergency Fire Pump & Heat Exchanger | [2] Dual 24V Cranking Batteries | [3] Catalytic/IR LEL Gas Detector | [4] Solenoid Slam-Shut Gas Valve | [5] Centrifugal Lint Exhaust Impeller | [6] Floor Hood, Cleanout Door & Stack"
        )
    }
}

internal fun getRelatedCalculationsForEquipment(equipment: Equipment): List<Pair<String, String>> {
    return when (equipment.familyId) {
        "FAM-A" -> listOf(
            "CALC-15" to "V-Belt Drive Sizing & Deflection",
            "CALC-17" to "Wash Batch Steam Consumption"
        )
        "FAM-B" -> when {
            equipment.id in listOf("EQ-B01", "EQ-B02") -> listOf("CALC-16" to "Centrifugal G-Force & Critical RPM Check")
            else -> listOf("CALC-18" to "Tumble Dryer Evaporation & Airflow")
        }
        "FAM-D" -> when (equipment.id) {
            "EQ-D01", "EQ-D02", "EQ-D05" -> listOf("CALC-07" to "Pump Hydraulic Power & Motor kW")
            else -> emptyList()
        }
        "FAM-E" -> listOf("CALC-15" to "V-Belt Pitch Length & Ratio")
        "FAM-F" -> listOf("CALC-14" to "Steam Header Pipe DN Sizing", "CALC-06" to "Flash Steam Recovery")
        "FAM-G" -> when (equipment.id) {
            "EQ-G01" -> listOf(
                "CALC-01" to "Direct Thermal Efficiency",
                "CALC-02" to "Indirect Boiler Efficiency (Gas)",
                "CALC-03" to "Fuel Firing Rate",
                "CALC-04" to "Steam-to-Fuel Evaporation Ratio",
                "CALC-05" to "Boiler Blowdown %",
                "CALC-08" to "Deaerator Steam Balance",
                "CALC-09" to "Safety Valve Capacity Estimate",
                "CALC-10" to "Economizer Flue Heat Recovery",
                "CALC-11" to "FD Fan Combustion Air Sizing",
                "CALC-13" to "Chimney Stack Draught"
            )
            "EQ-G02" -> listOf(
                "CALC-01" to "Direct Thermal Efficiency",
                "CALC-02" to "Indirect Boiler Efficiency (Solid Fuel Lab)",
                "CALC-03" to "Fuel Firing Rate",
                "CALC-04" to "Steam-to-Fuel Evaporation Ratio",
                "CALC-05" to "Boiler Blowdown %",
                "CALC-08" to "Deaerator Steam Balance",
                "CALC-09" to "Safety Valve Capacity Estimate",
                "CALC-10" to "Economizer Flue Heat Recovery",
                "CALC-11" to "FD Fan Combustion Air Sizing",
                "CALC-12" to "Induced Draft (ID) Fan Sizing",
                "CALC-13" to "Chimney Stack Draught"
            )
            "EQ-G03" -> listOf("CALC-09" to "Safety Valve Capacity Estimate")
            else -> listOf(
                "CALC-05" to "Boiler Blowdown %",
                "CALC-08" to "Deaerator Steam Balance"
            )
        }
        "FAM-H" -> when (equipment.id) {
            "EQ-H01" -> listOf("CALC-19" to "Water Softener Resin & Salt Sizing")
            "EQ-H03" -> emptyList() // Industrial RO Plant: no dedicated RO membrane/flux calculator in CALC-01..20
            else -> listOf("CALC-07" to "Transfer & Booster Pump Hydraulic Power")
        }
        "FAM-J" -> listOf("CALC-20" to "Compressed Air ΔP & Receiver Sizing")
        else -> emptyList()
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

private object EquipmentDetailPreviewData {
    val fullBoiler = Equipment(
        id = "EQ-G01",
        categoryId = "CAT-G1",
        familyId = "FAM-G",
        name = "3-Pass Firetube Natural Gas Boiler",
        bengaliName = "৩-পাস ন্যাচারাল গ্যাস ফায়ারটিউব বয়লার",
        aliases = listOf("Packaged Gas Boiler", "Scotch Marine 3-Pass"),
        contentDepth = ContentDepth.FULL_BLUEPRINT,
        criticalityTier = CriticalityTier.SAFETY_CRITICAL,
        capacityRange = "4,000 – 12,000 kg/h @ 10.54 bar(g)",
        operatingTemperature = "182 °C saturated steam",
        primaryDriveType = "Modulating gas burner + VFD FD blower",
        lubricationRegime = "High-temp EP2 grease on blower bearings",
        statutoryRequirement = "The Boilers Act, 2022 Sec 18 annual OCIB certificate",
        technicalParameters = listOf(
            TechnicalParameter(equipmentId = "EQ-G01", group = "Steam", parameterName = "MAWP", standardValue = "10.54", unit = "bar(g)", tolerance = "Pop valve 10.75 bar(g)")
        )
    )

    val researchPendingAsset = Equipment(
        id = "EQ-A05",
        categoryId = "CAT-A01",
        familyId = "FAM-A",
        name = "Barrier Sanitary Washing Machine",
        bengaliName = "ব্যারিয়ার স্যানিটারি ওয়াশিং মেশিন",
        aliases = listOf("Sanitary Washer", "Pass-Through Cleanroom Washer"),
        contentDepth = ContentDepth.RESEARCH_PENDING,
        criticalityTier = CriticalityTier.SECONDARY_AUXILIARY,
        capacityRange = "Awaiting owner confirmation & nameplate"
    )
}

@Preview(name = "EquipmentDetail - Dark Overview", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun EquipmentDetailDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        EquipmentDetailContent(
            equipmentId = "EQ-G01",
            state = EquipmentDetailUiState(
                isLoading = false,
                equipment = EquipmentDetailPreviewData.fullBoiler,
                selectedTab = 0,
                isBookmarked = true
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "EquipmentDetail - Light ResearchPending", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun EquipmentDetailLightResearchPendingPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        EquipmentDetailContent(
            equipmentId = "EQ-A05",
            state = EquipmentDetailUiState(
                isLoading = false,
                equipment = EquipmentDetailPreviewData.researchPendingAsset,
                selectedTab = 0,
                isBookmarked = false
            ),
            onNavigateBack = {}
        )
    }
}
