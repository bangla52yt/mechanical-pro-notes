package com.researchbangla.mechanicalpronotes.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.CalculationHistoryItem
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistProgress
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistTemplate
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.SystemFamily
import com.researchbangla.mechanicalpronotes.domain.model.UserBookmark
import com.researchbangla.mechanicalpronotes.domain.model.UserNote
import com.researchbangla.mechanicalpronotes.domain.repository.BoilerRepository
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeActivitySummary(
    val totalChecklistsCount: Int = PlantChecklistCatalog.ALL_CHECKLISTS.size,
    val completedChecklistsCount: Int = 0,
    val inProgressChecklistsCount: Int = 0,
    val pendingChecklistsCount: Int = PlantChecklistCatalog.ALL_CHECKLISTS.size,
    val activeChecklistId: String? = null,
    val activeChecklistTitle: String? = null,
    val activeChecklistProgress: String? = null,
    val notesCount: Int = 0,
    val latestNoteTitle: String? = null,
    val latestNoteEquipmentId: String? = null,
    val calculationHistoryCount: Int = 0,
    val latestCalculationId: String? = null,
    val latestCalculationTitle: String? = null
) {
    val hasRecentActivity: Boolean
        get() = notesCount > 0 || calculationHistoryCount > 0 ||
            completedChecklistsCount > 0 || inProgressChecklistsCount > 0
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val systemFamilies: List<SystemFamily> = emptyList(),
    val totalSeededEquipmentCount: Int = 0,
    val fullBlueprintCount: Int = 0,
    val partialBlueprintCount: Int = 0,
    val researchPendingCount: Int = 0,
    val inventoryOnlyCount: Int = 0,
    val dedicatedBoilersCount: Int = 0,
    val featuredEquipment: List<Equipment> = emptyList(),
    val bookmarks: List<UserBookmark> = emptyList(),
    val bookmarkedEquipmentIds: Set<String> = emptySet(),
    val activitySummary: HomeActivitySummary = HomeActivitySummary(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val equipmentRepository: EquipmentRepository,
    private val boilerRepository: BoilerRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val inFlightBookmarkKeys = mutableSetOf<String>()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val staticEquipmentFlow = combine(
                    equipmentRepository.getAllSystemFamilies(),
                    equipmentRepository.getAllEquipment(),
                    boilerRepository.getBoilerEquipment()
                ) { families, allEquip, boilers ->
                    Triple(families, allEquip, boilers)
                }

                val personalActivityFlow = combine(
                    userDataRepository.getAllUserNotes(),
                    userDataRepository.getCalculationHistory(),
                    userDataRepository.getAllChecklistProgress(),
                    userDataRepository.getCustomChecklists()
                ) { notes, history, checklistProgress, customChecklists ->
                    buildActivitySummary(
                        notes = notes,
                        history = history,
                        checklistProgress = checklistProgress,
                        customChecklists = customChecklists
                    )
                }

                combine(
                    staticEquipmentFlow,
                    userDataRepository.getAllBookmarks(),
                    personalActivityFlow
                ) { (families, allEquip, boilers), bookmarks, activitySummary ->
                    val featured = allEquip
                        .filter { it.contentDepth != ContentDepth.INVENTORY_ONLY }
                        .take(5)
                        .ifEmpty { allEquip.take(5) }
                    val uniqueBookmarks = bookmarks
                        .filter { it.entityId.isNotBlank() }
                        .distinctBy { it.entityType to it.entityId.trim().uppercase() }
                    val bookmarkedEqIds = uniqueBookmarks
                        .asSequence()
                        .filter { it.entityType == BookmarkEntityType.EQUIPMENT }
                        .map { it.entityId.trim().uppercase() }
                        .toSet()

                    HomeUiState(
                        isLoading = false,
                        systemFamilies = families,
                        totalSeededEquipmentCount = allEquip.size,
                        fullBlueprintCount = allEquip.count { it.contentDepth == ContentDepth.FULL_BLUEPRINT },
                        partialBlueprintCount = allEquip.count { it.contentDepth == ContentDepth.PARTIAL_BLUEPRINT },
                        researchPendingCount = allEquip.count { it.contentDepth == ContentDepth.RESEARCH_PENDING },
                        inventoryOnlyCount = allEquip.count { it.contentDepth == ContentDepth.INVENTORY_ONLY },
                        dedicatedBoilersCount = boilers.size,
                        featuredEquipment = featured,
                        bookmarks = uniqueBookmarks,
                        bookmarkedEquipmentIds = bookmarkedEqIds,
                        activitySummary = activitySummary
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load dashboard data.")
                }
            }
        }
    }

    fun toggleEquipmentBookmark(equipment: Equipment) {
        val normalizedId = equipment.id.trim()
        if (normalizedId.isEmpty()) return
        val lockKey = "${BookmarkEntityType.EQUIPMENT.name}:${normalizedId.uppercase()}"
        if (!inFlightBookmarkKeys.add(lockKey)) return

        viewModelScope.launch {
            try {
                val matchingStoredIds = _uiState.value.bookmarks
                    .filter {
                        it.entityType == BookmarkEntityType.EQUIPMENT &&
                            it.entityId.trim().equals(normalizedId, ignoreCase = true)
                    }
                    .map { it.entityId }
                    .distinct()
                    .filter { userDataRepository.isBookmarked(BookmarkEntityType.EQUIPMENT, it) }

                if (matchingStoredIds.isNotEmpty()) {
                    for (storedId in matchingStoredIds) {
                        userDataRepository.toggleBookmark(
                            entityType = BookmarkEntityType.EQUIPMENT,
                            entityId = storedId,
                            title = equipment.name
                        )
                    }
                } else {
                    userDataRepository.toggleBookmark(
                        entityType = BookmarkEntityType.EQUIPMENT,
                        entityId = normalizedId,
                        title = equipment.name
                    )
                }
            } finally {
                inFlightBookmarkKeys.remove(lockKey)
            }
        }
    }

    fun removeBookmark(bookmark: UserBookmark) {
        val trimmedId = bookmark.entityId.trim()
        if (trimmedId.isEmpty()) return
        val lockKey = "${bookmark.entityType.name}:${trimmedId.uppercase()}"
        if (!inFlightBookmarkKeys.add(lockKey)) return

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
                            entityType = bookmark.entityType,
                            entityId = candidateId,
                            title = bookmark.entityTitle
                        )
                    }
                }
            } finally {
                inFlightBookmarkKeys.remove(lockKey)
            }
        }
    }

    private fun buildActivitySummary(
        notes: List<UserNote>,
        history: List<CalculationHistoryItem>,
        checklistProgress: List<ChecklistProgress>,
        customChecklists: List<CustomChecklistTemplate>
    ): HomeActivitySummary {
        val completedByChecklist = checklistProgress
            .filter { it.isCompleted }
            .groupBy { it.checklistId.trim().uppercase() }
            .mapValues { (_, entries) -> entries.map { it.itemId }.toSet() }

        val packagedRefs = PlantChecklistCatalog.ALL_CHECKLISTS.map { chk ->
            ChecklistSummaryCandidate(
                id = chk.id,
                title = chk.title,
                itemIds = chk.items.map { it.id }
            )
        }
        val customRefs = customChecklists
            .filterNot { PlantChecklistCatalog.isPackagedChecklistId(it.id) }
            .map { tpl ->
                ChecklistSummaryCandidate(
                    id = tpl.id,
                    title = tpl.title,
                    itemIds = tpl.items.map { it.id }
                )
            }
        val allChecklists = packagedRefs + customRefs

        var completedCount = 0
        var inProgressCount = 0
        var inProgressHighlight: Triple<String, String, String>? = null
        var firstPendingHighlight: Triple<String, String, String>? = null

        for (chk in allChecklists) {
            val doneIds = completedByChecklist[chk.id.trim().uppercase()].orEmpty()
            val done = chk.itemIds.count { it in doneIds }
            val total = chk.itemIds.size
            when {
                total > 0 && done >= total -> completedCount += 1
                done in 1 until total -> {
                    inProgressCount += 1
                    if (inProgressHighlight == null) {
                        inProgressHighlight = Triple(chk.id, chk.title, "$done / $total checks")
                    }
                }
                else -> {
                    if (firstPendingHighlight == null && total > 0) {
                        firstPendingHighlight = Triple(chk.id, chk.title, "0 / $total checks")
                    }
                }
            }
        }

        val activeHighlight = inProgressHighlight ?: firstPendingHighlight
        val pendingCount = (allChecklists.size - completedCount).coerceAtLeast(0)
        val latestNote = notes.maxByOrNull { it.updatedAt }
        val latestHistory = history.maxByOrNull { it.timestamp }

        return HomeActivitySummary(
            totalChecklistsCount = allChecklists.size,
            completedChecklistsCount = completedCount,
            inProgressChecklistsCount = inProgressCount,
            pendingChecklistsCount = pendingCount,
            activeChecklistId = activeHighlight?.first,
            activeChecklistTitle = activeHighlight?.second,
            activeChecklistProgress = activeHighlight?.third,
            notesCount = notes.size,
            latestNoteTitle = latestNote?.title?.trim()?.ifEmpty { "Field Note" },
            latestNoteEquipmentId = latestNote?.equipmentId,
            calculationHistoryCount = history.size,
            latestCalculationId = latestHistory?.calculationId,
            latestCalculationTitle = latestHistory?.calculationTitle
        )
    }

    private data class ChecklistSummaryCandidate(
        val id: String,
        val title: String,
        val itemIds: List<Int>
    )
}

