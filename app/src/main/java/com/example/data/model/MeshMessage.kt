package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class MessageType {
    SOS,
    SAFE,
    ACK,
    OFFICIAL_ALERT,
    CANCEL
}

enum class EmergencyCategory(val displayName: String, val iconName: String) {
    FLOOD("Water Rising / Flood", "flood"),
    MEDICAL("Medical / Injury", "medical"),
    WOMEN_SAFETY("Women's safety / Threat", "women_safety"),
    KIDNAPPING("Kidnapping / Missing person", "kidnapping"),
    THEFT("Theft / Robbery", "theft"),
    CYBER_FRAUD("Online / Financial fraud", "cyber_fraud"),
    TRAPPED("Trapped / Debris Collapse", "trapped"),
    FIRE("Fire Hazard", "fire"),
    LANDSLIDE("Landslide / Collapse", "landslide"),
    STRANDED("Lost / Stranded", "stranded"),
    SUPPLIES("Need Food & Water", "supplies"),
    SAFE("I Am Safe", "safe"),
    OTHER("Other Emergency", "other")
}

enum class AlertPriority(val code: String, val level: Int, val label: String) {
    P1("P1", 1, "Life-Threatening"),
    P2("P2", 2, "Urgent"),
    P3("P3", 3, "Moderate"),
    P4("P4", 4, "Low / Info")
}

enum class DeliveryStep(val title: String, val order: Int) {
    LOCAL_SAVED("Saved Locally", 1),
    PEER_RELAYED("Sent to Peers", 2),
    GATEWAY_UPLOADED("Reached Internet", 3),
    RESPONDER_ACKNOWLEDGED("Responder Acknowledged", 4)
}

@JsonClass(generateAdapter = true)
data class MeshMessage(
    val msgId: String = UUID.randomUUID().toString(),
    val version: Int = 1,
    val type: MessageType = MessageType.SOS,
    val category: EmergencyCategory = EmergencyCategory.FLOOD,
    val priority: AlertPriority = AlertPriority.P1,
    val senderId: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyM: Float? = null,
    val peopleCount: Int = 1,
    val note: String = "",
    val lang: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val ttl: Int = 8,
    val hopCount: Int = 0,
    val senderPublicKey: String? = null,
    val signature: String = "",
    val isGatewayUploaded: Boolean = false,
    val isAcknowledged: Boolean = false,
    val ackByResponder: String? = null,
    val lastRelayNodeId: String? = null
)
