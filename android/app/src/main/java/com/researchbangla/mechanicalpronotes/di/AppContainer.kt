package com.researchbangla.mechanicalpronotes.di

import android.content.Context
import com.researchbangla.mechanicalpronotes.data.local.database.MechanicalProDatabase
import com.researchbangla.mechanicalpronotes.data.local.preferences.DataStoreUserPreferencesRepository
import com.researchbangla.mechanicalpronotes.data.repository.*
import com.researchbangla.mechanicalpronotes.domain.repository.*

/**
 * Manual Dependency Injection Container (DEC-015).
 *
 * Provides thread-safe, single-instance repositories and database instances
 * with zero code-generation overhead and zero KSP reflection lag.
 */
interface AppContainer {
    val database: MechanicalProDatabase
    val equipmentRepository: EquipmentRepository
    val boilerRepository: BoilerRepository
    val searchRepository: SearchRepository
    val calculationRepository: CalculationRepository
    val userDataRepository: UserDataRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: MechanicalProDatabase by lazy {
        MechanicalProDatabase.getInstance(context)
    }

    override val equipmentRepository: EquipmentRepository by lazy {
        EquipmentRepositoryImpl(database.equipmentDao())
    }

    override val boilerRepository: BoilerRepository by lazy {
        BoilerRepositoryImpl(database.boilerDao(), database.equipmentDao())
    }

    override val searchRepository: SearchRepository by lazy {
        SearchRepositoryImpl(
            searchDao = database.searchDao(),
            calculationDao = database.calculationDao(),
            userNoteDao = database.userNoteDao(),
            equipmentDao = database.equipmentDao()
        )
    }

    override val calculationRepository: CalculationRepository by lazy {
        CalculationRepositoryImpl(database.calculationDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        DataStoreUserPreferencesRepository(context)
    }

    override val userDataRepository: UserDataRepository by lazy {
        UserDataRepositoryImpl(
            database = database,
            userNoteDao = database.userNoteDao(),
            bookmarkDao = database.bookmarkDao(),
            calculationDao = database.calculationDao(),
            checklistDao = database.checklistDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }
}
