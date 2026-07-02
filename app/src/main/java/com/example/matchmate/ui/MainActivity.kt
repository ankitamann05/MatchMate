package com.example.matchmate.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchmate.R
import com.example.matchmate.model.MatchUiState
import com.example.matchmate.viewmodel.MatchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        // Starts loading the next page before the user reaches the end.
        const val PAGINATION_THRESHOLD = 3
    }

    private lateinit var matchAdapter: MatchProfileAdapter
    private val viewModel: MatchViewModel by viewModels()

    // Sets up the screen and starts observing match data from the ViewModel.
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

        // Sends card button actions to the ViewModel.
        matchAdapter = MatchProfileAdapter(
            onAccept = { profile ->
                viewModel.acceptProfile(profile)
            },
            onDecline = { profile ->
                viewModel.declineProfile(profile)
            }
        )

        val matchLayoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.matchesRecyclerView).apply {
            layoutManager = matchLayoutManager
            adapter = matchAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                // Loads more profiles when the list is scrolled near the bottom.
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
            matchAdapter.submitList(emptyList())
            showMessage(uiState.message)
            return
        }

        matchAdapter.submitList(uiState.profiles)
        showMessage(uiState.message)
    }

    // Shows one-time messages like accepted, declined, or offline errors.
    private fun showMessage(message: String?) {
        if (message.isNullOrBlank()) {
            return
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        viewModel.clearMessage()
    }
}
