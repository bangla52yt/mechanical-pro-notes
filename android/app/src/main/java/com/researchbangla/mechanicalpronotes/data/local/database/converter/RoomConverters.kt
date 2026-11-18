package com.researchbangla.mechanicalpronotes.data.local.database.converter

import androidx.room.TypeConverter

class RoomConverters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return if (data.isNullOrBlank()) {
            emptyList()
        } else {
            data.split("|||").map { it.trim() }
        }
    }
}
