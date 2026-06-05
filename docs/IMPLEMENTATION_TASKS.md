# Lagents Implementation Tasks

Last updated: 2026-06-05

Status values:

- `Not started`
- `In progress`
- `Blocked`
- `Done`

Progress rule: update this file after each implementation milestone. When a task is marked `Done`, add links or notes for the relevant code, tests, screenshots, or manual verification.

## Phase 0 - Documentation And Planning

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Create system plan | Done | `docs/AI_AGENTS_SYSTEM_PLAN.md` exists and captures product, architecture, security, UI, testing, and assumptions. | Manual review |
| Create implementation checklist | Done | `docs/IMPLEMENTATION_TASKS.md` exists with phases, task status, acceptance criteria, and test expectations. | Manual review |
| Keep decisions current | Not started | Major product or architecture changes are reflected in both docs before implementation continues. | Manual review |

## Phase 1 - Project Foundation

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Add core dependencies | Done | Gradle includes Navigation Compose, Room, DataStore, WorkManager, Kotlin serialization, OkHttp/SSE, speech APIs support, and test libraries. | `./gradlew :app:assembleDebug --no-daemon` passes |
| Add manual `AppContainer` | Done | `LagentsApplication` exposes one explicit container for app-wide dependencies without Hilt. | `./gradlew :app:assembleDebug --no-daemon` passes |
| Add app navigation shell | Done | App has Chat, Library, Workflows, Knowledge, Models, Debug, and Settings destinations. | `./gradlew :app:assembleDebug --no-daemon` passes |
| Add adaptive navigation | Done | Phones use bottom navigation and wider layouts use rail/two-pane structure. | `./gradlew :app:assembleDebug --no-daemon` passes; screenshots still needed |
| Add base UI components | Done | Shared buttons, chips, cards, dialogs, empty states, loading states, and error states exist. | `LagentsEmptyState`, `LagentsErrorState`, `LagentsLoadingState`, and `LagentsConfirmationDialog` build |
| Add day/night theme system | Done | Light, dark, system, and optional dynamic color settings are wired through Compose. | `./gradlew :app:assembleDebug --no-daemon` passes |
| Add DataStore settings foundation | Done | Settings repository persists theme, privacy, routing, budget, and voice preferences. | `AppSettingsRepositoryTest` passes |
| Add Room database foundation | Done | Database, DAOs, entities, and migration placeholder are created. | Room schema exported at `app/schemas/com.msa.lagents.data.local.LagentsDatabase/1.json`; build passes |

## Phase 2 - Core Domain Model

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Model conversations and messages | In progress | Conversations and messages persist, load, update, and delete correctly. | Room entities/DAO exist; DAO and repository tests still needed |
| Model agents | In progress | Agent profiles persist behavior, model routing, skills, tools, memory, voice, and workflow permissions. | Room entity/DAO exist; DAO and repository tests still needed |
| Model prompts | In progress | Prompt templates support variables, versions, tags, archive, restore, duplicate, and preview data. | Prompt and prompt-version entities/DAO exist; repository tests still needed |
| Model skills | In progress | Skills persist prompt-plus-tools bundles, required inputs, output format, memory policy, safety policy, and versions. | Skill and skill-version entities/DAO exist; repository tests still needed |
| Model tool configs | In progress | Built-in tool metadata and user configuration persist without enabling arbitrary script execution. | Tool config entity/DAO exist; tests still needed |
| Model provider configs | In progress | Provider records store display metadata and encrypted key references. | Provider config entity/DAO exist; encrypted key storage still separate |
| Model memories | In progress | Memories support user-approved saves, auto-suggestion review state, search metadata, and deletion. | Memory entity/DAO exist; repository tests still needed |
| Model debug traces | In progress | Debug trace entities can store prompt, routing, memory, RAG, tool, voice, usage, and error metadata without secrets. | Debug trace entity/DAO exist; trace creation tests still needed |

## Phase 3 - Provider Adapters

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Define provider interfaces | Not started | `ChatModelClient`, `ModelDescriptor`, `GenerationRequest`, `GenerationEvent`, `ToolCallDelta`, and `UsageEstimate` are stable enough for all providers. | Interface-focused unit tests with fakes |
| Add encrypted API key storage | Not started | User API keys are encrypted with Android Keystore-backed storage and never exported or logged. | Unit/instrumented key storage tests |
| Implement OpenAI adapter | Not started | OpenAI chat, streaming, tool calls, cancellation, usage, and errors map to shared events. | Fake SSE tests |
| Implement Anthropic adapter | Not started | Anthropic messages, streaming, tool use, cancellation, usage, and errors map to shared events. | Fake SSE tests |
| Implement Gemini adapter | Not started | Gemini/Firebase AI Logic calls, streaming, tool calls where supported, usage, and errors map to shared events. | Fake stream tests |
| Implement Mistral adapter | Not started | Mistral chat, streaming, tool calls, cancellation, usage, and errors map to shared events. | Fake SSE tests |
| Add provider model catalog | Not started | UI can list configured providers, available model descriptors, capabilities, and cost hints. | Repository and UI tests |
| Add provider error normalization | Not started | Auth, rate limit, safety, network, timeout, malformed response, and unavailable errors have app-level types. | Adapter unit tests |

## Phase 4 - Agent Assets CRUD

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Build Agents CRUD UI | In progress | Users can create, view, edit, duplicate, archive, restore, and delete agents. | Repository-backed create/list exists; edit/archive/delete and UI tests still needed |
| Build Prompts CRUD UI | In progress | Users can manage prompt templates, variables, versions, preview, archive, restore, and duplicate. | Repository-backed create/list exists; edit/archive/delete and tests still needed |
| Build Skills CRUD UI | In progress | Users can manage prompt-plus-tools skills, inputs, output formats, permissions, and versions. | Repository-backed create/list exists; edit/archive/delete and tests still needed |
| Build Tools CRUD/config UI | In progress | Users can configure built-in tools, permissions, metadata, enablement, and reset to defaults. | Repository-backed create/list exists; edit/delete/reset and tests still needed |
| Add JSON import/export | Not started | Prompts, skills, agents, and allowed tool metadata import/export with schema validation and preview. | Import/export unit tests |
| Add conflict resolution | Not started | Imports can preview conflicts and choose create, replace, skip, or duplicate where applicable. | Import UI tests |

## Phase 5 - Agent Runtime

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Implement prompt assembly | Not started | Agent, skill, prompt, memory, RAG context, user input, and tool schemas assemble deterministically. | Prompt assembly tests |
| Implement skill activation | Not started | Skills can be selected manually or activated by runtime rules without bypassing permissions. | Skill activation tests |
| Implement model router | Not started | Manual selection and routing rules handle local-only, offline, cost, privacy, tools, context length, and capability needs. | Router unit tests |
| Implement tool registry | Not started | Built-in tools register schemas, permissions, side-effect flags, and executors. | Tool registry tests |
| Implement tool approval flow | Not started | Side-effectful tools pause execution until user confirms or denies. | Runtime and UI tests |
| Implement tool execution loop | Not started | Runtime handles streamed tool calls, partial args, execution results, retries, cancellation, and final synthesis. | Runtime unit tests |
| Implement memory injection | Not started | Approved memories and reviewed auto-suggestions flow into runtime according to policy. | Memory/runtime tests |
| Implement debug trace creation | Not started | Every agent run can record prompt, routing, tools, RAG, memory, voice, usage, and errors safely. | Trace tests |

## Phase 6 - Chat Experience

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Build chat screen | Not started | Chat supports streaming messages, stop, regenerate, attachments, provider/model chips, agent/skill/prompt pickers, and privacy controls. | Compose UI tests |
| Add markdown/code rendering | Not started | Assistant responses render readable markdown and code without breaking layout. | Screenshot checks |
| Add tool approval cards | Not started | Tool calls show arguments, risk, approve/deny actions, and result status inline. | Compose UI tests |
| Add citations UI | Not started | RAG and document answers show source snippets and document references. | Compose UI tests |
| Add memory suggestions UI | Not started | Auto-suggested memories can be accepted, edited, or rejected. | Compose UI tests |
| Add inline workflow progress | Not started | Long-running workflow status appears in chat and links to workflow details. | Compose UI tests |

## Phase 7 - Local AI

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Define `LocalModelEngine` | Not started | Local engine interface supports load, unload, generate, stream, cancel, reset context, metadata, and errors. | Fake engine tests |
| Implement MediaPipe adapter | Not started | MediaPipe/LiteRT-compatible model inference works behind `LocalModelEngine`. | Device smoke test with tiny model |
| Add llama.cpp native module | Not started | GGUF inference is isolated in an optional module and reachable through `LocalModelEngine`. | Native build and smoke test |
| Build local model manager | Not started | Users can import, view, validate, load, unload, benchmark, and delete local models. | Compose UI tests |
| Add curated download flow | Not started | Compatible models can be downloaded with progress, storage checks, checksum validation, and cancellation. | Repository and UI tests |
| Add local model benchmarks | Not started | App records basic load time, token speed, memory status, and device notes. | Benchmark repository tests |

## Phase 8 - Knowledge And RAG

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Build knowledge collection CRUD | Not started | Users can create, edit, delete, search, and inspect collections. | Compose UI tests |
| Add document import | Not started | Users can import text, markdown, PDF, and document files with progress and error states. | Instrumented import tests |
| Add text extraction | Not started | Supported files extract text and metadata without blocking the UI. | Unit/instrumented tests |
| Add chunking pipeline | Not started | Documents chunk consistently with stored offsets and source metadata. | Chunking unit tests |
| Define embeddings interface | Not started | Embedding generation can use local default first and provider embeddings later. | Fake embedding tests |
| Implement local retrieval | Not started | Collection search returns ranked chunks with citations and metadata. | Retrieval tests |
| Add RAG tools | Not started | Agent tools can search, cite, summarize, add, and remove knowledge documents. | Tool/runtime tests |

## Phase 9 - Workflows

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Model workflow definitions | Not started | Workflows persist name, goal, agent, skill/tool permissions, schedule/manual trigger metadata, and safety policy. | DAO tests |
| Model workflow runs | Not started | Runs persist status, progress, logs, tool calls, approvals, retries, errors, and final output. | DAO tests |
| Implement WorkManager execution | Not started | Workflow runs execute in background with progress and cancellation support. | WorkManager tests |
| Add workflow approval gates | Not started | Side-effectful actions pause until user approval. | Runtime tests |
| Build Workflows UI | Not started | Users can start, inspect, cancel, retry, and review workflow runs. | Compose UI tests |
| Add workflow notifications | Not started | Long-running workflows expose status and approval-needed notifications where appropriate. | Instrumented notification checks |

## Phase 10 - Voice

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Define speech interfaces | Not started | STT and TTS abstractions expose session state, transcript events, playback state, errors, and cancellation. | Fake speech tests |
| Implement speech-to-text | Not started | Push-to-talk and continuous dictation use Android speech recognition with permission handling and editable transcripts. | Unit and device smoke tests |
| Implement text-to-speech | Not started | Assistant messages can be read aloud with play/stop/replay and voice profile settings. | Unit and device smoke tests |
| Add voice profiles | Not started | Users can choose language, voice, rate, pitch, audio focus behavior, and auto-read defaults. | Repository and UI tests |
| Add chat voice controls | Not started | Chat has microphone, live transcript sheet, TTS controls, and clear active indicators. | Compose UI tests |
| Add transcript retention settings | Not started | Users can control whether transcript text is saved and can delete retained transcripts. | Repository tests |

## Phase 11 - Privacy, Cost, And Debug

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Add local-only mode | Not started | Cloud calls, cloud fallback, and web tools are blocked when local-only mode is active. | Router and UI tests |
| Add per-agent permissions | Not started | Agents enforce permissions for cloud, web, files, memory, speech, Android intents, and side-effectful tools. | Permission tests |
| Add retention controls | Not started | Users can configure and apply retention for conversations, memories, traces, knowledge, and transcripts. | Repository tests |
| Add budget controls | Not started | Provider budgets and warnings block or warn before configured thresholds are exceeded. | Budget tests |
| Add redaction pipeline | Not started | Sensitive-text redaction can run before cloud calls where feasible and is visible in debug traces. | Redaction tests |
| Build debug console | Not started | Users can inspect prompt, routing, memory, RAG, tools, voice, usage, errors, and workflow steps. | Compose UI tests |
| Add replay support | Not started | Eligible prior runs can be replayed with the same inputs and selected model. | Runtime tests |
| Add audit logs | Not started | Tool calls, workflow actions, imports, cloud requests, microphone sessions, TTS playback, and approvals are recorded. | Audit repository tests |

## Phase 12 - Polished UI And Release Readiness

| Task | Status | Done when | Tests or checks |
| --- | --- | --- | --- |
| Polish visual system | Not started | Light/dark themes, semantic colors, typography, icons, density, spacing, and states feel consistent across screens. | Screenshot review |
| Add accessibility pass | Not started | Content descriptions, touch targets, contrast, font scaling, and no-voice fallback behavior are verified. | Accessibility checks |
| Add responsive pass | Not started | Phone, landscape, tablet, and foldable layouts avoid overlap and critical truncation. | Screenshot checks |
| Add error-state pass | Not started | Network, provider, local model, RAG, workflow, speech, storage, and permission errors have useful UI states. | Compose UI tests |
| Add migration tests | Not started | Room migrations are tested for every schema version after initial schema stabilizes. | Migration tests |
| Add release smoke checklist | Not started | Fresh install, key setup, local model import, chat, tool approval, RAG, workflow, STT, TTS, dark mode, and export are manually verified. | Manual QA checklist |

## Current Next Step

Continue with tests and Phase 2 domain modeling:

1. Add focused tests for `AppContainer`, Room DAOs, and `LibraryRepository`.
2. Expand Library CRUD from create/list to edit, archive, restore, duplicate, and delete.
3. Add form dialogs for agents, prompts, skills, and tool configs.
4. Keep `./gradlew :app:assembleDebug --no-daemon` and `./gradlew :app:testDebugUnitTest --no-daemon` green after each slice.
