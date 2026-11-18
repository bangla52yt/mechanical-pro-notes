package com.researchbangla.mechanicalpronotes.presentation.boiler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.R
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationStatusBadge
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.EquipmentCardItem
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.components.ZoomableSchematicCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@Composable
fun BoilerDashboardScreen(
    viewModel: BoilerViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToCalculation: (String) -> Unit,
    modifier: Modifier = Modifier,
    bookmarkedSectionIds: Set<String> = emptySet(),
    onToggleSectionBookmark: (BoilerTab) -> Unit = {},
    calculations: List<CalculationDefinition> = CalculationSpecifications.ALL_CALCULATIONS,
    onNavigateToSteamTable: () -> Unit = {},
    unitSystem: UnitSystem = UnitSystem.METRIC
) {
    val uiState by viewModel.uiState.collectAsState()
    BoilerDashboardContent(
        uiState = uiState,
        onOpenDrawer = onOpenDrawer,
        onSelectTab = { viewModel.selectTab(it) },
        onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
        onNavigateToCalculation = onNavigateToCalculation,
        modifier = modifier,
        bookmarkedSectionIds = bookmarkedSectionIds,
        onToggleSectionBookmark = onToggleSectionBookmark,
        calculations = calculations,
        onNavigateToSteamTable = onNavigateToSteamTable,
        unitSystem = unitSystem
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoilerDashboardContent(
    uiState: BoilerUiState,
    onOpenDrawer: () -> Unit,
    onSelectTab: (BoilerTab) -> Unit = {},
    onNavigateToEquipmentDetail: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    bookmarkedSectionIds: Set<String> = emptySet(),
    onToggleSectionBookmark: (BoilerTab) -> Unit = {},
    calculations: List<CalculationDefinition> = CalculationSpecifications.ALL_CALCULATIONS,
    onNavigateToSteamTable: () -> Unit = {},
    unitSystem: UnitSystem = UnitSystem.METRIC
) {
    val overviewCalcs = remember(calculations) {
        BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.OVERVIEW, calculations)
    }
    val gasCalcs = remember(calculations) {
        BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.GAS_BOILERS, calculations)
    }
    val jhutCalcs = remember(calculations) {
        BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.JHUT_BOILERS, calculations)
    }
    val statutoryCalcs = remember(calculations) {
        BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.STATUTORY_RULES, calculations)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Industrial Boilers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                    }
                },
                actions = {
                    val sectionId = boilerSectionBookmarkId(uiState.selectedTab)
                    val isBookmarked = bookmarkedSectionIds.any { it.equals(sectionId, ignoreCase = true) }
                    IconButton(
                        onClick = onNavigateToSteamTable,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Open Saturated Steam Table"
                        )
                    }
                    IconButton(
                        onClick = { onToggleSectionBookmark(uiState.selectedTab) },
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) {
                                "Remove bookmark for ${uiState.selectedTab.title}"
                            } else {
                                "Bookmark ${uiState.selectedTab.title}"
                            },
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row for Boiler Subsystems
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                BoilerTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            if (uiState.isLoading && uiState.boilerEquipment.isEmpty() && uiState.errorMessage == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.errorMessage != null) {
                        item {
                            SafetyWarningCard(
                                title = "Boiler Registry Notice",
                                message = uiState.errorMessage,
                                isStatutory = true
                            )
                        }
                    }

                    when (uiState.selectedTab) {
                        BoilerTab.OVERVIEW -> {
                            item {
                                SafetyWarningCard(
                                    title = "High Pressure Steam Utility Safety",
                                    message = "Industrial boilers operate under elevated pressure (typically 8.0–10.54 bar) and saturated steam temperatures up to 184°C. Adherence to The Boilers Act, 2022 and annual statutory safety valve popping tests is mandatory.",
                                    isStatutory = true
                                )
                            }

                            item {
                                BoilerSteamTableShortcutCard(
                                    unitSystem = unitSystem,
                                    onClick = onNavigateToSteamTable
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Boiler Engineering Calculations (${overviewCalcs.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.semantics { heading() }
                                    )
                                    Text(
                                        text = "Unit Preference: ${unitSystem.displayName} • Tap any formula to open Calculation Runner",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            items(overviewCalcs, key = { "overview-calc-${it.id}" }) { calc ->
                                BoilerCalculationShortcutCard(
                                    calc = calc,
                                    onClick = { onNavigateToCalculation(calc.id) }
                                )
                            }

                            item {
                                Text(
                                    text = "Industrial Boilers in Knowledge Base",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }

                            if (uiState.boilerEquipment.isEmpty()) {
                                item {
                                    EmptyStateCard(
                                        title = "No Boiler Equipment Loaded",
                                        message = "Verify local database availability in Settings."
                                    )
                                }
                            } else {
                                items(uiState.boilerEquipment, key = { "all-${it.id}" }) { boiler ->
                                    EquipmentCardItem(
                                        equipment = boiler,
                                        onClick = { onNavigateToEquipmentDetail(boiler.id) }
                                    )
                                }
                            }
                        }

                        BoilerTab.GAS_BOILERS -> {
                            item {
                                ZoomableSchematicCard(
                                    title = "3-Pass Wet-Back Natural Gas Firetube Boiler",
                                    subtitle = "Offline schematic (Pass 1 Furnace -> Passes 2 & 3 Smoke Tubes)",
                                    drawableRes = R.drawable.diag_gas_boiler_3pass,
                                    legendText = "Monitor LWCO probes, burner gas train pressure switch, and dual spring safety valves before firing."
                                )
                            }
                            item {
                                OutlinedButton(
                                    onClick = onNavigateToSteamTable,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Science, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open IAPWS-IF97 Steam Table (${unitSystem.displayName})")
                                }
                            }
                            item {
                                Text(
                                    text = "Natural Gas-Fired Packaged Boilers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }
                            if (uiState.gasBoilers.isEmpty()) {
                                item {
                                    EmptyStateCard(
                                        title = "No Gas Boiler Assets Listed",
                                        message = "Gas-fired boilers from FAM-G appear here when seeded."
                                    )
                                }
                            } else {
                                items(uiState.gasBoilers, key = { "gas-${it.id}" }) { boiler ->
                                    EquipmentCardItem(
                                        equipment = boiler,
                                        onClick = { onNavigateToEquipmentDetail(boiler.id) }
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "Gas Boiler Calculators (${gasCalcs.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }
                            items(gasCalcs, key = { "gas-calc-${it.id}" }) { calc ->
                                BoilerCalculationShortcutCard(
                                    calc = calc,
                                    onClick = { onNavigateToCalculation(calc.id) }
                                )
                            }
                        }

                        BoilerTab.JHUT_BOILERS -> {
                            item {
                                ZoomableSchematicCard(
                                    title = "Solid-Fuel (Cotton Jhut) Boiler & Flue Gas Cleaning",
                                    subtitle = "Offline schematic (FD Grate Air -> Cyclone Dust Collector -> ID Fan)",
                                    drawableRes = R.drawable.diag_jhut_boiler_draft,
                                    legendText = "Maintain slight furnace negative draft (-2 to -5 mm WC) and clear cyclone rotary airlock ash hoppers."
                                )
                            }
                            item {
                                OutlinedButton(
                                    onClick = onNavigateToSteamTable,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Science, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open IAPWS-IF97 Steam Table (${unitSystem.displayName})")
                                }
                            }
                            item {
                                Text(
                                    text = "Biomass, Fabric Waste (Jhut) & Solid Fuel Boilers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }
                            if (uiState.jhutBoilers.isEmpty()) {
                                item {
                                    EmptyStateCard(
                                        title = "No Jhut/Solid-Fuel Boilers Listed",
                                        message = "Solid-fuel boilers from FAM-G appear here when seeded."
                                    )
                                }
                            } else {
                                items(uiState.jhutBoilers, key = { "jhut-${it.id}" }) { boiler ->
                                    EquipmentCardItem(
                                        equipment = boiler,
                                        onClick = { onNavigateToEquipmentDetail(boiler.id) }
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "Solid-Fuel & Draft Calculators (${jhutCalcs.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }
                            items(jhutCalcs, key = { "jhut-calc-${it.id}" }) { calc ->
                                BoilerCalculationShortcutCard(
                                    calc = calc,
                                    onClick = { onNavigateToCalculation(calc.id) }
                                )
                            }
                        }

                        BoilerTab.STATUTORY_RULES -> {
                            item {
                                SafetyWarningCard(
                                    title = "The Boilers Act, 2022 (Act No. 06 of 2022)",
                                    message = "Enacted & commenced 11 April 2022; repealed The Boilers Act, 1923 under Sec 54. Key enforceable sections: Sec 2(7) (Boiler >25 L), Sec 9 (Certified operator license), Sec 13 (Classification & lifespan mandate), Sec 18/19 (Mandatory registration & annual Certificate of Use), Sec 24 (Written repair permit), Sec 28 (Mandatory accident reporting & inquiry), Sec 36/38 (Penalties up to 2 yrs imprisonment / 2 lakh BDT fine).",
                                    isStatutory = true
                                )
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Bangladesh Boiler Rules, 2025 (S.R.O. No. 279-Law/2025)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Published 29 June 2025 (69 Rules). Rule 3 lifespan caps: Electrode <=10 yr, Small Industrial <=15 yr, Industrial Packaged = 30 yr, Field-Erected = 60 yr (up to +10 yr max Board-approved extension with NDT/hydraulic/steam tests). Annual renewal requires OCIB internal/external exam and live safety-valve steam test. Verify exact legal compliance clauses in the official Bangladesh Gazette.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            item {
                                Text(
                                    text = "Preliminary Safety Sizing Check",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }
                            items(statutoryCalcs, key = { "stat-calc-${it.id}" }) { calc ->
                                BoilerCalculationShortcutCard(
                                    calc = calc,
                                    onClick = { onNavigateToCalculation(calc.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoilerSteamTableShortcutCard(
    unitSystem: UnitSystem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics { Role.Button },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "IAPWS-IF97 Saturated Steam Table",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lookup T_sat, h_f, h_fg, h_g & density (${unitSystem.displayName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open Steam Table",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BoilerCalculationShortcutCard(
    calc: CalculationDefinition,
    onClick: () -> Unit
) {
    val isResearchPending = calc.status == CalculationStatus.RESEARCH_PENDING
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics { Role.Button },
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${calc.id}: ${calc.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                CalculationStatusBadge(status = calc.status)
            }

            if (calc.bengaliTitle.isNotBlank()) {
                Text(
                    text = calc.bengaliTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (calc.formulaLatex.isNotBlank()) {
                Text(
                    text = calc.formulaLatex,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (calc.engineeringReference.isNotBlank()) {
                Text(
                    text = "Ref: ${calc.engineeringReference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isResearchPending) {
                        "Execution disabled (inspect definition)"
                    } else {
                        "Open Calculation Runner"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isResearchPending) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = if (isResearchPending) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private object BoilerDashboardPreviewData {
    val gasBoiler = Equipment(
        id = "EQ-G01",
        categoryId = "CAT-G1",
        familyId = "FAM-G",
        name = "Natural Gas 3-Pass Firetube Boiler",
        bengaliName = "ন্যাচারাল গ্যাস থ্রি-পাস বয়লার",
        contentDepth = ContentDepth.FULL_BLUEPRINT,
        criticalityTier = CriticalityTier.SAFETY_CRITICAL,
        capacityRange = "6,000 kg/h @ 10.54 bar(g)"
    )
}

@Preview(name = "BoilerDashboard - Light Overview", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun BoilerDashboardLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        BoilerDashboardContent(
            uiState = BoilerUiState(
                isLoading = false,
                selectedTab = BoilerTab.OVERVIEW,
                boilerEquipment = listOf(BoilerDashboardPreviewData.gasBoiler),
                gasBoilers = listOf(BoilerDashboardPreviewData.gasBoiler)
            ),
            onOpenDrawer = {}
        )
    }
}

@Preview(name = "BoilerDashboard - Dark Gas Schematics", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun BoilerDashboardDarkGasPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        BoilerDashboardContent(
            uiState = BoilerUiState(
                isLoading = false,
                selectedTab = BoilerTab.GAS_BOILERS,
                boilerEquipment = listOf(BoilerDashboardPreviewData.gasBoiler),
                gasBoilers = listOf(BoilerDashboardPreviewData.gasBoiler)
            ),
            onOpenDrawer = {}
        )
    }
}
