package com.msa.lagents.data.settings

data class AppSettings(
    val themePreference: ThemePreference = ThemePreference.System,
    val dynamicColorEnabled: Boolean = false,
    val privacyMode: PrivacyMode = PrivacyMode.Standard,
    val sensitiveTextRedactionEnabled: Boolean = true,
    val requireApprovalForSideEffects: Boolean = true,
    val routingPreference: RoutingPreference = RoutingPreference.Balanced,
    val budgetWarningsEnabled: Boolean = true,
    val monthlyBudgetCents: Int = 5_000,
    val voiceInputMode: VoiceInputMode = VoiceInputMode.PushToTalk,
    val autoReadAssistantResponses: Boolean = false,
    val transcriptRetention: TranscriptRetention = TranscriptRetention.SessionOnly,
)

enum class ThemePreference {
    System,
    Light,
    Dark,
}

enum class PrivacyMode {
    Standard,
    LocalOnly,
}

enum class RoutingPreference {
    Balanced,
    PrivacyFirst,
    CostFirst,
    CapabilityFirst,
}

enum class VoiceInputMode {
    PushToTalk,
    ContinuousDictation,
}

enum class TranscriptRetention {
    Never,
    SessionOnly,
    ConversationHistory,
}
