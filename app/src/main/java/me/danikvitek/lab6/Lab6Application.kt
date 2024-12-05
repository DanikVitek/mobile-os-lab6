package me.danikvitek.lab6

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Lab6Application : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = run {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "reminders"
    }
}
