package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.location.DeviceCoordinates
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun EmergencyMapScreen(
    currentCoordinates: DeviceCoordinates?,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var offlineMapFile by remember {
        mutableStateOf(indiaMapFile(context).takeIf { it.isFile && it.length() > 100_000_000L })
    }
    var installError by remember { mutableStateOf<String?>(null) }
    var copiedBytes by remember { mutableStateOf(0L) }
    var isInstalling by remember { mutableStateOf(false) }
    var retryInstall by remember { mutableStateOf(0) }
    var recenterRequest by remember { mutableStateOf(0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) onRefreshLocation()
        else Toast.makeText(context, "Allow location to show your position on the map.", Toast.LENGTH_LONG).show()
    }
    val ensureLocation = {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) onRefreshLocation()
        else locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LaunchedEffect(retryInstall) {
        if (offlineMapFile != null || isInstalling) return@LaunchedEffect
        isInstalling = true
        installError = null
        runCatching {
            withContext(Dispatchers.IO) {
                check(context.filesDir.usableSpace >= INDIA_MAP_SIZE_BYTES + 200_000_000L) {
                    "Free at least 1.9 GB of phone storage, then try again."
                }
                installBundledIndiaMap(context) { bytes ->
                    ContextCompat.getMainExecutor(context).execute { copiedBytes = bytes }
                }
            }
        }.onSuccess { offlineMapFile = it }
            .onFailure { installError = it.message ?: "Could not prepare the offline map." }
        isInstalling = false
    }

    Box(modifier = modifier.fillMaxSize().testTag("emergency_map_screen")) {
        if (offlineMapFile != null) {
            OfflineIndiaMapSurface(
                mapFile = offlineMapFile!!,
                latitude = currentCoordinates?.latitude,
                longitude = currentCoordinates?.longitude,
                accuracyM = currentCoordinates?.accuracyM,
                recenterRequest = recenterRequest,
                modifier = Modifier.fillMaxSize().testTag("offline_india_map")
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color(0xFF111820)))
            Card(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isInstalling) "Preparing offline India map" else "Offline map unavailable",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (isInstalling) {
                        CircularProgressIndicator(color = MeshConnectedGreen)
                        Text(
                            "${(copiedBytes / 1_000_000).coerceAtMost(INDIA_MAP_SIZE_BYTES / 1_000_000)} / ${INDIA_MAP_SIZE_BYTES / 1_000_000} MB",
                            color = Color(0xFFCFD8DC), fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (copiedBytes.toFloat() / INDIA_MAP_SIZE_BYTES).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            installError ?: "Free at least 1.9 GB of storage, then retry.",
                            color = Color(0xFFCFD8DC), fontSize = 12.sp
                        )
                        OutlinedButton(onClick = { retryInstall++ }) { Text("Try again") }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh.copy(alpha = 0.96f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentCoordinates == null) Icons.Filled.LocationOff else Icons.Filled.MyLocation,
                        contentDescription = null,
                        tint = if (currentCoordinates == null) MeshWarningOrange else MeshConnectedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (currentCoordinates == null) "Location unavailable" else "Your location",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    if (currentCoordinates != null) {
                        Text("±${currentCoordinates.accuracyM.toInt()} m", color = Color(0xFFCFD8DC), fontSize = 11.sp)
                    }
                }
                if (currentCoordinates != null) {
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%.5f, %.5f".format(Locale.US, currentCoordinates.latitude, currentCoordinates.longitude),
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(ClipboardManager::class.java)
                                clipboard?.setPrimaryClip(ClipData.newPlainText(
                                    "Coordinates",
                                    "${currentCoordinates.latitude}, ${currentCoordinates.longitude}"
                                ))
                                Toast.makeText(context, "Coordinates copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp).testTag("copy_gps_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy coordinates", tint = Color.White, modifier = Modifier.size(17.dp))
                        }
                        IconButton(
                            onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "My location: https://maps.google.com/?q=${currentCoordinates.latitude},${currentCoordinates.longitude}")
                                }
                                context.startActivity(Intent.createChooser(share, "Share location"))
                            },
                            modifier = Modifier.size(36.dp).testTag("share_gps_button")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share location", tint = Color.White, modifier = Modifier.size(17.dp))
                        }
                    }
                } else {
                    Text("Offline streets remain available. Turn on location to show your position.", color = Color(0xFFCFD8DC), fontSize = 11.sp)
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    val location = currentCoordinates
                    if (location == null) {
                        ensureLocation()
                        Toast.makeText(context, "Get a location fix to search nearby.", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:${location.latitude},${location.longitude}?q=hospitals+police+fire+stations")
                        )
                        context.startActivity(intent)
                    }
                },
                containerColor = Color(0xFF1E3A24),
                contentColor = MeshConnectedGreen,
                modifier = Modifier.size(48.dp).testTag("search_nearby_places_button")
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search nearby in Maps")
            }
            FloatingActionButton(
                onClick = {
                    if (currentCoordinates != null) {
                        recenterRequest++
                        onRefreshLocation()
                    } else ensureLocation()
                },
                containerColor = DarkSurfaceHigh,
                contentColor = Color(0xFF81D4FA),
                modifier = Modifier.size(48.dp).testTag("recenter_my_location_button")
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Center on my location")
            }
        }

        Text(
            text = "Offline streets · nearby search needs internet",
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                .background(Color(0xCC0F1720), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
