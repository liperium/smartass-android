package com.liara.smartass.static_objects

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.liara.smartass.MainActivity
import com.liara.smartass.R
import com.liara.smartass.data.Importance


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("NormalNotificationReceiver", "Received notification")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var agendaItemTitle = "Notification Title"
        var agendaItemDescription = "Notification Description"
        var importance = Importance.Basic
        var uid = -1
        intent.extras?.let {
            agendaItemTitle = it.getString("title").toString()
            agendaItemDescription = it.getString("description").toString()
            importance = Importance.fromInt(it.getInt("importance"))
            uid = it.getInt("id")
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", agendaItemTitle)
            putExtra("description", agendaItemDescription)
            putExtra("importance", importance.value)
            putExtra("id", uid)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            uid,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, importance.notificationChannel())
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(agendaItemTitle)
            .setContentText(agendaItemDescription).setContentIntent(pendingIntent)
        if (importance == Importance.Important) {
            builder.apply {
                setOngoing(true)
            }
        }

        builder.setDefaults(Notification.DEFAULT_VIBRATE)

        notificationManager.notify(uid, builder.build())
    }
}