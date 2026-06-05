package com.msa.lagents.data.settings

data class AppSettings(
    val themePreference: ThemePreference = ThemePreference.System,
    val dynamicColorEnabled: Boolean = false,
)

enum class ThemePreference {
    System,
    Light,
    Dark,
}
