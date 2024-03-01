package com.cristobal.accenturetrackerexample.domain.sources

import com.cristobal.accenturetrackerexample.domain.domainobjects.AuthCredentials

interface AuthDataSource {
    suspend fun checkIfUserIsLogged(): Boolean

    suspend fun signInWithEmailAndPassword(authCredentials: AuthCredentials)

    suspend fun signOut(): Boolean
}