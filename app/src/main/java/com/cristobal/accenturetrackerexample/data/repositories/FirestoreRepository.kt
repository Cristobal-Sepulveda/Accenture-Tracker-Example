package com.cristobal.accenturetrackerexample.data.repositories

import com.cristobal.accenturetrackerexample.domain.sources.FirestoreDataSource
import com.cristobal.accenturetrackerexample.utils.DRIVER_ACTIVE
import com.cristobal.accenturetrackerexample.utils.DRIVER_REGISTRY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.time.LocalTime

class FirestoreRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirestoreDataSource {

    override suspend fun changeDriverActiveStatusInFirestore(
        isActive: Boolean
    ) : Boolean = withContext(Dispatchers.IO) {
        val userEmail = auth.currentUser?.email ?: ""
        val deferred = CompletableDeferred<Boolean>()
        val data = hashMapOf(DRIVER_ACTIVE to isActive)

        firestore.collection(DRIVER_REGISTRY)
            .document(userEmail)
            .set(data, SetOptions.merge())
            .addOnFailureListener{
                deferred.complete(false)
            }
            .addOnSuccessListener{
                deferred.complete(isActive)
            }

        deferred.await()
    }

    override suspend fun saveLocationInFirestore(geoPoint: GeoPoint) {
        withContext(Dispatchers.IO) {
            val userEmail = auth.currentUser?.email ?: ""
            val localTime = LocalTime.now().toString()

            firestore.collection(DRIVER_REGISTRY)
                .document(userEmail)
                .get()
                .addOnSuccessListener {
                    if (it.data.isNullOrEmpty()) {
                        saveLocationForNewUserInFirestore(geoPoint, userEmail, localTime)
                    }else{
                        updateLocationForUserInFirestore(geoPoint, userEmail, localTime)
                    }
                }
        }
    }

    override fun getFirestoreChanges(): Flow<QuerySnapshot> {
        return callbackFlow {

            val listenerRegistration = firestore.collection(DRIVER_REGISTRY)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        close(exception)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        trySend(snapshot)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    private fun saveLocationForNewUserInFirestore(
        geoPoint: GeoPoint,
        userEmail: String,
        localTime: String
    ) {
        firestore.collection(DRIVER_REGISTRY)
            .document(userEmail)
            .set(
                mapOf(
                    "driverActive" to true,
                    "email" to userEmail,
                    "registroJornada" to mapOf(
                        "horasConRegistro" to arrayListOf(localTime),
                        "geopoints" to arrayListOf(geoPoint)
                    )
                )
            )
    }

    private fun updateLocationForUserInFirestore(
        geoPoint: GeoPoint,
        userEmail: String,
        localTime: String
    ) {
        firestore.collection(DRIVER_REGISTRY)
            .document(userEmail)
            .get()
            .addOnFailureListener {
                println("el update fallo: $it")
            }
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.data == null) return@addOnSuccessListener
                if (documentSnapshot.data?.get("registroJornada") == null) {
                    saveLocationForNewUserInFirestore(geoPoint, userEmail, localTime)
                    return@addOnSuccessListener
                }
                val documentData = documentSnapshot.data
                val actualRegistry = documentData?.get("registroJornada") as Map<*, *>
                val listOfGeoPoints = actualRegistry["geopoints"] as MutableList<GeoPoint>
                val listOfTimes = actualRegistry["horasConRegistro"] as MutableList<String>

                listOfGeoPoints.add(geoPoint)
                listOfTimes.add(localTime)

                val newDocument = mapOf(
                    "registroJornada" to mapOf(
                        "geopoints" to listOfGeoPoints,
                        "horasConRegistro" to listOfTimes
                    )
                )

                firestore.collection(DRIVER_REGISTRY)
                    .document(userEmail)
                    .update(newDocument)
                    .addOnFailureListener {
                        println("el update fallo: $it")
                    }
            }
    }
}