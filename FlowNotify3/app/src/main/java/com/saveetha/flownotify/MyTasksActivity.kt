package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.flownotify.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var searchEditText: EditText
    private lateinit var chipAll: Chip
    private lateinit var chipToday: Chip
    private lateinit var chipUpcoming: Chip
    private lateinit var chipCompleted: Chip

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
        // Show loading state if needed
        showLoading(true)

        // This is where you'd make your API call using Retrofit
        // For example:
        /*
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val call = apiService.getTasks(filter, searchQuery)

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val tasks = response.body()
                    if (tasks?.today.isNullOrEmpty() &&
                        tasks?.tomorrow.isNullOrEmpty() &&
                        tasks?.nextWeek.isNullOrEmpty()) {
                        // Show empty state
                        showEmptyState(true)
                    } else {
                        // Update RecyclerView with tasks
                        showEmptyState(false)
                        // recyclerView.adapter = TaskAdapter(tasks)
                    }
                } else {
                    // Handle error
                    showError("Failed to load tasks")
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                showLoading(false)
                showError("Network error: ${t.message}")
            }
        })
        */

        // For this template, we'll just simulate the API call
        showLoading(false)
        showEmptyState(true) // Show empty state initially
    }

    private fun showLoading(isLoading: Boolean) {
        // Implement loading state (e.g., show/hide a ProgressBar)
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
        // Show error message (e.g., using a Snackbar)
    }

    override fun onResume() {
        super.onResume()
        // Refresh tasks when returning to this screen
        fetchTasks(currentFilter)
    }
}