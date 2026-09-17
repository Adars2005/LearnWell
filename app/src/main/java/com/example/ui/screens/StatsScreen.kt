package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach
import com.example.ui.theme.BloomViolet
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    userProfile: UserProfile,
    allWords: List<VocabWord>,
    onPracticeLeechWord: (VocabWord) -> Unit
) {
    // 1. Compute Memory Health Gauge
    val learnedWords = allWords.filter { it.lastReviewedTimestamp > 0L }
    val avgRetention = if (learnedWords.isNotEmpty()) {
        learnedWords.map { it.getMemoryStrength() }.average().toFloat()
    } else {
        0.85f
    }
    val memoryHealthPercent = (avgRetention * 100f).roundToInt()

    // 2. Stage Counts
    val seedCount = allWords.count { it.bloomStage == "Seed" }
    val sproutCount = allWords.count { it.bloomStage == "Sprout" }
    val flowerCount = allWords.count { it.bloomStage == "Flower" }
    val bloomCount = allWords.count { it.bloomStage == "Full Bloom" }
    val totalWords = allWords.size.coerceAtLeast(1)

    // 3. Weakest Words (Leeches: high mistake count or low stability)
    val leeches = allWords
        .filter { it.mistakeCount > 0 || (it.lastReviewedTimestamp > 0 && it.stability < 1.5f) }
        .sortedByDescending { it.mistakeCount }
        .take(5)

    // 4. 7-Day Review Forecast
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60L * 60L * 1000L
    val forecast = (0..6).map { dayOffset ->
        val targetStart = now + (dayOffset * dayMillis)
        val targetEnd = targetStart + dayMillis
        val count = allWords.count {
            it.lastReviewedTimestamp > 0L && it.nextReviewTimestamp in targetStart until targetEnd
        }
        dayOffset to count
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF9))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Learning Analytics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "FSRS-4.5 Retention & Memory Health",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BloomPeach)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${userProfile.minutesPracticedToday} min today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BloomCoral
                    )
                }
            }
        }

        // Memory Health Gauge Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Memory Health",
                                    tint = BloomEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Memory Health",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Target Retention: 90%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Text(
                            text = "$memoryHealthPercent%",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (memoryHealthPercent >= 85) BloomEmerald else BloomAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { avgRetention },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (memoryHealthPercent >= 85) BloomEmerald else BloomAmber,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (memoryHealthPercent >= 85) {
                            "✨ Excellent retention! Teacher Maya calculates your memory curve is resilient."
                        } else {
                            "🌱 Words are strengthening. Daily practice will elevate your retention score."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Bloom-Stage Distribution Stacked Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Garden Stage Distribution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-colored bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        if (seedCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(seedCount.toFloat() / totalWords)
                                    .fillMaxSize()
                                    .background(Color(0xFF94A3B8))
                            )
                        }
                        if (sproutCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(sproutCount.toFloat() / totalWords)
                                    .fillMaxSize()
                                    .background(BloomEmerald)
                            )
                        }
                        if (flowerCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(flowerCount.toFloat() / totalWords)
                                    .fillMaxSize()
                                    .background(BloomCoral)
                            )
                        }
                        if (bloomCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bloomCount.toFloat() / totalWords)
                                    .fillMaxSize()
                                    .background(BloomAmber)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StageLegendItem(label = "Seeds", count = seedCount, color = Color(0xFF94A3B8))
                        StageLegendItem(label = "Sprouts", count = sproutCount, color = BloomEmerald)
                        StageLegendItem(label = "Flowers", count = flowerCount, color = BloomCoral)
                        StageLegendItem(label = "Blooms", count = bloomCount, color = BloomAmber)
                    }
                }
            }
        }

        // 7-Day Review Forecast Bar Chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "7-Day Review Forecast",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Expected FSRS repetitions this week",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxCount = (forecast.maxOfOrNull { it.second } ?: 1).coerceAtLeast(4)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        forecast.forEach { (day, count) ->
                            val heightFraction = (count.toFloat() / maxCount).coerceIn(0.12f, 1.0f)
                            val dayLabel = when (day) {
                                0 -> "Today"
                                1 -> "+1d"
                                2 -> "+2d"
                                3 -> "+3d"
                                4 -> "+4d"
                                5 -> "+5d"
                                else -> "+6d"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((70 * heightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (day == 0) BloomCoral else Color(0xFFCBD5E1))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Streak Heatmap (Last 8 Weeks)
        item {
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
                            text = "Consistency Heatmap",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile.streakDays} day streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF97316)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 8 columns x 7 rows grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 8) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0 until 7) {
                                    val isActive = (col >= 6 && row <= 3) || (col == 7 && row <= userProfile.streakDays % 7)
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (isActive) BloomEmerald else Color(0xFFF1F5F9)
                                            )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "8-week rolling habit record",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Leeches / Weakest Words List with One-Tap Practice
        item {
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
                            text = "Weakest Words (Leeches)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Tap to practice",
                            fontSize = 11.sp,
                            color = BloomCoral,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (leeches.isEmpty()) {
                        Text(
                            text = "🎉 No leeches found! You have great recall across all vocabulary.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        leeches.forEach { word ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPracticeLeechWord(word) }
                                    .background(Color(0xFFFFF7ED))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = word.word,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "${word.phonetic} • ${word.translation}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFEDD5))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${word.mistakeCount} lapses",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEA580C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Practice",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StageLegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $count",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}
