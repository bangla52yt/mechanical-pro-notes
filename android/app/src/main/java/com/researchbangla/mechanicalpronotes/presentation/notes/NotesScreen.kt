package com.researchbangla.mechanicalpronotes.presentation.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.model.UserNote
import com.researchbangla.mechanicalpronotes.presentation.components.EmptyStateCard
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToEquipment: (String) -> Unit,
    initialEquipmentId: String? = null
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleteNote by remember { mutableStateOf<UserNote?>(null) }

    LaunchedEffect(initialEquipmentId) {
        if (!initialEquipmentId.isNullOrBlank()) {
            viewModel.openNoteEditor(equipmentId = initialEquipmentId)
        }
    }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessage()
        }
    }

    NotesScreenContent(
        state = state,
        pendingDeleteNote = pendingDeleteNote,
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        onNavigateToEquipment = onNavigateToEquipment,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearchAndFilters = viewModel::clearSearchAndFilters,
        onSelectFilter = viewModel::selectFilter,
        onSelectSortOrder = viewModel::selectSortOrder,
        onOpenNoteDetail = viewModel::openNoteDetail,
        onCloseNoteDetail = viewModel::closeNoteDetail,
        onOpenNoteEditor = { eqId, note -> viewModel.openNoteEditor(eqId, note) },
        onCloseNoteEditor = viewModel::closeNoteEditor,
        onEditorEquipmentIdChange = viewModel::onEditorEquipmentIdChange,
        onEditorTitleChange = viewModel::onEditorTitleChange,
        onEditorBodyChange = viewModel::onEditorBodyChange,
        onEditorTagInputChange = viewModel::onEditorTagInputChange,
        onAddEditorTag = { viewModel.addEditorTag() },
        onRemoveEditorTag = viewModel::removeEditorTag,
        onSaveNote = viewModel::saveNote,
        onRequestDeleteNote = { pendingDeleteNote = it },
        onConfirmDeleteNote = { target ->
            viewModel.deleteNote(target.id)
            pendingDeleteNote = null
        },
        onDismissDeleteNote = { pendingDeleteNote = null }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesScreenContent(
    state: NotesUiState,
    pendingDeleteNote: UserNote? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenDrawer: () -> Unit,
    onNavigateToEquipment: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onClearSearchAndFilters: () -> Unit = {},
    onSelectFilter: (NoteFilter) -> Unit = {},
    onSelectSortOrder: (NoteSortOrder) -> Unit = {},
    onOpenNoteDetail: (UserNote) -> Unit = {},
    onCloseNoteDetail: () -> Unit = {},
    onOpenNoteEditor: (String, UserNote?) -> Unit,
    onCloseNoteEditor: () -> Unit,
    onEditorEquipmentIdChange: (String) -> Unit,
    onEditorTitleChange: (String) -> Unit,
    onEditorBodyChange: (String) -> Unit,
    onEditorTagInputChange: (String) -> Unit = {},
    onAddEditorTag: () -> Unit = {},
    onRemoveEditorTag: (String) -> Unit = {},
    onSaveNote: () -> Unit,
    onRequestDeleteNote: (UserNote) -> Unit,
    onConfirmDeleteNote: (UserNote) -> Unit,
    onDismissDeleteNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEquipmentPickerOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Field Notes & Logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onOpenNoteEditor(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, null)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new field note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.notes.isEmpty()) {
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
                        EmptyStateCard(
                            title = "No Personal Notes Yet",
                            message = "Tap + to record maintenance observations, gauge readings, overhaul reminders, or general plant notes. Notes are stored offline on your device."
                        )
                        Button(
                            onClick = {
                                onOpenNoteEditor(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, null)
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Field Note")
                        }
                    }
                }
            } else {
                // Search Bar + Filter + Sort Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text("Search title, body, equipment ID/name, or tags...")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear notes search")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NoteFilter.entries, key = { it.name }) { filter ->
                            val selected = state.selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectFilter(filter) },
                                label = { Text(filter.label) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item(key = "sort-label") {
                            Text(
                                text = "Sort:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(NoteSortOrder.entries, key = { it.name }) { sortOrder ->
                            val selected = state.selectedSortOrder == sortOrder
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSortOrder(sortOrder) },
                                label = { Text(sortOrder.label) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Sort,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }

                if (state.filteredNotes.isEmpty()) {
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
                            EmptyStateCard(
                                title = "No Matching Field Notes",
                                message = "No saved notes match your active search query or filter criteria."
                            )
                            OutlinedButton(
                                onClick = onClearSearchAndFilters,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.FilterAltOff, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset Search & Filters")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp)
                    ) {
                        item(key = "notes-count-header") {
                            Text(
                                text = "Personal Field Records (${state.filteredNotes.size} of ${state.notes.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        items(state.filteredNotes, key = { it.id }) { note ->
                            val resolvedEquipmentName = state.equipmentNamesById[note.equipmentId.trim().uppercase()]
                            val catalogLoaded = state.availableEquipment.isNotEmpty()
                            NoteCardItem(
                                note = note,
                                equipmentName = resolvedEquipmentName,
                                catalogLoaded = catalogLoaded,
                                onOpenDetail = { onOpenNoteDetail(note) },
                                onEdit = { onOpenNoteEditor(note.equipmentId, note) },
                                onDelete = { onRequestDeleteNote(note) },
                                onNavigateToEquipment = { onNavigateToEquipment(note.equipmentId) }
                            )
                        }
                    }
                }
            }
        }

        state.selectedNoteForDetail?.let { detailNote ->
            NoteDetailDialog(
                note = detailNote,
                equipmentName = state.equipmentNamesById[detailNote.equipmentId.trim().uppercase()],
                catalogLoaded = state.availableEquipment.isNotEmpty(),
                onDismiss = onCloseNoteDetail,
                onNavigateToEquipment = {
                    onCloseNoteDetail()
                    onNavigateToEquipment(detailNote.equipmentId)
                },
                onEdit = {
                    onCloseNoteDetail()
                    onOpenNoteEditor(detailNote.equipmentId, detailNote)
                },
                onDelete = {
                    onCloseNoteDetail()
                    onRequestDeleteNote(detailNote)
                }
            )
        }

        if (state.isEditorOpen) {
            val isBodyEmpty = state.editorBody.isBlank()
            val normalizedSelectedEqId = NoteAssociationHelper.normalizeEquipmentIdForStorage(state.editorEquipmentId)
            val isUnassociatedSelected = NoteAssociationHelper.isUnassociated(normalizedSelectedEqId)
            val selectedEquipmentName = state.equipmentNamesById[normalizedSelectedEqId.uppercase()]

            AlertDialog(
                onDismissRequest = onCloseNoteEditor,
                title = {
                    Text(if (state.editingNoteId == null) "New Field Note" else "Edit Field Note")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Searchable Equipment Association Selector
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Equipment Association",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val associationSummary = when {
                                    isUnassociatedSelected -> "General / Unassociated Note (No Equipment)"
                                    selectedEquipmentName != null -> "$normalizedSelectedEqId — $selectedEquipmentName"
                                    state.availableEquipment.isEmpty() -> normalizedSelectedEqId
                                    else -> "$normalizedSelectedEqId — Catalog record unavailable (preserved)"
                                }
                                Text(
                                    text = associationSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { isEquipmentPickerOpen = true },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Engineering,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isUnassociatedSelected) "Select Equipment" else "Change Equipment"
                                        )
                                    }
                                    if (!isUnassociatedSelected) {
                                        TextButton(
                                            onClick = {
                                                onEditorEquipmentIdChange(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID)
                                            },
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text("Unlink")
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.editorTitle,
                            onValueChange = onEditorTitleChange,
                            label = { Text("Title / Summary (Optional)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.editorBody,
                            onValueChange = onEditorBodyChange,
                            label = { Text("Observation & Action *") },
                            isError = isBodyEmpty,
                            supportingText = {
                                if (isBodyEmpty) {
                                    Text("Required field — enter observation before saving.")
                                }
                            },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Note Tags Editor
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                OutlinedTextField(
                                    value = state.editorTagInput,
                                    onValueChange = onEditorTagInputChange,
                                    label = { Text("Tags (comma or Add)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { onAddEditorTag() }),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = onAddEditorTag,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Add")
                                }
                            }

                            if (state.editorTags.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    state.editorTags.forEach { tag ->
                                        InputChip(
                                            selected = true,
                                            onClick = { onRemoveEditorTag(tag) },
                                            label = { Text("#$tag") },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove tag $tag",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onSaveNote,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onCloseNoteEditor,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (isEquipmentPickerOpen) {
            EquipmentPickerDialog(
                availableEquipment = state.availableEquipment,
                selectedEquipmentId = state.editorEquipmentId,
                onSelectEquipmentId = { chosenId ->
                    onEditorEquipmentIdChange(chosenId)
                    isEquipmentPickerOpen = false
                },
                onDismiss = { isEquipmentPickerOpen = false }
            )
        }

        pendingDeleteNote?.let { target ->
            val targetLabel = if (NoteAssociationHelper.isUnassociated(target.equipmentId)) {
                "General / Unassociated"
            } else {
                target.equipmentId
            }
            AlertDialog(
                onDismissRequest = onDismissDeleteNote,
                title = { Text("Delete Field Note?") },
                text = {
                    Text("Delete note \"${target.title}\" ($targetLabel)? This cannot be undone unless restored from backup.")
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmDeleteNote(target) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissDeleteNote,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun EquipmentPickerDialog(
    availableEquipment: List<EquipmentNoteOption>,
    selectedEquipmentId: String,
    onSelectEquipmentId: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pickerQuery by remember { mutableStateOf("") }
    val normalizedSelected = NoteAssociationHelper.normalizeEquipmentIdForStorage(selectedEquipmentId)
    val isUnknownLegacySelection = !NoteAssociationHelper.isUnassociated(normalizedSelected) &&
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
        title = { Text("Select Equipment") },
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
                                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear equipment filter")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item(key = "unassociated-option") {
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
                                    onClickLabel = "Select General Unassociated Note",
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
                                        text = "No Equipment (Unassociated Note)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Store as general plant or shift note",
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

                    if (isUnknownLegacySelection) {
                        item(key = "legacy-unavailable-option") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Keep existing equipment ID $normalizedSelected",
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
                                            text = "$normalizedSelected (Current — Unavailable in Catalog)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Keep preserved equipment reference",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            }
                        }
                    }

                    if (matchingEquipment.isEmpty()) {
                        item(key = "no-equipment-matches") {
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
                                        onClickLabel = "Associate with ${eq.id} ${eq.name}",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteDetailDialog(
    note: UserNote,
    equipmentName: String?,
    catalogLoaded: Boolean,
    onDismiss: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val updatedText = remember(note.updatedAt) { dateFormat.format(Date(note.updatedAt)) }
    val createdText = remember(note.createdAt) { dateFormat.format(Date(note.createdAt)) }
    val isUnassociated = NoteAssociationHelper.isUnassociated(note.equipmentId)
    val canOpenEquipment = !isUnassociated && (equipmentName != null || !catalogLoaded)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(note.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val eqDisplay = when {
                    isUnassociated -> "General / Unassociated Note"
                    equipmentName != null -> "${note.equipmentId} — $equipmentName"
                    else -> "${note.equipmentId} (Catalog record unavailable)"
                }
                Text(
                    text = eqDisplay,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = note.body,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (note.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        note.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "Created: $createdText\nUpdated: $updatedText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (canOpenEquipment) {
                    OutlinedButton(
                        onClick = onNavigateToEquipment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Equipment Record")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Delete")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Close")
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCardItem(
    note: UserNote,
    equipmentName: String? = null,
    catalogLoaded: Boolean = false,
    onOpenDetail: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToEquipment: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormat.format(Date(note.updatedAt)) }
    val isUnassociated = NoteAssociationHelper.isUnassociated(note.equipmentId)
    val isEquipmentNavigable = !isUnassociated && (equipmentName != null || !catalogLoaded)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open details for note ${note.title}",
                onClick = onOpenDetail
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isUnassociated -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.NoteAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "General / No Equipment",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        isEquipmentNavigable -> {
                            val eqLabel = if (!equipmentName.isNullOrBlank()) {
                                "${note.equipmentId} • $equipmentName"
                            } else {
                                note.equipmentId
                            }
                            AssistChip(
                                onClick = onNavigateToEquipment,
                                label = {
                                    Text(
                                        text = eqLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Engineering,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            )
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${note.equipmentId} • Catalog unavailable",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit note ${note.title}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete note ${note.title}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private val sampleNotes = listOf(
    UserNote(
        id = 1L,
        equipmentId = "EQ-G01",
        title = "Safety Valve Weep Check",
        body = "Observed slight steam whisper at 8.2 bar(g). Schedule seat lapping during Friday shutdown.",
        tags = listOf("boiler", "safety-valve"),
        createdAt = 1731640000000L,
        updatedAt = 1731648000000L
    ),
    UserNote(
        id = 2L,
        equipmentId = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
        title = "Friday Shutdown Tooling",
        body = "Prepare torque wrench and spare spiral-wound gaskets for utility isolation shift.",
        tags = listOf("shutdown"),
        createdAt = 1731560000000L,
        updatedAt = 1731561600000L
    )
)

@Preview(name = "Notes Empty Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotesScreenEmptyLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        NotesScreenContent(
            state = NotesUiState(isLoading = false, notes = emptyList()),
            onOpenDrawer = {},
            onNavigateToEquipment = {},
            onOpenNoteEditor = { _, _ -> },
            onCloseNoteEditor = {},
            onEditorEquipmentIdChange = {},
            onEditorTitleChange = {},
            onEditorBodyChange = {},
            onSaveNote = {},
            onRequestDeleteNote = {},
            onConfirmDeleteNote = {},
            onDismissDeleteNote = {}
        )
    }
}

@Preview(name = "Notes Populated Dark", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotesScreenPopulatedDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK, highLegibility = true) {
        NotesScreenContent(
            state = NotesUiState(
                isLoading = false,
                notes = sampleNotes,
                filteredNotes = sampleNotes,
                equipmentNamesById = mapOf("EQ-G01" to "Natural Gas Firetube Boiler")
            ),
            onOpenDrawer = {},
            onNavigateToEquipment = {},
            onOpenNoteEditor = { _, _ -> },
            onCloseNoteEditor = {},
            onEditorEquipmentIdChange = {},
            onEditorTitleChange = {},
            onEditorBodyChange = {},
            onSaveNote = {},
            onRequestDeleteNote = {},
            onConfirmDeleteNote = {},
            onDismissDeleteNote = {}
        )
    }
}

@Preview(name = "Notes Editor Open Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotesScreenEditorLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        NotesScreenContent(
            state = NotesUiState(
                isLoading = false,
                notes = sampleNotes,
                filteredNotes = sampleNotes,
                equipmentNamesById = mapOf("EQ-G01" to "Natural Gas Firetube Boiler"),
                isEditorOpen = true,
                editorEquipmentId = "EQ-G01",
                editorTitle = "Feedwater Temp Log",
                editorBody = "",
                editorTags = listOf("feedwater", "shift-a")
            ),
            onOpenDrawer = {},
            onNavigateToEquipment = {},
            onOpenNoteEditor = { _, _ -> },
            onCloseNoteEditor = {},
            onEditorEquipmentIdChange = {},
            onEditorTitleChange = {},
            onEditorBodyChange = {},
            onSaveNote = {},
            onRequestDeleteNote = {},
            onConfirmDeleteNote = {},
            onDismissDeleteNote = {}
        )
    }
}
