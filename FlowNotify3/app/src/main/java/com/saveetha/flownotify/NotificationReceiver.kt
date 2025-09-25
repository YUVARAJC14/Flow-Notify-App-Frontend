package com.saveetha.flownotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val dialogIntent = Intent(context, ReminderDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", intent.getStringExtra("title"))
            putExtra("type", intent.getStringExtra("type"))
            putExtra("id", intent.getStringExtra("id"))
        }
        context.startActivity(dialogIntent)
    }
}
