package com.researchbangla.mechanicalpronotes.domain.equipment

import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import java.text.Normalizer

/**
 * Immutable criteria for filtering plant equipment across Family, ContentDepth chips,
 * and normalized offline search query.
 */
data class EquipmentFilterCriteria(
    val familyId: String? = null,
    val selectedDepths: Set<ContentDepth> = emptySet(),
    val query: String = ""
)

/**
 * Pure-Kotlin filtering utilities shared by EquipmentList and Search screens
 * so filtering semantics are 100% deterministic and unit-testable.
 */
object EquipmentFilter {

    /**
     * Filters [items] by optional [criteria]:
     * - `familyId`: null matches all families; otherwise exact family match.
     * - `selectedDepths`: empty set means all depths; non-empty matches union (any selected depth).
     * - `query`: blank matches all; otherwise every whitespace-separated NFC token must match
     *   in ID, English name, Bengali name, aliases, primary drive type, or category ID.
     */
    fun apply(
        items: List<Equipment>,
        criteria: EquipmentFilterCriteria
    ): List<Equipment> {
        val tokens = tokenizeQuery(criteria.query)
        return items.filter { eq ->
            val familyOk = criteria.familyId == null || eq.familyId == criteria.familyId
            val depthOk = criteria.selectedDepths.isEmpty() || eq.contentDepth in criteria.selectedDepths
            val queryOk = tokens.isEmpty() || matchesTokens(eq, tokens)
            familyOk && depthOk && queryOk
        }
    }

    /**
     * Computes per-ContentDepth counts for the currently active family + search query scope
     * (excluding the depth filter itself so users can preview chip counts before toggling).
     */
    fun countByDepth(
        items: List<Equipment>,
        familyId: String? = null,
        query: String = ""
    ): Map<ContentDepth, Int> {
        val scoped = apply(
            items = items,
            criteria = EquipmentFilterCriteria(
                familyId = familyId,
                selectedDepths = emptySet(),
                query = query
            )
        )
        val counts = ContentDepth.entries.associateWith { 0 }.toMutableMap()
        scoped.forEach { eq ->
            counts[eq.contentDepth] = (counts[eq.contentDepth] ?: 0) + 1
        }
        return counts
    }

    internal fun tokenizeQuery(rawQuery: String): List<String> {
        val normalized = Normalizer.normalize(rawQuery.trim(), Normalizer.Form.NFC).lowercase()
        if (normalized.isBlank()) return emptyList()
        return normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    private fun matchesTokens(equipment: Equipment, tokens: List<String>): Boolean {
        val searchable = buildString {
            append(Normalizer.normalize(equipment.id, Normalizer.Form.NFC).lowercase())
            append(' ')
            append(Normalizer.normalize(equipment.name, Normalizer.Form.NFC).lowercase())
            append(' ')
            append(Normalizer.normalize(equipment.bengaliName, Normalizer.Form.NFC).lowercase())
            append(' ')
            equipment.aliases.forEach {
                append(Normalizer.normalize(it, Normalizer.Form.NFC).lowercase())
                append(' ')
            }
            equipment.primaryDriveType?.let {
                append(Normalizer.normalize(it, Normalizer.Form.NFC).lowercase())
                append(' ')
            }
            append(equipment.categoryId.lowercase())
        }
        return tokens.all { token -> searchable.contains(token) }
    }
}
