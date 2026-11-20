package com.researchbangla.mechanicalpronotes

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.researchbangla.mechanicalpronotes.data.local.database.MechanicalProDatabase
import com.researchbangla.mechanicalpronotes.data.local.database.migration.DatabaseMigrations
import com.researchbangla.mechanicalpronotes.data.local.preferences.DataStoreUserPreferencesRepository
import com.researchbangla.mechanicalpronotes.data.repository.SearchRepositoryImpl
import com.researchbangla.mechanicalpronotes.data.repository.UserDataRepositoryImpl
import com.researchbangla.mechanicalpronotes.domain.model.BackupImportResult
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistExecutionMetadata
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistShift
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistItem
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistTemplate
import com.researchbangla.mechanicalpronotes.domain.model.UserNote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Workstream G (AUD-005): Android Instrumented Tests covering packaged SQLite database open via
 * Room, FTS4 English & Bengali queries, personal note isolation, and Backup v3 round-trip restore.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseAndSearchInstrumentedTest {

    private lateinit var targetContext: Context
    private lateinit var database: MechanicalProDatabase
    private val testDbName = "mechanical_pro_notes_instrumented_test.db"

    @Before
    fun setUp() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        targetContext.deleteDatabase(testDbName)
        database = Room.databaseBuilder(
            targetContext,
            MechanicalProDatabase::class.java,
            testDbName
        )
            .createFromAsset("database/${MechanicalProDatabase.DATABASE_NAME}")
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        targetContext.deleteDatabase(testDbName)
    }

    @Test
    fun packagedDatabaseOpensCleanlyAndContainsAll64AssetsAnd20Calculations() = runBlocking {
        val equipmentList = database.equipmentDao().getAllEquipment().first()
        val families = database.equipmentDao().getAllSystemFamilies().first()
        val calculations = database.calculationDao().getAllCalculations().first()

        assertEquals(64, equipmentList.size)
        assertEquals(16, families.size)
        assertEquals(20, calculations.size)
    }

    @Test
    fun fts4BilingualEnglishAndBengaliSearchMatchesPackagedEquipment() = runBlocking {
        val searchRepo = SearchRepositoryImpl(
            searchDao = database.searchDao(),
            calculationDao = database.calculationDao(),
            userNoteDao = database.userNoteDao(),
            equipmentDao = database.equipmentDao()
        )

        val englishHits = searchRepo.search("boiler", limit = 20)
        assertTrue(englishHits.any { it.id == "EQ-G01" })

        val bengaliHits = searchRepo.search("বয়লার", limit = 20)
        assertTrue(bengaliHits.any { it.id == "EQ-G01" || it.id == "EQ-G02" })
    }

    @Test
    fun userDataRepositoryPersistsNotesCustomChecklistsAndRestoresV3Backup() = runBlocking {
        val prefsRepo = DataStoreUserPreferencesRepository(targetContext)
        prefsRepo.clearChecklistWorkflowPreferences()

        val userDataRepo = UserDataRepositoryImpl(
            database = database,
            userNoteDao = database.userNoteDao(),
            bookmarkDao = database.bookmarkDao(),
            calculationDao = database.calculationDao(),
            checklistDao = database.checklistDao(),
            userPreferencesRepository = prefsRepo
        )

        userDataRepo.saveNote(
            UserNote(
                equipmentId = "EQ-G01",
                title = "Instrumented Check",
                body = "Verified safety relief seal on device.",
                tags = listOf("boiler", "safety")
            )
        )
        userDataRepo.saveCustomChecklist(
            CustomChecklistTemplate(
                id = "CHK-CUSTOM-INST-1",
                title = "Device Round Check",
                equipmentId = "EQ-G01",
                items = listOf(CustomChecklistItem(1, "Inspect gauge cock", "Safety", true))
            )
        )
        userDataRepo.saveChecklistExecutionMetadata(
            ChecklistExecutionMetadata(
                checklistId = "CHK-CUSTOM-INST-1",
                equipmentId = "EQ-G01",
                operatorName = "Test Engineer",
                shift = ChecklistShift.DAY
            )
        )

        val backupJson = userDataRepo.exportUserPersonalDataJson()
        val cleared = userDataRepo.clearAllPersonalData()
        assertNotNull(cleared)
        assertTrue(userDataRepo.getAllUserNotes().first().isEmpty())
        assertTrue(userDataRepo.getCustomChecklists().first().isEmpty())

        val restored = userDataRepo.importUserPersonalDataJson(backupJson, overwrite = true)
        assertTrue(restored is BackupImportResult.Success)
        val success = restored as BackupImportResult.Success
        assertEquals(1, success.notesImported)
        assertEquals(1, success.customChecklistsImported)
        assertEquals(1, success.executionMetadataImported)

        val notesAfter = userDataRepo.getAllUserNotes().first()
        assertEquals(1, notesAfter.size)
        assertEquals("Instrumented Check", notesAfter.first().title)
        assertEquals(1, userDataRepo.getCustomChecklists().first().size)
        assertEquals("Test Engineer", userDataRepo.getAllChecklistExecutionMetadata().first()["CHK-CUSTOM-INST-1"]?.operatorName)

        prefsRepo.clearChecklistWorkflowPreferences()
    }
}
