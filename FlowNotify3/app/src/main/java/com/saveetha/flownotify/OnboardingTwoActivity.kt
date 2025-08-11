package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingTwoActivity : AppCompatActivity() {

    private lateinit var btnNext: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_two)

        // Initialize views
        btnNext = findViewById(R.id.btnNext)

        // Set click listener for next button
        btnNext.setOnClickListener {
            // Navigate to the third onboarding page or main app
            val intent = Intent(this, OnboardingThreeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish() // Remove this if you want to keep previous pages in the back stack
        }
    }
}