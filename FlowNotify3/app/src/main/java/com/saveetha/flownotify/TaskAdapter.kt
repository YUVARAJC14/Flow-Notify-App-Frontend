package com.saveetha.flownotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.network.Task
import com.saveetha.flownotify.network.TaskResponse

class TaskAdapter(private var items: List<Any>, private val onTaskClick: (Task) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TASK = 1
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_title)
        val time: TextView = itemView.findViewById(R.id.task_time)
        val priorityDot: View = itemView.findViewById(R.id.task_priority_dot)
        val checkBoxImage: ImageView = itemView.findViewById(R.id.task_checkbox_image)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.tv_header_title)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_HEADER
            is Task -> VIEW_TYPE_TASK
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.headerTitle.text = items[position] as String
        } else if (holder is TaskViewHolder) {
            val task = items[position] as Task
            holder.title.text = task.title
            holder.time.text = task.time
            holder.checkBoxImage.setImageResource(if (task.isCompleted) R.drawable.checkbox_selector else R.drawable.checkbox_selector)
            holder.checkBoxImage.setImageState(intArrayOf(if (task.isCompleted) android.R.attr.state_checked else -android.R.attr.state_checked), false)

            val colorRes = when (task.priority.lowercase()) {
                "high" -> R.color.priority_high
                "medium" -> R.color.priority_medium
                "low" -> R.color.priority_low
                else -> R.color.gray
            }
            holder.priorityDot.background.setTint(ContextCompat.getColor(holder.itemView.context, colorRes))

            holder.itemView.setOnClickListener { onTaskClick(task) }

            holder.checkBoxImage.setOnClickListener { view ->
                val isChecked = !task.isCompleted
                val updatedTask = task.copy(isCompleted = isChecked)
                val mutableItems = items.toMutableList()
                mutableItems[position] = updatedTask
                items = mutableItems
                notifyItemChanged(position)
                // Trigger the onTaskClick callback with the updated task to handle completion logic
                onTaskClick(updatedTask)
                view.performClick() // Simulate a click to trigger the state change drawable
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Any>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
