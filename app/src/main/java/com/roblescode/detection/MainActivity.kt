package com.roblescode.detection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.roblescode.detection.ui.navigation.AppNavHost
import com.roblescode.detection.ui.screens.DetectionViewModel
import com.roblescode.detection.ui.theme.ObjectDetectionTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val detectionViewModel by viewModels<DetectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ObjectDetectionTheme {
                val navController = rememberNavController()
                cameraExecutor = Executors.newSingleThreadExecutor()
                AppNavHost(navController = navController, detectionViewModel = detectionViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private lateinit var cameraExecutor: ExecutorService
}