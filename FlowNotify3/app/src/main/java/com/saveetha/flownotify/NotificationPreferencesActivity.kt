package com.saveetha.flownotify

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationPreferencesActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var allNotificationsSwitch: SwitchCompat
    private lateinit var pushNotificationsSwitch: SwitchCompat
    private lateinit var emailNotificationsSwitch: SwitchCompat
    private lateinit var inAppNotificationsSwitch: SwitchCompat

    private lateinit var taskAssignmentsSwitch: SwitchCompat
    private lateinit var dueDateRemindersSwitch: SwitchCompat
    private lateinit var taskUpdatesSwitch: SwitchCompat

    private lateinit var eventInvitationsSwitch: SwitchCompat
    private lateinit var eventRemindersSwitch: SwitchCompat
    private lateinit var eventUpdatesSwitch: SwitchCompat

    private lateinit var appUpdatesSwitch: SwitchCompat
    private lateinit var securityAlertsSwitch: SwitchCompat
    private lateinit var tipsSwitch: SwitchCompat

    private lateinit var dndSwitch: SwitchCompat
    private lateinit var dndTimeLayout: LinearLayout
    private lateinit var dndStartTimeText: TextView
    private lateinit var dndEndTimeText: TextView

    private lateinit var dueDateReminderTimeText: TextView
    private lateinit var eventReminderTimeText: TextView

    // Shared preferences to save notification settings
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_preferences)

        preferences = getSharedPreferences("notification_preferences", MODE_PRIVATE)

        initViews()
        loadSavedPreferences()
        setupListeners()
    }

    private fun initViews() {
        // Master switch
        allNotificationsSwitch = findViewById(R.id.switch_all_notifications)

        // Notification methods
        pushNotificationsSwitch = findViewById(R.id.switch_push_notifications)
        emailNotificationsSwitch = findViewById(R.id.switch_email_notifications)
        inAppNotificationsSwitch = findViewById(R.id.switch_in_app_notifications)

        // Task notifications
        taskAssignmentsSwitch = findViewById(R.id.switch_task_assignments)
        dueDateRemindersSwitch = findViewById(R.id.switch_due_date_reminders)
        taskUpdatesSwitch = findViewById(R.id.switch_task_updates)
        dueDateReminderTimeText = findViewById(R.id.tv_due_date_reminder_time)

        // Calendar notifications
        eventInvitationsSwitch = findViewById(R.id.switch_event_invitations)
        eventRemindersSwitch = findViewById(R.id.switch_event_reminders)
        eventUpdatesSwitch = findViewById(R.id.switch_event_updates)
        eventReminderTimeText = findViewById(R.id.tv_event_reminder_time)

        // System notifications
        appUpdatesSwitch = findViewById(R.id.switch_app_updates)
        securityAlertsSwitch = findViewById(R.id.switch_security_alerts)
        tipsSwitch = findViewById(R.id.switch_tips)

        // Do Not Disturb
        dndSwitch = findViewById(R.id.switch_dnd)
        dndTimeLayout = findViewById(R.id.layout_dnd_time)
        dndStartTimeText = findViewById(R.id.tv_dnd_start_time)
        dndEndTimeText = findViewById(R.id.tv_dnd_end_time)

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

        pushNotificationsSwitch.isChecked = preferences.getBoolean("push_notifications", true)
        emailNotificationsSwitch.isChecked = preferences.getBoolean("email_notifications", true)
        inAppNotificationsSwitch.isChecked = preferences.getBoolean("in_app_notifications", true)

        taskAssignmentsSwitch.isChecked = preferences.getBoolean("task_assignments", true)
        dueDateRemindersSwitch.isChecked = preferences.getBoolean("due_date_reminders", true)
        taskUpdatesSwitch.isChecked = preferences.getBoolean("task_updates", true)

        eventInvitationsSwitch.isChecked = preferences.getBoolean("event_invitations", true)
        eventRemindersSwitch.isChecked = preferences.getBoolean("event_reminders", true)
        eventUpdatesSwitch.isChecked = preferences.getBoolean("event_updates", true)

        appUpdatesSwitch.isChecked = preferences.getBoolean("app_updates", true)
        securityAlertsSwitch.isChecked = preferences.getBoolean("security_alerts", true)
        tipsSwitch.isChecked = preferences.getBoolean("tips", false)

        dndSwitch.isChecked = preferences.getBoolean("dnd_enabled", false)
        dndStartTimeText.text = preferences.getString("dnd_start_time", "10:00 PM")
        dndEndTimeText.text = preferences.getString("dnd_end_time", "7:00 AM")

        dueDateReminderTimeText.text = preferences.getString("due_date_reminder_time", "1 day before due date")
        eventReminderTimeText.text = preferences.getString("event_reminder_time", "30 minutes before event")

        // Update UI based on loaded preferences
        updateMasterSwitchState()
        updateSwitchStates()
        updateDndVisibility()
    }

    private fun updateMasterSwitchState() {
        // Master switch should be on only if all the main notification methods are on
        val allOn = pushNotificationsSwitch.isChecked &&
                emailNotificationsSwitch.isChecked &&
                inAppNotificationsSwitch.isChecked

        // Update without triggering the listener
        allNotificationsSwitch.setOnCheckedChangeListener(null)
        allNotificationsSwitch.isChecked = allOn
        setupMasterSwitchListener()
    }

    private fun updateSwitchStates() {
        val notificationsEnabled = allNotificationsSwitch.isChecked

        // Disable all notification switches if master switch is off
        setEnabledForAllSwitches(notificationsEnabled)
    }

    private fun updateDndVisibility() {
        dndTimeLayout.visibility = if (dndSwitch.isChecked) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        // Master switch listener
        setupMasterSwitchListener()

        // Method switches
        val methodSwitches = listOf(pushNotificationsSwitch, emailNotificationsSwitch, inAppNotificationsSwitch)
        methodSwitches.forEach { switch ->
            switch.setOnCheckedChangeListener { _, _ ->
                updateMasterSwitchState()
            }
        }

        // DND switch
        dndSwitch.setOnCheckedChangeListener { _, isChecked ->
            dndTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // DND time edit button
        findViewById<Button>(R.id.btn_edit_dnd_time).setOnClickListener {
            showDndTimeEditDialog()
        }

        // Due date reminder time click listener
        findViewById<LinearLayout>(R.id.tv_due_date_reminder_time).setOnClickListener {
            showDueDateReminderOptions()
        }

        // Event reminder time click listener
        findViewById<LinearLayout>(R.id.tv_event_reminder_time).setOnClickListener {
            showEventReminderOptions()
        }
    }

    private fun setupMasterSwitchListener() {
        allNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            setEnabledForAllSwitches(isChecked)

            // Update the switches to match master state
            if (!isChecked) {
                // Turn off all notification methods
                pushNotificationsSwitch.isChecked = false
                emailNotificationsSwitch.isChecked = false
                inAppNotificationsSwitch.isChecked = false
            } else {
                // Turn on all notification methods
                pushNotificationsSwitch.isChecked = true
                emailNotificationsSwitch.isChecked = true
                inAppNotificationsSwitch.isChecked = true
            }
        }
    }

    private fun setEnabledForAllSwitches(enabled: Boolean) {
        // Notification methods
        setEnabledForSwitch(pushNotificationsSwitch, enabled)
        setEnabledForSwitch(emailNotificationsSwitch, enabled)
        setEnabledForSwitch(inAppNotificationsSwitch, enabled)

        // Task notifications
        setEnabledForSwitch(taskAssignmentsSwitch, enabled)
        setEnabledForSwitch(dueDateRemindersSwitch, enabled)
        setEnabledForSwitch(taskUpdatesSwitch, enabled)

        // Calendar notifications
        setEnabledForSwitch(eventInvitationsSwitch, enabled)
        setEnabledForSwitch(eventRemindersSwitch, enabled)
        setEnabledForSwitch(eventUpdatesSwitch, enabled)

        // System notifications
        setEnabledForSwitch(appUpdatesSwitch, enabled)
        setEnabledForSwitch(securityAlertsSwitch, enabled)
        setEnabledForSwitch(tipsSwitch, enabled)

        // Do not disturb is always enabled
    }

    private fun setEnabledForSwitch(switch: SwitchCompat, enabled: Boolean) {
        switch.isEnabled = enabled
        switch.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun showDndTimeEditDialog() {
        val startTimeText = dndStartTimeText.text.toString()
        val endTimeText = dndEndTimeText.text.toString()

        // Parse the current times
        val startTime = parseTimeString(startTimeText)
        val endTime = parseTimeString(endTimeText)

        // Create the dialog for the start time
        showTimePickerDialog(startTime[0], startTime[1]) { startHour, startMinute ->
            val newStartTime = formatTimeString(startHour, startMinute)
            dndStartTimeText.text = newStartTime

            // Then show dialog for end time
            showTimePickerDialog(endTime[0], endTime[1]) { endHour, endMinute ->
                val newEndTime = formatTimeString(endHour, endMinute)
                dndEndTimeText.text = newEndTime
            }
        }
    }

    private fun showTimePickerDialog(hour: Int, minute: Int, onTimeSelected: (Int, Int) -> Unit) {
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun parseTimeString(timeString: String): IntArray {
        val time = IntArray(2)
        try {
            val format = SimpleDateFormat("h:mm a", Locale.US)
            val date = format.parse(timeString)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                time[0] = calendar.get(Calendar.HOUR_OF_DAY)
                time[1] = calendar.get(Calendar.MINUTE)
            }
        } catch (e: Exception) {
            // Use default time if parsing fails
            time[0] = 22 // 10 PM
            time[1] = 0
        }
        return time
    }

    private fun formatTimeString(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val format = SimpleDateFormat("h:mm a", Locale.US)
        return format.format(calendar.time)
    }

    private fun showDueDateReminderOptions() {
        val options = arrayOf("1 hour before", "3 hours before", "1 day before", "2 days before", "1 week before")
        showOptionsDialog("Due Date Reminder", options, dueDateReminderTimeText)
    }

    private fun showEventReminderOptions() {
        val options = arrayOf("At time of event", "5 minutes before", "15 minutes before", "30 minutes before", "1 hour before")
        showOptionsDialog("Event Reminder", options, eventReminderTimeText)
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

        // Notification methods
        editor.putBoolean("push_notifications", pushNotificationsSwitch.isChecked)
        editor.putBoolean("email_notifications", emailNotificationsSwitch.isChecked)
        editor.putBoolean("in_app_notifications", inAppNotificationsSwitch.isChecked)

        // Task notifications
        editor.putBoolean("task_assignments", taskAssignmentsSwitch.isChecked)
        editor.putBoolean("due_date_reminders", dueDateRemindersSwitch.isChecked)
        editor.putBoolean("task_updates", taskUpdatesSwitch.isChecked)
        editor.putString("due_date_reminder_time", dueDateReminderTimeText.text.toString())

        // Calendar notifications
        editor.putBoolean("event_invitations", eventInvitationsSwitch.isChecked)
        editor.putBoolean("event_reminders", eventRemindersSwitch.isChecked)
        editor.putBoolean("event_updates", eventUpdatesSwitch.isChecked)
        editor.putString("event_reminder_time", eventReminderTimeText.text.toString())

        // System notifications
        editor.putBoolean("app_updates", appUpdatesSwitch.isChecked)
        editor.putBoolean("security_alerts", securityAlertsSwitch.isChecked)
        editor.putBoolean("tips", tipsSwitch.isChecked)

        // Do Not Disturb
        editor.putBoolean("dnd_enabled", dndSwitch.isChecked)
        editor.putString("dnd_start_time", dndStartTimeText.text.toString())
        editor.putString("dnd_end_time", dndEndTimeText.text.toString())

        // In a real app, you would also send these settings to the server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1500)

                // Apply settings
                editor.apply()

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@NotificationPreferencesActivity,
                        "Notification preferences saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@NotificationPreferencesActivity,
                        "Failed to save preferences",
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
            .setMessage("Are you sure you want to exit without saving your notification preferences?")
            .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}