package com.saveetha.flownotify

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PasswordSecurityActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var securityScoreProgress: ProgressBar
    private lateinit var passwordLastChangedText: TextView
    private lateinit var recoveryOptionsStatusText: TextView
    private lateinit var twoFactorStatusText: TextView
    private lateinit var authAppsStatusText: TextView
    private lateinit var backupCodesStatusText: TextView
    private lateinit var activeSessionsText: TextView
    private lateinit var recentLoginText: TextView
    private lateinit var biometricStatusText: TextView

    private lateinit var loginNotificationsSwitch: SwitchCompat
    private lateinit var appPasswordSwitch: SwitchCompat
    private lateinit var biometricSwitch: SwitchCompat

    private lateinit var securityStatusText: TextView

    // Preferences
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_security)

        preferences = getSharedPreferences("security_settings", MODE_PRIVATE)

        initViews()
        setupListeners()
        loadSecurityData()
    }

    private fun initViews() {
        // Security score
        securityScoreProgress = findViewById(R.id.security_score_progress)
        securityStatusText = findViewById(R.id.tv_security_status)

        // Password section
        passwordLastChangedText = findViewById(R.id.tv_password_last_changed)
        recoveryOptionsStatusText = findViewById(R.id.tv_recovery_options_status)

        // Two-factor authentication section
        twoFactorStatusText = findViewById(R.id.tv_two_factor_status)
        authAppsStatusText = findViewById(R.id.tv_auth_apps_status)
        backupCodesStatusText = findViewById(R.id.tv_backup_codes_status)

        // Login activity section
        activeSessionsText = findViewById(R.id.tv_active_sessions)
        recentLoginText = findViewById(R.id.tv_recent_login)

        // Advanced security section
        loginNotificationsSwitch = findViewById(R.id.switch_login_notifications)
        appPasswordSwitch = findViewById(R.id.switch_app_password)
        biometricSwitch = findViewById(R.id.switch_biometric)
        biometricStatusText = findViewById(R.id.tv_biometric_status)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // Password section
        findViewById<LinearLayout>(R.id.option_change_password).setOnClickListener {
            navigateToChangePassword()
        }

        findViewById<LinearLayout>(R.id.option_recovery_options).setOnClickListener {
            showRecoveryOptionsDialog()
        }

        // Two-factor authentication section
        findViewById<LinearLayout>(R.id.option_two_factor).setOnClickListener {
            navigateToTwoFactorSetup()
        }

        findViewById<LinearLayout>(R.id.option_auth_apps).setOnClickListener {
            navigateToAuthApps()
        }

        findViewById<LinearLayout>(R.id.option_backup_codes).setOnClickListener {
            showBackupCodesDialog()
        }

        // Login activity section
        findViewById<LinearLayout>(R.id.option_active_sessions).setOnClickListener {
            navigateToActiveSessions()
        }

        findViewById<LinearLayout>(R.id.option_recent_logins).setOnClickListener {
            navigateToRecentLogins()
        }

        findViewById<LinearLayout>(R.id.option_logout_all).setOnClickListener {
            showLogoutAllConfirmation()
        }

        // Advanced security switches
        loginNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("login_notifications", isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "Login notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Login notifications disabled", Toast.LENGTH_SHORT).show()
            }
            updateSecurityScore()
        }

        appPasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("app_password", isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "App password requirement enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "App password requirement disabled", Toast.LENGTH_SHORT).show()
            }
            updateSecurityScore()
        }

        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("biometric", isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "Biometric authentication enabled", Toast.LENGTH_SHORT).show()
                biometricStatusText.text = "Use fingerprint or face recognition to log in"
            } else {
                Toast.makeText(this, "Biometric authentication disabled", Toast.LENGTH_SHORT).show()
                biometricStatusText.text = "Not enabled"
            }
            updateSecurityScore()
        }
    }

    private fun loadSecurityData() {
        // In a real app, you would fetch this data from your backend
        // For this example, we'll simulate with hardcoded values

        // Set password last changed date
        val passwordChangeDate = calculateDateFromDaysAgo(90)
        passwordLastChangedText.text = "Last changed ${getDaysAgoText(90)}"
        passwordLastChangedText.setTextColor(ContextCompat.getColor(this, R.color.security_weak))

        // Set recovery options status
        recoveryOptionsStatusText.text = "Email and recovery phone set"

        // Set 2FA status
        val twoFactorEnabled = preferences.getBoolean("two_factor_enabled", false)
        if (twoFactorEnabled) {
            twoFactorStatusText.text = "Enabled"
            twoFactorStatusText.setTextColor(ContextCompat.getColor(this, R.color.security_strong))
        } else {
            twoFactorStatusText.text = "Not enabled (Recommended)"
            twoFactorStatusText.setTextColor(ContextCompat.getColor(this, R.color.security_weak))
        }

        // Set auth apps status
        val authAppsConfigured = preferences.getBoolean("auth_apps_configured", false)
        authAppsStatusText.text = if (authAppsConfigured) "Configured" else "No apps configured"

        // Set backup codes status
        val backupCodesGenerated = preferences.getBoolean("backup_codes_generated", false)
        backupCodesStatusText.text = if (backupCodesGenerated) "Generated" else "Not generated"

        // Set active sessions
        activeSessionsText.text = "3 devices currently logged in"

        // Set recent login with current date/time from parameters
        val currentDateTime = "2025-08-08 05:07:45"
        val formattedDateTime = formatDateTime(currentDateTime)
        recentLoginText.text = "Last login: $formattedDateTime"

        // Load switch states from preferences
        loginNotificationsSwitch.isChecked = preferences.getBoolean("login_notifications", true)
        appPasswordSwitch.isChecked = preferences.getBoolean("app_password", false)
        biometricSwitch.isChecked = preferences.getBoolean("biometric", true)

        // Update security score
        updateSecurityScore()
    }

    private fun updateSecurityScore() {
        var score = 0

        // Password age (0-25 points)
        val passwordAgeDays = 90
        score += when {
            passwordAgeDays < 30 -> 25
            passwordAgeDays < 60 -> 20
            passwordAgeDays < 90 -> 15
            passwordAgeDays < 180 -> 10
            else -> 5
        }

        // Two-factor authentication (0-25 points)
        if (preferences.getBoolean("two_factor_enabled", false)) {
            score += 25
        }

        // Recovery options (0-10 points)
        score += 10

        // Login notifications (0-10 points)
        if (loginNotificationsSwitch.isChecked) {
            score += 10
        }

        // App password (0-10 points)
        if (appPasswordSwitch.isChecked) {
            score += 10
        }

        // Biometric authentication (0-10 points)
        if (biometricSwitch.isChecked) {
            score += 10
        }

        // Auth apps and backup codes (0-10 points)
        if (preferences.getBoolean("auth_apps_configured", false)) {
            score += 5
        }
        if (preferences.getBoolean("backup_codes_generated", false)) {
            score += 5
        }

        // Update the progress bar
        securityScoreProgress.progress = score

        // Update security status text and icon based on score
        when {
            score >= 80 -> {
                securityStatusText.text = "Your account security is strong"
                securityStatusText.setTextColor(ContextCompat.getColor(this, R.color.security_strong))
                findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.iv_security_status).setColorFilter(
                    ContextCompat.getColor(this, R.color.security_strong)
                )
            }
            score >= 60 -> {
                securityStatusText.text = "Your account security needs attention"
                securityStatusText.setTextColor(ContextCompat.getColor(this, R.color.security_medium))
                findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.iv_security_status).setColorFilter(
                    ContextCompat.getColor(this, R.color.security_medium)
                )
            }
            else -> {
                securityStatusText.text = "Your account security is at risk"
                securityStatusText.setTextColor(ContextCompat.getColor(this, R.color.security_weak))
                findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.iv_security_status).setColorFilter(
                    ContextCompat.getColor(this, R.color.security_weak)
                )
            }
        }
    }

    private fun navigateToChangePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun showRecoveryOptionsDialog() {
        val options = arrayOf("Update Recovery Email", "Update Recovery Phone", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Recovery Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUpdateRecoveryEmailDialog()
                    1 -> showUpdateRecoveryPhoneDialog()
                }
            }
            .show()
    }

    private fun showUpdateRecoveryEmailDialog() {
        // In a real app, this would be a proper form to update the recovery email
        Toast.makeText(this, "Recovery Email update dialog would show here", Toast.LENGTH_SHORT).show()
    }

    private fun showUpdateRecoveryPhoneDialog() {
        // In a real app, this would be a proper form to update the recovery phone
        Toast.makeText(this, "Recovery Phone update dialog would show here", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTwoFactorSetup() {
        // In a real app, this would navigate to the 2FA setup screen
        Toast.makeText(this, "Two-Factor Authentication setup would start here", Toast.LENGTH_SHORT).show()

        // For demo purposes, we'll toggle the state
        val currentState = preferences.getBoolean("two_factor_enabled", false)
        preferences.edit().putBoolean("two_factor_enabled", !currentState).apply()

        // Reload security data to update UI
        loadSecurityData()
    }

    private fun navigateToAuthApps() {
        // In a real app, this would navigate to the auth apps setup screen
        Toast.makeText(this, "Authentication Apps setup would start here", Toast.LENGTH_SHORT).show()

        // For demo purposes, we'll toggle the state
        val currentState = preferences.getBoolean("auth_apps_configured", false)
        preferences.edit().putBoolean("auth_apps_configured", !currentState).apply()

        // Reload security data to update UI
        loadSecurityData()
    }

    private fun showBackupCodesDialog() {
        // In a real app, this would show/generate backup codes
        val backupCodesGenerated = preferences.getBoolean("backup_codes_generated", false)

        if (backupCodesGenerated) {
            AlertDialog.Builder(this)
                .setTitle("Backup Codes")
                .setMessage("Your backup codes are:\n\n1234-5678\n8765-4321\n9876-1234\n4321-9876\n5678-9012\n\nKeep these in a safe place. Each code can only be used once.")
                .setPositiveButton("Close", null)
                .setNeutralButton("Regenerate") { _, _ ->
                    Toast.makeText(this, "New backup codes would be generated here", Toast.LENGTH_SHORT).show()
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Generate Backup Codes")
                .setMessage("Backup codes let you sign in when you don't have access to your two-factor authentication device. Would you like to generate backup codes?")
                .setPositiveButton("Generate") { _, _ ->
                    preferences.edit().putBoolean("backup_codes_generated", true).apply()
                    loadSecurityData()
                    Toast.makeText(this, "Backup codes generated", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun navigateToActiveSessions() {
        // In a real app, this would navigate to the active sessions screen
        Toast.makeText(this, "Active Sessions view would open here", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRecentLogins() {
        // In a real app, this would navigate to the recent logins screen
        Toast.makeText(this, "Recent Logins view would open here", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutAllConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out All Other Devices")
            .setMessage("Are you sure you want to log out from all other devices? This action will terminate all sessions except for your current one.")
            .setPositiveButton("Log Out All") { _, _ ->
                Toast.makeText(this, "All other sessions have been terminated", Toast.LENGTH_SHORT).show()
                activeSessionsText.text = "1 device currently logged in (this device)"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun calculateDateFromDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    private fun getDaysAgoText(daysAgo: Int): String {
        return when {
            daysAgo == 0 -> "today"
            daysAgo == 1 -> "yesterday"
            daysAgo < 7 -> "$daysAgo days ago"
            daysAgo < 30 -> "${daysAgo / 7} weeks ago"
            daysAgo < 365 -> "${daysAgo / 30} months ago"
            else -> "${daysAgo / 365} years ago"
        }
    }

    private fun formatDateTime(dateTimeString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateTimeString) ?: return dateTimeString

            val outputFormat = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.US)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return dateTimeString
        }
    }
}