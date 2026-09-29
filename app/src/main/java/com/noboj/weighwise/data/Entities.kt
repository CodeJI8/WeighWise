package com.noboj.weighwise.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val status: DecisionStatus,
    val gutPickOptionId: Long? = null,
    val finalOptionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewDelayMs: Long? = null,
    val reviewedAt: Long? = null,
    val satisfaction: Int? = null,
    val reviewNote: String? = null
)

enum class DecisionStatus {
    DECIDING,
    DECIDED
}

@Entity(
    tableName = "options",
    foreignKeys = [
        ForeignKey(
            entity = DecisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["decisionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val decisionId: Long,
    val name: String,
    val colorIndex: Int
)

@Entity(
    tableName = "criteria",
    foreignKeys = [
        ForeignKey(
            entity = DecisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["decisionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CriterionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val decisionId: Long,
    val name: String,
    val weight: Float, // normalized 0.0 to 1.0 internally
    val isMustHave: Boolean
)

@Entity(
    tableName = "scores",
    primaryKeys = ["optionId", "criterionId"],
    foreignKeys = [
        ForeignKey(
            entity = OptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CriterionEntity::class,
            parentColumns = ["id"],
            childColumns = ["criterionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScoreEntity(
    val optionId: Long,
    val criterionId: Long,
    val scoreValue: Float, // 0 to 10
    val isPass: Boolean // for must-have
)
