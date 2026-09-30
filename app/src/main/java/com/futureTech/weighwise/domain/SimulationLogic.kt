package com.futureTech.weighwise.domain

import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.data.ScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class Simulator(private val calculator: ScoreCalculator = ScoreCalculator()) {

    suspend fun runSimulations(
        options: List<OptionEntity>,
        criteria: List<CriterionEntity>,
        scores: List<ScoreEntity>,
        iterations: Int = 2000,
        seed: Long = System.currentTimeMillis()
    ): Map<Long, Float> = withContext(Dispatchers.Default) {
        val winCounts = mutableMapOf<Long, Int>()
        options.forEach { winCounts[it.id] = 0 }
        
        val random = Random(seed)
        
        for (i in 0 until iterations) {
            // Perturb weights by up to +/- 25%
            val perturbedCriteria = criteria.map { c ->
                if (c.isMustHave) {
                    c
                } else {
                    val perturbation = 1f + (random.nextFloat() * 0.5f - 0.25f) // 0.75 to 1.25
                    c.copy(weight = (c.weight * perturbation).coerceAtLeast(0.001f))
                }
            }
            
            val results = calculator.calculate(options, perturbedCriteria, scores)
            
            val validResults = results.filter { !it.isEliminated }
            if (validResults.isNotEmpty()) {
                val maxScore = validResults.first().score
                val winners = validResults.filter { it.score >= maxScore - 0.001f } // handle ties
                winners.forEach { 
                    winCounts[it.option.id] = (winCounts[it.option.id] ?: 0) + 1 
                }
            }
        }
        
        val totalWins = winCounts.values.sum().toFloat()
        winCounts.mapValues { if (totalWins > 0) it.value / totalWins else 0f }
    }
}

class SensitivityAnalyzer(private val calculator: ScoreCalculator = ScoreCalculator()) {

    data class SensitivityResult(
        val criterion: CriterionEntity,
        val requiredWeightChangePercent: Float, // +18% means it needs to be 18% higher
        val flippingOption: OptionEntity
    )

    suspend fun analyze(
        options: List<OptionEntity>,
        criteria: List<CriterionEntity>,
        scores: List<ScoreEntity>,
        currentWinner: OptionEntity
    ): List<SensitivityResult> = withContext(Dispatchers.Default) {
        val results = mutableListOf<SensitivityResult>()
        
        val validOptions = options.filter { opt -> 
            val isEliminated = criteria.filter { it.isMustHave }.any { mh ->
                val s = scores.find { it.optionId == opt.id && it.criterionId == mh.id }
                s == null || !s.isPass
            }
            !isEliminated
        }
        
        if (validOptions.size < 2) return@withContext emptyList()
        
        val mutableCriteria = criteria.filter { !it.isMustHave }.toMutableList()
        
        for (criterion in mutableCriteria) {
            // Binary search to find minimum weight change to dethrone currentWinner
            var low = 0f
            var high = 3.0f // up to 300% weight
            var bestChange: Float? = null
            var bestFlipper: OptionEntity? = null
            
            for (step in 0..15) { // ~0.0001 precision
                val mid = (low + high) / 2
                
                // Try applying mid as the new weight
                val testCriteria = mutableCriteria.map { if (it.id == criterion.id) it.copy(weight = mid) else it }
                val testResults = calculator.calculate(validOptions, testCriteria, scores)
                
                if (testResults.isNotEmpty() && testResults.first().option.id != currentWinner.id) {
                    bestChange = mid - criterion.weight
                    bestFlipper = testResults.first().option
                    // Try to find a smaller change
                    if (bestChange > 0) high = mid else low = mid
                } else {
                    // Winner didn't flip
                    if (mid > criterion.weight) low = mid else high = mid
                }
            }
            
            // We also need to search downwards if increasing it didn't work. 
            // Wait, binary search is monotonic? Not always. The derivative of score diff w.r.t weight is constant.
            // If the slope is negative, we need to decrease weight. If positive, increase.
            // A simpler algebraic approach is possible, but since we are off thread and 8x12 is small, 
            // just search both up and down linearly or grid search.
            
            // To be fast and accurate, let's just do a grid search to find the direction, then binary search.
        }
        
        // Simplified exact approach:
        for (criterion in mutableCriteria) {
            var minChangeToFlip = Float.MAX_VALUE
            var flipper: OptionEntity? = null
            
            for (challenger in validOptions) {
                if (challenger.id == currentWinner.id) continue
                
                val currentWinnerScoreAtC = scores.find { it.optionId == currentWinner.id && it.criterionId == criterion.id }?.scoreValue ?: 0f
                val challengerScoreAtC = scores.find { it.optionId == challenger.id && it.criterionId == criterion.id }?.scoreValue ?: 0f
                
                // If they score the same on this criterion, changing weight won't change their relative standing
                if (currentWinnerScoreAtC == challengerScoreAtC) continue
                
                // Let S_w be score of winner, S_c be score of challenger without this criterion
                var sWRest = 0f
                var sCRest = 0f
                for (other in mutableCriteria) {
                    if (other.id == criterion.id) continue
                    sWRest += (scores.find { it.optionId == currentWinner.id && it.criterionId == other.id }?.scoreValue ?: 0f) * other.weight
                    sCRest += (scores.find { it.optionId == challenger.id && it.criterionId == other.id }?.scoreValue ?: 0f) * other.weight
                }
                
                // We want: (sCRest + challengerScoreAtC * newWeight) > (sWRest + currentWinnerScoreAtC * newWeight)
                // newWeight * (challengerScoreAtC - currentWinnerScoreAtC) > sWRest - sCRest
                // newWeight > (sWRest - sCRest) / (challengerScoreAtC - currentWinnerScoreAtC) (if challengerScoreAtC > currentWinnerScoreAtC)
                
                val diffScore = challengerScoreAtC - currentWinnerScoreAtC
                val diffRest = sWRest - sCRest
                
                val newWeight = diffRest / diffScore
                
                if (newWeight >= 0) { // Weight can't be negative
                    val change = newWeight - criterion.weight
                    if (kotlin.math.abs(change) < kotlin.math.abs(minChangeToFlip)) {
                        minChangeToFlip = change
                        flipper = challenger
                    }
                }
            }
            
            if (flipper != null && minChangeToFlip != Float.MAX_VALUE) {
                val currentSum = mutableCriteria.sumOf { it.weight.toDouble() }.toFloat()
                // Convert absolute weight change to a percentage of current weight
                val percentChange = if (criterion.weight > 0) minChangeToFlip / criterion.weight else minChangeToFlip / currentSum
                results.add(SensitivityResult(criterion, percentChange * 100f, flipper))
            }
        }
        
        results.sortedBy { kotlin.math.abs(it.requiredWeightChangePercent) }
    }
}
