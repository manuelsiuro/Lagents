package com.msa.lagents.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import com.msa.lagents.data.settings.ThemePreference
import com.msa.lagents.ui.theme.core.LocalDimensions
import com.msa.lagents.ui.theme.core.LagentsDimensions

private val DarkColorScheme = darkColorScheme(
    primary = LagoonBlueLight,
    onPrimary = DarkBackground,
    secondary = CedarGreenLight,
    onSecondary = DarkBackground,
    tertiary = CopperLight,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE3E8E5),
    surface = DarkSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFFE3E8E5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC2CCC8),
    outline = DarkOutline,
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
)

private val LightColorScheme = lightColorScheme(
    primary = LagoonBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = CedarGreen,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = Copper,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = LightBackground,
    onBackground = androidx.compose.ui.graphics.Color(0xFF18201D),
    surface = LightSurface,
    onSurface = androidx.compose.ui.graphics.Color(0xFF18201D),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF46524D),
    outline = LightOutline,
    error = Danger,
)

@Composable
fun LagentsTheme(
    themePreference: ThemePreference = ThemePreference.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.System -> isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalDimensions provides LagentsDimensions()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
