package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TriageResult(
    val priority: AlertPriority,
    val shortNote: String,
    val flags: List<String>,
    val detectedLanguage: String = "hi",
    val englishTranslation: String? = null,
    val isAiPowered: Boolean = false,
    val conditionSummary: String? = null,
    val instructions: List<String> = emptyList()
)
