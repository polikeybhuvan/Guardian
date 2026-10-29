package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityDisasterPack
import com.example.data.model.CityDisasterPacksData
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

@Composable
fun CityDisasterPacksScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packs = remember { CityDisasterPacksData.availablePacks }
    val downloadedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            packs.forEach { this[it.id] = it.isDownloaded }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(16.dp)
            .testTag("city_disaster_packs_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF23354A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocationCity,
                        contentDescription = null,
                        tint = Color(0xFF81D4FA),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OFFLINE CITY DISASTER PACKS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Pre-loads regional relief shelters, topography elevations, and survival manifests for complete zero-connectivity resilience.",
                            fontSize = 11.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }
            }
        }

        items(packs, key = { it.id }) { pack ->
            val isDownloaded = downloadedMap[pack.id] == true
            val severityColor = when (pack.riskSeverity) {
                "CRITICAL" -> EmergencyRed
                "HIGH" -> MeshWarningOrange
                else -> Color(0xFF81D4FA)
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("city_pack_${pack.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isDownloaded) MeshConnectedGreen.copy(alpha = 0.5f) else DarkSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = severityColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${pack.hazardType} • ${pack.riskSeverity}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = severityColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${pack.dataSizeMb} MB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF90A4AE)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pack.cityName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏛️ ${pack.shelterCount} Verified Relief Shelters & Safe Zones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF81D4FA)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EMERGENCY AGENCIES:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9299)
                    )
                    pack.emergencyAgencies.forEach { agency ->
                        Text(
                            text = "• $agency",
                            fontSize = 11.sp,
                            color = Color(0xFFCFD8DC)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val nextState = !isDownloaded
                            downloadedMap[pack.id] = nextState
                            val toastMsg = if (nextState) "Downloaded & cached ${pack.cityName} offline pack" else "Purged cache for ${pack.cityName}"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDownloaded) Color(0xFF1E3A24) else Color(0xFF1565C0)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = if (isDownloaded) MeshConnectedGreen else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDownloaded) "CACHED OFFLINE (ACTIVE)" else "DOWNLOAD OFFLINE PACK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDownloaded) MeshConnectedGreen else Color.White
                        )
                    }
                }
            }
        }
    }
}
