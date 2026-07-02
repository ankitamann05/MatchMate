package com.example.matchmate.repository

import androidx.lifecycle.LiveData
import com.example.matchmate.database.MatchProfileDao
import com.example.matchmate.database.MatchProfileEntity
import com.example.matchmate.model.DecisionStatus
import com.example.matchmate.network.RandomUserApi
import javax.inject.Inject

class MatchRepository @Inject constructor(
    private val api: RandomUserApi,
    private val dao: MatchProfileDao
) {
    // Room is the source of truth, so the UI updates from cached profiles.
    val profiles: LiveData<List<MatchProfileEntity>> = dao.observeProfiles()

    // Fetches one page of match profiles and stores them while keeping saved decisions.
    suspend fun loadMatchesPage(page: Int, pageSize: Int): Result<Int> {
        return runCatching {
            val response = api.getMatches(results = pageSize, page = page)
            val profiles = response.results.map { user ->
                val email = user.email
                val existingProfile = dao.getProfile(email)

                MatchProfileEntity(
                    email = email,
                    fullName = "${user.name.first} ${user.name.last}",
                    age = user.dob.age,
                    city = user.location.city,
                    country = user.location.country,
                    photoUrl = user.picture.large,
                    decision = existingProfile?.decision ?: DecisionStatus.PENDING
                )
            }
            dao.upsertProfiles(profiles)
            profiles.size
        }
    }

    // Marks a profile as accepted or declined locally.
    fun updateDecision(email: String, decision: String): Result<Unit> {
        return runCatching {
            val updatedRows = dao.updateDecision(email, decision)
            check(updatedRows > 0) {
                "Could not save choice because this profile is no longer available."
            }
        }
    }
}
