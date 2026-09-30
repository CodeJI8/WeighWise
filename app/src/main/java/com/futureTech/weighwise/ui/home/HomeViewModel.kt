package com.futureTech.weighwise.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.data.ScoreEntity
import com.futureTech.weighwise.domain.WeighWiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: WeighWiseRepository
) : ViewModel() {

    val decisions: StateFlow<List<DecisionEntity>> = repository.getAllDecisions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    init {
        viewModelScope.launch {
            repository.getAllDecisions().collect { list ->
                if (list.isEmpty()) {
                    createDemoDecision(repository)
                }
            }
        }
    }
    
    private suspend fun createDemoDecision(repository: WeighWiseRepository) {
        val decision = DecisionEntity(title = "Job Offer", category = "Career", status = DecisionStatus.DECIDING)
        val opts = listOf(
            OptionEntity(decisionId=0, name="Startup X", colorIndex=0),
            OptionEntity(decisionId=0, name="Big Tech Y", colorIndex=1),
            OptionEntity(decisionId=0, name="Local Corp Z", colorIndex=2)
        )
        val crits = listOf(
            CriterionEntity(decisionId=0, name="Salary", weight=8f, isMustHave=false),
            CriterionEntity(decisionId=0, name="Growth", weight=9f, isMustHave=false),
            CriterionEntity(decisionId=0, name="Commute", weight=4f, isMustHave=false),
            CriterionEntity(decisionId=0, name="WLB", weight=7f, isMustHave=false)
        )
        // Dummy scores
        val scores = mutableListOf<ScoreEntity>()
        opts.forEachIndexed { o, opt ->
            crits.forEachIndexed { c, crit ->
                scores.add(ScoreEntity(o.toLong(), c.toLong(), (5..9).random().toFloat(), true))
            }
        }
        repository.saveFullDecision(decision, opts, crits, scores)
    }
}
