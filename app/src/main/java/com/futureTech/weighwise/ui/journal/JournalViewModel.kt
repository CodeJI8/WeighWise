package com.futureTech.weighwise.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.domain.ScoreCalculator
import com.futureTech.weighwise.domain.WeighWiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalItemUiState(
    val decision: DecisionEntity,
    val formattedDate: String,
    val isReadyToReview: Boolean,
    val satisfactionRating: Int?,
    val finalOptionName: String?
)

data class JournalUiState(
    val isLoading: Boolean = true,
    val gutMatchedCount: Int = 0,
    val totalGutCount: Int = 0,
    val averageSatisfaction: Float? = null,
    val journalItems: List<JournalItemUiState> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: WeighWiseRepository,
    private val scoreCalculator: ScoreCalculator
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = repository.getAllDecisions().flatMapLatest { decisions ->
        flow {
            var gutMatched = 0
            var totalGut = 0
            val satisfactions = mutableListOf<Int>()
            val items = mutableListOf<JournalItemUiState>()

            val now = System.currentTimeMillis()

            for (decision in decisions) {
                val options = repository.getOptions(decision.id)
                val criteria = repository.getCriteria(decision.id)
                val scores = repository.getScores(decision.id)

                val results = scoreCalculator.calculate(options, criteria, scores)
                val topWinner = results.firstOrNull { !it.isEliminated }

                // Check gut pick match
                if (decision.gutPickOptionId != null) {
                    totalGut++
                    val gutOption = options.find { it.id == decision.gutPickOptionId }
                    if (gutOption != null && topWinner != null && gutOption.id == topWinner.option.id) {
                        gutMatched++
                    }
                }

                // Check satisfaction
                if (decision.satisfaction != null) {
                    satisfactions.add(decision.satisfaction)
                }

                val isDelayPassed = if (decision.reviewDelayMs != null) {
                    now >= (decision.createdAt + decision.reviewDelayMs)
                } else true

                val isReadyToReview = decision.reviewedAt == null && isDelayPassed && decision.satisfaction == null

                val finalOpt = options.find { it.id == decision.finalOptionId } ?: topWinner?.option
                val formattedDate = formatRelativeDate(decision.createdAt)

                items.add(
                    JournalItemUiState(
                        decision = decision,
                        formattedDate = formattedDate,
                        isReadyToReview = isReadyToReview,
                        satisfactionRating = decision.satisfaction,
                        finalOptionName = finalOpt?.name
                    )
                )
            }

            val avgSat = if (satisfactions.isNotEmpty()) satisfactions.average().toFloat() else null

            emit(
                JournalUiState(
                    isLoading = false,
                    gutMatchedCount = gutMatched,
                    totalGutCount = if (totalGut > 0) totalGut else items.size,
                    averageSatisfaction = avgSat,
                    journalItems = items
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState(isLoading = true)
    )

    fun saveReview(decisionId: Long, satisfaction: Int, note: String?) {
        viewModelScope.launch {
            repository.saveReview(decisionId, satisfaction, note)
        }
    }

    private fun formatRelativeDate(timestamp: Long): String {
        val diffMs = (System.currentTimeMillis() - timestamp).coerceAtLeast(0)
        val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        val weeks = days / 7
        val months = days / 30

        return when {
            months >= 1 -> "Decided $months month${if (months > 1) "s" else ""} ago"
            weeks >= 1 -> "Decided $weeks week${if (weeks > 1) "s" else ""} ago"
            days >= 1 -> "Decided $days day${if (days > 1) "s" else ""} ago"
            else -> "Decided today"
        }
    }
}
