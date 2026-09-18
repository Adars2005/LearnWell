package com.example.voice.engine

import com.example.voice.model.GrammarCorrection
import com.example.voice.model.LearnerWord
import com.example.voice.model.SpeakingEvaluation
import kotlin.math.roundToInt

class SpeakingEvaluationEngine {

    // Common English / general filler words
    private val fillerWordsSet = setOf(
        "um", "uh", "like", "you know", "ah", "er", "basically", "actually", "literally", "sort of", "kind of"
    )

    fun evaluateSession(
        userTranscripts: List<String>,
        grammarErrors: List<GrammarCorrection>,
        targetLanguage: String,
        newWords: List<LearnerWord> = emptyList()
    ): SpeakingEvaluation {
        val totalWords = userTranscripts.sumOf { text ->
            text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }.coerceAtLeast(1)

        // Count filler words
        var fillerCount = 0
        userTranscripts.forEach { text ->
            val lower = text.lowercase()
            fillerWordsSet.forEach { filler ->
                val occurrences = Regex("\\b$filler\\b").findAll(lower).count()
                fillerCount += occurrences
            }
        }

        // Fluency score: based on total words spoken, sentence length variety, and low filler ratio
        val fillerRatio = fillerCount.toFloat() / totalWords.toFloat()
        val fluencyScore = (90 - (fillerRatio * 150).roundToInt() + (totalWords / 10).coerceAtMost(10))
            .coerceIn(50, 98)

        // Grammar score: penalized by detected grammar errors
        val grammarScore = (95 - (grammarErrors.size * 8)).coerceIn(55, 98)

        // Vocabulary score: variety of words used
        val uniqueWords = userTranscripts.flatMap {
            it.lowercase().split(Regex("\\W+")).filter { w -> w.length > 3 }
        }.toSet()
        val vocabScore = (60 + (uniqueWords.size * 2)).coerceIn(60, 96)

        // Pronunciation proxy: baseline estimate
        val pronunciationScore = (85 - (fillerCount * 2)).coerceIn(70, 95)

        // Overall weighted score
        val overallScore = ((fluencyScore * 0.3f) + (grammarScore * 0.3f) + (vocabScore * 0.25f) + (pronunciationScore * 0.15f)).roundToInt()

        val recommendations = mutableListOf<String>()
        if (fillerCount > 2) {
            recommendations.add("Try taking a gentle 1-second pause rather than using filler words like '$fillerCount fillers used'.")
        }
        if (grammarErrors.isNotEmpty()) {
            recommendations.add("Review past tense verbs and subject-verb agreements from this session.")
        } else {
            recommendations.add("Great grammatical consistency! Continue expanding compound sentences.")
        }
        if (totalWords < 40) {
            recommendations.add("Challenge yourself to speak in full paragraphs with 'because', 'however', and 'for example'.")
        } else {
            recommendations.add("Strong conversational stamina! Your speaking volume was very healthy.")
        }

        val extractedVocabUsed = uniqueWords.take(8).toList()

        return SpeakingEvaluation(
            fluencyScore = fluencyScore,
            grammarScore = grammarScore,
            vocabularyScore = vocabScore,
            pronunciationScore = pronunciationScore,
            overallScore = overallScore,
            fillerWords = fillerCount,
            grammarErrors = grammarErrors,
            vocabularyUsed = extractedVocabUsed,
            newWords = newWords,
            pronunciationIssues = if (fillerCount > 3) listOf("Word pacing", "Sentence rhythm") else listOf("Intonation on questions"),
            recommendations = recommendations
        )
    }
}
