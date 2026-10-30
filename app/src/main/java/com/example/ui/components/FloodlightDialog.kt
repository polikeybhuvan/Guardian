package com.example.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** Full-screen red, white, and blue attention signal. Tap anywhere to stop it. */
@Composable
fun FloodlightDialog(
    onDismiss: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "floodlightCycle")
    val colorPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floodlightColorPhase"
    )
    val flashColor = when (colorPhase.toInt().coerceIn(0, 2)) {
        0 -> Color.Red
        1 -> Color.White
        else -> Color.Blue
    }
    val view = LocalView.current
    val window = LocalActivity.current?.window
    val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    SideEffect {
        insetsController?.apply {
            isAppearanceLightStatusBars = flashColor == Color.White
            isAppearanceLightNavigationBars = flashColor == Color.White
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(flashColor)
            .clickable { onDismiss() }
            .testTag("floodlight_dialog")
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Stop floodlight",
                tint = Color.White
            )
        }
    }
}
