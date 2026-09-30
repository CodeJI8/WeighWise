package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureTech.weighwise.theme.DarkText
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.OptionPalette
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.WeighWiseTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun OptionSlider(
    optionName: String,
    value: Float, // e.g. 0f to 10f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    optionColor: Color = OptionPalette[0],
    valueRange: ClosedFloatingPointRange<Float> = 0f..10f,
    displayDecimal: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val labelColor = if (isDark) DarkText else LightText
    val inactiveTrackColor = if (isDark) Color(0xFF1B2748) else Color(0xFFEAE6DF)

    val currentFraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    val valueText = if (displayDecimal) {
        String.format(Locale.US, "%.1f", value)
    } else {
        value.roundToInt().toString()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Option Name and Value Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionName,
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = labelColor
            )
            Text(
                text = valueText,
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = labelColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Slider Canvas with Thick Track & White Thumb with Colored Border
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(valueRange) {
                    fun updateValue(x: Float) {
                        val fraction = (x / size.width).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }

                    detectTapGestures { offset ->
                        updateValue(offset.x)
                    }
                }
                .pointerInput(valueRange) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    }
                }
        ) {
            val trackHeight = 10.dp.toPx()
            val thumbRadius = 11.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            val width = size.width
            val height = size.height
            val centerY = height / 2f

            val thumbX = (currentFraction * width).coerceIn(thumbRadius, width - thumbRadius)

            // Inactive Track
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Active Track
            if (thumbX > 0f) {
                drawRoundRect(
                    color = optionColor,
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(thumbX, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                )
            }

            // White Thumb Interior
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, centerY)
            )

            // Thumb Colored Border
            drawCircle(
                color = optionColor,
                radius = thumbRadius,
                center = Offset(thumbX, centerY),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OptionSliderPreview() {
    WeighWiseTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionSlider(
                optionName = "Offer A",
                value = 6f,
                onValueChange = {},
                optionColor = OptionPalette[0]
            )
            OptionSlider(
                optionName = "Offer B",
                value = 9f,
                onValueChange = {},
                optionColor = OptionPalette[1]
            )
            OptionSlider(
                optionName = "Offer C",
                value = 4f,
                onValueChange = {},
                optionColor = OptionPalette[2]
            )
        }
    }
}
