package com.researchbangla.mechanicalpronotes.presentation.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

object CalculationCatalogFilter {
    fun filter(
        calculations: List<CalculationDefinition>,
        query: String,
        selectedCategory: String?
    ): List<CalculationDefinition> {
        val deduplicated = calculations.distinctBy { it.id.trim().uppercase() }
        val categoryFiltered = if (selectedCategory.isNullOrBlank() || selectedCategory.equals("All", ignoreCase = true)) {
            deduplicated
        } else {
            val targetCategory = selectedCategory.trim()
            deduplicated.filter { it.category.trim().equals(targetCategory, ignoreCase = true) }
        }

        val normalizedQuery = Normalizer.normalize(query.trim(), Normalizer.Form.NFC).lowercase()
        if (normalizedQuery.isEmpty()) return categoryFiltered

        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return categoryFiltered

        return categoryFiltered.mapIndexedNotNull { index, calc ->
            val variableMetadata = calc.variables.joinToString(" ") { v ->
                "${v.symbol} ${v.name} ${v.unit}"
            }
            val fullBlob = Normalizer.normalize(
                listOf(
                    calc.id,
                    calc.code,
                    calc.title,
                    calc.bengaliTitle,
                    calc.category,
                    calc.engineeringReference,
                    calc.assumptions,
                    calc.limitations,
                    calc.formulaLatex,
                    calc.status.name,
                    variableMetadata
                ).joinToString(" "),
                Normalizer.Form.NFC
            ).lowercase()

            if (!tokens.all { fullBlob.contains(it) }) {
                return@mapIndexedNotNull null
            }

            val primaryBlob = Normalizer.normalize(
                "${calc.id} ${calc.code} ${calc.title} ${calc.bengaliTitle} ${calc.category}",
                Normalizer.Form.NFC
            ).lowercase()

            val rank = when {
                calc.id.equals(normalizedQuery, ignoreCase = true) ||
                    calc.code.equals(normalizedQuery, ignoreCase = true) -> 0
                primaryBlob.contains(normalizedQuery) -> 1
                else -> 2
            }
            Triple(rank, index, calc)
        }
            .sortedWith(compareBy<Triple<Int, Int, CalculationDefinition>> { it.first }.thenBy { it.second })
            .map { it.third }
    }
}

data class CalculationListUiState(
    val isLoading: Boolean = true,
    val calculations: List<CalculationDefinition> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val history: List<CalculationHistoryItem> = emptyList(),
    val pendingHistoryFocusId: Long? = null,
    val errorMessage: String? = null
) {
    val filteredCalculations: List<CalculationDefinition>
        get() = CalculationCatalogFilter.filter(
            calculations = calculations,
            query = searchQuery,
            selectedCategory = selectedCategory
        )
}

data class CalculationRunnerUiState(
    val isLoading: Boolean = true,
    val definition: CalculationDefinition? = null,
    val inputValues: Map<String, String> = emptyMap(),
    val validationErrors: Map<String, String> = emptyMap(),
    val result: CalculationExecutionResult? = null,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val isExecuting: Boolean = false,
    val historySaved: Boolean = false,
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null
)

class CalculationViewModel(
    private val calculationRepository: CalculationRepository,
    private val userDataRepository: UserDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val _listState = MutableStateFlow(CalculationListUiState())
    val listState: StateFlow<CalculationListUiState> = _listState.asStateFlow()

    private val _runnerState = MutableStateFlow(CalculationRunnerUiState())
    val runnerState: StateFlow<CalculationRunnerUiState> = _runnerState.asStateFlow()

    private var bookmarkedCalcIds: Set<String> = emptySet()
    private val inFlightBookmarkIds = mutableSetOf<String>()

    init {
        loadCalculations()
        observeCalculationHistory()
        observeCalculationBookmarks()
        observeUnitPreferences()
    }

    private fun observeUnitPreferences() {
        val prefsRepo = userPreferencesRepository ?: return
        viewModelScope.launch {
            prefsRepo.userPreferences.collect { prefs ->
                _listState.update { it.copy(unitSystem = prefs.unitSystem) }
                _runnerState.update { it.copy(unitSystem = prefs.unitSystem) }
            }
        }
    }

    fun loadCalculations() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            calculationRepository.getAllCalculations().collect { list ->
                _listState.update {
                    it.copy(
                        isLoading = false,
                        calculations = list.distinctBy { def -> def.id.trim().uppercase() }
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _listState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String?) {
        val normalized = category?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
        _listState.update { it.copy(selectedCategory = normalized) }
    }

    fun clearFilters() {
        _listState.update { it.copy(searchQuery = "", selectedCategory = null) }
    }

    private fun observeCalculationHistory() {
        viewModelScope.launch {
            userDataRepository.getCalculationHistory().collect { items ->
                _listState.update { it.copy(history = items) }
            }
        }
    }

    fun focusHistoryRecord(historyId: Long) {
        _listState.update { it.copy(pendingHistoryFocusId = historyId) }
    }

    fun clearPendingHistoryFocus() {
        _listState.update { it.copy(pendingHistoryFocusId = null) }
    }

    private fun observeCalculationBookmarks() {
        viewModelScope.launch {
            userDataRepository.getAllBookmarks().collect { bookmarks ->
                val calcIds = bookmarks
                    .asSequence()
                    .filter { it.entityType == BookmarkEntityType.CALCULATION }
                    .map { it.entityId.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                bookmarkedCalcIds = calcIds
                _runnerState.update { current ->
                    val currentId = current.definition?.id?.trim()
                    current.copy(
                        isBookmarked = currentId != null && calcIds.any { it.equals(currentId, ignoreCase = true) }
                    )
                }
            }
        }
    }

    fun loadCalculationRunner(calculationId: String) {
        viewModelScope.launch {
            _runnerState.update {
                it.copy(
                    isLoading = true,
                    result = null,
                    validationErrors = emptyMap(),
                    isBookmarked = false
                )
            }
            val def = calculationRepository.getCalculationById(calculationId)
            if (def != null) {
                // Initialize default inputs
                val initialInputs = mutableMapOf<String, String>()
                def.variables.forEach { v ->
                    if (v.defaultValue != null) {
                        initialInputs[v.symbol] = v.defaultValue.toString()
                    } else {
                        initialInputs[v.symbol] = ""
                    }
                }
                val normalizedId = def.id.trim()
                _runnerState.update {
                    it.copy(
                        isLoading = false,
                        definition = def,
                        inputValues = initialInputs,
                        isBookmarked = bookmarkedCalcIds.any { id -> id.equals(normalizedId, ignoreCase = true) },
                        errorMessage = null
                    )
                }
            } else {
                _runnerState.update {
                    it.copy(
                        isLoading = false,
                        definition = null,
                        isBookmarked = false,
                        errorMessage = "Calculation definition '$calculationId' not found."
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val def = _runnerState.value.definition ?: return
        val normalizedId = def.id.trim()
        if (normalizedId.isEmpty() || !inFlightBookmarkIds.add(normalizedId)) return

        viewModelScope.launch {
            try {
                val nowBookmarked = userDataRepository.toggleBookmark(
                    entityType = BookmarkEntityType.CALCULATION,
                    entityId = normalizedId,
                    title = def.title
                )
                bookmarkedCalcIds = if (nowBookmarked) {
                    bookmarkedCalcIds + normalizedId
                } else {
                    bookmarkedCalcIds.filterNot { it.equals(normalizedId, ignoreCase = true) }.toSet()
                }
                _runnerState.update { current ->
                    if (current.definition?.id?.trim().equals(normalizedId, ignoreCase = true)) {
                        current.copy(isBookmarked = nowBookmarked)
                    } else {
                        current
                    }
                }
            } finally {
                inFlightBookmarkIds.remove(normalizedId)
            }
        }
    }

    fun onInputChange(symbol: String, value: String) {
        _runnerState.update {
            val updated = it.inputValues.toMutableMap()
            updated[symbol] = value
            val updatedErrors = it.validationErrors.toMutableMap()
            updatedErrors.remove(symbol)
            it.copy(
                inputValues = updated,
                validationErrors = updatedErrors,
                result = null,
                historySaved = false,
                errorMessage = null
            )
        }
    }

    fun executeCalculation() {
        val currentState = _runnerState.value
        val def = currentState.definition ?: return

        if (def.status != CalculationStatus.IMPLEMENTED) {
            _runnerState.update {
                it.copy(
                    errorMessage = "This calculation is flagged as ${def.status}. Execution is disabled until verified engineering data is integrated."
                )
            }
            return
        }

        // Validate inputs
        val parsedInputs = mutableMapOf<String, Double>()
        val errors = mutableMapOf<String, String>()

        for (v in def.variables) {
            val str = currentState.inputValues[v.symbol]?.trim().orEmpty()
            if (str.isEmpty()) {
                if (v.isRequired) {
                    errors[v.symbol] = "${v.name} is required."
                }
            } else {
                val num = str.toDoubleOrNull()
                if (num == null || !num.isFinite()) {
                    errors[v.symbol] = "Enter a valid finite decimal number."
                } else {
                    if (v.minBound != null && num < v.minBound) {
                        errors[v.symbol] = "Must be ≥ ${v.minBound} ${v.unit}"
                    } else if (v.maxBound != null && num > v.maxBound) {
                        errors[v.symbol] = "Must be ≤ ${v.maxBound} ${v.unit}"
                    } else {
                        parsedInputs[v.symbol] = num
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            _runnerState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _runnerState.update { it.copy(isExecuting = true, errorMessage = null) }
            try {
                val res = calculationRepository.executeCalculation(def.id, parsedInputs)
                _runnerState.update {
                    it.copy(
                        isExecuting = false,
                        result = res,
                        historySaved = false
                    )
                }
            } catch (e: Exception) {
                _runnerState.update {
                    it.copy(
                        isExecuting = false,
                        errorMessage = e.message ?: "Execution error occurred."
                    )
                }
            }
        }
    }

    fun saveToHistory() {
        val currentState = _runnerState.value
        val res = currentState.result ?: return
        val def = currentState.definition ?: return

        viewModelScope.launch {
            val historyItem = CalculationHistoryItem(
                calculationId = def.id,
                calculationTitle = def.title,
                timestamp = System.currentTimeMillis(),
                inputValues = currentState.inputValues.mapNotNull { (k, v) -> v.toDoubleOrNull()?.let { k to it } }.toMap(),
                outputValues = mapOf(res.primaryOutputName to res.primaryOutputValue) + res.secondaryOutputs,
                userRemark = res.notesAndRecommendations
            )
            userDataRepository.saveCalculationHistory(historyItem)
            _runnerState.update { it.copy(historySaved = true) }
        }
    }
}
