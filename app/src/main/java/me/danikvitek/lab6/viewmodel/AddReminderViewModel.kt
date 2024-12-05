package me.danikvitek.lab6.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import me.danikvitek.lab6.data.dao.ReminderDao
import me.danikvitek.lab6.di.WithTransaction
import me.danikvitek.lab6.receiver.AlarmReceiver
import me.danikvitek.lab6.receiver.BootReceiver
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val app: Application,
    private val reminderDao: ReminderDao,
    private val withTransaction: WithTransaction,
) : ViewModel() {
    private val alarmManager = app.getSystemService(AlarmManager::class.java)

    fun addReminder(title: String, text: String, datetime: Date): Flow<Long> {
        return channelFlow {
            withTransaction {
                reminderDao.insert(title, text, datetime)
                val id = reminderDao.getLastAdded()!!.id

                val pendingIntent = AlarmReceiver.constructPendingIntent(
                    app,
                    id,
                    title,
                    text,
                    datetime.time,
                )

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    datetime.time,
                    pendingIntent,
                )
                Log.i(AddReminderViewModel::class.simpleName, "Set alarm for Reminder(id=$id)")

                val bootReceiver = ComponentName(app, BootReceiver::class.java)
                app.packageManager.setComponentEnabledSetting(
                    bootReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
                Log.i(AddReminderViewModel::class.simpleName, "Enabled BootReceiver")

                send(id)
            }
        }.flowOn(Dispatchers.IO)
    }
}