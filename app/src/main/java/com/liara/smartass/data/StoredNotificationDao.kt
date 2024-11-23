package com.liara.smartass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface StoredNotificationDao {
  @Query("SELECT * FROM stored_notification")
  fun getAll(): List<StoredNotification>

  @Query("SELECT * FROM stored_notification WHERE uid IN (:itemId)")
  fun getId(itemId: Int): StoredNotification
  @Query("SELECT * FROM stored_notification WHERE agendaItemId IN (:agendaItemId)")
  fun getAssociatedNotification(agendaItemId: String): StoredNotification?

  @Query("SELECT * FROM stored_notification WHERE uid IN (:itemIds)")
  fun loadAllByIds(itemIds: IntArray): List<StoredNotification>

  @Insert
  fun insertAll(vararg items: StoredNotification): List<Long>

  @Delete
  fun delete(item: StoredNotification)

  @Update
  fun update(item: StoredNotification)

  @Query("DELETE FROM stored_notification")
  fun deleteTable()

  @RawQuery
  fun query(supportSQLiteQuery: SupportSQLiteQuery): Int
}
