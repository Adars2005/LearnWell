package com.example.voice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_sessions")
data class VoiceSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val mode: String,
    val subMode: String = "",
    val language: String,
    val level: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val durationSeconds: Int = 0,
    val audioDurationSeconds: Int = 0,
    val requestCount: Int = 0,
    val provider: String = "gemini-native-audio",
    val status: String = "COMPLETED"
)

@Entity(tableName = "conversation_messages")
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val role: String, // "user", "assistant", "system"
    val text: String,
    val timestamp: Long,
    val audioPath: String? = null,
    val correctionJson: String? = null
)

@Entity(tableName = "conversation_summaries")
data class ConversationSummaryEntity(
    @PrimaryKey
    val sessionId: String,
    val summaryText: String,
    val keyLearnings: String,
    val timestamp: Long
)

@Entity(tableName = "learner_profiles")
data class LearnerProfileEntity(
    @PrimaryKey
    val userId: String = "local_user",
    val language: String = "English",
    val nativeLanguage: String = "Telugu",
    val level: String = "B1",
    val goalsJson: String = "[\"speaking\",\"interview\"]",
    val weakGrammarJson: String = "[\"past_tense\",\"articles\"]",
    val weakPronunciationJson: String = "[\"th\"]",
    val vocabularyLearningJson: String = "[]",
    val vocabularyMasteredJson: String = "[]",
    val speakingScore: Int = 68,
    val commonErrorsJson: String = "[]",
    val preferredTopicsJson: String = "[\"travel\",\"technology\",\"career\"]",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "grammar_errors")
data class GrammarErrorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val originalText: String,
    val correctedText: String,
    val errorType: String,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "speaking_evaluations")
data class SpeakingEvaluationEntity(
    @PrimaryKey
    val sessionId: String,
    val fluencyScore: Int,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val pronunciationScore: Int,
    val overallScore: Int,
    val fillerWords: Int,
    val grammarErrorsJson: String,
    val vocabularyUsedJson: String,
    val newWordsJson: String,
    val recommendationsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val interviewType: String,
    val difficulty: String,
    val targetRole: String,
    val questionCount: Int = 0,
    val isCompleted: Boolean = false,
    val score: Int = 0
)

@Entity(tableName = "interview_answers")
data class InterviewAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val questionNumber: Int,
    val question: String,
    val answer: String,
    val feedback: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_usage_logs")
data class VoiceUsageLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String,
    val provider: String,
    val mode: String,
    val audioDurationSec: Int,
    val aiDurationSec: Int,
    val requestCount: Int,
    val estimatedTokens: Int,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
