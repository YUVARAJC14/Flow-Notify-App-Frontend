package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateNewPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var passwordStrengthMeter: ProgressBar
    private lateinit var passwordStrengthText: TextView
    private lateinit var resetPasswordButton: Button

    // Password requirement UI elements
    private lateinit var lengthCheckIcon: ImageView
    private lateinit var uppercaseCheckIcon: ImageView
    private lateinit var numberCheckIcon: ImageView
    private lateinit var specialCheckIcon: ImageView
    private lateinit var lengthCheckText: TextView
    private lateinit var uppercaseCheckText: TextView
    private lateinit var numberCheckText: TextView
    private lateinit var specialCheckText: TextView

    // Password requirements
    private var hasMinLength = false
    private var hasUppercase = false
    private var hasNumber = false
    private var hasSpecial = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_password)

        // Initialize views
        newPasswordEditText = findViewById(R.id.et_new_password)
        confirmPasswordEditText = findViewById(R.id.et_confirm_password)
        newPasswordLayout = findViewById(R.id.til_new_password)
        confirmPasswordLayout = findViewById(R.id.til_confirm_password)
        passwordStrengthMeter = findViewById(R.id.password_strength_meter)
        passwordStrengthText = findViewById(R.id.tv_password_strength)
        resetPasswordButton = findViewById(R.id.btn_reset_password)

        // Password requirement views
        lengthCheckIcon = findViewById(R.id.iv_check_length)
        uppercaseCheckIcon = findViewById(R.id.iv_check_uppercase)
        numberCheckIcon = findViewById(R.id.iv_check_number)
        specialCheckIcon = findViewById(R.id.iv_check_special)
        lengthCheckText = findViewById(R.id.tv_check_length)
        uppercaseCheckText = findViewById(R.id.tv_check_uppercase)
        numberCheckText = findViewById(R.id.tv_check_number)
        specialCheckText = findViewById(R.id.tv_check_special)

        // Set up back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Set up password validation
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                validatePassword(password)
                updatePasswordStrength(password)
                checkPasswordsMatch()
            }
        })

        // Set up confirm password validation
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkPasswordsMatch()
            }
        })

        // Set up reset password button
        resetPasswordButton.setOnClickListener {
            if (validateInputs()) {
                resetPassword()
            }
        }
    }

    private fun validatePassword(password: String) {
        // Check minimum length (8 characters)
        hasMinLength = password.length >= 8
        updateRequirementStatus(lengthCheckIcon, lengthCheckText, hasMinLength)

        // Check for at least one uppercase letter
        hasUppercase = password.any { it.isUpperCase() }
        updateRequirementStatus(uppercaseCheckIcon, uppercaseCheckText, hasUppercase)

        // Check for at least one number
        hasNumber = password.any { it.isDigit() }
        updateRequirementStatus(numberCheckIcon, numberCheckText, hasNumber)

        // Check for at least one special character
        hasSpecial = password.any { !it.isLetterOrDigit() }
        updateRequirementStatus(specialCheckIcon, specialCheckText, hasSpecial)

        // Update button status
        updateButtonStatus()
    }

    private fun updateRequirementStatus(icon: ImageView, text: TextView, isValid: Boolean) {
        if (isValid) {
            icon.setImageResource(R.drawable.ic_check_circle)
            icon.setColorFilter(ContextCompat.getColor(this, R.color.green))
            text.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            icon.setImageResource(R.drawable.ic_check_circle)
            icon.setColorFilter(ContextCompat.getColor(this, R.color.gray))
            text.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    private fun updatePasswordStrength(password: String) {
        val progress = calculatePasswordStrength(password)
        passwordStrengthMeter.progress = progress

        when {
            progress >= 80 -> {
                passwordStrengthText.text = "Strong"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
            progress >= 40 -> {
                passwordStrengthText.text = "Medium"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.green))
            }
            else -> {
                passwordStrengthText.text = "Weak"
                passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.password_weak))
            }
        }
    }

    private fun calculatePasswordStrength(password: String): Int {
        if (password.isEmpty()) return 0

        var score = 0

        // Length contributes up to 40 points
        score += minOf(password.length * 5, 40)

        // Character types contribute up to 60 points
        if (hasUppercase) score += 15
        if (hasNumber) score += 15
        if (hasSpecial) score += 20
        if (password.any { it.isLowerCase() }) score += 10

        return minOf(score, 100)
    }

    private fun checkPasswordsMatch() {
        val password = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (confirmPassword.isNotEmpty()) {
            if (password != confirmPassword) {
                confirmPasswordLayout.error = "Passwords do not match"
            } else {
                confirmPasswordLayout.error = null
            }
        }

        updateButtonStatus()
    }

    private fun updateButtonStatus() {
        val allRequirementsMet = hasMinLength && hasUppercase && hasNumber && hasSpecial
        val passwordsMatch = newPasswordEditText.text.toString() == confirmPasswordEditText.text.toString() && confirmPasswordEditText.text.toString().isNotEmpty()

        resetPasswordButton.isEnabled = allRequirementsMet && passwordsMatch
    }

    private fun validateInputs(): Boolean {
        val password = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        var isValid = true

        if (password.isEmpty()) {
            newPasswordLayout.error = "Please enter a new password"
            isValid = false
        } else if (!hasMinLength || !hasUppercase || !hasNumber || !hasSpecial) {
            newPasswordLayout.error = "Password doesn't meet requirements"
            isValid = false
        } else {
            newPasswordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun resetPassword() {
        val password = newPasswordEditText.text.toString()

        // Show loading dialog
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        loadingDialog.show()

        // Simulate API call
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate network delay
                delay(1500)

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    showSuccessDialog()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@CreateNewPasswordActivity,
                        "Failed to reset password. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Password Reset Successful")
            .setMessage("Your password has been reset successfully. You can now log in with your new password.")
            .setPositiveButton("Log In") { dialog, _ ->
                dialog.dismiss()
                navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLogin() {
        // In a real app, this would navigate back to the login screen with flags to clear the back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}