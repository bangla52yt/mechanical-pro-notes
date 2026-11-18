package com.researchbangla.mechanicalpronotes.domain.model

/**
 * Status of engineering calculation implementation.
 * Ensures unverified formulas are NEVER executed silently or presented as authoritative.
 */
enum class CalculationStatus {
    /**
     * Fully verified mathematical definition, SI units, boundary conditions,
     * worked examples, and authoritative engineering standard citation.
     */
    IMPLEMENTED,

    /**
     * Mathematical definition pending complete empirical constants or owner site data.
     */
    RESEARCH_PENDING,

    /**
     * Defined conceptually in blueprint but executable algorithm not yet implemented.
     */
    NOT_IMPLEMENTED
}

data class CalculationDefinition(
    val id: String,
    val code: String,
    val title: String,
    val bengaliTitle: String,
    val category: String,
    val formulaLatex: String,
    val engineeringReference: String,
    val assumptions: String,
    val limitations: String,
    val status: CalculationStatus,
    val variables: List<FormulaVariable> = emptyList()
)

data class FormulaVariable(
    val id: Long = 0,
    val calculationId: String,
    val symbol: String,
    val name: String,
    val unit: String,
    val defaultValue: Double? = null,
    val minBound: Double? = null,
    val maxBound: Double? = null,
    val isRequired: Boolean = true
)

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val fieldSymbol: String, val errorMessage: String) : ValidationResult
}

data class CalculationExecutionResult(
    val calculationId: String,
    val primaryOutputName: String,
    val primaryOutputValue: Double,
    val primaryOutputUnit: String,
    val secondaryOutputs: Map<String, Double> = emptyMap(),
    val notesAndRecommendations: String,
    val status: CalculationStatus = CalculationStatus.IMPLEMENTED
)
