package com.noboj.weighwise.domain

import com.noboj.weighwise.data.CriterionEntity
import com.noboj.weighwise.data.OptionEntity
import com.noboj.weighwise.data.ScoreEntity
import kotlin.math.pow
import kotlin.random.Random

data class OptionResult(
    val option: OptionEntity,
    val score: Float, // 0 to 100
    val isEliminated: Boolean,
    val eliminationReason: String? = null,
    val criterionScores: Map<Long, Float> // criterionId to weighted contribution (0-100)
)

class ScoreCalculator {
    fun calculate(
        options: List<OptionEntity>,
        criteria: List<CriterionEntity>,
        scores: List<ScoreEntity>
    ): List<OptionResult> {
        val results = mutableListOf<OptionResult>()
        
        // Normalize weights just in case
        val totalWeight = criteria.sumOf { it.weight.toDouble() }.toFloat()
        val normalizedCriteria = if (totalWeight > 0) {
            criteria.map { it.copy(weight = it.weight / totalWeight) }
        } else {
            criteria
        }
        
        val mustHaveCriteria = normalizedCriteria.filter { it.isMustHave }
        
        for (option in options) {
            val optionScores = scores.filter { it.optionId == option.id }
            
            // Check must-haves
            var eliminated = false
            var eliminationReason: String? = null
            for (mh in mustHaveCriteria) {
                val score = optionScores.find { it.criterionId == mh.id }
                if (score == null || !score.isPass) {
                    eliminated = true
                    eliminationReason = "Failed must-have: ${mh.name}"
                    break
                }
            }
            
            var totalScore = 0f
            val contributions = mutableMapOf<Long, Float>()
            
            if (!eliminated) {
                for (criterion in normalizedCriteria) {
                    val score = optionScores.find { it.criterionId == criterion.id }?.scoreValue ?: 0f
                    // score is 0-10, weight is 0-1. Max contribution for this criterion is weight * 100.
                    val contribution = (score / 10f) * criterion.weight * 100f
                    contributions[criterion.id] = contribution
                    totalScore += contribution
                }
            }
            
            results.add(OptionResult(option, totalScore, eliminated, eliminationReason, contributions))
        }
        
        return results.sortedByDescending { it.score }
    }
}

class AHPWeighter {
    // 1=Equal, 3=Slightly, 5=Clearly, 9=Strongly
    // Input is a matrix of pairwise comparisons or a list of answers.
    // To simplify, we can just use the geometric mean of the rows of the pairwise matrix.
    
    fun calculateWeights(matrix: Array<FloatArray>): FloatArray {
        val n = matrix.size
        if (n == 0) return FloatArray(0)
        
        val weights = FloatArray(n)
        var sumWeights = 0f
        
        for (i in 0 until n) {
            var product = 1.0f
            for (j in 0 until n) {
                product *= matrix[i][j]
            }
            weights[i] = product.pow(1.0f / n)
            sumWeights += weights[i]
        }
        
        for (i in 0 until n) {
            weights[i] /= sumWeights
        }
        
        return weights
    }
    
    fun calculateConsistencyRatio(matrix: Array<FloatArray>, weights: FloatArray): Float {
        val n = matrix.size
        if (n <= 2) return 0f // perfect consistency for n<=2
        
        var lambdaMax = 0f
        for (i in 0 until n) {
            var sum = 0f
            for (j in 0 until n) {
                sum += matrix[i][j] * weights[j]
            }
            lambdaMax += sum / weights[i]
        }
        lambdaMax /= n
        
        val ci = (lambdaMax - n) / (n - 1)
        val ri = getRandomIndex(n)
        
        if (ri == 0f) return 0f
        return ci / ri
    }
    
    private fun getRandomIndex(n: Int): Float {
        return when (n) {
            1, 2 -> 0f
            3 -> 0.58f
            4 -> 0.90f
            5 -> 1.12f
            6 -> 1.24f
            7 -> 1.32f
            8 -> 1.41f
            9 -> 1.45f
            10 -> 1.49f
            else -> 1.51f
        }
    }
}
