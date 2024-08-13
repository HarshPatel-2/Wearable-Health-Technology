package com.example.plhi_har.presentation

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.Interpreter

//
//class com.example.plhi_har.presentation.SensorDataProcessor {
//    private val dataBuffer = mutableListOf<FloatArray>()
//    private val windowSize = 128 // Number of samples per window
//    lateinit var tfliteInterpreter: Interpreter  // Ensure it's initialized before use
//
//    fun addToBuffer(values: FloatArray) {
//        if (dataBuffer.size < windowSize) {
//            dataBuffer.add(values)
//        } else {
//            val window = preprocessData(dataBuffer)
//            performInference(window)
//            dataBuffer.clear()
//        }
//    }
//
//    private fun preprocessData(buffer: List<FloatArray>): FloatArray {
//        // Manually concatenate float arrays
//        return buffer.reduce { acc, floats -> acc + floats }
//    }
//
//    private fun performInference(inputData: FloatArray) {
//        val inputBuffer = ByteBuffer.allocateDirect(inputData.size * 4).order(ByteOrder.nativeOrder())
//        inputData.forEach { inputBuffer.putFloat(it) }
//        inputBuffer.rewind()
//
//        val output = Array(1) { FloatArray(1) } // Adjust based on your model's output shape
//        tfliteInterpreter.run(inputBuffer, output)
//
//        // Handle output
//        updateUI(output[0])
//
//    }
//    private fun updateUI(activity: FloatArray) {
//        // Implementation depends on how you want to display it
//    }
//
//}

//class com.example.plhi_har.presentation.SensorDataProcessor {
//    private val dataBuffer = mutableListOf<FloatArray>()
//    private val windowSize = 128 // Number of samples per window
//    lateinit var tfliteInterpreter: Interpreter  // Ensure it's initialized before use
//
//    fun addToBuffer(values: FloatArray) {
//        if (dataBuffer.size < windowSize) {
//            dataBuffer.add(values)
//        } else {
//            val window = preprocessData(dataBuffer)
//            performInference(window)
//            dataBuffer.clear()
//        }
//    }
//
//    private fun preprocessData(buffer: List<FloatArray>): FloatArray {
//        // Manually concatenate float arrays
//        return buffer.reduce { acc, floats -> acc + floats }
//    }
//
//    private fun performInference(inputData: FloatArray) {
//        val inputBuffer = ByteBuffer.allocateDirect(inputData.size * 4).order(ByteOrder.nativeOrder())
//        inputData.forEach { inputBuffer.putFloat(it) }
//        inputBuffer.rewind()
//
//        val output = Array(1) { FloatArray(1) } // Adjust based on your model's output shape
//        tfliteInterpreter.run(inputBuffer, output)
//
//        // Handle output
//        updateUI(output[0])
//    }
//
//    private fun updateUI(activity: FloatArray) {
//        // Implementation depends on how you want to display it
//    }
//}

class SensorDataProcessor(private val updateActivityState: (String) -> Unit, private val windowSize: Int = 128) {
    private val dataBuffer = mutableListOf<FloatArray>()
    var tfliteInterpreter: Interpreter? = null

    fun addToBuffer(values: FloatArray) {
        if (dataBuffer.size < windowSize) {
            dataBuffer.add(values)
        } else {
            val window = preprocessData(dataBuffer)
            performInference(window)
            dataBuffer.clear()
        }
    }

    private fun preprocessData(buffer: List<FloatArray>): FloatArray {
        return buffer.flatMap { it.asList() }.toFloatArray()
    }

    private fun performInference(inputData: FloatArray) {
        val inputBuffer = ByteBuffer.allocateDirect(inputData.size * 4).order(ByteOrder.nativeOrder())
        inputData.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(activityNames.size) }
        tfliteInterpreter?.run(inputBuffer, output)

        handleOutput(output)
    }

    private fun handleOutput(output: Array<FloatArray>) {
        val activity = decodeActivity(output[0])
        updateActivityState(activity)
    }

    private fun decodeActivity(outputArray: FloatArray): String {
        val maxIndex = outputArray.indexOfMax()
        return activityNames.getOrElse(maxIndex) { "Unknown Activity" }
    }

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        var maxValue = Float.NEGATIVE_INFINITY
        for (i in indices) {
            if (this[i] > maxValue) {
                maxValue = this[i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    companion object {
        private val activityNames = listOf(
            "Cycling",
            "Jogging",
            "Jump Rope",
            "Push-up",
            "Run",
            "Sit-up",
            "Table Tennis",
            "Walk",
            "Walking Downstairs",
            "Walking Upstairs"
        )
    }
}