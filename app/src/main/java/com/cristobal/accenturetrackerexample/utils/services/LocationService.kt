package com.cristobal.accenturetrackerexample.utils.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.data.sharedpreference.SharedPreferenceUtil
import com.cristobal.accenturetrackerexample.utils.ACTION_LOCATION_BROADCAST
import com.cristobal.accenturetrackerexample.utils.NEW_LOCATION
import com.cristobal.accenturetrackerexample.utils.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

class LocationService : Service() {
    private var configurationChange = false
    private var serviceRunningInForeground = false
    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null



    override fun onCreate() {
        Log.d("LocationService", "onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(6)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime = TimeUnit.SECONDS.toMillis(8)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                val intent = Intent(ACTION_LOCATION_BROADCAST)
                intent.putExtra(NEW_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdates() {
        Log.d("LocationService", "subscribeToLocationUpdates()")
        try{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            startForegroundService(Intent(applicationContext, LocationService::class.java))
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }catch(e:Exception){
            Log.d("LocationService", "subscribeToLocationUpdates() error: ${e.message}")
        }
    }

    fun unsubscribeToLocationUpdates(){
        Log.d("LocationService", "unsubscribeToLocationUpdates()")
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            stopSelf()
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            Log.d("LocationService", "unsubscribeToLocationUpdates() error: ${unlikely.message}")
        }
    }

    private fun generateNotification(mainText: Int): Notification {
        val mainNotificationText = getString(mainText)
        val titleText = getString(R.string.app_name)
        val notificationCompatBuilder = NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        )
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)

        /*val activityPendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)*/

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.baseline_my_location_24_logo)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //.setContentIntent(activityPendingIntent)
            .build()
    }

    //Called by the system every time a client explicitly starts the service by calling startForegroundService(Intent),
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand()")
        startForeground(1, generateNotification(R.string.tracker_enabled))
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LocationService", "onBind()")
        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d("LocationService", "onRebind()")
        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("LocationService", "onUnbind()")
        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d("LocationService", "Start foreground service")
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

}
