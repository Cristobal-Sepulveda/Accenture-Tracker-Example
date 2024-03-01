package com.cristobal.accenturetrackerexample.domain.domainobjects

import com.google.android.gms.maps.model.Marker

data class DriverUiData(
    val userEmail: String,
    var marker: Marker,
    val speed: String
)