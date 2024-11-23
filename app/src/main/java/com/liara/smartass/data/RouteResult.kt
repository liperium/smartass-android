package com.liara.smartass.data

import com.google.android.gms.maps.model.LatLng

data class RouteResult(
  val route: List<LatLng>,
  val distance: Double,
  val duration: Int, // in seconds
)
