package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var sendResetLinkButton: Button
    private lateinit var alternativeOptionsText: TextView
    private lateinit var backToLoginText: TextView
    private lateinit var contactSupportText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        emailEditText = findViewById(R.id.et_email)
        emailLayout = findViewById(R.id.til_email)
        sendResetLinkButton = findViewById(R.id.btn_send_reset_link)
        alternativeOptionsText = findViewById(R.id.tv_alternative_options)
        backToLoginText = findViewById(R.id.tv_back_to_login)
        contactSupportText = findViewById(R.id.tv_contact_support)

        // Set up back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Set up email validation
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }
        })

        // Set up send reset link button
        sendResetLinkButton.setOnClickListener {
            if (validateEmail()) {
                sendResetLink()
            }
        }

        // Set up alternative options
        alternativeOptionsText.setOnClickListener {
            showAlternativeOptions()
        }

        // Set up back to login
        backToLoginText.setOnClickListener {
            finish()
        }

        // Set up contact support
        contactSupportText.setOnClickListener {
            contactSupport()
        }

        // Pre-fill email if user came from login screen with a saved email
        val email = intent.getStringExtra("email")
        if (!email.isNullOrEmpty()) {
            emailEditText.setText(email)
        }
    }

    private fun validateEmail(): Boolean {
        val email = emailEditText.text.toString().trim()

        return if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email address"
            false
        } else {
            emailLayout.error = null
            true
        }
    }

    private fun sendResetLink() {
        val email = emailEditText.text.toString().trim()

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

                    // Navigate to reset link sent screen
                    val intent = Intent(this@ForgotPasswordActivity, ResetLinkSentActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Failed to send reset link. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAlternativeOptions() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recovery_option, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set up SMS recovery option
        dialogView.findViewById<View>(R.id.option_sms_recovery).setOnClickListener {
            dialog.dismiss()
            navigateToSmsVerification()
        }

        // Set up security questions option
        dialogView.findViewById<View>(R.id.option_security_questions).setOnClickListener {
            dialog.dismiss()
            navigateToSecurityQuestions()
        }

        // Set up contact support option
        dialogView.findViewById<View>(R.id.option_contact_support).setOnClickListener {
            dialog.dismiss()
            contactSupport()
        }

        // Set up cancel button
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navigateToSmsVerification() {
        val intent = Intent(this, SmsVerificationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSecurityQuestions() {
        val intent = Intent(this, SecurityQuestionsActivity::class.java)
        startActivity(intent)
    }

    private fun contactSupport() {
        Toast.makeText(this, "Contacting support...", Toast.LENGTH_SHORT).show()
        // In a real app, this would open a support chat or email
    }
}