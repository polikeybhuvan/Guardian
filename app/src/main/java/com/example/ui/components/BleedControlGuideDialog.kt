package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BleedControlGuideDialog(
    onDismiss: () -> Unit
) {
    var markedTime by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bleed_control_guide_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23354A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF331616)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Arterial Bleed Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Emergency Stop-the-Bleed Protocol",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_bleed_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFB0BEC5))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Steps List
                val steps = listOf(
                    "1. Direct Pressure" to "Press hard directly on wound with sterile cloth, shirt, or gauze using full body weight.",
                    "2. Apply Tourniquet" to "Place 2-3 inches above the wound on arm or leg (never directly over a joint like knee/elbow).",
                    "3. Twist Windlass" to "Tighten windlass rod until bright red spurting bleeding completely stops. Lock rod in clip.",
                    "4. Record Time" to "Mark exact application time on forehead or tourniquet strap. Never loosen a placed tourniquet."
                )

                steps.forEachIndexed { index, (title, instruction) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F141C),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF26190B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = instruction,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Stamp Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tourniquet Time Stamp",
                                fontSize = 11.sp,
                                color = Color(0xFF90A4AE)
                            )
                            Text(
                                text = markedTime ?: "Not stamped yet",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (markedTime != null) Color(0xFFFF8A80) else Color(0xFF78909C)
                            )
                        }

                        Button(
                            onClick = {
                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                                markedTime = sdf.format(Date())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF331616),
                                contentColor = Color(0xFFFF8A80)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("stamp_tourniquet_time_button")
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Stamp Time", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It • Keep Firm Pressure", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
