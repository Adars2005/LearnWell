package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueDark
import com.example.ui.theme.DuoGrayBorder
import com.example.ui.theme.DuoGrayBorderDark
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGrayText
import com.example.ui.theme.DuoGrayTextDark
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedDark

enum class DuoButtonType {
    PRIMARY,    // Green
    SECONDARY,  // Blue
    OUTLINED,   // White with gray border
    DANGER,     // Red
    GHOST       // Transparent
}

@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: DuoButtonType = DuoButtonType.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "duo_button",
    height: Dp = 52.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = if (!enabled) {
        DuoGrayBorder
    } else {
        when (type) {
            DuoButtonType.PRIMARY -> DuoGreen
            DuoButtonType.SECONDARY -> DuoBlue
            DuoButtonType.DANGER -> DuoRed
            DuoButtonType.OUTLINED -> Color.White
            DuoButtonType.GHOST -> Color.Transparent
        }
    }

    val bottomShadowColor = if (!enabled) {
        DuoGrayBorderDark
    } else {
        when (type) {
            DuoButtonType.PRIMARY -> DuoGreenDark
            DuoButtonType.SECONDARY -> DuoBlueDark
            DuoButtonType.DANGER -> DuoRedDark
            DuoButtonType.OUTLINED -> DuoGrayBorderDark
            DuoButtonType.GHOST -> Color.Transparent
        }
    }

    val textColor = when {
        !enabled -> DuoGrayText
        type == DuoButtonType.OUTLINED -> DuoGrayTextDark
        type == DuoButtonType.GHOST -> DuoGreenDark
        else -> Color.White
    }

    val shape = RoundedCornerShape(16.dp)
    val shadowHeight = if (type == DuoButtonType.GHOST) 0.dp else 4.dp
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 0.dp,
        label = "press_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag(testTag)
            .clip(shape)
            .background(bottomShadowColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Top surface layer that slides down when pressed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - shadowHeight)
                .offset(y = pressOffset)
                .clip(shape)
                .background(topColor)
                .then(
                    if (type == DuoButtonType.OUTLINED && enabled) {
                        Modifier.border(2.dp, DuoGrayBorderDark, shape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}
