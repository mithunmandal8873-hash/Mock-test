package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val MockTestDarkColorScheme = darkColorScheme(
    primary = SapphirePrimary,
    onPrimary = TextPrimary,
    primaryContainer = NavySurfaceHighlight,
    onPrimaryContainer = SkyAccent,
    secondary = CyanMedical,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurfaceElevated,
    onSecondaryContainer = CyanMedical,
    tertiary = SkyAccent,
    onTertiary = NavyBackground,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = NavySurfaceHighlight,
    error = WrongRed,
    onError = TextPrimary,
    errorContainer = WrongRedContainer,
    onErrorContainer = TextPrimary
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MockTestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MockTestDarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
