package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.skill.SkillEntity

class PromptAssembler {
    
    fun assembleSystemPrompt(
        agent: AgentEntity,
        skill: SkillEntity? = null,
        prompt: PromptEntity? = null,
        variables: Map<String, String> = emptyMap(),
    ): String {
        return buildString {
            append(agent.systemBehavior)
            
            skill?.let {
                append("\n\n")
                append("Skill Instructions:\n")
                append(it.instructions)
            }
            
            prompt?.let {
                append("\n\n")
                append("Additional Context:\n")
                append(applyVariables(it.systemText, variables))
            }
        }.trim()
    }

    fun assembleUserText(
        prompt: PromptEntity,
        variables: Map<String, String>,
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
