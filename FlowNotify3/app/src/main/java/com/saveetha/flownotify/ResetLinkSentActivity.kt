package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetLinkSentActivity : AppCompatActivity() {

    private lateinit var emailMessageText: TextView
    private lateinit var resendEmailText: TextView
    private lateinit var backToLoginButton: Button
    private lateinit var contactSupportText: TextView

    private var resendTimer: CountDownTimer? = null
    private var canResend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_link_sent)

        // Initialize views
        emailMessageText = findViewById(R.id.tv_email_message)
        resendEmailText = findViewById(R.id.tv_resend_email)
        backToLoginButton = findViewById(R.id.btn_back_to_login)
        contactSupportText = findViewById(R.id.tv_contact_support)

        // Set up back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            navigateToLogin()
        }

        // Get email from intent
        val email = intent.getStringExtra("email") ?: "your email"
        emailMessageText.text = "We've sent a password reset link to\n$email"

        // Set up resend email
        resendEmailText.setOnClickListener {
            if (canResend) {
                resendEmail()
            }
        }

        // Set up back to login button
        backToLoginButton.setOnClickListener {
            navigateToLogin()
        }

        // Set up contact support
        contactSupportText.setOnClickListener {
            contactSupport()
        }

        // Start resend timer
        startResendTimer()
    }

    private fun startResendTimer() {
        canResend = false
        resendEmailText.alpha = 0.5f
        resendEmailText.text = "Resend Email (60s)"

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                resendEmailText.text = "Resend Email (${secondsRemaining}s)"
            }

            override fun onFinish() {
                canResend = true
                resendEmailText.alpha = 1.0f
                resendEmailText.text = "Resend Email"
            }
        }.start()
    }

    private fun resendEmail() {
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
                    Toast.makeText(
                        this@ResetLinkSentActivity,
                        "Reset link resent successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Restart the timer
                    startResendTimer()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@ResetLinkSentActivity,
                        "Failed to resend email. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToLogin() {
        // In a real app, this would navigate back to the login screen
        finish()
    }

    private fun contactSupport() {
        Toast.makeText(this, "Contacting support...", Toast.LENGTH_SHORT).show()
        // In a real app, this would open a support chat or email
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}