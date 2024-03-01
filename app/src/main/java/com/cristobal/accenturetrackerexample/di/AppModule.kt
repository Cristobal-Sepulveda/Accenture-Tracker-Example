package com.cristobal.accenturetrackerexample.di

import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.cristobal.accenturetrackerexample.data.repositories.AuthRepository
import com.cristobal.accenturetrackerexample.data.repositories.RoomRepository
import com.cristobal.accenturetrackerexample.data.repositories.FirestoreRepository
import com.cristobal.accenturetrackerexample.data.room.getDatabase
import com.cristobal.accenturetrackerexample.domain.sources.AuthDataSource
import com.cristobal.accenturetrackerexample.domain.sources.RoomDataSource
import com.cristobal.accenturetrackerexample.domain.sources.FirestoreDataSource
import com.cristobal.accenturetrackerexample.domain.usecases.SyncUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SignInUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SignOutUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.GetUserDataUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.SaveLocationInFirestoreUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.ChangeDriverActiveStatusInFirestoreUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.FirebaseSnapshotUseCase
import com.cristobal.accenturetrackerexample.domain.usecases.PermissionsValidatorUseCase
import com.cristobal.accenturetrackerexample.ui.viewmodels.AuthenticationViewModel
import com.cristobal.accenturetrackerexample.ui.viewmodels.MainViewModel
import com.cristobal.accenturetrackerexample.ui.viewmodels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val appModule = module {
    /* Android Services */
    single { androidContext().getSystemService<InputMethodManager>() }

    /* Dispatchers */
    single { Dispatchers.IO as CoroutineContext }

    /*Firebase*/
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    /* Repositories */
    singleOf(::AuthRepository) { bind<AuthDataSource>() }
    singleOf(::RoomRepository) { bind<RoomDataSource>() }
    singleOf(::FirestoreRepository) { bind<FirestoreDataSource>() }

    /* Room Database*/
    single { getDatabase(androidContext()) }

    /*Use cases*/
    factoryOf(::SyncUseCase)
    factoryOf(::SignInUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::GetUserDataUseCase)
    factoryOf(::SaveLocationInFirestoreUseCase)
    factoryOf(::ChangeDriverActiveStatusInFirestoreUseCase)
    factoryOf(::FirebaseSnapshotUseCase)
    factoryOf(::PermissionsValidatorUseCase)

    /* ViewModels */
    viewModelOf(::AuthenticationViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
}