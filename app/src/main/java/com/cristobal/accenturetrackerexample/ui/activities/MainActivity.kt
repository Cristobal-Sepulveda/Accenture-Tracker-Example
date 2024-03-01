package com.cristobal.accenturetrackerexample.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.databinding.ActivityMainBinding
import com.cristobal.accenturetrackerexample.domain.domainobjects.PermissionsValidation
import com.cristobal.accenturetrackerexample.ui.viewmodels.MainViewModel
import com.cristobal.accenturetrackerexample.utils.observe
import com.cristobal.accenturetrackerexample.utils.showCustomAlertDialog
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: MaterialToolbar
    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showCustomAlertDialog(
                        R.string.atention,
                        R.string.gps_turn_off_in_private_site
                    ){
                        exitPrivateSite()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initDataBinding()
        initViews()
        initObserver()
        initSystemChangesBroadcastReceiver()
    }

    override fun onStart(){
        super.onStart()
        initPermissionsValidation()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsStatusReceiver)
    }

    private fun initDataBinding() {
        toolbar = binding.toolbar
    }

    private fun initViews() {
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    exitPrivateSite()
                    true
                }

                else -> false
            }
        }
    }

    private fun initObserver() {
        with(viewModel) {
            observe(arePermissionsGranted, ::handleArePermissionsGranted)
        }
    }

    private fun initSystemChangesBroadcastReceiver() {
        registerReceiver(
            gpsStatusReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    private fun initPermissionsValidation() = viewModel.arePermissionsGranted(this)

    private fun handleArePermissionsGranted(result: PermissionsValidation?) {
        val permissionsNotGranted = result?.permissionsRefused
        permissionsNotGranted?.let {
            if(it.isNotEmpty()) exitPrivateSite()
        }
    }

    private fun exitPrivateSite() {
        viewModel.logout()
        startActivity(Intent(this, AuthenticationActivity::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
    }
}