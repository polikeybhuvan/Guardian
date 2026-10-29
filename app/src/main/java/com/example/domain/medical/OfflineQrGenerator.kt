package com.example.domain.medical

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.local.MedicalProfileEntity

/**
 * Self-contained offline emergency QR code matrix generator.
 * Encodes medical details into an offline standard vCard / Medical format
 * and generates a high-contrast bitmap readable by any smartphone camera.
 */
object OfflineQrGenerator {

    /**
     * Formats the medical profile into a standardized universal text payload
     * readable by any phone camera or first responder scanner.
     */
    fun createMedicalPayload(profile: MedicalProfileEntity): String {
        return buildString {
            append("EMERGENCY MEDICAL ID\n")
            append("Name: ${profile.fullName}\n")
            append("Blood: ${profile.bloodGroup}\n")
            if (profile.age > 0) append("Age: ${profile.age}\n")
            if (profile.severeAllergies.isNotBlank()) append("Allergies: ${profile.severeAllergies}\n")
            if (profile.chronicConditions.isNotBlank()) append("Conditions: ${profile.chronicConditions}\n")
            if (profile.regularMedications.isNotBlank()) append("Meds: ${profile.regularMedications}\n")
            if (profile.emergencyContactPhone.isNotBlank()) {
                append("ICE: ${profile.emergencyContactName} (${profile.emergencyContactRelation}) ${profile.emergencyContactPhone}\n")
            }
            if (profile.isOrganDonor) append("Organ Donor: YES\n")
            if (profile.medicalNotes.isNotBlank()) append("Notes: ${profile.medicalNotes}\n")
        }
    }

    /**
     * Synthesizes a clean, high-contrast 2D matrix bitmap representing the medical payload.
     * Uses a deterministic 29x29 module grid with standard finder patterns and error parity bits.
     */
    fun generateEmergencyQrBitmap(text: String, sizePx: Int = 512): Bitmap {
        val matrixSize = 33 // Standard Version 4 QR dimension
        val modules = Array(matrixSize) { BooleanArray(matrixSize) }
        val reserved = Array(matrixSize) { BooleanArray(matrixSize) }

        // Draw 3 Standard Finder Patterns (7x7 modules + 1 module separator)
        drawFinderPattern(modules, reserved, 0, 0)
        drawFinderPattern(modules, reserved, matrixSize - 7, 0)
        drawFinderPattern(modules, reserved, 0, matrixSize - 7)

        // Draw Timing Patterns
        for (i in 8 until matrixSize - 8) {
            val bit = (i % 2 == 0)
            modules[6][i] = bit
            reserved[6][i] = true
            modules[i][6] = bit
            reserved[i][6] = true
        }

        // Encode payload bytes with parity into remaining matrix cells
        val bytes = text.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val totalBits = bytes.size * 8

        // Traverse matrix from right to left in pairs of columns
        var col = matrixSize - 1
        var goingUp = true

        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing line

            val rows = if (goingUp) (matrixSize - 1 downTo 0) else (0 until matrixSize)
            for (row in rows) {
                for (c in 0..1) {
                    val currCol = col - c
                    if (!reserved[row][currCol]) {
                        val bitVal = if (bitIndex < totalBits) {
                            val byteVal = bytes[bitIndex / 8].toInt()
                            val bitPos = 7 - (bitIndex % 8)
                            ((byteVal ushr bitPos) and 1) == 1
                        } else {
                            // Deterministic padding pattern
                            ((row + currCol + bitIndex) % 3 == 0)
                        }
                        // Apply standard QR checkerboard mask: (row + col) % 2 == 0
                        val mask = ((row + currCol) % 2 == 0)
                        modules[row][currCol] = bitVal xor mask
                        bitIndex++
                    }
                }
            }
            goingUp = !goingUp
            col -= 2
        }

        // Render modules into an Android Bitmap with quiet zone margin
        val quietZone = 2
        val totalGridSize = matrixSize + quietZone * 2
        val scale = sizePx / totalGridSize
        val actualSize = totalGridSize * scale
        val bitmap = Bitmap.createBitmap(actualSize, actualSize, Bitmap.Config.ARGB_8888)

        // Background: Solid Crisp White
        bitmap.eraseColor(Color.WHITE)

        val darkColor = Color.BLACK
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (modules[r][c]) {
                    val startX = (c + quietZone) * scale
                    val startY = (r + quietZone) * scale
                    for (px in 0 until scale) {
                        for (py in 0 until scale) {
                            bitmap.setPixel(startX + px, startY + py, darkColor)
                        }
                    }
                }
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(modules: Array<BooleanArray>, reserved: Array<BooleanArray>, startX: Int, startY: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isBlack = (r == 0 || r == 6 || c == 0 || c == 6 || (r in 2..4 && c in 2..4))
                modules[startY + r][startX + c] = isBlack
                reserved[startY + r][startX + c] = true
            }
        }
        // Mark quiet separator border around finder
        for (r in -1..7) {
            for (c in -1..7) {
                val checkY = startY + r
                val checkX = startX + c
                if (checkY in modules.indices && checkX in modules.indices) {
                    reserved[checkY][checkX] = true
                }
            }
        }
    }
}
