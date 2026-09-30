package com.futureTech.weighwise.ui.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.data.ScoreEntity
import com.futureTech.weighwise.domain.AHPWeighter
import com.futureTech.weighwise.domain.ScoreCalculator
import com.futureTech.weighwise.domain.WeighWiseRepository
import com.futureTech.weighwise.theme.OptionPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CriterionInput(val name: String, val weight: Float, val isMustHave: Boolean)

data class WizardState(
    val title: String = "",
    val category: String = "Career",
    val options: List<String> = listOf("Offer A", "Offer B", "Offer C"),
    val criteria: List<CriterionInput> = listOf(
        CriterionInput("Salary", 8f, false),
        CriterionInput("Growth potential", 9f, false),
        CriterionInput("Commute", 4f, false)
    ),
    val scores: Map<Pair<Int, Int>, Float> = emptyMap(), // (optionIndex, criterionIndex) -> score
    val mustHavePasses: Map<Pair<Int, Int>, Boolean> = emptyMap(), // (optionIndex, criterionIndex) -> isPass
    val gutPickIndex: Int? = null,
    val currentStep: Int = 3, // 0 = Title, 1 = Options, 2 = Criteria & Weights, 3 = Scoring, 4 = Gut Check
    val currentScoringCriterionIndex: Int = 0
)

@HiltViewModel
class WizardViewModel @Inject constructor(
    private val repository: WeighWiseRepository,
    private val ahpWeighter: AHPWeighter,
    private val scoreCalculator: ScoreCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(WizardState())
    val state = _state.asStateFlow()

    fun updateTitle(title: String) { _state.update { it.copy(title = title) } }

    fun updateCategory(category: String) { _state.update { it.copy(category = category) } }

    fun updateOption(index: Int, name: String) {
        _state.update {
            val newOptions = it.options.toMutableList()
            if (index in newOptions.indices) {
                newOptions[index] = name
            }
            it.copy(options = newOptions)
        }
    }

    fun addOption() {
        _state.update {
            val count = it.options.size + 1
            it.copy(options = it.options + "Offer ${('A' + count - 1)}")
        }
    }

    fun removeOption(index: Int) {
        _state.update {
            if (it.options.size > 2 && index in it.options.indices) {
                val newOptions = it.options.toMutableList()
                newOptions.removeAt(index)
                it.copy(options = newOptions)
            } else it
        }
    }

    fun updateCriterion(index: Int, input: CriterionInput) {
        _state.update {
            val newC = it.criteria.toMutableList()
            if (index in newC.indices) {
                newC[index] = input
            }
            it.copy(criteria = newC)
        }
    }

    fun addCriterion() {
        _state.update { it.copy(criteria = it.criteria + CriterionInput("New criterion", 5f, false)) }
    }

    fun removeCriterion(index: Int) {
        _state.update {
            if (it.criteria.size > 1 && index in it.criteria.indices) {
                val newC = it.criteria.toMutableList()
                newC.removeAt(index)
                it.copy(criteria = newC)
            } else it
        }
    }

    fun updateScore(optionIndex: Int, criterionIndex: Int, score: Float) {
        _state.update {
            val newScores = it.scores.toMutableMap()
            newScores[optionIndex to criterionIndex] = score
            it.copy(scores = newScores)
        }
    }

    fun updateMustHavePass(optionIndex: Int, criterionIndex: Int, isPass: Boolean) {
        _state.update {
            val newPasses = it.mustHavePasses.toMutableMap()
            newPasses[optionIndex to criterionIndex] = isPass
            val newScores = it.scores.toMutableMap()
            newScores[optionIndex to criterionIndex] = if (isPass) 10f else 0f
            it.copy(mustHavePasses = newPasses, scores = newScores)
        }
    }

    fun setGutPick(index: Int) {
        _state.update { it.copy(gutPickIndex = index) }
    }

    fun nextScoringCriterion() {
        _state.update {
            if (it.currentScoringCriterionIndex < it.criteria.size - 1) {
                it.copy(currentScoringCriterionIndex = it.currentScoringCriterionIndex + 1)
            } else {
                it.copy(currentStep = it.currentStep + 1)
            }
        }
    }

    fun prevScoringCriterion() {
        _state.update {
            if (it.currentScoringCriterionIndex > 0) {
                it.copy(currentScoringCriterionIndex = it.currentScoringCriterionIndex - 1)
            } else {
                it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0))
            }
        }
    }

    fun nextStep() {
        _state.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(4)) }
    }

    fun prevStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun getLiveScoringSummary(): Pair<Float, String> {
        val s = _state.value
        if (s.options.isEmpty() || s.criteria.isEmpty()) return 0f to "No options"

        val tempOptions = s.options.mapIndexed { i, name ->
            OptionEntity(id = (i + 1).toLong(), decisionId = 0, name = name, colorIndex = i % OptionPalette.size)
        }
        val tempCriteria = s.criteria.mapIndexed { i, c ->
            CriterionEntity(id = (i + 1).toLong(), decisionId = 0, name = c.name, weight = c.weight, isMustHave = c.isMustHave)
        }
        val tempScores = mutableListOf<ScoreEntity>()
        s.options.forEachIndexed { oIdx, _ ->
            s.criteria.forEachIndexed { cIdx, crit ->
                val valScore = s.scores[oIdx to cIdx] ?: 5f
                val isPass = if (crit.isMustHave) (s.mustHavePasses[oIdx to cIdx] ?: (valScore >= 5f)) else (valScore > 0f)
                tempScores.add(
                    ScoreEntity(
                        optionId = (oIdx + 1).toLong(),
                        criterionId = (cIdx + 1).toLong(),
                        scoreValue = valScore,
                        isPass = isPass
                    )
                )
            }
        }

        val results = scoreCalculator.calculate(tempOptions, tempCriteria, tempScores)
        val validResults = results.filter { !it.isEliminated }

        if (validResults.isEmpty()) {
            return 0f to "All options eliminated"
        }

        val winner = validResults.first()
        val second = validResults.drop(1).firstOrNull()

        if (second == null) {
            val tilt = if (winner.option.id == 2L) 0.6f else -0.6f
            return tilt to "${winner.option.name} is leading"
        }

        val score0 = results.find { it.option.id == 1L }?.score ?: 0f
        val score1 = results.find { it.option.id == 2L }?.score ?: 0f
        val maxScore = maxOf(score0, score1, 1f)
        val rawTilt = ((score1 - score0) / maxScore).coerceIn(-1f, 1f)

        return rawTilt to "${winner.option.name} is leading"
    }

    fun saveDecision(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            val decision = DecisionEntity(
                title = s.title.ifEmpty { "Untitled decision" },
                category = s.category,
                status = DecisionStatus.DECIDING
            )

            val options = s.options.mapIndexed { i, name ->
                OptionEntity(decisionId = 0, name = name, colorIndex = i % OptionPalette.size)
            }

            val criteria = s.criteria.map {
                CriterionEntity(decisionId = 0, name = it.name, weight = it.weight, isMustHave = it.isMustHave)
            }

            val scoreEntities = mutableListOf<ScoreEntity>()
            s.options.forEachIndexed { optIdx, _ ->
                s.criteria.forEachIndexed { critIdx, crit ->
                    val valScore = s.scores[optIdx to critIdx] ?: 5f
                    val isPass = if (crit.isMustHave) {
                        s.mustHavePasses[optIdx to critIdx] ?: (valScore >= 5f)
                    } else {
                        valScore > 0f
                    }
                    scoreEntities.add(
                        ScoreEntity(
                            optionId = optIdx.toLong(),
                            criterionId = critIdx.toLong(),
                            scoreValue = valScore,
                            isPass = isPass
                        )
                    )
                }
            }

            val id = repository.saveFullDecision(decision, options, criteria, scoreEntities)

            if (s.gutPickIndex != null) {
                val finalOptions = repository.getOptions(id)
                if (s.gutPickIndex in finalOptions.indices) {
                    repository.updateGutPick(id, finalOptions[s.gutPickIndex].id)
                }
            }

            onSaved(id)
        }
    }
}
