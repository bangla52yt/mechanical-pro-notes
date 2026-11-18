package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.data.repository.BackupJsonCodec
import com.researchbangla.mechanicalpronotes.data.repository.BackupParseOutcome
import com.researchbangla.mechanicalpronotes.data.repository.CalculationRepositoryImpl
import com.researchbangla.mechanicalpronotes.domain.model.BackupImportFailureReason
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression tests for DEF-AUD-07 (categorized backup validation & checksum checks)
 * and DEF-AUD-08-D (Room-first CalculationRepositoryImpl source of truth).
 */
class BackupJsonCodecAndCalcRepoTest {

    private class FakeCalculationDao(
        private val calculations: Map<String, CalculationEntity> = emptyMap(),
        private val variables: Map<String, List<FormulaVariableEntity>> = emptyMap()
    ) : CalculationDao {
        override fun getAllCalculations(): Flow<List<CalculationEntity>> = flowOf(calculations.values.toList())
        override suspend fun getCalculationById(id: String): CalculationEntity? = calculations[id]
        override suspend fun getVariablesForCalculation(calcId: String): List<FormulaVariableEntity> =
            variables[calcId].orEmpty()
        override suspend fun insertCalculations(calculations: List<CalculationEntity>) = Unit
        override suspend fun insertVariables(variables: List<FormulaVariableEntity>) = Unit
        override fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>> = flowOf(emptyList())
        override suspend fun insertHistory(item: CalculationHistoryEntity): Long = 1L
        override suspend fun deleteAllHistory() = Unit
    }

    @Test
    fun signedBackupRoundTripsCleanlyAndPreservesAllPersonalCollections() {
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
                    checklistId = "CHK-BOILER-DAILY",
                    itemId = 1,
                    isCompleted = true,
                    completionTimestamp = 4000L,
                    remarks = "OK"
                )
            ),
            exportTimestamp = 5000L
        )

        val outcome = BackupJsonCodec.parseAndValidate(json)
        assertTrue(outcome is BackupParseOutcome.Valid)
        val payload = (outcome as BackupParseOutcome.Valid).payload
        assertEquals(1, payload.notes.size)
        assertEquals("EQ-G01", payload.notes.first().equipmentId)
        assertEquals(1, payload.bookmarks.size)
        assertEquals(1, payload.calculationHistory.size)
        assertEquals(1, payload.checklistProgress.size)
    }

    @Test
    fun backupValidationReturnsSpecificFailureCategoriesWithoutLeakingData() {
        val invalid = BackupJsonCodec.parseAndValidate("{not valid json")
        assertEquals(
            BackupImportFailureReason.INVALID_JSON,
            (invalid as BackupParseOutcome.Invalid).failure.reason
        )

        val missingMeta = BackupJsonCodec.parseAndValidate("""{"user_notes":[]}""")
        assertEquals(
            BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
            (missingMeta as BackupParseOutcome.Invalid).failure.reason
        )

        val wrongApp = BackupJsonCodec.parseAndValidate(
            """{"metadata":{"app_name":"Other App","backup_version":2}}"""
        )
        assertEquals(
            BackupImportFailureReason.UNSUPPORTED_APP,
            (wrongApp as BackupParseOutcome.Invalid).failure.reason
        )

        val futureVer = BackupJsonCodec.parseAndValidate(
            """{"metadata":{"app_name":"Mechanical Pro Notes","backup_version":99}}"""
        )
        assertEquals(
            BackupImportFailureReason.UNSUPPORTED_VERSION,
            (futureVer as BackupParseOutcome.Invalid).failure.reason
        )

        val signed = BackupJsonCodec.encodeBackup(
            notes = listOf(UserNoteEntity(equipmentId = "EQ-A01", title = "SecretPlantNote", body = "CONFIDENTIAL_TEXT")),
            bookmarks = emptyList(),
            history = emptyList(),
            checklists = emptyList()
        )
        val tampered = signed.replace("CONFIDENTIAL_TEXT", "MODIFIED_TEXT")
        val checksumFail = BackupJsonCodec.parseAndValidate(tampered)
        val failure = (checksumFail as BackupParseOutcome.Invalid).failure
        assertEquals(BackupImportFailureReason.CHECKSUM_MISMATCH, failure.reason)
        assertFalse(failure.message.contains("MODIFIED_TEXT"))
        assertFalse(failure.message.contains("SecretPlantNote"))
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
