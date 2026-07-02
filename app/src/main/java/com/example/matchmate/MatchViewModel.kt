package com.example.matchmate

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MatchRepository
    val profiles: LiveData<List<MatchProfileEntity>>

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _statusMessage = MutableLiveData<String?>(null)
    val statusMessage: LiveData<String?> = _statusMessage

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Watches network changes so offline choices can sync when internet returns.
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _statusMessage.postValue("Connection restored. Syncing saved choices...")
            syncPendingDecisions()
            refreshMatches()
        }

        override fun onLost(network: Network) {
            _statusMessage.postValue("Offline mode: showing cached matches.")
        }
    }

    // Connects the database, repository, and network listener when the ViewModel starts.
    init {
        val dao = MatchMateDatabase.getInstance(application).matchProfileDao()
        repository = MatchRepository(NetworkModule.randomUserApi, dao)
        profiles = repository.profiles
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        if (isOnline()) {
            syncPendingDecisions()
            refreshMatches()
        } else {
            _statusMessage.postValue("Offline mode: showing cached matches.")
        }
    }

    // Removes the network listener when the ViewModel is no longer used.
    override fun onCleared() {
        super.onCleared()
        runCatching {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    // Loads fresh matches from the API when online, otherwise keeps cached results visible.
    fun refreshMatches() {
        if (!isOnline()) {
            _isLoading.postValue(false)
            _statusMessage.postValue("Offline mode: showing cached matches.")
            return
        }

        _isLoading.postValue(true)
        _errorMessage.postValue(null)
        repository.refreshMatches(
            onSuccess = {
                _isLoading.postValue(false)
                _statusMessage.postValue(null)
            },
            onError = { message ->
                _isLoading.postValue(false)
                _errorMessage.postValue(message)
            }
        )
    }

    // Saves an accepted profile locally and syncs it if possible.
    fun acceptProfile(profile: MatchProfileEntity) {
        repository.updateDecision(profile.email, DecisionStatus.ACCEPTED)
        _statusMessage.postValue("Choice saved locally.")
        if (isOnline()) {
            syncPendingDecisions()
        }
    }

    // Saves a declined profile locally and syncs it if possible.
    fun declineProfile(profile: MatchProfileEntity) {
        repository.updateDecision(profile.email, DecisionStatus.DECLINED)
        _statusMessage.postValue("Choice saved locally.")
        if (isOnline()) {
            syncPendingDecisions()
        }
    }

    // Sends locally saved choices to the server once a connection is available.
    private fun syncPendingDecisions() {
        repository.syncPendingDecisions(
            onComplete = { syncedCount ->
                if (syncedCount > 0) {
                    _statusMessage.postValue("Synced $syncedCount saved choices.")
                }
            },
            onError = { message ->
                _statusMessage.postValue(message)
            }
        )
    }

    // Checks whether the device currently has internet access.
    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
