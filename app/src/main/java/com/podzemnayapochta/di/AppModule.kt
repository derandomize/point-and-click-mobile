package com.podzemnayapochta.di

import android.content.Context
import com.podzemnayapochta.data.repository.AssetContentRepository
import com.podzemnayapochta.data.repository.DataStoreSaveManager
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.SaveManager
import com.podzemnayapochta.domain.usecase.DeliverLetter
import com.podzemnayapochta.domain.usecase.DialogueEngine
import com.podzemnayapochta.domain.usecase.MoveTo
import com.podzemnayapochta.domain.usecase.QuestEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Граф зависимостей приложения (см. docs/tooling.md — DI: Hilt).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Provides
    @Singleton
    fun provideContentRepository(
        @ApplicationContext context: Context,
        json: Json,
    ): ContentRepository = AssetContentRepository(context = context, json = json)

    @Provides
    @Singleton
    fun provideSaveManager(
        @ApplicationContext context: Context,
        json: Json,
    ): SaveManager = DataStoreSaveManager(context = context, json = json)

    @Provides
    fun provideDeliverLetter(): DeliverLetter = DeliverLetter()

    @Provides
    fun provideMoveTo(): MoveTo = MoveTo()

    @Provides
    fun provideQuestEngine(): QuestEngine = QuestEngine()

    @Provides
    fun provideDialogueEngine(): DialogueEngine = DialogueEngine()
}
