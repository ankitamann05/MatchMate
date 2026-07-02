package com.example.matchmate.model

import com.example.matchmate.database.MatchProfileEntity

// Holds everything the main screen needs to render.
data class MatchUiState(
    val profiles: List<MatchProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val message: String? = null
)
