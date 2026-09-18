package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabWord
import com.example.data.srs.SpacedRepetitionEngine
import com.example.ui.components.DuoProgressBar
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueLight
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGrayBorderDark
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGrayText
import com.example.ui.theme.DuoGrayTextDark
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoYellow
import kotlin.math.roundToInt

@Composable
fun VocabVaultScreen(
    words: List<VocabWord>,
    targetLanguage: String,
    onBack: () -> Unit,
    onPlayAudio: (text: String, isSlow: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Due", "Learning", "Mastered"

    val filteredWords = remember(words, searchQuery, selectedFilter) {
        words.filter { word ->
            val matchesQuery = word.word.contains(searchQuery, ignoreCase = true) ||
                    word.translation.contains(searchQuery, ignoreCase = true) ||
                    word.phonetic.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Due" -> word.isDueForReview
                "Learning" -> word.repetitions in 1..2
                "Mastered" -> word.repetitions >= 3
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DuoGrayLight)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DuoGrayTextDark,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Memory Vault",
                        color = DuoGrayTextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$targetLanguage Spaced Repetition Deck (${words.size} words)",
                        color = DuoGrayText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = DuoBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search word or translation...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = DuoGrayText)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Due", "Learning", "Mastered")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = when (filter) {
                                        "Due" -> "⏰ Due (${words.count { it.isDueForReview }})"
                                        "Learning" -> "🌱 Learning (${words.count { it.repetitions in 1..2 }})"
                                        "Mastered" -> "⭐ Mastered (${words.count { it.repetitions >= 3 }})"
                                        else -> "All (${words.size})"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuoBlueLight,
                                selectedLabelColor = DuoBlue
                            )
                        )
                    }
                }
            }

            // Word Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredWords, key = { it.id }) { word ->
                    VocabVaultCard(
                        word = word,
                        onPlayNormal = { onPlayAudio(word.audioPrompt.ifEmpty { word.word }, false) },
                        onPlaySlow = { onPlayAudio(word.audioPrompt.ifEmpty { word.word }, true) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun VocabVaultCard(
    word: VocabWord,
    onPlayNormal: () -> Unit,
    onPlaySlow: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    var isFlipped by remember { mutableStateOf(false) }
    val memoryStrength = word.getMemoryStrength()
    val retentionPct = (memoryStrength * 100).roundToInt()

    val stageLabel = SpacedRepetitionEngine.getStageLabel(word.intervalDays, word.repetitions)
    val stageColor = when {
        word.repetitions == 0 -> DuoGrayText
        word.intervalDays <= 1 -> DuoYellow
        word.intervalDays <= 7 -> DuoBlue
        else -> DuoGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(2.dp, DuoGrayBorder, shape)
            .clickable { isFlipped = !isFlipped }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Word, Phonetic, Audio Buttons, Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.word,
                        color = DuoGrayTextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = word.phonetic,
                        color = DuoGrayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Stage Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stageColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stageLabel,
                        color = stageColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Normal Audio
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DuoBlueLight)
                        .clickable { onPlayNormal() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Pronounce",
                        tint = DuoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Slow Audio (Turtle)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DuoBlueLight)
                        .clickable { onPlaySlow() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SlowMotionVideo,
                        contentDescription = "Slow pronounce",
                        tint = DuoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meaning & Translation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meaning: ",
                    color = DuoGrayText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = word.translation,
                    color = DuoGrayTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (word.exampleSentence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DuoGrayLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "“${word.exampleSentence}”",
                            color = DuoGrayTextDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = word.exampleTranslation,
                            color = DuoGrayText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Teacher Maya's Memory Tip
            if (word.teacherTip.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "💡 ${word.teacherTip}",
                        color = Color(0xFF78350F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Memory Retention Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Memory Retention",
                    color = DuoGrayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$retentionPct%",
                    color = if (retentionPct >= 70) DuoGreen else if (retentionPct >= 40) DuoYellow else DuoRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            DuoProgressBar(
                progress = memoryStrength,
                height = 8.dp,
                barColor = if (retentionPct >= 70) DuoGreen else if (retentionPct >= 40) DuoYellow else DuoRed
            )
        }
    }
}
