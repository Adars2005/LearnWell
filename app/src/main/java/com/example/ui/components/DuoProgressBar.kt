package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoYellow

@Composable
fun DuoProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    barColor: Color = DuoGreen
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "progress"
    )

    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(DuoGrayBorder)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(barColor)
            ) {
                // Top subtle gloss shine highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height * 0.35f)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(shape)
                        .background(Color.White.copy(alpha = 0.35f))
                )
            }
        }
    }
}
