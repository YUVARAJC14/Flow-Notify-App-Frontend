package com.saveetha.flownotify

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.CreateTaskRequest
import com.saveetha.flownotify.network.TaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import okhttp3.OkHttpClient
import com.saveetha.flownotify.network.AuthInterceptor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewTaskActivity : AppCompatActivity() {

    private lateinit var editTextTaskTitle: EditText
    private lateinit var editTextTaskDescription: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var tvDueTime: TextView
    private lateinit var radioGroupPriority: RadioGroup
    private lateinit var switch10Min: SwitchCompat
    private lateinit var switch1Hour: SwitchCompat
    private lateinit var switch1Day: SwitchCompat
    private lateinit var btnSaveTask: Button
    private lateinit var btnClose: ImageButton

    private val calendar = Calendar.getInstance()
    private var hasSetDate = false
    private var hasSetTime = false


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
        setContentView(R.layout.activity_new_task)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }

        // Initialize UI components
        initViews()
        setupListeners()
    }

    private fun initViews() {
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle)
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription)
        tvDueDate = findViewById(R.id.tvDueDate)
        tvDueTime = findViewById(R.id.tvDueTime)
        radioGroupPriority = findViewById(R.id.radioGroupPriority)
        switch10Min = findViewById(R.id.switch10Min)
        switch1Hour = findViewById(R.id.switch1Hour)
        switch1Day = findViewById(R.id.switch1Day)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnClose = findViewById(R.id.btnClose)
    }

    private fun setupListeners() {
        // Date picker dialog
        tvDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Time picker dialog
        tvDueTime.setOnClickListener {
            showTimePickerDialog()
        }

        // Close button
        btnClose.setOnClickListener {
            finish()
        }

        // Save task button
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateText()
                hasSetDate = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeText()
                hasSetTime = true
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )

        timePickerDialog.show()
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvDueDate.text = dateFormat.format(calendar.time)
    }

    private fun updateTimeText() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        tvDueTime.text = timeFormat.format(calendar.time)
    }

    private fun getSelectedPriority(): String {
        return when (radioGroupPriority.checkedRadioButtonId) {
            R.id.radioButtonHigh -> "high"
            R.id.radioButtonMedium -> "medium"
            R.id.radioButtonLow -> "low"
            else -> "medium" // Default
        }
    }

    private fun getEnabledReminders(): List<String> {
        val reminders = mutableListOf<String>()

        if (switch10Min.isChecked) {
            reminders.add("10m")
        }

        if (switch1Hour.isChecked) {
            reminders.add("1h")
        }

        if (switch1Day.isChecked) {
            reminders.add("1d")
        }

        return reminders
    }

    private fun validateInputs(): Boolean {
        if (editTextTaskTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!hasSetDate) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!hasSetTime) {
            Toast.makeText(this, "Please select a due time", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if the selected date and time is in the past
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Due date and time cannot be in the past", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTask() {
        if (!validateInputs()) {
            return
        }

        btnSaveTask.isEnabled = false
        btnSaveTask.text = "Saving..."

        val title = editTextTaskTitle.text.toString().trim()
        val description = editTextTaskDescription.text.toString().trim()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dueDate = dateFormat.format(calendar.time)
        val dueTime = timeFormat.format(calendar.time)
        val priority = getSelectedPriority()
        val reminders = getEnabledReminders()

        val request = CreateTaskRequest(
            title = title,
            description = description.ifEmpty { null },
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            reminders = reminders
        )

        apiService.createTask(request).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                btnSaveTask.isEnabled = true
                btnSaveTask.text = "Save Task"

                if (response.isSuccessful) {
                    val taskResponse = response.body()
                    if (taskResponse != null) {
                        Toast.makeText(this@NewTaskActivity, "Task created successfully!", Toast.LENGTH_SHORT).show()

                        // Schedule notification
                        val reminders = getEnabledReminders()
                        if (reminders.isNotEmpty()) {
                            // Assuming only one reminder for now
                            val reminderTime = calendar.timeInMillis
                            NotificationScheduler.scheduleNotification(this@NewTaskActivity, reminderTime, taskResponse.id, "Task", taskResponse.title)
                        }

                        finish()
                    } else {
                        Toast.makeText(this@NewTaskActivity, "Failed to create task: Response body is null", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@NewTaskActivity, "Failed to create task: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                btnSaveTask.isEnabled = true
                btnSaveTask.text = "Save Task"
                Toast.makeText(this@NewTaskActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
