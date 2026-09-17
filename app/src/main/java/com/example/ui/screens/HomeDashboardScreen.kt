package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import com.example.ui.components.DuoHomeTopBar
import com.example.ui.components.MascotSpeechBubble
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomBackground
import com.example.ui.theme.BloomBorder
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomCoralDark
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach
import com.example.ui.theme.BloomTextDark
import com.example.ui.theme.BloomTextMuted
import com.example.voice.model.VoiceMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver

@Composable
fun HomeDashboardScreen(
    userProfile: UserProfile,
    allWords: List<VocabWord>,
    dueWords: List<VocabWord>,
    mistakeWords: List<VocabWord>,
    onStartDailyPractice: () -> Unit,
    onStartNewWordsPractice: () -> Unit,
    onStartMistakesReview: () -> Unit,
    onStartFlashcards: () -> Unit,
    onOpenVocabVault: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onRepairStreak: () -> Unit,
    onClaimQuest: (Int) -> Unit,
    onOpenVoiceAssistant: (VoiceMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showLanguagePicker by remember { mutableStateOf(false) }

    val languages = listOf("Hindi", "Spanish", "French", "Japanese", "German", "Italian", "Korean", "Mandarin")
    val dueCount = dueWords.size
    val newCount = allWords.count { it.repetitions == 0 }.coerceAtLeast(3)
    val estimatedMinutes = ((dueCount + newCount) * 0.8).toInt().coerceAtLeast(4)

    val seedsCount = allWords.count { it.repetitions == 0 }
    val sproutsCount = allWords.count { it.repetitions > 0 && it.intervalDays <= 2 }
    val flowersCount = allWords.count { it.intervalDays in 3..7 }
    val bloomsCount = allWords.count { it.intervalDays > 7 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BloomBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            DuoHomeTopBar(
                targetLanguage = userProfile.targetLanguage,
                streakDays = userProfile.streakDays,
                gems = userProfile.gems,
                hearts = if (userProfile.relaxedMode) 999 else userProfile.hearts,
                onLanguageClick = { showLanguagePicker = !showLanguagePicker },
                modifier = Modifier.background(Color.White)
            )

            // Language Picker Dropdown
            if (showLanguagePicker) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Switch Target Language",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            languages.take(4).forEach { lang ->
                                val sel = userProfile.targetLanguage.equals(lang, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) BloomCoral else Color(0xFFF1F5F9))
                                        .clickable {
                                            onSelectLanguage(lang)
                                            showLanguagePicker = false
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sel) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            languages.drop(4).forEach { lang ->
                                val sel = userProfile.targetLanguage.equals(lang, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) BloomCoral else Color(0xFFF1F5F9))
                                        .clickable {
                                            onSelectLanguage(lang)
                                            showLanguagePicker = false
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sel) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Comeback Flow Banner (if streak was broken)
                if (userProfile.isStreakBroken) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌱", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Your streak may be gone, but your progress isn't!",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9A3412)
                                    )
                                    Text(
                                        text = "${userProfile.totalXp} XP earned • ${allWords.size} words in memory",
                                        fontSize = 12.sp,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = onRepairStreak,
                                    enabled = userProfile.gems >= 200,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Repair Streak (200 💎)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Teacher Maya Mentorship Greeting
                MascotSpeechBubble(
                    imageRes = R.drawable.img_teacher_avatar,
                    text = "Welcome to your ${userProfile.targetLanguage} garden! Today's FSRS session keeps memory retention locked at 90%.",
                    avatarSize = 72.dp,
                    mentorName = "Teacher Maya",
                    audioSubtext = "Level ${userProfile.learningLevel} • ${userProfile.streakDays} days blooming"
                )

                // 1. "Today's Plan" Hero Card with Big CTA
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BloomCoral),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TODAY'S FSRS PLAN",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp
                                )
                            }

                            Text(
                                text = "≈ $estimatedMinutes min",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (dueCount > 0) {
                            Text(
                                text = "Review $dueCount Due + Learn $newCount New",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Interleaved exercises with Teacher Maya for optimal recall.",
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 13.sp
                            )
                        } else {
                            // "All caught up 🎉" state
                            Text(
                                text = "All Caught Up! 🎉",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No cards due right now. Ready to practice ahead or review mistakes?",
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (dueCount > 0) onStartDailyPractice() else onStartNewWordsPractice()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = BloomCoral,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (dueCount > 0) "START TODAY'S SESSION" else "PRACTICE AHEAD 🌱",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = BloomCoral
                            )
                        }
                    }
                }

                // AI Voice Companion Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(BloomPeach),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice AI",
                                        tint = BloomCoral,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Practice Speaking with Maya",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BloomTextDark
                                    )
                                    Text(
                                        text = "Interactive AI voice tutor & speaking coach",
                                        fontSize = 12.sp,
                                        color = BloomTextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Mode Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Friend" to VoiceMode.FRIEND,
                                "Tutor" to VoiceMode.TUTOR,
                                "Speaking" to VoiceMode.SPEAKING_PRACTICE,
                                "Interview" to VoiceMode.INTERVIEW
                            ).forEach { (label, mode) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BloomPeach.copy(alpha = 0.6f))
                                        .border(1.dp, BloomBorder, RoundedCornerShape(12.dp))
                                        .clickable { onOpenVoiceAssistant(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BloomCoral
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Daily Goal Progress Ring / Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Goal Progress",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${userProfile.todayWordsPracticed}/${userProfile.dailyGoalWords} words • ${userProfile.minutesPracticedToday}/${userProfile.dailyGoalMinutes} min",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            val fraction = (userProfile.todayWordsPracticed.toFloat() / userProfile.dailyGoalWords.coerceAtLeast(1))
                                .coerceIn(0f, 1f)
                            Text(
                                text = "${(fraction * 100).toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BloomEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = {
                                (userProfile.todayWordsPracticed.toFloat() / userProfile.dailyGoalWords.coerceAtLeast(1)).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BloomEmerald,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }

                // 3. Daily Quests Card with Claimable Gem Rewards
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Quests",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Resets Daily",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quest 1: Earn 30 XP
                        QuestRow(
                            title = "Earn 30 XP",
                            progressText = "${userProfile.questXpEarned}/30 XP",
                            isComplete = userProfile.questXpEarned >= 30,
                            isClaimed = userProfile.quest1Claimed,
                            gemReward = 25,
                            onClaim = { onClaimQuest(1) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quest 2: Get 8 correct
                        QuestRow(
                            title = "Get 8 Correct Answers",
                            progressText = "${userProfile.questCorrectAnswers}/8 correct",
                            isComplete = userProfile.questCorrectAnswers >= 8,
                            isClaimed = userProfile.quest2Claimed,
                            gemReward = 40,
                            onClaim = { onClaimQuest(2) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quest 3: Review 5 words
                        QuestRow(
                            title = "Review 5 Vocab Words",
                            progressText = "${userProfile.questWordsReviewed}/5 words",
                            isComplete = userProfile.questWordsReviewed >= 5,
                            isClaimed = userProfile.quest3Claimed,
                            gemReward = 50,
                            onClaim = { onClaimQuest(3) }
                        )
                    }
                }

                // 4. Memory Garden Widget with Stages & Customization info
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Yard,
                                    contentDescription = "Garden",
                                    tint = BloomEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Memory Garden",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Pot: ${userProfile.gardenPot} • Companion: ${userProfile.gardenCompanion}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Text(
                                text = "${allWords.size} words",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BloomEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GardenStagePill(label = "Seeds", count = seedsCount, icon = "🌱", modifier = Modifier.weight(1f))
                            GardenStagePill(label = "Sprouts", count = sproutsCount, icon = "🌿", modifier = Modifier.weight(1f))
                            GardenStagePill(label = "Flowers", count = flowersCount, icon = "🌸", modifier = Modifier.weight(1f))
                            GardenStagePill(label = "Blooms", count = bloomsCount, icon = "🌺", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 5. Quick Modes: Flashcards Study Mode & Mistake Rehabilitation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Flashcards Card
                    Card(
                        onClick = onStartFlashcards,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = "Flashcards",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Study Flashcards",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "FSRS 4-button grading",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Mistake Rehab Card
                    Card(
                        onClick = onStartMistakesReview,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BloomPeach),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Mistakes",
                                    tint = BloomCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Mistake Rehab",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "${mistakeWords.size} words to heal",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuestRow(
    title: String,
    progressText: String,
    isComplete: Boolean,
    isClaimed: Boolean,
    gemReward: Int,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = progressText,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        when {
            isClaimed -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Claimed ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BloomEmerald)
                }
            }
            isComplete -> {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = BloomEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Claim +$gemReward 💎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+$gemReward 💎", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun GardenStagePill(
    label: String,
    count: Int,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}
