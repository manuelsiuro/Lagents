package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.memory.MemoryEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity

class PromptAssembler {
    
    fun assembleSystemPrompt(
        agent: AgentEntity,
        skill: SkillEntity? = null,
        prompt: PromptEntity? = null,
        variables: Map<String, String> = emptyMap(),
        memories: List<MemoryEntity> = emptyList(),
    ): String {
        return buildString {
            append("Instructions: ${agent.systemBehavior}")
            
            if (memories.isNotEmpty()) {
                append("\n\nKnown information about the user or prior context:\n")
                memories.forEach { append("- ${it.content}\n") }
            }

            skill?.let {
                append("\n\n")
                append("Current Skill Instructions:\n")
                append(it.instructions)
            }
            
            prompt?.let {
                append("\n\n")
                append("Additional Context:\n")
                append(applyVariables(it.systemText, variables))
            }
        }
    }

    fun assembleUserText(
        prompt: PromptEntity,
        variables: Map<String, String> = emptyMap(),
    ): String {
        return applyVariables(prompt.userText, variables)
    }

    private fun applyVariables(text: String, variables: Map<String, String>): String {
        var result = text
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        return result
    }
}
