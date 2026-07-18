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

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandPurplePrimary,
    secondary = BrandPurpleSecondary,
    tertiary = BrandPurpleTertiary,
    background = ProfessionalBackground,
    surface = ProfessionalSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceContainer = ProfessionalSurfaceContainer,
    outline = BorderColorMedium
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandPurplePrimary,
    secondary = BrandPurpleSecondary,
    tertiary = BrandPurpleTertiary,
    background = ProfessionalBackground,
    surface = ProfessionalSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceContainer = ProfessionalSurfaceContainer,
    outline = BorderColorMedium
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Keep false for cohesive branding representation of NS Store
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
