package com.researchbangla.mechanicalpronotes.domain.model

/**
 * Dynamic user-created data models.
 *
 * CRITICAL SEPARATION MANDATE:
 * These entities are authored exclusively by the project owner and stored in dynamic tables.
 * They must NEVER be deleted, overwritten, or modified by updates to built-in technical data.
 */

data class UserNote(
    val id: Long = 0,
    val equipmentId: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(equipmentId.isNotBlank()) { "User note must be linked to a valid equipment ID." }
        require(body.isNotBlank()) { "User note body cannot be blank." }
    }
}

object NoteAssociationHelper {
    const val UNASSOCIATED_EQUIPMENT_ID = "UNASSOCIATED"
    private val UNASSOCIATED_TOKENS = setOf("UNASSOCIATED", "NONE", "GENERAL")

    fun isUnassociated(equipmentId: String?): Boolean {
        val trimmed = equipmentId?.trim().orEmpty()
        return trimmed.isEmpty() || trimmed.uppercase() in UNASSOCIATED_TOKENS
    }

    fun normalizeEquipmentIdForStorage(equipmentId: String?): String {
        return if (isUnassociated(equipmentId)) {
            UNASSOCIATED_EQUIPMENT_ID
        } else {
            equipmentId!!.trim()
        }
    }
}

object NoteTagHelper {
    const val TAG_DELIMITER = "|||"
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    fun normalizeSingleTag(raw: String): String {
        return raw.trim().replace(MULTI_SPACE_REGEX, " ")
    }

    fun normalizeTags(tags: Iterable<String>): List<String> {
        return tags
            .asSequence()
            .map(::normalizeSingleTag)
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .toList()
    }

    fun parseTagInput(rawInput: String): List<String> {
        if (rawInput.isBlank()) return emptyList()
        return normalizeTags(rawInput.split(",", "\n", TAG_DELIMITER))
    }

    fun parseTagsCsv(tagsCsv: String): List<String> {
        if (tagsCsv.isBlank()) return emptyList()
        return normalizeTags(tagsCsv.split(TAG_DELIMITER))
    }

    fun formatTagsCsv(tags: Iterable<String>): String {
        return normalizeTags(tags).joinToString(TAG_DELIMITER)
    }
}

data class UserBookmark(
    val id: Long = 0,
    val entityType: BookmarkEntityType,
    val entityId: String,
    val entityTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookmarkEntityType {
    EQUIPMENT,
    BOILER_SECTION,
    CALCULATION,
    CHECKLIST,
    TECHNICAL_STANDARD
}

data class ChecklistProgress(
    val id: Long = 0,
    val checklistId: String,
    val itemId: Int,
    val isCompleted: Boolean,
    val completionTimestamp: Long? = null,
    val remarks: String? = null
)

data class CalculationHistoryItem(
    val id: Long = 0,
    val calculationId: String,
    val calculationTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val inputValues: Map<String, Double>,
    val outputValues: Map<String, Double>,
    val userRemark: String? = null
)

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class UnitSystem(
    val displayName: String,
    val summary: String
) {
    SI(
        displayName = "SI",
        summary = "Strict SI display equivalents (K, kPa(a), m, W, m³/s, rad/s)"
    ),
    METRIC(
        displayName = "Metric",
        summary = "Plant engineering metric defaults (°C, bar(g), mm, kW, m³/h, RPM)"
    ),
    IMPERIAL(
        displayName = "Imperial",
        summary = "US/Imperial display equivalents (°F, psi(g), in/ft, hp, GPM/CFM, lb)"
    )
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isLargePlantFloorFont: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.METRIC
)

enum class BackupImportFailureReason {
    INVALID_JSON,
    MISSING_REQUIRED_FIELDS,
    UNSUPPORTED_APP,
    UNSUPPORTED_VERSION,
    CHECKSUM_MISMATCH,
    DATABASE_ERROR,
    UNEXPECTED_ERROR
}

sealed interface BackupImportResult {
    data class Success(
        val notesImported: Int,
        val bookmarksImported: Int,
        val historyImported: Int,
        val checklistsImported: Int,
        val customChecklistsImported: Int = 0,
        val executionMetadataImported: Int = 0
    ) : BackupImportResult

    data class Failure(
        val reason: BackupImportFailureReason,
        val message: String
    ) : BackupImportResult
}

sealed interface ClearPersonalDataResult {
    data class Success(
        val notesDeleted: Int,
        val bookmarksDeleted: Int,
        val historyDeleted: Int,
        val checklistEntriesDeleted: Int,
        val customChecklistsDeleted: Int = 0
    ) : ClearPersonalDataResult {
        val totalDeleted: Int
            get() = notesDeleted + bookmarksDeleted + historyDeleted +
                checklistEntriesDeleted + customChecklistsDeleted
    }

    data class Failure(
        val message: String
    ) : ClearPersonalDataResult
}

enum class ChecklistShift(val label: String) {
    DAY("Day"),
    EVENING("Evening"),
    NIGHT("Night"),
    UNSPECIFIED("Other / Not specified");

    companion object {
        fun fromStored(raw: String?): ChecklistShift {
            val trimmed = raw?.trim().orEmpty()
            return entries.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true) ||
                    it.label.equals(trimmed, ignoreCase = true)
            } ?: UNSPECIFIED
        }
    }
}

data class ChecklistExecutionMetadata(
    val checklistId: String,
    val equipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
    val operatorName: String = "",
    val shift: ChecklistShift = ChecklistShift.UNSPECIFIED,
    val updatedAt: Long = 0L
)

data class CustomChecklistItem(
    val id: Int,
    val text: String,
    val category: String = "Inspection",
    val isCrucial: Boolean = false
)

data class CustomChecklistTemplate(
    val id: String,
    val title: String,
    val description: String = "",
    val equipmentId: String = NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID,
    val items: List<CustomChecklistItem>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

