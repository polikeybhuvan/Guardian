package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertPriority
import com.example.domain.ai.OfflineGemmaTriageReport
import com.example.domain.ai.TriageStep
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.MeshWarningOrange
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.PriorityP3
import com.example.ui.theme.PriorityP4
import kotlinx.coroutines.delay

@Composable
fun OfflineGemmaTriageCard(
    report: OfflineGemmaTriageReport,
    modifier: Modifier = Modifier,
    onApplyToSos: (() -> Unit)? = null,
    isCompact: Boolean = false
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(!isCompact) }
    val completedSteps = remember { mutableStateListOf<Int>() }


    // Interactive Tourniquet elapsed timer
    var isTourniquetTimerActive by remember { mutableStateOf(false) }
    var tourniquetSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isTourniquetTimerActive) {
        if (isTourniquetTimerActive) {
            while (true) {
                delay(1000)
                tourniquetSeconds++
            }
        }
    }

    val badgeColor = when (report.priority) {
        AlertPriority.P1 -> PriorityP1
        AlertPriority.P2 -> PriorityP2
        AlertPriority.P3 -> PriorityP3
        AlertPriority.P4 -> PriorityP4
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_gemma_triage_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: Model Badge & Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2838))
                            .border(1.dp, Color(0xFF64B5F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "Rule-based triage",
                            tint = Color(0xFF81D4FA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RULE-BASED TRIAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Priority Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                ) {
                    Text(
                        text = report.triageCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Pattern-based guidance only; it can miss symptoms and is not a medical diagnosis. Call 112 for immediate danger.",
                fontSize = 11.sp,
                color = Color(0xFFCFD8DC),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pattern-matched condition guidance in English & Hindi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceHigh)
                    .padding(12.dp)
            ) {
                Text(
                    text = report.conditionName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = report.conditionNameHindi,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFB74D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.clinicalRationale,
                    fontSize = 11.sp,
                    color = Color(0xFFB0BEC5),
                    lineHeight = 16.sp
                )

                // Symptom Tags Row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    report.detectedSymptomTags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF181C24)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF90CAF9),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Compact mode toggle
            if (isCompact) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide First-Aid Steps" else "Show Immediate First-Aid Steps (${report.actionSteps.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81D4FA)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF81D4FA)
                    )
                }
            }

            // Expandable Action Steps & Disaster Guidance
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Life-Saving Action Checklist Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTION STEPS (जीवन रक्षक कदम)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9299),
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${completedSteps.size}/${report.actionSteps.size} Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedSteps.size == report.actionSteps.size) MeshConnectedGreen else Color(0xFF8E9299)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    report.actionSteps.forEach { step ->
                        val isChecked = completedSteps.contains(step.stepNumber)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) Color(0xFF162519) else DarkSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isChecked) MeshConnectedGreen.copy(alpha = 0.5f) else DarkSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isChecked) completedSteps.remove(step.stepNumber)
                                    else completedSteps.add(step.stepNumber)
                                }
                                .testTag("triage_step_${step.stepNumber}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) completedSteps.add(step.stepNumber)
                                        else completedSteps.remove(step.stepNumber)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MeshConnectedGreen,
                                        uncheckedColor = Color(0xFF8E9299)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Step ${step.stepNumber}: ${step.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChecked) Color(0xFFA5D6A7) else Color.White
                                        )
                                        if (step.isCritical) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmergencyRed.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "CRITICAL",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = EmergencyRed,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = step.titleHindi,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFFCC80)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = step.instruction,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCFD8DC),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = step.instructionHindi,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0BEC5),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Tourniquet timer
                    if (report.requiresTourniquet) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TourniquetTimerWidget(
                            isActive = isTourniquetTimerActive,
                            elapsedSeconds = tourniquetSeconds,
                            onToggle = { isTourniquetTimerActive = !isTourniquetTimerActive },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Critical Warnings / Contraindications
                    if (report.criticalWarnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("triage_warnings_card"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF261212)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB71C1C))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CRITICAL CONTRAINDICATIONS (ये गलतियां न करें)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmergencyRed,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                report.criticalWarnings.zip(report.criticalWarningsHindi).forEach { (en, hi) ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "❌ ", fontSize = 11.sp)
                                        Column {
                                            Text(text = en, fontSize = 11.sp, color = Color(0xFFFFCDD2), fontWeight = FontWeight.SemiBold)
                                            Text(text = hi, fontSize = 10.sp, color = Color(0xFFEF9A9A))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Improvised Disaster Supplies
                    if (report.improvisedSupplies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "IMPROVISED SUPPLIES TO LOCATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9299),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceHigh)
                                .padding(8.dp)
                        ) {
                            report.improvisedSupplies.forEach { supply ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF81D4FA))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = supply,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons Row: Copy Instructions & Attach to SOS
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val text = buildString {
                                    appendLine("🚨 [GEMMA AI DISASTER TRIAGE]")
                                    appendLine("Condition: ${report.conditionName} (${report.conditionNameHindi})")
                                    appendLine("Priority: ${report.triageCode}")
                                    appendLine("\nFirst-Aid Instructions:")
                                    report.actionSteps.forEach {
                                        appendLine("${it.stepNumber}. ${it.title}: ${it.instruction}")
                                    }
                                    if (report.criticalWarnings.isNotEmpty()) {
                                        appendLine("\nWarnings:")
                                        report.criticalWarnings.forEach { appendLine("- $it") }
                                    }
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Rule-based triage guidance", text))
                                Toast.makeText(context, "Triage instructions copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("copy_triage_instructions_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Guide", fontSize = 12.sp)
                        }

                        if (onApplyToSos != null) {
                            Button(
                                onClick = onApplyToSos,
                                modifier = Modifier.weight(1.3f).testTag("apply_triage_to_sos_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                            ) {
                                Icon(Icons.Filled.CrisisAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach to SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TourniquetTimerWidget(
    isActive: Boolean,
    elapsedSeconds: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) Color(0xFF1E2838) else DarkSurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFF2962FF) else DarkSurfaceBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .testTag("tourniquet_timer_widget")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                tint = if (isActive) Color(0xFF81D4FA) else Color(0xFF8E9299),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                val mins = elapsedSeconds / 60
                val secs = elapsedSeconds % 60
                val timeStr = "%02d:%02d".format(mins, secs)

                Text(
                    text = if (isActive) "Tourniquet: $timeStr" else "Start T-Timer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (isActive) FontFamily.Monospace else FontFamily.Default,
                    color = if (isActive) Color(0xFF81D4FA) else Color.White
                )
                Text(
                    text = if (isActive) "Max 2h safe window" else "Track time on limb",
                    fontSize = 9.sp,
                    color = Color(0xFF8E9299)
                )
            }
        }
    }
}
