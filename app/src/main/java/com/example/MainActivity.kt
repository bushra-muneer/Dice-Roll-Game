package com.example

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sensor.ShakeDetector
import com.example.ui.DiceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DiceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val uiState by viewModel.uiState.collectAsState()

                    // Reusable ShakeDetector with debounced callback
                    val shakeDetector = remember {
                        ShakeDetector(
                            threshold = uiState.shakeSensitivity.threshold,
                            debounceMs = 800L
                        ) {
                            viewModel.rollDice(triggeredByShake = true)
                        }
                    }

                    // Dynamically keep threshold in sync with user preferences
                    LaunchedEffect(uiState.shakeSensitivity) {
                        shakeDetector.threshold = uiState.shakeSensitivity.threshold
                    }

                    // Connect live acceleration values to UI meter
                    DisposableEffect(shakeDetector) {
                        shakeDetector.onAccelerationChanged = { force ->
                            viewModel.updateLiveAcceleration(force)
                        }
                        onDispose {
                            shakeDetector.onAccelerationChanged = null
                        }
                    }

                    // Lifecycle-aware sensor management: start on ON_RESUME, stop on ON_PAUSE
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> {
                                    val active = shakeDetector.start(sensorManager)
                                    viewModel.updateSensorActive(active)
                                }
                                Lifecycle.Event.ON_PAUSE -> {
                                    shakeDetector.stop()
                                    viewModel.updateSensorActive(false)
                                    viewModel.updateLiveAcceleration(0f)
                                }
                                else -> Unit
                            }
                        }

                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            shakeDetector.stop()
                            viewModel.updateSensorActive(false)
                        }
                    }

                    DiceScreen(viewModel = viewModel)
                }
            }
        }
    }
}
