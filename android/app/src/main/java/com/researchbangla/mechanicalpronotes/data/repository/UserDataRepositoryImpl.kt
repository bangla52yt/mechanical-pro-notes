package com.researchbangla.mechanicalpronotes.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.researchbangla.mechanicalpronotes.data.local.database.MechanicalProDatabase
import com.researchbangla.mechanicalpronotes.data.local.database.dao.BookmarkDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.ChecklistDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.UserNoteDao
import com.researchbangla.mechanicalpronotes.data.local.database.entity.CalculationHistoryEntity
import com.researchbangla.mechanicalpronotes.data.mapper.toDomain
import com.researchbangla.mechanicalpronotes.data.mapper.toEntity
import com.researchbangla.mechanicalpronotes.domain.catalog.PlantChecklistCatalog
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.UserDataRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserDataRepositoryImpl private constructor(
    private val database: MechanicalProDatabase?,
    private val userNoteDao: UserNoteDao,
    private val bookmarkDao: BookmarkDao,
    private val calculationDao: CalculationDao,
    private val checklistDao: ChecklistDao,
    private val userPreferencesRepository: UserPreferencesRepository?,
    private val runInTransaction: suspend (suspend () -> Unit) -> Unit
) : UserDataRepository {

    constructor(
        database: MechanicalProDatabase,
        userNoteDao: UserNoteDao,
        bookmarkDao: BookmarkDao,
        calculationDao: CalculationDao,
        checklistDao: ChecklistDao,
        userPreferencesRepository: UserPreferencesRepository? = null
    ) : this(
        database = database,
        userNoteDao = userNoteDao,
        bookmarkDao = bookmarkDao,
        calculationDao = calculationDao,
        checklistDao = checklistDao,
        userPreferencesRepository = userPreferencesRepository,
        runInTransaction = { block -> database.withTransaction { block() } }
    )

    internal constructor(
        userNoteDao: UserNoteDao,
        bookmarkDao: BookmarkDao,
        calculationDao: CalculationDao,
        checklistDao: ChecklistDao,
        userPreferencesRepository: UserPreferencesRepository? = null,
        runInTransaction: suspend (suspend () -> Unit) -> Unit = { it() }
    ) : this(
        database = null,
        userNoteDao = userNoteDao,
        bookmarkDao = bookmarkDao,
        calculationDao = calculationDao,
        checklistDao = checklistDao,
        userPreferencesRepository = userPreferencesRepository,
        runInTransaction = runInTransaction
    )

    private val fallbackCustomChecklistsFlow = MutableStateFlow<List<CustomChecklistTemplate>>(emptyList())
    private val fallbackExecutionMetadataFlow = MutableStateFlow<Map<String, ChecklistExecutionMetadata>>(emptyMap())

    override fun getNotesForEquipment(equipmentId: String): Flow<List<UserNote>> {
        return userNoteDao.getNotesForEquipment(equipmentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNote(note: UserNote): Long {
        return userNoteDao.insertNote(note.toEntity())
    }

    override suspend fun deleteNote(noteId: Long) {
        userNoteDao.deleteNote(noteId)
    }

    override fun getAllUserNotes(): Flow<List<UserNote>> {
        return userNoteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllBookmarks(): Flow<List<UserBookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleBookmark(
        entityType: BookmarkEntityType,
        entityId: String,
        title: String
    ): Boolean {
        val count = bookmarkDao.isBookmarked(entityType.name, entityId)
        return if (count > 0) {
            bookmarkDao.deleteBookmark(entityType.name, entityId)
            false
        } else {
            bookmarkDao.insertBookmark(
                com.researchbangla.mechanicalpronotes.data.local.database.entity.UserBookmarkEntity(
                    entityType = entityType.name,
                    entityId = entityId,
                    entityTitle = title
                )
            )
            true
        }
    }

    override suspend fun isBookmarked(entityType: BookmarkEntityType, entityId: String): Boolean {
        return bookmarkDao.isBookmarked(entityType.name, entityId) > 0
    }

    override fun getCalculationHistory(): Flow<List<CalculationHistoryItem>> {
        return calculationDao.getCalculationHistory().map { entities ->
            entities.map { entity ->
                CalculationHistoryItem(
                    id = entity.id,
                    calculationId = entity.calculationId,
                    calculationTitle = entity.calculationTitle,
                    timestamp = entity.timestamp,
                    inputValues = BackupJsonCodec.parseDoubleMap(entity.inputsJson),
                    outputValues = BackupJsonCodec.parseDoubleMap(entity.outputsJson),
                    userRemark = entity.userRemark
                )
            }
        }
    }

    override suspend fun saveCalculationHistory(item: CalculationHistoryItem): Long {
        val entity = CalculationHistoryEntity(
            calculationId = item.calculationId,
            calculationTitle = item.calculationTitle,
            timestamp = item.timestamp,
            inputsJson = BackupJsonCodec.encodeDoubleMap(item.inputValues),
            outputsJson = BackupJsonCodec.encodeDoubleMap(item.outputValues),
            userRemark = item.userRemark
        )
        return calculationDao.insertHistory(entity)
    }

    override fun getAllChecklistProgress(): Flow<List<ChecklistProgress>> {
        return checklistDao.getAllProgress().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getChecklistProgress(checklistId: String): Flow<List<ChecklistProgress>> {
        return checklistDao.getProgressForChecklist(checklistId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun setChecklistItemCompleted(
        checklistId: String,
        itemId: Int,
        isCompleted: Boolean,
        remarks: String?
    ) {
        val existing = checklistDao.getProgressForChecklist(checklistId)
            .first()
            .firstOrNull { it.itemId == itemId }
        val resolvedTimestamp = if (isCompleted) {
            if (existing?.isCompleted == true && existing.completionTimestamp != null) {
                existing.completionTimestamp
            } else {
                System.currentTimeMillis()
            }
        } else {
            null
        }
        checklistDao.upsertProgress(
            com.researchbangla.mechanicalpronotes.data.local.database.entity.ChecklistProgressEntity(
                checklistId = checklistId,
                itemId = itemId,
                isCompleted = isCompleted,
                completionTimestamp = resolvedTimestamp,
                remarks = remarks
            )
        )
    }

    override suspend fun resetChecklist(checklistId: String) {
        val normalizedId = checklistId.trim()
        checklistDao.clearChecklistProgress(normalizedId)
        val currentMeta = getAllChecklistExecutionMetadata().first().toMutableMap()
        val existingMeta = currentMeta[normalizedId]
        if (existingMeta != null) {
            currentMeta[normalizedId] = existingMeta.copy(
                operatorName = "",
                shift = ChecklistShift.UNSPECIFIED,
                updatedAt = System.currentTimeMillis()
            )
            persistExecutionMetadata(currentMeta)
        }
    }

    override fun getCustomChecklists(): Flow<List<CustomChecklistTemplate>> {
        return userPreferencesRepository?.observeCustomChecklists() ?: fallbackCustomChecklistsFlow
    }

    override suspend fun saveCustomChecklist(template: CustomChecklistTemplate): CustomChecklistTemplate {
        val now = System.currentTimeMillis()
        val candidateId = template.id.trim()
        require(!PlantChecklistCatalog.isPackagedChecklistId(candidateId)) {
            "Packaged default checklist templates cannot be modified or overwritten."
        }
        val current = getCustomChecklists().first().toMutableList()
        val resolvedId = candidateId.ifEmpty { "CHK-CUSTOM-$now" }
        val normalizedEquipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(template.equipmentId)
        val existing = current.firstOrNull { it.id.equals(resolvedId, ignoreCase = true) }
        val saved = template.copy(
            id = existing?.id ?: resolvedId,
            title = template.title.trim(),
            description = template.description.trim(),
            equipmentId = normalizedEquipmentId,
            createdAt = existing?.createdAt ?: template.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        current.removeAll { it.id.equals(saved.id, ignoreCase = true) }
        val updated = (listOf(saved) + current).sortedByDescending { it.updatedAt }
        persistCustomChecklists(updated)

        // Keep execution metadata equipment aligned when custom template equipment changes
        val currentMeta = getAllChecklistExecutionMetadata().first().toMutableMap()
        val existingMeta = currentMeta[saved.id]
        if (existingMeta != null && !existingMeta.equipmentId.equals(normalizedEquipmentId, ignoreCase = true)) {
            currentMeta[saved.id] = existingMeta.copy(
                equipmentId = normalizedEquipmentId,
                updatedAt = now
            )
            persistExecutionMetadata(currentMeta)
        }
        return saved
    }

    override suspend fun deleteCustomChecklist(checklistId: String): Boolean {
        val normalizedId = checklistId.trim()
        if (normalizedId.isEmpty() || PlantChecklistCatalog.isPackagedChecklistId(normalizedId)) {
            return false
        }
        val current = getCustomChecklists().first()
        val filtered = current.filterNot { it.id.equals(normalizedId, ignoreCase = true) }
        if (filtered.size == current.size) {
            return false
        }
        persistCustomChecklists(filtered)

        val currentMeta = getAllChecklistExecutionMetadata().first().toMutableMap()
        if (currentMeta.keys.removeAll { it.equals(normalizedId, ignoreCase = true) }) {
            persistExecutionMetadata(currentMeta)
        }

        runInTransaction {
            checklistDao.clearChecklistProgress(normalizedId)
            bookmarkDao.deleteBookmark(BookmarkEntityType.CHECKLIST.name, normalizedId)
        }
        return true
    }

    override fun getAllChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> {
        return userPreferencesRepository?.observeChecklistExecutionMetadata() ?: fallbackExecutionMetadataFlow
    }

    override suspend fun saveChecklistExecutionMetadata(metadata: ChecklistExecutionMetadata) {
        val normalizedId = metadata.checklistId.trim()
        if (normalizedId.isEmpty()) return
        val normalized = metadata.copy(
            checklistId = normalizedId,
            equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(metadata.equipmentId),
            operatorName = metadata.operatorName.trim(),
            updatedAt = metadata.updatedAt.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
        val currentMeta = getAllChecklistExecutionMetadata().first().toMutableMap()
        currentMeta.keys.removeAll { it.equals(normalizedId, ignoreCase = true) }
        currentMeta[normalizedId] = normalized
        persistExecutionMetadata(currentMeta)
    }

    private suspend fun persistCustomChecklists(list: List<CustomChecklistTemplate>) {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.saveCustomChecklists(list)
        } else {
            fallbackCustomChecklistsFlow.value = list
        }
    }

    private suspend fun persistExecutionMetadata(map: Map<String, ChecklistExecutionMetadata>) {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.saveChecklistExecutionMetadata(map)
        } else {
            fallbackExecutionMetadataFlow.value = map
        }
    }

    override suspend fun exportUserPersonalDataJson(): String {
        val notes = userNoteDao.getAllNotes().first()
        val bookmarks = bookmarkDao.getAllBookmarks().first()
        val history = calculationDao.getCalculationHistory().first()
        val checklists = checklistDao.getAllProgress().first()
        val customChecklists = getCustomChecklists().first()
        val executionMetadata = getAllChecklistExecutionMetadata().first().values.toList()

        // SHA-256 signed backup v3 covering Room personal tables plus DataStore checklist workflows
        return BackupJsonCodec.encodeBackup(
            notes = notes,
            bookmarks = bookmarks,
            history = history,
            checklists = checklists,
            customChecklists = customChecklists,
            executionMetadata = executionMetadata
        )
    }

    override suspend fun importUserPersonalDataJson(jsonString: String, overwrite: Boolean): BackupImportResult {
        val payload = when (val parsed = BackupJsonCodec.parseAndValidate(jsonString)) {
            is BackupParseOutcome.Invalid -> return parsed.failure
            is BackupParseOutcome.Valid -> parsed.payload
        }

        val prevNotes = userNoteDao.getAllNotes().first()
        val prevBookmarks = bookmarkDao.getAllBookmarks().first()
        val prevHistory = calculationDao.getCalculationHistory().first()
        val prevChecklists = checklistDao.getAllProgress().first()
        val prevCustomChecklists = getCustomChecklists().first()
        val prevExecutionMetadata = getAllChecklistExecutionMetadata().first()

        var notesAdded = 0
        var bookmarksAdded = 0
        var historyAdded = 0
        var checklistsUpdated = 0
        var roomCommitted = false

        return try {
            runInTransaction {
                if (overwrite) {
                    userNoteDao.deleteAllNotes()
                    bookmarkDao.deleteAllBookmarks()
                    calculationDao.deleteAllHistory()
                    checklistDao.deleteAllChecklistProgress()
                }

                val existingNoteKeys = if (overwrite) emptySet() else {
                    userNoteDao.getAllNotes().first().map {
                        "${it.equipmentId}|${it.title}|${it.body}|${it.createdAt}"
                    }.toHashSet()
                }

                for (note in payload.notes) {
                    val key = "${note.equipmentId}|${note.title}|${note.body}|${note.createdAt}"
                    if (key !in existingNoteKeys) {
                        userNoteDao.insertNote(note)
                        notesAdded++
                    }
                }

                for (bm in payload.bookmarks) {
                    if (overwrite || bookmarkDao.isBookmarked(bm.entityType, bm.entityId) == 0) {
                        bookmarkDao.insertBookmark(bm)
                        bookmarksAdded++
                    }
                }

                val existingHistoryKeys = if (overwrite) emptySet() else {
                    calculationDao.getCalculationHistory().first().map {
                        "${it.calculationId}|${it.timestamp}|${it.inputsJson}"
                    }.toHashSet()
                }

                for (hist in payload.calculationHistory) {
                    val key = "${hist.calculationId}|${hist.timestamp}|${hist.inputsJson}"
                    if (key !in existingHistoryKeys) {
                        calculationDao.insertHistory(hist)
                        historyAdded++
                    }
                }

                for (chk in payload.checklistProgress) {
                    checklistDao.upsertProgress(chk)
                    checklistsUpdated++
                }
            }
            roomCommitted = true

            val nextCustomChecklists = if (overwrite) {
                payload.customChecklists.distinctBy { it.id.trim().uppercase() }
            } else {
                val merged = prevCustomChecklists.toMutableList()
                val seenIds = merged.mapTo(mutableSetOf()) { it.id.trim().uppercase() }
                for (tpl in payload.customChecklists) {
                    val key = tpl.id.trim().uppercase()
                    if (seenIds.add(key)) {
                        merged += tpl
                    }
                }
                merged
            }.sortedByDescending { it.updatedAt }
            val customChecklistsImported = if (overwrite) {
                nextCustomChecklists.size
            } else {
                val prevIds = prevCustomChecklists.mapTo(mutableSetOf()) { it.id.trim().uppercase() }
                nextCustomChecklists.count { it.id.trim().uppercase() !in prevIds }
            }

            val nextExecutionMetadata = if (overwrite) {
                buildMap {
                    for (meta in payload.checklistExecutionMetadata) {
                        put(meta.checklistId, meta)
                    }
                }
            } else {
                val merged = prevExecutionMetadata.toMutableMap()
                for (meta in payload.checklistExecutionMetadata) {
                    val existingKey = merged.keys.firstOrNull { it.equals(meta.checklistId, ignoreCase = true) }
                    if (existingKey == null) {
                        merged[meta.checklistId] = meta
                    }
                }
                merged
            }
            val executionMetadataImported = if (overwrite) {
                nextExecutionMetadata.size
            } else {
                val prevMetaIds = prevExecutionMetadata.keys.mapTo(mutableSetOf()) { it.trim().uppercase() }
                nextExecutionMetadata.keys.count { it.trim().uppercase() !in prevMetaIds }
            }

            if (overwrite || customChecklistsImported > 0) {
                persistCustomChecklists(nextCustomChecklists)
            }
            if (overwrite || executionMetadataImported > 0) {
                persistExecutionMetadata(nextExecutionMetadata)
            }

            BackupImportResult.Success(
                notesImported = notesAdded,
                bookmarksImported = bookmarksAdded,
                historyImported = historyAdded,
                checklistsImported = checklistsUpdated,
                customChecklistsImported = customChecklistsImported,
                executionMetadataImported = executionMetadataImported
            )
        } catch (_: SQLiteException) {
            BackupImportResult.Failure(
                BackupImportFailureReason.DATABASE_ERROR,
                "Database transaction failed while restoring backup. Existing data was not modified."
            )
        } catch (_: IllegalStateException) {
            if (roomCommitted) {
                restorePreImportSnapshot(
                    prevNotes = prevNotes,
                    prevBookmarks = prevBookmarks,
                    prevHistory = prevHistory,
                    prevChecklists = prevChecklists,
                    prevCustomChecklists = prevCustomChecklists,
                    prevExecutionMetadata = prevExecutionMetadata
                )
            } else {
                BackupImportResult.Failure(
                    BackupImportFailureReason.DATABASE_ERROR,
                    "Database state prevented backup restore. Existing data was not modified."
                )
            }
        } catch (_: Exception) {
            if (roomCommitted) {
                restorePreImportSnapshot(
                    prevNotes = prevNotes,
                    prevBookmarks = prevBookmarks,
                    prevHistory = prevHistory,
                    prevChecklists = prevChecklists,
                    prevCustomChecklists = prevCustomChecklists,
                    prevExecutionMetadata = prevExecutionMetadata
                )
            } else {
                BackupImportResult.Failure(
                    BackupImportFailureReason.UNEXPECTED_ERROR,
                    "Unexpected error occurred while restoring backup."
                )
            }
        }
    }

    private suspend fun restorePreImportSnapshot(
        prevNotes: List<com.researchbangla.mechanicalpronotes.data.local.database.entity.UserNoteEntity>,
        prevBookmarks: List<com.researchbangla.mechanicalpronotes.data.local.database.entity.UserBookmarkEntity>,
        prevHistory: List<CalculationHistoryEntity>,
        prevChecklists: List<com.researchbangla.mechanicalpronotes.data.local.database.entity.ChecklistProgressEntity>,
        prevCustomChecklists: List<CustomChecklistTemplate>,
        prevExecutionMetadata: Map<String, ChecklistExecutionMetadata>
    ): BackupImportResult.Failure {
        val prefsRolledBack = runCatching {
            persistCustomChecklists(prevCustomChecklists)
            persistExecutionMetadata(prevExecutionMetadata)
        }.isSuccess
        val roomRolledBack = runCatching {
            runInTransaction {
                userNoteDao.deleteAllNotes()
                bookmarkDao.deleteAllBookmarks()
                calculationDao.deleteAllHistory()
                checklistDao.deleteAllChecklistProgress()
                prevNotes.forEach { userNoteDao.insertNote(it) }
                prevBookmarks.forEach { bookmarkDao.insertBookmark(it) }
                prevHistory.forEach { calculationDao.insertHistory(it) }
                prevChecklists.forEach { checklistDao.upsertProgress(it) }
            }
        }.isSuccess
        val rollbackNote = if (prefsRolledBack && roomRolledBack) {
            "Previous Room and checklist workflow records were restored."
        } else {
            "Compensating rollback was only partially completed across storage stores; verify saved notes and custom checklists."
        }
        return BackupImportResult.Failure(
            BackupImportFailureReason.DATABASE_ERROR,
            "Failed while saving checklist workflow preferences after database restore. $rollbackNote"
        )
    }

    override suspend fun clearAllPersonalData(): ClearPersonalDataResult {
        return try {
            var notesCount = 0
            var bookmarksCount = 0
            var historyCount = 0
            var checklistCount = 0
            val customChecklistsCount = getCustomChecklists().first().size

            runInTransaction {
                notesCount = userNoteDao.getAllNotes().first().size
                bookmarksCount = bookmarkDao.getAllBookmarks().first().size
                historyCount = calculationDao.getCalculationHistory().first().size
                checklistCount = checklistDao.getAllProgress().first().size

                userNoteDao.deleteAllNotes()
                bookmarkDao.deleteAllBookmarks()
                calculationDao.deleteAllHistory()
                checklistDao.deleteAllChecklistProgress()
            }

            if (userPreferencesRepository != null) {
                userPreferencesRepository.clearChecklistWorkflowPreferences()
            } else {
                fallbackCustomChecklistsFlow.value = emptyList()
                fallbackExecutionMetadataFlow.value = emptyMap()
            }

            ClearPersonalDataResult.Success(
                notesDeleted = notesCount,
                bookmarksDeleted = bookmarksCount,
                historyDeleted = historyCount,
                checklistEntriesDeleted = checklistCount,
                customChecklistsDeleted = customChecklistsCount
            )
        } catch (_: SQLiteException) {
            ClearPersonalDataResult.Failure(
                "Database transaction failed while clearing personal data. Existing user records were not modified."
            )
        } catch (_: IllegalStateException) {
            ClearPersonalDataResult.Failure(
                "Database state prevented clearing personal data. Existing user records were not modified."
            )
        } catch (_: Exception) {
            ClearPersonalDataResult.Failure(
                "Unexpected error occurred while clearing personal data. Existing user records were not modified."
            )
        }
    }
}
