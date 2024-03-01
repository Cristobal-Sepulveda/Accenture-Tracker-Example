package com.cristobal.accenturetrackerexample.domain.sources

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

interface FirestoreDataSource {

    suspend fun saveLocationInFirestore(geoPoint: GeoPoint)
    suspend fun changeDriverActiveStatusInFirestore(isActive: Boolean) : Boolean

    fun getFirestoreChanges(): Flow<QuerySnapshot>
}