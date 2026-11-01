package com.example.domain.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** Prepares the bundled LiteRT-LM Gemma model. Weights are staged in app-private storage. */
class OfflineGemmaChatModel(context: Context) {
    private val appContext = context.applicationContext
    private val modelFile = File(appContext.filesDir, MODEL_FILE_NAME)
    private val mutex = Mutex()

    private val _status = MutableStateFlow(if (isInstalled) "Gemma ready · offline" else "Preparing offline Gemma…")
    val status: StateFlow<String> = _status

    val isInstalled: Boolean get() = modelFile.isFile && modelFile.length() == MODEL_SIZE_BYTES

    suspend fun prepareBundledModel() = mutex.withLock {
        if (isInstalled) {
            _status.value = "Gemma ready · offline"
            return@withLock
        }
        _status.value = "Preparing offline Gemma…"
        withContext(Dispatchers.IO) {
            val tempFile = File(appContext.filesDir, "$MODEL_FILE_NAME.tmp")
            try {
                appContext.assets.open(BUNDLED_ASSET_FILE_NAME).use { input ->
                    copyAndVerify(input, tempFile)
                }
                check(tempFile.renameTo(modelFile)) { "Could not copy the bundled model into app storage." }
                _status.value = "Gemma ready · offline"
            } catch (error: Exception) {
                _status.value = "Model unavailable: ${error.message ?: "could not prepare offline model"}"
            } finally {
                tempFile.delete()
            }
        }
    }

    suspend fun importModel(sourceName: String, source: java.io.InputStream): Unit = withContext(Dispatchers.IO) {
        require(sourceName.endsWith(".litertlm", ignoreCase = true)) {
            "Choose the official Gemma .litertlm model file."
        }
        _status.value = "Installing offline model…"
        val tempFile = File(appContext.filesDir, "$MODEL_FILE_NAME.tmp")
        try {
            source.use { input -> copyAndVerify(input, tempFile) }
            check(tempFile.renameTo(modelFile)) { "Could not install model in app storage." }
            _status.value = "Gemma ready · offline"
        } catch (error: Throwable) {
            _status.value = "Model unavailable: ${error.message ?: "model installation failed"}"
            throw error
        } finally {
            tempFile.delete()
        }
    }

    private fun copyAndVerify(source: java.io.InputStream, target: File) {
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        target.outputStream().buffered().use { output ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = source.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
                digest.update(buffer, 0, count)
                size += count
            }
        }
        require(size == MODEL_SIZE_BYTES) { "Model file size is incorrect ($size bytes)." }
        val hash = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        require(hash == MODEL_SHA256) { "Model checksum does not match the official Gemma file." }
    }

    suspend fun reply(userText: String): String = mutex.withLock {
        withContext(Dispatchers.Default) {
            try {
                check(isInstalled) { "Bundled Gemma model is unavailable." }
                Engine(EngineConfig(modelPath = modelFile.absolutePath, backend = Backend.CPU())).use { engine ->
                    engine.initialize()
                    engine.createConversation(
                        ConversationConfig(
                            systemInstruction = Contents.of(SYSTEM_INSTRUCTION),
                            samplerConfig = SamplerConfig(topK = 40, topP = 0.9, temperature = 0.2)
                        )
                    ).use { conversation ->
                        val answer = conversation.sendMessageAsync(userText).toList().joinToString(separator = "") { it.toString() }.trim()
                        answer.takeIf { it.isNotBlank() } ?: "I couldn't prepare safe guidance. Call 112 for immediate danger."
                    }
                }
            } catch (error: Exception) {
                _status.value = "Model unavailable: ${error.message ?: "Gemma failed to load"}"
                throw error
            }
        }
    }

    companion object {
        const val MODEL_FILE_NAME = "gemma3-1b-it.litertlm"
        private const val MODEL_SIZE_BYTES = 584417280L
        private const val MODEL_SHA256 = "1325ae366d31950f137c9c357b9fa89448b176d76998180c08ceaca78bba98be"
        private const val BUNDLED_ASSET_FILE_NAME = "gemma3-1b-it-int4.litertlm"
        const val MODEL_DISPLAY_SIZE = "584 MB"
        private val SYSTEM_INSTRUCTION = """
            You are Guardian, an offline emergency first-aid assistant for people in India.
            Answer only questions about immediate emergencies, personal safety, disaster response,
            first aid, and getting help. If a request is unrelated, reply exactly:
            Sorry, I can only help regarding emergency situations.
            Ask for what happened, symptoms or injuries, when they began, how many people are affected,
            and location when useful. Keep replies concise: give the most urgent action first, then
            up to three short steps. Keep instructions calm and actionable. For immediate danger tell the user to
            call India's emergency number 112 when possible. For thermal burns, recommend cool running tap water
            for 20 minutes and never recommend ice or iced water; reserve 112 advice for severe burns or
            immediate danger. Do not claim to contact services,
            diagnose with certainty, or invent local resources. If unsure, say so and advise seeking
            trained emergency help. Never delay calling emergency services to continue this chat.
        """.trimIndent()
    }
}
