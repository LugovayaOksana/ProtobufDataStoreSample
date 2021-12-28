package com.example.protobuf

import android.app.Application
import com.example.protobuf.data.di.DI
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogger()
        DI.init(this)
    }

    private fun setupLogger() {
        Timber.plant(Timber.DebugTree())
    }
}