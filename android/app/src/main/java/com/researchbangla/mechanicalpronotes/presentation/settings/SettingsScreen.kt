package com.researchbangla.mechanicalpronotes.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.model.AppThemeMode
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticDangerColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var overwriteOnImport by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = state.exportedJson
        if (uri != null && json != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
                viewModel.clearExportedJson()
                viewModel.setImportMessage("Backup JSON saved to device storage (${json.length} bytes).")
                showExportDialog = false
            } catch (e: Exception) {
                viewModel.setImportMessage("Failed to write backup file: ${e.message}", isError = true)
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonText = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!jsonText.isNullOrBlank()) {
                    viewModel.importPersonalData(jsonText, overwrite = overwriteOnImport)
                } else {
                    viewModel.setImportMessage("Selected backup file is empty.", isError = true)
                }
            } catch (e: Exception) {
                viewModel.setImportMessage("Failed to read backup file: ${e.message}", isError = true)
            }
        }
    }

    SettingsScreenContent(
        state = state,
        overwriteOnImport = overwriteOnImport,
        showExportDialog = showExportDialog,
        onOpenDrawer = onOpenDrawer,
        onThemeModeSelect = viewModel::setThemeMode,
        onToggleLargePlantFloorFont = viewModel::toggleLargePlantFloorFont,
        onUnitSystemSelect = viewModel::setUnitSystem,
        onExportBackupClick = {
            viewModel.exportPersonalData()
            showExportDialog = true
        },
        onDismissExportDialog = {
            showExportDialog = false
            viewModel.clearExportedJson()
        },
        onSaveExportFile = {
            createBackupLauncher.launch("mechanical_pro_notes_backup.json")
        },
        onRestoreBackupClick = {
            openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        },
        onToggleOverwriteOnImport = { overwriteOnImport = it },
        onRequestClearPersonalData = viewModel::requestClearPersonalData,
        onProceedToFinalClearConfirmation = viewModel::proceedToFinalClearConfirmation,
        onCancelClearPersonalData = viewModel::cancelClearPersonalData,
        onConfirmClearAllPersonalData = viewModel::confirmClearAllPersonalData
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    overwriteOnImport: Boolean,
    showExportDialog: Boolean,
    onOpenDrawer: () -> Unit,
    onThemeModeSelect: (AppThemeMode) -> Unit,
    onToggleLargePlantFloorFont: (Boolean) -> Unit,
    onExportBackupClick: () -> Unit,
    onDismissExportDialog: () -> Unit,
    onSaveExportFile: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    onToggleOverwriteOnImport: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onUnitSystemSelect: (UnitSystem) -> Unit = {},
    onRequestClearPersonalData: () -> Unit = {},
    onProceedToFinalClearConfirmation: () -> Unit = {},
    onCancelClearPersonalData: () -> Unit = {},
    onConfirmClearAllPersonalData: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Privacy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mechanical Pro Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Version 1.0.0 • Offline Personal Plant Reference",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Personal offline mechanical engineering and wash plant maintenance reference app for Android (64 Plant Assets • 20 Calculators • IAPWS-IF97 Steam Table).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Security & Privacy Audits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    ComplianceRow(
                        title = "Zero Network Policy (DEC-016)",
                        status = "VERIFIED ABSENT",
                        detail = "No android.permission.INTERNET declared in manifest. 100% offline.",
                        isPositive = true
                    )

                    ComplianceRow(
                        title = "Personal Data Isolation (DEC-014)",
                        status = "PROTECTED",
                        detail = "Personal notes, bookmarks, checklists & history live in isolated tables immune to static seed updates.",
                        isPositive = true
                    )

                    ComplianceRow(
                        title = "Free-First Cost Policy (DEC-002)",
                        status = "100% FREE",
                        detail = "Zero cloud hosting, zero API charges, zero subscription tiers.",
                        isPositive = true
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Display & Readability",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )

                    Text(
                        text = "App Theme Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.themeMode == mode,
                                onClick = { onThemeModeSelect(mode) },
                                label = {
                                    Text(
                                        text = mode.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "Measurement Unit System",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.unitSystem.displayName}: ${state.unitSystem.summary}. Core engineering formulas, validation limits, and saved history/notes remain unchanged.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitSystem.entries.forEach { system ->
                            FilterChip(
                                selected = state.unitSystem == system,
                                onClick = { onUnitSystemSelect(system) },
                                label = {
                                    Text(
                                        text = system.displayName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .toggleable(
                                value = state.isLargePlantFloorFont,
                                role = Role.Switch,
                                onValueChange = onToggleLargePlantFloorFont
                            )
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Plant Floor High Legibility", fontWeight = FontWeight.Medium)
                            Text(
                                "Scale typography (~1.18x) for low-light boiler rooms and utility floors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = state.isLargePlantFloorFont,
                            onCheckedChange = null
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Personal Data Management & Backup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Manage your user-created records stored in isolated Room tables. Packaged equipment catalogs, formulas, Steam Table data, and references are managed separately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataCounterBox(
                            label = "Notes",
                            count = state.totalNotesCount,
                            modifier = Modifier.weight(1f)
                        )
                        DataCounterBox(
                            label = "Bookmarks",
                            count = state.totalBookmarksCount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataCounterBox(
                            label = "Calc History",
                            count = state.totalHistoryCount,
                            modifier = Modifier.weight(1f)
                        )
                        DataCounterBox(
                            label = "Checklist Logs",
                            count = state.totalChecklistEntriesCount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DataCounterBox(
                            label = "Custom Checklists",
                            count = state.totalCustomChecklistsCount,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onExportBackupClick,
                        enabled = !state.isExporting && !state.isClearingPersonalData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Backup JSON (SHA-256)")
                    }

                    OutlinedButton(
                        onClick = onRestoreBackupClick,
                        enabled = !state.isClearingPersonalData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Backup from Device Storage")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .toggleable(
                                value = overwriteOnImport,
                                role = Role.Checkbox,
                                onValueChange = onToggleOverwriteOnImport
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = overwriteOnImport,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replace existing personal records before restore",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Clears notes, bookmarks, calculation history & checklist progress only when restore verification passes.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    state.importMessage?.let { msg ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isImportError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = if (state.isImportError) "Backup error" else "Backup status",
                                tint = if (state.isImportError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.isImportError) "Error: $msg" else msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isImportError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Backup v2 Note: JSON export/restore covers Room tables (notes, bookmarks, calculation history, checklist item progress & remarks). Local custom checklist templates and execution metadata (equipment/operator/shift) live in on-device Preferences.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val dangerColors = semanticDangerColors(isDark)

                    Text(
                        text = "Clear All Personal Data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = dangerColors.content
                    )
                    Text(
                        text = "Deletes saved personal field notes, bookmarks, calculation history, checklist progress, custom checklists, and execution metadata. Packaged equipment catalogs, default checklists, formulas, Steam Table data, and unit/theme preferences remain intact.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = onRequestClearPersonalData,
                        enabled = !state.isClearingPersonalData,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = dangerColors.container.copy(alpha = 0.35f),
                            contentColor = dangerColors.content
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isClearingPersonalData) {
                                "Clearing Personal Data..."
                            } else {
                                "Clear All Personal Data"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    state.personalDataMessage?.let { msg ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isPersonalDataError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = if (state.isPersonalDataError) "Clear data error" else "Clear data status",
                                tint = if (state.isPersonalDataError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.isPersonalDataError) "Error: $msg" else msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isPersonalDataError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (showExportDialog && state.exportedJson != null) {
            AlertDialog(
                onDismissRequest = onDismissExportDialog,
                title = { Text("Backup JSON Ready (SHA-256)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Includes user notes, bookmarks, calculation history, and checklist progress (${state.exportedJson.length} characters).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = state.exportedJson,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onSaveExportFile,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Save .json File")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissExportDialog,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        when (state.clearConfirmationStep) {
            ClearDataConfirmationStep.STEP_1_WARNING -> {
                AlertDialog(
                    onDismissRequest = onCancelClearPersonalData,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = { Text("Step 1 of 2: Clear Personal Data?") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "This action will delete the following personal data categories stored on this device:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "• Personal Field Notes (${state.totalNotesCount})\n" +
                                    "• Saved Bookmarks (${state.totalBookmarksCount})\n" +
                                    "• Calculation History (${state.totalHistoryCount})\n" +
                                    "• Checklist Progress & Remarks (${state.totalChecklistEntriesCount})\n" +
                                    "• Custom Checklists & Shift Logs (${state.totalCustomChecklistsCount})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pre-packaged engineering equipment catalogs, formulas, IAPWS-IF97 Steam Table data, Technical References, and your Measurement Unit System preference will remain untouched.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onProceedToFinalClearConfirmation,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Continue")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onCancelClearPersonalData,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            ClearDataConfirmationStep.STEP_2_FINAL -> {
                val isDarkDialog = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val dialogDangerColors = semanticDangerColors(isDarkDialog)
                AlertDialog(
                    onDismissRequest = onCancelClearPersonalData,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = dialogDangerColors.content
                        )
                    },
                    title = { Text("Step 2 of 2: Confirm Deletion") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Please confirm that you want to permanently delete all saved personal field notes, bookmarks, calculation history, and checklist progress.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Packaged plant engineering data and your selected Unit System (${state.unitSystem.displayName}) will not be removed. Export a JSON backup first if you want a copy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onConfirmClearAllPersonalData,
                            enabled = !state.isClearingPersonalData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Delete My Personal Data")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = onCancelClearPersonalData,
                            enabled = !state.isClearingPersonalData,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            ClearDataConfirmationStep.NONE -> Unit
        }
    }
}

@Composable
private fun ComplianceRow(
    title: String,
    status: String,
    detail: String,
    isPositive: Boolean
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val badgeColors = if (isPositive) semanticPositiveColors(isDark) else semanticDangerColors(isDark)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = badgeColors.container,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = status,
                    color = badgeColors.content,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text(text = detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun DataCounterBox(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "Settings Light", showBackground = true, widthDp = 360, heightDp = 820)
@Composable
private fun SettingsScreenLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SettingsScreenContent(
            state = SettingsUiState(
                themeMode = AppThemeMode.LIGHT,
                isLargePlantFloorFont = false,
                totalNotesCount = 4,
                totalBookmarksCount = 6,
                totalHistoryCount = 12
            ),
            overwriteOnImport = false,
            showExportDialog = false,
            onOpenDrawer = {},
            onThemeModeSelect = {},
            onToggleLargePlantFloorFont = {},
            onExportBackupClick = {},
            onDismissExportDialog = {},
            onSaveExportFile = {},
            onRestoreBackupClick = {},
            onToggleOverwriteOnImport = {}
        )
    }
}

@Preview(name = "Settings Dark Import Error", showBackground = true, widthDp = 360, heightDp = 820)
@Composable
private fun SettingsScreenDarkErrorPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        SettingsScreenContent(
            state = SettingsUiState(
                themeMode = AppThemeMode.DARK,
                isLargePlantFloorFont = true,
                totalNotesCount = 2,
                totalBookmarksCount = 3,
                totalHistoryCount = 5,
                importMessage = "SHA-256 backup checksum mismatch — restore aborted.",
                isImportError = true
            ),
            overwriteOnImport = true,
            showExportDialog = false,
            onOpenDrawer = {},
            onThemeModeSelect = {},
            onToggleLargePlantFloorFont = {},
            onExportBackupClick = {},
            onDismissExportDialog = {},
            onSaveExportFile = {},
            onRestoreBackupClick = {},
            onToggleOverwriteOnImport = {}
        )
    }
}

