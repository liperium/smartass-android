package com.liara.smartass.data

import com.liara.smartass.GlobalVars.BASIC_NOTIFICATION_CHANNEL_ID
import com.liara.smartass.GlobalVars.IMPORTANT_NOTIFICATION_CHANNEL_ID
import com.liara.smartass.GlobalVars.MODERATE_NOTIFICATION_CHANNEL_ID

enum class Importance(val value: Int){
  Basic(0),//0
    Moderate(2),//2
  Important(1),//1
  ;

    fun toFrench() = when (this) {
        Basic -> "Basique"
        Moderate -> "Modéré"
        Important -> "Important"
    }

    fun notificationChannel(): String {
        return when (this) {
            Basic -> BASIC_NOTIFICATION_CHANNEL_ID
            Important -> IMPORTANT_NOTIFICATION_CHANNEL_ID
            Moderate -> MODERATE_NOTIFICATION_CHANNEL_ID
        }
    }

  companion object {
    fun fromInt(value: Int) = Importance.values().first { it.value == value }
  }
}

