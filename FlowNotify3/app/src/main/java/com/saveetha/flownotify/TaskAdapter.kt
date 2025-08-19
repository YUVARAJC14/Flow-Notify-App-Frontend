package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.TaskResponse

class TaskAdapter(private var tasks: List<TaskResponse>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_title)
        val time: TextView = itemView.findViewById(R.id.task_time)
        val priorityDot: View = itemView.findViewById(R.id.task_priority_dot)
        val checkBox: CheckBox = itemView.findViewById(R.id.task_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.time.text = "${task.dueDate} ${task.dueTime}"
        holder.checkBox.isChecked = task.isCompleted

        // Set priority dot color
        val colorRes = when (task.priority) {
            "high" -> R.color.priority_high
            "medium" -> R.color.priority_medium
            "low" -> R.color.priority_low
            else -> R.color.gray // Default color
        }
        holder.priorityDot.background.setTint(ContextCompat.getColor(holder.itemView.context, colorRes))

        // Handle checkbox state change (for future updates)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // In a real app, you would update the task's completion status in the backend
            // For now, we'll just update the local object
            // Create a new task instance with updated completion status
        val updatedTask = task.copy(isCompleted = isChecked)
        tasks = tasks.toMutableList().apply {
            set(position, updatedTask)
            }
        notifyItemChanged(position)            // You might want to notify a listener or ViewModel here
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskResponse>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}