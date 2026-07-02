package com.example.matchmate

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApi {
    // Requests a repeatable list of match profiles from the Random User API.
    @GET("api/")
    fun getMatches(
        @Query("results") results: Int = 10,
        @Query("seed") seed: String = "matchmate"
    ): Call<RandomUserResponse>
}
