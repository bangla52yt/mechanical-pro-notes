package com.researchbangla.mechanicalpronotes

import androidx.room.Database
import androidx.room.Entity
import com.researchbangla.mechanicalpronotes.data.local.database.MechanicalProDatabase
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.data.local.database.migration.DatabaseMigrations
import org.junit.Assert.*
import org.junit.Test

/**
 * Stage 3 Area A — Room Database Migration, Non-Destructive Upgrade & Schema Integrity Contract Tests.
 */
class DatabaseIntegrityAndMigrationTest {

    @Test
    fun earliestAndCurrentVersionsMatchShippedAssetVersion() {
        assertEquals(1, DatabaseMigrations.EARLIEST_SUPPORTED_VERSION)
        assertEquals(1, DatabaseMigrations.CURRENT_DATABASE_VERSION)
        assertTrue(
            "Version 1 baseline requires empty ALL_MIGRATIONS array until v2+ is introduced.",
            DatabaseMigrations.ALL_MIGRATIONS.isEmpty()
        )
    }

    @Test
    fun roomDatabaseAnnotationMatchesMigrationsVersionAndDeclaresAll16Entities() {
        val dbAnnotation = MechanicalProDatabase::class.java.getAnnotation(Database::class.java)
            ?: error("MechanicalProDatabase missing @Database annotation")

        assertEquals(DatabaseMigrations.CURRENT_DATABASE_VERSION, dbAnnotation.version)
        assertTrue(dbAnnotation.exportSchema)
        assertEquals(16, dbAnnotation.entities.size)

        val declaredEntityNames = dbAnnotation.entities.map { it.simpleName }.toSet()
        assertTrue(declaredEntityNames.contains("SystemFamilyEntity"))
        assertTrue(declaredEntityNames.contains("EquipmentCategoryEntity"))
        assertTrue(declaredEntityNames.contains("EquipmentEntity"))
        assertTrue(declaredEntityNames.contains("EquipmentSubtypeEntity"))
        assertTrue(declaredEntityNames.contains("ComponentEntity"))
        assertTrue(declaredEntityNames.contains("TechnicalParameterEntity"))
        assertTrue(declaredEntityNames.contains("KnowledgeSectionEntity"))
        assertTrue(declaredEntityNames.contains("FailureModeEntity"))
        assertTrue(declaredEntityNames.contains("MaintenanceProcedureEntity"))
        assertTrue(declaredEntityNames.contains("CalculationEntity"))
        assertTrue(declaredEntityNames.contains("FormulaVariableEntity"))
        assertTrue(declaredEntityNames.contains("UserNoteEntity"))
        assertTrue(declaredEntityNames.contains("UserBookmarkEntity"))
        assertTrue(declaredEntityNames.contains("CalculationHistoryEntity"))
        assertTrue(declaredEntityNames.contains("ChecklistProgressEntity"))
        assertTrue(declaredEntityNames.contains("EquipmentFtsEntity"))
    }

    @Test
    fun userDataTablesRemainIsolatedWithZeroForeignKeysToStaticKnowledge() {
        assertEquals(
            setOf("user_note", "user_bookmark", "calculation_history", "checklist_progress"),
            DatabaseMigrations.USER_DATA_TABLES.toSet()
        )
        assertEquals(12, DatabaseMigrations.STATIC_KNOWLEDGE_TABLES.size)

        val userEntityClasses = listOf(
            UserNoteEntity::class.java,
            UserBookmarkEntity::class.java,
            CalculationHistoryEntity::class.java,
            ChecklistProgressEntity::class.java
        )
        userEntityClasses.forEach { entityClass ->
            val entityAnnotation = entityClass.getAnnotation(Entity::class.java)
                ?: error("${entityClass.simpleName} missing @Entity")
            assertTrue(
                "${entityClass.simpleName} must declare zero foreign keys to static knowledge (DEC-014)",
                entityAnnotation.foreignKeys.isEmpty()
            )
        }
    }

    @Test
    fun staticAndUserTablesPartitionDatabaseEntitiesCompletely() {
        val allPartitionedTables =
            (DatabaseMigrations.USER_DATA_TABLES + DatabaseMigrations.STATIC_KNOWLEDGE_TABLES).toSet()
        assertEquals(16, allPartitionedTables.size)
    }
}
