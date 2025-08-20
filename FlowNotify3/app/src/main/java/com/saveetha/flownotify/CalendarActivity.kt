package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        initViews()
        setupBottomNavigation()
        setupListeners()
        generateCalendar()
        updateEventsForSelectedDate()
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
        // Add Event FAB
        findViewById<FloatingActionButton>(R.id.fab_add_event).setOnClickListener {
            val intent = Intent(this, NewEventActivity::class.java)
            startActivity(intent)
        }

        // Month Navigation
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
                R.id.nav_calendar -> true // Already on Calendar
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
        // Update month/year display
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = dateFormat.format(currentMonth.time)

        // Clear previous calendar days
        calendarGrid.removeAllViews()

        // Clone the current month calendar to work with
        val calendar = (currentMonth.clone() as Calendar)

        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Determine day of week for the 1st of the month (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
        // Adjust to match our grid which starts with Monday as first day
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2
        if (dayOfWeek < 0) dayOfWeek = 6 // Convert Sunday from -1 to 6

        // Add empty spaces before the 1st day
        for (i in 0 until dayOfWeek) {
            addDayToCalendar("", false, false)
        }

        // Get the number of days in the month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Get today's date components for highlighting
        val today = Calendar.getInstance()
        val isCurrentMonth = (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
        val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

        // Add the days of the month
        for (i in 1..daysInMonth) {
            val isToday = isCurrentMonth && i == todayDayOfMonth
            val isSelected = (selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == i)

            addDayToCalendar(i.toString(), isToday, isSelected)
        }

        // Fill the remaining cells to complete the grid
        val filledCells = dayOfWeek + daysInMonth
        val remainingCells = 42 - filledCells // 6 rows * 7 columns = 42 cells

        for (i in 0 until remainingCells) {
            addDayToCalendar("", false, false)
        }
    }

    private fun addDayToCalendar(dayText: String, isToday: Boolean, isSelected: Boolean) {
        val context = this
        val dayView = TextView(context).apply {
            text = dayText
            gravity = Gravity.CENTER
            setPadding(8, 20, 8, 20)
            textSize = 14f

            if (dayText.isEmpty()) {
                // Empty day cell
                setBackgroundResource(0)
            } else {
                // Regular day
                if (isToday) {
                    // Today's date
                    setBackgroundResource(R.drawable.bg_calendar_day_today)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                } else if (isSelected) {
                    // Selected date
                    setBackgroundResource(R.drawable.bg_calendar_day_selected)
                    setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    // Normal day
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                // Make days clickable
                setOnClickListener {
                    // Update selected date and refresh calendar
                    val day = dayText.toInt()
                    selectedDate = (currentMonth.clone() as Calendar)
                    selectedDate.set(Calendar.DAY_OF_MONTH, day)
                    generateCalendar()

                    // Update events for the selected day
                    updateEventsForSelectedDate()
                }
            }
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
            setMargins(2, 2, 2, 2)
        }

        calendarGrid.addView(dayView, params)
    }

    // In a real app, this method would fetch events for the selected date from backend
    private fun updateEventsForSelectedDate() {
        // API call to get events for the selected date
        // For now, we'll simulate an empty list.
        val events = emptyList<Any>() // Replace with your Event class

        if (events.isEmpty()) {
            scheduleScrollView.visibility = View.GONE
            noEventsTextView.visibility = View.VISIBLE
        } else {
            scheduleScrollView.visibility = View.VISIBLE
            noEventsTextView.visibility = View.GONE
            scheduleContainer.removeAllViews()
            // In a real app, you would loop through events and add them to scheduleContainer
            // For example:
            // for (event in events) {
            //     val eventView = layoutInflater.inflate(R.layout.item_event, scheduleContainer, false)
            //     // ... bind event data to eventView ...
            //     scheduleContainer.addView(eventView)
            // }
        }
    }
}
