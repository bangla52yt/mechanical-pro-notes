package com.researchbangla.mechanicalpronotes.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Database Migrations & Upgrade Integrity Contract for Mechanical Pro Notes.
 *
 * Current Shipped Schema Version: 1 (matches pre-packaged `mechanical_pro_notes.db` PRAGMA user_version = 1).
 *
 * Audit Result (Stage 3):
 * All 16 Room entities (15 relational tables + `equipment_fts` FTS4 virtual table), column types,
 * nullability flags, foreign-key actions, and indices already match `mechanical_pro_notes.db` v1 1:1.
 * Therefore no v1 -> v2 schema bump is warranted in Stage 3; the previous commented-out no-op
 * `MIGRATION_1_2` stub has been removed so Room can never execute an empty migration.
 *
 * INVIOLABLE MIGRATION RULE (DEC-014):
 * Destructive migration on upgrade (`fallbackToDestructiveMigration()`) is strictly forbidden.
 * Any future schema bump (`version >= 2`) MUST register a tested `Migration(from, to)` in
 * `ALL_MIGRATIONS` and preserve all four personal user tables (`USER_DATA_TABLES`).
 */
object DatabaseMigrations {

    const val EARLIEST_SUPPORTED_VERSION = 1
    const val CURRENT_DATABASE_VERSION = 1

    val USER_DATA_TABLES: List<String> = listOf(
        "user_note",
        "user_bookmark",
        "calculation_history",
        "checklist_progress"
    )

    val STATIC_KNOWLEDGE_TABLES: List<String> = listOf(
        "system_family",
        "equipment_category",
        "equipment",
        "equipment_subtype",
        "component",
        "technical_parameter",
        "knowledge_section",
        "failure_mode",
        "maintenance_procedure",
        "calculation",
        "formula_variable",
        "equipment_fts"
    )

    /**
     * All active Room migrations from earliest supported version (v1) to current version.
     * Currently empty because schema remains cleanly at v1 without destructive changes.
     */
    val ALL_MIGRATIONS: Array<Migration> = emptyArray()

    /**
     * Verifies during any migration or asset check that all four personal user tables exist
     * and contain zero CASCADE foreign keys pointing to static knowledge tables.
     */
    fun verifyUserTablesIsolatedAndPresent(db: SupportSQLiteDatabase): Boolean {
        val existingTables = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
            while (cursor.moveToNext()) {
                existingTables.add(cursor.getString(0))
            }
        }
        if (!existingTables.containsAll(USER_DATA_TABLES)) {
            return false
        }
        for (table in USER_DATA_TABLES) {
            db.query("PRAGMA foreign_key_list($table)").use { fkCursor ->
                if (fkCursor.count > 0) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Runs SQLite integrity_check and returns true only when PRAGMA integrity_check == 'ok'.
     */
    fun verifyDatabaseIntegrity(db: SupportSQLiteDatabase): Boolean {
        db.query("PRAGMA integrity_check").use { cursor ->
            return cursor.moveToFirst() && cursor.getString(0).equals("ok", ignoreCase = true)
        }
    }
}

