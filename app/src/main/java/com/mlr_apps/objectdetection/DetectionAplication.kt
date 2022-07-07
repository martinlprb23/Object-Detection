package com.mlr_apps.objectdetection

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DetectionAplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}