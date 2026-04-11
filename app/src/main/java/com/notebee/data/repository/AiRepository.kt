package com.notebee.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AiRepository"

@Singleton
class AiRepository @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    suspend fun suggestTags(title: String, content: String): List<String> {
        Log.d(TAG, "suggestTags called with title: $title")
        val prompt = """
            Analyze the following note and suggest up to 5 relevant tags. 
            Respond only with the tags separated by commas, no other text.
            
            Title: $title
            Content: $content
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val tags = response.text?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
            Log.d(TAG, "suggestTags success: $tags")
            tags
        } catch (e: Exception) {
            Log.e(TAG, "suggestTags error", e)
            emptyList()
        }
    }

    suspend fun generateTitle(content: String): String? {
        Log.d(TAG, "generateTitle called")
        val prompt = """
            Generate a concise and descriptive title (max 6 words) for the following note content.
            Respond only with the title, no quotes or other text.
            
            Content: $content
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val title = response.text?.trim()
            Log.d(TAG, "generateTitle success: $title")
            title
        } catch (e: Exception) {
            Log.e(TAG, "generateTitle error", e)
            null
        }
    }
}
