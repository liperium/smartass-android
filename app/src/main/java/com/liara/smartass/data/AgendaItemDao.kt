package com.liara.smartass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AgendaItemDao {
  @Query("SELECT * FROM agenda_item") fun getAll(): List<AgendaItem>

  @Query("SELECT * FROM agenda_item WHERE uid = :uid")
  fun getUid(uid: Int): AgendaItem
  @Query("SELECT * FROM agenda_item ORDER BY endInstant") fun getAllFlow(): Flow<List<AgendaItem>>
  @Query(
      "SELECT * FROM agenda_item WHERE dateShown = :date ORDER BY endInstant")
  fun getDateFlow(date: LocalDate): Flow<List<AgendaItem>>

  @Query(
    "SELECT * FROM agenda_item WHERE dateShown = :date ORDER BY endInstant"
  )
  fun getAtDate(date: LocalDate): List<AgendaItem>
  @Query("SELECT * FROM agenda_item WHERE uid IN (:itemIds)")
  fun loadAllByIds(itemIds: IntArray): List<AgendaItem>

  @Insert
  fun insertAll(vararg items: AgendaItem): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAllAndReplace(vararg items: AgendaItem)

  @Delete fun delete(item: AgendaItem)

  @Update fun update(item: AgendaItem)

  @Query("DELETE FROM agenda_item") fun deleteTable()

  @RawQuery fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
  @Query("SELECT * FROM agenda_item WHERE done") fun getDoneList(): List<AgendaItem>

  @Update fun updateAll(items: List<AgendaItem>)
}
