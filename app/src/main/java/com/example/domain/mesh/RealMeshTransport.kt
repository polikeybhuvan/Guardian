package com.example.domain.mesh

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.net.wifi.WifiManager
import com.example.data.model.MeshMessage
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.ConcurrentHashMap

data class MeshPeerNode(
    val id: String,
    val name: String,
    val rssi: Int = -65,
    val isConnected: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val batteryPct: Int = 85,
    val nodeRole: String = "Citizen Relay Node",
    val hopDistance: Int = 1,
    val isGateway: Boolean = false,
    val isRelayingAlert: Boolean = false
) {
    fun distanceMetersTo(targetLat: Double?, targetLng: Double?): Float {
        if (latitude == null || longitude == null || targetLat == null || targetLng == null) return 0f
        val r = 6371000.0
        val lat1Rad = Math.toRadians(targetLat)
        val lat2Rad = Math.toRadians(latitude)
        val dLat = Math.toRadians(latitude - targetLat)
        val dLng = Math.toRadians(longitude - targetLng)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (r * c).toFloat()
    }

    fun formattedDistance(originLat: Double?, originLng: Double?): String {
        val distM = distanceMetersTo(originLat, originLng)
        return if (distM >= 1000) String.format(java.util.Locale.US, "%.1f km", distM / 1000f) else "${distM.toInt()} m"
    }
}

data class MeshLogEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val details: String
)

class RealMeshTransport(
    private val context: Context,
    private val scope: CoroutineScope
) : MeshTransport {

    private val networkMonitor = NetworkMonitor(context)
    override val isInternetAvailable: StateFlow<Boolean> = networkMonitor.isOnline

    private val _connectedPeersCount = MutableStateFlow(0)
    override val connectedPeersCount: StateFlow<Int> = _connectedPeersCount.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<MeshPeerNode>>(emptyList())
    val discoveredPeers: StateFlow<List<MeshPeerNode>> = _discoveredPeers.asStateFlow()

    private val _transmissionLogs = MutableStateFlow<List<MeshLogEvent>>(emptyList())
    val transmissionLogs: StateFlow<List<MeshLogEvent>> = _transmissionLogs.asStateFlow()

    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val messageAdapter = Moshi.Builder().build().adapter(MeshMessage::class.java)
    private val endpoints = ConcurrentHashMap.newKeySet<String>()
    private val pendingEndpoints = ConcurrentHashMap.newKeySet<String>()
    private val pendingMessages = ConcurrentHashMap<String, MeshMessage>()
    private val payloadMessageIds = ConcurrentHashMap<Long, String>()
    private var isStarted = false
    private var incomingMessageListener: (suspend (MeshMessage) -> Unit)? = null

    init {
        logEvent("SYSTEM_INIT", "Offline peer-to-peer transport initialized; waiting for Nearby permissions.")
        checkRadioHardwareState()
    }

    override suspend fun start(onMessageReceived: suspend (MeshMessage) -> Unit): Boolean {
        incomingMessageListener = onMessageReceived
        if (isStarted) return true
        val requiredPermissions = buildList {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            incomingMessageListener = null
            logEvent("NEARBY_START_BLOCKED", "Missing Nearby permissions: ${missing.joinToString()}")
            return false
        }
        val strategy = Strategy.P2P_CLUSTER
        return try {
            connectionsClient.startAdvertising(
                "Guardian Rescue Node",
                SERVICE_ID,
                connectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(strategy).build()
            ).awaitResult()
            connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(strategy).build()
            ).awaitResult()
            isStarted = true
            logEvent("NEARBY_START", "Advertising and discovering nearby Guardian peers.")
            true
        } catch (error: Exception) {
            isStarted = false
            incomingMessageListener = null
            runCatching { connectionsClient.stopAdvertising() }
            runCatching { connectionsClient.stopDiscovery() }
            logEvent("NEARBY_START_FAILED", "Nearby startup failed: ${error.message ?: "permission or device unavailable"}")
            false
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> if (continuation.isActive) continuation.resume(result) }
        addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
        addOnCanceledListener { continuation.cancel() }
    }

    override suspend fun stop() {
        incomingMessageListener = null
        isStarted = false
        runCatching {
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            connectionsClient.stopAllEndpoints()
        }
        endpoints.clear()
        pendingEndpoints.clear()
        payloadMessageIds.clear()
        updatePeerCount()
        logEvent("NEARBY_STOP", "Nearby advertising, discovery, and peer connections stopped.")
    }

    override suspend fun setInternetAvailable(available: Boolean) {
        // Kept for interface compatibility; network state is driven by real NetworkMonitor
    }

    override suspend fun broadcastPayload(message: MeshMessage): Int {
        if (endpoints.isEmpty()) {
            pendingMessages[message.msgId] = message
            logEvent("LOCAL_ONLY", "Alert ${message.msgId.take(8)} is saved on this device; no nearby peer is connected.")
            return 0
        }
        pendingMessages[message.msgId] = message
        sendPendingMessages(endpoints.toList())
        return endpoints.size
    }

    private fun sendPendingMessages(targetEndpoints: List<String>) {
        if (targetEndpoints.isEmpty()) return
        pendingMessages.values.toList().forEach { message ->
        val bytes = runCatching { messageAdapter.toJson(message).toByteArray(Charsets.UTF_8) }.getOrElse {
            logEvent("SERIALIZE_FAILED", "Alert could not be encoded for nearby transfer.")
            return@forEach
        }
        if (bytes.size > MAX_MESSAGE_BYTES) {
            logEvent("MESSAGE_TOO_LARGE", "Alert ${message.msgId.take(8)} exceeds the nearby message size limit.")
            pendingMessages.remove(message.msgId)
            return@forEach
        }
        val payload = Payload.fromBytes(bytes)
        payloadMessageIds[payload.id] = message.msgId
        runCatching {
            connectionsClient.sendPayload(targetEndpoints, payload)
                .addOnSuccessListener { logEvent("PAYLOAD_SENT", "Alert ${message.msgId.take(8)} submitted to ${targetEndpoints.size} nearby peer(s).") }
                .addOnFailureListener { error -> logEvent("PAYLOAD_FAILED", "Nearby transfer failed: ${error.message ?: "connection lost"}") }
        }.onFailure { error ->
            logEvent("PAYLOAD_FAILED", "Nearby transfer could not start: ${error.message ?: "permission or connection unavailable"}")
        }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if (endpointId in endpoints || !pendingEndpoints.add(endpointId)) return
            connectionsClient.requestConnection("Guardian Rescue Node", endpointId, connectionLifecycleCallback)
                .addOnFailureListener { error ->
                    pendingEndpoints.remove(endpointId)
                    logEvent("CONNECTION_REQUEST_FAILED", "Could not connect to a nearby peer: ${error.message ?: "connection unavailable"}")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            pendingEndpoints.remove(endpointId)
            _discoveredPeers.value = _discoveredPeers.value.filterNot { it.id == endpointId }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            if (bytes.size > MAX_MESSAGE_BYTES) {
                logEvent("PAYLOAD_REJECTED", "Ignored an oversized payload from a nearby peer.")
                return
            }
            val message = runCatching { messageAdapter.fromJson(bytes.toString(Charsets.UTF_8)) }.getOrNull()
            if (message == null) {
                logEvent("PAYLOAD_REJECTED", "Ignored an invalid message from a nearby peer.")
                return
            }
            scope.launch { incomingMessageListener?.invoke(message) }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                payloadMessageIds.remove(update.payloadId)?.let { pendingMessages.remove(it) }
                logEvent("PAYLOAD_DELIVERED", "A nearby peer received a message payload.")
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                payloadMessageIds.remove(update.payloadId)
                logEvent("PAYLOAD_FAILED", "Nearby message transfer failed; it remains queued locally.")
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: com.google.android.gms.nearby.connection.ConnectionInfo) {
            // Nearby Connections encrypts the channel. SOS payloads also carry sender-key signatures,
            // which RoutingEngine verifies before saving or relaying them.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnFailureListener { error -> logEvent("CONNECTION_REJECTED", "Nearby connection could not be accepted: ${error.message ?: "unknown error"}") }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            pendingEndpoints.remove(endpointId)
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                endpoints.add(endpointId)
                _discoveredPeers.value = (_discoveredPeers.value.filterNot { it.id == endpointId } +
                    MeshPeerNode(id = endpointId, name = "Nearby Guardian peer", isConnected = true))
                updatePeerCount()
                logEvent("PEER_CONNECTED", "Connected to a nearby Guardian peer.")
                sendPendingMessages(listOf(endpointId))
            } else {
                logEvent("PEER_CONNECTION_FAILED", "Nearby connection did not complete.")
            }
        }

        override fun onDisconnected(endpointId: String) {
            endpoints.remove(endpointId)
            pendingEndpoints.remove(endpointId)
            _discoveredPeers.value = _discoveredPeers.value.filterNot { it.id == endpointId }
            updatePeerCount()
            logEvent("PEER_DISCONNECTED", "A nearby peer disconnected.")
        }
    }

    private fun updatePeerCount() {
        _connectedPeersCount.value = endpoints.size
    }

    fun isBluetoothEnabled(): Boolean {
        return runCatching {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bm?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            adapter?.isEnabled == true
        }.getOrDefault(false)
    }

    fun isWifiEnabled(): Boolean {
        return runCatching {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wm?.isWifiEnabled == true
        }.getOrDefault(false)
    }

    private fun checkRadioHardwareState() {
        val bt = isBluetoothEnabled()
        val wifi = isWifiEnabled()
        logEvent("RADIO_CHECK", "Bluetooth: ${if (bt) "Enabled" else "Disabled"}, Wi-Fi: ${if (wifi) "Enabled" else "Disabled"}")
    }

    fun logEvent(type: String, details: String) {
        val event = MeshLogEvent(System.currentTimeMillis(), type, details)
        _transmissionLogs.value = listOf(event) + _transmissionLogs.value.take(40)
    }

    companion object {
        private const val SERVICE_ID = "com.aistudio.sosmesh.guardian.nearby"
        private const val MAX_MESSAGE_BYTES = 64 * 1024
    }
}
