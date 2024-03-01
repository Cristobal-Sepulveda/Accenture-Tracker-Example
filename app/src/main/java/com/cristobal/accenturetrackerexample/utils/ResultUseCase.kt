package com.cristobal.accenturetrackerexample.utils

import androidx.lifecycle.LiveData
import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.data.models.postEmpty
import com.cristobal.accenturetrackerexample.data.models.postLoading
import com.cristobal.accenturetrackerexample.data.models.postSuccess
import com.cristobal.accenturetrackerexample.data.models.postThrowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class ResultUseCase<P, T> : BaseUseCase<P, T, LiveResult<T>>() {
    override suspend fun onStart(liveData: LiveResult<T>) {
        liveData.postLoading()
    }

    override suspend fun onEmpty(liveData: LiveResult<T>) {
        liveData.postEmpty()
    }

    override suspend fun onSuccess(liveData: LiveResult<T>, response: T) {
        liveData.postSuccess(response)
    }

    override suspend fun onFailure(liveData: LiveResult<T>, throwable: Throwable) {
        liveData.postThrowable(throwable)
    }
}

