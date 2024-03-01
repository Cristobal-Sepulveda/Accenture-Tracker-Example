package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.sources.FirestoreDataSource
import com.cristobal.accenturetrackerexample.utils.ResultUseCase

class ChangeDriverActiveStatusInFirestoreUseCase (
    private val firestoreDataSource: FirestoreDataSource
): ResultUseCase<Boolean, Boolean>(){

    override suspend fun executeOnBackground(params: Boolean): Boolean {
        return firestoreDataSource.changeDriverActiveStatusInFirestore(params)
    }

    override suspend fun executeOnException(throwable: Throwable): Throwable? {
        return super.executeOnException(throwable)
    }

    override suspend fun executeOnResponse(liveData: LiveResult<Boolean>, response: Boolean) {
        super.executeOnResponse(liveData, response)
    }
}