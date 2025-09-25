package com.saveetha.flownotify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.PopupMenu
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
    private lateinit var flowPercentageTextView: TextView
    private lateinit var flowProgressBar: ProgressBar
    private lateinit var upcomingTasksRecyclerView: RecyclerView
    private lateinit var scheduleRecyclerView: RecyclerView
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
        flowPercentageTextView = findViewById(R.id.tv_flow_percentage)
        flowProgressBar = findViewById(R.id.progress_bar_flow)
        upcomingTasksRecyclerView = findViewById(R.id.upcoming_tasks_recyclerview)
        scheduleRecyclerView = findViewById(R.id.schedule_recyclerview) // Changed
        emptyUpcomingTasks = findViewById(R.id.empty_upcoming_tasks)
        emptySchedule = findViewById(R.id.empty_schedule)
        addTaskButton = findViewById(R.id.btn_add_task)
        addEventButton = findViewById(R.id.btn_add_event)

        // Setup RecyclerView for upcoming tasks
        upcomingTaskAdapter = UpcomingTaskAdapter(emptyList()) { task: UpcomingTask ->
            showTaskDetailsDialog(task)
        }
        upcomingTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        upcomingTasksRecyclerView.adapter = upcomingTaskAdapter

        // Setup RecyclerView for today's schedule
        scheduleEventAdapter = ScheduleEventAdapter(emptyList()) { event ->
            showEventDetailsDialog(event)
        }
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        scheduleRecyclerView.adapter = scheduleEventAdapter
    }

    private fun showEventDetailsDialog(event: ScheduleEvent) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_details, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.tv_event_details_title)
        val notes = dialogView.findViewById<TextView>(R.id.tv_event_details_notes)
        val date = dialogView.findViewById<TextView>(R.id.tv_event_details_date)
        val time = dialogView.findViewById<TextView>(R.id.tv_event_details_time)
        val location = dialogView.findViewById<TextView>(R.id.tv_event_details_location)
        val completeButton = dialogView.findViewById<Button>(R.id.btn_complete_event)
        val deleteButton = dialogView.findViewById<Button>(R.id.btn_delete_event)

        title.text = event.title
        notes.text = "No notes available for this event view."
        date.text = event.time // Assuming time contains date info for this view
        time.text = event.time
        location.text = event.location ?: "No location specified."

        completeButton.setOnClickListener {
            markEventAsComplete(event.id)
            dialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteEvent(event.id)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun markEventAsComplete(eventId: String) {
        lifecycleScope.launch {
            try {
                val eventIdInt = eventId.toIntOrNull()
                if (eventIdInt == null) {
                    showError("Invalid event ID")
                    return@launch
                }
                val request = com.saveetha.flownotify.network.EventUpdateRequest(completed = true)
                val response = apiService.updateEvent(eventIdInt, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Event marked as complete", Toast.LENGTH_SHORT).show()
                    loadDashboardSummary() // Refresh the data
                } else {
                    showError("Failed to update event: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun deleteEvent(eventId: String) {
        lifecycleScope.launch {
            try {
                val eventIdInt = eventId.toIntOrNull()
                if (eventIdInt == null) {
                    showError("Invalid event ID")
                    return@launch
                }
                val response = apiService.deleteEvent(eventIdInt)
                if (response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Event deleted", Toast.LENGTH_SHORT).show()
                    loadDashboardSummary() // Refresh the data
                } else {
                    showError("Failed to delete event: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
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
        val historyButton = findViewById<ImageButton>(R.id.btn_history_menu)
        historyButton.setOnClickListener { view ->
            val popup = PopupMenu(this@HomeActivity, view)
            popup.menuInflater.inflate(R.menu.home_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_history -> {
                        startActivity(Intent(this, HistoryActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

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
                        val percentage = it.todaysFlow.percentage
                        flowProgressBar.progress = percentage
                        flowPercentageTextView.text = "$percentage%"
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
                    Toast.makeText(this@HomeActivity, "Task marked as complete", Toast.LENGTH_SHORT).show()
                    loadDashboardSummary() // Refresh the data
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
                    Toast.makeText(this@HomeActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                    loadDashboardSummary() // Refresh the data
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
        loadInitialData()
    }
}
