package com.saveetha.flownotify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class EditProfileActivity : AppCompatActivity() {

    // UI components
    private lateinit var profilePictureImageView: ShapeableImageView
    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var companyEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var changePictureButton: ImageView

    // Data
    private var selectedImageUri: Uri? = null
    private var originalProfileData = ProfileData()
    private var hasChanges = false

    // Constants
    companion object {
        private const val PERMISSION_REQUEST_READ_STORAGE = 1001
        private const val REQUEST_IMAGE_PICK = 1002
    }

    data class ProfileData(
        var name: String = "",
        var email: String = "",
        var location: String = "",
        var company: String = "",
        var bio: String = "",
        var profileImageUrl: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        retrieveProfileData()
        setupListeners()
    }

    private fun initViews() {
        // Profile image and buttons
        profilePictureImageView = findViewById(R.id.iv_profile_picture)
        changePictureButton = findViewById(R.id.btn_change_picture)

        // Form fields
        fullNameEditText = findViewById(R.id.et_full_name)
        emailEditText = findViewById(R.id.et_email)
        locationEditText = findViewById(R.id.et_location)
        companyEditText = findViewById(R.id.et_company)
        bioEditText = findViewById(R.id.et_bio)

        // Toolbar actions
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            saveChanges()
        }
    }

    private fun retrieveProfileData() {
        // Get data passed from ProfileActivity, if any
        intent.extras?.let { bundle ->
            originalProfileData.name = bundle.getString("userName", "YUVARAJC14")
            originalProfileData.email = bundle.getString("userEmail", "yuvaraj@example.com")
            originalProfileData.location = bundle.getString("userLocation", "Thandalam, Chennai")
            originalProfileData.company = bundle.getString("userCompany", "Saveetha School of Engineering")
            originalProfileData.bio = bundle.getString("userBio", "Student")
            originalProfileData.profileImageUrl = bundle.getString("profileImageUrl", "")
        }

        // If no data was passed, try to fetch from API
        if (originalProfileData.name == "YUVARAJC14" && originalProfileData.email == "yuvaraj@example.com") {
            fetchProfileDataFromApi()
        } else {
            // Fill form with existing data
            populateFormFields()
        }
    }

    private fun fetchProfileDataFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // In a real app, this would be an API call to get user data
                // For this example, we'll use hardcoded values from the GitHub profile

                withContext(Dispatchers.Main) {
                    // Set values from GitHub profile info
                    originalProfileData.name = "YUVARAJC14"
                    originalProfileData.email = "yuvaraj@example.com"
                    originalProfileData.location = "Thandalam, Chennai"
                    originalProfileData.company = "Saveetha School of Engineering"
                    originalProfileData.bio = "Student"
                    // We would normally get a profile image URL here too

                    populateFormFields()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Failed to load profile data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun populateFormFields() {
        // Set text fields
        fullNameEditText.setText(originalProfileData.name)
        emailEditText.setText(originalProfileData.email)
        locationEditText.setText(originalProfileData.location)
        companyEditText.setText(originalProfileData.company)
        bioEditText.setText(originalProfileData.bio)

        // Load profile image if available
        if (originalProfileData.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(originalProfileData.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(profilePictureImageView)
        } else {
            // Use default image
            profilePictureImageView.setImageResource(R.drawable.default_profile_image)
        }
    }

    private fun setupListeners() {
        // Profile picture change
        changePictureButton.setOnClickListener {
            showImageSelectionDialog()
        }

        // Option listeners
        findViewById<LinearLayout>(R.id.option_change_password).setOnClickListener {
            // Navigate to change password screen
            Intent(this, ChangePasswordActivity::class.java).also {
                startActivity(it)
            }
        }

        findViewById<LinearLayout>(R.id.option_notification_preferences).setOnClickListener {
            // Navigate to notification preferences screen
            Intent(this, NotificationPreferencesActivity::class.java).also {
                startActivity(it)
            }
        }

        findViewById<LinearLayout>(R.id.option_privacy_settings).setOnClickListener {
            // Navigate to privacy settings screen
            Intent(this, PrivacySettingsActivity::class.java).also {
                startActivity(it)
            }
        }

        // Monitor text changes to detect modifications
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                checkForChanges()
            }
        }

        // Add text change listeners to all editable fields
        fullNameEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        locationEditText.addTextChangedListener(textWatcher)
        companyEditText.addTextChangedListener(textWatcher)
        bioEditText.addTextChangedListener(textWatcher)
    }

    private fun checkForChanges() {
        val hasTextChanges = fullNameEditText.text.toString() != originalProfileData.name ||
                emailEditText.text.toString() != originalProfileData.email ||
                locationEditText.text.toString() != originalProfileData.location ||
                companyEditText.text.toString() != originalProfileData.company ||
                bioEditText.text.toString() != originalProfileData.bio

        hasChanges = hasTextChanges || selectedImageUri != null
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        // This is a simplified example - in a real app, you would also handle camera permissions
        // and create a file to save the photo
        Toast.makeText(this, "Camera functionality would be implemented here", Toast.LENGTH_SHORT).show()
    }

    private fun openGallery() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_STORAGE
            )
        } else {
            launchGallery()
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePictureImageView)

                hasChanges = true
            }
        }
    }

    private fun saveChanges() {
        if (!hasChanges) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate form
        if (fullNameEditText.text.isNullOrBlank()) {
            fullNameEditText.error = "Name cannot be empty"
            return
        }

        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        loadingDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", MODE_PRIVATE)
                val token = sharedPreferences.getString("accessToken", null) ?: return@launch

                val client = OkHttpClient()
                val json = "{\"name\":\"${fullNameEditText.text}\"}"
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://localhost:8000/api/users/me/profile")
                    .addHeader("Authorization", "Bearer $token")
                    .patch(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    if (response.isSuccessful) {
                        val resultIntent = Intent().apply {
                            putExtra("updatedName", fullNameEditText.text.toString())
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    override fun onBackPressed() {
        if (hasChanges) {
            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}