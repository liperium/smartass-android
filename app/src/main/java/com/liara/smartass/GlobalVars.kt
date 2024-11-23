package com.liara.smartass

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.auth0.android.Auth0
import com.google.android.gms.location.FusedLocationProviderClient
import com.liara.smartass.data.LoginInfo
import com.liara.smartass.data.LoginInfoDao
import com.liara.smartass.data.MapLocation
import com.liara.smartass.data.MapLocationPreferences
import com.liara.smartass.static_objects.Message
import com.liara.smartass.static_objects.StoredNotificationManager
import com.liara.smartass.static_objects.VisualFeedbackManager
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.time.Instant


object GlobalVars {
  private const val TAG = "GlobalVars"
  val account: Auth0 = Auth0(
    "ScRdUKplSkEdN9u45rKNMC6cuURQtrm0", "hdp-liara.us.auth0.com"
  )
  var userLoginInfo: MutableState<LoginInfo?> = mutableStateOf(null)
  lateinit var getCameraImage: ActivityResultLauncher<Uri>
  lateinit var cameraResultDeferred: CompletableDeferred<File?>
  lateinit var fusedLocationClient: FusedLocationProviderClient
  var lastLocation: MutableState<MapLocation?> = mutableStateOf(null)
  var latestPicturePath: String? = null
  const val BASIC_NOTIFICATION_CHANNEL_ID = "basic_item_notifications"
  const val IMPORTANT_NOTIFICATION_CHANNEL_ID = "important_item_notifications"
    const val MODERATE_NOTIFICATION_CHANNEL_ID = "moderate_item_notifications"
  lateinit var tempDir: String

  var locationList: MutableList<MapLocation> =
    emptyList<MapLocation>().toMutableList() // TODO Probably should be private with public setters and getters
  var baseLocationListSize = locationList.size

  fun updateLocationList(context: Context) {
    val mapLocationPreferences = MapLocationPreferences(context)
    locationList = mapLocationPreferences.getMapLocations().toMutableList()
    baseLocationListSize = locationList.size
  }

  fun saveLocationList(context: Context, newBaseList: List<MapLocation>) {
    val mapLocationPreferences = MapLocationPreferences(context)
    mapLocationPreferences.saveMapLocations(newBaseList)

    locationList = locationList.filter { !it.userManaged }
      .toMutableList() // Keeps non-user managed positions (ex: user position)
    locationList.addAll(newBaseList)

    baseLocationListSize = newBaseList.size

    VisualFeedbackManager.showMessage(
      "Paramêtres sauvegardés",
      type = Message.MessageType.ApplicationFeedback
    )
  }

  fun updateNotificationPreferences(
    context: Context,
    isPreferenceEnabled: List<Pair<StoredNotificationManager.UserNotificationConfig, Boolean>>
  ) {
    val storedNotificationManager =
      StoredNotificationManager.UserNotificationConfigPreferences(context)
    val oldConfigs = storedNotificationManager.getAllConfigs()
    storedNotificationManager.saveConfigs(isPreferenceEnabled.filter { it.second }.map { it.first })
  }

  fun updateTimestamp(loginInfoDao: LoginInfoDao, requestTimestamp: Instant) {
    val newLoginInfo = userLoginInfo.value!!.copy(lastUpdateTimestamp = requestTimestamp)

    loginInfoDao.update(newLoginInfo)
    userLoginInfo = mutableStateOf(newLoginInfo)
    Log.d(TAG, "Timestamp updated $requestTimestamp")
  }
}