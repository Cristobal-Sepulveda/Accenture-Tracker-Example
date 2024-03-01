package com.cristobal.accenturetrackerexample.domain.domainobjects

sealed class Result<T> {
    class OnLoading<T> : Result<T>()
    data class OnError<T>(val throwable: Throwable) : Result<T>()
    class OnEmpty<T> : Result<T>()
    data class OnSuccess<T>(val value: T) : Result<T>()
}
