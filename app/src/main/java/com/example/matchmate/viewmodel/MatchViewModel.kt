package com.example.matchmate.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmate.R
import com.example.matchmate.database.MatchProfileEntity
import com.example.matchmate.model.DecisionStatus
import com.example.matchmate.model.MatchUiState
import com.example.matchmate.repository.MatchRepository
import com.example.matchmate.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: MatchRepository,
    private val connectivityManager: ConnectivityManager,
    private val networkUtils: NetworkUtils,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private companion object {
        // Keeps API requests small so RecyclerView pagination stays smooth.
        const val PAGE_SIZE = 10
        const val FIRST_PAGE = 1
    }

    private var nextPage = FIRST_PAGE
    private var isPageRequestRunning = false

    // One state object keeps the Activity simple and avoids scattered UI flags.
    private val _uiState = MediatorLiveData<MatchUiState>().apply {
        value = MatchUiState()
    }
    val uiState: LiveData<MatchUiState> = _uiState

    // Watches network changes so cached matches can refresh when internet returns.
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModelScope.launch {
                loadMatchesPageInBackground(
                    page = FIRST_PAGE,
                    resetPaging = true,
                    successMessage = getString(R.string.online_message)
                )
            }
        }

        override fun onLost(network: Network) {
            viewModelScope.launch {
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        message = getString(R.string.offline_message)
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
        val isListeningForNetwork = runCatching {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }.isSuccess

        if (networkUtils.isOnline()) {
            viewModelScope.launch {
                loadMatchesPageInBackground(page = FIRST_PAGE, resetPaging = true)
            }
        } else {
            val message = if (isListeningForNetwork) {
                getString(R.string.offline_message)
            } else {
                getString(R.string.network_monitor_error)
            }
            updateState { copy(message = message) }
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

    // Requests the next page only when more data is available.
    fun loadNextPage() {
        viewModelScope.launch {
            if (_uiState.value?.canLoadMore == false) {
                return@launch
            }
            loadMatchesPageInBackground(page = nextPage, resetPaging = false)
        }
    }

    // Clears a toast message after the Activity has shown it.
    fun clearMessage() {
        updateState { copy(message = null) }
    }

    // Loads data on the IO dispatcher and stores it in Room through the repository.
    private suspend fun loadMatchesPageInBackground(
        page: Int,
        resetPaging: Boolean,
        successMessage: String? = null
    ) {
        if (isPageRequestRunning) {
            return
        }
        if (!networkUtils.isOnline()) {
            updateState {
                copy(
                    isLoading = false,
                    isLoadingMore = false,
                    message = getString(R.string.offline_message)
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
                        message = successMessage
                    )
                }
            },
            onFailure = {
                updateState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        message = getString(R.string.matches_error)
                    )
                }
            }
        )
        isPageRequestRunning = false
    }

    // Saves an accepted profile locally.
    fun acceptProfile(profile: MatchProfileEntity) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.updateDecision(profile.email, DecisionStatus.ACCEPTED)
            }
            handleDecisionResult(result, successMessage = getString(R.string.profile_accepted))
        }
    }

    // Saves a declined profile locally.
    fun declineProfile(profile: MatchProfileEntity) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.updateDecision(profile.email, DecisionStatus.DECLINED)
            }
            handleDecisionResult(result, successMessage = getString(R.string.profile_declined))
        }
    }

    // Converts database save results into UI messages.
    private suspend fun handleDecisionResult(result: Result<Unit>, successMessage: String) {
        result.fold(
            onSuccess = {
                updateState { copy(message = successMessage) }
            },
            onFailure = {
                updateState { copy(message = getString(R.string.save_error)) }
            }
        )
    }

    private fun getString(resId: Int): String {
        return context.getString(resId)
    }

    // Updates LiveData safely from either the main thread or a background dispatcher.
    private fun updateState(update: MatchUiState.() -> MatchUiState) {
        val currentState = _uiState.value ?: MatchUiState()
        val newState = currentState.update()
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _uiState.value = newState
        } else {
            _uiState.postValue(newState)
        }
    }

}
