package com.researchbangla.mechanicalpronotes.presentation.checklists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistProgress
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistShift
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticDangerColors
import com.researchbangla.mechanicalpronotes.presentation.theme.semanticPositiveColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    ChecklistScreenContent(
        state = state,
        onOpenDrawer = onOpenDrawer,
        onSelectChecklist = viewModel::selectChecklist,
        onToggleItem = viewModel::toggleItem,
        onResetCurrentChecklist = viewModel::resetCurrentChecklist,
        onToggleBookmark = viewModel::toggleChecklistBookmark,
        onUpdateItemRemark = viewModel::updateItemRemark,
        onSourceFilterChange = viewModel::setSourceFilter,
        onClearEquipmentFilter = viewModel::clearEquipmentFilter,
        onUpdateChecklistEquipment = viewModel::updateChecklistEquipment,
        onUpdateOperatorAndShift = viewModel::updateExecutionOperatorAndShift,
        onOpenCreateCustom = { viewModel.openCreateCustomChecklist() },
        onOpenEditCustom = viewModel::openEditCustomChecklist,
        onDeleteCustom = viewModel::deleteCustomChecklist,
        onCloseCustomEditor = viewModel::closeCustomChecklistEditor,
        onEditorTitleChange = viewModel::updateEditorTitle,
        onEditorDescriptionChange = viewModel::updateEditorDescription,
        onEditorEquipmentIdChange = viewModel::updateEditorEquipmentId,
        onDraftItemTextChange = viewModel::updateDraftItemText,
        onDraftItemCategoryChange = viewModel::updateDraftItemCategory,
        onToggleDraftItemCrucial = viewModel::toggleDraftItemCrucial,
        onAddOrSaveDraftItem = { viewModel.addOrSaveDraftItem() },
        onEditDraftItem = viewModel::editDraftItem,
        onCancelDraftItemEdit = viewModel::cancelDraftItemEdit,
        onRemoveDraftItem = viewModel::removeDraftItem,
        onSaveCustomChecklist = { viewModel.saveCustomChecklist() },
        onDismissFeedback = viewModel::clearFeedback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreenContent(
    state: ChecklistUiState,
    onOpenDrawer: () -> Unit,
    onSelectChecklist: (String) -> Unit,
    onToggleItem: (Int) -> Unit,
    onResetCurrentChecklist: () -> Unit,
    onToggleBookmark: (String) -> Unit = {},
    onUpdateItemRemark: (Int, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    onSourceFilterChange: (ChecklistSourceFilter) -> Unit = {},
    onClearEquipmentFilter: () -> Unit = {},
    onUpdateChecklistEquipment: (String, String) -> Unit = { _, _ -> },
    onUpdateOperatorAndShift: (String, String, ChecklistShift) -> Unit = { _, _, _ -> },
    onOpenCreateCustom: () -> Unit = {},
    onOpenEditCustom: (String) -> Unit = {},
    onDeleteCustom: (String) -> Unit = {},
    onCloseCustomEditor: () -> Unit = {},
    onEditorTitleChange: (String) -> Unit = {},
    onEditorDescriptionChange: (String) -> Unit = {},
    onEditorEquipmentIdChange: (String) -> Unit = {},
    onDraftItemTextChange: (String) -> Unit = {},
    onDraftItemCategoryChange: (String) -> Unit = {},
    onToggleDraftItemCrucial: () -> Unit = {},
    onAddOrSaveDraftItem: () -> Unit = {},
    onEditDraftItem: (Int) -> Unit = {},
    onCancelDraftItemEdit: () -> Unit = {},
    onRemoveDraftItem: (Int) -> Unit = {},
    onSaveCustomChecklist: () -> Unit = {},
    onDismissFeedback: () -> Unit = {}
) {
    val visibleList = state.visibleChecklists
    val activeChecklist = visibleList.find { it.id == state.selectedChecklistId }
        ?: visibleList.firstOrNull()
    val activeProgressByItem = activeChecklist?.let { state.progressByChecklist[it.id] } ?: emptyMap()

    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteCustomChecklistId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingRemarkItemId by rememberSaveable(activeChecklist?.id) { mutableStateOf<Int?>(null) }
    var remarkDraft by rememberSaveable(activeChecklist?.id) { mutableStateOf("") }

    var showExecutionEquipmentPicker by rememberSaveable { mutableStateOf(false) }
    var showEditorEquipmentPicker by rememberSaveable { mutableStateOf(false) }
    var showOperatorShiftDialog by rememberSaveable { mutableStateOf(false) }

    val pendingDeleteCustomChecklist = pendingDeleteCustomChecklistId?.let { targetId ->
        visibleList.firstOrNull { it.id == targetId }
            ?: state.checklists.firstOrNull { it.id == targetId }
    }
    val editingRemarkItem = editingRemarkItemId?.let { itemId ->
        activeChecklist?.items?.firstOrNull { it.id == itemId }
    }

    if (showResetConfirm && activeChecklist != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Checklist Progress?") },
            text = {
                Text(
                    "This clears all checked items, completion timestamps, inspection remarks, and operator/shift execution metadata for \"${activeChecklist.title}\". Template items and equipment association remain intact."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetCurrentChecklist()
                        showResetConfirm = false
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirm = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text("Cancel") }
            }
        )
    }

    pendingDeleteCustomChecklist?.let { deleteTarget ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCustomChecklistId = null },
            title = { Text("Delete Custom Checklist?") },
            text = {
                Text(
                    "Permanently delete user-created checklist \"${deleteTarget.title}\" along with its saved progress, remarks, and metadata? Packaged default checklists are never affected."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustom(deleteTarget.id)
                        pendingDeleteCustomChecklistId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteCustomChecklistId = null },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text("Cancel") }
            }
        )
    }

    val remarkTarget = editingRemarkItem
    if (remarkTarget != null) {
        val existingRemark = activeProgressByItem[remarkTarget.id]?.remarks
        AlertDialog(
            onDismissRequest = { editingRemarkItemId = null },
            modifier = Modifier.imePadding(),
            title = {
                Text(if (existingRemark.isNullOrBlank()) "Add Inspection Remark" else "Edit Inspection Remark")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = remarkTarget.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    OutlinedTextField(
                        value = remarkDraft,
                        onValueChange = { remarkDraft = it },
                        label = { Text("Inspection remark") },
                        placeholder = { Text("Enter gauge reading, fault observation, or sign-off note") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateItemRemark(remarkTarget.id, remarkDraft)
                        editingRemarkItemId = null
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            onUpdateItemRemark(remarkTarget.id, "")
                            editingRemarkItemId = null
                        },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Clear")
                    }
                    TextButton(
                        onClick = { editingRemarkItemId = null },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showOperatorShiftDialog && activeChecklist != null) {
        val existingMeta = state.executionMetadataFor(activeChecklist.id)
        OperatorShiftEditorDialog(
            checklistTitle = activeChecklist.title,
            initialOperatorName = existingMeta?.operatorName.orEmpty(),
            initialShift = existingMeta?.shift ?: ChecklistShift.UNSPECIFIED,
            onDismiss = { showOperatorShiftDialog = false },
            onSave = { operatorName, shift ->
                onUpdateOperatorAndShift(activeChecklist.id, operatorName, shift)
                showOperatorShiftDialog = false
            }
        )
    }

    if (showExecutionEquipmentPicker && activeChecklist != null) {
        val currentEq = state.effectiveEquipmentId(activeChecklist)
        ChecklistEquipmentPickerDialog(
            availableEquipment = state.availableEquipment,
            selectedEquipmentId = currentEq,
            onSelectEquipmentId = { eqId ->
                onUpdateChecklistEquipment(activeChecklist.id, eqId)
                showExecutionEquipmentPicker = false
            },
            onDismiss = { showExecutionEquipmentPicker = false }
        )
    }

    if (state.isCustomEditorOpen) {
        CustomChecklistEditorDialog(
            state = state,
            onDismiss = onCloseCustomEditor,
            onTitleChange = onEditorTitleChange,
            onDescriptionChange = onEditorDescriptionChange,
            onOpenEquipmentPicker = { showEditorEquipmentPicker = true },
            onDraftItemTextChange = onDraftItemTextChange,
            onDraftItemCategoryChange = onDraftItemCategoryChange,
            onToggleDraftItemCrucial = onToggleDraftItemCrucial,
            onAddOrSaveDraftItem = onAddOrSaveDraftItem,
            onEditDraftItem = onEditDraftItem,
            onCancelDraftItemEdit = onCancelDraftItemEdit,
            onRemoveDraftItem = onRemoveDraftItem,
            onSaveChecklist = onSaveCustomChecklist
        )
    }

    if (showEditorEquipmentPicker) {
        ChecklistEquipmentPickerDialog(
            availableEquipment = state.availableEquipment,
            selectedEquipmentId = state.editorEquipmentId,
            onSelectEquipmentId = { eqId ->
                onEditorEquipmentIdChange(eqId)
                showEditorEquipmentPicker = false
            },
            onDismiss = { showEditorEquipmentPicker = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Maintenance Checklists",
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
                actions = {
                    IconButton(
                        onClick = onOpenCreateCustom,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create custom checklist"
                        )
                    }
                    if (activeChecklist != null) {
                        val isBookmarked = state.bookmarkedChecklistIds.any {
                            it.equals(activeChecklist.id, ignoreCase = true)
                        }
                        IconButton(
                            onClick = { onToggleBookmark(activeChecklist.id) },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) {
                                    "Remove bookmark for ${activeChecklist.title}"
                                } else {
                                    "Bookmark ${activeChecklist.title}"
                                },
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = { showResetConfirm = true },
                        enabled = activeChecklist != null,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset ${activeChecklist?.title ?: "current checklist"}"
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
            state.feedbackMessage?.let { msg ->
                Surface(
                    color = if (state.isFeedbackError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isFeedbackError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissFeedback,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss message")
                        }
                    }
                }
            }

            state.equipmentFilterId?.let { filterEqId ->
                val filterDisplay = ChecklistWorkflowHelper.resolveEquipmentDisplay(
                    equipmentId = filterEqId,
                    equipmentById = state.equipmentById,
                    catalogLoaded = state.equipmentCatalogLoaded
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Engineering,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Filtered by equipment: $filterDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = onClearEquipmentFilter,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Show All")
                        }
                    }
                }
            }

            // Source Filter Row (All / Packaged / Custom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChecklistSourceFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        ChecklistSourceFilter.ALL -> state.checklists.size
                        ChecklistSourceFilter.PACKAGED -> state.checklists.count { !it.isCustom }
                        ChecklistSourceFilter.CUSTOM -> state.checklists.count { it.isCustom }
                    }
                    FilterChip(
                        selected = state.sourceFilter == filter,
                        onClick = { onSourceFilterChange(filter) },
                        label = {
                            Text(
                                text = "${filter.label} ($count)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                }
            }

            if (visibleList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleList.forEach { chk ->
                        val chipSummary = state.progressSummaryFor(chk)
                        FilterChip(
                            selected = chk.id == (activeChecklist?.id ?: state.selectedChecklistId),
                            onClick = { onSelectChecklist(chk.id) },
                            label = {
                                Text(
                                    text = if (chk.isCustom) {
                                        "${chk.title} (${chipSummary.completedCount}/${chipSummary.totalCount})"
                                    } else {
                                        chk.title
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }
            }

            when {
                state.isLoading && state.checklists.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                activeChecklist == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when {
                                state.sourceFilter == ChecklistSourceFilter.CUSTOM -> {
                                    EmptyStateCard(
                                        title = "No Custom Checklists Yet",
                                        message = "Create your own machine-specific or shift inspection checklist tailored to your plant."
                                    )
                                    Button(
                                        onClick = onOpenCreateCustom,
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Create Custom Checklist")
                                    }
                                }
                                state.equipmentFilterId != null -> {
                                    EmptyStateCard(
                                        title = "No Checklists Linked to ${state.equipmentFilterId}",
                                        message = "No packaged or custom checklist is currently associated with ${state.equipmentFilterId}. Create one or view all checklists."
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = onClearEquipmentFilter,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Show All")
                                        }
                                        Button(
                                            onClick = onOpenCreateCustom,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Create Custom")
                                        }
                                    }
                                }
                                else -> {
                                    EmptyStateCard(
                                        title = "No Maintenance Checklists Available",
                                        message = "Statutory boiler and wash plant inspection checklists could not be loaded."
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    val summary = state.progressSummaryFor(activeChecklist)
                    val totalItems = summary.totalCount
                    val completedCount = summary.completedCount
                    val progress = if (totalItems > 0) completedCount.toFloat() / totalItems.toFloat() else 0f
                    val effectiveEqId = state.effectiveEquipmentId(activeChecklist)
                    val resolvedEqLabel = ChecklistWorkflowHelper.resolveEquipmentDisplay(
                        equipmentId = effectiveEqId,
                        equipmentById = state.equipmentById,
                        catalogLoaded = state.equipmentCatalogLoaded
                    )
                    val executionMeta = state.executionMetadataFor(activeChecklist.id)
                    val operatorDisplay = executionMeta?.operatorName?.takeIf { it.isNotBlank() } ?: "Not specified"
                    val shiftDisplay = (executionMeta?.shift ?: ChecklistShift.UNSPECIFIED).label

                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val positiveColors = semanticPositiveColors(isDark)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeChecklist.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { heading() }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$completedCount / $totalItems Done",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when (summary.status) {
                                        ChecklistCompletionStatus.COMPLETED -> positiveColors.container
                                        ChecklistCompletionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                                        ChecklistCompletionStatus.NOT_STARTED -> MaterialTheme.colorScheme.surface
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = summary.status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (summary.status) {
                                            ChecklistCompletionStatus.COMPLETED -> positiveColors.content
                                            ChecklistCompletionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                                            ChecklistCompletionStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (activeChecklist.isCustom) {
                                            "Custom Template"
                                        } else {
                                            "Packaged Default (Read-Only)"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                if (summary.mandatoryTotalCount > 0) {
                                    Text(
                                        text = "Mandatory: ${summary.mandatoryCompletedCount}/${summary.mandatoryTotalCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeChecklist.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Reference: ${activeChecklist.standardReference}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Field inspection log only — not an official OCIB or statutory compliance certificate.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            if (activeChecklist.isCustom) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onOpenEditCustom(activeChecklist.id) },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Template")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { pendingDeleteCustomChecklistId = activeChecklist.id },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }

                    // Execution Assignment & Operator/Shift Metadata Card (separate from static/template data)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Equipment: $resolvedEqLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton(
                                    onClick = { showExecutionEquipmentPicker = true },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Change")
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Operator: $operatorDisplay",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Shift: $shiftDisplay",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = { showOperatorShiftDialog = true },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Operator & Shift")
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(activeChecklist.items, key = { it.id }) { item ->
                            val itemProgress = activeProgressByItem[item.id]
                            val isDone = state.completedItemIds.contains(item.id)
                            val savedRemark = itemProgress?.remarks?.takeIf { it.isNotBlank() }
                            val completionTimestamp = itemProgress?.completionTimestamp
                            val hasRemark = savedRemark != null

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp)
                                            .toggleable(
                                                value = isDone,
                                                role = Role.Checkbox,
                                                onValueChange = { onToggleItem(item.id) }
                                            )
                                            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isDone,
                                            onCheckedChange = null
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = item.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                if (item.isCrucial) {
                                                    val dangerColors = semanticDangerColors(isDark)
                                                    Surface(
                                                        color = dangerColors.container,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "MANDATORY",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = dangerColors.content,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium
                                            )

                                            if (completionTimestamp != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Completed: ${formatChecklistTimestamp(completionTimestamp)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            if (savedRemark != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Remark: $savedRemark",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            remarkDraft = savedRemark.orEmpty()
                                            editingRemarkItemId = item.id
                                        },
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (hasRemark) Icons.Default.Edit else Icons.Default.NoteAlt,
                                            contentDescription = if (hasRemark) {
                                                "Edit remark for ${item.text}"
                                            } else {
                                                "Add remark for ${item.text}"
                                            },
                                            tint = if (hasRemark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun OperatorShiftEditorDialog(
    checklistTitle: String,
    initialOperatorName: String,
    initialShift: ChecklistShift,
    onDismiss: () -> Unit,
    onSave: (String, ChecklistShift) -> Unit
) {
    var operatorDraft by rememberSaveable { mutableStateOf(initialOperatorName) }
    var selectedShiftName by rememberSaveable { mutableStateOf(initialShift.name) }
    val selectedShift = ChecklistShift.fromStored(selectedShiftName)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text("Operator & Shift") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = checklistTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                OutlinedTextField(
                    value = operatorDraft,
                    onValueChange = { operatorDraft = it },
                    label = { Text("Operator / Technician Name (Optional)") },
                    placeholder = { Text("e.g., Rahim Uddin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Optional local operator label only. Do not enter employee IDs, phone numbers, or sensitive personal identifiers.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Shift",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChecklistShift.entries.forEach { shift ->
                        val isSelected = selectedShift == shift
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = "Select shift ${shift.label}",
                                    onClick = { selectedShiftName = shift.name }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = shift.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(operatorDraft, selectedShift) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { onSave("", ChecklistShift.UNSPECIFIED) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Clear")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun ChecklistEquipmentPickerDialog(
    availableEquipment: List<ChecklistEquipmentOption>,
    selectedEquipmentId: String,
    onSelectEquipmentId: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pickerQuery by rememberSaveable { mutableStateOf("") }
    val normalizedSelected = NoteAssociationHelper.normalizeEquipmentIdForStorage(selectedEquipmentId)
    val isLegacySelection = !NoteAssociationHelper.isUnassociated(normalizedSelected) &&
        availableEquipment.none { it.id.equals(normalizedSelected, ignoreCase = true) }

    val matchingEquipment = remember(availableEquipment, pickerQuery) {
        val trimmed = pickerQuery.trim()
        if (trimmed.isEmpty()) {
            availableEquipment
        } else {
            availableEquipment.filter { option ->
                option.id.contains(trimmed, ignoreCase = true) ||
                    option.name.contains(trimmed, ignoreCase = true) ||
                    option.bengaliName.contains(trimmed, ignoreCase = true) ||
                    option.familyId.contains(trimmed, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text("Associate Equipment") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pickerQuery,
                    onValueChange = { pickerQuery = it },
                    placeholder = { Text("Search equipment ID or name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (pickerQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { pickerQuery = "" },
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item(key = "unassociated-checklist-eq") {
                        val isSelected = NoteAssociationHelper.isUnassociated(normalizedSelected)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Select General Unassociated",
                                    onClick = { onSelectEquipmentId(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID) }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "General / Unassociated",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Plant-wide or area inspection without a specific machine ID",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            }
                        }
                    }

                    if (isLegacySelection) {
                        item(key = "legacy-checklist-eq") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Keep preserved equipment ID $normalizedSelected",
                                        onClick = { onSelectEquipmentId(normalizedSelected) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "$normalizedSelected (Preserved — Unavailable in Catalog)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Keep legacy equipment ID intact",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            }
                        }
                    }

                    if (matchingEquipment.isEmpty()) {
                        item(key = "no-eq-match") {
                            Text(
                                text = if (availableEquipment.isEmpty()) {
                                    "Equipment catalog is currently empty."
                                } else {
                                    "No catalog equipment matches \"$pickerQuery\"."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(matchingEquipment, key = { it.id }) { eq ->
                            val isSelected = eq.id.equals(normalizedSelected, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Select ${eq.id} ${eq.name}",
                                        onClick = { onSelectEquipmentId(eq.id) }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${eq.id} — ${eq.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (eq.bengaliName.isNotBlank()) {
                                            Text(
                                                text = eq.bengaliName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Check, contentDescription = "Selected")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CustomChecklistEditorDialog(
    state: ChecklistUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onOpenEquipmentPicker: () -> Unit,
    onDraftItemTextChange: (String) -> Unit,
    onDraftItemCategoryChange: (String) -> Unit,
    onToggleDraftItemCrucial: () -> Unit,
    onAddOrSaveDraftItem: () -> Unit,
    onEditDraftItem: (Int) -> Unit,
    onCancelDraftItemEdit: () -> Unit,
    onRemoveDraftItem: (Int) -> Unit,
    onSaveChecklist: () -> Unit
) {
    val isEditing = state.editingCustomChecklistId != null
    val resolvedEq = ChecklistWorkflowHelper.resolveEquipmentDisplay(
        equipmentId = state.editorEquipmentId,
        equipmentById = state.equipmentById,
        catalogLoaded = state.equipmentCatalogLoaded
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Text(if (isEditing) "Edit Custom Checklist" else "Create Custom Checklist")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.editorTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Checklist Title *") },
                    placeholder = { Text("e.g., Hydro Extractor Pre-Shift Check") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.editorDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Scope or operational focus") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onOpenEquipmentPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Equipment: $resolvedEq",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                Text(
                    text = if (state.editingDraftItemIndex != null) {
                        "Edit Checklist Item #${state.editingDraftItemIndex + 1}"
                    } else {
                        "Add Checklist Item"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = state.draftItemText,
                    onValueChange = onDraftItemTextChange,
                    label = { Text("Inspection step text *") },
                    placeholder = { Text("Describe measurable verification or check") },
                    isError = state.draftItemError != null,
                    supportingText = state.draftItemError?.let { err -> { Text(err) } },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChecklistWorkflowHelper.PRESET_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = state.draftItemCategory.equals(cat, ignoreCase = true),
                            onClick = { onDraftItemCategoryChange(cat) },
                            label = { Text(cat) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .toggleable(
                            value = state.draftItemCrucial,
                            role = Role.Checkbox,
                            onValueChange = { onToggleDraftItemCrucial() }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.draftItemCrucial,
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as mandatory check item", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddOrSaveDraftItem,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(if (state.editingDraftItemIndex != null) "Update Item" else "Add Item")
                    }
                    if (state.editingDraftItemIndex != null) {
                        OutlinedButton(
                            onClick = onCancelDraftItemEdit,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Cancel Edit")
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Checklist Items (${state.editorItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (state.editorItems.isEmpty()) {
                    Text(
                        text = "No items added yet. Add at least one inspection item above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.editorItems.forEachIndexed { index, item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${index + 1}. [${item.category}]${if (item.isCrucial) " • MANDATORY" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = item.text,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEditDraftItem(index) },
                                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit item ${index + 1}")
                                    }
                                    IconButton(
                                        onClick = { onRemoveDraftItem(index) },
                                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove item ${index + 1}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                state.editorValidationError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveChecklist,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text(if (isEditing) "Update Checklist" else "Save Checklist")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun formatChecklistTimestamp(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date(timestamp))
}

private fun sampleChecklistUiState(
    selectedId: String = PlantChecklistCatalog.ID_BOILER_DAILY,
    completedIds: Set<Int> = setOf(1, 2)
): ChecklistUiState {
    val mapped = PlantChecklistCatalog.ALL_CHECKLISTS.map { catalog ->
        PlantChecklist(
            id = catalog.id,
            title = catalog.title,
            description = catalog.description,
            standardReference = catalog.standardReference,
            defaultEquipmentId = catalog.defaultEquipmentId,
            relatedFamilyId = catalog.relatedFamilyId,
            items = catalog.items.map { item ->
                ChecklistItem(
                    id = item.id,
                    text = item.text,
                    category = item.category,
                    isCrucial = item.isCrucial
                )
            }
        )
    }
    val sampleProgress = mapOf(
        selectedId to completedIds.associateWith { id ->
            ChecklistProgress(
                checklistId = selectedId,
                itemId = id,
                isCompleted = true,
                completionTimestamp = 1_763_200_000_000L,
                remarks = if (id == completedIds.firstOrNull()) "Gauge glass blown down clean; zero leakage." else null
            )
        }
    )
    return ChecklistUiState(
        checklists = mapped,
        selectedChecklistId = selectedId,
        completedByChecklist = mapOf(selectedId to completedIds),
        progressByChecklist = sampleProgress,
        completedItemIds = completedIds
    )
}

@Preview(name = "Checklists Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ChecklistScreenLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        ChecklistScreenContent(
            state = sampleChecklistUiState(),
            onOpenDrawer = {},
            onSelectChecklist = {},
            onToggleItem = {},
            onResetCurrentChecklist = {}
        )
    }
}

@Preview(name = "Checklists Dark High Legibility", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ChecklistScreenDarkHighLegibilityPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        ChecklistScreenContent(
            state = sampleChecklistUiState(
                selectedId = PlantChecklistCatalog.ID_HYDRO_SAFETY,
                completedIds = setOf(31)
            ),
            onOpenDrawer = {},
            onSelectChecklist = {},
            onToggleItem = {},
            onResetCurrentChecklist = {}
        )
    }
}
