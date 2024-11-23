package com.liara.smartass.static_objects

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liara.smartass.AppDatabase
import com.liara.smartass.MainActivity
import com.liara.smartass.data.AgendaItem
import com.liara.smartass.data.AgendaItemInterface
import com.liara.smartass.data.AgendaState
import com.liara.smartass.data.Importance
import com.liara.smartass.data.StoredNotification
import com.liara.smartass.data.StoredNotificationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant


object StoredNotificationManager {
    private const val TAG = "StoredNotificationManager"
    private lateinit var alarmManager: AlarmManager
    private suspend fun updateNotification(agendaItem: AgendaItemInterface, context: Context) {
        withContext(Dispatchers.IO) {
            val enabledConfigs =
                UserNotificationConfigPreferences(context).getAllConfigs().filter { it.enabled }
            // Check if notification already is in db
            // If so override it, update db

            // If not, check if future and add to db
            val storedNotificationDao: StoredNotificationDao = MainActivity.storedNotificationDao
            val notificationType = agendaItem.getItemImportance()

            val intent = Intent(context, NotificationReceiver::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // We add uid to the intent, so the receiver can find the item
            intent.putExtra("title", agendaItem.title)
            intent.putExtra("description", agendaItem.description)
            intent.putExtra("importance", notificationType.value)

            // Check if the intent is already in the system, and if the item is in the future
            var storedNotification =
                storedNotificationDao.getAssociatedNotification(agendaItem.getUniqueString())

            // If nothing to update in database and notification is passed, then nothing
            if (storedNotification == null && agendaItem.isPassed()) {
                return@withContext
            }
            // Creates a stored notification if needed
            if (storedNotification == null && agendaItem.isFuture()) {
                val helper = StoredNotification(0, agendaItem.getUniqueString())
                val newUid: Int = storedNotificationDao.insertAll(helper)[0].toInt()
                storedNotification = helper.copy(uid = newUid)
            }

            val pendingIntents = enabledConfigs.map {
                Pair(
                    PendingIntent.getBroadcast(
                        context,
                        storedNotification!!.uid * it.seed,
                        intent,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    ), it
                )
            }

            // If agendaItem is PASSED (maybe from update ), and is registered in DB
            if (agendaItem.isPassed() || agendaItem.done) {
                for (pendingIntent in pendingIntents) {
                    alarmManager.cancel(pendingIntent.first)
                }
                // Item was un-registered
                if (storedNotification != null) {
                    storedNotificationDao.delete(storedNotification)
                }
                Log.d(
                    TAG,
                    "Cancelling Alarm for ${agendaItem.title} | ${agendaItem.getUniqueString()}"
                )
            } else if (agendaItem.isFuture()) {
                // We add the uid to the pending intent, so we can update the notification if needed
                //using the devices zone and offset
                for (pendingIntent in pendingIntents) {
                    val config = pendingIntent.second
                    val notificationBaseTime: Instant = when (config.fromWhen) {
                        NotificationTime.Start -> agendaItem.getStartNotificationInstant()
                        NotificationTime.End -> agendaItem.getEndNotificationInstant()
                    }
                    val notificationCallTime = notificationBaseTime.plusSeconds(config.instantDiff)
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationCallTime.toEpochMilli(),
                        pendingIntent.first
                    )
                }

                // Item was registered
                Log.d(TAG, "Adding Alarm for ${agendaItem.title} | ${agendaItem.getUniqueString()}")
            }
        }
    }

    // Adds notifications for all items in the list, force will remove the check if the item is in the future. Necessary for updating items.
    fun updateNotificationForItems(
        agendaItem: Iterable<AgendaItemInterface>, context: Context
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            var count = 0
            for (item in agendaItem) {
                updateNotification(item, context)
                count++
            }
            if (count > 0) Log.d(TAG, "Managed $count notifications")
        }
    }

    fun testNotification(context: Context, importance: Importance = Importance.Basic) {
        val item = AgendaItem(
            -1,
            "Test Notification",
            "Test Desc",
            Instant.now(),
            Instant.now().plusSeconds(5),
            importance,
            AgendaState.Upcoming,
            false,
        )

        Toast.makeText(
            context, "Test notification should call at : ${item.endInstant}", Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "Test notification should call at : ${item.endInstant}")
        GlobalScope.launch(Dispatchers.Main) {
            updateNotification(item, context)
        }
    }

    fun removeAllNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll()
        } else {
            VisualFeedbackManager.showMessage("Can't cancel all notifications, device is too old")
            Log.e(TAG, "Can't cancel all notifications, device is too old")
        }
    }

    fun initVars(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val importance = NotificationManager.IMPORTANCE_HIGH
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        val allChannels = arrayOfNulls<NotificationChannel>(Importance.entries.size)
        //
        Importance.entries.forEachIndexed { index, it ->
            val channelName = "Importance: ${it.toFrench()}"
            val channelId = it.notificationChannel()
            val channel = when (it) {
                Importance.Basic -> NotificationChannel(channelId, channelName, importance).apply {
                    vibrationPattern = (longArrayOf(500))
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        audioAttributes
                    )
                }

                Importance.Moderate -> NotificationChannel(
                    channelId,
                    channelName,
                    importance
                ).apply {
                    vibrationPattern = (longArrayOf(500))
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        audioAttributes
                    )
                }

                Importance.Important -> {
                    audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM).build()
                    NotificationChannel(channelId, channelName, importance).apply {
                        enableLights(true)
                        vibrationPattern =
                            (longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500))
                        setSound(
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                            audioAttributes
                        )
                    }
                }
            }
            channel.vibrationPattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)

            allChannels[index] = channel
        }
        // Important Channel
        allChannels.forEach {
            if (it != null) {
                notificationManager.createNotificationChannel(it)
            }
        }

    }

    fun updateAllNotifications(appContext: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val agendaItems = AppDatabase.getInstance(appContext).agendaItemDao().getAll()
            updateNotificationForItems(agendaItems, appContext)
            TimeSensitiveNotificationBuilder.updateNotificationsNow(appContext)
        }
    }

    enum class NotificationTime {
        Start, End
    }

    data class UserNotificationConfig(
        val name: String,
        val fromWhen: NotificationTime,
        val instantDiff: Long, // in seconds, minus is before, plus is after
        val seed: Int, //TODO could be a bug in the future
        var enabled: Boolean = true,
    ) {
        companion object {
            fun getBaseConfigs(): List<UserNotificationConfig> {
                return listOf(
                    UserNotificationConfig("Début", NotificationTime.Start, 0, 421),
                    UserNotificationConfig("Fin", NotificationTime.End, 0, 419),
                )
            }
        }
    }

    class UserNotificationConfigPreferences(private val context: Context) {
        private val gson = Gson()
        private val sharedPreferences: SharedPreferences by lazy {
            context.getSharedPreferences("NotificationPreferences", Context.MODE_PRIVATE)
        }

        fun saveConfigs(configurations: List<UserNotificationConfig>) {
            val json = gson.toJson(configurations)
            sharedPreferences.edit().putString("configurations", json).apply()
        }

        fun getAllConfigs(): List<UserNotificationConfig> {
            val json = sharedPreferences.getString("configurations", null)
            return if (json != null) {
                val type = object : TypeToken<List<UserNotificationConfig>>() {}.type
                gson.fromJson(json, type)
            } else {
                UserNotificationConfig.getBaseConfigs()
            }
        }
    }
}