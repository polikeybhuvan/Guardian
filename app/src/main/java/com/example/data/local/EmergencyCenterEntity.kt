package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EmergencyCenter
import com.example.data.model.EmergencyCenterType

/**
 * Room entity storing offline cached hospitals, police stations, and relief centers.
 * Ensures emergency facility markers remain 100% visible and interactive during complete internet loss.
 */
@Entity(tableName = "cached_emergency_centers")
data class EmergencyCenterEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // Stored as Enum name (HOSPITAL, POLICE_STATION, RELIEF_CAMP, FIRE_STATION)
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val address: String = "",
    val isOpen24x7: Boolean = true,
    val specialty: String = "",
    val statusText: String = "Operational 24/7",
    val operationalDetail: String = "Active Emergency Intake",
    val statusBadge: String = "ACTIVE",
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): EmergencyCenter = EmergencyCenter(
        id = id,
        name = name,
        type = runCatching { EmergencyCenterType.valueOf(type) }.getOrDefault(EmergencyCenterType.HOSPITAL),
        latitude = latitude,
        longitude = longitude,
        phoneNumber = phoneNumber,
        address = address,
        isOpen24x7 = isOpen24x7,
        specialty = specialty,
        statusText = statusText,
        operationalDetail = operationalDetail,
        statusBadge = statusBadge
    )

    companion object {
        fun fromDomain(domain: EmergencyCenter): EmergencyCenterEntity = EmergencyCenterEntity(
            id = domain.id,
            name = domain.name,
            type = domain.type.name,
            latitude = domain.latitude,
            longitude = domain.longitude,
            phoneNumber = domain.phoneNumber,
            address = domain.address,
            isOpen24x7 = domain.isOpen24x7,
            specialty = domain.specialty,
            statusText = domain.statusText,
            operationalDetail = domain.operationalDetail,
            statusBadge = domain.statusBadge,
            cachedAt = System.currentTimeMillis()
        )
    }
}
