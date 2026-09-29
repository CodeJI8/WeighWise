package com.noboj.weighwise.di

import android.content.Context
import androidx.room.Room
import com.noboj.weighwise.data.AppDatabase
import com.noboj.weighwise.data.DecisionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "weighwise_db"
        ).build()
    }

    @Provides
    fun provideDecisionDao(database: AppDatabase): DecisionDao {
        return database.decisionDao()
    }
}
