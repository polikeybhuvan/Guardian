package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyContactEntity
import com.example.data.location.DeviceCoordinates
import com.example.data.location.SmsDispatchResult
import com.example.data.model.EmergencyCategory
import com.example.data.model.EmergencyCenter
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContactEntity>,
    currentCoordinates: DeviceCoordinates?,
    nearestHospital: EmergencyCenter?,
    nearestPolice: EmergencyCenter?,
    nearestRelief: EmergencyCenter?,
    selectedCategory: EmergencyCategory,
    lastSmsResult: SmsDispatchResult?,
    onAddContact: (String, String, String) -> Unit,
    onDeleteContact: (Long) -> Unit,
    onSendTestSms: () -> Unit = {},
    onOpenMedicalId: () -> Unit = {},
    onOpenFamilyCircle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showTestSmsConfirmation by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Safety Hub Navigation Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenMedicalId() }
                            .testTag("hub_open_medical_id"),
                        color = Color(0xFF162338),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C4366))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Medical ID & QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenFamilyCircle() }
                            .testTag("hub_open_family_circle"),
                        color = Color(0xFF13202E),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF233E5E))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Family Radar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("contacts_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmergencyRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Send SOS by SMS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Share your location with saved contacts.",
                                    fontSize = 13.sp,
                                    color = Color(0xFFB0B3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Location & Nearest Centers Status
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = if (currentCoordinates != null) MeshConnectedGreen else MeshWarningOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentCoordinates != null) {
                                            "GPS Ready: ${String.format(java.util.Locale.US, "%.4f", currentCoordinates.latitude)}, ${String.format(java.util.Locale.US, "%.4f", currentCoordinates.longitude)}"
                                        } else {
                                            "GPS Acquiring..."
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentCoordinates != null) MeshConnectedGreen else MeshWarningOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Nearest Hospital
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalHospital,
                                            contentDescription = null,
                                            tint = EmergencyRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = nearestHospital?.let { "${it.name.take(14)}.. (${it.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude)})" } ?: "Ambulance: 108",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }

                                    // Nearest Police
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Security,
                                            contentDescription = null,
                                            tint = Color(0xFF2962FF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = nearestPolice?.let { "${it.name.take(14)}.. (${it.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude)})" } ?: "Emergency: 112",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Preview the SOS template or send a clearly labeled setup test.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPreviewDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_sms_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4E57)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview SMS", fontSize = 13.sp)
                            }

                            Button(
                                onClick = { showTestSmsConfirmation = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_sms_broadcast_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send test SMS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            "Sends a clearly labeled test to your saved contacts. It does not send an SOS.",
                            fontSize = 11.sp,
                            color = Color(0xFFB0B3B8),
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Last SMS Result feedback
                        AnimatedVisibility(visible = lastSmsResult != null) {
                            lastSmsResult?.let { res ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (res.isSuccess) Color(0xFF1B3D2F) else Color(0xFF3E1C1C),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (res.isSuccess) MeshConnectedGreen.copy(alpha = 0.5f) else EmergencyRed.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (res.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                            contentDescription = null,
                                            tint = if (res.isSuccess) MeshConnectedGreen else EmergencyRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (res.isSuccess) {
                                                "Submitted to Android for ${res.sentCount} contact(s). Delivery is not confirmed."
                                            } else {
                                                res.error ?: "Failed to transmit SMS"
                                            },
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contacts Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IMPORTANT CONTACTS (${contacts.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9299),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "SMS to saved contacts",
                        fontSize = 11.sp,
                        color = MeshConnectedGreen
                    )
                }
            }

            // Empty state or list of contacts
            if (contacts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("empty_contacts_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF8E9299),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No contacts yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Choose who should receive your SOS.",
                                fontSize = 12.sp,
                                color = Color(0xFF8E9299),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                modifier = Modifier.testTag("add_first_contact_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add contact")
                            }
                        }
                    }
                }
            } else {
                items(contacts, key = { it.id }) { contact ->
                    ContactItemCard(
                        contact = contact,
                        onDelete = { onDeleteContact(contact.id) },
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                            context.startActivity(intent)
                        },
                        onMessage = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phoneNumber}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Bottom padding for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Contact
        if (contacts.isNotEmpty()) FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_contact_fab"),
            containerColor = EmergencyRed,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Contact")
        }
    }

    if (showTestSmsConfirmation) {
        AlertDialog(
            onDismissRequest = { showTestSmsConfirmation = false },
            title = { Text("Send a setup test?") },
            text = { Text("A non-emergency test SMS will be sent directly to your saved contacts. Android must grant Guardian the restricted SMS permission first. Your carrier may charge for SMS.") },
            confirmButton = {
                Button(onClick = {
                    showTestSmsConfirmation = false
                    onSendTestSms()
                }) { Text("Send test") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTestSmsConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Add Contact
    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, relationship ->
                onAddContact(name, phone, relationship)
                showAddDialog = false
            }
        )
    }

    // Dialog: Preview SMS Template
    if (showPreviewDialog) {
        val previewText = remember(contacts, currentCoordinates, nearestHospital, nearestPolice, nearestRelief) {
            buildString {
                append("🚨 [EMERGENCY SOS DISTRESS ALERT] 🚨\n")
                append("Incident: ${selectedCategory.displayName} (Urgent Rescue Needed)\n")
                if (currentCoordinates != null) {
                    append("Live Location: https://maps.google.com/?q=${currentCoordinates.latitude},${currentCoordinates.longitude}\n")
                    append("GPS Coords: ${String.format(java.util.Locale.US, "%.5f", currentCoordinates.latitude)}, ${String.format(java.util.Locale.US, "%.5f", currentCoordinates.longitude)} (±${currentCoordinates.accuracyM.toInt()}m)\n")
                } else {
                    append("Live Location: [GPS acquiring...]\n")
                }
                nearestHospital?.let { append("Nearest Hospital: ${it.name} (${it.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude)}) Ph: ${it.phoneNumber}\n") }
                nearestPolice?.let { append("Nearest Police: ${it.name} (${it.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude)}) Ph: ${it.phoneNumber}\n") }
                nearestRelief?.let { append("Nearest Relief: ${it.name} (${it.formattedDistance(currentCoordinates?.latitude, currentCoordinates?.longitude)}) Ph: ${it.phoneNumber}\n") }
                append("Urgent assistance requested. Please dispatch help or notify emergency authorities!")
            }
        }

        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = {
                Text(
                    text = "Distress SMS Template",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "This exact message will be dispatched via SMS to all ${contacts.size} emergency contacts when the SOS button is triggered:",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3B8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Text(
                            text = previewText,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPreviewDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Close")
                }
            },
            containerColor = DarkSurfaceContainer
        )
    }
}

@Composable
fun ContactItemCard(
    contact: EmergencyContactEntity,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contact_item_${contact.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2962FF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF82B1FF)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceHigh
                    ) {
                        Text(
                            text = contact.relationship,
                            fontSize = 10.sp,
                            color = Color(0xFFB0B3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.phoneNumber,
                    fontSize = 13.sp,
                    color = Color(0xFF8E9299)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(36.dp).testTag("call_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = MeshConnectedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onMessage,
                    modifier = Modifier.size(36.dp).testTag("message_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "SMS",
                        tint = Color(0xFF82B1FF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = EmergencyRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, relationship: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Family") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val relationshipOptions = listOf("Family", "Parent", "Spouse", "Sibling", "Friend", "Doctor", "Neighbor")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Important Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You can prepare or send an SOS SMS to this contact from this screen. SMS delivery depends on Android permission, mobile service, and your carrier.",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B3B8)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Contact Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmergencyRed,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMsg = null
                    },
                    label = { Text("Phone Number (with country code)") },
                    placeholder = { Text("+91 9876543210") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmergencyRed,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                )

                Text(
                    text = "Relationship",
                    fontSize = 12.sp,
                    color = Color(0xFF8E9299),
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationshipOptions.take(4).forEach { rel ->
                        val isSelected = relationship == rel
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmergencyRed else DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmergencyRed else DarkSurfaceBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EmergencyRed else DarkSurfaceBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) EmergencyRed else DarkSurfaceHigh)
                                .padding(vertical = 8.dp),
                            onClick = { relationship = rel }
                        ) {
                            Text(
                                text = rel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                errorMsg?.let { err ->
                    Text(text = err, color = EmergencyRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Please enter contact name"
                    } else if (phone.isBlank()) {
                        errorMsg = "Please enter phone number"
                    } else {
                        onConfirm(name, phone, relationship)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                modifier = Modifier.testTag("save_contact_button")
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8E9299))
            ) {
                Text("Cancel")
            }
        },
        containerColor = DarkSurfaceContainer
    )
}
