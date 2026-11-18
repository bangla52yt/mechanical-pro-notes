package com.researchbangla.mechanicalpronotes.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.researchbangla.mechanicalpronotes.domain.model.AppThemeMode
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistExecutionMetadata
import com.researchbangla.mechanicalpronotes.domain.model.ChecklistShift
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistItem
import com.researchbangla.mechanicalpronotes.domain.model.CustomChecklistTemplate
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.model.UnitSystem
import com.researchbangla.mechanicalpronotes.domain.model.UserPreferences
import com.researchbangla.mechanicalpronotes.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mechanical_pro_user_prefs"
)

class DataStoreUserPreferencesRepository(
    private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LARGE_PLANT_FLOOR_FONT = booleanPreferencesKey("large_plant_floor_font")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val CUSTOM_CHECKLISTS_JSON = stringPreferencesKey("custom_checklists_json")
        val CHECKLIST_EXECUTIONS_JSON = stringPreferencesKey("checklist_executions_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override val userPreferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        val rawTheme = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = runCatching { AppThemeMode.valueOf(rawTheme) }.getOrDefault(AppThemeMode.SYSTEM)
        val largeFont = prefs[Keys.LARGE_PLANT_FLOOR_FONT] ?: false
        val rawUnitSystem = prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
        val unitSystem = runCatching { UnitSystem.valueOf(rawUnitSystem) }.getOrDefault(UnitSystem.METRIC)
        UserPreferences(
            themeMode = themeMode,
            isLargePlantFloorFont = largeFont,
            unitSystem = unitSystem
        )
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setLargePlantFloorFont(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.LARGE_PLANT_FLOOR_FONT] = enabled
        }
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.UNIT_SYSTEM] = unitSystem.name
        }
    }

    override fun observeCustomChecklists(): Flow<List<CustomChecklistTemplate>> =
        context.userPreferencesDataStore.data.map { prefs ->
            parseCustomChecklistsJson(prefs[Keys.CUSTOM_CHECKLISTS_JSON])
        }

    override suspend fun saveCustomChecklists(checklists: List<CustomChecklistTemplate>) {
        val encoded = encodeCustomChecklistsJson(checklists)
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.CUSTOM_CHECKLISTS_JSON] = encoded
        }
    }

    override fun observeChecklistExecutionMetadata(): Flow<Map<String, ChecklistExecutionMetadata>> =
        context.userPreferencesDataStore.data.map { prefs ->
            parseExecutionMetadataJson(prefs[Keys.CHECKLIST_EXECUTIONS_JSON])
        }

    override suspend fun saveChecklistExecutionMetadata(metadata: Map<String, ChecklistExecutionMetadata>) {
        val encoded = encodeExecutionMetadataJson(metadata)
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.CHECKLIST_EXECUTIONS_JSON] = encoded
        }
    }

    override suspend fun clearChecklistWorkflowPreferences() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(Keys.CUSTOM_CHECKLISTS_JSON)
            prefs.remove(Keys.CHECKLIST_EXECUTIONS_JSON)
        }
    }

    private fun encodeCustomChecklistsJson(checklists: List<CustomChecklistTemplate>): String {
        val array = buildJsonArray {
            for (tpl in checklists) {
                add(
                    buildJsonObject {
                        put("id", tpl.id)
                        put("title", tpl.title)
                        put("description", tpl.description)
                        put("equipment_id", tpl.equipmentId)
                        put("created_at", tpl.createdAt)
                        put("updated_at", tpl.updatedAt)
                        put(
                            "items",
                            buildJsonArray {
                                for (item in tpl.items) {
                                    add(
                                        buildJsonObject {
                                            put("id", item.id)
                                            put("text", item.text)
                                            put("category", item.category)
                                            put("is_crucial", item.isCrucial)
                                        }
                                    )
                                }
                            }
                        )
                    }
                )
            }
        }
        return json.encodeToString(JsonArray.serializer(), array)
    }

    private fun parseCustomChecklistsJson(raw: String?): List<CustomChecklistTemplate> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = json.parseToJsonElement(raw) as? JsonArray ?: return emptyList()
            array.mapNotNull { el ->
                val obj = el as? JsonObject ?: return@mapNotNull null
                val id = obj.stringOrNull("id")?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val title = obj.stringOrNull("title")?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val description = obj.stringOrNull("description")?.trim().orEmpty()
                val equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(
                    obj.stringOrNull("equipment_id")
                )
                val createdAt = obj.longOrNull("created_at") ?: 0L
                val updatedAt = obj.longOrNull("updated_at") ?: createdAt
                val itemsArray = obj["items"] as? JsonArray ?: return@mapNotNull null
                val items = itemsArray.mapNotNull { itemEl ->
                    val itemObj = itemEl as? JsonObject ?: return@mapNotNull null
                    val itemId = itemObj.intOrNull("id") ?: return@mapNotNull null
                    val text = itemObj.stringOrNull("text")?.trim()?.takeIf { it.isNotEmpty() }
                        ?: return@mapNotNull null
                    val category = itemObj.stringOrNull("category")?.trim()?.ifEmpty { "Inspection" }
                        ?: "Inspection"
                    val isCrucial = itemObj.booleanOrNull("is_crucial") ?: false
                    CustomChecklistItem(
                        id = itemId,
                        text = text,
                        category = category,
                        isCrucial = isCrucial
                    )
                }
                if (items.isEmpty()) return@mapNotNull null
                CustomChecklistTemplate(
                    id = id,
                    title = title,
                    description = description,
                    equipmentId = equipmentId,
                    items = items,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeExecutionMetadataJson(metadata: Map<String, ChecklistExecutionMetadata>): String {
        val array = buildJsonArray {
            for (entry in metadata.values) {
                add(
                    buildJsonObject {
                        put("checklist_id", entry.checklistId)
                        put("equipment_id", entry.equipmentId)
                        put("operator_name", entry.operatorName)
                        put("shift", entry.shift.name)
                        put("updated_at", entry.updatedAt)
                    }
                )
            }
        }
        return json.encodeToString(JsonArray.serializer(), array)
    }

    private fun parseExecutionMetadataJson(raw: String?): Map<String, ChecklistExecutionMetadata> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val array = json.parseToJsonElement(raw) as? JsonArray ?: return emptyMap()
            buildMap {
                for (el in array) {
                    val obj = el as? JsonObject ?: continue
                    val checklistId = obj.stringOrNull("checklist_id")?.trim()?.takeIf { it.isNotEmpty() }
                        ?: continue
                    val equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(
                        obj.stringOrNull("equipment_id")
                    )
                    val operatorName = obj.stringOrNull("operator_name")?.trim().orEmpty()
                    val shift = ChecklistShift.fromStored(obj.stringOrNull("shift"))
                    val updatedAt = obj.longOrNull("updated_at") ?: 0L
                    put(
                        checklistId,
                        ChecklistExecutionMetadata(
                            checklistId = checklistId,
                            equipmentId = equipmentId,
                            operatorName = operatorName,
                            shift = shift,
                            updatedAt = updatedAt
                        )
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun JsonObject.stringOrNull(key: String): String? {
        val p = this[key] as? JsonPrimitive ?: return null
        return if (p.isString) p.content else null
    }

    private fun JsonObject.intOrNull(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.longOrNull(key: String): Long? =
        (this[key] as? JsonPrimitive)?.longOrNull

    private fun JsonObject.booleanOrNull(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull
}
