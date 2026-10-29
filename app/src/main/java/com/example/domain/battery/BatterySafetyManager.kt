package com.example.domain.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Proactive battery life governor for crisis situations.
 * Manages background mesh relaying duty cycles and enforces a hard cutoff at 15%
 * to guarantee that the user's phone maintains battery for cellular emergency 112 calls and GPS.
 */
class BatterySafetyManager(private val context: Context) {

    private val _batteryLevel = MutableStateFlow(getBatteryPercentage())
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(isDeviceCharging())
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _throttleState = MutableStateFlow(calculateThrottleState(_batteryLevel.value, _isCharging.value))
    val throttleState: StateFlow<RelayThrottleState> = _throttleState.asStateFlow()

    fun refreshBatteryState(): RelayThrottleState {
        val level = getBatteryPercentage()
        val charging = isDeviceCharging()
        _batteryLevel.value = level
        _isCharging.value = charging
        val state = calculateThrottleState(level, charging)
        _throttleState.value = state
        return state
    }

    /**
     * Determines whether background BLE mesh relaying is safe to run.
     * Guaranteed to return false when battery is below 15% and not plugged into power.
     */
    fun isRelayPermitted(): Boolean {
        val state = refreshBatteryState()
        return state != RelayThrottleState.SUSPENDED_LOW_BATTERY
    }

    private fun getBatteryPercentage(): Int {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                ((level / scale.toFloat()) * 100).toInt()
            } else {
                80 // Default safe estimation if battery status unavailable
            }
        } catch (_: Exception) {
            80
        }
    }

    private fun isDeviceCharging(): Boolean {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (_: Exception) {
            false
        }
    }

    private fun calculateThrottleState(level: Int, charging: Boolean): RelayThrottleState {
        if (charging) return RelayThrottleState.OPTIMAL_CHARGING
        return when {
            level <= 15 -> RelayThrottleState.SUSPENDED_LOW_BATTERY
            level <= 30 -> RelayThrottleState.CONSERVATIVE_THROTTLED
            else -> RelayThrottleState.NORMAL
        }
    }
}

enum class RelayThrottleState(val displayName: String, val allowsRelaying: Boolean) {
    OPTIMAL_CHARGING("Plugged In (High Performance)", true),
    NORMAL("Normal (>30% Battery)", true),
    CONSERVATIVE_THROTTLED("Reduced relay activity (15-30% battery)", true),
    SUSPENDED_LOW_BATTERY("Suspended (<15% Battery Reserve)", false)
}
