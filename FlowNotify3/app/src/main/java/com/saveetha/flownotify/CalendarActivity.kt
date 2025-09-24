package com.saveetha.flownotify

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
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

    private lateinit var monthYearTextView: TextView
    private lateinit var calendarGrid: GridLayout
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private var currentMonth = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    private lateinit var scheduleScrollView: NestedScrollView
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var noEventsTextView: TextView

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }
    private var monthlyEvents: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        initViews()
        setupBottomNavigation()
        setupListeners()
        generateCalendar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EVENT_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d("CalendarActivity", "Received result from NewEventActivity.")

            val newEvent: Event? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("new_event", Event::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra("new_event") as? Event
            }

            if (newEvent != null) {
                Log.d("CalendarActivity", "Successfully received new event: ${newEvent.title}")
                monthlyEvents.add(newEvent)
                redrawCalendar()
            } else {
                Log.e("CalendarActivity", "Failed to retrieve new_event from result. Forcing network refresh.")
                // Fallback to be safe
                generateCalendar()
            }
        }
    }

    private fun initViews() {
        monthYearTextView = findViewById(R.id.tv_month_year)
        calendarGrid = findViewById(R.id.calendar_grid)
        btnPrevMonth = findViewById(R.id.btn_prev_month)
        btnNextMonth = findViewById(R.id.btn_next_month)
        scheduleScrollView = findViewById(R.id.schedule_scroll_view)
        scheduleContainer = findViewById(R.id.schedule_container)
        noEventsTextView = findViewById(R.id.tv_no_events)
    }

    private fun setupListeners() {
        findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            val intent = Intent(this, NewEventActivity::class.java)
            startActivityForResult(intent, ADD_EVENT_REQUEST)
        }

        btnPrevMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            generateCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            generateCalendar()
        }
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

    private fun generateCalendar() {
        lifecycleScope.launch {
            fetchEventsForMonth()
            redrawCalendar()
        }
    }

    private fun redrawCalendar() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = dateFormat.format(currentMonth.time)

        calendarGrid.removeAllViews()
        val calendar = currentMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2
        if (dayOfWeek < 0) dayOfWeek = 6

        for (i in 0 until dayOfWeek) {
            addDayToCalendar("", false, false, false)
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
        val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonthStr = monthFormat.format(calendar.time)

        for (i in 1..daysInMonth) {
            val isToday = isCurrentMonth && i == todayDayOfMonth
            val isSelected = selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == i

            val dateStr = "$currentMonthStr-${String.format("%02d", i)}"
            val hasEvent = monthlyEvents.any { it.date == dateStr }

            addDayToCalendar(i.toString(), isToday, isSelected, hasEvent)
        }

        val filledCells = dayOfWeek + daysInMonth
        val remainingCells = 42 - filledCells
        for (i in 0 until remainingCells) {
            addDayToCalendar("", false, false, false)
        }
        updateEventsForSelectedDate()
    }

    private suspend fun fetchEventsForMonth() {
        val monthCalendar = currentMonth.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = monthCalendar.time
        monthCalendar.set(Calendar.DAY_OF_MONTH, monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDay = monthCalendar.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = sdf.format(firstDay)
        val endDate = sdf.format(lastDay)

        try {
            val response = apiService.getEvents(startDate, endDate)
            if (response.isSuccessful) {
                monthlyEvents = response.body()?.toMutableList() ?: mutableListOf()
            } else {
                Toast.makeText(this@CalendarActivity, "Failed to load events for the month.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@CalendarActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDayToCalendar(dayText: String, isToday: Boolean, isSelected: Boolean, hasEvent: Boolean) {
        val inflater = LayoutInflater.from(this)
        val dayView = inflater.inflate(R.layout.calendar_day_layout, calendarGrid, false)

        val dayTextView = dayView.findViewById<TextView>(R.id.tv_day_text)
        val eventDot = dayView.findViewById<View>(R.id.event_dot)

        dayTextView.text = dayText

        if (dayText.isNotEmpty()) {
            if (isSelected) {
                dayTextView.setBackgroundResource(R.drawable.bg_calendar_day_selected)
                dayTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (isToday) {
                dayTextView.setBackgroundResource(R.drawable.bg_calendar_day_today)
                dayTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                dayTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
            }

            if (hasEvent) {
                eventDot.visibility = View.VISIBLE
            }

            dayView.setOnClickListener {
                val day = dayText.toInt()
                selectedDate = (currentMonth.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, day)
                }
                redrawCalendar()
            }
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
            setMargins(4, 4, 4, 4)
        }

        calendarGrid.addView(dayView, params)
    }

    private fun updateEventsForSelectedDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = sdf.format(selectedDate.time)
        val events = monthlyEvents.filter { it.date == selectedDateStr }

        if (events.isEmpty()) {
            scheduleScrollView.visibility = View.GONE
            noEventsTextView.visibility = View.VISIBLE
        } else {
            scheduleScrollView.visibility = View.VISIBLE
            noEventsTextView.visibility = View.GONE
            scheduleContainer.removeAllViews()
            val inflater = LayoutInflater.from(this)
            for (event in events) {
                val eventView = inflater.inflate(R.layout.item_event, scheduleContainer, false)
                val cardRoot = eventView.findViewById<CardView>(R.id.card_event_root)
                val titleTextView = eventView.findViewById<TextView>(R.id.event_title)
                val timeTextView = eventView.findViewById<TextView>(R.id.event_time)
                val locationTextView = eventView.findViewById<TextView>(R.id.event_location)

                titleTextView.text = event.title
                timeTextView.text = "${event.startTime} - ${event.endTime}"
                locationTextView.text = event.location
                locationTextView.visibility = if (event.location.isNullOrEmpty()) View.GONE else View.VISIBLE

                val categoryDrawable = when (event.category.lowercase()) {
                    "work" -> R.drawable.bg_event_work
                    "personal" -> R.drawable.bg_event_personal
                    "health" -> R.drawable.bg_event_health
                    "social" -> R.drawable.bg_event_social
                    else -> R.drawable.bg_event_work
                }
                cardRoot.setBackgroundResource(categoryDrawable)

                scheduleContainer.addView(eventView)
            }
        }
    }

    companion object {
        private const val ADD_EVENT_REQUEST = 1
    }
}