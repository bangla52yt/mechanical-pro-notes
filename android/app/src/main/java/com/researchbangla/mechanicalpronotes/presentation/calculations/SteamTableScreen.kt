package com.researchbangla.mechanicalpronotes.presentation.calculations

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.steam.IapwsIf97SteamEngine
import com.researchbangla.mechanicalpronotes.domain.steam.SaturatedSteamProperties
import com.researchbangla.mechanicalpronotes.domain.unit.DisplayUnitConversion
import com.researchbangla.mechanicalpronotes.domain.unit.UnitPreferenceHelper
import com.researchbangla.mechanicalpronotes.presentation.components.MathFormulaCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteamTableScreen(
    onNavigateBack: () -> Unit,
    initialPressureText: String = "7.0",
    initialGaugeMode: Boolean = true,
    unitSystem: UnitSystem = UnitSystem.METRIC
) {
    var isGaugeMode by rememberSaveable { mutableStateOf(initialGaugeMode) }
    var inputPressureStr by rememberSaveable { mutableStateOf(initialPressureText) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentProperties by remember { mutableStateOf<SaturatedSteamProperties?>(null) }

    val quickPressures = listOf(0.5, 1.0, 2.0, 3.5, 5.0, 7.0, 8.0, 10.0, 12.0, 15.0, 18.0)

    fun calculate(pressureStr: String, gauge: Boolean) {
        val num = pressureStr.toDoubleOrNull()
        if (num == null || !num.isFinite()) {
            errorMessage = "Please enter a valid numeric pressure value."
            currentProperties = null
            return
        }

        try {
            val props = if (gauge) {
                IapwsIf97SteamEngine.getPropertiesByGaugePressure(num)
            } else {
                IapwsIf97SteamEngine.getPropertiesByAbsolutePressure(num)
            }
            currentProperties = props
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message ?: IapwsIf97SteamEngine.formatValidRangeHint(gauge)
            currentProperties = null
        }
    }

    LaunchedEffect(inputPressureStr, isGaugeMode) {
        calculate(inputPressureStr, isGaugeMode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saturated Steam Properties",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "IAPWS-IF97 Engine • ${unitSystem.displayName} Units",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MathFormulaCard(
                title = "Saturated Water & Steam Properties",
                formulaLatex = "T_sat = f(P),   h_g = h_f + h_fg,   v_g = f(P, T)",
                engineeringReference = "IAPWS-IF97 Region 4 Saturated Water/Steam ONLY (0.50–20.00 bar a; Eq. 31 T_sat + 56-node log-P v_g & linear-P enthalpy; excludes subcooled liquid, superheated, supercritical & vacuum <0.50 bar a)"
            )

            // Input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter Operating Pressure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    // Pressure basis selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isGaugeMode,
                            onClick = { isGaugeMode = true },
                            label = {
                                Text(
                                    text = "Gauge: bar(g)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        )
                        FilterChip(
                            selected = !isGaugeMode,
                            onClick = { isGaugeMode = false },
                            label = {
                                Text(
                                    text = "Absolute: bar(a)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        )
                    }

                    // Input field
                    OutlinedTextField(
                        value = inputPressureStr,
                        onValueChange = {
                            inputPressureStr = it
                        },
                        label = { Text(if (isGaugeMode) "Pressure bar(g)" else "Pressure bar(a)") },
                        suffix = { Text(if (isGaugeMode) "bar(g)" else "bar(a)") },
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                            } else {
                                val numericPressure = inputPressureStr.toDoubleOrNull()
                                val pressurePreview = if (numericPressure != null && unitSystem != UnitSystem.METRIC) {
                                    when (
                                        val converted = UnitPreferenceHelper.convertForDisplay(
                                            value = numericPressure,
                                            canonicalUnit = if (isGaugeMode) "bar(g)" else "bar(a)",
                                            targetSystem = unitSystem
                                        )
                                    ) {
                                        is DisplayUnitConversion.Converted ->
                                            " (${unitSystem.displayName}: ${converted.formattedValue} ${converted.unitSymbol})"
                                        else -> ""
                                    }
                                } else {
                                    ""
                                }
                                Text(IapwsIf97SteamEngine.formatValidRangeHint(isGaugeMode) + pressurePreview)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick presets
                    Text(
                        text = "Standard Boiler Setpoints:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPressures.forEach { p ->
                            AssistChip(
                                onClick = { inputPressureStr = p.toString() },
                                label = { Text("$p") },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }
            }

            // Results Card
            val props = currentProperties
            if (props != null) {
                val absPressureDisplay = remember(props.pressureAbsoluteBar, unitSystem) {
                    formatSteamEquivalent(props.pressureAbsoluteBar, "bar(a)", unitSystem)
                }
                val gaugePressureDisplay = remember(props.pressureGaugeBar, unitSystem) {
                    formatSteamEquivalent(props.pressureGaugeBar, "bar(g)", unitSystem)
                }
                val satTempDisplay = remember(props.saturationTempCelsius, unitSystem) {
                    formatSteamEquivalent(props.saturationTempCelsius, "°C", unitSystem)
                }
                val densityDisplay = remember(props.steamDensityKgM3, unitSystem) {
                    formatSteamEquivalent(props.steamDensityKgM3, "kg/m3", unitSystem)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thermodynamic State",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { heading() }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val positiveColors = semanticPositiveColors(MaterialTheme.colorScheme.surface.luminance() < 0.5f)
                            Surface(
                                color = positiveColors.container,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "IAPWS-IF97",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = positiveColors.content,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (unitSystem != UnitSystem.METRIC) {
                            Text(
                                text = "Preference: ${unitSystem.displayName}. Verified equivalents shown for pressure, temperature${if (unitSystem == UnitSystem.IMPERIAL) ", and density" else ""}; enthalpies (kJ/kg) and specific volumes (m3/kg) stay in canonical IAPWS-IF97 metric units.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        PropertyRow(
                            label = "Absolute Pressure",
                            value = "${props.pressureAbsoluteBar} bar(a)",
                            preferredEquivalent = absPressureDisplay
                        )
                        PropertyRow(
                            label = "Gauge Pressure (Patm=1.013 bar)",
                            value = "${props.pressureGaugeBar} bar(g)",
                            preferredEquivalent = gaugePressureDisplay
                        )
                        PropertyHighlightRow(
                            label = "Saturation Temperature (T_sat)",
                            value = if (unitSystem == UnitSystem.METRIC || satTempDisplay == null) {
                                "${props.saturationTempCelsius} °C"
                            } else {
                                satTempDisplay
                            },
                            subtext = if (unitSystem == UnitSystem.METRIC) {
                                "(${props.saturationTempKelvin} K)"
                            } else {
                                "Canonical: ${props.saturationTempCelsius} °C (${props.saturationTempKelvin} K)"
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        PropertyRow("Liquid Sensible Enthalpy (h_f)", "${props.liquidEnthalpyHfKjKg} kJ/kg")
                        PropertyHighlightRow("Latent Heat of Evaporation (h_fg)", "${props.evaporationEnthalpyHfgKjKg} kJ/kg", "Usable process heat")
                        PropertyRow("Total Steam Enthalpy (h_g = h_f + h_fg)", "${props.steamEnthalpyHgKjKg} kJ/kg")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        PropertyRow("Specific Volume of Steam (v_g)", "${props.steamSpecificVolumeVgM3Kg} m3/kg")
                        PropertyRow(
                            label = "Saturated Steam Density (ρ_g)",
                            value = "${props.steamDensityKgM3} kg/m3",
                            preferredEquivalent = densityDisplay
                        )
                        PropertyRow("Specific Volume of Water (v_f)", "${props.liquidSpecificVolumeVfM3Kg} m3/kg")
                    }
                }
            }
        }
    }
}

private fun formatSteamEquivalent(
    canonicalValue: Double,
    canonicalUnit: String,
    targetSystem: UnitSystem
): String? {
    if (targetSystem == UnitSystem.METRIC) return null
    return when (
        val converted = UnitPreferenceHelper.convertForDisplay(
            value = canonicalValue,
            canonicalUnit = canonicalUnit,
            targetSystem = targetSystem
        )
    ) {
        is DisplayUnitConversion.Converted -> "${converted.formattedValue} ${converted.unitSymbol}"
        else -> null
    }
}

@Composable
private fun PropertyRow(
    label: String,
    value: String,
    preferredEquivalent: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
            )
            if (!preferredEquivalent.isNullOrBlank()) {
                Text(
                    text = "≈ $preferredEquivalent",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun PropertyHighlightRow(label: String, value: String, subtext: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Preview(name = "SteamTable - Light", showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun SteamTableLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SteamTableScreen(onNavigateBack = {}, initialPressureText = "7.0")
    }
}

@Preview(name = "SteamTable - Dark Out of Range Error", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun SteamTableDarkErrorPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        SteamTableScreen(onNavigateBack = {}, initialPressureText = "25.0")
    }
}
