package com.example.matchmate.utils

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class NetworkUtils @Inject constructor(
    private val connectivityManager: ConnectivityManager
) {
    // Checks whether the device currently has internet access.
    fun isOnline(): Boolean {
        return runCatching {
            val network = connectivityManager.activeNetwork
            val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }.getOrDefault(false)
    }
}
