package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.location.DeviceCoordinates

@Composable
fun NearbyHelpRadarScreen(
    currentCoordinates: DeviceCoordinates?,
    onRefreshLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    EmergencyMapScreen(
        currentCoordinates = currentCoordinates,
        onRefreshLocation = onRefreshLocation,
        modifier = modifier.fillMaxSize().testTag("nearby_help_radar_screen")
    )
}