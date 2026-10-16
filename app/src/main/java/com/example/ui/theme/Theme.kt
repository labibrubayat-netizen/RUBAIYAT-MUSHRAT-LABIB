package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Custom Material 3 Dark Color Scheme (Electric Violet & Midnight Navy)
private val DarkColorScheme = darkColorScheme(
  primary = VioletLight,
  onPrimary = VioletContainerDark,
  primaryContainer = VioletDark,
  onPrimaryContainer = VioletSubtle,

  secondary = NavyIce,
  onSecondary = NavyDeep,
  secondaryContainer = NavyMedium,
  onSecondaryContainer = NavySubtle,

  tertiary = ElectricCyan,
  onTertiary = NavyDeep,
  tertiaryContainer = NavyAccent,
  onTertiaryContainer = Color(0xFFDBEAFE),

  background = SurfaceDark,
  onBackground = TextLight,
  surface = CardSurfaceDark,
  onSurface = TextLight,
  surfaceVariant = CardSurfaceVariantDark,
  onSurfaceVariant = TextDarkSubtle,

  outline = NavyLight,
  outlineVariant = Color(0xFF1E293B),
  inverseSurface = TextLight,
  inverseOnSurface = NavyPrimary,
  inversePrimary = VioletPrimary
)

// Custom Material 3 Light Color Scheme (Electric Violet & Deep Navy)
private val LightColorScheme = lightColorScheme(
  primary = VioletPrimary,
  onPrimary = Color.White,
  primaryContainer = VioletSubtle,
  onPrimaryContainer = VioletContainerDark,

  secondary = NavyPrimary,
  onSecondary = Color.White,
  secondaryContainer = NavySubtle,
  onSecondaryContainer = NavyPrimary,

  tertiary = NavyAccent,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFDBEAFE),
  onTertiaryContainer = Color(0xFF1E3A8A),

  background = SurfaceLight,
  onBackground = TextDark,
  surface = CardSurfaceLight,
  onSurface = TextDark,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = TextSubtle,

  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFE2E8F0),
  inverseSurface = NavyPrimary,
  inverseOnSurface = SurfaceLight,
  inversePrimary = VioletLight
)

val AppShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(20.dp),
  large = RoundedCornerShape(28.dp),
  extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content
  )
}
