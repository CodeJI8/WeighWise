package com.futureTech.weighwise.ui.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.data.DecisionStatus
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.data.ScoreEntity
import com.futureTech.weighwise.domain.ScoreCalculator
import com.futureTech.weighwise.domain.WeighWiseRepository
import com.futureTech.weighwise.theme.OptionPalette
import com.futureTech.weighwise.ui.components.ConfidenceLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DecisionItemUiState(
    val decision: DecisionEntity,
    val options: List<OptionEntity>,
    val criteriaCount: Int,
    val optionColors: List<Color>,
    val confidenceLevel: ConfidenceLevel,
    val confidenceLabel: String,
    val leadingOptionName: String?,
    val isClearWin: Boolean,
    val isCloseCall: Boolean
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0 = Deciding, 1 = Decided
    val openCount: Int = 0,
    val clearWinCount: Int = 0,
    val closeCallCount: Int = 0,
    val heroSubtitle: String = "No open decisions",
    val decidingDecisions: List<DecisionItemUiState> = emptyList(),
    val decidedDecisions: List<DecisionItemUiState> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeighWiseRepository,
    private val scoreCalculator: ScoreCalculator
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)

    private val _decisionItems = repository.getAllDecisions().flatMapLatest { decisions ->
        flow {
            val items = decisions.map { decision ->
                val options = repository.getOptions(decision.id)
                val criteria = repository.getCriteria(decision.id)
                val scores = repository.getScores(decision.id)

                val results = scoreCalculator.calculate(options, criteria, scores)
                val winner = results.firstOrNull { !it.isEliminated }
                val second = results.drop(1).firstOrNull { !it.isEliminated }

                val diff = if (winner != null && second != null) winner.score - second.score else 100f
                val isClearWin = diff >= 15f
                val isCloseCall = !isClearWin

                val (confidenceLevel, confidenceLabel) = if (decision.status == DecisionStatus.DECIDING) {
                    if (winner != null) {
                        if (isClearWin) {
                            ConfidenceLevel.CLEAR_WIN to "Clear win: ${winner.option.name}"
                        } else if (diff >= 5f) {
                            ConfidenceLevel.CLOSE_CALL to "Close call: ${winner.option.name}"
                        } else {
                            ConfidenceLevel.TOSS_UP to "Toss-up: ${winner.option.name}"
                        }
                    } else {
                        ConfidenceLevel.TOSS_UP to "Toss-up"
                    }
                } else {
                    val finalOption = options.find { it.id == decision.finalOptionId } ?: winner?.option
                    val name = finalOption?.name ?: "Decided"
                    ConfidenceLevel.CLEAR_WIN to "Decided: $name"
                }

                val colors = options.map { OptionPalette[it.colorIndex % OptionPalette.size] }

                DecisionItemUiState(
                    decision = decision,
                    options = options,
                    criteriaCount = criteria.size,
                    optionColors = colors,
                    confidenceLevel = confidenceLevel,
                    confidenceLabel = confidenceLabel,
                    leadingOptionName = winner?.option?.name,
                    isClearWin = isClearWin,
                    isCloseCall = isCloseCall
                )
            }
            emit(items)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(_selectedTab, _decisionItems) { tab, items ->
        val openItems = items.filter { it.decision.status == DecisionStatus.DECIDING }
        val decidedItems = items.filter { it.decision.status == DecisionStatus.DECIDED }

        val openCount = openItems.size
        val clearWins = openItems.count { it.isClearWin }
        val closeCalls = openItems.count { it.isCloseCall }

        val subtitle = when {
            openCount == 0 -> "No open decisions"
            clearWins > 0 && closeCalls > 0 -> "$clearWins clear win, $closeCalls close call"
            clearWins > 0 -> "$clearWins clear win${if (clearWins > 1) "s" else ""}"
            else -> "$closeCalls close call${if (closeCalls > 1) "s" else ""}"
        }

        HomeUiState(
            isLoading = false,
            selectedTab = tab,
            openCount = openCount,
            clearWinCount = clearWins,
            closeCallCount = closeCalls,
            heroSubtitle = subtitle,
            decidingDecisions = openItems,
            decidedDecisions = decidedItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.getAllDecisions().collect { list ->
                if (list.isEmpty()) {
                    createDemoDecision(repository)
                }
            }
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    private suspend fun createDemoDecision(repository: WeighWiseRepository) {
        val decision = DecisionEntity(title = "Job offer", category = "Career", status = DecisionStatus.DECIDING)
        val opts = listOf(
            OptionEntity(decisionId = 0, name = "Offer A", colorIndex = 0),
            OptionEntity(decisionId = 0, name = "Offer B", colorIndex = 1),
            OptionEntity(decisionId = 0, name = "Offer C", colorIndex = 2)
        )
        val crits = listOf(
            CriterionEntity(decisionId = 0, name = "Salary", weight = 8f, isMustHave = false),
            CriterionEntity(decisionId = 0, name = "Growth", weight = 9f, isMustHave = false),
            CriterionEntity(decisionId = 0, name = "Commute", weight = 4f, isMustHave = false),
            CriterionEntity(decisionId = 0, name = "WLB", weight = 7f, isMustHave = false)
        )
        val scores = mutableListOf<ScoreEntity>()
        opts.forEachIndexed { o, _ ->
            crits.forEachIndexed { c, _ ->
                val valScore = when (o) {
                    0 -> 7f
                    1 -> 9f
                    else -> 5f
                }
                scores.add(ScoreEntity(o.toLong(), c.toLong(), valScore, true))
            }
        }
        repository.saveFullDecision(decision, opts, crits, scores)
    }
}
