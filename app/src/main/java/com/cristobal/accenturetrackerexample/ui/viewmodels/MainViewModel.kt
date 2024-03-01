package com.cristobal.accenturetrackerexample.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.domainobjects.PermissionsValidation
import com.cristobal.accenturetrackerexample.domain.usecases.PermissionsValidatorUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SignOutUseCase
import com.cristobal.accenturetrackerexample.utils.execute

class MainViewModel(
    private val signOutUseCase: SignOutUseCase,
    private val permissionsValidatorUseCase: PermissionsValidatorUseCase
): ViewModel() {
    private val signOutResult = LiveResult<Boolean>()
    private val _permissionsGranted = MutableLiveData<PermissionsValidation>()
    val arePermissionsGranted: LiveData<PermissionsValidation> = _permissionsGranted

    fun logout () {
        execute(
            useCase = signOutUseCase,
            params = "",
            liveData = signOutResult
        )
    }

    fun arePermissionsGranted(activity: Activity) {
        val result = permissionsValidatorUseCase.arePermissionsAccepted(activity)
        _permissionsGranted.postValue(result)
    }
}