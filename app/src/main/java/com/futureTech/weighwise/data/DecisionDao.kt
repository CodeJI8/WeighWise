package com.futureTech.weighwise.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    @Insert
    suspend fun insertDecision(decision: DecisionEntity): Long

    @Insert
    suspend fun insertOptions(options: List<OptionEntity>): List<Long>

    @Insert
    suspend fun insertCriteria(criteria: List<CriterionEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<ScoreEntity>)

    @Query("SELECT * FROM decisions ORDER BY createdAt DESC")
    fun getAllDecisions(): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE id = :id")
    suspend fun getDecisionById(id: Long): DecisionEntity?

    @Query("SELECT * FROM options WHERE decisionId = :decisionId")
    suspend fun getOptionsForDecision(decisionId: Long): List<OptionEntity>

    @Query("SELECT * FROM criteria WHERE decisionId = :decisionId")
    suspend fun getCriteriaForDecision(decisionId: Long): List<CriterionEntity>

    @Query("SELECT * FROM scores WHERE optionId IN (SELECT id FROM options WHERE decisionId = :decisionId)")
    suspend fun getScoresForDecision(decisionId: Long): List<ScoreEntity>
    
    @Query("UPDATE decisions SET status = :status, finalOptionId = :finalOptionId WHERE id = :id")
    suspend fun updateDecisionStatus(id: Long, status: DecisionStatus, finalOptionId: Long?)
    
    @Query("UPDATE decisions SET gutPickOptionId = :gutPickId WHERE id = :id")
    suspend fun updateGutPick(id: Long, gutPickId: Long)
}
