package com.example.matchmate

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var matchesContainer: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var viewModel: MatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        matchesContainer = findViewById(R.id.matchesContainer)
        statusText = findViewById(R.id.statusText)

        viewModel = ViewModelProvider(this)[MatchViewModel::class.java]
        viewModel.profiles.observe(this) { profiles ->
            renderMatches(profiles)
        }
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                statusText.text = getString(R.string.loading_matches)
            }
        }
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                statusText.text = it
            }
        }
        viewModel.statusMessage.observe(this) { message ->
            message?.let {
                statusText.text = it
            }
        }
    }

    private fun renderMatches(matches: List<MatchProfileEntity>) {
        matchesContainer.removeAllViews()
        if (matches.isEmpty()) {
            statusText.text = getString(R.string.loading_matches)
            return
        }

        statusText.text = getString(R.string.cached_matches_loaded, matches.size)
        matches.forEach { profile ->
            matchesContainer.addView(createProfileCard(profile))
        }
    }

    private fun createProfileCard(profile: MatchProfileEntity): View {
        val card = LayoutInflater.from(this)
            .inflate(R.layout.item_match_profile, matchesContainer, false)

        val photo = card.findViewById<ImageView>(R.id.profilePhoto)
        val name = card.findViewById<TextView>(R.id.profileName)
        val location = card.findViewById<TextView>(R.id.profileLocation)
        val email = card.findViewById<TextView>(R.id.profileEmail)
        val decision = card.findViewById<TextView>(R.id.decisionText)
        val syncState = card.findViewById<TextView>(R.id.syncStateText)
        val declineButton = card.findViewById<Button>(R.id.declineButton)
        val acceptButton = card.findViewById<Button>(R.id.acceptButton)

        Glide.with(this)
            .load(profile.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_photo_placeholder)
            .error(R.drawable.bg_photo_placeholder)
            .into(photo)

        name.text = getString(R.string.profile_name_age, profile.fullName, profile.age)
        location.text = getString(R.string.profile_location, profile.city, profile.country)
        email.text = profile.email
        decision.visibility = if (profile.decision == DecisionStatus.PENDING) View.GONE else View.VISIBLE
        applyDecisionText(decision, profile.decision)

        syncState.visibility = if (profile.pendingSync) View.VISIBLE else View.GONE
        declineButton.setOnClickListener {
            viewModel.declineProfile(profile)
        }
        acceptButton.setOnClickListener {
            viewModel.acceptProfile(profile)
        }

        val hasDecision = profile.decision != DecisionStatus.PENDING
        declineButton.isEnabled = !hasDecision
        acceptButton.isEnabled = !hasDecision
        declineButton.alpha = if (hasDecision) 0.45f else 1f
        acceptButton.alpha = if (hasDecision) 0.45f else 1f

        return card
    }

    private fun applyDecisionText(decisionView: TextView, decision: String) {
        when (decision) {
            DecisionStatus.ACCEPTED -> {
                decisionView.text = getString(R.string.profile_accepted)
                decisionView.setTextColor(Color.parseColor("#237A4B"))
            }
            DecisionStatus.DECLINED -> {
                decisionView.text = getString(R.string.profile_declined)
                decisionView.setTextColor(Color.parseColor("#A04245"))
            }
        }
    }
}
