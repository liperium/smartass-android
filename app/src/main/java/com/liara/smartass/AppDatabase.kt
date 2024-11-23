package com.liara.smartass

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liara.smartass.data.AgendaItem
import com.liara.smartass.data.AgendaItemDao
import com.liara.smartass.data.InstantConverter
import com.liara.smartass.data.LocalDateConverter
import com.liara.smartass.data.RRuleConverter
import com.liara.smartass.data.RecurringItem
import com.liara.smartass.data.RecurringItemAtDate
import com.liara.smartass.data.RecurringItemAtDateDao
import com.liara.smartass.data.RecurringItemDao
import com.liara.smartass.data.StoredNotification
import com.liara.smartass.data.StoredNotificationDao
import com.liara.smartass.static_objects.Converters

@Database(
  entities = [AgendaItem::class, RecurringItem::class, RecurringItemAtDate::class, StoredNotification::class],
  version = 17,
    exportSchema = false
)
@TypeConverters(
  Converters::class,
  InstantConverter::class,
  LocalDateConverter::class,
  RRuleConverter::class
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun agendaItemDao(): AgendaItemDao
  abstract fun recurringItemDao(): RecurringItemDao
  abstract fun recurringItemAtDateDao(): RecurringItemAtDateDao
  abstract fun storedNotificationDao(): StoredNotificationDao

  companion object {

    const val DATABASE_NAME = "hdp"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    /*---------------------Create one (only one) instance of the Database--------------------------*/

    fun getInstance(context: Context): AppDatabase =
      INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

    private fun buildDatabase(context: Context) =
      Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
        // Delete Database, when something changed
        .fallbackToDestructiveMigration()
        .build()

    fun closeDatabase() {
      getInstance(MainActivity.appContext).close()
      INSTANCE = null
    }
  }
}
