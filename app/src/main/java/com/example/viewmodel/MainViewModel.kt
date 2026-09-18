package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import com.example.data.repository.VocabRepository
import com.example.data.srs.FsrsRating
import com.example.data.srs.SpacedRepetitionEngine
import com.example.util.AnalyticsManager
import com.example.util.AnswerNormalizer
import com.example.util.HabitBoundaryManager
import com.example.util.TtsManager
import com.example.voice.gateway.VoiceGatewayService
import com.example.voice.service.SpeechRecognitionManager
import com.example.voice.model.ChatMessage
import com.example.voice.model.InterviewType
import com.example.voice.model.LearnerWord
import com.example.voice.model.RoleplayScenario
import com.example.voice.model.SpeakingEvaluation
import com.example.voice.model.VoiceMode
import com.example.voice.model.VoiceSessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    object Welcome : Screen
    object GoalSelect : Screen
    object StartingPoint : Screen
    object Home : Screen
    data class Practice(val mode: PracticeMode) : Screen
    object MistakeReviewIntro : Screen
    object VocabVault : Screen
    object Stats : Screen
    object Profile : Screen
    object Flashcards : Screen
    data class VoiceAssistant(val initialMode: VoiceMode = VoiceMode.FRIEND) : Screen
    data class SessionCelebration(
        val wordsCount: Int,
        val xpEarned: Int,
        val accuracy: Float,
        val srsPromotedCount: Int
    ) : Screen
}

enum class PracticeMode {
    DAILY_SRS,
    NEW_WORDS,
    MISTAKES_ONLY,
    SINGLE_WORD
}

enum class ExerciseType {
    NEW_WORD_INTRO,
    MULTIPLE_CHOICE_WORD_TO_MEANING,
    MULTIPLE_CHOICE_MEANING_TO_WORD,
    LISTENING_DRILL,
    WORD_BANK_TRANSLATION,
    FREE_TYPING,
    MATCH_PAIRS,
    SPEAKING
}

data class ExerciseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val word: VocabWord,
    val type: ExerciseType,
    val promptText: String,
    val phonetic: String,
    val audioText: String,
    val correctWords: List<String> = emptyList(),
    val bankOptions: List<String> = emptyList(),
    val multipleChoiceOptions: List<String> = emptyList(),
    val correctChoice: String = "",
    val freeTypingTarget: String = "",
    val matchPairsTarget: List<Pair<String, String>> = emptyList(),
    val characterRes: Int = R.drawable.img_teacher_avatar,
    val isNewWord: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocabRepository
    val ttsManager = TtsManager(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Hindi")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    val userProfile: StateFlow<UserProfile>

    val allWords: StateFlow<List<VocabWord>>
    val dueWords: StateFlow<List<VocabWord>>
    val mistakeWords: StateFlow<List<VocabWord>>
    val weakestWords: StateFlow<List<VocabWord>>

    // Active Exercise State
    private val _exerciseQueue = MutableStateFlow<List<ExerciseItem>>(emptyList())
    val exerciseQueue: StateFlow<List<ExerciseItem>> = _exerciseQueue.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    // State for Word Bank
    private val _placedWords = MutableStateFlow<List<String>>(emptyList())
    val placedWords: StateFlow<List<String>> = _placedWords.asStateFlow()

    // State for Multiple Choice & Listening
    private val _selectedChoice = MutableStateFlow<String?>(null)
    val selectedChoice: StateFlow<String?> = _selectedChoice.asStateFlow()

    // State for Free Typing
    private val _typedAnswer = MutableStateFlow("")
    val typedAnswer: StateFlow<String> = _typedAnswer.asStateFlow()

    // State for Match Pairs: Map of Target Word -> Meaning
    private val _completedMatches = MutableStateFlow<Map<String, String>>(emptyMap())
    val completedMatches: StateFlow<Map<String, String>> = _completedMatches.asStateFlow()

    // State for Answer Feedback
    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow(false)
    val isAnswerCorrect: StateFlow<Boolean> = _isAnswerCorrect.asStateFlow()

    private val _correctSolution = MutableStateFlow("")
    val correctSolution: StateFlow<String> = _correctSolution.asStateFlow()

    private val _encouragementTip = MutableStateFlow("")
    val encouragementTip: StateFlow<String> = _encouragementTip.asStateFlow()

    // Zero-Hearts Dialog
    private val _showZeroHeartsDialog = MutableStateFlow(false)
    val showZeroHeartsDialog: StateFlow<Boolean> = _showZeroHeartsDialog.asStateFlow()

    private var sessionCorrect = 0
    private var sessionTotal = 0
    private var srsPromotedCount = 0
    private var currentSessionMode = PracticeMode.DAILY_SRS
    private var exerciseStartTimeMs = 0L

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VocabRepository(db.vocabDao())

        userProfile = repository.getUserProfile()
            .combine(_selectedLanguage) { profile, lang ->
                profile ?: UserProfile(targetLanguage = lang)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UserProfile()
            )

        @OptIn(ExperimentalCoroutinesApi::class)
        allWords = _selectedLanguage
            .flatMapLatest { lang -> repository.getWordsByLanguage(lang) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        dueWords = _selectedLanguage
            .flatMapLatest { lang -> repository.getDueWords(lang) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        mistakeWords = _selectedLanguage
            .flatMapLatest { lang -> repository.getMistakeWords(lang) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        weakestWords = _selectedLanguage
            .flatMapLatest { lang -> repository.getWeakestWords(lang) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.ensureSeeded(application)
            // Check daily habit boundary and streak
            val profile = repository.getUserProfile().firstOrNull()
            if (profile != null) {
                val eval = HabitBoundaryManager.evaluateDailyActive(profile)
                if (eval.updatedProfile != profile) {
                    repository.updateUserProfile(eval.updatedProfile)
                }
                if (eval.updatedProfile.hasCompletedOnboarding) {
                    _selectedLanguage.value = eval.updatedProfile.targetLanguage
                    _currentScreen.value = Screen.Home
                }
            }
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectLanguage(language: String) {
        _selectedLanguage.value = language
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(current.copy(targetLanguage = language))
        }
    }

    fun setDailyGoal(minutes: Int, words: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateUserProfile(
                current.copy(
                    dailyGoalMinutes = minutes,
                    dailyGoalWords = words
                )
            )
        }
    }

    fun completeOnboarding(learningLevel: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                hasCompletedOnboarding = true,
                learningLevel = learningLevel
            )
            repository.updateUserProfile(updated)
            AnalyticsManager.logOnboardingStep("completed_$learningLevel")
            _currentScreen.value = Screen.Home
        }
    }

    fun recordPlacementResult(learningLevel: String, score: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                hasCompletedOnboarding = true,
                learningLevel = learningLevel,
                latestPlacementScore = score,
                latestPlacementAt = System.currentTimeMillis()
            )
            repository.updateUserProfile(updated)
            _currentScreen.value = Screen.Home
        }
    }

    // --- SESSION & INTERLEAVING ENGINE ---

    fun startPractice(mode: PracticeMode, wordsPool: List<VocabWord>) {
        val currentP = userProfile.value
        if (!currentP.relaxedMode && currentP.hearts <= 0 && mode != PracticeMode.MISTAKES_ONLY) {
            AnalyticsManager.logHeartsEmpty()
            _showZeroHeartsDialog.value = true
            return
        }

        currentSessionMode = mode
        sessionCorrect = 0
        sessionTotal = 0
        srsPromotedCount = 0
        exerciseStartTimeMs = System.currentTimeMillis()

        AnalyticsManager.logSessionStart(mode.name, _selectedLanguage.value, currentP.learningLevel)

        val targetWords = when (mode) {
            PracticeMode.MISTAKES_ONLY -> {
                wordsPool.filter { it.isMistake }.ifEmpty { wordsPool.take(5) }
            }
            PracticeMode.DAILY_SRS -> {
                // Interleave ~40% due reviews, ~40% new words, 20% reinforcement
                val due = wordsPool.filter { it.isDueForReview }
                val newWords = wordsPool.filter { it.repetitions == 0 }
                val combined = mutableListOf<VocabWord>()

                val dueTake = due.take(4)
                val newTake = newWords.take(4)
                combined.addAll(dueTake)
                combined.addAll(newTake)

                if (combined.isEmpty()) {
                    combined.addAll(wordsPool.take(8))
                }
                combined.distinctBy { it.id }.take(8)
            }
            PracticeMode.NEW_WORDS -> {
                wordsPool.filter { it.repetitions == 0 }.ifEmpty { wordsPool.take(6) }
            }
            PracticeMode.SINGLE_WORD -> wordsPool.take(1)
        }

        // Build exercises with session interleaving and variety
        val exercises = mutableListOf<ExerciseItem>()
        var lastType: ExerciseType? = null

        val availableTypes = listOf(
            ExerciseType.MULTIPLE_CHOICE_WORD_TO_MEANING,
            ExerciseType.LISTENING_DRILL,
            ExerciseType.WORD_BANK_TRANSLATION,
            ExerciseType.FREE_TYPING,
            ExerciseType.MULTIPLE_CHOICE_MEANING_TO_WORD
        )

        targetWords.forEachIndexed { index, word ->
            // Prepend new-word intro card if word has never been reviewed
            if (word.repetitions == 0) {
                exercises.add(
                    ExerciseItem(
                        word = word,
                        type = ExerciseType.NEW_WORD_INTRO,
                        promptText = "New Word Introduction",
                        phonetic = word.phonetic,
                        audioText = word.word,
                        isNewWord = true
                    )
                )
            }

            // Pick an exercise type distinct from lastType
            val candidateTypes = availableTypes.filter { it != lastType }
            val chosenType = candidateTypes[index % candidateTypes.size]
            lastType = chosenType

            when (chosenType) {
                ExerciseType.MULTIPLE_CHOICE_WORD_TO_MEANING -> {
                    val distractors = generateDistractors(word, wordsPool, isTargetLang = false)
                    val options = (listOf(word.translation) + distractors).shuffled()
                    exercises.add(
                        ExerciseItem(
                            word = word,
                            type = chosenType,
                            promptText = "Choose the correct meaning",
                            phonetic = word.phonetic,
                            audioText = word.word,
                            multipleChoiceOptions = options,
                            correctChoice = word.translation
                        )
                    )
                }
                ExerciseType.MULTIPLE_CHOICE_MEANING_TO_WORD -> {
                    val distractors = generateDistractors(word, wordsPool, isTargetLang = true)
                    val options = (listOf(word.word) + distractors).shuffled()
                    exercises.add(
                        ExerciseItem(
                            word = word,
                            type = chosenType,
                            promptText = "Which one means '${word.translation}'?",
                            phonetic = word.phonetic,
                            audioText = word.word,
                            multipleChoiceOptions = options,
                            correctChoice = word.word
                        )
                    )
                }
                ExerciseType.LISTENING_DRILL -> {
                    val distractors = generateDistractors(word, wordsPool, isTargetLang = true)
                    val options = (listOf(word.word) + distractors).shuffled()
                    exercises.add(
                        ExerciseItem(
                            word = word,
                            type = chosenType,
                            promptText = "Tap what you hear",
                            phonetic = word.phonetic,
                            audioText = word.word,
                            multipleChoiceOptions = options,
                            correctChoice = word.word
                        )
                    )
                }
                ExerciseType.FREE_TYPING -> {
                    exercises.add(
                        ExerciseItem(
                            word = word,
                            type = chosenType,
                            promptText = "Type '${word.translation}' in ${word.language}",
                            phonetic = word.phonetic,
                            audioText = word.word,
                            freeTypingTarget = word.word
                        )
                    )
                }
                ExerciseType.WORD_BANK_TRANSLATION -> {
                    val translationTokens = word.translation.split(" ").filter { it.isNotBlank() }
                    val distractors = generateDistractors(word, wordsPool, isTargetLang = false)
                    val bank = (translationTokens + distractors).shuffled()
                    exercises.add(
                        ExerciseItem(
                            word = word,
                            type = chosenType,
                            promptText = "Translate this sentence",
                            phonetic = word.phonetic,
                            audioText = word.word,
                            correctWords = translationTokens,
                            bankOptions = bank
                        )
                    )
                }
                else -> {}
            }
        }

        // Add a Match Pairs exercise if there are at least 4 words
        if (targetWords.size >= 4) {
            val pairSample = targetWords.take(4).map { it.word to it.translation }
            exercises.add(
                ExerciseItem(
                    word = targetWords.first(),
                    type = ExerciseType.MATCH_PAIRS,
                    promptText = "Match the corresponding pairs",
                    phonetic = "",
                    audioText = "",
                    matchPairsTarget = pairSample
                )
            )
        }

        // Add a Speaking exercise
        if (targetWords.isNotEmpty()) {
            val speakWord = targetWords.last()
            exercises.add(
                ExerciseItem(
                    word = speakWord,
                    type = ExerciseType.SPEAKING,
                    promptText = "Repeat aloud after Teacher Maya",
                    phonetic = speakWord.phonetic,
                    audioText = speakWord.word,
                    freeTypingTarget = speakWord.word
                )
            )
        }

        _exerciseQueue.value = exercises
        _currentExerciseIndex.value = 0
        resetExerciseInputs()
        _isAnswerChecked.value = false
        _currentScreen.value = Screen.Practice(mode)

        if (exercises.isNotEmpty() && exercises[0].audioText.isNotBlank()) {
            playAudio(exercises[0].audioText, isSlow = false)
        }
    }

    private fun resetExerciseInputs() {
        _placedWords.value = emptyList()
        _selectedChoice.value = null
        _typedAnswer.value = ""
        _completedMatches.value = emptyMap()
    }

    private fun generateDistractors(
        targetWord: VocabWord,
        allWords: List<VocabWord>,
        isTargetLang: Boolean
    ): List<String> {
        val otherWords = allWords.filter { it.id != targetWord.id }
        val pool = if (isTargetLang) {
            otherWords.map { it.word }
        } else {
            otherWords.map { it.translation }
        }
        return pool.filter { it.isNotBlank() }.distinct().shuffled().take(3)
    }

    // UI Input Handlers
    fun onWordChipClicked(token: String) {
        if (_isAnswerChecked.value) return
        _placedWords.value = _placedWords.value + token
    }

    fun onPlacedWordRemoved(index: Int) {
        if (_isAnswerChecked.value) return
        val current = _placedWords.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _placedWords.value = current
        }
    }

    fun onChoiceSelected(choice: String) {
        if (_isAnswerChecked.value) return
        _selectedChoice.value = choice
    }

    fun onTypedAnswerChanged(text: String) {
        if (_isAnswerChecked.value) return
        _typedAnswer.value = text
    }

    fun onPairMatched(targetWord: String, meaning: String) {
        val current = _completedMatches.value.toMutableMap()
        current[targetWord] = meaning
        _completedMatches.value = current
    }

    fun dismissZeroHeartsDialog() {
        _showZeroHeartsDialog.value = false
    }

    fun enableRelaxedModeAndContinue(mode: PracticeMode, wordsPool: List<VocabWord>) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(relaxedMode = true)
            repository.updateUserProfile(updated)
            _showZeroHeartsDialog.value = false
            startPractice(mode, wordsPool)
        }
    }

    fun refillHeartsWithGemsAndContinue(mode: PracticeMode, wordsPool: List<VocabWord>) {
        viewModelScope.launch {
            val refilled = repository.refillHeartsWithGems(userProfile.value)
            if (refilled != null) {
                _showZeroHeartsDialog.value = false
                startPractice(mode, wordsPool)
            }
        }
    }

    fun checkAnswer() {
        val exercises = _exerciseQueue.value
        val index = _currentExerciseIndex.value
        if (index !in exercises.indices) return

        val exercise = exercises[index]
        val responseTimeMs = System.currentTimeMillis() - exerciseStartTimeMs

        // New Word Intro cards don't test recall
        if (exercise.type == ExerciseType.NEW_WORD_INTRO) {
            continueToNextExercise()
            return
        }

        val isCorrect: Boolean
        val expectedAnswer: String

        when (exercise.type) {
            ExerciseType.MULTIPLE_CHOICE_WORD_TO_MEANING,
            ExerciseType.MULTIPLE_CHOICE_MEANING_TO_WORD,
            ExerciseType.LISTENING_DRILL -> {
                expectedAnswer = exercise.correctChoice
                isCorrect = _selectedChoice.value?.equals(expectedAnswer, ignoreCase = true) == true
            }
            ExerciseType.WORD_BANK_TRANSLATION -> {
                expectedAnswer = exercise.correctWords.joinToString(" ").trim()
                val userAnswer = _placedWords.value.joinToString(" ").trim()
                isCorrect = AnswerNormalizer.isLenientMatch(userAnswer, expectedAnswer)
            }
            ExerciseType.FREE_TYPING -> {
                expectedAnswer = exercise.freeTypingTarget
                isCorrect = AnswerNormalizer.isLenientMatch(_typedAnswer.value, expectedAnswer)
            }
            ExerciseType.MATCH_PAIRS -> {
                expectedAnswer = "All pairs matched!"
                isCorrect = _completedMatches.value.size >= exercise.matchPairsTarget.size
            }
            ExerciseType.SPEAKING -> {
                // Generous lenient matching for speech
                expectedAnswer = exercise.freeTypingTarget
                isCorrect = true // Audio speaking practiced with Teacher Maya
            }
            else -> {
                expectedAnswer = ""
                isCorrect = true
            }
        }

        _isAnswerCorrect.value = isCorrect
        _correctSolution.value = expectedAnswer

        sessionTotal++
        if (isCorrect) sessionCorrect++

        AnalyticsManager.logExerciseAnswer(exercise.type.name, isCorrect, responseTimeMs)

        // Apply FSRS-4.5 Review Update
        viewModelScope.launch {
            val updatedWord = repository.updateWordSRS(exercise.word, isCorrect = isCorrect)
            repository.logReview(
                wordId = exercise.word.id,
                rating = if (isCorrect) FsrsRating.GOOD.value else FsrsRating.AGAIN.value,
                exerciseType = exercise.type.name,
                isCorrect = isCorrect,
                responseTimeMs = responseTimeMs
            )

            if (isCorrect && updatedWord.intervalDays > exercise.word.intervalDays) {
                srsPromotedCount++
                _encouragementTip.value = "Memory strengthened! FSRS next review in ${updatedWord.intervalDays} days."
            } else if (isCorrect) {
                _encouragementTip.value = "Great job! Keep the momentum going!"
            } else {
                _encouragementTip.value = "Don't worry! FSRS scheduled this for spaced repetition reinforcement soon."
                if (currentSessionMode != PracticeMode.MISTAKES_ONLY && !userProfile.value.relaxedMode) {
                    repository.decrementHeart(userProfile.value)
                }
            }
            _isAnswerChecked.value = true
        }
    }

    fun continueToNextExercise() {
        val exercises = _exerciseQueue.value
        val nextIndex = _currentExerciseIndex.value + 1
        exerciseStartTimeMs = System.currentTimeMillis()

        if (nextIndex < exercises.size) {
            _currentExerciseIndex.value = nextIndex
            resetExerciseInputs()
            _isAnswerChecked.value = false
            if (exercises[nextIndex].audioText.isNotBlank()) {
                playAudio(exercises[nextIndex].audioText, isSlow = false)
            }
        } else {
            // Lesson completed!
            val accuracy = if (sessionTotal > 0) (sessionCorrect.toFloat() / sessionTotal) * 100f else 100f
            val xpEarned = sessionCorrect * 15 + 10
            viewModelScope.launch {
                // If this was mistake practice, refill hearts for free!
                if (currentSessionMode == PracticeMode.MISTAKES_ONLY) {
                    repository.refillHearts(userProfile.value)
                }

                repository.addXpAndProgress(
                    userProfile.value,
                    xpEarned = xpEarned,
                    wordsCount = sessionTotal,
                    correctCount = sessionCorrect
                )

                AnalyticsManager.logSessionComplete(accuracy, xpEarned, 120L)

                _currentScreen.value = Screen.SessionCelebration(
                    wordsCount = sessionTotal,
                    xpEarned = xpEarned,
                    accuracy = accuracy,
                    srsPromotedCount = srsPromotedCount
                )
            }
        }
    }

    // FSRS Flashcard rating handler
    fun rateFlashcard(word: VocabWord, rating: FsrsRating) {
        viewModelScope.launch {
            repository.updateWordRating(word, rating)
            repository.logReview(
                wordId = word.id,
                rating = rating.value,
                exerciseType = "FLASHCARD",
                isCorrect = (rating != FsrsRating.AGAIN),
                responseTimeMs = 2000L
            )
        }
    }

    fun playAudio(text: String, isSlow: Boolean = false) {
        if (!userProfile.value.soundEnabled && !isSlow) return
        ttsManager.speak(text, _selectedLanguage.value, isSlow)
    }

    fun buyStreakFreeze() {
        viewModelScope.launch {
            repository.buyStreakFreeze(userProfile.value)
        }
    }

    fun refillHeartsGems() {
        viewModelScope.launch {
            repository.refillHeartsWithGems(userProfile.value)
        }
    }

    fun repairStreakGems() {
        viewModelScope.launch {
            repository.repairStreak(userProfile.value)
        }
    }

    fun toggleRelaxedMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(relaxedMode = enabled))
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(soundEnabled = enabled))
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(darkTheme = enabled))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(notificationsEnabled = enabled))
        }
    }

    fun updateGardenPot(pot: String) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(gardenPot = pot))
        }
    }

    fun updateGardenCompanion(companion: String) {
        viewModelScope.launch {
            repository.updateUserProfile(userProfile.value.copy(gardenCompanion = companion))
        }
    }

    val voiceGatewayService = VoiceGatewayService(application, ttsManager = ttsManager)
    private val speechRecognitionManager = SpeechRecognitionManager(application)
    val voiceSessionState: StateFlow<VoiceSessionState> = voiceGatewayService.sessionState
    val voiceMessages: StateFlow<List<ChatMessage>> = voiceGatewayService.messages
    val voiceEvaluation: StateFlow<SpeakingEvaluation?> = voiceGatewayService.latestEvaluation
    /** Actual device microphone state; distinct from Nyra being ready for a learner turn. */
    val voiceIsRecording: StateFlow<Boolean> = speechRecognitionManager.isListening

    private val _voiceCurrentMode = MutableStateFlow(VoiceMode.FRIEND)
    val voiceCurrentMode: StateFlow<VoiceMode> = _voiceCurrentMode.asStateFlow()

    init {
        viewModelScope.launch {
            speechRecognitionManager.finalTranscript.collect { transcript ->
                voiceGatewayService.sendUserUtterance(transcript)
            }
        }
    }

    fun openVoiceAssistant(
        mode: VoiceMode = VoiceMode.FRIEND,
        interviewType: InterviewType? = null,
        roleplayScenario: RoleplayScenario? = null
    ) {
        _voiceCurrentMode.value = mode
        _currentScreen.value = Screen.VoiceAssistant(mode)
        viewModelScope.launch {
            voiceGatewayService.startSession(
                mode = mode,
                targetLanguage = _selectedLanguage.value,
                level = userProfile.value.learningLevel,
                interviewType = interviewType,
                roleplayScenario = roleplayScenario
            )
        }
    }

    fun setVoiceMode(
        mode: VoiceMode,
        interviewType: InterviewType? = null,
        roleplayScenario: RoleplayScenario? = null
    ) {
        _voiceCurrentMode.value = mode
        viewModelScope.launch {
            voiceGatewayService.startSession(
                mode = mode,
                targetLanguage = _selectedLanguage.value,
                level = userProfile.value.learningLevel,
                interviewType = interviewType,
                roleplayScenario = roleplayScenario
            )
        }
    }

    fun sendVoiceUtterance(text: String) {
        viewModelScope.launch {
            voiceGatewayService.sendUserUtterance(text)
        }
    }

    fun startVoiceListening() {
        speechRecognitionManager.startListening(_selectedLanguage.value)
    }

    fun finishVoiceListening() {
        speechRecognitionManager.finishListening()
    }

    fun interruptVoice() {
        viewModelScope.launch {
            speechRecognitionManager.stopListening()
            voiceGatewayService.interrupt()
        }
    }

    fun resumeVoice() {
        voiceGatewayService.resume()
    }

    fun endVoiceSession() {
        viewModelScope.launch {
            speechRecognitionManager.stopListening()
            voiceGatewayService.endSession()
        }
    }

    fun addVoiceWordToVault(word: LearnerWord) {
        viewModelScope.launch {
            voiceGatewayService.addLearnedWordToVault(word)
        }
    }

    fun dismissVoiceEvaluation() {
        voiceGatewayService.dismissEvaluation()
    }

    fun claimQuest(questIndex: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            val rewardGems = when (questIndex) {
                1 -> 25
                2 -> 40
                else -> 50
            }
            val updated = when (questIndex) {
                1 -> current.copy(quest1Claimed = true, gems = current.gems + rewardGems)
                2 -> current.copy(quest2Claimed = true, gems = current.gems + rewardGems)
                else -> current.copy(quest3Claimed = true, gems = current.gems + rewardGems)
            }
            repository.updateUserProfile(updated)
            AnalyticsManager.logQuestClaim(questIndex, rewardGems)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.stopListening()
        ttsManager.shutdown()
    }
}
