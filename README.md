# Object Detection App with Jetpack Compose and TensorFlow Lite

This is a sample application that uses Jetpack Compose, TensorFlow Lite, and the SSD MobileNet model to perform real-time object detection on images.

## Preview


| <img src="/preview/previewimg.jpg" alt="Imagen de un móvil" width="300px" /> | <img src="/preview/preview2.jpg" alt="Imagen de un móvil" width="300px" /> |
| --- | --- |
|  IMG 1 | IMG 2 |

## Prerequisites

- Android Studio 4.0 or higher
- Basic knowledge of Jetpack Compose, TensorFlow Lite, and Kotlin

## Configuration

1. Clone the object detection application repository.
2. Open Android Studio and select "Open an existing project".
3. Navigate to the cloned folder and click "OK".
4. Wait for Android Studio to sync and build the project.

## Usage

1. Connect your Android device to your computer or use an emulator.
2. Run the application from Android Studio.
3. Once the application has started on your device, grant the camera permissions.
4. The application will automatically start detecting objects in real-time.

## Customizing the Model

If you want to use your own object detection model, follow these steps:

1. Place your TensorFlow Lite model file and dataset_labels.txt in the `app/src/main/ml` folder.
2. Modify the code in the `MainActivity` class to load your custom model. For example:
    ```kotlin
        companion object {
            private const val MODEL_FILE_NAME = "ssd_mobilenet_v1_1_metadata_1.tflite"
            private const val LABEL_FILE_NAME = "coco_dataset_labels_v1.txt"}

3. For newer TensorFlow models, modify the ObjectDetector class. For example:
    ```kotlin
        private val outputMap = mapOf(
        0 to outputScores,
        1 to outputBoundingBoxes,
        2 to outputDetectionNum,
        3 to outputLabels)

## Known Issues

- Object detection may be slow on older devices.
- Detection accuracy may vary depending on the quality of the model used.

## License

This project is licensed under the [MIT License](LICENSE).




