package com.example.data.model

import com.example.domain.ai.OfflineGemmaTriageReport
import java.util.UUID

/**
 * Message in the conversational Gemma AI Triage consultation interface.
 */
data class TriageChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val triageReport: OfflineGemmaTriageReport? = null,
    val quickFollowUps: List<String> = emptyList()
)
