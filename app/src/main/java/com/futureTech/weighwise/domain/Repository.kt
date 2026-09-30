package com.futureTech.weighwise.domain

import com.futureTech.weighwise.data.*
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
        
        val optionsWithId = options.map { it.copy(id = 0, decisionId = decisionId) }
        val optionIds = decisionDao.insertOptions(optionsWithId)
        
        val criteriaWithId = criteria.map { it.copy(id = 0, decisionId = decisionId) }
        val criteriaIds = decisionDao.insertCriteria(criteriaWithId)
        
        // Map temporary option/criterion IDs or indices to newly generated database IDs
        val scoresWithId = scores.mapNotNull { score ->
            val optionIndex = options.indexOfFirst { it.id != 0L && it.id == score.optionId }
                .let { if (it != -1) it else score.optionId.toInt() }
            val criterionIndex = criteria.indexOfFirst { it.id != 0L && it.id == score.criterionId }
                .let { if (it != -1) it else score.criterionId.toInt() }
            
            if (optionIndex in optionIds.indices && criterionIndex in criteriaIds.indices) {
                score.copy(
                    optionId = optionIds[optionIndex],
                    criterionId = criteriaIds[criterionIndex]
                )
            } else {
                null
            }
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
