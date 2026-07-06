package com.example.nipo

import android.app.Application
import com.google.android.libraries.places.api.Places

class Nipo : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }
}