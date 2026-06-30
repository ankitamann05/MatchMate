package com.example.matchmate

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    val randomUserApi: RandomUserApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://randomuser.me/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RandomUserApi::class.java)
    }
}
