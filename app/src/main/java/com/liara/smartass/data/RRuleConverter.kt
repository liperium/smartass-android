package com.liara.smartass.data

import androidx.room.TypeConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import net.fortuna.ical4j.model.property.RRule

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = RRule::class)
class RRuleConverter {
  @TypeConverter
  fun fromRRule(rRule: RRule): String {
    return rRule.toString().trimEnd().trimStart('R', 'R', 'U', 'L', 'E', ':')
  }

  @TypeConverter
  fun stringToRRule(textRRule: String): RRule {
    return RRule(textRRule)
  }
}