package com.example.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * High-performance, lifecycle-safe accelerometer shake detector.
 *
 * Computes dynamic linear acceleration magnitude:
 *   magnitude = |sqrt(x^2 + y^2 + z^2) - SensorManager.GRAVITY_EARTH|
 *
 * Applies configurable sensitivity thresholds and a cooldown debounce
 * to avoid erratic repeated triggers.
 */
class ShakeDetector(
    var threshold: Float = 9.5f,
    private val debounceMs: Long = 800L,
    private val onShakeListener: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTimestamp: Long = 0L
    private var isListening: Boolean = false

    var onAccelerationChanged: ((magnitude: Float) -> Unit)? = null

    /**
     * Registers the accelerometer listener. Call during onResume or DisposableEffect.
     */
    fun start(manager: SensorManager): Boolean {
        if (isListening) return true
        this.sensorManager = manager
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let { sensor ->
            val registered = manager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = registered
            return registered
        }
        return false
    }

    /**
     * Unregisters the accelerometer listener. Call during onPause or onDispose.
     */
    fun stop() {
        if (!isListening) return
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
            // Graceful handling during teardown
        } finally {
            sensorManager = null
            accelerometer = null
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val dynamicAcceleration = abs(totalAcceleration - SensorManager.GRAVITY_EARTH)

        // Notify live meter if attached
        onAccelerationChanged?.invoke(dynamicAcceleration)

        val now = System.currentTimeMillis()
        if (dynamicAcceleration >= threshold) {
            if (now - lastShakeTimestamp >= debounceMs) {
                lastShakeTimestamp = now
                onShakeListener()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for shake detection
    }
}
