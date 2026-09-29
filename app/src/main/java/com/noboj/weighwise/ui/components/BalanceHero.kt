package com.noboj.weighwise.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BalanceHero(
    tiltValue: Float, // -1f (left full tilt) to 1f (right full tilt)
    modifier: Modifier = Modifier,
    leftColor: Color = MaterialTheme.colorScheme.primary,
    rightColor: Color = MaterialTheme.colorScheme.secondary
) {
    val animatedTilt by animateFloatAsState(
        targetValue = tiltValue.coerceIn(-1f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "balanceTilt"
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(160.dp)) {
        val pivotX = size.width / 2f
        val pivotY = size.height - 20.dp.toPx()
        val beamLength = size.width * 0.6f
        val beamY = pivotY - 80.dp.toPx()
        
        // Pivot stand
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(pivotX - 16.dp.toPx(), pivotY)
                lineTo(pivotX + 16.dp.toPx(), pivotY)
                lineTo(pivotX, beamY - 10.dp.toPx())
                close()
            },
            color = onSurfaceVariant,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = onSurface,
            radius = 6.dp.toPx(),
            center = Offset(pivotX, beamY)
        )

        val maxAngle = 25f // degrees
        val angle = animatedTilt * maxAngle
        val angleRad = angle * (PI / 180f)

        withTransform({
            rotate(degrees = angle, pivot = Offset(pivotX, beamY))
        }) {
            // Beam
            drawLine(
                color = onSurface,
                start = Offset(pivotX - beamLength / 2, beamY),
                end = Offset(pivotX + beamLength / 2, beamY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Pans
        val dx = (beamLength / 2) * cos(angleRad).toFloat()
        val dy = (beamLength / 2) * sin(angleRad).toFloat()
        
        val leftX = pivotX - dx
        val leftY = beamY - dy
        val rightX = pivotX + dx
        val rightY = beamY + dy
        
        val panStringLength = 40.dp.toPx()
        val panWidth = 48.dp.toPx()

        // Left Pan
        drawLine(color = leftColor.copy(alpha = 0.5f), start = Offset(leftX, leftY), end = Offset(leftX - panWidth/2, leftY + panStringLength), strokeWidth = 2.dp.toPx())
        drawLine(color = leftColor.copy(alpha = 0.5f), start = Offset(leftX, leftY), end = Offset(leftX + panWidth/2, leftY + panStringLength), strokeWidth = 2.dp.toPx())
        drawArc(
            color = leftColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(leftX - panWidth/2, leftY + panStringLength - panWidth/4),
            size = Size(panWidth, panWidth/2),
            style = Stroke(width = 4.dp.toPx())
        )

        // Right Pan
        drawLine(color = rightColor.copy(alpha = 0.5f), start = Offset(rightX, rightY), end = Offset(rightX - panWidth/2, rightY + panStringLength), strokeWidth = 2.dp.toPx())
        drawLine(color = rightColor.copy(alpha = 0.5f), start = Offset(rightX, rightY), end = Offset(rightX + panWidth/2, rightY + panStringLength), strokeWidth = 2.dp.toPx())
        drawArc(
            color = rightColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(rightX - panWidth/2, rightY + panStringLength - panWidth/4),
            size = Size(panWidth, panWidth/2),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
