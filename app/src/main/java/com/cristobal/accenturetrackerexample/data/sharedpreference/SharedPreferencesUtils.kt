package com.cristobal.accenturetrackerexample.data.sharedpreference

import android.content.Context
import androidx.core.content.edit
import com.cristobal.accenturetrackerexample.utils.PACKAGE_NAME

internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    private const val KEY_ACCESS_FINE_LOCATION = "KEY_ACCESS_FINE_LOCATION"

    fun getLocationTrackingPref(context: Context): Boolean {
        return context.getSharedPreferences(
            PACKAGE_NAME, Context.MODE_PRIVATE
        ).getBoolean(KEY_FOREGROUND_ENABLED, false)
    }

    fun arePermissionsRequested(context: Context): Boolean {
        return context.getSharedPreferences(
            PACKAGE_NAME, Context.MODE_PRIVATE
        ).getBoolean(
            KEY_ACCESS_FINE_LOCATION, false
        )
    }

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) {
        context.getSharedPreferences(
            PACKAGE_NAME,
            Context.MODE_PRIVATE
        ).edit { putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates) }
    }

    fun savePermissionsWereRequested(context: Context) {
        context.getSharedPreferences(
            PACKAGE_NAME,
            Context.MODE_PRIVATE
        ).edit { putBoolean(KEY_ACCESS_FINE_LOCATION, true) }
    }
}
