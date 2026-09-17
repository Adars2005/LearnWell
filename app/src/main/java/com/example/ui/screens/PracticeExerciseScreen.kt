package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonType
import com.example.ui.components.FeedbackBottomSheet
import com.example.ui.components.WordChip
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach
import com.example.viewmodel.ExerciseItem
import com.example.viewmodel.ExerciseType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeExerciseScreen(
    exercise: ExerciseItem,
    exerciseIndex: Int,
    totalExercises: Int,
    hearts: Int,
    relaxedMode: Boolean,
    placedWords: List<String>,
    selectedChoice: String?,
    typedAnswer: String,
    completedMatches: Map<String, String>,
    isAnswerChecked: Boolean,
    isAnswerCorrect: Boolean,
    correctSolution: String,
    encouragementTip: String,
    onWordPlaced: (String) -> Unit,
    onWordRemoved: (Int) -> Unit,
    onChoiceSelected: (String) -> Unit,
    onTypedAnswerChanged: (String) -> Unit,
    onPairMatched: (String, String) -> Unit,
    onCheckAnswer: () -> Unit,
    onContinue: () -> Unit,
    onPlayAudio: (isSlow: Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (exerciseIndex + 1).toFloat() / totalExercises.coerceAtLeast(1)

    // Match pairs local selection state
    var selectedTargetWord by remember { mutableStateOf<String?>(null) }
    var selectedMeaning by remember { mutableStateOf<String?>(null) }
    var isSpeakingSimulated by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF9))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isAnswerChecked) 160.dp else 90.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BloomCoral,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Hearts
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Hearts",
                        tint = if (relaxedMode) BloomEmerald else BloomCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (relaxedMode) "∞" else "$hearts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (relaxedMode) BloomEmerald else BloomCoral
                    )
                }
            }

            // Exercise Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                when (exercise.type) {
                    ExerciseType.NEW_WORD_INTRO -> {
                        NewWordIntroView(
                            exercise = exercise,
                            onPlayAudio = { onPlayAudio(false) }
                        )
                    }

                    ExerciseType.MULTIPLE_CHOICE_WORD_TO_MEANING,
                    ExerciseType.MULTIPLE_CHOICE_MEANING_TO_WORD -> {
                        MultipleChoiceView(
                            exercise = exercise,
                            selectedChoice = selectedChoice,
                            onChoiceSelected = onChoiceSelected,
                            onPlayAudio = { onPlayAudio(false) }
                        )
                    }

                    ExerciseType.LISTENING_DRILL -> {
                        ListeningDrillView(
                            exercise = exercise,
                            selectedChoice = selectedChoice,
                            onChoiceSelected = onChoiceSelected,
                            onPlayNormal = { onPlayAudio(false) },
                            onPlaySlow = { onPlayAudio(true) }
                        )
                    }

                    ExerciseType.FREE_TYPING -> {
                        FreeTypingView(
                            exercise = exercise,
                            typedAnswer = typedAnswer,
                            onTypedAnswerChanged = onTypedAnswerChanged,
                            onPlayAudio = { onPlayAudio(false) }
                        )
                    }

                    ExerciseType.WORD_BANK_TRANSLATION -> {
                        WordBankTranslationView(
                            exercise = exercise,
                            placedWords = placedWords,
                            onWordPlaced = onWordPlaced,
                            onWordRemoved = onWordRemoved,
                            onPlayAudio = { onPlayAudio(false) }
                        )
                    }

                    ExerciseType.MATCH_PAIRS -> {
                        MatchPairsView(
                            pairs = exercise.matchPairsTarget,
                            completedMatches = completedMatches,
                            selectedTargetWord = selectedTargetWord,
                            selectedMeaning = selectedMeaning,
                            onSelectTargetWord = { word ->
                                selectedTargetWord = word
                                if (selectedMeaning != null) {
                                    val match = exercise.matchPairsTarget.find { it.first == word && it.second == selectedMeaning }
                                    if (match != null) {
                                        onPairMatched(word, selectedMeaning!!)
                                    }
                                    selectedTargetWord = null
                                    selectedMeaning = null
                                }
                            },
                            onSelectMeaning = { meaning ->
                                selectedMeaning = meaning
                                if (selectedTargetWord != null) {
                                    val match = exercise.matchPairsTarget.find { it.first == selectedTargetWord && it.second == meaning }
                                    if (match != null) {
                                        onPairMatched(selectedTargetWord!!, meaning)
                                    }
                                    selectedTargetWord = null
                                    selectedMeaning = null
                                }
                            }
                        )
                    }

                    ExerciseType.SPEAKING -> {
                        SpeakingView(
                            exercise = exercise,
                            isSpeakingSimulated = isSpeakingSimulated,
                            onStartSpeaking = { isSpeakingSimulated = true },
                            onPlayAudio = { onPlayAudio(false) }
                        )
                    }
                }
            }
        }

        // Bottom Action Button
        if (!isAnswerChecked) {
            val isButtonEnabled = when (exercise.type) {
                ExerciseType.NEW_WORD_INTRO -> true
                ExerciseType.MULTIPLE_CHOICE_WORD_TO_MEANING,
                ExerciseType.MULTIPLE_CHOICE_MEANING_TO_WORD,
                ExerciseType.LISTENING_DRILL -> selectedChoice != null
                ExerciseType.WORD_BANK_TRANSLATION -> placedWords.isNotEmpty()
                ExerciseType.FREE_TYPING -> typedAnswer.isNotBlank()
                ExerciseType.MATCH_PAIRS -> completedMatches.size >= exercise.matchPairsTarget.size
                ExerciseType.SPEAKING -> true
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                DuoButton(
                    text = if (exercise.type == ExerciseType.NEW_WORD_INTRO) "READY TO PRACTICE 🌱" else "CHECK ANSWER",
                    onClick = onCheckAnswer,
                    enabled = isButtonEnabled,
                    type = DuoButtonType.PRIMARY,
                    testTag = "check_answer_button"
                )
            }
        }

        // Feedback Sheet
        if (isAnswerChecked && exercise.type != ExerciseType.NEW_WORD_INTRO) {
            FeedbackBottomSheet(
                visible = isAnswerChecked,
                isCorrect = isAnswerCorrect,
                correctSolution = correctSolution,
                encouragementMessage = encouragementTip,
                onContinue = onContinue,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// 1. New Word Intro View
@Composable
private fun NewWordIntroView(
    exercise: ExerciseItem,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BloomPeach)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🌱 NEW WORD INTRO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = BloomCoral
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = exercise.word.word,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exercise.word.phonetic,
                    fontSize = 16.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = onPlayAudio,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BloomPeach)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen",
                        tint = BloomCoral,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = exercise.word.translation,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BloomCoral
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Example: ${exercise.word.exampleSentence}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "(${exercise.word.exampleTranslation})",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                if (exercise.word.teacherTip.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = BloomAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exercise.word.teacherTip,
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 2. Multiple Choice View
@Composable
private fun MultipleChoiceView(
    exercise: ExerciseItem,
    selectedChoice: String?,
    onChoiceSelected: (String) -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = exercise.promptText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Target word presentation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BloomPeach)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Play",
                    tint = BloomCoral
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = exercise.word.word,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                if (exercise.phonetic.isNotBlank()) {
                    Text(
                        text = exercise.phonetic,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Choices
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            exercise.multipleChoiceOptions.forEach { choice ->
                val isSelected = selectedChoice == choice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            2.dp,
                            if (isSelected) BloomCoral else Color(0xFFE2E8F0),
                            RoundedCornerShape(14.dp)
                        )
                        .background(if (isSelected) BloomPeach.copy(alpha = 0.5f) else Color.White)
                        .clickable { onChoiceSelected(choice) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = choice,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = BloomCoral
                        )
                    }
                }
            }
        }
    }
}

// 3. Listening Drill View
@Composable
private fun ListeningDrillView(
    exercise: ExerciseItem,
    selectedChoice: String?,
    onChoiceSelected: (String) -> Unit,
    onPlayNormal: () -> Unit,
    onPlaySlow: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tap what you hear",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Large Audio Buttons: Normal + Slow Turtle (0.6x)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onPlayNormal,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(BloomCoral)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Normal Speed",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Normal", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onPlaySlow,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(BloomPeach)
                ) {
                    Icon(
                        imageVector = Icons.Default.SlowMotionVideo,
                        contentDescription = "Slow Speed 0.6x",
                        tint = BloomCoral,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Slow (0.6x) 🐢", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 4 Options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            exercise.multipleChoiceOptions.forEach { choice ->
                val isSelected = selectedChoice == choice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            2.dp,
                            if (isSelected) BloomCoral else Color(0xFFE2E8F0),
                            RoundedCornerShape(14.dp)
                        )
                        .background(if (isSelected) BloomPeach.copy(alpha = 0.5f) else Color.White)
                        .clickable { onChoiceSelected(choice) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = choice,
                        fontSize = 17.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = BloomCoral
                        )
                    }
                }
            }
        }
    }
}

// 4. Free Typing View
@Composable
private fun FreeTypingView(
    exercise: ExerciseItem,
    typedAnswer: String,
    onTypedAnswerChanged: (String) -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = exercise.promptText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BloomPeach)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Listen",
                    tint = BloomCoral,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Hint Phonetic: ${exercise.phonetic}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "✨ Lenient matching: missing accents or case won't penalize you!",
                    fontSize = 11.sp,
                    color = BloomEmerald,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = typedAnswer,
            onValueChange = onTypedAnswerChanged,
            placeholder = { Text("Type your translation here...") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            singleLine = true
        )
    }
}

// 5. Word Bank Translation View
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordBankTranslationView(
    exercise: ExerciseItem,
    placedWords: List<String>,
    onWordPlaced: (String) -> Unit,
    onWordRemoved: (Int) -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = exercise.promptText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Target sentence display with audio
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BloomPeach)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Play",
                    tint = BloomCoral
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = exercise.word.word,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = exercise.word.phonetic,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Placed tray
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(14.dp)
        ) {
            if (placedWords.isEmpty()) {
                Text(
                    text = "Tap words from the bank below to construct translation",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    placedWords.forEachIndexed { index, token ->
                        WordChip(
                            text = token,
                            onClick = { onWordRemoved(index) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Available Bank
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            exercise.bankOptions.forEach { token ->
                val countInPlaced = placedWords.count { it == token }
                val countInBank = exercise.bankOptions.count { it == token }
                val isAvailable = countInPlaced < countInBank

                WordChip(
                    text = token,
                    onClick = { if (isAvailable) onWordPlaced(token) },
                    isUsed = !isAvailable
                )
            }
        }
    }
}

// 6. Match Pairs View
@Composable
private fun MatchPairsView(
    pairs: List<Pair<String, String>>,
    completedMatches: Map<String, String>,
    selectedTargetWord: String?,
    selectedMeaning: String?,
    onSelectTargetWord: (String) -> Unit,
    onSelectMeaning: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Match the Pairs",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = "Tap a word and its matching meaning",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Target Words Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pairs.forEach { (targetWord, _) ->
                    val isMatched = completedMatches.containsKey(targetWord)
                    val isSelected = selectedTargetWord == targetWord

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                when {
                                    isMatched -> BloomEmerald
                                    isSelected -> BloomCoral
                                    else -> Color(0xFFE2E8F0)
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                when {
                                    isMatched -> Color(0xFFECFDF5)
                                    isSelected -> BloomPeach
                                    else -> Color.White
                                }
                            )
                            .clickable(enabled = !isMatched) { onSelectTargetWord(targetWord) }
                            .padding(vertical = 14.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = targetWord,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) BloomEmerald else Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Meanings Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pairs.map { it.second }.shuffled(java.util.Random(42)).forEach { meaning ->
                    val isMatched = completedMatches.values.contains(meaning)
                    val isSelected = selectedMeaning == meaning

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                when {
                                    isMatched -> BloomEmerald
                                    isSelected -> BloomCoral
                                    else -> Color(0xFFE2E8F0)
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                when {
                                    isMatched -> Color(0xFFECFDF5)
                                    isSelected -> BloomPeach
                                    else -> Color.White
                                }
                            )
                            .clickable(enabled = !isMatched) { onSelectMeaning(meaning) }
                            .padding(vertical = 14.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = meaning,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) BloomEmerald else Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }
}

// 7. Speaking "Repeat Aloud" View
@Composable
private fun SpeakingView(
    exercise: ExerciseItem,
    isSpeakingSimulated: Boolean,
    onStartSpeaking: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Repeat Aloud",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Presentation
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onPlayAudio,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BloomPeach)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen to pronunciation",
                        tint = BloomCoral,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = exercise.word.word,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = exercise.word.phonetic,
                    fontSize = 15.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exercise.word.translation,
                    fontSize = 16.sp,
                    color = BloomCoral,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mic Button
        IconButton(
            onClick = onStartSpeaking,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(if (isSpeakingSimulated) BloomEmerald else BloomCoral)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Tap to speak",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isSpeakingSimulated) "Listening & grading speech... ✨" else "Tap microphone to speak",
            fontSize = 13.sp,
            color = if (isSpeakingSimulated) BloomEmerald else Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
