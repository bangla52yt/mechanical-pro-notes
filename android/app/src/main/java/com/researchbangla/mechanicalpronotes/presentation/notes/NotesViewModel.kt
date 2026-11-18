package com.researchbangla.mechanicalpronotes.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.model.NoteTagHelper
import com.researchbangla.mechanicalpronotes.domain.model.UserBookmark
import com.researchbangla.mechanicalpronotes.domain.model.UserNote
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

enum class NoteFilter(val label: String) {
    ALL("All Notes"),
    EQUIPMENT_ASSOCIATED("Equipment-Linked"),
    UNASSOCIATED("Unassociated"),
    TAGGED("Tagged")
}

enum class NoteSortOrder(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    RECENTLY_CREATED("Recently Created"),
    EQUIPMENT_ID("Equipment ID")
}

data class EquipmentNoteOption(
    val id: String,
    val name: String,
    val bengaliName: String = "",
    val familyId: String = ""
)

object NotesCatalogFilter {
    fun filterAndSort(
        notes: List<UserNote>,
        query: String,
        filter: NoteFilter,
        sortOrder: NoteSortOrder,
        equipmentNamesById: Map<String, String> = emptyMap()
    ): List<UserNote> {
        val trimmedQuery = Normalizer.normalize(query.trim(), Normalizer.Form.NFC).lowercase()
        val tokens = if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            trimmedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        }

        val filtered = notes.filter { note ->
            val isUnassociated = NoteAssociationHelper.isUnassociated(note.equipmentId)
            val matchesFilter = when (filter) {
                NoteFilter.ALL -> true
                NoteFilter.EQUIPMENT_ASSOCIATED -> !isUnassociated
                NoteFilter.UNASSOCIATED -> isUnassociated
                NoteFilter.TAGGED -> note.tags.isNotEmpty()
            }
            if (!matchesFilter) return@filter false
            if (tokens.isEmpty()) return@filter true

            val eqKey = note.equipmentId.trim().uppercase()
            val eqName = if (isUnassociated) "" else equipmentNamesById[eqKey].orEmpty()
            val eqSearchSegment = if (isUnassociated) {
                "general unassociated no equipment"
            } else {
                "${note.equipmentId} $eqName"
            }
            val searchableBlob = Normalizer.normalize(
                listOf(
                    note.title,
                    note.body,
                    eqSearchSegment,
                    note.tags.joinToString(" ")
                ).joinToString(" "),
                Normalizer.Form.NFC
            ).lowercase()

            tokens.all { searchableBlob.contains(it) }
        }

        return when (sortOrder) {
            NoteSortOrder.RECENTLY_UPDATED -> filtered.sortedWith(
                compareByDescending<UserNote> { it.updatedAt }.thenByDescending { it.id }
            )
            NoteSortOrder.RECENTLY_CREATED -> filtered.sortedWith(
                compareByDescending<UserNote> { it.createdAt }.thenByDescending { it.id }
            )
            NoteSortOrder.EQUIPMENT_ID -> filtered.sortedWith(
                compareBy<UserNote>(
                    { NoteAssociationHelper.isUnassociated(it.equipmentId) },
                    { it.equipmentId.trim().uppercase() },
                    { -it.updatedAt }
                )
            )
        }
    }
}

data class NotesUiState(
    val notes: List<UserNote> = emptyList(),
    val filteredNotes: List<UserNote> = emptyList(),
    val bookmarks: List<UserBookmark> = emptyList(),
    val availableEquipment: List<EquipmentNoteOption> = emptyList(),
    val equipmentNamesById: Map<String, String> = emptyMap(),
    val searchQuery: String = "",
    val activeSearchQuery: String = "",
    val selectedFilter: NoteFilter = NoteFilter.ALL,
    val selectedSortOrder: NoteSortOrder = NoteSortOrder.RECENTLY_UPDATED,
    val selectedNoteForDetail: UserNote? = null,
    val isLoading: Boolean = true,
    val isEditorOpen: Boolean = false,
    val editorEquipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
    val editorTitle: String = "",
    val editorBody: String = "",
    val editorTags: List<String> = emptyList(),
    val editorTagInput: String = "",
    val editorCreatedAt: Long? = null,
    val editingNoteId: Long? = null,
    val feedbackMessage: String? = null
)

@OptIn(FlowPreview::class)
class NotesViewModel(
    private val userDataRepository: UserDataRepository,
    private val equipmentRepository: EquipmentRepository? = null,
    private val onNotesMutated: (() -> Unit)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val inFlightBookmarkKeys = mutableSetOf<String>()
    private val searchQueryInput = MutableStateFlow("")
    private val debouncedSearchQuery = MutableStateFlow("")
    private val selectedFilterFlow = MutableStateFlow(NoteFilter.ALL)
    private val selectedSortOrderFlow = MutableStateFlow(NoteSortOrder.RECENTLY_UPDATED)
    private var pendingNoteIdToOpen: Long? = null

    init {
        observeSearchDebounce()
        loadNotesEquipmentAndBookmarks()
    }

    private fun observeSearchDebounce() {
        viewModelScope.launch {
            searchQueryInput
                .drop(1)
                .map { it.trim() }
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { normalizedQuery ->
                    debouncedSearchQuery.value = normalizedQuery
                }
        }
    }

    private fun loadNotesEquipmentAndBookmarks() {
        val equipmentFlow: Flow<List<Equipment>> =
            equipmentRepository?.getAllEquipment() ?: flowOf(emptyList())

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                userDataRepository.getAllUserNotes(),
                userDataRepository.getAllBookmarks(),
                equipmentFlow,
                debouncedSearchQuery,
                selectedFilterFlow
            ) { notes, bookmarks, equipmentList, query, filter ->
                NotesCombineSnapshot(notes, bookmarks, equipmentList, query, filter)
            }.combine(selectedSortOrderFlow) { snapshot, sortOrder ->
                val equipmentOptions = snapshot.equipment
                    .sortedBy { it.id }
                    .map { eq ->
                        EquipmentNoteOption(
                            id = eq.id,
                            name = eq.name,
                            bengaliName = eq.bengaliName,
                            familyId = eq.familyId
                        )
                    }
                val equipmentNames = equipmentOptions.associate {
                    it.id.trim().uppercase() to it.name
                }
                val filtered = NotesCatalogFilter.filterAndSort(
                    notes = snapshot.notes,
                    query = snapshot.query,
                    filter = snapshot.filter,
                    sortOrder = sortOrder,
                    equipmentNamesById = equipmentNames
                )

                NotesResolvedSnapshot(
                    notes = snapshot.notes,
                    filteredNotes = filtered,
                    bookmarks = snapshot.bookmarks,
                    equipmentOptions = equipmentOptions,
                    equipmentNames = equipmentNames,
                    activeQuery = snapshot.query,
                    filter = snapshot.filter,
                    sortOrder = sortOrder
                )
            }.collect { resolved ->
                val pendingId = pendingNoteIdToOpen
                val pendingMatch = if (pendingId != null) resolved.notes.firstOrNull { it.id == pendingId } else null
                if (pendingMatch != null) {
                    pendingNoteIdToOpen = null
                }
                _uiState.update { current ->
                    val refreshedDetail = current.selectedNoteForDetail?.let { openDetail ->
                        resolved.notes.firstOrNull { it.id == openDetail.id }
                    }
                    val updated = current.copy(
                        notes = resolved.notes,
                        filteredNotes = resolved.filteredNotes,
                        bookmarks = resolved.bookmarks,
                        availableEquipment = resolved.equipmentOptions,
                        equipmentNamesById = resolved.equipmentNames,
                        activeSearchQuery = resolved.activeQuery,
                        selectedFilter = resolved.filter,
                        selectedSortOrder = resolved.sortOrder,
                        selectedNoteForDetail = refreshedDetail,
                        isLoading = false
                    )
                    if (pendingMatch != null) {
                        updated.copy(
                            isEditorOpen = true,
                            selectedNoteForDetail = null,
                            editorEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(pendingMatch.equipmentId),
                            editorTitle = pendingMatch.title,
                            editorBody = pendingMatch.body,
                            editorTags = NoteTagHelper.normalizeTags(pendingMatch.tags),
                            editorTagInput = "",
                            editorCreatedAt = pendingMatch.createdAt,
                            editingNoteId = pendingMatch.id
                        )
                    } else {
                        updated
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryInput.value = query
        if (query.isBlank()) {
            debouncedSearchQuery.value = ""
        }
    }

    fun clearSearchAndFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedFilter = NoteFilter.ALL) }
        searchQueryInput.value = ""
        debouncedSearchQuery.value = ""
        selectedFilterFlow.value = NoteFilter.ALL
    }

    fun selectFilter(filter: NoteFilter) {
        selectedFilterFlow.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun selectSortOrder(sortOrder: NoteSortOrder) {
        selectedSortOrderFlow.value = sortOrder
        _uiState.update { it.copy(selectedSortOrder = sortOrder) }
    }

    fun openNoteDetail(note: UserNote) {
        _uiState.update { it.copy(selectedNoteForDetail = note) }
    }

    fun closeNoteDetail() {
        _uiState.update { it.copy(selectedNoteForDetail = null) }
    }

    fun openNoteEditor(
        equipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
        noteToEdit: UserNote? = null
    ) {
        pendingNoteIdToOpen = null
        val targetEquipmentId = if (noteToEdit != null) {
            NoteAssociationHelper.normalizeEquipmentIdForStorage(noteToEdit.equipmentId)
        } else {
            NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)
        }
        _uiState.update {
            it.copy(
                isEditorOpen = true,
                selectedNoteForDetail = null,
                editorEquipmentId = targetEquipmentId,
                editorTitle = noteToEdit?.title ?: "",
                editorBody = noteToEdit?.body ?: "",
                editorTags = NoteTagHelper.normalizeTags(noteToEdit?.tags.orEmpty()),
                editorTagInput = "",
                editorCreatedAt = noteToEdit?.createdAt,
                editingNoteId = noteToEdit?.id
            )
        }
    }

    fun openNoteById(
        noteId: Long,
        fallbackEquipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID
    ) {
        val match = _uiState.value.notes.firstOrNull { it.id == noteId }
        if (match != null) {
            pendingNoteIdToOpen = null
            openNoteEditor(equipmentId = match.equipmentId, noteToEdit = match)
        } else {
            pendingNoteIdToOpen = noteId
            if (_uiState.value.isLoading.not() && !NoteAssociationHelper.isUnassociated(fallbackEquipmentId)) {
                _uiState.update {
                    it.copy(
                        isEditorOpen = true,
                        selectedNoteForDetail = null,
                        editorEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(fallbackEquipmentId),
                        editorTitle = "",
                        editorBody = "",
                        editorTags = emptyList(),
                        editorTagInput = "",
                        editorCreatedAt = null,
                        editingNoteId = null
                    )
                }
            }
        }
    }

    fun closeNoteEditor() {
        _uiState.update {
            it.copy(
                isEditorOpen = false,
                editorEquipmentId = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
                editorTitle = "",
                editorBody = "",
                editorTags = emptyList(),
                editorTagInput = "",
                editorCreatedAt = null,
                editingNoteId = null
            )
        }
    }

    fun onEditorEquipmentIdChange(equipmentId: String) {
        val normalized = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)
        _uiState.update { it.copy(editorEquipmentId = normalized) }
    }

    fun onEditorTitleChange(title: String) {
        _uiState.update { it.copy(editorTitle = title) }
    }

    fun onEditorBodyChange(body: String) {
        _uiState.update { it.copy(editorBody = body) }
    }

    fun onEditorTagInputChange(input: String) {
        if (input.contains(',') || input.contains('\n')) {
            val segments = input.split(',', '\n')
            val completedTokens = segments.dropLast(1)
            val trailingDraft = segments.lastOrNull().orEmpty()
            _uiState.update { current ->
                val mergedTags = NoteTagHelper.normalizeTags(current.editorTags + completedTokens)
                current.copy(
                    editorTags = mergedTags,
                    editorTagInput = trailingDraft
                )
            }
        } else {
            _uiState.update { it.copy(editorTagInput = input) }
        }
    }

    fun addEditorTag(rawTag: String = _uiState.value.editorTagInput) {
        val parsed = NoteTagHelper.parseTagInput(rawTag)
        if (parsed.isEmpty()) {
            _uiState.update { it.copy(editorTagInput = "") }
            return
        }
        _uiState.update { current ->
            current.copy(
                editorTags = NoteTagHelper.normalizeTags(current.editorTags + parsed),
                editorTagInput = ""
            )
        }
    }

    fun removeEditorTag(tag: String) {
        val normalizedTarget = NoteTagHelper.normalizeSingleTag(tag).lowercase()
        _uiState.update { current ->
            current.copy(
                editorTags = current.editorTags.filterNot {
                    NoteTagHelper.normalizeSingleTag(it).lowercase() == normalizedTarget
                }
            )
        }
    }

    fun saveNote() {
        val state = _uiState.value
        val body = state.editorBody.trim()
        if (body.isEmpty()) {
            _uiState.update { it.copy(feedbackMessage = "Note body cannot be empty.") }
            return
        }

        val resolvedEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(state.editorEquipmentId)
        val title = state.editorTitle.trim().ifEmpty {
            if (NoteAssociationHelper.isUnassociated(resolvedEquipmentId)) {
                "General Field Note"
            } else {
                "Field Observation"
            }
        }
        val resolvedTags = NoteTagHelper.normalizeTags(
            state.editorTags + NoteTagHelper.parseTagInput(state.editorTagInput)
        )
        val now = System.currentTimeMillis()
        val preservedCreatedAt = if (state.editingNoteId != null) {
            state.editorCreatedAt
                ?: state.notes.firstOrNull { it.id == state.editingNoteId }?.createdAt
                ?: now
        } else {
            now
        }

        viewModelScope.launch {
            val note = UserNote(
                id = state.editingNoteId ?: 0L,
                equipmentId = resolvedEquipmentId,
                title = title,
                body = body,
                tags = resolvedTags,
                createdAt = preservedCreatedAt,
                updatedAt = now
            )
            userDataRepository.saveNote(note)
            onNotesMutated?.invoke()
            _uiState.update {
                it.copy(
                    isEditorOpen = false,
                    editorEquipmentId = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
                    editorTitle = "",
                    editorBody = "",
                    editorTags = emptyList(),
                    editorTagInput = "",
                    editorCreatedAt = null,
                    editingNoteId = null,
                    feedbackMessage = "Note saved successfully."
                )
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            userDataRepository.deleteNote(noteId)
            onNotesMutated?.invoke()
            _uiState.update { current ->
                current.copy(
                    selectedNoteForDetail = current.selectedNoteForDetail?.takeIf { it.id != noteId },
                    feedbackMessage = "Note deleted."
                )
            }
        }
    }

    fun toggleBookmark(entityType: BookmarkEntityType, entityId: String, title: String) {
        val normalizedId = entityId.trim()
        if (normalizedId.isEmpty()) return
        val toggleKey = "${entityType.name}:$normalizedId"
        if (!inFlightBookmarkKeys.add(toggleKey)) return

        val resolvedTitle = title.trim().ifEmpty { normalizedId }
        viewModelScope.launch {
            try {
                userDataRepository.toggleBookmark(entityType, normalizedId, resolvedTitle)
            } finally {
                inFlightBookmarkKeys.remove(toggleKey)
            }
        }
    }

    fun removeBookmark(bookmark: UserBookmark) {
        val trimmedId = bookmark.entityId.trim()
        if (trimmedId.isEmpty()) return
        val toggleKey = "${bookmark.entityType.name}:${trimmedId.uppercase()}"
        if (!inFlightBookmarkKeys.add(toggleKey)) return

        viewModelScope.launch {
            try {
                val storedCandidates = _uiState.value.bookmarks
                    .filter {
                        it.entityType == bookmark.entityType &&
                            it.entityId.trim().equals(trimmedId, ignoreCase = true)
                    }
                    .map { it.entityId }
                    .ifEmpty { listOf(bookmark.entityId, trimmedId) }
                    .distinct()

                for (candidateId in storedCandidates) {
                    if (userDataRepository.isBookmarked(bookmark.entityType, candidateId)) {
                        userDataRepository.toggleBookmark(
                            bookmark.entityType,
                            candidateId,
                            bookmark.entityTitle
                        )
                    }
                }
            } finally {
                inFlightBookmarkKeys.remove(toggleKey)
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private data class NotesCombineSnapshot(
        val notes: List<UserNote>,
        val bookmarks: List<UserBookmark>,
        val equipment: List<Equipment>,
        val query: String,
        val filter: NoteFilter
    )

    private data class NotesResolvedSnapshot(
        val notes: List<UserNote>,
        val filteredNotes: List<UserNote>,
        val bookmarks: List<UserBookmark>,
        val equipmentOptions: List<EquipmentNoteOption>,
        val equipmentNames: Map<String, String>,
        val activeQuery: String,
        val filter: NoteFilter,
        val sortOrder: NoteSortOrder
    )
}
