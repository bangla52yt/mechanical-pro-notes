package com.researchbangla.mechanicalpronotes.data.repository

import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.EquipmentDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.SearchDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.UserNoteDao
import com.researchbangla.mechanicalpronotes.data.mapper.toDomain
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationResultFormatter
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.catalog.EngineeringCatalogSearch
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.model.NoteAssociationHelper
import com.researchbangla.mechanicalpronotes.domain.model.NoteTagHelper
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationHistorySearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationSearchHit
import com.researchbangla.mechanicalpronotes.domain.repository.SearchRepository
import com.researchbangla.mechanicalpronotes.domain.repository.UnifiedSearchResult
import com.researchbangla.mechanicalpronotes.domain.repository.UserNoteSearchHit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchRepositoryImpl(
    private val searchDao: SearchDao,
    private val calculationDao: CalculationDao? = null,
    private val userNoteDao: UserNoteDao? = null,
    private val equipmentDao: EquipmentDao? = null
) : SearchRepository {

    override suspend fun search(query: String, limit: Int): List<Equipment> {
        val trimmed = Normalizer.normalize(query.trim(), Normalizer.Form.NFC)
        if (trimmed.isBlank()) return emptyList()

        return try {
            // Step 1: Attempt SQLite FTS4 query with Unicode-aware multi-word token prefixing
            val ftsQuery = buildSafeFtsQuery(trimmed)
            val ftsResults = if (ftsQuery != null) {
                searchDao.searchFts(ftsQuery, limit)
            } else {
                emptyList()
            }
            if (ftsResults.isNotEmpty()) {
                ftsResults.map { it.toDomain() }
            } else {
                // Step 2: Fallback to SQL LIKE query for fuzzy/hyphenated/Bengali substring match
                searchDao.searchLike(trimmed, limit).map { it.toDomain() }
            }
        } catch (_: Exception) {
            // Robust fallback if FTS query syntax error occurs
            searchDao.searchLike(trimmed, limit).map { it.toDomain() }
        }
    }

    internal fun buildSafeFtsQuery(raw: String): String? {
        val normalized = Normalizer.normalize(raw.trim(), Normalizer.Form.NFC)
        if (normalized.isBlank()) return null

        if (Regex("^[A-Za-z]{2,4}-[A-Za-z0-9-]+$").matches(normalized)) {
            return "\"$normalized\""
        }
        // Include \p{M} (combining marks) so Bengali vowel signs (া, ি, ী, ু, ে, ো) and halant (্) stay attached
        val tokens = Regex("[\\p{L}\\p{M}\\p{N}]+")
            .findAll(normalized)
            .map { "${it.value}*" }
            .toList()
        if (tokens.isEmpty()) return null
        return tokens.joinToString(" ")
    }

    override fun searchFlow(query: String, limit: Int): Flow<List<Equipment>> = flow {
        emit(search(query, limit))
    }

    override suspend fun searchKnowledgeBase(
        query: String,
        limit: Int
    ): UnifiedSearchResult {
        val trimmed = Normalizer.normalize(query.trim(), Normalizer.Form.NFC)
        if (trimmed.isBlank()) {
            return UnifiedSearchResult()
        }
        val tokens = tokenize(trimmed)
        if (tokens.isEmpty()) {
            return UnifiedSearchResult()
        }

        val equipmentHits = search(trimmed, limit)
        val catalogHits = EngineeringCatalogSearch.searchStaticCatalogs(trimmed, limit)
        val calculationHits = searchCalculations(trimmed, tokens, limit)
        val noteHits = searchPersonalNotes(tokens, limit)
        val historyHits = searchCalculationHistory(tokens, limit)

        return UnifiedSearchResult(
            equipment = equipmentHits,
            catalogHits = catalogHits,
            calculations = calculationHits,
            userNotes = noteHits,
            calculationHistory = historyHits
        )
    }

    private suspend fun searchCalculations(
        normalizedQuery: String,
        tokens: List<String>,
        limit: Int
    ): List<CalculationSearchHit> {
        val dbEntities = try {
            calculationDao?.getAllCalculations()?.firstOrNull().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        val specById = CalculationSpecifications.ALL_CALCULATIONS.associateBy { it.id.uppercase() }
        val definitions: List<CalculationDefinition> = when {
            dbEntities.size >= 20 -> dbEntities.map { entity ->
                entity.toDomain(variables = specById[entity.id.uppercase()]?.variables.orEmpty())
            }
            dbEntities.isNotEmpty() -> {
                val fromDao = dbEntities.map { entity ->
                    entity.toDomain(variables = specById[entity.id.uppercase()]?.variables.orEmpty())
                }
                val seenIds = fromDao.map { it.id.uppercase() }.toSet()
                fromDao + CalculationSpecifications.ALL_CALCULATIONS.filter { it.id.uppercase() !in seenIds }
            }
            else -> CalculationSpecifications.ALL_CALCULATIONS
        }.distinctBy { it.id.trim().uppercase() }

        val lowerQuery = normalizedQuery.lowercase()
        val ranked = definitions.mapIndexedNotNull { index, calc ->
            val variableMetadata = calc.variables.joinToString(" ") { v ->
                "${v.symbol} ${v.name} ${v.unit}"
            }
            val blob = normalizeAll(
                calc.id,
                calc.code,
                calc.title,
                calc.bengaliTitle,
                calc.category,
                calc.engineeringReference,
                calc.assumptions,
                calc.limitations,
                calc.formulaLatex,
                calc.status.name,
                variableMetadata
            )
            if (!tokens.all { blob.contains(it) }) return@mapIndexedNotNull null

            val rank = when {
                calc.id.equals(normalizedQuery, ignoreCase = true) ||
                    calc.code.equals(normalizedQuery, ignoreCase = true) -> 0
                normalizeAll(calc.id, calc.code, calc.title, calc.bengaliTitle, calc.category)
                    .contains(lowerQuery) -> 1
                else -> 2
            }
            Triple(rank, index, calc)
        }

        return ranked
            .sortedWith(compareBy<Triple<Int, Int, CalculationDefinition>> { it.first }.thenBy { it.second })
            .take(limit)
            .map { (_, _, calc) ->
                CalculationSearchHit(
                    id = calc.id,
                    code = calc.code,
                    title = calc.title,
                    bengaliTitle = calc.bengaliTitle,
                    category = calc.category,
                    engineeringReference = calc.engineeringReference,
                    status = calc.status
                )
            }
    }

    private suspend fun searchPersonalNotes(
        tokens: List<String>,
        limit: Int
    ): List<UserNoteSearchHit> {
        val notes = try {
            userNoteDao?.getAllNotes()?.firstOrNull().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        if (notes.isEmpty()) return emptyList()

        val equipmentNameById = try {
            equipmentDao?.getAllEquipment()?.firstOrNull().orEmpty()
                .associate { it.id.trim().uppercase() to it.name }
        } catch (_: Exception) {
            emptyMap()
        }

        return notes
            .sortedByDescending { it.updatedAt }
            .mapNotNull { entity ->
                val rawEq = entity.equipmentId.trim()
                val isUnassociated = NoteAssociationHelper.isUnassociated(rawEq)
                val safeEq = if (isUnassociated) NoteAssociationHelper.UNASSOCIATED_EQUIPMENT_ID else rawEq
                val eqName = if (isUnassociated) null else equipmentNameById[safeEq.uppercase()]
                val safeTitle = entity.title.trim()
                val safeBody = entity.body.trim()
                val parsedTags = NoteTagHelper.parseTagsCsv(entity.tagsCsv)
                val eqSearchText = if (isUnassociated) "general unassociated" else "$safeEq ${eqName.orEmpty()}"
                val blob = normalizeAll(
                    eqSearchText,
                    safeTitle,
                    safeBody,
                    parsedTags.joinToString(" ")
                )
                if (!tokens.all { blob.contains(it) }) return@mapNotNull null

                val displayTitle = safeTitle.ifEmpty {
                    when {
                        isUnassociated -> "General Field Note"
                        else -> "Field Note ($safeEq)"
                    }
                }
                val bodySnippet = safeBody.ifEmpty { "No note observation text" }
                UserNoteSearchHit(
                    id = entity.id,
                    equipmentId = safeEq,
                    displayTitle = displayTitle,
                    bodySnippet = bodySnippet,
                    tags = parsedTags,
                    updatedAt = entity.updatedAt,
                    equipmentName = eqName
                )
            }
            .take(limit)
    }

    private suspend fun searchCalculationHistory(
        tokens: List<String>,
        limit: Int
    ): List<CalculationHistorySearchHit> {
        val historyEntities = try {
            calculationDao?.getCalculationHistory()?.firstOrNull().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        if (historyEntities.isEmpty()) return emptyList()

        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
        return historyEntities
            .sortedByDescending { it.timestamp }
            .mapNotNull { entity ->
                val outputs = BackupJsonCodec.parseDoubleMap(entity.outputsJson)
                val inputs = BackupJsonCodec.parseDoubleMap(entity.inputsJson)
                val formattedTimestamp = if (entity.timestamp > 0L) {
                    formatter.format(Date(entity.timestamp))
                } else {
                    "Unknown time"
                }
                val resultSummary = if (outputs.isNotEmpty()) {
                    outputs.entries.joinToString(" • ") { (k, v) ->
                        val (label, formattedVal) = CalculationResultFormatter.formatSecondaryEntry(k, v)
                        "$label: $formattedVal"
                    }
                } else {
                    "No stored outputs"
                }
                val inputsSummary = if (inputs.isNotEmpty()) {
                    inputs.entries.take(3).joinToString(" • ") { (k, v) ->
                        "$k = ${CalculationResultFormatter.formatNumber(v, k)}"
                    }
                } else {
                    ""
                }
                val safeRemark = entity.userRemark?.trim()?.takeIf { it.isNotEmpty() }
                val rawOutputTokens = outputs.entries.joinToString(" ") { (k, v) -> "$k $v" }

                val blob = normalizeAll(
                    entity.calculationId,
                    entity.calculationTitle,
                    formattedTimestamp,
                    resultSummary,
                    rawOutputTokens,
                    inputsSummary,
                    safeRemark.orEmpty()
                )
                if (!tokens.all { blob.contains(it) }) return@mapNotNull null

                CalculationHistorySearchHit(
                    id = entity.id,
                    calculationId = entity.calculationId,
                    calculationTitle = entity.calculationTitle,
                    timestamp = entity.timestamp,
                    formattedTimestamp = formattedTimestamp,
                    resultSummary = resultSummary,
                    inputsSummary = inputsSummary,
                    userRemark = safeRemark
                )
            }
            .take(limit)
    }

    private fun tokenize(raw: String): List<String> {
        val nfc = Normalizer.normalize(raw.trim(), Normalizer.Form.NFC).lowercase()
        if (nfc.isBlank()) return emptyList()
        return nfc.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    private fun normalizeAll(vararg parts: String): String =
        Normalizer.normalize(parts.joinToString(" "), Normalizer.Form.NFC).lowercase()
}
