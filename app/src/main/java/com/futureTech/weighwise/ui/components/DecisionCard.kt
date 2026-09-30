package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureTech.weighwise.theme.DarkSecondaryText
import com.futureTech.weighwise.theme.DarkSurface
import com.futureTech.weighwise.theme.LightSecondaryText
import com.futureTech.weighwise.theme.LightSurface
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.OptionPalette
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme

@Composable
fun DecisionCard(
    title: String,
    category: String,
    criteriaCount: Int,
    optionColors: List<Color>,
    confidenceLevel: ConfidenceLevel,
    modifier: Modifier = Modifier,
    confidenceLabel: String? = null,
    onClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(24.dp)

    val surfaceColor = if (isDark) DarkSurface else LightSurface
    val titleColor = if (isDark) Color.White else LightText
    val subtitleColor = if (isDark) DarkSecondaryText else LightSecondaryText

    val cardModifier = if (isDark) {
        modifier
            .clip(shape)
            .background(surfaceColor)
            .border(width = 1.dp, color = Color(0xFF233256), shape = shape)
            .clickable(onClick = onClick)
            .padding(20.dp)
    } else {
        modifier
            .shadow(elevation = 3.dp, shape = shape, spotColor = Color(0x1A000000))
            .clip(shape)
            .background(surfaceColor)
            .clickable(onClick = onClick)
            .padding(20.dp)
    }

    Column(modifier = cardModifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$category, $criteriaCount criteria",
            fontFamily = SystemSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = subtitleColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option Color Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            optionColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Confidence Chip
        ConfidenceChip(
            level = confidenceLevel,
            customText = confidenceLabel
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DecisionCardPreview() {
    WeighWiseTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DecisionCard(
                title = "Job offer",
                category = "Career",
                criteriaCount = 6,
                optionColors = listOf(OptionPalette[0], OptionPalette[1], OptionPalette[2]),
                confidenceLevel = ConfidenceLevel.CLEAR_WIN,
                confidenceLabel = "Clear win: Offer B",
                onClick = {}
            )

            DecisionCard(
                title = "New laptop",
                category = "Buying",
                criteriaCount = 5,
                optionColors = listOf(OptionPalette[0], OptionPalette[3]),
                confidenceLevel = ConfidenceLevel.CLOSE_CALL,
                onClick = {}
            )
        }
    }
}
