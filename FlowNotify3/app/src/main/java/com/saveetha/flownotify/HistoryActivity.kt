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

import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class HistoryActivity : AppCompatActivity() {

    private lateinit var completedTasksRecyclerView: RecyclerView
    private lateinit var noHistoryTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var completedTaskAdapter: TaskAdapter

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_history)

        initViews()
        setupListeners()
        fetchHistory()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        completedTasksRecyclerView = findViewById(R.id.rv_completed_tasks)
        noHistoryTextView = findViewById(R.id.tv_no_history)
        backButton = findViewById(R.id.btn_back)
        
        completedTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        completedTaskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                showTaskDetailsDialog(task)
            },
            onTaskCheckedChange = { _, _ -> },
            onDeleteClick = { },
            onFinalCompleteClick = { }
        )
        completedTasksRecyclerView.adapter = completedTaskAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchHistory() {
        lifecycleScope.launch {
            try {
                val response = apiService.getTasks("completed", null)
                if (response.isSuccessful) {
                    val tasks = response.body()?.values?.flatten() ?: emptyList()
                    if (tasks.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        completedTaskAdapter.updateData(tasks)
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
            completedTasksRecyclerView.visibility = View.GONE
            noHistoryTextView.visibility = View.VISIBLE
        } else {
            completedTasksRecyclerView.visibility = View.VISIBLE
            noHistoryTextView.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showTaskDetailsDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_details, null)
        val dialog = AlertDialog.Builder(this, R.style.AlertDialog_Transparent)
            .setView(dialogView)
            .create()

        dialog.show()

        val window = dialog.window
        window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), android.view.WindowManager.LayoutParams.WRAP_CONTENT)

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