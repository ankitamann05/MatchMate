package com.example.matchmate.di

import android.content.Context
import android.net.ConnectivityManager
import com.example.matchmate.core.Constants
import com.example.matchmate.database.MatchMateDatabase
import com.example.matchmate.database.MatchProfileDao
import com.example.matchmate.network.RandomUserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Builds the Retrofit client once and reuses it across the app.
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRandomUserApi(retrofit: Retrofit): RandomUserApi {
        return retrofit.create(RandomUserApi::class.java)
    }

    // Provides the Room database used for offline profile storage.
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MatchMateDatabase {
        return MatchMateDatabase.getInstance(context)
    }

    // Exposes DAO methods for reading and updating profile decisions.
    @Provides
    fun provideMatchProfileDao(database: MatchMateDatabase): MatchProfileDao {
        return database.matchProfileDao()
    }

    // Shared by NetworkUtils and the ViewModel for connectivity checks and callbacks.
    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}
