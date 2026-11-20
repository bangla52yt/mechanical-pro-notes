package com.researchbangla.mechanicalpronotes.presentation.calculations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
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
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationResultFormatter
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationExecutionResult
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.model.FormulaVariable
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.unit.DisplayUnitConversion
import com.researchbangla.mechanicalpronotes.domain.unit.UnitPreferenceHelper
import com.researchbangla.mechanicalpronotes.presentation.components.MathFormulaCard
import com.researchbangla.mechanicalpronotes.presentation.components.SafetyWarningCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticWarningColors

@Composable
fun CalculationRunnerScreen(
    calculationId: String,
    viewModel: CalculationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSteamTable: () -> Unit = {}
) {
    LaunchedEffect(calculationId) {
        viewModel.loadCalculationRunner(calculationId)
    }

    val state by viewModel.runnerState.collectAsState()
    CalculationRunnerContent(
        calculationId = calculationId,
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToSteamTable = onNavigateToSteamTable,
        onInputChange = { sym, value -> viewModel.onInputChange(sym, value) },
        onExecuteCalculation = { viewModel.executeCalculation() },
        onSaveToHistory = { viewModel.saveToHistory() },
        onToggleBookmark = { viewModel.toggleBookmark() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationRunnerContent(
    calculationId: String,
    state: CalculationRunnerUiState,
    onNavigateBack: () -> Unit,
    onNavigateToSteamTable: () -> Unit = {},
    onInputChange: (String, String) -> Unit = { _, _ -> },
    onExecuteCalculation: () -> Unit = {},
    onSaveToHistory: () -> Unit = {},
    onToggleBookmark: () -> Unit = {}
) {
    val definition = state.definition

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = definition?.id ?: calculationId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (definition != null) {
                            Text(
                                text = definition.title,
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
                    if (definition != null) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (state.isBookmarked) {
                                    "Remove bookmark for ${definition.title}"
                                } else {
                                    "Bookmark ${definition.title}"
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
        } else if (definition == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SafetyWarningCard(
                    title = "Calculation Definition Unavailable",
                    message = state.errorMessage ?: "Calculation definition not found.",
                    isStatutory = true
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formula Header Card
                MathFormulaCard(
                    title = "Governing Formula (${definition.id})",
                    formulaLatex = definition.formulaLatex,
                    engineeringReference = definition.engineeringReference
                )

                // Formula Assumptions & Operating Limitations Card
                if (definition.assumptions.isNotBlank() || definition.limitations.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Engineering Assumptions & Limitations",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.semantics { heading() }
                            )
                            if (definition.assumptions.isNotBlank()) {
                                Text(
                                    text = "Assumptions: ${definition.assumptions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (definition.limitations.isNotBlank()) {
                                Text(
                                    text = "Limitations: ${definition.limitations}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (definition.id in listOf("CALC-01", "CALC-03", "CALC-06", "CALC-08", "CALC-14", "CALC-17")) {
                    OutlinedButton(
                        onClick = onNavigateToSteamTable,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Look Up Saturated Steam Properties (h_g / h_fg / v_g)")
                    }
                }

                // Status or Warning Notice if RESEARCH_PENDING / NOT_IMPLEMENTED
                if (definition.status != CalculationStatus.IMPLEMENTED) {
                    val warningColors = semanticWarningColors(MaterialTheme.colorScheme.surface.luminance() < 0.5f)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = warningColors.container),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Research pending notice", tint = warningColors.content)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formula status: ${definition.status}. Theoretical formula displayed for engineering study; execution disabled pending empirical constants.",
                                style = MaterialTheme.typography.bodySmall,
                                color = warningColors.content
                            )
                        }
                    }
                }

                // Input Parameters Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Input Variables (${definition.variables.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { heading() }
                            )
                            Text(
                                text = "Units: ${state.unitSystem.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (state.unitSystem != UnitSystem.METRIC) {
                            Text(
                                text = "Inputs use canonical plant units for verified bounds & formula safety; ${state.unitSystem.displayName} display equivalents appear where verified.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        definition.variables.forEachIndexed { index, variable ->
                            val currentValue = state.inputValues[variable.symbol] ?: ""
                            val error = state.validationErrors[variable.symbol]
                            val reqTag = if (variable.isRequired) " *" else " (Optional)"
                            val rangeText = if (variable.minBound != null || variable.maxBound != null) {
                                "Valid range: [${variable.minBound ?: "-∞"}, ${variable.maxBound ?: "∞"}] ${variable.unit}"
                            } else {
                                null
                            }

                            val parsedPreview = currentValue.trim().toDoubleOrNull()
                            val unitEquivText = if (state.unitSystem != UnitSystem.METRIC && parsedPreview != null && parsedPreview.isFinite()) {
                                when (val conv = UnitPreferenceHelper.convertForDisplay(parsedPreview, variable.unit, state.unitSystem)) {
                                    is DisplayUnitConversion.Converted -> "${state.unitSystem.displayName} ≈ ${conv.formattedValue} ${conv.unitSymbol}"
                                    else -> null
                                }
                            } else {
                                null
                            }

                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { onInputChange(variable.symbol, it) },
                                label = { Text("${variable.name} (${variable.symbol})$reqTag") },
                                suffix = { Text(variable.unit) },
                                isError = error != null,
                                supportingText = {
                                    when {
                                        error != null -> Text(
                                            text = "Error: $error",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        !variable.isRequired -> Text(
                                            text = listOfNotNull("Optional — leave blank if unknown", rangeText, unitEquivText).joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        rangeText != null || unitEquivText != null -> Text(
                                            text = listOfNotNull(rangeText, unitEquivText).joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = if (index == definition.variables.lastIndex) ImeAction.Done else ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onExecuteCalculation,
                            enabled = definition.status == CalculationStatus.IMPLEMENTED && !state.isExecuting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            if (state.isExecuting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = "Calculate & Verify")
                        }
                    }
                }

                // Execution Error Banner (if any)
                if (state.errorMessage != null) {
                    SafetyWarningCard(
                        title = "Error: Calculation Halted",
                        message = state.errorMessage,
                        isStatutory = true
                    )
                }

                // Results Card
                val result = state.result
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calculation Result",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { heading() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CalculationStatusBadge(status = result.status)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = result.primaryOutputName,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = CalculationResultFormatter.formatPrimaryValue(
                                    value = result.primaryOutputValue,
                                    unit = result.primaryOutputUnit,
                                    outputName = result.primaryOutputName
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (state.unitSystem != UnitSystem.METRIC) {
                                when (val primaryConv = UnitPreferenceHelper.convertForDisplay(
                                    value = result.primaryOutputValue,
                                    canonicalUnit = result.primaryOutputUnit,
                                    targetSystem = state.unitSystem
                                )) {
                                    is DisplayUnitConversion.Converted -> {
                                        Text(
                                            text = "${state.unitSystem.displayName} Equivalent: ${primaryConv.formattedValue} ${primaryConv.unitSymbol}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    is DisplayUnitConversion.Retained -> {
                                        Text(
                                            text = primaryConv.reason,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    null -> Unit
                                }
                            }

                            if (result.secondaryOutputs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Secondary Parameters:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                result.secondaryOutputs.forEach { (key, value) ->
                                    val (displayLabel, displayValue) = CalculationResultFormatter.formatSecondaryEntry(key, value)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = displayLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = displayValue,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            textAlign = TextAlign.End,
                                            fontWeight = if (CalculationResultFormatter.isStatusFlagKey(key)) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Engineering Notes:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = result.notesAndRecommendations,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onSaveToHistory,
                                enabled = !state.historySaved,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.historySaved) Icons.Default.Check else Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.historySaved) "Saved to History" else "Save to Personal History")
                            }
                        }
                    }
                }

                // Plant Disclaimer Card
                SafetyWarningCard(
                    title = "Engineering Disclaimer",
                    message = "Calculated figures are for engineering guidance and operational verification. In legal audits or statutory inspections, confirm setpoints with calibrated plant instruments and OCIB certified inspectors."
                )
            }
        }
    }
}

private object CalculationRunnerPreviewData {
    val calc01Def = CalculationDefinition(
        id = "CALC-01",
        code = "CALC-01",
        title = "Direct Boiler Thermal Efficiency",
        bengaliTitle = "বয়লারের সরাসরি তাপীয় দক্ষতা",
        category = "Boiler Thermal",
        formulaLatex = "eta = [ m_s * (h_s - h_w) ] / [ m_f * NCV ] * 100",
        engineeringReference = "ASME PTC 4 — Fired Steam Generators",
        assumptions = "Steady-state steam generation under dry saturated outlet conditions and dry NCV fuel basis.",
        limitations = "Requires calibrated steam flow & gas flow metering; does not isolate blowdown or radiation loss fractions.",
        status = CalculationStatus.IMPLEMENTED,
        variables = listOf(
            FormulaVariable(calculationId = "CALC-01", symbol = "m_s", name = "Steam Output Flow", unit = "kg/h", defaultValue = 4000.0, minBound = 10.0, isRequired = true),
            FormulaVariable(calculationId = "CALC-01", symbol = "h_s", name = "Steam Enthalpy", unit = "kJ/kg", defaultValue = 2778.0, minBound = 2000.0, maxBound = 3500.0, isRequired = true),
            FormulaVariable(calculationId = "CALC-01", symbol = "m_f", name = "Fuel Gas Flow", unit = "m3/h", defaultValue = 320.0, minBound = 1.0, isRequired = true),
            FormulaVariable(calculationId = "CALC-01", symbol = "T_amb", name = "Optional Ambient Reference Temp", unit = "°C", defaultValue = null, minBound = 0.0, maxBound = 60.0, isRequired = false)
        )
    )

    val calc01Result = CalculationExecutionResult(
        calculationId = "CALC-01",
        primaryOutputName = "Boiler Thermal Efficiency",
        primaryOutputValue = 85.30,
        primaryOutputUnit = "%",
        secondaryOutputs = mapOf(
            "SteamHeatOutputKjHr" to 9772000.0,
            "FuelHeatInputKjHr" to 11456000.0,
            "ThermalStatusFlag" to 1.0
        ),
        notesAndRecommendations = "Within target natural-gas firetube efficiency band (82%–90%). Inspect stack O2 trim to hold <3% excess air."
    )
}

@Preview(name = "CalculationRunner - Light With Result", showBackground = true, widthDp = 380, heightDp = 820)
@Composable
private fun CalculationRunnerLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        CalculationRunnerContent(
            calculationId = "CALC-01",
            state = CalculationRunnerUiState(
                isLoading = false,
                definition = CalculationRunnerPreviewData.calc01Def,
                inputValues = mapOf("m_s" to "4000", "h_s" to "2778", "m_f" to "320", "T_amb" to ""),
                result = CalculationRunnerPreviewData.calc01Result
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "CalculationRunner - Dark ResearchPending Error", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun CalculationRunnerDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        CalculationRunnerContent(
            calculationId = "CALC-09",
            state = CalculationRunnerUiState(
                isLoading = false,
                definition = CalculationRunnerPreviewData.calc01Def.copy(
                    id = "CALC-09",
                    title = "Safety Valve Capacity Estimate",
                    status = CalculationStatus.RESEARCH_PENDING
                ),
                inputValues = mapOf("m_s" to "-10", "h_s" to "NaN"),
                validationErrors = mapOf("m_s" to "Must be >= 10.0 kg/h", "h_s" to "Enter a valid finite decimal number.")
            ),
            onNavigateBack = {}
        )
    }
}
