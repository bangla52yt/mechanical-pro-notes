package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.data.local.database.dao.BookmarkDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.ChecklistDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.UserNoteDao
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.data.repository.BackupJsonCodec
import com.researchbangla.mechanicalpronotes.data.repository.BackupParseOutcome
import com.researchbangla.mechanicalpronotes.data.repository.CalculationRepositoryImpl
import com.researchbangla.mechanicalpronotes.data.repository.UserDataRepositoryImpl
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression tests for Backup v3 (v1/v2/v3 round-trip, custom_checklists, execution metadata,
 * SHA-256 validation, multi-store compensating rollback) and CalculationRepositoryImpl.
 */
class BackupJsonCodecAndCalcRepoTest {

    private class FakeCalculationDao(
        private val calculations: Map<String, CalculationEntity> = emptyMap(),
        private val variables: Map<String, List<FormulaVariableEntity>> = emptyMap()
    ) : CalculationDao {
        val historyFlow = MutableStateFlow<List<CalculationHistoryEntity>>(emptyList())
        private var nextId = 1L

        override fun getAllCalculations(): Flow<List<CalculationEntity>> = flowOf(calculations.values.toList())
        override suspend fun getCalculationById(id: String): CalculationEntity? = calculations[id]
        override suspend fun getVariablesForCalculation(calcId: String): List<FormulaVariableEntity> =
            variables[calcId].orEmpty()
        override suspend fun insertCalculations(calculations: List<CalculationEntity>) = Unit
        override suspend fun insertVariables(variables: List<FormulaVariableEntity>) = Unit
        override fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>> = historyFlow
        override suspend fun insertHistory(item: CalculationHistoryEntity): Long {
            val assigned = if (item.id > 0L) item.id else nextId++
            historyFlow.value = historyFlow.value + item.copy(id = assigned)
            return assigned
        }
        override suspend fun deleteAllHistory() {
            historyFlow.value = emptyList()
        }
    }

    private class FakeUserNoteDao : UserNoteDao {
        val notesFlow = MutableStateFlow<List<UserNoteEntity>>(emptyList())
        private var nextId = 1L

        override fun getNotesForEquipment(equipmentId: String): Flow<List<UserNoteEntity>> =
            flowOf(notesFlow.value.filter { it.equipmentId == equipmentId })
        override fun getAllNotes(): Flow<List<UserNoteEntity>> = notesFlow
        override suspend fun getNoteById(noteId: Long): UserNoteEntity? =
            notesFlow.value.firstOrNull { it.id == noteId }
        override suspend fun insertNote(note: UserNoteEntity): Long {
            val assigned = if (note.id > 0L) note.id else nextId++
            notesFlow.value = notesFlow.value.filterNot { it.id == assigned } + note.copy(id = assigned)
            return assigned
        }
        override suspend fun deleteNote(noteId: Long) {
            notesFlow.value = notesFlow.value.filterNot { it.id == noteId }
        }
        override suspend fun deleteAllNotes() {
            notesFlow.value = emptyList()
        }
    }

    private class FakeBookmarkDao : BookmarkDao {
        val bookmarksFlow = MutableStateFlow<List<UserBookmarkEntity>>(emptyList())
        private var nextId = 1L

        override fun getAllBookmarks(): Flow<List<UserBookmarkEntity>> = bookmarksFlow
        override suspend fun isBookmarked(entityType: String, entityId: String): Int =
            bookmarksFlow.value.count { it.entityType == entityType && it.entityId == entityId }
        override suspend fun insertBookmark(bookmark: UserBookmarkEntity): Long {
            val assigned = if (bookmark.id > 0L) bookmark.id else nextId++
            bookmarksFlow.value = bookmarksFlow.value.filterNot {
                it.entityType == bookmark.entityType && it.entityId == bookmark.entityId
            } + bookmark.copy(id = assigned)
            return assigned
        }
        override suspend fun deleteBookmark(entityType: String, entityId: String) {
            bookmarksFlow.value = bookmarksFlow.value.filterNot {
                it.entityType == entityType && it.entityId == entityId
            }
        }
        override suspend fun deleteAllBookmarks() {
            bookmarksFlow.value = emptyList()
        }
    }

    private class FakeChecklistDao : ChecklistDao {
        val progressFlow = MutableStateFlow<List<ChecklistProgressEntity>>(emptyList())
        private var nextId = 1L

        override fun getProgressForChecklist(checklistId: String): Flow<List<ChecklistProgressEntity>> =
            flowOf(progressFlow.value.filter { it.checklistId == checklistId })
        override fun getAllProgress(): Flow<List<ChecklistProgressEntity>> = progressFlow
        override suspend fun upsertProgress(item: ChecklistProgressEntity): Long {
            val existing = progressFlow.value.firstOrNull {
                it.checklistId == item.checklistId && it.itemId == item.itemId
            }
            val assigned = existing?.id ?: if (item.id > 0L) item.id else nextId++
            progressFlow.value = progressFlow.value.filterNot {
                it.checklistId == item.checklistId && it.itemId == item.itemId
            } + item.copy(id = assigned)
            return assigned
        }
        override suspend fun clearChecklistProgress(checklistId: String) {
            progressFlow.value = progressFlow.value.filterNot { it.checklistId == checklistId }
        }
        override suspend fun deleteAllChecklistProgress() {
            progressFlow.value = emptyList()
        }
    }

    private class FakePrefsRepo : UserPreferencesRepository {
        override val userPreferences: Flow<UserPreferences> = flowOf(UserPreferences())
        val customChecklists = MutableStateFlow<List<CustomChecklistTemplate>>(emptyList())
        val executionMetadata = MutableStateFlow<Map<String, ChecklistExecutionMetadata>>(emptyMap())
        var failNextSaveChecklists: Boolean = false
        var failNextSaveMetadata: Boolean = false

        override suspend fun setThemeMode(mode: AppThemeMode) = Unit
        override suspend fun setLargePlantFloorFont(enabled: Boolean) = Unit
        override fun observeCustomChecklists(): Flow<List<CustomChecklistTemplate>> = customChecklists
        override suspend fun saveCustomChecklists(checklists: List<CustomChecklistTemplate>) {
            if (failNextSaveChecklists) {
                failNextSaveChecklists = false
                throw IllegalStateException("Simulated DataStore write failure for custom checklists")
            }
            customChecklists.value = checklists
        }
        override fun observeChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> = executionMetadata
        override suspend fun saveChecklistExecutionMetadata(metadata: Map<String, ChecklistExecutionMetadata>) {
            if (failNextSaveMetadata) {
                failNextSaveMetadata = false
                throw IllegalStateException("Simulated DataStore write failure for metadata")
            }
            executionMetadata.value = metadata
        }
        override suspend fun clearChecklistWorkflowPreferences() {
            customChecklists.value = emptyList()
            executionMetadata.value = emptyMap()
        }
    }

    @Test
    fun signedV3BackupRoundTripsAllSixCollectionsCleanly() {
        val json = BackupJsonCodec.encodeBackup(
            notes = listOf(
                UserNoteEntity(
                    equipmentId = "EQ-G01",
                    title = "Safety Valve Note",
                    body = "Check spring seal after popping test.",
                    tagsCsv = "boiler|||safety",
                    createdAt = 1000L,
                    updatedAt = 2000L
                )
            ),
            bookmarks = listOf(
                UserBookmarkEntity(
                    entityType = "EQUIPMENT",
                    entityId = "EQ-G01",
                    entityTitle = "Natural Gas Firetube Boiler",
                    createdAt = 1500L
                )
            ),
            history = listOf(
                CalculationHistoryEntity(
                    calculationId = "CALC-01",
                    calculationTitle = "Direct Thermal Efficiency",
                    timestamp = 3000L,
                    inputsJson = """{"m_s":4000.0}""",
                    outputsJson = """{"Thermal Efficiency":85.3}""",
                    userRemark = "Shift A"
                )
            ),
            checklists = listOf(
                ChecklistProgressEntity(
                    checklistId = "CHK-CUSTOM-10",
                    itemId = 1,
                    isCompleted = true,
                    completionTimestamp = 4000L,
                    remarks = "OK"
                )
            ),
            customChecklists = listOf(
                CustomChecklistTemplate(
                    id = "CHK-CUSTOM-10",
                    title = "Hydro Drum Pre-Shift",
                    description = "Vibration and brake pad inspection",
                    equipmentId = "EQ-B01",
                    items = listOf(
                        CustomChecklistItem(1, "Inspect drum interlock pin", "Safety", isCrucial = true)
                    ),
                    createdAt = 4100L,
                    updatedAt = 4200L
                )
            ),
            executionMetadata = listOf(
                ChecklistExecutionMetadata(
                    checklistId = "CHK-CUSTOM-10",
                    equipmentId = "EQ-B01",
                    operatorName = "Kamrul Hasan",
                    shift = ChecklistShift.NIGHT,
                    updatedAt = 4300L
                )
            ),
            exportTimestamp = 5000L
        )

        val root = Json.parseToJsonElement(json).jsonObject
        assertEquals(3, root["metadata"]!!.jsonObject["backup_version"]!!.jsonPrimitive.int)

        val outcome = BackupJsonCodec.parseAndValidate(json)
        assertTrue(outcome is BackupParseOutcome.Valid)
        val payload = (outcome as BackupParseOutcome.Valid).payload
        assertEquals(1, payload.notes.size)
        assertEquals(1, payload.bookmarks.size)
        assertEquals(1, payload.calculationHistory.size)
        assertEquals(1, payload.checklistProgress.size)
        assertEquals(1, payload.customChecklists.size)
        assertEquals("CHK-CUSTOM-10", payload.customChecklists.first().id)
        assertEquals(1, payload.checklistExecutionMetadata.size)
        assertEquals("Kamrul Hasan", payload.checklistExecutionMetadata.first().operatorName)
        assertEquals(ChecklistShift.NIGHT, payload.checklistExecutionMetadata.first().shift)
    }

    @Test
    fun v1AndV2BackupsImportBackwardCompatiblyWithoutCustomChecklists() {
        val v1Json = """
            {
              "metadata": {
                "app_name": "Mechanical Pro Notes",
                "backup_version": 1,
                "export_timestamp": 1731456000000
              },
              "user_notes": [
                {"equipment_id": "EQ-A01", "title": "V1 Note", "body": "Legacy backup note"}
              ]
            }
        """.trimIndent()
        val v1Outcome = BackupJsonCodec.parseAndValidate(v1Json)
        assertTrue(v1Outcome is BackupParseOutcome.Valid)
        val v1Payload = (v1Outcome as BackupParseOutcome.Valid).payload
        assertEquals(1, v1Payload.notes.size)
        assertTrue(v1Payload.customChecklists.isEmpty())
        assertTrue(v1Payload.checklistExecutionMetadata.isEmpty())

        val v2UnsignedRoot = buildJsonObject {
            put("metadata", buildJsonObject {
                put("app_name", "Mechanical Pro Notes")
                put("backup_version", 2)
                put("export_timestamp", 1731456000000L)
            })
            put("user_notes", buildJsonArray { })
            put("bookmarks", buildJsonArray { })
            put("calculation_history", buildJsonArray { })
            put("checklist_progress", buildJsonArray {
                add(buildJsonObject {
                    put("checklist_id", "CHK-BLR-DAILY")
                    put("item_id", 2)
                    put("is_completed", true)
                })
            })
        }
        val pretty = Json { prettyPrint = true; prettyPrintIndent = "  " }
        val v2Sha = BackupJsonCodec.sha256(pretty.encodeToString(JsonObject.serializer(), v2UnsignedRoot))
        val v2Signed = buildJsonObject {
            put("metadata", buildJsonObject {
                put("app_name", "Mechanical Pro Notes")
                put("backup_version", 2)
                put("export_timestamp", 1731456000000L)
                put("sha256", v2Sha)
            })
            put("user_notes", v2UnsignedRoot["user_notes"]!!)
            put("bookmarks", v2UnsignedRoot["bookmarks"]!!)
            put("calculation_history", v2UnsignedRoot["calculation_history"]!!)
            put("checklist_progress", v2UnsignedRoot["checklist_progress"]!!)
        }
        val v2Outcome = BackupJsonCodec.parseAndValidate(pretty.encodeToString(JsonObject.serializer(), v2Signed))
        assertTrue(v2Outcome is BackupParseOutcome.Valid)
        val v2Payload = (v2Outcome as BackupParseOutcome.Valid).payload
        assertEquals(1, v2Payload.checklistProgress.size)
        assertTrue(v2Payload.customChecklists.isEmpty())
    }

    @Test
    fun backupValidationRejectsTamperingEmptyMalformedCustomChecklistsAndFutureVersions() {
        val invalid = BackupJsonCodec.parseAndValidate("{not valid json")
        assertEquals(BackupImportFailureReason.INVALID_JSON, (invalid as BackupParseOutcome.Invalid).failure.reason)

        val missingMeta = BackupJsonCodec.parseAndValidate("""{"user_notes":[]}""")
        assertEquals(
            BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
            (missingMeta as BackupParseOutcome.Invalid).failure.reason
        )

        val wrongApp = BackupJsonCodec.parseAndValidate("""{"metadata":{"app_name":"Other App","backup_version":3}}""")
        assertEquals(BackupImportFailureReason.UNSUPPORTED_APP, (wrongApp as BackupParseOutcome.Invalid).failure.reason)

        val futureVer = BackupJsonCodec.parseAndValidate(
            """{"metadata":{"app_name":"Mechanical Pro Notes","backup_version":99}}"""
        )
        assertEquals(
            BackupImportFailureReason.UNSUPPORTED_VERSION,
            (futureVer as BackupParseOutcome.Invalid).failure.reason
        )

        val emptyCustomItems = BackupJsonCodec.parseAndValidate(
            """
            {
              "metadata":{"app_name":"Mechanical Pro Notes","backup_version":3},
              "custom_checklists":[{"id":"CHK-CUSTOM-1","title":"No items","items":[]}]
            }
            """.trimIndent()
        )
        assertEquals(
            BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
            (emptyCustomItems as BackupParseOutcome.Invalid).failure.reason
        )

        val packagedCollision = BackupJsonCodec.parseAndValidate(
            """
            {
              "metadata":{"app_name":"Mechanical Pro Notes","backup_version":3},
              "custom_checklists":[
                {"id":"CHK-BLR-DAILY","title":"Collision","items":[{"id":1,"text":"X"}]}
              ]
            }
            """.trimIndent()
        )
        assertEquals(
            BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
            (packagedCollision as BackupParseOutcome.Invalid).failure.reason
        )

        val signed = BackupJsonCodec.encodeBackup(
            notes = listOf(UserNoteEntity(equipmentId = "EQ-A01", title = "SecretPlantNote", body = "CONFIDENTIAL_TEXT")),
            bookmarks = emptyList(),
            history = emptyList(),
            checklists = emptyList(),
            customChecklists = listOf(
                CustomChecklistTemplate(
                    id = "CHK-CUSTOM-5",
                    title = "Original",
                    items = listOf(CustomChecklistItem(1, "Check bolt"))
                )
            )
        )
        val tampered = signed.replace("CONFIDENTIAL_TEXT", "MODIFIED_TEXT")
        val checksumFail = BackupJsonCodec.parseAndValidate(tampered)
        val failure = (checksumFail as BackupParseOutcome.Invalid).failure
        assertEquals(BackupImportFailureReason.CHECKSUM_MISMATCH, failure.reason)
        assertFalse(failure.message.contains("MODIFIED_TEXT"))
        assertFalse(failure.message.contains("SecretPlantNote"))
    }

    @Test
    fun repositoryExportImportRestoresCustomChecklistsMetadataAndRollsBackOnPrefsFailure() = runTest {
        val noteDao = FakeUserNoteDao()
        val bmDao = FakeBookmarkDao()
        val calcDao = FakeCalculationDao()
        val chkDao = FakeChecklistDao()
        val prefs = FakePrefsRepo()
        val repo = UserDataRepositoryImpl(noteDao, bmDao, calcDao, chkDao, prefs)

        // Seed existing data before backup
        noteDao.insertNote(UserNoteEntity(equipmentId = "EQ-A01", title = "Existing", body = "Keep me"))
        prefs.customChecklists.value = listOf(
            CustomChecklistTemplate(
                id = "CHK-CUSTOM-EXISTING",
                title = "Existing Custom",
                items = listOf(CustomChecklistItem(1, "Grease pump"))
            )
        )
        prefs.executionMetadata.value = mapOf(
            "CHK-BLR-DAILY" to ChecklistExecutionMetadata(
                checklistId = "CHK-BLR-DAILY",
                equipmentId = "EQ-G01",
                operatorName = "Existing Op",
                shift = ChecklistShift.DAY
            )
        )

        // Create backup v3 payload from another session
        val backupV3 = BackupJsonCodec.encodeBackup(
            notes = listOf(UserNoteEntity(equipmentId = "EQ-B01", title = "Restored", body = "New body", createdAt = 99L)),
            bookmarks = listOf(UserBookmarkEntity(entityType = "CHECKLIST", entityId = "CHK-CUSTOM-NEW", entityTitle = "New")),
            history = emptyList(),
            checklists = listOf(ChecklistProgressEntity(checklistId = "CHK-CUSTOM-NEW", itemId = 1, isCompleted = true)),
            customChecklists = listOf(
                CustomChecklistTemplate(
                    id = "CHK-CUSTOM-NEW",
                    title = "Imported Custom",
                    equipmentId = "EQ-B01",
                    items = listOf(CustomChecklistItem(1, "Inspect seal"))
                )
            ),
            executionMetadata = listOf(
                ChecklistExecutionMetadata(
                    checklistId = "CHK-CUSTOM-NEW",
                    equipmentId = "EQ-B01",
                    operatorName = "Sajib",
                    shift = ChecklistShift.EVENING
                )
            )
        )

        // Simulate DataStore failure during restore -> triggers compensating rollback of Room + prefs!
        prefs.failNextSaveChecklists = true
        val failedImport = repo.importUserPersonalDataJson(backupV3, overwrite = true)
        assertTrue(failedImport is BackupImportResult.Failure)
        assertEquals(
            BackupImportFailureReason.DATABASE_ERROR,
            (failedImport as BackupImportResult.Failure).reason
        )
        assertTrue(failedImport.message.contains("restored", ignoreCase = true))
        assertEquals(1, noteDao.getAllNotes().first().size)
        assertEquals("Existing", noteDao.getAllNotes().first().first().title)
        assertEquals(1, prefs.customChecklists.value.size)
        assertEquals("CHK-CUSTOM-EXISTING", prefs.customChecklists.value.first().id)

        // Successful merge restore preserves existing + adds new v3 records
        val mergeImport = repo.importUserPersonalDataJson(backupV3, overwrite = false)
        assertTrue(mergeImport is BackupImportResult.Success)
        val success = mergeImport as BackupImportResult.Success
        assertEquals(1, success.notesImported)
        assertEquals(1, success.customChecklistsImported)
        assertEquals(1, success.executionMetadataImported)
        assertEquals(2, noteDao.getAllNotes().first().size)
        assertEquals(2, prefs.customChecklists.value.size)
        assertEquals("Sajib", prefs.executionMetadata.value["CHK-CUSTOM-NEW"]?.operatorName)

        // Empty backup succeeds cleanly with 0 imported
        val emptyBackup = BackupJsonCodec.encodeBackup(emptyList(), emptyList(), emptyList(), emptyList())
        val emptyMerge = repo.importUserPersonalDataJson(emptyBackup, overwrite = false)
        assertTrue(emptyMerge is BackupImportResult.Success)
        val emptySuccess = emptyMerge as BackupImportResult.Success
        assertEquals(0, emptySuccess.notesImported)
        assertEquals(0, emptySuccess.customChecklistsImported)
        assertEquals(0, emptySuccess.executionMetadataImported)
    }

    @Test
    fun calculationRepositoryPrefersRoomDataBeforeStaticFallback() = runTest {
        val roomEntity = CalculationEntity(
            id = "CALC-02",
            code = "CALC-02",
            title = "Room Authoritative Boiler Heat Loss Title",
            bengaliTitle = "রুম ডাটাবেস",
            category = "Boiler Thermal",
            formulaLatex = "eta = 100 - sum(L)",
            engineeringReference = "BS 845-1",
            assumptions = "Room assumption",
            limitations = "Room limitation",
            status = "RESEARCH_PENDING"
        )
        val roomVar = FormulaVariableEntity(
            id = 10L,
            calculationId = "CALC-02",
            symbol = "T_flue",
            name = "Flue Gas Temp",
            unit = "°C",
            defaultValue = 185.0,
            minBound = 80.0,
            maxBound = 450.0,
            isRequired = true
        )
        val repo = CalculationRepositoryImpl(
            FakeCalculationDao(
                calculations = mapOf("CALC-02" to roomEntity),
                variables = mapOf("CALC-02" to listOf(roomVar))
            )
        )

        val resolvedFromRoom = repo.getCalculationById("calc-02")
        assertNotNull(resolvedFromRoom)
        assertEquals("Room Authoritative Boiler Heat Loss Title", resolvedFromRoom?.title)
        assertEquals(CalculationStatus.RESEARCH_PENDING, resolvedFromRoom?.status)
        assertEquals(1, resolvedFromRoom?.variables?.size)

        val fallbackCalc01 = repo.getCalculationById("CALC-01")
        assertNotNull(fallbackCalc01)
        assertEquals("CALC-01", fallbackCalc01?.id)
    }
}
