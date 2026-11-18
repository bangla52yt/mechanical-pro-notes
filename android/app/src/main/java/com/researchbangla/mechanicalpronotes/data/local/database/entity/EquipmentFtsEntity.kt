package com.researchbangla.mechanicalpronotes.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Full-Text Search Virtual Table for Equipment.
 *
 * Indexed for sub-50ms offline queries across English names, Bengali names,
 * aliases, capacity, and drive types.
 */
@Fts4
@Entity(tableName = "equipment_fts")
data class EquipmentFtsEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,

    val id: String,
    val name: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    @ColumnInfo(name = "aliases_csv")
    val aliasesCsv: String?,
    @ColumnInfo(name = "content_depth_status")
    val contentDepthStatus: String,
    @ColumnInfo(name = "primary_drive_type")
    val primaryDriveType: String?
)
