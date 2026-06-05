# Lagents AI Agents System Plan

Last updated: 2026-06-05

## Summary

Lagents will evolve from a minimal Jetpack Compose starter app into a polished Android AI agents application. The app will support direct bring-your-own-key cloud LLM providers, on-device local LLMs, tool-using chat, autonomous workflows, reusable prompts, reusable skills, configurable tools, private local knowledge/RAG, speech-to-text, text-to-speech, privacy and cost controls, debug tracing, and refined day/night theming.

The application must not use Hilt or another generated dependency injection framework. Dependencies are wired manually through an application-level container.

## Reference Sources

- Android app architecture: https://developer.android.com/topic/architecture
- Compose architecture and unidirectional data flow: https://developer.android.com/develop/ui/compose/architecture
- Firebase AI Logic for Gemini on Android: https://firebase.google.com/docs/ai-logic
- MediaPipe LLM Inference on Android: https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference/android
- Google AI Edge Gallery: https://github.com/google-ai-edge/gallery
- llama.cpp Android guidance: https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- OpenAI function/tool calling: https://developers.openai.com/api/docs/guides/function-calling
- Anthropic streaming and tool use: https://platform.claude.com/docs/en/build-with-claude/streaming
- Mistral AI API documentation: https://docs.mistral.ai/

## Product Goals

- Provide one Android app where users can chat with agents, create reusable agent assets, run supervised autonomous workflows, and choose cloud or local models.
- Make advanced agent behavior inspectable through debug traces, citations, tool approval records, and replay where feasible.
- Keep user control central: explicit API keys, local-only mode, side-effect confirmations, retention controls, budgets, and clear microphone/playback indicators.
- Make the UI polished enough for daily use on phones, tablets, foldables, light theme, and dark theme.

## Architecture

Use a layered Android architecture:

- Compose UI screens render immutable UI state and emit user events.
- ViewModels expose `StateFlow` state and call domain use cases.
- Domain use cases coordinate agent runtime, provider routing, tools, workflows, memory, voice, and persistence.
- Repositories hide Room, DataStore, file storage, provider APIs, local engines, speech services, and Android intent integrations.
- Data sources implement network, database, file, Android platform, and local inference details.

Manual dependency wiring:

- `LagentsApplication` owns a single `AppContainer`.
- `AppContainer` creates shared clients, database, repositories, use cases, provider adapters, local engines, speech services, registries, and ViewModel factories.
- Feature-specific containers may be added when they reduce constructor noise, but they remain explicit Kotlin classes.
- Tests replace dependencies with fakes through container constructors or factory parameters.

Core dependencies to add as needed:

- Navigation Compose
- Room
- DataStore
- WorkManager
- Kotlin serialization
- OkHttp and Server-Sent Events support
- Firebase AI Logic for Gemini
- MediaPipe LLM Inference
- Android speech recognition APIs
- Android `TextToSpeech`
- Optional native module for llama.cpp/GGUF support

## Provider System

Implement direct cloud providers using user-supplied API keys:

- OpenAI
- Anthropic
- Gemini
- Mistral

Normalize all model providers behind shared domain interfaces:

- `ChatModelClient`
- `ModelDescriptor`
- `GenerationRequest`
- `GenerationEvent`
- `ToolCallDelta`
- `UsageEstimate`

Provider requirements:

- Streaming-first responses.
- Provider-specific SSE and tool-call events converted into one app-level event stream.
- Cancellation support.
- Error normalization for authentication, rate limits, content filtering, network failures, malformed tool calls, and provider unavailability.
- Token and cost estimation where provider metadata or local estimates are available.

## Local LLMs

Implement local model support behind a shared `LocalModelEngine` interface:

- MediaPipe/LiteRT adapter first for `.task` and `.litertlm` style models aligned with Google AI Edge tooling.
- llama.cpp/GGUF adapter in a separate native module for advanced local model support.

Local model manager requirements:

- Import model files through Android file picker.
- Add curated download support later with progress, storage checks, checksum validation, and clear model compatibility labels.
- Show engine, file size, model status, context length when known, quantization/type when known, storage use, and benchmark stats.
- Support cancellation, streaming tokens, context reset, warm sessions where safe, and memory-pressure error handling.

## Agent Assets

The app must provide full CRUD for Agents, Prompts, Skills, Tools, and Knowledge collections.

Prompts:

- Reusable templates with title, description, tags, variables, system/user text blocks, default model preference, and version metadata.
- Support preview, duplicate, archive, restore, and JSON import/export.

Skills:

- A skill is a reusable prompt-plus-tools bundle.
- Store instructions, required prompt templates, enabled tools, required inputs, output format, model capability requirements, memory policy, safety policy, and version metadata.
- Skills are selectable in chat, assignable to agents, and usable inside workflows.
- Skills are not arbitrary executable extensions in v1.

Tools:

- Built-in tools are registered in code and configurable through metadata records.
- Users can enable/disable tools, edit display metadata, configure permissions, and reset to defaults.
- User-created tools in v1 are declarative wrappers around approved app capabilities, not arbitrary scripts.

Agents:

- Store name, description, system behavior, default model/router rule, enabled skills, enabled tools, prompt defaults, memory policy, workflow permissions, default voice input mode, and preferred voice output profile.

## Agent Runtime

`AgentRuntime` coordinates:

- Prompt assembly.
- Skill activation.
- Model routing.
- Tool selection.
- User approval for side-effectful tool calls.
- Tool execution.
- Memory injection.
- Knowledge retrieval.
- Speech input normalization.
- Final response synthesis.
- Debug trace creation.

Initial tool categories:

- App tools: memory CRUD, prompt lookup, skill lookup, chat search, summarization, model/router controls, workflow state tools.
- Knowledge tools: search local collections, cite retrieved chunks, summarize collections, add/remove documents.
- Speech tools: transcribe current audio input, read assistant response aloud, stop playback, switch voice profile.
- Web/files: user-approved web fetch/search adapter and user-selected document/file analysis.
- Android actions: explicit intents only, such as open URL, share text/file, create email draft, create calendar draft, and post notification.

Autonomous workflows:

- Persist workflow definitions and workflow runs.
- Execute through WorkManager.
- Show progress, logs, retries, cancellation, and final status.
- Require confirmation before every side-effectful tool call.
- Keep an audit trail of approvals, denials, tool arguments, and results.

Memory:

- User-approved long-term memories.
- Optional auto-suggested memories queued for review before saving.
- Chat history and workflow logs stored by default.
- Optional voice transcript retention controlled by settings.

Model routing:

- Manual provider/model selection.
- Optional routing rules for local-only mode, privacy mode, offline mode, cost preference, tool support, context length, and multimodal capability.

## Knowledge And RAG

Add private local RAG in v1:

- Import user-selected text, markdown, PDF, and document files.
- Extract text and metadata.
- Chunk documents into searchable units.
- Store collections, documents, chunks, and embedding metadata locally.
- Use local embeddings by default when available.
- Keep provider/cloud embeddings behind an interface for later use.
- Require RAG answers to expose citations and source snippets in the debug console.

Knowledge tools must support:

- Search collection.
- Summarize collection.
- Add document.
- Remove document.
- Show source snippets.
- Explain retrieval decisions in debug traces.

## Voice Features

Speech-to-text:

- Push-to-talk microphone input in chat.
- Optional continuous dictation mode for long prompt composition.
- Interim and final transcripts displayed live before send.
- User can edit the transcript before submitting.
- Handle microphone permission, cancellation, no-speech timeout, unavailable recognizer, and offline limitations.
- Store transcript text only according to the retention setting.

Text-to-speech:

- Read assistant messages aloud on demand.
- Optional auto-read mode per chat or agent.
- Voice profile settings for language, voice, rate, pitch, and audio focus behavior.
- Playback controls for play, pause where supported, stop, replay, and skip current response.
- Stop playback automatically when recording starts or another playback starts.

Speech abstraction:

- `SpeechToTextEngine`
- `TextToSpeechEngine`
- `SpeechSession`
- `SpeechTranscriptEvent`
- `SpeechPlaybackState`

v1 uses Android platform speech recognition and Android `TextToSpeech`. Cloud STT/TTS providers may be added later behind the same interfaces.

## Privacy, Cost, And Security

Privacy controls:

- Local-only mode.
- Per-agent permissions for cloud, web, files, memory, speech, and Android intents.
- Sensitive-text redaction before cloud calls where feasible.
- Retention controls for conversations, memories, debug logs, knowledge collections, and voice transcripts.
- Clear microphone-active and playback-active indicators.

Cost controls:

- Per-provider budget limits.
- Token and cost estimates per conversation and workflow.
- Warnings before expensive models, large-context requests, or cloud fallback.

Security requirements:

- API keys encrypted at rest with Android Keystore-backed encryption.
- API keys never logged, exported, or shown in debug traces.
- JSON import/export validates schema version and previews changes before saving.
- Exports never include API keys.
- Audit logs for tool calls, workflow actions, asset imports, cloud requests, microphone sessions, TTS playback, and user approvals.

## Debug Console

The debug console must show:

- Assembled prompt.
- Selected model.
- Routing reason.
- Memories used.
- Retrieved knowledge chunks and citations.
- Tool calls and arguments.
- Tool approvals and denials.
- Speech transcript events.
- TTS playback events.
- Token and cost estimates.
- Provider errors.
- Workflow steps.

Replay support:

- Allow replay of prior runs with the same inputs and selected model where provider and local engine constraints allow.
- Preserve enough debug trace data to understand behavior without storing secrets or raw audio.

## UI And Theming

Build a complete Material 3 Compose app shell:

- Adaptive navigation with bottom bar on phones, navigation rail on tablets/foldables, and two-pane layouts where width allows.
- Primary screens: Chat, Library, Workflows, Knowledge, Models, Debug, Settings.
- Search, filter, sort, empty states, loading states, error states, and destructive-action confirmations for all CRUD surfaces.

Chat UI:

- Streaming message bubbles with markdown/code rendering.
- Model/provider chips.
- Agent, skill, prompt, model, voice, and privacy controls.
- Tool approval cards.
- Citations and attachments.
- Memory suggestions.
- Stop and regenerate actions.
- Microphone button.
- Live transcript sheet.
- TTS playback controls.
- Inline workflow progress.

Library UI:

- CRUD tabs for Agents, Prompts, Skills, and Tools.
- Dense list/detail layouts.
- Editors with sections for overview, prompts/instructions, tools, permissions, model routing, memory, voice behavior, and versions.
- JSON import/export with preview and conflict-resolution dialogs.

Knowledge and Models UI:

- Knowledge collections show document count, indexing status, last updated, storage use, and searchable citations.
- Model cards show provider/local engine, status, capability badges, context size, cost hints, storage size, and benchmark stats.
- Voice settings show recognizer status, TTS engine status, selected language/voice, rate, pitch, and test playback.

Visual design:

- Use explicit light and dark color schemes in `ui/theme`.
- Support system theme, manual light/dark override, and optional dynamic color on supported Android versions.
- Use semantic colors for success, warning, danger, provider, local, privacy, recording, and playback states.
- Use Material 3 typography with readable density.
- Do not scale font size with viewport width.
- Do not use negative letter spacing.
- Keep compact headings inside panels.
- Keep cards subtle with 8dp radius or less.
- Avoid nested cards and decorative gradients/orbs.
- Use Android-compatible Material Symbols or Lucide-style icons for navigation and actions.

Accessibility and responsiveness:

- Meet contrast targets in light and dark themes.
- Icon-only buttons have content descriptions and tooltips where supported.
- Touch targets are at least 48dp.
- Text must not overlap or truncate critical actions on small screens.
- Support font scaling, landscape, tablets, and foldables.
- Voice controls must remain operable without voice input.

## Persistence

Room entities:

- Conversations
- Messages
- Tool calls
- Tool results
- Agents
- Prompts
- Prompt versions
- Skills
- Skill versions
- Tool configs
- Memories
- Workflow definitions
- Workflow runs
- Knowledge collections
- Documents
- Chunks
- Embedding metadata
- Local models
- Provider configs
- Usage records
- Speech transcripts
- Voice profiles
- Debug traces

DataStore:

- Theme preference
- Privacy preferences
- Voice preferences
- Routing defaults
- Budget settings
- Lightweight app settings

File storage:

- Imported model files.
- Imported documents where user grants access or where app-local copies are needed.
- Exported JSON files.
- Temporary extraction and indexing artifacts.

## Testing Strategy

Required test categories:

- Unit tests for `AppContainer` wiring with fake dependencies.
- Unit tests for provider adapters with fake SSE streams.
- Unit tests for prompt rendering, variable validation, skill activation, tool permissions, JSON import/export, archive/restore, debug trace creation, and theme preference handling.
- Unit tests for STT state, transcript event merging, cancellation, permission-denied handling, and transcript retention.
- Unit tests for TTS queueing, playback state transitions, stop-on-recording behavior, voice profile persistence, and audio focus behavior.
- Unit tests for local RAG chunking, retrieval ranking, citation mapping, and privacy filtering.
- Unit tests for tool-call parsing, approval gates, cancellation, retries, memory flows, budget limits, and routing decisions.
- Room migration tests once schema versioning starts.
- WorkManager tests for workflow execution, cancellation, retry, and progress behavior.
- Compose UI tests for chat streaming, microphone/live transcript flow, TTS controls, library CRUD, tool approval cards, knowledge import/search, debug console, model selection, key setup, theme switching, and workflow status.
- Screenshot checks for light/dark themes on phone and tablet widths.
- Instrumented local-model and speech smoke tests where platform engines are available.

## Assumptions

- No Hilt or generated dependency injection framework.
- v1 uses direct API calls from Android with user-provided keys.
- No backend proxy in v1.
- v1 cloud providers are OpenAI, Anthropic, Gemini, and Mistral.
- v1 voice uses Android platform STT and TTS first.
- v1 includes local RAG first.
- Cloud RAG and cloud audio providers are future adapters only.
- v1 includes both local engine adapters, with MediaPipe implemented before llama.cpp.
- Skills are prompt-plus-tools bundles, not arbitrary executable extensions.
- Autonomous workflows are supported, but every side-effectful action requires confirmation.
- Android device actions are limited to explicit intents in v1.
- No Accessibility-based device control in v1.
- The app remains Android-only Kotlin/Compose for now, not Kotlin Multiplatform.
