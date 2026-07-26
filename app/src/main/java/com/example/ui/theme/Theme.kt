package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricTeal,
    secondary = EmeraldGreen,
    tertiary = AmberWarning,
    background = SlateDarkBg,
    surface = SlateCardBg,
    onPrimary = Color(0xFF00373E),
    onSecondary = Color(0xFF003311),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SlateCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SlateDivider
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00727A),
    secondary = Color(0xFF008E34),
    tertiary = Color(0xFFD47A00),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun BudgetBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our beautiful custom brand colors for a cohesive look rather than random device colors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    BudgetBuddyTheme(darkTheme = darkTheme, content = content)
}
