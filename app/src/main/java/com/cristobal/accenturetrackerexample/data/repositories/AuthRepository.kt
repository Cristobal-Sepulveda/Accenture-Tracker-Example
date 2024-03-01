package com.cristobal.accenturetrackerexample.data.repositories

import android.util.Log
import com.cristobal.accenturetrackerexample.domain.domainobjects.AuthCredentials
import com.cristobal.accenturetrackerexample.data.room.LocalDatabase
import com.cristobal.accenturetrackerexample.domain.sources.AuthDataSource
import com.cristobal.accenturetrackerexample.domain.sources.RoomDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AuthRepository(
    private val auth: FirebaseAuth,
    private val roomDatabase: RoomDataSource,
    private val ioDispatcher: CoroutineContext
) : AuthDataSource {

    override suspend fun checkIfUserIsLogged(): Boolean = withContext(ioDispatcher) {
        return@withContext auth.currentUser != null
    }

    override suspend fun signInWithEmailAndPassword(authCredentials: AuthCredentials) {
        withContext(ioDispatcher) {
            runCatching {
                val email = authCredentials.email
                val password = authCredentials.password
                auth.signInWithEmailAndPassword(email, password).await()
                roomDatabase.saveUser(email)
            }.onFailure {
                Log.e("signInWithEmailAndPassword", it.toString())
            }

        }
    }

    override suspend fun signOut(): Boolean {
        auth.signOut()
        return true
    }
}
