package com.liara.smartass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface LoginInfoDao {
  @Query("SELECT * FROM login_info") fun getAll(): List<LoginInfo>

  @Query("SELECT * FROM login_info WHERE uid IN (:itemId)") fun getId(itemId: Int): LoginInfo

  @Query("SELECT * FROM login_info WHERE uid IN (:itemIds)")
  fun loadAllByIds(itemIds: IntArray): List<LoginInfo>

  @Insert fun insertAll(vararg items: LoginInfo)

  @Delete fun delete(item: LoginInfo)

  @Update fun update(item: LoginInfo)

  @Query("DELETE FROM login_info") fun deleteTable()

  @RawQuery fun query(supportSQLiteQuery: SupportSQLiteQuery): Int
}
