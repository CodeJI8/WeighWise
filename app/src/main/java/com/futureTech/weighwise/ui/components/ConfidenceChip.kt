package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme

enum class ConfidenceLevel {
    CLEAR_WIN,
    CLOSE_CALL,
    TOSS_UP
}

@Composable
fun ConfidenceChip(
    level: ConfidenceLevel,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val isDark = isSystemInDarkTheme()
    val (backgroundColor, textColor, defaultText) = when (level) {
        ConfidenceLevel.CLEAR_WIN -> Triple(
            if (isDark) Color(0xFF143834) else Color(0xFFD6F5F2),
            if (isDark) Color(0xFF2DD4A7) else Color(0xFF0F8A80),
            "Clear win"
        )
        ConfidenceLevel.CLOSE_CALL -> Triple(
            if (isDark) Color(0xFF382F18) else Color(0xFFFFF3D6),
            if (isDark) Color(0xFFFFC043) else Color(0xFFB88000),
            "Close call"
        )
        ConfidenceLevel.TOSS_UP -> Triple(
            if (isDark) Color(0xFF262142) else Color(0xFFEBE8FE),
            if (isDark) Color(0xFF9B7BFF) else Color(0xFF5A4AD1),
            "Toss-up"
        )
    }

    val displayText = customText ?: defaultText

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontFamily = SystemSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfidenceChipPreview() {
    WeighWiseTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            ConfidenceChip(level = ConfidenceLevel.CLEAR_WIN, customText = "Clear win: Offer B")
            Spacer(modifier = Modifier.width(8.dp))
            ConfidenceChip(level = ConfidenceLevel.CLOSE_CALL)
            Spacer(modifier = Modifier.width(8.dp))
            ConfidenceChip(level = ConfidenceLevel.TOSS_UP)
        }
    }
}
