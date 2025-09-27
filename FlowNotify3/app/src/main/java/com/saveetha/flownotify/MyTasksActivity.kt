package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.Task
import kotlinx.coroutines.launch
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

class MyTasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var searchEditText: EditText
    private lateinit var chipAll: Chip
    private lateinit var chipToday: Chip
    private lateinit var chipUpcoming: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var taskAdapter: TaskAdapter

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasks)

        initViews()
        setupListeners()
        setupBottomNavigation()
        fetchTasks(currentFilter)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_tasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                showTaskDetailsDialog(task)
            },
            onTaskCheckedChange = { task, isChecked ->
                // The adapter handles the visual state change
            },
            onDeleteClick = { task ->
                deleteTask(task.id)
            },
            onFinalCompleteClick = { task ->
                markTaskAsComplete(task.id)
            }
        )
        recyclerView.adapter = taskAdapter

        emptyStateLayout = findViewById(R.id.layout_empty_state)
        searchEditText = findViewById(R.id.et_search)

        chipAll = findViewById(R.id.chip_all)
        chipToday = findViewById(R.id.chip_today)
        chipUpcoming = findViewById(R.id.chip_upcoming)
        chipCompleted = findViewById(R.id.chip_completed)
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            startActivity(Intent(this, NewTaskActivity::class.java))
        }

        searchEditText.setOnEditorActionListener { _, _, _ ->
            performSearch(searchEditText.text.toString())
            true
        }

        chipAll.setOnClickListener { updateFilter("all") }
        chipToday.setOnClickListener { updateFilter("today") }
        chipUpcoming.setOnClickListener { updateFilter("upcoming") }
        chipCompleted.setOnClickListener { updateFilter("completed") }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_tasks

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_tasks -> true
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateFilter(filter: String) {
        currentFilter = filter
        fetchTasks(filter)
    }

    private fun performSearch(query: String) {
        fetchTasks(currentFilter, query)
    }

    private fun fetchTasks(filter: String, searchQuery: String? = null) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = apiService.getTasks(filter, searchQuery)
                showLoading(false)

                if (response.isSuccessful) {
                    val tasksMap = response.body()
                    val displayList = mutableListOf<Any>()

                    if (filter == "completed") {
                        tasksMap?.get("completed")?.let {
                            if (it.isNotEmpty()) {
                                displayList.addAll(it)
                            }
                        }
                    } else {
                        tasksMap?.get("today")?.let {
                            if (it.isNotEmpty()) {
                                displayList.add("Today")
                                displayList.addAll(it)
                            }
                        }
                        tasksMap?.get("tomorrow")?.let {
                            if (it.isNotEmpty()) {
                                displayList.add("Tomorrow")
                                displayList.addAll(it)
                            }
                        }
                        tasksMap?.get("upcoming")?.let {
                            if (it.isNotEmpty()) {
                                displayList.add("Upcoming")
                                displayList.addAll(it)
                            }
                        }
                    }

                    if (displayList.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        taskAdapter.updateData(displayList)
                    }
                } else {
                    showError("Failed to load tasks")
                    showEmptyState(true)
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Network error: ${e.message}")
                showEmptyState(true)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
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
        dueDate.text = task.dueDate ?: "No date specified"
        dueTime.text = task.time ?: "No time specified"
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
                    Toast.makeText(this@MyTasksActivity, "Task marked as complete", Toast.LENGTH_SHORT).show()
                    fetchTasks(currentFilter) // Refresh the data
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
                    Toast.makeText(this@MyTasksActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                    fetchTasks(currentFilter) // Refresh the data
                } else {
                    showError("Failed to delete task: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchTasks(currentFilter)
    }
}
