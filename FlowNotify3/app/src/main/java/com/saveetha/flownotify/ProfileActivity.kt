package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.User
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var logoutButton: Button

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupListeners()

        fetchUserData()
    }

    private fun initViews() {
        fullNameTextView = findViewById(R.id.tv_full_name)
        emailTextView = findViewById(R.id.tv_email)
        profileImageView = findViewById(R.id.iv_profile_picture)
        themeSwitch = findViewById(R.id.switch_theme)
        logoutButton = findViewById(R.id.btn_log_out)
    }



    private fun setupListeners() {
        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            Log.d("ProfileActivity", "Edit Profile clicked")
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("ProfileActivity", "Theme switch toggled: $isChecked")
            applyTheme(isChecked)
        }

        findViewById<LinearLayout>(R.id.option_profile_information).setOnClickListener {
            Log.d("ProfileActivity", "Profile Information clicked")
            startActivity(Intent(this, ProfileInformationActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_password_security).setOnClickListener {
            Log.d("ProfileActivity", "Password & Security clicked")
            startActivity(Intent(this, PasswordSecurityActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_connected_accounts).setOnClickListener {
            Log.d("ProfileActivity", "Connected Accounts clicked")
            startActivity(Intent(this, ConnectedAccountsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_language).setOnClickListener {
            Log.d("ProfileActivity", "Language clicked")
            showLanguageOptions()
        }

        findViewById<LinearLayout>(R.id.option_notification_settings).setOnClickListener {
            Log.d("ProfileActivity", "Notification Settings clicked")
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_contact_support).setOnClickListener {
            Log.d("ProfileActivity", "Contact Support clicked")
            startActivity(Intent(this, SupportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_feedback).setOnClickListener {
            Log.d("ProfileActivity", "Feedback clicked")
            showFeedbackForm()
        }

        findViewById<LinearLayout>(R.id.option_privacy_policy).setOnClickListener {
            Log.d("ProfileActivity", "Privacy Policy clicked")
            openWebPage("https://www.flownotify.com/privacy-policy")
        }

        findViewById<LinearLayout>(R.id.option_terms_of_service).setOnClickListener {
            Log.d("ProfileActivity", "Terms of Service clicked")
            openWebPage("https://www.flownotify.com/terms-of-service")
        }

        logoutButton.setOnClickListener {
            Log.d("ProfileActivity", "Logout clicked")
            showLogoutConfirmation()
        }
    }

    private fun fetchUserData() {
        lifecycleScope.launch {
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let { updateUI(it) }
                } else {
                    updateUIWithFallbackData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateUIWithFallbackData()
            }
        }
    }

    private fun updateUI(user: User) {
        fullNameTextView.text = user.name
        emailTextView.text = user.email

        // Assuming profilePictureUrl is part of the User model, if not, it needs to be added
        // Glide.with(this).load(user.profilePictureUrl).into(profileImageView)
    }

    private fun updateUIWithFallbackData() {
        fullNameTextView.text = "YUVARAJC14"
        emailTextView.text = "yuvaraj@example.com"
        profileImageView.setImageResource(R.drawable.default_profile_image)
    }

    private fun showLanguageOptions() {
        // ... (This can be implemented similarly with a call to updateUserSettings)
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        lifecycleScope.launch {
            try {
                val theme = if (isDarkTheme) "dark" else "light"
                apiService.updateUserSettings(mapOf("theme" to theme))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", MODE_PRIVATE)
                val refreshToken = sharedPreferences.getString("refreshToken", null)
                if (refreshToken != null) {
                    apiService.logout(mapOf("refreshToken" to refreshToken))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showFeedbackForm() {
        startActivity(Intent(this, FeedbackActivity::class.java))
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchUserData()
    }
}