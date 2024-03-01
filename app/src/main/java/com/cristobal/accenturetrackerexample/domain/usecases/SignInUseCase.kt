package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.domainobjects.AuthCredentials
import com.cristobal.accenturetrackerexample.domain.sources.AuthDataSource
import com.cristobal.accenturetrackerexample.utils.ResultUseCase

class SignInUseCase(
    private val authRepository: AuthDataSource,
): ResultUseCase<AuthCredentials, Boolean>(){

    override suspend fun executeOnBackground(params: AuthCredentials): Boolean {
        authRepository.signInWithEmailAndPassword(params)
        return true
    }

    override suspend fun executeOnException(throwable: Throwable): Throwable? {
        return super.executeOnException(throwable)
    }

    override suspend fun executeOnResponse(liveData: LiveResult<Boolean>, response: Boolean) {
        super.executeOnResponse(liveData, response)
    }
}