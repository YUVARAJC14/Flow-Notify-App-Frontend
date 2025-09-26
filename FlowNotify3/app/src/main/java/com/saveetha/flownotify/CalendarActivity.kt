package com.saveetha.flownotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var calendarView: CalendarView
    private lateinit var scheduleTitleTextView: TextView
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var noEventsTextView: TextView
    private lateinit var fabAddEvent: FloatingActionButton
    private lateinit var scheduleAdapter: ScheduleAdapter

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        initViews()
        handleTopNotch()
        setupBottomNavigation()
        setupListeners()

        // Fetch events for the current date on initial load
        fetchEventsForDate(Calendar.getInstance())
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        calendarView = findViewById(R.id.calendar_view)
        scheduleTitleTextView = findViewById(R.id.tv_schedule_title)
        scheduleRecyclerView = findViewById(R.id.rv_schedule)
        noEventsTextView = findViewById(R.id.tv_no_events)
        fabAddEvent = findViewById(R.id.fab_add_event)

        // Setup RecyclerView
        scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(emptyList())
        scheduleRecyclerView.adapter = scheduleAdapter
    }

    private fun handleTopNotch() {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }
    }

    private fun setupListeners() {
        // Listener for date changes on the calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            fetchEventsForDate(selectedCalendar)
            updateScheduleTitle(selectedCalendar)
        }

        // FAB to add a new event
        fabAddEvent.setOnClickListener {
            val intent = Intent(this, NewEventActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchEventsForDate(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = sdf.format(calendar.time)

        lifecycleScope.launch {
            try {
                val response = apiService.getEvents(startDate = selectedDateStr, endDate = selectedDateStr)
                if (response.isSuccessful) {
                    val events = response.body() ?: emptyList()
                    updateScheduleUI(events)
                } else {
                    Toast.makeText(this@CalendarActivity, "Failed to load events.", Toast.LENGTH_SHORT).show()
                    updateScheduleUI(emptyList()) // Show empty state on failure
                }
            } catch (e: Exception) {
                Toast.makeText(this@CalendarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                updateScheduleUI(emptyList()) // Show empty state on error
            }
        }
    }

    private fun updateScheduleUI(events: List<Event>) {
        if (events.isEmpty()) {
            scheduleRecyclerView.visibility = View.GONE
            noEventsTextView.visibility = View.VISIBLE
        } else {
            scheduleRecyclerView.visibility = View.VISIBLE
            noEventsTextView.visibility = View.GONE
            scheduleAdapter.updateEvents(events)
        }
    }

    private fun updateScheduleTitle(calendar: Calendar) {
        val today = Calendar.getInstance()
        val title = if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            "Today's Schedule"
        } else {
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            "Schedule for ${sdf.format(calendar.time)}"
        }
        scheduleTitleTextView.text = title
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_calendar

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, MyTasksActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> true
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

    // Adapter for the schedule RecyclerView
    inner class ScheduleAdapter(private var events: List<Event>) : RecyclerView.Adapter<ScheduleAdapter.EventViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_event, parent, false)
            return EventViewHolder(view)
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            holder.bind(events[position])
        }

        override fun getItemCount(): Int = events.size

        fun updateEvents(newEvents: List<Event>) {
            this.events = newEvents
            notifyDataSetChanged()
        }

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.tv_event_title)
            private val timeTextView: TextView = itemView.findViewById(R.id.tv_event_time)
            private val locationTextView: TextView = itemView.findViewById(R.id.tv_event_location)
            private val container: View = itemView.findViewById(R.id.event_container)

            fun bind(event: Event) {
                titleTextView.text = event.title
                timeTextView.text = "${event.startTime} - ${event.endTime}"
                locationTextView.text = event.location ?: ""
                locationTextView.visibility = if (event.location.isNullOrEmpty()) View.GONE else View.VISIBLE

                val backgroundColor = when (event.category.lowercase()) {
                    "work" -> R.color.light_blue_bg
                    "personal" -> R.color.light_purple_bg
                    "health" -> R.color.light_green_bg
                    "social" -> R.color.light_pink_bg
                    else -> R.color.light_gray_bg
                }
                container.setBackgroundResource(backgroundColor)
            }
        }
    }
}
