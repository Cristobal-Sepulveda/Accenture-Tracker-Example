package com.cristobal.accenturetrackerexample.domain.domainobjects

sealed class Completable<Q> {
    class OnComplete<Q> : Completable<Q>()
    data class OnError<Q>(val throwable: Throwable) : Completable<Q>()
    class OnLoading<Q> : Completable<Q>()
}