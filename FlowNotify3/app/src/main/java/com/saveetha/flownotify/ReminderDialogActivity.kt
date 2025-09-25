package com.saveetha.flownotify

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.saveetha.flownotify.network.ApiClient
import com.saveetha.flownotify.network.ApiService
import com.saveetha.flownotify.network.EventUpdateRequest
import kotlinx.coroutines.launch

class ReminderDialogActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiClient.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_dialog)

        val title = intent.getStringExtra("title")
        val type = intent.getStringExtra("type")
        val id = intent.getStringExtra("id")

        findViewById<TextView>(R.id.tv_reminder_title).text = type
        findViewById<TextView>(R.id.tv_reminder_message).text = title

        findViewById<Button>(R.id.btn_dismiss).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_mark_complete).setOnClickListener {
            if (id != null) {
                if (type == "Task") {
                    markTaskAsComplete(id)
                } else {
                    markEventAsComplete(id)
                }
            }
            finish()
        }
    }

    private fun markTaskAsComplete(taskId: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.updateTask(taskId, mapOf("isCompleted" to true))
                if (!response.isSuccessful) {
                    showError("Failed to update task")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun markEventAsComplete(eventId: String) {
        lifecycleScope.launch {
            try {
                val eventIdInt = eventId.toIntOrNull()
                if (eventIdInt == null) {
                    showError("Invalid event ID")
                    return@launch
                }
                val request = EventUpdateRequest(completed = true)
                val response = apiService.updateEvent(eventIdInt, request)
                if (!response.isSuccessful) {
                    showError("Failed to update event")
                }
            } catch (e: Exception) {
                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
