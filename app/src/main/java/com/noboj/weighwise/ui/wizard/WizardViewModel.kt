package com.noboj.weighwise.ui.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noboj.weighwise.data.*
import com.noboj.weighwise.domain.AHPWeighter
import com.noboj.weighwise.domain.WeighWiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WizardState(
    val title: String = "",
    val category: String = "Other",
    val options: List<String> = listOf("Option A", "Option B"),
    val criteria: List<CriterionInput> = listOf(CriterionInput("Cost", 5f, false)),
    val scores: Map<Pair<Int, Int>, Float> = emptyMap(), // OptionIndex to CriterionIndex -> Score
    val gutPickIndex: Int? = null,
    val currentStep: Int = 0
)

data class CriterionInput(val name: String, val weight: Float, val isMustHave: Boolean)

@HiltViewModel
class WizardViewModel @Inject constructor(
    private val repository: WeighWiseRepository,
    private val ahpWeighter: AHPWeighter
) : ViewModel() {

    private val _state = MutableStateFlow(WizardState())
    val state = _state.asStateFlow()

    fun updateTitle(title: String) { _state.update { it.copy(title = title) } }
    
    fun updateCategory(category: String) { _state.update { it.copy(category = category) } }
    
    fun updateOption(index: Int, name: String) {
        _state.update {
            val newOptions = it.options.toMutableList()
            newOptions[index] = name
            it.copy(options = newOptions)
        }
    }
    
    fun addOption() {
        _state.update { it.copy(options = it.options + "New Option") }
    }
    
    fun updateCriterion(index: Int, input: CriterionInput) {
        _state.update {
            val newC = it.criteria.toMutableList()
            newC[index] = input
            it.copy(criteria = newC)
        }
    }
    
    fun addCriterion() {
        _state.update { it.copy(criteria = it.criteria + CriterionInput("New", 5f, false)) }
    }
    
    fun updateScore(optionIndex: Int, criterionIndex: Int, score: Float) {
        _state.update {
            val newScores = it.scores.toMutableMap()
            newScores[optionIndex to criterionIndex] = score
            it.copy(scores = newScores)
        }
    }
    
    fun setGutPick(index: Int) {
        _state.update { it.copy(gutPickIndex = index) }
    }
    
    fun nextStep() {
        _state.update { it.copy(currentStep = it.currentStep + 1) }
    }
    
    fun prevStep() {
        _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun saveDecision(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            val decision = DecisionEntity(
                title = s.title.ifEmpty { "Untitled" },
                category = s.category,
                status = DecisionStatus.DECIDING
            )
            
            val options = s.options.mapIndexed { i, name ->
                OptionEntity(decisionId = 0, name = name, colorIndex = i % 8)
            }
            
            val criteria = s.criteria.map {
                CriterionEntity(decisionId = 0, name = it.name, weight = it.weight, isMustHave = it.isMustHave)
            }
            
            val scoreEntities = mutableListOf<ScoreEntity>()
            s.options.forEachIndexed { optIdx, _ ->
                s.criteria.forEachIndexed { critIdx, crit ->
                    val value = s.scores[optIdx to critIdx] ?: 5f
                    scoreEntities.add(
                        ScoreEntity(
                            optionId = optIdx.toLong(), // Temp IDs
                            criterionId = critIdx.toLong(),
                            scoreValue = value,
                            isPass = value >= 5f // Simple logic for must-have
                        )
                    )
                }
            }
            
            val id = repository.saveFullDecision(decision, options, criteria, scoreEntities)
            
            if (s.gutPickIndex != null) {
                val finalOptions = repository.getOptions(id)
                repository.updateGutPick(id, finalOptions[s.gutPickIndex].id)
            }
            
            onSaved(id)
        }
    }
}
