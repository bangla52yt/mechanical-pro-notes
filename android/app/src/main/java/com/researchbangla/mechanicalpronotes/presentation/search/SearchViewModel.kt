package com.researchbangla.mechanicalpronotes.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.catalog.CatalogSearchHit
import com.researchbangla.mechanicalpronotes.domain.catalog.SearchResultType
import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilter
import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilterCriteria
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationHistorySearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationSearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.SearchRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserNoteSearchHit
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SearchScope(val label: String) {
    ALL("All"),
    EQUIPMENT("Equipment"),
    CALCULATIONS("Calculations"),
    NOTES("My Notes"),
    HISTORY("Calc History"),
    CHECKLISTS("Checklists"),
    REFERENCES("References"),
    FUNDAMENTALS("Fundamentals")
}

data class SearchUiState(
    val query: String = "",
    val results: List<Equipment> = emptyList(),
    val catalogHits: List<CatalogSearchHit> = emptyList(),
    val calculationHits: List<CalculationSearchHit> = emptyList(),
    val userNoteHits: List<UserNoteSearchHit> = emptyList(),
    val calculationHistoryHits: List<CalculationHistorySearchHit> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val selectedScope: SearchScope = SearchScope.ALL,
    val selectedFamilyFilter: String? = null,
    val selectedDepthFilters: Set<ContentDepth> = emptySet(),
    val errorMessage: String? = null
) {
    val filteredEquipment: List<Equipment>
        get() = if (selectedScope != SearchScope.ALL && selectedScope != SearchScope.EQUIPMENT) {
            emptyList()
        } else {
            EquipmentFilter.apply(
                items = results,
                criteria = EquipmentFilterCriteria(
                    familyId = selectedFamilyFilter,
                    selectedDepths = selectedDepthFilters,
                    query = "" // Query already applied by SearchRepository FTS/LIKE
                )
            )
        }

    val filteredCatalogHits: List<CatalogSearchHit>
        get() = when (selectedScope) {
            SearchScope.ALL -> catalogHits
            SearchScope.CHECKLISTS -> catalogHits.filter { it.type == SearchResultType.CHECKLIST }
            SearchScope.REFERENCES -> catalogHits.filter { it.type == SearchResultType.REFERENCE }
            SearchScope.FUNDAMENTALS -> catalogHits.filter { it.type == SearchResultType.FUNDAMENTAL }
            SearchScope.EQUIPMENT,
            SearchScope.CALCULATIONS,
            SearchScope.NOTES,
            SearchScope.HISTORY -> emptyList()
        }

    val filteredCalculationHits: List<CalculationSearchHit>
        get() = if (selectedScope == SearchScope.ALL || selectedScope == SearchScope.CALCULATIONS) {
            calculationHits
        } else {
            emptyList()
        }

    val filteredUserNoteHits: List<UserNoteSearchHit>
        get() = if (selectedScope == SearchScope.ALL || selectedScope == SearchScope.NOTES) {
            userNoteHits
        } else {
            emptyList()
        }

    val filteredCalculationHistoryHits: List<CalculationHistorySearchHit>
        get() = if (selectedScope == SearchScope.ALL || selectedScope == SearchScope.HISTORY) {
            calculationHistoryHits
        } else {
            emptyList()
        }
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQueryFlow
                .map { it.trim() }
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { trimmedQuery ->
                    if (trimmedQuery.length >= 2) {
                        performSearch(trimmedQuery)
                    } else {
                        _uiState.update {
                            it.copy(
                                results = emptyList(),
                                catalogHits = emptyList(),
                                calculationHits = emptyList(),
                                userNoteHits = emptyList(),
                                calculationHistoryHits = emptyList(),
                                isLoading = false,
                                hasSearched = false,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchQueryFlow.value = newQuery
    }

    fun selectScope(scope: SearchScope) {
        _uiState.update { it.copy(selectedScope = scope) }
    }

    fun filterByFamily(familyId: String?) {
        _uiState.update { it.copy(selectedFamilyFilter = familyId) }
    }

    fun toggleDepthFilter(depth: ContentDepth) {
        _uiState.update { current ->
            val updated = if (depth in current.selectedDepthFilters) {
                current.selectedDepthFilters - depth
            } else {
                current.selectedDepthFilters + depth
            }
            current.copy(selectedDepthFilters = updated)
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedScope = SearchScope.ALL,
                selectedFamilyFilter = null,
                selectedDepthFilters = emptySet()
            )
        }
    }

    fun clearSearch() {
        _uiState.update { SearchUiState() }
        searchQueryFlow.value = ""
    }

    fun refreshSearchIfActive() {
        val trimmed = _uiState.value.query.trim()
        if (_uiState.value.hasSearched && trimmed.length >= 2) {
            viewModelScope.launch {
                performSearch(trimmed)
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val unified = searchRepository.searchKnowledgeBase(query.trim())
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = unified.equipment,
                    catalogHits = unified.catalogHits,
                    calculationHits = unified.calculations,
                    userNoteHits = unified.userNotes,
                    calculationHistoryHits = unified.calculationHistory,
                    hasSearched = true
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasSearched = true,
                    errorMessage = e.message ?: "Search execution failed."
                )
            }
        }
    }
}
