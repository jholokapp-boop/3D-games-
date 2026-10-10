package com.example.cyber3d.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure native real-time AudioTrack synthesizer and Haptic feedback engine.
 * Synthesizes retro sci-fi sound waves directly from mathematical functions!
 */
class SoundSynth(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    /**
     * Synthesizes and plays a laser blaster sound.
     */
    fun playLaser() {
        if (!isSoundEnabled) return
        scope.launch {
            val durationMs = 120
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            var phase = 0.0
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / totalSamples
                // Frequency drops rapidly from 950 Hz down to 180 Hz
                val freq = 950.0 * (1.0 - t * 0.8)
                val amp = (1.0 - t) * 0.75 // Linear decay envelope
                phase += 2.0 * PI * freq / sampleRate
                val sampleVal = (sin(phase) * amp * Short.MAX_VALUE).toInt()
                buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Synthesizes a high-frequency crystalline chime for collecting energy cores.
     */
    fun playPickup() {
        if (!isSoundEnabled) return
        vibrate(30, 80)
        scope.launch {
            val durationMs = 160
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            var phase1 = 0.0
            var phase2 = 0.0
            val half = totalSamples / 2

            for (i in 0 until totalSamples) {
                val freq = if (i < half) 659.25 /* E5 */ else 987.77 /* B5 */
                val subT = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
                val amp = (1.0 - subT) * 0.7
                if (i < half) {
                    phase1 += 2.0 * PI * freq / sampleRate
                    buffer[i] = (sin(phase1) * amp * Short.MAX_VALUE).toInt().toShort()
                } else {
                    phase2 += 2.0 * PI * freq / sampleRate
                    buffer[i] = (sin(phase2) * amp * Short.MAX_VALUE).toInt().toShort()
                }
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Synthesizes a speed boost hyper-drive surge.
     */
    fun playBoost() {
        if (!isSoundEnabled) return
        vibrate(80, 200)
        scope.launch {
            val durationMs = 280
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            var phase = 0.0
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / totalSamples
                // Rising frequency with sub bass swell
                val freq = 120.0 + (t * t) * 450.0
                val amp = sin(t * PI) * 0.85
                phase += 2.0 * PI * freq / sampleRate
                buffer[i] = (sin(phase) * amp * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Synthesizes an explosion crash sound (noise + bass decay).
     */
    fun playExplosion() {
        if (!isSoundEnabled) return
        vibrate(220, 255)
        scope.launch {
            val durationMs = 350
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            var bassPhase = 0.0
            var noiseFilter = 0.0
            val rand = Random(42)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / totalSamples
                val env = (1.0 - t) * (1.0 - t)

                // White noise filtered
                val rawNoise = (rand.nextDouble() * 2.0 - 1.0)
                noiseFilter = noiseFilter * 0.75 + rawNoise * 0.25

                // Heavy sub bass rumble
                bassPhase += 2.0 * PI * (75.0 * (1.0 - t * 0.5)) / sampleRate
                val rumble = sin(bassPhase) * 0.6

                val mix = (noiseFilter * 0.5 + rumble * 0.5) * env * 0.95
                buffer[i] = (mix * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /**
     * Starts continuous procedural synthwave background music loop.
     */
    fun startSynthwaveMusic() {
        if (!isMusicEnabled || musicJob != null) return
        musicJob = scope.launch {
            // Synthwave bass notes in D minor (D2, F2, G2, A2)
            val bassFrequencies = doubleArrayOf(73.42, 73.42, 87.31, 87.31, 98.00, 98.00, 110.00, 98.00)
            var step = 0
            val noteDurationMs = 150L

            while (isActive && isMusicEnabled) {
                val freq = bassFrequencies[step % bassFrequencies.size]
                step++

                // Render one short bass synth pluck
                val samples = (sampleRate * (noteDurationMs.toInt())) / 1000
                val buffer = ShortArray(samples)
                var phase = 0.0

                for (i in 0 until samples) {
                    val t = i.toDouble() / samples
                    val env = kotlin.math.exp(-t * 3.5) * 0.35 // soft punchy pluck
                    phase += 2.0 * PI * freq / sampleRate
                    // Add 2nd harmonic for warm analog synth tone
                    val wave = sin(phase) + 0.4 * sin(phase * 2.0)
                    buffer[i] = (wave * env * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(buffer)
                delay(noteDurationMs)
            }
        }
    }

    fun stopSynthwaveMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playPcmBuffer(buffer: ShortArray) {
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback
            scope.launch {
                delay((buffer.size * 1000L / sampleRate) + 60L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Handle devices with audio limitations gracefully
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
