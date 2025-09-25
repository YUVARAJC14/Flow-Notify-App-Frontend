package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.UpcomingTask

class UpcomingTaskAdapter(private var tasks: List<UpcomingTask>, private val onTaskClick: (UpcomingTask) -> Unit) : RecyclerView.Adapter<UpcomingTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_title)
        val time: TextView = itemView.findViewById(R.id.task_time)
        val priorityDot: View = itemView.findViewById(R.id.priority_dot)
        val priorityBar: View = itemView.findViewById(R.id.priority_bar)
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
            "high" -> R.color.red
            "medium" -> R.color.orange
            "low" -> R.color.green
            else -> R.color.gray // Default color
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorRes)
        holder.priorityDot.background.setTint(color)
        holder.priorityBar.setBackgroundColor(color)
        holder.itemView.setOnClickListener { onTaskClick(task) }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<UpcomingTask>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}
