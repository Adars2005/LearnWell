package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomBackground
import com.example.ui.theme.BloomBorder
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach
import com.example.ui.theme.BloomTextDark
import com.example.ui.theme.BloomTextMuted
import com.example.R
import com.example.voice.model.ChatMessage
import com.example.voice.model.InterviewType
import com.example.voice.model.LearnerWord
import com.example.voice.model.RoleplayScenario
import com.example.voice.model.SpeakingEvaluation
import com.example.voice.model.VoiceMode
import com.example.voice.model.VoiceSessionState
import kotlinx.coroutines.delay

@Composable
fun VoiceAssistantScreen(
    sessionState: VoiceSessionState,
    messages: List<ChatMessage>,
    currentMode: VoiceMode,
    currentLanguage: String,
    currentLevel: String,
    evaluation: SpeakingEvaluation?,
    isRecording: Boolean,
    onModeSelected: (VoiceMode) -> Unit,
    onInterviewTypeSelected: (InterviewType) -> Unit,
    onRoleplayScenarioSelected: (RoleplayScenario) -> Unit,
    onSendUtterance: (String) -> Unit,
    onStartListening: () -> Unit,
    onFinishListening: () -> Unit,
    onInterrupt: () -> Unit,
    onResume: () -> Unit,
    onEndSession: () -> Unit,
    onReplayAudio: (String) -> Unit,
    onAddWordToVault: (LearnerWord) -> Unit,
    onCloseEvaluation: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    var selectedInterviewType by remember { mutableStateOf(InterviewType.HR) }
    var selectedRoleplayScenario by remember { mutableStateOf(RoleplayScenario.RESTAURANT) }
    var showScenarioPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onStartListening() }

    // Session Timer
    LaunchedEffect(sessionState) {
        if (sessionState != VoiceSessionState.ENDED && sessionState != VoiceSessionState.IDLE) {
            while (true) {
                delay(1000)
                sessionSeconds++
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mic Button pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BloomBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar: Level Badge, Mode Title, Timer, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(BloomPeach, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$currentLanguage • $currentLevel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BloomCoral
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Timer
                    val minutes = sessionSeconds / 60
                    val seconds = sessionSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BloomTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = BloomTextDark
                        )
                    }

                    IconButton(
                        onClick = {
                            // The header X is an exit affordance, not merely an evaluation action.
                            // End the active session, then leave the voice route immediately.
                            onEndSession()
                            onExit()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("voice_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit voice session and return home",
                            tint = BloomTextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VoiceMode.values()) { mode ->
                    val isSelected = mode == currentMode
                    val bg = if (isSelected) BloomCoral else Color.White
                    val textColor = if (isSelected) Color.White else BloomTextDark

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .border(1.dp, if (isSelected) BloomCoral else BloomBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onModeSelected(mode)
                                if (mode == VoiceMode.INTERVIEW || mode == VoiceMode.ROLEPLAY) {
                                    showScenarioPicker = true
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mode.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            // Sub-mode badge if Interview or Roleplay
            if (currentMode == VoiceMode.INTERVIEW || currentMode == VoiceMode.ROLEPLAY) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showScenarioPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val subTitle = if (currentMode == VoiceMode.INTERVIEW) {
                        "Interview: ${selectedInterviewType.title} (tap to change)"
                    } else {
                        "Scenario: ${selectedRoleplayScenario.title} (tap to change)"
                    }
                    Text(
                        text = "🎯 $subTitle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BloomCoral
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State Indicator & Waveform Card
            VoiceWaveformCard(sessionState = sessionState)

            Spacer(modifier = Modifier.height(12.dp))

            // Live Transcript View
            Text(
                text = "Live Conversation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BloomTextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap the microphone below to start speaking with Nyra...",
                                fontSize = 14.sp,
                                color = BloomTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatTranscriptBubble(
                        message = msg,
                        onReplayAudio = { onReplayAudio(msg.text) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fallback Text Input (if mic fails or learner prefers typing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Or type your response here...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BloomCoral,
                        unfocusedBorderColor = BloomBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendUtterance(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(BloomCoral, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Voice Action Controls: Stop, Big Mic, End Session
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop/Interrupt Button
                IconButton(
                    onClick = {
                        if (sessionState == VoiceSessionState.INTERRUPTED) onResume() else onInterrupt()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFE4E6), CircleShape)
                ) {
                    Icon(
                        imageVector = if (sessionState == VoiceSessionState.INTERRUPTED) Icons.Default.PlayArrow else Icons.Default.Stop,
                        contentDescription = if (sessionState == VoiceSessionState.INTERRUPTED) "Resume session" else "Pause session",
                        tint = if (sessionState == VoiceSessionState.INTERRUPTED) BloomEmerald else Color(0xFFE11D48)
                    )
                }

                // Large Animated Mic Button
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(BloomCoral)
                        .clickable {
                            if (sessionState == VoiceSessionState.ASSISTANT_SPEAKING) {
                                onInterrupt()
                            } else if (isRecording) {
                                onFinishListening()
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    onStartListening()
                                } else {
                                    microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("voice_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isRecording) "Finish recording" else "Start recording",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // End Session Button
                IconButton(
                    onClick = { onEndSession() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(BloomPeach, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "End Session",
                        tint = BloomCoral
                    )
                }
            }
        }

        // Sub-mode picker dialog
        if (showScenarioPicker) {
            SubModePickerDialog(
                currentMode = currentMode,
                selectedInterview = selectedInterviewType,
                selectedScenario = selectedRoleplayScenario,
                onSelectInterview = {
                    selectedInterviewType = it
                    onInterviewTypeSelected(it)
                    showScenarioPicker = false
                },
                onSelectScenario = {
                    selectedRoleplayScenario = it
                    onRoleplayScenarioSelected(it)
                    showScenarioPicker = false
                },
                onDismiss = { showScenarioPicker = false }
            )
        }

        // Post-Session Evaluation Dialog
        if (evaluation != null) {
            SpeakingEvaluationDialog(
                evaluation = evaluation,
                onAddWordToVault = onAddWordToVault,
                onDismiss = onCloseEvaluation,
                onGoHome = {
                    onCloseEvaluation()
                    onExit()
                }
            )
        }
    }
}

@Composable
fun VoiceWaveformCard(sessionState: VoiceSessionState) {
    val stateText = when (sessionState) {
        VoiceSessionState.IDLE -> "Ready to speak"
        VoiceSessionState.CONNECTING -> "Connecting to Voice Gateway..."
        VoiceSessionState.LISTENING -> "Listening to you..."
        VoiceSessionState.PROCESSING -> "Analyzing speech..."
        VoiceSessionState.ASSISTANT_SPEAKING -> "Nyra is speaking..."
        VoiceSessionState.INTERRUPTED -> "Interrupted. What's next?"
        VoiceSessionState.ERROR -> "Offline / Network fallback active"
        VoiceSessionState.ENDED -> "Session completed"
    }

    val stateColor = when (sessionState) {
        VoiceSessionState.LISTENING -> BloomEmerald
        VoiceSessionState.ASSISTANT_SPEAKING -> BloomCoral
        VoiceSessionState.PROCESSING -> BloomAmber
        VoiceSessionState.ERROR -> Color(0xFFEF4444)
        else -> BloomTextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BloomBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.img_teacher_avatar),
                    contentDescription = "Nyra, your language tutor",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(2.dp, BloomCoral, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(stateColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stateText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = stateColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Waveform Canvas
            val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
            val wavePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6.28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave_phase"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val barCount = 28
                val barWidth = size.width / (barCount * 1.6f)
                val isAnimated = sessionState == VoiceSessionState.LISTENING || sessionState == VoiceSessionState.ASSISTANT_SPEAKING

                for (i in 0 until barCount) {
                    val progress = i.toFloat() / barCount
                    val heightMultiplier = if (isAnimated) {
                        (kotlin.math.sin(wavePhase + progress * 8f) * 0.45f + 0.55f).toFloat()
                    } else {
                        0.25f
                    }
                    val barHeight = size.height * heightMultiplier
                    val x = i * (barWidth * 1.6f)
                    val y = (size.height - barHeight) / 2f

                    drawRoundRect(
                        color = if (isAnimated) stateColor else Color(0xFFCBD5E1),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTranscriptBubble(
    message: ChatMessage,
    onReplayAudio: () -> Unit
) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) BloomCoral else Color.White
    val textColor = if (isUser) Color.White else BloomTextDark
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Text(
                    text = "Nyra",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BloomTextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = BloomBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Replay Audio",
                        tint = BloomCoral,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onReplayAudio() }
                    )
                }
            }
        }

        // Contextual grammar correction tag if present
        if (message.grammarCorrection != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BloomPeach)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "💡 Correction: ${message.grammarCorrection.correction} (${message.grammarCorrection.explanation})",
                    fontSize = 11.sp,
                    color = BloomCoral,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SpeakingEvaluationDialog(
    evaluation: SpeakingEvaluation,
    onAddWordToVault: (LearnerWord) -> Unit,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌟 Speaking Performance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BloomTextDark
                )
                Text(
                    text = "Overall Session Score: ${evaluation.overallScore}/100",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BloomCoral
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score metrics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ScoreBadge("Fluency", evaluation.fluencyScore)
                    ScoreBadge("Grammar", evaluation.grammarScore)
                    ScoreBadge("Vocab", evaluation.vocabularyScore)
                    ScoreBadge("Pronounce", evaluation.pronunciationScore)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filler words counter
                Text(
                    text = "Filler Words Detected: ${evaluation.fillerWords}",
                    fontSize = 12.sp,
                    color = if (evaluation.fillerWords > 3) Color(0xFFE11D48) else BloomEmerald,
                    fontWeight = FontWeight.SemiBold
                )

                // Recommendations
                if (evaluation.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                    text = "Nyra's Recommendations",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BloomTextDark,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    evaluation.recommendations.take(2).forEach { rec ->
                        Text(
                            text = "• $rec",
                            fontSize = 12.sp,
                            color = BloomTextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                // New vocabulary is automatically added to the learner's review queue.
                if (evaluation.newWords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "New Words to Learn",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BloomTextDark,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    evaluation.newWords.take(3).forEach { word ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.word,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BloomTextDark
                                )
                                Text(
                                    text = word.meaning,
                                    fontSize = 11.sp,
                                    color = BloomTextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onGoHome,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BloomBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Home", color = BloomTextDark, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BloomCoral),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBadge(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(BloomPeach, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BloomCoral
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = BloomTextMuted)
    }
}

@Composable
fun SubModePickerDialog(
    currentMode: VoiceMode,
    selectedInterview: InterviewType,
    selectedScenario: RoleplayScenario,
    onSelectInterview: (InterviewType) -> Unit,
    onSelectScenario: (RoleplayScenario) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (currentMode == VoiceMode.INTERVIEW) "Select Interview Type" else "Select Roleplay Scenario",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BloomTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentMode == VoiceMode.INTERVIEW) {
                        items(InterviewType.values()) { type ->
                            val isSelected = type == selectedInterview
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectInterview(type) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) BloomPeach else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BloomCoral else BloomBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = type.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) BloomCoral else BloomTextDark
                                    )
                                    Text(
                                        text = "Interviewer: ${type.role}",
                                        fontSize = 12.sp,
                                        color = BloomTextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(RoleplayScenario.values()) { scenario ->
                            val isSelected = scenario == selectedScenario
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectScenario(scenario) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) BloomPeach else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BloomCoral else BloomBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = scenario.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) BloomCoral else BloomTextDark
                                    )
                                    Text(
                                        text = "${scenario.setting} • Partner: ${scenario.partnerRole}",
                                        fontSize = 12.sp,
                                        color = BloomTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BloomCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
