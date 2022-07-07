package com.mlr_apps.objectdetection

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.mlr_apps.objectdetection.Data.DetectionObject
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
    private val listener: ObjectDetectorCallback
) : ImageAnalysis.Analyzer {

    companion object {
        // Tamaño de entrada y salida del modelo
        private const val IMG_SIZE_X = 300 //300
        private const val IMG_SIZE_Y = 300 //300
        private const val MAX_DETECTION_NUM = 10//10

        // Dado que el modelo tflite utilizado esta vez ha sido cuantificado, la normalización relacionada no es 127.5f sino la siguiente
        private const val NORMALIZE_MEAN = 0f//0f
        private const val NORMALIZE_STD = 1f//1f

        // Umbral de puntuación de resultado de detección
        private const val SCORE_THRESHOLD = 0.6f
    }

    private var imageRotationDegrees: Int = 0
    private val tfImageProcessor by lazy {
        ImageProcessor.Builder()
            .add(ResizeOp(IMG_SIZE_X, IMG_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR)) // Cambiar el tamaño de la imagen para que se ajuste a la entrada del modelo
            .add(Rot90Op(-imageRotationDegrees / 90)) // El proxy de imagen que fluye se gira 90 grados
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // Relacionado con la normalización
            .build()
    }

    private val tfImageBuffer = TensorImage(DataType.UINT8)
    //private val tfImageBuffer = TensorImage(DataType.FLOAT32)

    // Cuadro delimitador de resultado de detección [1:10:4]
    // El cuadro delimitador tiene la forma de [arriba, izquierda, abajo, derecha]
    private val outputBoundingBoxes: Array<Array<FloatArray>> = arrayOf(
        Array(MAX_DETECTION_NUM) {
            FloatArray(4) //4
        }
    )

    // Índice de etiqueta de clase de resultado de detección [1:10]
    private val outputLabels: Array<FloatArray> = arrayOf(
        FloatArray(MAX_DETECTION_NUM)
    )

    // Cada puntuación del resultado de detección [1:10]
    private val outputScores: Array<FloatArray> = arrayOf(
        FloatArray(MAX_DETECTION_NUM)
    )

    // Número de objetos detectados (10 (constante) porque esta vez se configuró en el momento de la conversión de tflite)
    private val outputDetectionNum: FloatArray = FloatArray(1)

    // Juntar en un mapa para recibir resultados de detección
    private val outputMap = mapOf(
        0 to outputBoundingBoxes,
        1 to outputLabels,
        2 to outputScores,
        3 to outputDetectionNum

    //Change for new models version TensorFlow
    //0 to outputScores,
    //1 to outputBoundingBoxes,
    //2 to outputDetectionNum,
    //3 to outputLabels
    )

    // Infiera la imagen de vista previa que fluye desde cameraX colocándola en el modelo de detección de objetos.
    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        if (image.image == null) return
        imageRotationDegrees = image.imageInfo.rotationDegrees
        val detectedObjectList = detect(image.image!!)
        listener(detectedObjectList)
        image.close()
    }

    // Convierta la imagen a YUV-> mapa de bits RGB-> tensorflowImage-> tensorflowBuffer,
    // infiera y envíe el resultado como una lista

    private fun detect(targetImage: Image): List<DetectionObject> {
        val targetBitmap = Bitmap.createBitmap(targetImage.width, targetImage.height, Bitmap.Config.ARGB_8888)
        yuvToRgbConverter.yuvToRgb(targetImage, targetBitmap) // conversion a rgb
        tfImageBuffer.load(targetBitmap)
        val tensorImage = tfImageProcessor.process(tfImageBuffer)

        //tflite Realización de inferencias en el modelo
        interpreter.runForMultipleInputsOutputs(arrayOf(tensorImage.buffer), outputMap)

        // Dar formato al resultado de la inferencia y devolverlo como una lista
        val detectedObjectList = arrayListOf<DetectionObject>()
        loop@ for (i in 0 until outputDetectionNum[0].toInt()) {
            val score = outputScores[0][i]
            val label = labels[outputLabels[0][i].toInt()]
            val boundingBox = RectF(
                outputBoundingBoxes[0][i][1] * resultViewSize.width,
                outputBoundingBoxes[0][i][0] * resultViewSize.height,
                outputBoundingBoxes[0][i][3] * resultViewSize.width,
                outputBoundingBoxes[0][i][2] * resultViewSize.height
            )

            // Agregua solo aquellos que son más grandes que el umbral
            if (score >= SCORE_THRESHOLD) {
                detectedObjectList.add(
                    DetectionObject(
                        score = score,
                        label = label,
                        boundingBox = boundingBox
                    )
                )
            } else {
                    // Los resultados de la detección se clasifican en orden descendente de puntuación,
                    // por lo que si se supera el umbral, el bucle finaliza.
                break@loop
            }
        }
        return detectedObjectList.take(4)
    }
}
