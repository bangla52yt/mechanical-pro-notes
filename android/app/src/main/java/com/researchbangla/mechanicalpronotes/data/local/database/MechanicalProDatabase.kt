package com.researchbangla.mechanicalpronotes.data.local.database

import android.content.Context
import androidx.room.*
import com.researchbangla.mechanicalpronotes.data.local.database.converter.RoomConverters
import com.researchbangla.mechanicalpronotes.data.local.database.dao.*
import com.researchbangla.mechanicalpronotes.data.local.database.entity.*
import com.researchbangla.mechanicalpronotes.data.local.database.migration.DatabaseMigrations

@Database(
    entities = [
        SystemFamilyEntity::class,
        EquipmentCategoryEntity::class,
        EquipmentEntity::class,
        EquipmentSubtypeEntity::class,
        ComponentEntity::class,
        TechnicalParameterEntity::class,
        KnowledgeSectionEntity::class,
        FailureModeEntity::class,
        MaintenanceProcedureEntity::class,
        CalculationEntity::class,
        FormulaVariableEntity::class,
        UserNoteEntity::class,
        UserBookmarkEntity::class,
        CalculationHistoryEntity::class,
        ChecklistProgressEntity::class,
        EquipmentFtsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class MechanicalProDatabase : RoomDatabase() {

    abstract fun equipmentDao(): EquipmentDao
    abstract fun boilerDao(): BoilerDao
    abstract fun searchDao(): SearchDao
    abstract fun calculationDao(): CalculationDao
    abstract fun userNoteDao(): UserNoteDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        const val DATABASE_NAME = "mechanical_pro_notes.db"

        @Volatile
        private var INSTANCE: MechanicalProDatabase? = null

        fun getInstance(context: Context, useAssetDatabase: Boolean = true): MechanicalProDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    MechanicalProDatabase::class.java,
                    DATABASE_NAME
                )

                if (useAssetDatabase) {
                    // Pre-packaged SQLite asset distribution (DEC-012)
                    builder.createFromAsset("database/$DATABASE_NAME")
                }

                builder
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade()
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * For unit and integration testing: in-memory database instance.
         */
        fun createInMemory(context: Context): MechanicalProDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                MechanicalProDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}
