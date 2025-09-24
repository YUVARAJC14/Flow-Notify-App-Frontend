package com.saveetha.flownotify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.ScheduleEvent
import com.saveetha.flownotify.network.UpcomingTask
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var greetingTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var flowMessageTextView: TextView
    private lateinit var upcomingTasksRecyclerView: RecyclerView
    private lateinit var scheduleRecyclerView: RecyclerView // Changed
    private lateinit var emptyUpcomingTasks: LinearLayout
    private lateinit var emptySchedule: LinearLayout
    private lateinit var addTaskButton: Button
    private lateinit var addEventButton: Button

    private lateinit var upcomingTaskAdapter: UpcomingTaskAdapter
    private lateinit var scheduleEventAdapter: ScheduleEventAdapter // Added

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupCurrentDate()
        setupBottomNavigation()
        setupListeners()

        loadInitialData()
    }

    private fun initViews() {
        greetingTextView = findViewById(R.id.tv_greeting)
        dateTextView = findViewById(R.id.tv_date)
        flowMessageTextView = findViewById(R.id.tv_flow_message)
        upcomingTasksRecyclerView = findViewById(R.id.upcoming_tasks_recyclerview)
        scheduleRecyclerView = findViewById(R.id.schedule_recyclerview) // Changed
        emptyUpcomingTasks = findViewById(R.id.empty_upcoming_tasks)
        emptySchedule = findViewById(R.id.empty_schedule)
        addTaskButton = findViewById(R.id.btn_add_task)
        addEventButton = findViewById(R.id.btn_add_event)

        // Setup RecyclerView for upcoming tasks
        upcomingTaskAdapter = UpcomingTaskAdapter(emptyList()) { task ->
            showTaskDetailsDialog(task)
        }
        upcomingTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        upcomingTasksRecyclerView.adapter = upcomingTaskAdapter

        // Setup RecyclerView for today's schedule
        scheduleEventAdapter = ScheduleEventAdapter(emptyList())
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        scheduleRecyclerView.adapter = scheduleEventAdapter
    }

    private fun setupCurrentDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        dateTextView.text = formattedDate
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Already on Home
                R.id.nav_tasks -> {
                    startActivity(Intent(this, MyTasksActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
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

    private fun setupListeners() {
        addTaskButton.setOnClickListener {
            startActivity(Intent(this, NewTaskActivity::class.java))
        }

        addEventButton.setOnClickListener {
            startActivity(Intent(this, NewEventActivity::class.java))
        }
    }

    private fun loadInitialData() {
        loadUserData()
        loadDashboardSummary()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")
        greetingTextView.text = "Hello, $userName"
    }

    private fun loadDashboardSummary() {
        lifecycleScope.launch {
            try {
                val response = apiService.getDashboardSummary()
                if (response.isSuccessful) {
                    val summary = response.body()
                    summary?.let {
                        flowMessageTextView.text = "You have ${it.todaysFlow.percentage}% flow today!"
                        updateUpcomingTasks(it.upcomingTasks)
                        updateTodaysSchedule(it.todaysSchedule)
                    }
                } else {
                    showError("Failed to load dashboard data: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun updateUpcomingTasks(tasks: List<UpcomingTask>) {
        if (tasks.isEmpty()) {
            upcomingTasksRecyclerView.visibility = View.GONE
            emptyUpcomingTasks.visibility = View.VISIBLE
        } else {
            upcomingTasksRecyclerView.visibility = View.VISIBLE
            emptyUpcomingTasks.visibility = View.GONE
            upcomingTaskAdapter.updateTasks(tasks)
        }
    }

    private fun updateTodaysSchedule(events: List<ScheduleEvent>) {
        if (events.isEmpty()) {
            scheduleRecyclerView.visibility = View.GONE
            emptySchedule.visibility = View.VISIBLE
        } else {
            scheduleRecyclerView.visibility = View.VISIBLE
            emptySchedule.visibility = View.GONE
            scheduleEventAdapter.updateEvents(events)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        upcomingTasksRecyclerView.visibility = View.GONE
        emptyUpcomingTasks.visibility = View.VISIBLE
        scheduleRecyclerView.visibility = View.GONE
        emptySchedule.visibility = View.VISIBLE
    }

    private fun showTaskDetailsDialog(task: UpcomingTask) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_details, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.tv_task_details_title)
        val description = dialogView.findViewById<TextView>(R.id.tv_task_details_description)
        val dueDate = dialogView.findViewById<TextView>(R.id.tv_task_details_due_date)
        val dueTime = dialogView.findViewById<TextView>(R.id.tv_task_details_due_time)
        val priority = dialogView.findViewById<TextView>(R.id.tv_task_details_priority)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_close_details)

        title.text = task.title
        description.text = task.description ?: "No description available."
        dueDate.text = task.dueDate
        dueTime.text = task.time
        priority.text = task.priority.replaceFirstChar { it.uppercase() } + " Priority"

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadInitialData()
    }
}
