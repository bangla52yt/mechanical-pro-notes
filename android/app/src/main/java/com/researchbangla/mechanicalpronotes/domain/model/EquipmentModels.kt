package com.researchbangla.mechanicalpronotes.domain.model

/**
 * Top-level classification representing the 16 distinct machinery and utility families (A to P)
 * in textile and garment washing plants.
 */
data class SystemFamily(
    val id: String,
    val code: String,
    val name: String,
    val bengaliName: String,
    val iconName: String,
    val sortOrder: Int,
    val isDedicatedBoilerModule: Boolean = false
)

/**
 * Sub-category grouping equipment within a system family.
 */
data class EquipmentCategory(
    val id: String,
    val familyId: String,
    val name: String,
    val bengaliName: String,
    val description: String? = null
)

/**
 * Primary domain entity representing one of the 64 unique machinery assets.
 */
data class Equipment(
    val id: String,
    val categoryId: String,
    val familyId: String,
    val name: String,
    val bengaliName: String,
    val aliases: List<String> = emptyList(),
    val contentDepth: ContentDepth,
    val criticalityTier: CriticalityTier,
    val capacityRange: String? = null,
    val operatingTemperature: String? = null,
    val primaryDriveType: String? = null,
    val lubricationRegime: String? = null,
    val statutoryRequirement: String? = null,
    val subtypes: List<EquipmentSubtype> = emptyList(),
    val components: List<Component> = emptyList(),
    val technicalParameters: List<TechnicalParameter> = emptyList(),
    val knowledgeSections: List<KnowledgeSection> = emptyList(),
    val failureModes: List<FailureMode> = emptyList(),
    val maintenanceProcedures: List<MaintenanceProcedure> = emptyList(),
    val isPlantDependent: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Equipment ID cannot be blank." }
        require(name.isNotBlank()) { "Equipment name cannot be blank." }
        require(bengaliName.isNotBlank()) { "Bengali name cannot be blank." }
    }
}

enum class CriticalityTier(val displayName: String) {
    CRITICAL_UTILITY("Critical Utility"),
    PRIMARY_PRODUCTION("Primary Production"),
    SECONDARY_AUXILIARY("Secondary Auxiliary"),
    SAFETY_CRITICAL("Safety Critical")
}

data class EquipmentSubtype(
    val id: String,
    val equipmentId: String,
    val name: String,
    val bengaliName: String,
    val distinguishingFeatures: String,
    val typicalCapacity: String
)

data class Component(
    val id: String,
    val equipmentId: String,
    val name: String,
    val bengaliName: String,
    val materialSpecification: String,
    val primaryFunction: String,
    val wearTier: WearTier,
    val maintenanceMethod: String
)

enum class WearTier(val displayName: String) {
    CONSUMABLE("Consumable (Gaskets/O-Rings/Packing)"),
    WEAR_PART("Wear Part (Bearings/Seals/Bushings)"),
    STRUCTURAL_PERMANENT("Structural Permanent (Shell/Shaft/Frame)")
}

data class TechnicalParameter(
    val id: Long = 0,
    val equipmentId: String,
    val group: String,
    val parameterName: String,
    val standardValue: String,
    val unit: String,
    val tolerance: String? = null
)

data class KnowledgeSection(
    val id: Long = 0,
    val equipmentId: String,
    val sectionNumber: Int,
    val sectionTitle: String,
    val contentMarkdown: String,
    val verificationTier: String
) {
    init {
        require(sectionNumber in 1..21) { "Section number must be between 1 and 21." }
    }
}

data class FailureMode(
    val id: String,
    val equipmentId: String,
    val symptom: String,
    val rootCause: String,
    val diagnosticStep: String,
    val correctiveAction: String,
    val vibrationSignature: String? = null,
    val severity: SeverityLevel
)

enum class SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL_SHUTDOWN
}

data class MaintenanceProcedure(
    val id: String,
    val equipmentId: String,
    val title: String,
    val frequency: MaintenanceFrequency,
    val requiredTools: List<String> = emptyList(),
    val safetyLockoutProcedure: String,
    val actionSteps: List<String> = emptyList()
)

enum class MaintenanceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BI_ANNUAL,
    ANNUAL,
    STATUTORY_RENEWAL
}

data class Checklist(
    val id: String,
    val equipmentId: String,
    val title: String,
    val frequency: MaintenanceFrequency,
    val items: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val id: Long = 0,
    val checklistId: String,
    val itemText: String,
    val acceptanceCriteria: String,
    val isStatutoryMandate: Boolean = false
)
