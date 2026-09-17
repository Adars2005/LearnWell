package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGrayBorderDark
import com.example.ui.theme.DuoGrayTextDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed

@Composable
fun DuoLessonTopBar(
    progress: Float,
    hearts: Int,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier.testTag("close_exercise_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close lesson",
                tint = DuoGrayBorderDark,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            DuoProgressBar(progress = progress)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Hearts remaining",
                tint = DuoRed,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$hearts",
                color = DuoRed,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun DuoHomeTopBar(
    targetLanguage: String,
    streakDays: Int,
    gems: Int,
    hearts: Int,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Language Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, DuoGrayBorderDark, RoundedCornerShape(12.dp))
                .clickable { onLanguageClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (targetLanguage.lowercase()) {
                        "hindi" -> "🇮🇳 Hindi"
                        "spanish" -> "🇪🇸 Spanish"
                        "french" -> "🇫🇷 French"
                        "japanese" -> "🇯🇵 Japanese"
                        else -> "🌍 $targetLanguage"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DuoGrayTextDark
                )
            }
        }

        // Streak Fire
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = DuoOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streakDays",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = DuoOrange
            )
        }

        // Gems / Blue Shield
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Gems",
                tint = DuoBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$gems",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = DuoBlue
            )
        }

        // Hearts
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Hearts",
                tint = DuoRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$hearts",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = DuoRed
            )
        }
    }
}
