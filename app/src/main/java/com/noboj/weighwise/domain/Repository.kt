package com.noboj.weighwise.domain

import com.noboj.weighwise.data.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeighWiseRepository @Inject constructor(
    private val decisionDao: DecisionDao
) {
    fun getAllDecisions(): Flow<List<DecisionEntity>> = decisionDao.getAllDecisions()

    suspend fun getDecision(id: Long): DecisionEntity? = decisionDao.getDecisionById(id)
    suspend fun getOptions(decisionId: Long): List<OptionEntity> = decisionDao.getOptionsForDecision(decisionId)
    suspend fun getCriteria(decisionId: Long): List<CriterionEntity> = decisionDao.getCriteriaForDecision(decisionId)
    suspend fun getScores(decisionId: Long): List<ScoreEntity> = decisionDao.getScoresForDecision(decisionId)

    suspend fun saveFullDecision(
        decision: DecisionEntity,
        options: List<OptionEntity>,
        criteria: List<CriterionEntity>,
        scores: List<ScoreEntity>
    ): Long {
        // Since we need the generated IDs, we insert them sequentially
        val decisionId = decisionDao.insertDecision(decision)
        
        val optionsWithId = options.map { it.copy(decisionId = decisionId) }
        val optionIds = decisionDao.insertOptions(optionsWithId)
        
        val criteriaWithId = criteria.map { it.copy(decisionId = decisionId) }
        val criteriaIds = decisionDao.insertCriteria(criteriaWithId)
        
        // Map old temporary IDs to new generated IDs for scores
        // We assume temporary IDs are negative or we just use indices if we created them in memory
        val scoresWithId = scores.map { score ->
            val optionIndex = options.indexOfFirst { it.id == score.optionId }
            val criterionIndex = criteria.indexOfFirst { it.id == score.criterionId }
            
            score.copy(
                optionId = optionIds[optionIndex],
                criterionId = criteriaIds[criterionIndex]
            )
        }
        
        decisionDao.insertScores(scoresWithId)
        return decisionId
    }
    
    suspend fun updateGutPick(decisionId: Long, optionId: Long) {
        decisionDao.updateGutPick(decisionId, optionId)
    }

    suspend fun markDecided(decisionId: Long, finalOptionId: Long) {
        decisionDao.updateDecisionStatus(decisionId, DecisionStatus.DECIDED, finalOptionId)
    }
}
