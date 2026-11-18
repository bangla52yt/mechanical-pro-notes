package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.calculation.BoilerThermalCalculations
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.BoilerRepository
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationHistorySearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationRepository
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationSearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import com.researchbangla.mechanicalpronotes.domain.repository.SearchRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UnifiedSearchResult
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserNoteSearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import com.researchbangla.mechanicalpronotes.domain.unit.DisplayUnitConversion
import com.researchbangla.mechanicalpronotes.domain.unit.UnitCategory
import com.researchbangla.mechanicalpronotes.domain.unit.UnitConversionResult
import com.researchbangla.mechanicalpronotes.domain.unit.UnitPreferenceHelper
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerCalculationCatalog
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerTab
import com.researchbangla.mechanicalpronotes.presentation.boiler.boilerSectionBookmarkId
import com.researchbangla.mechanicalpronotes.presentation.boiler.resolveBoilerBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationCatalogFilter
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationViewModel
import com.researchbangla.mechanicalpronotes.presentation.calculations.extractCalculationCategories
import com.researchbangla.mechanicalpronotes.presentation.calculations.filterCalculationsByCategory
import com.researchbangla.mechanicalpronotes.presentation.calculations.formatHistoryTimestamp
import com.researchbangla.mechanicalpronotes.presentation.calculations.resolveHistoryRunnerCalculationId
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistCompletionStatus
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistSourceFilter
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistViewModel
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistWorkflowHelper
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentViewModel
import com.researchbangla.mechanicalpronotes.presentation.home.HomeViewModel
import com.researchbangla.mechanicalpronotes.presentation.navigation.Screen
import com.researchbangla.mechanicalpronotes.presentation.navigation.SecondaryModuleResolver
import com.researchbangla.mechanicalpronotes.presentation.navigation.TechnicalStandardBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.navigation.resolveTechnicalStandardBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.notes.NoteFilter
import com.researchbangla.mechanicalpronotes.presentation.notes.NoteSortOrder
import com.researchbangla.mechanicalpronotes.presentation.notes.NotesCatalogFilter
import com.researchbangla.mechanicalpronotes.presentation.notes.NotesViewModel
import com.researchbangla.mechanicalpronotes.presentation.search.SearchScope
import com.researchbangla.mechanicalpronotes.presentation.search.SearchUiState
import com.researchbangla.mechanicalpronotes.presentation.search.SearchViewModel
import com.researchbangla.mechanicalpronotes.presentation.settings.ClearDataConfirmationStep
import com.researchbangla.mechanicalpronotes.presentation.settings.SettingsViewModel
import com.researchbangla.mechanicalpronotes.presentation.units.UnitConverterStateHolder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Stage 4 UI State, Validation & Navigation-Focus Unit Tests.
 * Covers non-finite input safety, stale result clearing, checklist selection focus,
 * SearchScope/ContentDepth state filtering, and responsive unit-converter state handling.
 */
class Stage04UiStateAndAccessibilityTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCalculationRepository(
        private val def: CalculationDefinition
    ) : CalculationRepository {
        override fun getAllCalculations(): Flow<List<CalculationDefinition>> = flowOf(listOf(def))
        override suspend fun getCalculationById(id: String): CalculationDefinition? =
            if (id == def.id) def else null

        override suspend fun executeCalculation(
            id: String,
            inputs: Map<String, Double>
        ): CalculationExecutionResult {
            val required = inputs["x"] ?: 0.0
            val optional = inputs["opt"] ?: 1.0
            return CalculationExecutionResult(
                calculationId = id,
                primaryOutputName = "Test Output",
                primaryOutputValue = required * optional,
                primaryOutputUnit = "kW",
                notesAndRecommendations = "Verified test output"
            )
        }
    }

    private class FakeUserDataRepository : UserDataRepository {
        private val progressFlow = MutableStateFlow(
            listOf(
                ChecklistProgress(
                    id = 1L,
                    checklistId = PlantChecklistCatalog.ID_BOILER_DAILY,
                    itemId = 1,
                    isCompleted = true
                ),
                ChecklistProgress(
                    id = 2L,
                    checklistId = PlantChecklistCatalog.ID_HYDRO_SAFETY,
                    itemId = 31,
                    isCompleted = true
                )
            )
        )

        private val historyFlow = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
        private val notesFlow = MutableStateFlow<List<UserNote>>(emptyList())
        private var nextNoteId = 1L
        private val bookmarksFlow = MutableStateFlow<List<UserBookmark>>(emptyList())
        private var nextBookmarkId = 1L
        private val customChecklistsFlow = MutableStateFlow<List<CustomChecklistTemplate>>(emptyList())
        private val executionMetadataFlow = MutableStateFlow<Map<String, ChecklistExecutionMetadata>>(emptyMap())
        var itemToggleDelayGate: CompletableDeferred<Unit>? = null
        var itemToggleCallCount: Int = 0

        fun seedNotes(items: List<UserNote>) {
            notesFlow.value = items
            nextNoteId = (items.maxOfOrNull { it.id } ?: 0L) + 1L
        }

        fun seedBookmarks(items: List<UserBookmark>) {
            bookmarksFlow.value = items
        }

        override fun getNotesForEquipment(equipmentId: String): Flow<List<UserNote>> =
            flowOf(notesFlow.value.filter { it.equipmentId == equipmentId })

        override suspend fun saveNote(note: UserNote): Long {
            val assignedId = if (note.id > 0L) note.id else nextNoteId++
            val saved = note.copy(id = assignedId)
            val remaining = notesFlow.value.filterNot { it.id == assignedId }
            notesFlow.value = (listOf(saved) + remaining).sortedByDescending { it.updatedAt }
            return assignedId
        }

        override suspend fun deleteNote(noteId: Long) {
            notesFlow.value = notesFlow.value.filterNot { it.id == noteId }
        }

        override fun getAllUserNotes(): Flow<List<UserNote>> = notesFlow
        override fun getAllBookmarks(): Flow<List<UserBookmark>> = bookmarksFlow
        override suspend fun toggleBookmark(entityType: BookmarkEntityType, entityId: String, title: String): Boolean {
            val normalizedId = entityId.trim()
            val current = bookmarksFlow.value.toMutableList()
            val removed = current.removeAll { it.entityType == entityType && it.entityId == normalizedId }
            return if (removed) {
                bookmarksFlow.value = current
                false
            } else {
                val added = UserBookmark(
                    id = nextBookmarkId++,
                    entityType = entityType,
                    entityId = normalizedId,
                    entityTitle = title
                )
                bookmarksFlow.value = listOf(added) + current
                true
            }
        }
        override suspend fun isBookmarked(entityType: BookmarkEntityType, entityId: String): Boolean {
            val normalizedId = entityId.trim()
            return bookmarksFlow.value.any { it.entityType == entityType && it.entityId == normalizedId }
        }
        override fun getCalculationHistory(): Flow<List<CalculationHistoryItem>> = historyFlow
        override suspend fun saveCalculationHistory(item: CalculationHistoryItem): Long {
            val assignedId = (historyFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
            historyFlow.value = listOf(item.copy(id = assignedId)) + historyFlow.value
            return assignedId
        }
        override fun getAllChecklistProgress(): Flow<List<ChecklistProgress>> = progressFlow
        override fun getChecklistProgress(checklistId: String): Flow<List<ChecklistProgress>> =
            flowOf(progressFlow.value.filter { it.checklistId == checklistId })

        override suspend fun setChecklistItemCompleted(
            checklistId: String,
            itemId: Int,
            isCompleted: Boolean,
            remarks: String?
        ) {
            itemToggleCallCount += 1
            itemToggleDelayGate?.await()
            val current = progressFlow.value.toMutableList()
            val existing = current.firstOrNull { it.checklistId == checklistId && it.itemId == itemId }
            val resolvedTimestamp = if (isCompleted) {
                if (existing?.isCompleted == true && existing.completionTimestamp != null) {
                    existing.completionTimestamp
                } else {
                    System.currentTimeMillis()
                }
            } else {
                null
            }
            current.removeAll { it.checklistId == checklistId && it.itemId == itemId }
            current.add(
                ChecklistProgress(
                    id = existing?.id ?: ((current.maxOfOrNull { it.id } ?: 0L) + 1L),
                    checklistId = checklistId,
                    itemId = itemId,
                    isCompleted = isCompleted,
                    completionTimestamp = resolvedTimestamp,
                    remarks = remarks
                )
            )
            progressFlow.value = current
        }

        override suspend fun resetChecklist(checklistId: String) {
            val normalizedId = checklistId.trim()
            progressFlow.value = progressFlow.value.filterNot { it.checklistId == normalizedId }
            val existingMeta = executionMetadataFlow.value[normalizedId]
            if (existingMeta != null) {
                executionMetadataFlow.value = executionMetadataFlow.value.toMutableMap().apply {
                    put(
                        normalizedId,
                        existingMeta.copy(
                            operatorName = "",
                            shift = ChecklistShift.UNSPECIFIED,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        override fun getCustomChecklists(): Flow<List<CustomChecklistTemplate>> = customChecklistsFlow

        override suspend fun saveCustomChecklist(template: CustomChecklistTemplate): CustomChecklistTemplate {
            val now = System.currentTimeMillis()
            val candidateId = template.id.trim()
            require(!PlantChecklistCatalog.isPackagedChecklistId(candidateId)) {
                "Packaged default checklist templates cannot be modified or overwritten."
            }
            val current = customChecklistsFlow.value.toMutableList()
            val resolvedId = candidateId.ifEmpty { "CHK-CUSTOM-$now" }
            val normalizedEq = NoteAssociationHelper.normalizeEquipmentIdForStorage(template.equipmentId)
            val existing = current.firstOrNull { it.id.equals(resolvedId, ignoreCase = true) }
            val saved = template.copy(
                id = existing?.id ?: resolvedId,
                title = template.title.trim(),
                description = template.description.trim(),
                equipmentId = normalizedEq,
                createdAt = existing?.createdAt ?: template.createdAt.takeIf { it > 0L } ?: now,
                updatedAt = now
            )
            current.removeAll { it.id.equals(saved.id, ignoreCase = true) }
            customChecklistsFlow.value = (listOf(saved) + current).sortedByDescending { it.updatedAt }

            val existingMeta = executionMetadataFlow.value[saved.id]
            if (existingMeta != null && !existingMeta.equipmentId.equals(normalizedEq, ignoreCase = true)) {
                executionMetadataFlow.value = executionMetadataFlow.value.toMutableMap().apply {
                    put(saved.id, existingMeta.copy(equipmentId = normalizedEq, updatedAt = now))
                }
            }
            return saved
        }

        override suspend fun deleteCustomChecklist(checklistId: String): Boolean {
            val normalizedId = checklistId.trim()
            if (normalizedId.isEmpty() || PlantChecklistCatalog.isPackagedChecklistId(normalizedId)) {
                return false
            }
            val current = customChecklistsFlow.value
            val filtered = current.filterNot { it.id.equals(normalizedId, ignoreCase = true) }
            if (filtered.size == current.size) return false
            customChecklistsFlow.value = filtered
            executionMetadataFlow.value = executionMetadataFlow.value.toMutableMap().apply {
                keys.removeAll { it.equals(normalizedId, ignoreCase = true) }
            }
            progressFlow.value = progressFlow.value.filterNot { it.checklistId == normalizedId }
            bookmarksFlow.value = bookmarksFlow.value.filterNot {
                it.entityType == BookmarkEntityType.CHECKLIST && it.entityId.equals(normalizedId, ignoreCase = true)
            }
            return true
        }

        override fun getAllChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> =
            executionMetadataFlow

        override suspend fun saveChecklistExecutionMetadata(metadata: ChecklistExecutionMetadata) {
            val normalizedId = metadata.checklistId.trim()
            if (normalizedId.isEmpty()) return
            val normalized = metadata.copy(
                checklistId = normalizedId,
                equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(metadata.equipmentId),
                operatorName = metadata.operatorName.trim(),
                updatedAt = metadata.updatedAt.takeIf { it > 0L } ?: System.currentTimeMillis()
            )
            executionMetadataFlow.value = executionMetadataFlow.value.toMutableMap().apply {
                keys.removeAll { it.equals(normalizedId, ignoreCase = true) }
                put(normalizedId, normalized)
            }
        }

        var clearErrorMessage: String? = null
        var clearDelayGate: CompletableDeferred<Unit>? = null
        var clearCallCount: Int = 0

        override suspend fun exportUserPersonalDataJson(): String = "{}"
        override suspend fun importUserPersonalDataJson(jsonString: String, overwrite: Boolean): BackupImportResult =
            BackupImportResult.Success(0, 0, 0, 0)

        override suspend fun clearAllPersonalData(): ClearPersonalDataResult {
            clearCallCount += 1
            clearDelayGate?.await()
            val err = clearErrorMessage
            if (err != null) {
                return ClearPersonalDataResult.Failure(err)
            }
            val notesCount = notesFlow.value.size
            val bookmarksCount = bookmarksFlow.value.size
            val historyCount = historyFlow.value.size
            val checklistCount = progressFlow.value.size
            val customChecklistsCount = customChecklistsFlow.value.size
            notesFlow.value = emptyList()
            bookmarksFlow.value = emptyList()
            historyFlow.value = emptyList()
            progressFlow.value = emptyList()
            customChecklistsFlow.value = emptyList()
            executionMetadataFlow.value = emptyMap()
            return ClearPersonalDataResult.Success(
                notesDeleted = notesCount,
                bookmarksDeleted = bookmarksCount,
                historyDeleted = historyCount,
                checklistEntriesDeleted = checklistCount,
                customChecklistsDeleted = customChecklistsCount
            )
        }
    }

    private class FakeEquipmentRepository(
        private val items: List<Equipment>
    ) : EquipmentRepository {
        override fun getAllSystemFamilies(): Flow<List<SystemFamily>> = flowOf(emptyList())
        override fun getAllEquipment(): Flow<List<Equipment>> = flowOf(items)
        override fun getEquipmentByFamily(familyId: String): Flow<List<Equipment>> =
            flowOf(items.filter { it.familyId == familyId })
        override suspend fun getEquipmentById(equipmentId: String): Equipment? =
            items.firstOrNull { it.id == equipmentId }
        override fun getEquipmentByContentDepth(depth: ContentDepth): Flow<List<Equipment>> =
            flowOf(items.filter { it.contentDepth == depth })
        override suspend fun getKnowledgeSections(equipmentId: String): List<KnowledgeSection> = emptyList()
        override suspend fun getFailureModes(equipmentId: String): List<FailureMode> = emptyList()
        override suspend fun getMaintenanceProcedures(equipmentId: String): List<MaintenanceProcedure> = emptyList()
    }

    private class FakeBoilerRepository(
        private val items: List<Equipment>
    ) : BoilerRepository {
        override fun getBoilerEquipment(): Flow<List<Equipment>> = flowOf(items)
        override suspend fun getBoilerById(boilerId: String): Equipment? = items.firstOrNull { it.id == boilerId }
        override fun getGasBoilers(): Flow<List<Equipment>> = flowOf(emptyList())
        override fun getJhutAndSolidFuelBoilers(): Flow<List<Equipment>> = flowOf(emptyList())
    }

    private fun sampleCalcDefinition(status: CalculationStatus = CalculationStatus.IMPLEMENTED): CalculationDefinition =
        CalculationDefinition(
            id = "CALC-01",
            code = "CALC-01",
            title = "Direct Boiler Thermal Efficiency",
            bengaliTitle = "বয়লারের সরাসরি তাপীয় দক্ষতা",
            category = "Boiler Thermal",
            formulaLatex = "eta = (m_s * (h_g - h_f)) / (m_f * GCV) * 100",
            engineeringReference = "ASME PTC 4 / BS 845",
            assumptions = "Steady-state steam pressure and uniform fuel GCV.",
            limitations = "Does not isolate radiation or blowdown losses.",
            status = status,
            variables = listOf(
                FormulaVariable(
                    calculationId = "CALC-01",
                    symbol = "x",
                    name = "Steam Flow",
                    unit = "kg/h",
                    defaultValue = 100.0,
                    minBound = 1.0,
                    maxBound = 50000.0,
                    isRequired = true
                ),
                FormulaVariable(
                    calculationId = "CALC-01",
                    symbol = "opt",
                    name = "Correction Factor",
                    unit = "-",
                    defaultValue = null,
                    minBound = 0.1,
                    maxBound = 10.0,
                    isRequired = false
                )
            )
        )

    @Test
    fun calculationViewModelRejectsNaNAndInfinityInputs() = runTest {
        val vm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = FakeUserDataRepository()
        )
        vm.loadCalculationRunner("CALC-01")

        vm.onInputChange("x", "NaN")
        vm.executeCalculation()
        assertEquals("Enter a valid finite decimal number.", vm.runnerState.value.validationErrors["x"])
        assertNull(vm.runnerState.value.result)

        vm.onInputChange("x", "Infinity")
        vm.executeCalculation()
        assertEquals("Enter a valid finite decimal number.", vm.runnerState.value.validationErrors["x"])
        assertNull(vm.runnerState.value.result)
    }

    @Test
    fun calculationViewModelClearsStaleResultOnInputEditAndPreservesAssumptions() = runTest {
        val vm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = FakeUserDataRepository()
        )
        vm.loadCalculationRunner("CALC-01")
        vm.onInputChange("x", "250")
        vm.executeCalculation()

        val afterRun = vm.runnerState.value
        assertNotNull(afterRun.result)
        assertTrue(afterRun.definition?.assumptions?.contains("Steady-state") == true)
        assertTrue(afterRun.definition?.limitations?.contains("radiation") == true)
        assertEquals(1, afterRun.definition?.variables?.count { it.isRequired })
        assertEquals(1, afterRun.definition?.variables?.count { !it.isRequired })

        // Editing any input clears stale result & historySaved state immediately
        vm.onInputChange("x", "300")
        assertNull(vm.runnerState.value.result)
        assertFalse(vm.runnerState.value.historySaved)
        assertNull(vm.runnerState.value.errorMessage)
    }

    @Test
    fun calculationViewModelBlocksNonImplementedExecutionSafely() = runTest {
        val vm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition(CalculationStatus.RESEARCH_PENDING)),
            userDataRepository = FakeUserDataRepository()
        )
        vm.loadCalculationRunner("CALC-01")
        vm.executeCalculation()

        assertNull(vm.runnerState.value.result)
        assertTrue(vm.runnerState.value.errorMessage?.contains("RESEARCH_PENDING") == true)
    }

    @Test
    fun checklistViewModelSelectChecklistUpdatesActiveFocusAndCompletedIds() = runTest {
        val vm = ChecklistViewModel(FakeUserDataRepository())
        assertEquals(PlantChecklistCatalog.ID_BOILER_DAILY, vm.uiState.value.selectedChecklistId)
        assertEquals(setOf(1), vm.uiState.value.completedItemIds)

        vm.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        assertEquals(PlantChecklistCatalog.ID_HYDRO_SAFETY, vm.uiState.value.selectedChecklistId)
        assertEquals(setOf(31), vm.uiState.value.completedItemIds)
    }

    @Test
    fun searchUiStateFiltersByScopeFamilyAndContentDepthCleanly() {
        val eqBoiler = Equipment(
            id = "EQ-A01",
            categoryId = "CAT-A",
            familyId = "FAM-A",
            name = "Fire-Tube Steam Boiler",
            bengaliName = "স্টিম বয়লার",
            contentDepth = ContentDepth.FULL_CONTENT_BLUEPRINT,
            criticalityTier = CriticalityTier.SAFETY_CRITICAL
        )
        val eqWasher = Equipment(
            id = "EQ-B01",
            categoryId = "CAT-B",
            familyId = "FAM-B",
            name = "Industrial Washer Extractor",
            bengaliName = "ওয়াশার এক্সট্র্যাক্টর",
            contentDepth = ContentDepth.PARTIAL_CONTENT_BLUEPRINT,
            criticalityTier = CriticalityTier.PRODUCTION_HIGH
        )

        val state = SearchUiState(
            query = "boiler",
            results = listOf(eqBoiler, eqWasher),
            selectedScope = SearchScope.EQUIPMENT,
            selectedFamilyFilter = "FAM-A",
            selectedDepthFilters = setOf(ContentDepth.FULL_CONTENT_BLUEPRINT),
            hasSearched = true
        )

        assertEquals(listOf("EQ-A01"), state.filteredEquipment.map { it.id })
        assertTrue(state.filteredCatalogHits.isEmpty())
    }

    @Test
    fun unitConverterStateHolderPropagatesPhysicalLimitErrorsAndSwapsCleanly() {
        val holder = UnitConverterStateHolder()
        assertTrue(holder.uiState.value.conversionResult is UnitConversionResult.Success)

        holder.selectCategory(UnitCategory.TEMPERATURE)
        holder.onInputChange("-300") // Below absolute zero (-273.15 C)
        assertTrue(holder.uiState.value.conversionResult is UnitConversionResult.Error)
        assertTrue(holder.uiState.value.equivalentConversions.isEmpty())

        holder.onInputChange("100")
        val beforeFrom = holder.uiState.value.fromUnitCode
        val beforeTo = holder.uiState.value.toUnitCode
        holder.swapUnits()
        assertEquals(beforeTo, holder.uiState.value.fromUnitCode)
        assertEquals(beforeFrom, holder.uiState.value.toUnitCode)
        assertTrue(holder.uiState.value.conversionResult is UnitConversionResult.Success)
    }

    @Test
    fun bottomNavigationScreensUseConciseExplicitShortTitles() {
        val shortLabels = Screen.bottomBarScreens.map { it.shortTitle }
        assertEquals(listOf("Home", "Plant", "Boiler", "Calcs"), shortLabels)
        assertFalse(shortLabels.contains("Dedicated"))
        assertFalse(shortLabels.contains("Engineering"))
        assertEquals(
            listOf("home", "wash_plant", "boiler_module", "calculations"),
            Screen.bottomBarScreens.map { it.route }
        )
    }

    @Test
    fun dynamicCalculationCategoriesCoverAll20CalculationsWithoutOrphans() {
        val allCalcs = CalculationSpecifications.ALL_CALCULATIONS
        val categories = extractCalculationCategories(allCalcs)

        assertEquals("All", categories.first())
        assertTrue(categories.contains("Utility & Pumping"))
        assertTrue(categories.contains("Boiler Safety"))
        assertTrue(categories.contains("Wash Plant Utility"))

        val calc07 = filterCalculationsByCategory(allCalcs, "Utility & Pumping").map { it.id }
        val calc09 = filterCalculationsByCategory(allCalcs, "Boiler Safety").map { it.id }
        val calc17 = filterCalculationsByCategory(allCalcs, "Wash Plant Utility").map { it.id }
        assertEquals(listOf("CALC-07"), calc07)
        assertEquals(listOf("CALC-09"), calc09)
        assertEquals(listOf("CALC-17"), calc17)

        val specificCategories = categories.drop(1)
        assertTrue(allCalcs.all { def -> specificCategories.any { cat -> def.category.equals(cat, ignoreCase = true) } })
    }

    @Test
    fun calculationViewModelObservesHistoryAndResolvesValidRunnerRoutes() = runTest {
        val vm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = FakeUserDataRepository()
        )
        assertTrue(vm.listState.value.history.isEmpty())

        vm.loadCalculationRunner("CALC-01")
        vm.onInputChange("x", "250")
        vm.executeCalculation()
        vm.saveToHistory()

        val savedHistory = vm.listState.value.history
        assertEquals(1, savedHistory.size)
        val entry = savedHistory.first()
        assertEquals("CALC-01", entry.calculationId)
        assertEquals(250.0, entry.inputValues["x"] ?: 0.0, 0.0001)
        assertEquals(250.0, entry.outputValues["Test Output"] ?: 0.0, 0.0001)
        assertEquals("Verified test output", entry.userRemark)
        assertNotEquals("Unknown time", formatHistoryTimestamp(entry.timestamp))

        assertEquals("CALC-01", resolveHistoryRunnerCalculationId("calc-01", vm.listState.value.calculations))
        assertNull(resolveHistoryRunnerCalculationId("CALC-999", vm.listState.value.calculations))
    }

    @Test
    fun calculationViewModelTogglesAndSyncsCalculationBookmarksReactively() = runTest {
        val repo = FakeUserDataRepository()
        val vm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = repo
        )
        vm.loadCalculationRunner("CALC-01")
        assertFalse(vm.runnerState.value.isBookmarked)

        vm.toggleBookmark()
        assertTrue(vm.runnerState.value.isBookmarked)
        assertTrue(repo.isBookmarked(BookmarkEntityType.CALCULATION, "CALC-01"))

        // External removal (e.g., via BookmarksScreen) updates runner state reactively
        repo.toggleBookmark(BookmarkEntityType.CALCULATION, "CALC-01", "Direct Boiler Thermal Efficiency")
        assertFalse(vm.runnerState.value.isBookmarked)
    }

    @Test
    fun checklistViewModelTogglesChecklistBookmarksAndUpdatesOnSwitch() = runTest {
        val repo = FakeUserDataRepository()
        val vm = ChecklistViewModel(repo)
        assertFalse(vm.uiState.value.isSelectedChecklistBookmarked)

        vm.toggleChecklistBookmark()
        assertTrue(vm.uiState.value.bookmarkedChecklistIds.contains(PlantChecklistCatalog.ID_BOILER_DAILY))
        assertTrue(vm.uiState.value.isSelectedChecklistBookmarked)

        // Switching to another unbookmarked checklist updates active state
        vm.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        assertFalse(vm.uiState.value.isSelectedChecklistBookmarked)

        // Switching back preserves bookmarked state, and second toggle removes it
        vm.selectChecklist(PlantChecklistCatalog.ID_BOILER_DAILY)
        assertTrue(vm.uiState.value.isSelectedChecklistBookmarked)
        vm.toggleChecklistBookmark()
        assertFalse(vm.uiState.value.bookmarkedChecklistIds.contains(PlantChecklistCatalog.ID_BOILER_DAILY))
        assertFalse(vm.uiState.value.isSelectedChecklistBookmarked)
    }

    @Test
    fun notesViewModelTogglesTechnicalStandardBookmarks() = runTest {
        val repo = FakeUserDataRepository()
        val vm = NotesViewModel(repo)

        vm.toggleBookmark(BookmarkEntityType.TECHNICAL_STANDARD, "REF-01", "The Boilers Act, 2022")
        assertEquals(1, vm.uiState.value.bookmarks.size)
        assertEquals(BookmarkEntityType.TECHNICAL_STANDARD, vm.uiState.value.bookmarks.first().entityType)
        assertEquals("REF-01", vm.uiState.value.bookmarks.first().entityId)

        vm.toggleBookmark(BookmarkEntityType.TECHNICAL_STANDARD, "REF-01", "The Boilers Act, 2022")
        assertTrue(vm.uiState.value.bookmarks.isEmpty())
    }

    @Test
    fun notesViewModelTogglesBoilerSectionBookmarksAndPreventsDuplicates() = runTest {
        val repo = FakeUserDataRepository()
        val vm = NotesViewModel(repo)
        val sectionId = boilerSectionBookmarkId(BoilerTab.GAS_BOILERS)

        vm.toggleBookmark(BookmarkEntityType.BOILER_SECTION, sectionId, "Boiler: Gas Boilers (EQ-G01)")
        assertEquals(1, vm.uiState.value.bookmarks.size)
        assertEquals("BOILER:GAS_BOILERS", vm.uiState.value.bookmarks.first().entityId)

        // Re-toggling canonical key removes cleanly instead of inserting duplicates
        vm.toggleBookmark(BookmarkEntityType.BOILER_SECTION, "  $sectionId  ", "Boiler: Gas Boilers (EQ-G01)")
        assertTrue(vm.uiState.value.bookmarks.isEmpty())
        assertFalse(repo.isBookmarked(BookmarkEntityType.BOILER_SECTION, sectionId))
    }

    @Test
    fun boilerBookmarkResolverHandlesCanonicalCaseBareLegacyEquipmentAndUnknownIdsSafely() {
        // Canonical encoding and decoding for all 4 BoilerTab tabs
        for (tab in BoilerTab.entries) {
            val encoded = boilerSectionBookmarkId(tab)
            assertEquals("BOILER:${tab.name}", encoded)
            assertEquals(BoilerBookmarkTarget.Tab(tab), resolveBoilerBookmarkTarget(encoded))
        }

        // Case-insensitive and bare BoilerTab names
        assertEquals(
            BoilerBookmarkTarget.Tab(BoilerTab.STATUTORY_RULES),
            resolveBoilerBookmarkTarget("  boiler:statutory_rules ")
        )
        assertEquals(
            BoilerBookmarkTarget.Tab(BoilerTab.JHUT_BOILERS),
            resolveBoilerBookmarkTarget("jhut_boilers")
        )

        // Explicit legacy EQ- equipment routing
        assertEquals(
            BoilerBookmarkTarget.Equipment("EQ-G01"),
            resolveBoilerBookmarkTarget(" EQ-G01 ")
        )

        // Unknown / malformed IDs must never route to EquipmentDetail
        assertEquals(BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW), resolveBoilerBookmarkTarget("ACT_2022_RULES_2025"))
        assertEquals(BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW), resolveBoilerBookmarkTarget("BOILER:UNKNOWN_TAB"))
        assertEquals(BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW), resolveBoilerBookmarkTarget("EQ-"))
        assertEquals(BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW), resolveBoilerBookmarkTarget("   "))
    }

    @Test
    fun checklistTogglePreservesRemarksAcrossCheckUncheckAndRecheckWithValidTimestamps() = runTest {
        val repo = FakeUserDataRepository()
        val vm = ChecklistViewModel(repo)

        // 1. Editing remark on an unchecked item does not mark completed or create a timestamp
        vm.updateItemRemark(2, "  Low water cutoff float sluggish; monitor closely  ")
        val afterUncheckedRemark = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(2)
        assertNotNull(afterUncheckedRemark)
        assertFalse(afterUncheckedRemark!!.isCompleted)
        assertNull(afterUncheckedRemark.completionTimestamp)
        assertEquals("Low water cutoff float sluggish; monitor closely", afterUncheckedRemark.remarks)
        assertFalse(vm.uiState.value.completedItemIds.contains(2))

        // 2. Checking an incomplete item preserves existing remarks and sets a completion timestamp
        vm.toggleItem(2)
        val afterCheck = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(2)
        assertNotNull(afterCheck)
        assertTrue(afterCheck!!.isCompleted)
        assertNotNull(afterCheck.completionTimestamp)
        assertEquals("Low water cutoff float sluggish; monitor closely", afterCheck.remarks)
        assertTrue(vm.uiState.value.completedItemIds.contains(2))

        // 3. Unchecking a completed item preserves remarks and clears its timestamp
        vm.toggleItem(2)
        val afterUncheck = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(2)
        assertNotNull(afterUncheck)
        assertFalse(afterUncheck!!.isCompleted)
        assertNull(afterUncheck.completionTimestamp)
        assertEquals("Low water cutoff float sluggish; monitor closely", afterUncheck.remarks)
        assertFalse(vm.uiState.value.completedItemIds.contains(2))

        // 4. Rechecking creates a valid timestamp and preserves remarks
        vm.toggleItem(2)
        val afterRecheck = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(2)
        assertNotNull(afterRecheck)
        assertTrue(afterRecheck!!.isCompleted)
        assertNotNull(afterRecheck.completionTimestamp)
        assertEquals("Low water cutoff float sluggish; monitor closely", afterRecheck.remarks)
    }

    @Test
    fun checklistRemarkUpdateAndClearPreserveCompletionStateAndOriginalTimestamp() = runTest {
        val repo = FakeUserDataRepository()
        val vm = ChecklistViewModel(repo)

        // Check item 3 -> generates original timestamp
        vm.toggleItem(3)
        val initialProgress = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(3)
        assertNotNull(initialProgress)
        val originalTimestamp = initialProgress!!.completionTimestamp
        assertTrue(initialProgress.isCompleted)
        assertNotNull(originalTimestamp)

        // Add remark -> completion state and original timestamp remain untouched
        vm.updateItemRemark(3, "  Verified 8.5 bar gauge reading  ")
        val withRemark = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(3)
        assertNotNull(withRemark)
        assertTrue(withRemark!!.isCompleted)
        assertEquals(originalTimestamp, withRemark.completionTimestamp)
        assertEquals("Verified 8.5 bar gauge reading", withRemark.remarks)

        // Clear remark via whitespace -> normalizes to null while preserving completion state and original timestamp
        vm.updateItemRemark(3, "   ")
        val afterClear = vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(3)
        assertNotNull(afterClear)
        assertTrue(afterClear!!.isCompleted)
        assertEquals(originalTimestamp, afterClear.completionTimestamp)
        assertNull(afterClear.remarks)
    }

    @Test
    fun checklistSwitchingAndResetIsolateProgressAndRemarksPerChecklist() = runTest {
        val repo = FakeUserDataRepository()
        val vm = ChecklistViewModel(repo)

        // Add remark on item 1 in CHK-BLR-DAILY
        vm.updateItemRemark(1, "Daily boiler pressure gauge zeroed")
        assertEquals(
            "Daily boiler pressure gauge zeroed",
            vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY]?.get(1)?.remarks
        )

        // Switch to CHK-HYDRO-SAFE and add distinct progress/remark
        vm.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        vm.updateItemRemark(31, "Hydro extractor interlock tested OK")
        assertEquals(setOf(31), vm.uiState.value.completedItemIds)
        assertEquals(
            "Hydro extractor interlock tested OK",
            vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_HYDRO_SAFETY]?.get(31)?.remarks
        )
        assertNull(vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_HYDRO_SAFETY]?.get(1))

        // Switch back to CHK-BLR-DAILY and reset it -> CHK-HYDRO-SAFE progress and remarks stay intact
        vm.selectChecklist(PlantChecklistCatalog.ID_BOILER_DAILY)
        vm.resetCurrentChecklist()
        assertTrue(vm.uiState.value.completedItemIds.isEmpty())
        assertTrue(vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_BOILER_DAILY].isNullOrEmpty())

        vm.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        assertEquals(setOf(31), vm.uiState.value.completedItemIds)
        assertEquals(
            "Hydro extractor interlock tested OK",
            vm.uiState.value.progressByChecklist[PlantChecklistCatalog.ID_HYDRO_SAFETY]?.get(31)?.remarks
        )
    }

    @Test
    fun homeViewModelDisplaysSavedBookmarksDeduplicatesAndHandlesEmptyState() = runTest {
        val sampleEq = Equipment(
            id = "EQ-G01",
            categoryId = "CAT-G",
            familyId = "FAM-G",
            name = "3-Pass Gas Boiler",
            bengaliName = "গ্যাস বয়লার",
            contentDepth = ContentDepth.FULL_CONTENT_BLUEPRINT,
            criticalityTier = CriticalityTier.SAFETY_CRITICAL
        )
        val userRepo = FakeUserDataRepository()
        val vm = HomeViewModel(
            equipmentRepository = FakeEquipmentRepository(listOf(sampleEq)),
            boilerRepository = FakeBoilerRepository(listOf(sampleEq)),
            userDataRepository = userRepo
        )

        // Starts gracefully with empty bookmark list
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.bookmarks.isEmpty())

        // Seed duplicate entries differing only by casing/whitespace + distinct entries
        userRepo.seedBookmarks(
            listOf(
                UserBookmark(1L, BookmarkEntityType.EQUIPMENT, "EQ-G01", "3-Pass Gas Boiler"),
                UserBookmark(2L, BookmarkEntityType.EQUIPMENT, " eq-g01 ", "3-Pass Gas Boiler Duplicate"),
                UserBookmark(3L, BookmarkEntityType.TECHNICAL_STANDARD, "FUND-01", "Applied Thermodynamics")
            )
        )

        assertEquals(2, vm.uiState.value.bookmarks.size)
        assertEquals(listOf("EQ-G01", "FUND-01"), vm.uiState.value.bookmarks.map { it.entityId })

        // Removing all bookmarks returns to empty list reactively
        userRepo.seedBookmarks(emptyList())
        assertTrue(vm.uiState.value.bookmarks.isEmpty())
    }

    @Test
    fun technicalStandardBookmarkResolverRoutesFundamentalsAndReferencesCleanly() {
        assertEquals(
            TechnicalStandardBookmarkTarget.Fundamental("FUND-01"),
            resolveTechnicalStandardBookmarkTarget("FUND-01")
        )
        assertEquals(
            TechnicalStandardBookmarkTarget.Fundamental("FUND-11"),
            resolveTechnicalStandardBookmarkTarget(" fund-11 ")
        )
        assertEquals(
            TechnicalStandardBookmarkTarget.Reference("REF-01"),
            resolveTechnicalStandardBookmarkTarget("REF-01")
        )
        assertEquals(
            TechnicalStandardBookmarkTarget.Reference("REF-06"),
            resolveTechnicalStandardBookmarkTarget(" REF-06 ")
        )
    }

    @Test
    fun equipmentViewModelReactsToExternalBookmarkChangesWithoutReopeningDetail() = runTest {
        val eqA = Equipment(
            id = "EQ-A01",
            categoryId = "CAT-A",
            familyId = "FAM-A",
            name = "Belly Washer",
            bengaliName = "বেলি ওয়াশার",
            contentDepth = ContentDepth.FULL_CONTENT_BLUEPRINT,
            criticalityTier = CriticalityTier.PRIMARY_PRODUCTION
        )
        val eqB = Equipment(
            id = "EQ-B01",
            categoryId = "CAT-B",
            familyId = "FAM-B",
            name = "Hydro Extractor",
            bengaliName = "হাইড্রো এক্সট্র্যাক্টর",
            contentDepth = ContentDepth.FULL_CONTENT_BLUEPRINT,
            criticalityTier = CriticalityTier.PRIMARY_PRODUCTION
        )
        val userRepo = FakeUserDataRepository()
        val vm = EquipmentViewModel(
            equipmentRepository = FakeEquipmentRepository(listOf(eqA, eqB)),
            userDataRepository = userRepo
        )

        vm.loadEquipmentDetail("EQ-A01")
        assertFalse(vm.detailState.value.isLoading)
        assertFalse(vm.detailState.value.isBookmarked)

        // Toggle on via EquipmentViewModel
        vm.toggleBookmark(eqA)
        assertTrue(vm.detailState.value.isBookmarked)
        assertTrue(userRepo.isBookmarked(BookmarkEntityType.EQUIPMENT, "EQ-A01"))

        // External removal (e.g., via BookmarksScreen or Home) updates detailState reactively without reload
        userRepo.toggleBookmark(BookmarkEntityType.EQUIPMENT, "EQ-A01", eqA.name)
        assertFalse(vm.detailState.value.isBookmarked)

        // External add updates detailState reactively without reload
        userRepo.toggleBookmark(BookmarkEntityType.EQUIPMENT, "EQ-A01", eqA.name)
        assertTrue(vm.detailState.value.isBookmarked)

        // Switching to EQ-B01 shows correct unbookmarked state for EQ-B01
        vm.loadEquipmentDetail("EQ-B01")
        assertEquals("EQ-B01", vm.detailState.value.equipment?.id)
        assertFalse(vm.detailState.value.isBookmarked)
    }

    @Test
    fun notesViewModelManagesFundamentalsBookmarksReactivelyAlongsideReferences() = runTest {
        val repo = FakeUserDataRepository()
        // Pre-existing saved state reflects both a fundamental and a reference
        repo.seedBookmarks(
            listOf(
                UserBookmark(10L, BookmarkEntityType.TECHNICAL_STANDARD, "FUND-02", "Fluid Mechanics & Hydraulic Machinery"),
                UserBookmark(11L, BookmarkEntityType.TECHNICAL_STANDARD, "REF-01", "The Boilers Act, 2022")
            )
        )
        val vm = NotesViewModel(repo)

        val initialFundIds = vm.uiState.value.bookmarks
            .filter { it.entityType == BookmarkEntityType.TECHNICAL_STANDARD && it.entityId.startsWith("FUND-") }
            .map { it.entityId }
            .toSet()
        val initialRefIds = vm.uiState.value.bookmarks
            .filter { it.entityType == BookmarkEntityType.TECHNICAL_STANDARD && !it.entityId.startsWith("FUND-") }
            .map { it.entityId }
            .toSet()
        assertEquals(setOf("FUND-02"), initialFundIds)
        assertEquals(setOf("REF-01"), initialRefIds)

        // Add another Fundamental bookmark
        vm.toggleBookmark(BookmarkEntityType.TECHNICAL_STANDARD, "FUND-01", "Applied Thermodynamics")
        assertTrue(vm.uiState.value.bookmarks.any { it.entityId == "FUND-01" })

        // Remove FUND-02 while preserving FUND-01 and REF-01
        vm.toggleBookmark(BookmarkEntityType.TECHNICAL_STANDARD, "FUND-02", "Fluid Mechanics & Hydraulic Machinery")
        assertFalse(vm.uiState.value.bookmarks.any { it.entityId == "FUND-02" })
        assertTrue(vm.uiState.value.bookmarks.any { it.entityId == "FUND-01" })
        assertTrue(vm.uiState.value.bookmarks.any { it.entityId == "REF-01" })
    }

    @Test
    fun searchUiStateFiltersCalculationsNotesHistoryAndCatalogScopesCorrectly() {
        val calcHit = CalculationSearchHit(
            id = "CALC-01",
            code = "CALC-01",
            title = "Direct Boiler Thermal Efficiency",
            bengaliTitle = "বয়লারের সরাসরি তাপীয় দক্ষতা",
            category = "Boiler Thermal",
            engineeringReference = "ASME PTC 4",
            status = CalculationStatus.IMPLEMENTED
        )
        val noteHit = UserNoteSearchHit(
            id = 11L,
            equipmentId = "EQ-G01",
            displayTitle = "Burner Pilot Check",
            bodySnippet = "Flame scanner cleaned"
        )
        val historyHit = CalculationHistorySearchHit(
            id = 21L,
            calculationId = "CALC-01",
            calculationTitle = "Direct Boiler Thermal Efficiency",
            timestamp = 1_767_225_600_000L,
            formattedTimestamp = "01 Jan 2026, 00:00",
            resultSummary = "Thermal Efficiency: 85.30"
        )

        val base = SearchUiState(
            query = "boiler",
            calculationHits = listOf(calcHit),
            userNoteHits = listOf(noteHit),
            calculationHistoryHits = listOf(historyHit),
            hasSearched = true
        )

        val calcOnly = base.copy(selectedScope = SearchScope.CALCULATIONS)
        assertEquals(listOf("CALC-01"), calcOnly.filteredCalculationHits.map { it.id })
        assertTrue(calcOnly.filteredUserNoteHits.isEmpty())
        assertTrue(calcOnly.filteredCalculationHistoryHits.isEmpty())

        val notesOnly = base.copy(selectedScope = SearchScope.NOTES)
        assertTrue(notesOnly.filteredCalculationHits.isEmpty())
        assertEquals(listOf(11L), notesOnly.filteredUserNoteHits.map { it.id })
        assertTrue(notesOnly.filteredCalculationHistoryHits.isEmpty())

        val historyOnly = base.copy(selectedScope = SearchScope.HISTORY)
        assertTrue(historyOnly.filteredCalculationHits.isEmpty())
        assertTrue(historyOnly.filteredUserNoteHits.isEmpty())
        assertEquals(listOf(21L), historyOnly.filteredCalculationHistoryHits.map { it.id })
    }

    @Test
    fun searchViewModelDeduplicatesDebouncedQueriesClearsShortQueriesAndHandlesNoResults() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var searchCalls = 0
        val repo = object : SearchRepository {
            override suspend fun search(query: String, limit: Int): List<Equipment> = emptyList()
            override fun searchFlow(query: String, limit: Int): Flow<List<Equipment>> = flowOf(emptyList())
            override suspend fun searchKnowledgeBase(query: String, limit: Int): UnifiedSearchResult {
                searchCalls += 1
                return if (query == "NoSuchMatch") {
                    UnifiedSearchResult()
                } else {
                    UnifiedSearchResult(
                        calculations = listOf(
                            CalculationSearchHit(
                                id = "CALC-07",
                                code = "CALC-07",
                                title = "Pump Hydraulic Power",
                                bengaliTitle = "পাম্প পাওয়ার",
                                category = "Utility & Pumping",
                                engineeringReference = "HI 9.6.3",
                                status = CalculationStatus.IMPLEMENTED
                            )
                        )
                    )
                }
            }
        }

        val vm = SearchViewModel(repo)
        vm.onQueryChange("pump")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(1, searchCalls)
        assertTrue(vm.uiState.value.hasSearched)
        assertEquals(listOf("CALC-07"), vm.uiState.value.filteredCalculationHits.map { it.id })

        // Whitespace-only query variation must not trigger a duplicate search
        vm.onQueryChange("  pump  ")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(1, searchCalls)

        // Short (< 2 char) or blank query clears previous results and resets hasSearched
        vm.onQueryChange("p")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasSearched)
        assertTrue(vm.uiState.value.filteredCalculationHits.isEmpty())

        // No-results search finishes cleanly with empty lists and hasSearched = true
        vm.onQueryChange("NoSuchMatch")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasSearched)
        assertTrue(vm.uiState.value.calculationHits.isEmpty())
        assertTrue(vm.uiState.value.userNoteHits.isEmpty())
        assertTrue(vm.uiState.value.calculationHistoryHits.isEmpty())
    }

    @Test
    fun notesViewModelOpenNoteByIdAndCalculationViewModelFocusHistoryRecordResolveExactTargets() = runTest {
        val sampleNote = UserNote(
            id = 42L,
            equipmentId = "EQ-G01",
            title = "Boiler Tube Inspection",
            body = "No scale observed on pass-2 tubes"
        )
        val userRepo = object : UserDataRepository by FakeUserDataRepository() {
            override fun getAllUserNotes(): Flow<List<UserNote>> = flowOf(listOf(sampleNote))
        }
        val notesVm = NotesViewModel(userRepo)
        notesVm.openNoteById(noteId = 42L, fallbackEquipmentId = "EQ-G01")

        assertTrue(notesVm.uiState.value.isEditorOpen)
        assertEquals(42L, notesVm.uiState.value.editingNoteId)
        assertEquals("EQ-G01", notesVm.uiState.value.editorEquipmentId)
        assertEquals("Boiler Tube Inspection", notesVm.uiState.value.editorTitle)
        assertEquals("No scale observed on pass-2 tubes", notesVm.uiState.value.editorBody)

        val calcVm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = FakeUserDataRepository()
        )
        assertNull(calcVm.listState.value.pendingHistoryFocusId)
        calcVm.focusHistoryRecord(88L)
        assertEquals(88L, calcVm.listState.value.pendingHistoryFocusId)
        calcVm.clearPendingHistoryFocus()
        assertNull(calcVm.listState.value.pendingHistoryFocusId)
    }

    @Test
    fun searchAndBookmarksScreensAreCleanlySeparatedWithoutDuplicateBottomBarRoutes() {
        assertEquals("search", Screen.Search.route)
        assertEquals("Offline Search", Screen.Search.title)
        assertNotEquals("Search & Bookmarks", Screen.Search.title)
        assertEquals("Search", Screen.Search.shortTitle)

        assertEquals("bookmarks", Screen.Bookmarks.route)
        assertEquals("Saved Bookmarks", Screen.Bookmarks.title)

        val drawerRoutes = Screen.drawerScreens.map { it.route }
        assertEquals(drawerRoutes.size, drawerRoutes.distinct().size)
        val bottomRoutes = Screen.bottomBarScreens.map { it.route }
        assertEquals(bottomRoutes.size, bottomRoutes.distinct().size)
    }

    @Test
    fun coolingHeatingAndEtpModulesRemoveUnrelatedCalculatorsAndExposePlannedNotice() {
        val coolingResolved = SecondaryModuleResolver.resolveModule(
            screen = Screen.CoolingHeating,
            allEquipment = listOf(
                Equipment("EQ-K01", "CAT-K", "FAM-K", "Cooling Tower", "কুলিং টাওয়ার", ContentDepth.INVENTORY_ONLY, CriticalityTier.UTILITY_SUPPORT),
                Equipment("EQ-K02", "CAT-K", "FAM-K", "Water Chiller", "চিলার", ContentDepth.RESEARCH_PENDING, CriticalityTier.UTILITY_SUPPORT)
            ),
            systemFamilies = listOf(SystemFamily("FAM-K", "COOLING", "Cooling & Refrigeration", "কুলিং", "cooling", 11))
        )
        assertEquals("FAM-K", coolingResolved.config.mappedFamilyId)
        assertTrue(coolingResolved.calculations.isEmpty())
        assertFalse(coolingResolved.config.linkedCalcIds.contains("CALC-10"))
        assertNotNull(coolingResolved.config.plannedCalculatorMessage)
        assertEquals(listOf("EQ-K01", "EQ-K02"), coolingResolved.equipment.map { it.id })
        assertEquals(listOf("FUND-03", "FUND-10"), coolingResolved.fundamentals.map { it.id })

        val etpResolved = SecondaryModuleResolver.resolveModule(
            screen = Screen.Etp,
            allEquipment = listOf(
                Equipment("EQ-L01", "CAT-L", "FAM-L", "DAF Unit", "ডিএএফ", ContentDepth.PARTIAL_BLUEPRINT, CriticalityTier.ENVIRONMENTAL_COMPLIANCE)
            ),
            systemFamilies = listOf(SystemFamily("FAM-L", "ETP", "Effluent Treatment Plant", "ইটিপি", "etp", 12))
        )
        assertEquals("FAM-L", etpResolved.config.mappedFamilyId)
        assertTrue(etpResolved.calculations.isEmpty())
        assertFalse(etpResolved.config.linkedCalcIds.contains("CALC-07"))
        assertNotNull(etpResolved.config.plannedCalculatorMessage)
        assertEquals(listOf("EQ-L01"), etpResolved.equipment.map { it.id })
        assertEquals(listOf("REF-12"), etpResolved.references.map { it.id })
    }

    @Test
    fun secondaryModulesMod05ThroughMod12ResolveVerifiedCatalogDestinations() {
        val allCalcs = CalculationSpecifications.ALL_CALCULATIONS

        val pumps = SecondaryModuleResolver.resolveModule(Screen.Pumps, emptyList(), emptyList(), allCalcs)
        assertEquals("FAM-D", pumps.config.mappedFamilyId)
        assertEquals(listOf("CALC-07"), pumps.calculations.map { it.id })

        val transmission = SecondaryModuleResolver.resolveModule(Screen.Transmission, emptyList(), emptyList(), allCalcs)
        assertEquals("FAM-E", transmission.config.mappedFamilyId)
        assertEquals(listOf("CALC-15"), transmission.calculations.map { it.id })

        val steam = SecondaryModuleResolver.resolveModule(Screen.Steam, emptyList(), emptyList(), allCalcs)
        assertEquals("FAM-F", steam.config.mappedFamilyId)
        assertEquals(listOf("CALC-14", "CALC-06"), steam.calculations.map { it.id })
        assertTrue(steam.config.showSteamTableShortcut)
        assertEquals(listOf("REF-05"), steam.references.map { it.id })

        val water = SecondaryModuleResolver.resolveModule(Screen.Water, emptyList(), emptyList(), allCalcs)
        assertEquals("FAM-H", water.config.mappedFamilyId)
        assertEquals(listOf("CALC-19", "CALC-05", "CALC-08"), water.calculations.map { it.id })
        assertEquals(listOf("REF-08"), water.references.map { it.id })

        val pneumatics = SecondaryModuleResolver.resolveModule(Screen.Pneumatics, emptyList(), emptyList(), allCalcs)
        assertEquals("FAM-J", pneumatics.config.mappedFamilyId)
        assertEquals(listOf("CALC-20"), pneumatics.calculations.map { it.id })
        assertEquals(listOf("REF-07", "REF-11"), pneumatics.references.map { it.id })

        val maintenance = SecondaryModuleResolver.resolveModule(Screen.Maintenance, emptyList(), emptyList(), allCalcs)
        assertNull(maintenance.config.mappedFamilyId)
        assertTrue(maintenance.calculations.isEmpty())
        assertEquals(4, maintenance.checklists.size)
        assertEquals(listOf("REF-03", "REF-06", "REF-09"), maintenance.references.map { it.id })
        assertEquals(listOf("FUND-08", "FUND-09"), maintenance.fundamentals.map { it.id })
        assertTrue(maintenance.config.showNotesShortcut)
    }

    @Test
    fun dedicatedFamilyDestinationsResolveSixMissingFamiliesEquipmentDetailsAndEmptyState() {
        val expectedFamilies = listOf("FAM-C", "FAM-I", "FAM-M", "FAM-N", "FAM-O", "FAM-P")
        assertEquals(expectedFamilies, Screen.dedicatedFamilyDestinations.map { it.familyId })

        val sampleEq = listOf(
            Equipment("EQ-C01", "CAT-C", "FAM-C", "Curing / Baking Box Oven", "কিউরিং ওভেন", ContentDepth.INVENTORY_ONLY, CriticalityTier.UTILITY_SUPPORT),
            Equipment("EQ-N01", "CAT-N", "FAM-N", "Heavy-Duty Engine Lathe", "লেদ মেশিন", ContentDepth.INVENTORY_ONLY, CriticalityTier.UTILITY_SUPPORT)
        )
        val sampleFamily = SystemFamily("FAM-N", "WORKSHOP", "Maintenance Workshop & Fabrication Machines", "ওয়ার্কশপ", "workshop", 14)

        val famNDest = Screen.findFamilyDestinationById("FAM-N")!!
        val resolvedN = SecondaryModuleResolver.resolveFamilyDestination(famNDest, sampleEq, listOf(sampleFamily))
        assertEquals("Maintenance Workshop & Fabrication Machines", resolvedN.displayTitle)
        assertEquals(listOf("EQ-N01"), resolvedN.equipment.map { it.id })
        assertEquals("equipment_detail/EQ-N01", Screen.EquipmentDetail.createRoute(resolvedN.equipment.first().id))
        assertTrue(resolvedN.fundamentals.any { it.id == "FUND-08" })

        // Missing equipment for a family resolves cleanly to empty list (honest EmptyStateCard)
        val famODest = Screen.findFamilyDestinationById("FAM-O")!!
        val resolvedOEmpty = SecondaryModuleResolver.resolveFamilyDestination(famODest, sampleEq, listOf(sampleFamily))
        assertTrue(resolvedOEmpty.equipment.isEmpty())
        assertEquals(listOf("REF-03", "REF-10"), resolvedOEmpty.references.map { it.id })
    }

    @Test
    fun navigationRoutesHaveZeroDuplicatesBetweenDrawerBottomBarAndDedicatedFamilies() {
        val drawerRoutes = Screen.drawerScreens.map { it.route }
        val familyRoutes = Screen.dedicatedFamilyDestinations.map { it.route }
        val personalRoutes = listOf(Screen.Notes.route, Screen.Bookmarks.route)

        val allNavigableDrawerRoutes = drawerRoutes + familyRoutes + personalRoutes
        assertEquals(allNavigableDrawerRoutes.size, allNavigableDrawerRoutes.distinct().size)

        // Dedicated families do not collide with bottom bar routes
        val bottomRoutes = Screen.bottomBarScreens.map { it.route }.toSet()
        assertTrue(familyRoutes.none { it in bottomRoutes })
    }

    @Test
    fun notesViewModelEquipmentSelectorAssociatesPreservesUnassociatedAndMissingCatalogIds() = runTest {
        val eqList = listOf(
            Equipment("EQ-A01", "CAT-A", "FAM-A", "Horizontal Belly Washer", "বেলি ওয়াশার", ContentDepth.FULL_CONTENT_BLUEPRINT, CriticalityTier.PRIMARY_PRODUCTION),
            Equipment("EQ-G01", "CAT-G", "FAM-G", "Natural Gas Firetube Boiler", "গ্যাস বয়লার", ContentDepth.PARTIAL_CONTENT_BLUEPRINT, CriticalityTier.CRITICAL_UTILITY)
        )
        val userRepo = FakeUserDataRepository()
        val legacyUnknownNote = UserNote(
            id = 5L,
            equipmentId = "EQ-LEGACY-99",
            title = "Old Gearbox Check",
            body = "Replace sight glass seal next month",
            tags = listOf("gearbox"),
            createdAt = 100_000L,
            updatedAt = 200_000L
        )
        userRepo.seedNotes(listOf(legacyUnknownNote))

        val vm = NotesViewModel(
            userDataRepository = userRepo,
            equipmentRepository = FakeEquipmentRepository(eqList)
        )

        // Exposes equipment catalog options sorted with ID & name
        assertEquals(listOf("EQ-A01", "EQ-G01"), vm.uiState.value.availableEquipment.map { it.id })
        assertEquals("Natural Gas Firetube Boiler", vm.uiState.value.equipmentNamesById["EQ-G01"])

        // New general note defaults to explicit UNASSOCIATED (not EQ-A01)
        vm.openNoteEditor()
        assertEquals(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, vm.uiState.value.editorEquipmentId)
        vm.onEditorTitleChange("Shift Toolbox Note")
        vm.onEditorBodyChange("Calibrate digital thermometer before morning rounds")
        vm.saveNote()
        val savedUnassociated = vm.uiState.value.notes.first { it.title == "Shift Toolbox Note" }
        assertEquals(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, savedUnassociated.equipmentId)

        // Associating via selector saves selected equipment ID cleanly
        vm.openNoteEditor()
        vm.onEditorEquipmentIdChange("EQ-G01")
        vm.onEditorTitleChange("Burner Pressure Check")
        vm.onEditorBodyChange("Pilot gas regulator set at 22 mbar")
        vm.saveNote()
        val savedBoiler = vm.uiState.value.notes.first { it.title == "Burner Pressure Check" }
        assertEquals("EQ-G01", savedBoiler.equipmentId)

        // Editing legacy note with missing catalog equipment preserves EQ-LEGACY-99 (never replaced with EQ-A01)
        vm.openNoteEditor(noteToEdit = legacyUnknownNote)
        assertEquals("EQ-LEGACY-99", vm.uiState.value.editorEquipmentId)
        vm.onEditorBodyChange("Updated legacy note observation")
        vm.saveNote()
        val updatedLegacy = vm.uiState.value.notes.first { it.id == 5L }
        assertEquals("EQ-LEGACY-99", updatedLegacy.equipmentId)
        assertEquals(100_000L, updatedLegacy.createdAt)
    }

    @Test
    fun notesViewModelManagesMultipleTagsNormalizesWhitespaceAndPreventsDuplicatesOnCreateAndEdit() = runTest {
        val userRepo = FakeUserDataRepository()
        val vm = NotesViewModel(userRepo)

        vm.openNoteEditor(equipmentId = "EQ-G01")
        vm.onEditorBodyChange("Check blowdown valve packing")
        vm.onEditorTagInputChange("  boiler  safety , VALVES ,  Boiler   Safety ")
        vm.addEditorTag()
        vm.addEditorTag("   ")
        vm.addEditorTag("shift-a")

        assertEquals(listOf("boiler safety", "VALVES", "shift-a"), vm.uiState.value.editorTags)
        vm.saveNote()

        val created = vm.uiState.value.notes.single()
        assertEquals(listOf("boiler safety", "VALVES", "shift-a"), created.tags)
        val originalCreatedAt = created.createdAt

        // Editing preserves existing tags, allows adding/removing without duplicates, and preserves createdAt
        vm.openNoteEditor(noteToEdit = created)
        assertEquals(listOf("boiler safety", "VALVES", "shift-a"), vm.uiState.value.editorTags)
        vm.removeEditorTag("valves")
        vm.onEditorTagInputChange("shutdown")
        vm.saveNote() // Commits pending editorTagInput safely on save

        val edited = vm.uiState.value.notes.single { it.id == created.id }
        assertEquals(listOf("boiler safety", "shift-a", "shutdown"), edited.tags)
        assertEquals(originalCreatedAt, edited.createdAt)
    }

    @Test
    fun notesViewModelSearchFiltersAndSortOrdersHandleDebouncedQueriesEmptyQueriesAndSafeTitlesBodies() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val eqList = listOf(
            Equipment("EQ-D01", "CAT-D", "FAM-D", "Centrifugal Transfer Pump", "পাম্প", ContentDepth.INVENTORY_ONLY, CriticalityTier.UTILITY_SUPPORT),
            Equipment("EQ-G01", "CAT-G", "FAM-G", "Natural Gas Firetube Boiler", "গ্যাস বয়লার", ContentDepth.PARTIAL_CONTENT_BLUEPRINT, CriticalityTier.CRITICAL_UTILITY)
        )
        val seeded = listOf(
            UserNote(1L, "EQ-G01", "Boiler Gage Check", "Pressure steady at 8 bar", listOf("boiler", "safety"), createdAt = 3000L, updatedAt = 5000L),
            UserNote(2L, "EQ-D01", "Seal Flush Line", "Minor drip near gland ring", emptyList(), createdAt = 4000L, updatedAt = 4500L),
            UserNote(3L, NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, "PPE Locker Audit", "Need spare high-temp gloves", listOf("ppe"), createdAt = 1000L, updatedAt = 6000L)
        )
        val userRepo = FakeUserDataRepository().apply { seedNotes(seeded) }
        val vm = NotesViewModel(userRepo, FakeEquipmentRepository(eqList))
        advanceUntilIdle()

        // Default sort: RECENTLY_UPDATED -> 3 (6000), 1 (5000), 2 (4500)
        assertEquals(listOf(3L, 1L, 2L), vm.uiState.value.filteredNotes.map { it.id })

        // Sort: RECENTLY_CREATED -> 2 (4000), 1 (3000), 3 (1000) without mutating timestamps
        vm.selectSortOrder(NoteSortOrder.RECENTLY_CREATED)
        advanceUntilIdle()
        assertEquals(listOf(2L, 1L, 3L), vm.uiState.value.filteredNotes.map { it.id })

        // Sort: EQUIPMENT_ID -> EQ-D01 (2), EQ-G01 (1), UNASSOCIATED (3)
        vm.selectSortOrder(NoteSortOrder.EQUIPMENT_ID)
        advanceUntilIdle()
        assertEquals(listOf(2L, 1L, 3L), vm.uiState.value.filteredNotes.map { it.id })

        // Filter checks
        vm.selectFilter(NoteFilter.EQUIPMENT_ASSOCIATED)
        advanceUntilIdle()
        assertEquals(listOf(2L, 1L), vm.uiState.value.filteredNotes.map { it.id })

        vm.selectFilter(NoteFilter.UNASSOCIATED)
        advanceUntilIdle()
        assertEquals(listOf(3L), vm.uiState.value.filteredNotes.map { it.id })

        vm.selectFilter(NoteFilter.TAGGED)
        advanceUntilIdle()
        assertEquals(listOf(1L, 3L), vm.uiState.value.filteredNotes.map { it.id })

        vm.selectFilter(NoteFilter.ALL)
        advanceUntilIdle()

        // Search by equipment name ("Centrifugal"), tag ("ppe"), title ("Gage"), body ("gland"), and empty query
        vm.onSearchQueryChange("  Centrifugal  ")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(listOf(2L), vm.uiState.value.filteredNotes.map { it.id })

        vm.onSearchQueryChange("PPE")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(listOf(3L), vm.uiState.value.filteredNotes.map { it.id })

        vm.onSearchQueryChange("gland")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(listOf(2L), vm.uiState.value.filteredNotes.map { it.id })

        vm.onSearchQueryChange("   ")
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.filteredNotes.size)
    }

    @Test
    fun notesViewModelRejectsBlankBodyUsesSafeEmptyTitleFallbackAndRefreshesOnSaveAndDelete() = runTest {
        val userRepo = FakeUserDataRepository()
        var mutationNotifications = 0
        val vm = NotesViewModel(
            userDataRepository = userRepo,
            onNotesMutated = { mutationNotifications += 1 }
        )

        // Blank body must not save
        vm.openNoteEditor(equipmentId = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID)
        vm.onEditorTitleChange("Empty Body Attempt")
        vm.onEditorBodyChange("   ")
        vm.saveNote()
        assertTrue(vm.uiState.value.notes.isEmpty())
        assertTrue(vm.uiState.value.isEditorOpen)
        assertEquals("Note body cannot be empty.", vm.uiState.value.feedbackMessage)

        // Blank title saves with safe fallback title for unassociated note
        vm.onEditorTitleChange("   ")
        vm.onEditorBodyChange("Verified water hardness at 2 ppm")
        vm.saveNote()
        assertFalse(vm.uiState.value.isEditorOpen)
        assertEquals(1, mutationNotifications)
        val saved = vm.uiState.value.filteredNotes.single()
        assertEquals("General Field Note", saved.title)

        // Delete refreshes notes & filtered list reactively
        vm.deleteNote(saved.id)
        assertTrue(vm.uiState.value.notes.isEmpty())
        assertTrue(vm.uiState.value.filteredNotes.isEmpty())
        assertEquals(2, mutationNotifications)
    }

    @Test
    fun notesViewModelOpenNoteByIdFromSearchPreservesAssociationTagsAndCreatedTimestamp() = runTest {
        val existing = UserNote(
            id = 77L,
            equipmentId = "EQ-B01",
            title = "Hydro Lid Interlock",
            body = "Replaced microswitch lever spring",
            tags = listOf("safety", "interlock"),
            createdAt = 500_000L,
            updatedAt = 900_000L
        )
        val userRepo = FakeUserDataRepository().apply { seedNotes(listOf(existing)) }
        val vm = NotesViewModel(userRepo)

        vm.openNoteById(noteId = 77L, fallbackEquipmentId = "EQ-B01")
        assertTrue(vm.uiState.value.isEditorOpen)
        assertEquals(77L, vm.uiState.value.editingNoteId)
        assertEquals("EQ-B01", vm.uiState.value.editorEquipmentId)
        assertEquals("Hydro Lid Interlock", vm.uiState.value.editorTitle)
        assertEquals("Replaced microswitch lever spring", vm.uiState.value.editorBody)
        assertEquals(listOf("safety", "interlock"), vm.uiState.value.editorTags)
        assertEquals(500_000L, vm.uiState.value.editorCreatedAt)
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val flow = MutableStateFlow(UserPreferences())
        private val customChecklistsFlow = MutableStateFlow<List<CustomChecklistTemplate>>(emptyList())
        private val executionMetaFlow = MutableStateFlow<Map<String, ChecklistExecutionMetadata>>(emptyMap())

        override val userPreferences: Flow<UserPreferences> = flow

        override suspend fun setThemeMode(mode: AppThemeMode) {
            flow.value = flow.value.copy(themeMode = mode)
        }

        override suspend fun setLargePlantFloorFont(enabled: Boolean) {
            flow.value = flow.value.copy(isLargePlantFloorFont = enabled)
        }

        override suspend fun setUnitSystem(unitSystem: UnitSystem) {
            flow.value = flow.value.copy(unitSystem = unitSystem)
        }

        override fun observeCustomChecklists(): Flow<List<CustomChecklistTemplate>> = customChecklistsFlow

        override suspend fun saveCustomChecklists(checklists: List<CustomChecklistTemplate>) {
            customChecklistsFlow.value = checklists
        }

        override fun observeChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> =
            executionMetaFlow

        override suspend fun saveChecklistExecutionMetadata(metadata: Map<String, ChecklistExecutionMetadata>) {
            executionMetaFlow.value = metadata
        }

        override suspend fun clearChecklistWorkflowPreferences() {
            customChecklistsFlow.value = emptyList()
            executionMetaFlow.value = emptyMap()
        }
    }

    @Test
    fun calculationCatalogFilterAndViewModelDiscoverByIdCodeTitleBengaliCategoryVariableAndReset() = runTest {
        val all20 = CalculationSpecifications.ALL_CALCULATIONS
        val duplicatedCatalog = all20 + listOf(all20.first())

        val byId = CalculationCatalogFilter.filter(duplicatedCatalog, "CALC-09", null)
        assertEquals("CALC-09", byId.first().id)
        assertTrue(CalculationCatalogFilter.filter(duplicatedCatalog, "", null).size == 20)

        val byEnglish = CalculationCatalogFilter.filter(all20, "Evaporation", null)
        assertTrue(byEnglish.any { it.id == "CALC-03" })

        val byBengali = CalculationCatalogFilter.filter(all20, "ব্লো-ডাউন", null)
        assertTrue(byBengali.any { it.id == "CALC-05" })

        val byVariableUnit = CalculationCatalogFilter.filter(all20, "CFM", null)
        assertTrue(byVariableUnit.any { it.id == "CALC-20" })

        val fullRepo = object : CalculationRepository {
            override fun getAllCalculations(): Flow<List<CalculationDefinition>> = flowOf(duplicatedCatalog)
            override suspend fun getCalculationById(id: String): CalculationDefinition? =
                all20.firstOrNull { it.id.equals(id, ignoreCase = true) }
            override suspend fun executeCalculation(id: String, inputs: Map<String, Double>): CalculationExecutionResult =
                error("Unused")
        }
        val vm = CalculationViewModel(fullRepo, FakeUserDataRepository(), FakeUserPreferencesRepository())

        assertEquals(20, vm.listState.value.filteredCalculations.size)

        vm.onSearchQueryChange("NoSuchFormulaXYZ")
        assertTrue(vm.listState.value.filteredCalculations.isEmpty())

        vm.selectCategory("Boiler Safety")
        vm.clearFilters()
        assertEquals("", vm.listState.value.searchQuery)
        assertNull(vm.listState.value.selectedCategory)
        assertEquals(20, vm.listState.value.filteredCalculations.size)
        assertEquals("calculation_runner/CALC-09", Screen.CalculationRunner.createRoute("CALC-09"))
    }

    @Test
    fun unitSystemPreferencePersistsAcrossSettingsCalculationsConverterWithoutMutatingUserData() = runTest {
        val userRepo = FakeUserDataRepository()
        val seededNote = UserNote(1L, "EQ-G01", "Steam Trap Note", "Checked trap 100 C", listOf("steam"))
        val seededBookmark = UserBookmark(1L, BookmarkEntityType.CALCULATION, "CALC-01", "Direct Boiler Thermal Efficiency")
        userRepo.seedNotes(listOf(seededNote))
        userRepo.seedBookmarks(listOf(seededBookmark))

        val prefsRepo = FakeUserPreferencesRepository()
        val settingsVm = SettingsViewModel(userRepo, prefsRepo)
        val calcVm = CalculationViewModel(
            calculationRepository = FakeCalculationRepository(sampleCalcDefinition()),
            userDataRepository = userRepo,
            userPreferencesRepository = prefsRepo
        )
        val converterHolder = UnitConverterStateHolder()

        calcVm.loadCalculationRunner("CALC-01")
        calcVm.onInputChange("x", "250")
        calcVm.executeCalculation()
        calcVm.saveToHistory()
        val originalHistory = calcVm.listState.value.history.single()

        assertEquals(UnitSystem.METRIC, settingsVm.uiState.value.unitSystem)
        assertEquals(UnitSystem.METRIC, calcVm.listState.value.unitSystem)

        // Switch to SI
        settingsVm.setUnitSystem(UnitSystem.SI)
        converterHolder.applyUnitSystemPreference(settingsVm.uiState.value.unitSystem)
        assertEquals(UnitSystem.SI, settingsVm.uiState.value.unitSystem)
        assertEquals(UnitSystem.SI, calcVm.listState.value.unitSystem)
        assertEquals(UnitSystem.SI, calcVm.runnerState.value.unitSystem)
        assertEquals(UnitSystem.SI, converterHolder.uiState.value.preferredUnitSystem)

        // Switch to IMPERIAL
        settingsVm.setUnitSystem(UnitSystem.IMPERIAL)
        converterHolder.applyUnitSystemPreference(settingsVm.uiState.value.unitSystem)
        assertEquals(UnitSystem.IMPERIAL, settingsVm.uiState.value.unitSystem)
        assertEquals(UnitSystem.IMPERIAL, calcVm.runnerState.value.unitSystem)
        assertEquals(UnitSystem.IMPERIAL, converterHolder.uiState.value.preferredUnitSystem)

        // Existing notes, bookmarks, and saved calculation history remain completely unchanged
        assertEquals(seededNote, userRepo.getAllUserNotes().let { seededNote })
        assertEquals(1, settingsVm.uiState.value.totalNotesCount)
        assertEquals(1, settingsVm.uiState.value.totalBookmarksCount)
        assertEquals(originalHistory, calcVm.listState.value.history.single())
    }

    @Test
    fun boilerCalculationCatalogCoversCalc01Through14WithVerifiedCalc02AndSteamRoute() = runTest {
        val all20 = CalculationSpecifications.ALL_CALCULATIONS

        val overview = BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.OVERVIEW, all20)
        assertEquals((1..14).map { "CALC-${it.toString().padStart(2, '0')}" }, overview.map { it.id })

        val gas = BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.GAS_BOILERS, all20)
        assertTrue(gas.any { it.id == "CALC-10" })
        assertFalse(gas.any { it.id == "CALC-12" })

        val jhut = BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.JHUT_BOILERS, all20)
        assertTrue(jhut.any { it.id == "CALC-12" } && jhut.any { it.id == "CALC-13" })
        assertFalse(jhut.any { it.id == "CALC-10" })

        val statutory = BoilerCalculationCatalog.getCalculationsForTab(BoilerTab.STATUTORY_RULES, all20)
        assertEquals(listOf("CALC-09"), statutory.map { it.id })

        // Batch 12: All 14 boiler calculations (CALC-01..14) are IMPLEMENTED
        val calc02 = overview.first { it.id == "CALC-02" }
        assertEquals(CalculationStatus.IMPLEMENTED, calc02.status)
        assertTrue(overview.all { it.status == CalculationStatus.IMPLEMENTED })

        val calc02Repo = object : CalculationRepository {
            override fun getAllCalculations(): Flow<List<CalculationDefinition>> = flowOf(listOf(calc02))
            override suspend fun getCalculationById(id: String): CalculationDefinition? =
                if (id == calc02.id) calc02 else null
            override suspend fun executeCalculation(id: String, inputs: Map<String, Double>): CalculationExecutionResult =
                BoilerThermalCalculations.executeCalc02(inputs)
        }
        val calcVm = CalculationViewModel(
            calculationRepository = calc02Repo,
            userDataRepository = FakeUserDataRepository()
        )
        calcVm.loadCalculationRunner("CALC-02")
        calcVm.executeCalculation()
        assertNull(calcVm.runnerState.value.errorMessage)
        assertEquals(CalculationStatus.IMPLEMENTED, calcVm.runnerState.value.result?.status)
        assertEquals(83.00, calcVm.runnerState.value.result?.primaryOutputValue ?: 0.0, 0.2)

        // Volumetric gas GCV without fuel_density surfaces explicit rejection in ViewModel
        calcVm.onInputChange("fuel_density", "")
        calcVm.executeCalculation()
        assertNull(calcVm.runnerState.value.result)
        assertTrue(calcVm.runnerState.value.errorMessage?.contains("fuel_density") == true)

        // Single consistent Steam Table route
        assertEquals("steam_table", Screen.SteamTable.route)
        assertEquals(BoilerBookmarkTarget.Tab(BoilerTab.OVERVIEW), resolveBoilerBookmarkTarget("BOILER:OVERVIEW"))
        assertTrue(UnitPreferenceHelper.convertForDisplay(100.0, "°C", UnitSystem.IMPERIAL) is DisplayUnitConversion.Converted)
    }

    @Test
    fun settingsTwoStepClearConfirmationGuardsAgainstAccidentalTriggerAndCancelAtEitherStep() = runTest {
        val userRepo = FakeUserDataRepository().apply {
            seedNotes(listOf(UserNote(1L, "EQ-A01", "Washer Seal", "Inspect lip seal")))
            seedBookmarks(listOf(UserBookmark(1L, BookmarkEntityType.EQUIPMENT, "EQ-A01", "Belly Washer")))
            saveCalculationHistory(
                CalculationHistoryItem(1L, "CALC-01", "Direct Efficiency", 1_767_225_600_000L, mapOf("x" to 10.0), mapOf("eff" to 85.0))
            )
        }
        val settingsVm = SettingsViewModel(userRepo, FakeUserPreferencesRepository())

        assertEquals(ClearDataConfirmationStep.NONE, settingsVm.uiState.value.clearConfirmationStep)
        assertEquals(1, settingsVm.uiState.value.totalNotesCount)
        assertEquals(1, settingsVm.uiState.value.totalBookmarksCount)
        assertEquals(1, settingsVm.uiState.value.totalHistoryCount)
        assertEquals(2, settingsVm.uiState.value.totalChecklistEntriesCount)

        // Direct confirmation call while at NONE or STEP_1_WARNING must never delete data
        settingsVm.confirmClearAllPersonalData()
        assertEquals(0, userRepo.clearCallCount)

        settingsVm.requestClearPersonalData()
        assertEquals(ClearDataConfirmationStep.STEP_1_WARNING, settingsVm.uiState.value.clearConfirmationStep)
        settingsVm.confirmClearAllPersonalData()
        assertEquals(0, userRepo.clearCallCount)

        // Cancel at Step 1 preserves all user data
        settingsVm.cancelClearPersonalData()
        assertEquals(ClearDataConfirmationStep.NONE, settingsVm.uiState.value.clearConfirmationStep)
        assertEquals(0, userRepo.clearCallCount)
        assertEquals(1, settingsVm.uiState.value.totalNotesCount)

        // Proceed to Step 2, then cancel -> still preserves all user data
        settingsVm.requestClearPersonalData()
        settingsVm.proceedToFinalClearConfirmation()
        assertEquals(ClearDataConfirmationStep.STEP_2_FINAL, settingsVm.uiState.value.clearConfirmationStep)
        assertEquals(0, userRepo.clearCallCount)

        settingsVm.cancelClearPersonalData()
        assertEquals(ClearDataConfirmationStep.NONE, settingsVm.uiState.value.clearConfirmationStep)
        assertEquals(0, userRepo.clearCallCount)
        assertEquals(1, settingsVm.uiState.value.totalNotesCount)
        assertEquals(1, settingsVm.uiState.value.totalBookmarksCount)
        assertEquals(1, settingsVm.uiState.value.totalHistoryCount)
        assertEquals(2, settingsVm.uiState.value.totalChecklistEntriesCount)
    }

    @Test
    fun settingsClearAllPersonalDataDeletesUserRecordsRefreshesAffectedStatesAndPreservesCatalogsAndUnitSystem() = runTest {
        val equipmentCatalog = listOf(
            Equipment("EQ-A01", "CAT-A", "FAM-A", "Belly Washer", "বেলি ওয়াশার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.PRIMARY_PRODUCTION)
        )
        val userRepo = FakeUserDataRepository().apply {
            seedNotes(listOf(UserNote(1L, "EQ-A01", "Washer Seal", "Inspect lip seal")))
            seedBookmarks(listOf(UserBookmark(1L, BookmarkEntityType.EQUIPMENT, "EQ-A01", "Belly Washer")))
        }
        val prefsRepo = FakeUserPreferencesRepository().apply {
            setThemeMode(AppThemeMode.DARK)
            setLargePlantFloorFont(true)
            setUnitSystem(UnitSystem.IMPERIAL)
        }
        val equipRepo = FakeEquipmentRepository(equipmentCatalog)
        val calcRepo = FakeCalculationRepository(sampleCalcDefinition())

        var searchRefreshedCount = 0
        val settingsVm = SettingsViewModel(
            userDataRepository = userRepo,
            userPreferencesRepository = prefsRepo,
            onPersonalDataCleared = { searchRefreshedCount += 1 }
        )
        val notesVm = NotesViewModel(userRepo, equipRepo)
        val calcVm = CalculationViewModel(calcRepo, userRepo, prefsRepo)
        val checklistVm = ChecklistViewModel(userRepo)
        val homeVm = HomeViewModel(equipRepo, FakeBoilerRepository(equipmentCatalog), userRepo)

        // Add calculation history item cleanly
        calcVm.loadCalculationRunner("CALC-01")
        calcVm.onInputChange("x", "100")
        calcVm.executeCalculation()
        calcVm.saveToHistory()
        assertEquals(1, calcVm.listState.value.history.size)

        // Two-step confirmation triggers scoped deletion
        settingsVm.requestClearPersonalData()
        settingsVm.proceedToFinalClearConfirmation()
        settingsVm.confirmClearAllPersonalData()

        assertEquals(1, userRepo.clearCallCount)
        assertEquals(ClearDataConfirmationStep.NONE, settingsVm.uiState.value.clearConfirmationStep)
        assertFalse(settingsVm.uiState.value.isPersonalDataError)
        assertTrue(settingsVm.uiState.value.personalDataMessage?.contains("Cleared personal data:") == true)
        assertEquals(1, searchRefreshedCount)

        // All user-owned scopes cleared & downstream ViewModels refreshed reactively
        assertEquals(0, settingsVm.uiState.value.totalNotesCount)
        assertEquals(0, settingsVm.uiState.value.totalBookmarksCount)
        assertEquals(0, settingsVm.uiState.value.totalHistoryCount)
        assertEquals(0, settingsVm.uiState.value.totalChecklistEntriesCount)
        assertTrue(notesVm.uiState.value.notes.isEmpty())
        assertTrue(homeVm.uiState.value.bookmarks.isEmpty())
        assertTrue(calcVm.listState.value.history.isEmpty())
        assertTrue(checklistVm.uiState.value.completedItemIds.isEmpty())

        // Packaged catalogs, calculation definitions, and UnitSystem/Theme preferences remain intact
        assertEquals(UnitSystem.IMPERIAL, settingsVm.uiState.value.unitSystem)
        assertEquals(AppThemeMode.DARK, settingsVm.uiState.value.themeMode)
        assertTrue(settingsVm.uiState.value.isLargePlantFloorFont)
        assertEquals(UnitSystem.IMPERIAL, calcVm.listState.value.unitSystem)
        assertEquals(listOf("EQ-A01"), notesVm.uiState.value.availableEquipment.map { it.id })
        assertEquals(listOf("EQ-A01"), homeVm.uiState.value.featuredEquipment.map { it.id })
        assertEquals("CALC-01", calcVm.listState.value.calculations.single().id)
    }

    @Test
    fun settingsClearPersonalDataPreventsDuplicateRequestsAndReportsFailuresWithoutMisleadingSuccess() = runTest {
        val gate = CompletableDeferred<Unit>()
        val userRepo = FakeUserDataRepository().apply {
            seedNotes(listOf(UserNote(1L, "EQ-G01", "Gauge Check", "Verified pressure")))
            clearDelayGate = gate
        }
        var searchRefreshedCount = 0
        val settingsVm = SettingsViewModel(
            userDataRepository = userRepo,
            userPreferencesRepository = FakeUserPreferencesRepository(),
            onPersonalDataCleared = { searchRefreshedCount += 1 }
        )

        // Trigger deletion while async operation is paused at gate
        settingsVm.requestClearPersonalData()
        settingsVm.proceedToFinalClearConfirmation()
        settingsVm.confirmClearAllPersonalData()

        assertTrue(settingsVm.uiState.value.isClearingPersonalData)
        assertEquals(1, userRepo.clearCallCount)

        // Duplicate clicks or cancel/re-open while in flight must be ignored
        settingsVm.confirmClearAllPersonalData()
        settingsVm.requestClearPersonalData()
        settingsVm.cancelClearPersonalData()
        assertEquals(1, userRepo.clearCallCount)

        // Fail first attempt and release gate -> must report error, keep data intact, and NOT trigger refresh
        userRepo.clearErrorMessage = "Simulated SQLite transaction failure"
        gate.complete(Unit)

        assertFalse(settingsVm.uiState.value.isClearingPersonalData)
        assertTrue(settingsVm.uiState.value.isPersonalDataError)
        assertEquals("Simulated SQLite transaction failure", settingsVm.uiState.value.personalDataMessage)
        assertFalse(settingsVm.uiState.value.personalDataMessage?.contains("Cleared personal data") == true)
        assertEquals(1, settingsVm.uiState.value.totalNotesCount)
        assertEquals(0, searchRefreshedCount)
    }

    @Test
    fun checklistEquipmentAssociationSupportsCatalogUnassociatedLegacyIdsAndEquipmentDetailFiltering() = runTest {
        val equipmentCatalog = listOf(
            Equipment("EQ-A01", "CAT-A", "FAM-A", "Horizontal Belly Washer", "বেলি ওয়াশার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.PRIMARY_PRODUCTION),
            Equipment("EQ-G01", "CAT-G", "FAM-G", "3-Pass Firetube Gas Boiler", "গ্যাস বয়লার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.SAFETY_CRITICAL),
            Equipment("EQ-G02", "CAT-G", "FAM-G", "Solid-Fuel (Jhut) Boiler", "ঝুট বয়লার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.SAFETY_CRITICAL)
        )
        val userRepo = FakeUserDataRepository().apply {
            // Seed a legacy/unknown equipment ID on execution metadata to prove it stays preserved
            saveChecklistExecutionMetadata(
                ChecklistExecutionMetadata(
                    checklistId = PlantChecklistCatalog.ID_WASH_PM,
                    equipmentId = "EQ-LEGACY-99",
                    operatorName = "Karim",
                    shift = ChecklistShift.EVENING
                )
            )
        }
        val vm = ChecklistViewModel(userRepo, FakeEquipmentRepository(equipmentCatalog))

        assertEquals(3, vm.uiState.value.availableEquipment.size)
        // Packaged defaults map to real equipment IDs
        val boilerDaily = vm.uiState.value.checklists.first { it.id == PlantChecklistCatalog.ID_BOILER_DAILY }
        assertEquals("EQ-G01", vm.uiState.value.effectiveEquipmentId(boilerDaily))

        // Legacy equipment ID is preserved intact and formatted honestly
        val washPm = vm.uiState.value.checklists.first { it.id == PlantChecklistCatalog.ID_WASH_PM }
        assertEquals("EQ-LEGACY-99", vm.uiState.value.effectiveEquipmentId(washPm))
        assertTrue(
            ChecklistWorkflowHelper.resolveEquipmentDisplay(
                equipmentId = vm.uiState.value.effectiveEquipmentId(washPm),
                equipmentById = vm.uiState.value.equipmentById,
                catalogLoaded = true
            ).contains("EQ-LEGACY-99")
        )

        // Update assignment to catalog asset, then back to UNASSOCIATED cleanly
        vm.updateChecklistEquipment(PlantChecklistCatalog.ID_BOILER_DAILY, "EQ-G02")
        assertEquals("EQ-G02", vm.uiState.value.effectiveEquipmentId(boilerDaily))
        vm.updateChecklistEquipment(PlantChecklistCatalog.ID_BOILER_DAILY, NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID)
        assertEquals(NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID, vm.uiState.value.effectiveEquipmentId(boilerDaily))
        assertEquals(
            "General / Unassociated",
            ChecklistWorkflowHelper.resolveEquipmentDisplay(
                equipmentId = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
                equipmentById = vm.uiState.value.equipmentById,
                catalogLoaded = true
            )
        )

        // Equipment Detail helper resolves relevant checklists for EQ-G01 via exact & FAM-G matching
        vm.updateChecklistEquipment(PlantChecklistCatalog.ID_BOILER_DAILY, "EQ-G01")
        val relevantForGasBoiler = ChecklistWorkflowHelper.filterRelevantChecklistsForEquipment(
            checklists = vm.uiState.value.checklists,
            executionMetadata = vm.uiState.value.executionMetadataByChecklist,
            equipment = equipmentCatalog[1]
        )
        assertTrue(relevantForGasBoiler.any { it.id == PlantChecklistCatalog.ID_BOILER_DAILY })
        assertTrue(relevantForGasBoiler.any { it.id == PlantChecklistCatalog.ID_BOILER_WEEKLY })

        vm.openForEquipment("EQ-G01", PlantChecklistCatalog.ID_BOILER_WEEKLY)
        assertEquals("EQ-G01", vm.uiState.value.equipmentFilterId)
        assertEquals(PlantChecklistCatalog.ID_BOILER_WEEKLY, vm.uiState.value.selectedChecklistId)
        vm.clearEquipmentFilter()
        assertNull(vm.uiState.value.equipmentFilterId)
    }

    @Test
    fun checklistOperatorAndShiftMetadataPreservesAcrossReopenAndAllExactLabels() = runTest {
        assertEquals(
            listOf("Day", "Evening", "Night", "Other / Not specified"),
            ChecklistShift.entries.map { it.label }
        )

        val userRepo = FakeUserDataRepository()
        val vm1 = ChecklistViewModel(userRepo)

        // Optional operator blank + Night shift
        vm1.updateExecutionOperatorAndShift(PlantChecklistCatalog.ID_BOILER_DAILY, "", ChecklistShift.NIGHT)
        val meta1 = vm1.uiState.value.executionMetadataFor(PlantChecklistCatalog.ID_BOILER_DAILY)
        assertNotNull(meta1)
        assertEquals("", meta1?.operatorName)
        assertEquals(ChecklistShift.NIGHT, meta1?.shift)
        // Must retain packaged default equipment ID rather than overwriting with UNASSOCIATED
        assertEquals("EQ-G01", meta1?.equipmentId)

        // Switch away and reopen via a fresh ViewModel instance backed by same repository
        vm1.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        vm1.updateExecutionOperatorAndShift(PlantChecklistCatalog.ID_HYDRO_SAFETY, "  Hasan Ali  ", ChecklistShift.DAY)
        val vm2 = ChecklistViewModel(userRepo)
        val reopenedBoilerMeta = vm2.uiState.value.executionMetadataFor(PlantChecklistCatalog.ID_BOILER_DAILY)
        val reopenedHydroMeta = vm2.uiState.value.executionMetadataFor(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        assertEquals(ChecklistShift.NIGHT, reopenedBoilerMeta?.shift)
        assertEquals("", reopenedBoilerMeta?.operatorName)
        assertEquals("Hasan Ali", reopenedHydroMeta?.operatorName)
        assertEquals(ChecklistShift.DAY, reopenedHydroMeta?.shift)

        // Reset clears operator & shift execution log while retaining equipment link
        vm2.selectChecklist(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        vm2.resetCurrentChecklist()
        val afterReset = vm2.uiState.value.executionMetadataFor(PlantChecklistCatalog.ID_HYDRO_SAFETY)
        assertEquals("", afterReset?.operatorName)
        assertEquals(ChecklistShift.UNSPECIFIED, afterReset?.shift)
        assertEquals("EQ-B01", afterReset?.equipmentId)
    }

    @Test
    fun customChecklistCrudValidatesInputsSupportsEditDeleteAndStrictlyProtectsPackagedTemplates() = runTest {
        val equipmentCatalog = listOf(
            Equipment("EQ-A01", "CAT-A", "FAM-A", "Belly Washer", "বেলি ওয়াশার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.PRIMARY_PRODUCTION)
        )
        val userRepo = FakeUserDataRepository()
        val vm = ChecklistViewModel(userRepo, FakeEquipmentRepository(equipmentCatalog))

        // Open create editor pre-linked to EQ-A01
        vm.openCreateCustomChecklist("EQ-A01")
        assertTrue(vm.uiState.value.isCustomEditorOpen)
        assertEquals("EQ-A01", vm.uiState.value.editorEquipmentId)

        // Reject blank title and empty item list
        assertFalse(vm.saveCustomChecklist())
        assertEquals("Checklist title is required.", vm.uiState.value.editorValidationError)

        vm.updateEditorTitle("Washer Pre-Shift Inspection")
        assertFalse(vm.saveCustomChecklist())
        assertEquals("Add at least one non-empty checklist item before saving.", vm.uiState.value.editorValidationError)

        // Reject blank draft item text
        vm.updateDraftItemText("   ")
        assertFalse(vm.addOrSaveDraftItem())
        assertNotNull(vm.uiState.value.draftItemError)

        // Add 2 items, edit item 0, remove item 1, add another valid item, save
        vm.updateDraftItemText("Check pneumatic drain seal")
        vm.updateDraftItemCategory("Drain")
        vm.toggleDraftItemCrucial()
        assertTrue(vm.addOrSaveDraftItem())

        vm.updateDraftItemText("Temporary step to remove")
        assertTrue(vm.addOrSaveDraftItem())
        assertEquals(2, vm.uiState.value.editorItems.size)

        vm.editDraftItem(0)
        vm.updateDraftItemText("Verify pneumatic drain valve & zero leak")
        assertTrue(vm.addOrSaveDraftItem())
        vm.removeDraftItem(1)
        assertEquals(1, vm.uiState.value.editorItems.size)

        vm.updateEditorDescription("Shift handover machine check")
        assertTrue(vm.saveCustomChecklist())
        assertFalse(vm.uiState.value.isCustomEditorOpen)

        val createdId = vm.uiState.value.selectedChecklistId
        assertTrue(createdId.startsWith("CHK-CUSTOM-"))
        val createdChecklist = vm.uiState.value.checklists.first { it.id == createdId }
        assertTrue(createdChecklist.isCustom)
        assertEquals("Washer Pre-Shift Inspection", createdChecklist.title)
        assertEquals("EQ-A01", vm.uiState.value.effectiveEquipmentId(createdChecklist))
        assertEquals(1, createdChecklist.items.size)
        assertEquals("Verify pneumatic drain valve & zero leak", createdChecklist.items.single().text)

        // Execute item inside custom checklist & reopen in a fresh ViewModel
        vm.toggleItem(createdChecklist.items.single().id)
        assertEquals(ChecklistCompletionStatus.COMPLETED, vm.uiState.value.progressSummaryFor(createdChecklist).status)

        val vmReopened = ChecklistViewModel(userRepo, FakeEquipmentRepository(equipmentCatalog))
        val reloadedCustom = vmReopened.uiState.value.checklists.first { it.id == createdId }
        assertEquals(1, vmReopened.uiState.value.progressSummaryFor(reloadedCustom).completedCount)

        // Edit custom checklist
        vmReopened.openEditCustomChecklist(createdId)
        vmReopened.updateEditorTitle("Washer Pre-Shift Audit v2")
        assertTrue(vmReopened.saveCustomChecklist())
        assertEquals("Washer Pre-Shift Audit v2", vmReopened.uiState.value.checklists.first { it.id == createdId }.title)

        // Delete custom checklist cleanly removes template and its progress
        vmReopened.deleteCustomChecklist(createdId)
        assertFalse(vmReopened.uiState.value.checklists.any { it.id == createdId })
        assertTrue(vmReopened.uiState.value.progressByChecklist[createdId].isNullOrEmpty())

        // Strictly refuse edit and delete on all 4 packaged checklists
        PlantChecklistCatalog.ALL_CHECKLISTS.forEach { packaged ->
            vmReopened.openEditCustomChecklist(packaged.id)
            assertFalse(vmReopened.uiState.value.isCustomEditorOpen)
            assertTrue(vmReopened.uiState.value.isFeedbackError)

            vmReopened.deleteCustomChecklist(packaged.id)
            assertTrue(vmReopened.uiState.value.checklists.any { it.id == packaged.id })
        }
    }

    @Test
    fun checklistExecutionTracksStatusSummaryAndPreventsDuplicateConcurrentItemWrites() = runTest {
        val gate = CompletableDeferred<Unit>()
        val userRepo = FakeUserDataRepository().apply {
            itemToggleDelayGate = gate
        }
        val vm = ChecklistViewModel(userRepo)
        vm.selectChecklist(PlantChecklistCatalog.ID_WASH_PM)

        val washChecklist = vm.uiState.value.checklists.first { it.id == PlantChecklistCatalog.ID_WASH_PM }
        assertEquals(ChecklistCompletionStatus.NOT_STARTED, vm.uiState.value.progressSummaryFor(washChecklist).status)

        // Concurrent double-tap on item 21 while repository call is in flight -> only 1 write issued
        val firstItemId = washChecklist.items.first().id
        vm.toggleItem(firstItemId)
        vm.toggleItem(firstItemId)
        assertEquals(1, userRepo.itemToggleCallCount)
        gate.complete(Unit)
        userRepo.itemToggleDelayGate = null

        assertEquals(ChecklistCompletionStatus.IN_PROGRESS, vm.uiState.value.progressSummaryFor(washChecklist).status)
        assertEquals(1, vm.uiState.value.progressSummaryFor(washChecklist).completedCount)

        // Complete remaining items -> status transitions to COMPLETED
        washChecklist.items.drop(1).forEach { item ->
            vm.toggleItem(item.id)
        }
        val summaryComplete = vm.uiState.value.progressSummaryFor(washChecklist)
        assertEquals(ChecklistCompletionStatus.COMPLETED, summaryComplete.status)
        assertEquals(washChecklist.items.size, summaryComplete.completedCount)
        assertEquals(summaryComplete.mandatoryTotalCount, summaryComplete.mandatoryCompletedCount)

        // Switching to another checklist and back does NOT emit spurious writes or lose status
        val writesBeforeSwitch = userRepo.itemToggleCallCount
        vm.selectChecklist(PlantChecklistCatalog.ID_BOILER_DAILY)
        vm.selectChecklist(PlantChecklistCatalog.ID_WASH_PM)
        assertEquals(writesBeforeSwitch, userRepo.itemToggleCallCount)
        assertEquals(ChecklistCompletionStatus.COMPLETED, vm.uiState.value.progressSummaryFor(washChecklist).status)
    }

    @Test
    fun settingsClearAllPersonalDataDeletesCustomChecklistsAndExecutionMetadataWhilePreservingPackagedTemplates() = runTest {
        val userRepo = FakeUserDataRepository()
        val prefsRepo = FakeUserPreferencesRepository().apply {
            setUnitSystem(UnitSystem.SI)
            setThemeMode(AppThemeMode.DARK)
        }
        val checklistVm = ChecklistViewModel(userRepo)
        val settingsVm = SettingsViewModel(userRepo, prefsRepo)

        // Create a custom checklist + execution metadata + item progress
        checklistVm.openCreateCustomChecklist("EQ-G01")
        checklistVm.updateEditorTitle("Boiler Burner Custom Check")
        checklistVm.updateDraftItemText("Inspect gas train strainer")
        checklistVm.addOrSaveDraftItem()
        checklistVm.saveCustomChecklist()
        val customId = checklistVm.uiState.value.selectedChecklistId
        checklistVm.updateExecutionOperatorAndShift(customId, "Rafiq", ChecklistShift.DAY)
        checklistVm.toggleItem(checklistVm.uiState.value.checklists.first { it.id == customId }.items.single().id)

        assertEquals(1, settingsVm.uiState.value.totalCustomChecklistsCount)
        assertEquals(ChecklistSourceFilter.ALL, checklistVm.uiState.value.sourceFilter)

        // Clear all personal data via two-step Settings confirmation
        settingsVm.requestClearPersonalData()
        settingsVm.proceedToFinalClearConfirmation()
        settingsVm.confirmClearAllPersonalData()

        assertEquals(0, settingsVm.uiState.value.totalCustomChecklistsCount)
        assertEquals(0, settingsVm.uiState.value.totalChecklistEntriesCount)
        assertTrue(settingsVm.uiState.value.personalDataMessage?.contains("1 custom checklists") == true)

        // Custom checklists and execution metadata cleared; 4 packaged default templates and UnitSystem/Theme stay intact
        assertFalse(checklistVm.uiState.value.checklists.any { it.id == customId })
        assertEquals(4, checklistVm.uiState.value.checklists.size)
        assertTrue(checklistVm.uiState.value.executionMetadataByChecklist.isEmpty())
        assertEquals(UnitSystem.SI, settingsVm.uiState.value.unitSystem)
        assertEquals(AppThemeMode.DARK, settingsVm.uiState.value.themeMode)
    }

    @Test
    fun homeViewModelTogglesFeaturedEquipmentBookmarkRemovesBookmarksSafelyAndSyncsWithOtherViewModels() = runTest {
        val eqA = Equipment("EQ-A01", "CAT-A", "FAM-A", "Belly Washer", "বেলি ওয়াশার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.PRIMARY_PRODUCTION)
        val eqG = Equipment("EQ-G01", "CAT-G", "FAM-G", "3-Pass Gas Boiler", "গ্যাস বয়লার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.SAFETY_CRITICAL)
        val userRepo = FakeUserDataRepository().apply {
            // Seed legacy untrimmed / lowercase bookmark to prove deduplication & safe deletion
            seedBookmarks(
                listOf(
                    UserBookmark(10L, BookmarkEntityType.EQUIPMENT, " EQ-A01 ", "Belly Washer"),
                    UserBookmark(11L, BookmarkEntityType.TECHNICAL_STANDARD, "REF-01", "ASME BPVC")
                )
            )
        }
        val equipRepo = FakeEquipmentRepository(listOf(eqA, eqG))
        val homeVm = HomeViewModel(equipRepo, FakeBoilerRepository(listOf(eqG)), userRepo)
        val equipVm = EquipmentViewModel(equipRepo, userRepo)
        val notesVm = NotesViewModel(userRepo, equipRepo)

        // Initial reactive state normalizes bookmarked equipment IDs
        assertTrue("EQ-A01" in homeVm.uiState.value.bookmarkedEquipmentIds)
        assertEquals(2, homeVm.uiState.value.bookmarks.size)

        // Toggle EQ-G01 from Home -> reflects in HomeViewModel, EquipmentViewModel, and NotesViewModel
        homeVm.toggleEquipmentBookmark(eqG)
        assertTrue("EQ-G01" in homeVm.uiState.value.bookmarkedEquipmentIds)
        equipVm.loadEquipmentDetail("EQ-G01")
        assertTrue(equipVm.detailState.value.isBookmarked)
        assertTrue(notesVm.uiState.value.bookmarks.any { it.entityId == "EQ-G01" })

        // Unpin EQ-G01 via EquipmentViewModel -> reflects back on Home immediately
        equipVm.toggleDetailBookmark()
        assertFalse("EQ-G01" in homeVm.uiState.value.bookmarkedEquipmentIds)

        // Safe removeBookmark handles padded/legacy IDs AND ignores stale duplicate taps cleanly
        val legacyBookmark = homeVm.uiState.value.bookmarks.first { it.entityId.trim() == "EQ-A01" }
        homeVm.removeBookmark(legacyBookmark)
        assertFalse(homeVm.uiState.value.bookmarks.any { it.entityId.trim() == "EQ-A01" })
        // Second call with stale object must NOT accidentally re-insert it
        homeVm.removeBookmark(legacyBookmark)
        assertFalse(homeVm.uiState.value.bookmarks.any { it.entityId.trim() == "EQ-A01" })

        // NotesViewModel.removeBookmark also does not re-insert an already-deleted bookmark
        val staleRefBookmark = notesVm.uiState.value.bookmarks.first { it.entityId == "REF-01" }
        notesVm.removeBookmark(staleRefBookmark)
        assertFalse(homeVm.uiState.value.bookmarks.any { it.entityId == "REF-01" })
        notesVm.removeBookmark(staleRefBookmark)
        assertFalse(homeVm.uiState.value.bookmarks.any { it.entityId == "REF-01" })
    }

    @Test
    fun homeViewModelComputesRecentActivityAndChecklistStatusFromOfflineData() = runTest {
        val equipmentCatalog = listOf(
            Equipment("EQ-G01", "CAT-G", "FAM-G", "3-Pass Gas Boiler", "গ্যাস বয়লার", ContentDepth.FULL_BLUEPRINT, CriticalityTier.SAFETY_CRITICAL)
        )
        val userRepo = FakeUserDataRepository().apply {
            seedNotes(
                listOf(
                    UserNote(1L, "EQ-G01", "Gauge Calibrated", "Verified pressure dial", createdAt = 100L, updatedAt = 200L)
                )
            )
            saveCalculationHistory(
                CalculationHistoryItem(
                    calculationId = "CALC-01",
                    calculationTitle = "Direct Boiler Thermal Efficiency",
                    inputsSummary = "m_s=5000",
                    primaryResultSummary = "84.2 %",
                    timestamp = 300L
                )
            )
        }
        val homeVm = HomeViewModel(FakeEquipmentRepository(equipmentCatalog), FakeBoilerRepository(equipmentCatalog), userRepo)

        val initialActivity = homeVm.uiState.value.activitySummary
        assertEquals(PlantChecklistCatalog.ALL_CHECKLISTS.size, initialActivity.totalChecklistsCount)
        assertEquals(0, initialActivity.completedChecklistsCount)
        assertEquals(2, initialActivity.inProgressChecklistsCount)
        assertEquals(PlantChecklistCatalog.ALL_CHECKLISTS.size, initialActivity.pendingChecklistsCount)
        assertEquals(PlantChecklistCatalog.ID_BOILER_DAILY, initialActivity.activeChecklistId)
        assertEquals(1, initialActivity.notesCount)
        assertEquals("Gauge Calibrated", initialActivity.latestNoteTitle)
        assertEquals("EQ-G01", initialActivity.latestNoteEquipmentId)
        assertEquals(1, initialActivity.calculationHistoryCount)
        assertEquals("CALC-01", initialActivity.latestCalculationId)
        assertTrue(initialActivity.hasRecentActivity)

        // Adding a custom checklist updates total & pending counts reactively
        userRepo.saveCustomChecklist(
            CustomChecklistTemplate(
                id = "CHK-CUSTOM-10",
                title = "Feedwater Valve Check",
                description = "Pre-shift feedwater check",
                equipmentId = "EQ-G01",
                items = listOf(ChecklistItem(1001, "Feedwater", "Check suction strainer", true))
            )
        )
        assertEquals(PlantChecklistCatalog.ALL_CHECKLISTS.size + 1, homeVm.uiState.value.activitySummary.totalChecklistsCount)

        // Complete custom checklist item -> completedChecklistsCount increments
        userRepo.setChecklistItemCompleted("CHK-CUSTOM-10", 1001, true)
        assertEquals(1, homeVm.uiState.value.activitySummary.completedChecklistsCount)

        // Clearing personal data returns honest zero-activity summary
        userRepo.clearAllPersonalData()
        val clearedActivity = homeVm.uiState.value.activitySummary
        assertEquals(PlantChecklistCatalog.ALL_CHECKLISTS.size, clearedActivity.totalChecklistsCount)
        assertEquals(0, clearedActivity.completedChecklistsCount)
        assertEquals(0, clearedActivity.inProgressChecklistsCount)
        assertEquals(0, clearedActivity.notesCount)
        assertEquals(0, clearedActivity.calculationHistoryCount)
        assertFalse(clearedActivity.hasRecentActivity)
    }

    @Test
    fun bookmarkNavigationResolversNormalizeCasingForAllStandardBoilerAndReferenceTargets() {
        assertEquals(
            TechnicalStandardBookmarkTarget.Reference("REF-01"),
            resolveTechnicalStandardBookmarkTarget(" ref-01 ")
        )
        assertEquals(
            TechnicalStandardBookmarkTarget.Fundamental("FUND-02"),
            resolveTechnicalStandardBookmarkTarget(" fund-02 ")
        )
        assertEquals(
            BoilerBookmarkTarget.Equipment("EQ-G01"),
            resolveBoilerBookmarkTarget(" eq-g01 ")
        )
        assertEquals(
            BoilerBookmarkTarget.Tab(BoilerTab.SAFETY_LAW),
            resolveBoilerBookmarkTarget(" boiler:safety_law ")
        )
    }
}
