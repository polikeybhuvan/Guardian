package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.DeviceCoordinates
import com.example.data.model.AlertPriority
import com.example.data.model.EmergencyCategory
import com.example.data.model.TriageResult
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.PriorityP3
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Psychology
import com.example.domain.ai.OfflineGemmaTriageReport
import com.example.ui.theme.PriorityP4

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmergencyDetailsForm(
    selectedCategory: EmergencyCategory,
    peopleCount: Int,
    userNote: String,
    currentCoordinates: DeviceCoordinates?,
    triagePreview: TriageResult?,
    offlineGemmaReport: OfflineGemmaTriageReport? = null,
    onSelectCategory: (EmergencyCategory) -> Unit,
    onPeopleCountChange: (Int) -> Unit,
    onUserNoteChange: (String) -> Unit,
    onSubmitSos: () -> Unit,
    onOpenAiTriage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_details_form"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmergencyRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Incident Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Triage badge
                if (triagePreview != null) {
                    val badgeColor = when (triagePreview.priority) {
                        AlertPriority.P1 -> PriorityP1
                        AlertPriority.P2 -> PriorityP2
                        AlertPriority.P3 -> PriorityP3
                        AlertPriority.P4 -> PriorityP4
                    }
                    Surface(
                        color = Color(0xFF2E1717),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                    ) {
                        Text(
                            text = "${triagePreview.priority.code} • ${triagePreview.priority.label}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. INCIDENT TYPE SELECTION
            Text(
                text = "1. Select Incident Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFAAAAAA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incident_type_selector"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val availableCategories = listOf(
                    EmergencyCategory.FLOOD,
                    EmergencyCategory.MEDICAL,
                    EmergencyCategory.WOMEN_SAFETY,
                    EmergencyCategory.KIDNAPPING,
                    EmergencyCategory.THEFT,
                    EmergencyCategory.CYBER_FRAUD,
                    EmergencyCategory.TRAPPED,
                    EmergencyCategory.FIRE,
                    EmergencyCategory.LANDSLIDE,
                    EmergencyCategory.STRANDED,
                    EmergencyCategory.SUPPLIES,
                    EmergencyCategory.OTHER
                )

                availableCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) EmergencyRed else DarkSurfaceHigh,
                        label = "categoryChipBg"
                    )
                    val icon = getCategoryIcon(category)

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCategory(category) }
                            .testTag("incident_chip_${category.name.lowercase()}"),
                        color = chipBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFFF8A80) else DarkSurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFDDDDDD),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. NUMBER OF PEOPLE INVOLVED
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("people_count_selector"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "2. Number of People Involved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA)
                    )
                    Text(
                        text = "Victims requiring immediate aid",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }

                // Tactile Stepper + Presets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceHigh)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onPeopleCountChange(peopleCount - 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("people_count_minus")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease count", tint = Color.White)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$peopleCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onPeopleCountChange(peopleCount + 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("people_count_plus")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase count", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. SITUATION DETAILS / NOTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. Emergency Situation Note",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAAAAAA)
                )

                Text(
                    text = "${userNote.length}/140 chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (userNote.length > 120) PriorityP2 else Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = userNote,
                onValueChange = { if (it.length <= 140) onUserNoteChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_note_field"),
                placeholder = {
                    Text(
                        text = "e.g. 2 children with us, water rising to ceiling, need boat evacuation",
                        color = Color(0xFF6E7480),
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyRed,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E2E6),
                    focusedContainerColor = DarkSurfaceHigh,
                    unfocusedContainerColor = DarkSurfaceHigh
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Real-time Local Gemma AI Triage Insight
            if (offlineGemmaReport != null) {
                Spacer(modifier = Modifier.height(12.dp))
                com.example.ui.components.OfflineGemmaTriageCard(
                    report = offlineGemmaReport,
                    isCompact = true,
                    onApplyToSos = null
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. ATTACHED GPS LOCATION PREVIEW
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF13171C),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (currentCoordinates != null) MeshConnectedGreen else Color(0xFFFFB74D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Attached Coordinates",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFAAAAAA)
                            )
                            Text(
                                text = if (currentCoordinates != null)
                                    "${"%.4f".format(currentCoordinates.latitude)}, ${"%.4f".format(currentCoordinates.longitude)} (±${currentCoordinates.accuracyM.toInt()}m)"
                                else
                                    "GPS Acquiring / Satellite Fix Pending",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentCoordinates != null) Color.White else Color(0xFFFFB74D),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (currentCoordinates != null) Color(0xFF1B3D23) else Color(0xFF332514)
                    ) {
                        Text(
                            text = if (currentCoordinates != null) "GPS Locked" else "Pending",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentCoordinates != null) MeshConnectedGreen else Color(0xFFFFB74D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. SUBMIT / BROADCAST BUTTON
            Button(
                onClick = onSubmitSos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_emergency_details_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmergencyRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CrisisAlert, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trigger SOS with Details (${selectedCategory.displayName}, $peopleCount)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun getCategoryIcon(category: EmergencyCategory): ImageVector {
    return when (category) {
        EmergencyCategory.FLOOD -> Icons.Default.Waves
        EmergencyCategory.MEDICAL -> Icons.Default.MedicalServices
        EmergencyCategory.WOMEN_SAFETY -> Icons.Default.Security
        EmergencyCategory.KIDNAPPING -> Icons.Default.Warning
        EmergencyCategory.THEFT -> Icons.Default.Lock
        EmergencyCategory.CYBER_FRAUD -> Icons.AutoMirrored.Filled.HelpOutline
        EmergencyCategory.TRAPPED -> Icons.Default.Warning
        EmergencyCategory.FIRE -> Icons.Default.LocalFireDepartment
        EmergencyCategory.LANDSLIDE -> Icons.Default.Terrain
        EmergencyCategory.STRANDED -> Icons.Default.Navigation
        EmergencyCategory.SUPPLIES -> Icons.Default.Restaurant
        EmergencyCategory.OTHER -> Icons.AutoMirrored.Filled.HelpOutline
        EmergencyCategory.SAFE -> Icons.Default.Security
    }
}
