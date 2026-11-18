package com.researchbangla.mechanicalpronotes.domain.repository

import com.researchbangla.mechanicalpronotes.domain.model.*
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    fun getAllSystemFamilies(): Flow<List<SystemFamily>>
    fun getAllEquipment(): Flow<List<Equipment>>
    fun getEquipmentByFamily(familyId: String): Flow<List<Equipment>>
    suspend fun getEquipmentById(equipmentId: String): Equipment?
    fun getEquipmentByContentDepth(depth: ContentDepth): Flow<List<Equipment>>
    suspend fun getKnowledgeSections(equipmentId: String): List<KnowledgeSection>
    suspend fun getFailureModes(equipmentId: String): List<FailureMode>
    suspend fun getMaintenanceProcedures(equipmentId: String): List<MaintenanceProcedure>
}

interface BoilerRepository {
    fun getBoilerEquipment(): Flow<List<Equipment>>
    suspend fun getBoilerById(boilerId: String): Equipment?
    fun getGasBoilers(): Flow<List<Equipment>>
    fun getJhutAndSolidFuelBoilers(): Flow<List<Equipment>>
}

data class CalculationSearchHit(
    val id: String,
    val code: String,
    val title: String,
    val bengaliTitle: String,
    val category: String,
    val engineeringReference: String,
    val status: CalculationStatus
)

data class UserNoteSearchHit(
    val id: Long,
    val equipmentId: String,
    val displayTitle: String,
    val bodySnippet: String,
    val tags: List<String> = emptyList(),
    val updatedAt: Long = 0L,
    val equipmentName: String? = null
)

data class CalculationHistorySearchHit(
    val id: Long,
    val calculationId: String,
    val calculationTitle: String,
    val timestamp: Long,
    val formattedTimestamp: String,
    val resultSummary: String,
    val inputsSummary: String = "",
    val userRemark: String? = null
)

data class UnifiedSearchResult(
    val equipment: List<Equipment> = emptyList(),
    val catalogHits: List<com.researchbangla.mechanicalpronotes.domain.catalog.CatalogSearchHit> = emptyList(),
    val calculations: List<CalculationSearchHit> = emptyList(),
    val userNotes: List<UserNoteSearchHit> = emptyList(),
    val calculationHistory: List<CalculationHistorySearchHit> = emptyList()
) {
    val isEmpty: Boolean
        get() = equipment.isEmpty() &&
            catalogHits.isEmpty() &&
            calculations.isEmpty() &&
            userNotes.isEmpty() &&
            calculationHistory.isEmpty()
}

interface SearchRepository {
    suspend fun search(query: String, limit: Int = 30): List<Equipment>
    fun searchFlow(query: String, limit: Int = 30): Flow<List<Equipment>>
    suspend fun searchKnowledgeBase(query: String, limit: Int = 30): UnifiedSearchResult =
        UnifiedSearchResult(
            equipment = search(query, limit),
            catalogHits = com.researchbangla.mechanicalpronotes.domain.catalog.EngineeringCatalogSearch.searchStaticCatalogs(query, limit)
        )
}

interface CalculationRepository {
    fun getAllCalculations(): Flow<List<CalculationDefinition>>
    suspend fun getCalculationById(id: String): CalculationDefinition?
    suspend fun executeCalculation(id: String, inputs: Map<String, Double>): CalculationExecutionResult
}

interface UserDataRepository {
    // Personal Notes
    fun getNotesForEquipment(equipmentId: String): Flow<List<UserNote>>
    suspend fun saveNote(note: UserNote): Long
    suspend fun deleteNote(noteId: Long)
    fun getAllUserNotes(): Flow<List<UserNote>>

    // Bookmarks
    fun getAllBookmarks(): Flow<List<UserBookmark>>
    suspend fun toggleBookmark(entityType: BookmarkEntityType, entityId: String, title: String): Boolean
    suspend fun isBookmarked(entityType: BookmarkEntityType, entityId: String): Boolean

    // Calculation History
    fun getCalculationHistory(): Flow<List<CalculationHistoryItem>>
    suspend fun saveCalculationHistory(item: CalculationHistoryItem): Long

    // Checklist Progress Persistence
    fun getAllChecklistProgress(): Flow<List<ChecklistProgress>>
    fun getChecklistProgress(checklistId: String): Flow<List<ChecklistProgress>>
    suspend fun setChecklistItemCompleted(checklistId: String, itemId: Int, isCompleted: Boolean, remarks: String? = null)
    suspend fun resetChecklist(checklistId: String)

    // Custom Checklist Templates & Execution Metadata (GAP-06 Completion)
    fun getCustomChecklists(): Flow<List<CustomChecklistTemplate>> =
        kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun saveCustomChecklist(template: CustomChecklistTemplate): CustomChecklistTemplate = template
    suspend fun deleteCustomChecklist(checklistId: String): Boolean = false
    fun getAllChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> =
        kotlinx.coroutines.flow.flowOf(emptyMap())
    suspend fun saveChecklistExecutionMetadata(metadata: ChecklistExecutionMetadata) {}

    // Backup, Restore & Safe Personal Data Deletion Contracts
    suspend fun exportUserPersonalDataJson(): String
    suspend fun importUserPersonalDataJson(jsonString: String, overwrite: Boolean = false): BackupImportResult
    suspend fun clearAllPersonalData(): ClearPersonalDataResult =
        ClearPersonalDataResult.Failure("Personal data deletion is not available.")
}

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setLargePlantFloorFont(enabled: Boolean)
    suspend fun setUnitSystem(unitSystem: UnitSystem) {}

    fun observeCustomChecklists(): Flow<List<CustomChecklistTemplate>> =
        kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun saveCustomChecklists(checklists: List<CustomChecklistTemplate>) {}
    fun observeChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> =
        kotlinx.coroutines.flow.flowOf(emptyMap())
    suspend fun saveChecklistExecutionMetadata(metadata: Map<String, ChecklistExecutionMetadata>) {}
    suspend fun clearChecklistWorkflowPreferences() {}
}
