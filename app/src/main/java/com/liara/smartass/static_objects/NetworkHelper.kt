package com.liara.smartass.static_objects

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException

object NetworkHelper {
  fun checkNetwork(context: Context) {
    if (!isNetworkAvailable(context)) {
      VisualFeedbackManager.showMessage("Pas de connexion internet")
      throw IOException("E : Pas de connexion internet")
    }
  }

  fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
      getNetworkCapabilities(activeNetwork)?.run {
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
      }
        ?: false
    }
}