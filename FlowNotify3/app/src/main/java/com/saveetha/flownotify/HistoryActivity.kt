package com.saveetha.flownotify

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.TaskAdapter
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.Task
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noHistoryTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var historyTaskAdapter: TaskAdapter

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        initViews()
        setupListeners()
        fetchHistory()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_history_tasks)
        noHistoryTextView = findViewById(R.id.tv_no_history)
        backButton = findViewById(R.id.btn_back)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyTaskAdapter = TaskAdapter(emptyList<Task>()) { task ->
            showTaskDetailsDialog(task)
        }
        recyclerView.adapter = historyTaskAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchHistory() {
        lifecycleScope.launch {
            try {
                val response = apiService.getTasks("completed", null) // Assuming 'completed' fetches all non-active
                if (response.isSuccessful) {
                    val tasks = response.body()?.values?.flatten() ?: emptyList()
                    if (tasks.isEmpty()) {
                        showEmptyState(true)
                    }
                    else {
                        showEmptyState(false)
                        historyTaskAdapter.updateData(tasks)
                    }
                } else {
                    showError("Failed to load history")
                    showEmptyState(true)
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
                showEmptyState(true)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            noHistoryTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noHistoryTextView.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showTaskDetailsDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_details, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.tv_task_details_title)
        val description = dialogView.findViewById<TextView>(R.id.tv_task_details_description)
        val dueDate = dialogView.findViewById<TextView>(R.id.tv_task_details_due_date)
        val dueTime = dialogView.findViewById<TextView>(R.id.tv_task_details_due_time)
        val priority = dialogView.findViewById<TextView>(R.id.tv_task_details_priority)
        val completeButton = dialogView.findViewById<Button>(R.id.btn_complete_task)
        val deleteButton = dialogView.findViewById<Button>(R.id.btn_delete_task)

        title.text = task.title
        description.text = task.description ?: "No description available."
        dueDate.text = task.dueDate
        dueTime.text = task.time
        priority.text = task.priority.replaceFirstChar { it.uppercase() } + " Priority"

        completeButton.setOnClickListener {
            markTaskAsComplete(task.id)
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteTask(task.id)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun markTaskAsComplete(taskId: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.updateTask(taskId, mapOf("isCompleted" to true))
                if (response.isSuccessful) {
                    Toast.makeText(this@HistoryActivity, "Task marked as complete", Toast.LENGTH_SHORT).show()
                    fetchHistory() // Refresh the data
                } else {
                    showError("Failed to update task: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun deleteTask(taskId: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.deleteTask(taskId)
                if (response.isSuccessful) {
                    Toast.makeText(this@HistoryActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                    fetchHistory() // Refresh the data
                } else {
                    showError("Failed to delete task: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }
}