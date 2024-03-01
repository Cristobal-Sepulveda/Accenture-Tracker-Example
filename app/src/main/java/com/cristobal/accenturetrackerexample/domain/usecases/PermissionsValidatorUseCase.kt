package com.cristobal.accenturetrackerexample.domain.usecases

import android.Manifest
import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import com.cristobal.accenturetrackerexample.domain.domainobjects.PermissionsValidation

class PermissionsValidatorUseCase {
    fun arePermissionsAccepted(activity: Activity): PermissionsValidation {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.plus(
            listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissions.plus(
            listOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )

        val checkingPermissions = permissions.filter {
            ContextWrapper(activity).checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
        }

        return PermissionsValidation(checkingPermissions)
    }
}