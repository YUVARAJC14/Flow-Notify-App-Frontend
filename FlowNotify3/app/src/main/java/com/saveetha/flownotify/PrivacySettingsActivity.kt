package com.saveetha.flownotify

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrivacySettingsActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var dataCollectionSwitch: SwitchCompat
    private lateinit var activityStatusSwitch: SwitchCompat
    private lateinit var personalizedAdsSwitch: SwitchCompat
    private lateinit var analyticsTrackingSwitch: SwitchCompat
    private lateinit var doNotTrackSwitch: SwitchCompat

    // Permission elements
    private lateinit var cameraStatusText: TextView
    private lateinit var storageStatusText: TextView
    private lateinit var calendarStatusText: TextView
    private lateinit var cameraPermissionButton: Button
    private lateinit var storagePermissionButton: Button
    private lateinit var calendarPermissionButton: Button

    // Profile visibility
    private lateinit var profileVisibilityText: TextView

    // Shared preferences to save privacy settings
    private lateinit var preferences: SharedPreferences

    // Permission request codes
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val STORAGE_PERMISSION_REQUEST = 101
        private const val CALENDAR_PERMISSION_REQUEST = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_settings)

        preferences = getSharedPreferences("privacy_settings", MODE_PRIVATE)

        initViews()
        loadSavedPreferences()
        setupListeners()
        updatePermissionStatuses()
    }

    private fun initViews() {
        // Data Privacy
        dataCollectionSwitch = findViewById(R.id.switch_data_collection)

        // Account Privacy
        activityStatusSwitch = findViewById(R.id.switch_activity_status)
        profileVisibilityText = findViewById(R.id.tv_profile_visibility)

        // Ads and Tracking
        personalizedAdsSwitch = findViewById(R.id.switch_personalized_ads)
        analyticsTrackingSwitch = findViewById(R.id.switch_analytics)
        doNotTrackSwitch = findViewById(R.id.switch_do_not_track)

        // Permissions
        cameraStatusText = findViewById(R.id.tv_camera_status)
        storageStatusText = findViewById(R.id.tv_storage_status)
        calendarStatusText = findViewById(R.id.tv_calendar_status)
        cameraPermissionButton = findViewById(R.id.btn_camera_permission)
        storagePermissionButton = findViewById(R.id.btn_storage_permission)
        calendarPermissionButton = findViewById(R.id.btn_calendar_permission)

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
        dataCollectionSwitch.isChecked = preferences.getBoolean("data_collection", true)
        activityStatusSwitch.isChecked = preferences.getBoolean("activity_status", true)
        personalizedAdsSwitch.isChecked = preferences.getBoolean("personalized_ads", true)
        analyticsTrackingSwitch.isChecked = preferences.getBoolean("analytics_tracking", true)
        doNotTrackSwitch.isChecked = preferences.getBoolean("do_not_track", false)

        // Load profile visibility setting
        profileVisibilityText.text = preferences.getString(
            "profile_visibility",
            "Connected Users Only"
        )
    }

    private fun setupListeners() {
        // Data Privacy
        findViewById<LinearLayout>(R.id.option_manage_personal_data).setOnClickListener {
            showManagePersonalDataDialog()
        }

        findViewById<LinearLayout>(R.id.option_export_data).setOnClickListener {
            showExportDataDialog()
        }

        // Permission buttons
        cameraPermissionButton.setOnClickListener {
            requestCameraPermission()
        }

        storagePermissionButton.setOnClickListener {
            requestStoragePermission()
        }

        calendarPermissionButton.setOnClickListener {
            requestCalendarPermission()
        }

        // Account Privacy
        findViewById<LinearLayout>(R.id.option_profile_visibility).setOnClickListener {
            showProfileVisibilityOptions()
        }

        findViewById<LinearLayout>(R.id.option_blocked_users).setOnClickListener {
            navigateToBlockedUsers()
        }
    }

    private fun updatePermissionStatuses() {
        // Check Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraStatusText.text = "Permission granted"
            cameraStatusText.setTextColor(ContextCompat.getColor(this, R.color.green))
            cameraPermissionButton.text = "Revoke"
        } else {
            cameraStatusText.text = "Required for profile photos and scanning documents"
            cameraStatusText.setTextColor(ContextCompat.getColor(this, R.color.gray))
            cameraPermissionButton.text = "Grant"
        }

        // Check Storage permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            storageStatusText.text = "Permission granted"
            storageStatusText.setTextColor(ContextCompat.getColor(this, R.color.green))
            storagePermissionButton.text = "Revoke"
        } else {
            storageStatusText.text = "Required for uploading and downloading files"
            storageStatusText.setTextColor(ContextCompat.getColor(this, R.color.gray))
            storagePermissionButton.text = "Grant"
        }

        // Check Calendar permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            calendarStatusText.text = "Permission granted"
            calendarStatusText.setTextColor(ContextCompat.getColor(this, R.color.green))
            calendarPermissionButton.text = "Revoke"
        } else {
            calendarStatusText.text = "Required for syncing events with your device calendar"
            calendarStatusText.setTextColor(ContextCompat.getColor(this, R.color.gray))
            calendarPermissionButton.text = "Grant"
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            // Permission is already granted, offer to revoke
            showPermissionRevokeDialog("Camera") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST
            )
        } else {
            // Permission is already granted, offer to revoke
            showPermissionRevokeDialog("Storage") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun requestCalendarPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                CALENDAR_PERMISSION_REQUEST
            )
        } else {
            // Permission is already granted, offer to revoke
            showPermissionRevokeDialog("Calendar") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun showPermissionRevokeDialog(permissionName: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Revoke $permissionName Permission")
            .setMessage("To revoke this permission, you'll need to go to the system settings. Would you like to do this now?")
            .setPositiveButton("Go to Settings") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
                updatePermissionStatuses()
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
                updatePermissionStatuses()
            }
            CALENDAR_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Calendar permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Calendar permission denied", Toast.LENGTH_SHORT).show()
                }
                updatePermissionStatuses()
            }
        }
    }

    private fun showProfileVisibilityOptions() {
        val options = arrayOf("Everyone", "Connected Users Only", "Nobody")
        var selectedOption = when (profileVisibilityText.text.toString()) {
            "Everyone" -> 0
            "Nobody" -> 2
            else -> 1
        }

        AlertDialog.Builder(this)
            .setTitle("Profile Visibility")
            .setSingleChoiceItems(options, selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("OK") { _, _ ->
                profileVisibilityText.text = options[selectedOption]
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showManagePersonalDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Manage Personal Data")
            .setItems(arrayOf("View Stored Data", "Delete All Data")) { _, which ->
                when (which) {
                    0 -> showViewDataDialog()
                    1 -> showDeleteDataConfirmation()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showViewDataDialog() {
        // In a real app, this would retrieve and display the user's data
        val dataBuilder = StringBuilder()
        dataBuilder.append("User: YUVARAJC14\n")
        dataBuilder.append("Email: yuvaraj@example.com\n")
        dataBuilder.append("Join Date: 2023-05-15\n")
        dataBuilder.append("Last Login: 2025-08-08\n")
        dataBuilder.append("Location: Thandalam, Chennai\n")
        dataBuilder.append("Company: Saveetha School of Engineering\n")

        AlertDialog.Builder(this)
            .setTitle("Your Data")
            .setMessage(dataBuilder.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to delete all your personal data? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // In a real app, this would actually delete user data from the server
                Toast.makeText(this, "Data deletion request submitted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExportDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Export Your Data")
            .setMessage("Your data will be compiled and sent to your email address. This process may take up to 24 hours. Would you like to proceed?")
            .setPositiveButton("Export") { _, _ ->
                // In a real app, this would trigger the data export process
                Toast.makeText(this, "Export request submitted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToBlockedUsers() {
        // In a real app, this would navigate to a list of blocked users
        Toast.makeText(this, "Blocked users list would open here", Toast.LENGTH_SHORT).show()
        // Intent(this, BlockedUsersActivity::class.java).also { startActivity(it) }
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

        // Data Privacy
        editor.putBoolean("data_collection", dataCollectionSwitch.isChecked)

        // Account Privacy
        editor.putBoolean("activity_status", activityStatusSwitch.isChecked)
        editor.putString("profile_visibility", profileVisibilityText.text.toString())

        // Ads and Tracking
        editor.putBoolean("personalized_ads", personalizedAdsSwitch.isChecked)
        editor.putBoolean("analytics_tracking", analyticsTrackingSwitch.isChecked)
        editor.putBoolean("do_not_track", doNotTrackSwitch.isChecked)

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
                        this@PrivacySettingsActivity,
                        "Privacy settings saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@PrivacySettingsActivity,
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
            .setMessage("Are you sure you want to exit without saving your privacy settings?")
            .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Update permission statuses when returning to this screen
        // (e.g., if user changed permissions in system settings)
        updatePermissionStatuses()
    }
}