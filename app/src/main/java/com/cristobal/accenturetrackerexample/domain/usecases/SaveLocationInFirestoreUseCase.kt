package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.data.repositories.FirestoreRepository
import com.cristobal.accenturetrackerexample.domain.sources.FirestoreDataSource
import com.cristobal.accenturetrackerexample.utils.CompletableUseCase
import com.google.firebase.firestore.GeoPoint

class SaveLocationInFirestoreUseCase(
    private val firestoreDataSource: FirestoreDataSource
) : CompletableUseCase<GeoPoint, Nothing>() {
    override suspend fun executeOnBackground(params: GeoPoint): Nothing? {
        firestoreDataSource.saveLocationInFirestore(params)
        return null
    }
}