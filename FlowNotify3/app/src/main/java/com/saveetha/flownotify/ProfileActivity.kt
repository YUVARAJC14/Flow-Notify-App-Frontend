package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    // User data
    private var userName: String = "YUVARAJC14"
    private var userEmail: String = ""
    private var userLocation: String = ""
    private var profileImageUrl: String = ""

    // UI elements
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupBottomNavigation()
        setupListeners()

        // Fetch user data
        fetchUserData()
    }

    private fun initViews() {
        fullNameTextView = findViewById(R.id.tv_full_name)
        emailTextView = findViewById(R.id.tv_email)
        profileImageView = findViewById(R.id.iv_profile_picture)
        themeSwitch = findViewById(R.id.switch_theme)
        logoutButton = findViewById(R.id.btn_log_out)

        // Set initial app version
   }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_profile

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
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true // Already on Profile
                else -> false
            }
        }
    }

    private fun setupListeners() {
        // Edit Profile
        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            // Navigate to edit profile screen
            val intent = Intent(this, EditProfileActivity::class.java).apply {
                putExtra("userName", userName)
                putExtra("userEmail", userEmail)
                putExtra("userLocation", userLocation)
                putExtra("profileImageUrl", profileImageUrl)
            }
            startActivity(intent)
        }

        // Theme Switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Toggle app theme (light/dark)
            applyTheme(isChecked)
        }

        // Settings Options
        findViewById<LinearLayout>(R.id.option_profile_information).setOnClickListener {
            startActivity(Intent(this, ProfileInformationActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_password_security).setOnClickListener {
            startActivity(Intent(this, PasswordSecurityActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_connected_accounts).setOnClickListener {
            startActivity(Intent(this, ConnectedAccountsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_language).setOnClickListener {
            showLanguageOptions()
        }

        findViewById<LinearLayout>(R.id.option_notification_settings).setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_contact_support).setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_feedback).setOnClickListener {
            showFeedbackForm()
        }

        findViewById<LinearLayout>(R.id.option_privacy_policy).setOnClickListener {
            openWebPage("https://www.flownotify.com/privacy-policy")
        }

        findViewById<LinearLayout>(R.id.option_terms_of_service).setOnClickListener {
            openWebPage("https://www.flownotify.com/terms-of-service")
        }

        // Logout
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun fetchUserData() {
        // In a real app, this would come from your authentication system
        // For this example, we'll use the GitHub API to get some basic user data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the logged-in GitHub user from the function call
                userName = "YUVARAJC14" // This should be fetched from your auth system

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.github.com/users/$userName")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()

                if (response.isSuccessful && jsonData != null) {
                    val json = JSONObject(jsonData)

                    // Extract user information
                    val name = json.optString("name", userName)
                    val email = json.optString("email", "$userName@example.com")
                    val avatarUrl = json.optString("avatar_url", "")
                    val location = json.optString("location", "")

                    // Update class variables
                    this@ProfileActivity.userName = if (name.isNotEmpty()) name else userName
                    this@ProfileActivity.userEmail = email
                    this@ProfileActivity.userLocation = location
                    this@ProfileActivity.profileImageUrl = avatarUrl

                    withContext(Dispatchers.Main) {
                        updateUI()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                // Use fallback data
                withContext(Dispatchers.Main) {
                    updateUIWithFallbackData()
                }
            }
        }
    }

    private fun updateUI() {
        // Set user name and email
        fullNameTextView.text = userName
        emailTextView.text = userEmail

        // Load profile image
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(profileImageView)
        }
    }

    private fun updateUIWithFallbackData() {
        fullNameTextView.text = "YUVARAJC14"
        emailTextView.text = "yuvaraj@example.com"

        // Use default profile image
        profileImageView.setImageResource(R.drawable.default_profile_image)
    }

    private fun showLanguageOptions() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Chinese", "Japanese")
        var selectedLanguage = 0 // English by default

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedLanguage) { _, which ->
                selectedLanguage = which
            }
            .setPositiveButton("OK") { _, _ ->
                // Save selected language
                val language = languages[selectedLanguage]
                findViewById<TextView>(R.id.tv_language_value).text = language
                // In a real app, you would also update the app's locale
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFeedbackForm() {
        // Navigate to feedback form
        // You could also show a dialog here
        startActivity(Intent(this, FeedbackActivity::class.java))
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        // In a real app, you would apply the theme change here
        // For example:
        // AppCompatDelegate.setDefaultNightMode(
        //     if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        // )
    }

    private fun openWebPage(url: String) {
        // Open web page in browser
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                // Perform logout
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear authentication tokens and user data
        val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this screen
        fetchUserData()
    }
}