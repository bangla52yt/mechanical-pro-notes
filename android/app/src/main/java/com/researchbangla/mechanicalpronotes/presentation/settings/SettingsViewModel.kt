package com.researchbangla.mechanicalpronotes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.researchbangla.mechanicalpronotes.domain.model.AppThemeMode
import com.researchbangla.mechanicalpronotes.domain.model.BackupImportResult
import com.researchbangla.mechanicalpronotes.domain.model.ClearPersonalDataResult
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ClearDataConfirmationStep {
    NONE,
    STEP_1_WARNING,
    STEP_2_FINAL
}

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isLargePlantFloorFont: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val isExporting: Boolean = false,
    val exportedJson: String? = null,
    val importMessage: String? = null,
    val isImportError: Boolean = false,
    val totalNotesCount: Int = 0,
    val totalBookmarksCount: Int = 0,
    val totalHistoryCount: Int = 0,
    val totalChecklistEntriesCount: Int = 0,
    val totalCustomChecklistsCount: Int = 0,
    val clearConfirmationStep: ClearDataConfirmationStep = ClearDataConfirmationStep.NONE,
    val isClearingPersonalData: Boolean = false,
    val personalDataMessage: String? = null,
    val isPersonalDataError: Boolean = false
)

class SettingsViewModel(
    private val userDataRepository: UserDataRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val onPersonalDataCleared: () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var isClearingInFlight: Boolean = false

    init {
        loadPreferences()
        loadDataCounters()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        themeMode = prefs.themeMode,
                        isLargePlantFloorFont = prefs.isLargePlantFloorFont,
                        unitSystem = prefs.unitSystem
                    )
                }
            }
        }
    }

    private fun loadDataCounters() {
        viewModelScope.launch {
            userDataRepository.getAllUserNotes().collect { notes ->
                _uiState.update { it.copy(totalNotesCount = notes.size) }
            }
        }
        viewModelScope.launch {
            userDataRepository.getAllBookmarks().collect { bms ->
                _uiState.update { it.copy(totalBookmarksCount = bms.size) }
            }
        }
        viewModelScope.launch {
            userDataRepository.getCalculationHistory().collect { hist ->
                _uiState.update { it.copy(totalHistoryCount = hist.size) }
            }
        }
        viewModelScope.launch {
            userDataRepository.getAllChecklistProgress().collect { entries ->
                _uiState.update { it.copy(totalChecklistEntriesCount = entries.size) }
            }
        }
        viewModelScope.launch {
            userDataRepository.getCustomChecklists().collect { customTemplates ->
                _uiState.update { it.copy(totalCustomChecklistsCount = customTemplates.size) }
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleLargePlantFloorFont(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setLargePlantFloorFont(enabled)
        }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            userPreferencesRepository.setUnitSystem(unitSystem)
        }
    }

    fun requestClearPersonalData() {
        if (isClearingInFlight || _uiState.value.isClearingPersonalData) return
        _uiState.update {
            it.copy(
                clearConfirmationStep = ClearDataConfirmationStep.STEP_1_WARNING,
                personalDataMessage = null,
                isPersonalDataError = false
            )
        }
    }

    fun proceedToFinalClearConfirmation() {
        if (isClearingInFlight || _uiState.value.isClearingPersonalData) return
        _uiState.update { current ->
            if (current.clearConfirmationStep == ClearDataConfirmationStep.STEP_1_WARNING) {
                current.copy(clearConfirmationStep = ClearDataConfirmationStep.STEP_2_FINAL)
            } else {
                current
            }
        }
    }

    fun cancelClearPersonalData() {
        if (isClearingInFlight || _uiState.value.isClearingPersonalData) return
        _uiState.update { it.copy(clearConfirmationStep = ClearDataConfirmationStep.NONE) }
    }

    fun confirmClearAllPersonalData() {
        if (isClearingInFlight || _uiState.value.isClearingPersonalData) return
        if (_uiState.value.clearConfirmationStep != ClearDataConfirmationStep.STEP_2_FINAL) return

        isClearingInFlight = true
        _uiState.update {
            it.copy(
                isClearingPersonalData = true,
                personalDataMessage = null,
                isPersonalDataError = false
            )
        }

        viewModelScope.launch {
            try {
                when (val result = userDataRepository.clearAllPersonalData()) {
                    is ClearPersonalDataResult.Success -> {
                        val message = if (result.totalDeleted > 0) {
                            "Cleared personal data: ${result.notesDeleted} notes, " +
                                "${result.bookmarksDeleted} bookmarks, ${result.historyDeleted} calculation history entries, " +
                                "${result.checklistEntriesDeleted} checklist progress records, " +
                                "${result.customChecklistsDeleted} custom checklists. Packaged catalogs and unit preference remain intact."
                        } else {
                            "No saved personal data records needed clearing. Packaged catalogs and unit preference remain intact."
                        }
                        _uiState.update {
                            it.copy(
                                isClearingPersonalData = false,
                                clearConfirmationStep = ClearDataConfirmationStep.NONE,
                                personalDataMessage = message,
                                isPersonalDataError = false
                            )
                        }
                        onPersonalDataCleared()
                    }
                    is ClearPersonalDataResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isClearingPersonalData = false,
                                clearConfirmationStep = ClearDataConfirmationStep.NONE,
                                personalDataMessage = result.message,
                                isPersonalDataError = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearingPersonalData = false,
                        clearConfirmationStep = ClearDataConfirmationStep.NONE,
                        personalDataMessage = e.message ?: "Unexpected error occurred while clearing personal data. Existing records were not modified.",
                        isPersonalDataError = true
                    )
                }
            } finally {
                isClearingInFlight = false
            }
        }
    }

    fun exportPersonalData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, importMessage = null, isImportError = false) }
            try {
                val json = userDataRepository.exportUserPersonalDataJson()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportedJson = json
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        importMessage = "Export failed due to a local storage error.",
                        isImportError = true
                    )
                }
            }
        }
    }

    fun importPersonalData(jsonString: String, overwrite: Boolean = false) {
        viewModelScope.launch {
            try {
                when (val result = userDataRepository.importUserPersonalDataJson(jsonString, overwrite = overwrite)) {
                    is BackupImportResult.Success -> {
                        _uiState.update {
                            it.copy(
                                importMessage = "Backup verified (SHA-256) and restored: " +
                                    "${result.notesImported} notes, ${result.bookmarksImported} bookmarks, " +
                                    "${result.historyImported} calculations, ${result.checklistsImported} checklist entries, " +
                                    "${result.customChecklistsImported} custom checklists, " +
                                    "${result.executionMetadataImported} shift metadata records.",
                                isImportError = false
                            )
                        }
                    }
                    is BackupImportResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                importMessage = result.message,
                                isImportError = true
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        importMessage = "Unexpected error occurred while restoring backup.",
                        isImportError = true
                    )
                }
            }
        }
    }

    fun setImportMessage(message: String?, isError: Boolean = false) {
        _uiState.update { it.copy(importMessage = message, isImportError = isError) }
    }

    fun clearExportedJson() {
        _uiState.update { it.copy(exportedJson = null, importMessage = null, isImportError = false) }
    }
}
