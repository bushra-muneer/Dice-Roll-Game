package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Manages tactile haptic feedback and synthesized physical dice acoustic effects.
 */
class DiceFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Pre-synthesized PCM sound buffers (sample rate 44100Hz, 16-bit PCM mono)
    private val sampleRate = 44100
    private val clickPcmData: ByteArray = synthesizeDiceClick()
    private val impactPcmData: ByteArray = synthesizeDiceImpact()

    /**
     * Subtle tactile tick while dice tumble.
     */
    fun performTumbleTick(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10L)
            }
        } catch (_: Exception) {
            // Ignore if vibration is restricted
        }
    }

    /**
     * Firm tactile impact when dice settle.
     */
    fun performSettleImpact(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35L)
            }
        } catch (_: Exception) {
            // Ignore if vibration is restricted
        }
    }

    /**
     * Play light clatter sound during roll.
     */
    fun playTumbleSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            playPcmBuffer(clickPcmData)
        }
    }

    /**
     * Play solid dice impact sound when settling.
     */
    fun playImpactSound(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            playPcmBuffer(impactPcmData)
        }
    }

    private fun playPcmBuffer(buffer: ByteArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Clean up after completion
            audioScope.launch {
                val durationMs = (buffer.size.toFloat() / (sampleRate * 2) * 1000).toLong() + 50L
                kotlinx.coroutines.delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Graceful fallback if device audio system is unavailable
        }
    }

    companion object {
        /**
         * Synthesize a crisp 40ms dice collision click with acoustic resonance.
         */
        private fun synthesizeDiceClick(): ByteArray {
            val sampleRate = 44100
            val durationMs = 45
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Exponential decay envelope
                val envelope = exp(-t * 120.0)
                // Dual frequency resonant ping (simulating acrylic/wood click)
                val freq1 = 1800.0 + Random.nextDouble(-100.0, 100.0)
                val freq2 = 2900.0
                val sampleValue = (sin(2 * PI * freq1 * t) * 0.7 + sin(2 * PI * freq2 * t) * 0.3) * envelope
                val intSample = (sampleValue * 32767 * 0.65).toInt().coerceIn(-32768, 32767)

                buffer[i * 2] = (intSample and 0xFF).toByte()
                buffer[i * 2 + 1] = ((intSample shr 8) and 0xFF).toByte()
            }
            return buffer
        }

        /**
         * Synthesize an 80ms heavier tabletop impact thud.
         */
        private fun synthesizeDiceImpact(): ByteArray {
            val sampleRate = 44100
            val durationMs = 85
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = exp(-t * 60.0)
                // Low-mid wooden table resonance
                val freq1 = 280.0
                val freq2 = 520.0
                val sampleValue = (sin(2 * PI * freq1 * t) * 0.75 + sin(2 * PI * freq2 * t) * 0.25) * envelope
                val intSample = (sampleValue * 32767 * 0.85).toInt().coerceIn(-32768, 32767)

                buffer[i * 2] = (intSample and 0xFF).toByte()
                buffer[i * 2 + 1] = ((intSample shr 8) and 0xFF).toByte()
            }
            return buffer
        }
    }
}
