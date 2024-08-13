/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.plhi_har.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.plhi_har.presentation.theme.PLHI_HARTheme
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import android.content.res.AssetFileDescriptor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private val activityState = mutableStateOf("Initializing...")
    private val sensorDataProcessor = SensorDataProcessor(
        updateActivityState = { activity ->
            runOnUiThread { activityState.value = activity }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(activityState.value)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val sensorDelay = SensorManager.SENSOR_DELAY_GAME

        sensorManager.registerListener(this, accelerometer, sensorDelay)
        sensorManager.registerListener(this, gyroscope, sensorDelay)

        try {
            val model = loadModelFile("assets/model/mobilehart_model.tflite")
            sensorDataProcessor.tfliteInterpreter = Interpreter(model)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(modelPath: String): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            sensorDataProcessor.addToBuffer(it.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes here
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        sensorDataProcessor.tfliteInterpreter?.close()
    }
}


//@Composable
//fun ActivityDisplay(activity: String) {
//    Text(
//        modifier = Modifier.fillMaxWidth(),
//        textAlign = TextAlign.Center,
//        color = MaterialTheme.colors.primary,
//        text = activity
//    )
//}
//
//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp("Preview Android")
//}

@Composable
fun WearApp(activity: String) {
    PLHI_HARTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ActivityDisplay(activity)
                TimeDisplay()
            }
        }
    }
}

@Composable
fun ActivityDisplay(activity: String) {
    Text(
        text = activity,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            letterSpacing = 0.15.sp
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSurface
    )
}

@Composable
fun TimeDisplay() {
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedTime += 1
        }
    }

    Text(
        text = formatElapsedTime(elapsedTime),
        style = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 25.sp,
            letterSpacing = 0.1.sp
        ),
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(8.dp)
    )
}

private fun formatElapsedTime(seconds: Long): String {
    val hours = (seconds / 3600).toInt()
    val minutes = ((seconds % 3600) / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}