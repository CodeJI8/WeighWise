package com.futureTech.weighwise.ui.wizard

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.futureTech.weighwise.theme.DarkSecondaryText
import com.futureTech.weighwise.theme.DarkSurface
import com.futureTech.weighwise.theme.LightSecondaryText
import com.futureTech.weighwise.theme.LightSurface
import com.futureTech.weighwise.theme.LightText
import com.futureTech.weighwise.theme.OptionPalette
import com.futureTech.weighwise.theme.PrimaryTeal
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme
import com.futureTech.weighwise.ui.components.BalanceScale
import com.futureTech.weighwise.ui.components.OptionSlider
import com.futureTech.weighwise.ui.components.PrimaryButton
import kotlin.math.roundToInt

@Composable
fun WizardScreen(
    onFinish: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: WizardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    WizardScreenContent(
        state = state,
        liveSummary = viewModel.getLiveScoringSummary(),
        onTitleChange = { viewModel.updateTitle(it) },
        onOptionChange = { i, name -> viewModel.updateOption(i, name) },
        onAddOption = { viewModel.addOption() },
        onRemoveOption = { viewModel.removeOption(it) },
        onCriterionChange = { i, crit -> viewModel.updateCriterion(i, crit) },
        onAddCriterion = { viewModel.addCriterion() },
        onRemoveCriterion = { viewModel.removeCriterion(it) },
        onScoreChange = { o, c, valScore -> viewModel.updateScore(o, c, valScore) },
        onMustHavePassChange = { o, c, isPass -> viewModel.updateMustHavePass(o, c, isPass) },
        onGutPickSelect = { viewModel.setGutPick(it) },
        onNextScoringCriterion = { viewModel.nextScoringCriterion() },
        onPrevScoringCriterion = { viewModel.prevScoringCriterion() },
        onNextStep = { viewModel.nextStep() },
        onPrevStep = { viewModel.prevStep() },
        onFinish = { viewModel.saveDecision(onFinish) },
        onCancel = onCancel
    )
}

@Composable
fun WizardScreenContent(
    state: WizardState,
    liveSummary: Pair<Float, String>,
    onTitleChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onCriterionChange: (Int, CriterionInput) -> Unit,
    onAddCriterion: () -> Unit,
    onRemoveCriterion: (Int) -> Unit,
    onScoreChange: (Int, Int, Float) -> Unit,
    onMustHavePassChange: (Int, Int, Boolean) -> Unit,
    onGutPickSelect: (Int) -> Unit,
    onNextScoringCriterion: () -> Unit,
    onPrevScoringCriterion: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = 6
    // Mapping wizard internal steps (0 to 4) to 6 display steps
    val displayStepNumber = when (state.currentStep) {
        0 -> 1 // Setup
        1 -> 2 // Options
        2 -> 3 // Criteria & Weights
        3 -> 4 // Scoring
        4 -> 5 // Gut check
        else -> 6
    }

    val stepTitles = listOf(
        "What are you deciding?",
        "Name your options",
        "Set criteria & weights",
        "Score your options",
        "Gut check",
        "Review decision"
    )
    val currentStepTitle = stepTitles.getOrElse(displayStepNumber - 1) { "Wizard" }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                val isScoringStep = state.currentStep == 3
                val isLastCriterion = state.currentScoringCriterionIndex >= state.criteria.size - 1
                val isFinalStep = state.currentStep == 4

                val buttonText = when {
                    isScoringStep && !isLastCriterion -> "Next criterion"
                    isFinalStep -> "See Result"
                    else -> "Next step"
                }

                PrimaryButton(
                    text = buttonText,
                    onClick = {
                        when {
                            isScoringStep && !isLastCriterion -> onNextScoringCriterion()
                            isFinalStep -> onFinish()
                            else -> onNextStep()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Step Header & Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (state.currentStep == 3 && state.currentScoringCriterionIndex > 0) {
                            onPrevScoringCriterion()
                        } else if (state.currentStep > 0) {
                            onPrevStep()
                        } else {
                            onCancel()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Step $displayStepNumber of $totalSteps",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentStepTitle,
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Segmented Bar (6 segments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (s in 1..totalSteps) {
                    val isCompleted = s <= displayStepNumber
                    val segmentColor = if (isCompleted) PrimaryTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content Animated Transition
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "wizardStepTransition"
                ) { step ->
                    when (step) {
                        0 -> SetupStepView(state, onTitleChange)
                        1 -> OptionsStepView(state, onOptionChange, onAddOption, onRemoveOption)
                        2 -> CriteriaStepView(state, onCriterionChange, onAddCriterion, onRemoveCriterion)
                        3 -> ScoringStepView(
                            state = state,
                            liveSummary = liveSummary,
                            onScoreChange = onScoreChange,
                            onMustHavePassChange = onMustHavePassChange
                        )
                        4 -> GutCheckStepView(state, onGutPickSelect)
                    }
                }
            }
        }
    }
}

@Composable
fun SetupStepView(state: WizardState, onTitleChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Decision title",
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            placeholder = { Text("e.g. Job offer, New laptop") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun OptionsStepView(
    state: WizardState,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(state.options.size) { i ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val color = OptionPalette[i % OptionPalette.size]
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.options[i],
                    onValueChange = { onOptionChange(i, it) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                if (state.options.size > 2) {
                    IconButton(onClick = { onRemoveOption(i) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove option")
                    }
                }
            }
        }
        item {
            TextButton(onClick = onAddOption) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add another option", fontFamily = Sora, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CriteriaStepView(
    state: WizardState,
    onCriterionChange: (Int, CriterionInput) -> Unit,
    onAddCriterion: () -> Unit,
    onRemoveCriterion: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(state.criteria.size) { i ->
            val crit = state.criteria[i]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = crit.name,
                            onValueChange = { onCriterionChange(i, crit.copy(name = it)) },
                            label = { Text("Criterion name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (state.criteria.size > 1) {
                            IconButton(onClick = { onRemoveCriterion(i) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove criterion")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Weight: ${crit.weight.roundToInt()}", fontFamily = SystemSans, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Must-have?", fontFamily = SystemSans, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = crit.isMustHave,
                                onCheckedChange = { onCriterionChange(i, crit.copy(isMustHave = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryTeal)
                            )
                        }
                    }
                }
            }
        }
        item {
            TextButton(onClick = onAddCriterion) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add criterion", fontFamily = Sora, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScoringStepView(
    state: WizardState,
    liveSummary: Pair<Float, String>,
    onScoreChange: (Int, Int, Float) -> Unit,
    onMustHavePassChange: (Int, Int, Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val (liveTilt, leadingText) = liveSummary

    val currentCriterionIndex = state.currentScoringCriterionIndex.coerceIn(0, (state.criteria.size - 1).coerceAtLeast(0))
    val currentCriterion = state.criteria.getOrNull(currentCriterionIndex) ?: CriterionInput("Criterion", 5f, false)

    val totalWeight = state.criteria.sumOf { it.weight.toDouble() }.toFloat()
    val weightPercent = if (totalWeight > 0f) ((currentCriterion.weight / totalWeight) * 100).roundToInt() else 0

    val cardShape = RoundedCornerShape(24.dp)
    val cardSurface = if (isDark) DarkSurface else LightSurface

    val cardModifier = if (isDark) {
        Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardSurface)
            .border(width = 1.dp, color = Color(0xFF233256), shape = cardShape)
            .padding(20.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = cardShape, spotColor = Color(0x1A000000))
            .clip(cardShape)
            .background(cardSurface)
            .padding(20.dp)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dark Live Balance Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0xFF131D38) else Color(0xFF0B1B3A))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BalanceScale(
                        tiltValue = liveTilt,
                        beamColor = Color.White,
                        standColor = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = leadingText,
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Current Criterion Card
        item {
            Box(modifier = cardModifier) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currentCriterion.name,
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDark) Color.White else LightText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Weight $weightPercent%",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isDark) DarkSecondaryText else LightSecondaryText
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option sliders or Must-have switches
                    state.options.forEachIndexed { oIdx, optionName ->
                        val optionColor = OptionPalette[oIdx % OptionPalette.size]

                        if (currentCriterion.isMustHave) {
                            val isPass = state.mustHavePasses[oIdx to currentCriterionIndex] ?: true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(optionColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = optionName,
                                        fontFamily = Sora,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else LightText
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPass) "Pass" else "Fail",
                                        fontFamily = SystemSans,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isPass) PrimaryTeal else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = isPass,
                                        onCheckedChange = { onMustHavePassChange(oIdx, currentCriterionIndex, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryTeal
                                        )
                                    )
                                }
                            }
                        } else {
                            val scoreVal = state.scores[oIdx to currentCriterionIndex] ?: 5f
                            OptionSlider(
                                optionName = optionName,
                                value = scoreVal,
                                onValueChange = { onScoreChange(oIdx, currentCriterionIndex, it) },
                                optionColor = optionColor,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GutCheckStepView(state: WizardState, onGutPickSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Before we crunch the numbers...",
            fontFamily = SystemSans,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "What does your gut say?",
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        state.options.forEachIndexed { i, opt ->
            val isSelected = state.gutPickIndex == i
            val color = OptionPalette[i % OptionPalette.size]

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimaryTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onGutPickSelect(i) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = opt,
                        fontFamily = Sora,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScoringStepPreview() {
    WeighWiseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScoringStepView(
                state = WizardState(currentStep = 3),
                liveSummary = 0.35f to "Offer B is leading",
                onScoreChange = { _, _, _ -> },
                onMustHavePassChange = { _, _, _ -> }
            )
        }
    }
}
