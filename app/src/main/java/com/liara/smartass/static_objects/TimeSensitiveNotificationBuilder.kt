package com.liara.smartass.static_objects

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.liara.smartass.AppDatabase
import com.liara.smartass.static_objects.TimeSensitiveNotificationBuilder.makeNotificationForDay
import java.time.LocalDate

object TimeSensitiveNotificationBuilder {
    fun makeNotificationForDay(localDate: LocalDate, context: Context){
        val database = AppDatabase.getInstance(context)

        val items = database.recurringItemAtDateDao().getFromDate(localDate)
            .map { recurringItemAtDate ->
                recurringItemAtDate.getView(
                    database.recurringItemDao(),
                    localDate
                )
            }
        StoredNotificationManager.updateNotificationForItems(items,context)
    }

    fun updateNotificationsNow(appContext: Context) {
        makeNotificationForDay(LocalDate.now(), appContext)
        makeNotificationForDay(LocalDate.now().apply { plusDays(1) }, appContext)
    }
}
class DailyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    val TAG = "DailyWorker"
    override fun doWork(): Result {
        makeNotificationForDay(LocalDate.now().apply { plusDays(1) }, applicationContext)

        // Your task logic here
        Log.d(TAG, "Task executed")
        return Result.success()
    }
}