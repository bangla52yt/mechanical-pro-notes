package com.researchbangla.mechanicalpronotes.presentation.checklists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistExecutionMetadata
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistProgress
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistShift
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistItem
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistTemplate
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistItem(
    val id: Int,
    val text: String,
    val category: String,
    val isCrucial: Boolean = false,
    val isCompleted: Boolean = false
)

data class PlantChecklist(
    val id: String,
    val title: String,
    val description: String,
    val standardReference: String,
    val items: List<ChecklistItem>,
    val defaultEquipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
    val relatedFamilyId: String? = null,
    val isCustom: Boolean = false,
    val updatedAt: Long = 0L
)

enum class ChecklistCompletionStatus(val label: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class ChecklistProgressSummary(
    val completedCount: Int,
    val totalCount: Int,
    val mandatoryCompletedCount: Int,
    val mandatoryTotalCount: Int,
    val status: ChecklistCompletionStatus
)

data class ChecklistEquipmentOption(
    val id: String,
    val name: String,
    val bengaliName: String,
    val familyId: String
)

enum class ChecklistSourceFilter(val label: String) {
    ALL("All"),
    PACKAGED("Packaged"),
    CUSTOM("Custom")
}

data class CustomChecklistDraftItem(
    val id: Int,
    val text: String,
    val category: String = "Inspection",
    val isCrucial: Boolean = false
)

object ChecklistWorkflowHelper {
    val PRESET_CATEGORIES: List<String> = listOf(
        "Inspection",
        "Safety",
        "Pre-Start",
        "Drives",
        "Bearings",
        "Steam",
        "Water Quality",
        "Lubrication",
        "Electrical"
    )

    fun isPackagedChecklist(checklistId: String): Boolean =
        PlantChecklistCatalog.isPackagedChecklistId(checklistId)

    fun summarizeProgress(
        checklist: PlantChecklist,
        completedIds: Set<Int>
    ): ChecklistProgressSummary {
        val total = checklist.items.size
        val completed = checklist.items.count { it.id in completedIds }
        val mandatoryTotal = checklist.items.count { it.isCrucial }
        val mandatoryCompleted = checklist.items.count { it.isCrucial && it.id in completedIds }
        val status = when {
            total == 0 || completed == 0 -> ChecklistCompletionStatus.NOT_STARTED
            completed >= total -> ChecklistCompletionStatus.COMPLETED
            else -> ChecklistCompletionStatus.IN_PROGRESS
        }
        return ChecklistProgressSummary(
            completedCount = completed,
            totalCount = total,
            mandatoryCompletedCount = mandatoryCompleted,
            mandatoryTotalCount = mandatoryTotal,
            status = status
        )
    }

    fun effectiveEquipmentId(
        checklist: PlantChecklist,
        executionMetadata: Map<String, ChecklistExecutionMetadata>
    ): String {
        val overrideId = executionMetadata[checklist.id]?.equipmentId
            ?: executionMetadata.entries.firstOrNull { it.key.equals(checklist.id, ignoreCase = true) }?.value?.equipmentId
        val raw = overrideId ?: checklist.defaultEquipmentId
        return NoteAssociationHelper.normalizeEquipmentIdForStorage(raw)
    }

    fun matchesEquipment(
        checklist: PlantChecklist,
        executionMetadata: Map<String, ChecklistExecutionMetadata>,
        equipmentId: String,
        familyId: String?
    ): Boolean {
        val normalizedTarget = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)
        if (NoteAssociationHelper.isUnassociated(normalizedTarget)) {
            return NoteAssociationHelper.isUnassociated(effectiveEquipmentId(checklist, executionMetadata))
        }
        val effective = effectiveEquipmentId(checklist, executionMetadata)
        if (effective.equals(normalizedTarget, ignoreCase = true)) {
            return true
        }
        if (NoteAssociationHelper.isUnassociated(effective)) {
            return false
        }
        return !familyId.isNullOrBlank() && checklist.relatedFamilyId.equals(familyId, ignoreCase = true)
    }

    fun filterRelevantChecklistsForEquipment(
        checklists: List<PlantChecklist>,
        executionMetadata: Map<String, ChecklistExecutionMetadata>,
        equipment: Equipment
    ): List<PlantChecklist> {
        return checklists
            .filter { chk ->
                val effective = effectiveEquipmentId(chk, executionMetadata)
                effective.equals(equipment.id, ignoreCase = true) ||
                    chk.relatedFamilyId.equals(equipment.familyId, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<PlantChecklist> {
                    effectiveEquipmentId(it, executionMetadata).equals(equipment.id, ignoreCase = true)
                }.thenByDescending { it.isCustom }
            )
    }

    fun resolveEquipmentDisplay(
        equipmentId: String?,
        equipmentById: Map<String, ChecklistEquipmentOption>,
        catalogLoaded: Boolean
    ): String {
        val normalized = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)
        if (NoteAssociationHelper.isUnassociated(normalized)) {
            return "General / Unassociated"
        }
        val found = equipmentById[normalized]
            ?: equipmentById.entries.firstOrNull { it.key.equals(normalized, ignoreCase = true) }?.value
        return when {
            found != null -> "${found.id} — ${found.name}"
            catalogLoaded -> "$normalized (Preserved ID — not in catalog)"
            else -> normalized
        }
    }
}

data class ChecklistUiState(
    val checklists: List<PlantChecklist> = emptyList(),
    val selectedChecklistId: String = PlantChecklistCatalog.ID_BOILER_DAILY,
    val completedByChecklist: Map<String, Set<Int>> = emptyMap(),
    val progressByChecklist: Map<String, Map<Int, ChecklistProgress>> = emptyMap(),
    val completedItemIds: Set<Int> = emptySet(),
    val bookmarkedChecklistIds: Set<String> = emptySet(),
    val isSelectedChecklistBookmarked: Boolean = false,
    val customChecklistIds: Set<String> = emptySet(),
    val executionMetadataByChecklist: Map<String, ChecklistExecutionMetadata> = emptyMap(),
    val availableEquipment: List<ChecklistEquipmentOption> = emptyList(),
    val equipmentById: Map<String, ChecklistEquipmentOption> = emptyMap(),
    val equipmentCatalogLoaded: Boolean = false,
    val sourceFilter: ChecklistSourceFilter = ChecklistSourceFilter.ALL,
    val equipmentFilterId: String? = null,
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val isFeedbackError: Boolean = false,
    val isCustomEditorOpen: Boolean = false,
    val editingCustomChecklistId: String? = null,
    val editorTitle: String = "",
    val editorDescription: String = "",
    val editorEquipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
    val editorItems: List<CustomChecklistDraftItem> = emptyList(),
    val draftItemText: String = "",
    val draftItemCategory: String = "Inspection",
    val draftItemCrucial: Boolean = false,
    val editingDraftItemIndex: Int? = null,
    val editorValidationError: String? = null,
    val draftItemError: String? = null
) {
    val visibleChecklists: List<PlantChecklist>
        get() {
            val sourceFiltered = when (sourceFilter) {
                ChecklistSourceFilter.ALL -> checklists
                ChecklistSourceFilter.PACKAGED -> checklists.filterNot { it.isCustom }
                ChecklistSourceFilter.CUSTOM -> checklists.filter { it.isCustom }
            }
            val targetEqId = equipmentFilterId?.takeIf { it.isNotBlank() } ?: return sourceFiltered
            val targetFamilyId = equipmentById[targetEqId]?.familyId
                ?: equipmentById.entries.firstOrNull { it.key.equals(targetEqId, ignoreCase = true) }?.value?.familyId
            return sourceFiltered.filter { chk ->
                ChecklistWorkflowHelper.matchesEquipment(
                    checklist = chk,
                    executionMetadata = executionMetadataByChecklist,
                    equipmentId = targetEqId,
                    familyId = targetFamilyId
                )
            }
        }

    fun effectiveEquipmentId(checklist: PlantChecklist): String =
        ChecklistWorkflowHelper.effectiveEquipmentId(checklist, executionMetadataByChecklist)

    fun executionMetadataFor(checklistId: String): ChecklistExecutionMetadata? =
        executionMetadataByChecklist[checklistId]
            ?: executionMetadataByChecklist.entries.firstOrNull { it.key.equals(checklistId, ignoreCase = true) }?.value

    fun progressSummaryFor(checklist: PlantChecklist): ChecklistProgressSummary =
        ChecklistWorkflowHelper.summarizeProgress(
            checklist = checklist,
            completedIds = completedByChecklist[checklist.id] ?: emptySet()
        )
}

class ChecklistViewModel(
    private val userDataRepository: UserDataRepository,
    private val equipmentRepository: EquipmentRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState(isLoading = true))
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    private val inFlightBookmarkIds = mutableSetOf<String>()
    private val inFlightItemKeys = mutableSetOf<String>()
    private var packagedChecklists: List<PlantChecklist> = emptyList()
    private var latestCustomTemplates: List<CustomChecklistTemplate> = emptyList()

    init {
        loadDefaultChecklists()
        observeCustomChecklists()
        observeChecklistProgress()
        observeChecklistExecutionMetadata()
        observeChecklistBookmarks()
        observeEquipmentCatalog()
    }

    private fun observeEquipmentCatalog() {
        val repo = equipmentRepository
        if (repo == null) {
            _uiState.update { it.copy(equipmentCatalogLoaded = false) }
            return
        }
        viewModelScope.launch {
            repo.getAllEquipment().collect { equipmentList ->
                val options = equipmentList
                    .sortedBy { it.id }
                    .map { eq ->
                        ChecklistEquipmentOption(
                            id = eq.id,
                            name = eq.name,
                            bengaliName = eq.bengaliName,
                            familyId = eq.familyId
                        )
                    }
                _uiState.update { current ->
                    current.copy(
                        availableEquipment = options,
                        equipmentById = options.associateBy { it.id },
                        equipmentCatalogLoaded = true
                    )
                }
            }
        }
    }

    private fun observeCustomChecklists() {
        viewModelScope.launch {
            userDataRepository.getCustomChecklists().collect { templates ->
                latestCustomTemplates = templates
                publishMergedChecklists()
            }
        }
    }

    private fun observeChecklistProgress() {
        viewModelScope.launch {
            userDataRepository.getAllChecklistProgress().collect { entries ->
                val progressMap = entries
                    .groupBy { it.checklistId }
                    .mapValues { (_, list) -> list.associateBy { it.itemId } }
                val byChecklist = progressMap
                    .mapValues { (_, itemsById) ->
                        itemsById.values.filter { it.isCompleted }.map { it.itemId }.toSet()
                    }
                    .filterValues { it.isNotEmpty() }
                _uiState.update { current ->
                    current.copy(
                        progressByChecklist = progressMap,
                        completedByChecklist = byChecklist,
                        completedItemIds = byChecklist[current.selectedChecklistId] ?: emptySet()
                    )
                }
            }
        }
    }

    private fun observeChecklistExecutionMetadata() {
        viewModelScope.launch {
            userDataRepository.getAllChecklistExecutionMetadata().collect { metadataMap ->
                _uiState.update { current ->
                    current.copy(executionMetadataByChecklist = metadataMap)
                }
            }
        }
    }

    private fun observeChecklistBookmarks() {
        viewModelScope.launch {
            userDataRepository.getAllBookmarks().collect { bookmarks ->
                val checklistIds = bookmarks
                    .asSequence()
                    .filter { it.entityType == BookmarkEntityType.CHECKLIST }
                    .map { it.entityId.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                _uiState.update { current ->
                    current.copy(
                        bookmarkedChecklistIds = checklistIds,
                        isSelectedChecklistBookmarked = checklistIds.any {
                            it.equals(current.selectedChecklistId, ignoreCase = true)
                        }
                    )
                }
            }
        }
    }

    /**
     * Authoritative checklists backed by [PlantChecklistCatalog] (zero duplicated item definitions).
     * Statutory baselines preserved:
     * - CHK-BLR-DAILY: The Boilers Act, 2022 (Sec 18–19) / OCIB Operational Practice
     * - CHK-BLR-WEEKLY: Boilers Act 2022 / ASME Section I
     * - CHK-WASH-PREV: Wash Plant Maintenance SOP
     * - CHK-HYDRO-SAFE: Bangladesh Labour Rules 2015, Schedule IV
     */
    private fun loadDefaultChecklists() {
        val defaultChecklists = PlantChecklistCatalog.ALL_CHECKLISTS
            .map { catalog ->
                PlantChecklist(
                    id = catalog.id,
                    title = catalog.title,
                    description = catalog.description,
                    standardReference = catalog.standardReference,
                    defaultEquipmentId = catalog.defaultEquipmentId,
                    relatedFamilyId = catalog.relatedFamilyId,
                    isCustom = false,
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

        check(defaultChecklists.any { it.id == "CHK-BLR-DAILY" })
        check(defaultChecklists.any { it.id == "CHK-WASH-PREV" })
        check(defaultChecklists.any { it.id == "CHK-HYDRO-SAFE" })

        packagedChecklists = defaultChecklists
        publishMergedChecklists()
    }

    private fun publishMergedChecklists() {
        val customMapped = latestCustomTemplates
            .filterNot { PlantChecklistCatalog.isPackagedChecklistId(it.id) }
            .map { template ->
                PlantChecklist(
                    id = template.id,
                    title = template.title,
                    description = template.description.ifBlank { "User-created custom maintenance checklist" },
                    standardReference = "Custom Plant Checklist (Non-statutory user template)",
                    defaultEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(template.equipmentId),
                    relatedFamilyId = _uiState.value.equipmentById[template.equipmentId]?.familyId,
                    isCustom = true,
                    updatedAt = template.updatedAt,
                    items = template.items.map { item ->
                        ChecklistItem(
                            id = item.id,
                            text = item.text,
                            category = item.category.ifBlank { "Inspection" },
                            isCrucial = item.isCrucial
                        )
                    }
                )
            }
        val merged = packagedChecklists + customMapped
        val customIds = customMapped.map { it.id }.toSet()

        _uiState.update { current ->
            val resolvedSelectedId = if (merged.any { it.id == current.selectedChecklistId }) {
                current.selectedChecklistId
            } else {
                merged.firstOrNull()?.id ?: PlantChecklistCatalog.ID_BOILER_DAILY
            }
            current.copy(
                checklists = merged,
                customChecklistIds = customIds,
                selectedChecklistId = resolvedSelectedId,
                completedItemIds = current.completedByChecklist[resolvedSelectedId] ?: emptySet(),
                isSelectedChecklistBookmarked = current.bookmarkedChecklistIds.any {
                    it.equals(resolvedSelectedId, ignoreCase = true)
                },
                isLoading = false
            )
        }
    }

    fun selectChecklist(id: String) {
        _uiState.update { current ->
            val matched = current.checklists.firstOrNull { it.id.equals(id, ignoreCase = true) }
            val resolvedId = matched?.id ?: id
            val clearEqFilter = current.equipmentFilterId != null && current.visibleChecklists.none { it.id == resolvedId }
            val clearSourceFilter = current.sourceFilter != ChecklistSourceFilter.ALL &&
                ((current.sourceFilter == ChecklistSourceFilter.PACKAGED && matched?.isCustom == true) ||
                    (current.sourceFilter == ChecklistSourceFilter.CUSTOM && matched?.isCustom == false))
            current.copy(
                selectedChecklistId = resolvedId,
                completedItemIds = current.completedByChecklist[resolvedId] ?: emptySet(),
                isSelectedChecklistBookmarked = current.bookmarkedChecklistIds.any {
                    it.equals(resolvedId, ignoreCase = true)
                },
                equipmentFilterId = if (clearEqFilter) null else current.equipmentFilterId,
                sourceFilter = if (clearSourceFilter) ChecklistSourceFilter.ALL else current.sourceFilter
            )
        }
    }

    fun setSourceFilter(filter: ChecklistSourceFilter) {
        _uiState.update { current ->
            val nextState = current.copy(sourceFilter = filter)
            val visible = nextState.visibleChecklists
            val nextSelected = if (visible.isNotEmpty() && visible.none { it.id == current.selectedChecklistId }) {
                visible.first().id
            } else {
                current.selectedChecklistId
            }
            nextState.copy(
                selectedChecklistId = nextSelected,
                completedItemIds = current.completedByChecklist[nextSelected] ?: emptySet(),
                isSelectedChecklistBookmarked = current.bookmarkedChecklistIds.any {
                    it.equals(nextSelected, ignoreCase = true)
                }
            )
        }
    }

    fun filterByEquipment(equipmentId: String?) {
        val normalized = equipmentId?.trim()?.takeIf { it.isNotEmpty() }
        _uiState.update { current ->
            val nextState = current.copy(equipmentFilterId = normalized)
            val visible = nextState.visibleChecklists
            val nextSelected = if (visible.isNotEmpty() && visible.none { it.id == current.selectedChecklistId }) {
                visible.first().id
            } else {
                current.selectedChecklistId
            }
            nextState.copy(
                selectedChecklistId = nextSelected,
                completedItemIds = current.completedByChecklist[nextSelected] ?: emptySet(),
                isSelectedChecklistBookmarked = current.bookmarkedChecklistIds.any {
                    it.equals(nextSelected, ignoreCase = true)
                }
            )
        }
    }

    fun clearEquipmentFilter() {
        filterByEquipment(null)
    }

    fun openForEquipment(equipmentId: String, preferredChecklistId: String? = null) {
        val normalized = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)
        _uiState.update { current ->
            val nextState = current.copy(
                sourceFilter = ChecklistSourceFilter.ALL,
                equipmentFilterId = if (NoteAssociationHelper.isUnassociated(normalized)) null else normalized
            )
            val visible = nextState.visibleChecklists
            val preferredMatch = preferredChecklistId?.let { target ->
                visible.firstOrNull { it.id.equals(target, ignoreCase = true) }
                    ?: current.checklists.firstOrNull { it.id.equals(target, ignoreCase = true) }
            }
            val exactMatch = preferredMatch ?: visible.firstOrNull {
                current.effectiveEquipmentId(it).equals(normalized, ignoreCase = true)
            } ?: visible.firstOrNull()
            val resolvedId = exactMatch?.id ?: current.selectedChecklistId
            nextState.copy(
                selectedChecklistId = resolvedId,
                completedItemIds = current.completedByChecklist[resolvedId] ?: emptySet(),
                isSelectedChecklistBookmarked = current.bookmarkedChecklistIds.any {
                    it.equals(resolvedId, ignoreCase = true)
                }
            )
        }
    }

    fun toggleChecklistBookmark(checklistId: String = _uiState.value.selectedChecklistId) {
        val target = _uiState.value.checklists.find { it.id == checklistId } ?: return
        val normalizedId = target.id.trim()
        if (normalizedId.isEmpty() || !inFlightBookmarkIds.add(normalizedId)) return

        viewModelScope.launch {
            try {
                userDataRepository.toggleBookmark(
                    entityType = BookmarkEntityType.CHECKLIST,
                    entityId = normalizedId,
                    title = target.title
                )
            } finally {
                inFlightBookmarkIds.remove(normalizedId)
            }
        }
    }

    fun toggleItem(itemId: Int) {
        val currentState = _uiState.value
        val checklistId = currentState.selectedChecklistId
        val activeChecklist = currentState.checklists.firstOrNull { it.id == checklistId } ?: return
        if (activeChecklist.items.none { it.id == itemId }) return

        val key = "$checklistId:$itemId"
        if (!inFlightItemKeys.add(key)) return

        val existingProgress = currentState.progressByChecklist[checklistId]?.get(itemId)
        val currentlyCompleted = existingProgress?.isCompleted
            ?: (currentState.completedByChecklist[checklistId]?.contains(itemId) == true)
        val newCompleted = !currentlyCompleted

        viewModelScope.launch {
            try {
                userDataRepository.setChecklistItemCompleted(
                    checklistId = checklistId,
                    itemId = itemId,
                    isCompleted = newCompleted,
                    remarks = existingProgress?.remarks
                )
            } finally {
                inFlightItemKeys.remove(key)
            }
        }
    }

    fun updateItemRemark(itemId: Int, remarks: String) {
        val currentState = _uiState.value
        val checklistId = currentState.selectedChecklistId
        val activeChecklist = currentState.checklists.firstOrNull { it.id == checklistId } ?: return
        if (activeChecklist.items.none { it.id == itemId }) return

        val key = "$checklistId:$itemId"
        if (!inFlightItemKeys.add(key)) return

        val existingProgress = currentState.progressByChecklist[checklistId]?.get(itemId)
        val isCompleted = existingProgress?.isCompleted
            ?: (currentState.completedByChecklist[checklistId]?.contains(itemId) == true)
        val normalizedRemarks = remarks.trim().ifEmpty { null }

        viewModelScope.launch {
            try {
                userDataRepository.setChecklistItemCompleted(
                    checklistId = checklistId,
                    itemId = itemId,
                    isCompleted = isCompleted,
                    remarks = normalizedRemarks
                )
            } finally {
                inFlightItemKeys.remove(key)
            }
        }
    }

    fun resetCurrentChecklist() {
        val checklistId = _uiState.value.selectedChecklistId
        viewModelScope.launch {
            userDataRepository.resetChecklist(checklistId)
            _uiState.update {
                it.copy(
                    feedbackMessage = "Checklist execution progress reset.",
                    isFeedbackError = false
                )
            }
        }
    }

    fun updateChecklistEquipment(
        checklistId: String = _uiState.value.selectedChecklistId,
        equipmentId: String
    ) {
        val target = _uiState.value.checklists.firstOrNull { it.id.equals(checklistId, ignoreCase = true) } ?: return
        val existingMeta = _uiState.value.executionMetadataFor(target.id)
        val normalizedEquipment = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId)

        viewModelScope.launch {
            val updatedMeta = ChecklistExecutionMetadata(
                checklistId = target.id,
                equipmentId = normalizedEquipment,
                operatorName = existingMeta?.operatorName.orEmpty(),
                shift = existingMeta?.shift ?: ChecklistShift.UNSPECIFIED,
                updatedAt = System.currentTimeMillis()
            )
            userDataRepository.saveChecklistExecutionMetadata(updatedMeta)
        }
    }

    fun updateExecutionOperatorAndShift(
        checklistId: String = _uiState.value.selectedChecklistId,
        operatorName: String,
        shift: ChecklistShift
    ) {
        val target = _uiState.value.checklists.firstOrNull { it.id.equals(checklistId, ignoreCase = true) } ?: return
        val currentEffectiveEq = _uiState.value.effectiveEquipmentId(target)

        viewModelScope.launch {
            val updatedMeta = ChecklistExecutionMetadata(
                checklistId = target.id,
                equipmentId = currentEffectiveEq,
                operatorName = operatorName.trim(),
                shift = shift,
                updatedAt = System.currentTimeMillis()
            )
            userDataRepository.saveChecklistExecutionMetadata(updatedMeta)
        }
    }

    fun openCreateCustomChecklist(preselectedEquipmentId: String? = null) {
        val initialEq = NoteAssociationHelper.normalizeEquipmentIdForStorage(
            preselectedEquipmentId ?: _uiState.value.equipmentFilterId
        )
        _uiState.update {
            it.copy(
                isCustomEditorOpen = true,
                editingCustomChecklistId = null,
                editorTitle = "",
                editorDescription = "",
                editorEquipmentId = initialEq,
                editorItems = emptyList(),
                draftItemText = "",
                draftItemCategory = "Inspection",
                draftItemCrucial = false,
                editingDraftItemIndex = null,
                editorValidationError = null,
                draftItemError = null
            )
        }
    }

    fun openEditCustomChecklist(checklistId: String) {
        if (PlantChecklistCatalog.isPackagedChecklistId(checklistId)) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "Packaged default checklists are read-only and cannot be modified.",
                    isFeedbackError = true
                )
            }
            return
        }
        val target = _uiState.value.checklists.firstOrNull {
            it.id.equals(checklistId, ignoreCase = true) && it.isCustom
        } ?: return
        val template = latestCustomTemplates.firstOrNull { it.id.equals(target.id, ignoreCase = true) }

        _uiState.update {
            it.copy(
                isCustomEditorOpen = true,
                editingCustomChecklistId = target.id,
                editorTitle = template?.title ?: target.title,
                editorDescription = template?.description ?: target.description,
                editorEquipmentId = template?.equipmentId ?: target.defaultEquipmentId,
                editorItems = target.items.map { item ->
                    CustomChecklistDraftItem(
                        id = item.id,
                        text = item.text,
                        category = item.category,
                        isCrucial = item.isCrucial
                    )
                },
                draftItemText = "",
                draftItemCategory = "Inspection",
                draftItemCrucial = false,
                editingDraftItemIndex = null,
                editorValidationError = null,
                draftItemError = null
            )
        }
    }

    fun closeCustomChecklistEditor() {
        _uiState.update {
            it.copy(
                isCustomEditorOpen = false,
                editingCustomChecklistId = null,
                editorValidationError = null,
                draftItemError = null,
                editingDraftItemIndex = null
            )
        }
    }

    fun updateEditorTitle(title: String) {
        _uiState.update { it.copy(editorTitle = title, editorValidationError = null) }
    }

    fun updateEditorDescription(description: String) {
        _uiState.update { it.copy(editorDescription = description) }
    }

    fun updateEditorEquipmentId(equipmentId: String) {
        _uiState.update {
            it.copy(editorEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId))
        }
    }

    fun updateDraftItemText(text: String) {
        _uiState.update { it.copy(draftItemText = text, draftItemError = null) }
    }

    fun updateDraftItemCategory(category: String) {
        _uiState.update { it.copy(draftItemCategory = category) }
    }

    fun toggleDraftItemCrucial() {
        _uiState.update { it.copy(draftItemCrucial = !it.draftItemCrucial) }
    }

    fun addOrSaveDraftItem(): Boolean {
        val current = _uiState.value
        val trimmedText = current.draftItemText.trim()
        if (trimmedText.isEmpty()) {
            _uiState.update { it.copy(draftItemError = "Checklist item description cannot be blank.") }
            return false
        }
        val normalizedCategory = current.draftItemCategory.trim().ifEmpty { "Inspection" }
        val editIndex = current.editingDraftItemIndex
        val updatedItems = current.editorItems.toMutableList()
        if (editIndex != null && editIndex in updatedItems.indices) {
            val existing = updatedItems[editIndex]
            updatedItems[editIndex] = existing.copy(
                text = trimmedText,
                category = normalizedCategory,
                isCrucial = current.draftItemCrucial
            )
        } else {
            val nextId = ((updatedItems.maxOfOrNull { it.id } ?: 1000) + 1).coerceAtLeast(1001)
            updatedItems.add(
                CustomChecklistDraftItem(
                    id = nextId,
                    text = trimmedText,
                    category = normalizedCategory,
                    isCrucial = current.draftItemCrucial
                )
            )
        }
        _uiState.update {
            it.copy(
                editorItems = updatedItems,
                draftItemText = "",
                draftItemCategory = "Inspection",
                draftItemCrucial = false,
                editingDraftItemIndex = null,
                draftItemError = null,
                editorValidationError = null
            )
        }
        return true
    }

    fun editDraftItem(index: Int) {
        val item = _uiState.value.editorItems.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                draftItemText = item.text,
                draftItemCategory = item.category,
                draftItemCrucial = item.isCrucial,
                editingDraftItemIndex = index,
                draftItemError = null
            )
        }
    }

    fun cancelDraftItemEdit() {
        _uiState.update {
            it.copy(
                draftItemText = "",
                draftItemCategory = "Inspection",
                draftItemCrucial = false,
                editingDraftItemIndex = null,
                draftItemError = null
            )
        }
    }

    fun removeDraftItem(index: Int) {
        _uiState.update { current ->
            if (index !in current.editorItems.indices) return@update current
            val updated = current.editorItems.toMutableList().apply { removeAt(index) }
            val nextEditingIndex = when {
                current.editingDraftItemIndex == null -> null
                current.editingDraftItemIndex == index -> null
                current.editingDraftItemIndex > index -> current.editingDraftItemIndex - 1
                else -> current.editingDraftItemIndex
            }
            current.copy(
                editorItems = updated,
                editingDraftItemIndex = nextEditingIndex,
                draftItemText = if (current.editingDraftItemIndex == index) "" else current.draftItemText
            )
        }
    }

    fun saveCustomChecklist(): Boolean {
        val current = _uiState.value
        val trimmedTitle = current.editorTitle.trim()
        if (trimmedTitle.isEmpty()) {
            _uiState.update { it.copy(editorValidationError = "Checklist title is required.") }
            return false
        }
        val editId = current.editingCustomChecklistId?.trim()
        if (editId != null && PlantChecklistCatalog.isPackagedChecklistId(editId)) {
            _uiState.update {
                it.copy(editorValidationError = "Packaged default checklists cannot be overwritten.")
            }
            return false
        }
        val validItems = current.editorItems
            .map { it.copy(text = it.text.trim(), category = it.category.trim().ifEmpty { "Inspection" }) }
            .filter { it.text.isNotEmpty() }
        if (validItems.isEmpty()) {
            _uiState.update {
                it.copy(editorValidationError = "Add at least one non-empty checklist item before saving.")
            }
            return false
        }

        val now = System.currentTimeMillis()
        val templateId = editId?.takeIf { it.isNotEmpty() } ?: "CHK-CUSTOM-$now"
        val normalizedEq = NoteAssociationHelper.normalizeEquipmentIdForStorage(current.editorEquipmentId)
        val template = CustomChecklistTemplate(
            id = templateId,
            title = trimmedTitle,
            description = current.editorDescription.trim(),
            equipmentId = normalizedEq,
            items = validItems.map { item ->
                CustomChecklistItem(
                    id = item.id,
                    text = item.text,
                    category = item.category,
                    isCrucial = item.isCrucial
                )
            },
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            val saved = userDataRepository.saveCustomChecklist(template)
            val clearFilter = _uiState.value.equipmentFilterId != null &&
                !_uiState.value.equipmentFilterId.equals(normalizedEq, ignoreCase = true)
            val resetSourceFilter = _uiState.value.sourceFilter == ChecklistSourceFilter.PACKAGED
            _uiState.update {
                it.copy(
                    isCustomEditorOpen = false,
                    editingCustomChecklistId = null,
                    selectedChecklistId = saved.id,
                    completedItemIds = it.completedByChecklist[saved.id] ?: emptySet(),
                    isSelectedChecklistBookmarked = it.bookmarkedChecklistIds.any { bm ->
                        bm.equals(saved.id, ignoreCase = true)
                    },
                    equipmentFilterId = if (clearFilter) null else it.equipmentFilterId,
                    sourceFilter = if (resetSourceFilter) ChecklistSourceFilter.ALL else it.sourceFilter,
                    editorValidationError = null,
                    draftItemError = null,
                    feedbackMessage = "Saved custom checklist \"${saved.title}\".",
                    isFeedbackError = false
                )
            }
        }
        return true
    }

    fun deleteCustomChecklist(checklistId: String) {
        if (PlantChecklistCatalog.isPackagedChecklistId(checklistId)) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "Packaged default checklists cannot be deleted.",
                    isFeedbackError = true
                )
            }
            return
        }
        viewModelScope.launch {
            val deleted = userDataRepository.deleteCustomChecklist(checklistId)
            _uiState.update {
                if (deleted) {
                    it.copy(
                        feedbackMessage = "Custom checklist deleted.",
                        isFeedbackError = false
                    )
                } else {
                    it.copy(
                        feedbackMessage = "Custom checklist could not be deleted.",
                        isFeedbackError = true
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, isFeedbackError = false) }
    }
}
