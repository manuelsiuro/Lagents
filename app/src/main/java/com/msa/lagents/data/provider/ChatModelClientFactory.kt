package com.msa.lagents.data.provider

import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.data.provider.anthropic.AnthropicChatModelClient
import com.msa.lagents.data.provider.gemini.GeminiChatModelClient
import com.msa.lagents.data.provider.mistral.MistralChatModelClient
import com.msa.lagents.data.provider.openai.OpenAiChatModelClient
import com.msa.lagents.domain.model.ProviderType
import com.msa.lagents.domain.provider.ChatModelClient
import okhttp3.OkHttpClient

class ChatModelClientFactory(
    private val httpClient: OkHttpClient,
    private val apiKeyStorage: ApiKeyStorage,
) {
    fun create(config: ProviderConfigEntity): ChatModelClient? {
        val type = ProviderType.fromId(config.providerType) ?: return null
        val apiKey = config.apiKeyAlias?.let { apiKeyStorage.getApiKey(it) } ?: ""
        
        return when (type) {
            ProviderType.OpenAI -> OpenAiChatModelClient(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl ?: "https://api.openai.com/v1"
            )
            ProviderType.Anthropic -> AnthropicChatModelClient(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl ?: "https://api.anthropic.com/v1"
            )
            ProviderType.Gemini -> GeminiChatModelClient(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl ?: "https://generativelanguage.googleapis.com/v1beta"
            )
            ProviderType.Mistral -> MistralChatModelClient(
                httpClient = httpClient,
                apiKey = apiKey,
                baseUrl = config.baseUrl ?: "https://api.mistral.ai/v1"
            )
            ProviderType.Local -> null // Handled separately via LocalModelEngine
        }
    }
}
