package com.saveetha.flownotify

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.UpcomingTask

class UpcomingTaskAdapter(private var tasks: List<UpcomingTask>, private val onTaskClick: (UpcomingTask) -> Unit) : RecyclerView.Adapter<UpcomingTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_task_title)
        val time: TextView = itemView.findViewById(R.id.tv_task_time)
        val priorityBar: View = itemView.findViewById(R.id.priority_bar)
        val priorityDot: View = itemView.findViewById(R.id.priority_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.time.text = task.time

        val priorityColor = when (task.priority.lowercase()) {
            "high" -> R.color.priority_high
            "medium" -> R.color.priority_medium
            "low" -> R.color.priority_low
            else -> android.R.color.transparent
        }
        
        val color = ContextCompat.getColor(holder.itemView.context, priorityColor)
        holder.priorityBar.setBackgroundColor(color)

        val dotDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.circle_background)?.mutate() as GradientDrawable
        dotDrawable.setColor(color)
        holder.priorityDot.background = dotDrawable

        holder.itemView.setOnClickListener { onTaskClick(task) }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<UpcomingTask>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}
