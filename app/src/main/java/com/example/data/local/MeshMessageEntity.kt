package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCategory
import com.example.data.model.MeshMessage
import com.example.data.model.MessageType

@Entity(tableName = "mesh_messages")
data class MeshMessageEntity(
    @PrimaryKey
    val msgId: String,
    val version: Int,
    val type: String,
    val category: String,
    val priority: String,
    val senderId: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyM: Float?,
    val peopleCount: Int,
    val note: String,
    val lang: String,
    val createdAt: Long,
    val ttl: Int,
    val hopCount: Int,
    val senderPublicKey: String?,
    val signature: String,
    val isGatewayUploaded: Boolean,
    val isAcknowledged: Boolean,
    val ackByResponder: String?,
    val lastRelayNodeId: String?,
    val isLocalOrigin: Boolean
) {
    fun toDomain(): MeshMessage {
        return MeshMessage(
            msgId = msgId,
            version = version,
            type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.SOS),
            category = runCatching { EmergencyCategory.valueOf(category) }.getOrDefault(EmergencyCategory.FLOOD),
            priority = runCatching { AlertPriority.valueOf(priority) }.getOrDefault(AlertPriority.P1),
            senderId = senderId,
            latitude = latitude,
            longitude = longitude,
            accuracyM = accuracyM,
            peopleCount = peopleCount,
            note = note,
            lang = lang,
            createdAt = createdAt,
            ttl = ttl,
            hopCount = hopCount,
            senderPublicKey = senderPublicKey,
            signature = signature,
            isGatewayUploaded = isGatewayUploaded,
            isAcknowledged = isAcknowledged,
            ackByResponder = ackByResponder,
            lastRelayNodeId = lastRelayNodeId
        )
    }

    companion object {
        fun fromDomain(domain: MeshMessage, isLocalOrigin: Boolean = false): MeshMessageEntity {
            return MeshMessageEntity(
                msgId = domain.msgId,
                version = domain.version,
                type = domain.type.name,
                category = domain.category.name,
                priority = domain.priority.name,
                senderId = domain.senderId,
                latitude = domain.latitude,
                longitude = domain.longitude,
                accuracyM = domain.accuracyM,
                peopleCount = domain.peopleCount,
                note = domain.note,
                lang = domain.lang,
                createdAt = domain.createdAt,
                ttl = domain.ttl,
                hopCount = domain.hopCount,
                senderPublicKey = domain.senderPublicKey,
                signature = domain.signature,
                isGatewayUploaded = domain.isGatewayUploaded,
                isAcknowledged = domain.isAcknowledged,
                ackByResponder = domain.ackByResponder,
                lastRelayNodeId = domain.lastRelayNodeId,
                isLocalOrigin = isLocalOrigin
            )
        }
    }
}

@Entity(tableName = "seen_messages")
data class SeenMessageEntity(
    @PrimaryKey
    val msgId: String,
    val seenAt: Long = System.currentTimeMillis()
)
