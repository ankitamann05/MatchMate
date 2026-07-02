package com.example.matchmate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.matchmate.core.Constants
import com.example.matchmate.model.DecisionStatus

// Stores one match profile and the user's local decision.
@Entity(tableName = Constants.MATCH_PROFILE_TABLE)
data class MatchProfileEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val age: Int,
    val city: String,
    val country: String,
    val photoUrl: String,
    val decision: String = DecisionStatus.PENDING
)
