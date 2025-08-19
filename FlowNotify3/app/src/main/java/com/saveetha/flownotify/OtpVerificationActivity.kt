package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
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

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var verificationMessageText: TextView
    private lateinit var codeEditTexts: Array<EditText>
    private lateinit var resendCodeText: TextView
    private lateinit var countdownText: TextView
    private lateinit var verifyButton: Button
    private lateinit var changeNumberText: TextView

    private var phoneNumber: String = ""
    private var resendTimer: CountDownTimer? = null
    private var canResend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        // Get phone number from intent
        phoneNumber = intent.getStringExtra("phone_number") ?: ""

        // Initialize views
        verificationMessageText = findViewById(R.id.tv_verification_message)
        codeEditTexts = arrayOf(
            findViewById(R.id.et_code_1),
            findViewById(R.id.et_code_2),
            findViewById(R.id.et_code_3),
            findViewById(R.id.et_code_4),
            findViewById(R.id.et_code_5),
            findViewById(R.id.et_code_6)
        )
        resendCodeText = findViewById(R.id.tv_resend_code)
        countdownText = findViewById(R.id.tv_countdown)
        verifyButton = findViewById(R.id.btn_verify)
        changeNumberText = findViewById(R.id.tv_change_number)

        // Set up back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Set masked phone number
        val maskedNumber = getMaskedPhoneNumber(phoneNumber)
        verificationMessageText.text = "Enter the 6-digit code sent to $maskedNumber"

        // Set up OTP input
        setupOtpInputs()

        // Set up resend code
        resendCodeText.setOnClickListener {
            if (canResend) {
                resendCode()
            }
        }

        // Set up verify button
        verifyButton.setOnClickListener {
            verifyCode()
        }

        // Set up change number
        changeNumberText.setOnClickListener {
            finish() // Go back to phone number input
        }

        // Start resend timer
        startResendTimer()
    }

    private fun getMaskedPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length <= 4) return phoneNumber

        val countryCode = phoneNumber.substring(0, phoneNumber.length - 10)
        val lastFour = phoneNumber.takeLast(4)
        val masked = "*".repeat(6)

        return "$countryCode $masked$lastFour"
    }

    private fun setupOtpInputs() {
        for (i in codeEditTexts.indices) {
            codeEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Auto-focus to next field
                    if (s?.length == 1 && i < codeEditTexts.size - 1) {
                        codeEditTexts[i + 1].requestFocus()
                    }

                    // Enable verify button if all fields are filled
                    verifyButton.isEnabled = isCodeComplete()
                }
            })

            // Handle backspace to move to previous field
            codeEditTexts[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (codeEditTexts[i].text.isEmpty() && i > 0) {
                        codeEditTexts[i - 1].requestFocus()
                        codeEditTexts[i - 1].text = null
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun isCodeComplete(): Boolean {
        return codeEditTexts.all { it.text.isNotEmpty() }
    }

    private fun getEnteredCode(): String {
        return codeEditTexts.joinToString("") { it.text.toString() }
    }

    private fun startResendTimer() {
        canResend = false
        resendCodeText.alpha = 0.5f

        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                countdownText.text = "Resend available in 00:${String.format("%02d", secondsRemaining)}"
            }

            override fun onFinish() {
                canResend = true
                resendCodeText.alpha = 1.0f
                countdownText.text = "You can now resend the code"
            }
        }.start()
    }

    private fun resendCode() {
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
                        this@OtpVerificationActivity,
                        "Verification code resent successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Clear existing code
                    codeEditTexts.forEach { it.text = null }
                    codeEditTexts[0].requestFocus()

                    // Restart the timer
                    startResendTimer()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        "Failed to resend code. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun verifyCode() {
        val code = getEnteredCode()

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

                // For demo purposes, any 6-digit code is considered valid
                val isValid = code.length == 6

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()

                    if (isValid) {
                        showVerificationSuccessDialog()
                    } else {
                        Toast.makeText(
                            this@OtpVerificationActivity,
                            "Invalid verification code. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Clear code fields
                        codeEditTexts.forEach { it.text = null }
                        codeEditTexts[0].requestFocus()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        "Verification failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showVerificationSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Phone Verified!")
            .setMessage("Your phone number has been successfully verified. You can now enjoy all features of the app.")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                navigateToMainApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMainApp() {
        // In a real app, this would navigate to the main screen
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}