package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.sources.AuthDataSource
import com.cristobal.accenturetrackerexample.domain.sources.RoomDataSource
import com.cristobal.accenturetrackerexample.utils.ResultUseCase

class SignOutUseCase(
    private val authRepository: AuthDataSource,
    private val roomRepository: RoomDataSource
): ResultUseCase<String, Boolean>(){

    override suspend fun executeOnBackground(params: String): Boolean {
        val signOutResult = authRepository.signOut()
        roomRepository.deleteUsers()
        return signOutResult
    }

    override suspend fun executeOnException(throwable: Throwable): Throwable? {
        return super.executeOnException(throwable)
    }

    override suspend fun executeOnResponse(liveData: LiveResult<Boolean>, response: Boolean) {
        super.executeOnResponse(liveData, response)
    }
}