package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.ScheduleEvent

class ScheduleEventAdapter(private var events: List<ScheduleEvent>, private val onEventClick: (ScheduleEvent) -> Unit) : RecyclerView.Adapter<ScheduleEventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_title)
        val location: TextView = itemView.findViewById(R.id.event_location)
        val time: TextView = itemView.findViewById(R.id.event_time)
        val categoryIndicator: View = itemView.findViewById(R.id.category_indicator)
        val category: TextView = itemView.findViewById(R.id.event_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        holder.location.text = event.location
        holder.time.text = "${event.time} - ${event.endTime}"
        holder.category.text = event.category

        val categoryColor = when (event.category.lowercase()) {
            "work" -> R.color.category_work
            "personal" -> R.color.category_personal
            "social" -> R.color.category_social
            "health" -> R.color.category_health
            else -> R.color.light_gray_bg
        }
        holder.categoryIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, categoryColor))
        
        holder.itemView.setOnClickListener { onEventClick(event) }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<ScheduleEvent>) {
        this.events = newEvents
        notifyDataSetChanged()
    }
}
