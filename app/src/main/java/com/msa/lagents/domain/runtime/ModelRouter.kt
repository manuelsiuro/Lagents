package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.provider.ProviderRepository
import com.msa.lagents.data.settings.AppSettings
import com.msa.lagents.data.settings.PrivacyMode
import com.msa.lagents.domain.model.ModelDescriptor
import kotlinx.coroutines.flow.first

class ModelRouter(
    private val providerRepository: ProviderRepository,
) {
    suspend fun route(
        agent: AgentEntity,
        settings: AppSettings,
    ): ModelDescriptor? {
        val allModels = providerRepository.allModels.first()

        // 1. Respect Local-only mode
        if (settings.privacyMode == PrivacyMode.LocalOnly) {
            return allModels.find { it.isLocal }
        }

        // 2. Agent explicit override
        if (agent.defaultProviderId != null && agent.defaultModelId != null) {
            val agentModel = allModels.find { 
                it.providerId == agent.defaultProviderId && it.id == agent.defaultModelId 
            }
            if (agentModel != null) return agentModel
        }

        // 3. System default fallback
        // For now, just return the first available model
        return allModels.firstOrNull()
    }
}
