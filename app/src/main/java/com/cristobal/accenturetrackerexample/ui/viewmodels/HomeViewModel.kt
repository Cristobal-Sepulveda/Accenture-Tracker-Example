package com.cristobal.accenturetrackerexample.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristobal.accenturetrackerexample.data.models.LiveCompletable
import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.domainobjects.DriverFirestoreCurrentCaseData
import com.cristobal.accenturetrackerexample.domain.domainobjects.DriverUiData
import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData
import com.cristobal.accenturetrackerexample.domain.usecases.ChangeDriverActiveStatusInFirestoreUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.FirebaseSnapshotUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.GetUserDataUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SaveLocationInFirestoreUseCase
import com.cristobal.accenturetrackerexample.utils.execute
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val saveLocationInFirestoreUseCase: SaveLocationInFirestoreUseCase,
    private val changeDriverActiveStatusInFirestoreUseCase: ChangeDriverActiveStatusInFirestoreUseCase,
    private val firebaseSnapshotUseCase: FirebaseSnapshotUseCase
) : ViewModel() {

    private val savingInFirestoreRequest = LiveCompletable<Nothing>()
    private val _driverDataFirestoreSnapshotFlow = MutableStateFlow(
        emptyList<DriverFirestoreCurrentCaseData>()
    )
    val driverDataFirestoreSnapshotFlow: StateFlow<List<DriverFirestoreCurrentCaseData>> =
        _driverDataFirestoreSnapshotFlow
    val driverUiData = HashMap<String, DriverUiData>()
    val currentUserEmailAndProfileType = LiveResult<UserData>()
    val changeDriverActiveStatusResult = LiveResult<Boolean>()

    fun getUserEmailAndProfileType() {
        execute(
            useCase = getUserDataUseCase,
            params = "",
            liveData = currentUserEmailAndProfileType
        )
    }

    fun saveLocationInFirestore(geoPoint: GeoPoint) {
        execute(
            useCase = saveLocationInFirestoreUseCase,
            params = geoPoint,
            liveData = savingInFirestoreRequest
        )
    }

    fun changeDriverActiveStatusInFirestore(isActive: Boolean) {
        execute(
            useCase = changeDriverActiveStatusInFirestoreUseCase,
            params = isActive,
            liveData = changeDriverActiveStatusResult
        )
    }

    fun startSnapshotForLocationUpdates() {
        viewModelScope.launch {
            firebaseSnapshotUseCase.execute()
                .collect { snapshot ->
                    _driverDataFirestoreSnapshotFlow.value = snapshot
                }
        }
    }
}