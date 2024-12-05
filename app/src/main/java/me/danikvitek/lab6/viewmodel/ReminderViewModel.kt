package me.danikvitek.lab6.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.danikvitek.lab6.data.dao.ReminderDao
import me.danikvitek.lab6.di.WithTransaction
import me.danikvitek.lab6.receiver.AlarmReceiver
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val app: Application,
    private val reminderDao: ReminderDao,
    private val withTransaction: WithTransaction,
) : ViewModel() {
    private val alarmManager = app.getSystemService(AlarmManager::class.java)

    fun getReminder(id: Long) = reminderDao.getById(id)

    fun deleteReminder(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        withTransaction {
            Log.d(ReminderViewModel::class.simpleName, "Starting delete reminder transaction")
            reminderDao.getById(id).first()?.let { reminder ->
                Log.d(ReminderViewModel::class.simpleName, "Deleting reminder $id")
                reminderDao.deleteById(id)
                Log.i(ReminderViewModel::class.simpleName, "Deleted Reminder(id=$id)")

                alarmManager.cancel(
                    AlarmReceiver.constructPendingIntent(
                        app,
                        id,
                        reminder.title,
                        reminder.text,
                        reminder.datetime.time
                    )
                )
                Log.i(ReminderViewModel::class.simpleName, "Cancelled alarm for Reminder(id=$id)")
            }
        }
    }

}