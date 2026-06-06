package com.msa.lagents.ui.theme.core

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class LagentsDimensions(
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val paddingExtraLarge: Dp = 32.dp,
    val spacingSmall: Dp = 4.dp,
    val spacingMedium: Dp = 12.dp,
    val spacingLarge: Dp = 20.dp,
    val cardCornerRadius: Dp = 12.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val minTouchTarget: Dp = 48.dp
)

val LocalDimensions = staticCompositionLocalOf { LagentsDimensions() }
