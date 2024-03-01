package com.cristobal.accenturetrackerexample.domain.domainobjects

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.time.Duration
import java.time.LocalTime

data class DriverFirestoreCurrentCaseData(
    val user: String,
    val isActive: Boolean,
    val documentLastAction: String,
    val currentRegistryTime: LocalTime,
    val currentLatLng: LatLng,
    val previousRegistryTime: LocalTime,
    val previousLatLng: LatLng,
    var currentMarkerOnMap: Marker? = null
) {

    private fun getDistanceBetweenLocationsInMeters(): Int {
        val currentLatitude = Location("").apply {
            latitude = currentLatLng.latitude
            longitude = currentLatLng.longitude
        }
        val previousLatitude = Location("").apply {
            latitude = previousLatLng.latitude
            longitude = previousLatLng.longitude
        }

        return previousLatitude.distanceTo(currentLatitude).toInt()
    }

    private fun getTimeBetweenRegistriesInSeconds(): Long {
        return Duration.between(
            previousRegistryTime, currentRegistryTime
        )?.toMillis()?.div(1000) ?: 0
    }

    fun getSpeedInKmPerHour(): String {
        val distanceBetweenLocations = getDistanceBetweenLocationsInMeters().toDouble()
        val timeBetweenRegistries = getTimeBetweenRegistriesInSeconds().toDouble()

        val speedInKmPerHour = if (timeBetweenRegistries <= 0) {
            0.0
        } else {
            (distanceBetweenLocations / timeBetweenRegistries) * 3.6 // Convert m/s to km/h
        }

        return String.format("%.2f km/h", speedInKmPerHour)
    }
}