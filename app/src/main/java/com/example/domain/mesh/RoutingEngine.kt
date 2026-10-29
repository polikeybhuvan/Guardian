package com.example.domain.mesh

import com.example.data.crypto.CryptoSigner
import com.example.data.local.AlertDao
import com.example.data.local.MeshMessageEntity
import com.example.data.local.SeenMessageEntity
import com.example.data.model.MeshMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class RoutingEngine(
    private val alertDao: AlertDao,
    private val cryptoSigner: CryptoSigner,
    private val transport: MeshTransport,
    private val scope: CoroutineScope
) {
    // In-memory LRU deduplication cache for 5,000 seen message IDs
    private val seenMessageIds = LinkedHashSet<String>()
    private val maxCacheSize = 5000

    suspend fun startTransport(): Boolean =
        transport.start { incomingMsg -> handleIncomingPeerMessage(incomingMsg) }

    suspend fun stopTransport() = transport.stop()

    /**
     * Originates a new SOS or Safe check-in from this local phone.
     */
    suspend fun originateAlert(message: MeshMessage): MeshMessage {
        val signed = message.copy(
            senderId = cryptoSigner.getDeviceId(),
            senderPublicKey = cryptoSigner.getPublicKeyBase64(),
            ttl = 8,
            hopCount = 0
        ).let { it.copy(signature = cryptoSigner.signMessage(it)) }

        recordMessageSeen(signed.msgId)
        alertDao.insertMessage(MeshMessageEntity.fromDomain(signed, isLocalOrigin = true))

        // Broadcast to immediate peers
        scope.launch(Dispatchers.IO) {
            transport.broadcastPayload(signed)
        }

        return signed
    }

    /**
     * Handles an incoming alert received from a nearby peer.
     */
    suspend fun handleIncomingPeerMessage(message: MeshMessage) {
        // Validate the signed origin before trusting any packet fields, including duplicate ACK flags.
        if (!cryptoSigner.verifySignature(message)) {
            return
        }

        // 1. Deduplication check
        if (isAlreadySeen(message.msgId)) {
            // ACK/gateway metadata is not covered by the origin signature; never trust it from a duplicate.
            return
        }

        // 2. Mark message as seen
        recordMessageSeen(message.msgId)

        // 4. Store locally for store-and-forward
        alertDao.insertMessage(MeshMessageEntity.fromDomain(message, isLocalOrigin = false))

        // 5. TTL & Flooding check
        if (message.ttl <= 1) {
            // TTL expired, do not rebroadcast
            return
        }

        val forwardMsg = message.copy(
            ttl = message.ttl - 1,
            hopCount = message.hopCount + 1,
            lastRelayNodeId = cryptoSigner.getDeviceId()
        )

        // 6. Forward with random jitter delay (0.5s - 2.0s) to avoid collisions
        scope.launch(Dispatchers.IO) {
            val jitterMs = Random.nextLong(500, 2000)
            delay(jitterMs)
            transport.broadcastPayload(forwardMsg)
        }
    }

    private suspend fun isAlreadySeen(msgId: String): Boolean {
        synchronized(seenMessageIds) {
            if (seenMessageIds.contains(msgId)) return true
        }
        return alertDao.hasSeenMessage(msgId)
    }

    private suspend fun recordMessageSeen(msgId: String) {
        synchronized(seenMessageIds) {
            if (seenMessageIds.size >= maxCacheSize) {
                val firstKey = seenMessageIds.iterator().next()
                seenMessageIds.remove(firstKey)
            }
            seenMessageIds.add(msgId)
        }
        alertDao.markMessageSeen(SeenMessageEntity(msgId))
    }
}
