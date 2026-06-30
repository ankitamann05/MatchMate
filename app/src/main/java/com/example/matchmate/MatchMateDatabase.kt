package com.example.matchmate

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MatchProfileEntity::class], version = 2, exportSchema = false)
abstract class MatchMateDatabase : RoomDatabase() {
    abstract fun matchProfileDao(): MatchProfileDao

    companion object {
        @Volatile
        private var instance: MatchMateDatabase? = null

        fun getInstance(context: Context): MatchMateDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MatchMateDatabase::class.java,
                    "match_mate.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE match_profiles ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE match_profiles ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
