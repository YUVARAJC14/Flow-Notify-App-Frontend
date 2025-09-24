package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.ScheduleEvent

class ScheduleEventAdapter(private var events: List<ScheduleEvent>) : RecyclerView.Adapter<ScheduleEventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_title)
        val location: TextView = itemView.findViewById(R.id.event_location)
        val time: TextView = itemView.findViewById(R.id.event_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        holder.location.text = event.location
        holder.time.text = event.time

        val categoryDrawable = when (event.category.lowercase()) {
            "work" -> R.drawable.bg_category_work
            "personal" -> R.drawable.bg_category_personal
            "health" -> R.drawable.bg_category_health
            "social" -> R.drawable.bg_category_social
            else -> R.drawable.bg_category_work
        }
        holder.itemView.setBackgroundResource(categoryDrawable)
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<ScheduleEvent>) {
        this.events = newEvents
        notifyDataSetChanged()
    }
}
