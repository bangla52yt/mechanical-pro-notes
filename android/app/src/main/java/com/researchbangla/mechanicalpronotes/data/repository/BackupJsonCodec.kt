package com.researchbangla.mechanicalpronotes.data.repository

import com.researchbangla.mechanicalpronotes.data.local.database.entity.CalculationHistoryEntity
import com.researchbangla.mechanicalpronotes.data.local.database.entity.ChecklistProgressEntity
import com.researchbangla.mechanicalpronotes.data.local.database.entity.UserBookmarkEntity
import com.researchbangla.mechanicalpronotes.data.local.database.entity.UserNoteEntity
import com.researchbangla.mechanicalpronotes.domain.model.BackupImportFailureReason
import com.researchbangla.mechanicalpronotes.domain.model.BackupImportResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.security.MessageDigest

internal data class ValidatedBackupPayload(
    val notes: List<UserNoteEntity>,
    val bookmarks: List<UserBookmarkEntity>,
    val calculationHistory: List<CalculationHistoryEntity>,
    val checklistProgress: List<ChecklistProgressEntity>
)

internal sealed interface BackupParseOutcome {
    data class Valid(val payload: ValidatedBackupPayload) : BackupParseOutcome
    data class Invalid(val failure: BackupImportResult.Failure) : BackupParseOutcome
}

@OptIn(ExperimentalSerializationApi::class)
internal object BackupJsonCodec {

    const val APP_NAME = "Mechanical Pro Notes"
    const val CURRENT_BACKUP_VERSION = 2

    private val pretty2Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    private val pretty4Json = Json {
        prettyPrint = true
    }
    private val compactJson = Json {
        prettyPrint = false
    }

    fun encodeBackup(
        notes: List<UserNoteEntity>,
        bookmarks: List<UserBookmarkEntity>,
        history: List<CalculationHistoryEntity>,
        checklists: List<ChecklistProgressEntity>,
        exportTimestamp: Long = System.currentTimeMillis()
    ): String {
        val baseMetadata = buildJsonObject {
            put("app_name", APP_NAME)
            put("backup_version", CURRENT_BACKUP_VERSION)
            put("export_timestamp", exportTimestamp)
        }

        val notesArray = buildJsonArray {
            for (note in notes) {
                add(buildJsonObject {
                    put("equipment_id", note.equipmentId)
                    put("title", note.title)
                    put("body", note.body)
                    put("tags_csv", note.tagsCsv)
                    put("created_at", note.createdAt)
                    put("updated_at", note.updatedAt)
                })
            }
        }

        val bookmarksArray = buildJsonArray {
            for (bm in bookmarks) {
                add(buildJsonObject {
                    put("entity_type", bm.entityType)
                    put("entity_id", bm.entityId)
                    put("entity_title", bm.entityTitle)
                    put("created_at", bm.createdAt)
                })
            }
        }

        val historyArray = buildJsonArray {
            for (h in history) {
                add(buildJsonObject {
                    put("calculation_id", h.calculationId)
                    put("calculation_title", h.calculationTitle)
                    put("timestamp", h.timestamp)
                    put("inputs_json", h.inputsJson)
                    put("outputs_json", h.outputsJson)
                    put("user_remark", h.userRemark?.let(::JsonPrimitive) ?: JsonNull)
                })
            }
        }

        val checklistArray = buildJsonArray {
            for (c in checklists) {
                add(buildJsonObject {
                    put("checklist_id", c.checklistId)
                    put("item_id", c.itemId)
                    put("is_completed", c.isCompleted)
                    put("completion_timestamp", c.completionTimestamp?.let(::JsonPrimitive) ?: JsonNull)
                    put("remarks", c.remarks?.let(::JsonPrimitive) ?: JsonNull)
                })
            }
        }

        val rawRoot = buildJsonObject {
            put("metadata", baseMetadata)
            put("user_notes", notesArray)
            put("bookmarks", bookmarksArray)
            put("calculation_history", historyArray)
            put("checklist_progress", checklistArray)
        }

        val checksum = sha256(pretty2Json.encodeToString(JsonObject.serializer(), rawRoot))

        val signedMetadata = buildJsonObject {
            put("app_name", APP_NAME)
            put("backup_version", CURRENT_BACKUP_VERSION)
            put("export_timestamp", exportTimestamp)
            put("sha256", checksum)
        }

        val signedRoot = buildJsonObject {
            put("metadata", signedMetadata)
            put("user_notes", notesArray)
            put("bookmarks", bookmarksArray)
            put("calculation_history", historyArray)
            put("checklist_progress", checklistArray)
        }

        return pretty2Json.encodeToString(JsonObject.serializer(), signedRoot)
    }

    fun parseAndValidate(jsonString: String, defaultTimestamp: Long = System.currentTimeMillis()): BackupParseOutcome {
        if (jsonString.isBlank()) {
            return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.INVALID_JSON,
                    "Backup file is empty or contains invalid JSON."
                )
            )
        }

        val rootElement = try {
            compactJson.parseToJsonElement(jsonString)
        } catch (_: Exception) {
            return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.INVALID_JSON,
                    "Invalid or corrupt JSON backup format."
                )
            )
        }

        val root = rootElement as? JsonObject ?: return BackupParseOutcome.Invalid(
            BackupImportResult.Failure(
                BackupImportFailureReason.INVALID_JSON,
                "Backup root must be a valid JSON object."
            )
        )

        val metadata = root["metadata"] as? JsonObject ?: return BackupParseOutcome.Invalid(
            BackupImportResult.Failure(
                BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
                "Backup is missing the required metadata block."
            )
        )

        val appName = metadata.requiredNonBlankString("app_name") ?: return BackupParseOutcome.Invalid(
            BackupImportResult.Failure(
                BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
                "Backup metadata is missing the required app_name field."
            )
        )
        if (appName != APP_NAME) {
            return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.UNSUPPORTED_APP,
                    "Unrecognized backup application identifier."
                )
            )
        }

        val versionElement = metadata["backup_version"] ?: return BackupParseOutcome.Invalid(
            BackupImportResult.Failure(
                BackupImportFailureReason.MISSING_REQUIRED_FIELDS,
                "Backup metadata is missing the required backup_version field."
            )
        )
        val versionPrimitive = versionElement as? JsonPrimitive
        val version = versionPrimitive?.takeIf { it !is JsonNull }?.intOrNull ?: return BackupParseOutcome.Invalid(
            BackupImportResult.Failure(
                BackupImportFailureReason.UNSUPPORTED_VERSION,
                "Invalid backup version format in backup metadata."
            )
        )
        if (version < 1) {
            return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.UNSUPPORTED_VERSION,
                    "Unsupported backup version ($version)."
                )
            )
        }
        if (version > CURRENT_BACKUP_VERSION) {
            return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.UNSUPPORTED_VERSION,
                    "Unsupported future backup version (v$version). This app supports up to v$CURRENT_BACKUP_VERSION."
                )
            )
        }

        if (metadata.containsKey("sha256")) {
            val expectedSha = metadata.requiredNonBlankString("sha256") ?: return BackupParseOutcome.Invalid(
                BackupImportResult.Failure(
                    BackupImportFailureReason.CHECKSUM_MISMATCH,
                    "Backup SHA-256 checksum is empty or malformed."
                )
            )
            val strippedMetadata = JsonObject(metadata.filterKeys { it != "sha256" })
            val canonicalRoot = JsonObject(root.toMutableMap().apply { put("metadata", strippedMetadata) })
            val candidateHashes = listOf(
                sha256(pretty2Json.encodeToString(JsonObject.serializer(), canonicalRoot)),
                sha256(pretty4Json.encodeToString(JsonObject.serializer(), canonicalRoot)),
                sha256(compactJson.encodeToString(JsonObject.serializer(), canonicalRoot))
            )
            if (candidateHashes.none { it.equals(expectedSha, ignoreCase = true) }) {
                return BackupParseOutcome.Invalid(
                    BackupImportResult.Failure(
                        BackupImportFailureReason.CHECKSUM_MISMATCH,
                        "SHA-256 integrity verification failed. Backup file may be corrupted or modified."
                    )
                )
            }
        }

        val notes = mutableListOf<UserNoteEntity>()
        if (root.containsKey("user_notes") && root["user_notes"] !is JsonNull) {
            val arr = root["user_notes"] as? JsonArray ?: return missingField("Malformed user_notes array in backup file.")
            for (el in arr) {
                val obj = el as? JsonObject ?: return missingField("Malformed note entry in user_notes array.")
                val eqId = obj.requiredNonBlankString("equipment_id")
                    ?: return missingField("A user note entry is missing equipment_id.")
                val title = obj.requiredString("title")
                    ?: return missingField("A user note entry is missing title.")
                val body = obj.requiredNonBlankString("body")
                    ?: return missingField("A user note entry is missing body.")
                notes += UserNoteEntity(
                    equipmentId = eqId,
                    title = title,
                    body = body,
                    tagsCsv = obj.optionalString("tags_csv") ?: "",
                    createdAt = obj.optionalLong("created_at") ?: defaultTimestamp,
                    updatedAt = obj.optionalLong("updated_at") ?: defaultTimestamp
                )
            }
        }

        val bookmarks = mutableListOf<UserBookmarkEntity>()
        if (root.containsKey("bookmarks") && root["bookmarks"] !is JsonNull) {
            val arr = root["bookmarks"] as? JsonArray ?: return missingField("Malformed bookmarks array in backup file.")
            for (el in arr) {
                val obj = el as? JsonObject ?: return missingField("Malformed bookmark entry in bookmarks array.")
                val type = obj.requiredNonBlankString("entity_type")
                    ?: return missingField("A bookmark entry is missing entity_type.")
                val entityId = obj.requiredNonBlankString("entity_id")
                    ?: return missingField("A bookmark entry is missing entity_id.")
                val title = obj.requiredString("entity_title")
                    ?: return missingField("A bookmark entry is missing entity_title.")
                bookmarks += UserBookmarkEntity(
                    entityType = type,
                    entityId = entityId,
                    entityTitle = title,
                    createdAt = obj.optionalLong("created_at") ?: defaultTimestamp
                )
            }
        }

        val history = mutableListOf<CalculationHistoryEntity>()
        if (root.containsKey("calculation_history") && root["calculation_history"] !is JsonNull) {
            val arr = root["calculation_history"] as? JsonArray ?: return missingField("Malformed calculation_history array in backup file.")
            for (el in arr) {
                val obj = el as? JsonObject ?: return missingField("Malformed entry in calculation_history array.")
                val calcId = obj.requiredNonBlankString("calculation_id")
                    ?: return missingField("A calculation history entry is missing calculation_id.")
                val calcTitle = obj.requiredString("calculation_title")
                    ?: return missingField("A calculation history entry is missing calculation_title.")
                val inputsJson = obj.requiredString("inputs_json")
                    ?: return missingField("A calculation history entry is missing inputs_json.")
                val outputsJson = obj.requiredString("outputs_json")
                    ?: return missingField("A calculation history entry is missing outputs_json.")
                history += CalculationHistoryEntity(
                    calculationId = calcId,
                    calculationTitle = calcTitle,
                    timestamp = obj.optionalLong("timestamp") ?: defaultTimestamp,
                    inputsJson = inputsJson,
                    outputsJson = outputsJson,
                    userRemark = obj.optionalString("user_remark")
                )
            }
        }

        val checklists = mutableListOf<ChecklistProgressEntity>()
        if (root.containsKey("checklist_progress") && root["checklist_progress"] !is JsonNull) {
            val arr = root["checklist_progress"] as? JsonArray ?: return missingField("Malformed checklist_progress array in backup file.")
            for (el in arr) {
                val obj = el as? JsonObject ?: return missingField("Malformed entry in checklist_progress array.")
                val checklistId = obj.requiredNonBlankString("checklist_id")
                    ?: return missingField("A checklist entry is missing checklist_id.")
                val itemId = obj.requiredInt("item_id")
                    ?: return missingField("A checklist entry is missing item_id.")
                val isCompleted = obj.requiredBoolean("is_completed")
                    ?: return missingField("A checklist entry is missing is_completed.")
                checklists += ChecklistProgressEntity(
                    checklistId = checklistId,
                    itemId = itemId,
                    isCompleted = isCompleted,
                    completionTimestamp = obj.optionalLong("completion_timestamp"),
                    remarks = obj.optionalString("remarks")
                )
            }
        }

        return BackupParseOutcome.Valid(
            ValidatedBackupPayload(
                notes = notes,
                bookmarks = bookmarks,
                calculationHistory = history,
                checklistProgress = checklists
            )
        )
    }

    fun parseDoubleMap(json: String): Map<String, Double> {
        if (json.isBlank()) return emptyMap()
        return try {
            val obj = compactJson.parseToJsonElement(json) as? JsonObject ?: return emptyMap()
            buildMap {
                for ((key, value) in obj) {
                    val d = (value as? JsonPrimitive)?.doubleOrNull
                    if (d != null) put(key, d)
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun encodeDoubleMap(values: Map<String, Double>): String {
        val obj = buildJsonObject {
            for ((k, v) in values) {
                put(k, v)
            }
        }
        return compactJson.encodeToString(JsonObject.serializer(), obj)
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun missingField(msg: String): BackupParseOutcome.Invalid =
        BackupParseOutcome.Invalid(
            BackupImportResult.Failure(BackupImportFailureReason.MISSING_REQUIRED_FIELDS, msg)
        )

    private fun JsonObject.requiredString(key: String): String? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        if (primitive is JsonNull || !primitive.isString) return null
        return primitive.content
    }

    private fun JsonObject.requiredNonBlankString(key: String): String? {
        return requiredString(key)?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.optionalString(key: String): String? {
        val el = this[key] ?: return null
        if (el is JsonNull) return null
        val primitive = el as? JsonPrimitive ?: return null
        return if (primitive.isString) primitive.content else null
    }

    private fun JsonObject.optionalLong(key: String): Long? {
        val el = this[key] ?: return null
        if (el is JsonNull) return null
        return (el as? JsonPrimitive)?.longOrNull
    }

    private fun JsonObject.requiredInt(key: String): Int? {
        val el = this[key] as? JsonPrimitive ?: return null
        if (el is JsonNull) return null
        return el.intOrNull
    }

    private fun JsonObject.requiredBoolean(key: String): Boolean? {
        val el = this[key] as? JsonPrimitive ?: return null
        if (el is JsonNull) return null
        return el.booleanOrNull
    }
}
