package com.saveetha.flownotify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackActivity : AppCompatActivity() {

    // UI elements
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingDescriptionText: TextView
    private lateinit var feedbackTypeChips: ChipGroup
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var feedbackTitleEditText: TextInputEditText
    private lateinit var feedbackDescriptionEditText: TextInputEditText
    private lateinit var attachmentsRecyclerView: RecyclerView
    private lateinit var contactMeCheckbox: CheckBox
    private lateinit var contactEmailText: TextView
    private lateinit var submitFeedbackButton: Button
    private lateinit var noFeedbackLayout: LinearLayout
    private lateinit var previousFeedbackLayout: LinearLayout

    // User data
    private val userEmail = "yuvaraj@example.com"
    private val currentDate = "2025-08-08 08:44:31" // From the input parameters

    // Category data
    private val generalCategories = arrayOf("General", "User Interface", "Performance", "Notifications", "Calendar", "Tasks", "Integrations")
    private val bugCategories = arrayOf("Crash", "UI Glitch", "Performance Issue", "Feature Not Working", "Sync Problem")
    private val suggestionCategories = arrayOf("New Feature", "Improvement", "Integration Request", "Design Change")

    // Attachment data
    private val attachments = mutableListOf<AttachmentData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        initViews()
        setupListeners()
        updateUIForUser()
    }

    private fun initViews() {
        // Rating section
        ratingBar = findViewById(R.id.rating_bar)
        ratingDescriptionText = findViewById(R.id.tv_rating_description)

        // Feedback form
        feedbackTypeChips = findViewById(R.id.feedback_type_chips)
        categoryDropdown = findViewById(R.id.dropdown_category)
        feedbackTitleEditText = findViewById(R.id.et_feedback_title)
        feedbackDescriptionEditText = findViewById(R.id.et_feedback_description)
        attachmentsRecyclerView = findViewById(R.id.rv_attachments)
        contactMeCheckbox = findViewById(R.id.cb_contact_me)
        contactEmailText = findViewById(R.id.tv_contact_email)
        submitFeedbackButton = findViewById(R.id.btn_submit_feedback)

        // Previous feedback section
        noFeedbackLayout = findViewById(R.id.layout_no_feedback)
        previousFeedbackLayout = findViewById(R.id.layout_previous_feedback)

        // Setup attachments recycler view
        attachmentsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachmentsRecyclerView.adapter = AttachmentAdapter(attachments) { position ->
            removeAttachment(position)
        }

        // Set category dropdown default adapter
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, generalCategories)
        categoryDropdown.setAdapter(adapter)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // Check if user has previous feedback
        checkPreviousFeedback()
    }

    private fun setupListeners() {
        // Rating bar listener
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            updateRatingDescription(rating)
        }

        // Feedback type chip listener
        feedbackTypeChips.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_suggestion -> setCategoryAdapter(suggestionCategories)
                R.id.chip_bug -> setCategoryAdapter(bugCategories)
                else -> setCategoryAdapter(generalCategories)
            }
        }

        // Contact me checkbox
        contactMeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            contactEmailText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Add attachment button
        findViewById<Button>(R.id.btn_add_attachment).setOnClickListener {
            showAttachmentOptions()
        }

        // Rate on Play Store button
        findViewById<Button>(R.id.btn_rate_app_store).setOnClickListener {
            openPlayStore()
        }

        // Submit feedback button
        submitFeedbackButton.setOnClickListener {
            submitFeedback()
        }

        // View all feedback button
        findViewById<Button>(R.id.btn_view_all_feedback).setOnClickListener {
            showAllFeedback()
        }
    }

    private fun updateUIForUser() {
        // Set user email in the contact text
        contactEmailText.text = "We'll reach out to you at: $userEmail"

        // Pre-select a feedback type
        findViewById<Chip>(R.id.chip_suggestion).isChecked = true
    }

    private fun updateRatingDescription(rating: Float) {
        val description = when {
            rating == 0f -> "Tap to rate"
            rating <= 1f -> "Very Disappointed"
            rating <= 2f -> "Disappointed"
            rating <= 3f -> "Neutral"
            rating <= 4f -> "Satisfied"
            else -> "Very Satisfied"
        }

        ratingDescriptionText.text = description
    }

    private fun setCategoryAdapter(categories: Array<String>) {
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, categories)
        categoryDropdown.setAdapter(adapter)
        categoryDropdown.setText(categories[0], false)
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Attach File")

        AlertDialog.Builder(this)
            .setTitle("Add Attachment")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> choosePicture()
                    2 -> chooseFile()
                }
            }
            .show()
    }

    private fun takePicture() {
        // In a real app, this would launch the camera
        // For demo purposes, we'll just add a mock attachment
        addMockAttachment("Camera_${System.currentTimeMillis()}.jpg", AttachmentType.IMAGE)
    }

    private fun choosePicture() {
        // In a real app, this would open the gallery
        // For demo purposes, we'll just add a mock attachment
        addMockAttachment("Gallery_${System.currentTimeMillis()}.jpg", AttachmentType.IMAGE)
    }

    private fun chooseFile() {
        // In a real app, this would open a file picker
        // For demo purposes, we'll just add a mock attachment
        addMockAttachment("Document_${System.currentTimeMillis()}.pdf", AttachmentType.FILE)
    }

    private fun addMockAttachment(name: String, type: AttachmentType) {
        if (attachments.size >= 5) {
            Toast.makeText(this, "Maximum 5 attachments allowed", Toast.LENGTH_SHORT).show()
            return
        }

        attachments.add(AttachmentData(name, type, null))
        attachmentsRecyclerView.adapter?.notifyItemInserted(attachments.size - 1)
    }

    private fun removeAttachment(position: Int) {
        if (position >= 0 && position < attachments.size) {
            attachments.removeAt(position)
            attachmentsRecyclerView.adapter?.notifyItemRemoved(position)
        }
    }

    private fun checkPreviousFeedback() {
        // In a real app, this would check if the user has previously submitted feedback
        // For this example, we'll assume they have

        val hasPreviousFeedback = true

        if (hasPreviousFeedback) {
            noFeedbackLayout.visibility = View.GONE
            previousFeedbackLayout.visibility = View.VISIBLE
        } else {
            noFeedbackLayout.visibility = View.VISIBLE
            previousFeedbackLayout.visibility = View.GONE
        }
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.example.flownotify")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback for when Play Store app is not installed
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.example.flownotify")
            }
            startActivity(intent)
        }
    }

    private fun submitFeedback() {
        // Validate inputs
        val title = feedbackTitleEditText.text.toString().trim()
        val description = feedbackDescriptionEditText.text.toString().trim()
        val rating = ratingBar.rating

        if (title.isEmpty()) {
            feedbackTitleEditText.error = "Please enter a title"
            return
        }

        if (description.isEmpty()) {
            feedbackDescriptionEditText.error = "Please enter a description"
            return
        }

        // Show loading dialog
        val loadingDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        loadingDialog.show()

        // In a real app, this would send the feedback to your backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate API call
                kotlinx.coroutines.delay(1500)

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    showFeedbackSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@FeedbackActivity,
                        "Failed to submit feedback. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showFeedbackSuccess() {
        AlertDialog.Builder(this)
            .setTitle("Thank You!")
            .setMessage("Your feedback has been submitted successfully. We appreciate your input and will use it to improve Flow Notify.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showAllFeedback() {
        // In a real app, this would navigate to a screen showing all user feedback
        Toast.makeText(this, "Viewing all feedback history", Toast.LENGTH_SHORT).show()
    }
}

// Attachment data class and adapter
enum class AttachmentType {
    IMAGE, FILE
}

data class AttachmentData(
    val name: String,
    val type: AttachmentType,
    val uri: Uri?
)

class AttachmentAdapter(
    private val attachments: List<AttachmentData>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_attachment)
        val removeButton: ImageView = view.findViewById(R.id.btn_remove_attachment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attachment_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments[position]

        // Set image based on type
        if (attachment.type == AttachmentType.IMAGE) {
            if (attachment.uri != null) {
                holder.imageView.setImageURI(attachment.uri)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_file)
        }

        // Set remove button click listener
        holder.removeButton.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount() = attachments.size
}