package com.saveetha.flownotify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.TaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Map

import okhttp3.OkHttpClient
import com.saveetha.flownotify.network.AuthInterceptor

class HomeActivity : AppCompatActivity() {

    private lateinit var greetingTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var flowMessageTextView: TextView
    private lateinit var upcomingTasksRecyclerView: RecyclerView
    private lateinit var emptyUpcomingTasks: LinearLayout
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var emptySchedule: LinearLayout
    private lateinit var addTaskButton: Button
    private lateinit var addEventButton: Button

    private lateinit var taskAdapter: TaskAdapter

    private val apiService: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this))
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupCurrentDate()
        setupBottomNavigation()
        setupListeners()

        // These methods would fetch data from your backend
        loadUserData()
        loadFlowData()
        loadUpcomingTasks()
        loadTodaysSchedule()
    }

    private fun initViews() {
        greetingTextView = findViewById(R.id.tv_greeting)
        dateTextView = findViewById(R.id.tv_date)
        flowMessageTextView = findViewById(R.id.tv_flow_message)
        upcomingTasksRecyclerView = findViewById(R.id.upcoming_tasks_recyclerview)
        emptyUpcomingTasks = findViewById(R.id.empty_upcoming_tasks)
        scheduleContainer = findViewById(R.id.schedule_container)
        emptySchedule = findViewById(R.id.empty_schedule)
        addTaskButton = findViewById(R.id.btn_add_task)
        addEventButton = findViewById(R.id.btn_add_event)

        // Setup RecyclerView for upcoming tasks
        taskAdapter = TaskAdapter(emptyList())
        upcomingTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        upcomingTasksRecyclerView.adapter = taskAdapter
    }

    private fun setupCurrentDate() {
        // Format and display current date
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

    // Methods to load data from backend
    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")
        greetingTextView.text = "Hello, $userName"

        // API call example (using retrofit or your preferred network library):
        /*
        apiService.getUserProfile().enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        greetingTextView.text = "Hello, ${it.firstName}"
                    }
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                // Handle error
            }
        })
        */
    }

    private fun loadFlowData() {
        // In a real app, this would make an API call to get flow data
        // For now, we'll just set placeholders
        flowMessageTextView.text = "Loading..."

        // The actual progress circle would be created dynamically based on backend data
        // API call example:
        /*
        apiService.getTodaysFlow().enqueue(object : Callback<FlowData> {
            override fun onResponse(call: Call<FlowData>, response: Response<FlowData>) {
                if (response.isSuccessful) {
                    val flowData = response.body()
                    flowData?.let {
                        flowMessageTextView.text = it.message
                        // Create and display progress circle with it.percentage
                        setupProgressCircle(it.percentage)
                    }
                }
            }

            override fun onFailure(call: Call<FlowData>, t: Throwable) {
                // Handle error
            }
        })
        */
    }

private fun loadUpcomingTasks() {
    apiService.getTasks(filter = "upcoming").enqueue(
        object : Callback<Map<String, List<TaskResponse>>> {
            override fun onResponse(
                call: Call<Map<String, List<TaskResponse>>>,
                response: Response<Map<String, List<TaskResponse>>>
            ) {
                if (response.isSuccessful) {
                    val tasksMap = response.body()
                    val upcomingTasks = tasksMap?.get("upcoming") ?: emptyList()

                    if (upcomingTasks.isEmpty()) {
                        upcomingTasksRecyclerView.visibility = View.GONE
                        emptyUpcomingTasks.visibility = View.VISIBLE
                    } else {
                        upcomingTasksRecyclerView.visibility = View.VISIBLE
                        emptyUpcomingTasks.visibility = View.GONE
                        taskAdapter.updateTasks(upcomingTasks)
                    }
                } else {
                    Toast.makeText(
                        this@HomeActivity,
                        "Failed to load tasks: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    upcomingTasksRecyclerView.visibility = View.GONE
                    emptyUpcomingTasks.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<Map<String, List<TaskResponse>>>, t: Throwable) {
                Toast.makeText(
                    this@HomeActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                upcomingTasksRecyclerView.visibility = View.GONE
                emptyUpcomingTasks.visibility = View.VISIBLE
            }
        }
    )
}
    private fun loadTodaysSchedule() {
        // In a real app, this would make an API call to get today's schedule
        // For now, we'll just display the empty state
        scheduleContainer.visibility = View.GONE
        emptySchedule.visibility = View.VISIBLE

        // API call example:
        /*
        apiService.getTodaysSchedule().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    val events = response.body()
                    if (events.isNullOrEmpty()) {
                        scheduleContainer.visibility = View.GONE
                        emptySchedule.visibility = View.VISIBLE
                    } else {
                        scheduleContainer.visibility = View.VISIBLE
                        emptySchedule.visibility = View.GONE

                        // Clear previous views
                        scheduleContainer.removeAllViews()

                        // Add event items
                        for (event in events) {
                            val eventView = createEventView(event)
                            scheduleContainer.addView(eventView)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                // Handle error
            }
        })
        */
    }

    // These methods would create views for tasks and events in a real app
    /*
    private fun createTaskView(task: Task): View {
        val view = layoutInflater.inflate(R.layout.item_task, null)
        // Bind task data to view
        return view
    }

    private fun createEventView(event: Event): View {
        val view = layoutInflater.inflate(R.layout.item_event, null)
        // Bind event data to view
        return view
    }
    */

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen
        loadUserData()
        loadUpcomingTasks()
        loadTodaysSchedule()
    }
}
