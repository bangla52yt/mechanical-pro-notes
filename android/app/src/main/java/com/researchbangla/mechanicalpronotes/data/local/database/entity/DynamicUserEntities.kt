package com.researchbangla.mechanicalpronotes.data.local.database.entity

import androidx.room.*

/**
 * Dynamic User-Generated Tables.
 *
 * CRITICAL LIFECYCLE RULE:
 * Updates to pre-packaged technical equipment tables MUST NEVER alter, drop,
 * or cascade-delete any records in these tables.
 */

@Entity(
    tableName = "user_note",
    indices = [
        Index(value = ["equipment_id"]),
        Index(value = ["created_at"])
    ]
)
data class UserNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    val title: String,
    val body: String,
    @ColumnInfo(name = "tags_csv")
    val tagsCsv: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_bookmark",
    indices = [
        Index(value = ["entity_type", "entity_id"], unique = true)
    ]
)
data class UserBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "entity_title")
    val entityTitle: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "calculation_history",
    indices = [
        Index(value = ["calculation_id"]),
        Index(value = ["timestamp"])
    ]
)
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "calculation_id")
    val calculationId: String,
    @ColumnInfo(name = "calculation_title")
    val calculationTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "inputs_json")
    val inputsJson: String,
    @ColumnInfo(name = "outputs_json")
    val outputsJson: String,
    @ColumnInfo(name = "user_remark")
    val userRemark: String? = null
)

@Entity(
    tableName = "checklist_progress",
    indices = [
        Index(value = ["checklist_id", "item_id"], unique = true)
    ]
)
data class ChecklistProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "checklist_id")
    val checklistId: String,
    @ColumnInfo(name = "item_id")
    val itemId: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "completion_timestamp")
    val completionTimestamp: Long? = null,
    val remarks: String? = null
)
