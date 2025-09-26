package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class ChangePasswordActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var passwordStrengthContainer: LinearLayout
    private lateinit var passwordStrengthProgress: ProgressBar
    private lateinit var passwordStrengthText: TextView
    private lateinit var passwordSuggestions: TextView
    private lateinit var twoFactorSwitch: SwitchCompat

    // Password validation state
    private var isPasswordValid = false
    private var isPasswordMatching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Password fields
        currentPasswordEditText = findViewById(R.id.et_current_password)
        newPasswordEditText = findViewById(R.id.et_new_password)
        confirmPasswordEditText = findViewById(R.id.et_confirm_password)

        // Password strength indicators
        passwordStrengthContainer = findViewById(R.id.password_strength_container)
        passwordStrengthProgress = findViewById(R.id.password_strength_progress)
        passwordStrengthText = findViewById(R.id.password_strength_text)
        passwordSuggestions = findViewById(R.id.password_suggestions)

        // Two-factor authentication switch
        twoFactorSwitch = findViewById(R.id.switch_two_factor)

        // Toolbar actions
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            savePassword()
        }

        findViewById<LinearLayout>(R.id.option_reset_password).setOnClickListener {
            navigateToResetPassword()
        }
    }

    private fun setupListeners() {
        // Password change listeners
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isEmpty()) {
                    passwordStrengthContainer.visibility = View.GONE
                    isPasswordValid = false
                } else {
                    passwordStrengthContainer.visibility = View.VISIBLE
                    evaluatePasswordStrength(password)
                }

                // Check if passwords match whenever either field changes
                checkPasswordsMatch()
            }
        })

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Check if passwords match
                checkPasswordsMatch()
            }
        })

        // Two-factor authentication switch
        twoFactorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // In a real app, you would launch the 2FA setup flow
                showTwoFactorSetupInfo()
            }
        }
    }

    private fun evaluatePasswordStrength(password: String) {
        // Simple password strength evaluation
        // In a real app, you would use a more comprehensive algorithm

        val length = password.length
        val hasUppercase = password.matches(".*[A-Z].*".toRegex())
        val hasLowercase = password.matches(".*[a-z].*".toRegex())
        val hasDigit = password.matches(".*\\d.*".toRegex())
        val hasSpecialChar = password.matches(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*".toRegex())

        // Calculate score (0-100)
        var score = 0

        // Length contributes up to 40 points
        score += minOf(40, length * 4)

        // Character variety contributes up to 60 points
        if (hasUppercase) score += 15
        if (hasLowercase) score += 15
        if (hasDigit) score += 15
        if (hasSpecialChar) score += 15

        // Update UI based on score
        passwordStrengthProgress.progress = score

        // Determine strength level and suggestions
        when {
            score < 40 -> {
                passwordStrengthText.text = "Weak"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.red))
                passwordSuggestions.text = "Add more characters, including uppercase letters, numbers, and symbols."
                isPasswordValid = false
            }
            score < 70 -> {
                passwordStrengthText.text = "Medium"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.orange))

                val suggestion = when {
                    !hasUppercase -> "Add uppercase letters for stronger security."
                    !hasDigit -> "Add numbers for stronger security."
                    !hasSpecialChar -> "Add special characters for stronger security."
                    else -> "Make your password longer for better security."
                }
                passwordSuggestions.text = suggestion
                isPasswordValid = length >= 8
            }
            else -> {
                passwordStrengthText.text = "Strong"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.green))
                passwordSuggestions.text = "Great password! It's strong and secure."
                isPasswordValid = true
            }
        }
    }

    private fun checkPasswordsMatch() {
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Only check if both fields have content
        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
            isPasswordMatching = newPassword == confirmPassword

            if (!isPasswordMatching) {
                confirmPasswordEditText.error = "Passwords do not match"
            } else {
                confirmPasswordEditText.error = null
            }
        }
    }

    private fun savePassword() {
        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // ... (validation logic remains the same)

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
                val json = "{\"currentPassword\":\"$currentPassword\",\"newPassword\":\"$newPassword\"}"
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://localhost:8000/api/users/me/password")
                    .addHeader("Authorization", "Bearer $token")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    if (response.isSuccessful) {
                        showSuccessDialog()
                    } else {
                        showAlert("Error", "Failed to update password. Please try again.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    showAlert("Error", "Failed to update password. Please try again.")
                }
            }
        }
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Password Updated")
            .setMessage("Your password has been successfully updated. You'll use your new password the next time you log in.")
            .setPositiveButton("OK") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showTwoFactorSetupInfo() {
        AlertDialog.Builder(this)
            .setTitle("Set Up Two-Factor Authentication")
            .setMessage("Two-factor authentication adds an extra layer of security to your account. In addition to your password, you'll need to enter a code that's sent to your phone or generated by an authenticator app.")
            .setPositiveButton("Set Up Now") { _, _ ->
                // In a real app, this would start the 2FA setup process
                Toast.makeText(this, "Two-factor authentication setup would start here", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Later") { _, _ ->
                // Reset the switch if the user cancels
                twoFactorSwitch.isChecked = false
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToResetPassword() {
        // In a real app, navigate to password reset flow
        Toast.makeText(this, "Password reset flow would start here", Toast.LENGTH_SHORT).show()
    }
}