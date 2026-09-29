package com.noboj.weighwise.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noboj.weighwise.data.DecisionEntity
import com.noboj.weighwise.domain.OptionResult
import com.noboj.weighwise.domain.ScoreCalculator
import com.noboj.weighwise.domain.SensitivityAnalyzer
import com.noboj.weighwise.domain.Simulator
import com.noboj.weighwise.domain.WeighWiseRepository
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
    val winProbabilities: Map<Long, Float> = emptyMap(),
    val sensitivity: List<SensitivityAnalyzer.SensitivityResult> = emptyList(),
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
            
            // Run simulations off thread (Simulator does this internally)
            val probs = simulator.runSimulations(options, criteria, scores)
            
            val validWinner = results.firstOrNull { !it.isEliminated }
            val sensitivity = if (validWinner != null) {
                analyzer.analyze(options, criteria, scores, validWinner.option)
            } else {
                emptyList()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    decision = decision,
                    results = results,
                    winProbabilities = probs,
                    sensitivity = sensitivity,
                    isDecided = decision.status == com.noboj.weighwise.data.DecisionStatus.DECIDED
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
