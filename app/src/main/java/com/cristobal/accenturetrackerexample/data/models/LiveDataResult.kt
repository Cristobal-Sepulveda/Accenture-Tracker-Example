package com.cristobal.accenturetrackerexample.data.models

import androidx.lifecycle.MutableLiveData
import com.cristobal.accenturetrackerexample.domain.domainobjects.Completable
import com.cristobal.accenturetrackerexample.domain.domainobjects.Result

typealias LiveResult<T> = MutableLiveData<Result<T>>
typealias LiveCompletable<Q> = MutableLiveData<Completable<Q>>

/* LiveResult */
@JvmName("postLoadingResult")
fun <T> LiveResult<T>.postLoading() = postValue(Result.OnLoading())
@JvmName("postErrorResult")
fun <T> LiveResult<T>.postThrowable(throwable: Throwable) = postValue(Result.OnError(throwable))
@JvmName("postEmptyResult")
fun <T> LiveResult<T>.postEmpty() = postValue(Result.OnEmpty())
@JvmName("postSuccessResult")
fun <T> LiveResult<T>.postSuccess(value: T) = postValue(Result.OnSuccess(value))


/* LiveCompletable */
@JvmName("postCompleteCompletable")
fun <Q> LiveCompletable<Q>.postComplete() = postValue(Completable.OnComplete())
@JvmName("postErrorCompletable")
fun <Q> LiveCompletable<Q>.postError(error: Throwable) = postValue(Completable.OnError(error))
@JvmName("postLoadingCompletable")
fun <Q> LiveCompletable<Q>.postLoading() = postValue(Completable.OnLoading())



