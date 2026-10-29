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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.model.AlertPriority
import com.example.data.model.TriageChatMessage
import com.example.domain.ai.OfflineGemmaTriageReport
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GemmaTriageChatView(
    messages: List<TriageChatMessage>,
    isAiTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onApplyTriageToSos: (OfflineGemmaTriageReport) -> Unit,
    onTriggerSosShortcut: () -> Unit,
    modelStatus: String,
    onInstallModel: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onInstallModel(uri)
    }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val gemmaPrefs = remember(context) { context.getSharedPreferences("gemma_model_terms", android.content.Context.MODE_PRIVATE) }
    var acceptedGemmaTerms by remember { mutableStateOf(gemmaPrefs.getBoolean("accepted", false)) }

    if (!acceptedGemmaTerms) {
        AlertDialog(
            onDismissRequest = onTriggerSosShortcut,
            title = { Text("Gemma model terms") },
            text = {
                Column {
                    Text("Guardian includes Google Gemma 3. By continuing, you agree to the Gemma Terms of Use and its prohibited-use restrictions. The license notice and agreement are included in this app. Gemma can make mistakes; call 112 for immediate danger.")
                    TextButton(onClick = { uriHandler.openUri("https://ai.google.dev/gemma/terms") }) {
                        Text("Review Gemma Terms")
                    }
                    TextButton(onClick = { uriHandler.openUri("https://ai.google.dev/gemma/prohibited_use_policy") }) {
                        Text("Review prohibited-use policy")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    gemmaPrefs.edit().putBoolean("accepted", true).apply()
                    acceptedGemmaTerms = true
                }) { Text("Agree and continue") }
            },
            dismissButton = {
                TextButton(onClick = onTriggerSosShortcut) { Text("Not now") }
            }
        )
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isAiTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPills = remember {
        listOf(
            "Heat Stroke & High Fever",
            "Drowning & Submersion",
            "Severe Arterial Bleeding",
            "Smoke Inhalation & Wheezing"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("gemma_triage_chat_view")
    ) {
        // Chat Header with Gemma AI Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurfaceContainer,
            border = BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2838))
                            .border(1.5.dp, Color(0xFF64B5F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Emergency assistant",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = modelStatus,
                            fontSize = 11.sp,
                            color = Color(0xFF8E9299)
                        )
                        if (modelStatus.startsWith("Model unavailable")) {
                            Text(
                                text = "Choose Gemma model file",
                                modifier = Modifier.clickable { modelPicker.launch(arrayOf("*/*")) }.padding(top = 3.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF90CAF9)
                            )
                            Text(
                                text = "Official model · ${com.example.domain.ai.OfflineGemmaChatModel.MODEL_DISPLAY_SIZE}",
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://huggingface.co/litert-community/Gemma3-1B-IT")
                                }.padding(top = 3.dp),
                                fontSize = 10.sp,
                                color = Color(0xFF8E9299)
                            )
                        }
                    }
                }

                // Clear Chat button
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = Color(0xFF8E9299)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                if (message.isUser) {
                    UserChatBubble(message = message, timeFormat = timeFormat)
                } else {
                    AiChatBubble(
                        message = message,
                        timeFormat = timeFormat,
                        onApplyTriageToSos = onApplyTriageToSos,
                        onTriggerSosShortcut = onTriggerSosShortcut,
                        onFollowUpClick = { followUpText ->
                            onSendMessage(followUpText)
                        }
                    )
                }
            }

            // AI Typing indicator
            if (isAiTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2838)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF64B5F6)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Checking local first-aid guidance...",
                            fontSize = 11.sp,
                            color = Color(0xFF90CAF9),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Four compact scenarios, arranged in a stable grid so none are clipped.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101318))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPills.chunked(2).forEach { rowPills ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowPills.forEach { pill ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2430),
                            border = BorderStroke(1.dp, Color(0xFF2E384A)),
                            modifier = Modifier.weight(1f).clickable { onSendMessage(pill) }
                        ) {
                            Text(
                                text = pill,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE0E0E0),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                    if (rowPills.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        // Bottom Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurfaceContainer,
            border = BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "What happened? Include symptoms and when it began…",
                            fontSize = 13.sp,
                            color = Color(0xFF6E7480)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("triage_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF64B5F6),
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceHigh,
                        unfocusedContainerColor = DarkSurfaceHigh
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("triage_chat_send_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inputText.isNotBlank()) Color(0xFF2962FF) else Color(0xFF2A303C),
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Symptoms",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun cleanAssistantText(raw: String): String = raw
    .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
    .replace(Regex("(?m)^\\s*\\*\\s+"), "• ")
    .replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")
    .trim()

@Composable
private fun UserChatBubble(
    message: TriageChatMessage,
    timeFormat: SimpleDateFormat
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = "You",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF82B1FF)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )
        }

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFF1E3A5F),
            border = BorderStroke(1.dp, Color(0xFF2962FF)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AiChatBubble(
    message: TriageChatMessage,
    timeFormat: SimpleDateFormat,
    onApplyTriageToSos: (OfflineGemmaTriageReport) -> Unit,
    onTriggerSosShortcut: () -> Unit,
    onFollowUpClick: (String) -> Unit
) {
    val report = message.triageReport

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF64B5F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "First-aid guide",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_chat_response_card"),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
            border = BorderStroke(
                1.dp,
                if (report?.priority == AlertPriority.P1) EmergencyRed
                else if (report?.priority == AlertPriority.P2) MeshWarningOrange
                else DarkSurfaceBorder
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Priority Badge if report present
                if (report != null) {
                    val badgeColor = when (report.priority) {
                        AlertPriority.P1 -> EmergencyRed
                        AlertPriority.P2 -> MeshWarningOrange
                        AlertPriority.P3 -> MeshConnectedGreen
                        AlertPriority.P4 -> Color(0xFF82B1FF)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, badgeColor),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = report.triageCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = badgeColor
                            )
                        }
                    }
                }

                // Conversational Text / Clinical Diagnosis
                Text(
                    text = cleanAssistantText(message.text),
                    fontSize = 13.sp,
                    color = Color(0xFFE0E0E0),
                    lineHeight = 18.sp
                )

                // If structured report is available, provide full emergency kit guidance
                if (report != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Step-by-Step Emergency Instructions
                    Text(
                        text = "EMERGENCY ACTION STEPS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF82B1FF),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    report.actionSteps.forEach { step ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161A22),
                            border = BorderStroke(1.dp, if (step.isCritical) EmergencyRed.copy(alpha = 0.6f) else DarkSurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (step.isCritical) EmergencyRed else Color(0xFF2962FF),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${step.stepNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = step.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = step.instruction,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0B3B8),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Improvised supplies
                    if (report.improvisedSupplies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "IMPROVISED DISASTER SUPPLIES:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            report.improvisedSupplies.take(3).forEach { supply ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF202632)
                                ) {
                                    Text(
                                        text = "• $supply",
                                        fontSize = 10.sp,
                                        color = Color(0xFF81D4FA),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action Button: Apply to SOS Alert
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onApplyTriageToSos(report) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("apply_triage_to_sos_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmergencyRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CrisisAlert,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to SOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Follow-up prompt chips
                if (message.quickFollowUps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Try one",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9299)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.quickFollowUps.forEach { chipText ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E2838),
                                border = BorderStroke(1.dp, Color(0xFF2E4058)),
                                modifier = Modifier.clickable {
                                    if (chipText.contains("SOS", ignoreCase = true)) {
                                        onTriggerSosShortcut()
                                    } else {
                                        onFollowUpClick(chipText)
                                    }
                                }
                            ) {
                                Text(
                                    text = chipText,
                                    fontSize = 11.sp,
                                    color = Color(0xFF90CAF9),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
