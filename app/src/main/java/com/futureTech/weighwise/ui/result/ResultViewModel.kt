package com.futureTech.weighwise.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.domain.OptionResult
import com.futureTech.weighwise.domain.ScoreCalculator
import com.futureTech.weighwise.domain.SensitivityAnalyzer
import com.futureTech.weighwise.domain.Simulator
import com.futureTech.weighwise.domain.WeighWiseRepository
import com.futureTech.weighwise.ui.components.ConfidenceLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultState(
    val isLoading: Boolean = true,
    val decision: DecisionEntity? = null,
    val results: List<OptionResult> = emptyList(),
    val criteria: List<CriterionEntity> = emptyList(),
    val winProbabilities: Map<Long, Float> = emptyMap(),
    val sensitivity: List<SensitivityAnalyzer.SensitivityResult> = emptyList(),
    val winningReason: String = "",
    val confidenceLevel: ConfidenceLevel = ConfidenceLevel.CLEAR_WIN,
    val isDecided: Boolean = false
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: WeighWiseRepository,
    private val calculator: ScoreCalculator,
    private val simulator: Simulator,
    private val analyzer: SensitivityAnalyzer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val decisionId: Long = savedStateHandle.get<String>("decisionId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(ResultState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val decision = repository.getDecision(decisionId) ?: return@launch
            val options = repository.getOptions(decisionId)
            val criteria = repository.getCriteria(decisionId)
            val scores = repository.getScores(decisionId)

            val results = calculator.calculate(options, criteria, scores)
            val winner = results.firstOrNull { !it.isEliminated }
            val runnerUp = results.drop(1).firstOrNull { !it.isEliminated }

            val winningReason = if (winner != null) {
                calculator.generateWinningReason(winner, runnerUp, criteria)
            } else ""

            val diff = if (winner != null && runnerUp != null) winner.score - runnerUp.score else 100f
            val confidenceLevel = when {
                diff >= 15f -> ConfidenceLevel.CLEAR_WIN
                diff >= 5f -> ConfidenceLevel.CLOSE_CALL
                else -> ConfidenceLevel.TOSS_UP
            }

            // Run 2,000 simulation runs off the main thread (Simulator uses Dispatchers.Default)
            val probs = simulator.runSimulations(options, criteria, scores, iterations = 2000)

            // Run sensitivity analysis off the main thread (SensitivityAnalyzer uses Dispatchers.Default)
            val sensitivity = if (winner != null) {
                analyzer.analyze(options, criteria, scores, winner.option)
            } else emptyList()

            _state.update {
                it.copy(
                    isLoading = false,
                    decision = decision,
                    results = results,
                    criteria = criteria,
                    winProbabilities = probs,
                    sensitivity = sensitivity,
                    winningReason = winningReason,
                    confidenceLevel = confidenceLevel,
                    isDecided = decision.status == DecisionStatus.DECIDED
                )
            }
        }
    }

    fun lockDecision(optionId: Long) {
        viewModelScope.launch {
            repository.markDecided(decisionId, optionId)
            _state.update { it.copy(isDecided = true) }
        }
    }
}
