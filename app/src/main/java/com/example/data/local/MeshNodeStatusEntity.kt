package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.mesh.MeshPeerNode

@Entity(tableName = "mesh_nodes")
data class MeshNodeStatusEntity(
    @PrimaryKey
    val nodeId: String,
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
    val isRelayingAlert: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): MeshPeerNode {
        return MeshPeerNode(
            id = nodeId,
            name = name,
            rssi = rssi,
            isConnected = isConnected,
            lastSeenTimestamp = lastSeenTimestamp,
            latitude = latitude,
            longitude = longitude,
            batteryPct = batteryPct,
            nodeRole = nodeRole,
            hopDistance = hopDistance,
            isGateway = isGateway,
            isRelayingAlert = isRelayingAlert
        )
    }

    companion object {
        fun fromDomain(domain: MeshPeerNode): MeshNodeStatusEntity {
            return MeshNodeStatusEntity(
                nodeId = domain.id,
                name = domain.name,
                rssi = domain.rssi,
                isConnected = domain.isConnected,
                lastSeenTimestamp = domain.lastSeenTimestamp,
                latitude = domain.latitude,
                longitude = domain.longitude,
                batteryPct = domain.batteryPct,
                nodeRole = domain.nodeRole,
                hopDistance = domain.hopDistance,
                isGateway = domain.isGateway,
                isRelayingAlert = domain.isRelayingAlert,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
