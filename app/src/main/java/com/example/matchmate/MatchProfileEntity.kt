package com.example.matchmate

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores one match profile and the user's local decision.
@Entity(tableName = "match_profiles")
data class MatchProfileEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val age: Int,
    val city: String,
    val country: String,
    val photoUrl: String,
    val decision: String = DecisionStatus.PENDING,
    val pendingSync: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
