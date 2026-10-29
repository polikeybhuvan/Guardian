package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.mesh.MeshLogEvent
import com.example.domain.mesh.MeshPeerNode
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeshDiagnosticsScreen(
    isBluetoothEnabled: Boolean,
    isWifiEnabled: Boolean,
    isInternetAvailable: Boolean,
    deviceId: String,
    totalStoredAlerts: Int,
    connectedPeers: Int,
    peersList: List<MeshPeerNode>,
    logs: List<MeshLogEvent>,
    onRefreshState: () -> Unit,
    cachedSignalsCount: Int = 0,
    cachedNodesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Header info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NETWORK & RADIO DIAGNOSTICS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time device hardware & mesh telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA)
                    )
                }

                Surface(
                    color = DarkSurfaceHigh,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Text(
                        text = "ID: $deviceId",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF82B1FF),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hardware Radio Status Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "RADIO INTERFACES & BACKHAUL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RadioStatusRow(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth Radio (BLE)",
                        subtitle = "Required for nearby peer discovery & direct handshake",
                        isEnabled = isBluetoothEnabled,
                        enabledLabel = "Active & Enabled",
                        disabledLabel = "Disabled / Off"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RadioStatusRow(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi Aware & Direct",
                        subtitle = "High-throughput store-and-forward mesh bridge",
                        isEnabled = isWifiEnabled,
                        enabledLabel = "Enabled",
                        disabledLabel = "Disabled / Off"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RadioStatusRow(
                        icon = Icons.Default.CellTower,
                        title = "Cellular / Gateway Internet",
                        subtitle = "Bridges offline mesh alerts to disaster operations cloud",
                        isEnabled = isInternetAvailable,
                        enabledLabel = "Online (Gateway Active)",
                        disabledLabel = "Offline (Mesh Only)"
                    )
                }
            }
        }

        // Security & Cryptographic Node Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "DEVICE CRYPTO & DATABASE QUEUE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("KeyStore ECDSA", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                                Text("P-256 Hardware", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = MeshConnectedGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("SQLite Messages", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                                Text("$totalStoredAlerts Records", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Room Offline Resilience & Cache Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("room_offline_cache_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOM OFFLINE CACHE STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF14301B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MeshConnectedGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MeshConnectedGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ZERO DATA LOSS READY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeshConnectedGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Cached Distress SOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFAAAAAA)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$cachedSignalsCount Signals",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = EmergencyRed
                                )
                                Text(
                                    text = "Persisted for blackout replay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Cached Mesh Nodes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFAAAAAA)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$cachedNodesCount Nodes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF82B1FF)
                                )
                                Text(
                                    text = "GPS, battery & hop states",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Database: sos_mesh.db (v3) • Alerts are stored on this device and relayed only when Guardian peers connect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Real Transmission Logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1012)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RADIO & EVENT LOG",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA)
                        )

                        Text(
                            text = "${logs.size} Events",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF757575)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (logs.isEmpty()) {
                        Text(
                            text = "No events yet. Local actions and unavailable transmission attempts are logged here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF616161),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    } else {
                        logs.take(12).forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[${timeFormat.format(Date(event.timestamp))}] ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF888888),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = event.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (event.eventType.contains("GATEWAY", ignoreCase = true))
                                        MeshConnectedGreen
                                    else if (event.eventType.contains("BROADCAST", ignoreCase = true))
                                        Color(0xFF82B1FF)
                                    else
                                        Color(0xFFE0E0E0),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Button: Refresh Hardware Radio
        item {
            Button(
                onClick = onRefreshState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("refresh_diagnostics_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceHigh,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Radio & Network Status", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RadioStatusRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    enabledLabel: String,
    disabledLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) Color(0xFF1B3D23) else Color(0xFF2B2B2B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) MeshConnectedGreen else Color(0xFF888888),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA), fontSize = 11.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isEnabled) Color(0xFF14301B) else Color(0xFF262626),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) MeshConnectedGreen else Color(0xFF444444))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isEnabled) MeshConnectedGreen else Color(0xFF888888),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEnabled) enabledLabel else disabledLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) MeshConnectedGreen else Color(0xFFAAAAAA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
