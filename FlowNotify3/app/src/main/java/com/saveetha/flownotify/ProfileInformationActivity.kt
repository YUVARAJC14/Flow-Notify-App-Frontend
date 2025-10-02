package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.User
import kotlinx.coroutines.launch

class ProfileInformationActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var profilePictureImageView: ShapeableImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView

    private lateinit var emailTextView: TextView
    private lateinit var emailVerifiedTextView: TextView

    private lateinit var editProfileButton: MaterialButton

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_profile_information)

        initViews()
        setupListeners()
        fetchUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        // Profile preview section
        profilePictureImageView = findViewById(R.id.iv_profile_picture)
        fullNameTextView = findViewById(R.id.tv_full_name)
        usernameTextView = findViewById(R.id.tv_username)
        bioTextView = findViewById(R.id.tv_bio)

        // Basic information section
        emailTextView = findViewById(R.id.tv_email)
        emailVerifiedTextView = findViewById(R.id.tv_email_verified)

        // Edit profile button
        editProfileButton = findViewById(R.id.btn_edit_profile)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // Edit profile button
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            // TODO: Pass user data to EditProfileActivity
            startActivity(intent)
        }
    }

    private fun fetchUserData() {
        lifecycleScope.launch {
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let { updateUIWithUserData(it) }
                } else {
                    Toast.makeText(
                        this@ProfileInformationActivity,
                        "Failed to load profile information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ProfileInformationActivity,
                    "Failed to load profile information",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        // Profile preview section
        fullNameTextView.text = user.name
        usernameTextView.text = "@${user.email.substringBefore('@')}"
        bioTextView.text = user.bio ?: ""

        // Load profile image
        if (!user.profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(profilePictureImageView)
        } else {
            profilePictureImageView.setImageResource(R.drawable.default_profile_image)
        }

        // Basic information section
        emailTextView.text = user.email
        // The API doesn't provide email verification status, so we hide it for now.
        emailVerifiedTextView.visibility = View.GONE
    }
}