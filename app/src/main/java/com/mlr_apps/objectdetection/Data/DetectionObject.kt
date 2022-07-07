package com.mlr_apps.objectdetection.Data

import android.graphics.RectF

data class DetectionObject (
    val score: Float,
    val label: String,
    val boundingBox: RectF)