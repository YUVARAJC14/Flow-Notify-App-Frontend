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
        val icon: View = itemView.findViewById(R.id.event_icon)
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
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<ScheduleEvent>) {
        this.events = newEvents
        notifyDataSetChanged()
    }
}
