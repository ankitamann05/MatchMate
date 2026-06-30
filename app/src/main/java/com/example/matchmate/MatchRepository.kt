package com.example.matchmate

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MatchRepository(
    private val api: RandomUserApi,
    private val dao: MatchProfileDao
) {
    val profiles: LiveData<List<MatchProfileEntity>> = dao.observeProfiles()

    private val databaseExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun refreshMatches(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.getMatches().enqueue(object : Callback<RandomUserResponse> {
            override fun onResponse(
                call: Call<RandomUserResponse>,
                response: Response<RandomUserResponse>
            ) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    onError("Could not load matches. Please try again.")
                    return
                }

                databaseExecutor.execute {
                    val profiles = body.results.map { user ->
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
                    dao.deleteSyncedProfilesOutside(profiles.map { it.email })
                    onSuccess()
                }
            }

            override fun onFailure(call: Call<RandomUserResponse>, throwable: Throwable) {
                onError("Could not load matches. Check your connection and try again.")
            }
        })
    }

    fun updateDecision(email: String, decision: String) {
        databaseExecutor.execute {
            dao.updateDecision(email, decision, System.currentTimeMillis())
        }
    }

    fun syncPendingDecisions(
        onComplete: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        databaseExecutor.execute {
            val pendingProfiles = dao.getPendingSyncProfiles()
            if (pendingProfiles.isEmpty()) {
                onComplete(0)
                return@execute
            }

            api.getMatches(1).enqueue(object : Callback<RandomUserResponse> {
                override fun onResponse(
                    call: Call<RandomUserResponse>,
                    response: Response<RandomUserResponse>
                ) {
                    if (response.isSuccessful) {
                        databaseExecutor.execute {
                            dao.markSynced(pendingProfiles.map { it.email })
                            onComplete(pendingProfiles.size)
                        }
                    } else {
                        onError("Saved offline. Will sync when the connection is restored.")
                    }
                }

                override fun onFailure(call: Call<RandomUserResponse>, throwable: Throwable) {
                    onError("Saved offline. Will sync when the connection is restored.")
                }
            })
        }
    }
}
