package com.researchbangla.mechanicalpronotes.data.local.database.entity

import androidx.room.*

@Entity(tableName = "system_family")
data class SystemFamilyEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val name: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "is_dedicated_boiler")
    val isDedicatedBoiler: Boolean = false
)

@Entity(
    tableName = "equipment_category",
    foreignKeys = [
        ForeignKey(
            entity = SystemFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["family_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["family_id"])]
)
data class EquipmentCategoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "family_id")
    val familyId: String,
    val name: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    val description: String? = null
)

@Entity(
    tableName = "equipment",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SystemFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["family_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["family_id"]),
        Index(value = ["content_depth_status"])
    ]
)
data class EquipmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "family_id")
    val familyId: String,
    val name: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    @ColumnInfo(name = "aliases_csv")
    val aliasesCsv: String? = null,
    @ColumnInfo(name = "content_depth_status")
    val contentDepthStatus: String,
    @ColumnInfo(name = "criticality_tier")
    val criticalityTier: String,
    @ColumnInfo(name = "capacity_range")
    val capacityRange: String? = null,
    @ColumnInfo(name = "operating_temperature")
    val operatingTemperature: String? = null,
    @ColumnInfo(name = "primary_drive_type")
    val primaryDriveType: String? = null,
    @ColumnInfo(name = "lubrication_regime")
    val lubricationRegime: String? = null,
    @ColumnInfo(name = "statutory_requirement")
    val statutoryRequirement: String? = null,
    @ColumnInfo(name = "is_plant_dependent")
    val isPlantDependent: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "equipment_subtype",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipment_id"])]
)
data class EquipmentSubtypeEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    @ColumnInfo(name = "subtype_name")
    val subtypeName: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    @ColumnInfo(name = "distinguishing_features")
    val distinguishingFeatures: String,
    @ColumnInfo(name = "typical_capacity")
    val typicalCapacity: String
)

@Entity(
    tableName = "component",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["equipment_id"]),
        Index(value = ["wear_tier"])
    ]
)
data class ComponentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    val name: String,
    @ColumnInfo(name = "bengali_name")
    val bengaliName: String,
    @ColumnInfo(name = "material_specification")
    val materialSpecification: String,
    @ColumnInfo(name = "primary_function")
    val primaryFunction: String,
    @ColumnInfo(name = "wear_tier")
    val wearTier: String,
    @ColumnInfo(name = "maintenance_method")
    val maintenanceMethod: String
)

@Entity(
    tableName = "technical_parameter",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipment_id"])]
)
data class TechnicalParameterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    @ColumnInfo(name = "parameter_group")
    val parameterGroup: String,
    @ColumnInfo(name = "parameter_name")
    val parameterName: String,
    @ColumnInfo(name = "standard_value")
    val standardValue: String,
    val unit: String,
    val tolerance: String? = null
)

@Entity(
    tableName = "knowledge_section",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["equipment_id", "section_number"], unique = true)
    ]
)
data class KnowledgeSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    @ColumnInfo(name = "section_number")
    val sectionNumber: Int,
    @ColumnInfo(name = "section_title")
    val sectionTitle: String,
    @ColumnInfo(name = "content_markdown")
    val contentMarkdown: String,
    @ColumnInfo(name = "verification_tier")
    val verificationTier: String
)

@Entity(
    tableName = "failure_mode",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipment_id"])]
)
data class FailureModeEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    val symptom: String,
    @ColumnInfo(name = "root_cause")
    val rootCause: String,
    @ColumnInfo(name = "diagnostic_step")
    val diagnosticStep: String,
    @ColumnInfo(name = "corrective_action")
    val correctiveAction: String,
    @ColumnInfo(name = "vibration_signature")
    val vibrationSignature: String? = null,
    val severity: String
)

@Entity(
    tableName = "maintenance_procedure",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["equipment_id"])]
)
data class MaintenanceProcedureEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String,
    val title: String,
    val frequency: String,
    @ColumnInfo(name = "required_tools_csv")
    val requiredToolsCsv: String,
    @ColumnInfo(name = "safety_lockout_procedure")
    val safetyLockoutProcedure: String,
    @ColumnInfo(name = "action_steps_json")
    val actionStepsJson: String
)

@Entity(
    tableName = "calculation",
    indices = [Index(value = ["code"], unique = true)]
)
data class CalculationEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val title: String,
    @ColumnInfo(name = "bengali_title")
    val bengaliTitle: String,
    val category: String,
    @ColumnInfo(name = "formula_latex")
    val formulaLatex: String,
    @ColumnInfo(name = "engineering_reference")
    val engineeringReference: String,
    val assumptions: String,
    val limitations: String,
    val status: String
)

@Entity(
    tableName = "formula_variable",
    foreignKeys = [
        ForeignKey(
            entity = CalculationEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["calculation_id"])]
)
data class FormulaVariableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "calculation_id")
    val calculationId: String,
    val symbol: String,
    val name: String,
    val unit: String,
    @ColumnInfo(name = "default_value")
    val defaultValue: Double? = null,
    @ColumnInfo(name = "min_bound")
    val minBound: Double? = null,
    @ColumnInfo(name = "max_bound")
    val maxBound: Double? = null,
    @ColumnInfo(name = "is_required")
    val isRequired: Boolean = true
)
