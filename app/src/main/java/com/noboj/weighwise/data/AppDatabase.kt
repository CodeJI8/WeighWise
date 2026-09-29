package com.noboj.weighwise.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DecisionEntity::class,
        OptionEntity::class,
        CriterionEntity::class,
        ScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao
}
