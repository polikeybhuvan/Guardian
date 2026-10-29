package com.example.domain.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sqrt

internal data class WhistleAnalysis(
    val isWhistle: Boolean,
    val confidence: Float,
    val dominantFrequencyHz: Int,
    val rms: Double
)

/** Pure signal analysis so the frequency detector can be exercised without a microphone. */
internal object WhistleSignalAnalyzer {
    private val targetFrequencies = (2500..3800 step 100).toList()
    private const val MIN_RMS = 450.0
    private const val MIN_TONE_RATIO = 0.32

    fun analyze(samples: ShortArray, count: Int, sampleRate: Int): WhistleAnalysis {
        if (count <= 0 || count > samples.size || sampleRate <= 0) {
            return WhistleAnalysis(false, 0f, 0, 0.0)
        }

        var sumSquares = 0.0
        for (index in 0 until count) {
            val sample = samples[index].toDouble()
            sumSquares += sample * sample
        }
        val rms = sqrt(sumSquares / count)
        if (rms < MIN_RMS) return WhistleAnalysis(false, 0f, 0, rms)

        var bestPower = 0.0
        var bestFrequency = 0
        for (frequency in targetFrequencies) {
            val coefficient = 2.0 * cos(2.0 * Math.PI * frequency / sampleRate)
            var previous = 0.0
            var previousPrevious = 0.0
            for (index in 0 until count) {
                val current = samples[index].toDouble() + coefficient * previous - previousPrevious
                previousPrevious = previous
                previous = current
            }
            val power = previous * previous + previousPrevious * previousPrevious - coefficient * previous * previousPrevious
            if (power > bestPower) {
                bestPower = power
                bestFrequency = frequency
            }
        }

        val normalizedToneRatio = (bestPower / (sumSquares * count / 2.0)).coerceIn(0.0, 1.0)
        val detected = normalizedToneRatio >= MIN_TONE_RATIO
        val confidence = if (detected) (normalizedToneRatio / 0.75).coerceIn(0.35, 1.0).toFloat() else 0f
        return WhistleAnalysis(detected, confidence, if (detected) bestFrequency else 0, rms)
    }
}

/**
 * On-device acoustic detector that listens for emergency disaster whistles (~2.8 - 3.5 kHz)
 * or loud distress calls when trapped under debris or rubble.
 */
class AcousticWhistleDetector(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _detectionConfidence = MutableStateFlow(0f)
    val detectionConfidence: StateFlow<Float> = _detectionConfidence.asStateFlow()

    private val _whistleDetectedCount = MutableStateFlow(0)
    val whistleDetectedCount: StateFlow<Int> = _whistleDetectedCount.asStateFlow()

    private var listenerJob: Job? = null
    private val sampleRate = 16000

    fun startListening(onWhistleDetected: () -> Unit = {}) {
        if (_isListening.value || _isStarting.value) return
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            _lastError.value = "Microphone permission is required."
            return
        }

        _lastError.value = null
        _isStarting.value = true
        listenerJob = scope.launch(Dispatchers.IO) {
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            var audioRecord: AudioRecord? = null

            try {
                check(bufferSize > 0) { "This device cannot start audio capture." }
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(bufferSize, 2048)
                )
                check(audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    "The microphone recorder could not be initialized."
                }
                audioRecord.startRecording()
                check(audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    "The microphone did not start recording."
                }
                _isStarting.value = false
                _isListening.value = true
                val buffer = ShortArray(bufferSize)
                var consecutiveWhistleBlocks = 0

                while (isActive && _isListening.value) {
                    val read = audioRecord.read(buffer, 0, bufferSize)
                    if (read > 0) {
                        val analysis = WhistleSignalAnalyzer.analyze(buffer, read, sampleRate)
                        _detectionConfidence.value = analysis.confidence

                        if (analysis.isWhistle) {
                            consecutiveWhistleBlocks++
                            if (consecutiveWhistleBlocks >= 2) {
                                _whistleDetectedCount.value += 1
                                onWhistleDetected()
                                consecutiveWhistleBlocks = 0
                            }
                        } else {
                            consecutiveWhistleBlocks = 0
                        }
                    }
                }
            } catch (error: Exception) {
                if (isActive) _lastError.value = error.message ?: "Microphone capture failed."
            } finally {
                _isStarting.value = false
                _isListening.value = false
                runCatching { audioRecord?.stop() }
                runCatching { audioRecord?.release() }
            }
        }
    }

    fun stopListening() {
        _isListening.value = false
        _isStarting.value = false
        listenerJob?.cancel()
        listenerJob = null
        _detectionConfidence.value = 0f
    }
}
