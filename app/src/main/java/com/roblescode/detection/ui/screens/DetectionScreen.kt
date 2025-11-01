package com.roblescode.detection.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import com.roblescode.detection.data.utils.Response
import com.roblescode.detection.ui.components.CameraPermissionWrapper
import com.roblescode.detection.ui.components.CameraPreview
import com.roblescode.detection.ui.components.DrawBoxes
import com.roblescode.detection.ui.components.FloatingButton
import com.roblescode.detection.ui.components.ParametersSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjDetectionScreen(modifier: Modifier = Modifier, viewModel: DetectionViewModel) {
    val context = LocalContext.current

    val paint = Paint()
    val pathColorList = listOf(Color.Green, Color.Cyan, Color.Red, Color.Yellow)
    val showProperties = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.initialize() }

    Scaffold(
        contentWindowInsets = WindowInsets.ime,
        floatingActionButton = {
            FloatingButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = { showProperties.value = !showProperties.value }
            )
        },
    ) { innerPadding ->
        CameraPermissionWrapper(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (val state = viewModel.initialState.collectAsState().value) {
                is Response.Loading -> CircularProgressIndicator()
                is Response.Success<Unit> -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val boxConstraint = this
                        val density = LocalDensity.current
                        val sizeWith = with(density) { boxConstraint.maxWidth.toPx() }
                        val sizeHeight = with(density) { boxConstraint.maxHeight.toPx() }

                        CameraPreview { imageAnalysis ->
                            imageAnalysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                viewModel.buildAnalyzer(sizeWith, sizeHeight)
                            )
                        }

                        DrawBoxes(
                            paint = paint,
                            pathColorList = pathColorList,
                            detectionListObject = viewModel.detectionList.value
                        )
                    }
                }

                is Response.Failure -> {
                    Text(state.exception?.message ?: "An unknown error occurred")
                }
            }
        }
    }

    if (showProperties.value) {
        ParametersSheet(
            params = viewModel.parameters.collectAsState().value,
            onParamsChange = viewModel::updateParams,
            onDismiss = { showProperties.value = false },
        )
    }
}