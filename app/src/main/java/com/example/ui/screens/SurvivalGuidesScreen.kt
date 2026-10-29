package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfflineSurvivalProtocols
import com.example.data.model.SurvivalGuide
import com.example.data.model.TriageChatMessage
import com.example.domain.ai.OfflineGemmaTriageReport
import com.example.ui.components.OfflineGemmaTriageCard
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange

enum class SurvivalViewMode {
    OFFLINE_GEMMA_AI,
    FIELD_PROTOCOLS
}

@Composable
fun SurvivalGuidesScreen(
    customSymptomInput: String = "",
    customGemmaReport: OfflineGemmaTriageReport? = null,
    triageChatMessages: List<TriageChatMessage> = emptyList(),
    isAiTyping: Boolean = false,
    offlineGemmaStatus: String = "Emergency guidance available · install Gemma for AI chat",
    onInstallGemmaModel: (android.net.Uri) -> Unit = {},
    onSymptomInputChange: (String) -> Unit = {},
    onSendChatMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onApplyTriageToSos: (OfflineGemmaTriageReport) -> Unit = {},
    onTriggerSosShortcut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(SurvivalViewMode.OFFLINE_GEMMA_AI) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedGuideId by remember { mutableStateOf<String?>("cpr") }

    val guides = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            OfflineSurvivalProtocols.guides
        } else {
            OfflineSurvivalProtocols.guides.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.hindiTitle.contains(searchQuery, ignoreCase = true) ||
                it.summary.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("survival_guides_screen")
    ) {
        // Top High-Contrast Segmented Mode Switcher
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurfaceContainer,
            border = BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 1: Local rule-based triage guidance
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewMode = SurvivalViewMode.OFFLINE_GEMMA_AI }
                        .testTag("tab_gemma_ai_triage"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (viewMode == SurvivalViewMode.OFFLINE_GEMMA_AI) Color(0xFF1E2D4A) else Color.Transparent,
                    border = if (viewMode == SurvivalViewMode.OFFLINE_GEMMA_AI) BorderStroke(1.dp, Color(0xFF64B5F6)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = if (viewMode == SurvivalViewMode.OFFLINE_GEMMA_AI) Color(0xFF90CAF9) else Color(0xFF8E9299),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AID assistant",
                            fontWeight = if (viewMode == SurvivalViewMode.OFFLINE_GEMMA_AI) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (viewMode == SurvivalViewMode.OFFLINE_GEMMA_AI) Color.White else Color(0xFF8E9299)
                        )
                    }
                }

                // Tab 2: Standard Field Protocols
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewMode = SurvivalViewMode.FIELD_PROTOCOLS }
                        .testTag("tab_field_protocols"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (viewMode == SurvivalViewMode.FIELD_PROTOCOLS) Color(0xFF2E1717) else Color.Transparent,
                    border = if (viewMode == SurvivalViewMode.FIELD_PROTOCOLS) BorderStroke(1.dp, EmergencyRed) else null
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = if (viewMode == SurvivalViewMode.FIELD_PROTOCOLS) EmergencyRed else Color(0xFF8E9299),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Field Protocols",
                            fontWeight = if (viewMode == SurvivalViewMode.FIELD_PROTOCOLS) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (viewMode == SurvivalViewMode.FIELD_PROTOCOLS) Color.White else Color(0xFF8E9299)
                        )
                    }
                }
            }
        }

        // Body Content based on selected mode
        when (viewMode) {
            SurvivalViewMode.OFFLINE_GEMMA_AI -> {
                GemmaTriageChatView(
                    messages = triageChatMessages,
                    isAiTyping = isAiTyping,
                    modelStatus = offlineGemmaStatus,
                    onInstallModel = onInstallGemmaModel,
                    onSendMessage = onSendChatMessage,
                    onClearChat = onClearChat,
                    onApplyTriageToSos = onApplyTriageToSos,
                    onTriggerSosShortcut = onTriggerSosShortcut,
                    modifier = Modifier.fillMaxSize()
                )
            }

            SurvivalViewMode.FIELD_PROTOCOLS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("survival_header_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                            border = BorderStroke(1.dp, DarkSurfaceBorder)
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
                                            imageVector = Icons.Filled.MedicalServices,
                                            contentDescription = null,
                                            tint = EmergencyRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Offline Survival Protocols",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "100% Offline Medical & Disaster Triage Guides",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB0B3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search CPR, Bleeding, Flood, Burns...", fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF8E9299))
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = EmergencyRed,
                                        unfocusedBorderColor = DarkSurfaceBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("survival_search_input")
                                )
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "ACTIONABLE FIRST-AID PROTOCOLS (${guides.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9299),
                            letterSpacing = 1.sp
                        )
                    }

                    // Guides items
                    items(guides, key = { it.id }) { guide ->
                        val isExpanded = expandedGuideId == guide.id
                        SurvivalGuideCard(
                            guide = guide,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedGuideId = if (isExpanded) null else guide.id
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SurvivalGuideCard(
    guide: SurvivalGuide,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, badgeColor) = when (guide.urgencyLevel) {
        "CRITICAL" -> Pair(Icons.Filled.Favorite, EmergencyRed)
        "HIGH" -> Pair(Icons.Filled.Warning, MeshWarningOrange)
        else -> Pair(Icons.Filled.MedicalServices, Color(0xFF2962FF))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("guide_card_${guide.id}")
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = BorderStroke(
            1.dp,
            if (isExpanded) badgeColor.copy(alpha = 0.5f) else DarkSurfaceBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getGuideIcon(guide.id),
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guide.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = guide.hindiTitle,
                        fontSize = 11.sp,
                        color = Color(0xFFB0B3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = guide.urgencyLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF8E9299)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = guide.summary,
                fontSize = 13.sp,
                color = Color(0xFFC7CBD1),
                lineHeight = 18.sp
            )

            // Expanded Step-by-Step details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "STEP-BY-STEP ACTIONS:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    guide.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DOs and DONTs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // DOs
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF132A1E),
                            border = BorderStroke(1.dp, MeshConnectedGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MeshConnectedGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DO", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MeshConnectedGreen)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                guide.dos.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD1FAE5),
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }
                        }

                        // DON'Ts
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2E1212),
                            border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Cancel, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DON'T", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmergencyRed)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                guide.donts.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFD1D1),
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(bottom = 3.dp)
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

fun getGuideIcon(id: String): ImageVector {
    return when (id) {
        "cpr" -> Icons.Filled.Favorite
        "bleeding" -> Icons.Filled.Bloodtype
        "flood" -> Icons.Filled.WaterDamage
        "earthquake" -> Icons.Filled.Terrain
        "burns" -> Icons.Filled.LocalFireDepartment
        "fracture" -> Icons.Filled.MedicalServices
        "snakebite" -> Icons.Filled.Warning
        else -> Icons.Filled.MedicalServices
    }
}
