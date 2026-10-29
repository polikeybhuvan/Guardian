package com.example.data.location

import android.content.Intent
import android.net.Uri
import com.example.data.model.EmergencyCenter
import com.example.domain.mesh.MeshPeerNode

/**
 * Data source for nearby emergency facilities.
 *
 * No location-verified facility provider is configured yet, so this repository intentionally
 * returns no facility results instead of showing helpline numbers at invented map coordinates.
 */
class EmergencyCentersRepository {

    fun getEmergencyCenters(userLat: Double?, userLng: Double?): List<EmergencyCenter> = emptyList()

    fun getSurroundingMeshNodes(userLat: Double?, userLng: Double?): List<MeshPeerNode> = emptyList()

    fun getNearestHospital(userLat: Double?, userLng: Double?): EmergencyCenter? = null

    fun getNearestPoliceStation(userLat: Double?, userLng: Double?): EmergencyCenter? = null

    fun getNearestReliefCamp(userLat: Double?, userLng: Double?): EmergencyCenter? = null

    fun createNearbyHospitalsMapIntent(userLat: Double?, userLng: Double?): Intent =
        createMapSearchIntent("hospital", userLat, userLng)

    fun createNearbyPoliceMapIntent(userLat: Double?, userLng: Double?): Intent =
        createMapSearchIntent("police station", userLat, userLng)

    fun createNearbyFireMapIntent(userLat: Double?, userLng: Double?): Intent =
        createMapSearchIntent("fire station", userLat, userLng)

    private fun createMapSearchIntent(query: String, latitude: Double?, longitude: Double?): Intent {
        val uri = if (latitude != null && longitude != null) {
            Uri.parse("geo:$latitude,$longitude?q=${Uri.encode(query)}&z=14")
        } else {
            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        }
        return Intent(Intent.ACTION_VIEW, uri)
    }
}
