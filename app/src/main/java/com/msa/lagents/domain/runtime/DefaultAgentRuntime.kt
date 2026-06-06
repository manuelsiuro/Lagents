package com.msa.lagents.domain.runtime

import com.msa.lagents.data.local.agent.AgentDao
import com.msa.lagents.data.local.conversation.ConversationDao
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.data.local.debug.DebugTraceDao
import com.msa.lagents.data.local.debug.DebugTraceEntity
import com.msa.lagents.data.local.memory.MemoryDao
import com.msa.lagents.data.local.provider.ProviderConfigDao
import com.msa.lagents.data.provider.ChatModelClientFactory
import com.msa.lagents.data.settings.AppSettingsRepository
import com.msa.lagents.domain.local.LocalModelEngine
import com.msa.lagents.domain.model.ChatMessage
import com.msa.lagents.domain.model.GenerationEvent
import com.msa.lagents.domain.model.GenerationRequest
import com.msa.lagents.domain.model.MessageRole
import com.msa.lagents.domain.model.ProviderError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class DefaultAgentRuntime(
    private val agentDao: AgentDao,
    private val conversationDao: ConversationDao,
    private val providerConfigDao: ProviderConfigDao,
    private val memoryDao: MemoryDao,
    private val debugTraceDao: DebugTraceDao,
    private val clientFactory: ChatModelClientFactory,
    private val appSettingsRepository: AppSettingsRepository,
    private val skillActivator: SkillActivator,
    private val modelRouter: ModelRouter,
    private val promptAssembler: PromptAssembler,
    private val localModelEngine: LocalModelEngine,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : AgentRuntime {

    private val approvalChannel = Channel<Boolean>()

    override suspend fun provideApproval(approved: Boolean) {
        approvalChannel.send(approved)
    }

    override fun run(
        agentId: String,
        conversationId: String,
        userInput: String,
        variables: Map<String, String>,
    ): Flow<GenerationEvent> = flow {
        // 1. Load Agent
        val agent = agentDao.observeAgent(agentId).first()
            ?: throw Exception("Agent not found: $agentId")

        // 2. Resolve Assets (Skills, Prompts, Tools)
        val assets = skillActivator.resolveAgentAssets(agent)
        
        // 3. Resolve Model (Routing)
        val settings = appSettingsRepository.settings.first()
        val model = modelRouter.route(agent, settings)
            ?: throw Exception("No suitable model found for agent")

        // 4. Resolve Client
        val client = if (model.isLocal) {
            null // Use localModelEngine
        } else {
            val providerConfig = providerConfigDao.observeProviderConfig(model.providerId).first()
                ?: throw Exception("Provider configuration not found: ${model.providerId}")
            clientFactory.create(providerConfig)
                ?: throw Exception("Could not create client for provider: ${model.providerId}")
        }

        // 5. Load History and Memories
        val history = conversationDao.observeMessages(conversationId).first()
        val memories = memoryDao.observeMemoriesByReviewStatus("Approved").first()
        
        // 6. Assemble Prompt
        val systemPrompt = promptAssembler.assembleSystemPrompt(
            agent = agent,
            skill = assets.skill,
            prompt = assets.prompts.firstOrNull(),
            variables = variables,
            memories = memories
        )
        
        val userText = assets.prompts.firstOrNull()?.let {
            promptAssembler.assembleUserText(it, variables)
        } ?: userInput

        // 7. Save User Message
        val userMessageId = idGenerator()
        conversationDao.upsertMessage(
            MessageEntity(
                id = userMessageId,
                conversationId = conversationId,
                role = "user",
                content = userText,
                createdAtMillis = nowMillis()
            )
        )

        // 8. Prepare Request
        val requestMessages = buildList {
            history.forEach { 
                add(ChatMessage(role = it.role.toMessageRole(), content = it.content))
            }
            add(ChatMessage(role = MessageRole.User, content = userText))
        }

        val request = GenerationRequest(
            modelId = model.id,
            messages = requestMessages,
            systemPrompt = systemPrompt,
            tools = assets.tools.map { it.definition }
        )

        // 9. Execute Loop
        val messagesForLoop = requestMessages.toMutableList()
        var shouldLoop = true
        
        val traceId = idGenerator()
        val toolCalls = mutableListOf<String>()
        val events = mutableListOf<String>()
        var finalUsage: GenerationEvent.Usage? = null
        var finalError: String? = null

        while (shouldLoop) {
            val loopRequest = request.copy(messages = messagesForLoop)
            val currentResponse = StringBuilder()
            val currentToolCalls = mutableMapOf<Int, ToolCallState>()

            val eventFlow = if (model.isLocal) {
                localModelEngine.generate(loopRequest)
            } else {
                client?.generate(loopRequest) ?: throw Exception("Client is null for non-local model")
            }

            eventFlow.collect { event ->
                events.add(Json.encodeToString(event))
                when (event) {
                    is GenerationEvent.TextDelta -> {
                        currentResponse.append(event.delta)
                        emit(event)
                    }
                    is GenerationEvent.ToolCallDelta -> {
                        val state = currentToolCalls.getOrPut(event.index) { ToolCallState() }
                        event.id?.let { state.id = it }
                        event.name?.let { state.name = it }
                        event.argumentsDelta?.let { state.arguments.append(it) }
                        emit(event)
                    }
                    is GenerationEvent.ToolApprovalRequest -> emit(event)
                    is GenerationEvent.Citation -> emit(event)
                    is GenerationEvent.MemorySuggestion -> emit(event)
                    is GenerationEvent.WorkflowProgress -> emit(event)
                    is GenerationEvent.Usage -> {
                        finalUsage = event
                        emit(event)
                    }
                    is GenerationEvent.Error -> {
                        finalError = event.error.message
                        emit(event)
                    }
                    is GenerationEvent.Finished -> {
                        // Loop control
                        shouldLoop = false 

                        // Save Assistant Response
                        if (currentResponse.isNotEmpty()) {
                            val content = currentResponse.toString()
                            conversationDao.upsertMessage(
                                MessageEntity(
                                    id = idGenerator(),
                                    conversationId = conversationId,
                                    role = "assistant",
                                    content = content,
                                    createdAtMillis = nowMillis()
                                )
                            )
                            messagesForLoop.add(ChatMessage(role = MessageRole.Assistant, content = content))
                        }
                        
                        // Handle Tool Calls
                        if (currentToolCalls.isNotEmpty()) {
                            shouldLoop = true // Loop back for tool results
                            
                            val callStates = currentToolCalls.values.toList()
                            
                            callStates.forEach { call ->
                                val toolName = call.name ?: return@forEach
                                val tool = assets.tools.find { it.definition.name == toolName }
                                
                                val approved = if (tool?.requiresApproval == true || tool?.isSideEffectful == true) {
                                    emit(GenerationEvent.ToolApprovalRequest(
                                        callId = call.id ?: "unknown",
                                        toolName = toolName,
                                        argumentsJson = call.arguments.toString()
                                    ))
                                    approvalChannel.receive()
                                } else true

                                val result = if (!approved) {
                                    "Error: User denied execution of tool: $toolName"
                                } else if (tool != null) {
                                    try {
                                        tool.executor.execute(
                                            call.arguments.toString(),
                                            ToolContext(emitEvent = { e -> emit(e) })
                                        )
                                    } catch (e: Exception) {
                                        "Error executing tool: ${e.message}"
                                    }
                                } else {
                                    "Error: Tool not found: $toolName"
                                }
                                
                                toolCalls.add(Json.encodeToString(mapOf("tool" to toolName, "args" to call.arguments.toString(), "result" to result)))

                                // Save Tool Result
                                conversationDao.upsertMessage(
                                    MessageEntity(
                                        id = idGenerator(),
                                        conversationId = conversationId,
                                        role = "tool",
                                        content = result,
                                        createdAtMillis = nowMillis()
                                    )
                                )
                                messagesForLoop.add(
                                    ChatMessage(
                                        role = MessageRole.Tool,
                                        content = result,
                                        toolResultId = call.id
                                    )
                                )
                            }
                        }
                        
                        if (!shouldLoop) {
                            emit(event)
                        }
                    }
                }
            }
        }

        // 10. Record Trace
        debugTraceDao.upsertTrace(
            DebugTraceEntity(
                id = traceId,
                conversationId = conversationId,
                workflowRunId = null,
                agentId = agentId,
                modelProviderId = model.providerId,
                modelId = model.id,
                promptPreview = systemPrompt.take(500),
                routingReason = "Manual selection", // TODO: Get from router
                memoriesJson = "[]", // TODO
                toolCallsJson = Json.encodeToString(toolCalls),
                knowledgeJson = "[]", // TODO
                voiceJson = "{}", // TODO
                usageJson = Json.encodeToString(finalUsage),
                errorJson = finalError,
                createdAtMillis = nowMillis()
            )
        )
    }

    private data class ToolCallState(
        var id: String? = null,
        var name: String? = null,
        val arguments: StringBuilder = StringBuilder()
    )

    private fun String.toMessageRole() = when (this) {
        "system" -> MessageRole.System
        "user" -> MessageRole.User
        "assistant" -> MessageRole.Assistant
        "tool" -> MessageRole.Tool
        else -> MessageRole.User
    }
}
