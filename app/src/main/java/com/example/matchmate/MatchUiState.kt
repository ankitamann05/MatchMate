package com.example.matchmate

data class MatchUiState(
    val profiles: List<MatchProfileEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val message: String? = null
)
