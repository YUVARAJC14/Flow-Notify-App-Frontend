package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class OnboardingItem(val imageRes: Int, val title: String, val description: String)

class OnboardingAdapter(private val items: List<OnboardingItem>) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_page_item, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_onboarding_image)
        private val titleView: TextView = itemView.findViewById(R.id.tv_onboarding_title)
        private val descriptionView: TextView = itemView.findViewById(R.id.tv_onboarding_description)

        fun bind(item: OnboardingItem) {
            imageView.setImageResource(item.imageRes)
            titleView.text = item.title
            descriptionView.text = item.description
        }
    }
}