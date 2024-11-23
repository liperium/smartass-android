package com.liara.smartass.data

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.result.UserProfile
import com.liara.smartass.GlobalVars
import com.liara.smartass.MainActivity
import com.liara.smartass.static_objects.VisualFeedbackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

private const val TAG = "LoginInfo"

@Entity(tableName = "login_info")
data class LoginInfo(
  @PrimaryKey(autoGenerate = true) var uid: Int,
  val email: String,
  val name: String,
  val hashedID: String, //24 characters
  val accessToken: String,
  val lastUpdateTimestamp: Instant,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LoginInfo

    return uid == other.uid &&
        email == other.email &&
        hashedID == other.hashedID
  } // Kotlin recommendation for equal?

  override fun hashCode(): Int {
    var result = uid
    result = 31 * result + email.hashCode()
    result = 31 * result + hashedID.hashCode()
    return result
  } // Kotlin recommendation for hashCode?
  fun onlyId(): String {
    return hashedID.split("|", limit = 2)[1]
  }

  companion object {
    fun getUserInfo(opaqueToken: String) {
      val client = AuthenticationAPIClient(GlobalVars.account)
      // Client that uses app secrets to request user info
      Log.v(TAG, "GetUserInfo - accessToken = $opaqueToken")
      client.userInfo(opaqueToken).start(object : Callback<UserProfile, AuthenticationException> {
        override fun onFailure(error: AuthenticationException) {
          VisualFeedbackManager.showMessage("Failed to retrieve user info")
          Log.e(TAG, "Failed to retrieve user info")
        }

        override fun onSuccess(result: UserProfile) {
          // We have the user's profile!
          val resultingInfo = LoginInfo(
            0,
            email = result.email!!,
            name = result.name!!,
            hashedID = result.getId()!!,
            accessToken = opaqueToken,
            lastUpdateTimestamp = Instant.ofEpochMilli(0),
          )
          MainActivity.loginInfoDao.deleteTable() // Makes sure we are not storing multiple login infosd
          MainActivity.loginInfoDao.insertAll(resultingInfo)
          GlobalScope.launch(Dispatchers.IO) {
            MainActivity.recurringItemAtDateDao.deleteTable()
            MainActivity.recurringItemDao.deleteTable()
            MainActivity.agendaItemDao.deleteTable()
          }

          // Has to be initialized before this ( MainActivity OnCreate )
          GlobalVars.userLoginInfo.value = resultingInfo
        }
      })

    }
  }
}
