package com.cristobal.accenturetrackerexample

import android.app.Application
import com.cristobal.accenturetrackerexample.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            androidLogger()
            androidFileProperties()
            FirebaseApp.initializeApp(this@MyApp)
            modules(
                appModule
            )
        }
    }
}