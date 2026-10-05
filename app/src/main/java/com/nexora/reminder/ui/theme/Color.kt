package com.nexora.reminder.ui.theme

import androidx.compose.ui.graphics.Color

// Modern monochrome — pure black & white
val MonoBlack = Color(0xFF000000)
val MonoWhite = Color(0xFFFFFFFF)

// Dark surfaces (soft blacks for depth, still monochrome)
val BgDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF111111)
val SurfaceElevatedDark = Color(0xFF1A1A1A)
val OutlineDark = Color(0xFF2A2A2A)
val TextSecondaryDark = Color(0xFFA1A1A1)

// Light surfaces
val BgLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFF5F5F5)
val OutlineLight = Color(0xFFE5E5E5)
val TextSecondaryLight = Color(0xFF525252)

// Keep legacy names for compat (mapped to mono)
val NavyBackground = BgDark
val NavySurface = SurfaceDark
val NavySurfaceElevated = SurfaceElevatedDark
val NavyOutline = OutlineDark
val ElectricBlue = MonoWhite
val ElectricBlueDark = MonoBlack
val NeonLime = MonoWhite
val LimeGreenLight = MonoBlack
val TextDarkOn = MonoWhite
val TextLightOn = MonoBlack
val CoralError = Color(0xFF737373)
val RedErrorLight = Color(0xFF525252)
val HeatInactiveDark = Color(0xFF262626)
val HeatInactiveLight = Color(0xFFE5E5E5)
val HeatMissed = Color(0xFF737373)
val HeatCompleted = MonoWhite

// Heatmap mono levels (for light/dark adaptive use in composables)
val HeatLevel0Dark = Color(0xFF1A1A1A)
val HeatLevel1Dark = Color(0xFF404040)
val HeatLevelFullDark = MonoWhite
val HeatLevel0Light = Color(0xFFF5F5F5)
val HeatLevel1Light = Color(0xFFD4D4D4)
val HeatLevelFullLight = MonoBlack
