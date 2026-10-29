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
        val sb = StringBuilder()
        sb.append("🚨 [EMERGENCY SOS DISTRESS ALERT] 🚨\n")
        sb.append("Incident: ${category.displayName} ($peopleCount ${if (peopleCount == 1) "person" else "people"})\n")

        if (note.isNotBlank()) {
            sb.append("Details: ${note.trim()}\n")
        }

        if (coordinates != null) {
            sb.append("Live Location: https://maps.google.com/?q=${coordinates.latitude},${coordinates.longitude}\n")
            sb.append("GPS Coords: ${String.format(Locale.US, "%.5f", coordinates.latitude)}, ${String.format(Locale.US, "%.5f", coordinates.longitude)} (±${coordinates.accuracyM.toInt()}m)\n")
        } else {
            sb.append("Live Location: [GPS acquiring...]\n")
        }

        nearestHospital?.let { hosp ->
            val dist = hosp.formattedDistance(coordinates?.latitude, coordinates?.longitude)
            sb.append("Nearest Hospital: ${hosp.name} ($dist) Ph: ${hosp.phoneNumber}\n")
        }

        nearestPolice?.let { pol ->
            val dist = pol.formattedDistance(coordinates?.latitude, coordinates?.longitude)
            sb.append("Nearest Police: ${pol.name} ($dist) Ph: ${pol.phoneNumber}\n")
        }

        nearestRelief?.let { rel ->
            val dist = rel.formattedDistance(coordinates?.latitude, coordinates?.longitude)
            sb.append("Nearest Relief: ${rel.name} ($dist) Ph: ${rel.phoneNumber}\n")
        }

        sb.append("This SMS goes only to your saved Guardian contacts; it does not alert police or dispatch help. ")
        sb.append("If there is immediate danger in India, call 112.")
        return sb.toString()
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
                error = "SMS permission not granted. Please allow SEND_SMS in permission settings."
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
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        var sentCount = 0
        var failedCount = 0

        val parts = smsManager.divideMessage(messageText)

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
            } catch (e: Exception) {
                failedCount++
            }
        }

        return SmsDispatchResult(
            isSuccess = sentCount > 0,
            sentCount = sentCount,
            failedCount = failedCount,
            messageText = messageText,
            error = if (failedCount > 0 && sentCount == 0) "Failed to transmit SMS to contacts" else null
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
}
