package com.example.voice.model

data class GrammarCorrection(
    val original: String,
    val correction: String,
    val errorType: String,
    val explanation: String
)

data class LearnerWord(
    val word: String,
    val meaning: String,
    val example: String,
    val difficulty: String = "A1",
    val status: String = "learning"
)

data class SpeakingEvaluation(
    val fluencyScore: Int = 0,
    val grammarScore: Int = 0,
    val vocabularyScore: Int = 0,
    val pronunciationScore: Int = 0,
    val overallScore: Int = 0,
    val fillerWords: Int = 0,
    val grammarErrors: List<GrammarCorrection> = emptyList(),
    val vocabularyUsed: List<String> = emptyList(),
    val newWords: List<LearnerWord> = emptyList(),
    val pronunciationIssues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class CompactLearnerProfile(
    val language: String = "English",
    val nativeLanguage: String = "Telugu",
    val level: String = "B1",
    val goals: List<String> = listOf("speaking", "interview"),
    val weakGrammar: List<String> = listOf("past_tense", "articles"),
    val weakPronunciation: List<String> = listOf("th"),
    val vocabularyLearning: List<String> = emptyList(),
    val vocabularyMastered: List<String> = emptyList(),
    val speakingScore: Int = 68,
    val commonErrors: List<String> = emptyList(),
    val preferredTopics: List<String> = listOf("travel", "technology", "career")
)

data class ChatMessage(
    val id: String,
    val role: String, // "user" or "assistant" or "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val audioUrl: String? = null,
    val grammarCorrection: GrammarCorrection? = null
)
