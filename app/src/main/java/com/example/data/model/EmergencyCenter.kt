package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class EmergencyCenterType(val label: String, val badgeColorHex: Long) {
    HOSPITAL("Hospital / Trauma", 0xFFE53935),
    POLICE_STATION("Police Station", 0xFF1E88E5),
    RELIEF_CAMP("Relief & NDRF Camp", 0xFF43A047),
    FIRE_STATION("Fire & Rescue", 0xFFFB8C00)
}

data class EmergencyCenter(
    val id: String,
    val name: String,
    val type: EmergencyCenterType,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val address: String = "",
    val isOpen24x7: Boolean = true,
    val specialty: String = "",
    val statusText: String = "Operational 24/7",
    val operationalDetail: String = "Active Emergency Intake",
    val statusBadge: String = "ACTIVE"
) {
    /**
     * Haversine distance in meters to a given lat/lng coordinate
     */
    fun distanceMetersTo(targetLat: Double, targetLng: Double): Float {
        val r = 6371000.0 // Earth radius in meters
        val lat1Rad = Math.toRadians(targetLat)
        val lat2Rad = Math.toRadians(latitude)
        val dLat = Math.toRadians(latitude - targetLat)
        val dLng = Math.toRadians(longitude - targetLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }

    /**
     * Initial compass bearing from (originLat, originLng) towards this center (0° - 360°)
     */
    fun bearingFrom(originLat: Double, originLng: Double): Float {
        val lat1 = Math.toRadians(originLat)
        val lat2 = Math.toRadians(latitude)
        val dLng = Math.toRadians(longitude - originLng)

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return ((bearingDeg + 360) % 360).toFloat()
    }

    fun formattedDistance(originLat: Double?, originLng: Double?): String {
        if (originLat == null || originLng == null) return "Distance unavailable"
        val distM = distanceMetersTo(originLat, originLng)
        return if (distM >= 1000) {
            String.format(java.util.Locale.getDefault(), "%.1f km", distM / 1000f)
        } else {
            "${distM.toInt()} m"
        }
    }
}
