package com.roblescode.detection.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roblescode.detection.ui.constants.Routes
import com.roblescode.detection.ui.screens.DetectionViewModel
import com.roblescode.detection.ui.screens.ObjDetectionScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    detectionViewModel: DetectionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DETECTION,
        modifier = modifier
    ) {
        composable(Routes.DETECTION) {
            ObjDetectionScreen(viewModel = detectionViewModel)
        }
    }

}