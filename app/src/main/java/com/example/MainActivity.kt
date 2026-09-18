package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BloomBottomNavBar
import com.example.ui.components.ZeroHeartsDialog
import com.example.ui.screens.FlashcardStudyScreen
import com.example.ui.screens.GoalSelectScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MistakeReviewIntroScreen
import com.example.ui.screens.PracticeExerciseScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SessionCelebrationScreen
import com.example.ui.screens.StartingPointScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.VocabVaultScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.util.NotificationHelper
import com.example.viewmodel.PracticeMode
import com.example.viewmodel.Screen
import com.example.voice.model.VoiceMode

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    LinguaBloomApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LinguaBloomApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.toggleNotifications(granted)
        if (granted) NotificationHelper.showPracticeReminder(context, "Nyra is ready", "Your next language practice is ready when you are.")
    }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val dueWords by viewModel.dueWords.collectAsState()
    val mistakeWords by viewModel.mistakeWords.collectAsState()
    val weakestWords by viewModel.weakestWords.collectAsState()

    val exerciseQueue by viewModel.exerciseQueue.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val placedWords by viewModel.placedWords.collectAsState()
    val selectedChoice by viewModel.selectedChoice.collectAsState()
    val typedAnswer by viewModel.typedAnswer.collectAsState()
    val completedMatches by viewModel.completedMatches.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()
    val isAnswerCorrect by viewModel.isAnswerCorrect.collectAsState()
    val correctSolution by viewModel.correctSolution.collectAsState()
    val encouragementTip by viewModel.encouragementTip.collectAsState()
    val showZeroHeartsDialog by viewModel.showZeroHeartsDialog.collectAsState()

    // Determine if bottom navigation should be visible
    val isMainTabScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.VoiceAssistant ||
            currentScreen is Screen.VocabVault ||
            currentScreen is Screen.Stats ||
            currentScreen is Screen.Profile

    // Onboarding is deliberately excluded: a learner who has not selected a level has no
    // meaningful home curriculum to return to yet.
    val showHomeShortcut = currentScreen !is Screen.Welcome && currentScreen !is Screen.Home

    // Handle system back button
    BackHandler(enabled = currentScreen !is Screen.Welcome && currentScreen !is Screen.Home) {
        when (currentScreen) {
            is Screen.GoalSelect -> viewModel.setScreen(Screen.Welcome)
            is Screen.StartingPoint -> viewModel.setScreen(Screen.GoalSelect)
            is Screen.VoiceAssistant -> {
                viewModel.endVoiceSession()
                viewModel.setScreen(Screen.Home)
            }
            is Screen.Practice, Screen.MistakeReviewIntro, is Screen.SessionCelebration, Screen.Flashcards -> viewModel.setScreen(Screen.Home)
            is Screen.VocabVault, Screen.Stats, Screen.Profile -> viewModel.setScreen(Screen.Home)
            else -> {}
        }
    }

    // Zero Hearts 3-Option Dialog
    if (showZeroHeartsDialog) {
        ZeroHeartsDialog(
            userGems = userProfile.gems,
            onPracticeMistakesFree = {
                viewModel.dismissZeroHeartsDialog()
                viewModel.startPractice(PracticeMode.MISTAKES_ONLY, allWords)
            },
            onRefillGems = {
                viewModel.refillHeartsWithGemsAndContinue(PracticeMode.DAILY_SRS, allWords)
            },
            onEnableRelaxedMode = {
                viewModel.enableRelaxedModeAndContinue(PracticeMode.DAILY_SRS, allWords)
            },
            onDismiss = { viewModel.dismissZeroHeartsDialog() }
        )
    }

    Scaffold(
        bottomBar = {
            if (isMainTabScreen) {
                BloomBottomNavBar(
                    currentScreen = currentScreen,
                    onTabSelected = { targetScreen -> viewModel.setScreen(targetScreen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Welcome -> {
                        WelcomeScreen(
                            onGetStarted = { viewModel.setScreen(Screen.GoalSelect) },
                            onAlreadyHaveAccount = { viewModel.completeOnboarding("Beginner") }
                        )
                    }

                    is Screen.GoalSelect -> {
                        GoalSelectScreen(
                            onGoalConfirmed = { minutes, words ->
                                viewModel.setDailyGoal(minutes, words)
                                viewModel.setScreen(Screen.StartingPoint)
                            },
                            onBack = { viewModel.setScreen(Screen.Welcome) }
                        )
                    }

                    is Screen.StartingPoint -> {
                        StartingPointScreen(
                            language = userProfile.targetLanguage,
                            onStartSelected = { level ->
                                viewModel.completeOnboarding(level)
                            },
                            onPlacementFinished = { level, score -> viewModel.recordPlacementResult(level, score) },
                            onBack = { viewModel.setScreen(Screen.GoalSelect) }
                        )
                    }

                    is Screen.Home -> {
                        HomeDashboardScreen(
                            userProfile = userProfile,
                            allWords = allWords,
                            dueWords = dueWords,
                            mistakeWords = mistakeWords,
                            onStartDailyPractice = {
                                viewModel.startPractice(PracticeMode.DAILY_SRS, allWords)
                            },
                            onStartNewWordsPractice = {
                                viewModel.startPractice(PracticeMode.NEW_WORDS, allWords)
                            },
                            onStartMistakesReview = {
                                viewModel.setScreen(Screen.MistakeReviewIntro)
                            },
                            onStartFlashcards = {
                                viewModel.setScreen(Screen.Flashcards)
                            },
                            onTakePlacementTest = { viewModel.setScreen(Screen.StartingPoint) },
                            onSelectLanguage = { lang ->
                                viewModel.selectLanguage(lang)
                            },
                            onRepairStreak = {
                                viewModel.repairStreakGems()
                            },
                            onClaimQuest = { index ->
                                viewModel.claimQuest(index)
                            },
                            onOpenVoiceAssistant = { mode ->
                                viewModel.openVoiceAssistant(mode)
                            }
                        )
                    }

                    is Screen.VoiceAssistant -> {
                        val sessionState by viewModel.voiceSessionState.collectAsState()
                        val messages by viewModel.voiceMessages.collectAsState()
                        val currentVoiceMode by viewModel.voiceCurrentMode.collectAsState()
                        val evaluation by viewModel.voiceEvaluation.collectAsState()
                        val isVoiceRecording by viewModel.voiceIsRecording.collectAsState()

                        VoiceAssistantScreen(
                            sessionState = sessionState,
                            messages = messages,
                            currentMode = currentVoiceMode,
                            currentLanguage = userProfile.targetLanguage,
                            currentLevel = userProfile.learningLevel,
                            evaluation = evaluation,
                            isRecording = isVoiceRecording,
                            onModeSelected = { mode -> viewModel.setVoiceMode(mode) },
                            onInterviewTypeSelected = { type -> viewModel.setVoiceMode(VoiceMode.INTERVIEW, interviewType = type) },
                            onRoleplayScenarioSelected = { scenario -> viewModel.setVoiceMode(VoiceMode.ROLEPLAY, roleplayScenario = scenario) },
                            onSendUtterance = { text -> viewModel.sendVoiceUtterance(text) },
                            onStartListening = { viewModel.startVoiceListening() },
                            onFinishListening = { viewModel.finishVoiceListening() },
                            onInterrupt = { viewModel.interruptVoice() },
                            onResume = { viewModel.resumeVoice() },
                            onEndSession = { viewModel.endVoiceSession() },
                            onReplayAudio = { text -> viewModel.playAudio(text, false) },
                            onAddWordToVault = { word -> viewModel.addVoiceWordToVault(word) },
                            onCloseEvaluation = { viewModel.dismissVoiceEvaluation() },
                            onExit = { viewModel.setScreen(Screen.Home) }
                        )
                    }

                    is Screen.VocabVault -> {
                        VocabVaultScreen(
                            words = allWords,
                            targetLanguage = userProfile.targetLanguage,
                            onBack = { viewModel.setScreen(Screen.Home) },
                            onPlayAudio = { text, isSlow ->
                                viewModel.playAudio(text, isSlow)
                            }
                        )
                    }

                    is Screen.Stats -> {
                        StatsScreen(
                            userProfile = userProfile,
                            allWords = allWords,
                            onPracticeLeechWord = { word ->
                                viewModel.startPractice(PracticeMode.SINGLE_WORD, listOf(word))
                            }
                        )
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            onLanguageChange = { viewModel.selectLanguage(it) },
                            onLevelChange = { viewModel.completeOnboarding(it) },
                            onGoalChange = { m, w -> viewModel.setDailyGoal(m, w) },
                            onToggleSound = { viewModel.toggleSound(it) },
                            onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                            onToggleRelaxedMode = { viewModel.toggleRelaxedMode(it) },
                            onToggleNotifications = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleNotifications(enabled)
                                    if (enabled) NotificationHelper.showPracticeReminder(context, "Nyra is ready", "Your next language practice is ready when you are.")
                                }
                            },
                            onBuyStreakFreeze = { viewModel.buyStreakFreeze() },
                            onRefillHeartsGems = { viewModel.refillHeartsGems() },
                            onRepairStreakGems = { viewModel.repairStreakGems() },
                            onUpdateGardenPot = { viewModel.updateGardenPot(it) },
                            onUpdateGardenCompanion = { viewModel.updateGardenCompanion(it) }
                        )
                    }

                    is Screen.Flashcards -> {
                        FlashcardStudyScreen(
                            words = dueWords.ifEmpty { allWords },
                            onRateWord = { word, rating ->
                                viewModel.rateFlashcard(word, rating)
                            },
                            onPlayAudio = { text, rate ->
                                viewModel.playAudio(text, isSlow = rate < 1.0f)
                            },
                            onClose = { viewModel.setScreen(Screen.Home) }
                        )
                    }

                    is Screen.Practice -> {
                        if (exerciseQueue.isNotEmpty() && currentExerciseIndex in exerciseQueue.indices) {
                            val currentExercise = exerciseQueue[currentExerciseIndex]
                            PracticeExerciseScreen(
                                exercise = currentExercise,
                                exerciseIndex = currentExerciseIndex,
                                totalExercises = exerciseQueue.size,
                                hearts = userProfile.hearts,
                                relaxedMode = userProfile.relaxedMode,
                                placedWords = placedWords,
                                selectedChoice = selectedChoice,
                                typedAnswer = typedAnswer,
                                completedMatches = completedMatches,
                                isAnswerChecked = isAnswerChecked,
                                isAnswerCorrect = isAnswerCorrect,
                                correctSolution = correctSolution,
                                encouragementTip = encouragementTip,
                                onWordPlaced = { token -> viewModel.onWordChipClicked(token) },
                                onWordRemoved = { index -> viewModel.onPlacedWordRemoved(index) },
                                onChoiceSelected = { choice -> viewModel.onChoiceSelected(choice) },
                                onTypedAnswerChanged = { text -> viewModel.onTypedAnswerChanged(text) },
                                onPairMatched = { w, m -> viewModel.onPairMatched(w, m) },
                                onCheckAnswer = { viewModel.checkAnswer() },
                                onContinue = { viewModel.continueToNextExercise() },
                                onPlayAudio = { isSlow ->
                                    viewModel.playAudio(currentExercise.audioText, isSlow)
                                },
                                onClose = { viewModel.setScreen(Screen.Home) }
                            )
                        }
                    }

                    is Screen.MistakeReviewIntro -> {
                        MistakeReviewIntroScreen(
                            onContinue = {
                                viewModel.startPractice(PracticeMode.MISTAKES_ONLY, allWords)
                            },
                            onClose = { viewModel.setScreen(Screen.Home) }
                        )
                    }

                    is Screen.SessionCelebration -> {
                        SessionCelebrationScreen(
                            wordsCount = screen.wordsCount,
                            xpEarned = screen.xpEarned,
                            accuracy = screen.accuracy,
                            srsPromotedCount = screen.srsPromotedCount,
                            onContinue = { viewModel.setScreen(Screen.Home) }
                        )
                    }
                }
            }

            if (showHomeShortcut) {
                Text(
                    text = "Home",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 12.dp, top = 8.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(18.dp))
                        .clickable {
                            if (currentScreen is Screen.VoiceAssistant) viewModel.endVoiceSession()
                            viewModel.setScreen(Screen.Home)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
