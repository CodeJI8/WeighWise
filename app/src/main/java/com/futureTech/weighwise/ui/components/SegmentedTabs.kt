package com.futureTech.weighwise.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureTech.weighwise.theme.DarkSecondaryText
import com.futureTech.weighwise.theme.DarkText
import com.futureTech.weighwise.theme.LightSecondaryText
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.WeighWiseTheme

@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color(0xFF1B2748) else Color(0xFFEFECE6)
    val indicatorColor = if (isDark) Color(0xFF2B3A63) else Color(0xFFFFFFFF)

    val innerPadding = 4.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .padding(innerPadding)
    ) {
        val totalWidth = this.maxWidth
        val tabCount = tabs.size.coerceAtLeast(1)
        val tabWidth = totalWidth / tabCount

        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = 250),
            label = "tabIndicatorOffset"
        )

        // Sliding Indicator Pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(indicatorColor)
        )

        // Tab Items Row
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val textColor = if (isSelected) {
                    if (isDark) DarkText else LightText
                } else {
                    if (isDark) DarkSecondaryText else LightSecondaryText
                }

                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = Sora,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SegmentedTabsPreview() {
    WeighWiseTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            SegmentedTabs(
                tabs = listOf("Deciding", "Decided"),
                selectedIndex = 0,
                onTabSelected = {}
            )
        }
    }
}
