package com.saveetha.flownotify

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.AuthInterceptor
import com.saveetha.flownotify.network.CreateEventRequest
import com.saveetha.flownotify.network.Event
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewEventActivity : AppCompatActivity() {

    private lateinit var eventTitleEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var selectedDateTextView: TextView
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var notesEditText: EditText
    private lateinit var reminderTextView: TextView

    // Category selection
    private lateinit var categoryWork: TextView
    private lateinit var categoryPersonal: TextView
    private lateinit var categoryHealth: TextView
    private lateinit var categorySocial: TextView
    private var selectedCategory: String = "Work"

    // Date & Time
    private val calendar: Calendar = Calendar.getInstance()
    private var startTimeHour: Int = 10
    private var startTimeMinute: Int = 0
    private var endTimeHour: Int = 11
    private var endTimeMinute: Int = 0

    private val apiService: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this))
            .build()

        Retrofit.Builder()
            .baseUrl("http://localhost:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }

        initViews()
        setupInitialValues()
        setupListeners()
    }

    private fun initViews() {
        eventTitleEditText = findViewById(R.id.et_event_title)
        locationEditText = findViewById(R.id.et_location)
        selectedDateTextView = findViewById(R.id.tv_selected_date)
        startTimeTextView = findViewById(R.id.tv_start_time)
        endTimeTextView = findViewById(R.id.tv_end_time)
        notesEditText = findViewById(R.id.et_notes)
        reminderTextView = findViewById(R.id.tv_reminder)

        // Categories
        categoryWork = findViewById(R.id.category_work)
        categoryPersonal = findViewById(R.id.category_personal)
        categoryHealth = findViewById(R.id.category_health)
        categorySocial = findViewById(R.id.category_social)
    }

    private fun setupInitialValues() {
        // Set current date
        updateDateDisplay()

        // Set default time
        updateTimeDisplay()
    }

    private fun setupListeners() {
        // Close button
        findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            finish()
        }

        // Date picker
        findViewById<LinearLayout>(R.id.layout_date_picker).setOnClickListener {
            showDatePicker()
        }

        // Time pickers
        findViewById<LinearLayout>(R.id.layout_start_time).setOnClickListener {
            showTimePicker(true)
        }

        findViewById<LinearLayout>(R.id.layout_end_time).setOnClickListener {
            showTimePicker(false)
        }

        // Categories
        categoryWork.setOnClickListener {
            updateCategorySelection("Work")
        }

        categoryPersonal.setOnClickListener {
            updateCategorySelection("Personal")
        }

        categoryHealth.setOnClickListener {
            updateCategorySelection("Health")
        }

        categorySocial.setOnClickListener {
            updateCategorySelection("Social")
        }

        // Reminder selection
        findViewById<LinearLayout>(R.id.layout_reminders).setOnClickListener {
            showReminderOptions()
        }

        // Save button
        findViewById<Button>(R.id.btn_save_event).setOnClickListener {
            saveEvent()
        }
    }

    private fun updateCategorySelection(category: String) {
        // Update visual selection
        when (category) {
            "Work" -> {
                categoryWork.alpha = 1.0f
                categoryPersonal.alpha = 0.6f
                categoryHealth.alpha = 0.6f
                categorySocial.alpha = 0.6f
            }
            "Personal" -> {
                categoryWork.alpha = 0.6f
                categoryPersonal.alpha = 1.0f
                categoryHealth.alpha = 0.6f
                categorySocial.alpha = 0.6f
            }
            "Health" -> {
                categoryWork.alpha = 0.6f
                categoryPersonal.alpha = 0.6f
                categoryHealth.alpha = 1.0f
                categorySocial.alpha = 0.6f
            }
            else -> {
                categoryWork.alpha = 0.6f
                categoryPersonal.alpha = 0.6f
                categoryHealth.alpha = 0.6f
                categorySocial.alpha = 1.0f
            }
        }

        selectedCategory = category
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val hour = if (isStartTime) startTimeHour else endTimeHour
        val minute = if (isStartTime) startTimeMinute else endTimeMinute

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                if (isStartTime) {
                    startTimeHour = hourOfDay
                    startTimeMinute = minute

                    if (endTimeHour < hourOfDay || (endTimeHour == hourOfDay && endTimeMinute < minute)) {
                        endTimeHour = if (hourOfDay < 23) hourOfDay + 1 else hourOfDay
                        endTimeMinute = minute
                    }
                } else {
                    endTimeHour = hourOfDay
                    endTimeMinute = minute

                    if (startTimeHour > hourOfDay || (startTimeHour == hourOfDay && startTimeMinute > minute)) {
                        startTimeHour = hourOfDay
                        startTimeMinute = minute
                    }
                }
                updateTimeDisplay()
            },
            hour,
            minute,
            false // 12-hour format
        )
        timePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        val today = Calendar.getInstance()
        val isToday = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))

        selectedDateTextView.text = if (isToday) {
            "Today, $formattedDate"
        } else {
            formattedDate
        }
    }

    private fun updateTimeDisplay() {
        val startTimeCalendar = Calendar.getInstance()
        startTimeCalendar.set(Calendar.HOUR_OF_DAY, startTimeHour)
        startTimeCalendar.set(Calendar.MINUTE, startTimeMinute)
        val startTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedStartTime = startTimeFormat.format(startTimeCalendar.time)

        val endTimeCalendar = Calendar.getInstance()
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, endTimeHour)
        endTimeCalendar.set(Calendar.MINUTE, endTimeMinute)
        val endTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedEndTime = endTimeFormat.format(endTimeCalendar.time)

        startTimeTextView.text = formattedStartTime
        endTimeTextView.text = formattedEndTime
    }

    private fun showReminderOptions() {
        val reminderOptions = listOf(
            "No reminder",
            "5 minutes before",
            "15 minutes before",
            "30 minutes before",
            "1 hour before",
            "2 hours before",
            "1 day before"
        )
        val currentReminder = reminderTextView.text.toString()
        val currentIndex = reminderOptions.indexOf(currentReminder)
        val nextIndex = (currentIndex + 1) % reminderOptions.size
        reminderTextView.text = reminderOptions[nextIndex]
    }

    private fun getReminderInMinutes(): Int {
        return when (reminderTextView.text.toString()) {
            "5 minutes before" -> 5
            "15 minutes before" -> 15
            "30 minutes before" -> 30
            "1 hour before" -> 60
            "2 hours before" -> 120
            "1 day before" -> 1440
            else -> 0
        }
    }

    private fun saveEvent() {
        val title = eventTitleEditText.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter an event title", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateEventRequest(
            title = title,
            location = locationEditText.text.toString().trim(),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time),
            startTime = String.format("%02d:%02d", startTimeHour, startTimeMinute),
            endTime = String.format("%02d:%02d", endTimeHour, endTimeMinute),
            category = selectedCategory,
            notes = notesEditText.text.toString().trim(),
            reminder = getReminderInMinutes()
        )

        lifecycleScope.launch {
            try {
                val response = apiService.createEvent(request)
                if (response.isSuccessful) {
                    val newEvent = response.body()
                    if (newEvent != null) {
                        Toast.makeText(this@NewEventActivity, "Event created successfully", Toast.LENGTH_SHORT).show()

                        // Schedule notification
                        val reminderMinutes = getReminderInMinutes()
                        if (reminderMinutes > 0) {
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, this@NewEventActivity.calendar.get(Calendar.YEAR))
                                set(Calendar.MONTH, this@NewEventActivity.calendar.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, this@NewEventActivity.calendar.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, startTimeHour)
                                set(Calendar.MINUTE, startTimeMinute)
                                add(Calendar.MINUTE, -reminderMinutes)
                            }
                            NotificationScheduler.scheduleNotification(this@NewEventActivity, calendar.timeInMillis, newEvent.id, "Event", newEvent.title)
                        }

                        val resultIntent = Intent()
                        resultIntent.putExtra("new_event", newEvent as java.io.Serializable)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@NewEventActivity, "Failed to get event data from response", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@NewEventActivity, "Failed to create event: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NewEventActivity, "Error creating event: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}