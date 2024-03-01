package com.cristobal.accenturetrackerexample.utils
import com.cristobal.accenturetrackerexample.domain.domainobjects.Result

fun <T> Result<T>?.getValueOrNull(): T? {
    return if (this is Result.OnSuccess) {
        this.value
    } else {
        null
    }
}