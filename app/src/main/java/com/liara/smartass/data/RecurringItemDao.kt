package com.liara.smartass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface RecurringItemDao {
  @Query("SELECT * FROM recurring_item")
  fun getAll(): List<RecurringItem>

  @Query("SELECT * FROM recurring_item WHERE uid = :uid")
  fun getUid(uid: Int): RecurringItem


  @Query("SELECT * FROM recurring_item WHERE uid IN (:itemIds)")
  fun loadAllByIds(itemIds: IntArray): List<RecurringItem>

  @Insert
  fun insertAll(vararg items: RecurringItem): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAllAndReplace(vararg items: RecurringItem)

  @Delete
  fun delete(item: RecurringItem)

  @Update
  fun update(item: RecurringItem)

  @Query("DELETE FROM recurring_item")
  fun deleteTable()

  @RawQuery
  fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

  @Update
  fun updateAll(items: List<RecurringItem>)
}
