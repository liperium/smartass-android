package com.liara.smartass.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stored_notification")
data class StoredNotification(
  @PrimaryKey(autoGenerate = true) val uid: Int,
  val agendaItemId: String, // ID that is unique for ANY type of notification
)