package com.liara.smartass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(
  tableName = "recurring_item_at_date",
  primaryKeys = ["recurringItemId", "occurenceId"],
  foreignKeys = [
    ForeignKey(
      entity = RecurringItem::class,
      parentColumns = ["uid"],
      childColumns = ["recurringItemId"],
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class RecurringItemAtDate(
  val date: LocalDate,
  val recurringItemId: Int,
  val occurenceId: Int,
  val done: Boolean
) {
  fun getView(recurringItemsDao: RecurringItemDao, localDate: LocalDate): RecurringItemView {
    return recurringItemsDao.getUid(recurringItemId)
      .makeView(occurenceId, localDate, done)
  }
}

@Dao
interface RecurringItemAtDateDao {
  @Query("SELECT * FROM recurring_item_at_date")
  fun getAll(): List<RecurringItemAtDate>

  @Query("SELECT * FROM recurring_item_at_date WHERE recurringItemId = :reccurringId AND occurenceId = :viewId")
  fun getFromIds(reccurringId: Int, viewId: Int): RecurringItemAtDate?

  @Query("SELECT * FROM recurring_item_at_date WHERE date = :date")
  fun getFlowFromDate(date: LocalDate): Flow<List<RecurringItemAtDate>>

  @Query("SELECT * FROM recurring_item_at_date WHERE date = :date")
  fun getFromDate(date: LocalDate): List<RecurringItemAtDate>

  @Query("UPDATE recurring_item_at_date SET done = :done WHERE occurenceId = :viewId AND recurringItemId = :reccurringId")
  fun setDone(done: Boolean, viewId: Int, reccurringId: Int)

  @Insert
  fun insertAll(vararg items: RecurringItemAtDate): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAllAndReplace(vararg items: RecurringItemAtDate)

  @Delete
  fun delete(item: RecurringItemAtDate)

  @Update
  fun update(item: RecurringItemAtDate)

  @Query("DELETE FROM recurring_item_at_date")
  fun deleteTable()

  @Query("DELETE FROM recurring_item_at_date WHERE recurringItemId = :reccurringId")
  fun deleteAllFromRecurringItem(reccurringId: Int)

  @RawQuery
  fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

  @Update
  fun updateAll(items: List<RecurringItemAtDate>)
}