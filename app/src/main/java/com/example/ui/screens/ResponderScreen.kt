package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertPriority
import com.example.data.model.MeshMessage
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.PriorityP3
import com.example.ui.theme.PriorityP4
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResponderScreen(
    incidents: List<MeshMessage>,
    selectedFilter: AlertPriority?,
    onSelectFilter: (AlertPriority?) -> Unit,
    onAcknowledgeAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = if (selectedFilter == null) incidents else incidents.filter { it.priority == selectedFilter }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RESPONDER FIELD RADAR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "NSS / NCC / SDRF Volunteer Command",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA)
                )
            }

            Surface(
                color = if (incidents.isNotEmpty()) Color(0xFF3F1414) else DarkSurfaceHigh,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (incidents.isNotEmpty()) EmergencyRed else DarkSurfaceBorder)
            ) {
                Text(
                    text = "${incidents.size} Heard Alerts",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (incidents.isNotEmpty()) Color(0xFFFF8A80) else Color(0xFFCCCCCC)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Priority Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "All",
                isSelected = selectedFilter == null,
                onClick = { onSelectFilter(null) }
            )
            FilterChipItem(
                label = "P1",
                color = PriorityP1,
                isSelected = selectedFilter == AlertPriority.P1,
                onClick = { onSelectFilter(AlertPriority.P1) }
            )
            FilterChipItem(
                label = "P2",
                color = PriorityP2,
                isSelected = selectedFilter == AlertPriority.P2,
                onClick = { onSelectFilter(AlertPriority.P2) }
            )
            FilterChipItem(
                label = "P3",
                color = PriorityP3,
                isSelected = selectedFilter == AlertPriority.P3,
                onClick = { onSelectFilter(AlertPriority.P3) }
            )
            FilterChipItem(
                label = "P4 Safe",
                color = PriorityP4,
                isSelected = selectedFilter == AlertPriority.P4,
                onClick = { onSelectFilter(AlertPriority.P4) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Incident List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = Color(0xFF616161),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No distress signals in range",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFAAAAAA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This screen shows alerts received by Guardian. Phones without the app cannot receive or forward these messages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.msgId }) { alert ->
                    IncidentCard(
                        alert = alert,
                        formattedTime = timeFormat.format(Date(alert.createdAt)),
                        onAcknowledge = { onAcknowledgeAlert(alert.msgId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    color: Color = Color.White,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) DarkSurfaceHigh else Color(0xFF16191E),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else DarkSurfaceBorder
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun IncidentCard(
    alert: MeshMessage,
    formattedTime: String,
    onAcknowledge: () -> Unit
) {
    val prioColor = when (alert.priority) {
        AlertPriority.P1 -> PriorityP1
        AlertPriority.P2 -> PriorityP2
        AlertPriority.P3 -> PriorityP3
        AlertPriority.P4 -> PriorityP4
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incident_card_${alert.msgId.take(6)}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Priority Pill, Category, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(prioColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = alert.priority.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Note / Triage text
            Text(
                text = alert.note.ifBlank { "Distress alert received from mesh peer." },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE0E0E0),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips: People Count, Coordinates, Hops
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${alert.peopleCount} pers",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val locText = if (alert.latitude != null && alert.longitude != null)
                        "${"%.3f".format(alert.latitude)}, ${"%.3f".format(alert.longitude)}"
                    else
                        "GPS Pending"
                    Text(
                        text = locText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = MeshConnectedGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${alert.hopCount} hops",
                        style = MaterialTheme.typography.bodySmall,
                        color = MeshConnectedGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Responder Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (alert.isAcknowledged) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MeshConnectedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = alert.ackByResponder ?: "Acknowledged",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeshConnectedGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onAcknowledge,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("ack_button_${alert.msgId.take(6)}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A24),
                            contentColor = MeshConnectedGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acknowledge Rescue",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
