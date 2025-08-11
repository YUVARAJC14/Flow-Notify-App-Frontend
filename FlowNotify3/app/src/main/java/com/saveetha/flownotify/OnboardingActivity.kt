package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnNext: FloatingActionButton
    private val currentPage = 0 // This is the first page (index 0)
    private val totalPages = 3 // Total onboarding pages

    // Onboarding content for different pages
    private val titles = arrayOf(
        "Smart Notifications",
        "Schedule Management",
        "Priority Tasks"
    )

    private val descriptions = arrayOf(
        "Get personalized reminders that adapt to your schedule and priorities",
        "Manage your day efficiently with our intelligent scheduling assistant",
        "Focus on what matters most with our priority-based notification system"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize views
        btnNext = findViewById(R.id.btnNext)

        // Set click listener for next button
        btnNext.setOnClickListener {
            if (currentPage < totalPages - 1) {
                // If not on last page, proceed to next onboarding page
                navigateToNextOnboardingPage()
            } else {
                // If on last page, show a message instead of navigating
                finishOnboarding()
            }
        }
    }

    private fun navigateToNextOnboardingPage() {
        // In a real implementation, you would use a ViewPager2 to handle multiple pages
        // For this simple example, we'll just create a new intent to simulate going to the next page
        val intent = Intent(this, OnboardingTwoActivity::class.java)
        intent.putExtra("page", currentPage + 1)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish() // Remove this if you want to keep previous pages in the back stack
    }
    private fun finishOnboarding() {
        // Show a message indicating onboarding is complete
        Toast.makeText(this, "Onboarding completed!", Toast.LENGTH_LONG).show()

        // Optionally finish the activity or return to previous screen
        finish()

        // You can add code here later to navigate to your MainActivity when you create it
    }
}