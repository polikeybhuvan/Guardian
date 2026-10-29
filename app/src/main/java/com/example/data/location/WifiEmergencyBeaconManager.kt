package com.example.data.location

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Starts Android's local-only hotspot. Android chooses its SSID, and may reject the request
 * depending on device policy, permissions, or whether another hotspot is active.
 */
class WifiEmergencyBeaconManager(private val context: Context) {

    private val _isBeaconActive = MutableStateFlow(false)
    val isBeaconActive: StateFlow<Boolean> = _isBeaconActive.asStateFlow()

    private val _activeSsid = MutableStateFlow<String?>(null)
    val activeSsid: StateFlow<String?> = _activeSsid.asStateFlow()

    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null

    /**
     * Returns a UI label; Android controls the actual hotspot SSID.
     */
    fun generateEmergencySsid(latitude: Double? = null, longitude: Double? = null, category: String = "SOS"): String {
        return "Emergency hotspot"
    }

    /**
     * Requests Android to start a local-only hotspot.
     */
    fun startEmergencyBeacon(latitude: Double? = null, longitude: Double? = null, category: String = "SOS") {
        if (_isBeaconActive.value || hotspotReservation != null) return
        _activeSsid.value = null
        _isBeaconActive.value = false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.let { wm ->
                    wm.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                        override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                            super.onStarted(reservation)
                            if (reservation == null) {
                                _isBeaconActive.value = false
                                _activeSsid.value = null
                                return
                            }
                            hotspotReservation = reservation
                            _isBeaconActive.value = true
                            @Suppress("DEPRECATION")
                            _activeSsid.value = reservation.wifiConfiguration?.SSID ?: "Emergency hotspot active"
                        }

                        override fun onStopped() {
                            super.onStopped()
                            hotspotReservation = null
                            _isBeaconActive.value = false
                            _activeSsid.value = null
                        }

                        override fun onFailed(reason: Int) {
                            super.onFailed(reason)
                            hotspotReservation = null
                            _isBeaconActive.value = false
                            _activeSsid.value = null
                        }
                    }, Handler(Looper.getMainLooper()))
                }
            }
        } catch (_: SecurityException) {
            _isBeaconActive.value = false
            _activeSsid.value = null
        } catch (_: Exception) {
            _isBeaconActive.value = false
            _activeSsid.value = null
        }
    }

    /**
     * Stops the Wi-Fi emergency hotspot beacon and releases the reservation.
     */
    fun stopEmergencyBeacon() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) hotspotReservation?.close()
        } catch (_: Exception) {
        } finally {
            hotspotReservation = null
            _isBeaconActive.value = false
            _activeSsid.value = null
        }
    }

    /**
     * Toggles the Wi-Fi emergency beacon.
     */
    fun toggleEmergencyBeacon(latitude: Double? = null, longitude: Double? = null) {
        if (_isBeaconActive.value) {
            stopEmergencyBeacon()
        } else {
            startEmergencyBeacon(latitude, longitude)
        }
    }
}
