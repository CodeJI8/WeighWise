package com.futureTech.weighwise.ui.wizard

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun WizardScreen(
    onFinish: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: WizardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            LinearProgressIndicator(
                progress = { (state.currentStep + 1f) / 6f },
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            BottomAppBar {
                if (state.currentStep > 0) {
                    TextButton(onClick = { viewModel.prevStep() }) { Text("Back") }
                } else {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                }
                Spacer(Modifier.weight(1f))
                if (state.currentStep < 5) {
                    Button(onClick = { viewModel.nextStep() }) { Text("Next") }
                } else {
                    Button(onClick = { viewModel.saveDecision(onFinish) }) { Text("See Result") }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                }, label = "wizardStep"
            ) { step ->
                when (step) {
                    0 -> SetupStep(state, viewModel)
                    1 -> OptionsStep(state, viewModel)
                    2 -> CriteriaStep(state, viewModel)
                    3 -> WeightsStep(state, viewModel)
                    4 -> ScoringStep(state, viewModel)
                    5 -> GutCheckStep(state, viewModel)
                }
            }
        }
    }
}

@Composable
fun SetupStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("What are you deciding?", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Decision Title") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OptionsStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Options", style = MaterialTheme.typography.headlineMedium)
        state.options.forEachIndexed { i, opt ->
            OutlinedTextField(
                value = opt,
                onValueChange = { viewModel.updateOption(i, it) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }
        Button(onClick = { viewModel.addOption() }) { Text("Add Option") }
    }
}

@Composable
fun CriteriaStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Criteria", style = MaterialTheme.typography.headlineMedium)
        state.criteria.forEachIndexed { i, crit ->
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = crit.name,
                    onValueChange = { viewModel.updateCriterion(i, crit.copy(name = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Button(onClick = { viewModel.addCriterion() }) { Text("Add Criterion") }
    }
}

@Composable
fun WeightsStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Weights", style = MaterialTheme.typography.headlineMedium)
        state.criteria.forEachIndexed { i, crit ->
            Text(crit.name)
            Slider(
                value = crit.weight,
                onValueChange = { viewModel.updateCriterion(i, crit.copy(weight = it)) },
                valueRange = 0f..10f
            )
        }
    }
}

@Composable
fun ScoringStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Scoring", style = MaterialTheme.typography.headlineMedium)
        state.criteria.forEachIndexed { cIdx, crit ->
            Text(crit.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            state.options.forEachIndexed { oIdx, opt ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(opt, modifier = Modifier.weight(1f))
                    val score = state.scores[oIdx to cIdx] ?: 5f
                    Slider(
                        value = score,
                        onValueChange = { viewModel.updateScore(oIdx, cIdx, it) },
                        valueRange = 0f..10f,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }
}

@Composable
fun GutCheckStep(state: WizardState, viewModel: WizardViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Gut Check", style = MaterialTheme.typography.headlineMedium)
        Text("Before we crunch the numbers, what feels right?")
        state.options.forEachIndexed { i, opt ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = state.gutPickIndex == i,
                    onClick = { viewModel.setGutPick(i) }
                )
                Text(opt)
            }
        }
    }
}
