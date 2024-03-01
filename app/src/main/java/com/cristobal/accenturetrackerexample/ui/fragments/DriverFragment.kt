package com.cristobal.accenturetrackerexample.ui.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.data.sharedpreference.SharedPreferenceUtil
import com.cristobal.accenturetrackerexample.databinding.FragmentDriverBinding
import com.cristobal.accenturetrackerexample.domain.domainobjects.Result
import com.cristobal.accenturetrackerexample.utils.services.LocationService
import com.cristobal.accenturetrackerexample.ui.viewmodels.HomeViewModel
import com.cristobal.accenturetrackerexample.utils.ACTION_LOCATION_BROADCAST
import com.cristobal.accenturetrackerexample.utils.NEW_LOCATION
import com.cristobal.accenturetrackerexample.utils.PACKAGE_NAME
import com.cristobal.accenturetrackerexample.utils.defaultLocation
import com.cristobal.accenturetrackerexample.utils.driverDefaultZoom
import com.cristobal.accenturetrackerexample.utils.observe
import com.cristobal.accenturetrackerexample.utils.showToastWithStringResource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DriverFragment :
    Fragment(),
    OnMapReadyCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: FragmentDriverBinding
    private lateinit var map: GoogleMap
    private lateinit var fabTracker: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel by sharedViewModel<HomeViewModel>()
    private var lastKnownLocation: Location? = null
    private var locationServiceBound = false
    private var locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()

    /////////////////////////////////////////////////////////////////////////
    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Monitors connection to the while-in-use service.
    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("LocationService", "onServiceConnected()")
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        initObservers()
        initSharedPreference()
        initMap()
        initAndBindLocationService()

        viewModel.getUserEmailAndProfileType()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initMapCameraInCurrentLocation()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
                val isUserTrackingLocation = it.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                    false
                )
                when (isUserTrackingLocation) {
                    true -> binding.fabTracker.setImageResource(R.drawable.pause_24px)
                    false -> binding.fabTracker.setImageResource(R.drawable.play_arrow_24px)
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(
            requireActivity()
        ).unregisterReceiver(locationServiceBroadcastReceiver)

        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }

        locationService?.unsubscribeToLocationUpdates()
        if (::sharedPreferences.isInitialized) {
            SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
        super.onDestroy()
    }

    private fun initViews() {
        fabTracker = binding.fabTracker
        fabTracker.setOnClickListener {
            changeDriverActiveStatusInFirestore()
        }
    }

    private fun initObservers() {
        with(viewModel) {
            observe(changeDriverActiveStatusResult, ::handleChangeDriverActiveStatus)
        }
    }

    private fun initSharedPreference() {
        //Obteniendo sharedPreferences y poniendo un listener a cualquier cambio en esta key
        sharedPreferences =
            requireActivity().getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initAndBindLocationService() {
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        requireActivity().bindService(
            serviceIntent,
            locationServiceConnection,
            Context.BIND_AUTO_CREATE
        )
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(
                locationServiceBroadcastReceiver,
                IntentFilter(ACTION_LOCATION_BROADCAST)
            )
    }

    @SuppressLint("MissingPermission")
    private fun initMapCameraInCurrentLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            lastKnownLocation = task.result
            if (lastKnownLocation == null) {
                moveCamera(defaultLocation)
                showToastWithStringResource(R.string.can_not_get_location)
            } else {
                val latitude = lastKnownLocation!!.latitude
                val longitude = lastKnownLocation!!.longitude
                val latLng = LatLng(latitude, longitude)
                moveCamera(latLng)
            }
        }
    }


    private fun changeDriverActiveStatusInFirestore() {
        lifecycleScope.launch(Dispatchers.IO) {
            val isLocationServiceStarted = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                false
            )

            when (isLocationServiceStarted) {
                true -> {
                    viewModel.changeDriverActiveStatusInFirestore(false)
                }

                false -> {
                    viewModel.changeDriverActiveStatusInFirestore(true)
                }
            }
        }
    }

    private fun handleChangeDriverActiveStatus(result: Result<Boolean>?) {
        when (result) {
            is Result.OnError -> {}
            is Result.OnSuccess -> { initOrStopLocationService(result.value) }
            else -> {}
        }
    }

    private fun initOrStopLocationService(isActive: Boolean) {
        when (isActive) {
            false -> {
                locationService?.unsubscribeToLocationUpdates()
                showToastWithStringResource(R.string.tracker_stopped)
            }

            true -> {
                locationService?.subscribeToLocationUpdates()
                showToastWithStringResource(R.string.tracker_started)
            }
        }
    }

    private fun moveCamera(location: LatLng) {
        with(map) {
            clear()
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, driverDefaultZoom))
            addMarker(MarkerOptions().position(location))
        }
    }

    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_LOCATION_BROADCAST) return
            val location = intent.getParcelableExtra<Location>(NEW_LOCATION) ?: return
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            val latLng = LatLng(location.latitude, location.longitude)
            viewModel.saveLocationInFirestore(geoPoint)
            moveCamera(latLng)
        }
    }
}