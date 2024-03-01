package com.cristobal.accenturetrackerexample.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.databinding.FragmentSupervisorBinding
import com.cristobal.accenturetrackerexample.domain.domainobjects.DriverUiData
import com.cristobal.accenturetrackerexample.ui.viewmodels.HomeViewModel
import com.cristobal.accenturetrackerexample.utils.ADDED
import com.cristobal.accenturetrackerexample.utils.MODIFIED
import com.cristobal.accenturetrackerexample.utils.defaultLocation
import com.cristobal.accenturetrackerexample.utils.supervisorDefaultZoom
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SupervisorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSupervisorBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var fabSupervisorUp: FloatingActionButton
    private lateinit var fabSupervisorMapMode: FloatingActionButton
    private val viewModel by sharedViewModel<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSupervisorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        initMap()
        initSnapshotListener()
        observeSnapshotListener()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        moveCameraToDefaultLocation()
    }

    private fun initViews() {
        fabSupervisorUp = binding.fabSupervisorUp
        fabSupervisorMapMode = binding.fabSupervisorMapMode

        fabSupervisorUp.setOnClickListener {
            moveCameraToDefaultLocation()
        }
        fabSupervisorMapMode.setOnClickListener {
            changeMapType()
        }
    }

    private fun initMap() {
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
    }

    private fun initSnapshotListener() = viewModel.startSnapshotForLocationUpdates()

    private fun observeSnapshotListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.driverDataFirestoreSnapshotFlow.collect { flowDataState ->
                    flowDataState.forEach { driverFireStoreCurrentCaseData ->
                        val userEmail = driverFireStoreCurrentCaseData.user
                        val currentLocation = driverFireStoreCurrentCaseData.currentLatLng
                        val speedInKmPerHour = driverFireStoreCurrentCaseData.getSpeedInKmPerHour()
                        val isUserActive = driverFireStoreCurrentCaseData.isActive

                        when (driverFireStoreCurrentCaseData.documentLastAction) {
                            ADDED -> {
                                if (isUserActive) {
                                    val marker = addMarkerToMap(
                                        userEmail,
                                        currentLocation,
                                        speedInKmPerHour
                                    )
                                    marker?.let {
                                        viewModel.driverUiData[userEmail] = DriverUiData(
                                            userEmail,
                                            it,
                                            speedInKmPerHour
                                        )
                                    }
                                }
                            }

                            MODIFIED -> {
                                if (!isUserActive) {
                                    viewModel.driverUiData[userEmail]?.marker?.remove()
                                    viewModel.driverUiData.remove(userEmail)
                                } else {
                                    if (viewModel.driverUiData[userEmail] == null) {
                                        val marker = addMarkerToMap(
                                            userEmail,
                                            currentLocation,
                                            speedInKmPerHour
                                        )
                                        marker?.let {
                                            viewModel.driverUiData[userEmail] = DriverUiData(
                                                userEmail,
                                                it,
                                                speedInKmPerHour
                                            )
                                        }
                                    } else {
                                        viewModel.driverUiData[userEmail]?.marker?.apply {
                                            position = currentLocation
                                            title = "Driver: $userEmail"
                                            snippet = speedInKmPerHour
                                        }
                                    }
                                }

                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun addMarkerToMap(
        userEmail: String,
        latLng: LatLng,
        speed: String
    ): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Driver: $userEmail")
                .snippet(speed)
        )
    }

    private fun moveCameraToDefaultLocation() {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                defaultLocation,
                supervisorDefaultZoom
            )
        )
    }

    private fun changeMapType() {
        when (map.mapType) {
            GoogleMap.MAP_TYPE_SATELLITE -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
            GoogleMap.MAP_TYPE_NORMAL -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
    }
}