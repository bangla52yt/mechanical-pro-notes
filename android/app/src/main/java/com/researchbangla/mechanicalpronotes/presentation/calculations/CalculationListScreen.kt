package com.researchbangla.mechanicalpronotes.presentation.calculations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationResultFormatter
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationHistoryItem
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.components.MathFormulaCard
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticDangerColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticWarningColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun extractCalculationCategories(calculations: List<CalculationDefinition>): List<String> {
    val dynamicCategories = calculations
        .map { it.category.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
    return listOf("All") + dynamicCategories
}

internal fun filterCalculationsByCategory(
    calculations: List<CalculationDefinition>,
    selectedCategory: String?
): List<CalculationDefinition> {
    if (selectedCategory == null || selectedCategory.equals("All", ignoreCase = true)) {
        return calculations
    }
    return calculations.filter { it.category.trim().equals(selectedCategory.trim(), ignoreCase = true) }
}

internal fun resolveHistoryRunnerCalculationId(
    historyCalculationId: String,
    calculations: List<CalculationDefinition>
): String? {
    val trimmed = historyCalculationId.trim()
    if (trimmed.isEmpty()) return null
    return calculations.firstOrNull { it.id.equals(trimmed, ignoreCase = true) }?.id
}

internal fun formatHistoryTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Unknown time"
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(timestamp))
}

@Composable
fun CalculationListScreen(
    viewModel: CalculationViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToRunner: (String) -> Unit,
    onNavigateToSteamTable: () -> Unit = {},
    onNavigateToUnitConverter: () -> Unit = {}
) {
    val state by viewModel.listState.collectAsState()
    CalculationListContent(
        state = state,
        onOpenDrawer = onOpenDrawer,
        onNavigateToRunner = onNavigateToRunner,
        onNavigateToSteamTable = onNavigateToSteamTable,
        onNavigateToUnitConverter = onNavigateToUnitConverter,
        onClearPendingHistoryFocus = viewModel::clearPendingHistoryFocus,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSelectCategory = viewModel::selectCategory,
        onClearFilters = viewModel::clearFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationListContent(
    state: CalculationListUiState,
    onOpenDrawer: () -> Unit,
    onNavigateToRunner: (String) -> Unit,
    onNavigateToSteamTable: () -> Unit = {},
    onNavigateToUnitConverter: () -> Unit = {},
    onClearPendingHistoryFocus: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSelectCategory: (String?) -> Unit = {},
    onClearFilters: () -> Unit = {}
) {
    var localSearchQuery by rememberSaveable(state.searchQuery) { mutableStateOf(state.searchQuery) }
    var localSelectedCategory by rememberSaveable(state.selectedCategory) { mutableStateOf(state.selectedCategory) }
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.pendingHistoryFocusId) {
        if (state.pendingHistoryFocusId != null) {
            showHistorySheet = true
        }
    }

    val categories = remember(state.calculations) {
        extractCalculationCategories(state.calculations)
    }
    val activeCategory = localSelectedCategory?.takeIf { sel ->
        categories.any { it.equals(sel, ignoreCase = true) }
    }
    val filteredList = remember(state.calculations, localSearchQuery, activeCategory) {
        CalculationCatalogFilter.filter(
            calculations = state.calculations,
            query = localSearchQuery,
            selectedCategory = activeCategory
        )
    }

    if (showHistorySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showHistorySheet = false
                onClearPendingHistoryFocus()
            },
            sheetState = sheetState
        ) {
            CalculationHistorySheetContent(
                history = state.history,
                availableCalculations = state.calculations,
                focusedHistoryId = state.pendingHistoryFocusId,
                onDismiss = {
                    showHistorySheet = false
                    onClearPendingHistoryFocus()
                },
                onOpenCalculation = { calcId ->
                    showHistorySheet = false
                    onClearPendingHistoryFocus()
                    onNavigateToRunner(calcId)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Engineering Calculations",
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
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Open Saved Calculation History (${state.history.size})",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToUnitConverter) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Open Unit Converter", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToSteamTable) {
                        Icon(Icons.Default.Science, contentDescription = "Open IAPWS-IF97 Steam Table", tint = MaterialTheme.colorScheme.primary)
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (state.errorMessage != null) {
                    item {
                        SafetyWarningCard(
                            title = "Calculation Registry Notice",
                            message = state.errorMessage,
                            isStatutory = true
                        )
                    }
                }

                // Steam Table Feature Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open IAPWS-IF97 Saturated Steam Table",
                                onClick = onNavigateToSteamTable
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "IAPWS-IF97 Saturated Steam Table",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "0.5 to 20 bar pressure-to-enthalpy offline lookup",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Unit Converter Feature Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Open Engineering Unit Converter",
                                onClick = onNavigateToUnitConverter
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Engineering Unit Converter (13 Categories)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Pressure (g/a), Temp, Power, Flow, Density, Viscosity & more",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Search Bar for Calculation ID, Title, Bengali, Category, or Variable
                item {
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { updated ->
                            localSearchQuery = updated
                            onSearchQueryChange(updated)
                        },
                        placeholder = {
                            Text(
                                text = "Search CALC ID, title, বাংলা, category, variable...",
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
                            if (localSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        localSearchQuery = ""
                                        onSearchQueryChange("")
                                    },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear calculation search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Horizontal Category Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = (activeCategory == null && cat == "All") || activeCategory.equals(cat, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val next = if (cat == "All") null else cat
                                    localSelectedCategory = next
                                    onSelectCategory(next)
                                },
                                label = { Text(cat) },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }

                // Section header with counter, unit system indicator, and saved history trigger
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Engineering Calculations (${filteredList.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = "Display Unit Preference: ${state.unitSystem.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("History (${state.history.size})")
                        }
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EmptyStateCard(
                                title = "No Matching Calculations Found",
                                message = if (localSearchQuery.isNotBlank()) {
                                    "No calculation matches \"${localSearchQuery.trim()}\" under the active category filter."
                                } else {
                                    "No calculation algorithms found for selected category."
                                }
                            )
                            if (localSearchQuery.isNotBlank() || activeCategory != null) {
                                OutlinedButton(
                                    onClick = {
                                        localSearchQuery = ""
                                        localSelectedCategory = null
                                        onClearFilters()
                                    },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Reset Search & Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { calc ->
                        CalculationItemCard(
                            calculation = calc,
                            onClick = { onNavigateToRunner(calc.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationItemCard(
    calculation: CalculationDefinition,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${calculation.id} ${calculation.title}",
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = calculation.id,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                CalculationStatusBadge(status = calculation.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = calculation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (calculation.bengaliTitle.isNotEmpty()) {
                Text(
                    text = calculation.bengaliTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            MathFormulaCard(
                title = "Governing Formula",
                formulaLatex = calculation.formulaLatex,
                engineeringReference = calculation.engineeringReference
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${calculation.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (calculation.status == CalculationStatus.IMPLEMENTED) "Execute Formula" else "Inspect Definition",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CalculationStatusBadge(status: CalculationStatus) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val (colors, label) = when (status) {
        CalculationStatus.IMPLEMENTED -> semanticPositiveColors(isDark) to "IMPLEMENTED"
        CalculationStatus.RESEARCH_PENDING -> semanticWarningColors(isDark) to "RESEARCH PENDING"
        CalculationStatus.NOT_IMPLEMENTED -> semanticDangerColors(isDark) to "NOT IMPLEMENTED"
    }

    Surface(
        color = colors.container,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.content,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CalculationHistorySheetContent(
    history: List<CalculationHistoryItem>,
    availableCalculations: List<CalculationDefinition>,
    focusedHistoryId: Long? = null,
    onDismiss: () -> Unit,
    onOpenCalculation: (String) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(focusedHistoryId, history) {
        if (focusedHistoryId != null) {
            val idx = history.indexOfFirst { it.id == focusedHistoryId }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Saved Calculation History (${history.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Offline runs saved from the Calculation Runner",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close calculation history")
            }
        }

        if (history.isEmpty()) {
            EmptyStateCard(
                title = "No Saved Calculation History",
                message = "Open any implemented formula, execute a calculation, and tap \"Save to Personal History\" to log timestamps, inputs, and results offline."
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = history,
                    key = { item -> "${item.id}_${item.calculationId}_${item.timestamp}" }
                ) { item ->
                    val resolvedCalcId = resolveHistoryRunnerCalculationId(item.calculationId, availableCalculations)
                    CalculationHistoryEntryCard(
                        item = item,
                        isFocused = focusedHistoryId != null && item.id == focusedHistoryId,
                        canReopen = resolvedCalcId != null,
                        onReopen = {
                            if (resolvedCalcId != null) {
                                onOpenCalculation(resolvedCalcId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationHistoryEntryCard(
    item: CalculationHistoryItem,
    isFocused: Boolean = false,
    canReopen: Boolean,
    onReopen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = if (isFocused) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.calculationId,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatHistoryTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }

            Text(
                text = item.calculationTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (item.outputValues.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Saved Results",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    item.outputValues.forEach { (key, value) ->
                        val (label, formattedVal) = CalculationResultFormatter.formatSecondaryEntry(key, value)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedVal,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            if (item.inputValues.isNotEmpty()) {
                val inputSummary = item.inputValues.entries.joinToString(" • ") { (k, v) ->
                    "$k = ${CalculationResultFormatter.formatNumber(v, k)}"
                }
                Text(
                    text = "Inputs: $inputSummary",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.userRemark.isNullOrBlank()) {
                Text(
                    text = "Notes: ${item.userRemark}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canReopen) {
                OutlinedButton(
                    onClick = onReopen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Open Calculator")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Text(
                    text = "Calculator route '${item.calculationId}' unavailable in current registry.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private object CalculationListPreviewData {
    val sampleCalcs = listOf(
        CalculationDefinition(
            id = "CALC-01",
            code = "CALC-01",
            title = "Direct Boiler Thermal Efficiency",
            bengaliTitle = "বয়লারের সরাসরি তাপীয় দক্ষতা",
            category = "Boiler Thermal",
            formulaLatex = "eta = [ m_s * (h_s - h_w) ] / [ m_f * NCV ] * 100",
            engineeringReference = "ASME PTC 4",
            assumptions = "Steady-state steam firing.",
            limitations = "Does not isolate unburnt solid-fuel losses.",
            status = CalculationStatus.IMPLEMENTED
        ),
        CalculationDefinition(
            id = "CALC-09",
            code = "CALC-09",
            title = "Preliminary Safety Valve Relief Check",
            bengaliTitle = "সেফটি ভালভ ক্যাপাসিটি যাচাই",
            category = "Boiler Thermal",
            formulaLatex = "W = 0.51 * K_d * A * P_relieving",
            engineeringReference = "Preliminary sizing guidance pending OCIB audit",
            assumptions = "Dry saturated steam discharge.",
            limitations = "Preliminary estimate only.",
            status = CalculationStatus.RESEARCH_PENDING
        )
    )
}

@Preview(name = "CalculationList - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun CalculationListLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        CalculationListContent(
            state = CalculationListUiState(isLoading = false, calculations = CalculationListPreviewData.sampleCalcs),
            onOpenDrawer = {},
            onNavigateToRunner = {}
        )
    }
}

@Preview(name = "CalculationList - Dark", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun CalculationListDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        CalculationListContent(
            state = CalculationListUiState(isLoading = false, calculations = CalculationListPreviewData.sampleCalcs),
            onOpenDrawer = {},
            onNavigateToRunner = {}
        )
    }
}

@Preview(name = "CalculationList - Empty", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun CalculationListEmptyPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        CalculationListContent(
            state = CalculationListUiState(isLoading = false, calculations = emptyList()),
            onOpenDrawer = {},
            onNavigateToRunner = {}
        )
    }
}
