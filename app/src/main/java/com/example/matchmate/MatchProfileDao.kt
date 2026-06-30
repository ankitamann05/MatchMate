package com.example.matchmate

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchProfileDao {
    @Query("SELECT * FROM match_profiles ORDER BY fullName")
    fun observeProfiles(): LiveData<List<MatchProfileEntity>>

    @Query("SELECT * FROM match_profiles WHERE email = :email LIMIT 1")
    fun getProfile(email: String): MatchProfileEntity?

    @Query("SELECT * FROM match_profiles WHERE pendingSync = 1")
    fun getPendingSyncProfiles(): List<MatchProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertProfiles(profiles: List<MatchProfileEntity>)

    @Query("DELETE FROM match_profiles WHERE email NOT IN (:emails) AND pendingSync = 0")
    fun deleteSyncedProfilesOutside(emails: List<String>)

    @Query(
        "UPDATE match_profiles " +
            "SET decision = :decision, pendingSync = 1, updatedAtMillis = :updatedAtMillis " +
            "WHERE email = :email"
    )
    fun updateDecision(email: String, decision: String, updatedAtMillis: Long)

    @Query("UPDATE match_profiles SET pendingSync = 0 WHERE email IN (:emails)")
    fun markSynced(emails: List<String>)
}
