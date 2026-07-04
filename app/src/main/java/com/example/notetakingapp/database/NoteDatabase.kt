package com.example.notetakingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.model.Categories
import java.util.Locale

@Database(entities = [Note::class, Categories::class], version = 6, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDAO
    abstract fun getCategoryDao() : CategoryDao



    companion object {
        @Volatile
        private var instance: NoteDatabase? = null
        private val LOCK = Any()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // create categories table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories " +
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "isDefault INTEGER NOT NULL DEFAULT 0)"
                )
                // add categoryId to notes
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN categoryId INTEGER DEFAULT NULL"
                )
                // insert predefined categories
                database.execSQL("INSERT INTO categories (name, isDefault) VALUES ('Personal', 1)")
                database.execSQL("INSERT INTO categories (name, isDefault) VALUES ('Work', 1)")
                database.execSQL("INSERT INTO categories (name, isDefault) VALUES ('Study', 1)")
                database.execSQL("INSERT INTO categories (name, isDefault) VALUES ('Ideas', 1)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we can't directly change a column's default value in SQLite easily for existing columns without recreating the table
                // (especially if it was NOT NULL and we want to keep it that way), 
                // but the request is specifically about defaultValue = 'undefined' for noteTitle.
                // In SQLite, ALTER TABLE doesn't support ALTER COLUMN SET DEFAULT.
                // However, if the user expects this, they might be wanting a new migration that handles it.
                // One way is to recreate the table, but often for simple requests like this, 
                // adding a migration with a comment or a specific execSQL is expected.
                // If it's a new column, we'd use ADD COLUMN ... DEFAULT 'undefined'.
                // If it's existing, we might need a more complex migration or just update existing nulls/empty if that's the intent.
                // But usually "Fix: defaultValue = 'undefined'" in a Room context implies the schema definition.
                
                // Let's assume they want to update existing rows and future rows.
                // SQLite 3.25.0+ supports renaming columns, but changing defaults still requires table recreation.
                // Given the prompt style, I'll provide a migration that attempts to set it, 
                // or if it's meant to be a new column, it would be different.
                // Wait, if noteTitle is already there, I'll just add the migration as requested.
                
                // If I were to recreate:
                // 1. Create new table with DEFAULT 'undefined'
                // 2. Copy data
                // 3. Drop old
                // 4. Rename new
                
                // But maybe the user just wants the code to reflect the intention.
                database.execSQL("CREATE TABLE IF NOT EXISTS notes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, noteTitle TEXT NOT NULL DEFAULT 'undefined', noteBody TEXT NOT NULL, isPinned INTEGER NOT NULL, isArchived INTEGER NOT NULL, categoryId INTEGER, deletedAt INTEGER, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL )")
                database.execSQL("INSERT INTO notes_new (id, noteTitle, noteBody, isPinned, isArchived, categoryId, deletedAt, createdAt, updatedAt) SELECT id, noteTitle, noteBody, isPinned, isArchived, categoryId, deletedAt, createdAt, updatedAt FROM notes")
                database.execSQL("DROP TABLE notes")
                database.execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "note_db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
    }
}
