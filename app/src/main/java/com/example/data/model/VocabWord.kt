package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.math.pow

@Entity(
    tableName = "vocab_words",
    indices = [
        Index(value = ["language", "nextReviewTimestamp"])
    ]
)
data class VocabWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val language: String,            // "Hindi", "Spanish", "French", "Japanese", "German", "Italian", "Korean", "Mandarin"
    val word: String,                // e.g. "एक सेब", "किताब", "वह", "Hola", "Merci"
    val phonetic: String,            // e.g. "ek seb", "kitaab"
    val translation: String,         // e.g. "an apple", "book"
    val category: String,            // "Basics", "Food", "Animals", "Travel"
    val exampleSentence: String,     // Target language sentence
    val exampleTranslation: String,  // English translation
    val audioPrompt: String = "",    // Text for TTS
    val intervalDays: Int = 1,       // Spaced repetition interval (days)
    val easeFactor: Float = 2.5f,    // Legacy factor
    val stability: Float = 2.0f,     // FSRS-4.5 Stability (days)
    val difficulty: Float = 5.0f,    // FSRS-4.5 Difficulty (1.0 - 10.0)
    val level: String = "A1",        // CEFR: "A1", "A2", "B1"
    val repetitions: Int = 0,        // Number of consecutive successful reviews
    val lastReviewedTimestamp: Long = 0L,
    val nextReviewTimestamp: Long = 0L,
    val mistakeCount: Int = 0,
    val isMistake: Boolean = false,
    val totalReviews: Int = 0,
    val consecutiveCorrect: Int = 0,
    val teacherTip: String = ""      // Teacher Maya's mnemonic & cultural insight
) {
    val bloomStage: String
        get() = when {
            repetitions == 0 -> "Seed"
            intervalDays <= 2 -> "Sprout"
            intervalDays <= 7 -> "Flower"
            else -> "Full Bloom"
        }

    val bloomIcon: String
        get() = when {
            repetitions == 0 -> "🌱"
            intervalDays <= 2 -> "🌿"
            intervalDays <= 7 -> "🌸"
            else -> "🌺"
        }

    /**
     * Calculates estimated memory retention strength (0.0 to 1.0)
     * using the FSRS-4.5 Retrievability power curve:
     * R(t, S) = (1 + FACTOR * (t / S))^(-DECAY)
     * where FACTOR = 19/81 (~0.234567) and DECAY = 0.5
     */
    fun getMemoryStrength(currentTime: Long = System.currentTimeMillis()): Float {
        if (lastReviewedTimestamp == 0L) return 0f
        val elapsedDays = (currentTime - lastReviewedTimestamp).toFloat() / (1000f * 60f * 60f * 24f)
        val s = stability.coerceAtLeast(0.1f)
        val retrievability = (1.0f + 0.234567f * (elapsedDays / s)).pow(-0.5f)
        return retrievability.coerceIn(0.05f, 1.0f)
    }

    val isDueForReview: Boolean
        get() {
            if (lastReviewedTimestamp == 0L) return true
            return System.currentTimeMillis() >= nextReviewTimestamp
        }
}

