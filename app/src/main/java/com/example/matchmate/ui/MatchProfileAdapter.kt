package com.example.matchmate.ui

import android.content.res.ColorStateList
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
import com.example.matchmate.R
import com.example.matchmate.database.MatchProfileEntity
import com.example.matchmate.model.DecisionStatus

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

            declineButton.setOnClickListener {
                onDecline(profile)
            }
            acceptButton.setOnClickListener {
                onAccept(profile)
            }

            // Reset recycled button views before applying the current decision state.
            declineButton.text = itemView.context.getString(R.string.decline)
            acceptButton.text = itemView.context.getString(R.string.accept)
            declineButton.setTextColor(itemView.context.getColor(R.color.matchmate_decline_text))
            acceptButton.setTextColor(itemView.context.getColor(R.color.white))
            declineButton.setBackgroundResource(R.drawable.bg_decline_button)
            acceptButton.setBackgroundResource(R.drawable.bg_accept_button)
            declineButton.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            acceptButton.backgroundTintList = ColorStateList.valueOf(
                itemView.context.getColor(R.color.matchmate_brand)
            )
            declineButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            acceptButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            declineButton.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE
            declineButton.isEnabled = true
            acceptButton.isEnabled = true
            declineButton.isClickable = true
            acceptButton.isClickable = true
            declineButton.alpha = 1f
            acceptButton.alpha = 1f

            when (profile.decision) {
                DecisionStatus.ACCEPTED -> {
                    // Accepted cards show a single burgundy action-style state.
                    declineButton.visibility = View.GONE
                    acceptButton.text = itemView.context.getString(R.string.accepted)
                    acceptButton.setBackgroundResource(R.drawable.bg_accept_button)
                    acceptButton.backgroundTintList = ColorStateList.valueOf(
                        itemView.context.getColor(R.color.matchmate_brand)
                    )
                    acceptButton.setTextColor(itemView.context.getColor(R.color.white))
                    acceptButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_heart_outline,
                        0,
                        0,
                        0
                    )
                    acceptButton.compoundDrawablePadding = 10
                    acceptButton.isEnabled = true
                    acceptButton.isClickable = false
                    acceptButton.setOnClickListener(null)
                    acceptButton.alpha = 1f
                }
                DecisionStatus.DECLINED -> {
                    // Declined cards show a single muted state button.
                    acceptButton.visibility = View.GONE
                    declineButton.text = itemView.context.getString(R.string.declined)
                    declineButton.setBackgroundResource(R.drawable.bg_declined_state_button)
                    declineButton.backgroundTintList = ColorStateList.valueOf(
                        itemView.context.getColor(R.color.matchmate_declined_state_background)
                    )
                    declineButton.setTextColor(itemView.context.getColor(R.color.matchmate_declined_state_text))
                    declineButton.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_close,
                        0,
                        0,
                        0
                    )
                    declineButton.compoundDrawablePadding = 8
                    declineButton.isEnabled = true
                    declineButton.isClickable = false
                    declineButton.setOnClickListener(null)
                    declineButton.alpha = 1f
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MatchProfileEntity>() {
        override fun areItemsTheSame(
            oldItem: MatchProfileEntity,
            newItem: MatchProfileEntity
        ): Boolean {
            // Email is stable and unique for profiles from the API.
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
