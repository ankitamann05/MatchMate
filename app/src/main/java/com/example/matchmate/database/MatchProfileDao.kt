package com.example.matchmate.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.matchmate.core.Constants

@Dao
interface MatchProfileDao {
    // Streams all saved profiles to the UI.
    @Query("SELECT * FROM " + Constants.MATCH_PROFILE_TABLE + " ORDER BY fullName")
    fun observeProfiles(): LiveData<List<MatchProfileEntity>>

    // Finds an existing profile so saved choices are not overwritten.
    @Query("SELECT * FROM " + Constants.MATCH_PROFILE_TABLE + " WHERE email = :email LIMIT 1")
    fun getProfile(email: String): MatchProfileEntity?

    // Inserts new profiles or updates existing cached profiles.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertProfiles(profiles: List<MatchProfileEntity>)

    // Stores the user's choice locally.
    @Query(
        "UPDATE " + Constants.MATCH_PROFILE_TABLE + " " +
            "SET decision = :decision " +
            "WHERE email = :email"
    )
    fun updateDecision(email: String, decision: String): Int
}
