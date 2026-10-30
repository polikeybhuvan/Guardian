package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume

data class DeviceCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float
)

/**
 * Location access shared by the map and SOS flow. The fused provider can use GPS without a data
 * connection; the framework GPS provider is a fallback for devices where Play services returns
 * no fix. Every active request is bounded so an SOS can still be sent when GPS is unavailable.
 */
class LocationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(timeoutMillis: Long = 12_000): DeviceCoordinates? {
        if (!hasLocationPermission()) return null

        val totalTimeout = timeoutMillis.coerceAtLeast(1_000L)
        val startedAt = android.os.SystemClock.elapsedRealtime()
        val cached = withTimeoutOrNull(minOf(1_500L, totalTimeout / 4)) {
            getLastKnownCoordinates()
        }
        val fusedBudget = (totalTimeout * 2 / 3).coerceAtLeast(1_000L)

        val fusedFix = withTimeoutOrNull(fusedBudget) {
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        }
        fusedFix?.let { return it.toCoordinates() }

        val remaining = (totalTimeout - (android.os.SystemClock.elapsedRealtime() - startedAt))
            .coerceAtLeast(1_000L)
        val gpsFix = withTimeoutOrNull(remaining) { requestPlatformGpsFix() }
        return gpsFix?.toCoordinates() ?: cached
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownCoordinates(): DeviceCoordinates? {
        if (!hasLocationPermission()) return null

        val candidates = buildList {
            val fusedLastKnown = try {
                fusedLocationClient.lastLocation.await()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            fusedLastKnown?.let(::add)
            val providers = runCatching { locationManager.getProviders(true) }.getOrDefault(emptyList())
            providers.forEach { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()?.let(::add)
            }
        }
        return candidates.maxByOrNull(Location::getTime)?.toCoordinates()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestPlatformGpsFix(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission() || !runCatching {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }.getOrDefault(false)
        ) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        lateinit var listener: LocationListener
        fun finish(location: Location?) {
            if (continuation.isActive) continuation.resume(location)
            runCatching { locationManager.removeUpdates(listener) }
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = finish(location)
            override fun onProviderDisabled(provider: String) = finish(null)
            override fun onProviderEnabled(provider: String) = Unit
            @Deprecated("Deprecated by Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        continuation.invokeOnCancellation {
            runCatching { locationManager.removeUpdates(listener) }
        }
        runCatching {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1_000L,
                0f,
                listener,
                Looper.getMainLooper()
            )
        }.onFailure { finish(null) }
    }

    private fun Location.toCoordinates() = DeviceCoordinates(
        latitude = latitude,
        longitude = longitude,
        accuracyM = if (hasAccuracy()) accuracy else Float.NaN
    )
}
