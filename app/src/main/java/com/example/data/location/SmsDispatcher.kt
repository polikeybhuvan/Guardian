package com.example.data.location

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.data.local.EmergencyContactEntity
import com.example.data.model.EmergencyCategory
import com.example.data.model.EmergencyCenter
import kotlin.math.roundToInt
import java.util.Locale

data class SmsDispatchResult(
    val isSuccess: Boolean,
    val sentCount: Int,
    val failedCount: Int,
    val messageText: String,
    val error: String? = null
)

class SmsDispatcher(private val context: Context) {

    fun composeDistressMessage(
        category: EmergencyCategory,
        peopleCount: Int,
        note: String,
        coordinates: DeviceCoordinates?,
        nearestHospital: EmergencyCenter?,
        nearestPolice: EmergencyCenter?,
        nearestRelief: EmergencyCenter?
    ): String {
        // Keep the core alert short and plain-text so it normally fits in one SMS segment.
        // Long Unicode/multipart messages are more likely to be delayed or dropped by carriers.
        // Facility records are not verified, so they are intentionally omitted from SOS SMS.
        val incident = category.displayName.replace(Regex("\\s+"), " ").trim().take(32)
        val location = coordinates?.let {
            "https://maps.google.com/?q=${String.format(Locale.US, "%.5f", it.latitude)},${String.format(Locale.US, "%.5f", it.longitude)}"
        } ?: "GPS unavailable"
        val accuracy = coordinates?.accuracyM
            ?.takeIf { it.isFinite() && it >= 0f }
            ?.let { " +/-${it.roundToInt()}m" }
            .orEmpty()
        val core = "GUARDIAN SOS: $incident; ${peopleCount.coerceAtLeast(1)}p; $location$accuracy; call 112"
        val cleanNote = note.replace(Regex("\\s+"), " ").trim()
        if (cleanNote.isEmpty()) return core

        val separator = "; "
        val availableNoteLength = (160 - core.length - separator.length).coerceAtLeast(0)
        val shortNote = cleanNote.take(minOf(36, availableNoteLength))
        return if (shortNote.isEmpty()) core else "$core$separator$shortNote"
    }

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Sends SMS directly using Android SmsManager to all provided emergency contacts.
     */
    fun sendDirectSms(
        contacts: List<EmergencyContactEntity>,
        messageText: String
    ): SmsDispatchResult {
        if (!hasSmsPermission()) {
            return SmsDispatchResult(
                isSuccess = false,
                sentCount = 0,
                failedCount = contacts.size,
                messageText = messageText,
                error = "SMS permission is unavailable for this install. Open Messages to review and send the SOS."
            )
        }

        if (contacts.isEmpty()) {
            return SmsDispatchResult(
                isSuccess = false,
                sentCount = 0,
                failedCount = 0,
                messageText = messageText,
                error = "No emergency contacts saved. Please add emergency contacts."
            )
        }

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            legacyDefaultSmsManager()
        } ?: return SmsDispatchResult(
            isSuccess = false,
            sentCount = 0,
            failedCount = contacts.size,
            messageText = messageText,
            error = "SMS service is unavailable on this device."
        )

        var sentCount = 0
        var failedCount = 0

        val parts = try {
            smsManager.divideMessage(messageText)
        } catch (error: Exception) {
            return SmsDispatchResult(
                isSuccess = false,
                sentCount = 0,
                failedCount = contacts.size,
                messageText = messageText,
                error = error.message ?: "Android could not prepare the SOS SMS."
            )
        }
        var firstError: String? = null

        for (contact in contacts) {
            val cleanPhone = contact.phoneNumber.replace("[^0-9+]".toRegex(), "")
            if (cleanPhone.isBlank()) {
                failedCount++
                continue
            }

            try {
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, messageText, null, null)
                }
                sentCount++
            } catch (error: Exception) {
                failedCount++
                if (firstError == null) firstError = error.message ?: error.javaClass.simpleName
            }
        }

        return SmsDispatchResult(
            isSuccess = sentCount > 0,
            sentCount = sentCount,
            failedCount = failedCount,
            messageText = messageText,
            error = if (failedCount > 0) firstError ?: "Android could not submit the SMS to every contact." else null
        )
    }

    /**
     * Fallback intent to open standard messaging app prefilled with all numbers and distress text.
     */
    fun createMessagingIntent(contacts: List<EmergencyContactEntity>, messageText: String): Intent {
        val phoneNumbers = contacts.map { it.phoneNumber.trim() }.filter { it.isNotEmpty() }.joinToString(";")
        val uri = if (phoneNumbers.isNotEmpty()) Uri.parse("smsto:$phoneNumbers") else Uri.parse("smsto:")
        return Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    @Suppress("DEPRECATION")
    private fun legacyDefaultSmsManager(): SmsManager = SmsManager.getDefault()
}
