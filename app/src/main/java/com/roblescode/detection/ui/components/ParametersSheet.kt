package com.roblescode.detection.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roblescode.detection.data.models.DetectionParameters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersSheet(
    modifier: Modifier = Modifier,
    params: DetectionParameters,
    onParamsChange: (DetectionParameters) -> Unit,
    onDismiss: () -> Unit,
) {
    var scoreThreshold by remember { mutableFloatStateOf(params.scoreThreshold) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(modifier = modifier.padding(16.dp).padding(bottom = 24.dp)) {
            Text("Score Threshold: %.2f".format(scoreThreshold))
            Slider(
                value = scoreThreshold,
                onValueChange = {
                    scoreThreshold = it
                    onParamsChange(params.copy(scoreThreshold = it))
                },
                valueRange = 0f..1f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
