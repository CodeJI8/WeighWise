package com.noboj.weighwise.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AnimatedBar(
    score: Float, // 0 to 100
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    isWinner: Boolean = false
) {
    var started by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        started = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (started) score / 100f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "barProgress"
    )
    
    val animatedScore = (animatedProgress * 100).roundToInt()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.SemiBold
                )
                if (isWinner) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("★", color = MaterialTheme.colorScheme.secondary)
                }
            }
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isWinner) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
            )
        }
    }
}
