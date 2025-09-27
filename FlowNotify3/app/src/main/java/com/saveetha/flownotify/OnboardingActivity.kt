package com.saveetha.flownotify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupViewPager()
        setupListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        nextButton = findViewById(R.id.btn_next)
    }

    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnboardingItem(R.drawable.ic_notification_logo, "Welcome to FlowNotify", "Let's help you stay organized and productive."),
            OnboardingItem(R.drawable.ic_notification_logo_2, "Create & Conquer", "Effortlessly add tasks and events to your schedule."),
            OnboardingItem(R.drawable.ic_notification_logo_3, "Stay in the Flow", "Get smart reminders and track your progress.")
        )

        onboardingAdapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = onboardingAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // This is just to create the dots
        }.attach()
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            if (viewPager.currentItem < onboardingAdapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                // Navigate to the next activity after onboarding is finished
                val intent = Intent(this, NameInputActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingAdapter.itemCount - 1) {
                    nextButton.text = "Get Started"
                } else {
                    nextButton.text = "Next"
                }
            }
        })
    }
}
