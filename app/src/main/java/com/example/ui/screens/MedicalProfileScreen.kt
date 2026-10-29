package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.MedicalProfileEntity
import com.example.domain.medical.OfflineQrGenerator
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen

@Composable
fun MedicalProfileScreen(
    profile: MedicalProfileEntity?,
    onSaveProfile: (MedicalProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    val qrBitmap: Bitmap? = remember(profile) {
        if (profile != null) {
            val payload = OfflineQrGenerator.createMedicalPayload(profile)
            OfflineQrGenerator.generateEmergencyQrBitmap(payload, 480)
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("medical_profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header & Edit Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Emergency Medical ID",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Accessible offline to first responders via lockscreen QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color(0xFF81D4FA)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp).testTag("edit_medical_profile_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (profile == null) "Setup" else "Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (profile == null) {
                // Empty state setup card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("medical_profile_empty_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                        border = BorderStroke(1.dp, Color(0xFF23354A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Medical Profile Configured",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add your blood type, chronic allergies, and emergency contact so emergency doctors can treat you without delay.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0BEC5),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showEditDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("setup_medical_profile_cta")
                            ) {
                                Text("Set Up Medical ID Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Emergency ID Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("medical_profile_id_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2B)),
                        border = BorderStroke(1.5.dp, Color(0xFF23354A))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Top Row: Name, Blood Badge & Organ Donor
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.fullName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (profile.age > 0) {
                                        Text(
                                            text = "${profile.age} years old",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF90A4AE)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF331616),
                                    border = BorderStroke(1.5.dp, EmergencyRed)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "BLOOD: ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF8A80)
                                        )
                                        Text(
                                            text = profile.bloodGroup,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Critical Medical Data Grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MedicalInfoRow(
                                    label = "Severe Allergies",
                                    value = profile.severeAllergies.ifBlank { "None reported" },
                                    isWarning = profile.severeAllergies.isNotBlank()
                                )
                                MedicalInfoRow(
                                    label = "Chronic Conditions",
                                    value = profile.chronicConditions.ifBlank { "None reported" }
                                )
                                MedicalInfoRow(
                                    label = "Current Medications",
                                    value = profile.regularMedications.ifBlank { "None reported" }
                                )
                                if (profile.isOrganDonor) {
                                    MedicalInfoRow(
                                        label = "Organ Donor",
                                        value = "Yes (Registered Donor)"
                                    )
                                }
                                if (profile.medicalNotes.isNotBlank()) {
                                    MedicalInfoRow(
                                        label = "Special Notes",
                                        value = profile.medicalNotes
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // In Case of Emergency (ICE) Contact Banner
                            if (profile.emergencyContactPhone.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F1622),
                                    border = BorderStroke(1.dp, Color(0xFF1E2D44)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "IN CASE OF EMERGENCY (ICE)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF81D4FA)
                                            )
                                            Text(
                                                text = "${profile.emergencyContactName} (${profile.emergencyContactRelation})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = profile.emergencyContactPhone,
                                                fontSize = 11.sp,
                                                color = Color(0xFFB0BEC5)
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${profile.emergencyContactPhone}")
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MeshConnectedGreen, contentColor = Color.Black),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(36.dp).testTag("call_ice_contact_button")
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call ICE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Offline QR Code Presentation Card
                if (qrBitmap != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("medical_qr_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                            border = BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Offline First-Responder QR",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1B3D23)
                                    ) {
                                        Text(
                                            text = "ZERO INTERNET NEEDED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MeshConnectedGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // QR Code Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "Medical ID QR Code",
                                        modifier = Modifier.size(200.dp).padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Any phone camera or hospital tablet can scan this directly from your screen or printed lockscreen card.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF90A4AE),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Medical Profile Edit Dialog
    if (showEditDialog) {
        var fullName by remember { mutableStateOf(profile?.fullName ?: "") }
        var bloodGroup by remember { mutableStateOf(profile?.bloodGroup ?: "O+") }
        var ageStr by remember { mutableStateOf(if ((profile?.age ?: 0) > 0) profile?.age.toString() else "") }
        var severeAllergies by remember { mutableStateOf(profile?.severeAllergies ?: "") }
        var chronicConditions by remember { mutableStateOf(profile?.chronicConditions ?: "") }
        var regularMedications by remember { mutableStateOf(profile?.regularMedications ?: "") }
        var contactName by remember { mutableStateOf(profile?.emergencyContactName ?: "") }
        var contactPhone by remember { mutableStateOf(profile?.emergencyContactPhone ?: "") }
        var contactRelation by remember { mutableStateOf(profile?.emergencyContactRelation ?: "Spouse / Parent") }
        var isDonor by remember { mutableStateOf(profile?.isOrganDonor ?: false) }
        var notes by remember { mutableStateOf(profile?.medicalNotes ?: "") }

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("medical_profile_edit_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A24)),
                border = BorderStroke(1.dp, Color(0xFF23354A))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Edit Medical ID Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it },
                                label = { Text("Blood Group (e.g. O+, B-)") },
                                modifier = Modifier.weight(1f).testTag("input_medical_blood"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            OutlinedTextField(
                                value = ageStr,
                                onValueChange = { ageStr = it },
                                label = { Text("Age") },
                                modifier = Modifier.weight(0.6f).testTag("input_medical_age"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = severeAllergies,
                            onValueChange = { severeAllergies = it },
                            label = { Text("Severe Allergies (e.g. Penicillin)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_allergies"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = chronicConditions,
                            onValueChange = { chronicConditions = it },
                            label = { Text("Chronic Conditions (e.g. Asthma)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_conditions"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = regularMedications,
                            onValueChange = { regularMedications = it },
                            label = { Text("Regular Medications") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_meds"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Emergency Contact Name") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_contact_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Emergency Contact Phone") },
                            modifier = Modifier.fillMaxWidth().testTag("input_medical_contact_phone"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                val updated = MedicalProfileEntity(
                                    fullName = fullName.ifBlank { "Citizen" },
                                    bloodGroup = bloodGroup.ifBlank { "Unknown" },
                                    age = ageStr.toIntOrNull() ?: 0,
                                    severeAllergies = severeAllergies,
                                    chronicConditions = chronicConditions,
                                    regularMedications = regularMedications,
                                    emergencyContactName = contactName,
                                    emergencyContactPhone = contactPhone,
                                    emergencyContactRelation = contactRelation,
                                    isOrganDonor = isDonor,
                                    medicalNotes = notes
                                )
                                onSaveProfile(updated)
                                showEditDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("save_medical_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Medical ID", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalInfoRow(
    label: String,
    value: String,
    isWarning: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F141C),
        border = BorderStroke(1.dp, if (isWarning) Color(0xFF5C2D2A) else Color(0xFF1E2D44)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) Color(0xFFFF8A80) else Color(0xFF90A4AE)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
