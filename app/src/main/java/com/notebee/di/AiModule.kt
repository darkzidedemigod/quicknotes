package com.notebee.di

import com.google.ai.client.generativeai.GenerativeModel
import com.notebee.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // Changing to "gemini-1.5-flash-latest" or "gemini-pro" as "gemini-1.5-flash" 
        // returned a 404 in the current SDK version.
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}
