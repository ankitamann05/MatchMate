package com.example.matchmate

import androidx.lifecycle.LiveData
import javax.inject.Inject

class MatchRepository @Inject constructor(
    private val api: RandomUserApi,
    private val dao: MatchProfileDao
) {
    // Exposes saved profiles so the UI can update automatically from Room.
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
                    decision = existingProfile?.decision ?: DecisionStatus.PENDING,
                    pendingSync = existingProfile?.pendingSync ?: false,
                    updatedAtMillis = existingProfile?.updatedAtMillis ?: System.currentTimeMillis()
                )
            }
            dao.upsertProfiles(profiles)
            profiles.size
        }
    }

    // Marks a profile as accepted or declined and queues it for sync.
    fun updateDecision(email: String, decision: String) {
        dao.updateDecision(email, decision, System.currentTimeMillis())
    }

    // Clears pending sync flags after the server is reachable again.
    suspend fun syncPendingDecisions(): Result<Int> {
        return runCatching {
            val pendingProfiles = dao.getPendingSyncProfiles()
            if (pendingProfiles.isEmpty()) {
                return@runCatching 0
            }

            api.getMatches(results = 1)
            dao.markSynced(pendingProfiles.map { it.email })
            pendingProfiles.size
        }
    }
}
