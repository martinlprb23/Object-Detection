package com.roblescode.detection.core

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import com.roblescode.detection.data.models.DetectionObject
import com.roblescode.detection.data.models.DetectionParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op

typealias ObjectDetectorCallback = (image: List<DetectionObject>) -> Unit

class ObjectDetector(
    private val yuvToRgbConverter: YuvToRgbConverter,
    private val interpreter: Interpreter,
    private val labels: List<String>,
    private val resultViewSize: Size,
    private val listener: ObjectDetectorCallback,
) : ImageAnalysis.Analyzer {

    companion object {
        private const val IMG_SIZE_X = 300
        private const val IMG_SIZE_Y = 300
        private const val MAX_DETECTION_NUM = 10
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 1f
    }

    private var imageRotationDegrees: Int = 0
    private var reusableBitmap: Bitmap? = null
    private val tfImageBuffer = TensorImage(DataType.UINT8)

    // Preprocessor cached unless rotation changes
    private var lastRotation = -1
    private var tfImageProcessor: ImageProcessor? = null

    // Preallocated output buffers
    private val outputBoundingBoxes = arrayOf(Array(MAX_DETECTION_NUM) { FloatArray(4) })
    private val outputLabels = arrayOf(FloatArray(MAX_DETECTION_NUM))
    private val outputScores = arrayOf(FloatArray(MAX_DETECTION_NUM))
    private val outputDetectionNum = FloatArray(1)

    //Change for new models version TensorFlow
    //0 to outputScores,
    //1 to outputBoundingBoxes,
    //2 to outputDetectionNum,
    //3 to outputLabels
    private val outputMap = mapOf(
        0 to outputBoundingBoxes,
        1 to outputLabels,
        2 to outputScores,
        3 to outputDetectionNum
    )

    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private var params = DetectionParameters()
    fun updateParams(params: DetectionParameters) {
        this.params = params
    }


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return
        imageRotationDegrees = image.imageInfo.rotationDegrees

        // Process on background thread
        backgroundScope.launch {
            val detections = detect(mediaImage)
            listener(detections)
            image.close()
        }
    }

    private fun detect(targetImage: Image): List<DetectionObject> {
        // Reuse bitmap if size matches, otherwise recreate
        val bitmap = reusableBitmap?.takeIf {
            it.width == targetImage.width && it.height == targetImage.height
        } ?: createBitmap(targetImage.width, targetImage.height).also {
            reusableBitmap = it
        }

        yuvToRgbConverter.yuvToRgb(targetImage, bitmap)

        // Rebuild processor only if rotation changed
        if (tfImageProcessor == null || lastRotation != imageRotationDegrees) {
            tfImageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(IMG_SIZE_X, IMG_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
                .add(Rot90Op(-imageRotationDegrees / 90))
                .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD))
                .build()
            lastRotation = imageRotationDegrees
        }

        tfImageBuffer.load(bitmap)
        val tensorImage = tfImageProcessor!!.process(tfImageBuffer)

        // Inference
        interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputMap)

        // Parse results
        val results = mutableListOf<DetectionObject>()
        val detectionCount = outputDetectionNum[0].toInt().coerceAtMost(MAX_DETECTION_NUM)

        for (i in 0 until detectionCount) {
            val score = outputScores[0][i]
            if (score < params.scoreThreshold) continue

            val labelIndex = outputLabels[0][i].toInt().coerceIn(labels.indices)
            val label = labels[labelIndex]
            val box = outputBoundingBoxes[0][i]
            val rect = RectF(
                box[1] * resultViewSize.width,
                box[0] * resultViewSize.height,
                box[3] * resultViewSize.width,
                box[2] * resultViewSize.height
            )

            results.add(DetectionObject(score, label, rect))
        }

        return results.take(4)
    }
}
