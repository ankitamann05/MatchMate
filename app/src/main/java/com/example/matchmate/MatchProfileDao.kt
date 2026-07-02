package com.example.matchmate

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchProfileDao {
    // Streams all saved profiles to the UI.
    @Query("SELECT * FROM match_profiles ORDER BY fullName")
    fun observeProfiles(): LiveData<List<MatchProfileEntity>>

    // Finds an existing profile so saved choices are not overwritten.
    @Query("SELECT * FROM match_profiles WHERE email = :email LIMIT 1")
    fun getProfile(email: String): MatchProfileEntity?

    // Returns choices that still need to be synced.
    @Query("SELECT * FROM match_profiles WHERE pendingSync = 1")
    fun getPendingSyncProfiles(): List<MatchProfileEntity>

    // Inserts new profiles or updates existing cached profiles.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertProfiles(profiles: List<MatchProfileEntity>)

    // Removes old synced profiles that are not part of the latest API result.
    @Query("DELETE FROM match_profiles WHERE email NOT IN (:emails) AND pendingSync = 0")
    fun deleteSyncedProfilesOutside(emails: List<String>)

    // Stores the user's choice and marks it for syncing.
    @Query(
        "UPDATE match_profiles " +
            "SET decision = :decision, pendingSync = 1, updatedAtMillis = :updatedAtMillis " +
            "WHERE email = :email"
    )
    fun updateDecision(email: String, decision: String, updatedAtMillis: Long)

    // Marks queued choices as synced.
    @Query("UPDATE match_profiles SET pendingSync = 0 WHERE email IN (:emails)")
    fun markSynced(emails: List<String>)
}
