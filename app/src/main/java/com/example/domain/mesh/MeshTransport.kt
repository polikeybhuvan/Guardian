package com.example.domain.mesh

import com.example.data.model.MeshMessage
import kotlinx.coroutines.flow.StateFlow

interface MeshTransport {
    val connectedPeersCount: StateFlow<Int>
    val isInternetAvailable: StateFlow<Boolean>

    suspend fun start(onMessageReceived: suspend (MeshMessage) -> Unit): Boolean
    suspend fun stop()
    suspend fun broadcastPayload(message: MeshMessage): Int
    suspend fun setInternetAvailable(available: Boolean)
}
