package com.liara.smartass.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liara.smartass.GlobalVars
import com.liara.smartass.MainActivity
import com.liara.smartass.data.AgendaItem.Companion.stringToInstant
import com.liara.smartass.static_objects.RemoteManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.RRule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.GregorianCalendar

private const val TAG: String = "RecurringItem"

@Serializable
@Entity(tableName = "recurring_item")
data class RecurringItem(
  @PrimaryKey val uid: Int,
  val title: String,
  val description: String,
  val importance: Importance,
  val duration: Int, // Minutes?
  val first_occurence: Instant,
  val verif_method: VerifMethod,
  val rrule: RRule,
) : ModifiableItem {

  fun makeView(viewId: Int, localDate: LocalDate, isDone: Boolean): RecurringItemView {
    // End instant is in UTC time. I need to get the hour and minute representation at first_occurence and then add the duration and put the hours/minutes at the LocalDate provided
    val firstOccurrenceEndTime = first_occurence.plusSeconds(duration.toLong() * 60)
    val startInstant = first_occurence
      .atZone(ZoneId.of("UTC"))
      .toLocalDateTime()
      .withYear(localDate.year)
      .withMonth(localDate.monthValue)
      .withDayOfMonth(localDate.dayOfMonth).toInstant(ZoneOffset.UTC)
    val endInstant = firstOccurrenceEndTime
      .atZone(ZoneId.of("UTC"))
      .toLocalDateTime()
      .withYear(localDate.year)
      .withMonth(localDate.monthValue)
      .withDayOfMonth(localDate.dayOfMonth).toInstant(ZoneOffset.UTC)
    val view = RecurringItemView(
      recurringItemData = this,
      viewId,
      startInstant,
      endInstant,
      isDone,
      AgendaState.PassedBasicNotDone
    )
    view.updateState()
    return view
  }

  companion object {
    val instantFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val durationFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    fun fromJson(toParse: String): Pair<RecurringItem, Boolean> {
      val jsonObject = Json.parseToJsonElement(toParse).jsonObject
      Log.v("$TAG-fromJson", "$jsonObject")

      val firstOccurenceTimeStamp =
        jsonObject["first_occurrence"]?.jsonPrimitive?.content!!.toString()


      // Add timestamp to date
      val firstOccurenceInstant: Instant = stringToInstant(firstOccurenceTimeStamp)

      val rrule = RRule(jsonObject["rrule"]?.jsonPrimitive?.content!!)// The rules of the occurence
      val durationMinutes: Int = LocalTime.parse(
        jsonObject["duration"]?.jsonPrimitive?.content!!, durationFormatter
      ).toSecondOfDay() / 60

      val item = RecurringItem(
        uid = jsonObject["id"]?.jsonPrimitive?.content!!.toInt(),
        title = jsonObject["title"]?.jsonPrimitive?.content!!,
        description = jsonObject["description"]?.jsonPrimitive?.content!!,
        first_occurence = firstOccurenceInstant,
        importance = Importance.fromInt(jsonObject["importance"]?.jsonPrimitive?.content!!.toInt()),
        duration = durationMinutes,
        rrule = rrule,
        verif_method = VerifMethod.fromInt(jsonObject["verif_method"]?.jsonPrimitive?.content!!.toInt()),
      )
      val isDeleted = jsonObject["deleted"]?.jsonPrimitive?.content!!.toBoolean()

      return Pair(item, isDeleted)
    }

    private val maxRruleDate: Calendar = GregorianCalendar().apply {
      set(Calendar.MONTH, Calendar.JANUARY)
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.YEAR, 2026)
      set(Calendar.HOUR_OF_DAY, 1)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }
    // Builds a list for flows to work correctly,
    fun buildRecurringItemAtDate(
      itemsToMakeAtDate: Iterable<RecurringItem>,
      recurringItemsAtDateDao: RecurringItemAtDateDao = MainActivity.recurringItemAtDateDao
    ) {
      val icalDateEnd: Date = Date(maxRruleDate)

      val toAddItems: MutableList<RecurringItemAtDate> = mutableListOf()

      for (item in itemsToMakeAtDate) {
        val icalStartTimeItem = Date(java.util.Date(item.first_occurence.toEpochMilli()))
        for ((index, itemDate) in item.rrule.recur.getDates(
          icalStartTimeItem, icalDateEnd, Value.DATE
        ).withIndex()) {
          val atDate = itemDate.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
          // Need to always insert/replace, because date can be changed
          toAddItems.add(
            RecurringItemAtDate(
              atDate,
              item.uid,
              index,
              false
            )
          ) // Sets done as false, because you need to get the true tables after
        }
      }
      // Daos
      itemsToMakeAtDate.forEach {
        recurringItemsAtDateDao.deleteAllFromRecurringItem(it.uid)
      }
      recurringItemsAtDateDao.insertAllAndReplace(*toAddItems.toTypedArray())
    }

    fun getViewsForTodayFlow(
      localDate: LocalDate,
      recurringItemsDao: RecurringItemDao,
      recurringItemAtDateDao: RecurringItemAtDateDao
    ): Flow<List<RecurringItemView>> {
      return recurringItemAtDateDao.getFlowFromDate(localDate).map { recurringItemAtDate ->
        return@map recurringItemAtDate.map {
          it.getView(
            recurringItemsDao,
            localDate
          )
        }
      }
    }
  }

}


class RecurringItemView(
  val recurringItemData: RecurringItem,
  val viewId: Int, // Number of the occurence of the RecurringItem Rules. 1..NB of occurences possbile
  val startInstant: Instant,
  val endInstant: Instant,
  override var done: Boolean,
  override var state: AgendaState,
  override val title: String = recurringItemData.title,
  override val description: String = recurringItemData.description,
  override val verifMethod: VerifMethod = recurringItemData.verif_method,
) : AgendaItemInterface {
  override fun updateState() {
    state = getNewState()
  }

  fun getNewState(): AgendaState {
    if (done) {
      return AgendaState.Done
    }

    return if (isPassed()) { // Minutes?
      when (getItemImportance()) {
        Importance.Basic -> AgendaState.PassedBasicNotDone
        Importance.Important -> AgendaState.PassedImportantNotDone
        Importance.Moderate -> AgendaState.PassedModerateNotDone
      }
    } else {
      AgendaState.Upcoming
    }
  }

  override fun getHourInDay(): Int {
    val hoursLong: Int = recurringItemData.duration / 60
    return AgendaItem.utcToZonedDateTime(recurringItemData.first_occurence).hour + hoursLong
  }

  override fun getStartTime(): LocalTime {
    return AgendaItem.utcToZonedDateTime(recurringItemData.first_occurence).toLocalTime()
  }

  override fun getEndTime(): LocalTime {
    val endTime = getStartTime().apply { plusMinutes((recurringItemData.duration).toLong()) }
    return endTime
  }

  override fun deleteSelf(context: Context) {
    val item = this
    GlobalScope.launch(Dispatchers.IO) {
      RemoteManager.deleteRecurringItem(
        context, GlobalVars.userLoginInfo.value!!, item, MainActivity.recurringItemDao
      )
    }
  }
  override fun updateSelfRemote(context: Context, state: MutableState<AgendaState>): Boolean {
    val response = RemoteManager.doneRecurringItem(
      context,
      GlobalVars.userLoginInfo.value!!,
      this,
      MainActivity.recurringItemAtDateDao
    )
    if (response) {
      MainActivity.recurringItemAtDateDao.setDone(done, viewId, recurringItemData.uid)
      updateState()
      state.value = this.state
      return true
    } else
      return false
  }

  override fun getUniqueString(): String {
    return "${getType()}-${recurringItemData.uid}-${viewId}"
  }

  override fun getItemImportance(): Importance {
    return recurringItemData.importance
  }

  override fun getType(): AgendaItemTypes {
    return AgendaItemTypes.Recurring
  }

  override fun getEndNotificationInstant(): Instant {
    return endInstant
  }

  override fun getStartNotificationInstant(): Instant {
    return startInstant
  }

  override fun isPassed(fromTime: Instant): Boolean {
    return fromTime > endInstant
  }
}
