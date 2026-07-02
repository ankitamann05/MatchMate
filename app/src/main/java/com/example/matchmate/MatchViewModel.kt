package com.example.matchmate

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: MatchRepository,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 10
        const val FIRST_PAGE = 1
    }

    private var nextPage = FIRST_PAGE
    private var isPageRequestRunning = false

    private val _uiState = MediatorLiveData<MatchUiState>().apply {
        value = MatchUiState()
    }
    val uiState: LiveData<MatchUiState> = _uiState

    // Watches network changes so offline choices can sync when internet returns.
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModelScope.launch {
                updateState { copy(message = "Connection restored. Syncing saved choices...") }
                syncPendingDecisionsInBackground()
                loadMatchesPageInBackground(page = FIRST_PAGE, resetPaging = true)
            }
        }

        override fun onLost(network: Network) {
            viewModelScope.launch {
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        message = "Offline mode: showing cached matches."
                    )
                }
            }
        }
    }

    // Connects cached profile updates and the network listener when the ViewModel starts.
    init {
        _uiState.addSource(repository.profiles) { profiles ->
            updateState { copy(profiles = profiles) }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        if (isOnline()) {
            viewModelScope.launch {
                syncPendingDecisionsInBackground()
                loadMatchesPageInBackground(page = FIRST_PAGE, resetPaging = true)
            }
        } else {
            updateState { copy(message = "Offline mode: showing cached matches.") }
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
        viewModelScope.launch {
            loadMatchesPageInBackground(page = FIRST_PAGE, resetPaging = true)
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            if (_uiState.value?.canLoadMore == false) {
                return@launch
            }
            loadMatchesPageInBackground(page = nextPage, resetPaging = false)
        }
    }

    private suspend fun loadMatchesPageInBackground(page: Int, resetPaging: Boolean) {
        if (isPageRequestRunning) {
            return
        }
        if (!isOnline()) {
            updateState {
                copy(
                    isLoading = false,
                    isLoadingMore = false,
                    message = "Offline mode: showing cached matches."
                )
            }
            return
        }

        isPageRequestRunning = true
        if (resetPaging) {
            nextPage = FIRST_PAGE
        }
        updateState {
            copy(
                isLoading = resetPaging,
                isLoadingMore = !resetPaging,
                canLoadMore = if (resetPaging) true else canLoadMore,
                message = null
            )
        }
        val result = withContext(Dispatchers.IO) {
            repository.loadMatchesPage(page = page, pageSize = PAGE_SIZE)
        }
        result.fold(
            onSuccess = { loadedCount ->
                nextPage = page + 1
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        canLoadMore = loadedCount == PAGE_SIZE,
                        message = null
                    )
                }
            },
            onFailure = {
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        message = "Could not load matches. Check your connection and try again."
                    )
                }
            }
        )
        isPageRequestRunning = false
    }

    // Saves an accepted profile locally and syncs it if possible.
    fun acceptProfile(profile: MatchProfileEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateDecision(profile.email, DecisionStatus.ACCEPTED)
            }
            updateState { copy(message = "Choice saved locally.") }
            if (isOnline()) {
                syncPendingDecisionsInBackground()
            }
        }
    }

    // Saves a declined profile locally and syncs it if possible.
    fun declineProfile(profile: MatchProfileEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateDecision(profile.email, DecisionStatus.DECLINED)
            }
            updateState { copy(message = "Choice saved locally.") }
            if (isOnline()) {
                syncPendingDecisionsInBackground()
            }
        }
    }

    private suspend fun syncPendingDecisionsInBackground() {
        val result = withContext(Dispatchers.IO) {
            repository.syncPendingDecisions()
        }
        result.fold(
            onSuccess = { syncedCount ->
                if (syncedCount > 0) {
                    updateState { copy(message = "Synced $syncedCount saved choices.") }
                }
            },
            onFailure = {
                updateState { copy(message = "Saved offline. Will sync when the connection is restored.") }
            }
        )
    }

    private fun updateState(update: MatchUiState.() -> MatchUiState) {
        val currentState = _uiState.value ?: MatchUiState()
        val newState = currentState.update()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _uiState.value = newState
        } else {
            _uiState.postValue(newState)
        }
    }

    // Checks whether the device currently has internet access.
    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
