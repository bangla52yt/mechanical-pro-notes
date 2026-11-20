package com.researchbangla.mechanicalpronotes.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.catalog.CatalogSearchHit
import com.researchbangla.mechanicalpronotes.domain.catalog.SearchResultType
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationHistorySearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationSearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.UserNoteSearchHit
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.EquipmentCardItem
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEquipmentDetail: (String) -> Unit,
    onNavigateToChecklist: (String) -> Unit = {},
    onNavigateToReference: (String) -> Unit = {},
    onNavigateToFundamentals: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    onNavigateToUserNote: (Long, String) -> Unit = { _, _ -> },
    onNavigateToCalculationHistory: (Long) -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    SearchScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onQueryChange = { viewModel.onQueryChange(it) },
        onClearSearch = { viewModel.clearSearch() },
        onSelectScope = { viewModel.selectScope(it) },
        onFilterByFamily = { viewModel.filterByFamily(it) },
        onToggleDepthFilter = { viewModel.toggleDepthFilter(it) },
        onClearFilters = { viewModel.clearFilters() },
        onNavigateToEquipmentDetail = onNavigateToEquipmentDetail,
        onNavigateToChecklist = onNavigateToChecklist,
        onNavigateToReference = onNavigateToReference,
        onNavigateToFundamentals = onNavigateToFundamentals,
        onNavigateToCalculation = onNavigateToCalculation,
        onNavigateToUserNote = onNavigateToUserNote,
        onNavigateToCalculationHistory = onNavigateToCalculationHistory,
        onNavigateToBookmarks = onNavigateToBookmarks
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    state: SearchUiState,
    onNavigateBack: () -> Unit,
    onQueryChange: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onSelectScope: (SearchScope) -> Unit = {},
    onFilterByFamily: (String?) -> Unit = {},
    onToggleDepthFilter: (ContentDepth) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onNavigateToEquipmentDetail: (String) -> Unit = {},
    onNavigateToChecklist: (String) -> Unit = {},
    onNavigateToReference: (String) -> Unit = {},
    onNavigateToFundamentals: (String) -> Unit = {},
    onNavigateToCalculation: (String) -> Unit = {},
    onNavigateToUserNote: (Long, String) -> Unit = { _, _ -> },
    onNavigateToCalculationHistory: (Long) -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {}
) {
    val filteredEquipment = remember(
        state.results,
        state.selectedScope,
        state.selectedFamilyFilter,
        state.selectedDepthFilters
    ) { state.filteredEquipment }
    val filteredCatalog = remember(state.catalogHits, state.selectedScope) { state.filteredCatalogHits }
    val filteredCalculations = remember(state.calculationHits, state.selectedScope) { state.filteredCalculationHits }
    val filteredNotes = remember(state.userNoteHits, state.selectedScope) { state.filteredUserNoteHits }
    val filteredHistory = remember(state.calculationHistoryHits, state.selectedScope) { state.filteredCalculationHistoryHits }
    val totalMatches = filteredEquipment.size +
        filteredCalculations.size +
        filteredCatalog.size +
        filteredNotes.size +
        filteredHistory.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        placeholder = {
                            Text(
                                text = "Search machines, calcs, notes, laws...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearSearch,
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search query")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Knowledge scope selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchScope.entries.forEach { scope ->
                    FilterChip(
                        selected = state.selectedScope == scope,
                        onClick = { onSelectScope(scope) },
                        label = { Text(scope.label) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                }
            }

            // Family & status filter chips (shown when Equipment scope is active)
            if (state.selectedScope == SearchScope.ALL || state.selectedScope == SearchScope.EQUIPMENT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val familyChips = listOf(
                        null to "All Families",
                        "FAM-A" to "Washers (FAM-A)",
                        "FAM-B" to "Hydro & Dryers (FAM-B)",
                        "FAM-C" to "Finishing (FAM-C)",
                        "FAM-D" to "Pumps (FAM-D)",
                        "FAM-E" to "Drives & Bearings (FAM-E)",
                        "FAM-F" to "Steam & Valves (FAM-F)",
                        "FAM-G" to "Boilers (FAM-G)",
                        "FAM-H" to "Water & Softener (FAM-H)",
                        "FAM-I" to "Chemical Dosing (FAM-I)",
                        "FAM-J" to "Pneumatics (FAM-J)",
                        "FAM-K" to "Cooling & HVAC (FAM-K)",
                        "FAM-L" to "ETP (FAM-L)",
                        "FAM-M" to "Power & Drives (FAM-M)",
                        "FAM-N" to "Workshop & NDT (FAM-N)",
                        "FAM-O" to "Fire & Gas Safety (FAM-O)",
                        "FAM-P" to "Exhaust Blowers (FAM-P)"
                    )
                    familyChips.forEach { (familyId, label) ->
                        FilterChip(
                            selected = state.selectedFamilyFilter == familyId,
                            onClick = { onFilterByFamily(familyId) },
                            label = { Text(label) }
                        )
                    }
                }

                // Multi-select ContentDepth filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ContentDepth.FULL_BLUEPRINT to "Full",
                        ContentDepth.PARTIAL_BLUEPRINT to "Partial",
                        ContentDepth.INVENTORY_ONLY to "Inventory Only",
                        ContentDepth.RESEARCH_PENDING to "Research Pending"
                    ).forEach { (depth, label) ->
                        FilterChip(
                            selected = depth in state.selectedDepthFilters,
                            onClick = { onToggleDepthFilter(depth) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Results content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SafetyWarningCard(
                                title = "Error: Search Index Unavailable",
                                message = state.errorMessage,
                                isStatutory = true
                            )
                        }
                    }
                    state.query.trim().length < 2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateCard(
                                title = "Unified Offline Plant Search",
                                message = "Search Equipment (EQ-A01, বয়লার), Calculations (CALC-01, efficiency, pump), My Field Notes, Calculation History, Checklists, Technical References, and Mechanical Fundamentals."
                            )
                        }
                    }
                    state.hasSearched && totalMatches == 0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            EmptyStateCard(
                                title = "No Matching Records Found",
                                message = "No matching equipment, calculations, notes, calculation history, checklists, references, or fundamentals for \"${state.query}\" under the active filters."
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onClearFilters,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Reset Scope & Status Filters")
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item(key = "results-summary") {
                                Text(
                                    text = "Results ($totalMatches): ${filteredEquipment.size} Equip • ${filteredCalculations.size} Calcs • ${filteredNotes.size} Notes • ${filteredHistory.size} History • ${filteredCatalog.size} Refs",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.semantics { heading() }
                                )
                            }

                            if (filteredEquipment.isNotEmpty()) {
                                item(key = "hdr-equipment") {
                                    SearchSectionHeader(title = "Plant Equipment (${filteredEquipment.size})")
                                }
                                items(filteredEquipment, key = { it.id }) { equipment ->
                                    EquipmentCardItem(
                                        equipment = equipment,
                                        onClick = { onNavigateToEquipmentDetail(equipment.id) }
                                    )
                                }
                            }

                            if (filteredCalculations.isNotEmpty()) {
                                item(key = "hdr-calculations") {
                                    SearchSectionHeader(title = "Engineering Calculations (${filteredCalculations.size})")
                                }
                                items(filteredCalculations, key = { "calc-${it.id}" }) { calc ->
                                    CalculationSearchHitCard(
                                        hit = calc,
                                        onClick = { onNavigateToCalculation(calc.id) }
                                    )
                                }
                            }

                            if (filteredNotes.isNotEmpty()) {
                                item(key = "hdr-notes") {
                                    SearchSectionHeader(title = "Personal Field Notes (${filteredNotes.size})")
                                }
                                items(filteredNotes, key = { "note-${it.id}" }) { note ->
                                    UserNoteSearchHitCard(
                                        hit = note,
                                        onClick = { onNavigateToUserNote(note.id, note.equipmentId) }
                                    )
                                }
                            }

                            if (filteredHistory.isNotEmpty()) {
                                item(key = "hdr-history") {
                                    SearchSectionHeader(title = "Saved Calculation History (${filteredHistory.size})")
                                }
                                items(filteredHistory, key = { "hist-${it.id}" }) { hist ->
                                    CalculationHistorySearchHitCard(
                                        hit = hist,
                                        onClick = { onNavigateToCalculationHistory(hist.id) }
                                    )
                                }
                            }

                            if (filteredCatalog.isNotEmpty()) {
                                item(key = "hdr-catalog") {
                                    SearchSectionHeader(title = "Checklists, References & Fundamentals (${filteredCatalog.size})")
                                }
                                items(filteredCatalog, key = { "cat-${it.type.name}-${it.id}" }) { hit ->
                                    CatalogSearchHitCard(
                                        hit = hit,
                                        onClick = {
                                            when (hit.type) {
                                                SearchResultType.CHECKLIST -> onNavigateToChecklist(hit.id)
                                                SearchResultType.REFERENCE -> onNavigateToReference(hit.id)
                                                SearchResultType.FUNDAMENTAL -> onNavigateToFundamentals(hit.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 6.dp)
            .semantics { heading() }
    )
}

@Composable
private fun CalculationSearchHitCard(
    hit: CalculationSearchHit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open calculation ${hit.id} ${hit.title}",
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = hit.id,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "CALCULATION • ${hit.category}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (hit.bengaliTitle.isNotBlank()) {
                Text(
                    text = hit.bengaliTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val footer = buildString {
                append(hit.engineeringReference)
                if (hit.status == CalculationStatus.RESEARCH_PENDING) {
                    append(" • RESEARCH PENDING")
                }
            }
            if (footer.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = footer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserNoteSearchHitCard(
    hit: UserNoteSearchHit,
    onClick: () -> Unit
) {
    val dateText = remember(hit.updatedAt) {
        if (hit.updatedAt > 0L) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(hit.updatedAt))
        } else {
            ""
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open personal note ${hit.displayTitle}",
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    val isUnassociated = NoteAssociationHelper.isUnassociated(hit.equipmentId)
                    val assetLabel = when {
                        isUnassociated -> "General / Unassociated Note"
                        !hit.equipmentName.isNullOrBlank() -> "${hit.equipmentId} • ${hit.equipmentName}"
                        else -> "Asset: ${hit.equipmentId}"
                    }
                    Text(
                        text = assetLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "USER NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hit.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = hit.bodySnippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (hit.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hit.tags.joinToString(" • ") { "#$it" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (dateText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Updated: $dateText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalculationHistorySearchHitCard(
    hit: CalculationHistorySearchHit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open saved history for ${hit.calculationTitle} from ${hit.formattedTimestamp}",
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = hit.calculationId,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "CALC HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hit.calculationTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Run: ${hit.formattedTimestamp}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hit.resultSummary,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hit.inputsSummary.isNotBlank()) {
                Text(
                    text = "Inputs: ${hit.inputsSummary}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CatalogSearchHitCard(
    hit: CatalogSearchHit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${hit.title}",
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hit.id,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = hit.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = hit.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private object SearchPreviewData {
    val sampleResults = SearchUiState(
        query = "Boiler",
        hasSearched = true,
        results = listOf(
            Equipment(
                id = "EQ-G01",
                categoryId = "CAT-G1",
                familyId = "FAM-G",
                name = "Natural Gas 3-Pass Firetube Boiler",
                bengaliName = "ন্যাচারাল গ্যাস থ্রি-পাস বয়লার",
                contentDepth = ContentDepth.FULL_BLUEPRINT,
                criticalityTier = CriticalityTier.SAFETY_CRITICAL,
                capacityRange = "6,000 kg/h"
            )
        ),
        calculationHits = listOf(
            CalculationSearchHit(
                id = "CALC-01",
                code = "BOILER_DIRECT_EFFICIENCY",
                title = "Boiler Thermal Efficiency (Direct Method)",
                bengaliTitle = "বয়লার থার্মাল এফিসিয়েন্সি (ডাইরেক্ট মেথড)",
                category = "Boiler Thermal",
                engineeringReference = "ASME PTC 4.1 / BS EN 12953",
                status = CalculationStatus.IMPLEMENTED
            )
        ),
        userNoteHits = listOf(
            UserNoteSearchHit(
                id = 1L,
                equipmentId = "EQ-G01",
                displayTitle = "Morning Boiler Burner Inspection",
                bodySnippet = "Checked pilot flame sensor and flue stack temperature at 184 C.",
                updatedAt = 1731648000000L
            )
        ),
        calculationHistoryHits = listOf(
            CalculationHistorySearchHit(
                id = 5L,
                calculationId = "CALC-01",
                calculationTitle = "Boiler Thermal Efficiency (Direct Method)",
                timestamp = 1731648000000L,
                formattedTimestamp = "15 Nov 2024, 14:20",
                resultSummary = "Thermal Efficiency: 85.30 %",
                inputsSummary = "m_s = 4000.0 • m_f = 320.0"
            )
        ),
        catalogHits = listOf(
            CatalogSearchHit(
                id = "CHK-BLR-DAILY",
                type = SearchResultType.CHECKLIST,
                title = "Daily Boiler Pre-Start & Blowdown",
                subtitle = "The Boilers Act, 2022 (Sec 18–19) / OCIB Operational Practice",
                badgeLabel = "CHECKLIST"
            ),
            CatalogSearchHit(
                id = "REF-01",
                type = SearchResultType.REFERENCE,
                title = "The Boilers Act, 2022 (Act No. 06 of 2022)",
                subtitle = "Bangladesh Parliament • Statutory Mandate",
                badgeLabel = "REFERENCE"
            )
        )
    )
}

@Preview(name = "SearchScreen - Light Empty Prompt", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun SearchLightPromptPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SearchScreenContent(
            state = SearchUiState(),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "SearchScreen - Dark Results High Legibility", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun SearchDarkResultsPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        SearchScreenContent(
            state = SearchPreviewData.sampleResults,
            onNavigateBack = {}
        )
    }
}

@Preview(name = "SearchScreen - Light No Matches", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun SearchNoMatchesPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SearchScreenContent(
            state = SearchUiState(query = "Turbine XYZ", hasSearched = true),
            onNavigateBack = {}
        )
    }
}
