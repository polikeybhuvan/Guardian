package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

private const val PERMISSION_SETUP_PREFS = "guardian_permission_setup"
private const val PERMISSION_SETUP_DONE = "first_launch_request_complete"

@Composable
fun FirstLaunchPermissionsDialog() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PERMISSION_SETUP_PREFS, android.content.Context.MODE_PRIVATE)
    }
    val permissions = remember { guardianRuntimePermissions() }
    val missingPermissions = remember {
        permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    var isVisible by remember {
        mutableStateOf(!preferences.getBoolean(PERMISSION_SETUP_DONE, false) && missingPermissions.isNotEmpty())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        preferences.edit().putBoolean(PERMISSION_SETUP_DONE, true).apply()
        isVisible = false
    }

    if (!isVisible) return

    AlertDialog(
        onDismissRequest = {
            preferences.edit().putBoolean(PERMISSION_SETUP_DONE, true).apply()
            isVisible = false
        },
        title = { Text("Set up Guardian") },
        text = {
            Column {
                Text(
                    "Choose which emergency features Guardian can use: location for SOS and maps, " +
                        "SMS to alert your contacts, nearby access for relay, camera for Torch SOS, " +
                        "microphone for voice notes, and notifications for alerts."
                )
                Text(
                    "Android may show a few system permission screens. You can change access later in Settings.",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (missingPermissions.isEmpty()) {
                        preferences.edit().putBoolean(PERMISSION_SETUP_DONE, true).apply()
                        isVisible = false
                    } else {
                        permissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }
            ) { Text("Continue") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    preferences.edit().putBoolean(PERMISSION_SETUP_DONE, true).apply()
                    isVisible = false
                }
            ) { Text("Not now") }
        }
    )
}

private fun guardianRuntimePermissions(): List<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    add(Manifest.permission.SEND_SMS)
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.RECORD_AUDIO)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_ADVERTISE)
        add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.NEARBY_WIFI_DEVICES)
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}
