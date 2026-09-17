package com.example.voice.engine

import com.example.voice.model.CompactLearnerProfile
import com.example.voice.model.GrammarCorrection
import com.example.voice.model.LearnerWord
import com.example.voice.model.SpeakingEvaluation
import com.example.voice.model.VoiceMode

class LearningEngine(
    val grammarEngine: GrammarCorrectionEngine = GrammarCorrectionEngine(),
    val vocabEngine: VocabularyEngine = VocabularyEngine(),
    val interviewEngine: InterviewEngine = InterviewEngine(),
    val evaluationEngine: SpeakingEvaluationEngine = SpeakingEvaluationEngine()
) {

    fun checkGrammar(text: String, mode: VoiceMode): GrammarCorrection? {
        return grammarEngine.analyzeGrammar(text, mode)
    }

    fun extractVocabulary(targetLanguage: String, conversationHistory: String, level: String): List<LearnerWord> {
        return vocabEngine.extractVocabulary(targetLanguage, conversationHistory, level)
    }

    fun evaluateSpeakingSession(
        userTranscripts: List<String>,
        grammarErrors: List<GrammarCorrection>,
        targetLanguage: String,
        newWords: List<LearnerWord>
    ): SpeakingEvaluation {
        return evaluationEngine.evaluateSession(userTranscripts, grammarErrors, targetLanguage, newWords)
    }

    fun updateLearnerProfileAfterSession(
        currentProfile: CompactLearnerProfile,
        evaluation: SpeakingEvaluation
    ): CompactLearnerProfile {
        val updatedWeakGrammar = currentProfile.weakGrammar.toMutableSet()
        evaluation.grammarErrors.forEach { error ->
            updatedWeakGrammar.add(error.errorType)
        }

        val updatedVocabLearning = currentProfile.vocabularyLearning.toMutableSet()
        evaluation.newWords.forEach { word ->
            updatedVocabLearning.add(word.word)
        }

        // Rolling average of speaking score
        val newSpeakingScore = ((currentProfile.speakingScore * 0.7f) + (evaluation.overallScore * 0.3f)).toInt()

        return currentProfile.copy(
            weakGrammar = updatedWeakGrammar.toList(),
            vocabularyLearning = updatedVocabLearning.toList(),
            speakingScore = newSpeakingScore
        )
    }
}
