package com.cristobal.accenturetrackerexample.ui.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.ViewFlipper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.cristobal.accenturetrackerexample.R
import com.cristobal.accenturetrackerexample.data.sharedpreference.SharedPreferenceUtil
import com.cristobal.accenturetrackerexample.databinding.ActivityAuthenticationBinding
import com.cristobal.accenturetrackerexample.domain.domainobjects.AuthCredentials
import com.cristobal.accenturetrackerexample.domain.domainobjects.PermissionsValidation
import com.cristobal.accenturetrackerexample.domain.domainobjects.RequestAlertParams
import com.cristobal.accenturetrackerexample.domain.domainobjects.Result
import com.cristobal.accenturetrackerexample.ui.viewmodels.AuthenticationViewModel
import com.cristobal.accenturetrackerexample.utils.hideSoftKeyboard
import com.cristobal.accenturetrackerexample.utils.observe
import com.cristobal.accenturetrackerexample.utils.showCustomAlertDialog
import com.cristobal.accenturetrackerexample.utils.showToastWithString
import com.cristobal.accenturetrackerexample.utils.showToastWithStringResource
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationActivity : AppCompatActivity() {

    private val inputMethodManager by inject<InputMethodManager>()
    private val viewModel by viewModel<AuthenticationViewModel>()
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var loginButton: MaterialButton
    private lateinit var mailBox: TextInputLayout
    private lateinit var passwordBox: TextInputLayout

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.arePermissionsGranted(this)
    }

    private val locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val credentials = AuthCredentials(
                mailBox.editText?.text.toString(),
                passwordBox.editText?.text.toString()
            )

            if (result.resultCode == RESULT_OK) {
                viewModel.login(credentials)
            } else {
                checkingIfDeviceGpsIsOnBeforeLogin{ isGpsOn ->
                    if (isGpsOn) viewModel.login(credentials)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        initDataBinding()
        initViews()
        initObserver()
    }

    override fun onStart() {
        super.onStart()
        initPermissionsValidation()
    }

    private fun initDataBinding() {
        viewFlipper = binding.authenticationViewFlipper
        loginButton = binding.loginButton
        mailBox = binding.edittextEmail
        passwordBox = binding.edittextPassword
    }

    private fun initViews() {
        loginButton.setOnClickListener { initLoginProcess() }
    }

    private fun initObserver() {
        with(viewModel) {
            observe(arePermissionsGranted, ::handleArePermissionsGranted)
            observe(isAUserLogged, ::handleIsUserLoggedValidation)
            observe(loginValidation, ::handleLoginResult)
        }
    }

    private fun initPermissionsValidation() {
        if (SharedPreferenceUtil.arePermissionsRequested(this)) {
            viewFlipper.displayedChild = LoginStatus.LOADING.status
            viewModel.arePermissionsGranted(this)
        } else {
            SharedPreferenceUtil.savePermissionsWereRequested(this)
            val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.add(
                Manifest.permission.POST_NOTIFICATIONS
            )
            showCustomAlertDialog(
                R.string.fine_location_request_title,
                R.string.fine_location_request_message,
            ) {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    private fun handleArePermissionsGranted(result: PermissionsValidation?) {
        val permissionsNotGranted = result?.permissionsRefused

        permissionsNotGranted?.let {
            when (it.isEmpty()) {
                true -> {
                    viewModel.checkIfUserIsLogged()
                }

                false -> {
                    if (it.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showAppropriateAlertRequestingPermissions(
                            RequestAlertParams(
                                it,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                R.string.fine_location_request_title,
                                R.string.fine_location_request_message
                            )
                        )
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            showAppropriateAlertRequestingPermissions(
                                RequestAlertParams(
                                    it,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                    R.string.notification_request_title,
                                    R.string.notification_request_message,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showAppropriateAlertRequestingPermissions(
        requestAlertParams: RequestAlertParams
    ) {
        with(requestAlertParams) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this@AuthenticationActivity, permissionsShowRationale
            )

            showCustomAlertDialog(alertTitle, alertMessage) {
                if (showRationale) {
                    requestPermissionLauncher.launch(permissionRequestList.toTypedArray())
                } else {
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }

            }
        }

    }

    private fun handleIsUserLoggedValidation(result: Result<Boolean>?) {
        when (result) {
            is Result.OnError -> {
                showToastWithStringResource(R.string.error_checking_logged_status)
                viewFlipper.displayedChild = LoginStatus.DONE.status
            }

            is Result.OnSuccess -> {
                if (result.value) {
                    navigateToPrivateSite()
                } else {
                    viewFlipper.displayedChild = LoginStatus.DONE.status
                }
            }

            else -> {}
        }
    }

    private fun handleLoginResult(result: Result<Boolean>?) {
        when (result) {
            is Result.OnLoading -> {
                viewFlipper.displayedChild = LoginStatus.LOADING.status
            }

            is Result.OnError -> {
                Thread.sleep(2000)
                showToastWithString(result.throwable.message.toString())
                viewFlipper.displayedChild = LoginStatus.DONE.status
            }

            is Result.OnSuccess -> {
                navigateToPrivateSite()
            }

            else -> {}
        }
    }

    private fun navigateToPrivateSite() {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }

    private fun initLoginProcess() {
        hideSoftKeyboard(inputMethodManager)
        val credentials = AuthCredentials(
            mailBox.editText?.text.toString(),
            passwordBox.editText?.text.toString()
        )
        checkingIfDeviceGpsIsOnBeforeLogin { isGpsOn ->
            if (isGpsOn) viewModel.login(credentials)
        }
    }



// Example usage



    private fun checkingIfDeviceGpsIsOnBeforeLogin(callback: (Boolean) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener { callback(true)}
            .addOnFailureListener { exception ->
                callback(false)
                val statusCode = (exception as ResolvableApiException).statusCode
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    runCatching {
                        locationSettingsLauncher.launch(
                            IntentSenderRequest.Builder(
                                exception.resolution.intentSender
                            ).build()
                        )
                    }.onFailure {
                        showCustomAlertDialog(
                            R.string.atention,
                            R.string.turn_on_your_device_gps
                        ) {}
                    }
                }
            }
    }

    enum class LoginStatus(val status: Int) {
        LOADING(0),
        DONE(1),
    }
}


