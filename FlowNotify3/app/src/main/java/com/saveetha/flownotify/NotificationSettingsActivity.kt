package com.saveetha.flownotify

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationSettingsActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var allNotificationsSwitch: SwitchCompat
    private lateinit var chatNotificationsSwitch: SwitchCompat
    private lateinit var taskNotificationsSwitch: SwitchCompat
    private lateinit var calendarNotificationsSwitch: SwitchCompat
    private lateinit var teamNotificationsSwitch: SwitchCompat
    private lateinit var systemNotificationsSwitch: SwitchCompat

    private lateinit var slackNotificationsSwitch: SwitchCompat
    private lateinit var trelloNotificationsSwitch: SwitchCompat
    private lateinit var emailNotificationsSwitch: SwitchCompat

    private lateinit var doNotDisturbSwitch: SwitchCompat
    private lateinit var dndStatusText: TextView
    private lateinit var dndScheduleText: TextView
    private lateinit var dndScheduleLayout: LinearLayout
    private lateinit var dndScheduleDivider: View
    private lateinit var eventRemindersText: TextView
    private lateinit var taskDeadlinesText: TextView

    private lateinit var notificationSoundText: TextView
    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var ledLightSwitch: SwitchCompat
    private lateinit var hideContentSwitch: SwitchCompat

    // Shared preferences to save notification settings
    private lateinit var preferences: SharedPreferences

    // Default values
    private val defaultDndStart = "10:00 PM"
    private val defaultDndEnd = "7:00 AM"
    private val defaultEventReminder = "15 minutes before"
    private val defaultTaskDeadline = "1 day before"
    private val defaultNotificationSound = "Default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        preferences = getSharedPreferences("notification_settings", MODE_PRIVATE)

        initViews()
        loadSavedPreferences()
        setupListeners()
    }

    private fun initViews() {
        // Master switch
        allNotificationsSwitch = findViewById(R.id.switch_all_notifications)

        // Notification channels
        chatNotificationsSwitch = findViewById(R.id.switch_chat_notifications)
        taskNotificationsSwitch = findViewById(R.id.switch_task_notifications)
        calendarNotificationsSwitch = findViewById(R.id.switch_calendar_notifications)
        teamNotificationsSwitch = findViewById(R.id.switch_team_notifications)
        systemNotificationsSwitch = findViewById(R.id.switch_system_notifications)

        // External applications
        slackNotificationsSwitch = findViewById(R.id.switch_slack_notifications)
        trelloNotificationsSwitch = findViewById(R.id.switch_trello_notifications)
        emailNotificationsSwitch = findViewById(R.id.switch_email_notifications)

        // Notification timing
        doNotDisturbSwitch = findViewById(R.id.switch_do_not_disturb)
        dndStatusText = findViewById(R.id.tv_dnd_status)
        dndScheduleText = findViewById(R.id.tv_dnd_schedule)
        dndScheduleLayout = findViewById(R.id.layout_dnd_schedule)
        dndScheduleDivider = findViewById(R.id.divider_dnd_schedule)
        eventRemindersText = findViewById(R.id.tv_event_reminders)
        taskDeadlinesText = findViewById(R.id.tv_task_deadlines)

        // Preferences
        notificationSoundText = findViewById(R.id.tv_notification_sound)
        vibrationSwitch = findViewById(R.id.switch_vibration)
        ledLightSwitch = findViewById(R.id.switch_led_light)
        hideContentSwitch = findViewById(R.id.switch_hide_content)

        // Toolbar actions
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            savePreferences()
        }
    }

    private fun loadSavedPreferences() {
        // Load preferences with defaults
        allNotificationsSwitch.isChecked = preferences.getBoolean("all_notifications", true)

        chatNotificationsSwitch.isChecked = preferences.getBoolean("chat_notifications", true)
        taskNotificationsSwitch.isChecked = preferences.getBoolean("task_notifications", true)
        calendarNotificationsSwitch.isChecked = preferences.getBoolean("calendar_notifications", true)
        teamNotificationsSwitch.isChecked = preferences.getBoolean("team_notifications", true)
        systemNotificationsSwitch.isChecked = preferences.getBoolean("system_notifications", true)

        slackNotificationsSwitch.isChecked = preferences.getBoolean("slack_notifications", true)
        trelloNotificationsSwitch.isChecked = preferences.getBoolean("trello_notifications", false)
        emailNotificationsSwitch.isChecked = preferences.getBoolean("email_notifications", true)

        doNotDisturbSwitch.isChecked = preferences.getBoolean("do_not_disturb", false)
        dndScheduleText.text = preferences.getString("dnd_schedule", "$defaultDndStart - $defaultDndEnd")
        eventRemindersText.text = preferences.getString("event_reminders", defaultEventReminder)
        taskDeadlinesText.text = preferences.getString("task_deadlines", defaultTaskDeadline)

        notificationSoundText.text = preferences.getString("notification_sound", defaultNotificationSound)
        vibrationSwitch.isChecked = preferences.getBoolean("vibration", true)
        ledLightSwitch.isChecked = preferences.getBoolean("led_light", true)
        hideContentSwitch.isChecked = preferences.getBoolean("hide_content", false)

        // Update UI based on loaded preferences
        updateMasterSwitchState()
        updateDndVisibility()
    }

    private fun updateMasterSwitchState() {
        // Master switch should be on only if all the main notification channels are on
        val allChannelsOn = chatNotificationsSwitch.isChecked &&
                taskNotificationsSwitch.isChecked &&
                calendarNotificationsSwitch.isChecked &&
                teamNotificationsSwitch.isChecked &&
                systemNotificationsSwitch.isChecked

        // Update without triggering the listener
        allNotificationsSwitch.setOnCheckedChangeListener(null)
        allNotificationsSwitch.isChecked = allChannelsOn
        setupMasterSwitchListener()
    }

    private fun updateDndVisibility() {
        if (doNotDisturbSwitch.isChecked) {
            dndStatusText.text = dndScheduleText.text
            dndScheduleLayout.visibility = View.VISIBLE
            dndScheduleDivider.visibility = View.VISIBLE
        } else {
            dndStatusText.text = "Off"
            dndScheduleLayout.visibility = View.GONE
            dndScheduleDivider.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // Master switch listener
        setupMasterSwitchListener()

        // Notification channel switches
        val channelSwitches = listOf(
            chatNotificationsSwitch,
            taskNotificationsSwitch,
            calendarNotificationsSwitch,
            teamNotificationsSwitch,
            systemNotificationsSwitch
        )

        channelSwitches.forEach { switch ->
            switch.setOnCheckedChangeListener { _, _ ->
                updateMasterSwitchState()
            }
        }

        // Do Not Disturb switch
        doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateDndVisibility()
        }

        // DND Schedule click
        dndScheduleLayout.setOnClickListener {
            showDndScheduleDialog()
        }

        // Event reminders click
        (findViewById<LinearLayout>(R.id.tv_event_reminders).parent.parent as View).setOnClickListener {
            showEventReminderOptions()
        }

        // Task deadlines click
        (findViewById<LinearLayout>(R.id.tv_task_deadlines).parent.parent as View).setOnClickListener {
            showTaskDeadlineOptions()
        }

        // Notification sound click
        findViewById<LinearLayout>(R.id.option_notification_sound).setOnClickListener {
            showNotificationSoundOptions()
        }
    }

    private fun setupMasterSwitchListener() {
        allNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            setEnabledForAllSwitches(isChecked)

            // Update the switches to match master state
            if (!isChecked) {
                // Turn off all notification channels
                chatNotificationsSwitch.isChecked = false
                taskNotificationsSwitch.isChecked = false
                calendarNotificationsSwitch.isChecked = false
                teamNotificationsSwitch.isChecked = false
                systemNotificationsSwitch.isChecked = false

                // Turn off all external app integrations
                slackNotificationsSwitch.isChecked = false
                trelloNotificationsSwitch.isChecked = false
                emailNotificationsSwitch.isChecked = false
            } else {
                // Turn on all notification channels
                chatNotificationsSwitch.isChecked = true
                taskNotificationsSwitch.isChecked = true
                calendarNotificationsSwitch.isChecked = true
                teamNotificationsSwitch.isChecked = true
                systemNotificationsSwitch.isChecked = true

                // Don't automatically turn on external apps, user may want selective control
            }
        }
    }

    private fun setEnabledForAllSwitches(enabled: Boolean) {
        // Notification channels
        setEnabledForSwitch(chatNotificationsSwitch, enabled)
        setEnabledForSwitch(taskNotificationsSwitch, enabled)
        setEnabledForSwitch(calendarNotificationsSwitch, enabled)
        setEnabledForSwitch(teamNotificationsSwitch, enabled)
        setEnabledForSwitch(systemNotificationsSwitch, enabled)

        // External applications
        setEnabledForSwitch(slackNotificationsSwitch, enabled)
        setEnabledForSwitch(trelloNotificationsSwitch, enabled)
        setEnabledForSwitch(emailNotificationsSwitch, enabled)
    }

    private fun setEnabledForSwitch(switch: SwitchCompat, enabled: Boolean) {
        switch.isEnabled = enabled
        switch.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun showDndScheduleDialog() {
        val currentSchedule = dndScheduleText.text.toString()
        val parts = currentSchedule.split(" - ")
        val startTime = if (parts.size > 0) parts[0] else defaultDndStart
        val endTime = if (parts.size > 1) parts[1] else defaultDndEnd

        // First show dialog for start time
        showTimePickerDialog(startTime) { newStartTime ->
            // Then show dialog for end time
            showTimePickerDialog(endTime) { newEndTime ->
                val newSchedule = "$newStartTime - $newEndTime"
                dndScheduleText.text = newSchedule
                dndStatusText.text = newSchedule
            }
        }
    }

    private fun showTimePickerDialog(timeString: String, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // Parse the time string (e.g., "10:00 PM")
        try {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
            val date = timeFormat.parse(timeString)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val formattedTime = timeFormat.format(calendar.time)
                onTimeSelected(formattedTime)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun showEventReminderOptions() {
        val options = arrayOf(
            "At time of event",
            "5 minutes before",
            "15 minutes before",
            "30 minutes before",
            "1 hour before",
            "2 hours before",
            "1 day before"
        )

        showOptionsDialog("Event Reminders", options, eventRemindersText)
    }

    private fun showTaskDeadlineOptions() {
        val options = arrayOf(
            "At deadline",
            "1 hour before",
            "3 hours before",
            "1 day before",
            "2 days before",
            "1 week before"
        )

        showOptionsDialog("Task Deadlines", options, taskDeadlinesText)
    }

    private fun showNotificationSoundOptions() {
        val options = arrayOf(
            "Default",
            "None",
            "Beep",
            "Chime",
            "Ding",
            "Ping",
            "Tone"
        )

        showOptionsDialog("Notification Sound", options, notificationSoundText)
    }

    private fun showOptionsDialog(title: String, options: Array<String>, textView: TextView) {
        val currentValue = textView.text.toString()
        var selectedIndex = options.indexOf(currentValue)
        if (selectedIndex < 0) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("OK") { _, _ ->
                textView.text = options[selectedIndex]
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePreferences() {
        // Show loading
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        loadingDialog.show()

        // Save preferences
        val editor = preferences.edit()

        // Master switch
        editor.putBoolean("all_notifications", allNotificationsSwitch.isChecked)

        // Notification channels
        editor.putBoolean("chat_notifications", chatNotificationsSwitch.isChecked)
        editor.putBoolean("task_notifications", taskNotificationsSwitch.isChecked)
        editor.putBoolean("calendar_notifications", calendarNotificationsSwitch.isChecked)
        editor.putBoolean("team_notifications", teamNotificationsSwitch.isChecked)
        editor.putBoolean("system_notifications", systemNotificationsSwitch.isChecked)

        // External applications
        editor.putBoolean("slack_notifications", slackNotificationsSwitch.isChecked)
        editor.putBoolean("trello_notifications", trelloNotificationsSwitch.isChecked)
        editor.putBoolean("email_notifications", emailNotificationsSwitch.isChecked)

        // Notification timing
        editor.putBoolean("do_not_disturb", doNotDisturbSwitch.isChecked)
        editor.putString("dnd_schedule", dndScheduleText.text.toString())
        editor.putString("event_reminders", eventRemindersText.text.toString())
        editor.putString("task_deadlines", taskDeadlinesText.text.toString())

        // Preferences
        editor.putString("notification_sound", notificationSoundText.text.toString())
        editor.putBoolean("vibration", vibrationSwitch.isChecked)
        editor.putBoolean("led_light", ledLightSwitch.isChecked)
        editor.putBoolean("hide_content", hideContentSwitch.isChecked)

        // In a real app, you would also send these settings to the server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1000)

                // Apply settings
                editor.apply()

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@NotificationSettingsActivity,
                        "Notification settings saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@NotificationSettingsActivity,
                        "Failed to save settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Show confirmation dialog if changes were made
        // For simplicity, we're just showing a basic confirmation
        AlertDialog.Builder(this)
            .setTitle("Discard Changes")
            .setMessage("Are you sure you want to exit without saving your notification settings?")
            .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}