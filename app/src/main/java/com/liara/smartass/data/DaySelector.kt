package com.liara.smartass.data

import androidx.compose.runtime.MutableState
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

enum class RelativeDayChange {
  Previous,
  Next,
}

// Has to return new instance each time, because the remember block in the composable (UI) will not update
data class DaySelector(
    private var selectedDay: MutableState<LocalDate>
) { // Private so use Companion.getTodaysDaySelector()
    fun getDate(): MutableState<LocalDate> {
        return selectedDay
  }

    fun getOnlyDate(): LocalDate {
        return selectedDay.value
    }
  fun isToday(): Boolean {
      return selectedDay.value == LocalDate.now()
  }
  fun getFormattedDate(): String {
      return selectedDay.value.format(
        DateTimeFormatterBuilder()
            .appendPattern("EEEE, ")
            .appendPattern("d MMMM")
            .toFormatter(Locale.CANADA_FRENCH)
    )
  }

    fun changeRelativeDay(selection: RelativeDayChange) {
        selectedDay.value = selectedDay.value.plusDays(
        when (selection) {
          RelativeDayChange.Previous -> -1
          RelativeDayChange.Next -> 1
        }
    )
  }

    fun changeToDay(newDate: LocalDate) {
        selectedDay.value = newDate
  }

}
