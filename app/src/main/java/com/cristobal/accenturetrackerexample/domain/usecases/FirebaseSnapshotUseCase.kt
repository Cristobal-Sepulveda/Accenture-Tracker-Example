package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.domain.domainobjects.DriverFirestoreCurrentCaseData
import com.cristobal.accenturetrackerexample.domain.sources.FirestoreDataSource
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class FirebaseSnapshotUseCase(
    private val firestoreDataSource: FirestoreDataSource
) {
    fun execute(): Flow<List<DriverFirestoreCurrentCaseData>> {
        return firestoreDataSource.getFirestoreChanges()
            .map { snapshot ->
                processSnapshot(snapshot)
            }
    }

    private fun processSnapshot(snapshot: QuerySnapshot): List<DriverFirestoreCurrentCaseData> {
        runCatching {
            return snapshot.documentChanges.map { document ->
                val registroJornada = document.document.data["registroJornada"] as Map<String, *>
                val geoPoints = registroJornada["geopoints"] as List<GeoPoint>
                val horasConRegistro = registroJornada["horasConRegistro"] as List<String>

                val user = document.document.id
                val isActive = document.document.data["driverActive"] as Boolean

                val currentLocation = LatLng(
                    geoPoints.last().latitude,
                    geoPoints.last().longitude
                )
                val previousLocation = when(geoPoints.size) {
                    0 -> currentLocation
                    else -> LatLng(
                        geoPoints[geoPoints.size - 2].latitude,
                        geoPoints[geoPoints.size - 2].longitude
                    )
                }
                val currentRegistryTime = LocalTime.parse(
                    horasConRegistro.last()
                )
                val previousRegistryTime = when(horasConRegistro.size) {
                    0 -> currentRegistryTime
                    else -> LocalTime.parse(
                        horasConRegistro[horasConRegistro.size - 2]
                    )
                }
                DriverFirestoreCurrentCaseData(
                    user = user,
                    isActive = isActive,
                    documentLastAction = document.type.name,
                    currentRegistryTime = currentRegistryTime,
                    currentLatLng= currentLocation,
                    previousRegistryTime = previousRegistryTime,
                    previousLatLng = previousLocation,
                )
            }
        }.getOrElse { return emptyList() }
    }
}