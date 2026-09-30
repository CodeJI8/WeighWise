package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futureTech.weighwise.theme.WeighWiseTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BalanceScale(
    tiltValue: Float, // -1f (left side down) to 1f (right side down)
    modifier: Modifier = Modifier,
    beamColor: Color? = null,
    standColor: Color? = null,
    leftPanColor: Color = Color(0xFFFFB020), // Amber
    rightPanColor: Color = Color(0xFFFFFFFF)  // White
) {
    val animatedTilt by animateFloatAsState(
        targetValue = tiltValue.coerceIn(-1f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleTilt"
    )

    val isDark = isSystemInDarkTheme()
    val defaultBeamColor = beamColor ?: if (isDark) Color(0xFFE2E8F0) else Color(0xFFFFFFFF)
    val defaultStandColor = standColor ?: if (isDark) Color(0xFFA0AEC0) else Color(0xCCFFFFFF)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val pivotX = size.width / 2f
        val pivotY = size.height - 16.dp.toPx()
        val beamLength = size.width * 0.58f
        val beamCenterY = pivotY - 60.dp.toPx()

        // Fulcrum Stand (Triangle + Base)
        val standWidth = 24.dp.toPx()
        val standPath = Path().apply {
            moveTo(pivotX - standWidth / 2f, pivotY)
            lineTo(pivotX + standWidth / 2f, pivotY)
            lineTo(pivotX, beamCenterY)
            close()
        }
        drawPath(
            path = standPath,
            color = defaultStandColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pivot Circle
        drawCircle(
            color = defaultBeamColor,
            radius = 5.dp.toPx(),
            center = Offset(pivotX, beamCenterY)
        )

        val maxRotationDegrees = 20f
        // Positive tilt means right side goes DOWN (clockwise rotation in screen coords)
        val angleDegrees = animatedTilt * maxRotationDegrees
        val angleRad = angleDegrees * (PI / 180f)

        withTransform({
            rotate(degrees = angleDegrees, pivot = Offset(pivotX, beamCenterY))
        }) {
            // Beam Bar
            drawLine(
                color = defaultBeamColor,
                start = Offset(pivotX - beamLength / 2f, beamCenterY),
                end = Offset(pivotX + beamLength / 2f, beamCenterY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Beam end coordinates
        val dx = (beamLength / 2f) * cos(angleRad).toFloat()
        val dy = (beamLength / 2f) * sin(angleRad).toFloat()

        val leftX = pivotX - dx
        val leftY = beamCenterY - dy

        val rightX = pivotX + dx
        val rightY = beamCenterY + dy

        val stringLength = 32.dp.toPx()
        val panWidth = 40.dp.toPx()

        // Left Pan Strings & Pan
        drawLine(
            color = leftPanColor.copy(alpha = 0.7f),
            start = Offset(leftX, leftY),
            end = Offset(leftX - panWidth / 2f, leftY + stringLength),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = leftPanColor.copy(alpha = 0.7f),
            start = Offset(leftX, leftY),
            end = Offset(leftX + panWidth / 2f, leftY + stringLength),
            strokeWidth = 2.dp.toPx()
        )
        drawArc(
            color = leftPanColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(leftX - panWidth / 2f, leftY + stringLength - panWidth / 4f),
            size = Size(panWidth, panWidth / 2f)
        )

        // Right Pan Strings & Pan
        drawLine(
            color = rightPanColor.copy(alpha = 0.7f),
            start = Offset(rightX, rightY),
            end = Offset(rightX - panWidth / 2f, rightY + stringLength),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = rightPanColor.copy(alpha = 0.7f),
            start = Offset(rightX, rightY),
            end = Offset(rightX + panWidth / 2f, rightY + stringLength),
            strokeWidth = 2.dp.toPx()
        )
        drawArc(
            color = rightPanColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(rightX - panWidth / 2f, rightY + stringLength - panWidth / 4f),
            size = Size(panWidth, panWidth / 2f)
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BalanceScalePreview() {
    WeighWiseTheme {
        Box(modifier = Modifier.padding(16.dp).width(240.dp)) {
            BalanceScale(tiltValue = 0.5f)
        }
    }
}
