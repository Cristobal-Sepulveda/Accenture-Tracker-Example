package com.cristobal.accenturetrackerexample.utils

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<P, T, L : LiveData<*>>(
    protected open val backgroundContext: CoroutineContext = Dispatchers.IO,
    protected open val foregroundContext: CoroutineContext = Dispatchers.Main
) : KoinComponent {
    abstract suspend fun executeOnBackground(params: P): T?
    open suspend fun executeOnResponse(liveData: L, response: T) {}
    open suspend fun executeOnException(throwable: Throwable): Throwable? = null
    protected abstract suspend fun onStart(liveData: L)
    protected abstract suspend fun onEmpty(liveData: L)
    protected abstract suspend fun onSuccess(liveData: L, response: T)
    protected abstract suspend fun onFailure(liveData: L, throwable: Throwable)

    fun execute(params: P, liveData: L, coroutineScope: CoroutineScope) {
        coroutineScope.launch(foregroundContext) {
            onStart(liveData)
            runCatching {
                withContext(backgroundContext) {
                    val response = executeOnBackground(params)
                    response?.also { executeOnResponse(liveData, it) }
                }
            }.onSuccess { response ->
                if (response != null) onSuccess(liveData, response) else onEmpty(liveData)
            }.onFailure { throwable ->
                onFailure(liveData, throwable)
            }
        }
    }
}