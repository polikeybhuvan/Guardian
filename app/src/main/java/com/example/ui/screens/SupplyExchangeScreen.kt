package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.SupplyExchangeEntity
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

@Composable
fun SupplyExchangeScreen(
    supplies: List<SupplyExchangeEntity>,
    onAddSupply: (SupplyExchangeEntity) -> Unit,
    onResolveSupply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterType by remember { mutableStateOf("ALL") }
    var showPostDialog by remember { mutableStateOf(false) }

    val filteredList = remember(supplies, selectedFilterType) {
        when (selectedFilterType) {
            "REQUEST" -> supplies.filter { it.type == "REQUEST" }
            "OFFER" -> supplies.filter { it.type == "OFFER" }
            else -> supplies
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F141C))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All Supplies (${supplies.size})",
                    "REQUEST" to "Needs / Requests (${supplies.count { it.type == "REQUEST" }})",
                    "OFFER" to "Available / Offers (${supplies.count { it.type == "OFFER" }})"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilterType == key
                    val chipColor = when (key) {
                        "REQUEST" -> EmergencyRed
                        "OFFER" -> MeshConnectedGreen
                        else -> Color(0xFF81D4FA)
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) chipColor.copy(alpha = 0.25f) else Color(0xFF1E2838),
                        border = BorderStroke(1.dp, if (isSelected) chipColor else DarkSurfaceBorder),
                        modifier = Modifier.clickable { selectedFilterType = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF8E9299),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.VolunteerActivism,
                                contentDescription = null,
                                tint = Color(0xFF607D8B),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No mutual aid items posted yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8E9299)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button to post a genuine supply request or offer",
                                fontSize = 12.sp,
                                color = Color(0xFF607D8B)
                            )
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        val isOffer = item.type == "OFFER"
                        val tagColor = if (isOffer) MeshConnectedGreen else EmergencyRed

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("supply_card_${item.id}"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = tagColor.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, tagColor.copy(alpha = 0.6f))
                                        ) {
                                            Text(
                                                text = if (isOffer) "Available" else "Urgent Need",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tagColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81D4FA)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E2838)
                                ) {
                                    Text(
                                        text = item.quantity,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 ${item.locationLabel} • ${item.contactInfo}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF90A4AE)
                                )

                                Button(
                                    onClick = { onResolveSupply(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A24)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Mark Resolved", fontSize = 10.sp, color = MeshConnectedGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Post Supply FAB
        FloatingActionButton(
            onClick = { showPostDialog = true },
            containerColor = Color(0xFF00C853),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("post_supply_fab")
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Post Supply Need or Offer")
        }

        // Post Dialog
        if (showPostDialog) {
            var postType by remember { mutableStateOf("REQUEST") }
            var category by remember { mutableStateOf("WATER") }
            var title by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("Current Sector") }

            Dialog(onDismissRequest = { showPostDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                    border = BorderStroke(1.dp, Color(0xFF23354A))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Post Mutual Aid Supply Item",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Post Type Switcher
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("REQUEST" to "Need (Request)", "OFFER" to "Available (Offer)").forEach { (t, label) ->
                                Button(
                                    onClick = { postType = t },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (postType == t) (if (t == "OFFER") MeshConnectedGreen else EmergencyRed) else Color(0xFF1E2838)
                                    ),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Clean Water, Sterile Gauze)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity (e.g. 5 Liters, 2 Boxes)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Details & urgency") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Landmark") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showPostDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Cancel", color = Color(0xFFB0BEC5))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        onAddSupply(
                                            SupplyExchangeEntity(
                                                id = "SUPPLY_${System.currentTimeMillis()}",
                                                type = postType,
                                                category = category,
                                                title = title.trim(),
                                                description = description.trim(),
                                                quantity = quantity.trim().ifBlank { "1 Unit" },
                                                locationLabel = location.trim(),
                                                contactInfo = "Local Mesh Node"
                                            )
                                        )
                                        showPostDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeshConnectedGreen)
                            ) {
                                Text("Broadcast Item")
                            }
                        }
                    }
                }
            }
        }
    }
}
