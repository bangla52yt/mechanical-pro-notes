package com.researchbangla.mechanicalpronotes.presentation.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilter
import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilterCriteria
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EquipmentListUiState(
    val isLoading: Boolean = true,
    val selectedFamilyId: String? = null,
    val selectedDepths: Set<ContentDepth> = emptySet(),
    val searchQuery: String = "",
    val systemFamilies: List<SystemFamily> = emptyList(),
    val allEquipment: List<Equipment> = emptyList(),
    val equipmentList: List<Equipment> = emptyList(),
    val depthCounts: Map<ContentDepth, Int> = emptyMap(),
    val errorMessage: String? = null
)

data class EquipmentDetailUiState(
    val isLoading: Boolean = true,
    val equipment: Equipment? = null,
    val selectedTab: Int = 0,
    val isBookmarked: Boolean = false,
    val userNotes: List<UserNote> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class EquipmentViewModel(
    private val equipmentRepository: EquipmentRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _selectedFamilyId = MutableStateFlow<String?>(null)
    private val _selectedDepths = MutableStateFlow<Set<ContentDepth>>(emptySet())
    private val _searchQuery = MutableStateFlow("")
    private val _listState = MutableStateFlow(EquipmentListUiState())
    val listState: StateFlow<EquipmentListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(EquipmentDetailUiState())
    val detailState: StateFlow<EquipmentDetailUiState> = _detailState.asStateFlow()

    private val bookmarkedEquipmentIds = MutableStateFlow<Set<String>>(emptySet())
    private val inFlightBookmarkIds = mutableSetOf<String>()
    private var detailJob: Job? = null
    private var activeDetailRequestId: String? = null

    init {
        viewModelScope.launch {
            equipmentRepository.getAllSystemFamilies().collect { families ->
                _listState.update { it.copy(systemFamilies = families) }
            }
        }

        viewModelScope.launch {
            userDataRepository.getAllBookmarks()
                .map { bookmarks ->
                    bookmarks.asSequence()
                        .filter { it.entityType == BookmarkEntityType.EQUIPMENT }
                        .map { it.entityId.trim().uppercase() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                }
                .distinctUntilChanged()
                .collect { ids ->
                    bookmarkedEquipmentIds.value = ids
                }
        }

        viewModelScope.launch {
            combine(
                equipmentRepository.getAllEquipment(),
                _selectedFamilyId,
                _selectedDepths,
                _searchQuery
            ) { allItems, familyId, depths, query ->
                val criteria = EquipmentFilterCriteria(
                    familyId = familyId,
                    selectedDepths = depths,
                    query = query
                )
                val filtered = EquipmentFilter.apply(allItems, criteria)
                val counts = EquipmentFilter.countByDepth(allItems, familyId = familyId, query = query)
                EquipmentListUiState(
                    isLoading = false,
                    selectedFamilyId = familyId,
                    selectedDepths = depths,
                    searchQuery = query,
                    systemFamilies = _listState.value.systemFamilies,
                    allEquipment = allItems,
                    equipmentList = filtered,
                    depthCounts = counts,
                    errorMessage = null
                )
            }.collect { nextState ->
                _listState.value = nextState
            }
        }
    }

    fun selectFamily(familyId: String?) {
        _selectedFamilyId.value = familyId
    }

    fun toggleDepthFilter(depth: ContentDepth) {
        _selectedDepths.update { current ->
            if (depth in current) current - depth else current + depth
        }
    }

    fun clearDepthFilters() {
        _selectedDepths.value = emptySet()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearAllFilters() {
        _selectedFamilyId.value = null
        _selectedDepths.value = emptySet()
        _searchQuery.value = ""
    }

    fun loadEquipmentDetail(equipmentId: String, forceReload: Boolean = false) {
        val targetKey = equipmentId.trim().uppercase()
        val current = _detailState.value
        if (!forceReload &&
            current.errorMessage == null &&
            (current.equipment?.id?.trim()?.uppercase() == targetKey ||
                (current.isLoading && activeDetailRequestId == targetKey && detailJob?.isActive == true))
        ) {
            return
        }
        activeDetailRequestId = targetKey
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailState.update {
                it.copy(
                    isLoading = true,
                    selectedTab = 0,
                    isBookmarked = false,
                    errorMessage = null
                )
            }
            try {
                val equip = equipmentRepository.getEquipmentById(equipmentId)
                if (equip == null) {
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            equipment = null,
                            isBookmarked = false,
                            userNotes = emptyList(),
                            errorMessage = "Equipment '$equipmentId' not found in local database."
                        )
                    }
                    return@launch
                }

                val equipKey = equip.id.trim().uppercase()
                combine(
                    userDataRepository.getNotesForEquipment(equip.id),
                    bookmarkedEquipmentIds
                        .map { ids -> equipKey in ids }
                        .distinctUntilChanged()
                ) { notes, isBookmarked ->
                    notes to isBookmarked
                }.collect { (notes, isBookmarked) ->
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            equipment = equip,
                            isBookmarked = isBookmarked,
                            userNotes = notes,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _detailState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load equipment."
                    )
                }
            }
        }
    }

    fun toggleBookmark(equipment: Equipment) {
        val normalizedId = equipment.id.trim()
        val key = normalizedId.uppercase()
        if (normalizedId.isEmpty() || !inFlightBookmarkIds.add(key)) return

        viewModelScope.launch {
            try {
                val newStatus = userDataRepository.toggleBookmark(
                    BookmarkEntityType.EQUIPMENT,
                    normalizedId,
                    equipment.name
                )
                bookmarkedEquipmentIds.update { current ->
                    if (newStatus) current + key else current - key
                }
                _detailState.update { current ->
                    if (current.equipment?.id?.trim().equals(normalizedId, ignoreCase = true)) {
                        current.copy(isBookmarked = newStatus)
                    } else {
                        current
                    }
                }
            } finally {
                inFlightBookmarkIds.remove(key)
            }
        }
    }

    fun selectDetailTab(tabIndex: Int) {
        _detailState.update { it.copy(selectedTab = tabIndex) }
    }
}
