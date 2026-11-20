package com.researchbangla.mechanicalpronotes.presentation.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.unit.EngineeringUnit
import com.researchbangla.mechanicalpronotes.domain.unit.UnitCategory
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConversionResult
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConverterEngine
import com.researchbangla.mechanicalpronotes.domain.unit.UnitSubgroup
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode

@Composable
fun UnitConverterScreen(
    stateHolder: UnitConverterStateHolder,
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null
) {
    val state by stateHolder.uiState.collectAsState()
    UnitConverterContent(
        state = state,
        onSelectCategory = { stateHolder.selectCategory(it) },
        onInputChange = { stateHolder.onInputChange(it) },
        onSelectFromUnit = { stateHolder.selectFromUnit(it) },
        onSelectToUnit = { stateHolder.selectToUnit(it) },
        onSwapUnits = { stateHolder.swapUnits() },
        onNavigateBack = onNavigateBack,
        onOpenDrawer = onOpenDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterContent(
    state: UnitConverterUiState,
    onSelectCategory: (UnitCategory) -> Unit = {},
    onInputChange: (String) -> Unit = {},
    onSelectFromUnit: (String) -> Unit = {},
    onSelectToUnit: (String) -> Unit = {},
    onSwapUnits: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Engineering Unit Converter",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "13 Mechanical Categories • ${state.preferredUnitSystem.displayName} Preset • 100% Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Category selector strip (all 13 categories)
            item {
                Text(
                    text = "Select Physical Quantity (${UnitCategory.entries.size} Categories)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitCategory.entries.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category.displayName) }
                        )
                    }
                }
                state.selectedCategory.note?.let { note ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Numeric Input & From/To Unit Pickers
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = onInputChange,
                            label = { Text("Input Value (${state.availableUnits.firstOrNull { it.code == state.fromUnitCode }?.symbol ?: ""})") },
                            singleLine = true,
                            isError = state.conversionResult is UnitConversionResult.Error,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            if (maxWidth < 400.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    UnitDropdownField(
                                        label = "From Unit",
                                        selectedCode = state.fromUnitCode,
                                        units = state.availableUnits,
                                        onSelect = onSelectFromUnit,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        OutlinedButton(
                                            onClick = onSwapUnits,
                                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Swap From & To Units")
                                        }
                                    }
                                    UnitDropdownField(
                                        label = "To Unit",
                                        selectedCode = state.toUnitCode,
                                        units = state.availableUnits,
                                        onSelect = onSelectToUnit,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UnitDropdownField(
                                        label = "From Unit",
                                        selectedCode = state.fromUnitCode,
                                        units = state.availableUnits,
                                        onSelect = onSelectFromUnit,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onSwapUnits,
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap From and To Units")
                                    }
                                    UnitDropdownField(
                                        label = "To Unit",
                                        selectedCode = state.toUnitCode,
                                        units = state.availableUnits,
                                        onSelect = onSelectToUnit,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Output / Boundary Error Card
            item {
                when (val result = state.conversionResult) {
                    is UnitConversionResult.Success -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {},
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "CONVERTED RESULT",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${result.formattedResult} ${result.toUnit.symbol}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${state.inputText.trim()} ${result.fromUnit.symbol} (${result.fromUnit.name}) → ${result.toUnit.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                                result.advisoryNote?.let { adv ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = adv,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    is UnitConversionResult.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {},
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Physical limit or validation error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Error: Validation / Physical Limit Notice",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = result.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // All compatible equivalents within active category/subgroup
            if (state.equivalentConversions.isNotEmpty()) {
                item {
                    Text(
                        text = "All Compatible Units (${state.selectedCategory.displayName})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                items(state.equivalentConversions, key = { it.first.code }) { (unit, formatted) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {},
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                val subgroupLabel = when (unit.subgroup) {
                                    UnitSubgroup.VISCOSITY_DYNAMIC -> "Dynamic viscosity"
                                    UnitSubgroup.VISCOSITY_KINEMATIC -> "Kinematic viscosity"
                                    UnitSubgroup.SPEED_LINEAR -> "Linear velocity"
                                    UnitSubgroup.SPEED_ROTATIONAL -> "Rotational speed"
                                    UnitSubgroup.PRESSURE_GAUGE -> "Gauge (rel. 1.01325 bar atm)"
                                    UnitSubgroup.PRESSURE_ABSOLUTE -> "Absolute pressure"
                                    else -> unit.code
                                }
                                Text(
                                    text = subgroupLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$formatted ${unit.symbol}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdownField(
    label: String,
    selectedCode: String,
    units: List<EngineeringUnit>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val current = units.firstOrNull { it.code == selectedCode }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = current?.let { "${it.symbol} — ${it.name}" } ?: selectedCode,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${unit.symbol} — ${unit.name}", fontWeight = FontWeight.Medium)
                            if (unit.subgroup != UnitSubgroup.STANDARD) {
                                Text(
                                    text = unit.subgroup.name.replace('_', ' ').lowercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(unit.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(name = "UnitConverter - Light Compact", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun UnitConverterLightPreview() {
    val units = UnitConverterEngine.getUnits(UnitCategory.PRESSURE)
    val result = UnitConverterEngine.convert(10.0, "bar_g", "psi_g")
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        UnitConverterContent(
            state = UnitConverterUiState(
                selectedCategory = UnitCategory.PRESSURE,
                availableUnits = units,
                fromUnitCode = "bar_g",
                toUnitCode = "psi_g",
                inputText = "10",
                conversionResult = result,
                equivalentConversions = units.take(4).map { it to "10.00" }
            ),
            onOpenDrawer = {}
        )
    }
}

@Preview(name = "UnitConverter - Dark Boundary Error", showBackground = true, widthDp = 420, heightDp = 760)
@Composable
private fun UnitConverterDarkErrorPreview() {
    val units = UnitConverterEngine.getUnits(UnitCategory.TEMPERATURE)
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        UnitConverterContent(
            state = UnitConverterUiState(
                selectedCategory = UnitCategory.TEMPERATURE,
                availableUnits = units,
                fromUnitCode = "c",
                toUnitCode = "k",
                inputText = "-300",
                conversionResult = UnitConverterEngine.convert(-300.0, "c", "k"),
                equivalentConversions = emptyList()
            ),
            onOpenDrawer = {}
        )
    }
}
