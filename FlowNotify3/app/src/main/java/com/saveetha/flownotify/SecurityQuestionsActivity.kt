package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class SecurityQuestionsActivity : AppCompatActivity() {

    private lateinit var answer1EditText: TextInputEditText
    private lateinit var answer2EditText: TextInputEditText
    private lateinit var answer1Layout: TextInputLayout
    private lateinit var answer2Layout: TextInputLayout
    private lateinit var verifyButton: Button
    private lateinit var tryAnotherMethodText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_questions)

        // Initialize views
        answer1EditText = findViewById(R.id.et_answer1)
        answer2EditText = findViewById(R.id.et_answer2)
        answer1Layout = findViewById(R.id.til_answer1)
        answer2Layout = findViewById(R.id.til_answer2)
        verifyButton = findViewById(R.id.btn_verify)
        tryAnotherMethodText = findViewById(R.id.tv_try_another_method)

        // Set up back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Set up answer validation
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        }

        answer1EditText.addTextChangedListener(textWatcher)
        answer2EditText.addTextChangedListener(textWatcher)

        // Set up verify button
        verifyButton.setOnClickListener {
            if (validateInputs()) {
                verifyAnswers()
            }
        }

        // Set up try another method
        tryAnotherMethodText.setOnClickListener {
            finish() // Go back to the previous screen
        }
    }

    private fun validateInputs(): Boolean {
        val answer1 = answer1EditText.text.toString().trim()
        val answer2 = answer2EditText.text.toString().trim()

        var isValid = true

        if (answer1.isEmpty()) {
            answer1Layout.error = "Please provide an answer"
            isValid = false
        } else {
            answer1Layout.error = null
        }

        if (answer2.isEmpty()) {
            answer2Layout.error = "Please provide an answer"
            isValid = false
        } else {
            answer2Layout.error = null
        }

        return isValid
    }

    private fun verifyAnswers() {
        val answer1 = answer1EditText.text.toString().trim()
        val answer2 = answer2EditText.text.toString().trim()

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

                // For demo purposes, we'll use predetermined answers
                // In a real app, this would verify against stored values
                val correctAnswer1 = "max" // Case-insensitive comparison
                val correctAnswer2 = "chennai"

                val isAnswer1Correct = answer1.lowercase() == correctAnswer1.lowercase()
                val isAnswer2Correct = answer2.lowercase() == correctAnswer2.lowercase()

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()

                    if (isAnswer1Correct && isAnswer2Correct) {
                        // Navigate to create new password screen
                        val intent = Intent(this@SecurityQuestionsActivity, CreateNewPasswordActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showErrorDialog()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@SecurityQuestionsActivity,
                        "Verification failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Incorrect Answers")
            .setMessage("The answers you provided don't match our records. Please try again or use another recovery method.")
            .setPositiveButton("Try Again") { dialog, _ ->
                dialog.dismiss()
                answer1EditText.text = null
                answer2EditText.text = null
                answer1EditText.requestFocus()
            }
            .setNegativeButton("Use Another Method") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}