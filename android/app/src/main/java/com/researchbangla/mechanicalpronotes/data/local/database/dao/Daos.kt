package com.researchbangla.mechanicalpronotes.data.local.database.dao

import androidx.room.*
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Query("SELECT * FROM system_family ORDER BY sort_order ASC")
    fun getAllSystemFamilies(): Flow<List<SystemFamilyEntity>>

    @Query("SELECT * FROM equipment ORDER BY id ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE family_id = :familyId ORDER BY id ASC")
    fun getEquipmentByFamily(familyId: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = :equipmentId LIMIT 1")
    suspend fun getEquipmentById(equipmentId: String): EquipmentEntity?

    @Query("SELECT * FROM equipment WHERE content_depth_status = :depth ORDER BY id ASC")
    fun getEquipmentByContentDepth(depth: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment_subtype WHERE equipment_id = :equipmentId")
    suspend fun getSubtypesForEquipment(equipmentId: String): List<EquipmentSubtypeEntity>

    @Query("SELECT * FROM component WHERE equipment_id = :equipmentId ORDER BY wear_tier ASC")
    suspend fun getComponentsForEquipment(equipmentId: String): List<ComponentEntity>

    @Query("SELECT * FROM technical_parameter WHERE equipment_id = :equipmentId")
    suspend fun getParametersForEquipment(equipmentId: String): List<TechnicalParameterEntity>

    @Query("SELECT * FROM knowledge_section WHERE equipment_id = :equipmentId ORDER BY section_number ASC")
    suspend fun getKnowledgeSections(equipmentId: String): List<KnowledgeSectionEntity>

    @Query("SELECT * FROM failure_mode WHERE equipment_id = :equipmentId")
    suspend fun getFailureModes(equipmentId: String): List<FailureModeEntity>

    @Query("SELECT * FROM maintenance_procedure WHERE equipment_id = :equipmentId")
    suspend fun getMaintenanceProcedures(equipmentId: String): List<MaintenanceProcedureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemFamilies(families: List<SystemFamilyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentCategories(categories: List<EquipmentCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: List<EquipmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtypes(subtypes: List<EquipmentSubtypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<ComponentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(parameters: List<TechnicalParameterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeSections(sections: List<KnowledgeSectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFailureModes(modes: List<FailureModeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceProcedures(procedures: List<MaintenanceProcedureEntity>)
}

@Dao
interface BoilerDao {

    @Query("""
        SELECT e.* FROM equipment e
        INNER JOIN system_family f ON e.family_id = f.id
        WHERE f.is_dedicated_boiler = 1 OR e.family_id = 'FAM-G'
        ORDER BY e.id ASC
    """)
    fun getBoilerEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = 'EQ-G01' LIMIT 1")
    fun getGasBoiler(): Flow<EquipmentEntity?>

    @Query("SELECT * FROM equipment WHERE id = 'EQ-G02' LIMIT 1")
    fun getJhutBoiler(): Flow<EquipmentEntity?>

    @Query("""
        SELECT * FROM equipment 
        WHERE (family_id = 'FAM-G' OR id LIKE 'EQ-G%')
          AND (name LIKE '%Gas%' OR bengali_name LIKE '%গ্যাস%')
    """)
    fun getGasFiredBoilers(): Flow<List<EquipmentEntity>>

    @Query("""
        SELECT * FROM equipment 
        WHERE (family_id = 'FAM-G' OR id LIKE 'EQ-G%')
          AND (name LIKE '%Jhut%' OR name LIKE '%Biomass%' OR name LIKE '%Solid%' OR bengali_name LIKE '%ঝুট%')
    """)
    fun getJhutAndSolidFuelBoilers(): Flow<List<EquipmentEntity>>
}

@Dao
interface SearchDao {

    @Query("""
        SELECT e.* FROM equipment e
        JOIN equipment_fts fts ON e.id = fts.id
        WHERE equipment_fts MATCH :query
        LIMIT :limit
    """)
    suspend fun searchFts(query: String, limit: Int = 30): List<EquipmentEntity>

    /**
     * Fallback standard LIKE search if FTS syntax is not used or special characters are present.
     */
    @Query("""
        SELECT * FROM equipment
        WHERE id LIKE '%' || :query || '%'
           OR name LIKE '%' || :query || '%'
           OR bengali_name LIKE '%' || :query || '%'
           OR aliases_csv LIKE '%' || :query || '%'
        LIMIT :limit
    """)
    suspend fun searchLike(query: String, limit: Int = 30): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFts(ftsItems: List<EquipmentFtsEntity>)
}

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculation ORDER BY id ASC")
    fun getAllCalculations(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculation WHERE id = :id LIMIT 1")
    suspend fun getCalculationById(id: String): CalculationEntity?

    @Query("SELECT * FROM formula_variable WHERE calculation_id = :calcId ORDER BY id ASC")
    suspend fun getVariablesForCalculation(calcId: String): List<FormulaVariableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculations(calculations: List<CalculationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariables(variables: List<FormulaVariableEntity>)

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculationHistoryEntity): Long

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAllHistory()
}

@Dao
interface UserNoteDao {

    @Query("SELECT * FROM user_note WHERE equipment_id = :equipmentId ORDER BY updated_at DESC")
    fun getNotesForEquipment(equipmentId: String): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_note ORDER BY updated_at DESC")
    fun getAllNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_note WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Long): UserNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNoteEntity): Long

    @Query("DELETE FROM user_note WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    @Query("DELETE FROM user_note")
    suspend fun deleteAllNotes()
}

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM user_bookmark ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<UserBookmarkEntity>>

    @Query("SELECT COUNT(*) FROM user_bookmark WHERE entity_type = :entityType AND entity_id = :entityId")
    suspend fun isBookmarked(entityType: String, entityId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: UserBookmarkEntity): Long

    @Query("DELETE FROM user_bookmark WHERE entity_type = :entityType AND entity_id = :entityId")
    suspend fun deleteBookmark(entityType: String, entityId: String)

    @Query("DELETE FROM user_bookmark")
    suspend fun deleteAllBookmarks()
}

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklist_progress WHERE checklist_id = :checklistId ORDER BY item_id ASC")
    fun getProgressForChecklist(checklistId: String): Flow<List<ChecklistProgressEntity>>

    @Query("SELECT * FROM checklist_progress ORDER BY checklist_id ASC, item_id ASC")
    fun getAllProgress(): Flow<List<ChecklistProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(item: ChecklistProgressEntity): Long

    @Query("DELETE FROM checklist_progress WHERE checklist_id = :checklistId")
    suspend fun clearChecklistProgress(checklistId: String)

    @Query("DELETE FROM checklist_progress")
    suspend fun deleteAllChecklistProgress()
}
