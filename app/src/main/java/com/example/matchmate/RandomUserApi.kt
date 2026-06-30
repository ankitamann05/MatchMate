package com.example.matchmate

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApi {
    @GET("api/")
    fun getMatches(
        @Query("results") results: Int = 10,
        @Query("seed") seed: String = "matchmate"
    ): Call<RandomUserResponse>
}
