package com.example.matchmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.matchmate.core.Constants

@Database(entities = [MatchProfileEntity::class], version = 1, exportSchema = false)
abstract class MatchMateDatabase : RoomDatabase() {
    abstract fun matchProfileDao(): MatchProfileDao

    companion object {
        @Volatile
        private var instance: MatchMateDatabase? = null

        // Provides a single database instance for the whole app.
        fun getInstance(context: Context): MatchMateDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MatchMateDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .build()
                    .also { instance = it }
            }
        }
    }
}
