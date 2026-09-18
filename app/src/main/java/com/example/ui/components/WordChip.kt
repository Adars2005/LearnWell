package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGrayBorderDark
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGrayTextDark

@Composable
fun WordChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUsed: Boolean = false,
    testTag: String = "word_chip"
) {
    val shape = RoundedCornerShape(12.dp)

    if (isUsed) {
        // Muted placeholder in word bank
        Box(
            modifier = modifier
                .height(44.dp)
                .clip(shape)
                .background(DuoGrayLight)
                .border(2.dp, DuoGrayBorder, shape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Transparent, // Hidden text to keep width
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val pressOffset by animateDpAsState(
            targetValue = if (isPressed) 2.dp else 0.dp,
            label = "chip_offset"
        )

        Box(
            modifier = modifier
                .height(44.dp)
                .testTag(testTag)
                .clip(shape)
                .background(DuoGrayBorderDark)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Box(
                modifier = Modifier
                    .height(41.dp)
                    .offset(y = pressOffset)
                    .clip(shape)
                    .background(Color.White)
                    .border(2.dp, DuoGrayBorder, shape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = DuoGrayTextDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
