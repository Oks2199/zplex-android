package zechs.zplex.utils

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE movies ADD COLUMN modifiedTime INTEGER DEFAULT NULL")

        db.execSQL("ALTER TABLE shows ADD COLUMN modifiedTime INTEGER DEFAULT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watched_movies ADD COLUMN fileId TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE watched_movies ADD COLUMN offline INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE watched_shows ADD COLUMN fileId TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE watched_shows ADD COLUMN offline INTEGER NOT NULL DEFAULT 0")
    }
}
