package com.liara.smartass.components

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

class MapViewModel : ViewModel() {
    val cameraPositionState = CameraPositionState().apply {
        position =
            CameraPosition.fromLatLngZoom(LatLng(48.41661728276167, -71.06505566168171), 13f)
    }
}