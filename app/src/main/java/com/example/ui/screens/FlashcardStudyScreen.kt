package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabWord
import com.example.data.srs.FsrsRating
import com.example.data.srs.SpacedRepetitionEngine
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach

@Composable
fun FlashcardStudyScreen(
    words: List<VocabWord>,
    onRateWord: (VocabWord, FsrsRating) -> Unit,
    onPlayAudio: (String, Float) -> Unit,
    onClose: () -> Unit
) {
    if (words.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBF9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎉", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "All Caught Up!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No flashcards due right now.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = BloomCoral),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Learn")
                }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val currentWord = words[currentIndex.coerceIn(0, words.size - 1)]

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    fun moveTo(index: Int) {
        currentIndex = index.coerceIn(0, words.lastIndex)
        isFlipped = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF9))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
            }

            Text(
                text = "${currentIndex + 1} of ${words.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BloomPeach)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentWord.bloomIcon + " " + currentWord.bloomStage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BloomCoral
                )
            }
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / words.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BloomCoral,
            trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Area (Clickable to flip)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(currentIndex, words.size) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount -> dragDistance += dragAmount },
                        onDragEnd = {
                            when {
                                dragDistance < -80f && currentIndex < words.lastIndex -> moveTo(currentIndex + 1)
                                dragDistance > 80f && currentIndex > 0 -> moveTo(currentIndex - 1)
                            }
                            dragDistance = 0f
                        }
                    )
                }
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front of card
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentWord.word,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentWord.phonetic,
                            fontSize = 16.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        IconButton(
                            onClick = { onPlayAudio(currentWord.word, 1.0f) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BloomPeach)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = BloomCoral
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tap to reveal • Swipe left/right to change card",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    // Back of card (counter-rotate text so it isn't mirrored)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentWord.translation,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BloomCoral,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentWord.exampleSentence,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentWord.exampleTranslation,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        if (currentWord.teacherTip.isNotBlank()) {
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
                                        text = currentWord.teacherTip,
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

        Spacer(modifier = Modifier.height(20.dp))

        // FSRS 4-Button Grading Row (Revealed when card is flipped or tap to answer)
        Column {
            Text(
                text = "How well did you remember?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Again (1)
                RatingButton(
                    rating = FsrsRating.AGAIN,
                    intervalLabel = "<1d",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                ) {
                    onRateWord(currentWord, FsrsRating.AGAIN)
                    moveTo(currentIndex + 1)
                }

                // Hard (2)
                RatingButton(
                    rating = FsrsRating.HARD,
                    intervalLabel = "1d",
                    color = Color(0xFFF97316),
                    modifier = Modifier.weight(1f)
                ) {
                    onRateWord(currentWord, FsrsRating.HARD)
                    moveTo(currentIndex + 1)
                }

                // Good (3)
                RatingButton(
                    rating = FsrsRating.GOOD,
                    intervalLabel = "${currentWord.intervalDays.coerceAtLeast(2)}d",
                    color = BloomEmerald,
                    modifier = Modifier.weight(1f)
                ) {
                    onRateWord(currentWord, FsrsRating.GOOD)
                    moveTo(currentIndex + 1)
                }

                // Easy (4)
                RatingButton(
                    rating = FsrsRating.EASY,
                    intervalLabel = "${(currentWord.intervalDays * 2).coerceAtLeast(4)}d",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                ) {
                    onRateWord(currentWord, FsrsRating.EASY)
                    moveTo(currentIndex + 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RatingButton(
    rating: FsrsRating,
    intervalLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = rating.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = intervalLabel,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
