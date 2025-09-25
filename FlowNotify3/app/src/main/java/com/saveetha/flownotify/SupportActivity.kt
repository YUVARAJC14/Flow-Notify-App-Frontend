package com.saveetha.flownotify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SupportActivity : AppCompatActivity() {

    private lateinit var noTicketsLayout: LinearLayout
    private lateinit var activeTicketsLayout: LinearLayout
    private lateinit var liveChatStatusText: TextView
    private lateinit var startChatButton: Button

    // Sample data
    private var hasActiveTickets: Boolean = true
    private var isLiveChatAvailable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        initViews()
        setupListeners()
        checkSupportAvailability()
    }

    private fun initViews() {
        // Ticket layouts
        noTicketsLayout = findViewById(R.id.layout_no_tickets)
        activeTicketsLayout = findViewById(R.id.layout_active_tickets)

        // Live chat status
        liveChatStatusText = findViewById(R.id.tv_live_chat_status)
        startChatButton = findViewById(R.id.btn_start_chat)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // Quick Actions
        findViewById<LinearLayout>(R.id.action_contact_support).setOnClickListener {
            showContactSupportOptions()
        }

        findViewById<LinearLayout>(R.id.action_report_issue).setOnClickListener {
            showReportIssueDialog()
        }

        findViewById<LinearLayout>(R.id.action_faq).setOnClickListener {
            navigateToFAQ()
        }

        // Support Tickets
        findViewById<LinearLayout>(R.id.ticket_item_1).setOnClickListener {
            navigateToTicketDetails("12345")
        }

        findViewById<LinearLayout>(R.id.ticket_item_2).setOnClickListener {
            navigateToTicketDetails("12346")
        }

        findViewById<Button>(R.id.btn_view_all_tickets).setOnClickListener {
            navigateToAllTickets()
        }

        // Help Resources
        findViewById<LinearLayout>(R.id.resource_user_guide).setOnClickListener {
            navigateToUserGuide()
        }

        findViewById<LinearLayout>(R.id.resource_video_tutorials).setOnClickListener {
            navigateToVideoTutorials()
        }

        findViewById<LinearLayout>(R.id.resource_knowledge_base).setOnClickListener {
            navigateToKnowledgeBase()
        }

        findViewById<LinearLayout>(R.id.resource_community_forum).setOnClickListener {
            navigateToCommunityForum()
        }

        // Contact Information
        findViewById<LinearLayout>(R.id.contact_email).setOnClickListener {
            val email = "support@flownotify.com"
            composeEmail(email)
        }

        findViewById<LinearLayout>(R.id.contact_email)
            .findViewById<ImageButton>(R.id.btn_copy_email)
            .setOnClickListener {
                copyToClipboard("Email", "support@flownotify.com")
            }

        findViewById<LinearLayout>(R.id.contact_phone).setOnClickListener {
            val phone = "+18001234567"
            dialPhoneNumber(phone)
        }

        findViewById<LinearLayout>(R.id.contact_phone).setOnClickListener {
            val phone = "+18001234567"
            dialPhoneNumber(phone)
        }

        // Live Chat
        findViewById<Button>(R.id.btn_start_chat).setOnClickListener {
            startLiveChat()
        }
    }

    private fun checkSupportAvailability() {
        // Update ticket visibility based on whether user has active tickets
        if (hasActiveTickets) {
            noTicketsLayout.visibility = View.GONE
            activeTicketsLayout.visibility = View.VISIBLE
        } else {
            noTicketsLayout.visibility = View.VISIBLE
            activeTicketsLayout.visibility = View.GONE
        }

        // Check if live chat is available based on current time
        val currentDateTime = "2025-08-08 07:23:56" // From the input parameters
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val date = dateFormat.parse(currentDateTime)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date

                // Convert UTC to EST for support hours check
                val hour = calendar.get(Calendar.HOUR_OF_DAY) - 5 // UTC to EST conversion (simplified)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                // Check if within support hours (9AM-5PM EST, Monday-Friday)
                isLiveChatAvailable = hour >= 9 && hour < 17 &&
                        dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Default to available if there's an error parsing the date
            isLiveChatAvailable = true
        }

        // Update the UI based on availability
        if (isLiveChatAvailable) {
            liveChatStatusText.text = "Available Now"
            liveChatStatusText.setTextColor(resources.getColor(R.color.green, theme))
            startChatButton.isEnabled = true
        } else {
            liveChatStatusText.text = "Unavailable (9AM-5PM EST, Mon-Fri)"
            liveChatStatusText.setTextColor(resources.getColor(R.color.red, theme))
            startChatButton.isEnabled = false
        }
    }

    private fun showContactSupportOptions() {
        val options = arrayOf("Email Support", "Phone Support", "Live Chat")

        AlertDialog.Builder(this)
            .setTitle("Contact Support")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> composeEmail("support@flownotify.com")
                    1 -> dialPhoneNumber("+18001234567")
                    2 -> startLiveChat()
                }
            }
            .show()
    }

    private fun showReportIssueDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_report_issue, null)

        AlertDialog.Builder(this)
            .setTitle("Report Issue")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                // In a real app, this would submit the issue report
                Toast.makeText(this, "Issue report submitted", Toast.LENGTH_SHORT).show()

                // Update the active tickets to show the new ticket
                hasActiveTickets = true
                checkSupportAvailability()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToFAQ() {
        // In a real app, this would navigate to the FAQ screen
        Toast.makeText(this, "Navigating to FAQ", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTicketDetails(ticketId: String) {
        // In a real app, this would navigate to the ticket details screen
        Toast.makeText(this, "Viewing ticket #$ticketId", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllTickets() {
        // In a real app, this would navigate to the all tickets screen
        Toast.makeText(this, "Viewing all tickets", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToUserGuide() {
        openUrl("https://www.flownotify.com/help/user-guide")
    }

    private fun navigateToVideoTutorials() {
        openUrl("https://www.flownotify.com/help/video-tutorials")
    }

    private fun navigateToKnowledgeBase() {
        openUrl("https://www.flownotify.com/help/knowledge-base")
    }

    private fun navigateToCommunityForum() {
        openUrl("https://community.flownotify.com")
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open URL", Toast.LENGTH_SHORT).show()
        }
    }

    private fun composeEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request from YUVARAJC14")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                copyToClipboard("Email", email)
                Toast.makeText(this, "No email app found. Email address copied to clipboard.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            copyToClipboard("Email", email)
            Toast.makeText(this, "Unable to launch email app. Email address copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }

    private fun dialPhoneNumber(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard("Phone", phone)
            Toast.makeText(this, "Unable to dial. Phone number copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun startLiveChat() {
        if (!isLiveChatAvailable) {
            Toast.makeText(this, "Live chat is currently unavailable. Please try during support hours (9AM-5PM EST, Monday-Friday).", Toast.LENGTH_LONG).show()
            return
        }

        // In a real app, this would open the live chat interface
        AlertDialog.Builder(this)
            .setTitle("Live Chat")
            .setMessage("Connecting to a support agent. Please wait...")
            .setPositiveButton("OK", null)
            .show()
    }
}