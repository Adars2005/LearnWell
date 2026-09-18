package com.example.data.srs

import com.example.data.model.VocabWord
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Free Spaced Repetition Scheduler (FSRS-4.5) Engine.
 * Replaces SM-2 with modern Difficulty / Stability / Retrievability model.
 * Preserves full compatibility with existing callers while adding 4-button grading.
 */
enum class FsrsRating(val value: Int, val label: String) {
    AGAIN(1, "Again"),
    HARD(2, "Hard"),
    GOOD(3, "Good"),
    EASY(4, "Easy")
}

object SpacedRepetitionEngine {

    const val DEFAULT_DESIRED_RETENTION = 0.90f
    const val DECAY = 0.5f
    const val FACTOR = 0.234567f // (19/81)

    // FSRS 4.5 Standard Weight Parameters
    private val W = floatArrayOf(
        0.40255f, 1.18385f, 3.173f, 15.69105f, // w0-w3: Initial stabilities for [Again, Hard, Good, Easy]
        7.1949f, 0.5345f, 0.9604f, 0.046f,     // w4-w7: Difficulty init & update
        1.54575f, 0.1192f, 1.01925f,           // w8-w10: Stability recall update
        1.9395f, 0.11f, 0.29605f, 2.2698f,     // w11-w14: Stability lapse update
        0.2315f, 2.9898f                       // w15-w16: Hard penalty & Easy bonus
    )

    /**
     * Backward-compatible review processor for boolean answers.
     */
    fun processReview(
        word: VocabWord,
        isCorrect: Boolean,
        isFastRecall: Boolean = false,
        currentTime: Long = System.currentTimeMillis()
    ): VocabWord {
        val rating = when {
            !isCorrect -> FsrsRating.AGAIN
            isFastRecall -> FsrsRating.EASY
            else -> FsrsRating.GOOD
        }
        return processRating(word, rating, currentTime)
    }

    /**
     * Full 4-button FSRS-4.5 rating processor.
     */
    fun processRating(
        word: VocabWord,
        rating: FsrsRating,
        currentTime: Long = System.currentTimeMillis(),
        desiredRetention: Float = DEFAULT_DESIRED_RETENTION
    ): VocabWord {
        val lastTime = word.lastReviewedTimestamp
        val elapsedDays = if (lastTime > 0L) {
            max(0.1f, (currentTime - lastTime).toFloat() / (24f * 60f * 60f * 1000f))
        } else {
            0f
        }

        val currentD = word.difficulty.coerceIn(1.0f, 10.0f)
        val currentS = word.stability.coerceAtLeast(0.1f)

        val newD: Float
        val newS: Float
        val newReps: Int
        val isMistake = (rating == FsrsRating.AGAIN)

        if (word.repetitions == 0 || lastTime == 0L) {
            // First time learning word
            newS = W[rating.value - 1]
            newD = (W[4] - (rating.value - 3) * W[5]).coerceIn(1.0f, 10.0f)
            newReps = if (isMistake) 0 else 1
        } else {
            // Retrievability right before review
            val r = calculateRetrievability(elapsedDays, currentS)

            // Difficulty update with mean reversion
            val dTarget = currentD - W[6] * (rating.value - 3)
            newD = (W[7] * 5.0f + (1f - W[7]) * dTarget).coerceIn(1.0f, 10.0f)

            if (rating == FsrsRating.AGAIN) {
                // Lapse: S' = w11 * D^(-w12) * ((S+1)^w13 - 1) * e^(w14 * (1 - R))
                val lapseS = W[11] * newD.pow(-W[12]) * ((currentS + 1.0f).pow(W[13]) - 1.0f) * exp(W[14] * (1.0f - r))
                newS = lapseS.coerceIn(0.1f, currentS)
                newReps = 0
            } else {
                // Recall: Hard / Good / Easy
                val hardPenalty = if (rating == FsrsRating.HARD) W[15] else 1.0f
                val easyBonus = if (rating == FsrsRating.EASY) W[16] else 1.0f

                val recallMultiplier = 1.0f + exp(W[8]) *
                        (11.0f - newD) *
                        currentS.pow(-W[9]) *
                        (exp(W[10] * (1.0f - r)) - 1.0f) *
                        hardPenalty * easyBonus

                val updatedS = currentS * recallMultiplier
                newS = max(currentS + 0.1f, updatedS)
                newReps = word.repetitions + 1
            }
        }

        val intervalDays = nextIntervalDays(newS, desiredRetention)
        val nextReviewTime = currentTime + (intervalDays.toLong() * 24L * 60L * 60L * 1000L)

        return word.copy(
            intervalDays = intervalDays,
            stability = newS,
            difficulty = newD,
            repetitions = newReps,
            lastReviewedTimestamp = currentTime,
            nextReviewTimestamp = nextReviewTime,
            mistakeCount = if (isMistake) word.mistakeCount + 1 else word.mistakeCount,
            isMistake = isMistake,
            totalReviews = word.totalReviews + 1,
            consecutiveCorrect = if (!isMistake) word.consecutiveCorrect + 1 else 0
        )
    }

    /**
     * Calculates retrievability probability R in [0, 1]
     */
    fun calculateRetrievability(elapsedDays: Float, stability: Float): Float {
        if (stability <= 0f) return 0f
        return (1.0f + FACTOR * (elapsedDays / stability)).pow(-DECAY).coerceIn(0.01f, 1.0f)
    }

    /**
     * Calculates the optimal next interval in days for a target desired retention.
     * At desiredRetention = 0.90, interval ~ stability.
     */
    fun nextIntervalDays(stability: Float, desiredRetention: Float = DEFAULT_DESIRED_RETENTION): Int {
        val targetR = desiredRetention.coerceIn(0.70f, 0.98f)
        val interval = (stability / FACTOR) * (targetR.pow(-1.0f / DECAY) - 1.0f)
        return max(1, interval.roundToInt())
    }

    /**
     * Human-readable label for the word's retention state.
     */
    fun getStageLabel(intervalDays: Int, repetitions: Int): String {
        return when {
            repetitions == 0 -> "New Seed 🌱"
            intervalDays <= 2 -> "Sprout (1-2d) 🌿"
            intervalDays <= 7 -> "Flower (3-7d) 🌸"
            else -> "Golden Bloom (${intervalDays}d) 🌺"
        }
    }
}
