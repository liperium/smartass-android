package com.liara.smartass.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapLocationPreferences(private val context: Context) {

  private val sharedPreferences: SharedPreferences by lazy {
    context.getSharedPreferences("MapLocationPrefs", Context.MODE_PRIVATE)
  }

  private val gson = Gson()

  fun saveMapLocations(locations: List<MapLocation>) {
    val json = gson.toJson(locations)
    sharedPreferences.edit().putString("mapLocations", json).apply()
  }

  fun getMapLocations(): List<MapLocation> {
    val json = sharedPreferences.getString("mapLocations", null)
    return if (json != null) {
      val type = object : TypeToken<List<MapLocation>>() {}.type
      gson.fromJson(json, type)
    } else {
      listOf(
        MapLocation(LatLng(48.418948, -71.052471), "UQAC", "UQAC Chicoutimi"),
        MapLocation(LatLng(48.417483, -71.063305), "Maison", "755 Rue Bégin Chicoutimi")
      )
    }
  }
}

data class MapLocation(
  val location: LatLng,
  val shortName: String,
  val address: String?,
  val userManaged: Boolean = true // For the position of the user
)

// API MAPPING : https://developers.google.com/maps/documentation/routes/reference/rpc/google.maps.routing.v2#google.maps.routing.v2.TransitPreferences.TransitTravelMode
enum class MapsTransportType(
  val text: String,
  val mapsMapping: String,
  val icon: ImageVector,
  val mapsApiMapping: String
) {
  Walk("Marche", "walking", Icons.AutoMirrored.Filled.DirectionsWalk, "WALK"),
  Bike("Vélo", "bicycling", Icons.AutoMirrored.Filled.DirectionsBike, "BICYCLE"),
  Transit("Bus", "transit", Icons.Default.DirectionsBus, "TRANSIT"),
  Car("Auto", "driving", Icons.Default.DirectionsCar, "DRIVE"),
}

