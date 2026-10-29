package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.DeliveryStep
import com.example.data.model.MeshMessage
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MeshConnectedGreen
import com.example.ui.theme.PriorityP2

@Composable
fun DeliveryJourneyCard(
    activeAlert: MeshMessage?,
    currentStep: DeliveryStep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("delivery_journey_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceContainer
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DELIVERY LIFELINE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFAAAAAA)
                )

                if (activeAlert != null) {
                    Text(
                        text = "ID: ${activeAlert.msgId.take(8)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Step Journey
            StepRow(
                stepIndex = 1,
                title = "Device Saved",
                detail = if (activeAlert != null) {
                    val locStr = if (activeAlert.latitude != null && activeAlert.longitude != null)
                        "GPS: ${"%.3f".format(activeAlert.latitude)}, ${"%.3f".format(activeAlert.longitude)}"
                    else
                        "GPS Pending Fix"
                    "Signed via ECDSA Keystore • $locStr"
                } else "Ready to dispatch",
                icon = Icons.Default.Lock,
                isCompleted = activeAlert != null,
                isCurrent = currentStep == DeliveryStep.LOCAL_SAVED && activeAlert != null
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepRow(
                stepIndex = 2,
                title = "Nearby phone delivery",
                detail = if (activeAlert != null && activeAlert.hopCount > 0)
                    "Relayed through ${activeAlert.hopCount} Guardian peer(s)"
                else if (activeAlert != null)
                    "Waiting for a connected Guardian phone"
                else
                    "Enable relay and connect to another Guardian phone",
                icon = Icons.Default.NearMe,
                isCompleted = (currentStep.order >= DeliveryStep.PEER_RELAYED.order),
                isCurrent = currentStep == DeliveryStep.PEER_RELAYED
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepRow(
                stepIndex = 3,
                title = "Gateway Uploaded",
                detail = if (activeAlert?.isGatewayUploaded == true)
                    "Uploaded to Disaster Network by ${activeAlert.lastRelayNodeId ?: "Gateway Node"}"
                else
                    "Internet gateway delivery is not configured.",
                icon = Icons.Default.CloudDone,
                isCompleted = (currentStep.order >= DeliveryStep.GATEWAY_UPLOADED.order),
                isCurrent = currentStep == DeliveryStep.GATEWAY_UPLOADED
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepRow(
                stepIndex = 4,
                title = "Responder Acknowledged",
                detail = if (activeAlert?.isAcknowledged == true)
                    (activeAlert.ackByResponder ?: "Disaster response confirmed")
                else
                    "Pending responder acknowledgment",
                icon = Icons.Default.SupportAgent,
                isCompleted = (currentStep.order >= DeliveryStep.RESPONDER_ACKNOWLEDGED.order),
                isCurrent = currentStep == DeliveryStep.RESPONDER_ACKNOWLEDGED
            )
        }
    }
}

@Composable
private fun StepRow(
    stepIndex: Int,
    title: String,
    detail: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val iconBgColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MeshConnectedGreen
            isCurrent -> PriorityP2
            else -> Color(0xFF2C3036)
        },
        label = "iconBg"
    )

    val iconTint = if (isCompleted || isCurrent) Color.Black else Color(0xFF888888)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent || isCompleted) Color.White else Color(0xFF9E9E9E)
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrent) PriorityP2 else Color(0xFF757575),
                fontSize = 11.sp
            )
        }
    }
}
