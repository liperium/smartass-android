package com.liara.smartass

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liara.smartass.data.InstantConverter
import com.liara.smartass.data.LoginInfo
import com.liara.smartass.data.LoginInfoDao
import com.liara.smartass.static_objects.Converters

@Database(entities = [LoginInfo::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class, InstantConverter::class)
abstract class LocalDatabase : RoomDatabase() {
  abstract fun loginInfoDao(): LoginInfoDao

  companion object {
    private const val TAG = "LocalDatabase"
      private const val DATABASE_NAME = "local_db"

    @Volatile private var INSTANCE: LocalDatabase? = null

    /*---------------------Create one (only one) instance of the Database--------------------------*/

    fun getInstance(context: Context): LocalDatabase =
        INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }
    private fun buildDatabase(context: Context) =
        Room.databaseBuilder(context.applicationContext, LocalDatabase::class.java, DATABASE_NAME)
            // Delete Database, when something changed
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    fun closeDatabase() {
      getInstance(MainActivity.appContext).close()
      INSTANCE = null
      Log.v(TAG, "closed")
    }
  }
}
