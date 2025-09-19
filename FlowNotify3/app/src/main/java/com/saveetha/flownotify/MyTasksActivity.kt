package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
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

    // Current filter state
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasks)

        initViews()
        setupListeners()
        setupBottomNavigation()

        // Initial data load with default filter
        fetchTasks(currentFilter)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_tasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(emptyList())
        recyclerView.adapter = taskAdapter

        emptyStateLayout = findViewById(R.id.layout_empty_state)
        searchEditText = findViewById(R.id.et_search)

        chipAll = findViewById(R.id.chip_all)
        chipToday = findViewById(R.id.chip_today)
        chipUpcoming = findViewById(R.id.chip_upcoming)
        chipCompleted = findViewById(R.id.chip_completed)
    }

    private fun setupListeners() {
        // FAB click listener
        findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            // Navigate to New Task activity
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }

        // Search listener
        searchEditText.setOnEditorActionListener { _, _, _ ->
            performSearch(searchEditText.text.toString())
            true
        }

        // Filter chip listeners
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
                R.id.nav_tasks -> true // Already on Tasks
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
        // Call API with search parameter
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
                    val tasks = tasksMap?.values?.flatten() ?: emptyList()

                    if (tasks.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        taskAdapter.updateTasks(tasks)
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
        // For now, just controlling the empty state visibility
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

    override fun onResume() {
        super.onResume()
        // Refresh tasks when returning to this screen
        fetchTasks(currentFilter)
    }
}
