package com.futureTech.weighwise.ui.journal

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.theme.DarkSecondaryText
import com.futureTech.weighwise.theme.DarkSurface
import com.futureTech.weighwise.theme.HeroGradientEnd
import com.futureTech.weighwise.theme.HeroGradientStart
import com.futureTech.weighwise.theme.HighlightAmber
import com.futureTech.weighwise.theme.LightSecondaryText
import com.futureTech.weighwise.theme.LightSurface
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.PrimaryTeal
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme
import com.futureTech.weighwise.ui.components.ConfidenceChip
import com.futureTech.weighwise.ui.components.ConfidenceLevel
import com.futureTech.weighwise.ui.components.PrimaryButton
import java.util.Locale

@Composable
fun JournalScreen(
    onBack: () -> Unit,
    onDecisionClick: (Long) -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var reviewItem by remember { mutableStateOf<JournalItemUiState?>(null) }

    JournalScreenContent(
        uiState = uiState,
        onBack = onBack,
        onDecisionClick = onDecisionClick,
        onOpenReview = { reviewItem = it }
    )

    reviewItem?.let { item ->
        ReviewOutcomeDialog(
            item = item,
            onDismiss = { reviewItem = null },
            onSaveReview = { rating, note ->
                viewModel.saveReview(item.decision.id, rating, note)
                reviewItem = null
            }
        )
    }
}

@Composable
fun JournalScreenContent(
    uiState: JournalUiState,
    onBack: () -> Unit,
    onDecisionClick: (Long) -> Unit,
    onOpenReview: (JournalItemUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Back Arrow Row)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Title Block
                item {
                    Column {
                        Text(
                            text = "Decision journal",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Looking back",
                            fontFamily = Sora,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Insights Hero Card
                item {
                    InsightsHeroCard(
                        gutMatchedCount = uiState.gutMatchedCount,
                        totalGutCount = uiState.totalGutCount,
                        averageSatisfaction = uiState.averageSatisfaction
                    )
                }

                // Journal Items List
                if (uiState.journalItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No locked-in decisions yet.",
                                fontFamily = SystemSans,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.journalItems,
                        key = { it.decision.id }
                    ) { itemState ->
                        JournalCard(
                            itemState = itemState,
                            onClick = { onDecisionClick(itemState.decision.id) },
                            onReviewClick = { onOpenReview(itemState) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsHeroCard(
    gutMatchedCount: Int,
    totalGutCount: Int,
    averageSatisfaction: Float?,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(HeroGradientStart, HeroGradientEnd)
    )

    val satText = averageSatisfaction?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush = gradientBrush)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Gut matched the numbers",
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = SystemSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$gutMatchedCount of $totalGutCount",
                color = Color.White,
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 44.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Average satisfaction $satText",
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = SystemSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun JournalCard(
    itemState: JournalItemUiState,
    onClick: () -> Unit,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(24.dp)
    val cardSurface = if (isDark) DarkSurface else LightSurface

    val cardModifier = if (isDark) {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardSurface)
            .border(width = 1.dp, color = Color(0xFF233256), shape = shape)
            .clickable(onClick = onClick)
            .padding(20.dp)
    } else {
        modifier
            .shadow(elevation = 3.dp, shape = shape, spotColor = Color(0x1A000000))
            .clip(shape)
            .background(cardSurface)
            .clickable(onClick = onClick)
            .padding(20.dp)
    }

    Box(modifier = cardModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = itemState.decision.title,
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else LightText
            )

            Spacer(modifier = Modifier.height(4.dp))

            val subtitleText = if (itemState.isReadyToReview) "Ready to review" else itemState.formattedDate
            Text(
                text = subtitleText,
                fontFamily = SystemSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isDark) DarkSecondaryText else LightSecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (itemState.isReadyToReview) {
                Box(modifier = Modifier.clickable(onClick = onReviewClick)) {
                    ConfidenceChip(
                        level = ConfidenceLevel.CLOSE_CALL,
                        customText = "Review outcome"
                    )
                }
            } else if (itemState.satisfactionRating != null) {
                // 5 Star / Segment Rating
                SatisfactionRatingRow(rating = itemState.satisfactionRating)
            }
        }
    }
}

@Composable
fun SatisfactionRatingRow(rating: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isFilled = i <= rating
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) HighlightAmber else Color(0xFFEAE6DF))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewOutcomeDialog(
    item: JournalItemUiState,
    onDismiss: () -> Unit,
    onSaveReview: (rating: Int, note: String?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    var noteText by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) DarkSurface else LightSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Review outcome",
                    fontFamily = Sora,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isDark) Color.White else LightText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.decision.title,
                    fontFamily = SystemSans,
                    fontSize = 14.sp,
                    color = if (isDark) DarkSecondaryText else LightSecondaryText
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "How satisfied are you with this decision?",
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White else LightText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Star Rating Selector (1 to 5)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val isSelected = star <= selectedRating
                        IconButton(
                            onClick = { selectedRating = star },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Star $star",
                                tint = if (isSelected) HighlightAmber else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Reflection / Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Save review",
                    onClick = { onSaveReview(selectedRating, noteText.ifBlank { null }) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontFamily = SystemSans, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun JournalScreenPreview() {
    WeighWiseTheme {
        val mockDec1 = DecisionEntity(id = 1, title = "Apartment in the city", category = "Housing", status = DecisionStatus.DECIDED, satisfaction = 5)
        val mockDec2 = DecisionEntity(id = 2, title = "Freelance client", category = "Work", status = DecisionStatus.DECIDED, satisfaction = 4)
        val mockDec3 = DecisionEntity(id = 3, title = "Course choice", category = "Education", status = DecisionStatus.DECIDED)

        JournalScreenContent(
            uiState = JournalUiState(
                isLoading = false,
                gutMatchedCount = 4,
                totalGutCount = 6,
                averageSatisfaction = 4.2f,
                journalItems = listOf(
                    JournalItemUiState(decision = mockDec1, formattedDate = "Decided 3 months ago", isReadyToReview = false, satisfactionRating = 5, finalOptionName = "Apartment A"),
                    JournalItemUiState(decision = mockDec2, formattedDate = "Decided 1 month ago", isReadyToReview = false, satisfactionRating = 4, finalOptionName = "Client B"),
                    JournalItemUiState(decision = mockDec3, formattedDate = "Decided today", isReadyToReview = true, satisfactionRating = null, finalOptionName = "Course C")
                )
            ),
            onBack = {},
            onDecisionClick = {},
            onOpenReview = {}
        )
    }
}
