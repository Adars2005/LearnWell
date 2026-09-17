package com.example.voice.gateway

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.util.TtsManager
import com.example.voice.data.ConversationMessageEntity
import com.example.voice.data.ConversationSummaryEntity
import com.example.voice.data.GrammarErrorEntity
import com.example.voice.data.LearnerProfileEntity
import com.example.voice.data.SpeakingEvaluationEntity
import com.example.voice.data.VoiceSessionEntity
import com.example.voice.data.VoiceUsageLogEntity
import com.example.voice.engine.InterviewState
import com.example.voice.model.ChatMessage
import com.example.voice.model.CompactLearnerProfile
import com.example.voice.model.GrammarCorrection
import com.example.voice.model.InterviewType
import com.example.voice.model.LearnerWord
import com.example.voice.model.RoleplayScenario
import com.example.voice.model.SpeakingEvaluation
import com.example.voice.model.VoiceMode
import com.example.voice.model.VoiceSessionState
import com.example.voice.orchestrator.AIOrchestrator
import com.example.voice.orchestrator.LocalAction
import com.example.voice.orchestrator.LocalCommandResult
import com.example.voice.provider.AudioChunk
import com.example.voice.provider.GeminiLiveProvider
import com.example.voice.provider.TranscriptChunk
import com.example.voice.provider.VoiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

/**
 * Backend Voice Gateway service.
 * Manages session lifecycles, provider delegation, token budgeting,
 * rate limiting, and persistence separation between conversation history
 * and learner profile models.
 */
class VoiceGatewayService(
    private val context: Context,
    private val orchestrator: AIOrchestrator = AIOrchestrator(),
    private val ttsManager: TtsManager? = null
) {
    // Configurable thresholds
    val maxSessionDurationSeconds = 1800 // 30 minutes
    val maxAudioDurationSeconds = 60 // 60 seconds per utterance
    val maxDailySessions = 50

    private val db = AppDatabase.getDatabase(context)
    private val voiceDao = db.voiceDao()
    private val vocabDao = db.vocabDao()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Active session state
    private val _sessionState = MutableStateFlow(VoiceSessionState.IDLE)
    val sessionState: StateFlow<VoiceSessionState> = _sessionState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _latestEvaluation = MutableStateFlow<SpeakingEvaluation?>(null)
    val latestEvaluation: StateFlow<SpeakingEvaluation?> = _latestEvaluation.asStateFlow()

    private val _audioWaveform = MutableSharedFlow<Float>(extraBufferCapacity = 64)
    val audioWaveform: Flow<Float> = _audioWaveform.asSharedFlow()

    private var activeProvider: VoiceProvider = GeminiLiveProvider(ttsManager)
    private var currentSessionId: String = ""
    private var currentMode: VoiceMode = VoiceMode.FRIEND
    private var currentLanguage: String = "English"
    private var currentLevel: String = "B1"
    private var sessionStartTime: Long = 0L
    private var requestCount: Int = 0
    private var interviewState: InterviewState? = null
    private val detectedGrammarErrors = mutableListOf<GrammarCorrection>()

    /**
     * Endpoint: POST /voice/session
     * Initiates a new real-time voice session.
     */
    suspend fun startSession(
        mode: VoiceMode,
        targetLanguage: String,
        level: String,
        interviewType: InterviewType? = null,
        roleplayScenario: RoleplayScenario? = null
    ): String {
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        currentMode = mode
        currentLanguage = targetLanguage
        currentLevel = level
        sessionStartTime = System.currentTimeMillis()
        requestCount = 0
        detectedGrammarErrors.clear()
        _latestEvaluation.value = null
        _messages.value = emptyList()

        _sessionState.value = VoiceSessionState.CONNECTING

        val profile = getLearnerProfile()
            .copy(language = targetLanguage, level = level)
        val config = orchestrator.createProviderConfig(
            mode = mode,
            targetLanguage = targetLanguage,
            profile = profile,
            interviewType = interviewType,
            roleplayScenario = roleplayScenario
        )

        if (mode == VoiceMode.INTERVIEW && interviewType != null) {
            interviewState = orchestrator.learningEngine.interviewEngine.initializeInterview(interviewType)
        } else {
            interviewState = null
        }

        // Persist session record
        val entity = VoiceSessionEntity(
            sessionId = sessionId,
            mode = mode.name,
            subMode = interviewType?.name ?: roleplayScenario?.name ?: "",
            language = targetLanguage,
            level = level,
            startTime = sessionStartTime,
            provider = activeProvider.providerName
        )
        voiceDao.insertSession(entity)

        // Connect provider
        try {
            activeProvider.connect(sessionId, config)
            listenToProviderOutputs()
            _sessionState.value = VoiceSessionState.LISTENING
        } catch (e: Exception) {
            _sessionState.value = VoiceSessionState.ERROR
        }

        return sessionId
    }

    private fun listenToProviderOutputs() {
        scope.launch {
            activeProvider.receiveTranscripts().collect { chunk ->
                val role = if (chunk.isUser) "user" else "assistant"
                val chatMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = role,
                    text = chunk.text
                )
                _messages.value = _messages.value + chatMsg

                // Persist message
                voiceDao.insertMessage(
                    ConversationMessageEntity(
                        sessionId = currentSessionId,
                        role = role,
                        text = chunk.text,
                        timestamp = System.currentTimeMillis()
                    )
                )

                if (!chunk.isUser) {
                    _sessionState.value = VoiceSessionState.ASSISTANT_SPEAKING
                }
            }
        }
    }

    /**
     * Sends user speech / utterance text to the voice pipeline.
     */
    suspend fun sendUserUtterance(text: String) {
        if (text.isBlank()) return

        // 1. Token & Cost Optimization: Check local deterministic commands first
        val commandResult = orchestrator.checkLocalCommand(text, interviewState)
        if (commandResult is LocalCommandResult.Handled) {
            when (commandResult.action) {
                LocalAction.STOP -> interrupt()
                LocalAction.REPEAT -> {
                    val lastAssistant = _messages.value.lastOrNull { it.role == "assistant" }
                    if (lastAssistant != null) {
                        ttsManager?.speak(lastAssistant.text, currentLanguage)
                    }
                }
                LocalAction.NEXT_QUESTION -> {
                    if (interviewState != null) {
                        val nextQ = orchestrator.learningEngine.interviewEngine.getNextQuestion(interviewState!!, null)
                        if (nextQ != null) {
                            ttsManager?.speak(nextQ, currentLanguage)
                            _messages.value = _messages.value + ChatMessage(UUID.randomUUID().toString(), "assistant", nextQ)
                        } else {
                            endSession()
                        }
                    }
                }
                LocalAction.END_SESSION -> endSession()
                LocalAction.SPEAK_SLOWER -> {
                    val lastAssistant = _messages.value.lastOrNull { it.role == "assistant" }
                    if (lastAssistant != null) {
                        ttsManager?.speak(lastAssistant.text, currentLanguage, isSlow = true)
                    }
                }
            }
            return
        }

        // 2. Grammar check according to mode
        val correction = orchestrator.learningEngine.checkGrammar(text, currentMode)
        if (correction != null) {
            detectedGrammarErrors.add(correction)
            voiceDao.insertGrammarError(
                GrammarErrorEntity(
                    sessionId = currentSessionId,
                    originalText = correction.original,
                    correctedText = correction.correction,
                    errorType = correction.errorType,
                    explanation = correction.explanation
                )
            )
        }

        _sessionState.value = VoiceSessionState.PROCESSING
        requestCount++

        // 3. Interview mode handling
        if (currentMode == VoiceMode.INTERVIEW && interviewState != null) {
            val nextQ = orchestrator.learningEngine.interviewEngine.getNextQuestion(interviewState!!, text)
            _messages.value = _messages.value + ChatMessage(UUID.randomUUID().toString(), "user", text, grammarCorrection = correction)

            if (nextQ != null) {
                _messages.value = _messages.value + ChatMessage(UUID.randomUUID().toString(), "assistant", nextQ)
                _sessionState.value = VoiceSessionState.ASSISTANT_SPEAKING
                ttsManager?.speak(nextQ, currentLanguage)
            } else {
                endSession()
            }
            return
        }

        // 4. Regular conversational mode via active provider
        activeProvider.sendText(text)
    }

    /**
     * Simulates or pipes microphone waveform audio amplitude.
     */
    fun emitAudioAmplitude(amplitude: Float) {
        scope.launch {
            _audioWaveform.emit(amplitude)
        }
    }

    /**
     * Interrupts assistant speaking.
     */
    suspend fun interrupt() {
        activeProvider.interrupt()
        _sessionState.value = VoiceSessionState.INTERRUPTED
    }

    /**
     * Endpoint: POST /voice/session/end
     * Closes the session and generates speaking evaluation.
     */
    suspend fun endSession(): SpeakingEvaluation {
        val duration = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
        _sessionState.value = VoiceSessionState.ENDED

        activeProvider.close()

        val allUserTexts = _messages.value.filter { it.role == "user" }.map { it.text }
        val fullConversation = _messages.value.joinToString(" ") { it.text }

        // Extract vocabulary
        val newWords = orchestrator.learningEngine.extractVocabulary(currentLanguage, fullConversation, currentLevel)

        // Evaluate session
        val evaluation = orchestrator.learningEngine.evaluateSpeakingSession(
            userTranscripts = allUserTexts,
            grammarErrors = detectedGrammarErrors,
            targetLanguage = currentLanguage,
            newWords = newWords
        )
        _latestEvaluation.value = evaluation

        // Persist evaluation
        voiceDao.insertSpeakingEvaluation(
            SpeakingEvaluationEntity(
                sessionId = currentSessionId,
                fluencyScore = evaluation.fluencyScore,
                grammarScore = evaluation.grammarScore,
                vocabularyScore = evaluation.vocabularyScore,
                pronunciationScore = evaluation.pronunciationScore,
                overallScore = evaluation.overallScore,
                fillerWords = evaluation.fillerWords,
                grammarErrorsJson = evaluation.grammarErrors.joinToString(";") { "${it.original}->${it.correction}" },
                vocabularyUsedJson = evaluation.vocabularyUsed.joinToString(","),
                newWordsJson = evaluation.newWords.joinToString(";") { "${it.word}:${it.meaning}" },
                recommendationsJson = evaluation.recommendations.joinToString(";")
            )
        )

        // Update Session record
        val existingSession = voiceDao.getSessionById(currentSessionId)
        if (existingSession != null) {
            voiceDao.updateSession(
                existingSession.copy(
                    endTime = System.currentTimeMillis(),
                    durationSeconds = duration,
                    requestCount = requestCount,
                    status = "COMPLETED"
                )
            )
        }

        // Generate and store conversation summary (Level 3 memory)
        val summary = orchestrator.summarizeConversation(_messages.value)
        if (summary.isNotBlank()) {
            voiceDao.insertSummary(
                ConversationSummaryEntity(
                    sessionId = currentSessionId,
                    summaryText = summary,
                    keyLearnings = newWords.joinToString(", ") { it.word },
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Update Learner Profile
        val currentProfile = getLearnerProfile()
        val updatedProfile = orchestrator.learningEngine.updateLearnerProfileAfterSession(currentProfile, evaluation)
        saveLearnerProfile(updatedProfile)

        // Feedback becomes immediately reviewable. New items have a zero review timestamp,
        // so the existing Room/FSRS due query places them in the next daily session.
        evaluation.newWords.forEach { addLearnedWordToVault(it) }
        evaluation.grammarErrors.forEach { error ->
            addAdaptiveReviewItem(
                word = error.correction,
                meaning = error.explanation,
                example = error.correctedText,
                level = currentLevel,
                category = "Maya grammar: ${error.errorType}"
            )
        }

        // Log usage
        voiceDao.insertUsageLog(
            VoiceUsageLogEntity(
                sessionId = currentSessionId,
                provider = activeProvider.providerName,
                mode = currentMode.name,
                audioDurationSec = duration,
                aiDurationSec = duration / 2,
                requestCount = requestCount,
                estimatedTokens = requestCount * 120
            )
        )

        return evaluation
    }

    /**
     * Endpoint: POST /learning/vocabulary
     * Adds learned words into user's Vocab Vault.
     */
    suspend fun addLearnedWordToVault(word: LearnerWord) {
        addAdaptiveReviewItem(word.word, word.meaning, word.example, word.difficulty, "Voice Learning")
    }

    private suspend fun addAdaptiveReviewItem(
        word: String,
        meaning: String,
        example: String,
        level: String,
        category: String
    ) {
        if (word.isBlank() || vocabDao.getWordByLanguageAndText(currentLanguage, word) != null) return
        vocabDao.insertAll(listOf(com.example.data.model.VocabWord(
            language = currentLanguage,
            word = word,
            phonetic = "",
            translation = meaning,
            category = category,
            exampleSentence = example,
            exampleTranslation = "",
            audioPrompt = word,
            level = level
        )))
    }

    suspend fun getLearnerProfile(): CompactLearnerProfile {
        val entity = voiceDao.getLearnerProfile()
        return if (entity != null) {
            CompactLearnerProfile(
                language = entity.language,
                nativeLanguage = entity.nativeLanguage,
                level = entity.level,
                goals = decodeList(entity.goalsJson),
                weakGrammar = decodeList(entity.weakGrammarJson),
                weakPronunciation = decodeList(entity.weakPronunciationJson),
                vocabularyLearning = decodeList(entity.vocabularyLearningJson),
                vocabularyMastered = decodeList(entity.vocabularyMasteredJson),
                speakingScore = entity.speakingScore,
                commonErrors = decodeList(entity.commonErrorsJson),
                preferredTopics = decodeList(entity.preferredTopicsJson)
            )
        } else {
            CompactLearnerProfile(language = currentLanguage, level = currentLevel)
        }
    }

    private suspend fun saveLearnerProfile(profile: CompactLearnerProfile) {
        voiceDao.insertLearnerProfile(
            LearnerProfileEntity(
                language = profile.language,
                nativeLanguage = profile.nativeLanguage,
                level = profile.level,
                goalsJson = encodeList(profile.goals),
                weakGrammarJson = encodeList(profile.weakGrammar),
                weakPronunciationJson = encodeList(profile.weakPronunciation),
                vocabularyLearningJson = encodeList(profile.vocabularyLearning),
                vocabularyMasteredJson = encodeList(profile.vocabularyMastered),
                speakingScore = profile.speakingScore,
                commonErrorsJson = encodeList(profile.commonErrors),
                preferredTopicsJson = encodeList(profile.preferredTopics),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    private fun decodeList(value: String): List<String> = try {
        val json = JSONArray(value)
        List(json.length()) { index -> json.optString(index) }.filter { it.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }

    private fun encodeList(values: List<String>): String = JSONArray(values.distinct()).toString()
}
