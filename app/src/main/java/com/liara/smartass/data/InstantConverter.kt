package com.liara.smartass.data

import androidx.room.TypeConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import java.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Instant::class)
public class InstantConverter {
  @TypeConverter
  public fun fromInstant(instant: Instant?): Long? {
    return instant?.toEpochMilli()
  }

  @TypeConverter
  public fun longToInstant(timestamp: Long?): Instant? {
    return if (timestamp == null) {
      null
    } else {
      Instant.ofEpochMilli(timestamp)
    }
  }
}