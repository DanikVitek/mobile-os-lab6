package me.danikvitek.lab6.receiver


import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.danikvitek.lab6.Lab6Application
import me.danikvitek.lab6.R
import me.danikvitek.lab6.activity.BASE_URI
import me.danikvitek.lab6.activity.MainActivity

@Deprecated("Use AlarmManager instead of WorkManager alongside AlarmReceiver")
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val id = inputData.getLong(PARAM_ID, 0)
        val title = inputData.getString(PARAM_TITLE)!!
        val text = inputData.getString(PARAM_TEXT)!!
        val timestamp = inputData.getLong(PARAM_TIMESTAMP, 0)

        val intent = Intent(
            Intent.ACTION_VIEW,
            "$BASE_URI/reminder/${id}".toUri(),
            applicationContext,
            MainActivity::class.java,
        )
        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntent(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = Notification.Builder(applicationContext, Lab6Application.CHANNEL_ID)
            .setSmallIcon(R.drawable.reminder)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = PermissionChecker.checkPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS,
                Process.myPid(),
                Process.myUid(),
                null,
            )
            if (status != PermissionChecker.PERMISSION_GRANTED) {
                return Result.failure()
            }
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id.toInt(), notification)
        }

        return Result.success()
    }

    companion object {
        const val PARAM_ID = "ID"
        const val PARAM_TITLE = "TITLE"
        const val PARAM_TEXT = "TEXT"
        const val PARAM_TIMESTAMP = "TIMESTAMP"
    }
}