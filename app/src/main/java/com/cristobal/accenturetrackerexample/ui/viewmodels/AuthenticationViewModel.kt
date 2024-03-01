package com.cristobal.accenturetrackerexample.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.domainobjects.AuthCredentials
import com.cristobal.accenturetrackerexample.domain.domainobjects.PermissionsValidation
import com.cristobal.accenturetrackerexample.domain.usecases.PermissionsValidatorUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SignInUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SyncUseCase
import com.cristobal.accenturetrackerexample.utils.execute

class AuthenticationViewModel(
    private val syncUseCase: SyncUseCase,
    private val signInUseCase: SignInUseCase,
    private val permissionsValidatorUseCase: PermissionsValidatorUseCase
) : ViewModel() {
    val isAUserLogged = LiveResult<Boolean>()
    val loginValidation = LiveResult<Boolean>()
    private val _permissionsGranted = MutableLiveData<PermissionsValidation>()
    val arePermissionsGranted: LiveData<PermissionsValidation> = _permissionsGranted

    fun checkIfUserIsLogged() = execute(
        useCase = syncUseCase,
        params = "",
        liveData = isAUserLogged
    )

    fun login(authCredentials: AuthCredentials) {
        execute(
            useCase = signInUseCase,
            params = authCredentials,
            liveData = loginValidation
        )
    }

    fun arePermissionsGranted(activity: Activity) {
        val result = permissionsValidatorUseCase.arePermissionsAccepted(activity)
        _permissionsGranted.postValue(result)
    }
}