package com.galaxywk.originalmedialibrary.android.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Amber = Color(0xFFF5A524)
val AmberInk = Color(0xFF1B1305)

internal val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = AmberInk,
    primaryContainer = Color(0xFF3A2A10),
    onPrimaryContainer = Color(0xFFFFBE55),
    secondary = Color(0xFFFFBE55),
    onSecondary = AmberInk,
    secondaryContainer = Color(0xFF2E2414),
    onSecondaryContainer = Color(0xFFFFD99A),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFF2F2F4),
    surface = Color(0xFF0E0F12),
    onSurface = Color(0xFFF2F2F4),
    surfaceVariant = Color(0xFF1D1F25),
    onSurfaceVariant = Color(0xFF9B9DA6),
    surfaceContainerLowest = Color(0xFF0A0B0D),
    surfaceContainerLow = Color(0xFF121317),
    surfaceContainer = Color(0xFF16171C),
    surfaceContainerHigh = Color(0xFF1D1F25),
    surfaceContainerHighest = Color(0xFF262930),
    outline = Color(0xFF3A3D45),
    outlineVariant = Color(0xFF24262C),
    error = Color(0xFFFF6369),
    onError = Color(0xFF2B0507),
    errorContainer = Color(0xFF3A1416),
    onErrorContainer = Color(0xFFFFB3B6),
)

internal val LightColors = lightColorScheme(
    primary = Amber,
    onPrimary = AmberInk,
    primaryContainer = Color(0xFFFDEBCB),
    onPrimaryContainer = Color(0xFF6B3F00),
    secondary = Color(0xFFA86400),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1DA),
    onSecondaryContainer = Color(0xFF4A2C00),
    background = Color(0xFFF5F4F1),
    onBackground = Color(0xFF19191C),
    surface = Color(0xFFF5F4F1),
    onSurface = Color(0xFF19191C),
    surfaceVariant = Color(0xFFF1F0EC),
    onSurfaceVariant = Color(0xFF64656D),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFAF8),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF1F0EC),
    surfaceContainerHighest = Color(0xFFE6E4DF),
    outline = Color(0xFFD3D1CB),
    outlineVariant = Color(0xFFE6E4DF),
    error = Color(0xFFD4343C),
    onError = Color.White,
    errorContainer = Color(0xFFFBE3E4),
    onErrorContainer = Color(0xFF5C0D11),
)

@Immutable
data class StatusColors(val success: Color, val successContainer: Color)

internal val DarkStatus = StatusColors(success = Color(0xFF3DD68C), successContainer = Color(0xFF12301F))
internal val LightStatus = StatusColors(success = Color(0xFF12945A), successContainer = Color(0xFFDDF3E8))

val LocalStatusColors = staticCompositionLocalOf { DarkStatus }
