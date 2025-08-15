package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.RegisterRequest
import com.saveetha.flownotify.network.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateAccountActivity : AppCompatActivity() {

    // Input layouts
    private lateinit var inputLayoutFullName: TextInputLayout
    private lateinit var inputLayoutEmail: TextInputLayout
    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutConfirmPassword: TextInputLayout

    // EditTexts
    private lateinit var editTextFullName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText

    // Other views
    private lateinit var checkboxTerms: CheckBox
    private lateinit var txtTermsAndPolicy: TextView
    private lateinit var btnCreateAccount: Button

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure the layout file name matches exactly
        setContentView(R.layout.activity_create_account)

        // Initialize views
        initViews()
        setupTermsAndPolicyText()
        setupCreateAccountButton()
    }

    private fun initViews() {
        // Input layouts
        inputLayoutFullName = findViewById(R.id.inputLayoutFullName)
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail)
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword)
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword)

        // EditTexts
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)

        // Other views
        checkboxTerms = findViewById(R.id.checkboxTerms)
        txtTermsAndPolicy = findViewById(R.id.txtTermsAndPolicy)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
    }

    private fun setupTermsAndPolicyText() {
        val text = "I agree to the Terms of Service and Privacy Policy"
        val spannableString = SpannableString(text)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Terms of Service page
                Toast.makeText(this@CreateAccountActivity, "Terms of Service clicked", Toast.LENGTH_SHORT).show()
            }
        }

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Privacy Policy page
                Toast.makeText(this@CreateAccountActivity, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
            }
        }

        // Apply spans to make parts of the text clickable
        spannableString.setSpan(termsClickableSpan, text.indexOf("Terms of Service"),
            text.indexOf("Terms of Service") + "Terms of Service".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(privacyClickableSpan, text.indexOf("Privacy Policy"),
            text.indexOf("Privacy Policy") + "Privacy Policy".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        txtTermsAndPolicy.text = spannableString
        txtTermsAndPolicy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupCreateAccountButton() {
        btnCreateAccount.setOnClickListener {
            if (validateInputs()) {
                // Proceed with account creation
                createAccount()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate full name
        if (editTextFullName.text.toString().trim().isEmpty()) {
            inputLayoutFullName.error = "Please enter your full name"
            isValid = false
        } else {
            inputLayoutFullName.error = null
        }

        // Validate email
        val email = editTextEmail.text.toString().trim()
        if (email.isEmpty()) {
            inputLayoutEmail.error = "Please enter your email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.error = "Please enter a valid email address"
            isValid = false
        } else {
            inputLayoutEmail.error = null
        }

        // Validate password
        val password = editTextPassword.text.toString()
        if (password.isEmpty()) {
            inputLayoutPassword.error = "Please enter your password"
            isValid = false
        } else if (password.length < 8 || !password.any { it.isDigit() } ||
            !password.any { !it.isLetterOrDigit() }) {
            inputLayoutPassword.error =
                "Password must be at least 8 characters with 1 number and 1 special character"
            isValid = false
        } else {
            inputLayoutPassword.error = null
        }

        // Validate confirm password
        val confirmPassword = editTextConfirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            inputLayoutConfirmPassword.error = "Passwords don't match"
            isValid = false
        } else {
            inputLayoutConfirmPassword.error = null
        }

        // Validate terms checkbox
        if (!checkboxTerms.isChecked) {
            Toast.makeText(this, "Please agree to Terms of Service and Privacy Policy",
                Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun createAccount() {
        btnCreateAccount.isEnabled = false
        btnCreateAccount.text = "Creating Account..."

        val fullName = editTextFullName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        val request = RegisterRequest(fullName, email, password)

        apiService.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                btnCreateAccount.isEnabled = true
                btnCreateAccount.text = "Create Account"

                if (response.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@CreateAccountActivity, NameInputActivity::class.java)
                    intent.putExtra("fullName", fullName) // Pass the full name
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                } else {
                    // Handle API error
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@CreateAccountActivity, "Registration failed: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                btnCreateAccount.isEnabled = true
                btnCreateAccount.text = "Create Account"
                Toast.makeText(this@CreateAccountActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}