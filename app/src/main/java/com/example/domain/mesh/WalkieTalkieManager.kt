package com.example.domain.mesh

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class VoiceMemoItem(
    val id: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val audioBase64: String,
    val isLocal: Boolean = false
)

class WalkieTalkieManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _recordedMemos = MutableStateFlow<List<VoiceMemoItem>>(emptyList())
    val recordedMemos: StateFlow<List<VoiceMemoItem>> = _recordedMemos.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordFile: File? = null

    init {
        _recordedMemos.value = emptyList()
    }

    fun startRecording() {
        if (_isRecording.value) return
        val cacheFile = File(context.cacheDir, "temp_voice_memo.3gp")
        currentRecordFile = cacheFile

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        runCatching {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile(cacheFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            _isRecording.value = true
        }.onFailure {
            it.printStackTrace()
            _isRecording.value = false
        }
    }

    fun stopRecording(senderId: String, onRecorded: (VoiceMemoItem) -> Unit) {
        if (!_isRecording.value) return
        val recorder = mediaRecorder ?: return
        runCatching {
            recorder.stop()
            recorder.release()
        }
        mediaRecorder = null
        _isRecording.value = false

        val file = currentRecordFile
        if (file != null && file.exists() && file.length() > 0) {
            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val memo = VoiceMemoItem(
                id = "VOICE_${System.currentTimeMillis()}",
                senderId = senderId,
                senderName = "You (Local Origin)",
                timestamp = System.currentTimeMillis(),
                durationSeconds = (bytes.size / 1600).coerceIn(1, 10),
                audioBase64 = base64,
                isLocal = true
            )
            _recordedMemos.value = listOf(memo) + _recordedMemos.value
            onRecorded(memo)
        }
    }

    fun playMemo(memo: VoiceMemoItem) {
        if (_isPlaying.value) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            return
        }

        if (memo.audioBase64.isBlank()) {
            // Simulated voice beep / synthesized confirmation for dummy notes
            _isPlaying.value = true
            scope.launch(Dispatchers.Default) {
                kotlinx.coroutines.delay(3000)
                _isPlaying.value = false
            }
            return
        }

        val bytes = Base64.decode(memo.audioBase64, Base64.NO_WRAP)
        val tempPlayFile = File(context.cacheDir, "play_${memo.id}.3gp")
        FileOutputStream(tempPlayFile).use { it.write(bytes) }

        val player = MediaPlayer()
        runCatching {
            player.setDataSource(tempPlayFile.absolutePath)
            player.prepare()
            player.start()
            _isPlaying.value = true
            mediaPlayer = player

            player.setOnCompletionListener {
                _isPlaying.value = false
                player.release()
                mediaPlayer = null
            }
        }.onFailure {
            it.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun stopPlaying() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun addIncomingMemo(memo: VoiceMemoItem) {
        _recordedMemos.value = listOf(memo) + _recordedMemos.value
    }
}
