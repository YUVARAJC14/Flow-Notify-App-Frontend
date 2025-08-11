package com.saveetha.flownotify

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.animation.AnimatorInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd

class VerificationCompleteActivity : AppCompatActivity() {

    private lateinit var circleContainer: CardView
    private lateinit var ivCheckmark: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_complete)

        // Initialize views
        circleContainer = findViewById(R.id.circleContainer)
        ivCheckmark = findViewById(R.id.ivCheckmark)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        btnContinue = findViewById(R.id.btnContinue)

        // Hide check mark initially
        ivCheckmark.alpha = 0f
        ivCheckmark.scaleX = 0f
        ivCheckmark.scaleY = 0f

        // Initially hide text elements and button
        tvTitle.alpha = 0f
        tvDescription.alpha = 0f
        btnContinue.alpha = 0f
        btnContinue.translationY = 100f

        // Start animations after a short delay
        findViewById<View>(android.R.id.content).postDelayed({
            animateCheckmark()
        }, 300)

        // Set button click listener
        btnContinue.setOnClickListener {
            // Navigate to onboarding screen
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish() // This will close the VerificationCompleteActivity
        }
    }

    private fun animateCheckmark() {
        // Create circle container animation
        val containerScaleX = ObjectAnimator.ofFloat(circleContainer, View.SCALE_X, 0.5f, 1f)
        val containerScaleY = ObjectAnimator.ofFloat(circleContainer, View.SCALE_Y, 0.5f, 1f)

        val containerSet = AnimatorSet().apply {
            playTogether(containerScaleX, containerScaleY)
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        // Load the check animation from XML resource
        // Load the check animation from XML resource
        val checkmarkAnimation = AnimatorInflater.loadAnimator(this, R.animator.check_animation)
        checkmarkAnimation.setTarget(ivCheckmark)

        // Animate text and button appearance
        val titleFadeIn = ObjectAnimator.ofFloat(tvTitle, View.ALPHA, 0f, 1f).apply {
            duration = 400
            startDelay = 800
        }

        val descFadeIn = ObjectAnimator.ofFloat(tvDescription, View.ALPHA, 0f, 1f).apply {
            duration = 400
            startDelay = 1000
        }

        val buttonFadeIn = ObjectAnimator.ofFloat(btnContinue, View.ALPHA, 0f, 1f).apply {
            duration = 400
            startDelay = 1200
        }

        val buttonTranslateY =
            ObjectAnimator.ofFloat(btnContinue, View.TRANSLATION_Y, 100f, 0f).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
                startDelay = 1200
            }

        // Play all animations together
        val finalSet = AnimatorSet().apply {
            play(containerSet).with(checkmarkAnimation)
            play(titleFadeIn).after(checkmarkAnimation)
            play(descFadeIn).after(titleFadeIn)
            play(buttonFadeIn).with(buttonTranslateY).after(descFadeIn)
            start()
        }

        // Add some subtle continuous animation to the checkmark to keep it lively
        finalSet.doOnEnd {
            val pulseX = ObjectAnimator.ofFloat(ivCheckmark, View.SCALE_X, 1f, 1.1f, 1f).apply {
                duration = 1500
                interpolator = AccelerateDecelerateInterpolator()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }

            val pulseY = ObjectAnimator.ofFloat(ivCheckmark, View.SCALE_Y, 1f, 1.1f, 1f).apply {
                duration = 1500
                interpolator = AccelerateDecelerateInterpolator()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }

            AnimatorSet().apply {
                playTogether(pulseX, pulseY)
                start()
            }
        }
    }
    
}