package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonType
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomBackground
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomCoralDark
import com.example.ui.theme.BloomCoralLight
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach
import com.example.ui.theme.BloomTextDark
import com.example.ui.theme.BloomTextMuted
import com.example.ui.theme.BloomViolet

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onAlreadyHaveAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BloomBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Centered Brand Content with Teacher Maya
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Teacher Avatar in layered orbital rings (matching user uploaded photo)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(BloomPeach.copy(alpha = 0.5f))
                    .border(3.dp, BloomCoral.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(172.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, BloomCoralLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_teacher_avatar),
                        contentDescription = "Teacher Maya",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mentor Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BloomCoralLight)
                    .border(1.dp, BloomCoral.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "MEET TEACHER MAYA",
                    color = BloomCoralDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "LinguaBloom",
                color = BloomTextDark,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Grow your vocabulary through spaced repetition & loving mentorship",
                color = BloomTextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlights
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureChip("🌱 Memory Garden")
                FeatureChip("🧠 SM-2 Algorithm")
                FeatureChip("💡 Mentor Tips")
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            DuoButton(
                text = "START LEARNING",
                onClick = onGetStarted,
                type = DuoButtonType.PRIMARY,
                testTag = "welcome_get_started_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DuoButton(
                text = "I ALREADY HAVE AN ACCOUNT",
                onClick = onAlreadyHaveAccount,
                type = DuoButtonType.OUTLINED,
                testTag = "welcome_have_account_button"
            )
        }
    }
}

@Composable
private fun FeatureChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BloomTextDark
        )
    }
}
