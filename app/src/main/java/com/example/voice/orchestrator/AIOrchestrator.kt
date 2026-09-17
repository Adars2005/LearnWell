package com.example.voice.orchestrator

import com.example.voice.engine.InterviewState
import com.example.voice.engine.LearningEngine
import com.example.voice.model.ChatMessage
import com.example.voice.model.CompactLearnerProfile
import com.example.voice.model.InterviewType
import com.example.voice.model.RoleplayScenario
import com.example.voice.model.VoiceMode
import com.example.voice.provider.ProviderConfig
import com.example.voice.provider.VoiceProvider

sealed class LocalCommandResult {
    data class Handled(val message: String, val action: LocalAction) : LocalCommandResult()
    object NotACommand : LocalCommandResult()
}

enum class LocalAction {
    STOP,
    REPEAT,
    NEXT_QUESTION,
    END_SESSION,
    SPEAK_SLOWER
}

class AIOrchestrator(
    val learningEngine: LearningEngine = LearningEngine()
) {

    /**
     * Deterministic local command detection to prevent wasteful token usage.
     */
    fun checkLocalCommand(userUtterance: String, interviewState: InterviewState? = null): LocalCommandResult {
        val trimmed = userUtterance.trim().lowercase()
        return when {
            trimmed in setOf("stop", "pause", "be quiet", "stop speaking") -> {
                LocalCommandResult.Handled("Playback paused.", LocalAction.STOP)
            }
            trimmed in setOf("repeat", "say that again", "can you repeat that", "repeat please") -> {
                LocalCommandResult.Handled("Repeating the previous statement.", LocalAction.REPEAT)
            }
            trimmed in setOf("next question", "skip question", "next") -> {
                LocalCommandResult.Handled("Moving to the next question.", LocalAction.NEXT_QUESTION)
            }
            trimmed in setOf("end session", "exit", "finish session", "bye", "goodbye") -> {
                LocalCommandResult.Handled("Finishing session and preparing your evaluation.", LocalAction.END_SESSION)
            }
            trimmed in setOf("speak slower", "slower please", "too fast") -> {
                LocalCommandResult.Handled("I will speak more slowly now.", LocalAction.SPEAK_SLOWER)
            }
            else -> LocalCommandResult.NotACommand
        }
    }

    /**
     * Modular prompt builder.
     * Avoids giant monolithic prompts; builds strictly tailored instructions.
     */
    fun buildSystemPrompt(
        mode: VoiceMode,
        targetLanguage: String,
        profile: CompactLearnerProfile,
        interviewType: InterviewType? = null,
        roleplayScenario: RoleplayScenario? = null,
        conversationSummary: String? = null
    ): String {
        val basePrompt = """
            You are Maya, a warm, supportive, and exceptionally encouraging language teacher with a cheerful, humanlike personality.
            Target Language: $targetLanguage.
            Learner Level: ${profile.level}.
            Native Language: ${profile.nativeLanguage}.
            
            Core Teacher Persona Directives:
            - Respond in 1 to 2 short, natural conversational sentences (maximum 25 words). Fast, crisp, lively exchange.
            - Sound warm, caring, authentic, and humanlike. Use gentle enthusiasm, light humor, or warm conversational reactions ("Ah, that makes sense!", "Oh wonderful!", "I love that!").
            - Always finish with an inviting question or prompt so the student has an effortless turn to speak.
            - Never deliver long lectures or multi-paragraph speeches.
        """.trimIndent()

        val modePrompt = when (mode) {
            VoiceMode.FRIEND -> """
                [Mode: Friend]
                Goal: Casual, natural speaking practice.
                - Be friendly, enthusiastic, and conversational.
                - Ask follow-up questions about the learner's day, hobbies, and interests.
                - Do NOT correct minor errors or interrupt the conversational flow.
            """.trimIndent()

            VoiceMode.TUTOR -> """
                [Mode: Tutor]
                Goal: Active language learning and gentle guidance.
                - If the learner makes a notable grammatical error, briefly provide the natural correction.
                - Ask the learner to produce an example using the corrected structure.
                - Explain unfamiliar words simply.
            """.trimIndent()

            VoiceMode.SPEAKING_PRACTICE -> """
                [Mode: Speaking Practice]
                Goal: Boost learner fluency and confidence.
                - Give engaging topics or prompts and let the learner speak freely.
                - Do NOT interrupt while they are speaking.
                - Respond to their content naturally and ask thought-provoking follow-ups.
            """.trimIndent()

            VoiceMode.INTERVIEW -> """
                [Mode: Interview Simulation - ${interviewType?.title ?: "Professional"}]
                Goal: Conduct a realistic, high-standard job interview.
                - Act as an empathetic but professional ${interviewType?.role ?: "Interviewer"}.
                - Ask strictly ONE question at a time.
                - Acknowledge their response briefly, then ask a relevant follow-up or next interview question.
                - Do NOT evaluate them or reveal scores during the interview.
            """.trimIndent()

            VoiceMode.ROLEPLAY -> """
                [Mode: Roleplay - ${roleplayScenario?.title ?: "Scenario"}]
                Goal: Immersive everyday scenario.
                - Setting: ${roleplayScenario?.setting}.
                - Your Role: ${roleplayScenario?.partnerRole}.
                - Stay in character completely. Keep interactions authentic to real-life situations.
            """.trimIndent()
        }

        val memoryContext = if (!conversationSummary.isNullOrBlank()) {
            "\n[Context Summary]: $conversationSummary\n"
        } else {
            ""
        }

        val learnerWeaknesses = if (profile.weakGrammar.isNotEmpty()) {
            "\n[Known Learner Focus Areas]: Gently help with ${profile.weakGrammar.joinToString(", ")} if relevant."
        } else {
            ""
        }

        return "$basePrompt\n$modePrompt$memoryContext$learnerWeaknesses"
    }

    fun createProviderConfig(
        mode: VoiceMode,
        targetLanguage: String,
        profile: CompactLearnerProfile,
        interviewType: InterviewType? = null,
        roleplayScenario: RoleplayScenario? = null,
        conversationSummary: String? = null
    ): ProviderConfig {
        val prompt = buildSystemPrompt(mode, targetLanguage, profile, interviewType, roleplayScenario, conversationSummary)
        return ProviderConfig(
            systemPrompt = prompt,
            targetLanguage = targetLanguage,
            learnerLevel = profile.level
        )
    }

    /**
     * Level 3: Compressed conversation summary.
     */
    fun summarizeConversation(messages: List<ChatMessage>): String {
        if (messages.isEmpty()) return ""
        val userPoints = messages.filter { it.role == "user" }.map { it.text }
        val sample = userPoints.takeLast(4).joinToString("; ")
        return "Learner discussed: $sample."
    }
}
