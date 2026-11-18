package com.researchbangla.mechanicalpronotes.presentation.boiler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.repository.BoilerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BoilerUiState(
    val isLoading: Boolean = true,
    val selectedTab: BoilerTab = BoilerTab.OVERVIEW,
    val boilerEquipment: List<Equipment> = emptyList(),
    val gasBoilers: List<Equipment> = emptyList(),
    val jhutBoilers: List<Equipment> = emptyList(),
    val errorMessage: String? = null
)

enum class BoilerTab(val title: String) {
    OVERVIEW("Overview & Specs"),
    GAS_BOILERS("Gas Boilers (EQ-G01)"),
    JHUT_BOILERS("Jhut Boilers (EQ-G02)"),
    STATUTORY_RULES("Act 2022 & Rules 2025")
}

object BoilerCalculationCatalog {
    val OVERVIEW_CALC_IDS: List<String> = (1..14).map { "CALC-${it.toString().padStart(2, '0')}" }
    val GAS_BOILER_CALC_IDS: List<String> = listOf(
        "CALC-01", "CALC-02", "CALC-03", "CALC-04", "CALC-05",
        "CALC-08", "CALC-09", "CALC-10", "CALC-11", "CALC-14"
    )
    val JHUT_BOILER_CALC_IDS: List<String> = listOf(
        "CALC-01", "CALC-02", "CALC-03", "CALC-04", "CALC-05",
        "CALC-09", "CALC-12", "CALC-13", "CALC-14"
    )
    val STATUTORY_CALC_IDS: List<String> = listOf("CALC-09")

    fun calculationIdsForTab(tab: BoilerTab): List<String> = when (tab) {
        BoilerTab.OVERVIEW -> OVERVIEW_CALC_IDS
        BoilerTab.GAS_BOILERS -> GAS_BOILER_CALC_IDS
        BoilerTab.JHUT_BOILERS -> JHUT_BOILER_CALC_IDS
        BoilerTab.STATUTORY_RULES -> STATUTORY_CALC_IDS
    }

    fun getCalculationsForTab(
        tab: BoilerTab,
        availableCalculations: List<CalculationDefinition> = CalculationSpecifications.ALL_CALCULATIONS
    ): List<CalculationDefinition> {
        val source = availableCalculations.ifEmpty { CalculationSpecifications.ALL_CALCULATIONS }
        return calculationIdsForTab(tab).mapNotNull { targetId ->
            source.firstOrNull { it.id.equals(targetId, ignoreCase = true) }
                ?: CalculationSpecifications.getCalculationById(targetId)
        }.distinctBy { it.id.trim().uppercase() }
    }
}

sealed interface BoilerBookmarkTarget {
    data class Tab(val tab: BoilerTab) : BoilerBookmarkTarget
    data class Equipment(val equipmentId: String) : BoilerBookmarkTarget
}

fun boilerSectionBookmarkId(tab: BoilerTab): String = "BOILER:${tab.name}"

fun resolveBoilerBookmarkTarget(entityId: String): BoilerBookmarkTarget {
    val trimmed = entityId.trim()
    if (trimmed.isEmpty()) {
        return BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW)
    }

    if (trimmed.startsWith("BOILER:", ignoreCase = true)) {
        val rawTabName = trimmed.substringAfter(':').trim()
        val matched = BoilerTab.entries.firstOrNull { it.name.equals(rawTabName, ignoreCase = true) }
        return BoilerBookmarkTarget.Tab(matched ?: BoilerTab.OVERVIEW)
    }

    val bareTabMatch = BoilerTab.entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
    if (bareTabMatch != null) {
        return BoilerBookmarkTarget.Tab(bareTabMatch)
    }

    if (trimmed.startsWith("EQ-", ignoreCase = true) && trimmed.length > 3) {
        return BoilerBookmarkTarget.Equipment(trimmed.uppercase())
    }

    return BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW)
}

class BoilerViewModel(
    private val boilerRepository: BoilerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoilerUiState())
    val uiState: StateFlow<BoilerUiState> = _uiState.asStateFlow()

    init {
        loadBoilerData()
    }

    fun selectTab(tab: BoilerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun loadBoilerData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                combine(
                    boilerRepository.getBoilerEquipment(),
                    boilerRepository.getGasBoilers(),
                    boilerRepository.getJhutAndSolidFuelBoilers()
                ) { allBoilers, gas, jhut ->
                    Triple(allBoilers, gas, jhut)
                }.collect { (allBoilers, gas, jhut) ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            boilerEquipment = allBoilers,
                            gasBoilers = gas,
                            jhutBoilers = jhut
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load boiler data.")
                }
            }
        }
    }
}
