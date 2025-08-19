package com.saveetha.flownotify

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NameInputActivity : AppCompatActivity() {

    private lateinit var inputLayoutName: TextInputLayout
    private lateinit var editTextName: TextInputEditText
    private lateinit var btnContinue: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_input)

        // Initialize views
        inputLayoutName = findViewById(R.id.inputLayoutName)
        editTextName = findViewById(R.id.editTextName)
        btnContinue = findViewById(R.id.btnContinue)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FlowNotifyPrefs", Context.MODE_PRIVATE)

        // Set up text change listener
        editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Clear any error when user types
                inputLayoutName.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // Update button state based on input
                btnContinue.isEnabled = !s.isNullOrBlank()
            }
        })

        // Initially disable button until name is entered
        btnContinue.isEnabled = false

        // Check if a name was passed from the previous activity (e.g., CreateAccountActivity)
        val prefilledName = intent.getStringExtra("fullName")
        if (!prefilledName.isNullOrEmpty()) {
            editTextName.setText(prefilledName)
            btnContinue.isEnabled = true // Enable button if pre-filled
        }

        // Set up continue button click listener
        btnContinue.setOnClickListener {
            submitName()
        }
    }

    private fun submitName() {
        val name = editTextName.text.toString().trim()

        if (name.isEmpty()) {
            inputLayoutName.error = "Please enter your name"
            return
        }

        // Save name in SharedPreferences
        sharedPreferences.edit().putString("user_name", name).apply()

        // Show a brief confirmation message
        Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()

        // Navigate to the next screen in your app flow
        // This could be your main app screen or another onboarding screen
        val intent = Intent(this, VerificationCompleteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
        startActivity(intent)
        finish()
    }
}