package com.saveetha.flownotify

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class FlowNotifyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme", "system")

        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
