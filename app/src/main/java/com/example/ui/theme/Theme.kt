package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NusantaraDarkColorScheme = darkColorScheme(
    primary = BajajOrange,
    onPrimary = Color(0xFF1A0A00),
    primaryContainer = BatikCrimson,
    onPrimaryContainer = WarmCream,
    secondary = GarudaGold,
    onSecondary = Color(0xFF1F1400),
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = GarudaGoldSoft,
    tertiary = TropicalEmerald,
    onTertiary = WarmCream,
    background = ObsidianBg,
    onBackground = WarmCream,
    surface = ObsidianSurface,
    onSurface = WarmCream,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = MutedSand,
    outline = Color(0xFF5A4338)
)

@Composable
fun BajajNusantaraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NusantaraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
