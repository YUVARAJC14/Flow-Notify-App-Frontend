package com.saveetha.flownotify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a new notification."

        val notificationHelper = NotificationHelper(context)
        notificationHelper.createNotificationChannel()
        notificationHelper.showNotification(title, message)
    }
}