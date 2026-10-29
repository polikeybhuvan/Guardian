package com.example.domain.beacon

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class BeaconController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isTorchActive = MutableStateFlow(false)
    val isTorchActive: StateFlow<Boolean> = _isTorchActive.asStateFlow()

    private val _isSirenActive = MutableStateFlow(false)
    val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()
    private val _sirenError = MutableStateFlow<String?>(null)
    val sirenError: StateFlow<String?> = _sirenError.asStateFlow()


    private var torchJob: Job? = null
    private var sirenJob: Job? = null
    private var torchCameraId: String? = null

    init {
        findFlashCamera()
    }

    private fun findFlashCamera() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (hasFlash) {
                    torchCameraId = id
                    return
                }
            }
        } catch (_: Exception) {}
    }

    fun toggleTorchStrobe() {
        if (_isTorchActive.value) {
            stopTorchStrobe()
        } else {
            startTorchStrobe()
        }
    }

    fun startTorchStrobe() {
        stopTorchStrobe()
        val camId = torchCameraId ?: return
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
        _isTorchActive.value = true

        torchJob = scope.launch {
            try {
                // Morse Code SOS: 3 short, 3 long, 3 short
                val dotMs = 150L
                val dashMs = 450L
                val elementGap = 150L
                val letterGap = 350L
                val wordGap = 900L

                while (isActive) {
                    // S: ...
                    repeat(3) {
                        setTorch(camId, true)
                        delay(dotMs)
                        setTorch(camId, false)
                        delay(elementGap)
                    }
                    delay(letterGap)

                    // O: ---
                    repeat(3) {
                        setTorch(camId, true)
                        delay(dashMs)
                        setTorch(camId, false)
                        delay(elementGap)
                    }
                    delay(letterGap)

                    // S: ...
                    repeat(3) {
                        setTorch(camId, true)
                        delay(dotMs)
                        setTorch(camId, false)
                        delay(elementGap)
                    }
                    delay(wordGap)
                }
            } finally {
                setTorch(camId, false)
                _isTorchActive.value = false
            }
        }
    }

    fun stopTorchStrobe() {
        torchJob?.cancel()
        torchJob = null
        torchCameraId?.let { setTorch(it, false) }
        _isTorchActive.value = false
    }

    private fun setTorch(camId: String?, enabled: Boolean) {
        if (camId == null) return
        try {
            cameraManager?.setTorchMode(camId, enabled)
        } catch (_: CameraAccessException) {
        } catch (_: Exception) {}
    }

    fun toggleSiren() {
        if (_isSirenActive.value) {
            stopSiren()
        } else {
            startSiren()
        }
    }

    fun startSiren() {
        stopSiren()
        _sirenError.value = null

        sirenJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) {
                _sirenError.value = "This device could not prepare audio output. Check media volume and try again."
                return@launch
            }

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
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
                    .setBufferSizeInBytes(maxOf(minBufferSize, sampleRate))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                _sirenError.value = "Could not start the siren audio: ${e.message ?: "audio output unavailable"}"
                Log.e("GuardianSiren", "Could not create siren AudioTrack", e)
                return@launch
            }

            if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
                _sirenError.value = "This device could not initialize siren audio. Check media volume and try again."
                return@launch
            }

            try {
                audioTrack.setVolume(1.0f)
                audioTrack.play()
                if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    throw IllegalStateException("Audio output did not start")
                }
                _isSirenActive.value = true
                val sweepBuffer = ShortArray(sampleRate / 4)
                var phase = 0.0
                var ascending = true

                while (isActive) {
                    phase = generateToneSweep(
                        sweepBuffer,
                        sampleRate,
                        if (ascending) 700.0 else 1500.0,
                        if (ascending) 1500.0 else 700.0,
                        phase
                    )
                    var offset = 0
                    while (offset < sweepBuffer.size && isActive) {
                        val written = audioTrack.write(
                            sweepBuffer,
                            offset,
                            sweepBuffer.size - offset,
                            AudioTrack.WRITE_BLOCKING
                        )
                        check(written > 0) { "Audio output stopped (write result $written)" }
                        offset += written
                    }
                    ascending = !ascending
                }
            } catch (e: Exception) {
                if (isActive) {
                    _sirenError.value = "Siren playback stopped: ${e.message ?: "audio output unavailable"}"
                    Log.e("GuardianSiren", "Siren playback failed", e)
                }
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
                _isSirenActive.value = false
            }
        }
    }

    fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
        _isSirenActive.value = false
    }

    fun stopAll() {
        stopTorchStrobe()
        stopSiren()
    }

    private fun generateToneSweep(
        buffer: ShortArray,
        sampleRate: Int,
        startFreq: Double,
        endFreq: Double,
        initialPhase: Double
    ): Double {
        var currentPhase = initialPhase
        val n = buffer.size
        for (i in 0 until n) {
            val progress = i.toDouble() / n
            val freq = startFreq + (endFreq - startFreq) * progress
            currentPhase += 2.0 * Math.PI * freq / sampleRate
            buffer[i] = (sin(currentPhase) * 32000).toInt().toShort()
        }
        return currentPhase
    }
}
