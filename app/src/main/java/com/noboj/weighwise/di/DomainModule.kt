package com.noboj.weighwise.di

import com.noboj.weighwise.domain.AHPWeighter
import com.noboj.weighwise.domain.ScoreCalculator
import com.noboj.weighwise.domain.SensitivityAnalyzer
import com.noboj.weighwise.domain.Simulator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideScoreCalculator(): ScoreCalculator = ScoreCalculator()

    @Provides
    @Singleton
    fun provideAHPWeighter(): AHPWeighter = AHPWeighter()

    @Provides
    @Singleton
    fun provideSimulator(calculator: ScoreCalculator): Simulator = Simulator(calculator)

    @Provides
    @Singleton
    fun provideSensitivityAnalyzer(calculator: ScoreCalculator): SensitivityAnalyzer = SensitivityAnalyzer(calculator)
}
