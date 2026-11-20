package com.researchbangla.mechanicalpronotes.presentation.units

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.unit.EngineeringUnit
import com.researchbangla.mechanicalpronotes.domain.unit.UnitCategory
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConversionResult
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConverterEngine
import com.researchbangla.mechanicalpronotes.domain.unit.UnitPreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UnitConverterUiState(
    val selectedCategory: UnitCategory = UnitCategory.PRESSURE,
    val availableUnits: List<EngineeringUnit> = UnitConverterEngine.getUnits(UnitCategory.PRESSURE),
    val fromUnitCode: String = "bar_g",
    val toUnitCode: String = "psi_g",
    val inputText: String = "10",
    val preferredUnitSystem: UnitSystem = UnitSystem.METRIC,
    val conversionResult: UnitConversionResult = UnitConverterEngine.convert(10.0, "bar_g", "psi_g"),
    val equivalentConversions: List<Pair<EngineeringUnit, String>> = emptyList()
)

data class UnitConverterSnapshot(
    val categoryName: String,
    val fromUnitCode: String,
    val toUnitCode: String,
    val inputText: String,
    val unitSystemName: String
)

/**
 * Pure-Kotlin state holder for [UnitConverterScreen] (keeps 8 ViewModel count invariant intact
 * while preserving clean unidirectional data flow via StateFlow and configuration-change persistence).
 */
class UnitConverterStateHolder(
    initialSnapshot: UnitConverterSnapshot? = null
) {

    private val _uiState = MutableStateFlow(
        initialSnapshot?.let(::buildStateFromSnapshot) ?: buildInitialState()
    )
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    fun snapshot(): UnitConverterSnapshot {
        val current = _uiState.value
        return UnitConverterSnapshot(
            categoryName = current.selectedCategory.name,
            fromUnitCode = current.fromUnitCode,
            toUnitCode = current.toUnitCode,
            inputText = current.inputText,
            unitSystemName = current.preferredUnitSystem.name
        )
    }

    fun restore(snapshot: UnitConverterSnapshot) {
        _uiState.value = buildStateFromSnapshot(snapshot)
    }

    private fun buildStateFromSnapshot(snapshot: UnitConverterSnapshot): UnitConverterUiState {
        val category = runCatching { UnitCategory.valueOf(snapshot.categoryName) }
            .getOrDefault(UnitCategory.PRESSURE)
        val unitSystem = runCatching { UnitSystem.valueOf(snapshot.unitSystemName) }
            .getOrDefault(UnitSystem.METRIC)
        val units = UnitConverterEngine.getUnits(category)
        val (fallbackFrom, fallbackTo) = UnitPreferenceHelper.preferredCategoryUnits(category, unitSystem)
        val resolvedFrom = units.firstOrNull { it.code == snapshot.fromUnitCode }?.code ?: fallbackFrom
        val resolvedTo = units.firstOrNull { it.code == snapshot.toUnitCode }?.code ?: fallbackTo
        return recalculate(
            UnitConverterUiState(
                selectedCategory = category,
                availableUnits = units,
                fromUnitCode = resolvedFrom,
                toUnitCode = resolvedTo,
                inputText = snapshot.inputText,
                preferredUnitSystem = unitSystem
            )
        )
    }

    private fun buildInitialState(): UnitConverterUiState {
        val category = UnitCategory.PRESSURE
        val units = UnitConverterEngine.getUnits(category)
        val (from, to) = UnitPreferenceHelper.preferredCategoryUnits(category, UnitSystem.METRIC)
        val text = "10"
        return recalculate(
            UnitConverterUiState(
                selectedCategory = category,
                availableUnits = units,
                fromUnitCode = from,
                toUnitCode = to,
                inputText = text,
                preferredUnitSystem = UnitSystem.METRIC
            )
        )
    }

    companion object {
        val Saver: Saver<UnitConverterStateHolder, Any> = listSaver(
            save = { holder ->
                val snap = holder.snapshot()
                listOf(
                    snap.categoryName,
                    snap.fromUnitCode,
                    snap.toUnitCode,
                    snap.inputText,
                    snap.unitSystemName
                )
            },
            restore = { saved ->
                val list = saved.filterIsInstance<String>()
                if (list.size == 5) {
                    UnitConverterStateHolder(
                        UnitConverterSnapshot(
                            categoryName = list[0],
                            fromUnitCode = list[1],
                            toUnitCode = list[2],
                            inputText = list[3],
                            unitSystemName = list[4]
                        )
                    )
                } else {
                    UnitConverterStateHolder()
                }
            }
        )
    }

    fun applyUnitSystemPreference(unitSystem: UnitSystem) {
        _uiState.update { current ->
            if (current.preferredUnitSystem == unitSystem) {
                current
            } else {
                val (preferredFrom, preferredTo) = UnitPreferenceHelper.preferredCategoryUnits(
                    category = current.selectedCategory,
                    unitSystem = unitSystem
                )
                recalculate(
                    current.copy(
                        preferredUnitSystem = unitSystem,
                        fromUnitCode = preferredFrom,
                        toUnitCode = preferredTo
                    )
                )
            }
        }
    }

    fun selectCategory(category: UnitCategory) {
        val units = UnitConverterEngine.getUnits(category)
        if (units.isEmpty()) return
        val (defaultFrom, defaultTo) = UnitPreferenceHelper.preferredCategoryUnits(
            category = category,
            unitSystem = _uiState.value.preferredUnitSystem
        )
        val defaultInput = when (category) {
            UnitCategory.TEMPERATURE -> "100"
            UnitCategory.PRESSURE -> "10"
            else -> "1"
        }
        _uiState.update { current ->
            recalculate(
                current.copy(
                    selectedCategory = category,
                    availableUnits = units,
                    fromUnitCode = defaultFrom,
                    toUnitCode = defaultTo,
                    inputText = defaultInput
                )
            )
        }
    }

    fun selectFromUnit(unitCode: String) {
        _uiState.update { current ->
            recalculate(current.copy(fromUnitCode = unitCode))
        }
    }

    fun selectToUnit(unitCode: String) {
        _uiState.update { current ->
            recalculate(current.copy(toUnitCode = unitCode))
        }
    }

    fun onInputChange(newText: String) {
        _uiState.update { current ->
            recalculate(current.copy(inputText = newText))
        }
    }

    fun swapUnits() {
        _uiState.update { current ->
            recalculate(
                current.copy(
                    fromUnitCode = current.toUnitCode,
                    toUnitCode = current.fromUnitCode
                )
            )
        }
    }

    private fun recalculate(state: UnitConverterUiState): UnitConverterUiState {
        val result = UnitConverterEngine.convertFromText(
            rawInput = state.inputText,
            fromCode = state.fromUnitCode,
            toCode = state.toUnitCode
        )
        val equivalents = if (result is UnitConversionResult.Success) {
            state.availableUnits.mapNotNull { target ->
                when (val sub = UnitConverterEngine.convert(result.inputValue, state.fromUnitCode, target.code)) {
                    is UnitConversionResult.Success -> target to sub.formattedResult
                    is UnitConversionResult.Error -> null
                }
            }
        } else {
            emptyList()
        }
        return state.copy(
            conversionResult = result,
            equivalentConversions = equivalents
        )
    }
}
