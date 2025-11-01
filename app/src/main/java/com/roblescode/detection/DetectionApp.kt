package com.roblescode.detection

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DetectionApp:Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("DetectionApplication","onCreate")
    }
}