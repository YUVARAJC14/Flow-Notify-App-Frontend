package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.UpcomingTask

class UpcomingTaskAdapter(private var tasks: List<UpcomingTask>) : RecyclerView.Adapter<UpcomingTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_title)
        val time: TextView = itemView.findViewById(R.id.task_time)
        val priorityDot: View = itemView.findViewById(R.id.task_priority_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.time.text = task.time

        // Set priority dot color
        val colorRes = when (task.priority.lowercase()) {
            "high" -> R.color.priority_high
            "medium" -> R.color.priority_medium
            "low" -> R.color.priority_low
            else -> R.color.gray // Default color
        }
        holder.priorityDot.background.setTint(ContextCompat.getColor(holder.itemView.context, colorRes))
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<UpcomingTask>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}
