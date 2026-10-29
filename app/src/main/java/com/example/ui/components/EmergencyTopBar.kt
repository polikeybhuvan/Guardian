package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.example.R
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

@Composable
fun EmergencyTopBar(
    connectedPeers: Int,
    onOpenDiagnostics: () -> Unit = {},
    onOpenCityPacks: () -> Unit = {},
    onOpenMedicalId: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurfaceHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.guardian_shield_logo),
                contentDescription = null,
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
            )
            Text(
                "Guardian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            Surface(
                modifier = Modifier.clip(CircleShape).clickable(onClick = onOpenDiagnostics)
                    .testTag("topbar_peer_status"),
                color = if (connectedPeers > 0) Color(0xFF173224) else Color(0xFF332817),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CellTower,
                        contentDescription = null,
                        tint = if (connectedPeers > 0) MeshConnectedGreen else MeshWarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "$connectedPeers",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.size(10.dp))
            androidx.compose.foundation.layout.Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("topbar_more_button").size(38.dp)
                        .clip(CircleShape).background(Color(0xFF202630)),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFB8D5FF))
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Relay status") },
                        leadingIcon = { Icon(Icons.Default.CellTower, contentDescription = null) },
                        onClick = { menuExpanded = false; onOpenDiagnostics() },
                        modifier = Modifier.testTag("topbar_diagnostics_menu_item")
                    )
                    DropdownMenuItem(
                        text = { Text("Medical ID") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                        onClick = { menuExpanded = false; onOpenMedicalId() },
                        modifier = Modifier.testTag("topbar_medical_id_menu_item")
                    )
                    DropdownMenuItem(
                        text = { Text("City packs") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        onClick = { menuExpanded = false; onOpenCityPacks() },
                        modifier = Modifier.testTag("topbar_city_packs_menu_item")
                    )
                }
            }

        }
    }
}
