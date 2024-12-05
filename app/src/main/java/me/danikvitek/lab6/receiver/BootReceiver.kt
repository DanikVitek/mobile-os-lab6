package me.danikvitek.lab6.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.danikvitek.lab6.data.dao.ReminderDao
import javax.inject.Inject

class BootReceiver @Inject constructor(
    private val reminderDao: ReminderDao,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        TODO("Fetch reminders from database and schedule them")
        val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, 0)
        val title = intent.getStringExtra(AlarmReceiver.EXTRA_TITLE)!!
        val text = intent.getStringExtra(AlarmReceiver.EXTRA_TEXT)!!
        val timestamp = intent.getLongExtra(AlarmReceiver.EXTRA_TIMESTAMP, 0)

        val pendingIntent = AlarmReceiver.constructPendingIntent(
            context,
            id,
            title,
            text,
            timestamp,
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timestamp,
            pendingIntent,
        )
    }
}