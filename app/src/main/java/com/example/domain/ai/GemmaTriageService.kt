package com.example.domain.ai

import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCategory
import com.example.data.model.TriageResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GemmaTriageService(private val offlineChatModel: OfflineGemmaChatModel? = null) {

    val offlineGemmaEngine = OfflineGemmaEngine()

    val isOfflineGemmaInstalled: Boolean get() = offlineChatModel?.isInstalled == true
    val offlineGemmaStatus: StateFlow<String> = offlineChatModel?.status
        ?: MutableStateFlow("Emergency rules available · Gemma model missing")

    suspend fun prepareBundledGemmaModel() {
        offlineChatModel?.prepareBundledModel()
    }

    suspend fun installOfflineGemmaModel(sourceName: String, source: java.io.InputStream) {
        checkNotNull(offlineChatModel) { "Offline Gemma model storage is unavailable." }.importModel(sourceName, source)
    }

    /** Emergency-scope gate runs before either Gemma or the deterministic fallback. */
    suspend fun processChatInputAsync(
        userInput: String,
        history: List<com.example.data.model.TriageChatMessage> = emptyList()
    ): com.example.data.model.TriageChatMessage {
        val trimmed = userInput.trim()
        if (!isEmergencyRelated(trimmed)) {
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = "Sorry, I can only help regarding emergency situations."
            )
        }

        // Keep the vetted, specific local protocols as fast answers. Gemma handles other
        // emergency questions when its model is installed; without it, rules remain available.
        val localProtocol = trimmed.lowercase(java.util.Locale.ROOT).let {
            listOf("tourniquet", "cpr", "cardiac arrest", "recovery position", "unconscious breathing", "choking", "heimlich")
                .any(it::contains)
        }
        val model = offlineChatModel
        if (localProtocol || model?.isInstalled != true) return processChatInput(trimmed, history)

        val answer = runCatching { model.reply(trimmed) }.getOrElse {
            return processChatInput(trimmed, history)
        }
        return com.example.data.model.TriageChatMessage(isUser = false, text = answer)
    }

    private fun isEmergencyRelated(input: String): Boolean {
        val text = input.lowercase(java.util.Locale.ROOT)
        if (text.isBlank()) return false
        val unrelatedIntents = listOf(
            "capital of", "tell me a joke", "write a story", "homework", "assignment", "recipe",
            "weather forecast", "translate this", "song lyrics", "movie recommendation", "programming help"
        )
        if (unrelatedIntents.any { intent -> text.contains(intent) }) return false
        val emergencyTerms = listOf(
            "emergency", "urgent", "danger", "emergency number", "112", "sos", "first aid",
            "injur", "bleed", "blood", "wound", "burn", "fractur", "broken bone", "hurt",
            "unconscious", "breath", "chok", "cpr", "heart attack", "stroke", "seizure", "poison",
            "overdose", "snake bite", "snakebite", "drown", "fire", "smoke", "flood", "earthquake",
            "cyclone", "landslide", "trapped", "rubble", "disaster", "evacuat", "kidnap", "abduct",
            "stalk", "harass", "assault", "attack", "violence", "women safety", "woman safety",
            "unsafe", "followed", "theft", "robber", "robbery", "missing person", "lost child",
            "police", "ambulance", "hospital", "rescue", "survival", "tourniquet", "recovery position",
            "heat stroke", "hypothermia", "water safety", "fire extinguisher", "bleeding", "injury", "accident",
            "bachao", "khatra", "aag lag", "baadh", "bhukamp", "chot lag", "khoon beh", "saans nahi",
            "behosh", "durghatna", "suraksha", "madad karo", "mahila suraksha", "hamla", "kidnap"
        )
        return emergencyTerms.any { term -> text.contains(term) }
    }

    fun analyzeSymptomsDetailed(
        symptoms: String,
        category: EmergencyCategory = EmergencyCategory.MEDICAL,
        peopleCount: Int = 1
    ): OfflineGemmaTriageReport {
        return offlineGemmaEngine.analyzeSymptoms(symptoms, category, peopleCount)
    }

    /**
     * Interactive Chat Consultation Engine.
     * Takes conversational symptom input or first-aid queries, returns clinical triage advice,
     * life-saving steps, and context-aware follow-up suggestions.
     */
    fun processChatInput(
        userInput: String,
        history: List<com.example.data.model.TriageChatMessage> = emptyList()
    ): com.example.data.model.TriageChatMessage {
        val trimmed = userInput.trim()
        if (!isEmergencyRelated(trimmed)) {
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = "Sorry, I can only help regarding emergency situations."
            )
        }
        val lower = trimmed.lowercase(java.util.Locale.ROOT)

        // 1. Check for targeted medical/first-aid protocol questions
        if (lower.contains("tourniquet") || lower.contains("tourniquet kaise") || lower.contains("make tourniquet")) {
            val text = """
                🩸 **IMPROVISED TOURNIQUET PROTOCOL (CRITICAL LIFE SUPPORT)**

                • **When to Use:** Only for severe, life-threatening arterial bleeding from limbs (arms or legs) where direct pressure fails.
                • **Supplies Needed:** Wide cloth or belt (at least 2 inches wide), rigid stick (rod, wrench, thick branch), secure cord.

                **Step-by-Step Instructions:**
                1. **Position:** Place the band 2 to 3 inches above the bleeding wound. NEVER place directly over a joint (elbow or knee).
                2. **Initial Knot:** Wrap tightly around limb and tie a half-knot.
                3. **Windlass Stick:** Place the rigid stick on top of the knot and tie a full square knot over it.
                4. **Twist to Occlude:** Turn the stick firmly until arterial bleeding stops and the distal pulse disappears.
                5. **Secure the Stick:** Tie the ends of the cloth around the stick so it cannot unwind.
                6. **Document Time:** Write the exact time on victim's forehead (e.g., **"T 14:30"**) with pen/marker.

                ⚠️ **CRITICAL WARNING:** NEVER loosen or remove the tourniquet once placed. Only hospital trauma surgeons can safely release it.
            """.trimIndent()
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = text,
                quickFollowUps = listOf("What if bleeding continues?", "How to prevent shock?", "Apply to SOS Alert")
            )
        }

        if (lower.contains("cpr") || lower.contains("chest compression") || lower.contains("how to do cpr") || lower.contains("cardiac arrest")) {
            val text = """
                🫀 **CPR EMERGENCY PROTOCOL (ADULT LIFE SUPPORT)**

                • **Immediate Check:** Shake shoulders and shout: "Are you OK?". Look at chest for normal breathing for 5 seconds.
                • **If Unresponsive & Not Breathing:** Begin CPR immediately.

                **Step-by-Step Instructions:**
                1. **Position:** Place victim flat on their back on a hard surface (floor or ground).
                2. **Hand Placement:** Place heel of one hand on the center of the chest (lower half of breastbone). Interlock fingers of other hand on top.
                3. **Compression Rate:** Push hard and fast at **100 to 120 compressions per minute** (rhythm of "Stayin' Alive").
                4. **Compression Depth:** Push down at least **2 inches (5 cm)**, allowing full chest recoil after each pump.
                5. **Cycle:** Perform 30 compressions followed by 2 rescue breaths (tilt head back, pinch nose, blow for 1 second until chest rises). If untrained, perform **Hands-Only CPR** continuously.

                ⚠️ **DO NOT STOP** until emergency medical services arrive, an automated external defibrillator (AED) is attached, or victim begins breathing normally.
            """.trimIndent()
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = text,
                quickFollowUps = listOf("CPR for Children/Infants", "How to use AED?", "What is Recovery Position?")
            )
        }

        if (lower.contains("recovery position") || lower.contains("unconscious breathing")) {
            val text = """
                🛌 **RECOVERY POSITION (KEEP AIRWAY OPEN)**

                • **When to Use:** For an unresponsive victim who IS breathing normally and has NO suspected spinal trauma.
                
                **Step-by-Step Instructions:**
                1. Kneel next to victim. Place the arm nearest to you out at a right angle, palm up.
                2. Bring their far arm across chest, placing the back of their hand against their near cheek.
                3. Grasp their far knee and pull it up so the foot is flat on the ground.
                4. Gently roll the victim towards you onto their side using the bent knee as leverage.
                5. Tilt head gently backward to maintain open airway. Ensure mouth is facing downward so vomit or saliva drains freely.
            """.trimIndent()
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = text,
                quickFollowUps = listOf("How to check breathing?", "What if they stop breathing?", "Apply to SOS Alert")
            )
        }

        if (lower.contains("choking") || lower.contains("heimlich") || lower.contains("gasping for air")) {
            val text = """
                🫁 **CHOKING & SEVERE AIRWAY OBSTRUCTION**

                • **Check:** If victim can cough loudly or speak, encourage coughing. If unable to make sound or making high-pitched stridor:

                **Step-by-Step Instructions (5 & 5 Protocol):**
                1. **5 Back Blows:** Stand slightly behind and to the side. Support victim's chest with one hand and lean them forward. Deliver 5 sharp blows between shoulder blades with heel of your hand.
                2. **5 Abdominal Thrusts (Heimlich):** Stand behind victim. Wrap arms around waist. Make a fist with thumb side against stomach just above navel. Grasp fist with other hand and thrust inward and upward forcefully 5 times.
                3. **Repeat:** Alternate 5 back blows and 5 abdominal thrusts until object is expelled or victim loses consciousness.
                4. **If Unconscious:** Lower to floor and begin CPR immediately. Look in mouth before rescue breaths; sweep object only if clearly visible.
            """.trimIndent()
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = text,
                quickFollowUps = listOf("Choking in infants", "What if victim passes out?", "Apply to SOS Alert")
            )
        }

        val emergencyGuidance = when {
            listOf("stalk", "followed", "harass", "assault", "women safety", "woman safety", "unsafe", "abduct", "kidnap")
                .any(lower::contains) -> "If you are in immediate danger, call 112 now if you can. Move toward a staffed, well-lit public place; avoid confronting the person. Share your live location and a short message with a trusted contact. If speaking is unsafe, use your phone's emergency SOS options."
            listOf("theft", "robber", "robbery", "stolen", "snatch")
                .any(lower::contains) -> "Move to a safe place and do not chase or confront the person. Call 112 if anyone is in danger or injured. When safe, note what happened and contact local police and your bank or service provider if cards or devices were taken."
            listOf("flood", "baadh", "earthquake", "bhukamp", "cyclone", "landslide", "disaster", "evacuat")
                .any(lower::contains) -> "Follow official local evacuation instructions. Move away from floodwater, damaged buildings, and downed wires; take essential medicine and identification only if it is safe. Call 112 for immediate danger and tell a trusted contact where you are going."
            listOf("fire", "smoke", "aag lag").any(lower::contains) -> "Leave the building by the nearest safe exit; stay low under smoke and do not use lifts. Do not re-enter. Call 112 once safe and tell responders your location and whether anyone is trapped."
            else -> null
        }
        if (emergencyGuidance != null) {
            return com.example.data.model.TriageChatMessage(
                isUser = false,
                text = emergencyGuidance,
                quickFollowUps = listOf("Call 112", "Share location with a contact")
            )
        }

        // 2. Comprehensive Symptom Analysis with Offline Gemma Engine
        val report = offlineGemmaEngine.analyzeSymptoms(trimmed, EmergencyCategory.MEDICAL, 1)

        val headerEmoji = when (report.priority) {
            AlertPriority.P1 -> "🚨 **CRITICAL EMERGENCY (P1 - IMMEDIATE LIFE SUPPORT)**"
            AlertPriority.P2 -> "⚠️ **URGENT EMERGENCY (P2 - RAPID CARE REQUIRED)**"
            AlertPriority.P3 -> "🟡 **MODERATE EMERGENCY (P3 - DELAYED CARE)**"
            AlertPriority.P4 -> "🟢 **MINOR SYMPTOM (P4 - FIRST AID / OBSERVATION)**"
        }

        val actionStepsText = report.actionSteps.take(3).joinToString("\n") { step ->
            "${step.stepNumber}. **${step.title}:** ${step.instruction}"
        }

        val warningsText = if (report.criticalWarnings.isNotEmpty()) {
            "\n\n⚠️ **CRITICAL WARNING:** ${report.criticalWarnings.first()}"
        } else ""

        val conversationalAdvice = """
            $headerEmoji

            **Clinical Assessment:** ${report.conditionName}
            *${report.conditionNameHindi}*

            ${report.clinicalRationale}

            **Immediate Priority Steps:**
            $actionStepsText$warningsText
        """.trimIndent()

        val followUps = mutableListOf<String>()
        if (report.requiresTourniquet) followUps.add("How to make a tourniquet?")
        if (report.requiresCprRhythm) followUps.add("How to do CPR?")
        followUps.add("How to prevent shock?")
        followUps.add("Apply to SOS Alert")

        return com.example.data.model.TriageChatMessage(
            isUser = false,
            text = conversationalAdvice,
            triageReport = report,
            quickFollowUps = followUps.take(3)
        )
    }

    /**
     * Local on-device Gemma AI triage engine.
     * Evaluates symptoms, determines clinical triage code, generates step-by-step instructions.
     */
    fun triageOffline(
        category: EmergencyCategory,
        rawNote: String,
        peopleCount: Int
    ): TriageResult {
        val lower = rawNote.lowercase()
        val gemmaReport = offlineGemmaEngine.analyzeSymptoms(rawNote, category, peopleCount)

        val shortNote = if (rawNote.isBlank()) {
            "${category.displayName} - $peopleCount person(s)"
        } else {
            rawNote.take(140).trim()
        }

        val isHindi = lower.any { it in "अआइईउऊऋएऐओऔकखगघचछजझटठडढणतथदधनपफबभमयरलवशषसह" } ||
                lower.contains("hai") || lower.contains("madad") || lower.contains("bach") || lower.contains("khun")

        val instructions = gemmaReport.actionSteps.map {
            if (isHindi) "${it.stepNumber}. ${it.titleHindi}: ${it.instructionHindi}"
            else "${it.stepNumber}. ${it.title}: ${it.instruction}"
        }

        return TriageResult(
            priority = gemmaReport.priority,
            shortNote = shortNote,
            flags = gemmaReport.detectedSymptomTags,
            detectedLanguage = if (isHindi) "hi" else "en",
            isAiPowered = false,
            conditionSummary = if (isHindi) gemmaReport.conditionNameHindi else gemmaReport.conditionName,
            instructions = instructions
        )
    }
}
