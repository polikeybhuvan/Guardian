package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class DeviceCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): DeviceCoordinates? {
        return runCatching {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            location?.let {
                DeviceCoordinates(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyM = it.accuracy
                )
            } ?: getLastKnownCoordinates()
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownCoordinates(): DeviceCoordinates? {
        return runCatching {
            val lastLoc: Location? = fusedLocationClient.lastLocation.await()
            lastLoc?.let {
                DeviceCoordinates(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyM = it.accuracy
                )
            }
        }.getOrNull()
    }
}
