package com.futureTech.weighwise.ui.result

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.domain.OptionResult
import com.futureTech.weighwise.domain.SensitivityAnalyzer
import com.futureTech.weighwise.theme.DarkSecondaryText
import com.futureTech.weighwise.theme.DarkSurface
import com.futureTech.weighwise.theme.HighlightAmber
import com.futureTech.weighwise.theme.LightSecondaryText
import com.futureTech.weighwise.theme.LightSurface
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.OptionPalette
import com.futureTech.weighwise.theme.PrimaryTeal
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme
import com.futureTech.weighwise.ui.components.ConfidenceChip
import com.futureTech.weighwise.ui.components.ConfidenceLevel
import com.futureTech.weighwise.ui.components.PrimaryButton
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ResultScreen(
    decisionId: Long,
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    ResultScreenContent(
        state = state,
        onBack = onBack,
        onShare = {
            state.decision?.let { dec ->
                ShareUtils.shareResult(context, dec, state.results)
            }
        },
        onLockDecision = { optionId ->
            viewModel.lockDecision(optionId)
        }
    )
}

@Composable
fun ResultScreenContent(
    state: ResultState,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onLockDecision: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val winner = state.results.firstOrNull { !it.isEliminated }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (winner != null) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    PrimaryButton(
                        text = if (state.isDecided) "Decision locked!" else "Lock in decision",
                        enabled = !state.isDecided,
                        onClick = { onLockDecision(winner.option.id) }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
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
                // Header (Back + Share row)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Title Block
                item {
                    Column {
                        Text(
                            text = "Result",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = winner?.let { "${it.option.name} wins" } ?: "No winner",
                            fontFamily = Sora,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Winner Card with Amber Glow
                if (winner != null) {
                    item {
                        WinnerCard(
                            winner = winner,
                            confidenceLevel = state.confidenceLevel,
                            winningReason = state.winningReason
                        )
                    }
                }

                // Win Probability Section
                if (state.results.isNotEmpty()) {
                    item {
                        WinProbabilityCard(
                            results = state.results,
                            probabilities = state.winProbabilities
                        )
                    }
                }

                // "What would flip it?" Sensitivity Card
                if (state.sensitivity.isNotEmpty()) {
                    item {
                        val topSensitivity = state.sensitivity.first()
                        WhatWouldFlipItCard(sensitivity = topSensitivity)
                    }
                }
            }
        }
    }
}

@Composable
fun WinnerCard(
    winner: OptionResult,
    confidenceLevel: ConfidenceLevel,
    winningReason: String,
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
            .border(width = 2.dp, color = HighlightAmber, shape = shape)
            .padding(20.dp)
    } else {
        modifier
            .shadow(elevation = 6.dp, shape = shape, spotColor = HighlightAmber)
            .clip(shape)
            .background(cardSurface)
            .border(width = 2.dp, color = HighlightAmber, shape = shape)
            .padding(20.dp)
    }

    Box(modifier = cardModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ConfidenceChip(level = confidenceLevel)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${winner.score.roundToInt()}",
                    fontFamily = Sora,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    color = if (isDark) Color.White else LightText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "out of 100",
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isDark) DarkSecondaryText else LightSecondaryText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            if (winningReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = winningReason,
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.9f) else LightText
                )
            }
        }
    }
}

@Composable
fun WinProbabilityCard(
    results: List<OptionResult>,
    probabilities: Map<Long, Float>,
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
            .padding(20.dp)
    } else {
        modifier
            .shadow(elevation = 3.dp, shape = shape, spotColor = Color(0x1A000000))
            .clip(shape)
            .background(cardSurface)
            .padding(20.dp)
    }

    Box(modifier = cardModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Win probability",
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else LightText
            )

            Spacer(modifier = Modifier.height(16.dp))

            results.forEach { result ->
                val color = OptionPalette[result.option.colorIndex % OptionPalette.size]
                val prob = probabilities[result.option.id] ?: 0f
                val percentText = "${(prob * 100).roundToInt()}%"

                ProbabilityRow(
                    optionName = result.option.name,
                    probability = prob,
                    color = color,
                    percentText = percentText
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ProbabilityRow(
    optionName: String,
    probability: Float,
    color: Color,
    percentText: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (started) probability.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "probProgress"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = optionName,
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isDark) Color.White else LightText,
            modifier = Modifier.width(72.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Progress Bar Track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF1B2748) else Color(0xFFEAE6DF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = percentText,
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isDark) Color.White else LightText,
            modifier = Modifier.width(44.dp)
        )
    }
}

@Composable
fun WhatWouldFlipItCard(
    sensitivity: SensitivityAnalyzer.SensitivityResult,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(20.dp)
    val cardBackground = if (isDark) Color(0xFF143834) else Color(0xFFD6F5F2)
    val textColor = if (isDark) Color.White else LightText

    val changePercent = abs(sensitivity.requiredWeightChangePercent).roundToInt()
    val direction = if (sensitivity.requiredWeightChangePercent > 0) "more" else "less"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "What would flip it?",
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) PrimaryTeal else Color(0xFF0F8A80)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${sensitivity.criterion.name} would need to matter $changePercent% $direction for ${sensitivity.flippingOption.name} to win.",
                fontFamily = SystemSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = textColor
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ResultScreenPreview() {
    WeighWiseTheme {
        val mockOptionB = OptionEntity(id = 2, decisionId = 1, name = "Offer B", colorIndex = 1)
        val mockOptionA = OptionEntity(id = 1, decisionId = 1, name = "Offer A", colorIndex = 0)
        val mockOptionC = OptionEntity(id = 3, decisionId = 1, name = "Offer C", colorIndex = 2)

        ResultScreenContent(
            state = ResultState(
                isLoading = false,
                decision = DecisionEntity(id = 1, title = "Job offer", category = "Career", status = DecisionStatus.DECIDING),
                results = listOf(
                    OptionResult(option = mockOptionB, score = 84f, isEliminated = false, criterionScores = emptyMap()),
                    OptionResult(option = mockOptionA, score = 62f, isEliminated = false, criterionScores = emptyMap()),
                    OptionResult(option = mockOptionC, score = 45f, isEliminated = false, criterionScores = emptyMap())
                ),
                winProbabilities = mapOf(2L to 0.78f, 1L to 0.17f, 3L to 0.05f),
                winningReason = "Leads on growth and salary, despite lower commute.",
                confidenceLevel = ConfidenceLevel.CLEAR_WIN,
                isDecided = false
            ),
            onBack = {},
            onShare = {},
            onLockDecision = {}
        )
    }
}
