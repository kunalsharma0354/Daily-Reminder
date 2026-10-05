package com.nexora.reminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = SurfaceElevatedDark,
    onPrimaryContainer = MonoWhite,
    secondary = SurfaceDark,
    onSecondary = MonoWhite,
    tertiary = MonoWhite,
    onTertiary = MonoBlack,
    background = BgDark,
    onBackground = MonoWhite,
    surface = SurfaceDark,
    onSurface = MonoWhite,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = Color(0xFF1F1F1F),
    error = Color(0xFFA1A1A1),
    onError = MonoBlack,
    surfaceContainerHighest = SurfaceElevatedDark,
    inversePrimary = MonoBlack,
    scrim = MonoBlack.copy(0.6f)
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoBlack,
    onPrimaryContainer = MonoWhite,
    secondary = SurfaceElevatedLight,
    onSecondary = MonoBlack,
    tertiary = MonoBlack,
    onTertiary = MonoWhite,
    background = BgLight,
    onBackground = MonoBlack,
    surface = SurfaceLight,
    onSurface = MonoBlack,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = Color(0xFFF0F0F0),
    error = Color(0xFF525252),
    onError = MonoWhite,
    surfaceContainerHighest = SurfaceElevatedLight,
    inversePrimary = MonoWhite,
    scrim = MonoBlack.copy(0.4f)
)

@Composable
fun ReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// Modern tokens — 20dp cards, pill buttons
object ReminderTokens {
    val CardRadius = RoundedCornerShape(20.dp)
    val CardRadiusSmall = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(100.dp)
    val SheetRadius = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
