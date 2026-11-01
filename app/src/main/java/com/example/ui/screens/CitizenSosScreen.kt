package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.DeviceCoordinates
import com.example.data.location.SmsDispatchResult
import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCenter
import com.example.data.model.EmergencyCategory
import com.example.data.model.MeshMessage
import com.example.data.model.TriageResult
import com.example.ui.components.DeliveryJourneyCard
import com.example.ui.components.EmergencyDetailsForm
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.EmergencyRedLight
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.PriorityP3
import com.example.ui.theme.PriorityP4
import com.example.domain.ai.OfflineGemmaTriageReport
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun CitizenSosScreen(
    selectedCategory: EmergencyCategory,
    peopleCount: Int,
    userNote: String,
    triagePreview: TriageResult?,
    isTriageLoading: Boolean,
    isCountdownActive: Boolean,
    countdownSeconds: Int,
    latestAlert: MeshMessage?,
    currentCoordinates: DeviceCoordinates? = null,
    connectedPeers: Int = 0,
    isSirenActive: Boolean = false,
    sirenError: String? = null,
    isTorchActive: Boolean = false,
    savedContactsCount: Int = 0,
    emergencyContactPhoneNumbers: List<String> = emptyList(),
    lastSmsResult: SmsDispatchResult? = null,
    nearestHospital: EmergencyCenter? = null,
    nearestPolice: EmergencyCenter? = null,
    offlineGemmaReport: OfflineGemmaTriageReport? = null,
    onSelectCategory: (EmergencyCategory) -> Unit,
    onPeopleCountChange: (Int) -> Unit,
    onUserNoteChange: (String) -> Unit,
    onTriggerSos: () -> Unit,
    onCancelSos: () -> Unit,
    onUpdateLocation: () -> Unit = {},
    onToggleSiren: () -> Unit = {},
    onToggleTorch: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onOpenAiTriage: (() -> Unit)? = null,
    isMorseBeaconActive: Boolean = false,
    onToggleMorseBeacon: () -> Unit = {},
    onOpenFloodlight: () -> Unit = {},
    onOpenCompassHud: () -> Unit = {},
    onNavigateToFamily: () -> Unit = {},
    onNavigateToSupplies: () -> Unit = {},
    onNavigateToMedicalProfile: () -> Unit = {},
    isBackgroundRelayActive: Boolean = false,
    relayStatusError: String? = null,
    onToggleBackgroundRelay: () -> Unit = {},
    isWifiBeaconActive: Boolean = false,
    wifiBeaconSsid: String? = null,
    onToggleWifiBeacon: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onToggleTorch()
        else Toast.makeText(context, "Camera permission is needed to use the torch SOS signal.", Toast.LENGTH_LONG).show()
    }
    val handleTorchToggle = {
        if (isTorchActive) {
            onToggleTorch()
        } else if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            onToggleTorch()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Accompanist Location Permissions State (Fine + Coarse Location for SOS Coordinates)
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val sosPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // SOS remains available if location or other optional permissions are denied.
        if (results.values.any { !it }) {
            Toast.makeText(context, "Some SOS actions may be unavailable because permissions were denied.", Toast.LENGTH_LONG).show()
        }
        onTriggerSos()
    }
    val nearbyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onToggleBackgroundRelay()
        } else {
            Toast.makeText(context, "Nearby relay needs Location and Bluetooth/Nearby permissions.", Toast.LENGTH_LONG).show()
        }
    }
    val handleNearbyRelayToggle = {
        if (isBackgroundRelayActive) {
            onToggleBackgroundRelay()
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions += listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions += Manifest.permission.NEARBY_WIFI_DEVICES
            }
            val missing = permissions.filter { permission ->
                androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (missing.isEmpty()) onToggleBackgroundRelay()
            else nearbyPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    // Automatically fetch real coordinates when permissions become granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onUpdateLocation()
        }
    }

    // Pulsing animation for the main SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulseAlpha"
    )

    val handleSosTriggerWithPermissions = {
        val requiredForAutomaticActions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        val missingPermissions = requiredForAutomaticActions.filter { permission ->
            androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            onTriggerSos()
        } else {
            sosPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (savedContactsCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToContacts() }.testTag("setup_emergency_contacts_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2114)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeshWarningOrange.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MeshWarningOrange)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Add an emergency contact", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("SOS texts can only go to contacts you save here.", color = Color(0xFFD4C8B4), fontSize = 12.sp)
                        }
                        Text("Set up", color = Color(0xFFFFCC80), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        lastSmsResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("sos_sms_result_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (result.isSuccess) Color(0xFF153326) else Color(0xFF3A2020)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (result.isSuccess) MeshConnectedGreen else EmergencyRed)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(
                            text = if (result.isSuccess) "SMS submitted to Android for ${result.sentCount} contact(s). Delivery is not confirmed."
                            else result.error ?: "SMS could not be submitted.",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (!result.isSuccess && result.error.orEmpty().contains("SMS permission", ignoreCase = true)) {
                            Text(
                                text = "If Android won’t allow SMS access for this install, open Messages and tap Send.",
                                color = Color(0xFFD4C8B4),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        val numbers = emergencyContactPhoneNumbers
                                            .map { it.trim() }
                                            .map { it.replace("[^0-9+]".toRegex(), "") }
                                            .filter { it.isNotEmpty() }
                                            .joinToString(";")
                                        val smsIntent = Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse(if (numbers.isEmpty()) "smsto:" else "smsto:$numbers")
                                        ).putExtra("sms_body", result.messageText)
                                        runCatching {
                                            context.startActivity(Intent.createChooser(smsIntent, "Send Guardian SOS"))
                                        }.onFailure {
                                            Toast.makeText(context, "No messaging app is available to send this SOS.", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("open_sms_composer_button")
                                ) {
                                    Text("Open Messages")
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 0. OPEN PERIMETER WI-FI EMERGENCY BEACON (IF ACTIVE)
        // =====================================================================
        if (isWifiBeaconActive && wifiBeaconSsid != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("wifi_emergency_beacon_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1616)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmergencyRed)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmergencyRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Beacon active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$wifiBeaconSsid · Nearby phones will not receive a pop-up.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFCDD2),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onToggleWifiBeacon,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C2D2A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stop_wifi_beacon_button")
                        ) {
                            Text("Stop", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =====================================================================
        // Nearby delivery works only while relay is enabled and Guardian peers connect.
        // =====================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mesh_connectivity_status"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurfaceContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing radar antenna icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2415))
                                    .border(1.dp, MeshWarningOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CellTower,
                                    contentDescription = "Mesh Antenna",
                                    tint = MeshWarningOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (connectedPeers > 0) MeshConnectedGreen else MeshWarningOrange)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                    text = when {
                                        connectedPeers > 0 -> "Guardian peer connected"
                                        relayStatusError != null -> "Nearby relay not available"
                                        else -> "No Guardian peer connected"
                                    },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (connectedPeers > 0) MeshConnectedGreen else MeshWarningOrange
                                    )
                                }
                                Text(
                                    text = when {
                                        connectedPeers > 0 -> "$connectedPeers connected Guardian phone(s)"
                                        relayStatusError != null -> relayStatusError
                                        isBackgroundRelayActive -> "Searching for Guardian phones"
                                        else -> "Saved here • turn on relay to search nearby"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Store and forward telemetry badge row with GPS Status and View Map action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF13161A))
                            .clickable { onNavigateToMap() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (locationPermissionsState.allPermissionsGranted)
                                    Icons.Default.GpsFixed
                                else
                                    Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = if (locationPermissionsState.allPermissionsGranted && currentCoordinates != null)
                                    MeshConnectedGreen
                                else
                                    MeshWarningOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (locationPermissionsState.allPermissionsGranted) {
                                    val coords = currentCoordinates
                                    if (coords != null) "GPS: %.4f, %.4f".format(coords.latitude, coords.longitude)
                                    else "GPS: Acquiring location"
                                } else {
                                    "GPS: Permission Required"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (locationPermissionsState.allPermissionsGranted && currentCoordinates != null)
                                    MeshConnectedGreen
                                else
                                    MeshWarningOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "Map",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF81D4FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 2. ACCOMPANIST LOCATION PERMISSION RATIONALE CARD (IF NOT GRANTED)
        // =====================================================================
        if (!locationPermissionsState.allPermissionsGranted) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_permission_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26190B)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PriorityP2)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3E2812)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Location Service",
                                tint = PriorityP2,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share your location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Add GPS coordinates to your SOS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PriorityP2,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("grant_location_permission_button")
                        ) {
                            Text(
                                text = "Grant",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. PROMINENT 'TRIGGER EMERGENCY SOS' SECTION
        // =====================================================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1212)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF5C1B1B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Outer Pulsing Glow Aura
                    Box(
                        modifier = Modifier
                            .size(152.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        EmergencyRed.copy(alpha = ringPulseAlpha),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Central Tactile Emergency Disk
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(EmergencyRedLight, EmergencyRedDark)
                                    )
                                )
                                .border(3.dp, Color(0xFFFFCDD2), CircleShape)
                                .clickable { handleSosTriggerWithPermissions() }
                                .testTag("giant_sos_button")
                                .testTag("trigger_emergency_sos_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CrisisAlert,
                                    contentDescription = "Trigger Emergency SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "SOS",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "Only Guardian users can receive app alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC7A8A8),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick access to the national emergency number.
        item {
            Button(
                onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("call_112_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331616), contentColor = Color(0xFFFF8A80)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call 112", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("official_help_links_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("More ways to get help", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("For immediate danger, call 112.", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                    Text(
                        "SOS texts go only to your saved contacts. Guardian does not alert or dispatch police, hospitals, or nearby phones.",
                        color = Color(0xFFCFD8DC), fontSize = 12.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:181")))
                            }.testTag("women_helpline_181"),
                            color = Color(0xFF242D3B), shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Women’s helpline", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("181 · Call", color = Color(0xFFB8D5FF), fontSize = 12.sp)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")))
                            }.testTag("cyber_fraud_helpline_1930"),
                            color = Color(0xFF242D3B), shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Financial cyber fraud", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("1930 · Call", color = Color(0xFFB8D5FF), fontSize = 12.sp)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://112.gov.in/112-india-app")))
                        }.testTag("official_112_app_link"),
                        color = Color(0xFF1B2835), shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "112 India app · SHOUT for women and children  ↗",
                            modifier = Modifier.padding(11.dp), color = Color(0xFFB8D5FF), fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in/")))
                        },
                        color = Color(0xFF1B2835), shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "National Cyber Crime Reporting Portal  ↗",
                            modifier = Modifier.padding(11.dp), color = Color(0xFFB8D5FF), fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 5. Rescue tools are grouped in one place below.
        // =====================================================================
        // =====================================================================
        // 5B. ADVANCED SEARCH & RESCUE TACTICAL BEACONS & DETECTORS
        // =====================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sar_tactical_tools_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23354A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF132A3E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = Color(0xFF81D4FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rescue tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Section 2: Medical Profile & Tactical Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RescueToolTile("Siren", Icons.Filled.Campaign, isSirenActive, onToggleSiren, Modifier.weight(1f).testTag("siren_button"))
                        RescueToolTile("Torch SOS", Icons.Filled.FlashlightOn, isTorchActive, handleTorchToggle, Modifier.weight(1f).testTag("torch_sos_button"))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RescueToolTile("Link Emergency Contacts", Icons.Filled.Security, false, onNavigateToContacts, Modifier.weight(1f).testTag("link_emergency_contacts_button"))
                        RescueToolTile(if (isBackgroundRelayActive) "Nearby relay on" else "Nearby relay", Icons.Filled.Hub, isBackgroundRelayActive, handleNearbyRelayToggle, Modifier.weight(1f).testTag("toggle_background_relay_button"))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RescueToolTile("Medical ID Card", Icons.Filled.Security, false, onNavigateToMedicalProfile, Modifier.weight(1f).testTag("medical_profile_button"))
                        RescueToolTile("Compass HUD", Icons.Filled.Explore, false, onOpenCompassHud, Modifier.weight(1f).testTag("open_compass_hud_button"))
                    }

                    // Section 3: Acoustic SOS signaling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RescueToolTile(if (isMorseBeaconActive) "Stop Morse" else "Morse Audio", Icons.Filled.GraphicEq, isMorseBeaconActive, onToggleMorseBeacon, Modifier.weight(1f).testTag("morse_sos_sound_button"))
                        RescueToolTile("Floodlight", Icons.Filled.FlashlightOn, false, onOpenFloodlight, Modifier.weight(1f).testTag("floodlight_button"))

                    }

                }
            }
        }

        // =====================================================================
        // 6. EMERGENCY DETAILS COMPOSABLE FORM (Incident Type, People Count, Notes, Attached GPS)
        // =====================================================================
        item {
            EmergencyDetailsForm(
                selectedCategory = selectedCategory,
                peopleCount = peopleCount,
                userNote = userNote,
                currentCoordinates = currentCoordinates,
                triagePreview = triagePreview,
                offlineGemmaReport = offlineGemmaReport,
                onSelectCategory = onSelectCategory,
                onPeopleCountChange = onPeopleCountChange,
                onUserNoteChange = onUserNoteChange,
                onSubmitSos = handleSosTriggerWithPermissions,
                onOpenAiTriage = onOpenAiTriage
            )
        }

        // =====================================================================
        // 7. ACTIVE ALERT DELIVERY LIFELINE TRACKER
        // =====================================================================
        item {
            val step = when {
                latestAlert?.isAcknowledged == true -> com.example.data.model.DeliveryStep.RESPONDER_ACKNOWLEDGED
                latestAlert?.isGatewayUploaded == true -> com.example.data.model.DeliveryStep.GATEWAY_UPLOADED
                (latestAlert?.hopCount ?: 0) > 0 -> com.example.data.model.DeliveryStep.PEER_RELAYED
                latestAlert != null -> com.example.data.model.DeliveryStep.LOCAL_SAVED
                else -> com.example.data.model.DeliveryStep.LOCAL_SAVED
            }
            DeliveryJourneyCard(activeAlert = latestAlert, currentStep = step)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 5-Second Cancellation Countdown Overlay Dialog
    if (isCountdownActive) {
        AlertDialog(
            onDismissRequest = { /* Force explicit tap on Cancel button */ },
            containerColor = Color(0xFF1E1111),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PriorityP1,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Broadcasting SOS in $countdownSeconds seconds",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your SOS is saved on this phone. If SMS permission and mobile service are available, Guardian submits a text to your saved contacts. Delivery is not confirmed. Call 112 for immediate help.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCCCCCC),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap below to cancel if triggered by mistake",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFCDD2),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onCancelSos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("cancel_countdown_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmergencyRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cancel Distress Signal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

}

@Composable
private fun RescueToolTile(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (active) MeshConnectedGreen else Color(0xFF9FCBFF)
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (active) Color(0xFF14301B) else Color(0xFF17212E),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) MeshConnectedGreen.copy(alpha = 0.8f) else Color(0xFF2A3A50)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}
