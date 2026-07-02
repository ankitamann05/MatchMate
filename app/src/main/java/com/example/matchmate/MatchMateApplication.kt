package com.example.matchmate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Starts Hilt so dependencies can be injected across the app.
@HiltAndroidApp
class MatchMateApplication : Application() {
}
