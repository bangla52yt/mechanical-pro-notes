package com.researchbangla.mechanicalpronotes.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.researchbangla.mechanicalpronotes.di.AppContainer
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerViewModel
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationViewModel
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistViewModel
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentViewModel
import com.researchbangla.mechanicalpronotes.presentation.home.HomeViewModel
import com.researchbangla.mechanicalpronotes.presentation.notes.NotesViewModel
import com.researchbangla.mechanicalpronotes.presentation.search.SearchViewModel
import com.researchbangla.mechanicalpronotes.presentation.settings.SettingsViewModel

/**
 * Lifecycle-aware ViewModelProvider.Factory bridging manual DI [AppContainer]
 * with Compose `viewModel(factory = ...)` so all 8 ViewModels survive configuration changes.
 */
class AppViewModelFactory(
    private val appContainer: AppContainer,
    private val onNotesMutated: () -> Unit = {},
    private val onPersonalDataCleared: () -> Unit = {}
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                equipmentRepository = appContainer.equipmentRepository,
                boilerRepository = appContainer.boilerRepository,
                userDataRepository = appContainer.userDataRepository
            ) as T

            modelClass.isAssignableFrom(EquipmentViewModel::class.java) -> EquipmentViewModel(
                equipmentRepository = appContainer.equipmentRepository,
                userDataRepository = appContainer.userDataRepository
            ) as T

            modelClass.isAssignableFrom(BoilerViewModel::class.java) -> BoilerViewModel(
                boilerRepository = appContainer.boilerRepository
            ) as T

            modelClass.isAssignableFrom(CalculationViewModel::class.java) -> CalculationViewModel(
                calculationRepository = appContainer.calculationRepository,
                userDataRepository = appContainer.userDataRepository,
                userPreferencesRepository = appContainer.userPreferencesRepository
            ) as T

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(
                searchRepository = appContainer.searchRepository
            ) as T

            modelClass.isAssignableFrom(NotesViewModel::class.java) -> NotesViewModel(
                userDataRepository = appContainer.userDataRepository,
                equipmentRepository = appContainer.equipmentRepository,
                onNotesMutated = onNotesMutated
            ) as T

            modelClass.isAssignableFrom(ChecklistViewModel::class.java) -> ChecklistViewModel(
                userDataRepository = appContainer.userDataRepository,
                equipmentRepository = appContainer.equipmentRepository
            ) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                userDataRepository = appContainer.userDataRepository,
                userPreferencesRepository = appContainer.userPreferencesRepository,
                onPersonalDataCleared = onPersonalDataCleared
            ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
