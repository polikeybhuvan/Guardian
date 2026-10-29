package com.example.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SosMeshApp
import com.example.domain.battery.BatterySafetyManager
import com.example.domain.battery.RelayThrottleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

/**
 * Android Foreground Service for persistent background BLE mesh relaying.
 * Quietly forwards signed emergency packets while the phone is locked,
 * with proactive battery safety cutoffs to guarantee telephone power.
 */
class GuardianMeshRelayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var relayMonitorJob: Job? = null
    private lateinit var batterySafetyManager: BatterySafetyManager

    private var relayedPacketsCount = 0
    private var nearbyTransportStarted = false

    companion object {
        const val CHANNEL_ID = "guardian_mesh_relay_channel"
        const val NOTIFICATION_ID = 91101

        const val ACTION_START_RELAY = "com.example.guardian.ACTION_START_RELAY"
        const val ACTION_STOP_RELAY = "com.example.guardian.ACTION_STOP_RELAY"
        private const val PREFERENCES = "guardian_relay"
        private const val KEY_ENABLED = "enabled"

        fun isRelayEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

        fun startService(context: Context) {
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, true).apply()
            val intent = Intent(context, GuardianMeshRelayService::class.java).apply {
                action = ACTION_START_RELAY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
            val intent = Intent(context, GuardianMeshRelayService::class.java).apply {
                action = ACTION_STOP_RELAY
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        batterySafetyManager = BatterySafetyManager(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_RELAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent == null && !isRelayEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildOngoingNotification(batterySafetyManager.refreshBatteryState()))
        startRelayLoop()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        relayMonitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startRelayLoop() {
        relayMonitorJob?.cancel()
        relayMonitorJob = serviceScope.launch {
            while (isActive) {
                val throttleState = batterySafetyManager.refreshBatteryState()
                updateNotification(throttleState)

                if (throttleState.allowsRelaying) {
                    if (!nearbyTransportStarted) {
                        runCatching { (application as SosMeshApp).routingEngine.startTransport() }
                            .onSuccess { started -> nearbyTransportStarted = started }
                    }
                    delay(8000)
                } else {
                    if (nearbyTransportStarted) {
                        (application as SosMeshApp).routingEngine.stopTransport()
                        nearbyTransportStarted = false
                    }
                    delay(30000)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Guardian Mesh Relay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and relays emergency distress mesh packets with battery safety"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildOngoingNotification(throttleState: RelayThrottleState): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (throttleState.allowsRelaying) {
            "Nearby service active • ${batterySafetyManager.batteryLevel.value}% battery • waiting for peers"
        } else {
            "Suspended (<15% battery) • Reserving power for emergency calls"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian Community Mesh")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.guardian_shield_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(throttleState: RelayThrottleState) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildOngoingNotification(throttleState))
    }
}
