package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.DeviceCoordinates
import com.example.data.model.EmergencyCenter
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

@Composable
fun RescueBeaconBar(
    isSirenActive: Boolean,
    sirenError: String? = null,
    isTorchActive: Boolean,
    isScreenStrobeActive: Boolean,
    savedContactsCount: Int,
    nearestHospital: EmergencyCenter?,
    nearestPolice: EmergencyCenter?,
    currentCoordinates: DeviceCoordinates?,
    onToggleSiren: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleScreenStrobe: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rescue_beacon_bar"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Rescue tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSirenActive || isTorchActive) EmergencyRed else MeshConnectedGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rescue tools",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB0BEC5)
                    )
                }

            }

            if (sirenError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sirenError,
                    color = MeshWarningOrange,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag("siren_error_message")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Hardware Control Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tile 1: Acoustic Audio Siren
                BeaconTile(
                    title = if (isSirenActive) "Stop siren" else "Siren",
                    subtitle = if (isSirenActive) "Sound is playing" else "Audible alert",
                    icon = Icons.Filled.Campaign,
                    isActive = isSirenActive,
                    activeColor = EmergencyRed,
                    onClick = onToggleSiren,
                    testTag = "siren_toggle_button",
                    modifier = Modifier.weight(1f)
                )

                // Tile 2: visual SOS Morse pattern on the camera torch
                BeaconTile(
                    title = if (isTorchActive) "Stop torch SOS" else "Torch SOS",
                    subtitle = if (isTorchActive) "Flashing SOS pattern" else "Camera flash signal",
                    icon = Icons.Filled.FlashOn,
                    isActive = isTorchActive,
                    activeColor = MeshWarningOrange,
                    onClick = onToggleTorch,
                    testTag = "torch_strobe_toggle_button",
                    modifier = Modifier.weight(1f)
                )

                // Tile 3: Screen Strobe
                BeaconTile(
                    title = "Screen Strobe",
                    subtitle = if (isScreenStrobeActive) "Active" else "OLED Flash",
                    icon = Icons.Filled.LightMode,
                    isActive = isScreenStrobeActive,
                    activeColor = Color(0xFF2962FF),
                    onClick = onToggleScreenStrobe,
                    testTag = "screen_strobe_toggle_button",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Important Contacts Auto-SMS Broadcast Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToContacts() }
                    .testTag("sos_contacts_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2962FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF82B1FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (savedContactsCount > 0) {
                                    "$savedContactsCount Important Contacts Linked"
                                } else {
                                    "Link Emergency Contacts"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (savedContactsCount > 0) {
                                    "SMS alerts on SOS"
                            } else {
                                    "Add people to receive SOS texts"
                            },
                            fontSize = 11.sp,
                            color = if (savedContactsCount > 0) MeshConnectedGreen else Color(0xFF8E9299)
                        )
                    }

                    Text(
                        text = "Manage",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF82B1FF)
                    )
                }
            }

            // Nearest Emergency Facilities Quick Map Strip
            if (nearestHospital != null || nearestPolice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (nearestHospital != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToMap() }
                                .testTag("sos_nearest_hospital_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(EmergencyRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.LocalHospital,
                                        contentDescription = null,
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nearestHospital.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = nearestHospital.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude),
                                        fontSize = 10.sp,
                                        color = EmergencyRed
                                    )
                                }
                            }
                        }
                    }

                    if (nearestPolice != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2962FF).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToMap() }
                                .testTag("sos_nearest_police_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2962FF).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF2962FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nearestPolice.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = nearestPolice.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude),
                                        fontSize = 10.sp,
                                        color = Color(0xFF82B1FF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeaconTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tile_pulse"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else DarkSurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) activeColor else DarkSurfaceBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else Color(0xFF2C313C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) Color.White else Color(0xFF8E9299),
                    modifier = Modifier
                        .size(20.dp)
                        .scale(pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isActive) activeColor else Color(0xFF8E9299),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
