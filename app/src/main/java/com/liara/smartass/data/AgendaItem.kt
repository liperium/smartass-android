package com.liara.smartass.data

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liara.smartass.GlobalVars
import com.liara.smartass.MainActivity
import com.liara.smartass.static_objects.RemoteManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "AgendaItem"

@Entity(tableName = "agenda_item")
data class AgendaItem(
  @PrimaryKey(autoGenerate = true) var uid: Int,
  override var title: String,
  override var description: String,
  @ColumnInfo(name = "startInstant") var beginInstant: Instant,
  @ColumnInfo(name = "endInstant") var endInstant: Instant, // Temp affiché getDayTime() et celui pour la journée (getDaily ou wtv)
  var importance: Importance,
  override var state: AgendaState,
  override var done: Boolean = false,
  var dateShown: LocalDate = LocalDate.now(),
  override var verifMethod: VerifMethod = VerifMethod.Aucune,
) : AgendaItemInterface, ModifiableItem {

  override fun updateState() {
    state = getNewState()
  }
  override fun getType(): AgendaItemTypes {
    return AgendaItemTypes.Single
  }

  fun getNewState(): AgendaState {
    if (done) {
      return AgendaState.Done
    }

    // DEBUG TIME
    val condition = isPassed()

    return if (condition) { // Minutes?
      when (importance) {
        Importance.Basic -> AgendaState.PassedBasicNotDone
        Importance.Important -> AgendaState.PassedImportantNotDone
        Importance.Moderate -> AgendaState.PassedModerateNotDone
      }
    } else {
      AgendaState.Upcoming
    }
  }

  override fun getHourInDay(): Int {
    val returned = utcToZonedDateTime(endInstant)
    return returned.hour
  }

  // Get ZONED start time of event
  override fun getStartTime(): LocalTime {
    return utcToZonedDateTime(beginInstant).toLocalTime()
  }

  // Get ZONED end time of event
  override fun getEndTime(): LocalTime {
    return utcToZonedDateTime(endInstant).toLocalTime()
  }

  override fun getItemImportance(): Importance {
    return this.importance
  }

  override fun deleteSelf(context: Context) {
      GlobalScope.launch(Dispatchers.IO) {
      RemoteManager.deleteAgendaItem(
          context, GlobalVars.userLoginInfo.value!!, this@AgendaItem,
        MainActivity.agendaItemDao
      )
    }.start()
  }

  override fun updateSelfRemote(context: Context, state: MutableState<AgendaState>): Boolean {
    return RemoteManager.updateAgendaItem(
      context, GlobalVars.userLoginInfo.value!!, this,
      MainActivity.agendaItemDao, state
    )
  }

  override fun getUniqueString(): String {
    return "${getType()}-${uid}"
  }

  override fun getEndNotificationInstant(): Instant {
    return endInstant
  }

  override fun getStartNotificationInstant(): Instant {
    return beginInstant
  }

  // UTC - Checks if passed
  override fun isPassed(fromTime: Instant): Boolean {
    return fromTime >= endInstant
  }

  companion object {
    private var makeDumbListCalls = 0
    val myDispatcher: CoroutineDispatcher = Dispatchers.Default

    @Deprecated(message = "Should not be used for anything but checking UI")
    fun makeDumbList(numberOfItems: Int, daySelector: DaySelector): MutableList<AgendaItem> {
      val listAgendaItem = mutableListOf<AgendaItem>()
      for (i in 0 until numberOfItems) {

        val dateTime =
          LocalTime.of(i - (numberOfItems / 2) + LocalTime.now().hour, 0).atDate(LocalDate.now())
        val startLocalTime =
          dateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val endLocalTime =
          dateTime.plusHours(1).toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))

        val importance =
          if (i % 2 == makeDumbListCalls % 2) Importance.Basic else Importance.Important
        val newItem =
          AgendaItem(
            0,
            "Ménage - $i",
            "Faire le ménage - $i",
            startLocalTime,
            endLocalTime,
            importance,
            AgendaState.Upcoming,
            false,
            LocalDate.now()
          )
        listAgendaItem.add(newItem)
      }
      makeDumbListCalls++
      return listAgendaItem
    }


    // Convert from json string, returns the agenda item and if it was deleted
    fun fromJson(json: String): Pair<AgendaItem, Boolean> {

      // Example input json that I want to convert : {"id":6,"title":"Ménage Cuisine","description":"Faire la cuisine","user_id":1,"importance":0,"done":false,"last_update":"2024-01-24T15:12:24.681128","start":"UTCTIMESTAMP","end":"UTCTIMESTAMP"}
      // convert from json string
      val jsonObject = Json.parseToJsonElement(json).jsonObject

      val startTimeStamp = jsonObject["start"]?.jsonPrimitive?.content!!.toString()
      val endTimeStamp = jsonObject["end"]?.jsonPrimitive?.content!!.toString()

      // Add timestamp to date
      val timeStart: Instant = stringToInstant(startTimeStamp)
      val timeEnd: Instant = stringToInstant(endTimeStamp)

      val date: LocalDate = utcToZonedDateTime(timeEnd).toLocalDate()

      return Pair(
        AgendaItem(
          uid = jsonObject["id"]?.jsonPrimitive?.content!!.toInt(),
          title = jsonObject["title"]?.jsonPrimitive?.content!!,
          description = jsonObject["description"]?.jsonPrimitive?.content!!,
          beginInstant = timeStart,
          endInstant = timeEnd,
          importance = Importance.fromInt(jsonObject["importance"]?.jsonPrimitive?.content!!.toInt()),
          state = AgendaState.Upcoming,
          done = jsonObject["done"]?.jsonPrimitive?.content!!.toBoolean(),
          dateShown = date, // The date at which the item is shown
          verifMethod = VerifMethod.fromInt(jsonObject["verif_method"]?.jsonPrimitive?.content!!.toInt()),
        ),
        jsonObject["deleted"]?.jsonPrimitive?.content!!.toBoolean(),
      )
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    fun convertInstant(instant: Instant): String {
      //instant to format 2024-02-05T10:27:53
      return instant.atOffset(ZoneOffset.UTC).format(formatter)
    }

    fun stringToInstant(instant: String): Instant {
      return LocalDateTime.parse(instant, formatter).atOffset(ZoneOffset.UTC).toInstant()
    }

    fun utcToLocalInstant(instant: Instant): Instant {
      return utcToZonedDateTime(instant).toInstant()
    }

    fun utcToZonedDateTime(instant: Instant): ZonedDateTime {
      return instant
        .atOffset(ZoneOffset.UTC)
        .atZoneSameInstant(ZoneId.systemDefault())
    }
  }
}
