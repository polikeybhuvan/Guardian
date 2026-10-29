package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmergencyRedLight,
    onPrimary = Color(0xFF600004),
    primaryContainer = EmergencyRedContainer,
    onPrimaryContainer = OnEmergencyRedContainer,
    secondary = GoogleBlueLight,
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF00497D),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = GoogleGreenLight,
    onTertiary = Color(0xFF00391A),
    background = DarkSurfaceBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceBackground,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceBorder,
    error = EmergencyRedLight,
    onError = Color(0xFF600004)
)

private val LightColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = GoogleBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E3FC),
    onSecondaryContainer = Color(0xFF041E49),
    tertiary = GoogleGreen,
    onTertiary = Color.White,
    background = LightSurfaceBackground,
    onBackground = LightTextPrimary,
    surface = LightSurfaceContainer,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSurfaceBorder,
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep high-contrast emergency palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
