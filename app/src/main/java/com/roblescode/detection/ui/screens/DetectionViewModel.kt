package com.roblescode.detection.ui.screens

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.roblescode.detection.core.ObjectDetector
import com.roblescode.detection.core.YuvToRgbConverter
import com.roblescode.detection.data.models.DetectionObject
import com.roblescode.detection.data.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        const val TAG = "DetectionViewModel"

        const val MODEL_FILE_NAME = "ssd_mobilenet_v1_1_metadata_1.tflite"
        const val LABEL_FILE_NAME = "coco_dataset_labels_v1.txt"
    }

    lateinit var interpreter: Interpreter
    lateinit var yuvToRgbConverter: YuvToRgbConverter
    var labels: List<String> = emptyList()

    private val state = MutableStateFlow<Response<Unit>>((Response.Loading))
    val initialState = state.asStateFlow()

    var detectionList = mutableStateOf<List<DetectionObject>>(listOf())

    fun initialize() {
        try {
            state.value = Response.Loading
            val modelByteBuffer = loadModel(MODEL_FILE_NAME)
            labels = loadLabels(LABEL_FILE_NAME)
            yuvToRgbConverter = YuvToRgbConverter(context)
            interpreter = Interpreter(modelByteBuffer!!, null)
            state.value = Response.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "initialize: ", e)
            state.value = Response.Failure(e)
        }
    }

    fun loadModel(fileName: String): ByteBuffer? {
        return try {
            val modelFile = context.assets.openFd(fileName)
            val inputStream = FileInputStream(modelFile.fileDescriptor)
            val fileChannel = inputStream.channel
            fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                modelFile.startOffset,
                modelFile.declaredLength
            )
        } catch (e: Exception) {
            Log.e(TAG, "loadModel: ", e)
            null
        }
    }

    fun loadLabels(fileName: String): List<String> {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLines()
        } catch (e: Exception) {
            Log.e(TAG, "loadLabels: ", e)
            emptyList()
        }
    }

    fun buildAnalyzer(sizeWith: Float, sizeHeight: Float): ObjectDetector {
        return ObjectDetector(
            yuvToRgbConverter = yuvToRgbConverter,
            interpreter = interpreter,
            labels = labels,
            resultViewSize = Size(sizeWith.toInt(), sizeHeight.toInt()),
            listener = ::setList,
        )
    }


    fun setList(detectedObjectList: List<DetectionObject>) {
        if (detectedObjectList.isEmpty()) return
        detectionList.value = detectedObjectList
    }
}