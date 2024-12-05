package me.danikvitek.lab6.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.danikvitek.lab6.data.dao.ReminderDao
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderDao: ReminderDao

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(BootReceiver::class.simpleName, "onReceive")

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.i(BootReceiver::class.simpleName, "ACTION_BOOT_COMPLETED")

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        runBlocking(Dispatchers.IO) {
            reminderDao.getAll().first().forEach {
                Log.d(BootReceiver::class.simpleName, "Restarting alarm for Reminder(id=${it.id})")

                val timestamp = it.datetime.time
                val pendingIntent = AlarmReceiver.constructPendingIntent(
                    context,
                    it.id,
                    it.title,
                    it.text,
                    timestamp,
                )

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timestamp,
                    pendingIntent,
                )

                Log.i(BootReceiver::class.simpleName, "Restarted alarm for Reminder(id=${it.id})")
            }
        }

        Log.i(BootReceiver::class.simpleName, "Done restarting alarms")
    }
}