package com.saveetha.flownotify

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var enableAllSwitch: SwitchMaterial
    private lateinit var taskRemindersSwitch: SwitchMaterial
    private lateinit var eventAlertsSwitch: SwitchMaterial
    private lateinit var productivityUpdatesSwitch: SwitchMaterial
    private lateinit var dailySummariesSwitch: SwitchMaterial
    private lateinit var smartTimingSwitch: SwitchMaterial
    private lateinit var startTimeText: TextView
    private lateinit var endTimeText: TextView
    private lateinit var soundSpinner: Spinner

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preferences = getSharedPreferences("notification_settings", MODE_PRIVATE)

        initViews()
        loadSavedPreferences()
        setupListeners()
    }

    private fun initViews() {
        enableAllSwitch = findViewById(R.id.switch_enable_all)
        taskRemindersSwitch = findViewById(R.id.switch_task_reminders)
        eventAlertsSwitch = findViewById(R.id.switch_event_alerts)
        productivityUpdatesSwitch = findViewById(R.id.switch_productivity_updates)
        dailySummariesSwitch = findViewById(R.id.switch_daily_summaries)
        smartTimingSwitch = findViewById(R.id.switch_smart_timing)
        startTimeText = findViewById(R.id.tv_start_time)
        endTimeText = findViewById(R.id.tv_end_time)
        soundSpinner = findViewById(R.id.spinner_notification_sound)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { onBackPressed() }

        val soundAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.notification_sounds,
            android.R.layout.simple_spinner_item
        )
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        soundSpinner.adapter = soundAdapter
    }

    private fun loadSavedPreferences() {
        enableAllSwitch.isChecked = preferences.getBoolean("enable_all", true)
        taskRemindersSwitch.isChecked = preferences.getBoolean("task_reminders", true)
        eventAlertsSwitch.isChecked = preferences.getBoolean("event_alerts", true)
        productivityUpdatesSwitch.isChecked = preferences.getBoolean("productivity_updates", true)
        dailySummariesSwitch.isChecked = preferences.getBoolean("daily_summaries", true)
        smartTimingSwitch.isChecked = preferences.getBoolean("smart_timing", true)
        startTimeText.text = preferences.getString("quiet_start_time", "10:00 PM")
        endTimeText.text = preferences.getString("quiet_end_time", "7:00 AM")
        
        val sounds = resources.getStringArray(R.array.notification_sounds)
        val sound = preferences.getString("notification_sound", "Default")
        soundSpinner.setSelection(sounds.indexOf(sound))
    }

    private fun setupListeners() {
        enableAllSwitch.setOnCheckedChangeListener { _, isChecked ->
            taskRemindersSwitch.isChecked = isChecked
            eventAlertsSwitch.isChecked = isChecked
            productivityUpdatesSwitch.isChecked = isChecked
            dailySummariesSwitch.isChecked = isChecked
        }

        startTimeText.setOnClickListener { showTimePickerDialog(startTimeText) }
        endTimeText.setOnClickListener { showTimePickerDialog(endTimeText) }
    }

    private fun showTimePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            val format = SimpleDateFormat("h:mm a", Locale.US)
            textView.text = format.format(selectedCalendar.time)
        }, hour, minute, false).show()
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
    }

    private fun savePreferences() {
        val editor = preferences.edit()
        editor.putBoolean("enable_all", enableAllSwitch.isChecked)
        editor.putBoolean("task_reminders", taskRemindersSwitch.isChecked)
        editor.putBoolean("event_alerts", eventAlertsSwitch.isChecked)
        editor.putBoolean("productivity_updates", productivityUpdatesSwitch.isChecked)
        editor.putBoolean("daily_summaries", dailySummariesSwitch.isChecked)
        editor.putBoolean("smart_timing", smartTimingSwitch.isChecked)
        editor.putString("quiet_start_time", startTimeText.text.toString())
        editor.putString("quiet_end_time", endTimeText.text.toString())
        editor.putString("notification_sound", soundSpinner.selectedItem.toString())
        editor.apply()
    }
}
