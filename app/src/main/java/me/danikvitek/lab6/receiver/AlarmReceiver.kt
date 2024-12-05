package me.danikvitek.lab6.receiver

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import me.danikvitek.lab6.Lab6Application
import me.danikvitek.lab6.R
import me.danikvitek.lab6.activity.BASE_URI
import me.danikvitek.lab6.activity.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(AlarmReceiver::class.simpleName, "onReceive")

        val id = intent.getLongExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE)!!
        val text = intent.getStringExtra(EXTRA_TEXT)!!
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0)
        Log.d(
            AlarmReceiver::class.simpleName,
            "id=$id, title=$title, text=$text, timestamp=$timestamp"
        )

        val viewNotificationIntent = Intent(
            Intent.ACTION_VIEW,
            "$BASE_URI/reminder/${id}".toUri(),
            context,
            MainActivity::class.java,
        )

        val viewNotificationPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntent(viewNotificationIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = Notification.Builder(context, Lab6Application.CHANNEL_ID)
            .setSmallIcon(R.drawable.reminder)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setContentTitle(title)
            .run {
                if (text.isNotBlank()) this
                    .setContentText(text)
                    .setStyle(Notification.BigTextStyle().bigText(text))
                else this
            }
            .setContentIntent(viewNotificationPendingIntent)
            .setAutoCancel(true)
            .build()
        Log.d(AlarmReceiver::class.simpleName, "notification=$notification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            )
            Log.d(AlarmReceiver::class.simpleName, "status=$status")
            if (status != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        with(NotificationManagerCompat.from(context)) {
            notify(id.toInt(), notification)
        }
        Log.d(AlarmReceiver::class.simpleName, "Notification sent")
    }

    companion object {
        const val EXTRA_ID = "ID"
        const val EXTRA_TITLE = "TITLE"
        const val EXTRA_TEXT = "TEXT"
        const val EXTRA_TIMESTAMP = "TIMESTAMP"

        fun constructPendingIntent(
            context: Context,
            id: Long,
            title: String,
            text: String,
            timestamp: Long,
        ) = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_TIMESTAMP, timestamp)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )!!
    }
}