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
    primary = PolishPrimaryContainer,
    onPrimary = PolishOnPrimaryContainer,
    primaryContainer = PolishBrandBlue,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = PolishGreenContainer,
    onTertiary = PolishGreenOnContainer,
    background = Color(0xFF101418),
    surface = Color(0xFF1A1F26),
    onBackground = Color(0xFFE2E2E9),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF2B313B),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF43474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = PolishBrandBlue,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F8),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = PolishGreen,
    onTertiary = Color.White,
    tertiaryContainer = PolishGreenContainer,
    onTertiaryContainer = PolishGreenOnContainer,
    background = PolishBackground,
    surface = PolishSurface,
    onBackground = PolishTextPrimary,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder,
    error = PolishRed,
    onError = Color.White,
    errorContainer = PolishRedContainer,
    onErrorContainer = PolishRedOnContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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

@Composable
fun PowerStationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
