package com.example.domain.beacon

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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

/**
 * Synthesizes high-decibel acoustic Morse Code SOS pulses ("... --- ...")
 * directly using AudioTrack sine wave generation to guide rescue teams,
 * dogs, and emergency personnel in darkness or debris.
 */
class MorseSoundBeacon(private val scope: CoroutineScope) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var beaconJob: Job? = null
    private val sampleRate = 44100
    private val toneFrequency = 900.0 // 900 Hz piercing distress frequency
    @Volatile private var activeTrack: AudioTrack? = null

    fun start() {
        if (_isPlaying.value) return
        beaconJob = scope.launch(Dispatchers.IO) {
            val dotMs = 120L
            val dashMs = 360L
            val intraSpaceMs = 120L
            val letterSpaceMs = 360L
            val wordSpaceMs = 1000L

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) return@launch
            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, sampleRate / 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: return@launch
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                return@launch
            }
            activeTrack = track
            try {
                track.play()
                _isPlaying.value = true
            } catch (_: Exception) {
                activeTrack = null
                track.release()
                return@launch
            }
            try {
            while (isActive && _isPlaying.value) {
                // S: . . .
                playTone(track, dotMs)
                delay(intraSpaceMs)
                playTone(track, dotMs)
                delay(intraSpaceMs)
                playTone(track, dotMs)

                delay(letterSpaceMs)

                // O: - - -
                playTone(track, dashMs)
                delay(intraSpaceMs)
                playTone(track, dashMs)
                delay(intraSpaceMs)
                playTone(track, dashMs)

                delay(letterSpaceMs)

                // S: . . .
                playTone(track, dotMs)
                delay(intraSpaceMs)
                playTone(track, dotMs)
                delay(intraSpaceMs)
                playTone(track, dotMs)

                // Sequence pause before next cycle
                delay(wordSpaceMs)
            }
            } finally {
                activeTrack = null
                runCatching { track.pause() }
                runCatching { track.flush() }
                runCatching { track.release() }
                _isPlaying.value = false
                beaconJob = null
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        beaconJob?.cancel()
        activeTrack?.let { track ->
            runCatching { track.pause() }
            runCatching { track.flush() }
            runCatching { track.release() }
        }
        activeTrack = null
        beaconJob = null
    }

    private suspend fun playTone(track: AudioTrack, durationMs: Long) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val generatedSound = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / toneFrequency)
            // Apply slight envelope smoothing on edges to prevent speaker click
            val envelope = when {
                i < 100 -> i / 100.0
                i > numSamples - 100 -> (numSamples - i) / 100.0
                else -> 1.0
            }
            generatedSound[i] = (sin(angle) * Short.MAX_VALUE * 0.9 * envelope).toInt().toShort()
        }

        val written = track.write(generatedSound, 0, numSamples, AudioTrack.WRITE_BLOCKING)
        check(written > 0) { "Morse audio playback failed" }
        delay(durationMs)
    }
}
