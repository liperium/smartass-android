package com.liara.smartass.data

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {
  @TypeConverter
  public fun fromInstant(date: LocalDate?): String {
    return date.toString()
  }

  @TypeConverter
  public fun stringToLocalDate(timestamp: String): LocalDate {
      return LocalDate.parse(timestamp)
  }
}