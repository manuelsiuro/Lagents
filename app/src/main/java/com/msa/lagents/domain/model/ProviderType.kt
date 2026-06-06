package com.msa.lagents.domain.model

enum class ProviderType(val id: String) {
    OpenAI("openai"),
    Anthropic("anthropic"),
    Gemini("gemini"),
    Mistral("mistral"),
    Local("local");

    companion object {
        fun fromId(id: String): ProviderType? = entries.find { it.id == id }
    }
}
