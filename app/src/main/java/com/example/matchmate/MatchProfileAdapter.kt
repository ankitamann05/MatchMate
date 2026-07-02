package com.example.matchmate

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MatchProfileAdapter(
    private val onAccept: (MatchProfileEntity) -> Unit,
    private val onDecline: (MatchProfileEntity) -> Unit
) : ListAdapter<MatchProfileEntity, MatchProfileAdapter.MatchProfileViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_profile, parent, false)
        return MatchProfileViewHolder(view, onAccept, onDecline)
    }

    override fun onBindViewHolder(holder: MatchProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MatchProfileViewHolder(
        itemView: View,
        private val onAccept: (MatchProfileEntity) -> Unit,
        private val onDecline: (MatchProfileEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val photo: ImageView = itemView.findViewById(R.id.profilePhoto)
        private val name: TextView = itemView.findViewById(R.id.profileName)
        private val location: TextView = itemView.findViewById(R.id.profileLocation)
        private val email: TextView = itemView.findViewById(R.id.profileEmail)
        private val decision: TextView = itemView.findViewById(R.id.decisionText)
        private val syncState: TextView = itemView.findViewById(R.id.syncStateText)
        private val declineButton: Button = itemView.findViewById(R.id.declineButton)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)

        // Binds profile details and action button state for one card.
        fun bind(profile: MatchProfileEntity) {
            Glide.with(itemView)
                .load(profile.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_photo_placeholder)
                .error(R.drawable.bg_photo_placeholder)
                .into(photo)

            name.text = itemView.context.getString(
                R.string.profile_name_age,
                profile.fullName,
                profile.age
            )
            location.text = itemView.context.getString(
                R.string.profile_location,
                profile.city,
                profile.country
            )
            email.text = profile.email
            decision.visibility = if (profile.decision == DecisionStatus.PENDING) {
                View.GONE
            } else {
                View.VISIBLE
            }
            applyDecisionText(profile.decision)

            syncState.visibility = if (profile.pendingSync) View.VISIBLE else View.GONE
            declineButton.setOnClickListener {
                onDecline(profile)
            }
            acceptButton.setOnClickListener {
                onAccept(profile)
            }

            val hasDecision = profile.decision != DecisionStatus.PENDING
            declineButton.isEnabled = !hasDecision
            acceptButton.isEnabled = !hasDecision
            declineButton.alpha = if (hasDecision) 0.45f else 1f
            acceptButton.alpha = if (hasDecision) 0.45f else 1f
        }

        // Shows the saved choice with a matching label color.
        private fun applyDecisionText(decisionStatus: String) {
            when (decisionStatus) {
                DecisionStatus.ACCEPTED -> {
                    decision.text = itemView.context.getString(R.string.profile_accepted)
                    decision.setTextColor(Color.parseColor("#237A4B"))
                }
                DecisionStatus.DECLINED -> {
                    decision.text = itemView.context.getString(R.string.profile_declined)
                    decision.setTextColor(Color.parseColor("#A04245"))
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MatchProfileEntity>() {
        override fun areItemsTheSame(
            oldItem: MatchProfileEntity,
            newItem: MatchProfileEntity
        ): Boolean {
            return oldItem.email == newItem.email
        }

        override fun areContentsTheSame(
            oldItem: MatchProfileEntity,
            newItem: MatchProfileEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}
