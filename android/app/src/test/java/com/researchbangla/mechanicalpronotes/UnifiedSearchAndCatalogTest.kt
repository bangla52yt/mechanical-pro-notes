package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.EquipmentDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.SearchDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.UserNoteDao
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.data.repository.SearchRepositoryImpl
import com.researchbangla.mechanicalpronotes.domain.catalog.EngineeringCatalogSearch
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalTopicStatus
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalsCatalog
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.catalog.SearchResultType
import com.researchbangla.mechanicalpronotes.domain.catalog.TechnicalReferenceCatalog
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

/**
 * Stage 3 Area E + Batch 05 GAP-07 — Single-Source Catalogs, Fundamentals Content-Gap Honesty,
 * Unified Knowledge Search Isolation & Offline Discovery Tests.
 */
class UnifiedSearchAndCatalogTest {

    private class FakeSearchDao(
        private val results: List<EquipmentEntity> = emptyList()
    ) : SearchDao {
        override suspend fun searchFts(query: String, limit: Int): List<EquipmentEntity> = results
        override suspend fun searchLike(query: String, limit: Int): List<EquipmentEntity> = results
        override suspend fun insertFts(ftsItems: List<EquipmentFtsEntity>) = Unit
    }

    private class FakeCalculationDao(
        private val historyItems: List<CalculationHistoryEntity> = emptyList()
    ) : CalculationDao {
        override fun getAllCalculations(): Flow<List<CalculationEntity>> = flowOf(emptyList())
        override suspend fun getCalculationById(id: String): CalculationEntity? = null
        override suspend fun getVariablesForCalculation(calcId: String): List<FormulaVariableEntity> = emptyList()
        override suspend fun insertCalculations(calculations: List<CalculationEntity>) = Unit
        override suspend fun insertVariables(variables: List<FormulaVariableEntity>) = Unit
        override fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>> = flowOf(historyItems)
        override suspend fun insertHistory(item: CalculationHistoryEntity): Long = item.id
        override suspend fun deleteAllHistory() = Unit
    }

    private class FakeUserNoteDao(
        private val notes: List<UserNoteEntity> = emptyList()
    ) : UserNoteDao {
        override fun getNotesForEquipment(equipmentId: String): Flow<List<UserNoteEntity>> =
            flowOf(notes.filter { it.equipmentId == equipmentId })
        override fun getAllNotes(): Flow<List<UserNoteEntity>> = flowOf(notes)
        override suspend fun getNoteById(noteId: Long): UserNoteEntity? = notes.firstOrNull { it.id == noteId }
        override suspend fun insertNote(note: UserNoteEntity): Long = note.id
        override suspend fun deleteNote(noteId: Long) = Unit
        override suspend fun deleteAllNotes() = Unit
    }

    private class FakeEquipmentDao(
        private val equipment: List<EquipmentEntity> = emptyList()
    ) : EquipmentDao {
        override fun getAllSystemFamilies(): Flow<List<SystemFamilyEntity>> = flowOf(emptyList())
        override fun getAllEquipment(): Flow<List<EquipmentEntity>> = flowOf(equipment)
        override fun getEquipmentByFamily(familyId: String): Flow<List<EquipmentEntity>> =
            flowOf(equipment.filter { it.familyId == familyId })
        override suspend fun getEquipmentById(equipmentId: String): EquipmentEntity? =
            equipment.firstOrNull { it.id == equipmentId }
        override fun getEquipmentByContentDepth(depth: String): Flow<List<EquipmentEntity>> =
            flowOf(equipment.filter { it.contentDepthStatus == depth })
        override suspend fun getSubtypesForEquipment(equipmentId: String): List<EquipmentSubtypeEntity> = emptyList()
        override suspend fun getComponentsForEquipment(equipmentId: String): List<ComponentEntity> = emptyList()
        override suspend fun getParametersForEquipment(equipmentId: String): List<TechnicalParameterEntity> = emptyList()
        override suspend fun getKnowledgeSections(equipmentId: String): List<KnowledgeSectionEntity> = emptyList()
        override suspend fun getFailureModes(equipmentId: String): List<FailureModeEntity> = emptyList()
        override suspend fun getMaintenanceProcedures(equipmentId: String): List<MaintenanceProcedureEntity> = emptyList()
        override suspend fun insertSystemFamilies(families: List<SystemFamilyEntity>) = Unit
        override suspend fun insertEquipmentCategories(categories: List<EquipmentCategoryEntity>) = Unit
        override suspend fun insertEquipment(equipment: List<EquipmentEntity>) = Unit
        override suspend fun insertSubtypes(subtypes: List<EquipmentSubtypeEntity>) = Unit
        override suspend fun insertComponents(components: List<ComponentEntity>) = Unit
        override suspend fun insertParameters(parameters: List<TechnicalParameterEntity>) = Unit
        override suspend fun insertKnowledgeSections(sections: List<KnowledgeSectionEntity>) = Unit
        override suspend fun insertFailureModes(modes: List<FailureModeEntity>) = Unit
        override suspend fun insertMaintenanceProcedures(procedures: List<MaintenanceProcedureEntity>) = Unit
    }

    @Test
    fun authoritativeCatalogCountsAndIdsAreConsistent() {
        assertEquals(4, PlantChecklistCatalog.ALL_CHECKLISTS.size)
        assertEquals(
            setOf("CHK-BLR-DAILY", "CHK-BLR-WEEKLY", "CHK-WASH-PREV", "CHK-HYDRO-SAFE"),
            PlantChecklistCatalog.ALL_CHECKLISTS.map { it.id }.toSet()
        )
        assertTrue(PlantChecklistCatalog.ALL_CHECKLISTS.all { it.items.isNotEmpty() })

        assertEquals(12, TechnicalReferenceCatalog.ALL_REFERENCES.size)
        assertEquals(12, TechnicalReferenceCatalog.ALL_REFERENCES.map { it.id }.toSet().size)

        assertEquals(15, FundamentalsCatalog.ALL_TOPICS.size)
        assertEquals(
            15,
            FundamentalsCatalog.ALL_TOPICS.count { it.status == FundamentalTopicStatus.VERIFIED_BASELINE }
        )
        assertEquals(
            0,
            FundamentalsCatalog.ALL_TOPICS.count { it.status == FundamentalTopicStatus.CONTENT_GAP }
        )
        assertTrue(FundamentalsCatalog.ALL_TOPICS.all {
            it.sourceCitation.isNotBlank() &&
                it.theoreticalFoundations.isNotBlank() &&
                it.washingPlantApplications.isNotBlank() &&
                it.governingFormulas.isNotEmpty()
        })
    }

    @Test
    fun unifiedSearchCoversChecklistsReferencesAndFundamentalsInEnglishAndBengali() {
        val checklistHits = EngineeringCatalogSearch.searchStaticCatalogs("Mobrey")
        assertTrue(checklistHits.any { it.type == SearchResultType.CHECKLIST && it.id == "CHK-BLR-DAILY" })

        val referenceHits = EngineeringCatalogSearch.searchStaticCatalogs("বিধিমালা ২০২৫")
        assertTrue(referenceHits.any { it.type == SearchResultType.REFERENCE && it.id == "REF-02" })

        val fundamentalHits = EngineeringCatalogSearch.searchStaticCatalogs("PREN")
        assertTrue(fundamentalHits.any { it.type == SearchResultType.FUNDAMENTAL && it.id == "FUND-07" })

        val nfdBengali = Normalizer.normalize("তাপগতিবিদ্যা", Normalizer.Form.NFD)
        val nfdHits = EngineeringCatalogSearch.searchStaticCatalogs(nfdBengali)
        assertTrue(nfdHits.any { it.type == SearchResultType.FUNDAMENTAL && it.id == "FUND-01" })

        // Batch 12: FUND-11 through FUND-15 offline catalog search & cross-links
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("Buckingham").any { it.id == "FUND-11" })
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("Capstan").any { it.id == "FUND-12" })
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("SMED").any { it.id == "FUND-13" })
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("Rankine").any { it.id == "FUND-14" })
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("Pt100").any { it.id == "FUND-15" })
        assertTrue(EngineeringCatalogSearch.searchStaticCatalogs("ইনস্ট্রুমেন্টেশন").any { it.id == "FUND-15" })
    }

    @Test
    fun searchRepositorySearchKnowledgeBaseCombinesStaticEquipmentAndCatalogsWithoutPersonalData() = runTest {
        val boiler = EquipmentEntity(
            id = "EQ-G01",
            categoryId = "CAT-07",
            familyId = "FAM-G",
            name = "Gas-Fired 3-Pass Wet-Back Steam Boiler",
            bengaliName = "গ্যাস বয়লার",
            contentDepthStatus = "PARTIAL_CONTENT_BLUEPRINT",
            criticalityTier = "CRITICAL_UTILITY"
        )
        val repo = SearchRepositoryImpl(FakeSearchDao(listOf(boiler)))

        val unified = repo.searchKnowledgeBase("Boiler")
        assertEquals(1, unified.equipment.size)
        assertTrue(unified.catalogHits.any { it.type == SearchResultType.CHECKLIST })
        assertTrue(unified.catalogHits.any { it.type == SearchResultType.REFERENCE })
        assertTrue(unified.catalogHits.any { it.type == SearchResultType.FUNDAMENTAL })
        assertTrue(unified.calculations.isNotEmpty())
        assertTrue(unified.userNotes.isEmpty())
        assertTrue(unified.calculationHistory.isEmpty())

        val blankResult = repo.searchKnowledgeBase("   ")
        assertTrue(blankResult.isEmpty)
        assertTrue(blankResult.equipment.isEmpty())
        assertTrue(blankResult.catalogHits.isEmpty())
        assertTrue(blankResult.calculations.isEmpty())
    }

    @Test
    fun searchRepositoryMatchesCalculationsAcrossIdsCategoriesVariablesAndBengaliTitlesWithRanking() = runTest {
        val repo = SearchRepositoryImpl(FakeSearchDao())

        val exactId = repo.searchKnowledgeBase("CALC-01")
        assertEquals("CALC-01", exactId.calculations.firstOrNull()?.id)

        val categoryMatch = repo.searchKnowledgeBase("Boiler Safety")
        assertTrue(categoryMatch.calculations.any { it.id == "CALC-09" })
        assertEquals(CalculationStatus.IMPLEMENTED, categoryMatch.calculations.first { it.id == "CALC-09" }.status)

        val pumpMatch = repo.searchKnowledgeBase("Pump")
        assertTrue(pumpMatch.calculations.any { it.id == "CALC-07" })

        val bengaliMatch = repo.searchKnowledgeBase("তাপীয় দক্ষতা")
        assertTrue(bengaliMatch.calculations.any { it.id == "CALC-01" })

        val catalogOnly = EngineeringCatalogSearch.searchStaticCatalogs("CALC-01")
        assertTrue(catalogOnly.isEmpty())
    }

    @Test
    fun searchRepositoryMatchesUserNotesSafelyIncludingEmptyTitleAndBodyWithoutPollutingStaticKnowledge() = runTest {
        val notes = listOf(
            UserNoteEntity(
                id = 101L,
                equipmentId = "EQ-G01",
                title = "",
                body = "Feedwater check valve chattering above 8 bar",
                tagsCsv = "boiler|||valve",
                createdAt = 1000L,
                updatedAt = 2000L
            ),
            UserNoteEntity(
                id = 102L,
                equipmentId = "EQ-H03",
                title = "Gland Packing Tightened",
                body = "   ",
                tagsCsv = "pump|||maintenance",
                createdAt = 1500L,
                updatedAt = 3000L
            )
        )
        val repo = SearchRepositoryImpl(
            searchDao = FakeSearchDao(),
            userNoteDao = FakeUserNoteDao(notes)
        )

        // Empty title uses safe fallback while matching body & equipmentId
        val valveHits = repo.searchKnowledgeBase("chattering")
        assertEquals(1, valveHits.userNotes.size)
        val note101 = valveHits.userNotes.first()
        assertEquals(101L, note101.id)
        assertEquals("Field Note (EQ-G01)", note101.displayTitle)
        assertTrue(note101.bodySnippet.contains("chattering"))
        assertEquals(listOf("boiler", "valve"), note101.tags)
        assertTrue(valveHits.equipment.isEmpty())
        assertTrue(valveHits.catalogHits.isEmpty())

        // Empty body never crashes UserNote invariant and matches title + equipmentId
        val packingHits = repo.searchKnowledgeBase("EQ-H03 Packing")
        assertEquals(1, packingHits.userNotes.size)
        val note102 = packingHits.userNotes.first()
        assertEquals(102L, note102.id)
        assertEquals("Gland Packing Tightened", note102.displayTitle)
        assertEquals("No note observation text", note102.bodySnippet)
    }

    @Test
    fun searchRepositoryMatchesCalculationHistoryByTitleOutputsTimestampAndRemarkToDistinguishRuns() = runTest {
        val history = listOf(
            CalculationHistoryEntity(
                id = 201L,
                calculationId = "CALC-01",
                calculationTitle = "Direct Boiler Thermal Efficiency",
                timestamp = 1_767_225_600_000L, // 01 Jan 2026
                inputsJson = """{"m_s": 5000.0, "m_f": 410.0}""",
                outputsJson = """{"Thermal Efficiency": 85.32}""",
                userRemark = "Morning shift boiler run steady"
            ),
            CalculationHistoryEntity(
                id = 202L,
                calculationId = "CALC-01",
                calculationTitle = "Direct Boiler Thermal Efficiency",
                timestamp = 1_767_312_000_000L, // 02 Jan 2026
                inputsJson = """{"m_s": 4200.0, "m_f": 390.0}""",
                outputsJson = """{"Thermal Efficiency": 79.40}""",
                userRemark = "Night shift excess air high"
            )
        )
        val repo = SearchRepositoryImpl(
            searchDao = FakeSearchDao(),
            calculationDao = FakeCalculationDao(historyItems = history)
        )

        val shiftHits = repo.searchKnowledgeBase("Morning shift")
        assertEquals(1, shiftHits.calculationHistory.size)
        assertEquals(201L, shiftHits.calculationHistory.first().id)
        assertTrue(shiftHits.calculationHistory.first().resultSummary.contains("85.32"))
        assertTrue(shiftHits.calculationHistory.first().inputsSummary.contains("5000"))

        val valueHit = repo.searchKnowledgeBase("79.40")
        assertEquals(1, valueHit.calculationHistory.size)
        assertEquals(202L, valueHit.calculationHistory.first().id)

        val multiRunHits = repo.searchKnowledgeBase("CALC-01")
        assertEquals(2, multiRunHits.calculationHistory.size)
        assertEquals(listOf(202L, 201L), multiRunHits.calculationHistory.map { it.id })
        assertTrue(multiRunHits.calculationHistory.all { it.formattedTimestamp.isNotBlank() && it.resultSummary.isNotBlank() })
    }

    @Test
    fun searchRepositoryMatchesUserNotesByEquipmentNameAndHandlesUnassociatedNotesSafely() = runTest {
        val eqBoiler = EquipmentEntity(
            id = "EQ-G01",
            categoryId = "CAT-07",
            familyId = "FAM-G",
            name = "Natural Gas Firetube Steam Boiler",
            bengaliName = "গ্যাস বয়লার",
            contentDepthStatus = "PARTIAL_CONTENT_BLUEPRINT",
            criticalityTier = "CRITICAL_UTILITY"
        )
        val notes = listOf(
            UserNoteEntity(
                id = 301L,
                equipmentId = "EQ-G01",
                title = "Routine Check",
                body = "Checked sight glass clarity",
                tagsCsv = "sight-glass|||Shift A",
                createdAt = 1000L,
                updatedAt = 2000L
            ),
            UserNoteEntity(
                id = 302L,
                equipmentId = "UNASSOCIATED",
                title = "",
                body = "Prepare torque wrench set for weekend shutdown",
                tagsCsv = "shutdown|||tools",
                createdAt = 1500L,
                updatedAt = 2500L
            )
        )
        val repo = SearchRepositoryImpl(
            searchDao = FakeSearchDao(),
            userNoteDao = FakeUserNoteDao(notes),
            equipmentDao = FakeEquipmentDao(listOf(eqBoiler))
        )

        val eqNameHits = repo.searchKnowledgeBase("Firetube")
        assertEquals(1, eqNameHits.userNotes.size)
        assertEquals(301L, eqNameHits.userNotes.first().id)
        assertEquals("Natural Gas Firetube Steam Boiler", eqNameHits.userNotes.first().equipmentName)

        val unassociatedHits = repo.searchKnowledgeBase("torque")
        assertEquals(1, unassociatedHits.userNotes.size)
        assertEquals("UNASSOCIATED", unassociatedHits.userNotes.first().equipmentId)
        assertEquals("General Field Note", unassociatedHits.userNotes.first().displayTitle)
        assertNull(unassociatedHits.userNotes.first().equipmentName)
    }

    @Test
    fun batch14StandardsAndStatutoryCitationsAreAccurate() {
        val ref06 = TechnicalReferenceCatalog.ALL_REFERENCES.first { it.id == "REF-06" }
        assertTrue(ref06.summary.contains("30,000 r/min"))
        assertFalse(ref06.summary.contains("15,000"))
        assertTrue(ref06.mandatoryRequirements.contains("Group 1"))
        assertTrue(ref06.mandatoryRequirements.contains("Group 2"))
        assertTrue(ref06.mandatoryRequirements.contains("ISO 10816-7"))

        val ref01 = TechnicalReferenceCatalog.ALL_REFERENCES.first { it.id == "REF-01" }
        assertTrue(ref01.summary.contains("11 April 2022"))
        assertTrue(ref01.summary.contains("Section 54"))
        assertTrue(ref01.mandatoryRequirements.contains("Section 9"))
        assertTrue(ref01.mandatoryRequirements.contains("Section 13"))

        val ref02 = TechnicalReferenceCatalog.ALL_REFERENCES.first { it.id == "REF-02" }
        assertTrue(ref02.authority.contains("29 June 2025"))
        assertTrue(ref02.summary.contains("Rule 3"))
        assertFalse(ref02.mandatoryRequirements.contains("Section 13 mandates"))

        val dailyBoilerChecklist = PlantChecklistCatalog.ALL_CHECKLISTS.first { it.id == "CHK-BLR-DAILY" }
        assertFalse(dailyBoilerChecklist.standardReference.contains("Rule 44"))
    }
}
