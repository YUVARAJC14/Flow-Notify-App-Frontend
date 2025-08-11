package com.saveetha.flownotify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileInformationActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var profilePictureImageView: ShapeableImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var lastUpdatedTextView: TextView

    private lateinit var emailTextView: TextView
    private lateinit var emailVerifiedTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var dobTextView: TextView
    private lateinit var genderTextView: TextView

    private lateinit var locationTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var websiteTextView: TextView

    private lateinit var companyTextView: TextView
    private lateinit var positionTextView: TextView
    private lateinit var educationTextView: TextView

    private lateinit var linkedinTextView: TextView
    private lateinit var githubTextView: TextView
    private lateinit var twitterTextView: TextView

    private lateinit var editProfileButton: MaterialButton

    // User data
    private var userData = UserProfileData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_information)

        initViews()
        setupListeners()
        fetchUserData()
    }

    private fun initViews() {
        // Profile preview section
        profilePictureImageView = findViewById(R.id.iv_profile_picture)
        fullNameTextView = findViewById(R.id.tv_full_name)
        usernameTextView = findViewById(R.id.tv_username)
        bioTextView = findViewById(R.id.tv_bio)
        lastUpdatedTextView = findViewById(R.id.tv_last_updated)

        // Basic information section
        emailTextView = findViewById(R.id.tv_email)
        emailVerifiedTextView = findViewById(R.id.tv_email_verified)
        phoneTextView = findViewById(R.id.tv_phone)
        dobTextView = findViewById(R.id.tv_dob)
        genderTextView = findViewById(R.id.tv_gender)

        // Contact information section
        locationTextView = findViewById(R.id.tv_location)
        addressTextView = findViewById(R.id.tv_address)
        websiteTextView = findViewById(R.id.tv_website)

        // Professional information section
        companyTextView = findViewById(R.id.tv_company)
        positionTextView = findViewById(R.id.tv_position)
        educationTextView = findViewById(R.id.tv_education)

        // Social media section
        linkedinTextView = findViewById(R.id.tv_linkedin)
        githubTextView = findViewById(R.id.tv_github)
        twitterTextView = findViewById(R.id.tv_twitter)

        // Edit profile button
        editProfileButton = findViewById(R.id.btn_edit_profile)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // Copy buttons for social media
        findViewById<ImageView>(R.id.btn_copy_linkedin).setOnClickListener {
            copyToClipboard("LinkedIn", linkedinTextView.text.toString())
        }

        findViewById<ImageView>(R.id.btn_copy_github).setOnClickListener {
            copyToClipboard("GitHub", githubTextView.text.toString())
        }

        findViewById<ImageView>(R.id.btn_copy_twitter).setOnClickListener {
            copyToClipboard("Twitter", twitterTextView.text.toString())
        }

        // Website click
        websiteTextView.setOnClickListener {
            val website = websiteTextView.text.toString()
            if (website.isNotEmpty()) {
                openWebsite(website)
            }
        }

        // Social media clicks
        linkedinTextView.setOnClickListener {
            val linkedin = linkedinTextView.text.toString()
            if (linkedin.isNotEmpty()) {
                openWebsite(formatSocialUrl("https://www.linkedin.com/in/", linkedin))
            }
        }

        githubTextView.setOnClickListener {
            val github = githubTextView.text.toString()
            if (github.isNotEmpty()) {
                openWebsite(formatSocialUrl("https://github.com/", github))
            }
        }

        twitterTextView.setOnClickListener {
            val twitter = twitterTextView.text.toString()
            if (twitter.isNotEmpty()) {
                openWebsite(formatSocialUrl("https://twitter.com/", twitter))
            }
        }

        // Edit profile button
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userName", userData.fullName)
            intent.putExtra("userEmail", userData.email)
            intent.putExtra("userLocation", userData.location)
            intent.putExtra("userCompany", userData.company)
            intent.putExtra("userBio", userData.bio)
            intent.putExtra("profileImageUrl", userData.profileImageUrl)
            intent.putExtra("userPhone", userData.phone)
            intent.putExtra("userDob", userData.dob)
            intent.putExtra("userGender", userData.gender)
            intent.putExtra("userAddress", userData.address)
            intent.putExtra("userWebsite", userData.website)
            intent.putExtra("userPosition", userData.position)
            intent.putExtra("userEducation", userData.education)
            intent.putExtra("userLinkedin", userData.linkedin)
            intent.putExtra("userGithub", userData.github)
            intent.putExtra("userTwitter", userData.twitter)
            startActivity(intent)
        }
    }

    private fun fetchUserData() {
        // In a real app, this would fetch user data from your backend
        // For this example, we'll use hardcoded data based on the YUVARAJC14 user

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // This is where you would make your API call
                // For this example, we're just simulating with a delay
                kotlinx.coroutines.delay(500)

                // Create mock user data
                userData = UserProfileData(
                    fullName = "YUVARAJC14",
                    username = "yuvarajc14",
                    bio = "Student at Saveetha School of Engineering",
                    email = "yuvaraj@example.com",
                    emailVerified = true,
                    phone = "+91 9876543210",
                    dob = "June 14, 2000",
                    gender = "Male",
                    location = "Thandalam, Chennai, Tamil Nadu, India",
                    address = "123 Main Street, Thandalam, Chennai - 602105",
                    website = "https://yuvarajc14.github.io",
                    company = "Saveetha School of Engineering",
                    position = "Student",
                    education = "Bachelor of Technology in Computer Science",
                    linkedin = "linkedin.com/in/yuvarajc14",
                    github = "github.com/YUVARAJC14",
                    twitter = "twitter.com/yuvarajc14",
                    lastUpdated = "2025-08-08",
                    profileImageUrl = ""
                )

                withContext(Dispatchers.Main) {
                    updateUIWithUserData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileInformationActivity,
                        "Failed to load profile information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUIWithUserData() {
        // Profile preview section
        fullNameTextView.text = userData.fullName
        usernameTextView.text = "@${userData.username}"
        bioTextView.text = userData.bio
        lastUpdatedTextView.text = "Last updated: ${userData.lastUpdated}"

        // Load profile image
        if (userData.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(userData.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(profilePictureImageView)
        } else {
            profilePictureImageView.setImageResource(R.drawable.default_profile_image)
        }

        // Basic information section
        emailTextView.text = userData.email
        emailVerifiedTextView.visibility = if (userData.emailVerified) View.VISIBLE else View.GONE
        phoneTextView.text = userData.phone
        dobTextView.text = userData.dob
        genderTextView.text = userData.gender

        // Contact information section
        locationTextView.text = userData.location
        addressTextView.text = userData.address
        websiteTextView.text = userData.website

        // Professional information section
        companyTextView.text = userData.company
        positionTextView.text = userData.position
        educationTextView.text = userData.education

        // Social media section
        linkedinTextView.text = userData.linkedin
        githubTextView.text = userData.github
        twitterTextView.text = userData.twitter
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun openWebsite(url: String) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open website", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatSocialUrl(baseUrl: String, username: String): String {
        // Remove domain part if present
        val cleanUsername = if (username.contains("/")) {
            username.substringAfterLast("/")
        } else if (username.contains(".com/")) {
            username.substringAfterLast(".com/")
        } else {
            username
        }

        return baseUrl + cleanUsername
    }

    data class UserProfileData(
        var fullName: String = "",
        var username: String = "",
        var bio: String = "",
        var email: String = "",
        var emailVerified: Boolean = false,
        var phone: String = "",
        var dob: String = "",
        var gender: String = "",
        var location: String = "",
        var address: String = "",
        var website: String = "",
        var company: String = "",
        var position: String = "",
        var education: String = "",
        var linkedin: String = "",
        var github: String = "",
        var twitter: String = "",
        var lastUpdated: String = getCurrentDate(),
        var profileImageUrl: String = ""
    )

    companion object {
        private fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}