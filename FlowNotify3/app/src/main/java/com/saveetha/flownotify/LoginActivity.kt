package com.saveetha.flownotify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.LoginRequest
import com.saveetha.flownotify.network.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var emailOrUsernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var forgotPasswordText: TextView

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailOrUsernameEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)

        signInButton.setOnClickListener {
            login()
        }

        signUpButton.setOnClickListener {
            // Create an Intent to navigate to CreateAccountActivity
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)

            // Optional: Show a toast message for confirmation
            Toast.makeText(this, "Navigating to Create Account", Toast.LENGTH_SHORT).show()
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login() {
        val emailOrUsername = emailOrUsernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate email and password
        if (emailOrUsername.isEmpty()) {
            emailOrUsernameEditText.error = "Email or username is required"
            emailOrUsernameEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters long"
            passwordEditText.requestFocus()
            return
        }

        signInButton.isEnabled = false
        signInButton.text = "Logging In..."

        val request = LoginRequest(emailOrUsername, password)

        apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                signInButton.isEnabled = true
                signInButton.text = "Sign In"

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val sharedPreferences = getSharedPreferences("FlowNotifyPrefs", Context.MODE_PRIVATE)
                    loginResponse?.user?.name?.let { name ->
                        sharedPreferences.edit().putString("user_name", name).commit()
                    }
                    loginResponse?.accessToken?.let { token ->
                        sharedPreferences.edit().putString("access_token", token).commit()
                    }
                    // TODO: Save the access tokens securely
                    Toast.makeText(this@LoginActivity, "Login successful! Welcome ${loginResponse?.user?.name}", Toast.LENGTH_LONG).show()
                    // Navigate to the main activity
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@LoginActivity, "Login failed: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                signInButton.isEnabled = true
                signInButton.text = "Sign In"
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
