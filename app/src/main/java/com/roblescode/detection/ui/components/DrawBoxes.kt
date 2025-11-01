package com.roblescode.detection.ui.components

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.roblescode.detection.data.models.DetectionObject

@Composable
fun DrawBoxes(
    modifier: Modifier = Modifier,
    paint: Paint,
    pathColorList: List<Color>,
    detectionListObject: List<DetectionObject>,
) {
    Canvas(
        modifier = modifier.fillMaxSize(),
        onDraw = {
            detectionListObject.mapIndexed { i, detectionObject ->
                Log.d("Object", detectionObject.label + " --- " + detectionObject.score)
                paint.apply {
                    color = pathColorList[i].toArgb()
                    style = Paint.Style.FILL
                    isAntiAlias = true
                    textSize = 50f
                }

                drawRect(
                    color = pathColorList[i],
                    topLeft = Offset(
                        x = detectionObject.boundingBox.left,
                        y = detectionObject.boundingBox.top
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = detectionObject.boundingBox.width(),
                        height = detectionObject.boundingBox.height()
                    ),
                    style = Stroke(width = 3.dp.toPx())
                )

                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        detectionObject.label + " " + "%,.2f".format(detectionObject.score * 100) + "%",
                        detectionObject.boundingBox.left,            // x-coordinates of the origin (top left)
                        detectionObject.boundingBox.top - 5f, // y-coordinates of the origin (top left)
                        paint
                    )
                }
            }
        }
    )
}