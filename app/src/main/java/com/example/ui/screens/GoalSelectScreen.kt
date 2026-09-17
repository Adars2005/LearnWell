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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.R
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonType
import com.example.ui.components.DuoProgressBar
import com.example.ui.components.MascotSpeechBubble
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueLight
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGrayBorderDark
import com.example.ui.theme.DuoGrayText
import com.example.ui.theme.DuoGrayTextDark

data class GoalOption(
    val minutes: Int,
    val words: Int,
    val title: String,
    val subtitle: String
)

@Composable
fun GoalSelectScreen(
    onGoalConfirmed: (minutes: Int, words: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember {
        listOf(
            GoalOption(3, 5, "3 min / day", "Casual"),
            GoalOption(10, 10, "10 min / day", "Regular"),
            GoalOption(15, 15, "15 min / day", "Serious"),
            GoalOption(30, 25, "30 min / day", "Intense")
        )
    }

    var selectedIndex by remember { mutableIntStateOf(2) } // default 15 min / day Serious as in Screenshot 2

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // Top Bar with Back Arrow and Segmented Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DuoGrayText,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                DuoProgressBar(
                    progress = 0.35f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Teacher Maya with Speech Bubble
            MascotSpeechBubble(
                imageRes = R.drawable.img_teacher_avatar,
                text = "What's your daily goal? I'll schedule words right before you forget them!",
                avatarSize = 85.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex
                    val shape = RoundedCornerShape(16.dp)

                    val borderColor = if (isSelected) DuoBlue else DuoGrayBorder
                    val bgColor = if (isSelected) DuoBlueLight.copy(alpha = 0.35f) else Color.White
                    val textColor = if (isSelected) DuoBlue else DuoGrayTextDark
                    val labelColor = if (isSelected) DuoBlue else DuoGrayText

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("goal_option_${option.minutes}")
                            .clip(shape)
                            .background(bgColor)
                            .border(2.dp, borderColor, shape)
                            .clickable { selectedIndex = index }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.title,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = option.subtitle,
                                color = labelColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Bottom Committed Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            DuoButton(
                text = "I'M COMMITTED",
                onClick = {
                    val chosen = options[selectedIndex]
                    onGoalConfirmed(chosen.minutes, chosen.words)
                },
                type = DuoButtonType.PRIMARY,
                testTag = "goal_committed_button"
            )
        }
    }
}
