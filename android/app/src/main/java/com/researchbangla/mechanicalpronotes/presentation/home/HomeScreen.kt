package com.researchbangla.mechanicalpronotes.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.SystemFamily
import com.researchbangla.mechanicalpronotes.domain.model.UserBookmark
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.EquipmentCardItem
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToBoiler: () -> Unit,
    onNavigateToCalculations: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToUnitConverter: () -> Unit = {},
    onNavigateToFundamentals: () -> Unit = {},
    onNavigateToChecklists: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSteamTable: () -> Unit = {},
    onNavigateToBookmark: (String, BookmarkEntityType) -> Unit = { _, _ -> },
    onRemoveBookmark: (UserBookmark) -> Unit = {},
    onToggleEquipmentBookmark: (Equipment) -> Unit = {},
    onNavigateToActiveChecklist: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onOpenDrawer = onOpenDrawer,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToEquipment = onNavigateToEquipment,
        onNavigateToBoiler = onNavigateToBoiler,
        onNavigateToCalculations = onNavigateToCalculations,
        onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
        onNavigateToUnitConverter = onNavigateToUnitConverter,
        onNavigateToFundamentals = onNavigateToFundamentals,
        onNavigateToChecklists = onNavigateToChecklists,
        onNavigateToNotes = onNavigateToNotes,
        onNavigateToBookmarks = onNavigateToBookmarks,
        onNavigateToSteamTable = onNavigateToSteamTable,
        onNavigateToBookmark = onNavigateToBookmark,
        onRemoveBookmark = onRemoveBookmark,
        onToggleEquipmentBookmark = onToggleEquipmentBookmark,
        onNavigateToActiveChecklist = onNavigateToActiveChecklist,
        onNavigateToCalculation = onNavigateToCalculation,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onOpenDrawer: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToBoiler: () -> Unit,
    onNavigateToCalculations: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToUnitConverter: () -> Unit = {},
    onNavigateToFundamentals: () -> Unit = {},
    onNavigateToChecklists: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSteamTable: () -> Unit = {},
    onNavigateToBookmark: (String, BookmarkEntityType) -> Unit = { _, _ -> },
    onRemoveBookmark: (UserBookmark) -> Unit = {},
    onToggleEquipmentBookmark: (Equipment) -> Unit = {},
    onNavigateToActiveChecklist: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val fullColor = if (isDark) OperationalDarkGreen else OperationalGreen
    val partialColor = if (isDark) SteelBlueDarkPrimary else PartialBlueprintBlue
    val pendingColor = if (isDark) SafetyAmberDarkWarning else ResearchPendingOrange
    val inventoryColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else InventoryOnlyGrey
    val uniqueBookmarks = remember(uiState.bookmarks) {
        uiState.bookmarks
            .filter { it.entityId.isNotBlank() }
            .distinctBy { it.entityType to it.entityId.trim().uppercase() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mechanical Pro Notes",
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
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Open Offline Knowledge Search")
                    }
                    IconButton(
                        onClick = onNavigateToBookmarks,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Open Saved Bookmarks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.totalSeededEquipmentCount == 0 && uiState.errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        SafetyWarningCard(
                            title = "Dashboard Data Notice",
                            message = uiState.errorMessage,
                            isStatutory = true
                        )
                    }
                }

                // 1. Welcome & Search Trigger Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open offline knowledge search",
                                onClick = onNavigateToSearch
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Plant Offline Knowledge Search",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Search ${uiState.totalSeededEquipmentCount} machines, checklists, fundamentals, boiler laws (EN & বাংলা)...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SEARCH",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. High-Priority Dedicated Boiler Hub Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open dedicated industrial boiler module",
                                onClick = onNavigateToBoiler
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DEDICATED BOILER MODULE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "TOP PRIORITY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Industrial Steam Boilers & Heat Recovery",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Gas-Fired 3-Pass Wet-Back • Jhut/Solid-Fuel Boilers • The Boilers Act 2022 & Rules 2025 • Efficiency & Blowdown Tools",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // 3. Quick Action Buttons Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToEquipment,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Equipment (${uiState.totalSeededEquipmentCount})",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToCalculations,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Calculations (20)",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToUnitConverter,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Unit Converter (13)",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToFundamentals,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Fundamentals (15)",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToChecklists,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Checklists",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToNotes,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Notes",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToBookmarks,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Bookmarks (${uniqueBookmarks.size})",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = onNavigateToSteamTable,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Steam Table",
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 3B. Pinned / Saved Bookmarks Strip
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uniqueBookmarks.isEmpty()) {
                                    "Saved Bookmarks"
                                } else {
                                    "Saved Bookmarks (${uniqueBookmarks.size})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.semantics { heading() }
                            )
                            TextButton(
                                onClick = onNavigateToBookmarks,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("View All")
                            }
                        }

                        if (uniqueBookmarks.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "No bookmarks saved yet",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap the bookmark icon on equipment, boiler sections, calculations, checklists, references, or fundamentals to pin quick shortcuts here.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(
                                    items = uniqueBookmarks,
                                    key = { "${it.entityType.name}:${it.entityId.trim().uppercase()}" }
                                ) { bookmark ->
                                    HomePinnedBookmarkCard(
                                        bookmark = bookmark,
                                        onClick = { onNavigateToBookmark(bookmark.entityId, bookmark.entityType) },
                                        onRemove = { onRemoveBookmark(bookmark) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3C. Recent Activity & Checklist Status Summary
                item {
                    HomeRecentActivityCard(
                        summary = uiState.activitySummary,
                        onOpenChecklists = {
                            val activeId = uiState.activitySummary.activeChecklistId
                            if (activeId != null) {
                                onNavigateToActiveChecklist(activeId)
                            } else {
                                onNavigateToChecklists()
                            }
                        },
                        onOpenNotes = onNavigateToNotes,
                        onOpenCalculation = { calcId -> onNavigateToCalculation(calcId) }
                    )
                }

                // 4. Verified Knowledge Base Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "OFFLINE KNOWLEDGE BASE (${uiState.totalSeededEquipmentCount} ASSETS / ${uiState.systemFamilies.size} FAMILIES)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCounterColumn(
                                    count = uiState.fullBlueprintCount,
                                    label = "Full",
                                    color = fullColor,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCounterColumn(
                                    count = uiState.partialBlueprintCount,
                                    label = "Partial",
                                    color = partialColor,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCounterColumn(
                                    count = uiState.researchPendingCount,
                                    label = "Pending",
                                    color = pendingColor,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCounterColumn(
                                    count = uiState.inventoryOnlyCount,
                                    label = "Inventory",
                                    color = inventoryColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All ${uiState.totalSeededEquipmentCount} plant equipment assets indexed in FTS4 offline search with ${uiState.dedicatedBoilersCount} dedicated boiler assets.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 5. Featured Verified Machinery
                item {
                    Text(
                        text = "Featured Blueprint Equipment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                if (uiState.featuredEquipment.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Featured Assets Loaded",
                            message = "Browse all plant modules from the navigation drawer or verify local offline database integrity."
                        )
                    }
                } else {
                    items(uiState.featuredEquipment, key = { it.id }) { equip ->
                        val isEquipBookmarked = equip.id.trim().uppercase() in uiState.bookmarkedEquipmentIds
                        EquipmentCardItem(
                            equipment = equip,
                            onClick = { onNavigateToEquipmentDetail(equip.id) },
                            isBookmarked = isEquipBookmarked,
                            onToggleBookmark = { onToggleEquipmentBookmark(equip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCounterColumn(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeRecentActivityCard(
    summary: HomeActivitySummary,
    onOpenChecklists: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenCalculation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity & Checklists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                TextButton(
                    onClick = onOpenChecklists,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = "${summary.completedChecklistsCount}/${summary.totalChecklistsCount} Done"
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Open checklist workflow",
                        onClick = onOpenChecklists
                    ),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = summary.activeChecklistTitle ?: "All ${summary.totalChecklistsCount} checklists completed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (summary.activeChecklistTitle != null) {
                                    "${summary.activeChecklistProgress ?: "Ready"} • ${summary.pendingChecklistsCount} pending"
                                } else {
                                    "Tap to review completed maintenance checks"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!summary.hasRecentActivity) {
                Text(
                    text = "No completed checks, notes, or calculation history saved yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (summary.notesCount > 0 && summary.latestNoteTitle != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open plant notes",
                                onClick = onOpenNotes
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Latest Note: ${summary.latestNoteTitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${summary.notesCount} saved note(s)" +
                                        (summary.latestNoteEquipmentId?.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (summary.calculationHistoryCount > 0 &&
                    summary.latestCalculationId != null &&
                    summary.latestCalculationTitle != null
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Re-open recent calculation",
                                onClick = { onOpenCalculation(summary.latestCalculationId) }
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Last Calculation: ${summary.latestCalculationTitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${summary.calculationHistoryCount} saved run(s) • ${summary.latestCalculationId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun HomePinnedBookmarkCard(
    bookmark: UserBookmark,
    onClick: () -> Unit,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cleanId = bookmark.entityId.trim()
    val displayTitle = bookmark.entityTitle.trim().ifEmpty { cleanId }
    val typeBadge = when (bookmark.entityType) {
        BookmarkEntityType.EQUIPMENT -> "EQUIP"
        BookmarkEntityType.BOILER_SECTION -> "BOILER"
        BookmarkEntityType.CALCULATION -> "CALC"
        BookmarkEntityType.CHECKLIST -> "CHECKLIST"
        BookmarkEntityType.TECHNICAL_STANDARD -> {
            if (cleanId.startsWith("FUND-", ignoreCase = true)) "FUNDAMENTAL" else "REFERENCE"
        }
    }
    val typeIcon = when (bookmark.entityType) {
        BookmarkEntityType.EQUIPMENT -> Icons.Default.PrecisionManufacturing
        BookmarkEntityType.BOILER_SECTION -> Icons.Default.LocalFireDepartment
        BookmarkEntityType.CALCULATION -> Icons.Default.Calculate
        BookmarkEntityType.CHECKLIST -> Icons.Default.Checklist
        BookmarkEntityType.TECHNICAL_STANDARD -> Icons.Default.MenuBook
    }

    Card(
        modifier = modifier
            .widthIn(min = 204.dp, max = 252.dp)
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open $displayTitle",
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 6.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cleanId,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = typeBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = "Remove bookmark for $displayTitle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

private object HomePreviewData {
    val sampleState = HomeUiState(
        isLoading = false,
        systemFamilies = List(16) { idx ->
            SystemFamily(
                id = "FAM-${'A' + idx}",
                code = "${'A' + idx}",
                name = "System ${idx + 1}",
                bengaliName = "সিস্টেম ${idx + 1}",
                iconName = "precision",
                sortOrder = idx + 1
            )
        },
        totalSeededEquipmentCount = 64,
        fullBlueprintCount = 24,
        partialBlueprintCount = 16,
        researchPendingCount = 10,
        inventoryOnlyCount = 14,
        dedicatedBoilersCount = 4,
        featuredEquipment = listOf(
            Equipment(
                id = "EQ-A01",
                categoryId = "CAT-A1",
                familyId = "FAM-A",
                name = "Belly-Type Garment Washing Machine",
                bengaliName = "বেলি-টাইপ গার্মেন্টস ওয়াশিং মেশিন",
                contentDepth = ContentDepth.FULL_BLUEPRINT,
                criticalityTier = CriticalityTier.PRIMARY_PRODUCTION,
                capacityRange = "100 – 450 kg dry garment load"
            ),
            Equipment(
                id = "EQ-G01",
                categoryId = "CAT-G1",
                familyId = "FAM-G",
                name = "3-Pass Firetube Natural Gas Boiler",
                bengaliName = "৩-পাস ন্যাচারাল গ্যাস ফায়ারটিউব বয়লার",
                contentDepth = ContentDepth.FULL_BLUEPRINT,
                criticalityTier = CriticalityTier.SAFETY_CRITICAL,
                capacityRange = "4,000 – 12,000 kg/h"
            )
        ),
        bookmarks = listOf(
            UserBookmark(
                id = 1L,
                entityType = BookmarkEntityType.EQUIPMENT,
                entityId = "EQ-G01",
                entityTitle = "3-Pass Firetube Natural Gas Boiler"
            ),
            UserBookmark(
                id = 2L,
                entityType = BookmarkEntityType.CALCULATION,
                entityId = "CALC-01",
                entityTitle = "Direct Boiler Thermal Efficiency"
            )
        ),
        bookmarkedEquipmentIds = setOf("EQ-G01"),
        activitySummary = HomeActivitySummary(
            totalChecklistsCount = 4,
            completedChecklistsCount = 1,
            inProgressChecklistsCount = 1,
            pendingChecklistsCount = 3,
            activeChecklistId = "CHK-01",
            activeChecklistTitle = "Boiler Daily Safety & Pre-Start Checklist",
            activeChecklistProgress = "5 / 12 checks",
            notesCount = 2,
            latestNoteTitle = "Checked sight glass blowdown valve",
            latestNoteEquipmentId = "EQ-G01",
            calculationHistoryCount = 1,
            latestCalculationId = "CALC-01",
            latestCalculationTitle = "Direct Boiler Thermal Efficiency"
        )
    )
}

@Preview(name = "HomeScreen - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun HomeScreenLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        HomeScreenContent(
            uiState = HomePreviewData.sampleState,
            onOpenDrawer = {},
            onNavigateToSearch = {},
            onNavigateToEquipment = {},
            onNavigateToBoiler = {},
            onNavigateToCalculations = {},
            onNavigateToEquipmentDetail = {}
        )
    }
}

@Preview(name = "HomeScreen - Dark High Legibility", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun HomeScreenDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        HomeScreenContent(
            uiState = HomePreviewData.sampleState,
            onOpenDrawer = {},
            onNavigateToSearch = {},
            onNavigateToBoiler = {},
            onNavigateToEquipment = {},
            onNavigateToCalculations = {},
            onNavigateToEquipmentDetail = {}
        )
    }
}

@Preview(name = "HomeScreen - Empty State", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun HomeScreenEmptyPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        HomeScreenContent(
            uiState = HomeUiState(isLoading = false, featuredEquipment = emptyList()),
            onOpenDrawer = {},
            onNavigateToSearch = {},
            onNavigateToEquipment = {},
            onNavigateToBoiler = {},
            onNavigateToCalculations = {},
            onNavigateToEquipmentDetail = {}
        )
    }
}
