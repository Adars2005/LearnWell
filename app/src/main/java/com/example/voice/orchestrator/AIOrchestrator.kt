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
    data class RuleBasedPair(val question: String, val modelAnswer: String)

    /**
     * A deterministic POC curriculum: 10 everyday themes × 20 prompts = 200
     * question/answer pairs. It keeps a useful lesson running even when a cloud
     * model is unavailable, slow, or deliberately disabled to save credits.
     */
    val ruleBasedPocPairs: List<RuleBasedPair> by lazy {
        val themes = listOf(
            "greetings", "introducing yourself", "family", "daily routine", "food",
            "shopping", "travel", "directions", "work and study", "weekend plans"
        )
        val prompts = listOf(
            "Say hello and ask how someone is.", "Introduce yourself in one sentence.",
            "Tell me where you are from.", "Ask a simple follow-up question.",
            "Name something you like.", "Describe one thing you do every day.",
            "Make a polite request.", "Ask for help politely.", "Say thank you naturally.",
            "Respond positively to good news.", "Ask what something costs.",
            "Say what you would like to order.", "Ask where a place is.",
            "Give a short direction.", "Describe your plan for tomorrow.",
            "Talk about something you did yesterday.", "Share a preference using because.",
            "Ask someone to repeat slowly.", "Correct yourself politely.", "Close the conversation warmly."
        )
        val modelAnswers = listOf(
            "Hello! How are you?", "Hello, my name is Alex.", "I am from India.",
            "And what about you?", "I like learning languages.", "I practise for ten minutes every day.",
            "Could you please help me?", "Could you show me, please?", "Thank you very much.",
            "That is wonderful news!", "How much does this cost?", "I would like tea, please.",
            "Where is the station, please?", "Go straight, then turn left.", "Tomorrow I will study after work.",
            "Yesterday I practised a new phrase.", "I like it because it is useful.",
            "Could you repeat that slowly, please?", "Sorry, let me say that again.", "Thank you. See you soon!"
        )
        themes.flatMap { theme ->
            prompts.mapIndexed { index, prompt ->
                RuleBasedPair(
                    question = "${index + 1}. In a $theme conversation: $prompt",
                    modelAnswer = modelAnswers[index]
                )
            }
        }
    }

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
     * Zero-token replies for predictable social turns. Keeping these local makes the assistant
     * feel immediate and avoids spending a cloud request on greetings, thanks, and farewells.
     */
    fun localConversationReply(userUtterance: String, targetLanguage: String, turnIndex: Int = 0): String? {
        val text = userUtterance.trim().lowercase()
        val phrase = starterPhrase(targetLanguage)
        val mentionedLanguage = listOf("hindi", "spanish", "french", "japanese", "german", "italian", "korean", "mandarin", "portuguese", "arabic", "russian", "turkish", "english", "bengali", "tamil", "telugu", "marathi", "gujarati", "punjabi", "vietnamese", "indonesian", "dutch", "thai")
            .firstOrNull { text == it || text.contains("$it language") }
        if (mentionedLanguage != null && !mentionedLanguage.equals(targetLanguage, ignoreCase = true)) {
            return "We are practising $targetLanguage right now. Try this first: “$phrase”. Say it once, then tell me your name."
        }

        return when {
            text in setOf("hi", "hello", "hey", "namaste", "hola", "bonjour") ->
                "Hi, I’m Nyra. Let’s begin with one short $targetLanguage sentence: “$phrase”. Can you say it?"
            text in setOf("thank you", "thanks", "thankyou", "shukriya", "gracias", "merci") ->
                "You’re welcome. Would you like one more example or a quick speaking prompt?"
            text in setOf("help", "what can you do") ->
                "I can run a guided conversation, teach a phrase, correct a sentence, or role-play a real situation. Say a full sentence and I’ll coach you."
            else -> {
                val pair = ruleBasedPocPairs[turnIndex.mod(ruleBasedPocPairs.size)]
                val encouragement = when (turnIndex.mod(4)) {
                    0 -> "Good start."
                    1 -> "Nice effort."
                    2 -> "That is a useful answer."
                    else -> "You are building a real conversation."
                }
                "$encouragement ${pair.question} A model answer is: “${pair.modelAnswer}”. Now say your own version in $targetLanguage."
            }
        }
    }

    private fun starterPhrase(language: String): String = when (language.lowercase()) {
        "hindi" -> "Namaste, mera naam ___ hai."
        "spanish" -> "Hola, me llamo ___."
        "french" -> "Bonjour, je m'appelle ___."
        "japanese" -> "Konnichiwa, watashi wa ___ desu."
        "german" -> "Hallo, ich heiße ___."
        "italian" -> "Ciao, mi chiamo ___."
        "korean" -> "Annyeonghaseyo, jeoneun ___ imnida."
        "mandarin" -> "Ni hao, wo jiao ___."
        "portuguese" -> "Olá, eu me chamo ___."
        "arabic" -> "Marhaban, ismi ___."
        "russian" -> "Privet, menya zovut ___."
        "turkish" -> "Merhaba, benim adım ___."
        "bengali" -> "Nomoshkar, amar naam ___."
        "tamil" -> "Vanakkam, en peyar ___."
        "telugu" -> "Namaskaram, naa peru ___."
        "marathi" -> "Namaskar, maajhe naav ___ aahe."
        "gujarati" -> "Namaste, maru naam ___ chhe."
        "punjabi" -> "Sat sri akal, mera naam ___ hai."
        "vietnamese" -> "Xin chào, tôi tên là ___."
        "indonesian" -> "Halo, nama saya ___."
        "dutch" -> "Hallo, ik heet ___."
        "thai" -> "Sawasdee, chan chue ___."
        else -> "Hello, my name is ___."
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
            You are Nyra, a warm human-like $targetLanguage tutor for a ${profile.level} learner.
            Reply naturally in 1-3 short sentences (under 55 words), use $targetLanguage when helpful, gently correct only when useful, then invite the learner to speak. Never lecture.
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
