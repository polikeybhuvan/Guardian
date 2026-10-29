package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.location.DeviceCoordinates
import com.example.data.model.EmergencyCenter
import com.example.data.model.EmergencyCenterType
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen

/**
 * Full-screen Heads-Up Tactical Compass HUD.
 * Renders rotating azimuth dial and target bearing pointer needle directed toward
 * the nearest rescue facility (hospital or police) with real-time distance countdown.
 */
@Composable
fun TacticalCompassHudDialog(
    targetFacility: EmergencyCenter?,
    currentCoordinates: DeviceCoordinates?,
    azimuthDegrees: Float,
    isCompassAvailable: Boolean,
    onDismiss: () -> Unit
) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuthDegrees,
        animationSpec = tween(durationMillis = 150),
        label = "azimuth_smooth"
    )

    val targetBearing = if (targetFacility != null && currentCoordinates != null) {
        targetFacility.bearingFrom(currentCoordinates.latitude, currentCoordinates.longitude).toFloat()
    } else 0f

    // Needle relative angle = targetBearing - current phone heading
    val relativePointerAngle = (targetBearing - animatedAzimuth + 360f) % 360f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D14))
                .padding(20.dp)
                .testTag("tactical_compass_hud")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmergencyRed.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmergencyRed)
                        ) {
                            Text(
                                text = "TACTICAL HUD • BEARING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = EmergencyRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_compass_hud_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close HUD", tint = Color.White)
                    }
                }

                // Center Compass Dial & Target Needle
                Box(
                    modifier = Modifier
                        .size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Compass Graduations Dial (Rotates with device heading)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-animatedAzimuth)
                    ) {
                        val radius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Outer tactical ring
                        drawCircle(
                            color = Color(0xFF1E3A5A),
                            radius = radius - 4.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF0F1E30),
                            radius = radius - 20.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Cardinal tick marks
                        for (i in 0 until 360 step 30) {
                            val angleRad = Math.toRadians((i - 90).toDouble())
                            val tickLen = if (i % 90 == 0) 16.dp.toPx() else 8.dp.toPx()
                            val tickColor = if (i == 0) Color(0xFFFF1744) else Color(0xFF81D4FA)

                            val startX = (center.x + (radius - 6.dp.toPx()) * kotlin.math.cos(angleRad)).toFloat()
                            val startY = (center.y + (radius - 6.dp.toPx()) * kotlin.math.sin(angleRad)).toFloat()
                            val endX = (center.x + (radius - 6.dp.toPx() - tickLen) * kotlin.math.cos(angleRad)).toFloat()
                            val endY = (center.y + (radius - 6.dp.toPx() - tickLen) * kotlin.math.sin(angleRad)).toFloat()

                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                            )
                        }
                    }

                    // Dynamic Facility Directional Arrow (Points straight at hospital / police)
                    Canvas(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(relativePointerAngle)
                    ) {
                        val cx = size.width / 2
                        val cy = size.height / 2

                        val path = Path().apply {
                            moveTo(cx, 10f) // Sharp tip pointing forward
                            lineTo(cx + 24f, cy + 20f)
                            lineTo(cx + 8f, cy + 10f)
                            lineTo(cx + 8f, cy + 60f)
                            lineTo(cx - 8f, cy + 60f)
                            lineTo(cx - 8f, cy + 10f)
                            lineTo(cx - 24f, cy + 20f)
                            close()
                        }

                        drawPath(
                            path = path,
                            color = if (targetFacility?.type == EmergencyCenterType.HOSPITAL) EmergencyRed else Color(0xFF2979FF)
                        )
                    }

                    // Center Azimuth Digital readout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = "${animatedAzimuth.toInt()}°",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = getHeadingCardinal(animatedAzimuth),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81D4FA)
                        )
                    }
                }

                // Bottom Target Facility Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2A4260))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (targetFacility?.type == EmergencyCenterType.HOSPITAL) Icons.Filled.LocalHospital else Icons.Filled.LocalPolice,
                                    contentDescription = null,
                                    tint = if (targetFacility?.type == EmergencyCenterType.HOSPITAL) EmergencyRed else Color(0xFF81D4FA),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = targetFacility?.name ?: "Searching Nearest Facility",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            val dist = targetFacility?.formattedDistance(
                                currentCoordinates?.latitude,
                                currentCoordinates?.longitude
                            ) ?: "--"

                            Text(
                                text = dist,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MeshConnectedGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BEARING: ${targetBearing.toInt()}° ${getHeadingCardinal(targetBearing)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF81D4FA)
                            )
                            Text(
                                text = "STATUS: ${targetFacility?.statusBadge ?: "ONLINE"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getHeadingCardinal(deg: Float): String {
    val normalized = (deg % 360 + 360) % 360
    return when {
        normalized >= 337.5 || normalized < 22.5 -> "N"
        normalized < 67.5 -> "NE"
        normalized < 112.5 -> "E"
        normalized < 157.5 -> "SE"
        normalized < 202.5 -> "S"
        normalized < 247.5 -> "SW"
        normalized < 292.5 -> "W"
        else -> "NW"
    }
}
