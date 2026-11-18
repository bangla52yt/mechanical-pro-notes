package com.researchbangla.mechanicalpronotes.presentation.equipment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.SystemFamily
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.EquipmentCardItem
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@Composable
fun EquipmentListScreen(
    viewModel: EquipmentViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsState()
    EquipmentListContent(
        listState = listState,
        onOpenDrawer = onOpenDrawer,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToDetail = onNavigateToDetail,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onSelectFamily = { viewModel.selectFamily(it) },
        onClearDepthFilters = { viewModel.clearDepthFilters() },
        onToggleDepthFilter = { viewModel.toggleDepthFilter(it) },
        onClearAllFilters = { viewModel.clearAllFilters() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListContent(
    listState: EquipmentListUiState,
    onOpenDrawer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSelectFamily: (String?) -> Unit = {},
    onClearDepthFilters: () -> Unit = {},
    onToggleDepthFilter: (ContentDepth) -> Unit = {},
    onClearAllFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasActiveFilter = listState.selectedDepths.isNotEmpty() ||
        listState.searchQuery.isNotBlank() ||
        listState.selectedFamilyId != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val total = listState.allEquipment.size.takeIf { it > 0 } ?: 64
                    Text(
                        text = "Plant Equipment (${listState.equipmentList.size}/$total)",
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
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Global Plant Search")
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
                .imePadding()
        ) {
            if (listState.errorMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SafetyWarningCard(
                        title = "Equipment Catalog Notice",
                        message = listState.errorMessage,
                        isStatutory = true
                    )
                }
            }

            // Inline equipment filter search bar
            OutlinedTextField(
                value = listState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Filter by ID, English, বাংলা, drive type...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                trailingIcon = {
                    if (listState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear filter text")
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // System Family Tabs ("All" + 16 Families)
            if (listState.systemFamilies.isNotEmpty()) {
                val selectedTabIndex = if (listState.selectedFamilyId == null) {
                    0
                } else {
                    (listState.systemFamilies.indexOfFirst { it.id == listState.selectedFamilyId } + 1)
                        .coerceAtLeast(0)
                }
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = listState.selectedFamilyId == null,
                        onClick = { onSelectFamily(null) },
                        text = { Text("All (${listState.allEquipment.size})") }
                    )
                    listState.systemFamilies.forEach { family ->
                        Tab(
                            selected = listState.selectedFamilyId == family.id,
                            onClick = { onSelectFamily(family.id) },
                            text = { Text("${family.code}: ${family.name}") }
                        )
                    }
                }
            }

            // Multi-select ContentDepth filter chips reflecting actual database classifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = listState.selectedDepths.isEmpty(),
                    onClick = onClearDepthFilters,
                    label = {
                        val totalInFamily = listState.depthCounts.values.sum()
                        Text("All ($totalInFamily)")
                    }
                )
                listOf(
                    ContentDepth.FULL_BLUEPRINT to "Full",
                    ContentDepth.PARTIAL_BLUEPRINT to "Partial",
                    ContentDepth.INVENTORY_ONLY to "Inventory Only",
                    ContentDepth.RESEARCH_PENDING to "Research Pending"
                ).forEach { (depth, shortLabel) ->
                    val count = listState.depthCounts[depth] ?: 0
                    FilterChip(
                        selected = depth in listState.selectedDepths,
                        onClick = { onToggleDepthFilter(depth) },
                        label = { Text("$shortLabel ($count)") }
                    )
                }
            }

            if (listState.isLoading && listState.allEquipment.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (listState.equipmentList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyStateCard(
                        title = "No Matching Equipment",
                        message = "No equipment matches the selected family, content-depth status chips, or search text."
                    )
                    if (hasActiveFilter) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onClearAllFilters,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listState.equipmentList, key = { it.id }) { equipment ->
                        EquipmentCardItem(
                            equipment = equipment,
                            onClick = { onNavigateToDetail(equipment.id) }
                        )
                    }
                }
            }
        }
    }
}

private object EquipmentListPreviewData {
    val sampleFamilies = listOf(
        SystemFamily("FAM-A", "A", "Washers", "ওয়াশার", "washer", 1),
        SystemFamily("FAM-G", "G", "Boilers", "বয়লার", "boiler", 7, isDedicatedBoilerModule = true)
    )
    val sampleItems = listOf(
        Equipment(
            id = "EQ-A01",
            categoryId = "CAT-A1",
            familyId = "FAM-A",
            name = "Belly-Type Garment Washing Machine",
            bengaliName = "বেলি-টাইপ গার্মেন্টস ওয়াশিং মেশিন",
            contentDepth = ContentDepth.FULL_BLUEPRINT,
            criticalityTier = CriticalityTier.PRIMARY_PRODUCTION,
            capacityRange = "100 – 450 kg"
        ),
        Equipment(
            id = "EQ-G02",
            categoryId = "CAT-G2",
            familyId = "FAM-G",
            name = "Solid-Fuel (Jhut) Firetube Boiler",
            bengaliName = "ঝুট ও সলিড ফুয়েল বয়লার",
            contentDepth = ContentDepth.PARTIAL_BLUEPRINT,
            criticalityTier = CriticalityTier.SAFETY_CRITICAL,
            capacityRange = "2,000 – 8,000 kg/h"
        )
    )
}

@Preview(name = "EquipmentList - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun EquipmentListLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        EquipmentListContent(
            listState = EquipmentListUiState(
                isLoading = false,
                systemFamilies = EquipmentListPreviewData.sampleFamilies,
                allEquipment = EquipmentListPreviewData.sampleItems,
                equipmentList = EquipmentListPreviewData.sampleItems,
                depthCounts = mapOf(ContentDepth.FULL_BLUEPRINT to 1, ContentDepth.PARTIAL_BLUEPRINT to 1)
            ),
            onOpenDrawer = {},
            onNavigateToSearch = {},
            onNavigateToDetail = {}
        )
    }
}

@Preview(name = "EquipmentList - Dark Empty Filter", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun EquipmentListEmptyPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        EquipmentListContent(
            listState = EquipmentListUiState(
                isLoading = false,
                selectedFamilyId = "FAM-G",
                searchQuery = "Chiller",
                systemFamilies = EquipmentListPreviewData.sampleFamilies,
                allEquipment = EquipmentListPreviewData.sampleItems,
                equipmentList = emptyList()
            ),
            onOpenDrawer = {},
            onNavigateToSearch = {},
            onNavigateToDetail = {}
        )
    }
}
