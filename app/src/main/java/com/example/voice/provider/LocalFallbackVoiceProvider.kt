package com.example.voice.provider

import com.example.util.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Offline and local fallback voice provider.
 * Uses local TTS and pre-configured conversational heuristics.
 * Serves as a prototype and zero-cloud fallback.
 */
class LocalFallbackVoiceProvider(
    private val ttsManager: TtsManager? = null
) : VoiceProvider {

    override val providerName: String = "local-fallback"

    private val audioFlow = MutableSharedFlow<AudioChunk>(extraBufferCapacity = 64)
    private val transcriptFlow = MutableSharedFlow<TranscriptChunk>(extraBufferCapacity = 64)
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var isConnected = false
    private var targetLanguage = "Hindi"

    override suspend fun connect(sessionId: String, config: ProviderConfig) {
        isConnected = true
        targetLanguage = config.targetLanguage
        // Initial greeting
        scope.launch {
            val greeting = when (targetLanguage.lowercase()) {
                "hindi" -> "नमस्ते! मैं आपकी भाषा साथी हूँ। आज हम क्या अभ्यास करेंगे?"
                "spanish" -> "¡Hola! Soy tu compañera de idiomas. ¿Qué te gustaría practicar hoy?"
                "french" -> "Bonjour! Je suis votre partenaire d'apprentissage. De quoi voulez-vous parler?"
                "japanese" -> "こんにちは！一緒に練習しましょう。今日は何を話しますか？"
                "german" -> "Hallo! Ich bin dein Sprachpartner. Worüber möchtest du heute sprechen?"
                else -> "Hello! I am your language learning partner. What would you like to practice today?"
            }
            transcriptFlow.emit(TranscriptChunk(greeting, isUser = false, isFinal = true))
            ttsManager?.speak(greeting, targetLanguage)
        }
    }

    override suspend fun sendAudio(audioBytes: ByteArray) {
        // Audio received locally - simulate quick transcription
    }

    override suspend fun sendText(text: String) {
        if (!isConnected) return
        transcriptFlow.emit(TranscriptChunk(text, isUser = true, isFinal = true))

        scope.launch {
            // Friendly conversational reply
            val reply = generateLocalReply(text, targetLanguage)
            transcriptFlow.emit(TranscriptChunk(reply, isUser = false, isFinal = true))
            ttsManager?.speak(reply, targetLanguage)
        }
    }

    private fun generateLocalReply(userText: String, lang: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("namaste") || lower.contains("hola") -> {
                when (lang.lowercase()) {
                    "hindi" -> "नमस्ते! आप कैसे हैं? अपनी दिनचर्या के बारे में बताएं।"
                    "spanish" -> "¡Hola! ¿Cómo estás hoy? Cuéntame sobre tu día."
                    "french" -> "Bonjour! Comment allez-vous aujourd'hui?"
                    else -> "Hello! How are you doing today? Tell me what you've been working on."
                }
            }
            lower.contains("help") || lower.contains("grammar") -> {
                "Of course! Remember to focus on word order and verb conjugations. What sentence would you like to improve?"
            }
            lower.contains("interview") -> {
                "Great! Let's begin. Question 1: Could you please tell me about your background and strongest skills?"
            }
            else -> {
                when (lang.lowercase()) {
                    "hindi" -> "बहुत अच्छा प्रयास! क्या आप इस वाक्य को एक और उदाहरण के साथ कह सकते हैं?"
                    "spanish" -> "¡Muy bien! ¿Podrías darme otro ejemplo usando esa misma idea?"
                    "french" -> "Très bien! Pouvez-vous me donner un autre exemple avec cette idée?"
                    else -> "That is a great thought! Can you expand a bit more on that, or share another example?"
                }
            }
        }
    }

    override suspend fun interrupt() {
        ttsManager?.stop()
    }

    override fun receiveAudio(): Flow<AudioChunk> = audioFlow.asSharedFlow()

    override fun receiveTranscripts(): Flow<TranscriptChunk> = transcriptFlow.asSharedFlow()

    override suspend fun close() {
        isConnected = false
        ttsManager?.stop()
    }
}
