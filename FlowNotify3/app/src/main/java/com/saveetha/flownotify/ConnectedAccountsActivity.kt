package com.saveetha.flownotify

import android.content.Intent
import android.content.SharedPreferences
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ConnectedAccountsActivity : AppCompatActivity() {

    // Account Status TextViews
    private lateinit var googleStatusText: TextView
    private lateinit var facebookStatusText: TextView
    private lateinit var twitterStatusText: TextView
    private lateinit var githubStatusText: TextView
    private lateinit var microsoftStatusText: TextView
    private lateinit var linkedinStatusText: TextView
    private lateinit var googleWorkspaceStatusText: TextView
    private lateinit var dropboxStatusText: TextView
    private lateinit var slackStatusText: TextView
    private lateinit var trelloStatusText: TextView

    // Action Buttons
    private lateinit var googleActionButton: Button
    private lateinit var facebookActionButton: Button
    private lateinit var twitterActionButton: Button
    private lateinit var githubActionButton: Button
    private lateinit var microsoftActionButton: Button
    private lateinit var linkedinActionButton: Button
    private lateinit var googleWorkspaceActionButton: Button
    private lateinit var dropboxActionButton: Button
    private lateinit var slackActionButton: Button
    private lateinit var trelloActionButton: Button

    // Preferences
    private lateinit var preferences: SharedPreferences

    // Account connection status
    private data class AccountStatus(
        var isConnected: Boolean = false,
        var accountName: String = "",
        var connectionDate: String = ""
    )

    private val accounts = mutableMapOf<String, AccountStatus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected_accounts)

        preferences = getSharedPreferences("connected_accounts", MODE_PRIVATE)

        initViews()
        initAccountData()
        setupListeners()
    }

    private fun initViews() {
        // Status TextViews
        googleStatusText = findViewById(R.id.tv_google_status)
        facebookStatusText = findViewById(R.id.tv_facebook_status)
        twitterStatusText = findViewById(R.id.tv_twitter_status)
        githubStatusText = findViewById(R.id.tv_github_status)
        microsoftStatusText = findViewById(R.id.tv_microsoft_status)
        linkedinStatusText = findViewById(R.id.tv_linkedin_status)
        googleWorkspaceStatusText = findViewById(R.id.tv_google_workspace_status)
        dropboxStatusText = findViewById(R.id.tv_dropbox_status)
        slackStatusText = findViewById(R.id.tv_slack_status)
        trelloStatusText = findViewById(R.id.tv_trello_status)

        // Action Buttons
        googleActionButton = findViewById(R.id.btn_google_action)
        facebookActionButton = findViewById(R.id.btn_facebook_action)
        twitterActionButton = findViewById(R.id.btn_twitter_action)
        githubActionButton = findViewById(R.id.btn_github_action)
        microsoftActionButton = findViewById(R.id.btn_microsoft_action)
        linkedinActionButton = findViewById(R.id.btn_linkedin_action)
        googleWorkspaceActionButton = findViewById(R.id.btn_google_workspace_action)
        dropboxActionButton = findViewById(R.id.btn_dropbox_action)
        slackActionButton = findViewById(R.id.btn_slack_action)
        trelloActionButton = findViewById(R.id.btn_trello_action)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            onBackPressed()
        }

        // View all activity
        findViewById<TextView>(R.id.tv_view_all_activity).setOnClickListener {
            showAllActivityDialog()
        }
    }

    private fun initAccountData() {
        // Initialize with default values, then load from preferences
        accounts["google"] = AccountStatus()
        accounts["facebook"] = AccountStatus()
        accounts["twitter"] = AccountStatus()
        accounts["github"] = AccountStatus()
        accounts["microsoft"] = AccountStatus()
        accounts["linkedin"] = AccountStatus()
        accounts["googleWorkspace"] = AccountStatus()
        accounts["dropbox"] = AccountStatus()
        accounts["slack"] = AccountStatus()
        accounts["trello"] = AccountStatus()

        // Load account data from preferences or set defaults based on YUVARAJC14 user

        // For this sample, we'll hard-code some connected accounts for YUVARAJC14
        // In a real app, this would be loaded from your backend

        // Google
        accounts["google"]?.isConnected = true
        accounts["google"]?.accountName = "yuvaraj@gmail.com"
        accounts["google"]?.connectionDate = "2025-07-15 09:23:45"

        // Facebook - not connected

        // Twitter
        accounts["twitter"]?.isConnected = true
        accounts["twitter"]?.accountName = "@yuvarajc14"
        accounts["twitter"]?.connectionDate = "2025-06-22 14:10:33"

        // GitHub - connected most recently
        accounts["github"]?.isConnected = true
        accounts["github"]?.accountName = "YUVARAJC14"
        accounts["github"]?.connectionDate = "2025-08-08 05:07:00" // Current session

        // Microsoft - not connected

        // LinkedIn
        accounts["linkedin"]?.isConnected = true
        accounts["linkedin"]?.accountName = "Yuvaraj C"
        accounts["linkedin"]?.connectionDate = "2025-08-07 15:22:18"

        // Google Workspace
        accounts["googleWorkspace"]?.isConnected = true
        accounts["googleWorkspace"]?.accountName = "yuvaraj@saveetha.com"
        accounts["googleWorkspace"]?.connectionDate = "2025-07-20 08:45:12"

        // Dropbox - not connected

        // Slack
        accounts["slack"]?.isConnected = true
        accounts["slack"]?.accountName = "2 workspaces"
        accounts["slack"]?.connectionDate = "2025-08-05 09:45:30"

        // Trello - not connected

        updateUI()
    }

    private fun updateUI() {
        // Update status text and button for each account based on connection status
        updateAccountUI("google", googleStatusText, googleActionButton)
        updateAccountUI("facebook", facebookStatusText, facebookActionButton)
        updateAccountUI("twitter", twitterStatusText, twitterActionButton)
        updateAccountUI("github", githubStatusText, githubActionButton)
        updateAccountUI("microsoft", microsoftStatusText, microsoftActionButton)
        updateAccountUI("linkedin", linkedinStatusText, linkedinActionButton)
        updateAccountUI("googleWorkspace", googleWorkspaceStatusText, googleWorkspaceActionButton)
        updateAccountUI("dropbox", dropboxStatusText, dropboxActionButton)
        updateAccountUI("slack", slackStatusText, slackActionButton)
        updateAccountUI("trello", trelloStatusText, trelloActionButton)
    }

    private fun updateAccountUI(accountKey: String, statusText: TextView, actionButton: Button) {
        val account = accounts[accountKey] ?: return

        if (account.isConnected) {
            // Connected account UI
            statusText.text = "Connected as ${account.accountName}"
            statusText.setTextColor(getColor(R.color.green))

            if (accountKey == "slack") {
                actionButton.text = "Manage"
                actionButton.setTextColor(getColor(R.color.primary_blue))
            } else {
                actionButton.text = "Disconnect"
                actionButton.setTextColor(getColor(R.color.red))
            }
        } else {
            // Not connected account UI
            statusText.text = "Not connected"
            statusText.setTextColor(getColor(R.color.gray))
            actionButton.text = "Connect"
            actionButton.setTextColor(getColor(R.color.primary_blue))
        }
    }

    private fun setupListeners() {
        // Set up listeners for all action buttons
        googleActionButton.setOnClickListener { toggleConnection("google", "yuvaraj@gmail.com") }
        facebookActionButton.setOnClickListener { toggleConnection("facebook", "Yuvaraj C") }
        twitterActionButton.setOnClickListener { toggleConnection("twitter", "@yuvarajc14") }
        githubActionButton.setOnClickListener { toggleConnection("github", "YUVARAJC14") }
        microsoftActionButton.setOnClickListener { toggleConnection("microsoft", "yuvaraj@outlook.com") }
        linkedinActionButton.setOnClickListener { toggleConnection("linkedin", "Yuvaraj C") }
        googleWorkspaceActionButton.setOnClickListener { toggleConnection("googleWorkspace", "yuvaraj@saveetha.com") }
        dropboxActionButton.setOnClickListener { toggleConnection("dropbox", "yuvaraj@gmail.com") }

        // Special cases
        slackActionButton.setOnClickListener {
            if (accounts["slack"]?.isConnected == true) {
                showSlackWorkspacesDialog()
            } else {
                toggleConnection("slack", "2 workspaces")
            }
        }

        trelloActionButton.setOnClickListener { toggleConnection("trello", "YUVARAJC14") }
    }

    private fun toggleConnection(accountKey: String, accountName: String) {
        val account = accounts[accountKey] ?: return

        if (account.isConnected) {
            // Disconnect account
            showDisconnectConfirmation(accountKey, accountName)
        } else {
            // Connect account - in a real app, this would start the OAuth flow
            showConnectDialog(accountKey, accountName)
        }
    }

    private fun showConnectDialog(accountKey: String, accountName: String) {
        val accountDisplayName = getAccountDisplayName(accountKey)

        AlertDialog.Builder(this)
            .setTitle("Connect to $accountDisplayName")
            .setMessage("You'll be redirected to $accountDisplayName to authorize access to your account. Would you like to continue?")
            .setPositiveButton("Connect") { _, _ ->
                // Simulate successful connection
                simulateAccountConnection(accountKey, accountName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDisconnectConfirmation(accountKey: String, accountName: String) {
        val accountDisplayName = getAccountDisplayName(accountKey)

        AlertDialog.Builder(this)
            .setTitle("Disconnect from $accountDisplayName")
            .setMessage("Are you sure you want to disconnect from $accountDisplayName? You'll need to reconnect to use features that require this account.")
            .setPositiveButton("Disconnect") { _, _ ->
                // Disconnect account
                accounts[accountKey]?.isConnected = false
                accounts[accountKey]?.accountName = ""
                updateUI()

                Toast.makeText(this, "$accountDisplayName account disconnected", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSlackWorkspacesDialog() {
        val workspaces = arrayOf("Saveetha Engineering Team", "Project X Collaboration")

        AlertDialog.Builder(this)
            .setTitle("Connected Slack Workspaces")
            .setItems(workspaces) { _, which ->
                val workspace = workspaces[which]
                showSlackWorkspaceOptions(workspace)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSlackWorkspaceOptions(workspace: String) {
        val options = arrayOf("Open in Slack", "Disconnect from workspace")

        AlertDialog.Builder(this)
            .setTitle(workspace)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openUrl("https://slack.com/")
                    1 -> {
                        Toast.makeText(this, "Disconnected from $workspace", Toast.LENGTH_SHORT).show()
                        // In a real app, you'd remove just this workspace, not all
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAllActivityDialog() {
        val activityList = StringBuilder()

        activityList.append("GitHub connected on Aug 8, 2025 at 05:07\n\n")
        activityList.append("LinkedIn connected on Aug 7, 2025 at 15:22\n\n")
        activityList.append("Slack connected on Aug 5, 2025 at 09:45\n\n")
        activityList.append("Facebook disconnected on Aug 2, 2025 at 17:31\n\n")
        activityList.append("Google Workspace connected on Jul 20, 2025 at 08:45\n\n")
        activityList.append("Google connected on Jul 15, 2025 at 09:23\n\n")
        activityList.append("Twitter connected on Jun 22, 2025 at 14:10")

        AlertDialog.Builder(this)
            .setTitle("Connection Activity")
            .setMessage(activityList.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun simulateAccountConnection(accountKey: String, accountName: String) {
        // In a real app, this would be the callback from OAuth
        val currentDateTime = "2025-08-08 05:16:47" // Current timestamp from parameters

        accounts[accountKey]?.isConnected = true
        accounts[accountKey]?.accountName = accountName
        accounts[accountKey]?.connectionDate = currentDateTime

        updateUI()

        val accountDisplayName = getAccountDisplayName(accountKey)
        Toast.makeText(this, "$accountDisplayName account connected", Toast.LENGTH_SHORT).show()
    }

    private fun getAccountDisplayName(accountKey: String): String {
        return when (accountKey) {
            "google" -> "Google"
            "facebook" -> "Facebook"
            "twitter" -> "Twitter/X"
            "github" -> "GitHub"
            "microsoft" -> "Microsoft"
            "linkedin" -> "LinkedIn"
            "googleWorkspace" -> "Google Workspace"
            "dropbox" -> "Dropbox"
            "slack" -> "Slack"
            "trello" -> "Trello"
            else -> accountKey.capitalize(Locale.ROOT)
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open URL", Toast.LENGTH_SHORT).show()
        }
    }
}