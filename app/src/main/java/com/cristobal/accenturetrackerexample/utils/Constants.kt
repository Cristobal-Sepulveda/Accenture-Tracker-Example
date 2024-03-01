package com.cristobal.accenturetrackerexample.utils

import com.google.android.gms.maps.model.LatLng

val defaultLocation = LatLng(-33.47536870666403, -70.64367761577908)
const val supervisorDefaultZoom = 10.7f
const val driverDefaultZoom = 18f
const val PACKAGE_NAME = "com.cristobal.accenturetrackexample"
internal const val ACTION_LOCATION_BROADCAST = "$PACKAGE_NAME.action.LOCATION_BROADCAST"
const val DRIVER_REGISTRY = "DriverRegistry"
const val NOTIFICATION_CHANNEL_ID = "001"
internal const val NEW_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
const val DRIVER_ACTIVE = "driverActive"
const val ADDED = "ADDED"
const val MODIFIED = "MODIFIED"
const val SUPERVISOR = "Supervisor"
const val DRIVER = "Driver"
