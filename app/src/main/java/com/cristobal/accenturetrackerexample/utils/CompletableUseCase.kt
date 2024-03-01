package com.cristobal.accenturetrackerexample.utils

import com.cristobal.accenturetrackerexample.data.models.LiveCompletable
import com.cristobal.accenturetrackerexample.data.models.postComplete
import com.cristobal.accenturetrackerexample.data.models.postError
import com.cristobal.accenturetrackerexample.data.models.postLoading

abstract class CompletableUseCase<P, Q> : BaseUseCase<P, Q, LiveCompletable<Q>>() {
    override suspend fun onStart(liveData: LiveCompletable<Q>) {
        liveData.postLoading()
    }

    override suspend fun onSuccess(liveData: LiveCompletable<Q>, response: Q) {
        liveData.postComplete()
    }

    override suspend fun onEmpty(liveData: LiveCompletable<Q>) {}

    override suspend fun onFailure(liveData: LiveCompletable<Q>, throwable: Throwable) {
        liveData.postError(throwable)
    }
}