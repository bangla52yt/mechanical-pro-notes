package com.researchbangla.mechanicalpronotes.data.mapper

import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.domain.model.*

fun SystemFamilyEntity.toDomain(): SystemFamily = SystemFamily(
    id = id,
    code = code,
    name = name,
    bengaliName = bengaliName,
    iconName = iconName,
    sortOrder = sortOrder,
    isDedicatedBoilerModule = isDedicatedBoiler
)

fun EquipmentCategoryEntity.toDomain(): EquipmentCategory = EquipmentCategory(
    id = id,
    familyId = familyId,
    name = name,
    bengaliName = bengaliName,
    description = description
)

fun EquipmentEntity.toDomain(
    subtypes: List<EquipmentSubtype> = emptyList(),
    components: List<Component> = emptyList(),
    parameters: List<TechnicalParameter> = emptyList(),
    sections: List<KnowledgeSection> = emptyList(),
    failures: List<FailureMode> = emptyList(),
    procedures: List<MaintenanceProcedure> = emptyList()
): Equipment = Equipment(
    id = id,
    categoryId = categoryId,
    familyId = familyId,
    name = name,
    bengaliName = bengaliName,
    aliases = aliasesCsv?.split("|||")?.map { it.trim() } ?: emptyList(),
    contentDepth = ContentDepth.fromString(contentDepthStatus),
    criticalityTier = when (criticalityTier.uppercase()) {
        "CRITICAL_UTILITY" -> CriticalityTier.CRITICAL_UTILITY
        "SAFETY_CRITICAL" -> CriticalityTier.SAFETY_CRITICAL
        "SECONDARY_AUXILIARY" -> CriticalityTier.SECONDARY_AUXILIARY
        else -> CriticalityTier.PRIMARY_PRODUCTION
    },
    capacityRange = capacityRange,
    operatingTemperature = operatingTemperature,
    primaryDriveType = primaryDriveType,
    lubricationRegime = lubricationRegime,
    statutoryRequirement = statutoryRequirement,
    subtypes = subtypes,
    components = components,
    technicalParameters = parameters,
    knowledgeSections = sections,
    failureModes = failures,
    maintenanceProcedures = procedures,
    isPlantDependent = isPlantDependent
)

fun EquipmentSubtypeEntity.toDomain(): EquipmentSubtype = EquipmentSubtype(
    id = id,
    equipmentId = equipmentId,
    name = subtypeName,
    bengaliName = bengaliName,
    distinguishingFeatures = distinguishingFeatures,
    typicalCapacity = typicalCapacity
)

fun ComponentEntity.toDomain(): Component = Component(
    id = id,
    equipmentId = equipmentId,
    name = name,
    bengaliName = bengaliName,
    materialSpecification = materialSpecification,
    primaryFunction = primaryFunction,
    wearTier = when (wearTier.uppercase()) {
        "CONSUMABLE" -> WearTier.CONSUMABLE
        "WEAR_PART" -> WearTier.WEAR_PART
        else -> WearTier.STRUCTURAL_PERMANENT
    },
    maintenanceMethod = maintenanceMethod
)

fun TechnicalParameterEntity.toDomain(): TechnicalParameter = TechnicalParameter(
    id = id,
    equipmentId = equipmentId,
    group = parameterGroup,
    parameterName = parameterName,
    standardValue = standardValue,
    unit = unit,
    tolerance = tolerance
)

fun KnowledgeSectionEntity.toDomain(): KnowledgeSection = KnowledgeSection(
    id = id,
    equipmentId = equipmentId,
    sectionNumber = sectionNumber,
    sectionTitle = sectionTitle,
    contentMarkdown = contentMarkdown,
    verificationTier = verificationTier
)

fun FailureModeEntity.toDomain(): FailureMode = FailureMode(
    id = id,
    equipmentId = equipmentId,
    symptom = symptom,
    rootCause = rootCause,
    diagnosticStep = diagnosticStep,
    correctiveAction = correctiveAction,
    vibrationSignature = vibrationSignature,
    severity = when (severity.uppercase()) {
        "LOW" -> SeverityLevel.LOW
        "HIGH" -> SeverityLevel.HIGH
        "CRITICAL_SHUTDOWN" -> SeverityLevel.CRITICAL_SHUTDOWN
        else -> SeverityLevel.MEDIUM
    }
)

fun MaintenanceProcedureEntity.toDomain(): MaintenanceProcedure = MaintenanceProcedure(
    id = id,
    equipmentId = equipmentId,
    title = title,
    frequency = when (frequency.uppercase()) {
        "DAILY" -> MaintenanceFrequency.DAILY
        "WEEKLY" -> MaintenanceFrequency.WEEKLY
        "MONTHLY" -> MaintenanceFrequency.MONTHLY
        "QUARTERLY" -> MaintenanceFrequency.QUARTERLY
        "BI_ANNUAL" -> MaintenanceFrequency.BI_ANNUAL
        "ANNUAL" -> MaintenanceFrequency.ANNUAL
        else -> MaintenanceFrequency.MONTHLY
    },
    requiredTools = requiredToolsCsv.split("|||").map { it.trim() },
    safetyLockoutProcedure = safetyLockoutProcedure,
    actionSteps = actionStepsJson.split("|||").map { it.trim() }
)

fun UserNoteEntity.toDomain(): UserNote = UserNote(
    id = id,
    equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId),
    title = title,
    body = body,
    tags = NoteTagHelper.parseTagsCsv(tagsCsv),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserNote.toEntity(): UserNoteEntity = UserNoteEntity(
    id = id,
    equipmentId = NoteAssociationHelper.normalizeEquipmentIdForStorage(equipmentId),
    title = title,
    body = body,
    tagsCsv = NoteTagHelper.formatTagsCsv(tags),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserBookmarkEntity.toDomain(): UserBookmark = UserBookmark(
    id = id,
    entityType = when (entityType.uppercase()) {
        "BOILER_SECTION" -> BookmarkEntityType.BOILER_SECTION
        "CALCULATION" -> BookmarkEntityType.CALCULATION
        "CHECKLIST" -> BookmarkEntityType.CHECKLIST
        "TECHNICAL_STANDARD" -> BookmarkEntityType.TECHNICAL_STANDARD
        else -> BookmarkEntityType.EQUIPMENT
    },
    entityId = entityId,
    entityTitle = entityTitle,
    createdAt = createdAt
)

fun ChecklistProgressEntity.toDomain(): ChecklistProgress = ChecklistProgress(
    id = id,
    checklistId = checklistId,
    itemId = itemId,
    isCompleted = isCompleted,
    completionTimestamp = completionTimestamp,
    remarks = remarks
)

fun ChecklistProgress.toEntity(): ChecklistProgressEntity = ChecklistProgressEntity(
    id = id,
    checklistId = checklistId,
    itemId = itemId,
    isCompleted = isCompleted,
    completionTimestamp = completionTimestamp,
    remarks = remarks
)

fun CalculationEntity.toDomain(variables: List<FormulaVariable> = emptyList()): CalculationDefinition =
    CalculationDefinition(
        id = id,
        code = code,
        title = title,
        bengaliTitle = bengaliTitle,
        category = category,
        formulaLatex = formulaLatex,
        engineeringReference = engineeringReference,
        assumptions = assumptions,
        limitations = limitations,
        status = when (status.uppercase()) {
            "IMPLEMENTED" -> CalculationStatus.IMPLEMENTED
            "RESEARCH_PENDING" -> CalculationStatus.RESEARCH_PENDING
            else -> CalculationStatus.NOT_IMPLEMENTED
        },
        variables = variables
    )

fun FormulaVariableEntity.toDomain(): FormulaVariable = FormulaVariable(
    id = id,
    calculationId = calculationId,
    symbol = symbol,
    name = name,
    unit = unit,
    defaultValue = defaultValue,
    minBound = minBound,
    maxBound = maxBound,
    isRequired = isRequired
)
