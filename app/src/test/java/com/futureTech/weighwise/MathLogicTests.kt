package com.futureTech.weighwise

import com.futureTech.weighwise.data.CriterionEntity
import com.futureTech.weighwise.data.OptionEntity
import com.futureTech.weighwise.data.ScoreEntity
import com.futureTech.weighwise.domain.AHPWeighter
import com.futureTech.weighwise.domain.ScoreCalculator
import com.futureTech.weighwise.domain.SensitivityAnalyzer
import com.futureTech.weighwise.domain.Simulator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MathLogicTests {

    @Test
    fun testWeightNormalizationAndScoring() {
        val calc = ScoreCalculator()
        val opts = listOf(OptionEntity(1, 1, "A", 0), OptionEntity(2, 1, "B", 1))
        // Total weight is 10 (3+7). Normalizes to 0.3 and 0.7
        val crits = listOf(
            CriterionEntity(1, 1, "C1", 3f, false),
            CriterionEntity(2, 1, "C2", 7f, false)
        )
        val scores = listOf(
            ScoreEntity(1, 1, 10f, true), // A on C1: 10 * 0.3 = 30
            ScoreEntity(1, 2, 0f, true),  // A on C2: 0 * 0.7 = 0. Total = 30
            ScoreEntity(2, 1, 0f, true),  // B on C1: 0 * 0.3 = 0
            ScoreEntity(2, 2, 10f, true)  // B on C2: 10 * 0.7 = 70. Total = 70
        )
        
        val results = calc.calculate(opts, crits, scores)
        assertEquals(2, results.size)
        assertEquals("B", results[0].option.name) // Winner
        assertEquals(70f, results[0].score, 0.01f)
        assertEquals(30f, results[1].score, 0.01f)
    }

    @Test
    fun testDealBreakerElimination() {
        val calc = ScoreCalculator()
        val opts = listOf(OptionEntity(1, 1, "A", 0), OptionEntity(2, 1, "B", 1))
        val crits = listOf(
            CriterionEntity(1, 1, "C1", 10f, true) // Must have
        )
        val scores = listOf(
            ScoreEntity(1, 1, 8f, true),  // A passes
            ScoreEntity(2, 1, 9f, false)  // B fails
        )
        
        val results = calc.calculate(opts, crits, scores)
        val aResult = results.find { it.option.name == "A" }!!
        val bResult = results.find { it.option.name == "B" }!!
        
        assertFalse(aResult.isEliminated)
        assertTrue(bResult.isEliminated)
        assertEquals("Failed must-have: C1", bResult.eliminationReason)
    }
    
    @Test
    fun testTies() {
        val calc = ScoreCalculator()
        val opts = listOf(OptionEntity(1, 1, "A", 0), OptionEntity(2, 1, "B", 1))
        val crits = listOf(CriterionEntity(1, 1, "C1", 10f, false))
        val scores = listOf(
            ScoreEntity(1, 1, 5f, true),
            ScoreEntity(2, 1, 5f, true)
        )
        
        val results = calc.calculate(opts, crits, scores)
        assertEquals(50f, results[0].score, 0.01f)
        assertEquals(50f, results[1].score, 0.01f)
    }

    @Test
    fun testAHPWeighter() {
        val weighter = AHPWeighter()
        // 3x3 matrix where C1 is 3x more important than C2, C2 is 2x more important than C3
        val matrix = arrayOf(
            floatArrayOf(1f, 3f, 6f),
            floatArrayOf(1f/3f, 1f, 2f),
            floatArrayOf(1f/6f, 1f/2f, 1f)
        )
        val weights = weighter.calculateWeights(matrix)
        val cr = weighter.calculateConsistencyRatio(matrix, weights)
        
        assertEquals(3, weights.size)
        assertTrue(weights[0] > weights[1])
        assertTrue(weights[1] > weights[2])
        assertEquals(0f, cr, 0.01f) // perfectly consistent
    }

    @Test
    fun testSimulationDeterminism() = runBlocking {
        val sim = Simulator()
        val opts = listOf(OptionEntity(1, 1, "A", 0), OptionEntity(2, 1, "B", 1))
        val crits = listOf(CriterionEntity(1, 1, "C1", 5f, false), CriterionEntity(2, 1, "C2", 5f, false))
        val scores = listOf(
            ScoreEntity(1, 1, 8f, true), ScoreEntity(1, 2, 4f, true),
            ScoreEntity(2, 1, 4f, true), ScoreEntity(2, 2, 8f, true)
        )
        
        val result1 = sim.runSimulations(opts, crits, scores, iterations = 100, seed = 123L)
        val result2 = sim.runSimulations(opts, crits, scores, iterations = 100, seed = 123L)
        
        assertEquals(result1[1L], result2[1L])
        assertEquals(result1[2L], result2[2L])
    }
    
    @Test
    fun testSensitivityAnalyzer() = runBlocking {
        val analyzer = SensitivityAnalyzer()
        val opts = listOf(OptionEntity(1, 1, "A", 0), OptionEntity(2, 1, "B", 1))
        val crits = listOf(
            CriterionEntity(1, 1, "C1", 0.5f, false), // 50%
            CriterionEntity(2, 1, "C2", 0.5f, false)  // 50%
        )
        val scores = listOf(
            // A wins on C1 heavily, B wins on C2 barely
            ScoreEntity(1, 1, 10f, true), ScoreEntity(1, 2, 0f, true), // A = 50
            ScoreEntity(2, 1, 0f, true), ScoreEntity(2, 2, 8f, true)   // B = 40
        )
        // A is the winner
        
        val results = analyzer.analyze(opts, crits, scores, opts[0])
        
        // We expect C2 to be the most fragile, because increasing its weight helps B.
        // B needs to beat A. S_B = S_A -> 8 * W_2 = 10 * W_1
        // W_1 + W_2 = 1 (or whatever, weights are relative).
        // Since we are changing W_2 and keeping W_1 fixed for the threshold calc, 
        // W_2_new * 8 > 10 * 0.5 => W_2_new * 8 > 5 => W_2_new > 5/8 = 0.625
        // Current W_2 is 0.5. Change is +0.125.
        // 0.125 / 0.5 = +25% change needed in C2's weight.
        
        val c2Result = results.find { it.criterion.id == 2L }
        assertNotNull(c2Result)
        assertEquals(25f, c2Result!!.requiredWeightChangePercent, 1f)
    }
}
