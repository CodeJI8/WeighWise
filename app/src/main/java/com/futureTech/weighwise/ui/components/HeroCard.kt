package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureTech.weighwise.theme.HeroGradientEnd
import com.futureTech.weighwise.theme.HeroGradientStart
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme

@Composable
fun HeroCard(
    title: String,
    bigNumber: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    balanceTilt: Float = 0.4f,
    leftPanColor: Color = Color(0xFFFFB020),
    rightPanColor: Color = Color(0xFFFFFFFF),
    showScale: Boolean = true
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(HeroGradientStart, HeroGradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush = gradientBrush)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bigNumber,
                    color = Color.White,
                    fontFamily = Sora,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                )
            }

            if (showScale) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(90.dp)
                ) {
                    BalanceScale(
                        tiltValue = balanceTilt,
                        beamColor = Color.White,
                        standColor = Color.White.copy(alpha = 0.8f),
                        leftPanColor = leftPanColor,
                        rightPanColor = rightPanColor
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HeroCardPreview() {
    WeighWiseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HeroCard(
                title = "Open right now",
                bigNumber = "2",
                subtitle = "1 clear win, 1 close call",
                balanceTilt = 0.4f
            )
        }
    }
}
