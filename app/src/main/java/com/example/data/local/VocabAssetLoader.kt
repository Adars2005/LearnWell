package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.model.VocabWord
import org.json.JSONObject

object VocabAssetLoader {

    private const val TAG = "VocabAssetLoader"

    fun loadInitialDecks(context: Context): List<VocabWord> {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("vocab/decks_v1.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val decksArray = root.optJSONArray("decks")

            if (decksArray != null && decksArray.length() > 0) {
                val assetWords = mutableListOf<VocabWord>()
                for (i in 0 until decksArray.length()) {
                    val obj = decksArray.getJSONObject(i)
                    assetWords.add(
                        VocabWord(
                            language = obj.optString("language", "Hindi"),
                            word = obj.optString("word", ""),
                            phonetic = obj.optString("phonetic", ""),
                            translation = obj.optString("translation", ""),
                            category = obj.optString("category", "Basics"),
                            exampleSentence = obj.optString("exampleSentence", ""),
                            exampleTranslation = obj.optString("exampleTranslation", ""),
                            audioPrompt = obj.optString("audioPrompt", ""),
                            level = obj.optString("level", "A1"),
                            teacherTip = obj.optString("teacherTip", "")
                        )
                    )
                }
                // Merge with bundled rich datasets to guarantee comprehensive vocabulary coverage
                val bundledWords = InitialVocabData.getInitialWords()
                val combined = (assetWords + bundledWords).distinctBy { "${it.language}_${it.word}" }
                Log.d(TAG, "Loaded ${combined.size} words from assets + bundled dataset.")
                return combined
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load JSON asset decks, using bundled fallback", e)
        }

        return InitialVocabData.getInitialWords()
    }
}
