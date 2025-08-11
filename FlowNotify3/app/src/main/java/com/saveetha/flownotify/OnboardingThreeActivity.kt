package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingThreeActivity : AppCompatActivity() {

    private lateinit var btnNext: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_three)

        // Initialize views
        btnNext = findViewById(R.id.btnNext)

        // Set click listener for next button
        btnNext.setOnClickListener {
            // This is the last onboarding page, so navigate to main app
            val intent = Intent(this, NameInputActivity::class.java)
            // If MainActivity doesn't exist yet, you might want to navigate
            // to another activity like HomeActivity or DashboardActivity
            startActivity(intent)
            finishAffinity() // Close all activities in the stack
        }
    }
}