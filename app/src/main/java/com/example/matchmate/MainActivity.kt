package com.example.matchmate

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val PAGINATION_THRESHOLD = 3
    }

    private lateinit var matchAdapter: MatchProfileAdapter
    private lateinit var statusText: TextView
    private val viewModel: MatchViewModel by viewModels()

    // Sets up the screen and starts observing match data from the ViewModel.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val mainView = findViewById<View>(R.id.main)
        val initialLeft = mainView.paddingLeft
        val initialTop = mainView.paddingTop
        val initialRight = mainView.paddingRight
        val initialBottom = mainView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialLeft + systemBars.left,
                initialTop + systemBars.top,
                initialRight + systemBars.right,
                initialBottom + systemBars.bottom
            )
            insets
        }

        statusText = findViewById(R.id.statusText)
        matchAdapter = MatchProfileAdapter(
            onAccept = { profile -> viewModel.acceptProfile(profile) },
            onDecline = { profile -> viewModel.declineProfile(profile) }
        )

        val matchLayoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.matchesRecyclerView).apply {
            layoutManager = matchLayoutManager
            adapter = matchAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) {
                        return
                    }

                    val visibleItemCount = matchLayoutManager.childCount
                    val totalItemCount = matchLayoutManager.itemCount
                    val firstVisibleItem = matchLayoutManager.findFirstVisibleItemPosition()
                    val isNearEnd = firstVisibleItem + visibleItemCount >=
                        totalItemCount - PAGINATION_THRESHOLD

                    if (isNearEnd) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }

        viewModel.uiState.observe(this) { uiState ->
            renderState(uiState)
        }
    }

    // Renders all screen data from the single UI state object.
    private fun renderState(uiState: MatchUiState) {
        if (uiState.profiles.isEmpty()) {
            statusText.text = uiState.message ?: getString(R.string.loading_matches)
            matchAdapter.submitList(emptyList())
            return
        }

        statusText.text = when {
            uiState.isLoading -> getString(R.string.loading_matches)
            uiState.isLoadingMore -> getString(R.string.loading_more_matches)
            uiState.message != null -> uiState.message
            else -> getString(R.string.cached_matches_loaded, uiState.profiles.size)
        }
        matchAdapter.submitList(uiState.profiles)
    }
}
